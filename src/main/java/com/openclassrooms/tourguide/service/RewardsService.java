package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

/**
 * Computes and assigns rewards to users based on their visited locations.
 * Batch methods parallelize work across a fixed thread pool to handle
 * I/O-bound calls to external libraries concurrently.
 */
@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;
    /**
     * Fixed pool of 500 threads: reward-point lookups are I/O-bound (slow external
     * lib), so threads spend most of their time waiting — over-provisioning beyond
     * core count keeps throughput high.
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(500);

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Parallelizes {@link #calculateRewards(User)} across all users using
     * CompletableFuture and the fixed thread pool; blocks until every task completes.
     *
     * @param users the full list of users to process
     */
    public void calculateAllRewards(List<User> users) {
        List<CompletableFuture<Void>> futures = users.stream()
                .map(user -> CompletableFuture.runAsync(() -> calculateRewards(user), executorService))
                .collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * Assigns reward points for every attraction the user has visited and not yet been rewarded for.
     * Both lists are snapshotted defensively at entry ({@code new ArrayList<>}) because the Tracker
     * may concurrently append to {@code visitedLocations} or {@code userRewards} on the same user.
     * The duplicate check here is a best-effort optimisation; the atomic uniqueness guarantee
     * lives in {@link com.openclassrooms.tourguide.user.User#addUserReward(UserReward)}.
     */
    public void calculateRewards(User user) {
        List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());
        List<Attraction> attractions = gpsUtil.getAttractions();

        for (VisitedLocation visitedLocation : userLocations) {
            for (Attraction attraction : attractions) {
                boolean alreadyRewarded = user.getUserRewards().stream()
                        .anyMatch(r -> r.attraction.attractionName.equals(attraction.attractionName));
                if (!alreadyRewarded && nearAttraction(visitedLocation, attraction)) {
                    user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                }
            }
        }
    }

    /**
     * Shuts down the executor service pool on Spring bean destruction to prevent thread leaks.
     */
    @PreDestroy
    public void shutdownExecutorService() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }

    /**
     * Returns the reward points for visiting an attraction. Delegates to the external
     * RewardCentral library, which is slow (I/O-bound) and may block the calling thread.
     */
    public int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    /**
     * Returns the great-circle distance in statute miles between two locations.
     *
     * @return distance in miles
     */
    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }

}

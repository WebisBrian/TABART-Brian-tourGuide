package com.openclassrooms.tourguide.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;

/**
 * In-memory user model holding GPS history, earned rewards, trip preferences,
 * and trip deal offers.
 */
public class User {
    private final UUID userId;
    private final String userName;
    private String phoneNumber;
    private String emailAddress;
    private Date latestLocationTimestamp;
    private List<VisitedLocation> visitedLocations = new ArrayList<>();
    private List<UserReward> userRewards = new ArrayList<>();
    private UserPreferences userPreferences = new UserPreferences();
    private List<Provider> tripDeals = new ArrayList<>();

    public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
        this.latestLocationTimestamp = latestLocationTimestamp;
    }

    public Date getLatestLocationTimestamp() {
        return latestLocationTimestamp;
    }

    public void addToVisitedLocations(VisitedLocation visitedLocation) {
        visitedLocations.add(visitedLocation);
    }

    /**
     * Returns the raw internal list. Concurrent readers (e.g. the Tracker and reward
     * batch) must immediately snapshot it ({@code new ArrayList<>(...)}) to avoid
     * seeing mid-iteration structural changes.
     */
    public List<VisitedLocation> getVisitedLocations() {
        return visitedLocations;
    }

    public void clearVisitedLocations() {
        visitedLocations.clear();
    }

    /**
     * Adds a reward only if no reward for the same attraction already exists.
     * {@code synchronized} is mandatory: the Tracker and the reward batch call this
     * concurrently on the same User instance, so the check-then-add must be atomic
     * to prevent duplicate rewards and races on the non-thread-safe list.
     */
    public synchronized void addUserReward(UserReward userReward) {
        if (userRewards.stream().anyMatch(r -> r.attraction.attractionName.equals(userReward.attraction.attractionName))) {
            return;
        }
        userRewards.add(userReward);
    }

    /**
     * Returns the raw internal list. Concurrent readers must snapshot it
     * ({@code new ArrayList<>(...)}) before iterating.
     */
    public List<UserReward> getUserRewards() {
        return userRewards;
    }

    public UserPreferences getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    public VisitedLocation getLastVisitedLocation() {
        return visitedLocations.get(visitedLocations.size() - 1);
    }

    public void setTripDeals(List<Provider> tripDeals) {
        this.tripDeals = tripDeals;
    }

    public List<Provider> getTripDeals() {
        return tripDeals;
    }

}

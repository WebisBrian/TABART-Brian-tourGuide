package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

/**
 * Background thread that periodically triggers location tracking for all users.
 * Submitted to a single-thread executor at construction; runs until
 * interrupted or {@link #stopTracking()} is called.
 */
public class Tracker extends Thread {
    private Logger logger = LoggerFactory.getLogger(Tracker.class);
    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final TourGuideService tourGuideService;
    private boolean stop = false;

    public Tracker(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;

        executorService.submit(this);
    }

    /**
     * Signals the tracking loop to stop and shuts down the executor, interrupting
     * any in-progress sleep so the thread exits promptly.
     */
    public void stopTracking() {
        stop = true;
        executorService.shutdownNow();
    }

    /**
     * Tracking loop: calls {@code trackAllUsersLocation} then sleeps 5 minutes,
     * repeating until the thread is interrupted or {@link #stopTracking()} is called.
     */
    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();
        while (true) {
            if (Thread.currentThread().isInterrupted() || stop) {
                logger.debug("Tracker stopping");
                break;
            }

            List<User> users = tourGuideService.getAllUsers();
            logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
            stopWatch.start();
            tourGuideService.trackAllUsersLocation(users);
            stopWatch.stop();
            logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
            stopWatch.reset();
            try {
                logger.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch (InterruptedException e) {
                break;
            }
        }

    }
}

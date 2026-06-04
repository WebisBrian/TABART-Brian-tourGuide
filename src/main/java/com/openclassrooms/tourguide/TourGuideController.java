package com.openclassrooms.tourguide;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.dto.NearbyAttractionDTO;
import com.openclassrooms.tourguide.exception.UserNotFoundException;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import tripPricer.Provider;

/**
 * REST controller exposing the TourGuide HTTP API.
 */
@RestController
public class TourGuideController {

    private static final Logger logger = LoggerFactory.getLogger(TourGuideController.class);

    private final TourGuideService tourGuideService;

    public TourGuideController(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;
    }

    /** Returns a welcome message. */
    @GetMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * Returns the current or last known location of the user.
     *
     * @param userName the user's name
     */
    @GetMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam String userName) {
        return tourGuideService.getUserLocation(getUser(userName));
    }

    /**
     * Returns the 5 nearest attractions to the user's current location, with
     * distance and reward points.
     *
     * @param userName the user's name
     */
    @GetMapping("/getNearbyAttractions")
    public List<NearbyAttractionDTO> getNearbyAttractions(@RequestParam String userName) {
        User user = getUser(userName);
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
        return tourGuideService.getNearByAttractionsDTO(visitedLocation, user);
    }

    /**
     * Returns all rewards earned by the user.
     *
     * @param userName the user's name
     */
    @GetMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        return tourGuideService.getUserRewards(getUser(userName));
    }

    /**
     * Returns trip deal offers based on the user's preferences and accumulated reward points.
     *
     * @param userName the user's name
     */
    @GetMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        return tourGuideService.getTripDeals(getUser(userName));
    }

    private User getUser(String userName) {
        User user = tourGuideService.getUser(userName);
        if (user == null) {
            logger.warn("User not found: {}", userName);
            throw new UserNotFoundException(userName);
        }
        return user;
    }
}
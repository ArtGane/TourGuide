package tourGuide.controller;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.service.GpsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tripPricer.Provider;

import java.util.*;

@RestController
public class TourGuideController {

    @Autowired
    TourGuideService tourGuideService;

    @Autowired
    AttractionService attractionService;

    @Autowired
    GpsService gpsService;

    @Autowired
    UserService userService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public Location getLocation(@RequestParam String userName) {
        return gpsService.getUserLocation(userService.getUserByName(userName));
    }

    @GetMapping("/getNearbyAttractions")
    public List<NearbyAttractionDto> getNearbyAttractions(@RequestParam String userName) {
        List<NearbyAttractionDto> nearbyAttractionDtos = gpsService.getNearbyAttractions(userName);
        return nearbyAttractionDtos;
    }

    @GetMapping("/getAllCurrentLocations")
    public Map<String, VisitedLocation> getAllCurrentLocations() {
        Map<String, VisitedLocation> locationsMap = userService.getAllCurrentLocations();
        return locationsMap;
    }

    @GetMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(userService.getUserByName(userName));
        return providers;
    }

}
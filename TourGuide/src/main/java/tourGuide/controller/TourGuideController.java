package tourGuide.controller;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.model.UserPreferences;
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
    GpsService gpsService;

    @Autowired
    UserService userService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public Location getLocation(@RequestParam String userName) throws Exception {
        Location location = gpsService.getUserLocation(userService.getUserByName(userName)).call();
        return location;
    }

    @GetMapping("/getNearbyAttractions")
    public List<NearbyAttractionDto> getNearbyAttractions(@RequestParam String userName) throws Exception {
        List<NearbyAttractionDto> nearbyAttractionDtos = gpsService.getNearbyAttractions(userName);
        return nearbyAttractionDtos;
    }

    @GetMapping("/getAllCurrentLocations")
    public Map<String, Location> getAllCurrentLocations() {
        Map<String, Location> locationsMap = gpsService.getAllCurrentLocations();
        return locationsMap;
    }

    @GetMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(userService.getUserByName(userName));
        return providers;
    }

    @PostMapping("/updatePreferences")
    public UserPreferencesDto updateUserPreferences(@RequestParam String userName, @RequestBody UserPreferencesDto userPreferencesDto) {
        UserPreferencesDto preferences = userService.updateUserPreferences(userName, userPreferencesDto);
        return preferences;
    }

}
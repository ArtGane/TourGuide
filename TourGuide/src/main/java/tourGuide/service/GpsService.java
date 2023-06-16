package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.model.User;
import tourGuide.repository.AttractionRepository;
import tourGuide.repository.GpsRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GpsService {

    @Autowired
    private GpsRepository gpsRepository;

    @Autowired
    private RewardsService rewardsService;
    @Autowired
    private AttractionRepository attractionRepository;
    @Autowired
    private UserService userService;

    private List<Attraction> attractions = new ArrayList<>();

    private int proximityBuffer = 10;
    private int attractionProximityRange = 200;
    double statuteMiles;

    public double getDistance(Location loc1, Location loc2) {
        double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;

        return statuteMiles;
    }

    public boolean checkDistance(double statuteMiles) {
        if (statuteMiles < 10) {
            return true;
        } else {
            return false;
        }
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }
    @Async
    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsRepository.getUserLocation(user.getUserId());
        userService.addToVisitedLocations(visitedLocation, user.getUserName());
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    public Location getUserLocation(User user) {
        VisitedLocation visitedLocation = trackUserLocation(user);
        user.setLocation(visitedLocation.location);
        return visitedLocation.location;
    }

    public List<Attraction> getAttractions() {
        if (attractions.isEmpty()) {
            attractions = attractionRepository.getAllAttractions();
        }
        return attractions;
    }

    private List<Attraction> serializeAttractionToDto(User user) {
        getAttractions();
        VisitedLocation userLocation = user.getLastVisitedLocation();
        attractions.sort(Comparator.comparingDouble(attraction ->
                getDistance(userLocation.location, attraction)));
        List<Attraction> nearbyAttractions = attractions.stream()
                .limit(5)
                .collect(Collectors.toList());

        return nearbyAttractions;
    }

    public List<NearbyAttractionDto> getNearbyAttractions(String userName) {
        User user = userService.getUserByName(userName);
        List<NearbyAttractionDto> attractionDtos = new ArrayList<>();

        for (Attraction attraction : serializeAttractionToDto(user)) {
            NearbyAttractionDto nearbyAttractionDto = new NearbyAttractionDto();

            nearbyAttractionDto.setAttractionName(attraction.attractionName);

            Location attractionLocation = new Location(attraction.latitude, attraction.longitude);
            nearbyAttractionDto.setAttractionLocation(attractionLocation);

            nearbyAttractionDto.setUserLocation(getUserLocation(user));

            double distance = getDistance(getUserLocation(user), attraction);
            nearbyAttractionDto.setDistance(distance);

            attractionDtos.add(nearbyAttractionDto);
        }

        return attractionDtos;
    }

    public Attraction getAttractionByName(String name) {
        for (Attraction attraction : attractions) {
            if ((attraction.attractionName).equals(name)) {
                return attraction;
            }
        }
        return null;
    }


}

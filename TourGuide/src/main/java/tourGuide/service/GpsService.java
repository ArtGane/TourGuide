package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.repository.AttractionRepository;
import tourGuide.repository.GpsRepository;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class GpsService {
    private Logger logger = LoggerFactory.getLogger(GpsService.class);
    @Autowired
    RewardCentral rewardsCentral;
    @Autowired
    GpsRepository gpsRepository;
    @Autowired
    AttractionRepository attractionRepository;
    @Autowired
    UserService userService;

    public GpsService(RewardCentral rewardsCentral) {
        this.rewardsCentral = rewardsCentral;
    }

    private List<Attraction> attractions = new ArrayList<>();

    private int proximityBuffer = 10;
    double statuteMiles;

    public List<Attraction> getAttractions() {
        if (attractions.isEmpty()) {
            attractions = attractionRepository.getAllAttractions();
        }
        return attractions;
    }

    public VisitedLocation trackUserLocation(User user) throws Exception {
        VisitedLocation visitedLocation = gpsRepository.getUserLocation(user.getUserId());
        logger.info("Location tracked: {} - {}", visitedLocation.location.latitude, visitedLocation.location.longitude);

        userService.addToVisitedLocations(visitedLocation, user.getUserName());
        logger.info("Visited location added to user's visited locations.");

        calculateRewards(user);
        return visitedLocation;
    }

    public Callable<Location> getUserLocation(User user) {
        return () -> {
            VisitedLocation visitedLocation = trackUserLocation(user);
            user.setLocation(visitedLocation.location);
            return visitedLocation.location;
        };
    }

    public Map<String, Location> getAllCurrentLocations() {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        Map<String, Location> currentLocations = new HashMap<>();
        List<Callable<Location>> locationTasks = new ArrayList<>();

        List<User> users = userService.getAllUsers();

        for (User user : users) {
            Callable<Location> locationTask = getUserLocation(user);
            locationTasks.add(locationTask);
        }

        try {
            List<Future<Location>> futures = executorService.invokeAll(locationTasks);

            for (int index = 0; index < users.size(); index++) {
                User user = users.get(index);
                Location location = futures.get(index).get();
                currentLocations.put(user.getUserName(), location);
            }
        } catch (InterruptedException e) {
            System.err.println("Les tâches ont été interrompues : " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.err.println("Erreur lors de l'exécution des tâches : " + e.getMessage());
        } finally {
            executorService.shutdown();
        }

        return currentLocations;
    }

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

    private List<Attraction> serializeAttractionToDto(User user) throws Exception {
        getAttractions();
        Callable<Location> userLocationCallable = getUserLocation(user);
        Location userLocation;
        userLocation = userLocationCallable.call();

        attractions.sort(Comparator.comparingDouble(attraction ->
                getDistance(userLocation, attraction)));
        List<Attraction> nearbyAttractions = attractions.stream()
                .limit(5)
                .collect(Collectors.toList());

        return nearbyAttractions;
    }

    public List<NearbyAttractionDto> getNearbyAttractions(String userName) throws Exception {
        User user = userService.getUserByName(userName);
        List<NearbyAttractionDto> attractionDtos = new ArrayList<>();

        Callable<Location> userLocationCallable = getUserLocation(user);
        Location userLocation;
        try {
            userLocation = userLocationCallable.call();
        } catch (Exception e) {
            return attractionDtos;
        }

        for (Attraction attraction : serializeAttractionToDto(user)) {
            NearbyAttractionDto nearbyAttractionDto = new NearbyAttractionDto();

            nearbyAttractionDto.setAttractionName(attraction.attractionName);

            Location attractionLocation = new Location(attraction.latitude, attraction.longitude);
            nearbyAttractionDto.setAttractionLocation(attractionLocation);

            nearbyAttractionDto.setUserLocation(userLocation);

            double distance = getDistance(userLocation, attraction);
            nearbyAttractionDto.setDistance(distance);

            attractionDtos.add(nearbyAttractionDto);
        }

        logger.info("{} is close to {}, {}, {}, {}, {}", userName, attractionDtos.get(0).getAttractionName(), attractionDtos.get(1).getAttractionName(), attractionDtos.get(2).getAttractionName(), attractionDtos.get(3).getAttractionName(), attractionDtos.get(4).getAttractionName());

        return attractionDtos;
    }

    public boolean nearAttraction(Location location, Attraction attraction) {
        return getDistance(attraction, location) > proximityBuffer ? false : true;
    }

//    public void calculateRewards(User user) throws ExecutionException, InterruptedException {
//        List<VisitedLocation> userLocations = user.getVisitedLocations();
//        List<Attraction> attractions = getAttractions();
//        List<Callable<Void>> rewardTasks = new ArrayList<>();
//        for (VisitedLocation visitedLocation : userLocations) {
//            for (Attraction attraction : attractions) {
//                if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
//                    Callable<Void> rewardTask = () -> {
//                        if (nearAttraction(visitedLocation.location, attraction)) {
//                            user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
//                        }
//                        return null;
//                    };
//                    rewardTasks.add(rewardTask);
//                }
//            }
//        }
//        ExecutorService executorService = Executors.newCachedThreadPool();
//        List<Future<Void>> futures = executorService.invokeAll(rewardTasks);
//        for (Future<Void> future : futures) {
//            future.get();
//        }
//        executorService.shutdown();
//    }

    private int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    private void calculateRewards(User user) {
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        List<Attraction> attractions = getAttractions();
        userService.updateUserPreferences(user.getUserPreferences());
        for (VisitedLocation visitedLocation : userLocations) {
            for (Attraction attraction : attractions) {
                if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
                    if (nearAttraction(visitedLocation.location, attraction)) {
                        int rewardPoints = getRewardPoints(attraction, user);
                        user.addUserReward(new UserReward(visitedLocation, attraction, rewardPoints));
                        logger.info("GpsService: {} add a new reward from {}: {}", user.getUserName(), attraction.attractionName, rewardPoints);
                    }
                }
            }
        }
    }

    private int calculateTotalRewardPoints(User user) {
        int totalRewardPoints = 0;
        for (UserReward userReward : user.getUserRewards()) {
            totalRewardPoints += userReward.getRewardPoints();
        }
        return totalRewardPoints;
    }

    public void getAllRewardsPoints() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<User> users = userService.getAllUsers();
        List<Callable<Void>> rewardTasks = new ArrayList<>();

        for (User user : users) {
            Callable<Void> rewardTask = () -> {
                calculateRewards(user);
                return null;
            };
            rewardTasks.add(rewardTask);
        }

        executorService.invokeAll(rewardTasks);
        executorService.shutdown();

        Map<String, Integer> rewardsPoints = new HashMap<>();
        for (User user : users) {
            int totalRewardPoints = calculateTotalRewardPoints(user);
            rewardsPoints.put(user.getUserName(), totalRewardPoints);
        }
    }

}

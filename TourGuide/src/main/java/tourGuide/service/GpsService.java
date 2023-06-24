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

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsRepository.getUserLocation(user.getUserId());
        logger.info("Location tracked: {} | {}", visitedLocation.location.latitude, visitedLocation.location.longitude);

        userService.addToVisitedLocations(visitedLocation, user.getUserName());
        logger.info("Visited location added to user's visited locations.");

        calculateRewardsCallable(user);
        return visitedLocation;
    }

    /**
     * Récupère la localisation de l'utilisateur.
     *
     * @param user L'utilisateur dont on souhaite obtenir la localisation.
     * @return Un objet Callable<Location> qui peut être utilisé pour obtenir la localisation de l'utilisateur.
     */
    public Callable<Location> getUserLocation(User user) {
        return () -> {
            VisitedLocation visitedLocation = trackUserLocation(user);
            user.setLocation(visitedLocation.location);
            return visitedLocation.location;
        };
    }

    /**
     * Récupère les emplacements actuels de tous les utilisateurs.
     * Cette méthode utilise un ThreadPool d'exécuteurs pour paralléliser la récupération des emplacements des utilisateurs.
     *
     * @return Une carte (Map) associant le nom d'utilisateur (String) à son emplacement actuel (Location).
     * Si aucun utilisateur n'est trouvé ou s'il y a une erreur lors de l'exécution des tâches de localisation,
     * la carte retournée sera vide.
     */
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

    private int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    /**
     * Calcule les récompenses pour un utilisateur donné en fonction d'une liste d'attractions.
     *
     * @param user L'utilisateur pour lequel les récompenses doivent être calculées.
     * @param attractions La liste des attractions utilisées pour calculer les récompenses.
     */
    public void calculateRewards(User user, List<Attraction> attractions) {
        List<VisitedLocation> userLocations = user.getVisitedLocations();

        for(VisitedLocation visitedLocation : userLocations) {
            for(Attraction attraction : attractions) {
                if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
                    if(nearAttraction(visitedLocation.location, attraction)) {
                        int rewardPoint = getRewardPoints(attraction, user);
                        user.addUserReward(new UserReward(visitedLocation, attraction, rewardPoint));
                        logger.info("GpsService: {} add a new reward from {}: {}", user.getUserName(), attraction.attractionName, rewardPoint);
                    }
                }
            }
        }
    }

    /**
     * Crée un objet Callable<Void> pour le calcul des récompenses d'un utilisateur donné.
     *
     * @param user L'utilisateur pour lequel les récompenses doivent être calculées.
     * @return Un objet Callable<Void> qui effectue le calcul des récompenses.
     */
    private Callable<Void> calculateRewardsCallable(User user) {
        return () -> {
            List<Attraction> attractions = getAttractions();
            calculateRewards(user, attractions);
            return null;
        };
    }

    /**
     * Récupère toutes les récompenses pour une liste d'utilisateurs donnée.
     *
     * @param users La liste des utilisateurs pour lesquels les récompenses doivent être récupérées.
     */
    public void getAllRewards(List<User> users) {
        ExecutorService executorService = Executors.newFixedThreadPool(1000);

        try {
            List<Callable<Void>> rewardTasks = new ArrayList<>();

            for (User user : users) {
                Callable<Void> task = calculateRewardsCallable(user);
                rewardTasks.add(task);
            }

            executorService.invokeAll(rewardTasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
    }
}

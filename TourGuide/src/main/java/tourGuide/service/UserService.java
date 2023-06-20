package tourGuide.service;

import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tourGuide.model.UserReward;
import tourGuide.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    private Map<String, User> internalUserMap = new HashMap<>();

    public List<User> users = new ArrayList<>();

    public Map<String, User> getInternalUserMap() {
        if (internalUserMap.isEmpty()) {
            internalUserMap = userRepository.getMapOfUsers();
        }
        return internalUserMap;
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    public void updateUserPreferences(UserPreferences userPreferences) {
        Random random = new Random();
        int tripDuration = random.nextInt(4, 20);
        userPreferences.setTripDuration(tripDuration);

        int ticketQuantity = random.nextInt(1, 8);
        userPreferences.setTicketQuantity(ticketQuantity);

        int numberOfAdults = random.nextInt(1, 5);
        userPreferences.setNumberOfAdults(numberOfAdults);

        int numberOfChildren = random.nextInt(1, 3);
        userPreferences.setNumberOfChildren(numberOfChildren);
    }

    public void addToVisitedLocations(VisitedLocation visitedLocation, String userName) {
        User user = getUserByName(userName);
        if (user != null) {
            user.addToVisitedLocations(visitedLocation);
            logger.info("UserService: " + userName + " location is " + visitedLocation.location.latitude + visitedLocation.location.longitude);
        } else {
            logger.debug("UserService: " + userName + " doesn't exist");
        }
    }

    public User getUserByName(String userName) {
        User user = getInternalUserMap().get(userName);
        if (user == null) {
            logger.info("No user found with the name: {}", userName);
        }
        return user;
    }

    public List<User> getAllUsers() {
        users = getInternalUserMap().values().stream().collect(Collectors.toList());
        return users;
    }

}

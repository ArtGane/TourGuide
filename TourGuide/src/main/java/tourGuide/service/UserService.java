package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tourGuide.model.User;
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

    private Map<String, VisitedLocation> locationsMap = new HashMap<>();

    public Map<String, User> getInternalUserMap() {
        if (internalUserMap.isEmpty()) {
            internalUserMap = userRepository.getMapOfUsers();
        }
        return internalUserMap;
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    @Async
    public void addToVisitedLocations(VisitedLocation visitedLocation, String userName) {
        User user = getUserByName(userName);
        if (user != null) {
            user.addToVisitedLocations(visitedLocation);
            logger.info("UserService: " + userName + " location is " + visitedLocation.location);
        }
        logger.debug("UserService: " + userName + " doesn't exist");

    }

    public void addUserReward(String userName, VisitedLocation visitedLocation, Attraction attraction, int rewardPoints) {
        User user = getUserByName(userName);
        if (user != null) {
            UserReward userReward = new UserReward(visitedLocation, attraction, rewardPoints);
            user.addUserReward(userReward);
            logger.info("UserService: " + userName + " new reward is " + userReward);

        }
        logger.debug("UserService: " + userName + " doesn't exist");
    }

    public User getUserByName(String userName) {
        return getInternalUserMap().get(userName);
    }

    public List<User> getAllUsers() {
        users = getInternalUserMap().values().stream().collect(Collectors.toList());
        return users;
    }

    public Map<String, VisitedLocation> getAllCurrentLocations() {
        List<User> users = getAllUsers();
        for (User user : users) {
            VisitedLocation userLocation = user.getLastVisitedLocation();
            locationsMap.put(String.valueOf(user.getUserName()), userLocation);
        }
        return locationsMap;
    }


}

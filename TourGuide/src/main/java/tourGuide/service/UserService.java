package tourGuide.service;

import gpsUtil.location.VisitedLocation;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tourGuide.model.UserReward;
import tourGuide.repository.UserRepository;

import javax.money.Monetary;
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


    public UserPreferencesDto updateUserPreferences(String userName, UserPreferencesDto preferencesDto) {
        User user = getUserByName(userName);

        if (user != null) {
            UserPreferences userPreferences = user.getUserPreferences();

            userPreferences.setAttractionProximity(preferencesDto.getAttractionProximity());
            userPreferences.setCurrency(Monetary.getCurrency(preferencesDto.getCurrency()));
            userPreferences.setLowerPricePoint(Money.of(preferencesDto.getLowerPricePoint(), userPreferences.getCurrency()));
            userPreferences.setHighPricePoint(Money.of(preferencesDto.getHighPricePoint(), userPreferences.getCurrency()));
            userPreferences.setTripDuration(preferencesDto.getTripDuration());
            userPreferences.setTicketQuantity(preferencesDto.getTicketQuantity());
            userPreferences.setNumberOfAdults(preferencesDto.getNumberOfAdults());
            userPreferences.setNumberOfChildren(preferencesDto.getNumberOfChildren());


            UserPreferencesDto updatedPreferencesDto = convertToDto(userPreferences);

            return updatedPreferencesDto;
        } else {
            logger.error("UserService: User can't be null");
            return null;
        }
    }

    private UserPreferencesDto convertToDto(UserPreferences userPreferences) {
        UserPreferencesDto preferencesDto = new UserPreferencesDto();
        preferencesDto.setAttractionProximity(userPreferences.getAttractionProximity());
        preferencesDto.setCurrency(userPreferences.getCurrency().getCurrencyCode());
        preferencesDto.setLowerPricePoint(userPreferences.getLowerPricePoint().getNumber().intValue());
        preferencesDto.setHighPricePoint(userPreferences.getHighPricePoint().getNumber().intValue());
        preferencesDto.setTripDuration(userPreferences.getTripDuration());
        preferencesDto.setTicketQuantity(userPreferences.getTicketQuantity());
        preferencesDto.setNumberOfAdults(userPreferences.getNumberOfAdults());
        preferencesDto.setNumberOfChildren(userPreferences.getNumberOfChildren());

        logger.info("Les preferences utilisateur ont ete mises à jour");

        return preferencesDto;
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

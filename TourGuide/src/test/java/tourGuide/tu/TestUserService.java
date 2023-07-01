package tourGuide.tu;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tourGuide.repository.UserRepository;
import tourGuide.service.UserService;

import javax.money.Monetary;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestUserService {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Test
    public void getUserByNameTestOk() {
        Map<String, User> users = new HashMap<>();
        users.put("TestUser0", new User(UUID.randomUUID(), "TestUser0", "06060606", "test@test.fr", new UserPreferences()));
        users.put("TestUser1", new User(UUID.randomUUID(), "TestUser1", "07070707", "test@test.fr", new UserPreferences()));
        users.put("TestUser2", new User(UUID.randomUUID(), "TestUser2", "08080808", "test@test.fr", new UserPreferences()));

        when(userRepository.getMapOfUsers()).thenReturn(users);

        User result = userService.getUserByName("TestUser0");

        assertNotNull(result);
    }

    @Test
    public void getUserByNameTestNull() {
        Map<String, User> users = new HashMap<>();

        when(userRepository.getMapOfUsers()).thenReturn(users);

        User result = userService.getUserByName("TestUser0");

        assertNull(result);
    }

    @Test
    public void getAllUsersTest() {
        Map<String, User> users = new HashMap<>();
        users.put("TestUser0", new User(UUID.randomUUID(), "TestUser0", "06060606", "test@test.fr", new UserPreferences()));
        users.put("TestUser1", new User(UUID.randomUUID(), "TestUser1", "07070707", "test@test.fr", new UserPreferences()));
        users.put("TestUser2", new User(UUID.randomUUID(), "TestUser2", "08080808", "test@test.fr", new UserPreferences()));
        users.put("TestUser3", new User(UUID.randomUUID(), "TestUser2", "08080808", "test@test.fr", new UserPreferences()));
        users.put("TestUser4", new User(UUID.randomUUID(), "TestUser2", "08080808", "test@test.fr", new UserPreferences()));

        when(userRepository.getMapOfUsers()).thenReturn(users);


        assertEquals(userService.getAllUsers().size(), 5);
    }

    @Test
    public void updateUserPreferencesTest() {
        Map<String, User> users = new HashMap<>();
        User user = new User(UUID.randomUUID(), "TestUser0", "06060606", "test@test.fr", new UserPreferences());
        users.put("TestUser0", user);

        when(userRepository.getMapOfUsers()).thenReturn(users);

        UserPreferencesDto userPreferencesDto = new UserPreferencesDto();
        userPreferencesDto.setCurrency("EUR");
        userPreferencesDto.setLowerPricePoint(100);
        userPreferencesDto.setHighPricePoint(400);
        userPreferencesDto.setTicketQuantity(2);
        userPreferencesDto.setNumberOfAdults(1);
        userPreferencesDto.setNumberOfChildren(1);

        userService.updateUserPreferences("TestUser0", userPreferencesDto);

        assertEquals(Monetary.getCurrency("EUR"), user.getUserPreferences().getCurrency());
        assertEquals(Money.of(100, Monetary.getCurrency("EUR")), user.getUserPreferences().getLowerPricePoint());
        assertEquals(Money.of(400, Monetary.getCurrency("EUR")), user.getUserPreferences().getHighPricePoint());
    }

    @Test
    public void addUserTest() {
        User user = new User(UUID.randomUUID(), "TestUser0", "06060606", "test@test.fr", new UserPreferences());
        userService.addUser(user);
        assertEquals(user, userService.getUserByName("TestUser0"));
        assertEquals(userService.getAllUsers().size(), 1);
    }

    @Test
    void addToVisitedLocations() {
        Map<String, User> users = new HashMap<>();
        User user = new User(UUID.randomUUID(), "TestUser0", "06060606", "test@test.fr", new UserPreferences());
        users.put("TestUser0", user);
        VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), new Location(12.1515213, 95.2146), Date.from(Instant.now()));

        when(userRepository.getMapOfUsers()).thenReturn(users);

        boolean addVisitedLocation = userService.addToVisitedLocations(visitedLocation, user.getUserName());

        assertTrue(addVisitedLocation);
        assertEquals(user.getLastVisitedLocation(), visitedLocation);
    }
}
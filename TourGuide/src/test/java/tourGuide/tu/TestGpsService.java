package tourGuide.tu;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tourGuide.repository.AttractionRepository;
import tourGuide.repository.GpsRepository;
import tourGuide.service.GpsService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import tourGuide.service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestGpsService {
    @InjectMocks
    GpsService gpsService;

    @Mock
    private Logger logger;

    @Mock
    private ExecutorService executorService;

    @Mock
    AttractionRepository attractionRepository;

    @Mock
    GpsRepository gpsRepository;

    @Mock
    UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAttractions() {
        List<Attraction> expectedAttractions = new ArrayList<>();
        expectedAttractions.add(new Attraction("Attraction 1", "city 1", "state 1", 12.122, 456.123));
        expectedAttractions.add(new Attraction("Attraction 2", "city 2", "state 2", 178.122, 54.123));
        when(attractionRepository.getAllAttractions()).thenReturn(expectedAttractions);

        List<Attraction> actualAttractions = gpsService.getAttractions();

        assertEquals(expectedAttractions, actualAttractions);
    }

    @Test
    void trackUserLocation() {
        User user = new User(UUID.randomUUID(), "TestUser0", "06060606", "test@test.fr", new UserPreferences());
        VisitedLocation expectedVisitedLocation = new VisitedLocation(user.getUserId(), new Location(12.345, 67.890), Date.from(Instant.now()));
        when(gpsRepository.getUserLocation(user.getUserId())).thenReturn(expectedVisitedLocation);
        when(userService.addToVisitedLocations(expectedVisitedLocation, user.getUserName())).thenReturn(true);

        VisitedLocation actualVisitedLocation = gpsService.trackUserLocation(user);

        assertEquals(expectedVisitedLocation, actualVisitedLocation);
        verify(logger, times(1)).info("Location tracked: {} | {}", expectedVisitedLocation.location.latitude, expectedVisitedLocation.location.longitude);
        verify(logger, times(1)).info("Visited location added to user's visited locations.");
    }

    @Test
    void getDistance() {
        Location loc1 = new Location(10.5, 5.00);
        Location loc2 = new Location(20.00, 10.5);

        double distance = gpsService.getDistance(loc1, loc2);

        assertNotNull(distance);
    }

    @Test
    void getNearbyAttractions() {
    }

    @Test
    void nearAttraction() {
        Location loc1 = new Location(10.5, 5.00);
        Attraction attraction = new Attraction("Attraction", "City", "State", 10.0, 10.0);

        assertNotNull(gpsService.nearAttraction(loc1, attraction));
    }

    @Test
    void calculateRewards() {
    }

}

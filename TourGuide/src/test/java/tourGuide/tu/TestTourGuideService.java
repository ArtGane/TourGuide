package tourGuide.tu;

import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tourGuide.model.UserReward;
import tourGuide.service.TourGuideService;
import tripPricer.Provider;
import tripPricer.TripPricer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestTourGuideService {
	@InjectMocks
	TourGuideService tourGuideService;

	@Mock
	TripPricer tripPricer;

	@Test
	public void getTripDealsTest() throws Exception {
		User user = new User(UUID.randomUUID(), "TestUser0", "06060606", "test@test.fr", new UserPreferences());
		Attraction attraction = new Attraction("Le fun", "Ville du fun", "Etat du fun", 123.45648, 31.52564);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), new Location(124.35654, 32.15486), Date.from(Instant.now()));
		UserReward userReward = new UserReward(visitedLocation, attraction);

		UserPreferences userPreferences = user.getUserPreferences();
		userPreferences.setNumberOfAdults(2);
		userPreferences.setNumberOfChildren(2);
		userPreferences.setTripDuration(5);
		user.setUserPreferences(userPreferences);
		userReward.setRewardPoints(100);
		user.addUserReward(userReward);

		Provider provider = new Provider(UUID.randomUUID(), "testProvider", 100);
		List<Provider> providers = Arrays.asList(provider);

		when(tripPricer.getPrice(anyString(), any(UUID.class), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(providers);

		List<Provider> result = tourGuideService.getTripDeals(user);

		assertEquals(providers, result);
		assertEquals(providers, user.getTripDeals());

		verify(tripPricer, times(1)).getPrice(anyString(), any(UUID.class), anyInt(), anyInt(), anyInt(), anyInt());
	}
}


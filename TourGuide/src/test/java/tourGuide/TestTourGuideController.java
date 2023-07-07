package tourGuide;

import gpsUtil.location.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tourGuide.controller.TourGuideController;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tourGuide.service.GpsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tripPricer.Provider;

import java.util.*;

import static org.mockito.Mockito.when;

@WebMvcTest(controllers = TourGuideController.class)
public class TestTourGuideController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GpsService gpsService;

    @MockBean
    private UserService userService;

    @MockBean
    private TourGuideService tourGuideService;

    @Test
    void getLocation() throws Exception {
        Location mockLocation = new Location(123.45, 67.89);
        User user = new User(UUID.randomUUID(), "TestUser0", "06060606", "test@test.fr", new UserPreferences());

        when(gpsService.getUserLocation(user)).thenReturn(() -> mockLocation);
        when(userService.getUserByName("TestUser0")).thenReturn(user);

        // Performing the request and asserting the response
        mockMvc.perform(MockMvcRequestBuilders.get("/getLocation")
                        .param("userName", "TestUser0"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.latitude").value(mockLocation.latitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$.longitude").value(mockLocation.longitude));
    }

    @Test
    void getNearbyAttractions() throws Exception {
        String userName = "TestUser0";
        List<NearbyAttractionDto> mockAttractions = Arrays.asList(
                new NearbyAttractionDto("Attraction 1", new Location(1.23, 4.56), new Location(7.89, 0.12), 10.0),
                new NearbyAttractionDto("Attraction 2", new Location(2.34, 5.67), new Location(8.90, 1.23), 15.0)
        );

        when(gpsService.getNearbyAttractions(userName)).thenReturn(mockAttractions);

        // Performing the request and asserting the response
        mockMvc.perform(MockMvcRequestBuilders.get("/getNearbyAttractions")
                        .param("userName", userName))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(mockAttractions.size()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attractionName").value(mockAttractions.get(0).getAttractionName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attractionLocation.latitude").value(mockAttractions.get(0).getAttractionLocation().latitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attractionLocation.longitude").value(mockAttractions.get(0).getAttractionLocation().longitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].userLocation.latitude").value(mockAttractions.get(0).getUserLocation().latitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].userLocation.longitude").value(mockAttractions.get(0).getUserLocation().longitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].distance").value(mockAttractions.get(0).getDistance()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attractionName").value(mockAttractions.get(1).getAttractionName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attractionLocation.latitude").value(mockAttractions.get(1).getAttractionLocation().latitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attractionLocation.longitude").value(mockAttractions.get(1).getAttractionLocation().longitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].userLocation.latitude").value(mockAttractions.get(1).getUserLocation().latitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].userLocation.longitude").value(mockAttractions.get(1).getUserLocation().longitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].distance").value(mockAttractions.get(1).getDistance()));
    }

    @Test
    void getAllCurrentLocations() throws Exception {
        Map<String, Location> mockLocationsMap = new HashMap<>();
        mockLocationsMap.put("User1", new Location(1.23, 4.56));
        mockLocationsMap.put("User2", new Location(7.89, 0.12));

        when(gpsService.getAllCurrentLocations()).thenReturn(mockLocationsMap);

        // Performing the request and asserting the response
        mockMvc.perform(MockMvcRequestBuilders.get("/getAllCurrentLocations"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(mockLocationsMap.size()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.User1.latitude").value(mockLocationsMap.get("User1").latitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$.User1.longitude").value(mockLocationsMap.get("User1").longitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$.User2.latitude").value(mockLocationsMap.get("User2").latitude))
                .andExpect(MockMvcResultMatchers.jsonPath("$.User2.longitude").value(mockLocationsMap.get("User2").longitude));

    }

    @Test
    void getTripDeals() throws Exception {
        String userName = "TestUser0";
        User mockUser = new User(UUID.randomUUID(), "TestUser0", "06060606", "test@test.fr", new UserPreferences());
        List<Provider> mockProviders = Arrays.asList(
                new Provider(UUID.randomUUID(), "Provider1", 10.00),
                new Provider(UUID.randomUUID(), "Provider2", 5.00)
        );

        when(tourGuideService.getTripDeals(mockUser)).thenReturn(mockProviders);
        when(userService.getUserByName(userName)).thenReturn(mockUser);

        // Performing the request and asserting the response
        mockMvc.perform(MockMvcRequestBuilders.get("/getTripDeals")
                        .param("userName", userName))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(mockProviders.size()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(mockProviders.get(0).name))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].price").value(mockProviders.get(0).price))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(mockProviders.get(1).name))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].price").value(mockProviders.get(1).price));

    }

    @Test
    void updateUserPreferences() throws Exception {
        String username = "TestUser0";
        Map<String, User> users = new HashMap<>();
        User user = new User(UUID.randomUUID(), "TestUser0", "06060606", "test@test.fr", new UserPreferences());
        users.put("TestUser0", user);

        UserPreferencesDto userPreferencesDtoDto = new UserPreferencesDto();
        userPreferencesDtoDto.setAttractionProximity(10);
        userPreferencesDtoDto.setCurrency("EUR");
        userPreferencesDtoDto.setLowerPricePoint(50);
        userPreferencesDtoDto.setHighPricePoint(850);
        userPreferencesDtoDto.setTripDuration(7);
        userPreferencesDtoDto.setTicketQuantity(3);
        userPreferencesDtoDto.setNumberOfAdults(2);
        userPreferencesDtoDto.setNumberOfChildren(1);

        UserPreferencesDto responseDto = new UserPreferencesDto();
        responseDto.setAttractionProximity(10);
        responseDto.setCurrency("EUR");
        responseDto.setLowerPricePoint(50);
        responseDto.setHighPricePoint(850);
        responseDto.setTripDuration(7);
        responseDto.setTicketQuantity(3);
        responseDto.setNumberOfAdults(2);
        responseDto.setNumberOfChildren(1);

        when(userService.updateUserPreferences(username, userPreferencesDtoDto)).thenReturn(responseDto);
        when(userService.getUserByName(username)).thenReturn(user);

        // Performing the request and asserting the response
        mockMvc.perform(MockMvcRequestBuilders.post("/updatePreferences")
                        .param("userName", user.getUserName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"attractionProximity\": 10,\n" +
                                "  \"currency\": \"EUR\",\n" +
                                "  \"lowerPricePoint\": 50,\n" +
                                "  \"highPricePoint\": 850,\n" +
                                "  \"tripDuration\": 7,\n" +
                                "  \"ticketQuantity\": 3,\n" +
                                "  \"numberOfAdults\": 2,\n" +
                                "  \"numberOfChildren\": 1\n" +
                                "}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
//                .andExpect(MockMvcResultMatchers.jsonPath("$.attractionProximity").value(responseDto.getAttractionProximity()))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value(responseDto.getCurrency()))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.lowerPricePoint").value(responseDto.getLowerPricePoint()))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.highPricePoint").value(responseDto.getHighPricePoint()))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.tripDuration").value(responseDto.getTripDuration()))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.ticketQuantity").value(responseDto.getTicketQuantity()))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfAdults").value(responseDto.getNumberOfAdults()))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfChildren").value(responseDto.getNumberOfChildren()));
    }

}

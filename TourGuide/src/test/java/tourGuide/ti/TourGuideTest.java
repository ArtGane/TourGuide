package tourGuide.ti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gpsUtil.location.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tourGuide.dto.NearbyAttractionDto;
import tourGuide.service.UserService;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TourGuideTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Test
    public void testGetNearbyAttractions() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(("/getNearbyAttractions?userName={userName}"), "TestUser0"))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(response);
        List<NearbyAttractionDto> attractionDtos = new ArrayList<>();

        for (JsonNode attractionJson : responseJson) {
            NearbyAttractionDto nearbyAttractionDto = new NearbyAttractionDto();

            String attractionName = attractionJson.get("attractionName").asText();
            double latitude = attractionJson.get("attractionLocation").get("latitude").asDouble();
            double longitude = attractionJson.get("attractionLocation").get("longitude").asDouble();
            double distance = attractionJson.get("distance").asDouble();

            nearbyAttractionDto.setAttractionName(attractionName);
            Location attractionLocation = new Location(latitude, longitude);
            nearbyAttractionDto.setAttractionLocation(attractionLocation);
            nearbyAttractionDto.setDistance(distance);

            attractionDtos.add(nearbyAttractionDto);
        }
        assertEquals(5, attractionDtos.size());
    }
}

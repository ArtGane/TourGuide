package tourGuide.service;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {

    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    @Autowired
    private TripPricer tripPricer;

    private static final String tripPricerApiKey = "test-server-api-key";

    public TourGuideService() {
    }

    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }


}

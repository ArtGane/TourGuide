package tourGuide;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tourGuide.conf.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.service.GpsService;
import tourGuide.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class TestPerformance {

    @Autowired
    GpsService gpsService;

    @Autowired
    UserService userService;

    @Test
    public void highVolumeTrackLocation() {
        List<User> users = userService.getAllUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        gpsService.getAllCurrentLocations();
        stopWatch.stop();

        System.out.println("Number of users created: " + InternalTestHelper.getInternalUserNumber());
        System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

        for (User user : users) {
            assertEquals(4, user.getVisitedLocations().size());
        }

        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    @Test
    public void highVolumeGetRewards() {
        Attraction attraction = gpsService.getAttractions().get(0);
        List<User> allUsers = userService.getAllUsers();
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        gpsService.getAllRewards(allUsers);
        stopWatch.stop();

        for (User user : allUsers) {
            assertTrue(user.getUserRewards().size() > 0);
        }

        System.out.println("Number of users created: " + allUsers.size());
        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

}

package tourGuide.repository;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tourGuide.service.UserService;

import java.util.UUID;

@Repository
public class GpsRepository {

    @Autowired
    private GpsUtil gpsUtiL;

    public VisitedLocation getUserLocation(UUID userId) {
        return gpsUtiL.getUserLocation(userId);
    }

}

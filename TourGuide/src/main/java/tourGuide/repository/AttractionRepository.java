package tourGuide.repository;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AttractionRepository {

    @Autowired
    GpsUtil gpsUtil = new GpsUtil();

    private List<Attraction> attractions = gpsUtil.getAttractions();

    public List<Attraction> getAllAttractions() {
        return attractions;
    }

}

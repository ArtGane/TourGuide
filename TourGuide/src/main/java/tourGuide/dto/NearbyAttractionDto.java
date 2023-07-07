package tourGuide.dto;

import gpsUtil.location.Location;
import org.springframework.stereotype.Component;

@Component
public class NearbyAttractionDto {

    private String attractionName;
    private Location attractionLocation;
    private Location userLocation;
    private double distance;

    public NearbyAttractionDto() {
    }

    public NearbyAttractionDto(String attractionName, Location attractionLocation, Location userLocation, double distance) {
        this.attractionName = attractionName;
        this.attractionLocation = attractionLocation;
        this.userLocation = userLocation;
        this.distance = distance;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public Location getAttractionLocation() {
        return attractionLocation;
    }

    public void setAttractionLocation(Location attractionLocation) {
        this.attractionLocation = attractionLocation;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}

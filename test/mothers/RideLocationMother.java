package mothers;

import models.RideLocation;

import java.util.Date;

/**
 * Created by sivanookala on 03/11/16.
 */
public class RideLocationMother {
    public static double LAT_LONGS[] = {14.9026234, 79.9940092,
            14.9026312, 79.9940611,
            14.9026337, 79.9940604,
            14.9026234, 79.9940092,
            14.9026234, 79.9940092,
            14.9026234, 79.9940092,
            14.9026234, 79.9940092,
            14.9026234, 79.9940092,
            14.9026234, 79.9940092,
            14.9025803, 79.9940529,
            14.9026445, 79.9939145,
            14.9027952, 79.9939259,
            14.9027952, 79.9939259,
            14.9029727, 79.993934,
            14.9027644, 79.9941403,
            14.9027644, 79.9941403,
            14.9025803, 79.9940529,
            14.9025803, 79.9940529,
            14.9026234, 79.9940092,
            14.9026234, 79.9940092,
            14.9026234, 79.9940092,
            14.9026234, 79.9940092,
            14.9026234, 79.9940092,
            14.9026234, 79.9940092,
            14.9026234, 79.9940092
    };

    public static RideLocation createRideLocation(Long rideId) {
        RideLocation rideLocation = new RideLocation();
        rideLocation.setRideId(rideId);
        return rideLocation;
    }

    public static RideLocation createRideLocation(Long rideId, Double latitude, Double longitude, int i) {
        RideLocation rideLocation = new RideLocation();
        rideLocation.setRideId(rideId);
        rideLocation.setLatitude(latitude);
        rideLocation.setLongitude(longitude);
        rideLocation.setLocationTime(new Date(new Date().getTime() + i * 60 * 1000));
        return rideLocation;
    }

    public static RideLocation createRideLocation(double latitude, double longitude) {
        RideLocation rideLocation = new RideLocation();
        rideLocation.setLatitude(latitude);
        rideLocation.setLongitude(longitude);
        return rideLocation;
    }
}

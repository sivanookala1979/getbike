package utils;

import models.RideLocation;

import java.util.List;

/**
 * Created by sivanookala on 03/11/16.
 */
public class DistanceUtils {

    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        return distance(lat1, lon1, lat2, lon2, "K") * 1000.0;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static double distanceMeters(List<RideLocation> locationList) {
        double result = 0;
        for (int i = 0; i < locationList.size() - 1; i++) {
            RideLocation first = locationList.get(i);
            RideLocation second = locationList.get(i + 1);
            double distanceInMeters = distanceMeters(first.getLatitude(), first.getLongitude(), second.getLatitude(), second.getLongitude());
            result += distanceInMeters;
        }
        return result;
    }
}

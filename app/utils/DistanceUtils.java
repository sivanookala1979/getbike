package utils;

import models.PricingProfile;
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
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        }
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

        if (Double.isNaN(dist)) {
            System.out.println("Something wrong with lats " + lat1 + " " + lon1 + " " + lat2 + " " + lon2);
            dist = 0.0;
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

    public static double distanceKilometers(List<RideLocation> locationList) {
        return ((int) (distanceMeters(locationList) / 10)) / 100.0;
    }

    public static int timeInMinutes(List<RideLocation> locationList) {
        int result = 0;
        if (locationList.size() >= 2) {
            result = (int) ((locationList.get(locationList.size() - 1).getLocationTime().getTime() - locationList.get(0).getLocationTime().getTime()) / (1000.0 * 60.0));
        }
        return result;
    }

    public static double estimateBasePrice(double distanceInKilometers) {
        double estimatedTimeInMinutesForTravellingAKm = 3;
        return calculateBasePrice(distanceInKilometers, distanceInKilometers * estimatedTimeInMinutesForTravellingAKm);
    }

    public static double calculateBasePrice(double distanceInKilometers, double timeInMinutes) {
        double basePrice = 20.0;
        int freeKilometers = 3;
        double pricePerKilometer = 4.0;
        double pricePerMinute = 1.0;
        double freeTimeInMinutes = 5;
        return calculateBasePrice(basePrice, freeKilometers, pricePerKilometer, pricePerMinute, distanceInKilometers, timeInMinutes, freeTimeInMinutes);
    }

    private static double calculateBasePrice(double basePrice, int freeKilometers, double pricePerKilometer, double pricePerMinute, double distanceInKilometers, double timeInMinutes, double freeTimeInMinutes) {
        double result = 0;
        // Add Base Price
        result += basePrice;
        // Add Price For Distance
        if (distanceInKilometers > freeKilometers) {
            result += (distanceInKilometers - freeKilometers) * pricePerKilometer;
        }
        // Add Price For Time
        if (timeInMinutes > freeTimeInMinutes) {
            result += (timeInMinutes - freeTimeInMinutes) * pricePerMinute;
        }
        return round1(result);
    }

    public static double calculateBasePrice(double distanceInKilometers, double timeInMinutes, PricingProfile pricingProfile) {
        if (pricingProfile.isFixedPrice()) {
            return pricingProfile.getFixedPriceAmount();
        }
        double result = 0.0;
        if (pricingProfile.isHasBasePackage()) {
            result = pricingProfile.basePackageAmount;

            if (distanceInKilometers > pricingProfile.getBasePackageKilometers()) {
                result += (distanceInKilometers - pricingProfile.getBasePackageKilometers()) * pricingProfile.getAdditionalPerKilometer();
            }

            if (timeInMinutes > pricingProfile.getBasePackageMinutes()) {
                result += (timeInMinutes - pricingProfile.getBasePackageMinutes()) * pricingProfile.getAdditionalPerMinute();
            }
        } else {
            result = distanceInKilometers * pricingProfile.getAdditionalPerKilometer();
            result += timeInMinutes * pricingProfile.getAdditionalPerMinute();
        }
        return round2(result);
    }

    public static double round1(double result) {
        return (Math.round(result * 10.0)) / 10.0;
    }

    public static double round2(double result) {
        return (Math.round(result * 100.0)) / 100.0;
    }
}

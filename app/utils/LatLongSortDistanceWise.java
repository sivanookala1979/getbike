package utils;


import dataobject.Point;

import java.util.Comparator;

/**
 * Created by darling on 16/5/17.
 */
public class LatLongSortDistanceWise implements Comparator<Point> {
    Point point;

    public LatLongSortDistanceWise(Point point) {
        this.point = point;
    }

    private Double distanceFromMe(Point p) {
        double theta = p.getLng() - point.getLng();
        double dist = Math.sin(deg2rad(p.getLat())) * Math.sin(deg2rad(point.getLat()))
                + Math.cos(deg2rad(p.getLat())) * Math.cos(deg2rad(point.getLat()))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        return dist;
    }

    private double deg2rad(double deg) { return (deg * Math.PI / 180.0); }
    private double rad2deg(double rad) { return (rad * 180.0 / Math.PI); }

    @Override
    public int compare(Point p1, Point p2) {
        return distanceFromMe(p1).compareTo(distanceFromMe(p2));
    }
}

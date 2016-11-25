package utils;

import models.RideLocation;
import mothers.RideLocationMother;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;


/**
 * Created by sivanookala on 03/11/16.
 */
public class DistanceUtilsTest {

    @Test
    public void distanceTESTHappyFlow() {
        assertEquals(262.6777938054349, DistanceUtils.distance(32.9697, -96.80322, 29.46786, -98.53506, "M"));
        assertEquals(422.73893139401383, DistanceUtils.distance(32.9697, -96.80322, 29.46786, -98.53506, "K"));
        assertEquals(0.00834094914310627, DistanceUtils.distance(14.9026234, 79.9940092, 14.902665, 79.9940738, "K"));
    }

    @Test
    public void distanceMetersTESTHappyFlow() {
        assertEquals(8.34094914310627, DistanceUtils.distanceMeters(14.9026234, 79.9940092, 14.902665, 79.9940738));
    }

    @Test
    public void distanceMetersTESTWithList() {
        List<RideLocation> locationList = new ArrayList<>();
        double latlongs[] = {14.9026234, 79.9940092,
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
        for (int i = 0; i < latlongs.length; i += 2) {
            locationList.add(RideLocationMother.createRideLocation(latlongs[i], latlongs[i + 1]));
        }
        assertEquals(132.80391589455584, DistanceUtils.distanceMeters(locationList));

    }

}
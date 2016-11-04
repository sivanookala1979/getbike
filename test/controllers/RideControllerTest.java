package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import models.Ride;
import models.RideLocation;
import models.User;
import mothers.RideLocationMother;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import play.twirl.api.Content;
import utils.CustomCollectionUtils;
import utils.DistanceUtils;
import utils.NumericConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dataobject.RideStatus.RideAccepted;
import static dataobject.RideStatus.RideRequested;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;

/**
 * Created by sivanookala on 21/10/16.
 */
public class RideControllerTest extends BaseControllerTest {

    @Test
    public void getBikeTESTHappyFlow() {
        User user = new User();
        user.setPhoneNumber("8282828282");
        user.setAuthToken(UUID.randomUUID().toString());
        user.save();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result result = route(fakeRequest(GET, "/getBike?" +
                Ride.LATITUDE +
                "=" + startLatitude + "&" +
                Ride.LONGITUDE +
                "=" + startLongitude).header("Authorization", user.getAuthToken()));
        Ride ride = CustomCollectionUtils.first(Ride.find.where().eq(Ride.REQUESTOR_ID, user.getId()).findList());
        JsonNode jsonNode = jsonFromResult(result);
        assertNotNull(ride);
        assertEquals(user.getId(), ride.getRequestorId());
        assertEquals(startLatitude, ride.getStartLatitude(), NumericConstants.DELTA);
        assertEquals(startLongitude, ride.getStartLongitude(), NumericConstants.DELTA);
        assertEquals(ride.getId().longValue(), jsonNode.get(Ride.RIDE_ID).longValue());
        assertEquals(RideRequested, ride.getRideStatus());
    }

    @Test
    public void acceptRideTESTHappyFlow() {
        User user = new User();
        user.setPhoneNumber("8282828282");
        user.setAuthToken(UUID.randomUUID().toString());
        user.save();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result getBikeResult = route(fakeRequest(GET, "/getBike?" +
                Ride.LATITUDE + "=" + startLatitude + "&" +
                Ride.LONGITUDE + "=" + startLongitude).header("Authorization", user.getAuthToken()));
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        Result acceptRideResult = route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        JsonNode acceptRideJsonNode = jsonFromResult(acceptRideResult);
        Ride ride = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        assertNotNull(ride);
        assertEquals(user.getId(), ride.getRiderId());
        assertEquals(RideAccepted, ride.getRideStatus());
        assertNotNull(ride.getAcceptedAt());
        assertEquals("success", acceptRideJsonNode.get("result").textValue());
    }

    @Test
    public void storeLocationsTESTHappyFlow() {
        User user = new User();
        user.setPhoneNumber("8282828282");
        user.setAuthToken(UUID.randomUUID().toString());
        user.save();
        Ride ride = new Ride();
        ride.setRiderId(user.getId());
        ride.save();
        List<RideLocation> locationList = new ArrayList<>();
        locationList.add(RideLocationMother.createRideLocation(ride.getId()));

        Result result = route(fakeRequest(POST, "/storeLocations").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(locationList))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        List<RideLocation> actualRideLocations = RideLocation.find.all();
        assertEquals(locationList.size(), actualRideLocations.size());
        for (RideLocation rideLocation : actualRideLocations) {
            assertEquals(user.getId(), rideLocation.getPostedById());
            assertNotNull(rideLocation.getReceivedAt());
        }
    }


    @Test
    public void closeRideTESTHappyFlow() {
        User user = new User();
        user.setPhoneNumber("8282828282");
        user.setAuthToken(UUID.randomUUID().toString());
        user.save();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result getBikeResult = route(fakeRequest(GET, "/getBike?" +
                Ride.LATITUDE + "=" + startLatitude + "&" +
                Ride.LONGITUDE + "=" + startLongitude).header("Authorization", user.getAuthToken()));
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));

        Ride ride = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        List<RideLocation> rideLocations = new ArrayList<>();


        double latlongs[] = RideLocationMother.LAT_LONGS;
        for (int i = 0; i < latlongs.length; i += 2) {
            RideLocation rideLocation = RideLocationMother.createRideLocation(ride.getId(), latlongs[i], latlongs[i + 1]);
            rideLocation.save();
            rideLocations.add(rideLocation);
        }
        Result closeRideResult = route(fakeRequest(GET, "/closeRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        JsonNode closeRideJsonNode = jsonFromResult(closeRideResult);
        System.out.println(closeRideJsonNode.toString());
        assertEquals("success", closeRideJsonNode.get("result").textValue());
        JsonNode rideJsonObject = closeRideJsonNode.get("ride");
        assertEquals(ride.getId().longValue(), rideJsonObject.get("id").longValue());
        assertEquals("RideClosed", rideJsonObject.get("rideStatus").textValue());
        assertEquals(DistanceUtils.distanceMeters(rideLocations), rideJsonObject.get("orderDistance").doubleValue());
    }

    @Test
    public void ridePathTESTHappyFlow() {
        User user = new User();
        user.setName("Siva Nookala");
        user.setPhoneNumber("8282828282");
        user.setAuthToken(UUID.randomUUID().toString());
        user.save();
        List<String> latLongs = new ArrayList<>();
        latLongs.add("hello");
        latLongs.add("hi");
        Content html = views.html.ridePath.render(latLongs, 23.45, 57.68);
        Assert.assertEquals("text/html", html.contentType());
        String body = html.body();
        assertTrue(body.contains("hello"));
        assertTrue(body.contains("hi"));
        assertTrue(body.contains("23.45"));
        assertTrue(body.contains("57.68"));
    }

    //--------------------------------------------
    //       Setup
    //--------------------------------------------
    @Before
    public void setUp() {
        super.setUp();
        Ebean.createSqlUpdate("delete from ride").execute();
        Ebean.createSqlUpdate("delete from ride_location").execute();
    }

}

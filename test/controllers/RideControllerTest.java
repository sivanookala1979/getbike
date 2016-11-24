package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import dataobject.RideStatus;
import models.Ride;
import models.RideLocation;
import models.User;
import mothers.RideLocationMother;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import play.twirl.api.Content;
import utils.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static dataobject.RideStatus.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static play.test.Helpers.*;

/**
 * Created by sivanookala on 21/10/16.
 */
public class RideControllerTest extends BaseControllerTest {

    @Test
    public void getBikeTESTHappyFlow() {
        User user = loggedInUser();
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
    public void getBikeTESTWithAccessToken() {
        User user = loggedInUser();
        User otherUser = new User();
        otherUser.setName("OtherName");
        otherUser.setGcmCode("fQGK9w6iePY:APA91bEKA_u9AVswVGU0D84RSvH-DZowv33G4Mayp0gjOwljN-TMLUitP37zpPLMi4WcJSzlMccXrTdhyTCBYxn7OBAxlR_BRCAZmZ7BCccSmXkLCPFRzB4j723sUT5Ksfmm0mgQQE4e");
        otherUser.save();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        when(gcmUtilsMock.sendMessage(eq(otherUser), contains("A new ride request with ride Id "), eq("newRide"), anyLong())).thenReturn(true);
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
        verify(gcmUtilsMock).sendMessage(eq(otherUser), contains("A new ride request with ride Id "), eq("newRide"), anyLong());
    }

    @Test
    public void acceptRideTESTHappyFlow() {
        User user = loggedInUser();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result getBikeResult = route(fakeRequest(GET, "/getBike?" +
                Ride.LATITUDE + "=" + startLatitude + "&" +
                Ride.LONGITUDE + "=" + startLongitude).header("Authorization", user.getAuthToken()));
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);

        when(gcmUtilsMock.sendMessage(user, "Your ride is accepted by Siva Nookala ( 8282828282 ) and the rider will be contacting you shortly.", "rideAccepted", getBikeJsonNode.get(Ride.RIDE_ID).longValue())).thenReturn(true);
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
        verify(gcmUtilsMock).sendMessage(user, "Your ride is accepted by Siva Nookala ( 8282828282 ) and the rider will be contacting you shortly.", "rideAccepted", getBikeJsonNode.get(Ride.RIDE_ID).longValue());
    }

    @Test
    public void storeLocationsTESTHappyFlow() {
        User user = loggedInUser();
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
        User user = loggedInUser();
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
        assertEquals(DistanceUtils.distanceMeters(RideLocation.find.where().eq("rideId", ride.getId()).order("locationTime asc").findList()), rideJsonObject.get("orderDistance").doubleValue());
    }


    @Test
    public void openRidesTESTHappyFlow() {
        User user = loggedInUser();
        Ride firstRide = createRide();
        GetBikeUtils.sleep(200);
        Ride secondRide = createRide();
        Result actual = route(fakeRequest(GET, "/openRides").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 2;
        assertEquals(knownNumberOfRides, ridesList.size());
        assertEquals(secondRide.getId().longValue(), ridesList.get(0).get("id").longValue());
        assertEquals(firstRide.getId().longValue(), ridesList.get(1).get("id").longValue());
    }

    @Test
    public void openRidesTESTWithNoOpenRides() {
        User user = loggedInUser();
        Result actual = route(fakeRequest(GET, "/openRides").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 0;
        assertEquals(knownNumberOfRides, ridesList.size());
    }

    @Test
    public void openRidesTESTWithOpenAndClosedRides() {
        User user = loggedInUser();
        Ride firstRide = createRide();
        GetBikeUtils.sleep(200);
        Ride closedRide = createRide();
        closedRide.setRideStatus(RideClosed);
        closedRide.save();
        Ride secondRide = createRide();
        Ride acceptedRide = createRide();
        acceptedRide.setRideStatus(RideAccepted);
        acceptedRide.save();
        Result actual = route(fakeRequest(GET, "/openRides").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 2;
        assertEquals(knownNumberOfRides, ridesList.size());
        assertEquals(secondRide.getId().longValue(), ridesList.get(0).get("id").longValue());
        assertEquals(firstRide.getId().longValue(), ridesList.get(1).get("id").longValue());
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

    @Test
    public void currentRideTESTHappyFlow() {
        User user = loggedInUser();
        Ride firstRide = createRide();
        firstRide.setRideStatus(RideAccepted);
        firstRide.setRiderId(user.getId());
        firstRide.save();
        Result actual = route(fakeRequest(GET, "/currentRide").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode currentRide = responseObject.get("ride");
        assertEquals(firstRide.getId().longValue(), currentRide.get("id").longValue());
    }

    @Test
    public void currentRideTESTWithNoRide() {
        User user = loggedInUser();
        Result actual = route(fakeRequest(GET, "/currentRide").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("failure", responseObject.get("result").textValue());
    }


    @Test
    public void getRideByIdTESTHappyFlow() {
        User user = loggedInUser();
        Ride firstRide = createRide();
        firstRide.setRequestorId(user.getId());
        firstRide.save();
        Result actual = route(fakeRequest(GET, "/getRideById?" + Ride.RIDE_ID + "=" + firstRide.getId()).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode rideById = responseObject.get("ride");
        assertEquals(firstRide.getId().longValue(), rideById.get("id").longValue());
        assertEquals(firstRide.getStartLatitude(), rideById.get("startLatitude").doubleValue());
        assertEquals(firstRide.getStartLongitude(), rideById.get("startLongitude").doubleValue());
        assertEquals(user.getPhoneNumber(), responseObject.get("requestorPhoneNumber").textValue());
        assertEquals(user.getName(), responseObject.get("requestorName").textValue());
        assertEquals("Address of " + firstRide.getStartLatitude() + "," + firstRide.getStartLongitude(), responseObject.get("requestorAddress").textValue());
    }

    @Test
    public void getRideByIdTESTWithInvalidRideId() {
        User user = loggedInUser();
        Result actual = route(fakeRequest(GET, "/getRideById?" + Ride.RIDE_ID + "=" + 221).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("failure", responseObject.get("result").textValue());
    }

    IGcmUtils gcmUtilsMock;

    //--------------------------------------------
    //       Setup
    //--------------------------------------------
    @Before
    public void setUp() {
        super.setUp();
        Ebean.createSqlUpdate("delete from ride").execute();
        Ebean.createSqlUpdate("delete from ride_location").execute();
        gcmUtilsMock = mock(IGcmUtils.class);
        ApplicationContext.defaultContext().setGcmUtils(gcmUtilsMock);
    }


    @NotNull
    private Ride createRide() {
        Ride firstRide = new Ride();
        firstRide.setRideStatus(RideStatus.RideRequested);
        firstRide.setRequestedAt(new Date());
        firstRide.setStartLongitude(22.27);
        firstRide.setStartLatitude(97.654);
        firstRide.save();
        return firstRide;
    }

}

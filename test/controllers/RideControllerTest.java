package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import models.Ride;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Result;
import utils.CustomCollectionUtils;
import utils.NumericConstants;

import java.util.UUID;

import static dataobject.RideStatus.RideAccepted;
import static dataobject.RideStatus.RideRequested;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

    //--------------------------------------------
    //       Setup
    //--------------------------------------------
    @Before
    public void setUp() {
        super.setUp();
        Ebean.createSqlUpdate("delete from ride").execute();
    }
}

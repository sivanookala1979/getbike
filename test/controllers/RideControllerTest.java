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
        Result result = route(fakeRequest(GET, "/getbike?latitude=" + startLatitude + "&longitude=" + startLongitude).header("Authorization", user.getAuthToken()));
        Ride ride = CustomCollectionUtils.first(Ride.find.where().eq("requestorId", user.getId()).findList());
        JsonNode jsonNode = jsonFromResult(result);
        assertNotNull(ride);
        assertEquals(user.getId(), ride.getRequestorId());
        assertEquals(startLatitude, ride.getStartLatitude(), NumericConstants.DELTA);
        assertEquals(startLongitude, ride.getStartLongitude(), NumericConstants.DELTA);
        assertEquals(ride.getId().longValue(), jsonNode.get("rideId").longValue());
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

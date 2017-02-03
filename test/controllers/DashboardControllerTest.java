package controllers;

import models.RiderPosition;
import models.User;
import org.junit.Test;
import play.mvc.Result;

import java.util.Date;

import static junit.framework.TestCase.assertTrue;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

/**
 * Created by Sivakumar Sudarsi on 3/2/17.
 */
public class DashboardControllerTest extends BaseControllerTest {

    @Test
    public void riderPositionsTESTWithHappyFlow(){
        User user = loggedInUser();
        user.setLastKnownLatitude(45.98);
        user.setLastKnownLongitude(89.12);
        Date locationDate  = new Date();
        user.setLastLocationTime(locationDate);
        user.update();
        RiderPosition riderPosition = new RiderPosition();
        riderPosition.setLastKnownLatitude(user.getLastKnownLatitude());
        riderPosition.setLastKnownLongitude(user.getLastKnownLongitude());
        riderPosition.setLastLocationTime(user.getLastLocationTime());
        riderPosition.setUserId(user.id);
        riderPosition.save();
        Result result = route(fakeRequest("GET", "/riderPositions/" + user.id + "/10")).withHeader("Content-type" , "text/html");
        assertTrue(contentAsString(result).contains("45.98"));
        assertTrue(contentAsString(result).contains("89.12"));
        assertTrue(contentAsString(result).contains(locationDate.toString()));
    }
}

package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import dataobject.RideStatus;
import models.Ride;
import models.User;
import models.Wallet;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.WithApplication;
import utils.GetBikeUtils;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

/**
 * Created by sivanookala on 21/10/16.
 */
public class BaseControllerTest extends WithApplication {

    @NotNull
    protected User loggedInUser() {
        User user = new User();
        user.setName("Siva Nookala");
        user.setPhoneNumber("8282828282");
        user.setAuthToken(UUID.randomUUID().toString());
        user.setGender('M');
        user.setValidProofsUploaded(true);
        user.save();
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setAmount(500.0);
        wallet.save();
        return user;
    }

    @NotNull
    protected User otherUser() {
        User user = new User();
        user.setName("Adarsh T");
        user.setPhoneNumber("9949287789");
        user.setAuthToken(UUID.randomUUID().toString());
        user.setGender('M');
        user.setValidProofsUploaded(true);
        user.save();
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setAmount(500.0);
        wallet.save();
        return user;
    }

    @Before
    public void setUp() {
        Ebean.createSqlUpdate("delete from user").execute();
        Ebean.createSqlUpdate("delete from login_otp").execute();
        Ebean.createSqlUpdate("delete from pricing_profile").execute();
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .configure("play.http.router", "router.Routes")
                .build();
    }

    protected JsonNode jsonFromResult(Result result) {
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType().get());
        assertEquals("UTF-8", result.charset().get());
        String resultString = contentAsString(result);
        return Json.parse(resultString);
    }

    @NotNull
    protected Ride createRide(long rideRequestorId) {
        Ride firstRide = new Ride();
        firstRide.setRideStatus(RideStatus.RideRequested);
        firstRide.setRequestedAt(new Date());
        firstRide.setStartLongitude(22.27);
        firstRide.setStartLatitude(97.654);
        firstRide.setRequestorId(rideRequestorId);
        firstRide.setRideGender('M');
        firstRide.save();
        GetBikeUtils.sleep(200);
        return firstRide;
    }


}

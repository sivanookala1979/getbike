package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.WithApplication;

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
        user.setPhoneNumber("8282828282");
        user.setAuthToken(UUID.randomUUID().toString());
        user.save();
        return user;
    }

    @Before
    public void setUp() {
        Ebean.createSqlUpdate("delete from user").execute();
        Ebean.createSqlUpdate("delete from login_otp").execute();
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

}

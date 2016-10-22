package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.LoginOtp;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.*;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;


public class UserControllerTest extends BaseControllerTest {


    @Test
    public void signupTESTHappyFlow() {
        User user = new User();
        user.setGender('M');
        user.setName("Siva Nookala");
        user.setEmail("siva.nookala@gmail.com");
        user.setPhoneNumber("9949287789");
        Result result = route(fakeRequest(POST, "/signup").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        cAssertUser(user, result);
    }

    @Test
    public void signupTESTUser2() {
        User user = new User();
        user.setGender('M');
        user.setName("Shravya M");
        user.setEmail("shravya@vave.co.in");
        user.setPhoneNumber("8282828282");
        Result result = route(fakeRequest(POST, "/signup").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        cAssertUser(user, result);
    }

    @Test
    public void loginTESTHappyFlow() {
        User user = new User();
        user.setPhoneNumber("8282828282");
        user.save();
        Result result = route(fakeRequest(POST, "/login").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        LoginOtp loginOtp = LoginOtp.find.where().eq("userId", user.getId()).orderBy("createdAt").findList().get(0);
        assertNotNull(loginOtp);
        assertEquals(6, loginOtp.getGeneratedOtp().length());
    }

    @Test
    public void loginTESTWithInvalidUser() {
        User user = new User();
        user.setPhoneNumber("8383Invalid");
        Result result = route(fakeRequest(POST, "/login").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("failure", jsonNode.get("result").textValue());
    }

    @Test
    public void loginWithOtpTESTHappyFlow() {
        User user = new User();
        user.setPhoneNumber("8282828282");
        user.save();
        route(fakeRequest(POST, "/login").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        LoginOtp loginOtp = LoginOtp.find.where().eq("userId", user.getId()).orderBy("createdAt").findList().get(0);
        ObjectNode objectNode = Json.newObject();
        objectNode.set("phoneNumber", Json.toJson(user.getPhoneNumber()));
        objectNode.set("otp", Json.toJson(loginOtp.getGeneratedOtp()));
        Result result = route(fakeRequest(POST, "/loginWithOtp").bodyJson(objectNode)).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        User actual = User.find.byId(user.id);
        assertNotNull(actual.getAuthToken());
        assertTrue(actual.getAuthToken().length() > 30);
    }

    @Test
    public void loginWithOtpTESTInvalidOtp() {
        User user = new User();
        user.setPhoneNumber("8282828282");
        user.save();
        route(fakeRequest(POST, "/login").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        LoginOtp loginOtp = LoginOtp.find.where().eq("userId", user.getId()).orderBy("createdAt").findList().get(0);
        ObjectNode objectNode = Json.newObject();
        objectNode.set("phoneNumber", Json.toJson(user.getPhoneNumber()));
        objectNode.set("otp", Json.toJson(loginOtp.getGeneratedOtp() + "JUNK"));
        Result result = route(fakeRequest(POST, "/loginWithOtp").bodyJson(objectNode)).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("failure", jsonNode.get("result").textValue());
    }

    @Test
    public void loginWithOtpTESTWithNoPreviousOtpRequest() {
        User user = new User();
        user.setPhoneNumber("8282828282");
        user.save();
        ObjectNode objectNode = Json.newObject();
        objectNode.set("phoneNumber", Json.toJson(user.getPhoneNumber()));
        objectNode.set("otp", Json.toJson("JUNKJUNK"));
        Result result = route(fakeRequest(POST, "/loginWithOtp").bodyJson(objectNode)).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("failure", jsonNode.get("result").textValue());
    }

    //--------------------------------------------
    //       Setup
    //--------------------------------------------

    private void cAssertUser(User user, Result result) {
        JsonNode jsonNode = jsonFromResult(result);
        assertTrue(jsonNode.has("email"));
        assertEquals(user.getName(), jsonNode.get("name").textValue());
        assertEquals(user.getEmail(), jsonNode.get("email").textValue());
        assertEquals(user.getPhoneNumber(), jsonNode.get("phoneNumber").textValue());
        User actual = User.find.byId(jsonNode.get("id").asLong());
        assertNotNull(actual);
        assertEquals(user.getGender(), actual.getGender());
    }

}
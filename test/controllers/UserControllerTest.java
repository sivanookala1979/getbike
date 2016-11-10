package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.LoginOtp;
import models.Ride;
import models.RideLocation;
import models.User;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import play.twirl.api.Content;
import utils.GetBikeErrorCodes;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;
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
    public void signupTESTAlreadyExistingUser() {
        User user = new User();
        user.setGender('M');
        user.setName("Shravya M");
        user.setEmail("shravya@vave.co.in");
        user.setPhoneNumber("8282828282");
        route(fakeRequest(POST, "/signup").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        Result result = route(fakeRequest(POST, "/signup").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("failure", jsonNode.get("result").textValue());
        assertEquals(GetBikeErrorCodes.USER_ALREADY_EXISTS, jsonNode.get("errorCode").intValue());
    }

    @Test
    public void loginTESTHappyFlow() {
        User user = new User();
        user.setPhoneNumber("9949287789");
        user.save();
        Result result = route(fakeRequest(POST, "/login").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        LoginOtp loginOtp = LoginOtp.find.where().eq("userId", user.getId()).orderBy("createdAt").findList().get(0);
        assertNotNull(loginOtp);
        assertEquals(6, loginOtp.getGeneratedOtp().length());
        assertNotNull(loginOtp.getCreatedAt());
    }

    @Test
    public void loginTESTWithInvalidUser() {
        User user = new User();
        user.setPhoneNumber("8383Invalid");
        Result result = route(fakeRequest(POST, "/login").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("failure", jsonNode.get("result").textValue());
        assertEquals(GetBikeErrorCodes.INVALID_USER, jsonNode.get("errorCode").intValue());
    }

    @Test
    public void loginWithOtpTESTHappyFlow() {
        User user = new User();
        user.setPhoneNumber("9949287789");
        user.save();
        route(fakeRequest(POST, "/login").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        LoginOtp loginOtp = LoginOtp.find.where().eq("userId", user.getId()).orderBy("createdAt").findList().get(0);
        ObjectNode objectNode = Json.newObject();
        objectNode.set("phoneNumber", Json.toJson(user.getPhoneNumber()));
        objectNode.set("otp", Json.toJson(loginOtp.getGeneratedOtp()));
        Result result = route(fakeRequest(POST, "/loginWithOtp").bodyJson(objectNode)).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        assertTrue(jsonNode.get("authToken").textValue().length() > 0);
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

    @Test
    public void usersTESTHappyFlow() {
        User user = new User();
        user.setName("Siva Nookala");
        user.setPhoneNumber("8282828282");
        user.setAuthToken(UUID.randomUUID().toString());
        user.save();
        Content html = views.html.userIndex.render(Collections.singletonList(user), Ride.find.all(), RideLocation.find.all(), LoginOtp.find.all());
        assertEquals("text/html", html.contentType());
        String body = html.body();
        System.out.println("Name : " + user.getName());
        assertTrue(body.contains(user.getName()));
        assertTrue(body.contains(user.getPhoneNumber()));
        assertTrue(body.contains(user.getAuthToken()));

    }

    @Test
    public void storeGcmCodeTESTHappyFlow() {
        User user = loggedInUser();
        String gcmCode = UUID.randomUUID().toString();
        Result result = route(fakeRequest(GET, "/storeGcmCode?" +
                "gcmCode" + "=" + gcmCode).header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        User actual = User.find.byId(user.getId());
        assertEquals(gcmCode, actual.getGcmCode());
    }

    @Test
    public void storeGcmCodeTESTWithNoAuthorizationCode() {
        User user = loggedInUser();
        String gcmCode = UUID.randomUUID().toString();
        Result result = route(fakeRequest(GET, "/storeGcmCode?" +
                "gcmCode" + "=" + gcmCode));
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
package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.LoginOtp;
import models.Ride;
import models.RideLocation;
import models.User;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Content;
import utils.GetBikeErrorCodes;
import utils.NumericConstants;

import java.io.File;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;
import static play.test.Helpers.*;


public class UserControllerTest extends BaseControllerTest {


    @Test
    public void userApproveAcceptTESTWithHappyFlowWithUploadProofs() {
        User user = new User();
        user.setGender('M');
        user.setName("Siva");
        user.setEmail("siva@gmail.com");
        user.setPhoneNumber("2233665599");
        user.setDrivingLicenseImageName("testdriver.png");
        user.setVehiclePlateImageName("testplate.png");
        user.save();
        Result result = route(fakeRequest(GET, "/user/accept/" + user.getId()));
        User dbUser = User.find.where().eq("id", user.id).findUnique();
        assertEquals("Siva", user.getName());
        assertEquals(200, result.status());
    }

    @Test
    public void userApproveAcceptTESTWithHappyFlowWithOutUploadProofs() {
        User user = new User();
        user.setGender('M');
        user.setName("Siva");
        user.setEmail("siva@gmail.com");
        user.setPhoneNumber("2233665599");
        user.save();
        Result result = route(fakeRequest(GET, "/user/accept/" + user.getId()));
        User dbUser = User.find.where().eq("id", user.id).findUnique();
        assertEquals("Siva", user.getName());
        assertEquals(303, result.status());
    }

    @Test
    public void updateUserProofValidationApproveTESTWithHappyFlowWithAccept() {
        User user = new User();
        user.id = 231l;
        user.setGender('M');
        user.setName("Siva");
        user.setEmail("siva@gmail.com");
        user.setPhoneNumber("2233665599");
        user.setDrivingLicenseImageName("testdriver.png");
        user.setVehiclePlateImageName("testplate.png");
        user.save();
        Result result = route(fakeRequest(GET, "/user/approve/" + user.getId() + "/" + user.isValidProofsUploaded()));
        User dbUser = User.find.where().eq("id", user.getId()).findUnique();
        assertEquals("Siva", dbUser.getName());
        assertEquals(true, dbUser.isValidProofsUploaded());
        assertEquals(303, result.status());
    }

    @Test
    public void updateUserProofValidationApproveTESTWithHappyFlowWithReject() {
        User user = new User();
        user.id = 232l;
        user.setGender('M');
        user.setName("Siva");
        user.setEmail("siva@gmail.com");
        user.setPhoneNumber("2233665599");
        user.setDrivingLicenseImageName("testdriver.png");
        user.setVehiclePlateImageName("testplate.png");
        user.setValidProofsUploaded(true);
        user.save();
        Result result = route(fakeRequest(GET, "/user/approve/" + user.getId() + "/" + user.isValidProofsUploaded()));
        User dbUser = User.find.where().eq("id", user.getId()).findUnique();
        assertEquals("Siva", dbUser.getName());
        assertEquals(false, dbUser.isValidProofsUploaded());
        assertEquals(303, result.status());
    }

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

    @Test
    public void storeLastKnownLocationTESTHappyFlow() {
        User user = loggedInUser();
        ObjectNode objectNode = Json.newObject();
        objectNode.put("lastKnownLatitude", 54.67);
        objectNode.put("lastKnownLongitude", 21.34);
        Date locationDate = new Date();
        objectNode.set("lastLocationTime", Json.toJson(locationDate));
        Result result = route(fakeRequest(POST, "/storeLastKnownLocation").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(objectNode))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        User actual = User.find.byId(user.id);
        assertEquals(54.67, actual.getLastKnownLatitude().doubleValue(), NumericConstants.DELTA);
        assertEquals(21.34, actual.getLastKnownLongitude().doubleValue(), NumericConstants.DELTA);
        assertEquals(locationDate, actual.getLastLocationTime());
    }

    @Test
    public void storeDrivingLicenseTESTHappyFlow() {
        User user = loggedInUser();
        ObjectNode objectNode = Json.newObject();
        objectNode.put("imageData", "aGVsbG8gaGVsbG8gaGVsbG8=");
        objectNode.put("drivingLicenseNumber", "HYD/55522/3333");
        Result result = route(fakeRequest(POST, "/storeDrivingLicense").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(objectNode))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        User actual = User.find.byId(user.getId());
        assertEquals("HYD/55522/3333", actual.getDrivingLicenseNumber());
        assertNotNull(actual.getDrivingLicenseImageName());
        assertTrue(actual.getDrivingLicenseImageName().endsWith(".png"));
        assertTrue(new File("public/" + actual.getDrivingLicenseImageName()).exists());
        Result imageCheckResult = route(fakeRequest(GET, "/" + actual.getDrivingLicenseImageName()));
        assertEquals(200, imageCheckResult.status());
        new File("public/" + actual.getDrivingLicenseImageName()).deleteOnExit();
    }

    @Test
    public void storeVehiclePlateTESTHappyFlow() {
        User user = loggedInUser();
        ObjectNode objectNode = Json.newObject();
        objectNode.put("imageData", "aGVsbG8gaGVsbG8gaGVsbG8=");
        objectNode.put("vehiclePlateNumber", "AP09BF3497");
        Result result = route(fakeRequest(POST, "/storeVehiclePlate").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(objectNode))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        User actual = User.find.byId(user.getId());
        assertEquals("AP09BF3497", actual.getVehicleNumber());
        assertNotNull(actual.getVehiclePlateImageName());
        assertTrue(actual.getVehiclePlateImageName().endsWith(".png"));
        assertTrue(new File("public/" + actual.getVehiclePlateImageName()).exists());
        Result imageCheckResult = route(fakeRequest(GET, "/" + actual.getVehiclePlateImageName()));
        assertEquals(200, imageCheckResult.status());
        new File("public/" + actual.getVehiclePlateImageName()).deleteOnExit();
    }

    @Test
    public void getPublicProfileTESTWithLoggedInUser() {
        User user = loggedInUser();
        User otherUser = new User();
        otherUser.setName("Good Gangaraju");
        otherUser.setPhoneNumber("373629282");
        otherUser.setDrivingLicenseNumber("2827829/AP/2929");
        otherUser.setVehicleNumber("AP01K02");
        otherUser.setVehiclePlateImageName("/assets/3445-vp.png");
        otherUser.setDrivingLicenseImageName("/assets/28927-vpg.jpg");
        otherUser.save();
        Result result = route(fakeRequest(GET, "/getPublicProfile/" + otherUser.getId()).header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        assertEquals(otherUser.getName(), jsonNode.get("profile").get("name").textValue());
        assertEquals(otherUser.getPhoneNumber(), jsonNode.get("profile").get("phoneNumber").textValue());
        assertEquals(otherUser.getDrivingLicenseNumber(), jsonNode.get("profile").get("drivingLicenseNumber").textValue());
        assertEquals(otherUser.getVehicleNumber(), jsonNode.get("profile").get("vehicleNumber").textValue());
        assertEquals(otherUser.getVehiclePlateImageName(), jsonNode.get("profile").get("vehiclePlateImageName").textValue());
        assertEquals(otherUser.getDrivingLicenseImageName(), jsonNode.get("profile").get("drivingLicenseImageName").textValue());
    }

    @Test
    public void getPrivateProfileTESTWithLoggedInUser() {
        User user = loggedInUser();
        user.setOccupation("Software Engineer");
        user.setCity("Delhi");
        user.save();
        Result result = route(fakeRequest(GET, "/getPrivateProfile").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        assertEquals(user.getName(), jsonNode.get("privateProfile").get("name").textValue());
        assertEquals(user.getPhoneNumber(), jsonNode.get("privateProfile").get("phoneNumber").textValue());
        assertEquals(user.getOccupation(), jsonNode.get("privateProfile").get("occupation").textValue());
        assertEquals(user.getCity(), jsonNode.get("privateProfile").get("city").textValue());
    }

    @Test
    public void getCurrentRideTESTNoRide() {
        User user = loggedInUser();
        Result result = route(fakeRequest(GET, "/getCurrentRide").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("failure", jsonNode.get("result").textValue());
        assertFalse(jsonNode.has("rideId"));
    }

    @Test
    public void getCurrentRideTESTHappyFlow() {
        User user = loggedInUser();
        user.setRideInProgress(true);
        user.setCurrentRideId(24l);
        user.save();
        Result result = route(fakeRequest(GET, "/getCurrentRide").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        assertEquals(user.getCurrentRideId().longValue(), jsonNode.get("rideId").longValue());
    }

    @Test
    public void updatePrivateProfileTESTWithLoggedInUser() {
        User user = loggedInUser();
        user.setOccupation("Software Engineer");
        user.setCity("Delhi");
        user.setHomeLocation("Kandukur");
        user.setOfficeLocation("Pullareddy Nagar");
        ObjectNode objectNode = Json.newObject();
        objectNode.set("user", Json.toJson(user));
        objectNode.set("imageData", Json.toJson("aGVsbG8gaGVsbG8gaGVsbG8="));
        Result result = route(fakeRequest(POST, "/updatePrivateProfile").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(objectNode))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        User actual = User.find.byId(user.getId());
        assertEquals("Delhi", actual.getCity());
        assertEquals("Kandukur", actual.getHomeLocation());
        assertEquals("Pullareddy Nagar", actual.getOfficeLocation());
        assertEquals("Software Engineer", actual.getOccupation());
        assertNotNull(actual.getProfileImage());
        assertTrue(actual.getProfileImage().endsWith(".png"));
        assertTrue(new File("public/" + actual.getProfileImage()).exists());
        Result imageCheckResult = route(fakeRequest(GET, "/" + actual.getProfileImage()));
        assertEquals(200, imageCheckResult.status());
        new File("public/" + actual.getProfileImage()).deleteOnExit();

    }

    @Test
    public void getPublicProfileTESTWithNoUserId() {
        User user = loggedInUser();
        user.setDrivingLicenseNumber("2827829/AP/2929");
        user.setVehicleNumber("AP01K02");
        user.setVehiclePlateImageName("/assets/3445-vp.png");
        user.setDrivingLicenseImageName("/assets/28927-vpg.jpg");
        user.save();
        Result result = route(fakeRequest(GET, "/getPublicProfile/0").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        assertTrue(jsonNode.get("profile").get("promoCode").textValue().length() > 0);
        User actual = User.find.byId(user.getId());
        assertTrue(actual.getPromoCode().startsWith("siva"));
        assertFalse(actual.getPromoCode().contains(" "));
        assertEquals(actual.getPromoCode(), jsonNode.get("profile").get("promoCode").textValue());
        assertEquals(user.getName(), jsonNode.get("profile").get("name").textValue());
        assertEquals(user.getPhoneNumber(), jsonNode.get("profile").get("phoneNumber").textValue());
        assertEquals(user.getDrivingLicenseNumber(), jsonNode.get("profile").get("drivingLicenseNumber").textValue());
        assertEquals(user.getVehicleNumber(), jsonNode.get("profile").get("vehicleNumber").textValue());
        assertEquals(user.getVehiclePlateImageName(), jsonNode.get("profile").get("vehiclePlateImageName").textValue());
        assertEquals(user.getDrivingLicenseImageName(), jsonNode.get("profile").get("drivingLicenseImageName").textValue());
    }


    @Test
    public void clearCurrentRideTESTHappyFlow() {
        User user = loggedInUser();
        user.setCurrentRideId(23l);
        user.setRideInProgress(true);
        user.save();
        BaseController.IS_TEST = true;
        Result result = route(fakeRequest(GET, "/users/clearCurrentRide/" + user.getId()).header("Authorization", user.getAuthToken()));
        System.out.println(result.redirectLocation());
        User actual = User.find.byId(user.getId());
        assertFalse(actual.isRideInProgress());
        assertNull(actual.getCurrentRideId());
    }

    @Test
    public void ensurePromoCodeTESTHappyFlow() {
        User user = loggedInUser();
        UserController.ensurePromoCode(user);
        assertTrue(user.getPromoCode().startsWith("siva"));
        assertFalse(user.getPromoCode().contains(" "));
        assertEquals("siva".length() + 6, user.getPromoCode().length());
    }

    @Test
    public void ensurePromoCodeTESTWithNoName() {
        User user = loggedInUser();
        user.setName(null);
        user.save();
        UserController.ensurePromoCode(user);
        assertTrue(user.getPromoCode().startsWith("getbike"));
        assertFalse(user.getPromoCode().contains(" "));
        assertEquals("getbike".length() + 6, user.getPromoCode().length());
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
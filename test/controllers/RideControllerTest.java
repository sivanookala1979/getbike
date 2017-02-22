package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dataobject.RideStatus;
import models.Ride;
import models.RideLocation;
import models.User;
import models.Wallet;
import mothers.RideLocationMother;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import play.twirl.api.Content;
import utils.*;

import java.util.*;

import static controllers.RideController.FREE_RIDE_MAX_DISCOUNT;
import static controllers.UserController.JOINING_BONUS;
import static dataobject.RideStatus.*;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.test.Helpers.*;
import static utils.DateUtils.minutesOld;
import static utils.DistanceUtils.round2;
import static utils.GetBikeErrorCodes.CAN_NOT_ACCEPT_YOUR_OWN_RIDE;
import static utils.GetBikeErrorCodes.RIDE_ALREADY_IN_PROGRESS;

/**
 * Created by sivanookala on 21/10/16.
 */
public class RideControllerTest extends BaseControllerTest {

    @Test
    public void getBikeTESTHappyFlow() {
        User user = loggedInUser();
        ObjectNode requestObjectNode = Json.newObject();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        requestObjectNode.set(Ride.LATITUDE, Json.toJson(startLatitude));
        requestObjectNode.set(Ride.LONGITUDE, Json.toJson(startLongitude));
        requestObjectNode.set("sourceAddress", Json.toJson("Pullareddy Nagar, Kavali"));
        requestObjectNode.set("destinationAddress", Json.toJson("Musunuru, Kavali"));
        requestObjectNode.set("modeOfPayment", Json.toJson("Cash"));
        Result result = route(fakeRequest(POST, "/getBike").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        Ride ride = CustomCollectionUtils.first(Ride.find.where().eq(Ride.REQUESTOR_ID, user.getId()).findList());
        JsonNode jsonNode = jsonFromResult(result);
        assertNotNull(ride);
        assertEquals(user.getId(), ride.getRequestorId());
        assertEquals(startLatitude, ride.getStartLatitude(), NumericConstants.DELTA);
        assertEquals(startLongitude, ride.getStartLongitude(), NumericConstants.DELTA);
        assertEquals("Pullareddy Nagar, Kavali", ride.getSourceAddress());
        assertEquals("Musunuru, Kavali", ride.getDestinationAddress());
        assertEquals(ride.getId().longValue(), jsonNode.get(Ride.RIDE_ID).longValue());
        assertEquals('M', ride.getRideGender());
        assertEquals(RideRequested, ride.getRideStatus());
    }

    @Test
    public void getBikeTESTWithPreviousRideNotClosed() {
        User user = loggedInUser();
        ObjectNode requestObjectNode = Json.newObject();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        requestObjectNode.set(Ride.LATITUDE, Json.toJson(startLatitude));
        requestObjectNode.set(Ride.LONGITUDE, Json.toJson(startLongitude));
        requestObjectNode.set("sourceAddress", Json.toJson("Pullareddy Nagar, Kavali"));
        requestObjectNode.set("destinationAddress", Json.toJson("Musunuru, Kavali"));
        requestObjectNode.set("modeOfPayment", Json.toJson("Cash"));
        Result firstResult = route(fakeRequest(POST, "/getBike").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        JsonNode firstJsonNode = jsonFromResult(firstResult);
        Ride firstRide = Ride.find.byId(firstJsonNode.get(Ride.RIDE_ID).longValue());
        Result secondResult = route(fakeRequest(POST, "/getBike").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        JsonNode secondJsonNode = jsonFromResult(secondResult);
        Ride secondRide = Ride.find.byId(secondJsonNode.get(Ride.RIDE_ID).longValue());
        assertNotNull(secondRide);
        assertEquals(firstRide.getId(), secondRide.getId());
    }

    @Test
    public void getBikeTESTWithPreviousRideExpired() {
        User user = loggedInUser();
        ObjectNode requestObjectNode = Json.newObject();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        requestObjectNode.set(Ride.LATITUDE, Json.toJson(startLatitude));
        requestObjectNode.set(Ride.LONGITUDE, Json.toJson(startLongitude));
        requestObjectNode.set("sourceAddress", Json.toJson("Pullareddy Nagar, Kavali"));
        requestObjectNode.set("destinationAddress", Json.toJson("Musunuru, Kavali"));
        requestObjectNode.set("modeOfPayment", Json.toJson("Cash"));
        Result firstResult = route(fakeRequest(POST, "/getBike").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        JsonNode firstJsonNode = jsonFromResult(firstResult);
        Ride firstRide = Ride.find.byId(firstJsonNode.get(Ride.RIDE_ID).longValue());
        firstRide.setRequestedAt(minutesOld(16));
        firstRide.save();
        Result secondResult = route(fakeRequest(POST, "/getBike").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        JsonNode secondJsonNode = jsonFromResult(secondResult);
        Ride secondRide = Ride.find.byId(secondJsonNode.get(Ride.RIDE_ID).longValue());
        assertNotNull(secondRide);
        assertNotEquals(firstRide.getId(), secondRide.getId());
        Ride firstRideReloaded = Ride.find.byId(firstJsonNode.get(Ride.RIDE_ID).longValue());
        assertEquals(RideCancelled, firstRideReloaded.getRideStatus());
    }

    @Test
    public void hailCustomerTESTWithNoPreviousUser() {
        User user = loggedInUser();
        ObjectNode requestObjectNode = Json.newObject();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        requestObjectNode.set(Ride.LATITUDE, Json.toJson(startLatitude));
        requestObjectNode.set(Ride.LONGITUDE, Json.toJson(startLongitude));
        requestObjectNode.set("sourceAddress", Json.toJson("Pullareddy Nagar, Kavali"));
        requestObjectNode.set("destinationAddress", Json.toJson("Musunuru, Kavali"));
        requestObjectNode.set("phoneNumber", Json.toJson("7776663334"));
        requestObjectNode.set("name", Json.toJson("Subbarao Vellanki"));
        requestObjectNode.set("email", Json.toJson("subbarao.vellanki@gmail.com"));
        requestObjectNode.set("gender", Json.toJson('M'));
        requestObjectNode.set("modeOfPayment", Json.toJson("Cash"));
        Result result = route(fakeRequest(POST, "/hailCustomer").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        Ride ride = CustomCollectionUtils.first(Ride.find.where().eq("riderId", user.getId()).findList());
        JsonNode jsonNode = jsonFromResult(result);
        User requestor = User.find.where().eq("phoneNumber", "7776663334").findUnique();
        assertNotNull(requestor);
        assertNotNull(ride);
        assertEquals(ride.getId(), requestor.getCurrentRequestRideId());
        assertEquals(true, requestor.isRequestInProgress());
        assertEquals("Subbarao Vellanki", requestor.getName());
        assertEquals("subbarao.vellanki@gmail.com", requestor.getEmail());
        assertEquals('M', requestor.getGender());
        assertEquals(user.getId(), ride.getRiderId());
        assertEquals(requestor.getId(), ride.getRequestorId());
        assertEquals(startLatitude, ride.getStartLatitude(), NumericConstants.DELTA);
        assertEquals(startLongitude, ride.getStartLongitude(), NumericConstants.DELTA);
        assertEquals("Pullareddy Nagar, Kavali", ride.getSourceAddress());
        assertEquals("Musunuru, Kavali", ride.getDestinationAddress());
        assertEquals(ride.getId().longValue(), jsonNode.get(Ride.RIDE_ID).longValue());
        assertEquals('M', ride.getRideGender());
        assertEquals(RideAccepted, ride.getRideStatus());
        User actual = User.find.byId(user.getId());
        assertTrue(actual.isRideInProgress());
        assertEquals(ride.getId(), actual.getCurrentRideId());
        assertEquals(JOINING_BONUS, WalletController.getWalletAmount(requestor));
    }

    @Test
    public void hailCustomerTESTWithExistingUser() {
        User user = loggedInUser();
        User requestor = new User();
        requestor.setPhoneNumber("7776663334");
        requestor.save();
        ObjectNode requestObjectNode = Json.newObject();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        requestObjectNode.set(Ride.LATITUDE, Json.toJson(startLatitude));
        requestObjectNode.set(Ride.LONGITUDE, Json.toJson(startLongitude));
        requestObjectNode.set("sourceAddress", Json.toJson("Pullareddy Nagar, Kavali"));
        requestObjectNode.set("destinationAddress", Json.toJson("Musunuru, Kavali"));
        requestObjectNode.set("phoneNumber", Json.toJson("7776663334"));
        requestObjectNode.set("modeOfPayment", Json.toJson("Cash"));
        Result result = route(fakeRequest(POST, "/hailCustomer").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        Ride ride = CustomCollectionUtils.first(Ride.find.where().eq("riderId", user.getId()).findList());
        JsonNode jsonNode = jsonFromResult(result);
        assertNotNull(ride);
        assertEquals(user.getId(), ride.getRiderId());
        assertEquals(requestor.getId(), ride.getRequestorId());
        assertEquals(startLatitude, ride.getStartLatitude(), NumericConstants.DELTA);
        assertEquals(startLongitude, ride.getStartLongitude(), NumericConstants.DELTA);
        assertEquals("Pullareddy Nagar, Kavali", ride.getSourceAddress());
        assertEquals("Musunuru, Kavali", ride.getDestinationAddress());
        assertEquals('M', ride.getRideGender());
        assertEquals(ride.getId().longValue(), jsonNode.get(Ride.RIDE_ID).longValue());
        assertEquals(RideAccepted, ride.getRideStatus());
        User actual = User.find.byId(user.getId());
        assertTrue(actual.isRideInProgress());
        assertEquals(ride.getId(), actual.getCurrentRideId());
        User requestorReloaded = User.find.byId(requestor.getId());
        assertEquals(ride.getId(), requestorReloaded.getCurrentRequestRideId());
        assertEquals(true, requestorReloaded.isRequestInProgress());
    }

    @Test
    public void hailCustomerTESTWithRideInProgress() {
        User user = loggedInUser();
        user.setRideInProgress(true);
        user.setCurrentRideId(24l);
        user.save();
        ObjectNode requestObjectNode = Json.newObject();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        requestObjectNode.set(Ride.LATITUDE, Json.toJson(startLatitude));
        requestObjectNode.set(Ride.LONGITUDE, Json.toJson(startLongitude));
        requestObjectNode.set("sourceAddress", Json.toJson("Pullareddy Nagar, Kavali"));
        requestObjectNode.set("destinationAddress", Json.toJson("Musunuru, Kavali"));
        requestObjectNode.set("phoneNumber", Json.toJson("7776663334"));
        requestObjectNode.set("modeOfPayment", Json.toJson("Cash"));
        Result result = route(fakeRequest(POST, "/hailCustomer").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        JsonNode hailCustomerJsonNode = jsonFromResult(result);
        assertEquals(GetBikeErrorCodes.RIDE_ALREADY_IN_PROGRESS, hailCustomerJsonNode.get("errorCode").intValue());
        assertEquals("failure", hailCustomerJsonNode.get("result").textValue());
        User actual = User.find.byId(user.getId());
        assertTrue(actual.isRideInProgress());
        assertEquals(24l, actual.getCurrentRideId().longValue());
    }

    @Test
    public void hailCustomerTESTWithNoWalletBalance() {
        User user = loggedInUser();
        user.setRideInProgress(true);
        user.setCurrentRideId(24l);
        user.save();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        ObjectNode requestObjectNode = Json.newObject();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        requestObjectNode.set(Ride.LATITUDE, Json.toJson(startLatitude));
        requestObjectNode.set(Ride.LONGITUDE, Json.toJson(startLongitude));
        requestObjectNode.set("sourceAddress", Json.toJson("Pullareddy Nagar, Kavali"));
        requestObjectNode.set("destinationAddress", Json.toJson("Musunuru, Kavali"));
        requestObjectNode.set("phoneNumber", Json.toJson("7776663334"));
        requestObjectNode.set("modeOfPayment", Json.toJson("Cash"));
        Result result = route(fakeRequest(POST, "/hailCustomer").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        JsonNode hailCustomerJsonNode = jsonFromResult(result);
        assertEquals(GetBikeErrorCodes.INSUFFICIENT_WALLET_AMOUNT, hailCustomerJsonNode.get("errorCode").intValue());
        assertEquals("failure", hailCustomerJsonNode.get("result").textValue());
    }

    @Test
    public void hailCustomerTESTWithProofsNotUploaded() {
        User user = loggedInUser();
        user.setValidProofsUploaded(false);
        user.save();
        ObjectNode requestObjectNode = Json.newObject();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        requestObjectNode.set(Ride.LATITUDE, Json.toJson(startLatitude));
        requestObjectNode.set(Ride.LONGITUDE, Json.toJson(startLongitude));
        requestObjectNode.set("sourceAddress", Json.toJson("Pullareddy Nagar, Kavali"));
        requestObjectNode.set("destinationAddress", Json.toJson("Musunuru, Kavali"));
        requestObjectNode.set("phoneNumber", Json.toJson("7776663334"));
        requestObjectNode.set("modeOfPayment", Json.toJson("Cash"));
        Result result = route(fakeRequest(POST, "/hailCustomer").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        JsonNode hailCustomerJsonNode = jsonFromResult(result);
        assertEquals(GetBikeErrorCodes.RIDE_VALID_PROOFS_UPLOAD, hailCustomerJsonNode.get("errorCode").intValue());
        assertEquals("failure", hailCustomerJsonNode.get("result").textValue());
    }

    @Test
    public void getBikeTESTWithAccessToken() {
        User user = loggedInUser();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        User otherUser = new User();
        otherUser.setName("OtherName");
        otherUser.setGcmCode("fQGK9w6iePY:APA91bEKA_u9AVswVGU0D84RSvH-DZowv33G4Mayp0gjOwljN-TMLUitP37zpPLMi4WcJSzlMccXrTdhyTCBYxn7OBAxlR_BRCAZmZ7BCccSmXkLCPFRzB4j723sUT5Ksfmm0mgQQE4e");
        otherUser.setLastKnownLatitude(startLatitude);
        otherUser.setLastKnownLongitude(startLongitude);
        otherUser.setGender('M');
        otherUser.save();
        when(gcmUtilsMock.sendMessage(eq(Collections.singletonList(otherUser)), contains("A new ride request with ride Id "), eq("newRide"), anyLong())).thenReturn(true);
        Result result = requestGetBike(user, startLatitude, startLongitude);
        Ride ride = CustomCollectionUtils.first(Ride.find.where().eq(Ride.REQUESTOR_ID, user.getId()).findList());
        JsonNode jsonNode = jsonFromResult(result);
        assertNotNull(ride);
        assertEquals(user.getId(), ride.getRequestorId());
        assertEquals(startLatitude, ride.getStartLatitude(), NumericConstants.DELTA);
        assertEquals(startLongitude, ride.getStartLongitude(), NumericConstants.DELTA);
        assertEquals(ride.getId().longValue(), jsonNode.get(Ride.RIDE_ID).longValue());
        assertEquals('M', ride.getRideGender());
        assertEquals(RideRequested, ride.getRideStatus());
        verify(gcmUtilsMock).sendMessage(eq(Collections.singletonList(otherUser)), contains("A new ride request with ride Id "), eq("newRide"), anyLong());
    }

    @Test
    public void acceptRideTESTHappyFlow() {
        User user = loggedInUser();
        User otherUser = otherUser();
        Ride ride = createRide(otherUser.getId());
        when(gcmUtilsMock.sendMessage(otherUser, "Your ride is accepted by Siva Nookala ( 8282828282 ) and the rider will be contacting you shortly.", "rideAccepted", ride.getId())).thenReturn(true);
        Result acceptRideResult = route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + ride.getId()).header("Authorization", user.getAuthToken()));
        JsonNode acceptRideJsonNode = jsonFromResult(acceptRideResult);
        System.out.println(acceptRideJsonNode);
        Ride actualRide = Ride.find.byId(ride.getId());
        assertNotNull(actualRide);
        assertEquals(user.getId(), actualRide.getRiderId());
        assertEquals(RideAccepted, actualRide.getRideStatus());
        assertNotNull(actualRide.getAcceptedAt());
        assertEquals("success", acceptRideJsonNode.get("result").textValue());
        verify(gcmUtilsMock).sendMessage(otherUser, "Your ride is accepted by Siva Nookala ( 8282828282 ) and the rider will be contacting you shortly.", "rideAccepted", ride.getId());
        User actualUser = User.find.byId(user.getId());
        assertTrue(actualUser.isRideInProgress());
        assertEquals(actualRide.getId(), actualUser.getCurrentRideId());
    }

    @Test
    public void acceptRideTESTWithYourOwnRide() {
        User user = loggedInUser();
        Ride ride = createRide(user.getId());
        Result acceptRideResult = route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + ride.getId()).header("Authorization", user.getAuthToken()));
        JsonNode acceptRideJsonNode = jsonFromResult(acceptRideResult);
        assertEquals(CAN_NOT_ACCEPT_YOUR_OWN_RIDE, acceptRideJsonNode.get("errorCode").intValue());
        assertEquals("failure", acceptRideJsonNode.get("result").textValue());
    }

    @Test
    public void rateRideTESTHappyFlow() {
        User user = loggedInUser();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result getBikeResult = requestGetBike(user, startLatitude, startLongitude);
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        when(gcmUtilsMock.sendMessage(user, "Your ride is accepted by Siva Nookala ( 8282828282 ) and the rider will be contacting you shortly.", "rideAccepted", getBikeJsonNode.get(Ride.RIDE_ID).longValue())).thenReturn(true);
        Result acceptRideResult = route(fakeRequest(GET, "/rateRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID) + "&rating=3").header("Authorization", user.getAuthToken()));
        JsonNode acceptRideJsonNode = jsonFromResult(acceptRideResult);
        Ride ride = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        assertNotNull(ride);
        assertEquals(3, ride.getRating().intValue());
    }

    @Test
    public void updatePaymentStatusTESTFlow(){
        User user= loggedInUser();
        Ride ride = new Ride();
        ride.setRiderId(user.id);
        ride.setRideStatus(RideStatus.RideClosed);
        ride.save();
        Result actual = route(fakeRequest(GET, "/updatePaymentStatus?" + Ride.RIDE_ID + "=" + ride.getId()).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        assertTrue(Ride.find.byId(ride.getId()).isPaid());
    }

    @Test
    public void acceptRideTESTWithRideInProgress() {
        User user = loggedInUser();
        user.setRideInProgress(true);
        user.setCurrentRideId(24l);
        user.save();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result getBikeResult = requestGetBike(user, startLatitude, startLongitude);
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        Result acceptRideResult = route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        JsonNode acceptRideJsonNode = jsonFromResult(acceptRideResult);
        Ride ride = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        assertEquals(RideRequested, ride.getRideStatus());
        assertEquals(GetBikeErrorCodes.RIDE_ALREADY_IN_PROGRESS, acceptRideJsonNode.get("errorCode").intValue());
        assertEquals("failure", acceptRideJsonNode.get("result").textValue());
        User actual = User.find.byId(user.getId());
        assertTrue(actual.isRideInProgress());
        assertEquals(24l, actual.getCurrentRideId().longValue());
    }

    @Test
    public void acceptRideTESTWithInvalidProofsUploadIsFalse() {
        User user = loggedInUser();
        user.setValidProofsUploaded(false);
        user.save();
        double startLatitude = 20.4567;
        double startLongitude = 42.17186;
        Result getBikeResult = requestGetBike(user, startLatitude, startLongitude);
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        Result acceptRideResult = route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        JsonNode acceptRideJsonNode = jsonFromResult(acceptRideResult);
        assertEquals(GetBikeErrorCodes.RIDE_VALID_PROOFS_UPLOAD, acceptRideJsonNode.get("errorCode").intValue());

    }

    @Test
    public void acceptRideTESTWithInsufficientWalletAmount() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        double startLatitude = 20.4567;
        double startLongitude = 42.17186;
        System.out.println("Wallet Amount " + WalletController.getWalletAmount(user));
        Result getBikeResult = requestGetBike(user, startLatitude, startLongitude);
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        Result acceptRideResult = route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        JsonNode acceptRideJsonNode = jsonFromResult(acceptRideResult);
        assertEquals(GetBikeErrorCodes.INSUFFICIENT_WALLET_AMOUNT, acceptRideJsonNode.get("errorCode").intValue());

    }

    @Test
    public void acceptRideTESTWithInvalidProofsUploadIsTrue() {
        User user = loggedInUser();
        user.setValidProofsUploaded(true);
        user.setRideInProgress(true);
        user.save();
        double startLatitude = 20.4567;
        double startLongitude = 42.17186;
        Result getBikeResult = requestGetBike(user, startLatitude, startLongitude);
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        Result acceptRideResult = route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        JsonNode acceptRideJsonNode = jsonFromResult(acceptRideResult);
        Ride ride = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        assertEquals(RIDE_ALREADY_IN_PROGRESS, acceptRideJsonNode.get("errorCode").intValue());
    }

    @Test
    public void acceptRideTESTAfterAllocatedToSomeoneElse() {
        User user = loggedInUser();
        User otherUser = otherUser();
        Ride ride = createRide(otherUser.getId());
        ride.setRideStatus(RideAccepted);
        ride.save();
        Result acceptRideResult = route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + ride.getId()).header("Authorization", user.getAuthToken()));
        JsonNode acceptRideJsonNode = jsonFromResult(acceptRideResult);
        assertEquals(GetBikeErrorCodes.RIDE_ALLOCATED_TO_OTHERS, acceptRideJsonNode.get("errorCode").intValue());
        assertEquals("failure", acceptRideJsonNode.get("result").textValue());
        User actual = User.find.byId(user.getId());
        assertFalse(actual.isRideInProgress());
        assertNull(actual.getCurrentRideId());
    }

    @Test
    public void startRideTESTHappyFlow() {
        User user = loggedInUser();
        user.setLastKnownLatitude(23.45);
        user.setLastKnownLongitude(76.34);
        user.save();
        User otherUser = otherUser();
        Ride ride = createRide(otherUser.getId());
        ride.setRiderId(user.getId());
        ride.setRideStatus(RideAccepted);
        ride.save();
        when(gcmUtilsMock.sendMessage(otherUser, "23.45,76.34,true", "riderLocation", ride.getId())).thenReturn(true);
        Result acceptRideResult = route(fakeRequest(GET, "/startRide?" +
                Ride.RIDE_ID +
                "=" + ride.getId()).header("Authorization", user.getAuthToken()));
        JsonNode startRideJsonNode = jsonFromResult(acceptRideResult);
        Ride actualRide = Ride.find.byId(ride.getId());
        verify(gcmUtilsMock).sendMessage(otherUser, "23.45,76.34,true", "riderLocation", ride.getId());
        assertNotNull(actualRide);
        assertTrue(actualRide.isRideStarted());
        assertNotNull(actualRide.getRideStartedAt());
        assertEquals("success", startRideJsonNode.get("result").textValue());
    }

    @Test
    public void cancelRideTESTWithRideRequested() {
        User user = loggedInUser();
        Ride ride = createRide(user.getId());
        user.setRequestInProgress(true);
        user.setCurrentRequestRideId(ride.getId());
        user.save();
        Result acceptRideResult = route(fakeRequest(GET, "/cancelRide?" +
                Ride.RIDE_ID +
                "=" + ride.getId()).header("Authorization", user.getAuthToken()));
        JsonNode startRideJsonNode = jsonFromResult(acceptRideResult);
        Ride actualRide = Ride.find.byId(ride.getId());
        assertEquals(RideCancelled, actualRide.getRideStatus());
        assertEquals("success", startRideJsonNode.get("result").textValue());
        user.refresh();
        assertFalse(user.isRequestInProgress());
        assertNull(user.getCurrentRequestRideId());
    }

    @Test
    public void cancelRideTESTWithRideAccepted() {
        User user = loggedInUser();
        User otherUser = otherUser();
        Ride ride = createRide(otherUser.getId());
        ride.setRiderId(user.getId());
        ride.setRideStatus(RideAccepted);
        ride.save();
        otherUser.setCurrentRequestRideId(ride.getId());
        otherUser.setRequestInProgress(true);
        otherUser.save();
        user.setCurrentRideId(ride.getId());
        user.setRideInProgress(true);
        user.save();
        when(gcmUtilsMock.sendMessage(user, "Ride " + ride.getId() + " is cancelled.", "rideCancelled", ride.getId())).thenReturn(true);
        Result acceptRideResult = route(fakeRequest(GET, "/cancelRide?" +
                Ride.RIDE_ID +
                "=" + ride.getId()).header("Authorization", otherUser.getAuthToken()));
        JsonNode startRideJsonNode = jsonFromResult(acceptRideResult);
        Ride actualRide = Ride.find.byId(ride.getId());
        assertEquals(RideCancelled, actualRide.getRideStatus());
        assertEquals("success", startRideJsonNode.get("result").textValue());
        verify(gcmUtilsMock).sendMessage(user, "Ride " + ride.getId() + " is cancelled.", "rideCancelled", ride.getId());
        user.refresh();
        otherUser.refresh();
        assertFalse(user.isRideInProgress());
        assertNull(user.getCurrentRideId());
        assertFalse(otherUser.isRequestInProgress());
        assertNull(otherUser.getCurrentRequestRideId());
    }

    @Test
    public void cancelRideTESTWithRideStarted() {
        User user = loggedInUser();
        User otherUser = otherUser();
        Ride ride = createRide(otherUser.getId());
        ride.setRiderId(user.getId());
        ride.setRideStatus(RideAccepted);
        ride.setRideStarted(true);
        ride.save();
        Result acceptRideResult = route(fakeRequest(GET, "/cancelRide?" +
                Ride.RIDE_ID +
                "=" + ride.getId()).header("Authorization", otherUser.getAuthToken()));
        JsonNode startRideJsonNode = jsonFromResult(acceptRideResult);
        Ride actualRide = Ride.find.byId(ride.getId());
        assertEquals(RideAccepted, actualRide.getRideStatus());
        assertEquals("failure", startRideJsonNode.get("result").textValue());
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
    public void estimateRideTESTHappyFlow() {
        User user = loggedInUser();
        Ride ride = new Ride();
        ride.setRiderId(user.getId());
        ride.save();
        List<RideLocation> locationList = new ArrayList<>();
        locationList.add(RideLocationMother.createRideLocation(ride.getId(), 44.35, 56.77, 0));
        locationList.add(RideLocationMother.createRideLocation(ride.getId(), 43.35, 56.67, 2));
        Result result = route(fakeRequest(POST, "/estimateRide").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(locationList))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        System.out.println(jsonNode);
        assertEquals(round2(DistanceUtils.distanceKilometers(locationList) * 1.2), jsonNode.get("orderDistance").doubleValue());
        assertEquals(DistanceUtils.estimateBasePrice(round2(DistanceUtils.distanceKilometers(locationList) * 1.2)), jsonNode.get("orderAmount").doubleValue());
    }

    @Test
    public void closeRideTESTHappyFlow() {
        User user = loggedInUser();
        user.setRideInProgress(false);
        user.setCurrentRideId(null);
        user.save();
        double walletAmountBefore = WalletController.getWalletAmount(user);
        User otherUser = otherUser();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result getBikeResult = requestGetBike(otherUser, startLatitude, startLongitude);
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));

        Ride ride = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        when(gcmUtilsMock.sendMessage(otherUser, "Your ride is now closed.", "rideClosed", getBikeJsonNode.get(Ride.RIDE_ID).longValue())).thenReturn(true);
        List<RideLocation> rideLocations = new ArrayList<>();


        double latlongs[] = RideLocationMother.LAT_LONGS;
        for (int i = 0; i < latlongs.length; i += 2) {
            RideLocation rideLocation = RideLocationMother.createRideLocation(ride.getId(), latlongs[i], latlongs[i + 1], i);
            rideLocation.save();
            rideLocations.add(rideLocation);
        }
        Result closeRideResult = route(fakeRequest(GET, "/closeRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        double walletAmountAfter = WalletController.getWalletAmount(user);

        JsonNode closeRideJsonNode = jsonFromResult(closeRideResult);
        System.out.println(closeRideJsonNode.toString());
        assertEquals("success", closeRideJsonNode.get("result").textValue());
        JsonNode rideJsonObject = closeRideJsonNode.get("ride");
        assertEquals(ride.getId().longValue(), rideJsonObject.get("id").longValue());
        assertEquals("RideClosed", rideJsonObject.get("rideStatus").textValue());
        double expectedDistance = DistanceUtils.distanceKilometers(RideLocation.find.where().eq("rideId", ride.getId()).order("locationTime asc").findList());
        assertEquals(expectedDistance, rideJsonObject.get("orderDistance").doubleValue());
        assertEquals(DistanceUtils.calculateBasePrice(expectedDistance, DistanceUtils.timeInMinutes(rideLocations)), rideJsonObject.get("orderAmount").doubleValue());
        verify(gcmUtilsMock).sendMessage(otherUser, "Your ride is now closed.", "rideClosed", getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        User actual = User.find.byId(user.getId());
        assertNull(actual.getCurrentRideId());
        assertFalse(actual.isRideInProgress());
        ride = Ride.find.byId(ride.id);
        assertEquals(walletAmountBefore - ride.getTotalBill(), walletAmountAfter);
    }

    @Test
    public void closeRideTESTWithUpdatingActualSourceAddressAndActualDestinationAddress() {
        User user = loggedInUser();
        user.setRideInProgress(false);
        user.setCurrentRideId(null);
        user.save();
        User otherUser = otherUser();
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result getBikeResult = requestGetBike(otherUser, startLatitude, startLongitude);
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));

        Ride ride = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        when(gcmUtilsMock.sendMessage(otherUser, "Your ride is now closed.", "rideClosed", getBikeJsonNode.get(Ride.RIDE_ID).longValue())).thenReturn(true);
        List<RideLocation> rideLocations = new ArrayList<>();


        double latlongs[] = RideLocationMother.LAT_LONGS;
        for (int i = 0; i < latlongs.length; i += 2) {
            RideLocation rideLocation = RideLocationMother.createRideLocation(ride.getId(), latlongs[i], latlongs[i + 1], i);
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
        double expectedDistance = DistanceUtils.distanceKilometers(RideLocation.find.where().eq("rideId", ride.getId()).order("locationTime asc").findList());
        assertEquals(expectedDistance, rideJsonObject.get("orderDistance").doubleValue());
        assertEquals(DistanceUtils.calculateBasePrice(expectedDistance, DistanceUtils.timeInMinutes(rideLocations)), rideJsonObject.get("orderAmount").doubleValue());
        verify(gcmUtilsMock).sendMessage(otherUser, "Your ride is now closed.", "rideClosed", getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        User actual = User.find.byId(user.getId());
        assertNull(actual.getCurrentRideId());
        assertFalse(actual.isRideInProgress());
        Ride afterCloseRide = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        assertNull(afterCloseRide.getActualSourceAddress());
        assertNull(afterCloseRide.getActualDestinationAddress());
        GetBikeUtils.sleep(4000);
        Ride afterSleepRide = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        System.out.println(afterSleepRide.getActualSourceAddress() + " XXXXXXXXXXX " + afterSleepRide.getActualDestinationAddress());
        assertNotNull(afterSleepRide.getActualSourceAddress());
        assertNotNull(afterSleepRide.getActualDestinationAddress());
    }

    @Test
    public void closeRideTESTWithReferralCode() {
        User referrer = new User();
        referrer.setPromoCode("siva625255");
        referrer.save();
        User user = loggedInUser();
        user.setRideInProgress(false);
        user.setCurrentRideId(null);
        user.save();
        User otherUser = otherUser();
        otherUser.setSignupPromoCode(referrer.getPromoCode());
        otherUser.setFreeRidesEarned(1);
        otherUser.setFreeRidesSpent(0);
        otherUser.save();
        double walletAmountBefore = WalletController.getWalletAmount(user);
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result getBikeResult = requestGetBike(otherUser, startLatitude, startLongitude);
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        Ride ride = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        List<RideLocation> rideLocations = new ArrayList<>();
        double latlongs[] = RideLocationMother.LAT_LONGS;
        for (int i = 0; i < latlongs.length; i += 2) {
            RideLocation rideLocation = RideLocationMother.createRideLocation(ride.getId(), latlongs[i], latlongs[i + 1], i);
            rideLocation.save();
            rideLocations.add(rideLocation);
        }
        route(fakeRequest(GET, "/closeRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        referrer.refresh();
        otherUser.refresh();
        ride.refresh();
        assertTrue(ride.isFreeRide());
        assertEquals(FREE_RIDE_MAX_DISCOUNT, ride.getFreeRideDiscount());
        double walletAmountAfter = WalletController.getWalletAmount(user);
        assertEquals(1, otherUser.getFreeRidesSpent().intValue());
        assertEquals(1, referrer.getFreeRidesEarned().intValue());
        assertEquals(walletAmountBefore - ride.getTotalBill() + (FREE_RIDE_MAX_DISCOUNT * 10.0), walletAmountAfter);
    }

    @Test
    public void closeRideTESTMoreThanOneFreeRideEarned() {
        User referrer = new User();
        referrer.setPromoCode("siva625255");
        referrer.setFreeRidesEarned(0);
        referrer.save();
        User user = loggedInUser();
        user.setRideInProgress(false);
        user.setCurrentRideId(null);
        user.save();
        User otherUser = otherUser();
        otherUser.setSignupPromoCode(referrer.getPromoCode());
        otherUser.setFreeRidesEarned(2);
        otherUser.setFreeRidesSpent(0);
        otherUser.save();
        double walletAmountBefore = WalletController.getWalletAmount(user);
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result getBikeResult = requestGetBike(otherUser, startLatitude, startLongitude);
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        Ride ride = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        List<RideLocation> rideLocations = new ArrayList<>();
        double latlongs[] = RideLocationMother.LAT_LONGS;
        for (int i = 0; i < latlongs.length; i += 2) {
            RideLocation rideLocation = RideLocationMother.createRideLocation(ride.getId(), latlongs[i], latlongs[i + 1], i);
            rideLocation.save();
            rideLocations.add(rideLocation);
        }
        route(fakeRequest(GET, "/closeRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        referrer.refresh();
        otherUser.refresh();
        ride.refresh();
        assertTrue(ride.isFreeRide());
        assertEquals(FREE_RIDE_MAX_DISCOUNT, ride.getFreeRideDiscount());
        double walletAmountAfter = WalletController.getWalletAmount(user);
        assertEquals(1, otherUser.getFreeRidesSpent().intValue());
        assertEquals(0, referrer.getFreeRidesEarned().intValue());
        assertEquals(walletAmountBefore - ride.getTotalBill() + (FREE_RIDE_MAX_DISCOUNT * 10.0), walletAmountAfter);
    }

    @Test
    public void closeRideTESTWithReferralCodeAndInvoiceAmountLessThanFreeDiscount() {
        User referrer = new User();
        referrer.setPromoCode("siva625255");
        referrer.save();
        User user = loggedInUser();
        user.setRideInProgress(false);
        user.setCurrentRideId(null);
        user.save();
        User otherUser = otherUser();
        otherUser.setSignupPromoCode(referrer.getPromoCode());
        otherUser.setFreeRidesEarned(1);
        otherUser.setFreeRidesSpent(0);
        otherUser.save();
        double walletAmountBefore = WalletController.getWalletAmount(user);
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result getBikeResult = requestGetBike(otherUser, startLatitude, startLongitude);
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        Ride ride = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        route(fakeRequest(GET, "/closeRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        referrer.refresh();
        otherUser.refresh();
        ride.refresh();
        assertTrue(ride.isFreeRide());
        assertEquals(ride.getTotalBill(), ride.getFreeRideDiscount());
        double walletAmountAfter = WalletController.getWalletAmount(user);
        assertEquals(1, otherUser.getFreeRidesSpent().intValue());
        assertEquals(1, referrer.getFreeRidesEarned().intValue());
        assertEquals(walletAmountBefore - ride.getTotalBill() + (ride.getTotalBill() * 10.0), walletAmountAfter);
    }


    @Test
    public void closeRideTESTWithNoSignupPromoCode() {
        User referrer = new User();
        referrer.setPromoCode("siva625255");
        referrer.setFreeRidesEarned(0);
        referrer.save();
        User user = loggedInUser();
        user.setRideInProgress(false);
        user.setCurrentRideId(null);
        user.save();
        User otherUser = otherUser();
        otherUser.setFreeRidesEarned(1);
        otherUser.setFreeRidesSpent(0);
        otherUser.save();
        double walletAmountBefore = WalletController.getWalletAmount(user);
        double startLatitude = 23.4567;
        double startLongitude = 72.17186;
        Result getBikeResult = requestGetBike(otherUser, startLatitude, startLongitude);
        JsonNode getBikeJsonNode = jsonFromResult(getBikeResult);
        route(fakeRequest(GET, "/acceptRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        Ride ride = Ride.find.byId(getBikeJsonNode.get(Ride.RIDE_ID).longValue());
        route(fakeRequest(GET, "/closeRide?" +
                Ride.RIDE_ID +
                "=" + getBikeJsonNode.get(Ride.RIDE_ID)).header("Authorization", user.getAuthToken()));
        referrer.refresh();
        otherUser.refresh();
        ride.refresh();
        assertTrue(ride.isFreeRide());
        assertEquals(ride.getTotalBill(), ride.getFreeRideDiscount());
        double walletAmountAfter = WalletController.getWalletAmount(user);
        assertEquals(1, otherUser.getFreeRidesSpent().intValue());
        assertEquals(0, referrer.getFreeRidesEarned().intValue());
        assertEquals(walletAmountBefore - ride.getTotalBill() + (ride.getTotalBill() * 10.0), walletAmountAfter);
    }

    @Test
    public void openRidesTESTHappyFlow() {
        User user = loggedInUser();
        User otherUser = otherUser();
        Ride firstRide = createRide(otherUser.getId());
        Ride secondRide = createRide(otherUser.getId());
        Result actual = route(fakeRequest(GET, "/openRides?latitude=" + firstRide.getStartLatitude() + "&longitude=" + firstRide.getStartLongitude()).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 2;
        assertEquals(knownNumberOfRides, ridesList.size());
        assertEquals(secondRide.getId().longValue(), ridesList.get(0).get("ride").get("id").longValue());
        assertEquals(firstRide.getId().longValue(), ridesList.get(1).get("ride").get("id").longValue());
        assertEquals(otherUser.getName(), ridesList.get(0).get("requestorName").textValue());
        assertEquals(otherUser.getPhoneNumber(), ridesList.get(0).get("requestorPhoneNumber").textValue());
        assertEquals(otherUser.getName(), ridesList.get(1).get("requestorName").textValue());
        assertEquals(otherUser.getPhoneNumber(), ridesList.get(1).get("requestorPhoneNumber").textValue());
    }

    @Test
    public void openRidesTESTWithNoOpenRides() {
        User user = loggedInUser();
        Result actual = route(fakeRequest(GET, "/openRides?latitude=" + 23.45 + "&longitude=" + 72.83).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 0;
        assertEquals(knownNumberOfRides, ridesList.size());
    }

    @Test
    public void openRidesTESTWithOpenAndClosedRides() {
        User user = loggedInUser();
        User otherUser = otherUser();
        Ride firstRide = createRide(otherUser.getId());
        Ride closedRide = createRide(3736);
        closedRide.setRideStatus(RideClosed);
        closedRide.save();
        Ride secondRide = createRide(otherUser.getId());
        Ride acceptedRide = createRide(otherUser.getId());
        acceptedRide.setRideStatus(RideAccepted);
        acceptedRide.save();
        Result actual = route(fakeRequest(GET, "/openRides?latitude=" + firstRide.getStartLatitude() + "&longitude=" + firstRide.getStartLongitude()).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 2;
        assertEquals(knownNumberOfRides, ridesList.size());
        assertEquals(secondRide.getId().longValue(), ridesList.get(0).get("ride").get("id").longValue());
        assertEquals(firstRide.getId().longValue(), ridesList.get(1).get("ride").get("id").longValue());
        assertEquals(otherUser.getName(), ridesList.get(0).get("requestorName").textValue());
        assertEquals(otherUser.getPhoneNumber(), ridesList.get(0).get("requestorPhoneNumber").textValue());
        assertEquals(otherUser.getName(), ridesList.get(1).get("requestorName").textValue());
        assertEquals(otherUser.getPhoneNumber(), ridesList.get(1).get("requestorPhoneNumber").textValue());
    }

    @Test
    public void openRidesTESTWithMaleAndFemaleRides() {
        User user = loggedInUser();
        User otherUser = otherUser();
        Ride maleRide1 = createRide(otherUser.getId());
        Ride maleRide2 = createRide(otherUser.getId());
        Ride femaleRide = createRide(otherUser.getId());
        femaleRide.setRideGender('F');
        femaleRide.save();
        Result actual = route(fakeRequest(GET, "/openRides?latitude=" + maleRide1.getStartLatitude() + "&longitude=" + maleRide1.getStartLongitude()).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 2;
        assertEquals(knownNumberOfRides, ridesList.size());
        assertEquals(maleRide2.getId().longValue(), ridesList.get(0).get("ride").get("id").longValue());
        assertEquals(maleRide1.getId().longValue(), ridesList.get(1).get("ride").get("id").longValue());
        assertEquals(otherUser.getName(), ridesList.get(0).get("requestorName").textValue());
        assertEquals(otherUser.getPhoneNumber(), ridesList.get(0).get("requestorPhoneNumber").textValue());
        assertEquals(otherUser.getName(), ridesList.get(1).get("requestorName").textValue());
        assertEquals(otherUser.getPhoneNumber(), ridesList.get(1).get("requestorPhoneNumber").textValue());
    }

    @Test
    public void openRidesTESTWithSelfRides() {
        User user = loggedInUser();
        User otherUser = otherUser();
        Ride selfRide = createRide(user.getId());
        Ride maleRide2 = createRide(otherUser.getId());
        Result actual = route(fakeRequest(GET, "/openRides?latitude=" + maleRide2.getStartLatitude() + "&longitude=" + maleRide2.getStartLongitude()).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 1;
        assertEquals(knownNumberOfRides, ridesList.size());
        assertEquals(maleRide2.getId().longValue(), ridesList.get(0).get("ride").get("id").longValue());
        assertEquals(otherUser.getName(), ridesList.get(0).get("requestorName").textValue());
        assertEquals(otherUser.getPhoneNumber(), ridesList.get(0).get("requestorPhoneNumber").textValue());
    }


    @Test
    public void openRidesTESTWithOldRides() {
        User user = loggedInUser();
        User otherUser = otherUser();
        Ride oldRide = createRide(otherUser.getId());
        oldRide.setRequestedAt(minutesOld(16));
        oldRide.save();
        Ride maleRide2 = createRide(otherUser.getId());
        Result actual = route(fakeRequest(GET, "/openRides?latitude=" + maleRide2.getStartLatitude() + "&longitude=" + maleRide2.getStartLongitude()).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 1;
        assertEquals(knownNumberOfRides, ridesList.size());
        assertEquals(maleRide2.getId().longValue(), ridesList.get(0).get("ride").get("id").longValue());
        assertEquals(otherUser.getName(), ridesList.get(0).get("requestorName").textValue());
        assertEquals(otherUser.getPhoneNumber(), ridesList.get(0).get("requestorPhoneNumber").textValue());
    }

    @Test
    public void openRidesTESTWithFarAwayRides() {
        User user = loggedInUser();
        User otherUser = otherUser();
        Ride maleRide2 = createRide(otherUser.getId());
        Ride awayRide = createRide(otherUser.getId());
        awayRide.setStartLatitude(maleRide2.getStartLatitude() - 20);
        awayRide.save();
        Result actual = route(fakeRequest(GET, "/openRides?latitude=" + maleRide2.getStartLatitude() + "&longitude=" + maleRide2.getStartLongitude()).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 1;
        assertEquals(knownNumberOfRides, ridesList.size());
        assertEquals(maleRide2.getId().longValue(), ridesList.get(0).get("ride").get("id").longValue());
        assertEquals(otherUser.getName(), ridesList.get(0).get("requestorName").textValue());
        assertEquals(otherUser.getPhoneNumber(), ridesList.get(0).get("requestorPhoneNumber").textValue());
    }

    @Test
    public void getMyCompletedRidesTESTHappyFlow() {
        User user = loggedInUser();
        Ride firstRide = createRide(user.getId());
        GetBikeUtils.sleep(200);
        Ride closedRide1 = createRide(user.getId());
        closedRide1.setRideStatus(RideClosed);
        closedRide1.save();
        Ride secondRide = createRide(user.getId());
        Ride closedRide2 = createRide(user.getId());
        closedRide2.setRideStatus(RideClosed);
        closedRide2.save();
        Result actual = route(fakeRequest(GET, "/getMyCompletedRides").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 2;
        assertEquals(knownNumberOfRides, ridesList.size());
        assertEquals(closedRide2.getId().longValue(), ridesList.get(0).get("ride").get("id").longValue());
        assertEquals(closedRide1.getId().longValue(), ridesList.get(1).get("ride").get("id").longValue());
        assertEquals(user.getName(), ridesList.get(0).get("requestorName").textValue());
        assertEquals(user.getPhoneNumber(), ridesList.get(0).get("requestorPhoneNumber").textValue());
        assertEquals(user.getName(), ridesList.get(1).get("requestorName").textValue());
        assertEquals(user.getPhoneNumber(), ridesList.get(1).get("requestorPhoneNumber").textValue());
    }

    @Test
    public void getRidesGivenByMeTESTHappyFlow() {
        User user = loggedInUser();
        Ride firstRide = createRide(user.getId());
        GetBikeUtils.sleep(200);
        Ride closedRide1 = createRide(user.getId());
        closedRide1.setRiderId(user.getId());
        closedRide1.setRideStatus(RideClosed);
        closedRide1.save();
        Ride secondRide = createRide(user.getId());
        Ride closedRide2 = createRide(user.getId());
        closedRide2.setRiderId(user.getId());
        closedRide2.setRideStatus(RideClosed);
        closedRide2.save();
        Result actual = route(fakeRequest(GET, "/getRidesGivenByMe").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode ridesList = responseObject.get("rides");
        int knownNumberOfRides = 2;
        assertEquals(knownNumberOfRides, ridesList.size());
        assertEquals(closedRide2.getId().longValue(), ridesList.get(0).get("ride").get("id").longValue());
        assertEquals(closedRide1.getId().longValue(), ridesList.get(1).get("ride").get("id").longValue());
        assertEquals(user.getName(), ridesList.get(0).get("requestorName").textValue());
        assertEquals(user.getPhoneNumber(), ridesList.get(0).get("requestorPhoneNumber").textValue());
        assertEquals(user.getName(), ridesList.get(1).get("requestorName").textValue());
        assertEquals(user.getPhoneNumber(), ridesList.get(1).get("requestorPhoneNumber").textValue());
    }

    @Test
    public void ridePathTESTHappyFlow() {
        User user = new User();
        user.setName("Siva Nookala");
        user.setPhoneNumber("8282828282");
        user.setAuthToken(UUID.randomUUID().toString());
        user.save();
        RideLocation firstRideLocation = new RideLocation();
        firstRideLocation.setLatitude(23.45);
        firstRideLocation.setLongitude(57.68);
        List<String> latLongs = new ArrayList<>();
        latLongs.add("hello");
        latLongs.add("hi");
        Ride ride = new Ride();
        ride.save();
        Content html = views.html.ridePath.render(latLongs, firstRideLocation, ride);
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
        Ride firstRide = createRide(user.getId());
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
        Ride firstRide = createRide(user.getId());
        Result actual = route(fakeRequest(GET, "/getRideById?" + Ride.RIDE_ID + "=" + firstRide.getId()).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode rideById = responseObject.get("ride");
        assertTrue(rideById.get("userCustomer").asBoolean());
        assertFalse(rideById.get("userRider").asBoolean());
        assertEquals(firstRide.getId().longValue(), rideById.get("id").longValue());
        assertEquals(firstRide.getStartLatitude(), rideById.get("startLatitude").doubleValue());
        assertEquals(firstRide.getStartLongitude(), rideById.get("startLongitude").doubleValue());
        assertEquals(user.getPhoneNumber(), responseObject.get("requestorPhoneNumber").textValue());
        assertEquals(user.getName(), responseObject.get("requestorName").textValue());
        assertEquals("Address of " + firstRide.getStartLatitude() + "," + firstRide.getStartLongitude(), responseObject.get("requestorAddress").textValue());
    }

    @Test
    public void getCompleteRideByIdTESTHappyFlow() {
        User user = loggedInUser();
        Ride firstRide = createRide(user.getId());
        List<RideLocation> rideLocations = new ArrayList<>();
        double latlongs[] = RideLocationMother.LAT_LONGS;
        for (int i = 0; i < latlongs.length; i += 2) {
            RideLocation rideLocation = RideLocationMother.createRideLocation(firstRide.getId(), latlongs[i], latlongs[i + 1], i);
            rideLocation.save();
            rideLocations.add(rideLocation);
        }
        Result actual = route(fakeRequest(GET, "/getCompleteRideById?" + Ride.RIDE_ID + "=" + firstRide.getId()).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode rideById = responseObject.get("ride");
        assertTrue(rideById.get("userCustomer").asBoolean());
        assertFalse(rideById.get("userRider").asBoolean());
        assertEquals(firstRide.getId().longValue(), rideById.get("id").longValue());
        assertEquals(firstRide.getStartLatitude(), rideById.get("startLatitude").doubleValue());
        assertEquals(firstRide.getStartLongitude(), rideById.get("startLongitude").doubleValue());
        assertEquals(user.getPhoneNumber(), responseObject.get("requestorPhoneNumber").textValue());
        assertEquals(user.getName(), responseObject.get("requestorName").textValue());
        assertEquals("Address of " + firstRide.getStartLatitude() + "," + firstRide.getStartLongitude(), responseObject.get("requestorAddress").textValue());
        assertEquals(rideLocations.size(), responseObject.get("rideLocations").size());
    }

    @Test
    public void getCompleteRideByIdTESTCalledByRider() {
        User user = loggedInUser();
        Ride firstRide = createRide(user.getId());
        User otherUser = otherUser();
        firstRide.setRiderId(otherUser.getId());
        firstRide.save();
        List<RideLocation> rideLocations = new ArrayList<>();
        double latlongs[] = RideLocationMother.LAT_LONGS;
        for (int i = 0; i < latlongs.length; i += 2) {
            RideLocation rideLocation = RideLocationMother.createRideLocation(firstRide.getId(), latlongs[i], latlongs[i + 1], i);
            rideLocation.save();
            rideLocations.add(rideLocation);
        }
        Result actual = route(fakeRequest(GET, "/getCompleteRideById?" + Ride.RIDE_ID + "=" + firstRide.getId()).header("Authorization", otherUser.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        JsonNode rideById = responseObject.get("ride");
        assertFalse(rideById.get("userCustomer").asBoolean());
        assertTrue(rideById.get("userRider").asBoolean());
        assertEquals(firstRide.getId().longValue(), rideById.get("id").longValue());
        assertEquals(firstRide.getStartLatitude(), rideById.get("startLatitude").doubleValue());
        assertEquals(firstRide.getStartLongitude(), rideById.get("startLongitude").doubleValue());
        assertEquals(user.getPhoneNumber(), responseObject.get("requestorPhoneNumber").textValue());
        assertEquals(user.getName(), responseObject.get("requestorName").textValue());
        assertEquals("Address of " + firstRide.getStartLatitude() + "," + firstRide.getStartLongitude(), responseObject.get("requestorAddress").textValue());
        assertEquals(rideLocations.size(), responseObject.get("rideLocations").size());
    }

    @Test
    public void loadNearByRidersTESTHappyFlow() {
        User user = loggedInUser();
        Result actual = route(fakeRequest(GET, "/loadNearByRiders?latitude=23.45&longitude=11.56").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        System.out.println(responseObject.get("riders").toString());
        assertEquals(5, responseObject.get("riders").size());
    }

    @Test
    public void getRideByIdTESTWithInvalidRideId() {
        User user = loggedInUser();
        Result actual = route(fakeRequest(GET, "/getRideById?" + Ride.RIDE_ID + "=" + 221).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("failure", responseObject.get("result").textValue());
    }

    @Test
    public void getRelevantRidersTESTHappyFlow() {
        User user = loggedInUser();
        User rider1 = createRider(23.45, 56.78);
        User rider2 = createRider(23.45, 56.78);
        List<User> actual = RideController.getRelevantRiders(user.getId(), 23.45, 56.78, user.getGender());
        assertEquals(2, actual.size());
        cAssertHasUser(actual, rider1);
        cAssertHasUser(actual, rider2);
    }

    @Test
    public void getRelevantRidersTESTWhenAwayFromUser() {
        User user = loggedInUser();
        User rider1 = createRider(23.45, 56.78);
        User rider2 = createRider(23.45, 56.78);
        List<User> actual = RideController.getRelevantRiders(user.getId(), 53.45, 66.78, user.getGender());
        assertEquals(0, actual.size());
    }

    @Test
    public void getRelevantRidersTESTWhenOneNearAndOneAway() {
        User user = loggedInUser();
        User rider1 = createRider(23.47, 56.79);
        User rider2 = createRider(53.45, 66.78);
        List<User> actual = RideController.getRelevantRiders(user.getId(), 23.45, 56.78, user.getGender());
        assertEquals(1, actual.size());
        cAssertHasUser(actual, rider1);
    }

    @Test
    public void dateWiseFilterTESTWithHappyFlow() {
        User user = loggedInUser();
        user.setName("Siva");
        user.update();
        Ride ride = createRide(user.id);
        Date date = new Date();
        ride.setAcceptedAt(date);
        ride.setSourceAddress("Kavalu , Nellore");
        ride.setActualSourceAddress("Kavali , Nellore");
        ride.setActualDestinationAddress("Ongole , Prakasam");
        ride.setRiderMobileNumber("9988778899");
        ride.setAcceptedAt(date);
        ride.setRideStartedAt(date);
        ride.setRideEndedAt(date);
        ride.setOrderAmount(1000.0);
        ride.setRequestorName("Siva");
        ride.setRiderName("Sudarsi");
        ride.setRideStatus(RideStatus.RideClosed);
        ride.setRiderId(user.id);
        ride.update();
        Result result = route(fakeRequest(GET, "/rideFilter?startDate=" + DateUtils.convertDateToString(new Date(), DateUtils.YYYYMMDD) + "&endDate=" + DateUtils.convertDateToString(new Date(), DateUtils.YYYYMMDD) + "&status=ALL&srcName=" + user.getName()));
        JsonNode actual = jsonFromResult(result);
        assertEquals("8282828282", actual.findPath("riderMobileNumber").textValue());
        assertEquals(user.getName(), actual.findPath("riderName").textValue());
        assertEquals(ride.getOrderAmount(), actual.findPath("orderAmount").asDouble());
        assertEquals(ride.getRideGender(), actual.findPath("rideGender").asText().charAt(0));
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



    private Result requestGetBike(User user, double startLatitude, double startLongitude) {
        ObjectNode requestObjectNode = Json.newObject();
        requestObjectNode.set(Ride.LATITUDE, Json.toJson(startLatitude));
        requestObjectNode.set(Ride.LONGITUDE, Json.toJson(startLongitude));
        requestObjectNode.set("sourceAddress", Json.toJson("Pullareddy Nagar, Kavali"));
        requestObjectNode.set("destinationAddress", Json.toJson("Musunuru, Kavali"));
        requestObjectNode.set("modeOfPayment", Json.toJson("Cash"));
        return route(fakeRequest(POST, "/getBike").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
    }

    private User createRider(double latitude, double longitude) {
        User user = new User();
        user.setLastKnownLatitude(latitude);
        user.setLastKnownLongitude(longitude);
        user.setLastLocationTime(new Date());
        user.setGender('M');
        user.save();
        return user;
    }


    private void cAssertHasUser(List<User> actual, User searchUser) {
        boolean found = false;
        for (User user : actual) {
            if (user.getId().equals(searchUser.getId())) {
                found = true;
                break;
            }
        }
        assertTrue("Could not find user " + searchUser.getId(), found);
    }

}
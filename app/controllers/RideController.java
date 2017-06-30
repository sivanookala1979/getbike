package controllers;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dataobject.*;
import dataobject.Point;
import models.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.libs.ws.ahc.AhcWSClient;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import scala.concurrent.ExecutionContextExecutor;
import utils.*;

import javax.inject.Inject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

import static controllers.UserController.JOINING_BONUS;
import static controllers.WalletController.getWalletAmount;
import static controllers.WalletController.hasPointsInWallet;
import static dataobject.RideStatus.*;
import static utils.DateUtils.isTimePassed;
import static utils.DateUtils.minutesOld;
import static utils.DistanceUtils.round2;
import static utils.GetBikeErrorCodes.*;
import static utils.NumericUtils.increment;
import static utils.NumericUtils.zeroIfNull;
import static utils.SMSHelper.sendSms;
import static utils.SMSHelper.smsPrepare;
/**
 * Created by sivanookala on 21/10/16.
 */
public class RideController extends BaseController {

    @Inject
    FormFactory formFactory;
    @Inject WSClient ws;

    final static double MAX_DISTANCE_IN_KILOMETERS = 6.0;
    public static final double MINIMUM_WALLET_AMOUNT_FOR_ACCEPTING_RIDE = 200.0;
    public static final double FREE_RIDE_MAX_DISCOUNT = 50.0;

    public LinkedHashMap<String, String> rideTableHeaders = getTableHeadersList(new String[]{"Requester Id", "Rider Id", "Rider Status", "Order Distance", "Order Amount", "Requested At", "Accepted At", "Ride Started At", "Ride Ended At", "Start Latitude", "Start Longitude", "Source Address", "Destination Address", "Total Fare", "TaxesAndFees", "Sub Total", "Rouding Off", "Total Bill"}, new String[]{"requestorId", "requestorName", "riderId", "rideStatus", "orderDistance", "orderAmount", "requestedAt", "acceptedAt", "rideStartedAt", "rideEndedAt", "startLatitude", "startLongitude", "sourceAddress", "destinationAddress", "totalFare", "taxesAndFees", "subTotal", "roundingOff", "totalBill"});
    public LinkedHashMap<String, String> rideLocationTableHeaders = getTableHeadersList(new String[]{"", "", "Ride Location", "Ride Id", "Location Time", "Latitude", "Longitude"}, new String[]{"", "", "id", "rideId", "locationTime", "latitude", "longitude"});
    public LinkedHashMap<String, String> nonGeoLocationTableHeaders = getTableHeadersList(new String[]{"Id", "Mobile Number", "Latitude", "Longitude", "Address", "Requested At"}, new String[]{"id", "mobileNumber", "latitude", "longitude", "addressArea", "requestedAt"});
    public LinkedHashMap<String, String> parcelTableHeaders = getTableHeadersList(new String[]{"Id", "Created At", "Pickup Location", "Drop Location", "Pickup Details", "Pickup Contact", "Drop Details", "Drop Contact"}, new String[]{"id", "createdAt", "pickupLocation", "dropLocation", "pickupDetails", "pickupContact", "dropDetails", "dropContact"});

    @BodyParser.Of(BodyParser.Json.class)
    public Result getBike() {
        User user = currentUser();
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        if (user != null) {
            JsonNode locationsJson = request().body().asJson();
            Double startLatitude = locationsJson.get(Ride.LATITUDE).doubleValue();
            Double startLongitude = locationsJson.get(Ride.LONGITUDE).doubleValue();
            Ride ride = null;
            if (user.isRequestInProgress()) {
                Ride previousRide = cancelIfExpired(user, result);
                if (RideRequested.equals(previousRide.getRideStatus()) || RideAccepted.equals(previousRide.getRideStatus())) {
                    ride = previousRide;
                }
            }
            if (ride == null) {
                ride = new Ride();
                ride.setStartLatitude(startLatitude);
                ride.setStartLongitude(startLongitude);
                ride.setSourceAddress(locationsJson.get("sourceAddress").textValue());
                ride.setDestinationAddress(locationsJson.get("destinationAddress").textValue());
                if (locationsJson.has("modeOfPayment")) {
                    ride.setModeOfPayment(locationsJson.get("modeOfPayment").textValue());
                } else {
                    ride.setModeOfPayment("Cash");
                }
                ride.setRequestorId(user.getId());
                ride.setRideStatus(RideRequested);
                ride.setRequestedAt(new Date());
                ride.setRideGender(user.getGender());
                ride.save();
            }
            user.setCurrentRequestRideId(ride.getId());
            user.setRequestInProgress(true);
            user.save();
            result = SUCCESS;
            setJson(objectNode, Ride.RIDE_ID, ride.getId());
            publishRideDetails(user, ride, false);
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Ride cancelIfExpired(User user, String result) {
        Ride previousRide = Ride.find.byId(user.getCurrentRequestRideId());
        if (previousRide != null && isTimePassed(previousRide.getRequestedAt(), new Date(), 15 * 60)) {
            processCancelRide(result, user, previousRide);
        }
        return previousRide;
    }

    public Result acceptRide() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        int errorCode = GENERAL_FAILURE;
        User user = currentUser();
        if (user != null) {
            double walletAmount = getWalletAmount(user);
            if (!hasPointsInWallet(MINIMUM_WALLET_AMOUNT_FOR_ACCEPTING_RIDE, walletAmount)) {
                errorCode = INSUFFICIENT_WALLET_AMOUNT;
            } else if (!user.isValidProofsUploaded()) {
                errorCode = RIDE_VALID_PROOFS_UPLOAD;
            } else {
                if (user.isRideInProgress()) {
                    errorCode = RIDE_ALREADY_IN_PROGRESS;
                } else {
                    Long rideId = getLong(Ride.RIDE_ID);
                    Ride ride = Ride.find.byId(rideId);
                    if (ride != null && user.getId().equals(ride.getRequestorId())) {
                        errorCode = CAN_NOT_ACCEPT_YOUR_OWN_RIDE;
                    } else if (ride != null && "Parcel".equals(ride.getRideType()) && !user.isPrimeRider()) {
                        errorCode = CAN_NOT_ACCEPT_PARCEL;
                    } else if (ride != null && RideRequested.equals(ride.getRideStatus())) {
                        ride.setRideStatus(RideAccepted);
                        ride.setRiderId(user.getId());
                        ride.setAcceptedAt(new Date());
                        ride.save();
                        user.setRideInProgress(true);
                        user.setCurrentRideId(ride.getId());
                        user.save();
                        User requestor = User.find.byId(ride.getRequestorId());
                        IGcmUtils gcmUtils = ApplicationContext.defaultContext().getGcmUtils();
                        gcmUtils.sendMessage(requestor, "Your ride is accepted by " + user.getName() + " ( " + user.getPhoneNumber() + " ) and the rider will be contacting you shortly.", "rideAccepted", ride.getId());
                        sendSms("53732", requestor.getPhoneNumber(), "&F1=" + smsPrepare(requestor.getName()) + "&F2=" + smsPrepare(user.getName() + " (" + user.getPhoneNumber() + ")"));
                        result = SUCCESS;
                    } else {
                        errorCode = RIDE_ALLOCATED_TO_OTHERS;
                    }
                }
            }
        }
        setResult(objectNode, result);
        objectNode.set("errorCode", Json.toJson(errorCode));
        return ok(Json.toJson(objectNode));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result hailCustomer() {
        User user = currentUser();
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        int errorCode = GENERAL_FAILURE;
        if (user != null) {
            double walletAmount = getWalletAmount(user);
            if (!hasPointsInWallet(MINIMUM_WALLET_AMOUNT_FOR_ACCEPTING_RIDE, walletAmount)) {
                errorCode = INSUFFICIENT_WALLET_AMOUNT;
            } else if (!user.isValidProofsUploaded()) {
                errorCode = RIDE_VALID_PROOFS_UPLOAD;
            } else {
                if (user.isRideInProgress()) {
                    errorCode = RIDE_ALREADY_IN_PROGRESS;
                } else {
                    JsonNode locationsJson = request().body().asJson();
                    Double startLatitude = locationsJson.get(Ride.LATITUDE).doubleValue();
                    Double startLongitude = locationsJson.get(Ride.LONGITUDE).doubleValue();
                    Ride ride = new Ride();
                    ride.setStartLatitude(startLatitude);
                    ride.setStartLongitude(startLongitude);
                    ride.setSourceAddress(locationsJson.get("sourceAddress").textValue());
                    ride.setDestinationAddress(locationsJson.get("destinationAddress").textValue());
                    if (locationsJson.has("modeOfPayment")) {
                        ride.setModeOfPayment(locationsJson.get("modeOfPayment").textValue());
                    } else {
                        ride.setModeOfPayment("Cash");
                    }
                    ride.setRiderId(user.getId());
                    ride.setRideGender(user.getGender());
                    User requestor = getRequestor(locationsJson);
                    ride.setRequestorId(requestor.getId());
                    ride.setRideStatus(RideAccepted);
                    ride.setRequestedAt(new Date());
                    ride.setAcceptedAt(ride.getRequestedAt());
                    ride.save();
                    user.setRideInProgress(true);
                    user.setCurrentRideId(ride.getId());
                    user.save();
                    requestor.setCurrentRequestRideId(ride.getId());
                    requestor.setRequestInProgress(true);
                    requestor.save();
                    result = SUCCESS;
                    setJson(objectNode, Ride.RIDE_ID, ride.getId());
                }
            }
        }
        setResult(objectNode, result);
        objectNode.set("errorCode", Json.toJson(errorCode));
        return ok(Json.toJson(objectNode));
    }

    @NotNull
    private User getRequestor(JsonNode locationsJson) {
        if (locationsJson.has("vendorId")) {
            User vendor = User.find.byId(locationsJson.get("vendorId").longValue());
            if (vendor != null)
                return vendor;
        }
        String phoneNumber = locationsJson.get("phoneNumber").textValue();
        User requestor = User.find.where().eq("phoneNumber", phoneNumber).findUnique();
        if (requestor == null) {
            requestor = new User();
            requestor.setPhoneNumber(phoneNumber);
            if (locationsJson.has("name") && StringUtils.isNotNullAndEmpty(locationsJson.get("name").textValue())) {
                requestor.setName(locationsJson.get("name").textValue());
            }
            if (locationsJson.has("email") && StringUtils.isNotNullAndEmpty(locationsJson.get("email").textValue())) {
                requestor.setEmail(locationsJson.get("email").textValue());
            }
            if (locationsJson.has("gender") && StringUtils.isNotNullAndEmpty(locationsJson.get("gender").textValue())) {
                requestor.setGender(locationsJson.get("gender").textValue().charAt(0));
            }
            requestor.save();
            WalletController.processAddBonusPointsToWallet(requestor.getId(), JOINING_BONUS);
        }
        return requestor;
    }


    @BodyParser.Of(BodyParser.Json.class)
    public Result storeLocations() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            JsonNode locationsJson = request().body().asJson();
            for (int i = 0; i < locationsJson.size(); i++) {
                JsonNode location = locationsJson.get(i);
                RideLocation rideLocation = Json.fromJson(location, RideLocation.class);
                Ride ride = Ride.find.byId(rideLocation.getRideId());
                if (ride != null && ride.getRiderId() != null && ride.getRiderId().equals(user.getId())) {
                    rideLocation.setPostedById(user.getId());
                    rideLocation.setReceivedAt(new Date());
                    rideLocation.save();
                    result = SUCCESS;
                }
            }
            setJson(objectNode, "locationCount", locationsJson.size());
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result estimateRide() {
        Ride ride = new Ride();
        User user = currentUser();
        if (user != null) {
            JsonNode locationsJson = request().body().asJson();
            ArrayList<RideLocation> rideLocations = new ArrayList<>();
            for (int i = 0; i < locationsJson.size(); i++) {
                JsonNode location = locationsJson.get(i);
                RideLocation rideLocation = Json.fromJson(location, RideLocation.class);
                rideLocations.add(rideLocation);
            }
            double estimationBuffer = 1.2;
            ride.setOrderDistance(round2(DistanceUtils.distanceKilometers(rideLocations) * estimationBuffer));
            ride.setOrderAmount(DistanceUtils.estimateBasePrice(ride.getOrderDistance()));
        }
        return ok(Json.toJson(ride));
    }

    public Result closeRide() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Long rideId = getLong(Ride.RIDE_ID);
            Ride ride = Ride.find.byId(rideId);
            if (ride != null && RideAccepted.equals(ride.getRideStatus())) {
                ride.setRideStatus(RideClosed);
                List<RideLocation> locations = RideLocation.find.where().eq("rideId", rideId).order("locationTime asc").findList();
                ride.setOrderDistance(DistanceUtils.distanceKilometers(locations));
                User requestor = User.find.byId(ride.getRequestorId());
                int timeInMinutes = DistanceUtils.timeInMinutes(locations);
                ride.setOrderAmount(DistanceUtils.calculateBasePrice(ride.getOrderDistance(), timeInMinutes));
                if (requestor.isSpecialPrice()) {
                    PricingProfile pricingProfile = PricingProfile.find.where().eq("name", requestor.getProfileType()).findUnique();
                    if (pricingProfile != null) {
                        ride.setOrderAmount(DistanceUtils.calculateBasePrice(ride.getOrderDistance(), timeInMinutes, pricingProfile));
                    }
                }
                ride.setTotalFare(round2(ride.getOrderAmount()));
                ride.setTaxesAndFees(round2(ride.getTotalFare() * 0.40 * 0.066));
                ride.setSubTotal(round2(ride.getTotalFare() + ride.getTaxesAndFees()));
                ride.setRoundingOff(round2((ride.getSubTotal() - ride.getSubTotal().intValue())));
                ride.setTotalBill(round2((double) ride.getSubTotal().intValue()));
                if (locations.size() >= 2) {
                    RideLocation firstLocation = locations.get(0);
                    RideLocation lastLocation = locations.get(locations.size() - 1);
                    ride.setRideStartedAt(firstLocation.getLocationTime());
                    ride.setRideEndedAt(lastLocation.getLocationTime());
                    updateAddresses(firstLocation, lastLocation, ride);
                } else {
                    ride.setRideEndedAt(new Date());
                }
                ride.save();
                cleanRider(user);
                cleanRequestor(requestor);
                addRideWalletEntry(user, ride);
                processFreeRide(ride, requestor, user);
                IGcmUtils gcmUtils = ApplicationContext.defaultContext().getGcmUtils();
                gcmUtils.sendMessage(requestor, "Your ride is now closed.", "rideClosed", ride.getId());
                sendSms("53731", requestor.getPhoneNumber(), "&F1=" + ride.getTotalBill());
                objectNode.set("ride", Json.toJson(ride));
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    private void processFreeRide(Ride ride, User requestor, User rider) {
        if (zeroIfNull(requestor.getFreeRidesEarned()) > zeroIfNull(requestor.getFreeRidesSpent())) {
            requestor.setFreeRidesSpent(increment(requestor.getFreeRidesSpent()));
            requestor.save();
            ride.setFreeRide(true);
            double riderBonus = Math.min(FREE_RIDE_MAX_DISCOUNT, ride.getTotalBill());
            ride.setFreeRideDiscount(riderBonus);
            ride.save();
            Wallet wallet = new Wallet();
            wallet.setUserId(rider.getId());
            wallet.setAmount(WalletController.convertToWalletAmount(riderBonus));
            wallet.setTransactionDateTime(new Date());
            wallet.setDescription("Free Ride Given with Trip ID : " + ride.getId() + " for Rs. " + riderBonus);
            wallet.setType(WalletEntryType.FREE_RIDE);
            wallet.save();
            if (StringUtils.isNotNullAndEmpty(requestor.getSignupPromoCode()) && zeroIfNull(requestor.getFreeRidesEarned()) == 1) {
                User referrer = User.find.where().eq("promoCode", requestor.getSignupPromoCode()).findUnique();
                if (referrer != null) {
                    referrer.setFreeRidesEarned(increment(referrer.getFreeRidesEarned()));
                    referrer.save();
                }
            }
        }
    }

    private void cleanRequestor(User requestor) {
        requestor.setRequestInProgress(false);
        requestor.setCurrentRequestRideId(null);
        requestor.save();
    }

    private void cleanRider(User rider) {
        rider.setRideInProgress(false);
        rider.setCurrentRideId(null);
        rider.save();
    }

    private void addRideWalletEntry(User user, Ride ride) {
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setAmount(-ride.getTotalBill());
        wallet.setTransactionDateTime(new Date());
        wallet.setDescription("Given Ride with Trip ID : " + ride.getId() + " for Rs. " + ride.getTotalBill());
        wallet.setType(WalletEntryType.RIDE_GIVEN);
        wallet.save();
    }

    public Result startRide() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Long rideId = getLong(Ride.RIDE_ID);
            Ride ride = Ride.find.byId(rideId);
            if (ride != null && ride.getRiderId().equals(user.getId()) && RideAccepted.equals(ride.getRideStatus())) {
                ride.setRideStarted(true);
                ride.setRideStartedAt(new Date());
                ride.save();
                sendLocationToRequestor(user, ride);
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public static void sendLocationToRequestor(User rider, Ride ride) {
        if (rider != null && ride != null && ride.getRequestorId() != null) {
            User requestor = User.find.byId(ride.getRequestorId());
            if (requestor != null) {
                IGcmUtils gcmUtils = ApplicationContext.defaultContext().getGcmUtils();
                gcmUtils.sendMessage(requestor, rider.getLastKnownLatitude() + "," + rider.getLastKnownLongitude() + "," + ride.isRideStarted(), "riderLocation", ride.getId());
            }
        }
    }


    public Result cancelRide() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Long rideId = getLong(Ride.RIDE_ID);
            Ride ride = Ride.find.byId(rideId);
            result = processCancelRide(result, user, ride);
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public String processCancelRide(String result, User requestor, Ride ride) {
        if (ride != null && ride.getRequestorId().equals(requestor.getId())) {
            boolean rideRequested = RideRequested.equals(ride.getRideStatus());
            boolean rideNotStarted = (RideAccepted.equals(ride.getRideStatus()) && !ride.isRideStarted());
            if (rideRequested || rideNotStarted) {
                ride.setRideStatus(RideCancelled);
                ride.save();
                cleanRequestor(requestor);
                if (ride.getRiderId() != null) {
                    User rider = User.find.byId(ride.getRiderId());
                    cleanRider(rider);
                    IGcmUtils gcmUtils = ApplicationContext.defaultContext().getGcmUtils();
                    gcmUtils.sendMessage(rider, "Ride " + ride.getId() + " is cancelled.", "rideCancelled", ride.getId());
                }
                result = SUCCESS;
            }
        }
        return result;
    }

    public Result ridePath() {
        Long rideId = getLong(Ride.RIDE_ID);
        List<String> rideLocationStrings = new ArrayList<>();
        List<RideLocation> rideLocations = RideLocation.find.where().eq("rideId", rideId).order("locationTime asc").findList();
        Logger.info("Ride Locations  " + rideLocationStrings);
        RideLocation firstLocation = new RideLocation();
        if (rideLocations.size() > 0) {
            firstLocation = rideLocations.get(0);
        }
        for (RideLocation rideLocation : rideLocations) {
            rideLocationStrings.add("{lat: " + rideLocation.getLatitude() +
                    ", lng: " + rideLocation.getLongitude() +
                    "}");
        }
        Ride ride = Ride.find.where().eq("id", rideId).findUnique();
        String sourceAddress = "";
        String destinationAddress = "";
        String lastSixWordsFromDestinationAddress = "";
        Logger.info("Ride firstLocation  " + firstLocation);
        if (ride.getRideStatus().equals(RideClosed) && rideLocationStrings.isEmpty()) {
            sourceAddress = ride.getSourceAddress();
            destinationAddress = ride.getDestinationAddress();
            Logger.info("Ride Locations  sourceAddress : " + sourceAddress);
            Logger.info("Ride Locations  destinationAddress " + destinationAddress);
            String[] tokens = destinationAddress.split(" ");
            lastSixWordsFromDestinationAddress = tokens[tokens.length - 8] + " " + tokens[tokens.length - 7] + " " + tokens[tokens.length - 6] + " " + tokens[tokens.length - 5] + " " + tokens[tokens.length - 4] + " " + tokens[tokens.length - 3] + " " + tokens[tokens.length - 2] + " " + tokens[tokens.length - 1];
            Logger.info("Ride Locations  last:  " + lastSixWordsFromDestinationAddress);
        }
        loadNames(ride);
        return ok(views.html.ridePath.render(rideLocationStrings, firstLocation, ride, sourceAddress.replaceAll("[^a-zA-Z ;]+", ""), lastSixWordsFromDestinationAddress.replaceAll("[^a-zA-Z ;]+", "")));
    }

    public Result openRides() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            ArrayNode ridesNodes = Json.newArray();
            double latitude = getDouble("latitude");
            double longitude = getDouble("longitude");
            List<Ride> openRides = Ride.find.where().eq("rideStatus", RideRequested).ge("requestedAt", minutesOld(15)).raw("ride_gender = '" + user.getGender() + "' and ride_type != 'Parcel' and requestor_id != " + user.getId() + " and ( 3959 * acos( cos( radians(" + latitude +
                    ") ) * cos( radians(start_latitude) ) " +
                    "   * cos( radians(start_longitude) - radians(" + longitude +
                    ")) + sin(radians(" + latitude + ")) " +
                    "   * sin( radians(start_latitude)))) < " +
                    MAX_DISTANCE_IN_KILOMETERS + " ").setMaxRows(5).order("requestedAt desc").findList();
            if (user.isPrimeRider()) {
                List<Ride> parcelRides = Ride.find.where().eq("rideStatus", RideRequested).ge("requestedAt", minutesOld(15)).raw("ride_type = 'Parcel' and requestor_id != " + user.getId() + " and ( 3959 * acos( cos( radians(" + latitude +
                        ") ) * cos( radians(start_latitude) ) " +
                        "   * cos( radians(start_longitude) - radians(" + longitude +
                        ")) + sin(radians(" + latitude + ")) " +
                        "   * sin( radians(start_latitude)))) < " +
                        MAX_DISTANCE_IN_KILOMETERS + " ").setMaxRows(5).order("requestedAt asc").findList();
                openRides.addAll(0, parcelRides);
            }
            for (Ride ride : openRides) {
                ObjectNode rideNode = Json.newObject();
                rideNode.set("ride", Json.toJson(ride));
                User requestor = User.find.byId(ride.getRequestorId());
                rideNode.set("requestorPhoneNumber", Json.toJson(requestor.getPhoneNumber()));
                rideNode.set("requestorName", Json.toJson(requestor.getName()));
                rideNode.set("requestorAddress", Json.toJson("Address of " + ride.getStartLatitude() + "," + ride.getStartLongitude()));
                ridesNodes.add(rideNode);
            }

            List<Ride> allGroupRides = Ride.find.where().eq("is_group_ride", true).eq("rider_id" , user.id).eq("ride_status" , RideAccepted).findList();
            ArrayNode groupNodes = Json.newArray();
            for(Ride ride :allGroupRides){
                ObjectNode groupRide = Json.newObject();
                List<Ride> rideList = Ride.find.where().eq("group_ride_id", ride.id).findList();
                groupRide.set("groupId" , Json.toJson(ride.id));
                groupRide.set("firstRide" , Json.toJson(rideList.get(0)));
                groupRide.set("lastRide" , Json.toJson(rideList.get(rideList.size()-1)));
                groupRide.set("isGroupRide" , Json.toJson(true));
                groupRide.set("numberOfRides" , Json.toJson(rideList.size()));
                groupNodes.add(groupRide);
            }
            objectNode.set("groupRides" , groupNodes);
            objectNode.set("rides", ridesNodes);
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result getMyCompletedRides() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            ArrayNode ridesNodes = Json.newArray();
            List<Ride> closedRides = Ride.find.where().eq("rideStatus", RideClosed).eq("requestorId", user.getId()).setMaxRows(5).order("requestedAt desc").findList();
            for (Ride ride : closedRides) {
                ObjectNode rideNode = Json.newObject();
                rideNode.set("ride", Json.toJson(ride));
                User requestor = User.find.byId(ride.getRequestorId());
                rideNode.set("requestorPhoneNumber", Json.toJson(requestor.getPhoneNumber()));
                rideNode.set("requestorName", Json.toJson(requestor.getName()));
                rideNode.set("requestorAddress", Json.toJson("Address of " + ride.getStartLatitude() + "," + ride.getStartLongitude()));
                ridesNodes.add(rideNode);
            }
            objectNode.set("rides", ridesNodes);
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result getRidesGivenByMe() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            ArrayNode ridesNodes = Json.newArray();
            List<Ride> closedRides = Ride.find.where().eq("rideStatus", RideClosed).eq("riderId", user.getId()).setMaxRows(10).order("requestedAt desc").findList();
            for (Ride ride : closedRides) {
                ObjectNode rideNode = Json.newObject();
                rideNode.set("ride", Json.toJson(ride));
                User requestor = User.find.byId(ride.getRequestorId());
                rideNode.set("requestorPhoneNumber", Json.toJson(requestor.getPhoneNumber()));
                rideNode.set("requestorName", Json.toJson(requestor.getName()));
                rideNode.set("requestorAddress", Json.toJson("Address of " + ride.getStartLatitude() + "," + ride.getStartLongitude()));
                ridesNodes.add(rideNode);
            }
            objectNode.set("rides", ridesNodes);
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result currentRide() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Ride currentRide = Ride.find.where().eq("riderId", user.getId()).eq("rideStatus", RideAccepted).findUnique();
            if (currentRide != null) {
                objectNode.set("ride", Json.toJson(currentRide));
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result rateRide() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Long rideId = getLong(Ride.RIDE_ID);
            Ride ride = Ride.find.byId(rideId);
            if (ride != null && ride.getRequestorId().equals(user.getId())) {
                ride.setRating(getInt("rating"));
                ride.save();
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result updatePaymentStatus() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Long rideId = getLong(Ride.RIDE_ID);
            Ride ride = Ride.find.byId(rideId);
            if (ride != null && user.getId().equals(ride.getRiderId())) {
                ride.setPaid(true);
                ride.save();
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result getRideById() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Ride rideById = Ride.find.byId(getLong(Ride.RIDE_ID));
            if (rideById != null) {
                updateRiderOrCustomer(user, rideById);
                objectNode.set("ride", Json.toJson(rideById));
                User requestor = User.find.byId(rideById.getRequestorId());
                objectNode.set("requestorPhoneNumber", Json.toJson(requestor.getPhoneNumber()));
                objectNode.set("requestorName", Json.toJson(requestor.getName()));
                objectNode.set("requestorAddress", Json.toJson("Address of " + rideById.getStartLatitude() + "," + rideById.getStartLongitude()));
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }


    public Result getCompleteRideById() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Ride rideById = Ride.find.byId(getLong(Ride.RIDE_ID));
            if (rideById != null) {
                updateRiderOrCustomer(user, rideById);
                objectNode.set("ride", Json.toJson(rideById));
                User requestor = User.find.byId(rideById.getRequestorId());
                objectNode.set("requestorPhoneNumber", Json.toJson(requestor.getPhoneNumber()));
                objectNode.set("requestorName", Json.toJson(requestor.getName()));
                objectNode.set("requestorAddress", Json.toJson("Address of " + rideById.getStartLatitude() + "," + rideById.getStartLongitude()));
                objectNode.set("rideLocations", Json.toJson(RideLocation.find.where().eq("rideId", rideById.getId()).order("locationTime asc").findList()));
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    private void updateRiderOrCustomer(User user, Ride rideById) {
        rideById.setUserCustomer(user.getId().equals(rideById.getRequestorId()));
        rideById.setUserRider(user.getId().equals(rideById.getRiderId()));
    }

    public Result loadNearByRiders() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Double latitude = getDouble("latitude");
            Double longitude = getDouble("longitude");
            List<RideLocation> riders = new ArrayList<>();
            List<User> relevantRiders = getRelevantRiders(user.getId(), latitude, longitude, user.getGender(), false);
            int i = 0;
            for (User relevantRider : relevantRiders) {
                RideLocation randomLocation = new RideLocation();
                randomLocation.setLatitude(relevantRider.getLastKnownLatitude());
                randomLocation.setLongitude(relevantRider.getLastKnownLongitude());
                riders.add(randomLocation);
                i++;
            }
            for (; i < 5; i++) {
                RideLocation randomLocation = new RideLocation();
                randomLocation.setLatitude(noise(latitude, 0.004));
                randomLocation.setLongitude(noise(longitude, 0.001));
                riders.add(randomLocation);
            }
            objectNode.set("riders", Json.toJson(riders));
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result geoFencingAreaValidation() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Double userLatitude = getDouble("latitude");
            Double userLongitude = getDouble("longitude");
            List<GeoFencingLocation> allGeoFencingLocations = GeoFencingLocation.find.all();
            if (allGeoFencingLocations.size() > 0) {
                List<GeoFencingLocation> locationArrayList = new ArrayList<>();
                for (GeoFencingLocation geoFencingLocationList : allGeoFencingLocations) {
                    GeoFencingLocation geoFencingLocation = GeoFencingLocation.find.byId(geoFencingLocationList.getId());
                    locationArrayList.add(geoFencingLocation);
                    double geoLatitude = geoFencingLocation.getLatitude();
                    double geoLongitude = geoFencingLocation.getLongitude();
                    int radius = geoFencingLocation.getRadius();
                    double distance = getDistanceFromLatLngInKm(userLatitude, userLongitude, geoLatitude, geoLongitude);
                    if (distance < radius) {
                        result = SUCCESS;
                    }
                    objectNode.set("locations", Json.toJson(locationArrayList));
                }
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public double getDistanceFromLatLngInKm(double userLatitude, double userLongitude, double geoLatitude, double geoLongitude) {
        int radius = 6371; // Radius of the earth in km

        double dLat = deg2rad(geoLatitude - userLatitude);
        double dLon = deg2rad(geoLongitude - userLongitude);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(deg2rad(userLatitude)) * Math.cos(deg2rad(geoLatitude)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radius * c; // Distance in km
    }

    public double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }


    private double noise(Double latitude, double factor) {

        return latitude * ((1 - factor) + (factor * 2 * Math.random()));
    }


    private void publishRideDetails(User user, Ride ride, boolean onlyPrimeRiders) {
        IGcmUtils gcmUtils = ApplicationContext.defaultContext().getGcmUtils();
        gcmUtils.sendMessage(getRelevantRiders(user.getId(), ride.getStartLatitude(), ride.getStartLongitude(), user.getGender(), onlyPrimeRiders), "A new ride request with ride Id " + ride.getId() + " is active.", "newRide", ride.getId());
    }

    public static List<User> getRelevantRiders(Long currentId, Double latitude, Double longitude, char gender, boolean onlyPrimeRiders) {
        ExpressionList<User> relevantRiders = User.find.where().eq("isRideInProgress", false);
        if (onlyPrimeRiders) {
            relevantRiders = relevantRiders.eq("primeRider", true);
        }
        return relevantRiders.eq("validProofsUploaded", true).ge("lastLocationTime", minutesOld(15)).eq("isRequestInProgress", false).raw("( 3959 * acos( cos( radians(" + latitude +
                ") ) * cos( radians( last_known_latitude ) ) " +
                "   * cos( radians(last_known_longitude) - radians(" + longitude +
                ")) + sin(radians(" + latitude + ")) " +
                "   * sin( radians(last_known_latitude)))) < " +
                MAX_DISTANCE_IN_KILOMETERS + " ").raw("gender = '" + gender + "'").gt("lastKnownLatitude", 0.0).gt("lastKnownLongitude", 0.0).not().eq("id", currentId).findList();
    }

    public Result rideList() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.rideList.render(rideTableHeaders, "col-sm-12", "", "Ride", "", "", ""));
    }

    public Result pendingList() {
        List<String> profileTypes = new ArrayList<>();
        for (PricingProfile user : PricingProfile.find.all()) {
            if (isNotNullAndEmpty(user.getName())) {
                profileTypes.add(user.getName());
            }
        }

        return ok(views.html.pendingList.render(profileTypes, rideTableHeaders, "col-sm-12", "", "Ride", "", "", ""));
    }

    public Result rideLocationList() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.rideLocationList.render(rideLocationTableHeaders));
    }

    public Result performSearch(String name) {
        List<Ride> rideList = Ride.find.all();
        return ok(Json.toJson(rideList));
    }


    public Result performSearch1(String name) {
        List<RideLocation> rideLocationList = RideLocation.find.all();
        return ok(Json.toJson(rideLocationList));
    }

    public Result dateWiseFilter() {
        int numberOfRides = 0;
        double totalDistance = 0.0;
        double totalAmount = 0.0;
        int noOfCompleted = 0;
        int noOfPending = 0;
        int noOfaccepted = 0;
        String startDate = request().getQueryString("startDate");
        String endDate = request().getQueryString("endDate");
        String status = request().getQueryString("status");
        String srcName = request().getQueryString("srcName");
        Logger.debug("Start Date is " + startDate + " End date is " + endDate);
        if ("ALL".equals(status) || "null".equals(status)) {
            status = null;
        }
        List<Ride> listOfRides = new ArrayList<>();
        List<Object> listOfIds = new ArrayList<>();
        ExpressionList<Ride> rideQuery = null;
        if (isNotNullAndEmpty(srcName)) {
            listOfIds = User.find.where().or(Expr.like("lower(name)", "%" + srcName.toLowerCase() + "%"), Expr.like("lower(phoneNumber)", "%" + srcName.toLowerCase() + "%")).findIds();
            if (listOfIds.size() == 0) {
                rideQuery = Ride.find.where().or(Expr.or(Expr.in("requestorId", listOfIds), Expr.in("riderId", listOfIds)), Expr.idEq(Long.valueOf(srcName)));
            } else {
                rideQuery = Ride.find.where().or(Expr.in("requestorId", listOfIds), Expr.in("riderId", listOfIds));
            }

        } else {
            rideQuery = Ride.find.where();
        }
        if (isNotNullAndEmpty(status) && isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate)) {
            listOfRides = rideQuery.between("requested_at", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).eq("ride_status", status).orderBy("requested_at desc").findList();
        } else if (isNotNullAndEmpty(status) && !isNotNullAndEmpty(startDate) && !isNotNullAndEmpty(endDate)) {
            listOfRides = rideQuery.eq("ride_status", status).orderBy("requested_at desc").findList();
        } else if (!isNotNullAndEmpty(status) && isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate)) {
            listOfRides = rideQuery.between("requested_at", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).orderBy("requested_at desc").findList();
        } else if (!isNotNullAndEmpty(status) && !isNotNullAndEmpty(startDate) && !isNotNullAndEmpty(endDate)) {
            listOfRides = rideQuery.orderBy("requested_at desc").findList();
        }
        for (Ride ride : listOfRides) {
            loadNames(ride);
            if (ride.getRequestedAt() != null) {
                ride.setFormatedRequestAt(ride.getRequestedAt());
            }
            if (ride.getAcceptedAt() != null) {
                ride.setFormatedAcceptedAt(ride.getAcceptedAt());
            }
            if (ride.isPaid()) {
                ride.setRidePaymentStatus("Success");
            } else {
                ride.setRidePaymentStatus("Pending");
            }
            if (ride.getRideStartedAt() != null) {
                ride.setFormatedRideStartedAt(ride.getRideStartedAt());
            }
            if (ride.getRideEndedAt() != null) {
                ride.setFormatedRideEndedAt(ride.getRideEndedAt());
            }
            if (ride.getOrderDistance() != null) {
                totalDistance = totalDistance + ride.getOrderDistance();
            }
            if (ride.getTotalBill() != null) {
                totalAmount = totalAmount + ride.getTotalBill();
            }
            if (ride.getRideStatus().equals(RideStatus.RideRequested)) {
                Logger.info("Inside Ride req");
                noOfPending++;
            }
            if (ride.getRideStatus().equals(RideStatus.RideAccepted)) {
                ride.setRiderMobileNumber(User.find.where().eq("id", ride.getRiderId()).findUnique().getPhoneNumber());
                Logger.info("Inside Ride acc");
                noOfaccepted++;
            }
            if (ride.getRideStatus().equals(RideStatus.RideClosed)) {
                ride.setRiderMobileNumber(User.find.where().eq("id", ride.getRiderId()).findUnique().getPhoneNumber());
                Logger.info("Inside Ride clo");
                noOfCompleted++;
            }
        }
        numberOfRides = listOfRides.size();
        JSONObject obj = new JSONObject();
        obj.put("numberOfRides", numberOfRides);
        obj.put("totalDistance", round2(totalDistance));
        obj.put("totalAmount", round2(totalAmount));
        obj.put("pending", noOfPending);
        obj.put("accepted", noOfaccepted);
        obj.put("closed", noOfCompleted);
        ObjectNode objectNode = Json.newObject();
        setJson(objectNode, "rideSummary", obj);
        List<Ride> list = new ArrayList<>();
        for (Ride ride : listOfRides) {
            if (ride.getActualDestinationAddress() != null) {
                ride.setActualDestinationAddress(ride.getActualDestinationAddress().replaceAll(",", " "));
            }
            if (ride.getDestinationAddress() != null) {
                ride.setDestinationAddress(ride.getDestinationAddress().replaceAll(",", " "));
            }
            if (ride.getSourceAddress() != null) {
                ride.setSourceAddress(ride.getSourceAddress().replaceAll(",", " "));
            }
            if (ride.getActualSourceAddress() != null) {
                ride.setActualSourceAddress(ride.getActualSourceAddress().replaceAll(",", " "));
            }
            if (ride.getRequestorId() != null) {
                ride.setCustomerMobileNumber(User.find.where().eq("id", ride.getRequestorId()).findUnique().phoneNumber);
            }
            list.add(ride);
        }
        setResult(objectNode, list);
        return ok(Json.toJson(objectNode));
    }

    private void loadNames(Ride ride) {
        if (ride.getRequestorId() != null) {
            ride.setRequestorName(User.find.where().eq("id", ride.getRequestorId()).findUnique().getDisplayName());
        } else {
            ride.setRequestorName("Not Provided");
        }
        if (ride.getRiderId() != null) {
            ride.setRiderName(User.find.where().eq("id", ride.getRiderId()).findUnique().getDisplayName());
        } else {
            ride.setRiderName("Not Provided");
        }
    }

    private void updateAddresses(RideLocation firstLocation, RideLocation lastLocation, Ride ride) {
        Consumer<WSResponse> destinationAddressConsumer = response -> {
            Ride currentRide = Ride.find.byId(ride.id);
            currentRide.setActualDestinationAddress(response.asJson().get("results").get(0).get("formatted_address").asText());
            currentRide.update();
            System.out.println("Updated actual destination address");
        };
        Consumer<WSResponse> sourceAddressConsumer = response -> {
            Ride currentRide = Ride.find.byId(ride.id);
            currentRide.setActualSourceAddress(response.asJson().get("results").get(0).get("formatted_address").asText());
            currentRide.update();
            updateAddressByLatitudeAndLogitude(lastLocation, destinationAddressConsumer);
            System.out.println("Updated actual source address");
        };
        updateAddressByLatitudeAndLogitude(firstLocation, sourceAddressConsumer);
    }

    private void updateAddressByLatitudeAndLogitude(RideLocation location, Consumer<WSResponse> addressConsumer) {
        AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
                .setMaxRequestRetry(0)
                .setShutdownQuietPeriod(0)
                .setShutdownTimeout(0).build();
        String name = "wsclient";
        ActorSystem system = ActorSystem.create(name);
        ActorMaterializerSettings settings = ActorMaterializerSettings.create(system);
        ActorMaterializer materializer = ActorMaterializer.create(settings, system, name);
        WSClient client = new AhcWSClient(config, materializer);
        client.url("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.getLatitude() + "," + location.getLongitude() + "&key=AIzaSyDxqQEvtdEtl6dDIvG7vcm6QTO45Si0FZs").get().whenComplete((r, e) -> {

            Optional.ofNullable(r).ifPresent(addressConsumer);
        }).thenRun(() -> {

            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).thenRun(system::terminate);
    }

    public Result addGeoFencingLocation() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.geoFencingLocation.render());
    }

    public Result saveGeoFencingLocation() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        GeoFencingLocation location = new GeoFencingLocation();
        DynamicForm dynamicForm = formFactory.form().bindFromRequest();
        location.setAddressArea(dynamicForm.get("addressArea"));
        location.setLatitude(Double.parseDouble(dynamicForm.get("latitude")));
        location.setLongitude(Double.parseDouble(dynamicForm.get("longitude")));
        location.setRadius(Integer.parseInt(dynamicForm.get("radius")));
        location.save();
        return redirect("/allFencinglocations");
    }

    public Result viewGeoFencingLocation() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        ObjectNode objectNode = Json.newObject();
        List<GeoFencingLocation> location = GeoFencingLocation.find.orderBy("id").findList();
        objectNode.put("size", GeoFencingLocation.find.all().size());
        setResult(objectNode, location);
        return ok(Json.toJson(objectNode));
    }

    public Result getAllGeoFencingLocations() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.geoFencingLocationList.render());
    }

    public Result editGeoFencinglocations(Long id) {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        GeoFencingLocation geoFencingLocation = GeoFencingLocation.find.byId(id);
        return ok(views.html.updateGeoFencingLocation.render(geoFencingLocation));
    }

    public Result updateGeoFencinglocations() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        DynamicForm requestData = formFactory.form().bindFromRequest();
        String geoId = requestData.get("id");
        String addressArea = requestData.get("addressArea");
        String latitude = requestData.get("latitude");
        String longitude = requestData.get("longitude");
        String radius = requestData.get("radius");
        GeoFencingLocation geoFencingLocation = GeoFencingLocation.find.byId(Long.valueOf(geoId));
        geoFencingLocation.setAddressArea(addressArea);
        geoFencingLocation.setLatitude(Double.parseDouble(latitude));
        geoFencingLocation.setLongitude(Double.parseDouble(longitude));
        geoFencingLocation.setRadius(Integer.parseInt(radius));
        geoFencingLocation.update();
        return redirect("/allFencinglocations");
    }

    public Result deleteGeoFencinglocations(Long id) {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        GeoFencingLocation geoFencingLocation = GeoFencingLocation.find.byId(id);
        geoFencingLocation.delete();
        return redirect("/allFencinglocations");
    }

    public Result addOfflineTrip() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.addOfflineTrip.render());
    }

    public Result saveOfflineTrip() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        DynamicForm dynamicForm = formFactory.form().bindFromRequest();
        Ride ride = new Ride();
        ride.setRiderMobileNumber(dynamicForm.get("riderMobileNumber"));
        ride.setCustomerMobileNumber(dynamicForm.get("customerMobileNumber"));
        ride.setActualSourceAddress(dynamicForm.get("actualSourceAddress"));
        ride.setActualDestinationAddress(dynamicForm.get("actualDestinationAddress"));
        ride.setOrderDistance(Double.parseDouble(dynamicForm.get("orderDistance")));
        ride.setTotalBill(Double.parseDouble(dynamicForm.get("totalBill")));
        ride.setStartLatitude(17.3850);
        ride.setStartLongitude(78.4867);
        ride.setModeOfPayment("Cash-Offline trip");
        ride.setRideStatus(RideStatus.RideClosed);
        ride.setPaid(true);
        User riderUser = User.find.where().eq("phoneNumber", dynamicForm.get("riderMobileNumber")).findUnique();
        if (riderUser != null) {
            ride.setRiderId(riderUser.getId());
            ride.setRiderName(riderUser.getName());
        }
        User customerUser = User.find.where().eq("phoneNumber", dynamicForm.get("customerMobileNumber")).findUnique();
        if (customerUser != null) {
            ride.setRequestorId(customerUser.getId());
            ride.setRequestorName(customerUser.getName());
        }
        ride.setRequestedAt(DateUtils.getDateFromString(dynamicForm.get("startTime")));
        ride.setAcceptedAt(DateUtils.getDateFromString(dynamicForm.get("startTime")));
        ride.setRideStartedAt(DateUtils.getDateFromString(dynamicForm.get("startTime")));
        ride.setRideEndedAt(DateUtils.getDateFromString(dynamicForm.get("startTime")));
        ride.save();
        return redirect("/ride/rideList");
    }

    public Result userRequestFromNonGeoFencingLocation() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            JsonNode locationsJson = request().body().asJson();
            NonGeoFencingLocation nonGeoFencingLocation = new NonGeoFencingLocation();
            nonGeoFencingLocation.setMobileNumber(user.getPhoneNumber());
            nonGeoFencingLocation.setLatitude(locationsJson.get("latitude").doubleValue());
            nonGeoFencingLocation.setLongitude(locationsJson.get("longitude").doubleValue());
            nonGeoFencingLocation.setAddressArea(locationsJson.get("addressArea").textValue());
            nonGeoFencingLocation.setRequestedAt(new Date());
            nonGeoFencingLocation.save();
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result dateWiseFilterForNonGeoFencingLocations() {

        String startDate = request().getQueryString("startDate");
        String endDate = request().getQueryString("endDate");
        String srcNumber = request().getQueryString("srcNumber");
        List<NonGeoFencingLocation> nonGeoFencingLocationList = new ArrayList<>();
        List<Object> listOfIds = new ArrayList<>();
        ExpressionList<NonGeoFencingLocation> nonGeoeLoactionQuery = null;

        if (isNotNullAndEmpty(srcNumber)) {
            listOfIds = NonGeoFencingLocation.find.where().or(Expr.like("lower(mobileNumber)", "%" + srcNumber.toLowerCase() + "%"), Expr.like("lower(mobileNumber)", "%" + srcNumber.toLowerCase() + "%")).orderBy("id").findIds();
            nonGeoeLoactionQuery = NonGeoFencingLocation.find.where().or(Expr.in("id", listOfIds), Expr.in("id", listOfIds));
        } else {
            nonGeoeLoactionQuery = NonGeoFencingLocation.find.where();
        }
        if (isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate)) {
            nonGeoFencingLocationList = nonGeoeLoactionQuery.between("requested_at", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).orderBy("id").findList();
        } else if (!isNotNullAndEmpty(startDate) && !isNotNullAndEmpty(endDate)) {
            nonGeoFencingLocationList = nonGeoeLoactionQuery.orderBy("id").findList();
        }

        ObjectNode objectNode = Json.newObject();

        setResult(objectNode, nonGeoFencingLocationList);
        return ok(Json.toJson(objectNode));
    }

    public Result allNonGeoFencingLocations() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.nonGeoFencingLocationsList.render(nonGeoLocationTableHeaders, "col-sm-12", "", "NonGeoLocation", "", "", ""));

    }

    public Result addParcel() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.addParcel.render(getString("vendorId")));
    }


    public Result saveParcel() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        DynamicForm dynamicForm = formFactory.form().bindFromRequest();
        Ride ride = new Ride();
        ride.setRequestorId(Long.parseLong(dynamicForm.get("vendorId")));
        ride.setSourceAddress(dynamicForm.get("sourceAddress"));
        ride.setParcelPickupNumber(dynamicForm.get("pickupMobileNumber"));
        ride.setDestinationAddress(dynamicForm.get("destinationAddress"));
        ride.setParcelDropoffNumber(dynamicForm.get("dropoffMobileNumber"));
        if (StringUtils.isNullOrEmpty(dynamicForm.get("sourceLatitude"))) {
            ride.setStartLatitude(14.9029817);
            ride.setStartLongitude(79.9944657);
        } else {
            ride.setStartLatitude(Double.parseDouble(dynamicForm.get("sourceLatitude")));
            ride.setStartLongitude(Double.parseDouble(dynamicForm.get("sourceLongitude")));
        }
        ride.setRideStatus(RideStatus.RideRequested);
        ride.setModeOfPayment("Cash");
        ride.setRequestedAt(new Date());
        ride.setParcelRequestRaisedAt(new Date());
        ride.setRideType("Parcel");
        ride.save();
        User vendor = User.find.byId(ride.getRequestorId());
        publishRideDetails(vendor, ride, true);
        return redirect("/ride/rideList");
    }

    public Result saveParcelEntries() {
        ObjectNode objectNode = Json.newObject();
        try {
            JsonNode json = request().body().asJson();
            JsonNode parcelJson = (JsonNode) json.findPath("parcelData");
            ArrayNode parcelArray = (ArrayNode) parcelJson;
            Logger.info("Date ----" + json.findPath("createdAt").asText().isEmpty());
            if (!json.findPath("createdAt").asText().isEmpty()) {
                for (int i = 0; i < parcelArray.size(); i++) {
                    JsonNode jsonNode = parcelArray.get(i);
                    Ride ride = new Ride();
                    Logger.info("Vendor Name ------" + session("vendorName"));
                    ride.setRequestorName(session("vendorName"));
                    ride.setRequestorId(User.find.where().eq("email", session("vendorName")).findUnique().getId());
                    ride.setParcelOrderId(jsonNode.get("orderId").asText());
                    ride.setCodAmount(jsonNode.get("codAmount").asDouble());
                    ride.setSourceAddress(jsonNode.get("pickupLocation").asText());
                    ride.setDestinationAddress(jsonNode.get("dropLocation").asText());
                    ride.setParcelPickupNumber(jsonNode.get("pickupContact").asText());
                    ride.setParcelDropoffNumber(jsonNode.get("dropContact").asText());
                    ride.setStartLatitude(jsonNode.get("startLat").asDouble());
                    ride.setStartLongitude(jsonNode.get("startLong").asDouble());
                    ride.setRideStatus(RideStatus.RideRequested);
                    if (ride.getStartLatitude().doubleValue() == 0.0 || ride.getStartLongitude().doubleValue() == 0.0) {
                        ride.setStartLatitude(14.9029817);
                        ride.setStartLongitude(79.9944657);
                    }
                    ride.setModeOfPayment("Cash");
                    ride.setRequestedAt(DateUtils.getDateFromString(jsonNode.get("createdAt").asText()));
                    ride.setParcelRequestRaisedAt(new Date());
                    ride.setParcelPickupDetails(jsonNode.get("pickupDetails").asText());
                    ride.setParcelDropoffDetails(jsonNode.get("dropDetails").asText());
                    ride.setRideType("Parcel");
                    ride.save();
                    User vendor = User.find.byId(ride.getRequestorId());
                    publishRideDetails(vendor, ride, true);
                    Logger.info("----------Parcel Saved---------");
                }
                objectNode.put(SUCCESS, SUCCESS);
            } else {
                objectNode.put(FAILURE, FAILURE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            objectNode.put(FAILURE, FAILURE);
        }
        return ok(objectNode);
    }

    public Result homeScreen() {
        return ok(views.html.vendorHome.render());
    }

    public Result addNewParcelEntries() {
        if (!isValidateVendorSession()) {
            return redirect(routes.LoginController.login());
        }
        System.out.println("Vendor Name------" + session("vendorName"));
        return ok(views.html.addNewParcel.render());
    }

    public Result dateWiseFilterForParcelHistoryList() {
        int numberOfRides = 0;
        double totalDistance = 0.0;
        double totalAmount = 0.0;
        int noOfCompleted = 0;
        int noOfPending = 0;
        int noOfAccepted = 0;
        String startDate = request().getQueryString("startDate");
        String endDate = request().getQueryString("endDate");
        String searchTripId = request().getQueryString("searchTripId");
        String status = request().getQueryString("status");
        Logger.info("Status----------" + status);
        List<Ride> parcelList = new ArrayList<>();
        List<Object> listOfIds = new ArrayList<>();
        if ("ALL".equals(status) || "null".equals(status)) {
            status = null;
        }
        if ("null".equals(searchTripId)) {
            searchTripId = null;
        }
        Long requestorId = User.find.where().eq("email", session("vendorName")).findUnique().getId();
        ExpressionList<Ride> parcelExpressionList = Ride.find.where().eq("requestorId", requestorId);
        if (isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate) && isNotNullAndEmpty(status) && isNotNullAndEmpty(searchTripId)) {
            parcelList = parcelExpressionList.between("requested_at", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).eq("ride_status", status).eq("id", searchTripId).orderBy("requested_at desc").findList();
        } else if (isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate) && !isNotNullAndEmpty(status) && !isNotNullAndEmpty(searchTripId)) {
            parcelList = parcelExpressionList.between("requested_at", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).orderBy("requested_at desc").findList();
        } else if (isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate) && !isNotNullAndEmpty(status) && isNotNullAndEmpty(searchTripId)) {
            parcelList = parcelExpressionList.between("requested_at", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).eq("id", searchTripId).orderBy("requested_at desc").findList();
        } else if (isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate) && isNotNullAndEmpty(status) && !isNotNullAndEmpty(searchTripId)) {
            parcelList = parcelExpressionList.between("requested_at", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).eq("ride_status", status).orderBy("requested_at desc").findList();
        } else if (!isNotNullAndEmpty(startDate) && !isNotNullAndEmpty(endDate) && !isNotNullAndEmpty(status)) {
            parcelList = parcelExpressionList.eq("ride_status", status).orderBy("requested_at desc").findList();
        }
        for (Ride ride : parcelList) {
            loadNames(ride);
            if (ride.getRequestedAt() != null) {
                ride.setFormatedRequestAt(ride.getRequestedAt());
            }
            if (ride.getAcceptedAt() != null) {
                ride.setFormatedAcceptedAt(ride.getAcceptedAt());
            }
            if (ride.isPaid()) {
                ride.setRidePaymentStatus("Success");
            } else {
                ride.setRidePaymentStatus("Pending");
            }
            if (ride.getRideStartedAt() != null) {
                ride.setFormatedRideStartedAt(ride.getRideStartedAt());
            }
            if (ride.getRideEndedAt() != null) {
                ride.setFormatedRideEndedAt(ride.getRideEndedAt());
            }
            if (ride.getOrderDistance() != null) {
                totalDistance = totalDistance + ride.getOrderDistance();
            }
            if (ride.getTotalBill() != null) {
                totalAmount = totalAmount + ride.getTotalBill();
            }
            if (ride.getRideStatus().equals(RideStatus.RideRequested)) {
                Logger.info("Inside Ride req");
                noOfPending++;
            }
            if (ride.getRideStatus().equals(RideStatus.RideAccepted)) {
                ride.setRiderMobileNumber(User.find.where().eq("id", ride.getRiderId()).findUnique().getPhoneNumber());
                Logger.info("Inside Ride acc");
                noOfAccepted++;
            }
            if (ride.getRideStatus().equals(RideStatus.RideClosed)) {
                ride.setRiderMobileNumber(User.find.where().eq("id", ride.getRiderId()).findUnique().getPhoneNumber());
                Logger.info("Inside Ride clo");
                noOfCompleted++;
            }
        }
        numberOfRides = parcelList.size();
        JSONObject obj = new JSONObject();
        obj.put("numberOfRides", numberOfRides);
        obj.put("totalDistance", round2(totalDistance));
        obj.put("totalAmount", round2(totalAmount));
        obj.put("pending", noOfPending);
        obj.put("accepted", noOfAccepted);
        obj.put("closed", noOfCompleted);
        ObjectNode objectNode = Json.newObject();
        setJson(objectNode, "rideSummary", obj);

        setResult(objectNode, parcelList);
        return ok(Json.toJson(objectNode));
    }

    public Result allParcelEntries() {
        if (!isValidateVendorSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.parcelHistoryList.render(parcelTableHeaders, "col-sm-12", "", "Parcels", "", "", ""));

    }


    public Result storeParcelBillPhoto() {
        JsonNode userJson = request().body().asJson();
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Ride parcelRide = Ride.find.byId(userJson.get("rideId").longValue());
            if (parcelRide != null && user.getId().equals(parcelRide.getRiderId())) {
                String encodedImageData = userJson.get("imageData").textValue();
                byte[] decoded = Base64.getDecoder().decode(encodedImageData);
                try {
                    String imagePath = "uploads/" + parcelRide.getId() + "-delproof-" + UUID.randomUUID() + ".png";
                    FileOutputStream fileOutputStream = new FileOutputStream("public/" + imagePath);
                    fileOutputStream.write(decoded);
                    fileOutputStream.close();
                    parcelRide.setParcelDropoffImageName(imagePath);
                    parcelRide.save();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result getTripsAmountForDate() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        String dateString = getString("dateString");
        User user = currentUser();
        if (user != null) {
            double customerTripsAmount = 0, parcelTripsAmount = 0;
            List<Ride> closedRides = Ride.find.where().eq("rideStatus", RideClosed).eq("riderId", user.getId()).between("requested_at", DateUtils.getNewDate(dateString, 0, 0, 0), DateUtils.getNewDate(dateString, 23, 59, 59)).findList();
            for (Ride ride : closedRides) {
                if ("Parcel".equals(ride.getRideType())) {
                    parcelTripsAmount = parcelTripsAmount + ride.getTotalBill();
                } else {
                    customerTripsAmount = customerTripsAmount + ride.getTotalBill();
                }
            }
            objectNode.set("customerTripsAmount", Json.toJson(customerTripsAmount));
            objectNode.set("parcelTripsAmount", Json.toJson(parcelTripsAmount));
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result editTripDetails(Long id) {
        Ride ride = Ride.find.byId(id);
        return ok(views.html.editTripsDetails.render(ride));
    }

    public Result updateTripDetail() {
        DynamicForm requestData = formFactory.form().bindFromRequest();
        String rideId = requestData.get("tripId");
        String amount = requestData.get("amount");
        String rideComments = requestData.get("rideComments");
        String parcelOrderId = requestData.get("parcelOrderId");
        String distance = requestData.get("distance");
        String requestedAtTime = requestData.get("requestedTime");
        String acceptedTime = requestData.get("acceptedTime");
        String startTime = requestData.get("startTime");
        String endTime = requestData.get("endTime");
        RideStatus rideStatus = RideClosed;
        if ("RideRequested".equals(requestData.get("rideStatus"))) {
            rideStatus = RideRequested;
        } else if ("RideAccepted".equals(requestData.get("rideStatus"))) {
            rideStatus = RideAccepted;
        } else if ("RideCancelled".equals(requestData.get("rideStatus"))) {
            rideStatus = RideCancelled;
        } else if ("RideRescheduled".equals(requestData.get("rideStatus"))) {
            rideStatus = Rescheduled;
        } else if ("RideStarted".equals(requestData.get("rideStatus"))) {
            rideStatus = RideStarted;
        }
        Long riderId = Long.parseLong(requestData.get("riderId"));
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        Ride ride = Ride.find.byId(Long.valueOf(rideId));
        if (User.find.where().findIds().contains(riderId)) {
            ride.setTotalBill(Double.parseDouble(amount));
            ride.setOrderDistance(Double.parseDouble(distance));
            try {
                ride.setRequestedAt(formatter.parse(requestedAtTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (acceptedTime.length() != 0) {
                ride.setAcceptedAt(DateUtils.getDateFromString(acceptedTime));
            }
            if (startTime.length() != 0) {
                ride.setRideStartedAt(DateUtils.getDateFromString(startTime));
            }
            if (endTime.length() != 0) {
                ride.setRideEndedAt(DateUtils.getDateFromString(endTime));
            }
            if (parcelOrderId != null) {
                ride.setParcelOrderId(parcelOrderId);
            }
            if (!(rideComments.length()==0)) {
                ride.setRideComments(rideComments);
            }
            ride.setRideStatus(rideStatus);
            ride.setRiderId(riderId);
            ride.update();
            //update call health api call here;
            //call health id is 2017 in dev and change when we push to prod;
            if (ride.getRequestorId()==1697) {
                sendSms("53731", "7995053001", "&F1=" + ride.getTotalBill());
                /*String url = "https://medicines-uat.callhealthshop.com/MZIMRestServices/v1/postMZIMOrderStatus";
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("source_type", "getbike");
                jsonBody.put("omorder_id", ride.getParcelOrderId());
                if (Rescheduled.equals(ride.getRideStatus())) {
                    jsonBody.put("order_status", "RequestForReschedule");
                } else if (RideCancelled.equals(ride.getRideStatus())) {
                    jsonBody.put("order_status", "RequestForCancel");
                } else {
                    jsonBody.put("order_status", ride.getRideStatus());
                }
                jsonBody.put("last_updated_on", new Date());*/

                callHealthAPICall(ride);

                /*String url = "https://medicines-uat.callhealthshop.com/MZIMRestServices/v1/postMZIMOrderStatus";
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("source_type", "getbike");
                jsonBody.put("omorder_id", ride.getParcelOrderId());
                if (Rescheduled.equals(ride.getRideStatus())) {
                    jsonBody.put("order_status", "RequestForReschedule");
                } else if (RideCancelled.equals(ride.getRideStatus())) {
                    jsonBody.put("order_status", "RequestForCancel");
                } else {
                    jsonBody.put("order_status", ride.getRideStatus());
                }
                jsonBody.put("last_updated_on", new Date());
                apiPostCall(url,jsonBody.toString());*/
            }
        } else {
            flash("error", "Invalid RiderId " + riderId + " Please Give Valid RiderId !");
            return badRequest(views.html.editTripsDetails.render(ride));
        }
        return redirect("/ride/rideList");
    }

    public void callHealthAPICall(Ride ride) {
        System.out.println("conrol111-1-1-1-1-1-1-1--1-1-1-1-1=====");
        AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
                .setMaxRequestRetry(0)
                .setShutdownQuietPeriod(0)
                .setShutdownTimeout(0).build();
        String name = "wsclient";
        final JsonNode task = Json.newObject()
                .put("source_type", "getbike")
                .put("omorder_id", ride.getParcelOrderId())
                .put("order_status", String.valueOf(ride.getRideStatus()))
                .put("last_updated_on", String.valueOf(new Date()));
        ActorSystem system = ActorSystem.create(name);
        ActorMaterializerSettings settings = ActorMaterializerSettings.create(system);
        ActorMaterializer materializer = ActorMaterializer.create(settings, system, name);
        WSClient client = new AhcWSClient(config, materializer);
        client.url("https://medicines-uat.callhealthshop.com/MZIMRestServices/v1/postMZIMOrderStatus").post(task).whenComplete((r, e) -> {
        }).thenRun(() -> {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).thenRun(system::terminate);
    }

     /*public CompletionStage<Result> callHealthAPICall(Ride ride) {
         System.out.println("Controller inside my async callss method!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        final JsonNode task = Json.newObject()
                .put("source_type", "getbike")
                .put("omorder_id", ride.getParcelOrderId())
                .put("order_status", String.valueOf(ride.getRideStatus()))
                .put("last_updated_on", String.valueOf(new Date()));

        final CompletionStage<WSResponse> eventualResponse = ws.url("https://medicines-uat.callhealthshop.com/MZIMRestServices/v1/postMZIMOrderStatus")
                .post(task);

        return eventualResponse.thenApplyAsync(response -> ok(response.asJson()),
                exec);
    }*/

    public static String sendPostRequest(String requestUrl, String payload) {
        StringBuffer jsonString = new StringBuffer();
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println("Testing phase..... response  called my sendPostRequest!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return jsonString.toString();
    }

    public void apiPostCall(String completeUrl, String body) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(completeUrl);
        httpPost.setHeader("Content-type", "application/json");
        try {
            StringEntity stringEntity = new StringEntity(body);
            httpPost.getRequestLine();
            httpPost.setEntity(stringEntity);
            httpClient.execute(httpPost);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Result importExcelData() {
        return ok(views.html.importExcelData.render());
    }

    public Result saveImportedExcelData() throws IOException {
        DataFormatter formatter = new DataFormatter();
        String dateFormat = "dd/MM/yy HH:mm";
        ObjectNode objectNode = Json.newObject();
        try {
            Http.MultipartFormData body = request().body().asMultipartFormData();
            if (body == null) {
                return badRequest("Invalid request, required is POST with enctype=multipart/form-data.");
            }
            String importedFile = fileUtils.fileUpload(body.getFile("importedFile"));
            Logger.info("File Name :" + importedFile);
            String excelFilePath = "public/uploads/" + importedFile;
            FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
            Workbook workbook = getWorkbook(inputStream, excelFilePath);
            Sheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = firstSheet.iterator();
            while (iterator.hasNext()) {
                Row nextRow = iterator.next();
                // display row number in the console.
                System.out.println("Row No.: ---------" + nextRow.getRowNum());
                if (nextRow.getRowNum() == 0) {
                    continue; //just skip the rows if row number is 0
                }
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                Ride aRide = new Ride();
                aRide.setRequestorName(session("vendorName"));
                aRide.setRideStatus(RideStatus.RideRequested);
                aRide.setRequestorId(User.find.where().eq("email", session("vendorName")).findUnique().getId());
                aRide.setRideType("Parcel");
                while (cellIterator.hasNext()) {
                    Cell nextCell = cellIterator.next();
                    int columnIndex = nextCell.getColumnIndex();
                    switch (columnIndex) {
                        case 1:
                            aRide.setRequestedAt(DateUtils.dateFromString(formatter.formatCellValue(nextCell), dateFormat));
                            break;
                        case 2:
                            aRide.setParcelPickupDetails((String) getCellValue(nextCell));
                            break;
                        case 3:
                            aRide.setParcelPickupNumber(formatter.formatCellValue(nextCell));
                            break;
                        case 4:
                            aRide.setSourceAddress((String) getCellValue(nextCell));
                            break;
                        case 5:
                            aRide.setParcelDropoffDetails((String) getCellValue(nextCell));
                            break;
                        case 6:
                            aRide.setParcelDropoffNumber(formatter.formatCellValue(nextCell));
                            break;
                        case 7:
                            aRide.setDestinationAddress((String) getCellValue(nextCell));
                            break;
                        case 8:
                            aRide.setCodAmount((double) getCellValue(nextCell));
                            break;
                        case 9:
                            aRide.setParcelOrderId(formatter.formatCellValue(nextCell));
                            break;
                    }
                }
                aRide.save();
            }
            inputStream.close();
            objectNode.put(SUCCESS, SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            objectNode.put(FAILURE, FAILURE);
        }
        return redirect("/parcel/all");
    }

    public Result vendorStoreData() {
        JsonNode userJson = request().body().asJson();
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        String responseDetail = "Invalid User !";
        List<Ride> parcelList = new ArrayList<>();
        int numberOfRides = 0;
        User vendorUser = currentUser();
        if (vendorUser != null) {
            User user = User.find.where().eq("email", vendorUser.getEmail()).findUnique();
            ExpressionList<Ride> parcelExpressionList = Ride.find.where().eq("requestorId", user.getId());
            parcelList = parcelExpressionList.orderBy("requested_at desc").findList();
            numberOfRides = parcelList.size();
            System.out.print("Ride List Size ---> " + numberOfRides);
            result = SUCCESS;
            responseDetail = user.getEmail() + " is a Valid User";
        }
        JSONObject obj = new JSONObject();
        obj.put("STATUS", result);
        obj.put("RESPONSE DETAILS", responseDetail);
        setJson(objectNode, "RESPONSE", obj);
        setResult(objectNode, parcelList);
        return ok(Json.toJson(objectNode));
    }

    public Result createVendorOrder() {
        JsonNode userJson = request().body().asJson();
        String apiKey = userJson.get("apiKey").textValue();
        System.out.println("Authentication:" + apiKey);
        ObjectNode objectNode = Json.newObject();
        String result = "FAILURE";
        String statusCode = "500";
        String msg = "Order Creation Fails Due to Unauthorized  API token :" + apiKey;
        User vendorUser = validateVendor(apiKey);
        int count = Ride.find.where().eq("parcelOrderId", userJson.get("data").get("parcelOrderId").asText()).findRowCount();
        Ride aRide = new Ride();
        if (count != 0) {
            msg = "Order Creation Fails Due to Duplicate ParcelOrderId";
        }
        if (vendorUser != null && count == 0) {
            aRide.setRequestorId(vendorUser.getId());
            aRide.setRequestorName(vendorUser.getName());
            aRide.setRideStatus(RideStatus.RideRequested);
            aRide.setModeOfPayment("Cash");
            aRide.setRideType("Parcel");
            aRide.setSourceAddress(userJson.get("data").get("sourceAddress").textValue());
            System.out.print("Addd:" + userJson.get("data").get("sourceAddress").textValue());
            aRide.setParcelOrderId(userJson.get("data").get("parcelOrderId").asText());
            System.out.print("ParcelOrderId :" + userJson.get("data").get("parcelOrderId").asText());
            aRide.setDestinationAddress(userJson.get("data").get("destinationAddress").textValue());
            aRide.setStartLatitude(userJson.get("data").get("startLatitude").asDouble());
            aRide.setStartLongitude(userJson.get("data").get("startLongitude").asDouble());
            aRide.setRequestedAt(DateUtils.getDateFromString(userJson.get("data").get("requestedAt").textValue()));
            aRide.setParcelDropoffNumber(userJson.get("data").get("parcelDropoffNumber").textValue());
            aRide.setParcelPickupNumber(userJson.get("data").get("parcelPickupNumber").textValue());
            aRide.setParcelPickupDetails(userJson.get("data").get("parcelPickupDetails").textValue());
            aRide.setParcelDropoffDetails(userJson.get("data").get("parcelDropoffDetails").textValue());
            aRide.setCodAmount(userJson.get("data").get("codAmount").asDouble());
            aRide.setParcelRequestRaisedAt(new Date());
            aRide.save();
            result = "SUCCESS";
            statusCode = "200K";
            msg = "Order Created Successfully ";
        }
        JSONObject obj = new JSONObject();
        obj.put("STATUS", result);
        obj.put("CODE", statusCode);
        obj.put("MSG", msg);
        JSONObject obj2 = new JSONObject();
        obj2.put("tripId", aRide.getId());
        obj2.put("vendorId", aRide.getRequestorId());
        obj2.put("rideStatus", aRide.getRideStatus());
        setJson(objectNode, "RESPONSE", obj);
        setJson(objectNode, "data", obj2);
        return ok(Json.toJson(objectNode));
    }

    public Result createReOrderId() {
        JsonNode userJson = request().body().asJson();
        String apiKey = userJson.get("apiKey").textValue();
        System.out.println("Authentication IN REORDER ID:---------------" + apiKey);
        ObjectNode objectNode = Json.newObject();
        String result = "FAILURE";
        String statusCode = "500";
        String msg = "Order Creation Fails Due to Unauthorized  API token :" + apiKey;
        Ride ride = null;
        User vendorUser = validateVendor(apiKey);
        int count = Ride.find.where().eq("parcelOrderId", userJson.get("data").get("parcelOrderId").asText()).findRowCount();
        System.out.println("count match is : "+count);
        if (count != 1) {
            msg = "wrong parameters(in valid order id)";
        }
        if (vendorUser != null && count == 1) {
            ride = Ride.find.where().eq("parcelOrderId",userJson.get("data").get("parcelOrderId").asText()).findUnique();
            if (ride != null) {
                ride.setRequestedAt(DateUtils.getDateFromString(userJson.get("data").get("updatedTime").textValue()));
                ride.setRideStatus(Rescheduled);
                ride.setParcelReOrderId(userJson.get("data").get("parcelReOrderId").asText());
                ride.update();
                result = "SUCCESS";
                statusCode = "200K";
                msg = "Order Updated Successfully";
            }
        }
        JSONObject obj = new JSONObject();
        obj.put("STATUS", result);
        obj.put("CODE", statusCode);
        obj.put("MSG", msg);
        JSONObject obj2 = new JSONObject();
        if (ride != null) {
            obj2.put("tripId", ride.getId());
            obj2.put("vendorId", ride.getRequestorId());
            obj2.put("rideStatus", ride.getRideStatus());
        }
        setJson(objectNode, "RESPONSE", obj);
        setJson(objectNode, "data", obj2);
        return ok(Json.toJson(objectNode));
    }

    public Result getVendorOrderStatus() {
        JsonNode userJson = request().body().asJson();
        String apiKey = userJson.get("apiKey").textValue();
        System.out.println("Authentication:" + apiKey);
        ObjectNode objectNode = Json.newObject();
        JSONObject obj = new JSONObject();
        JSONObject obj2 = new JSONObject();
        String result = "FAILURE";
        String statusCode = "500";
        String msg = "Order Retrieval Fails Due to Unauthorized  API token :" + apiKey;
        User vendorUser = validateVendor(apiKey);
        String dateFormat = "MM/dd/yyyy hh:mm a";
        Ride aRide = null;
        if (vendorUser != null) {
            Long parcelOrderId = Long.parseLong(userJson.get("data").get("parcelOrderId").asText());
            System.out.println("Order Id " + parcelOrderId);
            aRide = Ride.find.where().eq("requestorId", vendorUser.getId()).eq("parcelOrderId", parcelOrderId).findUnique();
            if (aRide != null) {
                obj2.put("tripId", aRide.getId());
                obj2.put("vendorId", aRide.getRequestorId());
                if (Rescheduled.equals(aRide.getRideStatus()) && aRide.getParcelReOrderId() == null) {
                    obj2.put("rideStatus", "RequestForReschedule");
                    obj2.put("comments",aRide.getRideComments());
                } else if (RideCancelled.equals(aRide.getRideStatus())) {
                    obj2.put("rideStatus", "RequestForCancel");
                    obj2.put("comments",aRide.getRideComments());
                } else {
                    obj2.put("rideStatus", aRide.getRideStatus());
                }
                if (aRide.getParcelReOrderId() != null) {
                    obj2.put("parcelReOrderId",aRide.getParcelReOrderId());
                }
                obj2.put("requestedAt", DateUtils.convertDateToString(aRide.getRequestedAt(), dateFormat));
                obj2.put("riderId", aRide.getRiderId());
                obj2.put("riderName", (aRide.getRiderId() != null) ? User.find.where().eq("id", aRide.getRiderId()).findUnique().getName() : null);
                obj2.put("acceptedAt", (aRide.getAcceptedAt() != null) ? DateUtils.convertDateToString(aRide.getAcceptedAt(), dateFormat) : null);
                obj2.put("startedAt", (aRide.getRideStartedAt() != null) ? DateUtils.convertDateToString(aRide.getRideStartedAt(), dateFormat) : null);
                obj2.put("rideEndedAt", (aRide.getRideEndedAt() != null) ? DateUtils.convertDateToString(aRide.getRideEndedAt(), dateFormat) : null);
                obj2.put("totalBill", aRide.getTotalBill());
                result = "SUCCESS";
                statusCode = "200K";
                msg = "Order Retrieve Successfully ";
            }
            if (aRide == null) {
                msg = "No Data with OrderId " + parcelOrderId;
            }
        }

        obj.put("STATUS", result);
        obj.put("CODE", statusCode);
        obj.put("MSG", msg);

        setJson(objectNode, "RESPONSE", obj);
        setJson(objectNode, "data", obj2);
        return ok(Json.toJson(objectNode));
    }

    private Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();
        }
        return null;
    }

    private Workbook getWorkbook(FileInputStream inputStream, String excelFilePath) throws IOException {
        Workbook workbook = null;
        if (excelFilePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (excelFilePath.endsWith("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
        return workbook;
    }

    public Result getPendingRides() {
        String startDate = request().getQueryString("startDate");
        String endDate = request().getQueryString("endDate");
        String status = request().getQueryString("status");

        if ("ALL".equals(status) || "null".equals(status)) {
            status = null;
        }
        List<Ride> listOfRides = new ArrayList<>();

        ExpressionList<Ride> rideQuery = Ride.find.where();
        if (isNotNullAndEmpty(status) && isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate)) {
            listOfRides = rideQuery.between("requested_at", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).isNull("group_ride_id").eq("ride_status", "RideRequested").orderBy("requested_at desc").findList();
        } else if (isNotNullAndEmpty(status) && !isNotNullAndEmpty(startDate) && !isNotNullAndEmpty(endDate)) {
            listOfRides = rideQuery.eq("ride_status", status).orderBy("requested_at desc").findList();
        } else if (!isNotNullAndEmpty(status) && isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate)) {
            listOfRides = rideQuery.between("requested_at", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).isNull("group_ride_id").eq("ride_status", "RideRequested").orderBy("requested_at desc").findList();
        } else  {
            listOfRides = rideQuery.eq("ride_status", "RideRequested").isNull("group_ride_id").orderBy("requested_at desc").findList();
        }
        for (Ride ride :listOfRides) {
            loadNames(ride);
        }

        ObjectNode objectNode = Json.newObject();
        setResult(objectNode, listOfRides);
        return ok(Json.toJson(objectNode));
    }

    public Result getGroupRides(String ids) {
        String[] split = ids.split(",");
        List<Ride> rideList = new ArrayList<>();
        for(String id : split) {
            Ride ride = Ride.find.byId(Long.valueOf(id));
            if(isNotNullAndEmpty(ride.getDestinationAddress())) {
                String[] latLongPositions = getLatLongPositions(ride.getDestinationAddress());
                if(latLongPositions != null && latLongPositions[0] != null && latLongPositions[1] != null) {
                    ride.setEndLatitude(Double.valueOf(latLongPositions[0]));
                    ride.setEndLongitude(Double.valueOf(latLongPositions[1]));
                rideList.add(ride);
                }
            }
        }

        List<Point> orderedPoints = getOrderedPoints(rideList);

        String riderLocationString = "[";
        for (Point point : orderedPoints) {
            System.out.println(point.getLat() + " " + point.getLng());
            if (point.getLat() != null && point.getLng() != null && point.getLat() != 0.0 && point.getLng() != 0.0) {
                if(point.isSource()) {
                    riderLocationString += "['" + point.getSourceAddress() + "'," + point.getLat() + "," + point.getLng() + "," + point.getRideId() + "],";
                }else{
                    riderLocationString += "['" + point.getDestinationAddress() + "'," + point.getLat() + "," + point.getLng() + "," + point.getRideId() + "],";
                }
            }
        }
        if (riderLocationString.endsWith(",")) {
            riderLocationString = riderLocationString.substring(0, riderLocationString.length() - 1);
        }

        riderLocationString += "]";

        List<User> primeRiders = User.find.where().eq("primeRider", true).findList();

        return ok(views.html.grouprides.render(riderLocationString , ids , primeRiders));
    }

    public Result saveGroupRides(String ids) {
        DynamicForm dynamicForm = formFactory.form().bindFromRequest();
        String primeRiderId = dynamicForm.get("PrimeRider");
        System.out.println("prime rider id "+primeRiderId);
        if (isNotNullAndEmpty(ids)) {
            String[] split = ids.split(",");

            Ride groupRide = new Ride();
            groupRide.setRiderId(Long.valueOf(primeRiderId));
            groupRide.setRideStatus(RideStatus.RideAccepted);
            groupRide.setGroupRide(true);
            groupRide.save();
            for (String id : split) {
                Ride ride = Ride.find.byId(Long.valueOf(id));
                ride.setGroupRideId(groupRide.id);
                ride.save();
            }
        }
        return redirect("/pending");
    }

    public List<Point> getOrderedPoints(List<Ride> rides) {

        List<Point> result = new ArrayList<>();
        List<Point> eligiblePoints = getEligiblePoints(rides);
        System.out.println(eligiblePoints.size() + " Size");
        Ride firstRide = rides.get(0);
        Point referencePoint = new Point(firstRide.getStartLatitude(), firstRide.getStartLongitude());
        for(int i = 0; i < rides.size() * 2; i++){
            Collections.sort(eligiblePoints, new LatLongSortDistanceWise(referencePoint));
            referencePoint = eligiblePoints.get(0);
            for (Ride ride : rides) {
                if (ride.getId().equals(referencePoint.getRideId())) {
                    if (referencePoint.isSource()) {
                        ride.setProcessRideSource(true);
                    } else {
                        ride.setProcessRideDestination(true);
                    }
                    break;
                }
            }
            result.add(referencePoint);
            eligiblePoints = getEligiblePoints(rides);
            System.out.println(eligiblePoints.size() + " Size");
        }
        return result;
    }

    public List<Point> getEligiblePoints(List<Ride> rides) {
        List<Point> result = new ArrayList<>();
        for (Ride ride : rides) {
            System.out.println(ride.getStartLatitude()+" "+ride.getStartLongitude()+" "+ride.getEndLatitude()+" "+ride.getEndLongitude());
            if (!ride.isProcessRideSource()) {
                if(ride.getStartLatitude() != null && ride.getStartLongitude() != null) {
                    Point start = new Point(ride.getStartLatitude(), ride.getStartLongitude());
                    start.setRideId(ride.getId());
                    start.setSourceAddress(ride.getSourceAddress());
                    start.setSource(true);
                    result.add(start);
                }
            } else if (ride.isProcessRideSource() && !ride.isProcessRideDestination()) {
                if(ride.getEndLatitude() != null && ride.getEndLongitude() != null) {
                    Point end = new Point(ride.getEndLatitude(), ride.getEndLongitude());
                    end.setDestinationAddress(ride.getDestinationAddress());
                    end.setRideId(ride.getId());
                    end.setSource(false);
                    result.add(end);
                }
            }
        }
        System.out.println("Input Rides Size " + rides.size() + " output points " + result.size());
        return result;
    }
        public static String[] getLatLongPositions(String address)
        {
            System.out.println("Address is "+address);
            int responseCode = 0;
            try {
                String api = "http://maps.googleapis.com/maps/api/geocode/xml?address=" + URLEncoder.encode(address, "UTF-8") + "&sensor=true";
                URL url = new URL(api);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.connect();
                responseCode = httpConnection.getResponseCode();
                if (responseCode == 200) {
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    ;
                    Document document = builder.parse(httpConnection.getInputStream());
                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    XPathExpression expr = xpath.compile("/GeocodeResponse/status");
                    String status = (String) expr.evaluate(document, XPathConstants.STRING);
                    if (status.equals("OK")) {
                        expr = xpath.compile("//geometry/location/lat");
                        String latitude = (String) expr.evaluate(document, XPathConstants.STRING);
                        expr = xpath.compile("//geometry/location/lng");
                        String longitude = (String) expr.evaluate(document, XPathConstants.STRING);
                        return new String[]{latitude, longitude};
                    } else {
                        throw new Exception("Error from the API - response status: " + status);
                    }
                }
            }catch (Exception ex){
                ex.getStackTrace();
            }
            return null;
        }

        public Result getRiderLocations(Long id){
            ArrayNode jsonNodes = Json.newArray();
            Ride groupRide = Ride.find.byId(id);
            if(id != null){
                List<Ride> group_ride_id = Ride.find.where().eq("group_ride_id", id).findList();
                List<Ride> rideList = new ArrayList<>();
                for(Ride ride: group_ride_id){
                    if(isNotNullAndEmpty(ride.getDestinationAddress())) {
                        String[] latLongPositions = getLatLongPositions(ride.getDestinationAddress());
                        if(latLongPositions != null && latLongPositions[0] != null && latLongPositions[1] != null) {
                            ride.setEndLatitude(Double.valueOf(latLongPositions[0]));
                            ride.setEndLongitude(Double.valueOf(latLongPositions[1]));
                            rideList.add(ride);
                        }
                    }
                }
                List<Point> orderedPoints = getOrderedPoints(rideList);
                for (Point point:orderedPoints){
                    jsonNodes.add(Json.toJson(point));
                }
            }
            ObjectNode objectNode = Json.newObject();
            objectNode.set("riderLocations" , jsonNodes);
            objectNode.set("groupId" , Json.toJson(id));
            objectNode.set("groupRiderId" , Json.toJson(groupRide.getRiderId()));
            return ok(objectNode);
        }

}
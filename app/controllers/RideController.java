package controllers;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dataobject.RideStatus;
import dataobject.WalletEntryType;
import models.Ride;
import models.RideLocation;
import models.User;
import models.Wallet;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.json.simple.JSONObject;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.libs.ws.ahc.AhcWSClient;
import play.mvc.BodyParser;
import play.mvc.Result;
import utils.*;

import java.util.*;
import java.util.function.Consumer;

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
    final static double MAX_DISTANCE_IN_KILOMETERS = 10.0;
    public static final double MINIMUM_WALLET_AMOUNT_FOR_ACCEPTING_RIDE = 200.0;
    public static final double FREE_RIDE_MAX_DISCOUNT = 50.0;

    public LinkedHashMap<String, String> rideTableHeaders = getTableHeadersList(new String[]{"Requester Id", "Rider Id", "Rider Status", "Order Distance", "Order Amount", "Requested At", "Accepted At", "Ride Started At", "Ride Ended At", "Start Latitude", "Start Longitude", "Source Address", "Destination Address", "Total Fare", "TaxesAndFees", "Sub Total", "Rouding Off", "Total Bill"}, new String[]{"requestorId", "requestorName", "riderId", "rideStatus", "orderDistance", "orderAmount", "requestedAt", "acceptedAt", "rideStartedAt", "rideEndedAt", "startLatitude", "startLongitude", "sourceAddress", "destinationAddress", "totalFare", "taxesAndFees", "subTotal", "roundingOff", "totalBill"});
    public LinkedHashMap<String, String> rideLocationTableHeaders = getTableHeadersList(new String[]{"", "", "Ride Location", "Ride Id", "Location Time", "Latitude", "Longitude"}, new String[]{"", "", "id", "rideId", "locationTime", "latitude", "longitude"});

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
                ride.setModeOfPayment(locationsJson.get("modeOfPayment").textValue());
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
            publishRideDetails(user, ride);
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
                    ride.setModeOfPayment(locationsJson.get("modeOfPayment").textValue());
                    ride.setRiderId(user.getId());
                    ride.setRideGender(user.getGender());
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
                ride.setOrderAmount(DistanceUtils.calculateBasePrice(ride.getOrderDistance(), DistanceUtils.timeInMinutes(locations)));
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
                User requestor = User.find.byId(ride.getRequestorId());
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
        loadNames(ride);
        return ok(views.html.ridePath.render(rideLocationStrings, firstLocation, ride));

    }

    public Result openRides() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            ArrayNode ridesNodes = Json.newArray();
            double latitude = getDouble("latitude");
            double longitude = getDouble("longitude");
            List<Ride> openRides = Ride.find.where().eq("rideStatus", RideRequested).ge("requestedAt", minutesOld(15)).raw("ride_gender = '" + user.getGender() + "' and requestor_id != " + user.getId() + " and ( 3959 * acos( cos( radians(" + latitude +
                    ") ) * cos( radians(start_latitude) ) " +
                    "   * cos( radians(start_longitude) - radians(" + longitude +
                    ")) + sin(radians(" + latitude + ")) " +
                    "   * sin( radians(start_latitude)))) < " +
                    MAX_DISTANCE_IN_KILOMETERS + " ").setMaxRows(5).order("requestedAt desc").findList();
            for (Ride ride : openRides) {
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

    public Result updatePaymentStatus(){
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
            for (int i = 0; i < 5; i++) {
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

    private double noise(Double latitude, double factor) {

        return latitude * ((1 - factor) + (factor * 2 * Math.random()));
    }


    private void publishRideDetails(User user, Ride ride) {
        IGcmUtils gcmUtils = ApplicationContext.defaultContext().getGcmUtils();
        gcmUtils.sendMessage(getRelevantRiders(user.getId(), ride.getStartLatitude(), ride.getStartLongitude(), user.getGender()), "A new ride request with ride Id " + ride.getId() + " is active.", "newRide", ride.getId());
    }

    public static List<User> getRelevantRiders(Long currentId, Double latitude, Double longitude, char gender) {
        return User.find.where().eq("isRideInProgress", false).eq("isRequestInProgress", false).raw("( 3959 * acos( cos( radians(" + latitude +
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

}
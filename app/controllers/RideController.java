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
import models.Ride;
import models.RideLocation;
import models.User;
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
import utils.ApplicationContext;
import utils.DateUtils;
import utils.DistanceUtils;
import utils.IGcmUtils;

import java.util.*;
import java.util.function.Consumer;

import static dataobject.RideStatus.*;
import static utils.DateUtils.minutesOld;
import static utils.DistanceUtils.round2;
import static utils.GetBikeErrorCodes.*;

/**
 * Created by sivanookala on 21/10/16.
 */
public class RideController extends BaseController {

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
            Ride ride = new Ride();
            ride.setStartLatitude(startLatitude);
            ride.setStartLongitude(startLongitude);
            ride.setSourceAddress(locationsJson.get("sourceAddress").textValue());
            ride.setDestinationAddress(locationsJson.get("destinationAddress").textValue());
            ride.setRequestorId(user.getId());
            ride.setRideStatus(RideRequested);
            ride.setRequestedAt(new Date());
            ride.setRideGender(user.getGender());
            ride.save();
            result = SUCCESS;
            setJson(objectNode, Ride.RIDE_ID, ride.getId());
            publishRideDetails(user, ride);
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result acceptRide() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        int errorCode = GENERAL_FAILURE;
        User user = currentUser();
        if (user != null) {
            if (!user.isValidProofsUploaded()) {
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
                ride.setRiderId(user.getId());
                ride.setRideGender(user.getGender());
                String phoneNumber = locationsJson.get("phoneNumber").textValue();
                User requestor = User.find.where().eq("phoneNumber", phoneNumber).findUnique();
                if (requestor == null) {
                    requestor = new User();
                    requestor.setPhoneNumber(phoneNumber);
                    requestor.save();
                }
                ride.setRequestorId(requestor.getId());
                ride.setRideStatus(RideAccepted);
                ride.setRequestedAt(new Date());
                ride.setAcceptedAt(ride.getRequestedAt());
                ride.save();
                user.setRideInProgress(true);
                user.setCurrentRideId(ride.getId());
                user.save();
                result = SUCCESS;
                setJson(objectNode, Ride.RIDE_ID, ride.getId());
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
            ride.setOrderDistance(DistanceUtils.distanceKilometers(rideLocations));
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
                user.setRideInProgress(false);
                user.setCurrentRideId(null);
                user.save();
                User requestor = User.find.byId(ride.getRequestorId());
                IGcmUtils gcmUtils = ApplicationContext.defaultContext().getGcmUtils();
                gcmUtils.sendMessage(requestor, "Your ride is now closed.", "rideClosed", ride.getId());
                objectNode.set("ride", Json.toJson(ride));
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
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
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }


    public Result cancelRide() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Long rideId = getLong(Ride.RIDE_ID);
            Ride ride = Ride.find.byId(rideId);
            if (ride != null && ride.getRequestorId().equals(user.getId())) {
                boolean rideRequested = RideRequested.equals(ride.getRideStatus());
                boolean rideNotStarted = (RideAccepted.equals(ride.getRideStatus()) && !ride.isRideStarted());
                if (rideRequested || rideNotStarted) {
                    ride.setRideStatus(RideCancelled);
                    ride.save();
                    if (ride.getRiderId() != null) {
                        User rider = User.find.byId(ride.getRiderId());
                        IGcmUtils gcmUtils = ApplicationContext.defaultContext().getGcmUtils();
                        gcmUtils.sendMessage(rider, "Ride " + ride.getId() + " is cancelled.", "rideCancelled", ride.getId());
                    }
                    result = SUCCESS;
                }
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
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
            // TODO: 05/01/17 Show rides which are in seven km range
            List<Ride> openRides = Ride.find.where().eq("rideStatus", RideRequested).ge("requestedAt", minutesOld(5)).raw("ride_gender = '" + user.getGender() + "' and requestor_id != " + user.getId()).setMaxRows(5).order("requestedAt desc").findList();
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


    public Result getRideById() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Ride rideById = Ride.find.byId(getLong(Ride.RIDE_ID));
            if (rideById != null) {
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

    private void publishRideDetails(User user, Ride ride) {
        IGcmUtils gcmUtils = ApplicationContext.defaultContext().getGcmUtils();
        gcmUtils.sendMessage(getRelevantRiders(user.getId(), ride.getStartLatitude(), ride.getStartLongitude(), user.getGender()), "A new ride request with ride Id " + ride.getId() + " is active.", "newRide", ride.getId());
    }

    public static List<User> getRelevantRiders(Long currentId, Double latitude, Double longitude, char gender) {
        double distanceInKilometers = 10.0;
        return User.find.where().eq("isRideInProgress", false).raw("( 3959 * acos( cos( radians(" + latitude +
                ") ) * cos( radians( last_known_latitude ) ) " +
                "   * cos( radians(last_known_longitude) - radians(" + longitude +
                ")) + sin(radians(" + latitude + ")) " +
                "   * sin( radians(last_known_latitude)))) < " +
                distanceInKilometers + " ").raw("gender = '" + gender + "'").gt("lastKnownLatitude", 0.0).gt("lastKnownLongitude", 0.0).not().eq("id", currentId).findList();
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
        if ("ALL".equals(status) || "null".equals(status)) {
            status = null;
        }
        List<Ride> listOfRides = new ArrayList<>();
        List<Object> listOfIds = new ArrayList<>();
        ExpressionList<Ride> rideQuery = null;
        if (isNotNullAndEmpty(srcName)) {
            listOfIds = User.find.where().or(Expr.like("lower(name)", "%" + srcName.toLowerCase() + "%"), Expr.like("lower(phoneNumber)", "%" + srcName.toLowerCase() + "%")).findIds();
            rideQuery = Ride.find.where().or(Expr.in("requestorId", listOfIds), Expr.in("riderId", listOfIds));
        } else {
            rideQuery = Ride.find.where();
        }
        if (isNotNullAndEmpty(status) && isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate)) {
            listOfRides = rideQuery.between("requested_at", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).eq("ride_status", status).findList();
        } else if (isNotNullAndEmpty(status) && !isNotNullAndEmpty(startDate) && !isNotNullAndEmpty(endDate)) {
            listOfRides = rideQuery.eq("ride_status", status).findList();
        } else if (!isNotNullAndEmpty(status) && isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate)) {
            listOfRides = rideQuery.between("requested_at", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).findList();
        } else if (!isNotNullAndEmpty(status) && !isNotNullAndEmpty(startDate) && !isNotNullAndEmpty(endDate)) {
            listOfRides = rideQuery.findList();
        }
        for (Ride ride : listOfRides) {
            loadNames(ride);
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
                Logger.info("Inside Ride acc");
                noOfaccepted++;
            }
            if (ride.getRideStatus().equals(RideStatus.RideClosed)) {
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
        setResult(objectNode, listOfRides);
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
        AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
                .setMaxRequestRetry(0)
                .setShutdownQuietPeriod(0)
                .setShutdownTimeout(0).build();

        String name = "wsclient";
        ActorSystem system = ActorSystem.create(name);
        ActorMaterializerSettings settings = ActorMaterializerSettings.create(system);
        ActorMaterializer materializer = ActorMaterializer.create(settings, system, name);
        WSClient client = new AhcWSClient(config, materializer);
        Consumer<WSResponse> sourceAddressConsumer = response -> {
            ride.setActualSourceAddress(response.asJson().get("results").get(0).get("formatted_address").toString());
            ride.save();
        };
        Consumer<WSResponse> destinationAddressConsumer = response -> {
            ride.setActualDestinationAddress(response.asJson().get("results").get(0).get("formatted_address").toString());
            ride.save();
        };

        updateAddressByLatitudeAndLogitude(firstLocation, system, client, sourceAddressConsumer);
        updateAddressByLatitudeAndLogitude(lastLocation, system, client, destinationAddressConsumer);

    }

    private void updateAddressByLatitudeAndLogitude(RideLocation location, ActorSystem system, WSClient client, Consumer<WSResponse> addressConsumer) {
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
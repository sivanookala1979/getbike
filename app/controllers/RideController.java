package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Ride;
import models.RideLocation;
import models.User;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import utils.ApplicationContext;
import utils.DistanceUtils;
import utils.IGcmUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static dataobject.RideStatus.*;
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
            if (user.isRideInProgress()) {
                errorCode = RIDE_ALREADY_IN_PROGRESS;
            } else {
                Long rideId = getLong(Ride.RIDE_ID);
                Ride ride = Ride.find.byId(rideId);
                if (ride != null && RideRequested.equals(ride.getRideStatus())) {
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
                ride.setTotalFare(DistanceUtils.round2(ride.getOrderAmount()));
                ride.setTaxesAndFees(DistanceUtils.round2(ride.getTotalFare() * 0.061));
                ride.setSubTotal(DistanceUtils.round2(ride.getTotalFare() + ride.getTaxesAndFees()));
                ride.setRoundingOff(DistanceUtils.round2((ride.getSubTotal() - ride.getSubTotal().intValue())));
                ride.setTotalBill(DistanceUtils.round2((double) ride.getSubTotal().intValue()));
                if (locations.size() >= 2) {
                    ride.setRideStartedAt(locations.get(0).getLocationTime());
                    ride.setRideEndedAt(locations.get(locations.size() - 1).getLocationTime());
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

    public Result ridePath() {
        Long rideId = getLong(Ride.RIDE_ID);
        List<String> rideLocationStrings = new ArrayList<>();
        List<RideLocation> rideLocations = RideLocation.find.where().eq("rideId", rideId).order("locationTime asc").findList();
        Logger.info("Ride Locations  " + rideLocationStrings);
        if (!rideLocations.isEmpty()) {
            RideLocation firstLocation = rideLocations.get(0);
            for (RideLocation rideLocation : rideLocations) {
                rideLocationStrings.add("{lat: " + rideLocation.getLatitude() +
                        ", lng: " + rideLocation.getLongitude() +
                        "}");
            }
            Ride ride = Ride.find.where().eq("id", rideId).findUnique();
            if (ride.getRiderId() != null) {
                ride.riderName = User.find.where().eq("id", ride.getRiderId()).findUnique().getName();
            }
            return ok(views.html.ridePath.render(rideLocationStrings, firstLocation.getLatitude(), firstLocation.getLongitude(), ride));
        } else
            return redirect("/ride/rideList");

    }

    public Result openRides() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            ArrayNode ridesNodes = Json.newArray();
            List<Ride> openRides = Ride.find.where().eq("rideStatus", RideRequested).setMaxRows(5).order("requestedAt desc").findList();
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
        gcmUtils.sendMessage(getRelevantRiders(user.getId()), "A new ride request with ride Id " + ride.getId() + " is active.", "newRide", ride.getId());
    }

    private List<User> getRelevantRiders(Long currentId) {
        return User.find.where().not().eq("id", currentId).findList();
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
        String startDate = request().getQueryString("startDate");
        String endDate = request().getQueryString("endDate");
        String status = request().getQueryString("status");
        String srcName = request().getQueryString("srcName");
        Logger.info("Search name  " + srcName);
        List<Ride> listOfRides = new ArrayList<>();
        List<User> listOfNames = new ArrayList<>();
        List<User> listOfPhNumbers = new ArrayList<>();
        if (isNotNullAndEmpty(srcName)) {
            listOfNames = User.find.where().contains("name", srcName).findList();
            listOfPhNumbers = User.find.where().contains("phoneNumber", srcName).findList();
        }
        Logger.info("List of names  ************ " + listOfNames);
        Logger.info("List of phone numbers ********* " + listOfPhNumbers);
        if (isNotNullAndEmpty(status) && isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate)) {
            listOfRides = Ride.find.where().between("requested_at", startDate, endDate).eq("ride_status", status).findList();
        } else if (isNotNullAndEmpty(status) && !isNotNullAndEmpty(startDate) && !isNotNullAndEmpty(endDate)) {
            listOfRides = Ride.find.where().eq("ride_status", status).findList();
        } else if (!isNotNullAndEmpty(status) && isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate)) {
            listOfRides = Ride.find.where().between("requested_at", startDate, endDate).findList();
        } else if (!isNotNullAndEmpty(status) && !isNotNullAndEmpty(startDate) && !isNotNullAndEmpty(endDate)) {
            listOfRides = Ride.find.all();
        }
        for (Ride ride : listOfRides) {
            if (ride.getRequestorId() != null) {
                ride.requestorName = User.find.where().eq("id", ride.getRequestorId()).findUnique().getName();
            }
            if (ride.getRiderId() != null) {
                ride.riderName = User.find.where().eq("id", ride.getRiderId()).findUnique().getName();
            }
        }
        Logger.info("List of rides  " + listOfRides.size());
        ObjectNode objectNode = Json.newObject();
        setResult(objectNode, listOfRides);
        return ok(Json.toJson(objectNode));
    }

}
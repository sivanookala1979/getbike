package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Ride;
import models.RideLocation;
import models.User;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import utils.DistanceUtils;

import java.util.Date;
import java.util.List;

import static dataobject.RideStatus.RideAccepted;
import static dataobject.RideStatus.RideClosed;
import static dataobject.RideStatus.RideRequested;

/**
 * Created by sivanookala on 21/10/16.
 */
public class RideController extends BaseController {

    public Result getBike() {
        Double startLatitude = getDouble(Ride.LATITUDE);
        Double startLongitude = getDouble(Ride.LONGITUDE);
        User user = currentUser();
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        if (user != null) {
            Ride ride = new Ride();
            ride.setStartLatitude(startLatitude);
            ride.setStartLongitude(startLongitude);
            ride.setRequestorId(user.getId());
            ride.setRideStatus(RideRequested);
            ride.setRequestedAt(new Date());
            ride.save();
            result = SUCCESS;
            setJson(objectNode, Ride.RIDE_ID, ride.getId());
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result acceptRide() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            Long rideId = getLong(Ride.RIDE_ID);
            Ride ride = Ride.find.byId(rideId);
            if (ride != null && RideRequested.equals(ride.getRideStatus())) {
                ride.setRideStatus(RideAccepted);
                ride.setRiderId(user.getId());
                ride.setAcceptedAt(new Date());
                ride.save();
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
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
                if (ride != null && ride.getRiderId().equals(user.getId())) {
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
                ride.setOrderDistance(DistanceUtils.distanceMeters(locations));
                ride.save();
                objectNode.set("ride", Json.toJson(ride));
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }
}

package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Ride;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Date;

import static dataobject.RideStatus.RideAccepted;
import static dataobject.RideStatus.RideRequested;

/**
 * Created by sivanookala on 21/10/16.
 */
public class RideController extends BaseController {

    public Result getBike() {
        Double startLatitude = getDouble("latitude");
        Double startLongitude = getDouble("longitude");
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
            setJson(objectNode, "rideId", ride.getId());
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result acceptRide(){
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if(user != null)
        {
            Long rideId = getLong("rideId");
            Ride ride = Ride.find.byId(rideId);
            if(ride != null && RideRequested.equals(ride.getRideStatus()))
            {
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
}

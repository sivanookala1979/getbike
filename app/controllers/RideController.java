package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Ride;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

/**
 * Created by sivanookala on 21/10/16.
 */
public class RideController extends Controller {

    public Result getBike() {
        Double startLatitude = Double.parseDouble(request().getQueryString("latitude"));
        Double startLongitude = Double.parseDouble(request().getQueryString("longitude"));
        User user = User.find.where().eq("authToken", request().getHeader("Authorization")).findUnique();
        ObjectNode objectNode = Json.newObject();
        String result = "failure";
        if (user != null) {
            Ride ride = new Ride();
            ride.setStartLatitude(startLatitude);
            ride.setStartLongitude(startLongitude);
            ride.setRequestorId(user.getId());
            ride.save();
            result = "success";
            objectNode.set("rideId", Json.toJson(ride.getId()));
        }
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }
}

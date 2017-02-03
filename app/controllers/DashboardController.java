package controllers;

import models.RiderPosition;
import models.User;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;

import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class DashboardController extends BaseController {

    public Result allRiders() {
        String riderLocationString = "[";
        for (User user : User.find.all()) {
            System.out.println(user.getLastKnownLatitude() + " " + user.getLastKnownLongitude());
            if (user.getLastKnownLatitude() != null && user.getLastKnownLongitude() != null && user.getLastKnownLatitude() != 0.0 && user.getLastKnownLongitude() != 0.0) {
                riderLocationString += "{lat: " + user.getLastKnownLatitude() +
                        ", lng: " + user.getLastKnownLongitude() +
                        ", picture: { url : \"/assets/images/small-bike.png\", width: 32, height: 32 } " +
                        ", infowindow : \"<b>" + user.getName() + "</b> <br/>" + user.getPhoneNumber() + "\"" +
                        "},";
            }
        }
        if (riderLocationString.endsWith(",")) {
            riderLocationString = riderLocationString.substring(0, riderLocationString.length() - 1);
        }
        riderLocationString += "]";

        return ok(views.html.allRiders.render(riderLocationString));
    }

    public Result getRadiusRiders() {
        String latAndLag1 = request().getQueryString("latAndLag");
        String radius1 = request().getQueryString("radius");
        String[] split = latAndLag1.split(",");
        Double latitude = Double.valueOf(split[0].substring(1));
        Double longitude = Double.valueOf(split[1].substring(0 , split[1].length()-1));
        Double radius = Double.valueOf(radius1);
        List<User> list = User.find.where().eq("isRideInProgress", false).eq("isRequestInProgress", false).raw("( 3959 * acos( cos( radians(" + latitude +
                ") ) * cos( radians( last_known_latitude ) ) " +
                "   * cos( radians(last_known_longitude) - radians(" + longitude +
                ")) + sin(radians(" + latitude + ")) " +
                "   * sin( radians(last_known_latitude)))) < " +
                radius + " ").findList();
        Logger.info("List of objects "+list);
        return ok(Json.toJson(list));
    }

    public Result riderPositions(Long id, int rowCount){
        List<RiderPosition> list = RiderPosition.find.where().eq("user_id", id).order("lastLocationTime desc").setMaxRows(rowCount).findList();
        Logger.info("All list data size is "+list.size());
        String riderPositionsString = "[";
        int count = 0;
        for(RiderPosition riderLocation :list){
            System.out.println(riderLocation.getLastKnownLatitude() + " " + riderLocation.getLastKnownLongitude());
            if (riderLocation.getLastKnownLatitude() != null && riderLocation.getLastKnownLongitude() != null && riderLocation.getLastKnownLatitude() != 0.0 && riderLocation.getLastKnownLongitude() != 0.0) {
                riderPositionsString += "{lat: " + riderLocation.getLastKnownLatitude() +
                        ", count: "+count+
                        ", lng: " + riderLocation.getLastKnownLongitude() +
                        ", infowindow : \"<b> " + count + ". " + riderLocation.getLastLocationTime() + "</b>" + "\"" +
                        "},";
                count++;
            }
        }
        if (riderPositionsString.endsWith(",")) {
            riderPositionsString = riderPositionsString.substring(0, riderPositionsString.length() - 1);
        }
        riderPositionsString += "]";
        Logger.debug("Rider position string "+riderPositionsString);
        return ok(views.html.userRides.render(riderPositionsString));
    }

}

package controllers;

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

}

package controllers;

import models.User;
import play.mvc.Result;

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

}

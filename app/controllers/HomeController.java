package controllers;

import models.Ride;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends BaseController {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result imageAt(String imageName) {
        File imageFile = new File("public/uploads/" + imageName);
        try {
            if (imageFile.exists()) {
                String resourceType = "image+" + imageName.substring(imageName.length() - 3);
                return ok(new FileInputStream(imageFile)).as(resourceType);
            } else {
                return notFound(imageFile.getAbsoluteFile());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return notFound(imageFile.getAbsoluteFile());
        }
    }

    public Result homeScreen() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.home.render(User.find.all(), Ride.find.all()));
    }
}
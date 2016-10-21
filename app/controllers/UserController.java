package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class UserController extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public Result signup() {
        JsonNode userJson = request().body().asJson();
        User user = Json.fromJson(userJson, User.class);
        user.save();
        return ok(Json.toJson(user));
    }

}

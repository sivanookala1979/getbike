package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.LoginOtp;
import models.User;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utils.NumericUtils;

import static utils.CustomCollectionUtils.first;

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

    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
        JsonNode userJson = request().body().asJson();
        User user = Json.fromJson(userJson, User.class);
        User actual = User.find.where().eq("phoneNumber", user.getPhoneNumber()).findUnique();
        String result = "failure";
        if (actual != null) {
            LoginOtp loginOtp = new LoginOtp();
            loginOtp.setUserId(actual.getId());
            loginOtp.setGeneratedOtp(NumericUtils.generateOtp());
            loginOtp.save();
            result = "success";
        }
        ObjectNode objectNode = Json.newObject();
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));

    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result loginWithOtp() {
        JsonNode userJson = request().body().asJson();
        User actual = User.find.where().eq("phoneNumber", userJson.get("phoneNumber").textValue()).findUnique();
        String result = "failure";
        if (actual != null) {
            LoginOtp loginOtp = first(LoginOtp.find.where().eq("userId", actual.getId()).order("createdAt").findList());
            if (loginOtp != null && loginOtp.getGeneratedOtp().equals(userJson.get("otp").textValue())) {
                result = "success";
            }
        }
        ObjectNode objectNode = Json.newObject();
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));

    }
}

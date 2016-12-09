package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class ProfileController extends BaseController {

    @BodyParser.Of(BodyParser.Json.class)
    public Result saveAccountDetails() {
        String result = FAILURE;
        JsonNode userJson = request().body().asJson();
        User user = currentUser();
        if (user != null) {
            user.setAccountHolderName(userJson.get("accountHolderName").textValue());
            user.setAccountNumber(userJson.get("accountNumber").textValue());
            user.setIfscCode(userJson.get("ifscCode").textValue());
            user.setBankName(userJson.get("bankName").textValue());
            user.setBranchName(userJson.get("branchName").textValue());
            user.save();
            result = SUCCESS;
        }
        ObjectNode objectNode = Json.newObject();
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }

    public Result getAccountDetails() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            objectNode.set("accountDetails", Json.toJson(user));
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }
}

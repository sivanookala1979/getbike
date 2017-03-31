package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.RoasterRecord;
import models.User;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;

import java.util.Collections;
import java.util.List;

/**
 * Created by Siva Sudarsi on 1/12/16.
 */
public class RoasterRecordController extends BaseController {

    @BodyParser.Of(BodyParser.Json.class)
    public Result addRoasterRecord() {
        JsonNode userJson = request().body().asJson();
        RoasterRecord roasterRecord = Json.fromJson(userJson, RoasterRecord.class);
        User user = currentUser();
        String result = "failure";
        if (user != null) {
            roasterRecord.setRiderId(user.getId());
            roasterRecord.save();
            result = "success";
        }
        ObjectNode objectNode = Json.newObject();
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }

    public Result getRoaster() {
        ObjectNode objectNode = Json.newObject();
        User user = currentUser();
        String result = "failure";
        if (user != null) {
            List<RoasterRecord> records = RoasterRecord.find.where().eq("riderId", user.getId()).findList();
            Collections.reverse(records);
            objectNode.set("records", Json.toJson(records));
            result = "success";
        }
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }

}

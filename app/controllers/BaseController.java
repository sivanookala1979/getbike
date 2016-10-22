package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import scala.util.parsing.json.JSONObject;

/**
 * Created by sivanookala on 22/10/16.
 */
public class BaseController extends Controller {
    public static final String FAILURE = "failure";
    public static final String SUCCESS = "success";
    public static final String RESULT = "result";

    protected User currentUser() {
        return User.find.where().eq("authToken", request().getHeader("Authorization")).findUnique();
    }

    protected Double getDouble(String param)
    {
       return Double.parseDouble(request().getQueryString(param));
    }

    protected Long getLong(String param)
    {
        return Long.parseLong(request().getQueryString(param));
    }

    protected void setJson(ObjectNode jsonObject, String key, Object data)
    {
        jsonObject.set(key, Json.toJson(data));
    }

    protected void setResult(ObjectNode jsonObject,  Object data)
    {
        jsonObject.set(RESULT, Json.toJson(data));
    }
}

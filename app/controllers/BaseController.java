package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.User;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;

import java.util.LinkedHashMap;

/**
 * Created by sivanookala on 22/10/16.
 */
public class BaseController extends Controller {
    static boolean IS_TEST = false;
    public static final String FAILURE = "failure";
    public static final String SUCCESS = "success";
    public static final String RESULT = "result";

    @Inject
    FormFactory formFactory;

    protected User currentUser() {
        String authToken = request().getHeader("Authorization");
        if (authToken != null && !authToken.trim().isEmpty()) {
            return User.find.where().eq("authToken", authToken).findUnique();
        }
        return null;
    }

    protected Double getDouble(String param) {
        return Double.parseDouble(request().getQueryString(param));
    }

    protected Long getLong(String param) {
        return Long.parseLong(request().getQueryString(param));
    }

    protected Integer getInt(String param) {
        return Integer.parseInt(request().getQueryString(param));
    }

    protected String getString(String param) {
        return request().getQueryString(param);
    }

    protected void setJson(ObjectNode jsonObject, String key, Object data) {
        jsonObject.set(key, Json.toJson(data));
    }

    protected void setResult(ObjectNode jsonObject, Object data) {
        jsonObject.set(RESULT, Json.toJson(data));
    }

    protected LinkedHashMap<String, String> getTableHeadersList(String[] keys, String[] values) {
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        for (int index = 0; index < keys.length; index++) {
            result.put(keys[index], values[index]);
        }
        return result;
    }

    protected boolean isValidateSession() {
        if (IS_TEST) return true;
        return session("User") != null;

    }

    protected boolean isValidateAdmin() {
        return session("admin") != null;
    }


    public boolean isNotNullAndEmpty(String value) {

        return value != null && !value.isEmpty();
    }

}

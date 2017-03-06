package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.data.DynamicForm;
import play.libs.Json;


import models.*;
import play.data.FormFactory;
import play.mvc.Result;

import com.google.inject.Inject;

import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class SystemSettingsController extends BaseController {

    @Inject
    FormFactory formFactory;

    public Result getAllSystemSettings() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        ObjectNode objectNode = Json.newObject();
        List<SystemSettings> systemSetting = SystemSettings.find.orderBy("id").findList();
        objectNode.put("size", SystemSettings.find.all().size());
        setResult(objectNode, systemSetting);
        return ok(Json.toJson(objectNode));
    }

    public Result allSystemSettings() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.systemSettingsList.render());
    }

    public Result addNewSystemSetting() {
        return ok(views.html.newSystemSettings.render());
    }


    public Result saveNewSystemSetting() {
        ObjectNode objectNode = Json.newObject();
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        try {
            SystemSettings newSettings = new SystemSettings();
            DynamicForm dynamicForm = formFactory.form().bindFromRequest();
            newSettings.setKey(dynamicForm.get("key"));
            newSettings.setValue(dynamicForm.get("value"));
            newSettings.setDescription(dynamicForm.get("description"));
            newSettings.save();
            objectNode.put(SUCCESS, SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            objectNode.put(FAILURE, FAILURE);
        }
        return redirect("/systemSettings/all");
    }

    public Result deleteSystemSettings(Long id) {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        ObjectNode objectNode = Json.newObject();
        try {
            SystemSettings systemSettings = SystemSettings.find.byId(id);
            systemSettings.delete();
            objectNode.put("success", SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            objectNode.put("failure", FAILURE);
        }
        Logger.info("Status : " + objectNode);
        return redirect("/systemSettings/all");
    }

    public Result editSystemSettings(Long id) {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        SystemSettings setting = SystemSettings.find.byId(id);
        return ok(views.html.updateSystemSetting.render(setting));
    }

    public Result updateSystemSetting() {
        ObjectNode objectNode = Json.newObject();
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        try {
            DynamicForm requestData = formFactory.form().bindFromRequest();
            String Id = requestData.get("id");
            String key = requestData.get("key");
            String value = requestData.get("value");
            String description=requestData.get("description");
            SystemSettings settings = SystemSettings.find.byId(Long.valueOf(Id));
            settings.setKey(key);
            settings.setValue(value);
            settings.setDescription(description);
            settings.update();
            objectNode.put(SUCCESS, SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            objectNode.put(FAILURE, FAILURE);
        }
        Logger.info("Status : " + objectNode);
        return redirect("/systemSettings/all");
    }
}

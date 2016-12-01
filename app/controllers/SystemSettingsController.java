package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.mvc.BodyParser;


import models.*;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;

import com.google.inject.Inject;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class SystemSettingsController extends BaseController {

    @Inject
    FormFactory formFactory;
    public LinkedHashMap<String, String> tableHeaders=getTableHeadersList(new String[]{"Edit","Delete", "#", "Settings Key", "Settings Value"}, new String[]{"", "", "id", "key",  "value"});

    public Result newSystemSettings() {
        Form<SystemSettings> systemSettingsForm = formFactory.form(SystemSettings.class).bindFromRequest();
        String pageType ="New";
        return ok(views.html.newSystemSettings.render(systemSettingsForm, pageType));
    }

    public Result saveData(){
        Form<SystemSettings> systemSettingsForm = formFactory.form(SystemSettings.class).bindFromRequest();
        Logger.info("iside of saveData..");
        JsonNode jsonNode = request().body().asJson();
        ObjectNode objectNode = Json.newObject();
        try {
            if (systemSettingsForm.hasErrors()) {
                flash("error", "Missing fields");
                return badRequest(views.html.newSystemSettings.render(systemSettingsForm, "New"));
            } else {
                Logger.info("inside of else..");
                SystemSettings systemSettings = new SystemSettings();
                systemSettings.setKey(jsonNode.findPath("key").asText());
                systemSettings.setValue(jsonNode.findPath("value").asText());
                Logger.info("systemsETTINGS->" + systemSettings);
                systemSettings.save();
                Logger.info("id>"+systemSettings.id);
            }
            objectNode.put(SUCCESS, SUCCESS);
        }
        catch(Exception e){
                e.printStackTrace();
            objectNode.put(FAILURE, FAILURE);
            }

        return  ok(objectNode);
    }

    public Result systemSettingList(){
        return  ok(views.html.systemSettingsList.render(tableHeaders));
    }

    public Result performSearch(String name) {
        List<SystemSettings> systemSettings = null;
        if(name!=null && !name.isEmpty()){
            systemSettings= SystemSettings.find.where().like("upper(key)", "%" + name.toUpperCase() + "%").findList();
        }else {
            systemSettings = SystemSettings.find.where().findList();
        }
        return ok(Json.toJson(systemSettings));
    }

    public Result editSystemSetting(Long id){
        ObjectNode objectNode = Json.newObject();
        JsonNode jsonNode = request().body().asJson();
        SystemSettings systemSettings  = null;
        try{
            systemSettings = Json.fromJson(jsonNode, SystemSettings.class);
            systemSettings.update();
            objectNode.put(SUCCESS, SUCCESS);

        }catch(Exception e){
            e.printStackTrace();
            objectNode.put(FAILURE, FAILURE);
        }
        return ok(objectNode);
    }

    public Result deleteSystemSetting(Long id){
        ObjectNode objectNode = Json.newObject();
        try {
            SystemSettings.find.ref(id).delete();
            objectNode.put("success", SUCCESS);
        }catch(Exception e){
            e.printStackTrace();
            objectNode.put("failure", FAILURE);
        }
        return ok(objectNode);
    }
}

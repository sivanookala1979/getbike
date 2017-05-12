package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.CashInAdvance;
import models.GeoFencingLocation;
import models.LeaveInAdvance;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Result;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by ram on 21/4/17.
 */
public class CashInAdvanceController extends BaseController {

    public LinkedHashMap<String, String> cashInAdvanceRequests = getTableHeadersList(new String[]{"Id", "Mobile Number", "Latitude", "Longitude", "Address", "Requested At"}, new String[]{"id", "mobileNumber", "latitude", "longitude", "addressArea", "requestedAt"});

    public Result saveUserCashInAdvanceRequest() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            JsonNode locationsJson = request().body().asJson();
            CashInAdvance cashInAdvance = new CashInAdvance();
            cashInAdvance.setRiderId(user.getId());
            cashInAdvance.setRiderMobileNumber(user.getPhoneNumber());
            cashInAdvance.setRiderName(user.getName());
            cashInAdvance.setRequestStatus(null);
            cashInAdvance.setRiderDescription(locationsJson.get("riderDescription").textValue());
            cashInAdvance.setAdminDescription("NA");
            cashInAdvance.setAmount(locationsJson.get("amount").doubleValue());
            cashInAdvance.setRequestedAt(new Date());
            cashInAdvance.save();
            result = SUCCESS;
            objectNode.set("requestStatus", Json.toJson(cashInAdvance.getRequestStatus()));
            objectNode.set("requestId", Json.toJson(cashInAdvance.getId()));
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result viewCashInAdvanceList() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        ObjectNode objectNode = Json.newObject();
        List<CashInAdvance> location = CashInAdvance.find.orderBy("id").findList();
        objectNode.put("size", CashInAdvance.find.all().size());
        setResult(objectNode, location);
        return ok(Json.toJson(objectNode));
    }

    public Result getAllCashInAdvanceList() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.cashInAdvanceList.render());
    }
    public  Result processCashInAdvance(Long id){
       CashInAdvance  cashInAdvance = CashInAdvance.find.byId(id);
       return ok(views.html.processCashInAdvanceRequest.render(cashInAdvance));
    }
    public Result processCashInAdvanceRequest(){
        DynamicForm requestData = formFactory.form().bindFromRequest();
        Long id = Long.parseLong(requestData.get("id"));
        Boolean requestStatus = Boolean.valueOf(requestData.get("requestStatus"));
        System.out.print("----------------"+requestData);
        String description = requestData.get("description");
        CashInAdvance cashInAdvance = CashInAdvance.find.byId(id);
        cashInAdvance.setAdminDescription(description);
        cashInAdvance.setRequestStatus(requestStatus);
        cashInAdvance.update();
        return redirect("/getAllCashInAdvanceList");
    }
    public Result makeRequestApprove(Long id) {
        CashInAdvance cashInAdvance = CashInAdvance.find.where().eq("id", id).findUnique();
        cashInAdvance.setRequestStatus(true);
        cashInAdvance.update();
        return ok("/getAllCashInAdvanceList");
    }

    public Result makeRequestReject(Long id) {
        CashInAdvance cashInAdvance = CashInAdvance.find.where().eq("id", id).findUnique();
        cashInAdvance.setRequestStatus(false);
        cashInAdvance.update();
        return ok("/getAllCashInAdvanceList");
    }

    public Result getCashRequests() {
        ObjectNode objectNode = Json.newObject();
        User user = currentUser();
        String result = "failure";
        if (user != null) {
            List<CashInAdvance> records = CashInAdvance.find.where().eq("riderId", user.getId()).findList();
            Collections.reverse(records);
            objectNode.set("records", Json.toJson(records));
            result = "success";
        }
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }
    public Result notificationsUpdateforCashAndLeaveInAdvanceToDashboard() {
        ObjectNode objectNode = Json.newObject();
        List<CashInAdvance> cashInAdvanceList = CashInAdvance.find.where().eq("requestStatus", null).findList();
        int cashInAdvanceCount = cashInAdvanceList.size();
        List<LeaveInAdvance> leaveInAdvanceList = LeaveInAdvance.find.where().eq("requestStatus", null).findList();
        int leaveInAdvanceCount = leaveInAdvanceList.size();
        objectNode.put("notificationCountForCashInAdvance", cashInAdvanceCount);
        objectNode.put("notificationCountForLeaveInAdvance", leaveInAdvanceCount);
        objectNode.put("totalCount", leaveInAdvanceCount +cashInAdvanceCount);
        return ok(Json.toJson(objectNode));
    }

}

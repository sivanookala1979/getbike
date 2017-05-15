package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.LeaveInAdvance;
import models.User;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Result;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by RAM on 8/5/17.
 */
public class LeaveInAdvanceController extends BaseController {

    public Result saveLeaveRequest() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            JsonNode locationsJson = request().body().asJson();
            LeaveInAdvance leaveInAdvance = new LeaveInAdvance();
            leaveInAdvance.setRiderId(user.getId());
            leaveInAdvance.setRiderMobileNumber(user.getPhoneNumber());
            leaveInAdvance.setRiderName(user.getName());
            leaveInAdvance.setRequestStatus(null);
            leaveInAdvance.setRiderDescription(locationsJson.get("riderDescription").textValue());
            leaveInAdvance.setAdminDescription("NA");
            leaveInAdvance.setFromDate(locationsJson.get("fromDate").textValue());
            leaveInAdvance.setToDate(locationsJson.get("toDate").textValue());
            leaveInAdvance.setLeavesRequired(locationsJson.get("leavesRequired").textValue());
            leaveInAdvance.setRequestedAt(new Date());
            leaveInAdvance.save();
            result = SUCCESS;
            objectNode.set("requestStatus", Json.toJson(leaveInAdvance.getRequestStatus()));
            objectNode.set("requestId", Json.toJson(leaveInAdvance.getId()));
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result getLeaveRequests() {
        ObjectNode objectNode = Json.newObject();
        User user = currentUser();
        String result = "failure";
        if (user != null) {
            List<LeaveInAdvance> records = LeaveInAdvance.find.where().eq("riderId", user.getId()).findList();
            Collections.reverse(records);
            objectNode.set("records", Json.toJson(records));
            result = "success";
        }
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }

    public Result makeRequestApprove(Long id) {
        LeaveInAdvance leaveInAdvance = LeaveInAdvance.find.where().eq("id", id).findUnique();
        leaveInAdvance.setRequestStatus(true);
        leaveInAdvance.update();
        return ok("/getAllLeaveInAdvanceList");
    }

    public Result makeRequestReject(Long id) {
        LeaveInAdvance leaveInAdvance = LeaveInAdvance.find.where().eq("id", id).findUnique();
        leaveInAdvance.setRequestStatus(false);
        leaveInAdvance.update();
        return ok("/getAllLeaveInAdvanceList");
    }

    public Result viewLeaveInAdvanceList() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        ObjectNode objectNode = Json.newObject();
        List<LeaveInAdvance> location = LeaveInAdvance.find.orderBy("id").findList();
        objectNode.put("size", LeaveInAdvance.find.all().size());
        setResult(objectNode, location);
        return ok(Json.toJson(objectNode));
    }

    public Result getAllLeaveInAdvanceList() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.leaveInAdvanceList.render());
    }

    public  Result processLeaveInAdvance(Long id){
        LeaveInAdvance leaveInAdvance = LeaveInAdvance.find.byId(id);
        return ok(views.html.processLeaveInAdvanceRequest.render(leaveInAdvance));
    }

    public Result processLeaveInAdvanceRequest(){
        DynamicForm requestData = formFactory.form().bindFromRequest();
        Long id = Long.parseLong(requestData.get("id"));
        Boolean requestStatus = Boolean.valueOf(requestData.get("requestStatus"));
        System.out.print("----------------"+requestData);
        String description = requestData.get("description");
        LeaveInAdvance leaveInAdvance = LeaveInAdvance.find.byId(id);
        leaveInAdvance.setAdminDescription(description);
        leaveInAdvance.setRequestStatus(requestStatus);
        leaveInAdvance.update();
        return redirect("/getAllLeaveInAdvanceList");
    }

}

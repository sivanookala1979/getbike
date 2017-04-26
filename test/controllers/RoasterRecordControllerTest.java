package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.CashInAdvance;
import models.RoasterRecord;
import models.User;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;

import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static play.test.Helpers.*;


public class RoasterRecordControllerTest extends BaseControllerTest {

    @Test
    public void addRoasterRecordTESTHappyFlow() {
        User user = loggedInUser();
        RoasterRecord roasterRecord = new RoasterRecord();
        roasterRecord.setRideId(234l);
        roasterRecord.setCustomerOrderNumber("397297Aa/iwow");
        roasterRecord.setAmountCollected(450.0);
        roasterRecord.setDeliveryDate(new Date());
        roasterRecord.setSourceAddress("Ameerpet");
        roasterRecord.setDestinationAddress("Khairathabad");

        Result firstResult = route(fakeRequest(POST, "/addRoasterRecord").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(roasterRecord))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(firstResult);
        assertEquals("success", jsonNode.get("result").textValue());
        List<RoasterRecord> roasterRecords = RoasterRecord.find.where().eq("riderId", user.getId()).findList();
        assertEquals(1, roasterRecords.size());
        assertEquals(roasterRecord.getCustomerOrderNumber(), roasterRecords.get(0).getCustomerOrderNumber());
        assertEquals(roasterRecord.getSourceAddress(), roasterRecords.get(0).getSourceAddress());
        assertEquals(roasterRecord.getDestinationAddress(), roasterRecords.get(0).getDestinationAddress());
        assertEquals(roasterRecord.getDeliveryDate(), roasterRecords.get(0).getDeliveryDate());
        assertEquals(user.getId(), roasterRecords.get(0).getRiderId());
        assertNull(roasterRecord.getRiderId());
    }

    @Test
    public void getRoasterTESTHappyFlow() {
        User user = loggedInUser();
        RoasterRecord roasterRecord = new RoasterRecord();
        roasterRecord.setRideId(234l);
        roasterRecord.setCustomerOrderNumber("397297Aa/iwow");
        roasterRecord.setAmountCollected(450.0);
        roasterRecord.setDeliveryDate(new Date());
        roasterRecord.setSourceAddress("Ameerpet");
        roasterRecord.setDestinationAddress("Khairathabad");
        roasterRecord.setRiderId(user.getId());
        roasterRecord.save();
        RoasterRecord roasterRecord2 = new RoasterRecord();
        roasterRecord2.setCustomerOrderNumber("8272927");
        roasterRecord2.setRiderId(user.getId());
        roasterRecord2.save();

        Result actual = route(fakeRequest(GET, "/getRoaster").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(actual);
        assertEquals("success", jsonNode.get("result").textValue());
        JsonNode recordsList = jsonNode.get("records");
        int knownNumberOfRides = 2;
        assertEquals(knownNumberOfRides, recordsList.size());
        assertEquals("397297Aa/iwow", recordsList.get(1).get("customerOrderNumber").textValue());
        assertEquals("8272927", recordsList.get(0).get("customerOrderNumber").textValue());
    }

    @Test
    public void getCashRequestsHappyTestFlow() {
        User user = loggedInUser();
        CashInAdvance cashInAdvance = new CashInAdvance();
        cashInAdvance.setRiderId(user.getId());
        cashInAdvance.setAmount(236.0);
        cashInAdvance.setRequestStatus(false);
        cashInAdvance.setRiderDescription("asap!");
        cashInAdvance.save();
        CashInAdvance cashInAdvance1 = new CashInAdvance();
        cashInAdvance1.setRiderId(user.getId());
        cashInAdvance1.setAmount(58.0);
        cashInAdvance1.setRequestStatus(true);
        cashInAdvance1.setRiderDescription("plz");
        cashInAdvance1.save();

        Result actual = route(fakeRequest(GET, "/getCashRequests").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(actual);
        assertEquals("success", jsonNode.get("result").textValue());
        JsonNode recordsList = jsonNode.get("records");
        int knownNumberOfRecords = 2;
        assertEquals(knownNumberOfRecords, recordsList.size());
        assertEquals(58.0, recordsList.get(0).get("amount").doubleValue());
        assertEquals(236.0, recordsList.get(1).get("amount").doubleValue());
        assertEquals("asap!",recordsList.get(1).get("riderDescription").textValue());
    }

    @Test
    public void saveUserCashInAdvanceRequestHappyTestFlow() {
        User user = loggedInUser();
        ObjectNode requestObjectNode = Json.newObject();
        requestObjectNode.set("riderDescription", Json.toJson("Please please credit 290 rupees to my account asap!"));
        requestObjectNode.set("amount",Json.toJson(290));
        Result result = route(fakeRequest(POST, "/saveUserCashInAdvanceRequest").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        assertEquals(null, jsonNode.get("requestStatus").textValue());
    }

    @Test
    public void saveUserCashInAdvanceRequestTestFlowForApprove() {
        User user = loggedInUser();
        ObjectNode requestObjectNode = Json.newObject();
        requestObjectNode.set("riderDescription", Json.toJson("Please credit 200 rupees to my account asap!"));
        requestObjectNode.set("amount",Json.toJson(200));
        Result result = route(fakeRequest(POST, "/saveUserCashInAdvanceRequest").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        assertEquals(null, jsonNode.get("requestStatus").textValue());
        Long id = jsonNode.get("requestId").longValue();
        System.out.println("Testing phase: id is .........."+id);
        Result result1 = route(fakeRequest(GET, "/cashInAdvance/approve/" + id).header("Authorization", user.getAuthToken()));
        CashInAdvance dbCashInAdvance = CashInAdvance.find.where().eq("id",id).findUnique();
        System.out.println("Testing phase: amount requested is :"+dbCashInAdvance.getAmount());
        System.out.println("Testing phase: request status :"+dbCashInAdvance.getRequestStatus());
        assertEquals(200.0,dbCashInAdvance.getAmount());
        assertEquals(true,dbCashInAdvance.getRequestStatus().booleanValue());
        assertEquals(dbCashInAdvance.getRiderId(),user.getId());
    }

    @Test
    public void saveUserCashInAdvanceRequestTestFlowForReject() {
        User user = loggedInUser();
        ObjectNode requestObjectNode = Json.newObject();
        requestObjectNode.set("riderDescription", Json.toJson("Please credit 900 rupees to my account asap! bcoz i am suffering"));
        requestObjectNode.set("amount",Json.toJson(900));
        Result result = route(fakeRequest(POST, "/saveUserCashInAdvanceRequest").header("Authorization", user.getAuthToken()).bodyJson(requestObjectNode)).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        assertEquals(null, jsonNode.get("requestStatus").textValue());
        Long id = jsonNode.get("requestId").longValue();
        System.out.println("Testing phase: id is .........."+id);
        Result result1 = route(fakeRequest(GET, "/cashInAdvance/reject/" + id).header("Authorization", user.getAuthToken()));
        CashInAdvance dbCashInAdvance = CashInAdvance.find.where().eq("id",id).findUnique();
        System.out.println("Testing phase: amount requested is :"+dbCashInAdvance.getAmount());
        System.out.println("Testing phase: request status :"+dbCashInAdvance.getRequestStatus());
        assertEquals(900.0,dbCashInAdvance.getAmount());
        assertEquals(false,dbCashInAdvance.getRequestStatus().booleanValue());
        assertEquals(dbCashInAdvance.getRiderId(),user.getId());
    }

}
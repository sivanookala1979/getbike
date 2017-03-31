package controllers;

import com.fasterxml.jackson.databind.JsonNode;
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

}
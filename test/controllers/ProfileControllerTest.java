package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;

import static junit.framework.TestCase.assertEquals;
import static play.test.Helpers.*;


public class ProfileControllerTest extends BaseControllerTest {

    @Test
    public void saveAccountDetailsTESTHappyFlow() {
        User user = loggedInUser();
        ObjectNode accountDetails = Json.newObject();
        accountDetails.put("accountHolderName", "Adarsh Thatikonda");
        accountDetails.put("accountNumber", "123456");
        accountDetails.put("ifscCode", "IF00023");
        accountDetails.put("bankName", "Axis");
        accountDetails.put("branchName", "Kavali Axis");
        Result result = route(fakeRequest(POST, "/profile/saveAccountDetails").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(accountDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        User actual = User.find.byId(user.getId());
        assertEquals("Adarsh Thatikonda", actual.getAccountHolderName());
        assertEquals("123456", actual.getAccountNumber());
        assertEquals("IF00023", actual.getIfscCode());
        assertEquals("Axis", actual.getBankName());
        assertEquals("Kavali Axis", actual.getBranchName());
    }

    @Test
    public void getAccountDetailsTestHappyFlow() {
        User user = loggedInUser();
        user.setAccountHolderName("Adarsh Thatikonda");
        user.setAccountNumber("123456");
        user.setIfscCode("IF00023");
        user.setBankName("Axis");
        user.setBranchName("Kavali Axis");
        user.save();
        Result result = route(fakeRequest(GET, "/profile/getAccountDetails").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("success", jsonNode.get("result").textValue());
        assertEquals(user.getAccountHolderName(), jsonNode.get("accountDetails").get("accountHolderName").textValue());
        assertEquals(user.getAccountNumber(), jsonNode.get("accountDetails").get("accountNumber").textValue());
        assertEquals(user.getIfscCode(), jsonNode.get("accountDetails").get("ifscCode").textValue());
        assertEquals(user.getBankName(), jsonNode.get("accountDetails").get("bankName").textValue());
        assertEquals(user.getBranchName(), jsonNode.get("accountDetails").get("branchName").textValue());

    }
}
package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Ride;
import models.User;
import models.Wallet;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import utils.CustomCollectionUtils;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static play.test.Helpers.*;


public class WalletControllerTest extends BaseControllerTest {

    @Test
    public void addMoneyTESTHappyFlow() {
        User user = loggedInUser();
        ObjectNode walletDetails = Json.newObject();
        walletDetails.put("amount", 50.0);
       Result result = route(fakeRequest(POST, "/wallet/addMoney").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(walletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        Logger.info(jsonNode.get("result").textValue().toString());
        Wallet wallet = CustomCollectionUtils.first(Wallet.find.where().eq("userId", user.getId()).findList());
        assertEquals(50.0*10, wallet.getAmount());
        assertNotNull(wallet.getTransactionDateTime());
        assertEquals("Added 500.0 points for a recharge of Rs 50.0", wallet.getDescription());

        assertEquals("success", jsonNode.get("result").textValue());
    }

    @Test
    public void getBalanceAmountTESTHappyFlow() {
        User user = loggedInUser();
        ObjectNode walletDetails = Json.newObject();
        walletDetails.put("amount", 65.0);
        route(fakeRequest(POST, "/wallet/addMoney").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(walletDetails))).withHeader("Content-Type", "application/json");

        walletDetails = Json.newObject();
        walletDetails.put("amount", 50.0);
        route(fakeRequest(POST, "/wallet/addMoney").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(walletDetails))).withHeader("Content-Type", "application/json");

        Result result = route(fakeRequest(GET, "/wallet/getBalanceAmount").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);

        assertEquals(1150.0, jsonNode.get("balanceAmount").doubleValue());
        assertEquals("success", jsonNode.get("result").textValue());
    }

    @Test
    public void addRechargeMobileTESTHappyFlow() {
        User user = new User();
        ObjectNode walletDetails = Json.newObject();
        walletDetails.put("mobileNumber", "9949257729");
        walletDetails.put("amount", 50.0);
        walletDetails.put("operator", "Airtel");
        walletDetails.put("circle", "AP");
        Result result = route(fakeRequest(POST, "/wallet/addMoney").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(walletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals("failure", jsonNode.get("result").textValue());
    }

}
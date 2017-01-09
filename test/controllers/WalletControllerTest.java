package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import models.Wallet;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import utils.CustomCollectionUtils;
import utils.GetBikeUtils;

import java.util.Date;

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
        assertEquals(50.0 * 10, wallet.getAmount());
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

        Wallet rideEntry1 = new Wallet();
        rideEntry1.setUserId(user.getId());
        rideEntry1.setAmount(-67.0);
        rideEntry1.setType("RideGiven");
        rideEntry1.save();
        Wallet rideEntry2 = new Wallet();
        rideEntry2.setUserId(user.getId());
        rideEntry2.setAmount(-33.0);
        rideEntry2.setType("RideGiven");
        rideEntry2.save();
        Result result = route(fakeRequest(GET, "/wallet/getBalanceAmount").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);

        assertEquals(1050.0, jsonNode.get("balanceAmount").doubleValue());
        assertEquals("success", jsonNode.get("result").textValue());
    }

    @Test
    public void myEntriesTESTHappyFlow() {
        User user = loggedInUser();
        ObjectNode walletDetails = Json.newObject();
        walletDetails.put("amount", 65.0);
        route(fakeRequest(POST, "/wallet/addMoney").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(walletDetails))).withHeader("Content-Type", "application/json");
        GetBikeUtils.sleep(200);
        walletDetails = Json.newObject();
        walletDetails.put("amount", 50.0);
        route(fakeRequest(POST, "/wallet/addMoney").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(walletDetails))).withHeader("Content-Type", "application/json");
        GetBikeUtils.sleep(200);
        Wallet rideEntry1 = new Wallet();
        rideEntry1.setUserId(user.getId());
        rideEntry1.setAmount(-67.0);
        rideEntry1.setType("RideGiven");
        rideEntry1.setTransactionDateTime(new Date());
        rideEntry1.save();
        GetBikeUtils.sleep(200);
        Wallet rideEntry2 = new Wallet();
        rideEntry2.setUserId(user.getId());
        rideEntry2.setAmount(-33.0);
        rideEntry2.setType("RideGiven");
        rideEntry2.setTransactionDateTime(new Date());
        rideEntry2.save();
        Result result = route(fakeRequest(GET, "/wallet/myEntries").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);
        System.out.println(jsonNode);
        assertEquals(4, jsonNode.get("entries").size());
        assertEquals("RideGiven", jsonNode.get("entries").get(0).get("type").textValue());
        assertEquals(-33.0, jsonNode.get("entries").get(0).get("amount").doubleValue());
        assertEquals("AddMoney", jsonNode.get("entries").get(2).get("type").textValue());
        assertEquals(500.0, jsonNode.get("entries").get(2).get("amount").doubleValue());
        assertEquals("success", jsonNode.get("result").textValue());
    }

    @Test
    public void addRechargeMobileTESTHappyFlow() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode rechargeDetails = Json.newObject();
        rechargeDetails.put("mobileNumber", "9949257729");
        rechargeDetails.put("amount", 100.0);
        rechargeDetails.put("operator", "Airtel");
        rechargeDetails.put("circle", "AP");
        Result result = route(fakeRequest(POST, "/wallet/rechargeMobile").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(rechargeDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.SUCCESS, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(0.0, new WalletController().getWalletAmount(user));
    }

    @Test
    public void addRechargeMobileTESTWithAmountAsNegative() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode rechargeDetails = Json.newObject();
        rechargeDetails.put("mobileNumber", "9949257729");
        rechargeDetails.put("amount", -100.0);
        rechargeDetails.put("operator", "Airtel");
        rechargeDetails.put("circle", "AP");
        Result result = route(fakeRequest(POST, "/wallet/rechargeMobile").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(rechargeDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.FAILURE, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(walletAmount.getAmount(), new WalletController().getWalletAmount(user));
    }

    @Test
    public void addRechargeMobileTESTWithAmountAsZero() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode rechargeDetails = Json.newObject();
        rechargeDetails.put("mobileNumber", "9949257729");
        rechargeDetails.put("amount", 0.0);
        rechargeDetails.put("operator", "Airtel");
        rechargeDetails.put("circle", "AP");
        Result result = route(fakeRequest(POST, "/wallet/rechargeMobile").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(rechargeDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.SUCCESS, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(walletAmount.getAmount(), new WalletController().getWalletAmount(user));
    }

    @Test
    public void addRechargeMobileTESTWithAmountAsMoreThanTheWalletAmount() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode rechargeDetails = Json.newObject();
        rechargeDetails.put("mobileNumber", "9949257729");
        rechargeDetails.put("amount", 200.0);
        rechargeDetails.put("operator", "Airtel");
        rechargeDetails.put("circle", "AP");
        Result result = route(fakeRequest(POST, "/wallet/rechargeMobile").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(rechargeDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.FAILURE, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(walletAmount.getAmount(), new WalletController().getWalletAmount(user));
    }

    @Test
    public void addRechargeMobileTESTWithAmountLessThanTheWalletAmount() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode rechargeDetails = Json.newObject();
        rechargeDetails.put("mobileNumber", "9949257729");
        rechargeDetails.put("amount", 50.0);
        rechargeDetails.put("operator", "Airtel");
        rechargeDetails.put("circle", "AP");
        Result result = route(fakeRequest(POST, "/wallet/rechargeMobile").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(rechargeDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.SUCCESS, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(500.0, new WalletController().getWalletAmount(user));
    }

    @Test
    public void redeemToWalletTESTHappyFlow() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode payTmWalletDetails = Json.newObject();
        payTmWalletDetails.put("mobileNumber", "9960862529");
        payTmWalletDetails.put("walletName", "PayTm");
        payTmWalletDetails.put("amount", 100.0);
        Result result = route(fakeRequest(POST, "/wallet/redeemToWallet").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(payTmWalletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.SUCCESS, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(0.0, new WalletController().getWalletAmount(user));
    }

    @Test
    public void redeemToWalletTESTWithAmountAsNegative() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode payTmWalletDetails = Json.newObject();
        payTmWalletDetails.put("mobileNumber", "9960862529");
        payTmWalletDetails.put("walletName", "PayTm");
        payTmWalletDetails.put("amount", -100.0);
        Result result = route(fakeRequest(POST, "/wallet/redeemToWallet").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(payTmWalletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.FAILURE, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(walletAmount.getAmount(), new WalletController().getWalletAmount(user));
    }

    @Test
    public void redeemToWalletTESTWithAmountAsZero() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode payTmWalletDetails = Json.newObject();
        payTmWalletDetails.put("mobileNumber", "9960862529");
        payTmWalletDetails.put("walletName", "PayTm");
        payTmWalletDetails.put("amount", 0.0);
        Result result = route(fakeRequest(POST, "/wallet/redeemToWallet").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(payTmWalletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.SUCCESS, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(walletAmount.getAmount(), new WalletController().getWalletAmount(user));
    }

    @Test
    public void redeemToWalletTESTWithAmountAsMoreThanTheWalletAmount() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode payTmWalletDetails = Json.newObject();
        payTmWalletDetails.put("mobileNumber", "9960862529");
        payTmWalletDetails.put("walletName", "PayTm");
        payTmWalletDetails.put("amount", 200.0);
        Result result = route(fakeRequest(POST, "/wallet/redeemToWallet").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(payTmWalletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.FAILURE, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(walletAmount.getAmount(), new WalletController().getWalletAmount(user));
    }

    @Test
    public void redeemToWalletTESTWithAmountAsLessThanTheWalletAmount() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode payTmWalletDetails = Json.newObject();
        payTmWalletDetails.put("mobileNumber", "9960862529");
        payTmWalletDetails.put("walletName", "PayTm");
        payTmWalletDetails.put("amount", 25.0);
        Result result = route(fakeRequest(POST, "/wallet/redeemToWallet").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(payTmWalletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.SUCCESS, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(750.0, new WalletController().getWalletAmount(user));
    }

    @Test
    public void redeemToBankTESTHappyFlow() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode walletDetails = Json.newObject();
        walletDetails.put("amount", 100.0);
        Result result = route(fakeRequest(POST, "/wallet/redeemToBank").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(walletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.SUCCESS, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(0.0, new WalletController().getWalletAmount(user));
    }

    @Test
    public void redeemToBankTESTWithAmountAsNegative() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode walletDetails = Json.newObject();
        walletDetails.put("amount", -100.0);
        Result result = route(fakeRequest(POST, "/wallet/redeemToBank").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(walletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.FAILURE, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(walletAmount.getAmount(), new WalletController().getWalletAmount(user));
    }

    @Test
    public void redeemToBankTESTWithAmountMorethanTheWalletAmount() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode walletDetails = Json.newObject();
        walletDetails.put("amount", 200.0);
        Result result = route(fakeRequest(POST, "/wallet/redeemToBank").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(walletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.FAILURE, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(walletAmount.getAmount(), new WalletController().getWalletAmount(user));
    }

    @Test
    public void redeemToBankTESTWithAmountLessthanTheWalletAmount() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode walletDetails = Json.newObject();
        walletDetails.put("amount", 10.0);
        Result result = route(fakeRequest(POST, "/wallet/redeemToBank").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(walletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.SUCCESS, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(900.0, new WalletController().getWalletAmount(user));
    }

    @Test
    public void redeemToBankTESTWithAmountAsZero() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet walletAmount = createWallet(user);
        ObjectNode walletDetails = Json.newObject();
        walletDetails.put("amount", 0.0);
        Result result = route(fakeRequest(POST, "/wallet/redeemToBank").header("Authorization", user.getAuthToken()).bodyJson(Json.toJson(walletDetails))).withHeader("Content-Type", "application/json");
        JsonNode jsonNode = jsonFromResult(result);
        assertEquals(BaseController.SUCCESS, jsonNode.get(BaseController.RESULT).textValue());
        assertEquals(walletAmount.getAmount(), new WalletController().getWalletAmount(user));
    }

    @NotNull
    private Wallet createWallet(User user) {
        Wallet walletAmount = new Wallet();
        walletAmount.setUserId(user.getId());
        walletAmount.setAmount(1000.0);
        walletAmount.setType("AddMoney");
        walletAmount.save();
        return walletAmount;
    }

}
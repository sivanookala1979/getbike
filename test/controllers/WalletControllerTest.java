package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dataobject.WalletEntryType;
import models.User;
import models.Wallet;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import utils.DateUtils;
import utils.GetBikeUtils;

import java.util.Date;

import static dataobject.WalletEntryType.*;
import static junit.framework.TestCase.assertEquals;
import static play.test.Helpers.*;


public class WalletControllerTest extends BaseControllerTest {


    @Test
    public void getBalanceAmountTESTHappyFlow() {
        User user = loggedInUser();
        user.setFreeRidesEarned(2);
        user.setFreeRidesSpent(1);
        user.save();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet addCash1 = new Wallet();
        addCash1.setAmount(650.0);
        addCash1.setType(WalletEntryType.PAY_U_PAYMENT);
        addCash1.setUserId(user.getId());
        addCash1.save();
        GetBikeUtils.sleep(200);
        Wallet addCash2 = new Wallet();
        addCash2.setAmount(500.0);
        addCash2.setType(PAY_U_PAYMENT);
        addCash2.setUserId(user.getId());
        addCash2.save();
        GetBikeUtils.sleep(200);

        Wallet rideEntry1 = new Wallet();
        rideEntry1.setUserId(user.getId());
        rideEntry1.setAmount(-67.0);
        rideEntry1.setType(RIDE_GIVEN);
        rideEntry1.save();
        GetBikeUtils.sleep(200);

        Wallet rideEntry2 = new Wallet();
        rideEntry2.setUserId(user.getId());
        rideEntry2.setAmount(-33.0);
        rideEntry2.setType(RIDE_GIVEN);
        rideEntry2.save();
        Result result = route(fakeRequest(GET, "/wallet/getBalanceAmount").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);

        assertEquals(1050.0, jsonNode.get("balanceAmount").doubleValue());
        assertEquals(1050.0, jsonNode.get("cashBalance").doubleValue());
        assertEquals(0.0, jsonNode.get("promoBalance").doubleValue());
        assertEquals(1050.0, jsonNode.get("userBalance").doubleValue());
        assertEquals(2, jsonNode.get("freeRidesEarned").intValue());
        assertEquals(1, jsonNode.get("freeRidesSpent").intValue());
        assertEquals("success", jsonNode.get("result").textValue());
    }

    @Test
    public void getBalanceAmountTESTWithPromoBalanceAndCashBalance() {
        User user = loggedInUser();
        user.setFreeRidesEarned(2);
        user.setFreeRidesSpent(1);
        user.save();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet addCash1 = new Wallet();
        addCash1.setAmount(650.0);
        addCash1.setType(WalletEntryType.PAY_U_PAYMENT);
        addCash1.setUserId(user.getId());
        addCash1.save();
        GetBikeUtils.sleep(200);
        Wallet addCash2 = new Wallet();
        addCash2.setAmount(500.0);
        addCash2.setType(BONUS_POINTS);
        addCash2.setUserId(user.getId());
        addCash2.save();
        GetBikeUtils.sleep(200);

        Wallet rideEntry1 = new Wallet();
        rideEntry1.setUserId(user.getId());
        rideEntry1.setAmount(-67.0);
        rideEntry1.setType(RIDE_GIVEN);
        rideEntry1.save();
        GetBikeUtils.sleep(200);

        Wallet rideEntry2 = new Wallet();
        rideEntry2.setUserId(user.getId());
        rideEntry2.setAmount(-33.0);
        rideEntry2.setType(RIDE_GIVEN);
        rideEntry2.save();
        Result result = route(fakeRequest(GET, "/wallet/getBalanceAmount").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);

        assertEquals(1050.0, jsonNode.get("balanceAmount").doubleValue());
        assertEquals(650.0, jsonNode.get("cashBalance").doubleValue());
        assertEquals(400.0, jsonNode.get("promoBalance").doubleValue());
        assertEquals(1050.0, jsonNode.get("userBalance").doubleValue());
        assertEquals(2, jsonNode.get("freeRidesEarned").intValue());
        assertEquals(1, jsonNode.get("freeRidesSpent").intValue());
        assertEquals("success", jsonNode.get("result").textValue());
    }

    @Test
    public void getBalanceAmountTESTWithOnlyPromoBalance() {
        User user = loggedInUser();
        user.setFreeRidesEarned(2);
        user.setFreeRidesSpent(1);
        user.save();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet addCash1 = new Wallet();
        addCash1.setAmount(650.0);
        addCash1.setType(WalletEntryType.BONUS_POINTS);
        addCash1.setUserId(user.getId());
        addCash1.save();
        GetBikeUtils.sleep(200);
        Wallet addCash2 = new Wallet();
        addCash2.setAmount(500.0);
        addCash2.setType(BONUS_POINTS);
        addCash2.setUserId(user.getId());
        addCash2.save();
        GetBikeUtils.sleep(200);

        Wallet rideEntry1 = new Wallet();
        rideEntry1.setUserId(user.getId());
        rideEntry1.setAmount(-67.0);
        rideEntry1.setType(RIDE_GIVEN);
        rideEntry1.save();
        GetBikeUtils.sleep(200);

        Wallet rideEntry2 = new Wallet();
        rideEntry2.setUserId(user.getId());
        rideEntry2.setAmount(-33.0);
        rideEntry2.setType(RIDE_GIVEN);
        rideEntry2.save();
        Result result = route(fakeRequest(GET, "/wallet/getBalanceAmount").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);

        assertEquals(1050.0, jsonNode.get("balanceAmount").doubleValue());
        assertEquals(0.0, jsonNode.get("cashBalance").doubleValue());
        assertEquals(1050.0, jsonNode.get("promoBalance").doubleValue());
        assertEquals(1050.0, jsonNode.get("userBalance").doubleValue());
        assertEquals(2, jsonNode.get("freeRidesEarned").intValue());
        assertEquals(1, jsonNode.get("freeRidesSpent").intValue());
        assertEquals("success", jsonNode.get("result").textValue());
    }

    @Test
    public void getBalanceAmountTESTWithOnlyCashBalance() {
        User user = loggedInUser();
        user.setFreeRidesEarned(2);
        user.setFreeRidesSpent(2);
        user.save();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet addCash1 = new Wallet();
        addCash1.setAmount(650.0);
        addCash1.setType(WalletEntryType.PAY_U_PAYMENT);
        addCash1.setUserId(user.getId());
        addCash1.save();
        GetBikeUtils.sleep(200);
        Wallet addCash2 = new Wallet();
        addCash2.setAmount(500.0);
        addCash2.setType(PAY_U_PAYMENT);
        addCash2.setUserId(user.getId());
        addCash2.save();
        GetBikeUtils.sleep(200);

        Wallet rideEntry1 = new Wallet();
        rideEntry1.setUserId(user.getId());
        rideEntry1.setAmount(-300.0);
        rideEntry1.setType(REDEEM_TO_WALLET);
        rideEntry1.save();
        GetBikeUtils.sleep(200);

        Wallet rideEntry2 = new Wallet();
        rideEntry2.setUserId(user.getId());
        rideEntry2.setAmount(-400.0);
        rideEntry2.setType(REDEEM_TO_BANK);
        rideEntry2.save();
        Result result = route(fakeRequest(GET, "/wallet/getBalanceAmount").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);

        assertEquals(450.0, jsonNode.get("balanceAmount").doubleValue());
        assertEquals(450.0, jsonNode.get("cashBalance").doubleValue());
        assertEquals(0.0, jsonNode.get("promoBalance").doubleValue());
        assertEquals(450.0, jsonNode.get("userBalance").doubleValue());
        assertEquals(2, jsonNode.get("freeRidesEarned").intValue());
        assertEquals(2, jsonNode.get("freeRidesSpent").intValue());
        assertEquals("success", jsonNode.get("result").textValue());
    }


    @Test
    public void myEntriesTESTHappyFlow() {
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        Wallet addCash1 = new Wallet();
        addCash1.setAmount(650.0);
        addCash1.setType(PAY_U_PAYMENT);
        addCash1.setUserId(user.getId());
        addCash1.save();
        GetBikeUtils.sleep(200);

        Wallet addCash2 = new Wallet();
        addCash2.setAmount(500.0);
        addCash2.setType(PAY_U_PAYMENT);
        addCash2.setUserId(user.getId());
        addCash2.save();
        GetBikeUtils.sleep(200);

        Wallet rideEntry1 = new Wallet();
        rideEntry1.setUserId(user.getId());
        rideEntry1.setAmount(-67.0);
        rideEntry1.setType(RIDE_GIVEN);
        rideEntry1.setTransactionDateTime(new Date());
        rideEntry1.save();
        GetBikeUtils.sleep(200);
        Wallet rideEntry2 = new Wallet();
        rideEntry2.setUserId(user.getId());
        rideEntry2.setAmount(-33.0);
        rideEntry2.setType(RIDE_GIVEN);
        rideEntry2.setTransactionDateTime(new Date());
        rideEntry2.save();
        Result result = route(fakeRequest(GET, "/wallet/myEntries").header("Authorization", user.getAuthToken()));
        JsonNode jsonNode = jsonFromResult(result);
        System.out.println(jsonNode);
        assertEquals(4, jsonNode.get("entries").size());
        assertEquals(RIDE_GIVEN, jsonNode.get("entries").get(0).get("type").textValue());
        assertEquals(-33.0, jsonNode.get("entries").get(0).get("amount").doubleValue());
        assertEquals(PAY_U_PAYMENT, jsonNode.get("entries").get(2).get("type").textValue());
        assertEquals(650.0, jsonNode.get("entries").get(2).get("amount").doubleValue());
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

    @Test
    public void addBonusPointsToWalletTESTHappyFlow() {
        BaseController.IS_TEST = true;
        User user = loggedInUser();
        Ebean.deleteAll(Wallet.find.where().eq("user_id", user.id).findList());
        route(fakeRequest(GET, "/wallet/addBonusPointsToWallet/" + user.id + "/30").header("Authorization", user.getAuthToken()));
        assertEquals(30.0, new WalletController().getWalletAmount(user));
    }

    @NotNull
    private Wallet createWallet(User user) {
        Wallet walletAmount = new Wallet();
        walletAmount.setUserId(user.getId());
        walletAmount.setAmount(1000.0);
        walletAmount.setType(WalletEntryType.PAY_U_PAYMENT);
        walletAmount.save();
        return walletAmount;
    }
    @Test
    public void dateWiseFilterForRedeemEventRedeemToBankTESTWithHappyFlow(){
        User user = loggedInUser();
        user.setName("Wahid");
        user.update();
        Wallet wallet = new Wallet();
        wallet.setUserId(user.id);
        wallet.setUserName(user.name);
        wallet.setTransactionDateTime(new Date());
        wallet.setStatusActedAt(new Date());
        wallet.setType(REDEEM_TO_BANK);
        wallet.setAmount(-100.0);
        wallet.setNotificationSeen(false);
        wallet.setDescription("Transfer Rs.100.0 To your Given Bank Account");
        wallet.setMobileNumber("9960862529");
        wallet.save();
        Result result = route(fakeRequest(GET, "/dateWiseFilterForRedeem?startDate=" + DateUtils.convertDateToString(new Date(), DateUtils.YYYYMMDD) + "&endDate=" + DateUtils.convertDateToString(new Date(), DateUtils.YYYYMMDD) + "&status=StatusALL&redeemType=TypeALL&srcName=" + user.getName()));
        JsonNode actual = jsonFromResult(result);
        assertEquals("9960862529", actual.findPath("mobileNumber").textValue());
        assertEquals(wallet.getUserName(),actual.findPath("userName").asText());
        assertEquals(wallet.getType(),actual.findPath("type").asText());
        assertEquals(wallet.getAmount(),actual.findPath("amount").asDouble());
    }
    @Test
    public void dateWiseFilterForRedeemEventRedeemToWalletTESTWithHappyFlow(){
        User user = loggedInUser();
        user.setName("Wahid");
        user.update();
        Wallet wallet = new Wallet();
        wallet.setUserId(user.id);
        wallet.setUserName(user.name);
        wallet.setTransactionDateTime(new Date());
        wallet.setStatusActedAt(new Date());
        wallet.setType(REDEEM_TO_WALLET);
        wallet.setAmount(-100.0);
        wallet.setWalletName("PayTm");
        wallet.setDescription("Transfer Rs.100.0 To your PayTm Wallet");
        wallet.setMobileNumber("9960862529");
        wallet.setNotificationSeen(false);
        wallet.save();
        Result result = route(fakeRequest(GET, "/dateWiseFilterForRedeem?startDate=" + DateUtils.convertDateToString(new Date(), DateUtils.YYYYMMDD) + "&endDate=" + DateUtils.convertDateToString(new Date(), DateUtils.YYYYMMDD) + "&status=StatusALL&redeemType=TypeALL&srcName=" + user.getName()));
        JsonNode actual = jsonFromResult(result);
        assertEquals("9960862529", actual.findPath("mobileNumber").textValue());
        assertEquals(wallet.getUserName(),actual.findPath("userName").asText());
        assertEquals(wallet.getType(),actual.findPath("type").asText());
        assertEquals(wallet.getAmount(),actual.findPath("amount").asDouble());
        assertEquals(wallet.getWalletName(),actual.findPath("walletName").asText());
    }
    @Test
    public void dateWiseFilterForRedeemEventRechargeTESTWithHappyFlow(){
        User user = loggedInUser();
        user.setName("Wahid");
        user.update();
        Wallet wallet = new Wallet();
        wallet.setUserId(user.id);
        wallet.setUserName(user.name);
        wallet.setTransactionDateTime(new Date());
        wallet.setStatusActedAt(new Date());
        wallet.setType(MOBILE_RECHARGE);
        wallet.setAmount(-100.0);
        wallet.setDescription("Recharged amount of Rs. 100.0 for your mobile number 9960862529");
        wallet.setMobileNumber("9960862529");
        wallet.setNotificationSeen(false);
        wallet.save();
        Result result = route(fakeRequest(GET, "/dateWiseFilterForRedeem?startDate=" + DateUtils.convertDateToString(new Date(), DateUtils.YYYYMMDD) + "&endDate=" + DateUtils.convertDateToString(new Date(), DateUtils.YYYYMMDD) + "&status=StatusALL&redeemType=TypeALL&srcName=" + user.getName()));
        JsonNode actual = jsonFromResult(result);
        assertEquals("9960862529", actual.findPath("mobileNumber").textValue());
        assertEquals(wallet.getUserName(),actual.findPath("userName").asText());
        assertEquals(wallet.getType(),actual.findPath("type").asText());
        assertEquals(wallet.getAmount(),actual.findPath("amount").asDouble());
    }

}
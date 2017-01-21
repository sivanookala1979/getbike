package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import models.Wallet;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;

import java.util.Date;
import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class WalletController extends BaseController {

    public static final double AMOUNT_MULTIPLIER = 10.0;

    @BodyParser.Of(BodyParser.Json.class)
    public Result addMoney() {
        String result = FAILURE;
        JsonNode userJson = request().body().asJson();
        User user = currentUser();
        if (user != null) {
            Wallet wallet = new Wallet();
            wallet.setUserId(user.getId());
            double amount = userJson.get("amount").doubleValue();
            wallet.setAmount(convertToWalletAmount(amount));
            wallet.setTransactionDateTime(new Date());
            wallet.setDescription("Added " + wallet.getAmount() + " points for a recharge of Rs " + amount);
            wallet.setType("AddMoney");
            wallet.save();
            result = SUCCESS;
        }
        ObjectNode objectNode = Json.newObject();
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }

    public Result rechargeMobile() {
        String result = FAILURE;
        JsonNode userJson = request().body().asJson();
        User user = currentUser();
        if (user != null) {
            double amount = userJson.get("amount").doubleValue();
            double walletAmount = getWalletAmount(user);
            if (hasValidAmountInWallet(amount, walletAmount)) {
                Wallet wallet = new Wallet();
                wallet.setUserId(user.getId());
                wallet.setMobileNumber(userJson.get("mobileNumber").textValue());
                wallet.setCircle(userJson.get("circle").textValue());
                wallet.setOperator(userJson.get("operator").textValue());
                wallet.setType("MobileRecharge");
                wallet.setAmount(-convertToWalletAmount(amount));
                wallet.setDescription("Recharged amount of Rs. " + amount + " for your mobile number " + wallet.getMobileNumber());
                wallet.setTransactionDateTime(new Date());
                wallet.save();
                result = SUCCESS;
            }

        }
        ObjectNode objectNode = Json.newObject();
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }


    public Result redeemToWallet() {
        String result = FAILURE;
        JsonNode userJson = request().body().asJson();
        User user = currentUser();
        if (user != null) {
            double amount = userJson.get("amount").doubleValue();
            double walletAmount = getWalletAmount(user);
            if (hasValidAmountInWallet(amount, walletAmount)) {
                Wallet wallet = new Wallet();
                wallet.setUserId(user.getId());
                wallet.setMobileNumber(userJson.get("mobileNumber").textValue());
                wallet.setWalletName(userJson.get("walletName").textValue());
                wallet.setAmount(-convertToWalletAmount(amount));
                wallet.setDescription("Transfer Rs." + amount + " To your" + wallet.getWalletName() + " Wallet");
                wallet.setTransactionDateTime(new Date());
                wallet.setType("RedeemToWallet");
                wallet.save();
                result = SUCCESS;
            }

        }
        ObjectNode objectNode = Json.newObject();
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }


    public Result redeemToBank() {
        String result = FAILURE;
        JsonNode userJson = request().body().asJson();
        User user = currentUser();
        if (user != null) {
            Wallet wallet = new Wallet();
            wallet.setUserId(user.getId());
            double amount = userJson.get("amount").doubleValue();
            double walletAmount = getWalletAmount(user);
            if (hasValidAmountInWallet(amount, walletAmount)) {
                wallet.setAmount(-convertToWalletAmount(amount));
                wallet.setDescription("Transfer Rs." + amount + " To your Given Bank Account");
                wallet.setTransactionDateTime(new Date());
                wallet.setType("RedeemToBank");
                wallet.save();
                result = SUCCESS;
            }

        }
        ObjectNode objectNode = Json.newObject();
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }

    public Result getBalanceAmount() {
        String result = FAILURE;
        User user = currentUser();
        ObjectNode objectNode = Json.newObject();

        if (user != null) {
            double amount = getWalletAmount(user);
            objectNode.set("balanceAmount", Json.toJson(amount));
            result = SUCCESS;
        }
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }

    public Result myEntries() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            ArrayNode entriesNodes = Json.newArray();
            List<Wallet> entriesList = Wallet.find.where().eq("userId", user.getId()).order("transactionDateTime desc").findList();
            for (Wallet entry : entriesList) {
                entriesNodes.add(Json.toJson(entry));
            }
            objectNode.set("entries", entriesNodes);
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }


    public static double getWalletAmount(User user) {
        List<Wallet> wallets = Wallet.find.where().eq("userId", user.getId()).findList();
        double amount = 0;
        for (Wallet wallet : wallets) {
            amount += wallet.getAmount();
        }
        return amount;
    }

    public static double convertToWalletAmount(double amount) {
        return amount * AMOUNT_MULTIPLIER;
    }

    public static boolean hasValidAmountInWallet(double amount, double walletAmount) {
        return amount >= 0 && amount <= convertToCash(walletAmount);
    }

    public static boolean hasPointsInWallet(double points, double walletAmount) {
        return points >= 0 && points <= walletAmount;
    }

    public static double convertToCash(double walletAmount) {
        return walletAmount / AMOUNT_MULTIPLIER;
    }

    public Result walletEntries(Long id) {
        if (isValidateSession()) {
            User user = User.find.byId(id);
            if (user != null) {
                double amount = getWalletAmount(user);
                List<Wallet> entries = Wallet.find.where().eq("userId", user.getId()).findList();
                return ok(views.html.walletEntries.render(entries, amount, user));
            }
        }

        return redirect("/users/usersList");
    }

    public Result addBonusPointsToWallet(Long id, int amount) {
        if (isValidateAdmin()) {
            Wallet wallet = new Wallet();
            wallet.setUserId(id);
            wallet.setTransactionDateTime(new Date());
            wallet.setAmount((double) amount);
            wallet.setType("BonusPoints");
            wallet.setDescription("Bonus Points from GetBike");
            wallet.save();
        }
        return redirect("/wallet/entries/" + id);
    }
}

package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dataobject.WalletEntryType;
import models.User;
import models.Wallet;
import play.libs.Json;
import play.mvc.Result;

import java.util.Date;
import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class WalletController extends BaseController {

    public static final double AMOUNT_MULTIPLIER = 10.0;

    public Result rechargeMobile() {
        String result = FAILURE;
        JsonNode userJson = request().body().asJson();
        User user = currentUser();
        if (user != null) {
            double amount = userJson.get("amount").doubleValue();
            if (canPayOutUsingCash(user, amount)) {
                Wallet wallet = new Wallet();
                wallet.setUserId(user.getId());
                wallet.setMobileNumber(userJson.get("mobileNumber").textValue());
                wallet.setCircle(userJson.get("circle").textValue());
                wallet.setOperator(userJson.get("operator").textValue());
                wallet.setType(WalletEntryType.MOBILE_RECHARGE);
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
            if (canPayOutUsingCash(user, amount)) {
                Wallet wallet = new Wallet();
                wallet.setUserId(user.getId());
                wallet.setMobileNumber(userJson.get("mobileNumber").textValue());
                wallet.setWalletName(userJson.get("walletName").textValue());
                wallet.setAmount(-convertToWalletAmount(amount));
                wallet.setDescription("Transfer Rs." + amount + " To your" + wallet.getWalletName() + " Wallet");
                wallet.setTransactionDateTime(new Date());
                wallet.setType(WalletEntryType.REDEEM_TO_WALLET);
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
            double amount = userJson.get("amount").doubleValue();
            if (canPayOutUsingCash(user, amount)) {
                Wallet wallet = new Wallet();
                wallet.setUserId(user.getId());
                wallet.setAmount(-convertToWalletAmount(amount));
                wallet.setDescription("Transfer Rs." + amount + " To your Given Bank Account");
                wallet.setTransactionDateTime(new Date());
                wallet.setType(WalletEntryType.REDEEM_TO_BANK);
                wallet.save();
                result = SUCCESS;
            }

        }
        ObjectNode objectNode = Json.newObject();
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }

    private boolean canPayOutUsingCash(User user, double amount) {
        double walletAmount = getWalletAmount(user);
        double cashAmount = getCashTransactionsAmount(user);
        return hasValidAmountInWallet(amount, walletAmount) && hasValidAmountInWallet(amount, cashAmount);
    }

    public Result getBalanceAmount() {
        String result = FAILURE;
        User user = currentUser();
        ObjectNode objectNode = Json.newObject();

        if (user != null) {
            double amount = getWalletAmount(user);
            double cashAmount = getCashTransactionsAmount(user);
            double promoAmount = amount - cashAmount;
            if (amount < cashAmount) {
                cashAmount = amount;
                promoAmount = 0.0;
            }
            objectNode.set("balanceAmount", Json.toJson(amount));
            objectNode.set("freeRidesEarned", Json.toJson(user.getFreeRidesEarned()));
            objectNode.set("freeRidesSpent", Json.toJson(user.getFreeRidesSpent()));
            objectNode.set("cashBalance", Json.toJson(cashAmount));
            objectNode.set("promoBalance", Json.toJson(promoAmount));
            objectNode.set("userBalance", Json.toJson(amount));
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

    public static double getCashTransactionsAmount(User user) {
        List<Wallet> wallets = Wallet.find.where().eq("userId", user.getId()).in("type", WalletEntryType.PAY_U_PAYMENT, WalletEntryType.MOBILE_RECHARGE, WalletEntryType.REDEEM_TO_BANK, WalletEntryType.REDEEM_TO_WALLET).findList();
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

    public Result addBonusPointsToWallet(Long userId, int amount) {
        if (isValidateSession()) {
            processAddBonusPointsToWallet(userId, amount);
        }
        return redirect("/wallet/entries/" + userId);
    }

    public static void processAddBonusPointsToWallet(Long userId, double amount) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setTransactionDateTime(new Date());
        wallet.setAmount(amount);
        wallet.setType(WalletEntryType.BONUS_POINTS);
        wallet.setDescription("Bonus Points from GetBike");
        wallet.save();
    }
}

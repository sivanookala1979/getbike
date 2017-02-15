package controllers;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dataobject.WalletEntryType;
import models.User;
import models.Wallet;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class WalletController extends BaseController {

    public LinkedHashMap<String, String> walletTableHearder = getTableHeadersList(new String[]{"Name", "Requested/Acted " +
                    "Date Time", "Amount", "Description", "Type", "Mobile Number", "Circle", "Wallet Name", "Status", "accept", "reject"},
            new String[]{"name", "requestedActedDate", "amount", "discription", "type", "mobileNumber", "circle", "walletName", "status", "accept", "reject"});
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
                List<Wallet> entries = Wallet.find.where().eq("userId", user.getId()).order("transactionDateTime desc").findList();
                return ok(views.html.walletEntries.render(entries, amount, user));
            }
        }

        return redirect("/users/usersList");
    }

    public Result walletPaginationList(){
        String walletUserId = request().getQueryString("id");
        ObjectNode objectNode = Json.newObject();
        String pageNumber = request().getQueryString("pageNumber");
        List<Wallet> walletsList = Wallet.find.where().eq("user_id" , walletUserId).orderBy("transactionDateTime desc").findPagedList(Integer.parseInt(pageNumber) - 1, 10).getList();
        objectNode.put("size", Wallet.find.where().eq("user_id" , walletUserId).findList().size());

        setResult(objectNode, walletsList);
        return ok(Json.toJson(objectNode));

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

    public Result isAmountPaidStatusAccepted(Long id) {
        Wallet wallet = Wallet.find.byId(id);
        wallet.setIsAmountPaidStatus("Accepted");
        wallet.setStatusActedAt(new Date());
        wallet.update();
        return redirect("/wallet/entries/" + wallet.getUserId());
    }

    public Result isAmountPaidStatusRejected(Long id) {
        Wallet wallet = Wallet.find.byId(id);
        wallet.setIsAmountPaidStatus("Rejected");
        wallet.setStatusActedAt(new Date());
        wallet.update();
        return redirect("/wallet/entries/" + wallet.getUserId());
    }

    public Result dateWiseFilterForRedeem() {
        String startDate = request().getQueryString("startDate");
        String endDate = request().getQueryString("endDate");
        String status = request().getQueryString("status");
        String redeemType = request().getQueryString("redeemType");
        String srcName = request().getQueryString("srcName");
        String walletStatus = request().getQueryString("walletStatus");
        String walletId = request().getQueryString("id");
        if ("Status ALL".equals(status) || "null".equals(status)) {
            status = null;
        }
        if ("Type ALL".equals(redeemType) || "null".equals(redeemType)) {
            redeemType = null;
        }
        List<Wallet> listOfRedeemWallet = new ArrayList<>();
        List<Object> listOfIds = new ArrayList<>();
        ExpressionList<Wallet> rideQuery = null;
        if (isNotNullAndEmpty(srcName)) {
            listOfIds = User.find.where().or(Expr.like("lower(name)", "%" + srcName.toLowerCase() + "%"), Expr.like("lower(phoneNumber)", "%" + srcName.toLowerCase() + "%")).orderBy("id").findIds();
            rideQuery = Wallet.find.where().or(Expr.in("userId", listOfIds), Expr.in("userId", listOfIds));
        } else {
            rideQuery = Wallet.find.where();
        }
        if (isNotNullAndEmpty(status) && isNotNullAndEmpty(redeemType) && isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate)) {
            listOfRedeemWallet = rideQuery.between("transaction_date_time", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).eq("is_amount_paid_status", status).eq("type", redeemType).orderBy("id").findList();
        } else if (isNotNullAndEmpty(status) && isNotNullAndEmpty(redeemType) && !isNotNullAndEmpty(startDate) && !isNotNullAndEmpty(endDate)) {
            listOfRedeemWallet = rideQuery.eq("is_amount_paid_status", status).eq("type", redeemType).findList();
        } else if (!isNotNullAndEmpty(status) && !isNotNullAndEmpty(redeemType) && isNotNullAndEmpty(startDate) && isNotNullAndEmpty(endDate)) {
            listOfRedeemWallet = rideQuery.between("transaction_date_time", DateUtils.getNewDate(startDate, 0, 0, 0), DateUtils.getNewDate(endDate, 23, 59, 59)).orderBy("id").findList();
        } else if (!isNotNullAndEmpty(status) && !isNotNullAndEmpty(redeemType) && !isNotNullAndEmpty(startDate) && !isNotNullAndEmpty(endDate)) {
            listOfRedeemWallet = rideQuery.orderBy("id").findList();
        }
        if (isNotNullAndEmpty(walletId) && isNotNullAndEmpty(walletStatus)) {
            if (walletStatus.equalsIgnoreCase("Reject")) {
                Wallet wallet = Wallet.find.byId(Long.parseLong(walletId));
                wallet.setIsAmountPaidStatus("Rejected");
                Logger.info("Walllet Status Store" + wallet.getIsAmountPaidStatus());
                wallet.setStatusActedAt(new Date());
                wallet.update();
            }
            if (walletStatus.equalsIgnoreCase("Accept")) {
                Wallet wallet = Wallet.find.byId(Long.parseLong(walletId));
                wallet.setIsAmountPaidStatus("Accepted");
                Logger.info("Wallet Status Store" + wallet.getIsAmountPaidStatus());
                wallet.setStatusActedAt(new Date());
                wallet.update();
            }
        }
        ObjectNode objectNode = Json.newObject();
        List<Wallet> list = new ArrayList<>();
        for (Wallet wallet : listOfRedeemWallet) {

            if (!isNotNullAndEmpty(wallet.getIsAmountPaidStatus())) {
                wallet.setIsAmountPaidStatus("Raised");
            }
            if (wallet.getType().equalsIgnoreCase("MobileRecharge") || wallet.getType().equalsIgnoreCase("RedeemToBank") || wallet.getType().equalsIgnoreCase("RedeemToWallet")) {
                list.add(wallet);
            }
        }
        setResult(objectNode, list);
        return ok(Json.toJson(objectNode));
    }

    public Result redeemWalletEntries() {
        for (Wallet wallet : Wallet.find.all()) {
            if (wallet.getIsAmountPaidStatus() == null) {
                wallet.setIsAmountPaidStatus("Raised");
                wallet.update();
                Logger.info("Wallet details " + wallet.getIsAmountPaidStatus());
            }
        }
        return ok(views.html.redeemEventDetails.render(walletTableHearder, "col-sm-12", "", "Wallet", "", "", ""));
    }
}
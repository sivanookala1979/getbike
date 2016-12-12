package controllers;

import com.fasterxml.jackson.databind.JsonNode;
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

    @BodyParser.Of(BodyParser.Json.class)
    public Result addMoney() {
        String result = FAILURE;
        JsonNode userJson = request().body().asJson();
        User user = currentUser();
        if (user != null) {
            Wallet wallet = new Wallet();
            wallet.setUserId(user.getId());
            double amount = userJson.get("amount").doubleValue();
            wallet.setAmount(amount * 10);
            wallet.setTransactionDateTime(new Date());
            wallet.setDescription("Added " + wallet.getAmount() + " points for a recharge of Rs " + amount);
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
            Wallet wallet = new Wallet();
            wallet.setUserId(user.getId());
            wallet.setMobileNumber(userJson.get("mobileNumber").textValue());
            wallet.setCircle(userJson.get("circle").textValue());
            wallet.setOperator(userJson.get("operator").textValue());
            double amount = userJson.get("amount").doubleValue();
            double walletAmount = getWalletAmount(user);
            if(amount<walletAmount) {
                wallet.setAmount(amount * 10);
                wallet.setTransactionDateTime(new Date());
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

    private double getWalletAmount(User user) {
        List<Wallet> wallets = Wallet.find.where().eq("userId", user.getId()).findList();
        double amount = 0;
        for (Wallet wallet : wallets) {
            amount += wallet.getAmount();
        }
        return amount;
    }
}

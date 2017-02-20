package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dataobject.WalletEntryType;
import models.User;
import models.Wallet;
import org.jetbrains.annotations.NotNull;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import views.html.payUFailure;
import views.html.payUSuccess;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by sivanookala on 04/01/17.
 */
public class PaymentController extends BaseController {

    public Result payUSuccess() {
        Map<String, String[]> formUrlEncoded = request().body().asFormUrlEncoded();
        String formAsString = getFormAsString();
        if (formUrlEncoded.get("udf1") != null) {
            User paymentUser = User.find.where().eq("authToken", formUrlEncoded.get("udf1")[0]).findUnique();
            if (paymentUser != null) {
                Wallet wallet = new Wallet();
                String stringAmount = formUrlEncoded.get("amount")[0];
                Double walletAmount = Double.parseDouble(stringAmount);
                wallet.setAmount(WalletController.convertToWalletAmount(walletAmount));
                wallet.setUserId(paymentUser.getId());
                wallet.setType(WalletEntryType.PAY_U_PAYMENT);
                wallet.setDescription("Pay U Payment with details Txn ID : " + formUrlEncoded.get("txnid")[0] + " for Rs. " + stringAmount);
                wallet.setPgDetails(formAsString.length() >= 4000 ? formAsString.substring(0, 4000) : formAsString);
                wallet.setTransactionDateTime(new Date());
                wallet.save();
            }
        }
        return ok(payUSuccess.render(formAsString));
    }

    public Result payUFailure() {
        return ok(payUFailure.render(getFormAsString()));
    }

    @NotNull
    private String getFormAsString() {
        Map<String, String[]> formUrlEncoded = request().body().asFormUrlEncoded();
        String response = "";
        for (String key : formUrlEncoded.keySet()) {
            String values[] = formUrlEncoded.get(key);
            response += key + " : ";
            for (String value : values) {
                response += value + ", ";
            }
            response += "\n";
        }
        System.out.println(response);
        return response;
    }


    public Result paytmCheckSumGenerator() {
        Map<String, String[]> formUrlEncoded = request().body().asFormUrlEncoded();
        ObjectNode objectNode = Json.newObject();
        com.paytm.merchant.CheckSumServiceHelper checkSumServiceHelper = com.paytm.merchant.CheckSumServiceHelper.getCheckSumServiceHelper();
        TreeMap<String, String> parameters = new TreeMap<String, String>();
        String merchantKey = "zxiWpvNgpfS5!rUG";
        parameters.put("MID", "VaveIn61514259730321");
        parameters.put("ORDER_ID", formUrlEncoded.get("ORDER_ID")[0]);
        parameters.put("CUST_ID", formUrlEncoded.get("CUST_ID")[0]);
        parameters.put("TXN_AMOUNT", formUrlEncoded.get("TXN_AMOUNT")[0]);
        parameters.put("CHANNEL_ID", "WAP");
        parameters.put("INDUSTRY_TYPE_ID", "Retail");
        parameters.put("WEBSITE", formUrlEncoded.get("WEBSITE")[0]);
        Logger.info("formUrlEncoded " + formUrlEncoded);
        try {
            String checkSum = checkSumServiceHelper.genrateCheckSum(merchantKey, parameters);
            objectNode.set("CHECKSUMHASH", Json.toJson(checkSum));
            objectNode.set("payt_STATUS", Json.toJson("1"));
            for (String key : parameters.keySet()) {
                objectNode.set(key, Json.toJson(parameters.get(key)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ok(Json.toJson(objectNode));
    }

}

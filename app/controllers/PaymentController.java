package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataobject.WalletEntryType;
import models.User;
import models.Wallet;
import org.jetbrains.annotations.NotNull;
import play.Logger;
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
        TreeMap<String, String> parametersOut = new TreeMap<String, String>();
        com.paytm.merchant.CheckSumServiceHelper checkSumServiceHelper = com.paytm.merchant.CheckSumServiceHelper.getCheckSumServiceHelper();
        TreeMap<String, String> parameters = new TreeMap<String, String>();
        String merchantKey = "zxiWpvNgpfS5!rUG";
        parameters.put("MID", "WorldP64425807474247");
        parameters.put("ORDER_ID", formUrlEncoded.get("ORDER_ID")[0]);
        parameters.put("CUST_ID", formUrlEncoded.get("CUST_ID")[0]);
        parameters.put("TXN_AMOUNT", formUrlEncoded.get("TXN_AMOUNT")[0]);
        parameters.put("CHANNEL_ID", "WAP");
        parameters.put("INDUSTRY_TYPE_ID", "Retail");
        parameters.put("WEBSITE", formUrlEncoded.get("WEBSITE")[0]);
        Logger.info("formUrlEncoded " + formUrlEncoded);
        try {
            //String checkSum = checkSumServiceHelper.genrateCheckSum(merchantKey, parameters);
            parametersOut.put("CHECKSUMHASH", "hrcuaRueHQXhzVxFzpoWedePVyA/rv7Y2Icb3Dte8404xCKx4A1k1yKQjIkbEPldp8kgm+CEuPcjkxiKt/zLgcJDgiSFqPTH8DUIXKD6BvQ=");
            parametersOut.put("payt_STATUS", "1");
            for (String key : parameters.keySet()) {
                parametersOut.put(key, parameters.get(key));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return ok(gson.toJson(parametersOut));
    }

}

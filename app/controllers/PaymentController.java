package controllers;

import dataobject.WalletEntryType;
import models.User;
import models.Wallet;
import org.jetbrains.annotations.NotNull;
import play.mvc.Result;
import views.html.payUFailure;
import views.html.payUSuccess;

import java.util.Date;
import java.util.Map;

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


}

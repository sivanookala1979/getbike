package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paytm.merchant.CheckSumServiceHelper;
import dataobject.WalletEntryType;
import models.PaymentOrder;
import models.Ride;
import models.User;
import models.Wallet;
import org.jetbrains.annotations.NotNull;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import views.html.payUFailure;
import views.html.payUSuccess;

import java.util.*;

/**
 * Created by sivanookala on 04/01/17.
 */
public class PaymentController extends BaseController {

    public static final String MERCHANT_KEY = "zxiWpvNgpfS5!rUG";

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
        Set<String> paramNames = formUrlEncoded.keySet();
        TreeMap<String, String> parameters = new TreeMap<String, String>();
        TreeMap<String, String> parametersOut = new TreeMap<String, String>();
        parameters.put("MID", "");
        parameters.put("ORDER_ID", "");
        parameters.put("INDUSTRY_TYPE_ID", "");
        parameters.put("CHANNEL_ID", "");
        parameters.put("TXN_AMOUNT", "");
        parameters.put("CUST_ID", "");
        parameters.put("WEBSITE", "");
        for (String paramName : paramNames) {
            String paramValue = formUrlEncoded.get(paramName)[0];
            if (paramValue.toLowerCase().contains("refund")) {
                continue;
            }
            parameters.put(paramName, paramValue);
        }
        Logger.info("formUrlEncoded " + formUrlEncoded);
        try {
            String checkSum = CheckSumServiceHelper.getCheckSumServiceHelper().genrateCheckSum(MERCHANT_KEY, parameters);
            parametersOut.put("CHECKSUMHASH", checkSum);
            parametersOut.put("payt_STATUS", "1");
            parametersOut.put("ORDER_ID", formUrlEncoded.get("ORDER_ID")[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return ok(gson.toJson(parametersOut));
    }

    public Result paytmCheckSumVerify() {
        Map<String, String[]> formUrlEncoded = request().body().asFormUrlEncoded();
        Logger.info("formUrlEncoded " + formUrlEncoded);
        String formAsString = getFormAsString();
        Set<String> paramNames = formUrlEncoded.keySet();
        TreeMap<String, String> parameters = new TreeMap<String, String>();
        String paytmChecksum = "";
        for (String paramName : paramNames) {
            if (paramName.equals("CHECKSUMHASH")) {
                paytmChecksum = formUrlEncoded.get(paramName)[0];
            } else {
                String paramValue = formUrlEncoded.get(paramName)[0];
                Logger.info("Paytm Response Param : " + paramName + "=" + paramValue);
                parameters.put(paramName, paramValue);
            }
        }
        boolean isValidChecksum = false;
        try {
            isValidChecksum = CheckSumServiceHelper.getCheckSumServiceHelper().verifycheckSum(MERCHANT_KEY, parameters, paytmChecksum);
            if (isValidChecksum) {
                PaymentOrder paymentOrder = PaymentOrder.find.where().eq("orderIdentifier", parameters.get("ORDERID")).findUnique();
                if (paymentOrder != null) {
                    if ("Wallet".equals(paymentOrder.getOrderType())) {
                        Wallet wallet = new Wallet();
                        String stringAmount = parameters.get("TXN_AMOUNT");
                        Double walletAmount = Double.parseDouble(stringAmount);
                        wallet.setAmount(WalletController.convertToWalletAmount(walletAmount));
                        wallet.setUserId(paymentOrder.getUserId());
                        wallet.setType(WalletEntryType.PAY_U_PAYMENT);
                        wallet.setDescription("Paytm Payment with details Txn ID : " + parameters.get("TXNID") + " for Rs. " + stringAmount);
                        wallet.setPgDetails(formAsString.length() >= 4000 ? formAsString.substring(0, 4000) : formAsString);
                        wallet.setTransactionDateTime(new Date());
                        wallet.save();
                    }
                }
            }
            parameters.put("IS_CHECKSUM_VALID", isValidChecksum == true ? "Y" : "N");
        } catch (Exception e) {
            parameters.put("IS_CHECKSUM_VALID", isValidChecksum == true ? "Y" : "N");
        }

        //
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        StringBuilder outputHtml = new StringBuilder();
        outputHtml.append("<html>");
        outputHtml.append("<head>");
        outputHtml.append("<meta http-equiv='Content-Type' content='text/html;charset=ISO-8859-I'>");
        outputHtml.append("<title>Paytm</title>");
        outputHtml.append("<script type='text/javascript'>");
        outputHtml.append("function response(){");
        outputHtml.append("return document.getElementById('response').value;");
        outputHtml.append("}");
        outputHtml.append("</script>");
        outputHtml.append("</head>");
        outputHtml.append("<body>");
        outputHtml.append("Redirect back to the app<br>");
        outputHtml.append("<form name='frm' method='post'>");
        outputHtml.append("<input type='hidden' id='response' name='responseField' value='" + gson.toJson(parameters) + "' />");
        outputHtml.append("</form>");
        outputHtml.append("</body>");
        outputHtml.append("</html>");
        return ok(outputHtml.toString()).as("text/html");
    }


    public Result generateOrderId() {
        User user = currentUser();
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        if (user != null) {
            PaymentOrder paymentOrder = new PaymentOrder();
            paymentOrder.setOrderIdentifier(UUID.randomUUID().toString());
            paymentOrder.setUserId(user.getId());
            paymentOrder.setOrderDateTime(new Date());
            paymentOrder.setOrderType(getString("type"));
            if ("Wallet".equals(paymentOrder.getOrderType())) {
                paymentOrder.setAmount(getDouble("amount"));
            }
            if ("Ride".equals(paymentOrder.getOrderType())) {
                Ride ride = Ride.find.byId(getLong("rideId"));
                if (ride != null) {
                    paymentOrder.setAmount(ride.getTotalBill());
                    paymentOrder.setRideId(ride.getId());
                }
            }
            if (paymentOrder.getAmount() != null && paymentOrder.getAmount() > 0.0) {
                paymentOrder.setStatus("Requested");
                paymentOrder.save();
                result = SUCCESS;
                setJson(objectNode, "orderIdentifier", paymentOrder.getOrderIdentifier());
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

}

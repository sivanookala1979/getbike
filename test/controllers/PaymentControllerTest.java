package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.PaymentOrder;
import models.Ride;
import models.User;
import org.junit.Test;
import play.mvc.Result;

import java.util.TreeMap;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;


public class PaymentControllerTest extends BaseControllerTest {

    public void paytmCheckSumGeneratorTESTHappyFlow() {
        TreeMap<String, String> formParams = new TreeMap<>();
        formParams.put("INDUSTRY_TYPE_ID", "Retail");
        Result result = route(fakeRequest(POST, "/paytmCheckSumGenerator").bodyForm(formParams));
        JsonNode jsonFromResult = jsonFromResult(result);
        System.out.println(jsonFromResult);
        assertTrue(jsonFromResult.has("CHECKSUMHASH"));
        System.out.println(jsonFromResult.get("CHECKSUMHASH").asText());
    }

    @Test
    public void generateOrderIdTESTForWallet() {
        User user = loggedInUser();
        Result actual = route(fakeRequest(GET, "/generateOrderId?type=Wallet&amount=20.0").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        String orderIdentifier = responseObject.get("orderIdentifier").textValue();
        PaymentOrder paymentOrder = PaymentOrder.find.where().eq("orderIdentifier", orderIdentifier).findUnique();
        assertNotNull(paymentOrder);
        assertEquals(user.getId(), paymentOrder.getUserId());
        assertEquals("Wallet", paymentOrder.getOrderType());
        assertEquals(20.0, paymentOrder.getAmount());
        assertNotNull(paymentOrder.getOrderDateTime());
        assertEquals("Requested", paymentOrder.getStatus());
    }

    @Test
    public void generateOrderIdTESTForRide() {
        User user = loggedInUser();
        Ride ride = createRide(user.getId());
        ride.setTotalBill(10.0);
        ride.save();
        Result actual = route(fakeRequest(GET, "/generateOrderId?type=Ride&rideId=" + ride.getId()).header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        String orderIdentifier = responseObject.get("orderIdentifier").textValue();
        PaymentOrder paymentOrder = PaymentOrder.find.where().eq("orderIdentifier", orderIdentifier).findUnique();
        assertNotNull(paymentOrder);
        assertEquals(user.getId(), paymentOrder.getUserId());
        assertEquals("Ride", paymentOrder.getOrderType());
        assertEquals(ride.getTotalBill(), paymentOrder.getAmount());
        assertEquals(ride.getId(), paymentOrder.getRideId());
        assertNotNull(paymentOrder.getOrderDateTime());
        assertEquals("Requested", paymentOrder.getStatus());
    }

    @Test
    public void testStatusTESTWithFailedTransaction() throws Exception {

        String mid = "VavInf16301400717586";
        String orderId = "d6c8b054-9da6-4f1a-88a7-14bcc9c24f91";
        double amount = 100.0;

        boolean result = new PaymentController().isValidStatus(mid, orderId, amount);
        System.out.println(result);
        assertFalse(result);
    }


    @Test
    public void testStatusTESTHappyFlow() throws Exception {
        String mid = "VavInf16301400717586";
        String orderId = "4ca30d33-b135-4a38-a5a1-284996d347a5";
        double amount = 20.0;

        boolean result = new PaymentController().isValidStatus(mid, orderId, amount);
        System.out.println(result);
        assertTrue(result);
    }
}
package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import play.mvc.Result;

import java.util.TreeMap;

import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;


public class PaymentControllerTest extends BaseControllerTest {

    @Test
    public void paytmCheckSumGeneratorTESTHappyFlow() {
        TreeMap<String, String> formParams = new TreeMap<>();
        formParams.put("INDUSTRY_TYPE_ID", "Retail");
        Result result = route(fakeRequest(POST, "/paytmCheckSumGenerator").bodyForm(formParams));
        JsonNode jsonFromResult = jsonFromResult(result);
        System.out.println(jsonFromResult);
        assertTrue(jsonFromResult.has("CHECKSUMHASH"));
        System.out.println(jsonFromResult.get("CHECKSUMHASH").asText());
    }
}
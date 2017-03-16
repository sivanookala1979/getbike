package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.PromotionsBanner;
import models.User;
import org.junit.Test;
import play.mvc.Result;

import static junit.framework.TestCase.assertEquals;
import static play.test.Helpers.GET;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

/**
 * Created by ramkoti on 16/3/17.
 */
public class PromotionalBannerControllerTest extends BaseControllerTest {

    @Test
    public void sendPromotionalBannerWithUrlTestFlowForHdpiImage() {
        User user = loggedInUser();
        PromotionsBanner promotionsBanner = new PromotionsBanner();
        promotionsBanner.setHdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0900.jpg");
        promotionsBanner.setLdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0901.jpg");
        promotionsBanner.setMdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0902.jpg");
        promotionsBanner.setXhdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb903.jpg");
        promotionsBanner.setXxhdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8fe905.jpg");
        promotionsBanner.setPromotionsURL("getbike.co.in");
        promotionsBanner.setShowThisBanner(true);
        promotionsBanner.save();
        Result actual = route(fakeRequest(GET, "/promotion/sendPromotion?resolution=hdpi").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        assertEquals("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0900.jpg",responseObject.get("hdpi").textValue());
        assertEquals("getbike.co.in",responseObject.get("promotionsURL").textValue());
        promotionsBanner.setShowThisBanner(false);
        promotionsBanner.update();
    }

    @Test
    public void sendPromotionalBannerWithUrlTestFlowForLdpiImage() {
        User user = loggedInUser();
        PromotionsBanner promotionsBanner = new PromotionsBanner();
        promotionsBanner.setHdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0906.jpg");
        promotionsBanner.setLdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0907.jpg");
        promotionsBanner.setMdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0908.jpg");
        promotionsBanner.setXhdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb909.jpg");
        promotionsBanner.setXxhdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8fe995.jpg");
        promotionsBanner.setPromotionsURL("google.co.in");
        promotionsBanner.setShowThisBanner(true);
        promotionsBanner.save();
        Result actual = route(fakeRequest(GET, "/promotion/sendPromotion?resolution=ldpi").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        assertEquals("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0907.jpg",responseObject.get("ldpi").textValue());
        assertEquals("google.co.in",responseObject.get("promotionsURL").textValue());
        promotionsBanner.setShowThisBanner(false);
        promotionsBanner.update();
    }

    @Test
    public void sendPromotionalBannerWithUrlTestFlowForMdpiImage() {
        User user = loggedInUser();
        PromotionsBanner promotionsBanner = new PromotionsBanner();
        promotionsBanner.setHdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0910.jpg");
        promotionsBanner.setLdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0921.jpg");
        promotionsBanner.setMdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0932.jpg");
        promotionsBanner.setXhdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb943.jpg");
        promotionsBanner.setXxhdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8fe955.jpg");
        promotionsBanner.setPromotionsURL("redbike.co.in");
        promotionsBanner.setShowThisBanner(true);
        promotionsBanner.save();
        Result actual = route(fakeRequest(GET, "/promotion/sendPromotion?resolution=mdpi").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        assertEquals("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0932.jpg",responseObject.get("mdpi").textValue());
        assertEquals("redbike.co.in",responseObject.get("promotionsURL").textValue());
        promotionsBanner.setShowThisBanner(false);
        promotionsBanner.update();
    }

    @Test
    public void sendPromotionalBannerWithUrlTestFlowForXhdpiImage() {
        User user = loggedInUser();
        PromotionsBanner promotionsBanner = new PromotionsBanner();
        promotionsBanner.setHdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0110.jpg");
        promotionsBanner.setLdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0121.jpg");
        promotionsBanner.setMdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0132.jpg");
        promotionsBanner.setXhdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb143.jpg");
        promotionsBanner.setXxhdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8fe155.jpg");
        promotionsBanner.setPromotionsURL("bike.co.in");
        promotionsBanner.setShowThisBanner(true);
        promotionsBanner.save();
        Result actual = route(fakeRequest(GET, "/promotion/sendPromotion?resolution=xhdpi").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        assertEquals("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb143.jpg",responseObject.get("xhdpi").textValue());
        assertEquals("bike.co.in",responseObject.get("promotionsURL").textValue());
        promotionsBanner.setShowThisBanner(false);
        promotionsBanner.update();
    }

    @Test
    public void sendPromotionalBannerWithUrlTestFlowForXxhdpiImage() {
        User user = loggedInUser();
        PromotionsBanner promotionsBanner = new PromotionsBanner();
        promotionsBanner.setHdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0115.jpg");
        promotionsBanner.setLdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0126.jpg");
        promotionsBanner.setMdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb0137.jpg");
        promotionsBanner.setXhdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8feb148.jpg");
        promotionsBanner.setXxhdpiPromotionalBanner("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8fe159.jpg");
        promotionsBanner.setPromotionsURL("lap.co.in");
        promotionsBanner.setShowThisBanner(true);
        promotionsBanner.save();
        Result actual = route(fakeRequest(GET, "/promotion/sendPromotion?resolution=xxhdpi").header("Authorization", user.getAuthToken()));
        JsonNode responseObject = jsonFromResult(actual);
        assertEquals("success", responseObject.get("result").textValue());
        assertEquals("getbike.co.in-febbe4b4-5bab-41d2-bc75-d8c14988d25dd9e7e0c0032e27e05d6e39de8fe159.jpg",responseObject.get("xxhdpi").textValue());
        assertEquals("lap.co.in",responseObject.get("promotionsURL").textValue());
        promotionsBanner.setShowThisBanner(false);
        promotionsBanner.update();
    }
}

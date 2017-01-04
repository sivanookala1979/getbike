package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.BodyParser;
import play.mvc.Result;
import views.html.payUSuccess;
import views.html.payUFailure;

import static play.mvc.Controller.request;

/**
 * Created by sivanookala on 04/01/17.
 */
public class PaymentController extends BaseController{
    public Result payUSuccess() {
        logRequest();
        JsonNode locationsJson = request().body().asJson();
        return ok(payUSuccess.render(locationsJson.toString()));
    }

    public Result payUFailure() {
        logRequest();
        JsonNode locationsJson = request().body().asJson();
        return ok(payUFailure.render(locationsJson.toString()));
    }

    private void logRequest() {
        System.out.println(request().headers());
        System.out.println(request().headers().get("Content-Type")[0]);
        System.out.println(request().body().asText());
    }
}

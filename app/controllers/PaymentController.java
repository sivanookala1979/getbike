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
    @BodyParser.Of(BodyParser.Json.class)
    public Result payUSuccess() {
        JsonNode locationsJson = request().body().asJson();
        return ok(payUSuccess.render(locationsJson.toString()));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result payUFailure() {
        JsonNode locationsJson = request().body().asJson();
        return ok(payUFailure.render(locationsJson.toString()));
    }
}

package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import play.mvc.BodyParser;
import play.mvc.Result;
import views.html.payUSuccess;
import views.html.payUFailure;

import java.util.Map;

import static play.mvc.Controller.request;

/**
 * Created by sivanookala on 04/01/17.
 */
public class PaymentController extends BaseController{
    public Result payUSuccess() {
        logRequest();
        return ok(payUSuccess.render(getFormAsString()));
    }

    @NotNull
    private String getFormAsString() {
        Map<String, String[]> formUrlEncoded = request().body().asFormUrlEncoded();
        String response = "";
        for(String key : formUrlEncoded.keySet())
        {
            String values[] = formUrlEncoded.get(key);
            response += key + " : ";
            for(String value : values)
            {
                response += value +", ";
            }
            response += "\n";
        }
        return response;
    }

    public Result payUFailure() {
        logRequest();
        return ok(payUFailure.render(getFormAsString()));
    }

    private void logRequest() {
        System.out.println(request().headers());
        System.out.println(request().headers().get("Content-Type")[0]);
        System.out.println(request().body().asText());
    }
}

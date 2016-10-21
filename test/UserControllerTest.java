import com.fasterxml.jackson.databind.JsonNode;
import controllers.HomeController;
import controllers.UserController;
import models.User;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;


public class UserControllerTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .configure("play.http.router", "router.Routes")
                .build();
    }

    @Test
    public void signupTESTHappyFlow() {
        User user = new User();
        user.setGender('M');
        user.setName("Siva Nookala");
        user.setEmail("siva.nookala@gmail.com");
        user.setPhoneNumber("9949287789");
        Result result = route(fakeRequest(POST, "/signup").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        cAssertUser(user, result);
    }
    @Test
    public void signupTESTUser2() {
        User user = new User();
        user.setGender('M');
        user.setName("Shravya M");
        user.setEmail("shravya@vave.co.in");
        user.setPhoneNumber("8282828282");
        Result result = route(fakeRequest(POST, "/signup").bodyJson(Json.toJson(user))).withHeader("Content-Type", "application/json");
        cAssertUser(user, result);
    }

    private void cAssertUser(User user, Result result) {
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType().get());
        assertEquals("UTF-8", result.charset().get());
        String resultString = contentAsString(result);
        JsonNode jsonNode = Json.parse(resultString);
        assertTrue(jsonNode.has("email"));
        assertEquals(user.getName(), jsonNode.get("name").textValue());
        assertEquals(user.getEmail(), jsonNode.get("email").textValue());
        assertEquals(user.getPhoneNumber(), jsonNode.get("phoneNumber").textValue());
        User actual = User.find.byId(jsonNode.get("id").asLong());
        assertNotNull(actual);
        assertEquals(user.getGender(), actual.getGender());
    }

}
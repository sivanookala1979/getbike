package controllers;

import models.User;
import models.UserLogin;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.login;

import javax.inject.Inject;
import java.util.LinkedHashMap;

/**
 * Created by Siva Sudarsi on 1/12/16.
 */
public class LoginController extends BaseController {

    @Inject
    FormFactory formFactory;
    public LinkedHashMap<String, String> userTableHeaders = getTableHeadersList(new String[]{"", "", "#", "Name", "Phone Number", "Auth.Token", "Gcm Code"}, new String[]{"", "", "id", "name", "phoneNumber", "authToken", "gcmCode"});

    public Result login() {
        Form<UserLogin> logInForm = formFactory.form(UserLogin.class).bindFromRequest();
        return ok(views.html.login.render(logInForm));
    }

    public Result loginUserDetails() {
        Form<UserLogin> userForm = formFactory.form(UserLogin.class).bindFromRequest();
        UserLogin user = userForm.get();
        int rowCount = User.find.where().eq("email", user.getUsername()).eq("password", user.getPassword()).findRowCount();
        if (rowCount != 0) {
            session("User", user.getUsername());
            return ok(views.html.usersList.render(userTableHeaders));
        }
        flash("error", "Invalid Username/Password !");
        return badRequest(views.html.login.render(userForm));
    }

    public Result logout() {
        session().clear();
        Form<UserLogin> logInForm = formFactory.form(UserLogin.class).bindFromRequest();
        return ok(login.render(logInForm));
    }

}

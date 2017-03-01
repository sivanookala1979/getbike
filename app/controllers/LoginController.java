package controllers;

import models.User;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.login;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Siva Sudarsi on 1/12/16.
 */
public class LoginController extends BaseController {

    @Inject
    FormFactory formFactory;
    public LinkedHashMap<String, String> userTableHeaders = getTableHeadersList(new String[]{"", "", "#", "Name", "Phone Number", "Auth.Token", "Gcm Code"}, new String[]{"", "", "id", "name", "phoneNumber", "authToken", "gcmCode"});

    public Result login() {
        int rowCount = User.find.where().eq("email", "admin_getbike").findRowCount();
        if (rowCount == 0) {
            User user = new User();
            user.setEmail("admin_getbike");
            user.setPassword("cerone");
            user.setRole("Admin");
            user.save();
        }
        Form<User> logInForm = formFactory.form(User.class).bindFromRequest();
        return ok(views.html.login.render(logInForm));
    }

    public Result loginUserDetails() {
        Form<User> userForm = formFactory.form(User.class).bindFromRequest();
        User user = userForm.get();
        int rowCount = User.find.where().eq("email", user.getEmail()).eq("password", user.getPassword()).findRowCount();
        if (rowCount != 0) {
            User uniqueUser = User.find.where().eq("email", user.getEmail()).eq("password", user.getPassword()).findUnique();
            if (uniqueUser.getRole() != null) {
                session("admin", uniqueUser.getRole());
            }
            session("User", user.getEmail());
            return redirect("/home");
        } else {
            flash("error", "Invalid Username/Password !");
            return badRequest(views.html.login.render(userForm));
        }
    }

    public Result logout() {
        session().clear();
        Form<User> logInForm = formFactory.form(User.class).bindFromRequest();
        return ok(login.render(logInForm));
    }

    public Result createNewUser() {
        if (isValidateAdmin()) {
            Form<User> userForm = formFactory.form(User.class).bindFromRequest();
            return ok(views.html.usermaintenance.render(userForm));
        }
        return redirect("/");
    }

    public Result changePassword() {
        if (isValidateAdmin()) {
            Form<User> userForm = formFactory.form(User.class).bindFromRequest();
            int count = 0;
            List<User> allUsers = User.find.all();
            List<User> listOfUsers = new ArrayList<>();
            for (User user : allUsers) {
                if ((user.getEmail() != null && user.getRole() != null)) {
                    listOfUsers.add(user);
                }
            }
            return ok(views.html.changepassword.render(userForm, listOfUsers));
        }
        return redirect("/");
    }

    public Result updateUserDetails() {
        Form<User> userForm = formFactory.form(User.class).bindFromRequest();
        User user = userForm.get();
        User updateUser = User.find.where().eq("email", user.getEmail()).findUnique();
        updateUser.setPassword(user.getPassword());
        updateUser.setRole(user.getRole());
        updateUser.update();
        flash("error", "User Details updated successfully");
        return redirect("/user/changepassword");
    }

    public Result createNewLoginDetails() {
        Form<User> logInForm = formFactory.form(User.class).bindFromRequest();
        User user = logInForm.get();
        int rowCount = User.find.where().eq("email", user.getEmail()).findRowCount();
        Logger.info("Row count  " + rowCount);
        if (rowCount == 0) {
            user.save();
            flash("error", "User Details successfully saved!");
            return ok(views.html.usermaintenance.render(logInForm));
        } else {
            flash("error", "User name already exist!");
            return redirect("/user/userMaintenance");
        }
    }
}

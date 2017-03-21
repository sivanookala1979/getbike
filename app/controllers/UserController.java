package controllers;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import utils.*;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.util.*;

import static utils.CustomCollectionUtils.first;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class UserController extends BaseController {

    public static final double JOINING_BONUS = 1000.0;
    @Inject
    FormFactory formFactory;

    public LinkedHashMap<String, String> loginOtpTableHeaders = getTableHeadersList(new String[]{"", "", "#", "User Id", "Mobile Number", "OTP", "Created At"}, new String[]{"", "", "id", "userId", "phoneNumber", "generatedOtp", "createdAt"});
    public LinkedHashMap<String, String> signUpPromoCodeTableHeaders = getTableHeadersList(new String[]{"Id", "Promo Code Used", "Mobile Number", "Name"}, new String[]{"id", "signupPromoCode", "phoneNumber", "name"});

    public Result index() {
        return ok(views.html.userIndex.render(User.find.all(), Ride.find.all(), RideLocation.find.all(), LoginOtp.find.all()));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result signup() {
        int errorCode = 0;
        JsonNode userJson = request().body().asJson();
        User user = Json.fromJson(userJson, User.class);
        int previousUserCount = User.find.where().eq("phoneNumber", user.getPhoneNumber()).findRowCount();
        boolean validPromoCode = true;
        User referrer = null;
        if (StringUtils.isNotNullAndEmpty(user.getSignupPromoCode())) {
            referrer = User.find.where().eq("promoCode", user.getSignupPromoCode()).findUnique();
            if (referrer == null) validPromoCode = false;
        }
        if (!validPromoCode) {
            errorCode = GetBikeErrorCodes.INVALID_PROMO_CODE;
        } else if (previousUserCount == 0) {
            user.setFreeRidesEarned(0);
            user.save();
            WalletController.processAddBonusPointsToWallet(user.getId(), JOINING_BONUS);
            if (referrer != null) {
                user.setFreeRidesEarned(1);
                user.save();
            }
            return ok(Json.toJson(user));
        } else {
            User previousUser = User.find.where().eq("phoneNumber", user.getPhoneNumber()).findUnique();
            if (previousUser != null) {
                if (StringUtils.isNullOrEmpty(previousUser.getName())) {
                    previousUser.setName(user.getName());
                    previousUser.setEmail(user.getEmail());
                    previousUser.setGender(user.getGender());
                    previousUser.save();
                }
            }
            errorCode = GetBikeErrorCodes.USER_ALREADY_EXISTS;
        }
        ObjectNode objectNode = Json.newObject();
        objectNode.set("errorCode", Json.toJson(errorCode));
        objectNode.set("result", Json.toJson("failure"));
        return ok(Json.toJson(objectNode));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
        JsonNode userJson = request().body().asJson();
        User user = Json.fromJson(userJson, User.class);
        User actual = User.find.where().eq("phoneNumber", user.getPhoneNumber()).findUnique();
        String result = "failure";
        if (actual != null) {
            LoginOtp loginOtp = new LoginOtp();
            loginOtp.setUserId(actual.getId());
            loginOtp.setGeneratedOtp(NumericUtils.generateOtp());
            loginOtp.setCreatedAt(new Date());
            loginOtp.save();
            String generatedOtp = loginOtp.getGeneratedOtp();
            String phoneNumber = user.getPhoneNumber();
            sendOtpToPhoneNumber(generatedOtp, phoneNumber);

            result = "success";
        }
        ObjectNode objectNode = Json.newObject();
        objectNode.set("errorCode", Json.toJson(GetBikeErrorCodes.INVALID_USER));
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));
    }


    @BodyParser.Of(BodyParser.Json.class)
    public Result loginWithOtp() {
        JsonNode userJson = request().body().asJson();
        ObjectNode objectNode = Json.newObject();
        User actual = User.find.where().eq("phoneNumber", userJson.get("phoneNumber").textValue()).findUnique();
        String result = "failure";
        if (actual != null) {
            LoginOtp loginOtp = first(LoginOtp.find.where().eq("userId", actual.getId()).order("createdAt desc").findList());
            if (loginOtp != null && loginOtp.getGeneratedOtp().equals(userJson.get("otp").textValue())) {
                result = "success";
                actual.setAuthToken(UUID.randomUUID().toString());
                actual.save();
                objectNode.set("authToken", Json.toJson(actual.getAuthToken()));
            }
        }
        objectNode.set("result", Json.toJson(result));
        return ok(Json.toJson(objectNode));

    }

    public Result storeGcmCode() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            String gcmCode = getString("gcmCode");
            user.setGcmCode(gcmCode);
            user.save();
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result storeLastKnownLocation() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            JsonNode userJson = request().body().asJson();
            User userLocation = Json.fromJson(userJson, User.class);
            if (userLocation.getLastKnownLatitude() != null && userLocation.getLastKnownLongitude() != null) {
                user.setLastKnownLatitude(userLocation.getLastKnownLatitude());
                user.setLastKnownLongitude(userLocation.getLastKnownLongitude());
                user.setLastLocationTime(userLocation.getLastLocationTime());
                user.save();
                RiderPosition riderPosition = new RiderPosition();
                riderPosition.setUserId(user.getId());
                riderPosition.setLastKnownLatitude(user.getLastKnownLatitude());
                riderPosition.setLastKnownLongitude(user.getLastKnownLongitude());
                riderPosition.setLastLocationTime(user.getLastLocationTime());
                riderPosition.save();
                if (user.isRideInProgress()) {
                    Ride currentRide = Ride.find.byId(user.getCurrentRideId());
                    RideController.sendLocationToRequestor(user, currentRide);
                }
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result getPublicProfile(Long userId) {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            User requestedUser = user;
            if (userId != null && userId > 0) {
                requestedUser = User.find.byId(userId);
            }
            if (requestedUser != null) {
                ensurePromoCode(requestedUser);
                User publicUser = new User();
                publicUser.setName(requestedUser.getName());
                publicUser.setPhoneNumber(requestedUser.getPhoneNumber());
                publicUser.setVehiclePlateImageName(requestedUser.getVehiclePlateImageName());
                publicUser.setVehicleNumber(requestedUser.getVehicleNumber());
                publicUser.setDrivingLicenseImageName(requestedUser.getDrivingLicenseImageName());
                publicUser.setDrivingLicenseNumber(requestedUser.getDrivingLicenseNumber());
                publicUser.setPromoCode(requestedUser.getPromoCode());
                objectNode.set("profile", Json.toJson(publicUser));
            }
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public static void ensurePromoCode(User requestedUser) {
        if (requestedUser.getPromoCode() == null) {
            if (requestedUser.getName() != null && !requestedUser.getName().trim().isEmpty()) {
                requestedUser.setPromoCode(requestedUser.getName().split(" ")[0].toLowerCase() + NumericUtils.generateOtp());
            } else {
                requestedUser.setPromoCode("getbike" + NumericUtils.generateOtp());
            }
            requestedUser.save();
        }
    }

    public Result getPrivateProfile() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            objectNode.set("privateProfile", Json.toJson(user));
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result getCurrentRide() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            if (user.isRideInProgress() && user.getCurrentRideId() != null && user.getCurrentRideId() > 0) {
                objectNode.set("rideId", Json.toJson(user.getCurrentRideId()));
                result = SUCCESS;
            }
            if (user.isRequestInProgress() && user.getCurrentRequestRideId() != null && user.getCurrentRequestRideId() > 0) {
                new RideController().cancelIfExpired(user, "SUCCESS");
                if (user.isRequestInProgress()) {
                    objectNode.set("requestId", Json.toJson(user.getCurrentRequestRideId()));
                    result = SUCCESS;
                }
            }
            String appVersion = getString("version");
            if (StringUtils.isNotNullAndEmpty(appVersion)) {
                user.setAppVersion(appVersion);
                user.save();
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updatePrivateProfile() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            JsonNode userJson = request().body().asJson();
            User privateProfileUser = Json.fromJson(userJson.get("user"), User.class);
            user.setName(privateProfileUser.getName());
            user.setCity(privateProfileUser.getCity());
            user.setOccupation(privateProfileUser.getOccupation());
            user.setEmail(privateProfileUser.getEmail());
            user.setYearOfBirth(privateProfileUser.getYearOfBirth());
            user.setHomeLocation(privateProfileUser.getHomeLocation());
            user.setOfficeLocation(privateProfileUser.getOfficeLocation());
            String encodedImageData = userJson.get("imageData").textValue();
            byte[] decoded = Base64.getDecoder().decode(encodedImageData);
            try {
                String imagePath = "uploads/" + user.getId() + "-profile-" + UUID.randomUUID() + ".png";
                FileOutputStream fileOutputStream = new FileOutputStream("public/" + imagePath);
                fileOutputStream.write(decoded);
                fileOutputStream.close();
                user.setProfileImage(imagePath);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            user.save();
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }


    public Result storeDrivingLicense() {
        JsonNode userJson = request().body().asJson();
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            String drivingLicenseNumber = userJson.get("drivingLicenseNumber").textValue();
            user.setDrivingLicenseNumber(drivingLicenseNumber);
            String encodedImageData = userJson.get("imageData").textValue();
            byte[] decoded = Base64.getDecoder().decode(encodedImageData);
            try {
                String imagePath = "uploads/" + user.getId() + "-dl-" + UUID.randomUUID() + ".png";
                FileOutputStream fileOutputStream = new FileOutputStream("public/" + imagePath);
                fileOutputStream.write(decoded);
                fileOutputStream.close();
                user.setDrivingLicenseImageName(imagePath);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            user.setValidProofsUploaded(false);
            SMSHelper.sendSms("54037",user.getPhoneNumber(),"&");
            user.save();
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));

    }

    public Result storeVehiclePlate() {
        JsonNode userJson = request().body().asJson();
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null) {
            String vehiclePlateNumber = userJson.get("vehiclePlateNumber").textValue();
            user.setVehicleNumber(vehiclePlateNumber);
            String encodedImageData = userJson.get("imageData").textValue();
            byte[] decoded = Base64.getDecoder().decode(encodedImageData);
            try {
                String imagePath = "uploads/" + user.getId() + "-vp-" + UUID.randomUUID() + ".png";
                FileOutputStream fileOutputStream = new FileOutputStream("public/" + imagePath);
                fileOutputStream.write(decoded);
                fileOutputStream.close();
                user.setVehiclePlateImageName(imagePath);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            user.setValidProofsUploaded(false);
            SMSHelper.sendSms("54037",user.getPhoneNumber(),"&");
            user.save();
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    private void sendOtpToPhoneNumber(String generatedOtp, String phoneNumber) {
        SMSHelper.sendSms("53733", phoneNumber, "&F1=" + generatedOtp);
    }


    public Result loginOtpList() {
        for (LoginOtp loginOtp : LoginOtp.find.all()) {
            loginOtp.setCreatedAt(loginOtp.getCreatedAt());
        }
        setUserPhnumberToOtpList();
        return ok(views.html.loginOtpList.render(loginOtpTableHeaders));
    }

    private void setUserPhnumberToOtpList() {
        for (LoginOtp loginOtp : LoginOtp.find.all()) {
            loginOtp.setPhoneNumber(User.find.where().eq("id", loginOtp.getUserId()).findUnique().phoneNumber);
            loginOtp.update();
        }
    }

    public Result userApproveAccept(Long id) {
        User user = User.find.where().eq("id", id).findUnique();
        String drivingLicenseImageName = user.getDrivingLicenseImageName();
        String vehiclePlateImageName = user.getVehiclePlateImageName();
        Logger.info("Driver lience " + drivingLicenseImageName + "      " + vehiclePlateImageName);
        if (drivingLicenseImageName == null || vehiclePlateImageName == null) {
            flash("error", "No images are upload");
            return redirect("/users/usersList");
        }
        return ok(views.html.viewuploads.render(drivingLicenseImageName, vehiclePlateImageName, user.isValidProofsUploaded(), user.id));
    }

    public Result updateUserProofValidationApprove(Long id, Boolean isValidProofs) {
        Logger.info("Boolean is " + isValidProofs);
        User user = User.find.where().eq("id", id).findUnique();
        if (isValidProofs){
            IGcmUtils gcmUtils = ApplicationContext.defaultContext().getGcmUtils();
            gcmUtils.sendMessage(user, "Your profile is declined. Please resubmit the proofs.", "getbike", null);
            Logger.debug("Rejected .......................");
            SMSHelper.sendSms("54039",user.getPhoneNumber(),"&");
        }
        else {
            IGcmUtils gcmUtils = ApplicationContext.defaultContext().getGcmUtils();
            gcmUtils.sendMessage(user, "Your profile is successfully updated.", "getbike", null);
            Logger.debug("Approve..........................");
            SMSHelper.sendSms("54038",user.getPhoneNumber(),"&");
        }
        user.setValidProofsUploaded(!isValidProofs);
        user.update();
        return redirect("/users/usersList");
    }

    public Result usersList() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        List<String> headers = Arrays.asList("#", "Name", "Phone Number", "Gender", "License Number", "Validate Uploaded Profile", "Wallet", "Special Price");
        List<User> allUsers = User.find.all();
        return ok(views.html.usersList.render(headers, allUsers));
    }

    public Result clearCurrentRide(Long id) {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        User user = User.find.where().eq("id", id).findUnique();
        user.setRideInProgress(false);
        user.setCurrentRideId(null);
        user.update();
        return redirect("/users/usersList");
    }

    public Result performSearch(String name) {
        List<User> userList = null;
        if (name != null && !name.isEmpty()) {
            userList = User.find.where().like("upper(name)", "%" + name.toUpperCase() + "%").findList();
        } else {
            userList = User.find.all();
        }
        return ok(Json.toJson(userList));
    }

    public Result loginOtpSearch() {
        String mobileNumber = request().getQueryString("input");
        String pageNumber = request().getQueryString("pageNumber");
        int pgNumber = Integer.parseInt(pageNumber);
        ObjectNode objectNode = Json.newObject();
        List<LoginOtp> userList = null;
        if (mobileNumber != null && !mobileNumber.isEmpty() && Character.isDigit(mobileNumber.charAt(0))) {
            userList = LoginOtp.find.where().like("phoneNumber", "%" + mobileNumber + "%").orderBy("createdAt desc").findPagedList(pgNumber - 1, 10).getList();
            objectNode.put("size", LoginOtp.find.where().like("phoneNumber", "%" + mobileNumber + "%").findList().size());
        } else {
            userList = LoginOtp.find.where().orderBy("createdAt desc").findPagedList(Integer.parseInt(pageNumber) - 1, 10).getList();
            objectNode.put("size", LoginOtp.find.all().size());
        }
        setResult(objectNode, userList);
        return ok(Json.toJson(objectNode));
    }

    public Result usersListSearch() {
        List<User> userList = null;
        ObjectNode objectNode = Json.newObject();
        String searchInput = request().getQueryString("input");
        String pageNumber = request().getQueryString("pageNumber");
        if (searchInput != null && !searchInput.isEmpty() && searchInput.length() > 0) {
            userList = User.find.where().or(Expr.like("lower(name)", "%" + searchInput.toLowerCase() + "%"),
                    Expr.like("lower(phoneNumber)", "%" + searchInput.toLowerCase() + "%")).order("id").findPagedList(Integer.parseInt(pageNumber) - 1, 10).getList();
            objectNode.put("size", User.find.where().or(Expr.like("lower(name)", "%" + searchInput.toLowerCase() + "%"),
                    Expr.like("lower(phoneNumber)", "%" + searchInput.toLowerCase() + "%")).findList().size());
        } else {
            userList = User.find.where().orderBy("id").findPagedList(Integer.parseInt(pageNumber) - 1, 10).getList();
            objectNode.put("size", User.find.all().size());
        }
        setResult(objectNode, userList);
        return ok(Json.toJson(objectNode));
    }

    public Result performSearch1(String name) {
        List<LoginOtp> loginOtpList = LoginOtp.find.all();
        return ok(Json.toJson(loginOtpList));
    }

    public Result userSpecialPrice(Long id) {
        User user = User.find.byId(id);
        return ok(views.html.specialprice.render(user));
    }

    public Result updateUserDetailsWithSpecialPrice() {
        DynamicForm requestData = formFactory.form().bindFromRequest();
        String userId = requestData.get("userId");
        String name = requestData.get("name");
        String spePrice = requestData.get("spePrice");
        User user = User.find.byId(Long.valueOf(userId));
        user.setSpePrice(Double.valueOf(spePrice));
        user.setSpecialPrice(true);
        user.update();
        return redirect("/users/usersList");
    }

    public Result editUserDetails(Long id) {
        User user = User.find.byId(id);
        return ok(views.html.editUsersDetails.render(user));
    }

    public Result updateUserDetail() {
        DynamicForm requestData = formFactory.form().bindFromRequest();
        String userId = requestData.get("userId");
        String name = requestData.get("name");
        String email = requestData.get("email");
        String mobileNumber = requestData.get("mobileNumber");
        String gender = requestData.get("gender");
        User user = User.find.byId(Long.valueOf(userId));
        user.setName(name);
        user.setGender(gender.charAt(0));
        user.setPhoneNumber(mobileNumber);
        user.setEmail(email);
        user.setPromoCode(requestData.get("promoCode"));
        user.update();
        return redirect("/users/usersList");
    }
    public Result SearchForPromoCodeLogins() {

        String srcName = request().getQueryString("srcName");
        System.out.print("Name ------------."+srcName);
        List<User> userList = new ArrayList<>();
        List<Object> listOfIds = new ArrayList<>();
        ExpressionList<User> userExpressionList = null;

        if (isNotNullAndEmpty(srcName)) {
            listOfIds = User.find.where().or(Expr.like("lower(signupPromoCode)", "%" + srcName.toLowerCase() + "%"), Expr.like("lower(signupPromoCode)", "%" + srcName.toLowerCase() + "%")).orderBy("id").findIds();
            userExpressionList = User.find.where().or(Expr.in("id", listOfIds), Expr.in("id", listOfIds));
            userList = userExpressionList.orderBy("id").findList();
        } else if(!isNotNullAndEmpty(srcName)){
            flash("Enter the search For Promo code !");
        }
        ObjectNode objectNode = Json.newObject();
        setResult(objectNode, userList);
        return ok(Json.toJson(objectNode));
    }

    public Result viewSignUpPromoCodeLogins() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.viewSignUpPromoCodeUsers.render(signUpPromoCodeTableHeaders, "col-sm-12", "", "PromoCode", "", "", ""));

    }

    public Result checkTutorialCompletedStatus() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null){
            if (user.isAppTutorialStatus()){
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    public Result storeTutorialCompletedStatus(){
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        User user = currentUser();
        if (user != null){
            user.setAppTutorialStatus(true);
            user.update();
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

}
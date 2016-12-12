package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.LoginOtp;
import models.Ride;
import models.RideLocation;
import models.User;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import utils.GetBikeErrorCodes;
import utils.NumericUtils;

import java.io.*;
import java.util.*;

import static utils.CustomCollectionUtils.first;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class UserController extends BaseController {

    public LinkedHashMap<String, String> loginOtpTableHeaders = getTableHeadersList(new String[]{"", "", "#", "User Id", "OTP", "Created At"}, new String[]{"", "", "id", "userId", "generatedOtp", "createdAt"});
    public LinkedHashMap<String, String> userTableHeaders = getTableHeadersList(new String[]{"", "", "#", "Name", "Phone Number", "Auth.Token", "Gcm Code"}, new String[]{"", "", "id", "name", "phoneNumber", "authToken", "gcmCode"});

    public Result index() {
        return ok(views.html.userIndex.render(User.find.all(), Ride.find.all(), RideLocation.find.all(), LoginOtp.find.all()));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result signup() {
        int errorCode = 0;
        JsonNode userJson = request().body().asJson();
        User user = Json.fromJson(userJson, User.class);
        int previousUserCount = User.find.where().eq("phoneNumber", user.getPhoneNumber()).findRowCount();
        if (previousUserCount == 0) {
            user.save();
            return ok(Json.toJson(user));
        } else {
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
            sendSms(generatedOtp, phoneNumber);

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
            user.save();
            result = SUCCESS;
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }

    private void sendSms(String generatedOtp, String phoneNumber) {
        String message = "Dear Customer, your NETSECURE code is " + generatedOtp + ".";
        message = message.replaceAll("%", "%25");
        message = message.replaceAll("&", "%26");
        //message = message.replaceAll("+", "%2B");
        message = message.replaceAll("#", "%23");
        message = message.replaceAll("=", "%3D");
        message = message.replaceAll(" ", "%20");
        String url = "http://smslane.com/vendorsms/pushsms.aspx?user=siva_nookala&password=957771&msisdn=91" + phoneNumber + "&sid=JavaMC&msg=" + message + "&fl=0&gwid=2";
        try {
            Process process = Runtime.getRuntime().exec("curl " + url);
            System.out.println("Process result : " + process.waitFor());
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stderr));

            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println("Stdout: " + line);
            }


            while ((line = reader.readLine()) != null) {
                System.out.println("Stdout: " + line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Result loginOtpList() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.loginOtpList.render(loginOtpTableHeaders));
    }

    public Result usersList() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        return ok(views.html.usersList.render(userTableHeaders));
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

    public Result performSearch1(String name) {
        List<LoginOtp> loginOtpList = LoginOtp.find.all();
        return ok(Json.toJson(loginOtpList));
    }
}

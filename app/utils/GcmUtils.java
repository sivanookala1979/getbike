package utils;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sivanookala on 09/11/16.
 */
public class GcmUtils implements IGcmUtils {

    public static final String SERVER_API_KEY = "AIzaSyBrJJcH_DyggMiWCmMK79OPLxxaf5YYVSw";

    public boolean sendMessage(User user, String messageString, String messageType, Long rideId) {
        return sendMessage(Collections.singletonList(user), messageString, messageType, rideId);
    }

    public boolean sendMessage(List<User> users, String messageString, String messageType, Long rideId) {
        boolean result = false;
        List<String> registrationIds = new ArrayList<>();
        try {
            Sender sender = new Sender(SERVER_API_KEY);
            Message.Builder builder = new Message.Builder()
                    .addData("message", messageString);
            if (StringUtils.isNotNullAndEmpty(messageType)) {
                builder.addData("messageType", messageType);
            }
            if (rideId != null && rideId > 0) {
                builder.addData("rideId", rideId + "");
            }
            Message message = builder.build();
            for (User user : users) {
                if (user != null && user.getGcmCode() != null && !user.getGcmCode().isEmpty()) {
                    registrationIds.add(user.getGcmCode());
                }
            }
            if (!registrationIds.isEmpty()) {
                com.google.android.gcm.server.MulticastResult serverResult = sender.send(message, registrationIds, 2);
                System.out.println("GCM Message ID: " + serverResult.getMulticastId());
            } else {
                System.out.println("No valid registration Ids");
            }
            result = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}

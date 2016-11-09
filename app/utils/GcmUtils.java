package utils;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import models.User;

/**
 * Created by sivanookala on 09/11/16.
 */
public class GcmUtils implements IGcmUtils {

    public static final String SERVER_API_KEY = "AIzaSyBrJJcH_DyggMiWCmMK79OPLxxaf5YYVSw";

    public void sendMessage(User user, String messageString) {
        if (user != null && user.getGcmCode() != null && !user.getGcmCode().isEmpty()) {
            try {
                Sender sender = new Sender(SERVER_API_KEY);
                Message message = new Message.Builder()
                        .addData("message", messageString)
                        .build();
                com.google.android.gcm.server.Result result = sender.send(message, user.getGcmCode(), 2);
                System.out.println("GCM Message ID: " + result.getMessageId());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

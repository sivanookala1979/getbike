package utils;

import models.User;

/**
 * Created by sivanookala on 09/11/16.
 */
public interface IGcmUtils {
     void sendMessage(User user, String messageString);
}

package utils;

import models.User;

/**
 * Created by sivanookala on 09/11/16.
 */
public interface IGcmUtils {
     boolean sendMessage(User user, String messageString, String messageType, Long rideId);
}

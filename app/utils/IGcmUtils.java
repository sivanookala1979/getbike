package utils;

import models.User;

import java.util.List;

/**
 * Created by sivanookala on 09/11/16.
 */
public interface IGcmUtils {
    boolean sendMessage(User user, String messageString, String messageType, Long rideId);

    boolean sendMessage(List<User> users, String messageString, String messageType, Long rideId);
}

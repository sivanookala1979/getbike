package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by sivanookala on 15/02/17.
 */
public class SMSHelper {
    public static boolean PERFORM_DELIVERIES = false;

    public static void sendSms(String templateId, String phoneNumber, String urlParams) {
        String command = "curl " + "http://123.63.33.43//blank/sms/user/urlsmstemp.php?username=Vave&pass=Vav@einf5&senderid=getbyk&tempid=" +
                templateId +
                "&response=Y&dest_mobileno=" + phoneNumber + urlParams;
        System.out.println("Delivery : " + PERFORM_DELIVERIES + " SMS Command : " + command);
        if (!PERFORM_DELIVERIES) return;
        try {
            Process process = Runtime.getRuntime().exec(command);
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


    public static String smsPrepare(String message) {
        if (message == null) return "";
        message = message.replaceAll("%", "%25");
        message = message.replaceAll("&", "%26");
        //message = message.replaceAll("+", "%2B");
        message = message.replaceAll("#", "%23");
        message = message.replaceAll("=", "%3D");
        message = message.replaceAll(" ", "%20");
        return message;
    }
}

package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sivanookala on 16/12/16.
 */
public class DateUtils {
    public static Date stringToDate(String date) {
        Date dateTime = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dateTime = simpleDateFormat.parse(date);
        } catch (Exception ex) {
            System.out.println("Exception " + ex);
        }
        return dateTime;
    }

    public static String getNewDate(String oldDate, int hours, int minutes, int secounds) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String newDate = oldDate;
        Date stringToDate = stringToDate(oldDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(stringToDate);
        cal.add(Calendar.MINUTE, minutes);
        cal.add(Calendar.HOUR, hours);
        cal.add(Calendar.SECOND, secounds);
        newDate = sdf.format(cal.getTime());
        return newDate;
    }
}

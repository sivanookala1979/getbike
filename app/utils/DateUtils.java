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

    public static Date minutesOld(int minutes) {
        return new Date(new Date().getTime() - minutes * 60 * 1000);
    }

    public static boolean isTimePassed(Date oldDate, Date newDate, int timeInSeconds) {
        return (newDate.getTime() - oldDate.getTime()) > timeInSeconds * 1000;
    }

    public static String convertDateMilliSecondToString(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy hh:mm:ss a");
        return sdf.format(date);
    }
}

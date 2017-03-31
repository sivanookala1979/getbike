package utils;

import play.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sivanookala on 16/12/16.
 */
public class DateUtils {
    public static final String YYYYMMDD = "YYYY-MM-dd";
    public static final String MMMDDYYYYHHMMSSA = "MMM dd yyyy hh:mm:ss a";

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

    public static String convertDateToString(Date userDate, String dateFormat) {
        Date date = new Date(userDate.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);
    }

    public static Date convertUTCDateToISTDate(String utcDate) {
        DateFormat formater = new SimpleDateFormat("MMM dd yyyy hh:mm:ss a");
        TimeZone istTime = TimeZone.getTimeZone("IST");
        formater.setTimeZone(istTime);
        Date date = null;
        try {
            date = formater.parse(utcDate);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return date;
    }

    public static List<Date> previousWeekDates() {
        List<Date> dates = new ArrayList<>();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // declare as DateFormat
        Date todayD = null;
        int j = 0;
        for (int i = 0; i < 7; i++) {
            Calendar today = Calendar.getInstance();
            today.add(Calendar.DATE, j);
            todayD = today.getTime();
            j--;
            dates.add(todayD);
        }
        Collections.reverse(dates);
        return dates;
    }
    public static Date getDateFromString(String stringDate){
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
        Date date = null;
        try {
            date = formatter.parse(stringDate);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
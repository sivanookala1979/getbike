package utils;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtils {

    public static final int CHARACTER_MEAN = 177;
    public static final String DEPTH_FORMAT = "%10.2f";
    public static final String ONE_DECIMAL_FORMAT = "%3.1f";
    public static final String TWO_DECIMALS_FORMAT = "%4.2f";
    public static final String THREE_DECIMALS_FORMAT = "%4.3f";
    public static final String FIVE_DECIMALS_FORMAT = "%6.5f";
    public static final String FOUR_DECIMALS_FORMAT = "%1.4f";
    public static final String PROBABILITY_FORMAT = "%6.4f";
    public static final String SIX_DECIMALS_FORMAT = "%1.6f";
    public static final String STATISTICS_FORMAT = "%10.4f";
    public static final String EXPONENTIAL_FORMAT = "%7.5E";
    public static final String EXPONENTIAL_DECIMAL_FORMAT = "#.###E000";
    public static final DecimalFormat sixDecimalFormat = new DecimalFormat("###.######");
    public static final DecimalFormat fiveDecimalFormat = new DecimalFormat("###.#####");
    public static final DecimalFormat twoDecimalFormat = new DecimalFormat("##0.00");
    public static final DecimalFormat fourDecimalFormat = new DecimalFormat("##0.0000");
    public static final DecimalFormat oneDecimalFormat = new DecimalFormat("##0.0");
    public static final DecimalFormat threeDecimalFormat = new DecimalFormat("##0.000");
    public static final DecimalFormat fiveDecimalFloatFormat = new DecimalFormat("##0.00000");
    public static final DecimalFormat zeroDecimalFormat = new DecimalFormat("###");
    public static final DecimalFormat exponentialDecimalFormat = new DecimalFormat(EXPONENTIAL_DECIMAL_FORMAT);
    public static final DecimalFormat atmostThreeDecimalsFormat = new DecimalFormat("####.###");

    public static String formatDepth(Double value, int numberOfDecimals) {
        String result = null;
        numberOfDecimals = Math.max(numberOfDecimals, 0);
        switch (numberOfDecimals) {
            case 0:
                result = zeroDecimalFormat.format(value);
                break;
            case 1:
                result = oneDecimalFormat.format(value);
                break;
            case 2:
                result = twoDecimalFormat.format(value);
                break;
            case 3:
                result = threeDecimalFormat.format(value);
                break;
            case 4:
                result = fourDecimalFormat.format(value);
                break;
            default:
                result = fiveDecimalFloatFormat.format(value);
        }
        return result;
    }

    public static String formatRangeItem(Double value) {
        String format = "###.#####";
        if (value != null && value != 0.0) {
            if ((value < 0.00001 && value > -0.00001) || value > 200000 || value < -200000) {
                format = EXPONENTIAL_DECIMAL_FORMAT;
            } else if (value < 0.001 && value > -0.001) {
                format = "###.######";
            } else if (value < 0.01 && value > -0.01) {
                format = "###.####";
            }
        }
        DecimalFormat decimalFormat = new DecimalFormat(format);
        return decimalFormat.format(value);
    }

    public static String formatPercent(Double value, boolean includePercent) {
        String formate = includePercent ? "%5.2f %%" : "%5.2f";
        return value == null ? StringUtils.EMPTY : String.format(formate, value * 100);
    }

    public static String formatDateAndTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMMMMMM dd, yyyy   HH:mm:ss");
        return sdf.format(date);
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMMMMMM dd, yyyy");
        return sdf.format(date);
    }

    public static String formatDateShort(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(GetBikeUtils.SHORT_DAY_MONTH_FORMAT);
        return sdf.format(date);
    }

    public static String formatTime(Long timeInMillSeconds) {
        Long timeInSeconds = timeInMillSeconds / 1000;
        int hours = (int) (timeInSeconds / (60 * 60));
        int minutes = (int) ((timeInSeconds - hours * 60 * 60) / 60);
        int seconds = (int) (timeInSeconds - hours * 60 * 60 - minutes * 60);
        return String.format("%2dH:%2dM:%2dS", hours, minutes, seconds);
    }

    public static String formatDouble(double d) {
        return fiveDecimalFormat.format(d);
    }

    public static String formatDouble(Double d) {
        if (d == null) {
            return "";
        }
        return fiveDecimalFormat.format(d);
    }

    public static String formatDouble(DecimalFormat decimalFormat, Double d) {
        if (d == null) {
            return "";
        }
        return decimalFormat.format(d);
    }

    public static String formatDoubleShowFineValues(Double d) {
        if (d != null && NumericUtils.isNotZero(d)) {
            if (d < 0.00001 && d > -0.00001) {
                return exponentialDecimalFormat.format(d);
            }
            if (d < 0.001 && d > -0.001) {
                return sixDecimalFormat.format(d);
            }
        }
        return formatDouble(d);
    }

    public static String formatFloat(float f) {
        return fiveDecimalFormat.format(f);
    }

    public static String formatDouble(String string, double d) {
        String formattedRange = String.format(string, d);
        return formattedRange;
    }

    public static String formatVersus(String string1, String string2) {
        return String.format("%s Vs %s", string1, string2);
    }

    public static String formatInteger(Integer intValue) {
        return intValue.toString();
    }

    public static String formatRange(Object object1, Object object2) {
        return String.format("%s - %s", object1.toString(), object2.toString());
    }

    public static String formatStatistics(double dMean, double dStandardDeviation) {
        return formatStatistics(dMean, dStandardDeviation, STATISTICS_FORMAT);
    }

    public static String formatStatistics(double dMean, double dStandardDeviation, String format) {
        return String.format(format + "%c" + format, dMean, (char) CHARACTER_MEAN, dStandardDeviation);
    }

    public static String formatNameAndIndex(int index, String name) {
        return (index + 1) + ". " + name;
    }
}
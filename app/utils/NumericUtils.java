package utils;


import java.math.BigDecimal;
import java.util.*;

/**
 * @author Srinivasarao Kandibanda
 * @version 1.0, Mar 23, 2011
 */
public class NumericUtils {

    private static final double ALLOWED_DIFFERENCE = 0.000001;
    public static final double ZERO = 0.0;
    public static final int INTEGER_ZERO = 0;

    public static boolean isPositive(Double value) {
        return value != null && value > 0;
    }

    public static int increment(Integer value) {
        return 1 + (value == null ? 0 : value);
    }

    public static double inverse(double value) {
        return 1.0 / value;
    }

    public static boolean isPositiveOrZero(Double value) {
        return value != null && value >= 0;
    }

    public static boolean isNegative(double value) {
        return value < 0;
    }

    public static boolean isNegativeOrZero(double value) {
        return value <= 0;
    }

    public static boolean isZero(double value) {
        return value == 0.0;
    }

    public static boolean isNotZero(double value) {
        return value != 0.0;
    }

    public static boolean isValid(double value) {
        return !Double.isInfinite(value) && !Double.isNaN(value);
    }

    public static boolean isNotNullAndValid(Double value) {
        return value != null && !Double.isInfinite(value) && !Double.isNaN(value);
    }

    public static boolean isNotNullAndValid(Double... values) {
        boolean result = true;
        for (Double value : values) {
            result = result && isNotNullAndValid(value);
        }
        return result;
    }

    public static boolean equalsAny(Integer searchFor, Integer... values) {
        boolean result = false;
        for (Integer value : values) {
            if (value == null && searchFor == null) {
                result = true;
                break;
            }
            if (value != null && value.equals(searchFor)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static boolean equals(Integer value1, Integer value2) {
        return value1 != null && value2 != null && value1.intValue() == value2.intValue();
    }

    public static double zeroIfNull(Double value) {
        return value == null ? NumericUtils.ZERO : value;
    }

    public static int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    public static Double nullIfZero(double value) {
        return isZero(value) ? null : value;
    }

    public static List<Integer> getRandomSeedList(int numberOfRandomSeeds) {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < numberOfRandomSeeds; i++) {
            Double randomSeed = Math.random() * NumericConstants.INTEGER_MAX;
            if (randomSeed.intValue() > NumericConstants.INTEGER_MIN && randomSeed.intValue() < NumericConstants.INTEGER_MAX) {
                result.add(randomSeed.intValue());
            }
        }
        return result;
    }

    public static Double getDouble(Float input) {
        return input != null ? input.doubleValue() : null;
    }

    public static Float getFloat(Double input) {
        return input != null ? input.floatValue() : null;
    }

    public static boolean equals(double value1, double value2) {
        return Math.abs(value1 - value2) < ALLOWED_DIFFERENCE;
    }

    public static boolean isLessThan(double value1, double value2) {
        return !equals(value1, value2) && value1 < value2;
    }

    public static boolean isLessThanOrEquals(double value1, double value2) {
        return equals(value1, value2) || value1 < value2;
    }

    public static boolean isGreaterThan(double value1, double value2) {
        return !equals(value1, value2) && value1 > value2;
    }

    public static boolean isGreaterThanOrEquals(double value1, double value2) {
        return equals(value1, value2) || value1 > value2;
    }

    public static boolean isInIncludingRange(double start, double end, double value) {
        return isGreaterThanOrEquals(value, start) && isLessThanOrEquals(value, end);
    }

    public static boolean isNotFractional(double mean) {
        return (mean <= -1 || mean >= 1);
    }

    public static int getMaximumNumberOfDecimals(List<Double> rangeValues) {
        int result = -1;
        for (Double value : rangeValues) {
            result = Math.max(result, getDecimalCount(value));
        }
        return result;
    }

    public static int getDecimalCount(Double value) {
        int result = 0;
        if (value != null) {
            String stringAfterDecimal = StringUtils.getStringAfter(value.toString(), '.');
            result = isZero(Double.parseDouble(stringAfterDecimal)) ? 0 : stringAfterDecimal.length();
        }
        return result;
    }

    public static boolean hasZero(int[] values) {
        boolean result = false;
        for (int value : values) {
            if (NumericUtils.isZero(value)) {
                result = true;
            }
        }
        return result;
    }

    public static Map<String, Double> getAdjustMapValuesSumToHundred(Map<String, Double> map) {
        Map<String, Double> result = new HashMap<String, Double>();
        Double sum = 0.0;
        BigDecimal newSum = new BigDecimal(0);
        BigDecimal maxiumSum = new BigDecimal(100);
        for (String key : map.keySet()) {
            sum += map.get(key);
        }
        if (sum != maxiumSum.doubleValue()) {
            int size = map.size();
            for (String key : map.keySet()) {
                Double newValue = roundToTwoDecimals(((map.get(key) / sum) * maxiumSum.doubleValue()));
                if (size > 1) {
                    result.put(key, newValue);
                } else {
                    result.put(key, roundToTwoDecimals(maxiumSum.subtract(newSum).doubleValue()));
                }
                newSum = newSum.add(new BigDecimal(newValue));
                size--;
            }
            return result;
        }
        return map;
    }

    public static Double roundToTwoDecimals(Double value) {
        return Double.parseDouble(FormatUtils.formatDouble(FormatUtils.TWO_DECIMALS_FORMAT, value));
    }

    public static double round1(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();
        long factor = (long) antiLog(places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static Double round(Double value, int numberOfDecimals) {
        Double factor = 100000.0;
        switch (numberOfDecimals) {
            case 1:
                factor = 10.0;
                break;
            case 2:
                factor = 100.0;
                break;
            case 3:
                factor = 1000.0;
                break;
            case 4:
                factor = 10000.0;
                break;
            default:
                break;
        }
        return Math.floor(value * factor + 0.5) / factor;
    }

    public static Double truncateToTwoDecimals(Double value) {
        double factor = 100;
        return value != null ? ((int) (value * factor)) / factor : null;
    }

    public static double divide(double numerator, double denominator) {
        // TODO: Siva Nookala, Jun 7, 2012: Need to investigate why 'return isZero(numerator) ? ZERO : numerator / denominator;' crashes the JVM on 64-bit Windows 7.
        double result = numerator / denominator;
        return Double.isNaN(result) && numerator == 0.0 ? 0.0 : result;
    }

    public static boolean isEvenNumber(Integer number) {
        if (number != null) {
            if (number % 2 == 0) {
                return true;
            }
        }
        return false;
    }

    public static double negate(double value) {
        return -value;
    }

    public static double getTotal(double[] values) {
        double result = 0.0;
        for (double value : values) {
            result += value;
        }
        return result;
    }

    public static Double round(Double value) {
        return round(value, 4);
    }

    public static double excludeFraction(double value) {
        return (int) value;
    }

    public static int roundToNearestInt(Double value) {
        return (int) round1(value, 0);
    }

    public static double restrict(double start, double end, double input) {
        if (isGreaterThan(start, end)) {
            if (isGreaterThan(input, start)) {
                return start;
            } else if (isLessThan(input, end)) {
                return end;
            } else {
                return input;
            }
        }
        if (isGreaterThan(input, end)) {
            return end;
        } else if (isLessThan(input, start)) {
            return start;
        } else {
            return input;
        }
    }

    public static List<Double> getSteps(Double startValue, Double endValue, Double stepSize) {
        List<Double> steps = new ArrayList<Double>();
        if (endValue < startValue) {
            Double temp = startValue;
            startValue = endValue;
            endValue = temp;
        }
        for (Double value = startValue; value < endValue; ) {
            steps.add(value);
            final BigDecimal valueBigDecimal = new BigDecimal(Double.toString(value)).add(new BigDecimal(stepSize));
            value = valueBigDecimal.doubleValue();
        }
        steps.add(endValue);
        return steps;
    }

    public static double antiLog(double value) {
        return Math.pow(10, value);
    }

    public static <T> T getNearestValueForGivenValueFromList(double value, List<T> values, final DoubleProvider<T> provider) {
        List<T> copy = new ArrayList<T>();
        copy.addAll(values);
        int matchedIndex = 0;
        Collections.sort(copy, new Comparator<T>() {

            @Override
            public int compare(T arg0, T arg1) {
                return new Double(provider.getDoubleValue(arg0)).compareTo(new Double(provider.getDoubleValue(arg1)));
            }
        });
        double previousDifference = Math.abs(value - provider.getDoubleValue(CustomCollectionUtils.getFirstElement(copy)));
        for (int i = 1; i < copy.size(); i++) {
            double currentDifference = Math.abs(value - provider.getDoubleValue(copy.get(i)));
            if (currentDifference < previousDifference) {
                previousDifference = currentDifference;
                matchedIndex = i;
            }
        }
        return matchedIndex < copy.size() ? copy.get(matchedIndex) : null;
    }

    public static String generateOtp() {
        int size = 6;
        StringBuilder generatedToken = new StringBuilder();
        for (int i = 0; i < size; i++) {
            generatedToken.append(((int) (Math.random() * 10)) % 10);
        }
        return generatedToken.toString();
    }
}
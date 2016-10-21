package utils;

import java.util.*;

public class StringUtils {

    private static final String CARRIAGE_RETURN = "\r";
    public static final String HYPHEN = "-";
    public static final String HYPHEN_WITH_SPACES = " - ";
    public static final int SPLIT_SIZE = 250;
    public static final String EMPTY = "";
    public static final String PATTERN_FOR_REMOVE_SPACES_AND_TABS = "\\s+";
    public static final String UNDER_SCORE = "_";
    public static final String GREATER_THAN = ">";
    public static final char OPEN_BRACKET = '(';
    public static final char CLOSE_BRACKET = ')';

    public static String getStringBefore(String str, char delimiter) {
        return getStringBefore(str, delimiter + "");
    }

    public static String getStringBefore(String str, String delimiter) {
        String result = EMPTY;
        int index = str.indexOf(delimiter);
        if (index != -1) {
            result = str.substring(0, index);
        }
        return result;
    }

    public static String removeSpaces(String stringForRemoval) {
        return StringUtils.replace(stringForRemoval, " ", EMPTY);
    }

    public static String replace(String input, String unwantedPattern, String requiredPattern) {
        if (input == null) {
            return null;
        }
        return input.replaceAll(unwantedPattern, requiredPattern);
    }

    public static String getStringAfter(String str, char delimiter) {
        return getStringAfter(str, delimiter + "");
    }

    public static String getStringAfter(String str, String delimiter) {
        String result = EMPTY;
        int index = str.indexOf(delimiter);
        if (index != -1) {
            result = str.substring(index + delimiter.length());
        }
        return result;
    }

    public static List<String> getTokens(String string, String delimiters) {
        List<String> result = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(string, delimiters);
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken());
        }
        CustomCollectionUtils.removeNullOrEmpties(result);
        return result;
    }

    public static List<String> getTokensIncludingEmpties(final String input, char delimiter) {
        List<String> result = new ArrayList<String>();
        String workingString = input;
        while (isNotNullAndEmpty(workingString)) {
            int index = workingString.indexOf(delimiter);
            if (index != -1) {
                result.add(workingString.substring(0, index));
                workingString = workingString.substring(index + 1);
            } else {
                result.add(workingString);
                break;
            }
        }
        if (input.endsWith(Character.toString(delimiter))) {
            result.add(EMPTY);
        }
        return result;
    }

    public static boolean isNotNullAndEmpty(String string) {
        return string != null && !string.trim().isEmpty();
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    public static boolean isTrue(String booleanValue) {
        return StringUtils.isNotNullAndEmpty(booleanValue) && Boolean.valueOf(booleanValue);
    }

    public static boolean isNullOrEmptyObject(Object value) {
        return value == null || isNullOrEmpty(value.toString());
    }

    public static String suffixSpaces(String input, int length) {
        StringBuilder result = new StringBuilder();
        result.append(input);
        for (int i = input.length(); i < length; i++) {
            result.append(GamlsUtils.SPACE);
        }
        return result.toString();
    }

    public static String prefixSpaces(String input, int length) {
        String result = EMPTY;
        if (input == null)
            return null;
        if (input.length() <= length) {
            for (int i = 0; i < length; i++) {
                if (i == (length - (input.length()))) {
                    result += input;
                    break;
                }
                result += GamlsUtils.SPACE;
            }
            return result;
        }
        return input;
    }

    public static List<String> splitStrings(String input, int i) {
        List<String> result = new ArrayList<String>();
        int lengthRemaining = input.length();
        while (lengthRemaining > i) {
            result.add(input.substring(0, i));
            input = input.substring(i);
            lengthRemaining = input.length();
        }
        result.add(input);
        return result;
    }

    public static String mergeStrings(List<String> inputStrings, String delimiter) {
        StringBuilder result = new StringBuilder();
        if (inputStrings != null) {
            int inputStringsSize = inputStrings.size();
            for (int i = 0; i < inputStringsSize; i++) {
                result.append(inputStrings.get(i));
                if (delimiter != null && i < inputStringsSize - 1) {
                    result.append(delimiter);
                }
            }
        }
        return result.toString();
    }

    public static List<String> getStringsBefore(List<String> inputStrings, String separator) {
        List<String> result = new ArrayList<String>();
        for (String inputString : inputStrings) {
            if (isNotNullAndEmpty(inputString)) {
                if (inputString.contains(separator)) {
                    String value = getStringBefore(inputString, separator);
                    if (isNotNullAndEmpty(value)) {
                        result.add(value.trim());
                    }
                } else {
                    result.add(inputString.trim());
                }
            }
        }
        return result;
    }

    public static String mergeStrings(List<String> inputStrings) {
        StringBuilder result = new StringBuilder();
        for (String string : inputStrings) {
            result.append(string);
        }
        return result.toString();
    }

    public static String appendCloneDetails(String string) {
        return string + " (Copy)";
    }

    public static boolean equalsAny(String searchFor, String... values) {
        return equalsAny(searchFor, false, values);
    }

    public static boolean equalsAnyIgnoreCase(String searchFor, String... values) {
        return equalsAny(searchFor, true, values);
    }

    public static boolean equalsAny(String searchFor, boolean ignoreCase, String... values) {
        boolean result = false;
        for (String value : values) {
            if (value == null && searchFor == null) {
                result = true;
                break;
            }
            if (value != null && value.equals(searchFor)) {
                result = true;
                break;
            }
            if (ignoreCase && value != null && value.equalsIgnoreCase(searchFor)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static <T> boolean equalsAnyObject(T searchFor, T... values) {
        boolean result = false;
        for (T value : values) {
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

    public static boolean equals(String value1, String value2) {
        return value1 == value2 || (value1 != null && value1.equals(value2));
    }

    public static boolean notEquals(String value1, String value2) {
        return !equals(value1, value2);
    }

    public static String removeCharacters(String cellValue) {
        String result = "";
        for (byte character : cellValue.getBytes()) {
            if (Character.isDigit(character)) {
                result += (char) character;
            }
        }
        return result;
    }

    public static String toYesOrNoString(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? "Yes" : "No";
    }

    public static String getFirstNonEmptyString(String... strings) {
        for (String input : strings) {
            if (StringUtils.isNotNullAndEmpty(input)) {
                return input;
            }
        }
        return null;
    }

    public static int getLongestStringLength(List<String> strings) {
        int length = 0;
        for (String string : strings) {
            length = Math.max(length, string.length());
        }
        return length;
    }

    public static String[][] extractStringGrid(Object input) {
        String[][] result = {{}};
        if (input != null) {
            List<String> lines = getLines(input.toString());
            result = new String[lines.size()][];
            for (int rowIndex = 0; rowIndex < lines.size(); rowIndex++) {
                result[rowIndex] = lines.get(rowIndex).split(GamlsUtils.TAB);
            }
        }
        return result;
    }

    private static List<String> getLines(String inputString) {
        StringTokenizer st = new StringTokenizer(inputString, GamlsUtils.LINE_DELIMITERS);
        List<String> lines = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            lines.add(st.nextToken());
        }
        return lines;
    }

    public static String removeBrackets(String input) {
        if (hasValidBrackets(input)) {
            int startIndex = -1;
            for (int i = input.length() - 1; i >= 0; i--) {
                if (input.charAt(i) == OPEN_BRACKET) {
                    startIndex = i;
                    break;
                }
            }
            input = input.replace(input.substring(startIndex, input.indexOf(CLOSE_BRACKET, startIndex) + 1), StringUtils.EMPTY).trim();
        }
        return hasValidBrackets(input) ? removeBrackets(input) : input;
    }

    public static boolean hasValidBrackets(String input) {
        int startIndex = input.indexOf(OPEN_BRACKET);
        int endIndex = startIndex != -1 ? input.indexOf(CLOSE_BRACKET, startIndex) : -1;
        return endIndex != -1;
    }

    public static boolean hasString(String input, String searchString) {
        return input != null && searchString != null && input.indexOf(searchString) >= 0;
    }

    public static boolean hasStringIgnoreCase(String input, String searchString) {
        return isNotNullAndEmpty(input) && isNotNullAndEmpty(searchString) && input.toUpperCase().contains(searchString.toUpperCase());
    }

    public static boolean hasAnyString(String input, String... searchStrings) {
        boolean result = false;
        for (String searchString : searchStrings) {
            if (hasString(input, searchString)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static boolean isFirstTokenNumeric(String input) {
        boolean result = false;
        String[] tokens = input.split(PATTERN_FOR_REMOVE_SPACES_AND_TABS);
        for (String token : tokens) {
            if (isNotNullAndEmpty(token)) {
                result = isValidNumber(token.trim());
                break;
            }
        }
        return result;
    }

    public static boolean isValidNumber(String parsedString) {
        boolean result = false;
        try {
            double parseDouble = Double.parseDouble(parsedString);
            if (NumericUtils.isValid(parseDouble)) {
                result = true;
            }
        } catch (NumberFormatException parseException) {
            GamlsLogger.fatal("Could not parse string " + parsedString);
        }
        return result;
    }

    public static List<Integer> parseNumbers(String parsedString, boolean acceptZeroAndOne) {
        boolean isValidNumber = true;
        HashSet<Integer> result = new HashSet<Integer>();
        try {
            List<String> tokens = getTokens(parsedString, GamlsUtils.COMMA);
            for (String token : tokens) {
                if (token.contains(HYPHEN)) {
                    int startNumber = Integer.parseInt(getStringBefore(token, HYPHEN).trim());
                    int endNumber = Integer.parseInt(getStringAfter(token, HYPHEN.charAt(0)).trim());
                    if (startNumber > endNumber) {
                        throw new NumberFormatException();
                    }
                    for (int number = startNumber; number <= endNumber; number++) {
                        result.add(number);
                    }
                } else {
                    result.add(Integer.parseInt(token.trim()));
                }
            }
        } catch (NumberFormatException parseException) {
            GamlsLogger.fatal("Could not parse string " + parsedString);
            isValidNumber = false;
        }
        if (!acceptZeroAndOne) {
            if (result.contains(0) || result.contains(1)) {
                isValidNumber = false;
            }
        }
        return isValidNumber ? new ArrayList<Integer>(result) : new ArrayList<Integer>();
    }

    public static boolean isValidIntegerRange(String parsedString, boolean acceptZeroAndOne) {
        final List<Integer> parseNumbers = parseNumbers(parsedString, acceptZeroAndOne);
        return !parseNumbers.isEmpty();
    }

    public static String emptyIfNull(String input) {
        return input == null ? EMPTY : input;
    }

    public static boolean areAllEmpties(List<String> linesColumns) {
        boolean result = true;
        for (String column : linesColumns) {
            if (StringUtils.isNotNullAndEmpty(column)) {
                result = false;
                break;
            }
        }
        return result;
    }

    public static int getMatchingStringIndex(List<String> tokens, String searchString) {
        int index = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).toUpperCase().contains(searchString.toUpperCase())) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static String getLastNonEmptyLineFromList(List<String> list) {
        String result = null;
        if (list != null) {
            int size = list.size();
            for (int i = size - 1; i >= NumericUtils.ZERO; i--) {
                String value = list.get(i);
                if (StringUtils.isNotNullAndEmpty(value)) {
                    result = value;
                    break;
                }
            }
        }
        return result;
    }

    public static String ensureSpace(String input) {
        if (input != null && !input.startsWith(GamlsUtils.SPACE)) {
            input = GamlsUtils.SPACE + input;
        }
        return input;
    }

    public static List<String> getLinesWithOutCarriageReturnFromString(String input) {
        List<String> result = new ArrayList<String>();
        String[] split = input.split(GamlsUtils.NEW_LINE);
        for (String string : split) {
            if (string.endsWith(CARRIAGE_RETURN)) {
                string = string.replaceAll(CARRIAGE_RETURN, EMPTY);
            }
            result.add(string);
        }
        return result;
    }

    public static String concatenateStringsSeperatedByComma(String... values) {
        String result = "";
        for (int i = 0; i < values.length; i++) {
            String inputString = values[i];
            if (StringUtils.isNullOrEmpty(result)) {
                result = inputString;
                continue;
            }
            if (i == values.length - 1) {
                result = result + " and " + inputString;
            } else {
                result = result + "," + inputString;
            }
        }
        return result;
    }

    public static String mergeByComma(Collection<String> input) {
        String result = EMPTY;
        for (String string : input) {
            result += string + GamlsUtils.COMMA;
        }
        return result.substring(0, result.lastIndexOf(GamlsUtils.COMMA));
    }
}
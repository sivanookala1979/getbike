package utils;

import java.util.*;

/**
 * Created by sivanookala on 21/10/16.
 */
public class CustomCollectionUtils {

    public static <T> T first(List<T> list) {
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public static void copyList(java.util.List dest, java.util.List source) {
        dest.clear();
        for (int i = 0; i < source.size(); i++) {
            dest.add(source.get(i));
        }
    }

    public static <T> List<T> excludeLastItem(java.util.List<T> source) {
        List<T> result = new ArrayList<T>();
        if (source != null) {
            result.addAll(source);
        }
        if (!result.isEmpty()) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    public static void copyMap(java.util.Map dest, java.util.Map source) {
        dest.clear();
        for (Object key : source.keySet()) {
            dest.put(key, source.get(key));
        }
    }

    public static void removeNullOrEmpties(java.util.List source) {
        for (int i = source.size() - 1; i >= 0; i--) {
            if (source.get(i) == null || source.get(i).toString().trim().equals("")) {
                source.remove(i);
            }
        }
    }

    public static List<Object> getAsObjectList(java.util.List source) {
        List<Object> result = new ArrayList<Object>();
        copyList(result, source);
        return result;
    }

    public static String[] convertToStringArray(List source) {
        String[] result = new String[source.size()];
        int i = 0;
        for (Object object : source) {
            result[i] = object.toString();
            i++;
        }
        return result;
    }

    public static String[] toStringArray(Object[] objects) {
        String[] values = new String[objects.length];
        int i = 0;
        for (Object lineStyle : objects) {
            values[i] = lineStyle.toString();
            i++;
        }
        return values;
    }

    public static boolean areAllValuesNotNull(Double... values) {
        boolean result = true;
        for (Double value : values) {
            if (value == null) {
                result = false;
                break;
            }
        }
        return result;
    }

    public static List<String> convertToStringList(List source) {
        List<String> result = new ArrayList<String>();
        for (Object object : source) {
            result.add(object.toString());
        }
        return result;
    }

    public static void move(Object object, int increment, List list) {
        int index = list.indexOf(object);
        int swapIndex = index + increment;
        if (swapIndex >= 0 && swapIndex < list.size()) {
            Object previousItem = list.get(swapIndex);
            list.set(index, previousItem);
            list.set(swapIndex, object);
        }
    }

    public static void move(int index, int increment, List list) {
        int swapIndex = index + increment;
        if (swapIndex >= 0 && swapIndex < list.size()) {
            Object object = list.remove(index);
            list.add(swapIndex, object);
        }
    }

    public static boolean containsInteger(int[] list, int value) {
        for (int selectedWell : list) {
            if (selectedWell == value) {
                return true;
            }
        }
        return false;
    }

    public static void swapItems(int sourceIndex, int destIndex, java.util.List items) {
        Object source = items.get(sourceIndex);
        Object dest = items.get(destIndex);
        items.remove(sourceIndex);
        items.add(sourceIndex, dest);
        items.remove(destIndex);
        items.add(destIndex, source);
    }

    public static void moveBefore(List list, Object toBeMoved, Object location) {
        if ((location == null || list.contains(location)) && list.contains(toBeMoved)) {
            list.remove(toBeMoved);
            int locationToBeMovedTo = list.indexOf(location);
            if (locationToBeMovedTo >= 0) {
                list.add(locationToBeMovedTo, toBeMoved);
            } else {
                list.add(toBeMoved);
            }
        }
    }

    public static void moveAfter(List list, Object toBeMoved, Object location) {
        if ((location == null || list.contains(location)) && list.contains(toBeMoved)) {
            list.remove(toBeMoved);
            int locationToBeMovedTo = list.indexOf(location);
            list.add(locationToBeMovedTo + 1, toBeMoved);
        }
    }

    public static void swapItems(int source, int dest, String[] items) {
        String swap = items[source];
        items[source] = items[dest];
        items[dest] = swap;
    }

    public static List getAsList(Object... objects) {
        List result = new ArrayList();
        for (Object object : objects) {
            result.add(object);
        }
        return result;
    }

    public static Set getAsSet(Object... objects) {
        Set result = new HashSet();
        for (Object object : objects) {
            result.add(object);
        }
        return result;
    }

    public static List getReverseNotNullList(Object[] objects) {
        List result = new ArrayList();
        for (Object object : objects) {
            if (object != null) {
                result.add(0, object);
            }
        }
        return result;
    }

    public static boolean isValidIndex(List list, int index) {
        return (list != null) && (index >= 0 && index < list.size());
    }

    public static <T> T getValue(List<T> list, Integer index) {
        T result = null;
        if ((index != null) && isValidIndex(list, index)) {
            result = list.get(index);
        }
        return result;
    }

    public static Integer[] convertToIntegerArray(int[] input) {
        Integer[] result = new Integer[input.length];
        int i = 0;
        for (int val : input)
            result[i++] = val;
        return result;
    }

    public static int[] convertToIntArray(List<Integer> input) {
        int[] result = new int[input.size()];
        int i = 0;
        for (int val : input)
            result[i++] = val;
        return result;
    }

    public static float[] convertToFloatArray(List<Float> input) {
        float[] result = new float[input.size()];
        int i = 0;
        for (float val : input)
            result[i++] = val;
        return result;
    }

    public static double[] convertToDoubleArray(List<Double> values) {
        double values2[] = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            values2[i] = values.get(i);
        }
        return values2;
    }

    public static void addUniqueElements(List destinationList, List sourceList) {
        if (destinationList != null && sourceList != null) {
            for (Object sourceItem : sourceList) {
                if (!destinationList.contains(sourceItem)) {
                    destinationList.add(sourceItem);
                }
            }
        }
    }

    public static <T> T getFirstElementIfValueNull(List<T> list, T t) {
        T result = t;
        if (result == null) {
            result = getFirstElement(list);
        }
        return result;
    }

    public static <T> T getFirstElement(List<T> list) {
        T result = null;
        if (list != null && list.size() > 0) {
            result = list.get(0);
        }
        return result;
    }

    public static <T> T getLastElement(List<T> list) {
        T result = null;
        if (list != null && list.size() > 0) {
            result = list.get(list.size() - 1);
        }
        return result;
    }

    public static <T> T getLastButOneElement(List<T> list) {
        T result = null;
        if (list != null && list.size() > 1) {
            result = list.get(list.size() - 2);
        }
        return result;
    }

    public static String[] mergeStringArrays(String[] array1, String[] array2) {
        final int size = array1.length + array2.length;
        String[] result = new String[size];
        int i = 0;
        for (; i < array1.length; i++) {
            result[i] = array1[i];
        }
        for (int j = 0; j < array2.length; i++, j++) {
            result[i] = array2[j];
        }
        return result;
    }

    public static boolean objectsNotNullAndEquals(Object object1, Object object2) {
        return object1 != null && object2 != null && object1.equals(object2);
    }

    public static float[][] convertToFloatArray(Float[][] input) {
        float[][] result = new float[input.length][];
        for (int i = 0; i < input.length; i++) {
            result[i] = new float[input[i].length];
            for (int j = 0; j < input[i].length; j++) {
                result[i][j] = input[i][j];
            }
        }
        return result;
    }

    public static Float[][] convertToFloatArray(float[][] input) {
        Float[][] result = new Float[input.length][];
        for (int i = 0; i < input.length; i++) {
            result[i] = new Float[input[i].length];
            for (int j = 0; j < input[i].length; j++) {
                result[i][j] = input[i][j];
            }
        }
        return result;
    }

    public static List<Double> convertToDoubleList(double[] input) {
        List<Double> result = new ArrayList<Double>();
        for (Double item : input) {
            result.add(item);
        }
        return result;
    }

    public static final String RANGE_STRING_FORMAT = FormatUtils.TWO_DECIMALS_FORMAT;

    public static List<String> getRangeStrings(double start, double end, double increment) {
        List<String> result = new ArrayList<String>();
        for (double point = start; NumericUtils.isLessThan(point, end); point += increment) {
            result.add(FormatUtils.formatDouble(RANGE_STRING_FORMAT, point));
        }
        result.add(FormatUtils.formatDouble(RANGE_STRING_FORMAT, end));
        return result;
    }

    public static <U, T> Set<U> uniqueList(List<? extends T> input, ObjectProvider<T, U> objectProvider) {
        return new HashSet<U>(collectObjects(input, objectProvider));
    }

    public static <U, T> List<U> collectObjects(List<? extends T> input, ObjectProvider<T, U> objectProvider) {
        List<U> result = new ArrayList<U>();
        for (T t : input) {
            result.add(objectProvider.getObject(t));
        }
        return result;
    }

    public static <U, T> Map<U, List<T>> groupObjects(List<T> objectsList, ObjectProvider<T, U> objectProvider) {
        Map<U, List<T>> result = new HashMap<U, List<T>>();
        for (U u : uniqueList(objectsList, objectProvider)) {
            List<T> ts = new ArrayList<T>();
            for (T t : objectsList) {
                if (u.equals(objectProvider.getObject(t))) {
                    ts.add(t);
                }
            }
            result.put(u, ts);
        }
        return result;
    }

    public static <T> List<T> select(List<? extends T> input, ObjectSelector<T> objectSelector) {
        List<T> result = new ArrayList<T>();
        for (T t : input) {
            if (objectSelector.isValid(t)) {
                result.add(t);
            }
        }
        return result;
    }

    public static <T> T match(List<? extends T> input, ObjectSelector<T> objectSelector) {
        T result = null;
        int size = input.size();
        for (int i = 0; i < size; i++) {
            T t = input.get(i);
            if (objectSelector.isValid(t)) {
                result = t;
                break;
            }
        }
        return result;
    }

    public static boolean isValidIndexAndNotFirstElement(List list, int index) {
        return CustomCollectionUtils.isValidIndex(list, index - 1);
    }

    public static boolean isValidIndexAndNotLastElement(List list, int index) {
        return CustomCollectionUtils.isValidIndex(list, index) && index < list.size() - 1;
    }

    public static List<String> createIndices(List<String> input, int height) {
        List<String> result = new ArrayList<String>();
        int inputListSize = input.size();
        float sampleIndexIncrement = (float) inputListSize / (float) height;
        float k = 0;
        for (int i = 0; i < inputListSize; k = k + sampleIndexIncrement, i = (int) k) {
            String matchedString = input.get(i);
            for (int j = 1; j <= matchedString.length(); j++) {
                if (!result.contains(matchedString.substring(0, j))) {
                    result.add(matchedString.substring(0, j));
                    break;
                }
            }
        }
        return result;
    }

    public static <T> T getMatchedElementForMaxIndexInDescendingOrder(List<T> list, int maxIndex) {
        T result = null;
        if (maxIndex >= 0) {
            if (list == null || list.isEmpty()) {
                return null;
            }
            if (list != null && list.size() > maxIndex) {
                return list.get(maxIndex);
            }
            result = getMatchedElementForMaxIndexInDescendingOrder(list, maxIndex - 1);
        }
        return result;
    }

    public static Boolean isSorted(List list) {
        List sortedList = new ArrayList(list);
        Collections.sort(sortedList, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return sortedList.equals(list);
    }

    public static <T> T getItemBefore(List<T> list, T searchObject) {
        T result = null;
        if (list != null) {
            int searchObjectIndex = list.indexOf(searchObject);
            if (isValidIndex(list, searchObjectIndex) && isValidIndex(list, searchObjectIndex - 1)) {
                result = list.get(searchObjectIndex - 1);
            }
        }
        return result;
    }

    public static <T> T getItemAfter(List<T> list, T searchObject) {
        T result = null;
        if (list != null) {
            int searchObjectIndex = list.indexOf(searchObject);
            if (isValidIndex(list, searchObjectIndex) && isValidIndex(list, searchObjectIndex + 1)) {
                result = list.get(searchObjectIndex + 1);
            }
        }
        return result;
    }

    public static <T> List<T> getNotNullValues(final List<T> keys, HashMap<T, T> map) {
        List<T> result = new ArrayList<T>();
        for (T key : keys) {
            if (map.get(key) != null) {
                result.add(map.get(key));
            }
        }
        return result;
    }
}
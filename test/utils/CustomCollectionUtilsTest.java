package utils;

/**
 * Created by sivanookala on 21/10/16.
 */

import org.junit.Test;

import java.util.*;
import static org.junit.Assert.*;

/**
 * @author Srinivas Kandibanda
 * @version 1.0, Jan 1, 2003
 */
public class CustomCollectionUtilsTest {

    @Test public void testCopyListTESTHappyFlow() throws Exception {
        // Setup fixture
        List actual = new ArrayList();
        List source = CustomCollectionUtils.getAsList("UI00", new Integer(50), new Double(50));
        // Exercise SUT
        CustomCollectionUtils.copyList(actual, source);
        // Verify outcome
        assertEquals(actual.size(), source.size());
        for (int i = 0; i < actual.size(); i++) {
            assertEquals(actual.get(i), source.get(i));
        }
    }

    @Test public void testCopyMapTESTHappyFlow() throws Exception {
        // Setup fixture
        Map<String, Integer> source = new HashMap<String, Integer>();
        source.put("Some String", 1);
        source.put("Some String", 2);
        source.put("Some String", 3);
        Map<String, Integer> destination = new HashMap<String, Integer>();
        // Exercise SUT
        CustomCollectionUtils.copyMap(destination, source);
        // Verify outcome
        assertNotSame(destination, source);
        assertEquals(destination.size(), source.size());
        for (String key : source.keySet()) {
            assertEquals(source.get(key), destination.get(key));
        }
    }

    @Test public void testRemoveNullOrEmptiesTESTHappyFlow() throws Exception {
        // Setup fixture
        List source = CustomCollectionUtils.getAsList("UI00", 50, null, 50.9, "");
        // Exercise SUT
        CustomCollectionUtils.removeNullOrEmpties(source);
        // Verify outcome
        assertEquals(3, source.size());
        assertEquals("UI00", source.get(0));
        assertEquals(50, source.get(1));
        assertEquals(50.9, source.get(2));
    }

    @Test public void testGetAsObjectListTESTHappyFlow() throws Exception {
        // Setup fixture
        List source = CustomCollectionUtils.getAsList("UI00", new Integer(50), new Double(50), "String2");
        // Exercise SUT
        List<Object> actual = CustomCollectionUtils.getAsObjectList(source);
        // Verify outcome
        assertEquals("UI00", actual.get(0));
        assertEquals(new Integer(50), actual.get(1));
        assertEquals(new Double(50), actual.get(2));
        assertEquals("String2", actual.get(3));
    }

    @Test public void testConvertToStringArrayTESTHappyFlow() throws Exception {
        // Setup fixture
        List source = CustomCollectionUtils.getAsList("UI00", new Integer(50), new Double(34.5), "String2");
        // Exercise SUT
        String[] actual = CustomCollectionUtils.convertToStringArray(source);
        // Verify outcome
        assertEquals("UI00", actual[0]);
        assertEquals("50", actual[1]);
        assertEquals("34.5", actual[2]);
        assertEquals("String2", actual[3]);
    }

    @Test public void testConvertToStringListTESTHappyFlow() throws Exception {
        // Setup fixture
        List source = CustomCollectionUtils.getAsList("UI00", new Integer(50), new Double(34.5), "String2");
        // Exercise SUT
        List<String> actual = CustomCollectionUtils.convertToStringList(source);
        // Verify outcome
        assertEquals("UI00", actual.get(0));
        assertEquals("50", actual.get(1));
        assertEquals("34.5", actual.get(2));
        assertEquals("String2", actual.get(3));
    }

    @Test public void testToStringArrayTESTHappyFlow() throws Exception {
        // Setup fixture
        Object[] source = new Object[]{
                "UI00",
                new Integer(50),
                new Double(34.5),
                "String2"};
        // Exercise SUT
        String[] actual = CustomCollectionUtils.toStringArray(source);
        // Verify outcome
        assertEquals("UI00", actual[0]);
        assertEquals("50", actual[1]);
        assertEquals("34.5", actual[2]);
        assertEquals("String2", actual[3]);
    }

    @Test public void testSwapItemsTESTForList() throws Exception {
        // Setup fixture
        List source = CustomCollectionUtils.getAsList("UI00", new Integer(50), new Double(50), "String2");
        // Exercise SUT
        CustomCollectionUtils.swapItems(0, 2, source);
        // Verify outcome
        assertEquals(new Double(50), source.get(0));
        assertEquals(new Integer(50), source.get(1));
        assertEquals("UI00", source.get(2));
        assertEquals("String2", source.get(3));
    }

    @Test public void testSwapItemsTESTHappyFlow() throws Exception {
        // Setup fixture
        String items[] = {
                "String1",
                "String2",
                "String3",
                "String4",
                "String5"};
        // Exercise SUT
        CustomCollectionUtils.swapItems(3, 4, items);
        // Verify outcome
        assertEquals("String5", items[3]);
        assertEquals("String4", items[4]);
    }

    @Test public void testMoveTESTIncrementByOne() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.move(object3, 1, list);
        // Verify Outcome
        assertEquals(list.get(2), object4);
        assertEquals(list.get(3), object3);
    }

    @Test public void testMoveTESTDecrementByOne() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.move(object3, -2, list);
        // Verify Outcome
        assertEquals(list.get(0), object3);
        assertEquals(list.get(2), object1);
    }

    @Test public void testMoveTESTByIndexBackword() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String1");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.move(2, -1, list);
        // Verify Outcome
        assertEquals(list.get(0), object1);
        assertEquals(list.get(1), object3);
        assertEquals(list.get(2), object2);
    }

    @Test public void testMoveTESTByIndexForward() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String1");
        Object object4 = new String("String4");
        Object object5 = new String("String1");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.move(0, 1, list);
        // Verify Outcome
        assertEquals(list.get(0), object2);
        assertEquals(list.get(1), object1);
        assertEquals(list.get(2), object3);
    }

    @Test public void testMoveBeforeTESTMoveBackWard() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.moveBefore(list, object4, object2);
        // Verify Outcome
        assertEquals(list.get(0), object1);
        assertEquals(list.get(1), object4);
        assertEquals(list.get(2), object2);
        assertEquals(list.get(3), object3);
        assertEquals(list.get(4), object5);
    }

    @Test public void testMoveBeforeTESTMoveForward() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.moveBefore(list, object2, object5);
        // Verify Outcome
        assertEquals(list.get(0), object1);
        assertEquals(list.get(1), object3);
        assertEquals(list.get(2), object4);
        assertEquals(list.get(3), object2);
        assertEquals(list.get(4), object5);
    }

    @Test public void testMoveBeforeTESTMoveToFirst() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.moveBefore(list, object3, object1);
        // Verify Outcome
        assertEquals(list.get(0), object3);
        assertEquals(list.get(1), object1);
        assertEquals(list.get(2), object2);
        assertEquals(list.get(3), object4);
        assertEquals(list.get(4), object5);
    }

    @Test public void testMoveBeforeTESTWithReferenceNull() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.moveBefore(list, object3, null);
        // Verify Outcome
        assertEquals(list.get(0), object1);
        assertEquals(list.get(1), object2);
        assertEquals(list.get(2), object4);
        assertEquals(list.get(3), object5);
        assertEquals(list.get(4), object3);
    }

    @Test public void testMoveAfterTESTMoveToEnd() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.moveAfter(list, object1, object5);
        // Verify Outcome
        assertEquals(list.get(0), object2);
        assertEquals(list.get(1), object3);
        assertEquals(list.get(2), object4);
        assertEquals(list.get(3), object5);
        assertEquals(list.get(4), object1);
    }

    @Test public void testMoveAfterTESTWithReferenceNull() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.moveAfter(list, object5, null);
        // Verify Outcome
        assertEquals(list.get(0), object5);
        assertEquals(list.get(1), object1);
        assertEquals(list.get(2), object2);
        assertEquals(list.get(3), object3);
        assertEquals(list.get(4), object4);
    }

    @Test public void testMoveAfterTESTMoveInBetween() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.moveAfter(list, object2, object4);
        // Verify Outcome
        assertEquals(list.get(0), object1);
        assertEquals(list.get(1), object3);
        assertEquals(list.get(2), object4);
        assertEquals(list.get(3), object2);
        assertEquals(list.get(4), object5);
    }

    @Test public void testMoveAfterTESTMoveBackwards() {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        // Exercise SUT
        CustomCollectionUtils.moveAfter(list, object4, object1);
        // Verify Outcome
        assertEquals(list.get(0), object1);
        assertEquals(list.get(1), object4);
        assertEquals(list.get(2), object2);
        assertEquals(list.get(3), object3);
        assertEquals(list.get(4), object5);
    }

    @Test public void testContainsIntegerTESTHappyFlow() {
        // Setup fixture
        int[] list = {
                20,
                -99,
                42};
        // Exercise SUT
        assertTrue(CustomCollectionUtils.containsInteger(list, 20));
        assertTrue(CustomCollectionUtils.containsInteger(list, -99));
        assertTrue(CustomCollectionUtils.containsInteger(list, 42));
        assertFalse(CustomCollectionUtils.containsInteger(list, -11));
        assertFalse(CustomCollectionUtils.containsInteger(list, 0));
        // Verify Outcome
    }

    @Test public void testGetAsListTESTHappyFlow() throws Exception {
        // Setup fixture
        // Exercise SUT
        List actual = CustomCollectionUtils.getAsList("UI00", new Integer(50), new Double(50));
        // Verify outcome
        assertEquals(3, actual.size());
        assertEquals("UI00", actual.get(0));
        assertEquals(new Integer(50), actual.get(1));
        assertEquals(new Double(50), actual.get(2));
    }

    @Test public void testGetAsSetTESTHappyFlow() throws Exception {
        // Setup fixture
        // Exercise SUT
        Set actual = CustomCollectionUtils.getAsSet("UI00", new Integer(50), new Double(50));
        // Verify outcome
        assertEquals(3, actual.size());
        assertTrue(actual.contains("UI00"));
        assertTrue(actual.contains(new Integer(50)));
        assertTrue(actual.contains(new Double(50)));
    }

    @Test public void testIsValidIndexTESTHappyFlow() throws Exception {
        // Setup fixture
        List list = CustomCollectionUtils.getAsList("UI00", new Integer(50), new Double(50));
        // Exercise SUT
        // Verify outcome
        assertFalse(CustomCollectionUtils.isValidIndex(list, -1));
        assertTrue(CustomCollectionUtils.isValidIndex(list, 0));
        assertTrue(CustomCollectionUtils.isValidIndex(list, 2));
        assertFalse(CustomCollectionUtils.isValidIndex(list, 3));
        assertFalse(CustomCollectionUtils.isValidIndex(null, 3));
        assertFalse(CustomCollectionUtils.isValidIndex(null, 2));
    }

    @Test public void testGetValueTESTHappyFlow() throws Exception {
        // Setup fixture
        List list = CustomCollectionUtils.getAsList("UI00", new Integer(50), new Double(50));
        // Exercise SUT
        // Verify outcome
        assertNull(CustomCollectionUtils.getValue(list, null));
        assertNull(CustomCollectionUtils.getValue(list, 5));
        assertNull(CustomCollectionUtils.getValue(list, -1));
        assertEquals("UI00", CustomCollectionUtils.getValue(list, 0));
        assertEquals(new Integer(50), CustomCollectionUtils.getValue(list, 1));
        assertEquals(new Double(50), CustomCollectionUtils.getValue(list, 2));
    }

    @Test public void testConvertToIntArrayTESTHappyFlow() throws Exception {
        // Setup fixture
        List<Integer> input = CustomCollectionUtils.getAsList(3, 10, 200);
        // Exercise SUT
        int[] actual = CustomCollectionUtils.convertToIntArray(input);
        // Verify outcome
        assertEquals(3, actual[0]);
        assertEquals(10, actual[1]);
        assertEquals(200, actual[2]);
    }

    @Test public void testConvertToIntegerArrayTESTHappyFlow() throws Exception {
        // Setup fixture
        int input[] = {
                10,
                35,
                28};
        // Exercise SUT
        Integer[] actual = CustomCollectionUtils.convertToIntegerArray(input);
        // Verify outcome
        assertEquals(10, actual[0].intValue());
        assertEquals(35, actual[1].intValue());
        assertEquals(28, actual[2].intValue());
    }

    @Test public void testConvertToFloatArrayTESTHappyFlow() throws Exception {
        // Setup fixture
        List<Float> input = CustomCollectionUtils.getAsList(3.21f, 10.9f, 200.21f);
        // Exercise SUT
        float[] actual = CustomCollectionUtils.convertToFloatArray(input);
        // Verify outcome
        assertEquals(3.21f, actual[0], NumericConstants.DELTA);
        assertEquals(10.9f, actual[1], NumericConstants.DELTA);
        assertEquals(200.21f, actual[2], NumericConstants.DELTA);
    }

    @Test public void testConvertToDoubleArrayTESTHappyFlow() throws Exception {
        // Setup fixture
        List<Double> input = CustomCollectionUtils.getAsList(3.21, 10.9, 200.21, 2101321808.0);
        // Exercise SUT
        double[] actual = CustomCollectionUtils.convertToDoubleArray(input);
        // Verify outcome
        assertEquals(3.21, actual[0], NumericConstants.DELTA);
        assertEquals(10.9, actual[1], NumericConstants.DELTA);
        assertEquals(200.21, actual[2], NumericConstants.DELTA);
        assertEquals(2101321808.0, actual[3], NumericConstants.DELTA);
    }

    @Test public void testAddUniqueElementsTESTHappyFlow() throws Exception {
        // Setup fixture
        List dest = CustomCollectionUtils.getAsList(new Integer(20), "POI", new Double(21));
        List source = CustomCollectionUtils.getAsList(new Integer(20), "POI", "NEWSTRING");
        int countBefore = dest.size();
        // Exercise SUT
        CustomCollectionUtils.addUniqueElements(dest, source);
        // Verify outcome
        assertEquals(countBefore + 1, dest.size());
        assertEquals(new Integer(20), dest.get(0));
        assertEquals("POI", dest.get(1));
        assertEquals(new Double(21), dest.get(2));
        assertEquals("NEWSTRING", dest.get(3));
    }

    @Test public void testGetFirstElementTESTHappyFlow() throws Exception {
        // Setup fixture
        List list = new ArrayList();
        list.add("String 1");
        list.add(new Integer(50));
        // Exercise SUT
        Object actual = CustomCollectionUtils.getFirstElement(list);
        // Verify outcome
        assertEquals("String 1", actual);
    }

    @Test public void testGetFirstElementTESTWithEmptyList() throws Exception {
        // Setup fixture
        List list = new ArrayList();
        // Exercise SUT
        Object actual = CustomCollectionUtils.getFirstElement(list);
        // Verify outcome
        assertNull(actual);
    }

    @Test public void testGetLastElementTESTHappyFlow() throws Exception {
        // Setup fixture
        List list = new ArrayList();
        list.add("String 1");
        list.add(new Integer(50));
        list.add("String 3");
        // Exercise SUT
        Object actual = CustomCollectionUtils.getLastElement(list);
        // Verify outcome
        assertEquals("String 3", actual);
    }

    @Test public void testGetLastElementTESTWithEmptyList() throws Exception {
        // Setup fixture
        List list = new ArrayList();
        // Exercise SUT
        Object actual = CustomCollectionUtils.getLastElement(list);
        // Verify outcome
        assertNull(actual);
    }

    @Test public void testGetLastButOneElementTESTHappyFlow() throws Exception {
        // Setup fixture
        List list = new ArrayList();
        list.add("String 1");
        list.add(new Integer(50));
        list.add("String 3");
        list.add("String 4");
        list.add("String 67");
        // Exercise SUT
        Object actual = CustomCollectionUtils.getLastButOneElement(list);
        // Verify outcome
        assertEquals("String 4", actual);
    }

    @Test public void testGetLastButOneElementTESTWithSingleElementInTheList() throws Exception {
        // Setup fixture
        List list = new ArrayList();
        list.add("String 1");
        // Exercise SUT
        Object actual = CustomCollectionUtils.getLastButOneElement(list);
        // Verify outcome
        assertNull(actual);
    }

    @Test public void testGetLastButOneElementTESTWithEmptyList() throws Exception {
        // Setup fixture
        // Exercise SUT
        Object actual = CustomCollectionUtils.getLastButOneElement(Collections.EMPTY_LIST);
        // Verify outcome
        assertNull(actual);
    }

    @Test public void testMoveTESTForward() throws Exception {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        int current = 0;
        int destination = 2;
        int increment = destination - current;
        // Exercise SUT
        CustomCollectionUtils.move(current, increment, list);
        // Verify outcome
        assertEquals(5, list.size());
        assertEquals(object2, list.get(0));
        assertEquals(object3, list.get(1));
        assertEquals(object1, list.get(2));
        assertEquals(object4, list.get(3));
        assertEquals(object5, list.get(4));
    }

    @Test public void testMoveTESTBackward() throws Exception {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        int current = 4;
        int destination = 0;
        int increment = destination - current;
        // Exercise SUT
        CustomCollectionUtils.move(current, increment, list);
        // Verify outcome
        assertEquals(5, list.size());
        assertEquals(object5, list.get(0));
        assertEquals(object1, list.get(1));
        assertEquals(object2, list.get(2));
        assertEquals(object3, list.get(3));
        assertEquals(object4, list.get(4));
    }

    @Test public void testMoveTESTSwapLastTwoValues() throws Exception {
        // Setup fixture
        List list = new ArrayList();
        Object object1 = new String("String1");
        Object object2 = new String("String2");
        Object object3 = new String("String3");
        Object object4 = new String("String4");
        Object object5 = new String("String5");
        list.add(object1);
        list.add(object2);
        list.add(object3);
        list.add(object4);
        list.add(object5);
        int current = 4;
        int destination = 3;
        int increment = destination - current;
        // Exercise SUT
        CustomCollectionUtils.move(current, increment, list);
        // Verify outcome
        assertEquals(5, list.size());
        assertEquals(object1, list.get(0));
        assertEquals(object2, list.get(1));
        assertEquals(object3, list.get(2));
        assertEquals(object5, list.get(3));
        assertEquals(object4, list.get(4));
    }

    @Test public void testMergeStringArraysTESTHappyFlow() throws Exception {
        // Setup fixture
        String[] array1 = {
                "element1",
                "element2"};
        String[] array2 = {
                "element3",
                "element4",
                "element5"};
        // Exercise SUT
        String[] actual = CustomCollectionUtils.mergeStringArrays(array1, array2);
        // Verify outcome
        assertEquals(array1.length + array2.length, actual.length);
        assertEquals(array1[0], actual[0]);
        assertEquals(array1[1], actual[1]);
        assertEquals(array2[0], actual[2]);
        assertEquals(array2[1], actual[3]);
        assertEquals(array2[2], actual[4]);
    }

    @Test public void testGetReverseNotNullListTESTHappyFlow() throws Exception {
        // Setup fixture
        Object[] input = new Object[]{
                "String 1",
                "String 2",
                "String 3",
                null,
                "String 4",
                null,
                null};
        // Exercise SUT
        List actual = CustomCollectionUtils.getReverseNotNullList(input);
        // Verify outcome
        assertEquals("String 4", actual.get(0));
        assertEquals("String 3", actual.get(1));
        assertEquals("String 2", actual.get(2));
        assertEquals("String 1", actual.get(3));
    }

    @Test public void testAreAllValuesNotNullTESTHappyFlow() throws Exception {
        // Setup fixture
        // Exercise SUT
        boolean actual = CustomCollectionUtils.areAllValuesNotNull(12.0, 34.5, 45.8);
        // Verify outcome
        assertTrue(actual);
    }

    @Test public void testAreAllValuesNotNullTESTWithNullValue() throws Exception {
        // Setup fixture
        // Exercise SUT
        boolean actual = CustomCollectionUtils.areAllValuesNotNull(12.0, null, 45.8);
        // Verify outcome
        assertFalse(actual);
    }

    @Test public void testObjectsNotNullAndEqualsTESTHappyFlow() throws Exception {
        // Setup fixture
        Object object1 = new String("String1");
        Object object2 = new String("String1");
        // Exercise SUT
        boolean actual = CustomCollectionUtils.objectsNotNullAndEquals(object1, object2);
        // Verify outcome
        assertTrue(actual);
    }

    @Test public void testObjectsNotNullAndEqualsTESTWithUnMatchedObjects() throws Exception {
        // Setup fixture
        Object object1 = new String("String1");
        Object object2 = new Double(23.3);
        // Exercise SUT
        boolean actual = CustomCollectionUtils.objectsNotNullAndEquals(object1, object2);
        // Verify outcome
        assertFalse(actual);
    }

    @Test public void testGetFirstElementIfValueNullTESTWithPassedValueNull() throws Exception {
        // Setup fixture
        List list = new ArrayList();
        list.add("String 1");
        list.add(new Integer(50));
        // Exercise SUT
        Object actual = CustomCollectionUtils.getFirstElementIfValueNull(list, null);
        // Verify outcome
        assertEquals("String 1", actual);
    }

    @Test public void testGetFirstElementIfValueNullTESTWithPassedValueNotNull() throws Exception {
        // Setup fixture
        List list = new ArrayList();
        list.add("String 1");
        list.add(new Integer(50));
        // Exercise SUT
        Object actual = CustomCollectionUtils.getFirstElementIfValueNull(list, "String valid");
        // Verify outcome
        assertEquals("String valid", actual);
    }

    @Test public void testConvertToFloatArrayTESTWith2DFloatArray() throws Exception {
        // Setup fixture
        Float[][] expected = {
                {
                        2f,
                        3f,
                        5f},
                {
                        7f,
                        8f}};
        // Exercise SUT
        float[][] actual = CustomCollectionUtils.convertToFloatArray(expected);
        // Verify outcome
        assertEquals(2f, actual[0][0], NumericConstants.DELTA);
        assertEquals(3f, actual[0][1], NumericConstants.DELTA);
        assertEquals(5f, actual[0][2], NumericConstants.DELTA);
        assertEquals(7f, actual[1][0], NumericConstants.DELTA);
        assertEquals(8f, actual[1][1], NumericConstants.DELTA);
    }

    @Test public void testConvertToFloatArrayTESTWithPremitiveTypeToObjectiveType() throws Exception {
        // Setup fixture
        float[][] expected = {
                {
                        5f,
                        8f},
                {
                        3f,
                        9f,
                        11f}};
        // Exercise SUT
        Float[][] actual = CustomCollectionUtils.convertToFloatArray(expected);
        // Verify outcome
        assertEquals(5f, (float) actual[0][0], NumericConstants.DELTA);
        assertEquals(8f, (float) actual[0][1], NumericConstants.DELTA);
        assertEquals(3f, (float) actual[1][0], NumericConstants.DELTA);
        assertEquals(9f, (float) actual[1][1], NumericConstants.DELTA);
        assertEquals(11f, (float) actual[1][2], NumericConstants.DELTA);
    }

    @Test public void testConvertToDoubleListTESTHappyFlow() throws Exception {
        // Setup fixture
        double[] input = {
                23.45,
                11.02,
                05.56,
                105.005};
        // Exercise SUT
        List<Double> actual = CustomCollectionUtils.convertToDoubleList(input);
        // Verify outcome
        for (int i = 0; i < input.length; i++) {
            assertEquals((double) input[i], (double) actual.get(i), NumericConstants.DELTA);
        }
    }

    @Test public void testGetRangeStringsTESTHappyFlow() throws Exception {
        // Setup fixture
        // Exercise SUT
        List<String> actual = CustomCollectionUtils.getRangeStrings(2.65, 2.72, 0.01);
        // Verify outcome
        assertEquals(CustomCollectionUtils.getAsList("2.65", "2.66", "2.67", "2.68", "2.69", "2.70", "2.71", "2.72"), actual);
    }

    @Test public void testUniqueListTESTHappyFlow() throws Exception {
        // Setup fixture
        // Exercise SUT
        Set<String> actual = CustomCollectionUtils.uniqueList(CustomCollectionUtils.getAsList("2.65", "2.66", "2.68", "2.68", "2.69", "2.71", "2.71", "2.72"), new ObjectProvider<String, String>() {

            @Override
            public String getObject(String t) {
                return t.substring(2);
            }
        });
        // Verify outcome
        assertEquals(6, actual.size());
        assertEquals(CustomCollectionUtils.getAsSet("65", "66", "68", "69", "71", "72"), actual);
    }

    @Test public void testCollectObjectsTESTHappyFlow() throws Exception {
        // Setup fixture
        // Exercise SUT
        List input = CustomCollectionUtils.getAsList("2.65", "2.66", "2.68", "2.68", "2.69", "2.71", "2.71", "2.72");
        List<String> actual = CustomCollectionUtils.collectObjects(input, new ObjectProvider<String, String>() {

            @Override
            public String getObject(String t) {
                return t.substring(2);
            }
        });
        // Verify outcome
        assertEquals(input.size(), actual.size());
        assertEquals(CustomCollectionUtils.getAsList("65", "66", "68", "68", "69", "71", "71", "72"), actual);
    }

    @Test public void testGroupObjectsTESTHappyFlow() throws Exception {
        // Setup fixture
        List<String> input = CustomCollectionUtils.getAsList("2.66", "2.66", "2.68", "2.68", "2.69", "2.71", "2.71", "2.71");
        // Exercise SUT
        Map<String, List<String>> actual = CustomCollectionUtils.groupObjects(input, new ObjectProvider<String, String>() {

            @Override
            public String getObject(String t) {
                return t.substring(1);
            }
        });
        // Verify outcome
        assertEquals(4, actual.size());
        assertEquals(CustomCollectionUtils.getAsList("2.66", "2.66"), actual.get(".66"));
        assertEquals(CustomCollectionUtils.getAsList("2.68", "2.68"), actual.get(".68"));
        assertEquals(CustomCollectionUtils.getAsList("2.69"), actual.get(".69"));
        assertEquals(CustomCollectionUtils.getAsList("2.71", "2.71", "2.71"), actual.get(".71"));
    }

    @Test public void testSelectTESTHappyFlow() throws Exception {
        // Setup fixture
        List<String> input = CustomCollectionUtils.getAsList("2.66", "5.71", "2.98", "3.69", "4.61", "2.76", "2.88", "6.61");
        // Exercise SUT
        List<String> actual = CustomCollectionUtils.select(input, new ObjectSelector<String>() {

            @Override
            public boolean isValid(String t) {
                return t.endsWith("1");
            }
        });
        // Verify outcome
        assertEquals(CustomCollectionUtils.getAsList("5.71", "4.61", "6.61"), actual);
    }

    @Test public void testMatchTESTHappyFlow() throws Exception {
        // Setup fixture
        List<String> input = CustomCollectionUtils.getAsList("2.66", "5.71", "2.98", "3.69", "4.61", "2.76", "2.88", "6.61");
        // Exercise SUT
        String actual = CustomCollectionUtils.match(input, new ObjectSelector<String>() {

            @Override
            public boolean isValid(String t) {
                return t.equals("2.76");
            }
        });
        // Verify outcome
        assertEquals("2.76", actual);
    }

    @Test public void testIsValidIndexAndNotFirstElementTESTHappyFlow() throws Exception {
        // Setup fixture
        List<Integer> list = CustomCollectionUtils.getAsList(2, 5, 6, 8, 9, 3, 1);
        // Exercise SUT
        // Verify outcome
        assertTrue(CustomCollectionUtils.isValidIndexAndNotFirstElement(list, 3));
        assertFalse(CustomCollectionUtils.isValidIndexAndNotFirstElement(list, 0));
        assertTrue(CustomCollectionUtils.isValidIndexAndNotFirstElement(list, list.size()));
    }

    @Test public void testIsValidIndexAndNotLastElementTESTHappyFlow() throws Exception {
        // Setup fixture
        List<Integer> list = CustomCollectionUtils.getAsList(2, 5, 6, 8, 9, 3, 1);
        // Exercise SUT
        // Verify outcome
        assertTrue(CustomCollectionUtils.isValidIndexAndNotLastElement(list, 3));
        assertTrue(CustomCollectionUtils.isValidIndexAndNotLastElement(list, 0));
        assertFalse(CustomCollectionUtils.isValidIndexAndNotLastElement(list, list.size()));
    }

    @Test public void testExcludeLastItemTESTHappyFlow() throws Exception {
        // Setup fixture
        // Exercise SUT
        // Verify outcome
        assertEquals(CustomCollectionUtils.getAsList("String1"), CustomCollectionUtils.excludeLastItem(CustomCollectionUtils.getAsList("String1", "String2")));
        assertEquals(Collections.EMPTY_LIST, CustomCollectionUtils.excludeLastItem(CustomCollectionUtils.getAsList("String1")));
        assertEquals(Collections.EMPTY_LIST, CustomCollectionUtils.excludeLastItem(Collections.EMPTY_LIST));
        assertEquals(Collections.EMPTY_LIST, CustomCollectionUtils.excludeLastItem(null));
        assertEquals(CustomCollectionUtils.getAsList("String1", "String2", "String3"), CustomCollectionUtils.excludeLastItem(CustomCollectionUtils.getAsList("String1", "String2", "String3", "String4")));
    }

    @Test public void testCreateIndicesTESTHappyFlow() throws Exception {
        // Setup fixture
        List<String> input = CustomCollectionUtils.getAsList("abc", "bcd", "cde", "def", "fgh", "ghi", "hij", "ijk", "jkl", "klm", "lmn", "mno", "nop");
        int listHeight = 10;
        // Exercise SUT
        List<String> actual = CustomCollectionUtils.createIndices(input, listHeight);
        // Verify outcome
        int i = 0;
        assertEquals(listHeight, actual.size());
        assertEquals("a", actual.get(i++));
        assertEquals("b", actual.get(i++));
        assertEquals("c", actual.get(i++));
        assertEquals("d", actual.get(i++));
        assertEquals("g", actual.get(i++));
        assertEquals("h", actual.get(i++));
        assertEquals("i", actual.get(i++));
        assertEquals("k", actual.get(i++));
        assertEquals("l", actual.get(i++));
        assertEquals("m", actual.get(i++));
    }

    @Test public void testCreateIndicesTESTWithOtherPattern() throws Exception {
        // Setup fixture
        List<String> input = CustomCollectionUtils.getAsList("abc", "bcd", "bcde", "bdef", "bfgh", "bghi", "bhij", "bijk", "bjkl", "klm", "lmn", "mno", "nop");
        int listHeight = 10;
        // Exercise SUT
        List<String> actual = CustomCollectionUtils.createIndices(input, listHeight);
        // Verify outcome
        int i = 0;
        assertEquals(listHeight, actual.size());
        assertEquals("a", actual.get(i++));
        assertEquals("b", actual.get(i++));
        assertEquals("bc", actual.get(i++));
        assertEquals("bd", actual.get(i++));
        assertEquals("bg", actual.get(i++));
        assertEquals("bh", actual.get(i++));
        assertEquals("bi", actual.get(i++));
        assertEquals("k", actual.get(i++));
        assertEquals("l", actual.get(i++));
        assertEquals("m", actual.get(i++));
    }

    @Test public void testCreateIndicesTESTListSizeAsFive() throws Exception {
        // Setup fixture
        List<String> input = CustomCollectionUtils.getAsList("abc", "bcd", "bcde", "bdef", "bfgh", "bghi", "bhij", "bijk", "bhjkl", "klm", "lmn", "mno", "nop");
        int listHeight = 5;
        // Exercise SUT
        List<String> actual = CustomCollectionUtils.createIndices(input, listHeight);
        // Verify outcome
        int i = 0;
        assertEquals(listHeight, actual.size());
        assertEquals("a", actual.get(i++));
        assertEquals("b", actual.get(i++));
        assertEquals("bg", actual.get(i++));
        assertEquals("bi", actual.get(i++));
        assertEquals("l", actual.get(i++));
    }

    @Test public void testCreateIndicesTESTListSizeFiveAndWithAnotherPattern() throws Exception {
        // Setup fixture
        List<String> input = CustomCollectionUtils.getAsList("abc", "abcd", "abc", "abf", "g");
        int listHeight = 5;
        // Exercise SUT
        List<String> actual = CustomCollectionUtils.createIndices(input, listHeight);
        // Verify outcome
        int i = 0;
        assertEquals("a", actual.get(i++));
        assertEquals("ab", actual.get(i++));
        assertEquals("abc", actual.get(i++));
        assertEquals("abf", actual.get(i++));
        assertEquals("g", actual.get(i++));
    }

    @Test public void testGetMatchedElementForMaxIndexInDescendingOrderTESTHappyFlow() throws Exception {
        // Setup fixture
        List<String> list = new ArrayList<String>();
        list.add("first");
        list.add("second");
        list.add("third");
        list.add("fourth");
        // Exercise SUT
        String actual = CustomCollectionUtils.getMatchedElementForMaxIndexInDescendingOrder(list, 3);
        // Verify outcome
        assertEquals("fourth", actual);
    }

    @Test public void testGetMatchedElementForMaxIndexInDescendingOrderTESTWithIndexLessThanListSize() throws Exception {
        // Setup fixture
        List<String> list = new ArrayList<String>();
        list.add("first");
        list.add("second");
        list.add("third");
        list.add("fourth");
        // Exercise SUT
        String actual = CustomCollectionUtils.getMatchedElementForMaxIndexInDescendingOrder(list, 1);
        // Verify outcome
        assertEquals("second", actual);
    }

    @Test
    public void testGetMatchedElementForMaxIndexInDescendingOrderTESTWithEmptyList() throws Exception {
        // Setup fixture
        // Exercise SUT
        String actual = (String) CustomCollectionUtils.getMatchedElementForMaxIndexInDescendingOrder(Collections.EMPTY_LIST, 4);
        // Verify outcome
        assertEquals(null, actual);
    }

    @Test public void testGetMatchedElementForMaxIndexInDescendingOrderTESTWithListAsNull() throws Exception {
        // Setup fixture
        // Exercise SUT
        String actual = CustomCollectionUtils.getMatchedElementForMaxIndexInDescendingOrder(null, 4);
        // Verify outcome
        assertEquals(null, actual);
    }

    @Test public void testGetMatchedElementForMaxIndexInDescendingOrderTESTWithIndexGreaterThanTheListSize() throws Exception {
        // Setup fixture
        List<String> list = new ArrayList<String>();
        list.add("first");
        list.add("second");
        // Exercise SUT
        String actual = CustomCollectionUtils.getMatchedElementForMaxIndexInDescendingOrder(list, 10);
        // Verify outcome
        assertEquals("second", actual);
    }

    @Test public void testGetMatchedElementForMaxIndexInDescendingOrderTESTWithInvalidIndex() throws Exception {
        // Setup fixture
        int invalidIndex = -3;
        List<String> list = new ArrayList<String>();
        list.add("first");
        list.add("second");
        list.add("third");
        list.add("fourth");
        // Exercise SUT
        String actual = CustomCollectionUtils.getMatchedElementForMaxIndexInDescendingOrder(list, invalidIndex);
        // Verify outcome
        assertEquals(null, actual);
    }

    @Test public void testIsSortedTESTHappyFlow() throws Exception {
        // Setup fixture
        // Exercise SUT
        // Verify outcome
        assertTrue(CustomCollectionUtils.isSorted(CustomCollectionUtils.getAsList("A", "B", "C", "D")));
        assertFalse(CustomCollectionUtils.isSorted(CustomCollectionUtils.getAsList("A", "C", "B", "D")));
    }

    @Test public void testGetItemBeforeTESTHappyFlow() throws Exception {
        // Setup fixture
        List list = CustomCollectionUtils.getAsList("A", "B", "C", "D");
        // Exercise SUT
        // Verify outcome
        assertEquals("B", CustomCollectionUtils.getItemBefore(list, "C"));
        assertEquals("C", CustomCollectionUtils.getItemBefore(list, "D"));
        assertEquals(null, CustomCollectionUtils.getItemBefore(list, "A"));
        assertEquals(null, CustomCollectionUtils.getItemBefore(list, "E"));
    }

    @Test public void testGetItemAfterTESTHappyFlow() throws Exception {
        // Setup fixture
        List list = CustomCollectionUtils.getAsList("A", "B", "C", "D");
        // Exercise SUT
        // Verify outcome
        assertEquals("D", CustomCollectionUtils.getItemAfter(list, "C"));
        assertEquals(null, CustomCollectionUtils.getItemAfter(list, "D"));
        assertEquals("B", CustomCollectionUtils.getItemAfter(list, "A"));
        assertEquals(null, CustomCollectionUtils.getItemAfter(list, "E"));
    }

    @Test public void testGetNotNullValuesTESTHappyFlow() throws Exception {
        // Setup fixture
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Key1", "Value1");
        map.put("Key2", "Value2");
        map.put("Key3", "Value3");
        map.put("Key4", "Value4");
        // Exercise SUT
        // Verify outcome
        assertEquals(CustomCollectionUtils.getAsList("Value1", "Value3", "Value4"), CustomCollectionUtils.getNotNullValues(CustomCollectionUtils.getAsList("Key1", "Key3", "Key4"), map));
        assertEquals(Collections.EMPTY_LIST, CustomCollectionUtils.getNotNullValues(CustomCollectionUtils.getAsList("Invalid_Key1", "Key2_Invalid"), map));
        assertEquals(CustomCollectionUtils.getAsList("Value2", "Value3", "Value1"), CustomCollectionUtils.getNotNullValues(CustomCollectionUtils.getAsList("Key2", "Key3", "Key1"), map));
        assertEquals(CustomCollectionUtils.getAsList("Value2", "Value3", "Value1"), CustomCollectionUtils.getNotNullValues(CustomCollectionUtils.getAsList("Key_Invalid_6", "Key2", "Key3", "Key_Invalid_4", "Key1"), map));
    }
}

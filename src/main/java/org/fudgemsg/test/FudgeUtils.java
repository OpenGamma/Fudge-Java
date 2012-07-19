/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fudgemsg.test;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.UnknownFudgeFieldValue;
import org.fudgemsg.mapping.BuilderUtil;

import java.util.*;

/**
 * Utilities for testing.
 */
public class FudgeUtils {

  /**
   * @param expectedMsg [documentation not available]
   * @param actualMsg   [documentation not available]
   */
  public static void assertAllFieldsMatch(FudgeMsg expectedMsg, FudgeMsg actualMsg) {
    assertAllFieldsMatch(expectedMsg, actualMsg, true);
  }

  /**
   * @param expectedMsg       [documentation not available]
   * @param actualMsg         [documentation not available]
   * @param fieldOrderMatters [documentation not available]
   */
  public static void assertAllFieldsMatch(FudgeMsg expectedMsg, FudgeMsg actualMsg,
                                          boolean fieldOrderMatters) {
    List<FudgeField> expectedFields = expectedMsg.getAllFields();
    List<FudgeField> actualFields = actualMsg.getAllFields();
    if (!fieldOrderMatters) {
      expectedFields = order(expectedFields);
      actualFields = order(actualFields);
    }
    Iterator<FudgeField> expectedIter = expectedFields.iterator();
    Iterator<FudgeField> actualIter = actualFields.iterator();
    FudgeField expectedField;
    FudgeField actualField;
    while (expectedIter.hasNext()) {
      expectedField = expectedIter.next();
      // let's skip negative ordinals
      while (expectedField.getOrdinal() != null && expectedField.getOrdinal() < 0){
        expectedField = expectedIter.next();
      }
      assertTrue(actualIter.hasNext());
      actualField = actualIter.next();
      // let's skip negative ordinals
      while (actualField.getOrdinal() != null && actualField.getOrdinal() < 0){
        assertTrue(actualIter.hasNext());
        actualField = actualIter.next();
      }
      assertEquals(expectedField.getName(), actualField.getName());
      assertEquals(expectedField.getType(), actualField.getType());
      assertEquals(expectedField.getOrdinal(), actualField.getOrdinal());
      if (expectedField.getValue().getClass().isArray()) {
        assertEquals(expectedField.getValue().getClass(), actualField.getValue().getClass());
        if (expectedField.getValue() instanceof byte[]) {
          FudgeUtils.assertArraysMatch((byte[]) expectedField.getValue(), (byte[]) actualField.getValue());
        } else if (expectedField.getValue() instanceof short[]) {
          FudgeUtils.assertArraysMatch((short[]) expectedField.getValue(), (short[]) actualField.getValue());
        } else if (expectedField.getValue() instanceof int[]) {
          FudgeUtils.assertArraysMatch((int[]) expectedField.getValue(), (int[]) actualField.getValue());
        } else if (expectedField.getValue() instanceof long[]) {
          FudgeUtils.assertArraysMatch((long[]) expectedField.getValue(), (long[]) actualField.getValue());
        } else if (expectedField.getValue() instanceof float[]) {
          FudgeUtils.assertArraysMatch((float[]) expectedField.getValue(), (float[]) actualField.getValue());
        } else if (expectedField.getValue() instanceof double[]) {
          FudgeUtils.assertArraysMatch((double[]) expectedField.getValue(), (double[]) actualField.getValue());
        }
      } else if (expectedField.getValue() instanceof FudgeMsg) {
        assertTrue(actualField.getValue() instanceof FudgeMsg);
        assertAllFieldsMatch((FudgeMsg) expectedField.getValue(), (FudgeMsg) actualField.getValue(), fieldOrderMatters);
      } else if (expectedField.getValue() instanceof UnknownFudgeFieldValue) {
        assertTrue(actualField.getValue() instanceof UnknownFudgeFieldValue);
        UnknownFudgeFieldValue expectedValue = (UnknownFudgeFieldValue) expectedField.getValue();
        UnknownFudgeFieldValue actualValue = (UnknownFudgeFieldValue) actualField.getValue();
        assertEquals(expectedField.getType().getTypeId(), actualField.getType().getTypeId());
        assertEquals(expectedValue.getType().getTypeId(), actualField.getType().getTypeId());
        FudgeUtils.assertArraysMatch(expectedValue.getContents(), actualValue.getContents());
      } else {
        assertEquals(expectedField.getValue(), actualField.getValue());
      }
    }
    if (actualIter.hasNext()) {
      FudgeField fudgeField = actualIter.next();
      assertFalse(fudgeField.getOrdinal() != BuilderUtil.KEY_TYPE_HINT_ORDINAL && fudgeField.getOrdinal() != BuilderUtil.VALUE_TYPE_HINT_ORDINAL);
    }

  }

  private static List<FudgeField> order(List<FudgeField> expectedFields) {
    expectedFields = new ArrayList<FudgeField>(expectedFields);
    Collections.sort(expectedFields, new Comparator<FudgeField>() {
      @Override
      public int compare(FudgeField o1, FudgeField o2) {
        if ((o1.getOrdinal() != null) || (o2.getOrdinal() != null)) {
          if (o1.getOrdinal() == null) {
            return -1;
          } else if (o2.getOrdinal() == null) {
            return 1;
          } else {
            int comparison = (o1.getOrdinal() - o2.getOrdinal());
            if (comparison != 0) {
              return comparison;
            }
          }
        }
        if ((o1.getName() != null) || (o2.getName() != null)) {
          if (o1.getName() == null) {
            return -1;
          } else if (o2.getName() == null) {
            return 1;
          } else {
            int comparison = o1.getName().compareTo(o2.getName());
            if (comparison != 0) {
              return comparison;
            }
          }
        }
        return 0;
      }
    });
    return expectedFields;
  }

  /**
   * @param expected the expected data
   * @param actual   the actual data
   */
  public static void assertArraysMatch(double[] expected, double[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      // No tolerance intentionally.
      assertEquals(expected[i], actual[i], 0.0);
    }
  }

  /**
   * @param expected the expected data
   * @param actual   the actual data
   */
  public static void assertArraysMatch(float[] expected, float[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      // No tolerance intentionally.
      assertEquals(expected[i], actual[i], 0.0f);
    }
  }

  /**
   * @param expected the expected data
   * @param actual   the actual data
   */
  public static void assertArraysMatch(long[] expected, long[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

  /**
   * @param expected the expected data
   * @param actual   the actual data
   */
  public static void assertArraysMatch(int[] expected, int[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

  /**
   * @param expected the expected data
   * @param actual   the actual data
   */
  public static void assertArraysMatch(short[] expected, short[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

  /**
   * @param expected the expected data
   * @param actual   the actual data
   */
  public static void assertArraysMatch(byte[] expected, byte[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

  // avoid JUnit being exposed
  //-------------------------------------------------------------------------
  private static void assertTrue(boolean actual) {
    if (actual == true) {
      return;
    }
    throw new AssertionError("Expected: true, Actual false");
  }

  private static void assertFalse(boolean actual) {
    if (actual == false) {
      return;
    }
    throw new AssertionError("Expected: false, Actual true");
  }

  private static void assertEquals(long expected, long actual) {
    if (actual == expected) {
      return;
    }
    throw new AssertionError("Expected: " + expected + ", Actual: " + actual);
  }

  private static void assertEquals(double expected, double actual, double diff) {
    if (Math.abs(actual - expected) <= diff) {
      return;
    }
    throw new AssertionError("Expected: " + expected + ", Actual: " + actual);
  }

  private static void assertEquals(float expected, float actual, float diff) {
    if (Math.abs(actual - expected) <= diff) {
      return;
    }
    throw new AssertionError("Expected: " + expected + ", Actual: " + actual);
  }

  private static void assertEquals(Object expected, Object actual) {
    if (actual == expected || (actual != null && actual.equals(expected))) {
      return;
    }
    throw new AssertionError("Expected: " + expected + ", Actual: " + actual);
  }

}

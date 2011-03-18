/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc. and other contributors.
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

package org.fudgemsg.util;

import java.util.Map;

/**
 * Contains utility methods for checking inputs to methods.
 */
public final class ArgumentChecker {
  
  /**
   * Restricted constructor.
   */
  private ArgumentChecker() {
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified boolean is true.
   * This will normally be the result of a caller-specific check.
   * 
   * @param trueIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message, not null
   * @throws IllegalArgumentException if the test value is false
   */
  public static void isTrue(boolean trueIfValid, String message) {
    if (trueIfValid == false) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Checks that the specified boolean is false.
   * This will normally be the result of a caller-specific check.
   * 
   * @param falseIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message, not null
   * @throws IllegalArgumentException if the test value is false
   */
  public static void isFalse(boolean falseIfValid, String message) {
    if (falseIfValid) {
      throw new IllegalArgumentException(message);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter is non-null.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   */
  public static void notNull(Object parameter, String name) {
    if (parameter == null) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be null");
    }
  }

  /**
   * Checks that the specified injected parameter is non-null.
   * As a convention, the name of the parameter should be the exact name that you would
   * provide in a Spring configuration file.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   */
  public static void notNullInjected(Object parameter, String name) {
    if (parameter == null) {
      throw new IllegalArgumentException("Injected input parameter '" + name + "' must not be null");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter is non-null and not empty.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   * @throws IllegalArgumentException if the input is empty
   */
  public static void notEmpty(String parameter, String name) {
    notNull(parameter, name);
    if (parameter.length() == 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be zero length");
    }
  }

  /**
   * Checks that the specified parameter array is non-null and not empty.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   * @throws IllegalArgumentException if the input is empty
   */
  public static void notEmpty(Object[] parameter, String name) {
    notNull(parameter, name);
    if (parameter.length == 0) {
      throw new IllegalArgumentException("Input parameter array '" + name + "' must not be zero length");
    }
  }

  /**
   * Checks that the specified parameter array is non-null and not empty.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   * @throws IllegalArgumentException if the input is empty
   */
  public static void notEmpty(Object[][] parameter, String name) {
    notNull(parameter, name);
    if (parameter.length == 0) {
      throw new IllegalArgumentException("Input parameter array '" + name + "' must not be zero length");
    }
  }

  /**
   * Checks that the specified parameter array is non-null and not empty.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   * @throws IllegalArgumentException if the input is empty
   */
  public static void notEmpty(double[] parameter, String name) {
    notNull(parameter, name);
    if (parameter.length == 0) {
      throw new IllegalArgumentException("Input parameter array '" + name + "' must not be zero length");
    }
  }

  /**
   * Checks that the specified parameter array is non-null and not empty.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   * @throws IllegalArgumentException if the input is empty
   */
  public static void notEmpty(long[] parameter, String name) {
    notNull(parameter, name);
    if (parameter.length == 0) {
      throw new IllegalArgumentException("Input parameter array '" + name + "' must not be zero length");
    }
  }

  /**
   * Checks that the specified parameter collection is non-null and not empty.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   * @throws IllegalArgumentException if the input is empty
   */
  public static void notEmpty(Iterable<?> parameter, String name) {
    notNull(parameter, name);
    if (parameter.iterator().hasNext() == false) {
      throw new IllegalArgumentException("Input parameter collection '" + name + "' must not be zero length");
    }
  }

  /**
   * Checks that the specified parameter map is non-null and not empty.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   * @throws IllegalArgumentException if the input is empty
   */
  public static void notEmpty(Map<?, ?> parameter, String name) {
    notNull(parameter, name);
    if (parameter.size() == 0) {
      throw new IllegalArgumentException("Input parameter map '" + name + "' must not be zero length");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter array is non-null and contains no nulls.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or contains nulls
   */
  public static void noNulls(Object[] parameter, String name) {
    notNull(parameter, name);
    for (int i = 0; i < parameter.length; i++) {
      if (parameter[i] == null) {
        throw new IllegalArgumentException("Input parameter array '" + name + "' must not contain null at index " + i);
      }
    }
  }

  /**
   * Checks that the specified parameter collection is non-null and contains no nulls.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or contains nulls
   */
  public static void noNulls(Iterable<?> parameter, String name) {
    notNull(parameter, name);
    for (Object obj : parameter) {
      if (obj == null) {
        throw new IllegalArgumentException("Input parameter collection '" + name + "' must not contain null");
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the argument is not negative.
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative
   */
  public static void notNegative(int parameter, String name) {
    if (parameter < 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative");
    }
  }

  /**
   * Checks that the argument is not negative.
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative
   */
  public static void notNegative(double parameter, String name) {
    if (parameter < 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative");
    }
  }

  /**
   * Checks that the argument is not negative or zero.
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative or zero
   */
  public static void notNegativeOrZero(int parameter, String name) {
    if (parameter <= 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative or zero");
    }
  }

  /**
   * Checks that the argument is not negative or zero.
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative or zero
   */
  public static void notNegativeOrZero(double parameter, String name) {
    if (parameter <= 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative or zero");
    }
  }

  /**
   * Checks a collection for null elements.
   * 
   * @param iterable  the collection to test, not null
   * @return true if the collection contains a null element
   * @throws IllegalArgumentException if the collection is null
   */
  public static boolean hasNullElement(Iterable<?> iterable) {
    notNull(iterable, "collection");
    for (Object o : iterable) {
      if (o == null) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks a collection of doubles for negative elements.
   * 
   * @param iterable  the collection to test, not null
   * @return true if the collection contains a negative element
   * @throws IllegalArgumentException if the collection is null
   */

  public static boolean hasNegativeElement(Iterable<Double> iterable) {
    notNull(iterable, "collection");
    for (Double d : iterable) {
      if (d < 0) {
        return true;
      }
    }
    return false;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that a value is within the range low < x < high.
   * 
   * @param low Low value of the range
   * @param high High value of the range
   * @param x  the value
   * @return true if low < x < high
   */
  public static boolean isInRangeExclusive(double low, double high, double x) {
    if (x > low && x < high) {
      return true;
    }
    return false;
  }

  /**
   * Checks that a value is within the range low <= x <= high.
   * 
   * @param low  the low value of the range
   * @param high  the high value of the range
   * @param x  the value
   * @return true if low <= x <= high
   */
  public static boolean isInRangeInclusive(double low, double high, double x) {
    if (x >= low && x <= high) {
      return true;
    }
    return false;
  }

  /**
   * Checks that a value is within the range low < x <= high.
   * 
   * @param low  the low value of the range
   * @param high  the high value of the range
   * @param x  the value
   * @return true if low < x <= high
   */

  public static boolean isInRangeExcludingLow(double low, double high, double x) {
    if (x > low && x <= high) {
      return true;
    }
    return false;
  }

  /**
   * Checks that a value is within the range low <= x < high.
   * 
   * @param low  the low value of the range
   * @param high  the high value of the range
   * @param x  the value
   * @return true if low <= x < high
   */
  public static boolean isInRangeExcludingHigh(double low, double high, double x) {
    if (x >= low && x < high) {
      return true;
    }
    return false;
  }



}

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
package org.fudgemsg.types;

/**
 * Granularity options for date and time types.
 */
public enum DateTimeAccuracy {

  /**
   * Millenia precision.
   */
  MILLENIUM(0),
  /**
   * Century precision.
   */
  CENTURY(1),
  /**
   * Year precision.
   */
  YEAR(2),
  /**
   * Month precision. 
   */
  MONTH(3),
  /**
   * Day precision. 
   */
  DAY(4),
  /**
   * Hour precision. 
   */
  HOUR(5),
  /**
   * Minute precision.
   */
  MINUTE(6),
  /**
   * Second precision.
   */
  SECOND(7),
  /**
   * Millisecond precision.
   */
  MILLISECOND(8),
  /**
   * Microsecond precision.
   */
  MICROSECOND(9),
  /**
   * Nanosecond precision.
   */
  NANOSECOND(10);

  /**
   * The accuracy type code.
   */
  private final int _encodedValue;

  /**
   * Restricted constructor.
   * @param encodedValue
   */
  private DateTimeAccuracy(final int encodedValue) {
    _encodedValue = encodedValue;
  }

  /**
   * Converts the enum to the Fudge wire value for date and time.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>
   * 
   * @return the numeric value
   */
  public final int getEncodedValue() {
    return _encodedValue;
  }

  /**
   * Converts the Fudge wire value to the enum for date and time.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>
   * 
   * @param value  numeric value
   * @return the {@link DateTimeAccuracy}, {@code null} if the value is invalid
   */
  public final static DateTimeAccuracy fromEncodedValue(int value) {
    switch (value) {
      case 10 : return NANOSECOND;
      case 9 : return MICROSECOND;
      case 8 : return MILLISECOND;
      case 7 : return SECOND;
      case 6 : return MINUTE;
      case 5 : return HOUR;
      case 4 : return DAY;
      case 3 : return MONTH;
      case 2 : return YEAR;
      case 1 : return CENTURY;
      case 0 : return MILLENIUM;
      default : return null;
    }
  }

  /**
   * Tests if this accuracy is a greater precision than another.
   * For example, SECOND precision is greater than MINUTE precision.
   * 
   * @param accuracy  the other accuracy, not null
   * @return {@code true} if greater, {@code false} otherwise
   */
  public boolean greaterThan(final DateTimeAccuracy accuracy) {
    return getEncodedValue() > accuracy.getEncodedValue();
  }

  /**
   * Tests is this accuracy is a lower precision than another
   * For example, MINUTE precision is less than SECOND precision.
   * 
   * @param accuracy  the other accuracy, not null
   * @return {@code true} if lower, {@code false} otherwise.
   */
  public boolean lessThan(final DateTimeAccuracy accuracy) {
    return getEncodedValue() < accuracy.getEncodedValue();
  }

}

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

import static org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH;
import static org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR;
import static org.threeten.bp.temporal.ChronoField.YEAR;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.threeten.bp.DateTimeException;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Year;
import org.threeten.bp.YearMonth;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * A date at varying precision.
 * <p>
 * This is the low level Fudge representation for a date.
 * Dates can be more easily used through the secondary type mechanism.
 * <p>
 * For more details, please refer to <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>.
 */
public class FudgeDate {

  /**
   * The maximum year.
   */
  public static final int MAX_YEAR = 4_194_303;  // 23 bits max
  /**
   * The maximum year.
   */
  public static final int MIN_YEAR = -4_194_304;  // 23 bits min
  /**
   * The maximum value.
   */
  public static final FudgeDate MAX = new FudgeDate(MAX_YEAR, 15, 31);  // year = Integer.MAX_VALUE >> 9
  /**
   * The minimum value.
   */
  public static final FudgeDate MIN = new FudgeDate(MIN_YEAR, 15, 31);  // year = Integer.MIN_VALUE >> 9

  /**
   * The year, using negative for BCE and positive for CE, year zero invalid.
   */
  private final int _year;
  /**
   * The month.
   */
  private final int _month;
  /**
   * The day.
   */
  private final int _day;

  //-------------------------------------------------------------------------
  /**
   * Obtains a {@link FudgeDate} object representing just a year.
   * 
   * @param year  the year, using negative for BCE and positive for CE,
   *  year zero invalid, from -999,999 to 999,999
   * @throws RuntimeException if the year is invalid
   * @return the date, not null
   */
  public static FudgeDate ofYear(int year) {
    return FudgeDate.of(year, 0, 0);
  }

  /**
   * Obtains a {@link FudgeDate} object representing a year and a month.
   * 
   * @param year  the year, using negative for BCE and positive for CE,
   *  year zero invalid, from -999,999 to 999,999
   * @param month  the month, from 1 to 31, or 0 if not set
   * @return the date, not null
   * @throws RuntimeException if the year or month is invalid
   */
  public static FudgeDate ofYearMonth(int year, final int month) {
    return FudgeDate.of(year, month, 0);
  }

  /**
   * Obtains a {@link FudgeDate} object.
   * 
   * @param year  the year, using negative for BCE and positive for CE,
   *  year zero invalid, from -999,999 to 999,999
   * @param month  the month, from 1 to 12, or 0 if not set
   * @param day  the day, from 1 to 31, or 0 if not set
   * @return the date, not null
   * @throws RuntimeException if the year, month or day is invalid
   */
  public static FudgeDate of(int year, int month, int day) {
    if (year == 0) {
      throw new IllegalArgumentException("Year zero is not allowed, -1 is 1BCE");
    }
    if (year < MIN_YEAR) {
      throw new IllegalArgumentException("Year too small, minimum is " + MIN_YEAR);
    }
    if (year > MAX_YEAR) {
      throw new IllegalArgumentException("Year too large, minimum is " + MAX_YEAR);
    }
    if (month == 0) {
      if (day != 0) {
        throw new IllegalArgumentException("Cannot specify day without month");
      }
    } else {
      if (day != 0) {
        LocalDate.of(year, month, day);  // validation
      } else {
        MONTH_OF_YEAR.checkValidIntValue(month);
      }
    }
    return new FudgeDate(year, month, day);
  }

  /**
   * Obtains a {@link FudgeDate} object from a message.
   * <p>
   * This is intended for use when reading a message.
   * 
   * @param message  the Fudge encoded message
   * @return the date, not null
   */
  public static FudgeDate ofMessage(int message) {
    final int dayOfMonth = (message & 31);
    final int monthOfYear = (message >> 5) & 15;
    final int year = message >> 9; // will sign-extend
    return new FudgeDate(year, monthOfYear, dayOfMonth);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a {@link FudgeDate} object.
   * 
   * @param date  the {@link Calendar} object supplying the year, month and day
   * @return the date, not null
   */
  public static FudgeDate from(Calendar date) {
    return FudgeDate.of(date.get(Calendar.ERA) == GregorianCalendar.BC ? -date.get(Calendar.YEAR) : date.get(Calendar.YEAR),
        date.isSet(Calendar.MONTH) ? (date.get(Calendar.MONTH) + 1) : 0,
        date.isSet(Calendar.DAY_OF_MONTH) ? date.get(Calendar.DAY_OF_MONTH) : 0);
  }

  /**
   * Obtains a {@link FudgeDate} object.
   * <p>
   * The temporal object will be queried for the year, month and day fields.
   * The accuracy will be set based on the available fields.
   * Thus a {@code YearMonth} instance will have month accuracy.
   * 
   * @param temporal  the temporal object to create a date from, not null
   * @return the date, not null
   * @throws DateTimeException if unable to convert
   */
  public static FudgeDate from(TemporalAccessor temporal) {
    int year = temporal.get(YEAR);
    if (year <= 0) {
      year--;
    }
    int month = 0;
    int day = 0;
    if (temporal.isSupported(MONTH_OF_YEAR)) {
      month = temporal.get(MONTH_OF_YEAR);
      if (temporal.isSupported(DAY_OF_MONTH)) {
        day = temporal.get(DAY_OF_MONTH);
      }
    }
    if (year == Year.MAX_VALUE && month == 12 && day == 31) {
      return MAX;
    }
    if (year == (Year.MIN_VALUE - 1) && month == 1 && day == 1) {
      return MIN;
    }
    return FudgeDate.of(year, month, day);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a new {@link FudgeDate}.
   * <p>
   * No validation is performed for accurately storing the content of messages.
   * 
   * @param year  the year
   * @param month  the month
   * @param day  the day
   */
  protected FudgeDate(int year, int month, int day) {
    _year = year;
    _month = month;
    _day = day;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the ISO year.
   * This uses negative for BCE and positive for CE, counting year zero as the
   * first year of the previous era.
   * 
   * @return the ISO year
   */
  public int getYearISO() {
    return (_year >= 0 ? _year : _year + 1);
  }

  /**
   * Gets the stored year.
   * The year uses negative for BCE and positive for CE, year zero invalid.
   * If the non-validated constructor is used (such as reading a message) then
   * the result will not be validated.
   * 
   * @return the year
   */
  public int getYear() {
    return _year;
  }

  /**
   * Gets the stored month-of-year.
   * <p>
   * Normally returns 1 to 12.
   * Returns 0 if the date just represents a year.
   * Returns 15 if is a maximum or minimum date.
   * Can return 13, 14 or 15 if an invalid message is read.
   * 
   * @return month-of-year
   */
  public int getMonthOfYear() {
    return _month;
  }

  /**
   * Gets the stored day-of-month.
   * This returns 0 if the date just represents a year or year-month.
   * The maximum and minimum dates returns 31.
   * If the non-validated constructor is used (such as reading a message) then
   * the result will not be validated.
   * 
   * @return the day-of-month
   */
  public int getDayOfMonth() {
    return _day;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the accuracy of the date.
   * 
   * @return the accuracy, not null
   */
  public DateTimeAccuracy getAccuracy() {
    if (getDayOfMonth() == 0) {
      if (getMonthOfYear() == 0) {
        return DateTimeAccuracy.YEAR;
      } else {
        return DateTimeAccuracy.MONTH;
      }
    } else {
      return DateTimeAccuracy.DAY;
    }
  }

  /**
   * Converts this date to a {@code LocalDate}, using the first day-of-month and
   * first month-of-year if the fields are not set.
   * 
   * @return a {@code LocalDate} roughly equivalent to this date, not null
   * @throws DateTimeException if the year, month or day are invalid
   */
  public LocalDate toLocalDate() {
    // lenient
    if (_month == 15) {
      if (_year < 0) {
        return LocalDate.MIN;
      } else {
        return LocalDate.MAX;
      }
    }
    int year = getYearISO();
    int month = Math.min(Math.max(_month, 1), 12);
    int day = Math.min(Math.max(_day, 1), 31);
    day = Math.min(day, YearMonth.of(year, month).lengthOfMonth());
    return LocalDate.of(year, month, day);
  }

  /**
   * Converts this date to a Fudge message {@code int}.
   * 
   * @return the Fudge int
   */
  public int toMessage() {
    return (_year << 9) | ((_month & 15) << 5) | (_day & 31);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this date equals another.
   * 
   * @param object  the object to compare to, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof FudgeDate) {
      final FudgeDate other = (FudgeDate) object;
      return other.getYear() == getYear() &&
            other.getMonthOfYear() == getMonthOfYear() &&
            other.getDayOfMonth() == getDayOfMonth();
    }
    return false;
  }

  /**
   * Returns a suitable hash code.
   * 
   * @return the hash code.
   */
  @Override
  public int hashCode() {
    return (getYear() * 17 + getMonthOfYear() + 1) * 17 + getDayOfMonth();
  }

  /**
   * Returns a string representation of the date.
   * 
   * @return the date as a string, not null
   */
  @Override
  public String toString() {
    return toLocalDate().toString();
  }

}

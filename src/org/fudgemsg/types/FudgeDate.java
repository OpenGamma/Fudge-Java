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

import java.util.Calendar;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.DateProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

/**
 * A date at varying precision.
 * <p>
 * This is the low level Fudge representation for a date.
 * Dates can be more easily used through the secondary type mechanism.
 * <p>
 * For more details, please refer to <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>.
 */
public class FudgeDate implements DateProvider {

  /**
   * The year.
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

  /**
   * Constructs a new {@link FudgeDate} object representing just a year.
   * 
   * @param year  the year
   */
  public FudgeDate(final int year) {
    this(year, 0, 0);
  }

  /**
   * Constructs a new {@link FudgeDate} object representing a year and a month.
   * 
   * @param year  the year
   * @param month  the month
   */
  public FudgeDate(final int year, final int month) {
    this(year, month, 0);
  }

  /**
   * Constructs a new {@link FudgeDate} object.
   * 
   * @param year  the year
   * @param month  the month
   * @param day  the day
   */
  public FudgeDate(final int year, final int month, final int day) {
    if (month < 0) {
      throw new IllegalArgumentException("month cannot be negative");
    }
    if (day < 0) {
      throw new IllegalArgumentException("day cannot be negative");
    }
    if ((month == 0) && (day > 0)) {
      throw new IllegalArgumentException("cannot specify day without month");
    }
    _year = year;
    _month = month;
    _day = day;
  }

  /**
   * Creates a new {@link FudgeDate} object.
   * 
   * @param date  the {@link Calendar} object supplying the year, month and day
   */
  public FudgeDate(final Calendar date) {
    this(date.get(Calendar.YEAR),
        date.isSet(Calendar.MONTH) ? (date.get(Calendar.MONTH) + 1) : 0,
        date.isSet(Calendar.DAY_OF_MONTH) ? date.get(Calendar.DAY_OF_MONTH) : 0);
  }

  /**
   * Creates a new {@link FudgeDate} object.
   * 
   * @param instant  the date corresponding to this instant at UTC will initialize the object
   */
  protected FudgeDate(final Instant instant) {
    this((DateProvider) ZonedDateTime.ofInstant(instant, TimeZone.UTC));
  }

  /**
   * Creates a new {@link FudgeDate} object.
   * 
   * @param localDate  the {@link LocalDate} representation of the date
   */
  protected FudgeDate(final LocalDate localDate) {
    this(localDate.getYear(), localDate.getMonthOfYear().getValue(), localDate.getDayOfMonth());
  }

  /**
   * Creates a new {@link FudgeDate} object.
   * 
   * @param instantProvider  the date corresponding to the {@link Instant} provided at UTC will initialize the object
   */
  public FudgeDate(final InstantProvider instantProvider) {
    this(instantProvider.toInstant());
  }

  /**
   * Creates a new {@link FudgeDate} object.
   * 
   * @param dateProvider  provides the {@link LocalDate} representation of the date
   */
  public FudgeDate(final DateProvider dateProvider) {
    this(dateProvider.toLocalDate());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the stored year.
   * 
   * @return the year
   */
  public int getYear() {
    return _year;
  }

  /**
   * Gets the month-of-year, or 0 if the date just represents a year
   * 
   * @return month-of-year
   */
  public int getMonthOfYear() {
    return _month;
  }

  /**
   * Gets the day of the month, or 0 if the date just represents a year or year/month.
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
   */
  @Override
  public LocalDate toLocalDate() {
    return LocalDate.of(
        getYear(),
        getMonthOfYear() == 0 ? 1 : getMonthOfYear(),
        getDayOfMonth() == 0 ? 1 : getDayOfMonth());
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

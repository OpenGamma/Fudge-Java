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
import javax.time.calendar.DateTimeProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.ZoneOffset;

/**
 * A date-time at varying precision.
 * <p>
 * This is the low level Fudge representation for a date-time.
 * Date-times can be more easily used through the secondary type mechanism.
 * <p>
 * For more details, please refer to <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>.
 */
public class FudgeDateTime implements DateTimeProvider, InstantProvider {

  /**
   * The Fudge date.
   */
  private final FudgeDate _date;
  /**
   * The Fudge time.
   */
  private final FudgeTime _time;

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param precision  the resolution of the representation
   * @param year  the year
   * @param month  the month
   * @param day  the day
   * @param timezoneOffset  the time-zone offset in 15 minute intervals 
   * @param seconds  the seconds since midnight
   * @param nanos  the nanoseconds within the second
   */
  public FudgeDateTime(
      final DateTimeAccuracy precision, final int year, final int month, final int day,
      final int timezoneOffset, final int seconds, final int nanos) {
    this(new FudgeDate(year, month, day), new FudgeTime(precision, timezoneOffset, seconds, nanos));
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param date  the date, not null
   * @param time  the time, not null
   */
  public FudgeDateTime(final FudgeDate date, final FudgeTime time) {
    if (date == null) {
      throw new NullPointerException("date cannot be null");
    }
    if (time == null) {
      throw new NullPointerException("time cannot be null");
    }
    if (date.getAccuracy().lessThan(DateTimeAccuracy.DAY)) {
      if (date.getAccuracy().lessThan(DateTimeAccuracy.MONTH)) {
        if (time.getAccuracy().greaterThan(DateTimeAccuracy.YEAR)) {
          throw new IllegalArgumentException(date.getAccuracy() + " date too low precision for " + time.getAccuracy() + " datetime");
        }
      } else {
        if (time.getAccuracy().greaterThan(DateTimeAccuracy.MONTH)) {
          throw new IllegalArgumentException(date.getAccuracy() + " date too low precision for " + time.getAccuracy() + " datetime");
        } else if (time.getAccuracy().lessThan(DateTimeAccuracy.MONTH)) {
          throw new IllegalArgumentException(date.getAccuracy() + " date too high precision for " + time.getAccuracy() + " datetime");
        }
      }
    }
    _date = date;
    _time = time;
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param accuracy  the resolution of the representation
   * @param instant  the time instant, converted to a date-time using UTC
   */
  protected FudgeDateTime(final DateTimeAccuracy accuracy, final Instant instant) {
    this(accuracy, OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param offsetDateTime  the date and time
   */
  public FudgeDateTime(final OffsetDateTime offsetDateTime) {
    this(DateTimeAccuracy.NANOSECOND, offsetDateTime);
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param accuracy  the resolution of the representation
   * @param offsetDateTime  the date and time
   */
  public FudgeDateTime(final DateTimeAccuracy accuracy, final OffsetDateTime offsetDateTime) {
    this(new FudgeDate(offsetDateTime.toOffsetDate()), new FudgeTime(accuracy, offsetDateTime.toOffsetTime()));
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param offsetDate  the date, midnight on this day will be used for the time
   */
  public FudgeDateTime(final OffsetDate offsetDate) {
    this(new FudgeDate(offsetDate), new FudgeTime(DateTimeAccuracy.DAY, offsetDate.atMidnight().toOffsetTime()));
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param localDateTime  the date, Midnight on this day will be used for the time
   */
  protected FudgeDateTime(final LocalDateTime localDateTime) {
    this(DateTimeAccuracy.NANOSECOND, localDateTime);
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param accuracy  the resolution of the representation 
   * @param localDateTime  the date and time
   */
  protected FudgeDateTime(final DateTimeAccuracy accuracy, final LocalDateTime localDateTime) {
    this(new FudgeDate(localDateTime), new FudgeTime(accuracy, localDateTime));
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param instantProvider  the provider of an instant - the date and time at UTC will be used  
   */
  public FudgeDateTime(final InstantProvider instantProvider) {
    this(DateTimeAccuracy.NANOSECOND, instantProvider);
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param accuracy  the resolution of the representation
   * @param instantProvider  the provider of an instant - the date and time at UTC will be used 
   */
  public FudgeDateTime(final DateTimeAccuracy accuracy, final InstantProvider instantProvider) {
    this(accuracy, instantProvider.toInstant());
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param dateTimeProvider  the provider of date and time 
   */
  public FudgeDateTime(final DateTimeProvider dateTimeProvider) {
    this(DateTimeAccuracy.NANOSECOND, dateTimeProvider);
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param accuracy  the resolution of the representation 
   * @param dateTimeProvider  the provider of date and time
   */
  public FudgeDateTime(final DateTimeAccuracy accuracy, final DateTimeProvider dateTimeProvider) {
    this(accuracy, dateTimeProvider.toLocalDateTime());
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param calendar  the representation of the date and time
   */
  public FudgeDateTime(final Calendar calendar) {
    this(new FudgeDate(calendar), new FudgeTime(calendar));
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the date component.
   * 
   * @return the date, not null
   */
  public FudgeDate getDate() {
    return _date;
  }

  /**
   * Returns the time component.
   * 
   * @return the time, not null
   */
  public FudgeTime getTime() {
    return _time;
  }

  /**
   * Returns the resolution of the representation
   * 
   * @return the resolution, not null
   */
  public DateTimeAccuracy getAccuracy() {
    return getTime().getAccuracy();
  }

  //-------------------------------------------------------------------------
  /**
   * Converts this date to a {@code LocalDate}, using the first appropriate
   * value if any field is not set.
   * 
   * @return a {@code LocalDate} roughly equivalent to this date-time, not null
   */
  @Override
  public LocalDate toLocalDate() {
    return getDate().toLocalDate();
  }

  /**
   * Converts this date to a {@code LocalDateTime}, using the first appropriate
   * value if any field is not set.
   * 
   * @return a {@code LocalDateTime} roughly equivalent to this date-time, not null
   */
  @Override
  public LocalDateTime toLocalDateTime() {
    return LocalDateTime.of(getDate(), getTime());
  }

  /**
   * Converts this date to a {@code LocalTime}, using the first appropriate
   * value if any field is not set.
   * 
   * @return a {@code LocalTime} roughly equivalent to this date-time, not null
   */
  @Override
  public LocalTime toLocalTime() {
    return getTime().toLocalTime();
  }

  /**
   * Converts this date to a {@code OffsetDateTime}, using the first appropriate
   * value if any field is not set.
   * 
   * @return a {@code OffsetDateTime} roughly equivalent to this date-time, not null
   */
  public OffsetDateTime toOffsetDateTime() {
    return OffsetDateTime.of(getDate(), getTime(), getTime().getOffset());
  }

  /**
   * Converts this date to a {@code OffsetDate}, using the first appropriate
   * value if any field is not set.
   * 
   * @return a {@code OffsetDate} roughly equivalent to this date-time, not null
   */
  public OffsetDate toOffsetDate() {
    return OffsetDate.of(getDate(), getTime().getOffset());
  }

  /**
   * Converts this date to a {@code OffsetTime}, using the first appropriate
   * value if any field is not set.
   * 
   * @return a {@code OffsetTime} roughly equivalent to this date-time, not null
   */
  public OffsetTime toOffsetTime() {
    return getTime().toOffsetTime();
  }

  /**
   * Converts this date to an {@code Instant}, using the first appropriate
   * value if any field is not set.
   * 
   * @return an {@code Instant} roughly equivalent to this date-time, not null
   */
  @Override
  public Instant toInstant() {
    return toOffsetDateTime().toInstant();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this date-time equals another.
   * 
   * @param object  the object to compare to, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof FudgeDateTime) {
      final FudgeDateTime other = (FudgeDateTime) object;
      return getDate().equals(other.getDate()) && getTime().equals(other.getTime());
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
    return getDate().hashCode() * 17 + getTime().hashCode();
  }

  /**
   * Returns a string representation of the date-time.
   * 
   * @return the date-time as a string, not null
   */
  @Override
  public String toString() {
    return toOffsetDateTime().toString();
  }

}

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

import org.threeten.bp.DateTimeException;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.temporal.TemporalAccessor;
import org.threeten.bp.temporal.TemporalQueries;

/**
 * A date-time at varying precision.
 * <p>
 * This is the low level Fudge representation for a date-time.
 * Date-times can be more easily used through the secondary type mechanism.
 * <p>
 * For more details, please refer to <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>.
 */
public class FudgeDateTime {

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
   * <p>
   * This will use any offset from the temporal object.
   * The temporal object must represent at least date and time.
   * 
   * @param instant  the instant, not null
   * @return the date-time, not null
   * @throws DateTimeException if unable to convert
   */
  public static FudgeDateTime ofUTC(final Instant instant) {
    return new FudgeDateTime(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));
  }

  /**
   * Creates a new Fudge date/time representation.
   * <p>
   * This will use any offset from the temporal object.
   * The temporal object must represent at least date and time.
   * 
   * @param accuracy  the resolution of the representation, not null
   * @param instant  the instant, not null
   * @return the date-time, not null
   * @throws DateTimeException if unable to convert
   */
  public static FudgeDateTime ofUTC(final DateTimeAccuracy accuracy, final Instant instant) {
    return new FudgeDateTime(accuracy, OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param precision  the resolution of the representation
   * @param year  the year, using negative for BCE and positive for CE, year zero invalid
   * @param month  the month, from 1 to 12
   * @param day  the day, from 1 to 31
   * @param timezoneOffset  the time-zone offset in 15 minute intervals 
   * @param seconds  the seconds since midnight
   * @param nanos  the nanoseconds within the second
   * @throws IllegalArgumentException if the month or day is invalid
   */
  public FudgeDateTime(
      final DateTimeAccuracy precision, final int year, final int month, final int day,
      final int timezoneOffset, final int seconds, final int nanos) {
    this(FudgeDate.of(year, month, day), new FudgeTime(precision, timezoneOffset, seconds, nanos));
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
   * @param localDateTime  the date, Midnight on this day will be used for the time
   */
  protected FudgeDateTime(final LocalDateTime localDateTime) {
    this(DateTimeAccuracy.NANOSECOND, localDateTime);
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param accuracy  the resolution of the representation, not null
   * @param localDateTime  the date and time, not null
   * @param offset  the offset, may be null
   */
  public FudgeDateTime(final DateTimeAccuracy accuracy, final LocalDateTime localDateTime, ZoneOffset offset) {
    this(FudgeDate.from(localDateTime.toLocalDate()), new FudgeTime(accuracy, localDateTime.toLocalTime(), offset));
  }

  /**
   * Creates a new Fudge date/time representation.
   * <p>
   * This will use any offset from the temporal object.
   * The temporal object must represent at least date and time.
   * 
   * @param temporal  the temporal object providing the time, not null
   * @throws DateTimeException if unable to convert
   */
  public FudgeDateTime(final TemporalAccessor temporal) {
    this(DateTimeAccuracy.NANOSECOND, temporal);
  }

  /**
   * Creates a new Fudge date/time representation.
   * <p>
   * This will use any offset from the temporal object.
   * The temporal object must represent at least date and time.
   * 
   * @param accuracy  the resolution of the representation , not null
   * @param temporal  the temporal object providing the time, not null
   * @throws DateTimeException if unable to convert
   */
  public FudgeDateTime(final DateTimeAccuracy accuracy, final TemporalAccessor temporal) {
    this(accuracy, LocalDateTime.from(temporal), temporal.query(TemporalQueries.offset()));
  }

  /**
   * Creates a new Fudge date/time representation.
   * 
   * @param calendar  the representation of the date and time, not null
   */
  public FudgeDateTime(final Calendar calendar) {
    this(FudgeDate.from(calendar), new FudgeTime(calendar));
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
  public LocalDate toLocalDate() {
    return getDate().toLocalDate();
  }

  /**
   * Converts this date to a {@code LocalDateTime}, using the first appropriate
   * value if any field is not set.
   * 
   * @return a {@code LocalDateTime} roughly equivalent to this date-time, not null
   */
  public LocalDateTime toLocalDateTime() {
    return toLocalDate().atTime(toLocalTime());
  }

  /**
   * Converts this date to a {@code LocalTime}, using the first appropriate
   * value if any field is not set.
   * 
   * @return a {@code LocalTime} roughly equivalent to this date-time, not null
   */
  public LocalTime toLocalTime() {
    return getTime().toLocalTime();
  }

  /**
   * Converts this date to a {@code OffsetDateTime}, using the first appropriate
   * value if any field is not set.
   * <p>
   * The offset is defaulted to UTC if not set.
   * 
   * @return a {@code OffsetDateTime} roughly equivalent to this date-time, not null
   */
  public OffsetDateTime toOffsetDateTime() {
    return toOffsetTime().atDate(toLocalDate());
  }

  /**
   * Converts this date to a {@code OffsetTime}, using the first appropriate
   * value if any field is not set.
   * <p>
   * The offset is defaulted to UTC if not set.
   * 
   * @return a {@code OffsetTime} roughly equivalent to this date-time, not null
   */
  public OffsetTime toOffsetTime() {
    return getTime().toOffsetTime();
  }

  /**
   * Converts this date to an {@code Instant}, using the first appropriate
   * value if any field is not set.
   * <p>
   * The offset is defaulted to UTC if not set.
   * 
   * @return an {@code Instant} roughly equivalent to this date-time, not null
   */
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

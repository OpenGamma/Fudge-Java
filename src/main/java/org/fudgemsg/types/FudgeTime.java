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
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.temporal.TemporalAccessor;
import org.threeten.bp.temporal.TemporalQueries;

/**
 * A time at varying precision.
 * <p>
 * This is the low level Fudge representation for a time.
 * Times can be more easily used through the secondary type mechanism.
 * <p>
 * For more details, please refer to <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>.
 */
public class FudgeTime {

  /**
   * Reserved value to indicate there is no time-zone offset.
   */
  static final int NO_TIMEZONE_OFFSET = -128;

  /**
   * The accuracy.
   */
  private final DateTimeAccuracy _accuracy;
  /**
   * The time-zone offset.
   */
  private final int _timezoneOffset;
  /**
   * The time.
   */
  private final LocalTime _localTime;

  /**
   * Creates a new {@link FudgeTime}.
   * 
   * @param accuracy  the resolution of the time, not null
   * @param timezoneOffset  the time-zone offset (15 minute intervals)
   * @param seconds  the seconds since midnight
   * @param nanos  the nanoseconds within the second
   */
  public FudgeTime(final DateTimeAccuracy accuracy, final int timezoneOffset, int seconds, int nanos) {
    _accuracy = accuracy;
    _timezoneOffset = timezoneOffset;
    if (seconds < 0) {
      throw new IllegalArgumentException("seconds cannot be negative");
    }
    if (nanos < 0) {
      throw new IllegalArgumentException("nanos cannot be negative");
    }
    if (accuracy.greaterThan(DateTimeAccuracy.SECOND)) {
      if (accuracy.greaterThan(DateTimeAccuracy.MILLISECOND)) {
        if (accuracy.greaterThan(DateTimeAccuracy.MICROSECOND)) {
          // As accurate as can be - no rounding needed
        } else {
          nanos -= nanos % 1000;
        }
      } else {
        nanos -= nanos % 1000000;
      }
    } else {
      nanos = 0;
      if (accuracy.greaterThan(DateTimeAccuracy.DAY)) {
        if (accuracy.greaterThan(DateTimeAccuracy.HOUR)) {
          if (accuracy.greaterThan(DateTimeAccuracy.MINUTE)) {
            // Accurate to the second - already rounded for this
          } else {
            seconds -= seconds % 60;
          }
        } else {
          seconds -= seconds % 3600;
        }
      } else {
        seconds = 0;
      }
    }
    _localTime = LocalTime.of(seconds / 3600, (seconds / 60) % 60, seconds % 60, nanos);
  }

  /**
   * Creates a new Fudge time representation.
   * 
   * @param accuracy  the granularity of the representation, not null
   * @param timezoneOffset  the time-zone offset in 15 minute intervals from UTC
   * @param localTime  the time, not null
   */
  protected FudgeTime(final DateTimeAccuracy accuracy, final int timezoneOffset, final LocalTime localTime) {
    this(accuracy, timezoneOffset, localTime.toSecondOfDay(), localTime.getNano());
  }

  /**
   * Creates a new Fudge time representation.
   * <p>
   * This will use any offset from the temporal object.
   * 
   * @param accuracy  the granularity of the representation, not null
   * @param time  the time, not null
   * @param offset  the offset, may be null
   */
  public FudgeTime(final DateTimeAccuracy accuracy, final LocalTime time, ZoneOffset offset) {
    this(accuracy, calcOffset(offset), time);
  }

  /**
   * Creates a new Fudge time representation.
   * <p>
   * This will use any offset from the temporal object.
   * 
   * @param temporal  the temporal object providing the time, not null
   * @throws DateTimeException if unable to convert
   */
  public FudgeTime(final TemporalAccessor temporal) {
    this(DateTimeAccuracy.NANOSECOND, temporal);
  }

  /**
   * Creates a new Fudge time representation.
   * <p>
   * This will use any offset from the temporal object.
   * 
   * @param accuracy  the granularity of the representation, not null
   * @param temporal  the temporal object providing the time, not null
   * @throws DateTimeException if unable to convert
   */
  public FudgeTime(final DateTimeAccuracy accuracy, final TemporalAccessor temporal) {
    this(accuracy, calcOffset(temporal.query(TemporalQueries.offset())), LocalTime.from(temporal));
  }

  /**
   * Calculates the offset code.
   * @param temporal  the temporal to query
   * @return the offset code
   */
  private static int calcOffset(ZoneOffset offset) {
    if (offset == null) {
      return NO_TIMEZONE_OFFSET;
    }
    return offset.getTotalSeconds() / 900;
  }

  /**
   * Creates a new {@link FudgeTime} with the time from a {@link Calendar} object.
   * 
   * @param time  the {@code Calendar} to copy the time from
   */
  public FudgeTime(final Calendar time) {
    _accuracy = DateTimeAccuracy.MILLISECOND;
    if (time.isSet(Calendar.ZONE_OFFSET) || time.isSet(Calendar.DST_OFFSET)) {
      _timezoneOffset = ((time.isSet(Calendar.ZONE_OFFSET) ? time.get(Calendar.ZONE_OFFSET) : 0) + (time
          .isSet(Calendar.DST_OFFSET) ? time.get(Calendar.DST_OFFSET) : 0)) / 900000;
    } else {
      _timezoneOffset = NO_TIMEZONE_OFFSET;
    }
    _localTime = LocalTime.of(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.SECOND),
        time.get(Calendar.MILLISECOND) * 1000000);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the accuracy of the time.
   * 
   * @return the accuracy, not null
   */
  public DateTimeAccuracy getAccuracy() {
    return _accuracy;
  }

  /**
   * Checks if the time has an offset.
   * 
   * @return {@code true} if the {@link FudgeTime} has a time-zone offset, {@code false} otherwise
   */
  public boolean hasTimezoneOffset() {
    return getEncodedTimezoneOffset() != NO_TIMEZONE_OFFSET;
  }

  /**
   * Gets the time-zone offset as it would be encoded returning zero when no offset.
   * See also {@link #toZoneOffset}.
   * 
   * @return the time-zone offset (15 minute intervals) or 0 if there is no offset
   */
  public int getTimezoneOffset() {
    if (_timezoneOffset != NO_TIMEZONE_OFFSET) {
      return _timezoneOffset;
    } else {
      return 0;
    }
  }

  /**
   * Gets the time-zone offset as it would be encoded.
   * <p>
   * This is the wire level encoded form of the offset.
   * If there is no offset, the value of -128 will be returned.
   * This is different to the behaviour of {@link #getTimezoneOffset} which would return 0
   * for both there being no time-zone information and there being a time-zone offset of 0.
   * 
   * @return the time-zone offset (15 minute intervals) or -128 for none
   */
  public final int getEncodedTimezoneOffset() {
    return _timezoneOffset;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the hour of the day.
   * 
   * @return hour
   */
  public int getHour() {
    return toLocalTime().getHour();
  }

  /**
   * Gets the minute within the hour.
   * 
   * @return minutes
   */
  public int getMinute() {
    return toLocalTime().getMinute();
  }

  /**
   * Gets the second within the minute.
   * 
   * @return seconds
   */
  public int getSeconds() {
    return toLocalTime().getSecond();
  }

  /**
   * Gets the milliseconds within the second.
   *
   * @return milliseconds
   */
  public int getMillis() {
    return getNanos() / 1000000;
  }

  /**
   * Gets the microseconds within the second.
   * 
   * @return microseconds
   */
  public int getMicros() {
    return getNanos() / 1000;
  }

  /**
   * Gets the number of nanoseconds within the second.
   * 
   * @return nanoseconds within the second
   */
  public int getNanos() {
    return toLocalTime().getNano();
  }

  /**
   * Gets the number of seconds since midnight.
   * 
   * @return seconds
   */
  public int getSecondsSinceMidnight() {
    return toLocalTime().toSecondOfDay();
  }

  //-------------------------------------------------------------------------
  /**
   * Converts the time-zone offset to a {@link ZoneOffset}.
   * See also {@link #getTimezoneOffset}.
   * 
   * @return the time-zone offset, null if no offset
   */
  public ZoneOffset toZoneOffset() {
    if (getTimezoneOffset() == NO_TIMEZONE_OFFSET) {
      return null;
    }
    return ZoneOffset.ofTotalSeconds(getTimezoneOffset() * 900);
  }

  /**
   * Converts this date to a {@code LocalTime}, using the first appropriate
   * value if any field is not set.
   * 
   * @return a {@code LocalTime} roughly equivalent to this time, not null
   */
  public LocalTime toLocalTime () {
    return _localTime;
  }

  /**
   * Converts this date to an {@code OffsetTime}, using the first appropriate
   * value if any field is not set.
   * <p>
   * The offset is defaulted to UTC if not set.
   * 
   * @return an {@code OffsetTime} roughly equivalent to this time, not null
   */
  public OffsetTime toOffsetTime() {
    if (getTimezoneOffset() == NO_TIMEZONE_OFFSET) {
      return toLocalTime().atOffset(ZoneOffset.UTC);
    }
    return toLocalTime().atOffset(toZoneOffset());
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this time equals another.
   * 
   * @param object  the object to compare to, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals (final Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof FudgeTime) {
      final FudgeTime other = (FudgeTime) object;
      return other.getAccuracy() == getAccuracy() &&
            other.toLocalTime().equals(toLocalTime()) &&
            other.getEncodedTimezoneOffset() == getEncodedTimezoneOffset();
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
    return (toLocalTime().hashCode() * 17 + _timezoneOffset + 1) * 17 + _accuracy.getEncodedValue();
  }

  /**
   * Returns a string representation of the time.
   * 
   * @return the time as a string, not null
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if (getAccuracy().greaterThan(DateTimeAccuracy.DAY)) {
      if (getHour() < 10) {
        sb.append('0');
      }
      sb.append(getHour());
      if (getAccuracy().greaterThan(DateTimeAccuracy.HOUR)) {
        sb.append(':');
        if (getMinute() < 10) {
          sb.append('0');
        }
        sb.append(getMinute());
        if (getAccuracy().greaterThan(DateTimeAccuracy.MINUTE)) {
          sb.append(':');
          if (getSeconds() < 10) {
            sb.append('0');
          }
          sb.append(getSeconds());
          if (getAccuracy().greaterThan(DateTimeAccuracy.SECOND)) {
            int frac = getNanos();
            if (getAccuracy().lessThan(DateTimeAccuracy.NANOSECOND)) {
              frac /= 1000;
              if (getAccuracy().lessThan(DateTimeAccuracy.MICROSECOND)) {
                frac /= 1000;
              }
            }
            sb.append('.').append(frac);
          }
        }
      }
    }
    if (hasTimezoneOffset()) {
      int tz = getTimezoneOffset() * 15;
      if (tz == 0) {
        sb.append(" UTC");
      } else {
        sb.append((tz > 0) ? " +" : " -");
        if (tz < 0) {
          tz = -tz;
        }
        sb.append(tz / 60).append(':');
        tz %= 60;
        if (tz < 10) {
          sb.append('0');
        }
        sb.append(tz);
      }
    }
    return sb.toString();
  }

}

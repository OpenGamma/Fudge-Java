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
package org.fudgemsg.types.secondary;

import javax.time.calendar.YearMonth;

import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Secondary type for JSR-310 object conversion.
 */
public class JSR310YearMonthFieldType extends SecondaryFieldType<YearMonth, FudgeDate> {

  /**
   * Singleton instance of the type.
   */
  public static final JSR310YearMonthFieldType INSTANCE = new JSR310YearMonthFieldType();

  /**
   * Restricted constructor.
   */
  private JSR310YearMonthFieldType() {
    super(FudgeWireType.DATE, YearMonth.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeDate secondaryToPrimary(final YearMonth object) {
    return new FudgeDate(object.getYear(), object.getMonthOfYear().getValue());
  }

  @Override
  public YearMonth primaryToSecondary(final FudgeDate object) {
    return YearMonth.of(object.getYear(), object.getMonthOfYear());
  }

}

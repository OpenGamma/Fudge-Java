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

import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.threeten.bp.LocalDate;

/**
 * Secondary type for ThreeTen object conversion.
 */
public class ThreeTenLocalDateFieldType extends SecondaryFieldType<LocalDate, FudgeDate> {

  /**
   * Singleton instance of the type.
   */
  public static final ThreeTenLocalDateFieldType INSTANCE = new ThreeTenLocalDateFieldType();

  /**
   * Restricted constructor.
   */
  private ThreeTenLocalDateFieldType() {
    super(FudgeWireType.DATE, LocalDate.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeDate secondaryToPrimary(final LocalDate object) {
    return FudgeDate.from(object);
  }

  @Override
  public LocalDate primaryToSecondary(final FudgeDate object) {
    return object.toLocalDate();
  }

}

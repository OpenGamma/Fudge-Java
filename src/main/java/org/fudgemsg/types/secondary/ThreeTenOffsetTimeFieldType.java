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

import org.fudgemsg.types.FudgeTime;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.threeten.bp.OffsetTime;

/**
 * Secondary type for ThreeTen object conversion.
 */
public class ThreeTenOffsetTimeFieldType extends SecondaryFieldType<OffsetTime, FudgeTime> {

  /**
   * Singleton instance of the type.
   */
  public static final ThreeTenOffsetTimeFieldType INSTANCE = new ThreeTenOffsetTimeFieldType();

  /**
   * Restricted constructor.
   */
  private ThreeTenOffsetTimeFieldType() {
    super(FudgeWireType.TIME, OffsetTime.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeTime secondaryToPrimary(final OffsetTime object) {
    return new FudgeTime(object);
  }

  @Override
  public OffsetTime primaryToSecondary(final FudgeTime object) {
    return object.toOffsetTime();
  }

}

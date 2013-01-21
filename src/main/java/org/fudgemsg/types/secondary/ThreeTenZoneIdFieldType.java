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

import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.threeten.bp.ZoneId;

/**
 * Secondary type for ThreeTen {@link ZoneId} conversion to/from a {@link String} transport object. 
 */
public class ThreeTenZoneIdFieldType extends SecondaryFieldType<ZoneId, String> {

  /**
   * Singleton instance of the type.
   */
  public static final ThreeTenZoneIdFieldType INSTANCE = new ThreeTenZoneIdFieldType();

  /**
   * Restricted constructor.
   */
  private ThreeTenZoneIdFieldType() {
    super(FudgeWireType.STRING, ZoneId.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(ZoneId object) {
    return object.getId();
  }

  @Override
  public ZoneId primaryToSecondary(String object) {
    return ZoneId.of(object);
  }

}

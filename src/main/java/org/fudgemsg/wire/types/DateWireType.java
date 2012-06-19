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
package org.fudgemsg.wire.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.fudgemsg.types.FudgeDate;

/**
 * The type definition for a date.
 * <p>
 * This is currently backed by a {@link FudgeDate}.
 * The secondary type mechanism is used to support additional Java representations,
 * such as {@link Date}, {@link Calendar} and {@code javax.time} classes.</p>
 * <p>
 * For more details, please refer to <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime Encoding</a>.
 */
final class DateWireType extends FudgeWireType {

  /**
   * Standard Fudge field type: date.
   * See {@link FudgeWireType#DATE_TYPE_ID}.
   */
  public static final DateWireType INSTANCE = new DateWireType();

  /**
   * Restricted constructor.
   */
  private DateWireType() {
    super(FudgeWireType.DATE_TYPE_ID, FudgeDate.class, 4);
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeDate readValue(DataInput input, int dataSize) throws IOException {
    return DateTimeWireType.readFudgeDate(input);
  }

  @Override
  public void writeValue(DataOutput output, Object value) throws IOException {
    FudgeDate data = (FudgeDate) value;
    DateTimeWireType.writeFudgeDate(output, data);
  }

}

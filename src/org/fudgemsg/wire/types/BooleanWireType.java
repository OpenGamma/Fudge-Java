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

/**
 * The wire type definition for a boolean.
 */
final class BooleanWireType extends FudgeWireType {

  /**
   * Standard Fudge field type: boolean.
   * See {@link FudgeWireType#BOOLEAN_TYPE_ID}.
   */
  public static final BooleanWireType INSTANCE = new BooleanWireType();

  /**
   * Restricted constructor.
   */
  private BooleanWireType() {
    super(FudgeWireType.BOOLEAN_TYPE_ID, Boolean.TYPE, 1);
  }

  //-------------------------------------------------------------------------
  @Override
  public Boolean readValue(DataInput input, int dataSize) throws IOException {
    return input.readBoolean();
  }

  @Override
  public void writeValue(DataOutput output, Object value) throws IOException {
    output.writeBoolean((Boolean) value);
  }

}

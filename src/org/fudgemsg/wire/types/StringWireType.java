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

import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.wire.UTF8;

/**
 * The wire type definition for a UTF-8 encoded string.
 */
final class StringWireType extends FudgeWireType {

  /**
   * Standard Fudge field type: string.
   * See {@link FudgeTypeDictionary#STRING_TYPE_ID}.
   */
  public static final StringWireType INSTANCE = new StringWireType();

  /**
   * Restricted constructor.
   */
  private StringWireType() {
    super(FudgeTypeDictionary.STRING_TYPE_ID, String.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public int getSize(Object value, FudgeTaxonomy taxonomy) {
    String data = (String) value;
    return UTF8.getLengthBytes(data);
  }

  @Override
  public String readValue(DataInput input, int dataSize) throws IOException {
    return UTF8.readString(input, dataSize);
  }

  @Override
  public void writeValue(DataOutput output, Object value) throws IOException {
    String data = (String) value;
    UTF8.writeString(output, data);
  }

}

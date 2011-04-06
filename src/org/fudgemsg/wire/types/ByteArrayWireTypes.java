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

import org.fudgemsg.taxonomy.FudgeTaxonomy;

/**
 * The type definition for byte arrays.
 */
final class ByteArrayWireTypes extends FudgeWireType {

  /**
   * Standard Fudge field type: arbitrary length byte array.
   * See {@link FudgeWireType#BYTE_ARRAY_TYPE_ID}.
   */
  public static final ByteArrayWireTypes VARIABLE_SIZED_INSTANCE = new ByteArrayWireTypes();
  /**
   * Standard Fudge field type: byte array of length 4.
   * See {@link FudgeWireType#BYTE_ARR_4_TYPE_ID}.
   */
  public static final ByteArrayWireTypes LENGTH_4_INSTANCE = new ByteArrayWireTypes(FudgeWireType.BYTE_ARR_4_TYPE_ID, 4);
  /**
   * Standard Fudge field type: byte array of length 8.
   * See {@link FudgeWireType#BYTE_ARR_8_TYPE_ID}.
   */
  public static final ByteArrayWireTypes LENGTH_8_INSTANCE = new ByteArrayWireTypes(FudgeWireType.BYTE_ARR_8_TYPE_ID, 8);
  /**
   * Standard Fudge field type: byte array of length 16.
   * See {@link FudgeWireType#BYTE_ARR_16_TYPE_ID}.
   */
  public static final ByteArrayWireTypes LENGTH_16_INSTANCE = new ByteArrayWireTypes(FudgeWireType.BYTE_ARR_16_TYPE_ID, 16);
  /**
   * Standard Fudge field type: byte array of length 20.
   * See {@link FudgeWireType#BYTE_ARR_20_TYPE_ID}.
   */
  public static final ByteArrayWireTypes LENGTH_20_INSTANCE = new ByteArrayWireTypes(FudgeWireType.BYTE_ARR_20_TYPE_ID, 20);
  /**
   * Standard Fudge field type: byte array of length 32.
   * See {@link FudgeWireType#BYTE_ARR_32_TYPE_ID}.
   */
  public static final ByteArrayWireTypes LENGTH_32_INSTANCE = new ByteArrayWireTypes(FudgeWireType.BYTE_ARR_32_TYPE_ID, 32);
  /**
   * Standard Fudge field type: byte array of length 64.
   * See {@link FudgeWireType#BYTE_ARR_64_TYPE_ID}.
   */
  public static final ByteArrayWireTypes LENGTH_64_INSTANCE = new ByteArrayWireTypes(FudgeWireType.BYTE_ARR_64_TYPE_ID, 64);
  /**
   * Standard Fudge field type: byte array of length 128.
   * See {@link FudgeWireType#BYTE_ARR_128_TYPE_ID}.
   */
  public static final ByteArrayWireTypes LENGTH_128_INSTANCE = new ByteArrayWireTypes(FudgeWireType.BYTE_ARR_128_TYPE_ID, 128);
  /**
   * Standard Fudge field type: byte array of length 256.
   * See {@link FudgeWireType#BYTE_ARR_256_TYPE_ID}.
   */
  public static final ByteArrayWireTypes LENGTH_256_INSTANCE = new ByteArrayWireTypes(FudgeWireType.BYTE_ARR_256_TYPE_ID, 256);
  /**
   * Standard Fudge field type: byte array of length 512.
   * See {@link FudgeWireType#BYTE_ARR_512_TYPE_ID}.
   */
  public static final ByteArrayWireTypes LENGTH_512_INSTANCE = new ByteArrayWireTypes(FudgeWireType.BYTE_ARR_512_TYPE_ID, 512);

  /**
   * Restricted constructor for variable width.
   */
  private ByteArrayWireTypes() {
    super(FudgeWireType.BYTE_ARRAY_TYPE_ID, byte[].class);
  }

  /**
   * Restricted constructor for fixed widths.
   */
  private ByteArrayWireTypes(byte typeId, int length) {
    super(typeId, byte[].class, length);
  }

  //-------------------------------------------------------------------------
  @Override
  public int getSize(Object value, FudgeTaxonomy taxonomy) {
    return ((byte[]) value).length;
  }

  @Override
  public byte[] readValue(DataInput input, int dataSize) throws IOException {
    if (!isVariableSize()) {
      dataSize = getFixedSize();
    }
    byte[] result = new byte[dataSize];
    input.readFully(result);
    return result;
  }

  @Override
  public void writeValue(DataOutput output, Object value) throws IOException {
    byte[] bytes = (byte[]) value;
    if (!isVariableSize()) {
      if (bytes.length != getFixedSize()) {
        throw new IllegalArgumentException("Used fixed size type of size " + getFixedSize() + " but passed array of size " + bytes.length);
      }
    }
    output.write(bytes);
  }

}

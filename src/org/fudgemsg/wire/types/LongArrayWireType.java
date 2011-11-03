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
 * The type definition for arrays of 64-bit integers.
 */
final class LongArrayWireType extends FudgeWireType {

  /**
   * Standard Fudge field type: arrays of 64-bit integers.
   * See {@link FudgeWireType#LONG_ARRAY_TYPE_ID}.
   */
  public static final LongArrayWireType INSTANCE = new LongArrayWireType();

  /**
   * Restricted constructor.
   */
  private LongArrayWireType() {
    super(FudgeWireType.LONG_ARRAY_TYPE_ID, long[].class);
  }

  //-------------------------------------------------------------------------
  @Override
  public int getSize(Object value, FudgeTaxonomy taxonomy) {
    long[] data = (long[]) value;
    return data.length * 8;
  }

  @Override
  public long[] readValue(DataInput input, int dataSize) throws IOException {
    //Reading this in one go is faster, but increases memory requirement by x2.  Should do it in buffered chunks
    byte[] bytes = new byte[dataSize];
    input.readFully(bytes);
    
    int nDoubles = dataSize / 8;
    long[] result = new long[nDoubles];
    for (int i = 0; i < nDoubles; i++) {
      long l = readLong(bytes, i*8);
      result[i] = l;
    }
    return result;
  }

  public static final long readLong(byte[] buff, int offset) {
    //Fudge spec requires Network Byte Order
    
    long a = (long) buff[0 + offset] << 56;
    long b = (long) (buff[1 + offset] & 255) << 48;
    long c = (long) (buff[2 + offset] & 255) << 40;
    long d = (long) (buff[3 + offset] & 255) << 32;
    long e = (long) (buff[4 + offset] & 255) << 24;
    long f = (buff[5 + offset] & 255) << 16;
    long g = ((buff[6 + offset] & 255) << 8);
    int h = (buff[7 + offset] & 255) << 0;
    return a + b + c + d + e + f + g + h;
  }


  @Override
  public void writeValue(DataOutput output, Object value) throws IOException {
    long[] data = (long[]) value;
    for (long l : data) {
      output.writeLong(l);
    }
  }

}

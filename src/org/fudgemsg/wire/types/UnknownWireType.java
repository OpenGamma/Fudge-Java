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

import org.fudgemsg.UnknownFudgeFieldValue;
import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * The type definition for an unknown wire type.
 * <p>
 * The type system of Fudge is extensible, with a total of 255 types available,
 * but less than 32 reserved as standard types at present.
 * Application types should be allocated from 255 downwards to avoid clashes.
 * <p>
 * A field can be processed as a raw byte array even when the type is not recognized.
 */
final class UnknownWireType extends FudgeWireType {

  /**
   * Creates a new variable width wire type.
   * 
   * @param typeId  the wire type identifier
   */
  UnknownWireType(int typeId) {
    super(typeId, UnknownFudgeFieldValue.class);
  }

  /**
   * Creates a new fixed width wire type.
   * 
   * @param typeId  the wire type identifier
   * @param fixedWidth  the fixed width
   */
  UnknownWireType(int typeId, int fixedWidth) {
    super(typeId, UnknownFudgeFieldValue.class, fixedWidth);
  }

  //-------------------------------------------------------------------------
  @Override
  public int getSize(Object value, FudgeTaxonomy taxonomy) {
    UnknownFudgeFieldValue data = (UnknownFudgeFieldValue) value;
    return data.getContents().length;
  }

  @Override
  public UnknownFudgeFieldValue readValue(DataInput input, int dataSize) throws IOException {
    byte[] contents = new byte[dataSize];
    input.readFully(contents);
    return new UnknownFudgeFieldValue(contents, this);
  }

  @Override
  public void writeValue(DataOutput output, Object value) throws IOException {
    UnknownFudgeFieldValue data = (UnknownFudgeFieldValue) value;
    output.write(data.getContents());
  }

}

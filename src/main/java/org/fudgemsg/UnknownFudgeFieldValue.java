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
package org.fudgemsg;

import org.fudgemsg.wire.types.FudgeWireType;

/**
 * A container to store a variable-sized field with a type that the current
 * installation of Fudge cannot handle on decoding.
 * <p>
 * This class holds a mutable byte array but is unmodifiable.
 */
public class UnknownFudgeFieldValue {

  /**
   * The raw data content.
   */
  private final byte[] _contents;
  /**
   * The field type.
   */
  private final FudgeWireType _type;

  /**
   * Creates a new instance to represent a block of data in an unknown type.
   * 
   * @param contents  the raw contents from the Fudge message stream, not null
   * @param type  the field type for the unknown type, not null
   */
  public UnknownFudgeFieldValue(byte[] contents, FudgeWireType type) {
    if (contents == null) {
      throw new NullPointerException("Data content must not be null");
    }
    if (type == null) {
      throw new NullPointerException("FudgeWireType must not be null");
    }
    _contents = contents;
    _type = type;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the raw contents of the original data which should not be modified.
   * 
   * @return the data content, treat as unmodifiable, not null
   */
  public byte[] getContents() {
    return _contents;
  }

  /**
   * Gets the wire type.
   * 
   * @return the wire type, not null
   */
  public FudgeWireType getType() {
    return _type;
  }

}

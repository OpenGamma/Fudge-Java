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

import java.io.Serializable;

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeTypeDictionary;

/**
 * The wire type of a field as defined by the Fudge encoding specification.
 * <p>
 * In order to efficiently send messages, Fudge needs to know the type of each piece of data.
 * A standard set of types is supported by all Fudge-compliant systems.
 * This set may be extended with custom types within a closed Fudge implementation.
 * Custom types must be registered with {@link FudgeTypeDictionary}.
 * <p>
 * This class is not final but is thread-safe in isolation.
 * Subclasses must be immutable and thread-safe.
 */
public abstract class FudgeWireType extends FudgeFieldType implements Serializable {

  /**
   * Wire type id: indicator.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte INDICATOR_TYPE_ID = (byte) 0;
  /**
   * Wire type id: boolean.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BOOLEAN_TYPE_ID = (byte) 1;
  /**
   * Wire type id: 8-bit signed integer.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_TYPE_ID = (byte) 2;
  /**
   * Wire type id: 16-bit signed integer.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte SHORT_TYPE_ID = (byte) 3;
  /**
   * Wire type id: 32-bit signed integer.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte INT_TYPE_ID = (byte) 4;
  /**
   * Wire type id: 64-bit signed integer.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte LONG_TYPE_ID = (byte) 5;
  /**
   * Wire type id: byte array.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARRAY_TYPE_ID = (byte) 6;
  /**
   * Wire type id: array of 16-bit signed integers.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte SHORT_ARRAY_TYPE_ID = (byte) 7;
  /**
   * Wire type id: array of 32-bit signed integers.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte INT_ARRAY_TYPE_ID = (byte) 8;
  /**
   * Wire type id: array of 64-bit signed integers.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte LONG_ARRAY_TYPE_ID = (byte) 9;
  /**
   * Wire type id: 32-bit floating point.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte FLOAT_TYPE_ID = (byte) 10;
  /**
   * Wire type id: 64-bit floating point.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte DOUBLE_TYPE_ID = (byte) 11;
  /**
   * Wire type id: array of 32-bit floating point.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte FLOAT_ARRAY_TYPE_ID = (byte) 12;
  /**
   * Wire type id: array of 64-bit floating point.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte DOUBLE_ARRAY_TYPE_ID = (byte) 13;
  /**
   * Wire type id: string.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte STRING_TYPE_ID = (byte) 14;
  /**
   * Wire type id: embedded Fudge sub-message.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte SUB_MESSAGE_TYPE_ID = (byte) 15;
  // End message indicator type removed as unnecessary, hence no 16
  /**
   * Wire type id: byte array of length 4.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARRAY_4_TYPE_ID = (byte) 17;
  /**
   * Wire type id: byte array of length 8.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARRAY_8_TYPE_ID = (byte) 18;
  /**
   * Wire type id: byte array of length 16.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARRAY_16_TYPE_ID = (byte) 19;
  /**
   * Wire type id: byte array of length 20.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARRAY_20_TYPE_ID = (byte) 20;
  /**
   * Wire type id: byte array of length 32.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARRAY_32_TYPE_ID = (byte) 21;
  /**
   * Wire type id: byte array of length 64.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARRAY_64_TYPE_ID = (byte) 22;
  /**
   * Wire type id: byte array of length 128.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARRAY_128_TYPE_ID = (byte) 23;
  /**
   * Wire type id: byte array of length 256.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARRAY_256_TYPE_ID = (byte) 24;
  /**
   * Wire type id: byte array of length 512.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARRAY_512_TYPE_ID = (byte) 25;
  /**
   * Wire type id: date.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte DATE_TYPE_ID = (byte) 26;
  /**
   * Wire type id: time.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte TIME_TYPE_ID = (byte) 27;
  /**
   * Wire type id: combined date and time.
   * See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte DATETIME_TYPE_ID = (byte) 28;

  /**
   * Wire type: embedded sub-message.
   * See {@link #SUB_MESSAGE_TYPE_ID}.
   */
  public static final FudgeWireType SUB_MESSAGE = SubMessageWireType.INSTANCE;
  /**
   * Wire type: indicator.
   * See {@link #INDICATOR_TYPE_ID}.
   */
  public static final FudgeWireType INDICATOR = IndicatorWireType.INSTANCE;
  /**
   * Wire type: boolean.
   * See {@link #BOOLEAN_TYPE_ID}.
   */
  public static final FudgeWireType BOOLEAN = BooleanWireType.INSTANCE;
  /**
   * Wire type: boolean.
   * See {@link #BYTE_TYPE_ID}.
   */
  public static final FudgeWireType BYTE = ByteWireType.INSTANCE;
  /**
   * Wire type: boolean.
   * See {@link #SHORT_TYPE_ID}.
   */
  public static final FudgeWireType SHORT = ShortWireType.INSTANCE;
  /**
   * Wire type: boolean.
   * See {@link #INT_TYPE_ID}.
   */
  public static final FudgeWireType INT = IntWireType.INSTANCE;
  /**
   * Wire type: boolean.
   * See {@link #LONG_TYPE_ID}.
   */
  public static final FudgeWireType LONG = LongWireType.INSTANCE;
  /**
   * Wire type: boolean.
   * See {@link #FLOAT_TYPE_ID}.
   */
  public static final FudgeWireType FLOAT = FloatWireType.INSTANCE;
  /**
   * Wire type: boolean.
   * See {@link #DOUBLE_TYPE_ID}.
   */
  public static final FudgeWireType DOUBLE = DoubleWireType.INSTANCE;
  /**
   * Wire type: string.
   * See {@link #STRING_TYPE_ID}.
   */
  public static final FudgeWireType STRING = StringWireType.INSTANCE;
  /**
   * Wire type: short array.
   * See {@link #SHORT_ARRAY_TYPE_ID}.
   */
  public static final FudgeWireType SHORT_ARRAY = ShortArrayWireType.INSTANCE;
  /**
   * Wire type: int array.
   * See {@link #INT_ARRAY_TYPE_ID}.
   */
  public static final FudgeWireType INT_ARRAY = IntArrayWireType.INSTANCE;
  /**
   * Wire type: long array.
   * See {@link #LONG_ARRAY_TYPE_ID}.
   */
  public static final FudgeWireType LONG_ARRAY = LongArrayWireType.INSTANCE;
  /**
   * Wire type: float array.
   * See {@link #FLOAT_ARRAY_TYPE_ID}.
   */
  public static final FudgeWireType FLOAT_ARRAY = FloatArrayWireType.INSTANCE;
  /**
   * Wire type: double array.
   * See {@link #DOUBLE_ARRAY_TYPE_ID}.
   */
  public static final FudgeWireType DOUBLE_ARRAY = DoubleArrayWireType.INSTANCE;
  /**
   * Wire type: arbitrary length byte array.
   * See {@link #BYTE_ARRAY_TYPE_ID}.
   */
  public static final FudgeWireType BYTE_ARRAY = ByteArrayWireTypes.VARIABLE_SIZED_INSTANCE;
  /**
   * Wire type: byte array of length 4.
   * See {@link #BYTE_ARRAY_4_TYPE_ID}.
   */
  public static final FudgeWireType BYTE_ARRAY_4 = ByteArrayWireTypes.LENGTH_4_INSTANCE;
  /**
   * Wire type: byte array of length 8.
   * See {@link #BYTE_ARRAY_8_TYPE_ID}.
   */
  public static final FudgeWireType BYTE_ARRAY_8 = ByteArrayWireTypes.LENGTH_8_INSTANCE;
  /**
   * Wire type: byte array of length 16.
   * See {@link #BYTE_ARRAY_16_TYPE_ID}.
   */
  public static final FudgeWireType BYTE_ARRAY_16 = ByteArrayWireTypes.LENGTH_16_INSTANCE;
  /**
   * Wire type: byte array of length 20.
   * See {@link #BYTE_ARRAY_20_TYPE_ID}.
   */
  public static final FudgeWireType BYTE_ARRAY_20 = ByteArrayWireTypes.LENGTH_20_INSTANCE;
  /**
   * Wire type: byte array of length 32.
   * See {@link #BYTE_ARRAY_32_TYPE_ID}.
   */
  public static final FudgeWireType BYTE_ARRAY_32 = ByteArrayWireTypes.LENGTH_32_INSTANCE;
  /**
   * Wire type: byte array of length 64.
   * See {@link #BYTE_ARRAY_64_TYPE_ID}.
   */
  public static final FudgeWireType BYTE_ARRAY_64 = ByteArrayWireTypes.LENGTH_64_INSTANCE;
  /**
   * Wire type: byte array of length 128.
   * See {@link #BYTE_ARRAY_128_TYPE_ID}.
   */
  public static final FudgeWireType BYTE_ARRAY_128 = ByteArrayWireTypes.LENGTH_128_INSTANCE;
  /**
   * Wire type: byte array of length 256.
   * See {@link #BYTE_ARRAY_256_TYPE_ID}.
   */
  public static final FudgeWireType BYTE_ARRAY_256 = ByteArrayWireTypes.LENGTH_256_INSTANCE;
  /**
   * Wire type: byte array of length 512.
   * See {@link #BYTE_ARRAY_512_TYPE_ID}.
   */
  public static final FudgeWireType BYTE_ARRAY_512 = ByteArrayWireTypes.LENGTH_512_INSTANCE;
  /**
   * Wire type: date.
   * See {@link #TIME_TYPE_ID}.
   */
  public static final FudgeWireType DATE = DateWireType.INSTANCE;
  /**
   * Wire type: date.
   * See {@link #TIME_TYPE_ID}.
   */
  public static final FudgeWireType TIME = TimeWireType.INSTANCE;
  /**
   * Wire type: combined date and time.
   * See {@link #DATETIME_TYPE_ID}.
   */
  public static final FudgeWireType DATETIME = DateTimeWireType.INSTANCE;

  /**
   * Creates a new variable width wire type when the type is unknown.
   * 
   * @param typeId  the wire type identifier
   * @return the wire type, not null
   */
  public static FudgeWireType unknown(int typeId) {
    return new UnknownWireType(typeId);
  }

  /**
   * Creates a new fixed width wire type when the type is unknown.
   * 
   * @param typeId  the wire type identifier
   * @param fixedWidth  the fixed width
   * @return the wire type, not null
   */
  public static FudgeWireType unknown(int typeId, int fixedWidth) {
    return new UnknownWireType(typeId, fixedWidth);
  }

  /**
   * Chooses the best wire type for the specified byte array.
   * <p>
   * There are byte array wire types for a variety of common lengths.
   * This method chooses the most efficient to use.
   * 
   * @param array  the array to choose a wire type for, null returns the variable length type
   * @return the most efficient wire type available, not null
   */
  public static FudgeWireType bestMatchByteArray(byte[] array) {
    if (array == null) {
      return BYTE_ARRAY;
    }
    switch (array.length) {
      case 4: return BYTE_ARRAY_4;
      case 8: return BYTE_ARRAY_8;
      case 16: return BYTE_ARRAY_16;
      case 20: return BYTE_ARRAY_20;
      case 32: return BYTE_ARRAY_32;
      case 64: return BYTE_ARRAY_64;
      case 128: return BYTE_ARRAY_128;
      case 256: return BYTE_ARRAY_256;
      case 512: return BYTE_ARRAY_512;
      default: return BYTE_ARRAY;
    }
  }

  //-------------------------------------------------------------------------
//  /**
//   * The Fudge type id, from the specification.
//   */
//  private final int _typeId;
//  /**
//   * The standard Java equivalent type.
//   */
//  private final Class<?> _javaType;
//  /**
//   * Whether the type is sent as a variable size in the protocol.
//   */
//  private final boolean _isVariableSize;
//  /**
//   * The size of the type in bytes when the size is fixed.
//   */
//  private final int _fixedSize;

  /**
   * Constructs a new variable width wire type based on the underlying Java type.
   * <p>
   * The Fudge type identifier must be unique within the {@link FudgeTypeDictionary}.
   * 
   * @param typeId  the type dictionary unique type identifier, from 0 to 255
   * @param javaType  the underlying Java type, not null
   */
  protected FudgeWireType(int typeId, Class<?> javaType) {
    super(typeId, javaType, true, 0);
//    ArgumentChecker.notNull(javaType, "Java type must not be null");
//    if (typeId < 0 || typeId > 255) {
//      throw new IllegalArgumentException("The type id must fit in an unsigned byte");
//    }
//    _typeId = typeId;
//    _javaType = javaType;
//    _isVariableSize = true;
//    _fixedSize = 0;
  }

  /**
   * Constructs a new fixed width wire type based on the underlying Java type.
   * <p>
   * The Fudge type identifier must be unique within the {@link FudgeTypeDictionary}.
   * 
   * @param typeId  the type dictionary unique type identifier, from 0 to 255
   * @param javaType  the underlying Java type, not null
   * @param fixedSize  the size in bytes if fixed size, zero for variable width
   */
  protected FudgeWireType(int typeId, Class<?> javaType, int fixedSize) {
    super(typeId, javaType, false, fixedSize);
//    ArgumentChecker.notNull(javaType, "Java type must not be null");
//    if (typeId < 0 || typeId > 255) {
//      throw new IllegalArgumentException("The type id must fit in an unsigned byte");
//    }
//    _typeId = typeId;
//    _javaType = javaType;
//    _isVariableSize = false;
//    _fixedSize = fixedSize;
  }

//  //-------------------------------------------------------------------------
//  /**
//   * Gets the Fudge wire type identifier.
//   * <p>
//   * This is the unsigned byte used on the wire to identify the type.
//   * 
//   * @return the type identifier, from 0 to 255
//   */
//  public final int getTypeId() {
//    return _typeId;
//  }
//
//  /**
//   * Gets the standard Java type for values of this type.
//   * 
//   * @return the standard Java type, not null
//   */
//  public final Class<?> getJavaType() {
//    return _javaType;
//  }
//
//  /**
//   * Checks if the type has a variable width.
//   * 
//   * @return true if variable width, false for fixed width
//   */
//  public final boolean isVariableSize() {
//    return _isVariableSize;
//  }
//
//  /**
//   * Checks if the type has a fixed width.
//   * 
//   * @return true if variable width, false for fixed width
//   */
//  public final boolean isFixedSize() {
//    return !_isVariableSize;
//  }
//
//  /**
//   * Gets the number of bytes used to encode a value if the type is fixed width.
//   * 
//   * @return the fixed width size in bytes, zero if variable width
//   */
//  public final int getFixedSize() {
//    return _fixedSize;
//  }

  /**
   * Checks if the type is registered and known.
   * 
   * @return true if type is known
   */
  public final boolean isTypeKnown() {
    return !isTypeUnknown();
  }

  /**
   * Checks if the type is unregistered and unknown.
   * 
   * @return true if type is unknown
   */
  public final boolean isTypeUnknown() {
    return this instanceof UnknownWireType;
  }

//  //-------------------------------------------------------------------------
//  /**
//   * Gets the number of bytes used to encode a value.
//   * <p>
//   * A variable width type must override this method.
//   * A fixed width type will return the {@link #getFixedSize() fixed size}.
//   * 
//   * @param value  the value to check, not used for fixed width types
//   * @param taxonomy  the taxonomy being used for the encoding, not used for fixed width types
//   * @return the size in bytes
//   */
//  public int getSize(Object value, FudgeTaxonomy taxonomy) {
//    if (isVariableSize()) {
//      throw new UnsupportedOperationException("This method must be overridden for variable size types");
//    }
//    return getFixedSize();
//  }
//
//  //-------------------------------------------------------------------------
//  /**
//   * Writes a value of this type to the output.
//   * <p>
//   * This is intended for use by variable width types and must write the given value.
//   * The implementation must write exactly the number of bytes returned by the
//   * {@link #getSize(Object,FudgeTaxonomy) size calculation}.
//   * 
//   * @param output  the output target to write the value to, not null
//   * @param value  the value to write
//   * @throws IOException if an error occurs, which must be wrapped by the caller
//   */
//  public abstract void writeValue(DataOutput output, Object value) throws IOException;
//
//  /**
//   * Reads a value of this type to the output.
//   * <p>
//   * This is intended for use by variable width types and must read the given value.
//   * The implementation must read exactly the number of bytes passed into the method.
//   * 
//   * @param input  the input source to read the value from, not null
//   * @param dataSize  the number of bytes of data to read
//   * @return the value that was read
//   * @throws IOException if an error occurs, which must be wrapped by the caller
//   */
//  public abstract Object readValue(DataInput input, int dataSize) throws IOException;

//  //-------------------------------------------------------------------------
//  /**
//   * Checks if this type equals another at the wire type level.
//   * <p>
//   * Note that this only checks the wire type identifier, not the Java type.
//   * 
//   * @param obj  the object to compare to, null returns false
//   * @return true if equal
//   */
//  @Override
//  public final boolean equals(Object obj) {
//    if (obj == this) {
//      return true;
//    }
//    if (obj instanceof FudgeWireType) {
//      FudgeWireType other = (FudgeWireType) obj;
//      return getTypeId() == other.getTypeId(); // assume system is correctly setup and type is unique
//    }
//    return false;
//  }
//
//  /**
//   * Gets a suitable hash code.
//   * 
//   * @return the hash code
//   */
//  @Override
//  public final int hashCode() {
//    return getTypeId();
//  }
//
//  /**
//   * Returns a description of the type.
//   * 
//   * @return the descriptive string, not null
//   */
//  @Override
//  public final String toString() {
//    return "FudgeWireType[" + getTypeId() + "-" + getJavaType() + "]";
//  }

}

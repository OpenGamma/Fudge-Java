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
package org.fudgemsg.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * The base type definition for a secondary field type that converts Java objects
 * to/from more fundamental Fudge types. This approach is more lightweight than
 * the tools available in the mapping package, but also limited as there is
 * no access to the {@link FudgeContext} when the conversion takes place. The
 * {@link SecondaryFieldType} class provides a simpler interface to use for most
 * extensions.
 *
 * @param <SecondaryType> secondary type
 * @param <PrimitiveType> type there is a primary {@link FudgeFieldType} for
 * @param <ConversionType> base type to support mappings from, e.g. use Object to convert from any of the Fudge primitives
 */
public abstract class SecondaryFieldTypeBase<SecondaryType, ConversionType, PrimitiveType extends ConversionType>
    extends FudgeFieldType implements FudgeTypeConverter<ConversionType, SecondaryType> {

  /**
   * The wire type.
   */
  private final FudgeWireType _wireType;

  /**
   * Creates a new secondary type on top of an existing Fudge type.
   * 
   * @param wireType  the existing Fudge primitive type
   * @param javaType  the Java type for conversion
   */
  protected SecondaryFieldTypeBase(FudgeWireType wireType, Class<SecondaryType> javaType) {
    super(wireType.getTypeId(), javaType, wireType.isVariableSize(), wireType.getFixedSize());
    _wireType = wireType;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying Fudge wire type.
   * 
   * @return the primary wire type
   */
  public FudgeWireType getPrimaryType() {
    return _wireType;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an object from the secondary type to a primitive Fudge type for writing. An implementation
   * may assume that the {@code object} parameter is not {@code null}.
   * 
   * @param object the secondary instance
   * @return the underlying Fudge data to write out
   */
  public abstract PrimitiveType secondaryToPrimary(SecondaryType object);

  /**
   * Calculates the resultant size by converting to the primary object and invoking the delegate. If conversion
   * overhead is noticeable, a subclass should consider overriding this and calculating the value directly.
   * 
   * @param value the value to convert (if it will not be a fixed width type)
   * @param taxonomy the taxonomy used to encode
   */
  @SuppressWarnings("unchecked")
  @Override
  public int getSize(Object value, FudgeTaxonomy taxonomy) {
    SecondaryType data = (SecondaryType) value;
    return getPrimaryType().getSize(secondaryToPrimary(data), taxonomy);
  }

  @SuppressWarnings("unchecked")
  @Override
  public SecondaryType readValue(DataInput input, int dataSize) throws IOException {
    PrimitiveType readValue = (PrimitiveType) getPrimaryType().readValue(input, dataSize);
    return primaryToSecondary(readValue);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void writeValue(DataOutput output, Object value) throws IOException {
    SecondaryType data = (SecondaryType) value;
    getPrimaryType().writeValue(output, secondaryToPrimary(data));
  }

}

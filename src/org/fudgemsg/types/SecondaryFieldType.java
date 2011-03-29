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

/**
 * The type definition for a secondary field type that converts Java objects
 * to a more fundamental Fudge type. This approach is more lightweight than
 * the tools available in the mapping package, but also limited as there is
 * no access to the {@link FudgeContext} when the conversion takes place.
 *
 * @param <SecondaryType> secondary type
 * @param <PrimitiveType> type there is a primary {@link FudgeFieldType} for
 */
public abstract class SecondaryFieldType<SecondaryType, PrimitiveType> extends
    SecondaryFieldTypeBase<SecondaryType, PrimitiveType, PrimitiveType> {

  /**
   * Creates a new secondary type on top of an existing Fudge type.
   * 
   * @param type existing Fudge primitive type
   * @param javaType Java type for conversion
   */
  protected SecondaryFieldType(FudgeFieldType type, Class<SecondaryType> javaType) {
    super(type, javaType);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts Fudge primitive data to the secondary type. This is an optional operation - it will only be
   * invoked if the user attempts to convert from the underlying type used for transport back to the 
   * secondary type. An implementation may assume that the {@code object} parameter is not {@code null}.
   * 
   * @param object  the Fudge data
   * @return a secondary type instance
   */
  @Override
  public SecondaryType primaryToSecondary(PrimitiveType object) {
    throw new UnsupportedOperationException("cannot convert from " + getTypeId() + " to " + getJavaType());
  }

  @Override
  public boolean canConvertPrimary(Class<? extends PrimitiveType> clazz) {
    return getPrimaryType().getJavaType().isAssignableFrom(clazz);
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

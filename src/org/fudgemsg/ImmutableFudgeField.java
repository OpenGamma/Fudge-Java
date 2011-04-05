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

import java.io.Serializable;

/**
 * A single immutable field in the Fudge system.
 * <p>
 * This is the standard immutable implementation of {@link FudgeField}.
 * <p>
 * This class makes no guarantees about the immutability or thread-safety of its
 * content, although it holds the references in an immutable and thread-safe way.
 */
public final class ImmutableFudgeField implements FudgeField, Serializable {

  /**
   * The optional field name.
   */
  private final String _name;
  /**
   * The optional field ordinal.
   */
  private final Integer _ordinal;
  /**
   * The Fudge field type.
   */
  private final FudgeFieldType _type;
  /**
   * The value.
   */
  private final Object _value;

  /**
   * Obtains an immutable version of the specified field.
   * <p>
   * If the field is an instance of this class, it is returned, otherwise a new
   * instance is created.
   * 
   * @param field  the field to obtain data from, not null
   * @return the equivalent immutable field, not null
   */
  public static ImmutableFudgeField of(FudgeField field) {
    if (field instanceof ImmutableFudgeField) {
      return (ImmutableFudgeField) field;
    }
    return ImmutableFudgeField.of(field.getType(), field.getValue(), field.getName(), field.getOrdinal());
  }

  /**
   * Obtains a field from the type, value, name and ordinal.
   * 
   * @param type  the Fudge field type, not null
   * @param value  the payload value, may be null
   * @return the created immutable field, not null
   */
  public static ImmutableFudgeField of(FudgeFieldType type, Object value) {
    return new ImmutableFudgeField(type, value, null, null);
  }

  /**
   * Obtains a field from the type, value, name and ordinal.
   * 
   * @param type  the Fudge field type, not null
   * @param value  the payload value, may be null
   * @param name  the optional field name, null if no name
   * @return the created immutable field, not null
   */
  public static ImmutableFudgeField of(FudgeFieldType type, Object value, String name) {
    return new ImmutableFudgeField(type, value, name, null);
  }

  /**
   * Obtains a field from the type, value, name and ordinal.
   * 
   * @param type  the Fudge field type, not null
   * @param value  the payload value, may be null
   * @param ordinal  the optional field ordinal, null if no ordinal
   * @return the created immutable field, not null
   */
  public static ImmutableFudgeField of(FudgeFieldType type, Object value, Integer ordinal) {
    return new ImmutableFudgeField(type, value, null, ordinal);
  }

  /**
   * Obtains a field from the type, value, name and ordinal.
   * 
   * @param type  the Fudge field type, not null
   * @param value  the payload value, may be null
   * @param name  the optional field name, null if no name
   * @param ordinal  the optional field ordinal, null if no ordinal
   * @return the created immutable field, not null
   */
  public static ImmutableFudgeField of(FudgeFieldType type, Object value, String name, Integer ordinal) {
    return new ImmutableFudgeField(type, value, name, ordinal);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a field from the type, value, name and ordinal.
   * 
   * @param type  the Fudge field type, not null
   * @param value  the payload value, may be null
   * @param name  the optional field name, null if no name
   * @param ordinal  the optional field ordinal, null if no ordinal
   */
  private ImmutableFudgeField(FudgeFieldType type, Object value, String name, Integer ordinal) {
    if (type == null) {
      throw new NullPointerException("Type must not be null");
    }
    _type = type;
    _value = value;
    _name = name;
    _ordinal = ordinal;
  }

  //-------------------------------------------------------------------------
  @Override
  public String getName() {
    return _name;
  }

  @Override
  public Integer getOrdinal() {
    return _ordinal;
  }

  @Override
  public FudgeFieldType getType() {
    return _type;
  }

  @Override
  public Object getValue() {
    return _value;
  }

  //-------------------------------------------------------------------------
  /**
   * Compares this field to another field.
   * <p>
   * This checks the type, value, name and ordinal.
   * 
   * @param obj  the other field, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ImmutableFudgeField) {
      ImmutableFudgeField other = (ImmutableFudgeField) obj;
      return getType().equals(other.getType()) &&
          equal(getOrdinal(), other.getOrdinal()) &&
          equal(getName(), other.getName()) &&
          equal(getValue(), other.getValue());
    }
    return false;
  }

  private boolean equal(final Object a, final Object b) {
    return a == b || (a != null && a.equals(b));
  }

  /**
   * Returns a suitable hash code.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return getType().hashCode() ^
          (getValue() == null ? 0 : getValue().hashCode()) ^
          (getName() == null ? 0 : getName().hashCode()) ^
          (getOrdinal() == null ? 0 : getOrdinal().hashCode());
  }

  /**
   * Gets a string description of the field.
   * 
   * @return the description, not null
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Field[");
    if (getName() != null) {
      sb.append(getName());
      if (getOrdinal() == null) {
        sb.append(":");
      } else {
        sb.append(",");
      }
    }
    if (getOrdinal() != null) {
      sb.append(getOrdinal()).append(":");
    }

    sb.append(getType());
    sb.append("-").append(getValue());
    sb.append("]");
    return sb.toString();
  }

}

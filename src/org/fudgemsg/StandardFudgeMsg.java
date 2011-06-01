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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fudgemsg.taxonomy.FudgeTaxonomy;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * A standard mutable Fudge message.
 * <p>
 * The message consists of a list of {@link FudgeField Fudge fields}.
 * This class holds the entire message in memory.
 * <p>
 * Applications are recommended to store and manipulate a {@link FudgeMsg}
 * instance or a {@link MutableFudgeMsg} rather than this class
 * for future flexibility.
 * <p>
 * This class is mutable and not thread-safe.
 */
public class StandardFudgeMsg extends AbstractFudgeMsg implements MutableFudgeMsg {

  /**
   * The list of fields.
   */
  private final List<FudgeField> _fields = new ArrayList<FudgeField>();

  /**
   * Constructor taking a Fudge context.
   * 
   * @param fudgeContext  the context to use for type resolution and other services, not null
   */
  protected StandardFudgeMsg(FudgeContext fudgeContext) {
    this(fudgeContext, null);
  }

  /**
   * Constructor taking a Fudge context that copies another message.
   * <p>
   * The fields from the container are copied into this message, creating a new
   * field for each supplied field.
   * 
   * @param fudgeContext  the context to use for type resolution and other services, not null
   * @param fieldsToCopy  the initial set of fields to shallow copy, null ignored
   */
  protected StandardFudgeMsg(final FudgeContext fudgeContext, Iterable<FudgeField> fieldsToCopy) {
    super(fudgeContext);
    if (fieldsToCopy != null) {
      for (FudgeField field : fieldsToCopy) {
        _fields.add(UnmodifiableFudgeField.of(field));
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the live list of fields.
   * 
   * @return the mutable list of fields, not null
   */
  @Override
  protected List<FudgeField> getFields() {
    return _fields;
  }

  /**
   * Gets a modifiable iterator over the list of fields in this message.
   * <p>
   * A message is partially ordered and the returned iterator reflects that order.
   * 
   * @return the modifiable iterator of fields, not null
   */
  @Override
  public Iterator<FudgeField> iterator() {
    return getFields().iterator();  // modifiable iterator, as this is a mutable message
  }

  //-------------------------------------------------------------------------
  @Override
  public void add(FudgeField field) {
    if (field == null) {
      throw new NullPointerException("FudgeField must not be null");
    }
    getFields().add(UnmodifiableFudgeField.of(field));
  }

  @Override
  public void add(String name, Object value) {
    add(name, null, value);
  }

  @Override
  public void add(Integer ordinal, Object value) {
    add(null, ordinal, value);
  }

  @Override
  public void add(String name, Integer ordinal, Object value) {
    FudgeFieldType type = determineTypeFromValue(value);
    if (type == null) {
      throw new IllegalArgumentException("Cannot determine a Fudge type for value " + value + " of type " + value.getClass());
    } else if (type == FudgeWireType.INDICATOR) {
      add(name, ordinal, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
    } else {
      add(name, ordinal, type, value);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void add(String name, Integer ordinal, FudgeFieldType type, Object value) {
    if (type == null) {
      throw new NullPointerException("FudgeFieldType must not be null");
    }
    if (ordinal != null && (ordinal > Short.MAX_VALUE || ordinal < Short.MIN_VALUE)) {
      throw new IllegalArgumentException("Ordinal must be within signed 16-bit range.");
    }
    
    // adjust integral values to the lowest possible representation
    switch (type.getTypeId()) {
      case FudgeWireType.SHORT_TYPE_ID:
      case FudgeWireType.INT_TYPE_ID:
      case FudgeWireType.LONG_TYPE_ID:
        if (type instanceof SecondaryFieldType<?, ?>) {
          value = ((SecondaryFieldType<Object, ?>) type).secondaryToPrimary(value);
          type = ((SecondaryFieldType<?, ?>) type).getPrimaryType();
        }
        long valueAsLong = ((Number) value).longValue();
        if (valueAsLong >= Byte.MIN_VALUE && valueAsLong <= Byte.MAX_VALUE) {
          value = new Byte((byte) valueAsLong);
          type = FudgeWireType.BYTE;
        } else if (valueAsLong >= Short.MIN_VALUE && valueAsLong <= Short.MAX_VALUE) {
          value = new Short((short) valueAsLong);
          type = FudgeWireType.SHORT;
        } else if (valueAsLong >= Integer.MIN_VALUE && valueAsLong <= Integer.MAX_VALUE) {
          value = new Integer((int) valueAsLong);
          type = FudgeWireType.INT;
        }
        break;
    }
    
    UnmodifiableFudgeField field = UnmodifiableFudgeField.of(type, value, name, ordinal);
    getFields().add(field);
  }

  /**
   * Resolves an arbitrary Java object to an underlying Fudge type (if possible).
   * 
   * @param value  the object to resolve, null returns the indicator type
   * @return the field type, null if no intrinsic type (or registered secondary type) is available
   */
  protected FudgeFieldType determineTypeFromValue(Object value) {
    if (value == null) {
      return FudgeWireType.INDICATOR;
    }
    if (value instanceof byte[]) {
      return FudgeWireType.bestMatchByteArray((byte[]) value);
    }
    FudgeFieldType type = getFudgeContext().getTypeDictionary().getByJavaType(value.getClass());
    if (type == null && value instanceof UnknownFudgeFieldValue) {
      UnknownFudgeFieldValue unknownValue = (UnknownFudgeFieldValue) value;
      type = unknownValue.getType();
    }
    return type;
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves any field ordinals to field names from the given taxonomy.
   * 
   * @param taxonomy  the taxonomy to use, null ignored
   */
  public void setNamesFromTaxonomy(FudgeTaxonomy taxonomy) {
    if (taxonomy == null) {
      return;
    }
    for (int i = 0; i < getFields().size(); i++) {
      FudgeField field = getFields().get(i);
      if ((field.getOrdinal() != null) && (field.getName() == null)) {
        String nameFromTaxonomy = taxonomy.getFieldName(field.getOrdinal());
        if (nameFromTaxonomy != null) {
          field = UnmodifiableFudgeField.of(field.getType(), field.getValue(), nameFromTaxonomy, field.getOrdinal());
          getFields().set(i, field);
        }
      }
      if (field.getValue() instanceof StandardFudgeMsg) {
        StandardFudgeMsg subMsg = (StandardFudgeMsg) field.getValue();
        subMsg.setNamesFromTaxonomy(taxonomy);
      } else if (field.getValue() instanceof FudgeMsg) {
        StandardFudgeMsg subMsg = new StandardFudgeMsg(getFudgeContext(), (FudgeMsg) field.getValue());
        subMsg.setNamesFromTaxonomy(taxonomy);
        field = UnmodifiableFudgeField.of(field.getType(), subMsg, field.getName(), field.getOrdinal());
        getFields().set(i, field);
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(Integer ordinal) {
    final Iterator<FudgeField> it = iterator();
    while (it.hasNext()) {
      final FudgeField field = it.next();
      if (fieldOrdinalEquals(ordinal, field))
        it.remove();
    }
  }

  @Override
  public void remove(String name) {
    final Iterator<FudgeField> it = iterator();
    while (it.hasNext()) {
      final FudgeField field = it.next();
      if (fieldNameEquals(name, field))
        it.remove();
    }
  }

  @Override
  public void remove(String name, Integer ordinal) {
    final Iterator<FudgeField> ii = iterator();
    while (ii.hasNext()) {
      final FudgeField field = ii.next();
      if (fieldOrdinalEquals(ordinal, field) && fieldNameEquals(name, field))
        ii.remove();
    }
  }

  @Override
  public void clear() {
    getFields().clear();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    return obj instanceof StandardFudgeMsg && super.equals(obj);
  }

  @Override
  public int hashCode() {
    return StandardFudgeMsg.class.hashCode() ^ super.hashCode();
  }

}

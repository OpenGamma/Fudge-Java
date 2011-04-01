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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * Abstract implementation of {@code FudgeMsg}.
 * <p>
 * Most methods in the {@code FudgeMsg} interface are convenience methods
 * which can be implemented based on a few key methods.
 * This abstract class provides the convenience method implementations, allowing subclasses
 * to supply the data.
 * <p>
 * This class is mutable and not thread-safe.
 */
public class AbstractFudgeMsg implements FudgeMsg, Iterable<FudgeField>, Serializable {

  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;
  /**
   * The list of fields.
   */
  private final List<FudgeField> _fields = new ArrayList<FudgeField>();

  /**
   * Constructor taking a Fudge context.
   * 
   * @param fudgeContext  the context to use for type resolution and other services, not null
   */
  protected AbstractFudgeMsg(FudgeContext fudgeContext) {
    if (fudgeContext == null) {
      throw new NullPointerException("Context must be provided.");
    }
    _fudgeContext = fudgeContext;
  }

  /**
   * Constructor taking a set of fields and a Fudge context.
   * <p>
   * The fields from the given container are converted to be immutable.
   * 
   * @param fields  the initial set of fields, not null
   * @param fudgeContext  the context to use for type resolution and other services, not null
   */
  protected AbstractFudgeMsg(final FudgeMsg fields, final FudgeContext fudgeContext) {
    if (fields == null) {
      throw new NullPointerException("Cannot initialize from a null FudgeFieldContainer");
    }
    if (fudgeContext == null) {
      throw new NullPointerException("Context must be provided");
    }
    _fudgeContext = fudgeContext;
    for (FudgeField field : fields.getAllFields()) {
      _fields.add(FudgeMsgField.of(field));
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Returns this message's {@link FudgeContext}.
   * 
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the live list of fields.
   * 
   * @return the mutable list of fields, not null
   */
  protected List<FudgeField> getFields() {
    return _fields;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the name matches the name of the given field.
   * 
   * @param name  the name to match against, null matches null
   * @param field  the field to check, not null
   * @return true if the field name matches
   */
  protected boolean fieldNameEquals(final String name, final FudgeField field) {
    if (name == null) {
      return field.getName() == null;
    } else {
      return name.equals(field.getName());
    }
  }

  /**
   * Checks if the ordinal matches the ordinal of the given field.
   * 
   * @param ordinal  the ordinal to match against, null matches null
   * @param field  the field to check, not null
   * @return true if the field name matches
   */
  protected boolean fieldOrdinalEquals(final Integer ordinal, final FudgeField field) {
    if (ordinal == null) {
      return field.getOrdinal() == null;
    } else {
      return ordinal.equals(field.getOrdinal());
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first field value with the given field name and type identifier using
   * type converters if necessary.
   * <p>
   * This first searches for an exact match on name and type.
   * If the name is found but the type is different a type converter is used to try
   * to convert the type.
   * 
   * @param <T>  the class to convert to
   * @param clazz  the type to convert to, not null
   * @param name  the field name, null matches null
   * @param typeId  the type identifier
   * @return the field value, null if no matching field found
   */
  @SuppressWarnings("unchecked")
  protected <T> T getFirstTypedValue(Class<T> clazz, String name, int typeId) {
    FudgeField secondBest = null;
    for (FudgeField field : getFields()) {
      if (fieldNameEquals(name, field)) {
        if (field.getType().getTypeId() == typeId) {
          return (T) field.getValue();  // perfect match
        } else {
          if (secondBest == null) {
            if (getFudgeContext().getTypeDictionary().canConvertField(clazz, field)) {
              secondBest = field;
            }
          }
        }
      }
    }
    if (secondBest == null) {
      return null;
    }
    return getFudgeContext().getTypeDictionary().getFieldValue(clazz, secondBest);
  }

  /**
   * Gets the first field value with the given field ordinal and type identifier using
   * type converters if necessary.
   * <p>
   * This first searches for an exact match on ordinal and type.
   * If the ordinal is found but the type is different a type converter is used to try
   * to convert the type.
   * 
   * @param <T>  the class to convert to
   * @param clazz  the type to convert to, not null
   * @param ordinal  the field ordinal
   * @param typeId  the type identifier
   * @return the field value, null if no matching field found
   */
  @SuppressWarnings("unchecked")
  protected <T> T getFirstTypedValue(Class<T> clazz, Integer ordinal, int typeId) {
    FudgeField secondBest = null;
    for (FudgeField field : getFields()) {
      if (fieldOrdinalEquals(ordinal, field)) {
        if (field.getType().getTypeId() == typeId) {
          return (T) field.getValue();  // perfect match
        } else {
          if (secondBest == null) {
            if (getFudgeContext().getTypeDictionary().canConvertField(clazz, field)) {
              secondBest = field;
            }
          }
        }
      }
    }
    if (secondBest == null) {
      return null;
    }
    return getFudgeContext().getTypeDictionary().getFieldValue(clazz, secondBest);
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
      if (field.getOrdinal() != null && field.getName() == null) {
        String nameFromTaxonomy = taxonomy.getFieldName(field.getOrdinal());
        if (nameFromTaxonomy != null) {
          field = FudgeMsgField.of(field.getType(), field.getValue(), nameFromTaxonomy, field.getOrdinal());
          getFields().set(i, field);
        }
      }
      if (field.getValue() instanceof StandardFudgeMsg) {
        StandardFudgeMsg subMsg = (StandardFudgeMsg) field.getValue();
        subMsg.setNamesFromTaxonomy(taxonomy);
      } else if (field.getValue() instanceof FudgeMsg) {
        StandardFudgeMsg subMsg = new StandardFudgeMsg((FudgeMsg) field.getValue(), getFudgeContext());
        subMsg.setNamesFromTaxonomy(taxonomy);
        field = FudgeMsgField.of(field.getType(), subMsg, field.getName(), field.getOrdinal());
        getFields().set(i, field);
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public short getNumFields() {
    int size = getFields().size();
    assert size <= Short.MAX_VALUE;
    return (short) size;
  }

  @Override
  public boolean isEmpty() {
    return getNumFields() == 0;
  }

  @Override
  public Iterator<FudgeField> iterator() {
    return Collections.unmodifiableList(getFields()).iterator();
  }

  @Override
  public List<FudgeField> getAllFields() {
    return Collections.unmodifiableList(getFields());
  }

  @Override
  public Set<String> getAllFieldNames() {
    Set<String> result = new TreeSet<String>();
    for (FudgeField field : getFields()) {
      if (field.getName() != null) {
        result.add(field.getName());
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeField getByIndex(int index) {
    return getFields().get(index);
  }

  @Override
  public boolean hasField(String name) {
    return getByName(name) != null;
  }

  @Override
  public List<FudgeField> getAllByName(String name) {
    List<FudgeField> fields = new ArrayList<FudgeField>();
    for (FudgeField field : getFields()) {
      if (fieldNameEquals(name, field)) {
        fields.add(field);
      }
    }
    return fields;
  }

  @Override
  public FudgeField getByName(String name) {
    for (FudgeField field : getFields()) {
      if (fieldNameEquals(name, field)) {
        return field;
      }
    }
    return null;
  }

  @Override
  public boolean hasField(Integer ordinal) {
    return getByOrdinal(ordinal) != null;
  }

  @Override
  public List<FudgeField> getAllByOrdinal(Integer ordinal) {
    List<FudgeField> fields = new ArrayList<FudgeField>();
    for (FudgeField field : getFields()) {
      if (fieldOrdinalEquals(ordinal, field)) {
        fields.add(field);
      }
    }
    return fields;
  }

  @Override
  public FudgeField getByOrdinal(Integer ordinal) {
    for (FudgeField field : getFields()) {
      if (fieldOrdinalEquals(ordinal, field)) {
        return field;
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
  // REVIEW kirk 2009-08-16 -- All of these getters are currently extremely unoptimized.
  // There may be an option required if we have a lot of random access to the field content
  // to speed things up by building an index.

  @Override
  public Object getValue(String name) {
    FudgeField field = getByName(name);
    return (field != null) ? field.getValue() : null;
  }

  @Override
  public Object getValue(int ordinal) {
    FudgeField field = getByOrdinal(ordinal);
    return (field != null) ? field.getValue() : null;
  }

  @Override
  public Double getDouble(String name) {
    return getFirstTypedValue(Double.class, name, FudgeTypeDictionary.DOUBLE_TYPE_ID);
  }

  @Override
  public Double getDouble(int ordinal) {
    return getFirstTypedValue(Double.class, ordinal, FudgeTypeDictionary.DOUBLE_TYPE_ID);
  }

  @Override
  public Float getFloat(String name) {
    return getFirstTypedValue(Float.class, name, FudgeTypeDictionary.FLOAT_TYPE_ID);
  }

  @Override
  public Float getFloat(int ordinal) {
    return getFirstTypedValue(Float.class, ordinal, FudgeTypeDictionary.FLOAT_TYPE_ID);
  }

  @Override
  public Long getLong(String name) {
    return getFirstTypedValue(Long.class, name, FudgeTypeDictionary.LONG_TYPE_ID);
  }

  @Override
  public Long getLong(int ordinal) {
    return getFirstTypedValue(Long.class, ordinal, FudgeTypeDictionary.LONG_TYPE_ID);
  }

  @Override
  public Integer getInt(String name) {
    return getFirstTypedValue(Integer.class, name, FudgeTypeDictionary.INT_TYPE_ID);
  }

  @Override
  public Integer getInt(int ordinal) {
    return getFirstTypedValue(Integer.class, ordinal, FudgeTypeDictionary.INT_TYPE_ID);
  }

  @Override
  public Short getShort(String name) {
    return getFirstTypedValue(Short.class, name, FudgeTypeDictionary.SHORT_TYPE_ID);
  }

  @Override
  public Short getShort(int ordinal) {
    return getFirstTypedValue(Short.class, ordinal, FudgeTypeDictionary.SHORT_TYPE_ID);
  }

  @Override
  public Byte getByte(String name) {
    return getFirstTypedValue(Byte.class, name, FudgeTypeDictionary.BYTE_TYPE_ID);
  }

  @Override
  public Byte getByte(int ordinal) {
    return getFirstTypedValue(Byte.class, ordinal, FudgeTypeDictionary.BYTE_TYPE_ID);
  }

  @Override
  public String getString(String name) {
    return getFirstTypedValue(String.class, name, FudgeTypeDictionary.STRING_TYPE_ID);
  }

  @Override
  public String getString(int ordinal) {
    return getFirstTypedValue(String.class, ordinal, FudgeTypeDictionary.STRING_TYPE_ID);
  }

  @Override
  public Boolean getBoolean(String name) {
    return getFirstTypedValue(Boolean.class, name, FudgeTypeDictionary.BOOLEAN_TYPE_ID);
  }

  @Override
  public Boolean getBoolean(int ordinal) {
    return getFirstTypedValue(Boolean.class, ordinal, FudgeTypeDictionary.BOOLEAN_TYPE_ID);
  }

  @Override
  public FudgeMsg getMessage(int ordinal) {
    return getFirstTypedValue(FudgeMsg.class, ordinal, FudgeTypeDictionary.FUDGE_MSG_TYPE_ID);
  }

  @Override
  public FudgeMsg getMessage(String name) {
    return getFirstTypedValue(FudgeMsg.class, name, FudgeTypeDictionary.FUDGE_MSG_TYPE_ID);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T getValue(final Class<T> clazz, final String name) {
    final FudgeTypeDictionary dictionary = getFudgeContext().getTypeDictionary();
    for (FudgeField field : getFields()) {
      if (fieldNameEquals(name, field) && dictionary.canConvertField(clazz, field)) {
        return dictionary.getFieldValue(clazz, field);
      }
    }
    return null;
  }

  @Override
  public <T> T getValue(final Class<T> clazz, final int ordinal) {
    final FudgeTypeDictionary dictionary = getFudgeContext().getTypeDictionary();
    for (FudgeField field : getFields()) {
      if (fieldOrdinalEquals(ordinal, field) && dictionary.canConvertField(clazz, field)) {
        return dictionary.getFieldValue(clazz, field);
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T getFieldValue(final Class<T> clazz, final FudgeField field) {
    return getFudgeContext().getFieldValue(clazz, field);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this message equals another.
   * <p>
   * The check is performed on the entire list of fields in the message.
   * 
   * @param obj  the object to compare to, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof AbstractFudgeMsg) {
      final AbstractFudgeMsg fm = (AbstractFudgeMsg) obj;
      Iterator<FudgeField> me = iterator();
      Iterator<FudgeField> other = fm.iterator();
      while (me.hasNext() && other.hasNext()) {
        if (!me.next().equals(other.next()))
          return false;
      }
      return me.hasNext() == other.hasNext();
    }
    return false;
  }

  /**
   * Gets a suitable hash code.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return getNumFields();  // poor hash code, but better than nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("FudgeMsg[");
    Iterator<FudgeField> iterator = iterator();
    while (iterator.hasNext()) {
      FudgeField field = iterator.next();
      if (field.getOrdinal() != null) {
        sb.append(field.getOrdinal());
        sb.append(": ");
      }
      if (field.getName() != null) {
        sb.append(field.getName());
      }
      sb.append(" => ");
      sb.append(field.getValue());
      sb.append(", ");
    }
    if (sb.length() > 13) {
      sb.delete(sb.length() - 2, sb.length());
    }
    sb.append("]");
    return sb.toString();
  }

}

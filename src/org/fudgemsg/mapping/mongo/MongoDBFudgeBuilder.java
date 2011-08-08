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

package org.fudgemsg.mapping.mongo;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.SecondaryFieldTypeBase;
import org.fudgemsg.wire.types.FudgeWireType;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Builder for encoding and decoding MongoDB objects.
 */
public class MongoDBFudgeBuilder implements FudgeBuilder<DBObject> {
  
  /**
   * Singleton instance of the builder.
   */
  public static final FudgeBuilder<DBObject> INSTANCE = new MongoDBFudgeBuilder ();

  private MongoDBFudgeBuilder() {
  }

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, DBObject dbObject) {
    if (dbObject == null) {
      return null;
    }
    MutableFudgeMsg msg = serializer.newMessage();
    for (String key : dbObject.keySet()) {
      Object value = dbObject.get(key);
      if (value instanceof List<?>) {
        for (Object element : (List<?>) value) {
          msg.add(key, decodeObjectValue(serializer, element));
        }
      } else {
        msg.add(key, decodeObjectValue(serializer, value));
      }
    }
    return msg;
  }

  private Object decodeObjectValue(FudgeSerializer serializer, Object value) {
    if (value instanceof DBObject) {
      DBObject dbObject = (DBObject) value;
      return buildMessage(serializer, dbObject);
    }
    return value;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object encodePrimitiveFieldValue(final FudgeDeserializer deserializer, Object fieldValue) {
    FudgeFieldType valueType = deserializer.getFudgeContext().getTypeDictionary().getByJavaType(fieldValue.getClass());
    if (valueType == null) {
      throw new IllegalArgumentException("Cannot handle serialization of object " + fieldValue + " of type "
          + fieldValue.getClass() + " as no Fudge type available in context");
    }
    
    switch (valueType.getTypeId()) {
    case FudgeWireType.INDICATOR_TYPE_ID:
      // REVIEW kirk 2010-08-20 -- Is this the right behavior here?
      return null;
    case FudgeWireType.BOOLEAN_TYPE_ID :
    case FudgeWireType.BYTE_ARRAY_128_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_16_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_20_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_256_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_32_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_4_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_512_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_64_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_8_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_TYPE_ID:
    case FudgeWireType.BYTE_TYPE_ID:
    case FudgeWireType.DOUBLE_ARRAY_TYPE_ID:
    case FudgeWireType.DOUBLE_TYPE_ID:
    case FudgeWireType.FLOAT_ARRAY_TYPE_ID:
    case FudgeWireType.FLOAT_TYPE_ID:
    case FudgeWireType.INT_ARRAY_TYPE_ID:
    case FudgeWireType.INT_TYPE_ID:
    case FudgeWireType.LONG_ARRAY_TYPE_ID:
    case FudgeWireType.LONG_TYPE_ID:
    case FudgeWireType.SHORT_ARRAY_TYPE_ID:
    case FudgeWireType.SHORT_TYPE_ID:
    case FudgeWireType.STRING_TYPE_ID:
      if (valueType instanceof SecondaryFieldTypeBase) {
        SecondaryFieldTypeBase secondaryType = (SecondaryFieldTypeBase) valueType;
        return secondaryType.secondaryToPrimary(fieldValue);
      }
      // Built-in support.
      return fieldValue;
    case FudgeWireType.DATE_TYPE_ID:
    case FudgeWireType.DATETIME_TYPE_ID:
    case FudgeWireType.TIME_TYPE_ID:
      // FIXME kirk 2010-08-20 -- This is an insanely gross hack around the rest of the
      // fix for FRJ-83 breaking all dates, exposed by FRJ-84.
      return fieldValue;
    }
    // If we get this far, it's a user-defined type. Nothing we can do here.
    throw new IllegalStateException("User-defined types must be handled before they get to MongoDBFudgeBuilder currently. Value type " + valueType);
  }

  private Object encodeFieldValue(final FudgeDeserializer deserializer, final Object currentValue, Object fieldValue) {
    boolean structureExpected = false;
    if (fieldValue instanceof FudgeMsg) {
      fieldValue = buildObject(deserializer, (FudgeMsg) fieldValue);
      structureExpected = true;
    }
    if (currentValue instanceof List<?>) {
      List<Object> l = new ArrayList<Object>((List<?>) (currentValue));
      l.add(fieldValue);
      return l;
    } else if (currentValue != null) {
      List<Object> l = new ArrayList<Object>();
      l.add(currentValue);
      if (!structureExpected) {
        fieldValue = encodePrimitiveFieldValue(deserializer, fieldValue);
      }
      l.add(fieldValue);
      return l;
    }
    if (structureExpected) {
      return fieldValue;
    }
    return encodePrimitiveFieldValue(deserializer, fieldValue);
  }

  @Override
  public DBObject buildObject(FudgeDeserializer deserializer, FudgeMsg fields) {
    if (fields == null) {
      return null;
    }
    BasicDBObject dbObject = new BasicDBObject();
    for (FudgeField field : fields.getAllFields()) {
      if (field.getName() == null) {
        if (field.getOrdinal() == 0) {
          continue;
        }
        // REVIEW kirk 2009-10-22 -- Should this be configurable so that it just
        // silently drops unnamed fields?
        throw new IllegalArgumentException("Field encountered without a name (" + field + ")");
      }
      Object value = field.getValue();
      value = encodeFieldValue(deserializer, dbObject.get(field.getName()), value);
      dbObject.put(field.getName(), value);
    }
    return dbObject;
  }

}

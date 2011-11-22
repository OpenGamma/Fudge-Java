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

package org.fudgemsg.mapping;

import java.util.*;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.types.FudgeTypeConverter;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Builder for {@code Map} objects.
 * <p/>
 * This builder is immutable and thread safe.
 */
/* package */final class MapBuilder implements FudgeBuilder<Map<?, ?>> {
  // map stored as sub-message where each entry is stored as two fields
  // the two fields may be read as all keys then all values, all values
  // then all keys or a more typical mixture of key-value-key-value-...
  // keys have ordinal 1, values have ordinal 2
  // nulls are sent using the indicator type

  /**
   * Singleton instance of the builder.
   */
  /* package */static final FudgeBuilder<Map<?, ?>> INSTANCE = new MapBuilder();

  private MapBuilder() {
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Map<?, ?> map) {
    final MutableFudgeMsg msg = serializer.newMessage();

    Set<Class> topTypesKeys = BuilderUtil.getTopTypes(map.keySet());
    Set<Class> topTypesValues = BuilderUtil.getTopTypes(map.values());

    if (map.isEmpty()) {
      msg.add(BuilderUtil.KEY_TYPE_HINT_ORDINAL, null);
      msg.add(BuilderUtil.VALUE_TYPE_HINT_ORDINAL, null);
    } else {
      // we are hinting the Map that all its entries <Key, Value> should have common type
      for (Class topType : topTypesKeys) {
        msg.add(null, BuilderUtil.KEY_TYPE_HINT_ORDINAL, FudgeWireType.STRING, topType.getName());
      }
      for (Class topType : topTypesValues) {
        msg.add(null, BuilderUtil.VALUE_TYPE_HINT_ORDINAL, FudgeWireType.STRING, topType.getName());
      }

    }
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (entry.getKey() == null) {
        msg.add(null, BuilderUtil.KEY_ORDINAL, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else {
        serializer.addToMessage(msg, null, BuilderUtil.KEY_ORDINAL, entry.getKey());
      }
      if (entry.getValue() == null) {
        msg.add(null, BuilderUtil.VALUE_ORDINAL, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else {
        serializer.addToMessage(msg, null, BuilderUtil.VALUE_ORDINAL, entry.getValue());
      }
    }
    return msg;
  }

  @Override
  public Map<?, ?> buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    final Map<Object, Object> map = new HashMap<Object, Object>();
    final Queue<Object> keys = new LinkedList<Object>();
    final Queue<Object> values = new LinkedList<Object>();

    final List<FudgeField> keysTypeHints = message.getAllByOrdinal(BuilderUtil.KEY_TYPE_HINT_ORDINAL);
    final List<FudgeField> valuesTypeHints = message.getAllByOrdinal(BuilderUtil.VALUE_TYPE_HINT_ORDINAL);

    FudgeObjectBuilder<?> keyBuilder = BuilderUtil.findObjectBuilder(deserializer, keysTypeHints);
    FudgeTypeConverter keyTypeConverter = BuilderUtil.findTypeConverter(deserializer, keysTypeHints);
    FudgeObjectBuilder<?> valueBuilder = BuilderUtil.findObjectBuilder(deserializer, valuesTypeHints);
    FudgeTypeConverter valueTypeConverter = BuilderUtil.findTypeConverter(deserializer, valuesTypeHints);


    for (FudgeField field : message) {

      final Object value = field.getValue();
      Object obj;

      if (field.getOrdinal() != null && field.getOrdinal() == BuilderUtil.KEY_ORDINAL) {

        if(value instanceof IndicatorType){
          obj = null;
        } else if (keyBuilder != null && value instanceof FudgeMsg) {
          obj = keyBuilder.buildObject(deserializer, (FudgeMsg) value);
        } else if (keyTypeConverter != null) {
          obj = keyTypeConverter.primaryToSecondary(value);
        } else {
          obj = deserializer.fieldValueToObject(field);
        }
        obj = (obj instanceof IndicatorType) ? null : obj;

        if (values.isEmpty()) {
          // no values ready, so store the key till next time
          keys.add(obj);
        } else {
          // store key along with next value
          map.put(obj, values.remove());
        }
      } else if (field.getOrdinal() != null && field.getOrdinal() == BuilderUtil.VALUE_ORDINAL) {

        if(value instanceof IndicatorType){
          obj = null;
        } else if (valueBuilder != null && value instanceof FudgeMsg) {
          obj = valueBuilder.buildObject(deserializer, (FudgeMsg) value);
        } else if (valueTypeConverter != null) {
          obj = valueTypeConverter.primaryToSecondary(value);
        } else {
          obj = deserializer.fieldValueToObject(field);
        }
        obj = (obj instanceof IndicatorType) ? null : obj;

        if (keys.isEmpty()) {
          // no keys ready, so store the value till next time
          values.add(obj);
        } else {
          // store value along with next key
          map.put(keys.remove(), obj);
        }
      } else if (field.getOrdinal() != null && (field.getOrdinal() == BuilderUtil.KEY_TYPE_HINT_ORDINAL || field.getOrdinal() == BuilderUtil.VALUE_TYPE_HINT_ORDINAL)) {
        continue;
      } else {
        throw new IllegalArgumentException("Sub-message interpretted as a map but found invalid ordinal " + field + ")");
      }
    }
    if (keys.size() > 0) {
      throw new IllegalArgumentException("Sub-message interpretted as a map but had more keys than values");
    }
    if (values.size() > 0) {
      throw new IllegalArgumentException("Sub-message interpretted as a map but had more values than keys");
    }
    return map;
  }

}

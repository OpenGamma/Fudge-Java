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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Builder for {@code Map} objects.
 * <p>
 * This builder is immutable and thread safe.
 * 
 * @author Andrew Griffin
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
  /**
   * The ordinal to use for the map key.
   */
  private static final int KEY_ORDINAL = 1;
  /**
   * The ordinal to use for the map value.
   */
  private static final int VALUE_ORDINAL = 2;

  private MapBuilder() {
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Map<?, ?> map) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (entry.getKey() == null) {
        msg.add(null, KEY_ORDINAL, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else {
        context.objectToFudgeMsgWithClassHeaders(msg, null, KEY_ORDINAL, entry.getKey());
      }
      if (entry.getValue() == null) {
        msg.add(null, VALUE_ORDINAL, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else {
        context.objectToFudgeMsgWithClassHeaders(msg, null, VALUE_ORDINAL, entry.getValue());
      }
    }
    return msg;
  }

  @Override
  public Map<?, ?> buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    final Map<Object, Object> map = new HashMap<Object, Object>();
    final Queue<Object> keys = new LinkedList<Object>();
    final Queue<Object> values = new LinkedList<Object>();
    for (FudgeField field : message) {
      Object obj = context.fieldValueToObject(field);
      obj = (obj instanceof IndicatorType) ? null : obj;
      if (field.getOrdinal() != null && field.getOrdinal() == KEY_ORDINAL) {
        if (values.isEmpty()) {
          // no values ready, so store the key till next time
          keys.add(obj);
        } else {
          // store key along with next value
          map.put(obj, values.remove());
        }
      } else if (field.getOrdinal() != null && field.getOrdinal() == VALUE_ORDINAL) {
        if (keys.isEmpty()) {
          // no keys ready, so store the value till next time
          values.add(obj);
        } else {
          // store value along with next key
          map.put(keys.remove(), obj);
        }
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

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

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.types.FudgeTypeConverter;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

import static org.fudgemsg.mapping.BuilderUtil.fieldValueToObject;
import static org.fudgemsg.mapping.BuilderUtil.typeHintsFromFields;

/**
 * Builder for {@code List} objects.
 * <p/>
 * This builder is immutable and thread safe.
 */
/* package */final class ListBuilder implements FudgeBuilder<List<?>> {
  // a list is sent as a sub-message where each field is a list element
  // each list element has neither a name nor an ordinal
  // nulls are sent using the indicator type
  // a set may be read into a list

  /**
   * Singleton instance of the builder.
   */
  /* package */static final FudgeBuilder<List<?>> INSTANCE = new ListBuilder();

  private ListBuilder() {
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, List<?> list) {
    final MutableFudgeMsg msg = serializer.newMessage();

    List<Class<?>> topTypesKeys = BuilderUtil.getTopTypes(list);

    if (list.isEmpty()) {
      msg.add(BuilderUtil.VALUE_TYPE_HINT_ORDINAL, null);
    } else {
      for (Class<?> topType : topTypesKeys) {
        // we are hinting the List that all its entries should have common type
        msg.add(null, BuilderUtil.VALUE_TYPE_HINT_ORDINAL, FudgeWireType.STRING, topType.getName());
      }

      for (Object entry : list) {
        if (entry == null) {
          msg.add(null, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
        } else {
          serializer.addToMessage(msg, null, null, entry);
        }
      }
    }
    return msg;
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  @Override
  public List<?> buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    final List<Object> list = new ArrayList<>();
    final List<FudgeField> typeHints = message.getAllByOrdinal(BuilderUtil.VALUE_TYPE_HINT_ORDINAL);
    for (FudgeField field : message) {
      if ((field.getOrdinal() != null) && (field.getOrdinal() != BuilderUtil.VALUE_TYPE_HINT_ORDINAL)) {
        throw new IllegalArgumentException("Sub-message interpretted as a list but found invalid ordinal " + field + ")");
      }

      if (field.getOrdinal() != null && field.getOrdinal() == BuilderUtil.VALUE_TYPE_HINT_ORDINAL) {
        continue;
      }

      final Object value = field.getValue();
      final Object obj;

      if(value instanceof IndicatorType){
        obj = null;
      } else {
        obj = fieldValueToObject(deserializer, typeHintsFromFields(deserializer, typeHints), field);
      }
      list.add((obj instanceof IndicatorType) ? null : obj);

    }
    return list;
  }

}

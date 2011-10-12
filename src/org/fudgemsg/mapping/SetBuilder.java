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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.types.FudgeTypeConverter;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Builder for {@code Set} objects.
 * <p/>
 * This builder is immutable and thread safe.
 */
/* package */final class SetBuilder implements FudgeBuilder<Set<?>> {
  // a list is sent as a sub-message where each field is a set element
  // each field has the ordinal 1
  // nulls are sent using the indicator type

  /**
   * Singleton instance of the {@link SetBuilder}.
   */
  /* package */static final FudgeBuilder<Set<?>> INSTANCE = new SetBuilder();

  private SetBuilder() {
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Set<?> set) {
    Class theCommonNonAbstractAncestor = BuilderUtil.getCommonNonAbstractAncestorOfObjects(set);
    final MutableFudgeMsg msg = serializer.newMessage();

    if (theCommonNonAbstractAncestor != null) {
      // we are hinting the List that all its entries should have common type
      msg.add(null, BuilderUtil.KEY_TYPE_HINT_ORDINAL, FudgeWireType.STRING, theCommonNonAbstractAncestor.getName());
    }
    for (Object entry : set) {
      if (entry == null) {
        msg.add(null, 1, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else {
        serializer.addToMessageWithClassHeaders(msg, null, BuilderUtil.KEY_ORDINAL, entry);
      }
    }
    return msg;
  }

  @Override
  public Set<?> buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    final Set<Object> set = new HashSet<Object>();

    final List<FudgeField> typeHints = message.getAllByOrdinal(BuilderUtil.KEY_TYPE_HINT_ORDINAL);
    FudgeObjectBuilder<?> entryBuilder = BuilderUtil.findObjectBuilder(deserializer, typeHints);
    FudgeTypeConverter typeConverter = BuilderUtil.findTypeConverter(deserializer, typeHints);

    for (FudgeField field : message) {
      if ((field.getOrdinal() == null) && (field.getOrdinal() != BuilderUtil.KEY_ORDINAL) && (field.getOrdinal() != BuilderUtil.KEY_TYPE_HINT_ORDINAL)) {
        throw new IllegalArgumentException("Sub-message interpretted as a set but found invalid ordinal " + field + ")");
      } else if (field.getOrdinal() == BuilderUtil.KEY_TYPE_HINT_ORDINAL) {
        continue;
      } else {
        final Object value = field.getValue();
        final Object obj;
        if(value instanceof IndicatorType){
          obj = null;
        } else if (entryBuilder != null && value instanceof FudgeMsg) {
          obj = entryBuilder.buildObject(deserializer, (FudgeMsg) value);
        } else if (typeConverter != null) {
          obj = typeConverter.primaryToSecondary(value);
        } else {
          obj = deserializer.fieldValueToObject(field);
        }
        set.add((obj instanceof IndicatorType) ? null : obj);
      }
    }
    return set;
  }

}

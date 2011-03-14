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
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.types.IndicatorFieldType;
import org.fudgemsg.types.IndicatorType;

/**
 * Builder for {@code List} objects.
 * <p>
 * This builder is immutable and thread safe.
 * 
 * @author Andrew Griffin
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
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, List<?> list) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    for (Object entry : list) {
      if (entry == null) {
        msg.add(null, null, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
      } else {
        context.objectToFudgeMsgWithClassHeaders(msg, null, null, entry);
      }
    }
    return msg;
  }

  @Override
  public List<?> buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    final List<Object> list = new ArrayList<Object>();
    for (FudgeField field : message) {
      if ((field.getOrdinal() != null) && (field.getOrdinal() != 1)) {
        throw new IllegalArgumentException("Sub-message interpretted as a list but found invalid ordinal " + field + ")");
      }
      Object obj = context.fieldValueToObject(field);
      obj = (obj instanceof IndicatorType) ? null : obj;
      list.add(obj);
    }
    return list;
  }

}

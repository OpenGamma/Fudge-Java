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
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.types.IndicatorFieldType;
import org.fudgemsg.types.IndicatorType;

/**
 * Builder for {@code Set} objects.
 * <p>
 * This builder is immutable and thread safe.
 * 
 * @author Andrew Griffin
 */
/* package */final class SetBuilder implements FudgeBuilder<Set<?>> {
  // a list is sent as a sub-message where each field is a set element
  // each field has the ordinal 1
  // nulls are sent using the indicator type

  /**
   * Singleton instance of the {@link SetBuilder}.
   */
  /* package */static final FudgeBuilder<Set<?>> INSTANCE = new SetBuilder();
  /**
   * The ordinal to use for the set.
   */
  private static final int ORDINAL = 1;

  private SetBuilder() {
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Set<?> set) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    for (Object entry : set) {
      if (entry == null) {
        msg.add(null, 1, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
      } else {
        context.objectToFudgeMsgWithClassHeaders(msg, null, ORDINAL, entry);
      }
    }
    return msg;
  }

  @Override
  public Set<?> buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    final Set<Object> set = new HashSet<Object>();
    for (FudgeField field : message) {
      if ((field.getOrdinal() == null) && (field.getOrdinal() != ORDINAL)) {
        throw new IllegalArgumentException("Sub-message interpretted as a set but found invalid ordinal " + field + ")");
      }
      Object obj = context.fieldValueToObject(field);
      obj = (obj instanceof IndicatorType) ? null : obj;
      set.add(obj);
    }
    return set;
  }

}

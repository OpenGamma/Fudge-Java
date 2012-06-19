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

import java.util.Iterator;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

/**
 * Builder wrapper for objects that are already Fudge messages.
 * <p>
 * The FudgeFieldContainer class name is added so that the serialization framework will
 * decode the messages as messages and not as serialized objects.
 * <p>
 * This builder is immutable and thread safe.
 */
/* package */class FudgeFieldContainerBuilder implements FudgeBuilder<FudgeMsg> {

  /**
   * Singleton instance.
   */
  /* package */static final FudgeBuilder<FudgeMsg> INSTANCE = new FudgeFieldContainerBuilder();

  private FudgeFieldContainerBuilder() {
  }

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FudgeMsg fields) {
    final MutableFudgeMsg msg = serializer.newMessage(fields);
    // add the interface name
    msg.add(null, FudgeSerializer.TYPES_HEADER_ORDINAL, FudgeMsg.class.getName());
    return msg;
  }

  @Override
  public FudgeMsg buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    final MutableFudgeMsg msg = deserializer.getFudgeContext().newMessage(message);
    // remove the class name(s) if added
    final Integer ordinal = FudgeSerializer.TYPES_HEADER_ORDINAL;
    final Iterator<FudgeField> fields = msg.iterator();
    while (fields.hasNext()) {
      final FudgeField field = fields.next();
      if (ordinal.equals(field.getOrdinal()) && (field.getName() == null)) {
        fields.remove();
        break;
      }
    }
    return msg;
  }

}

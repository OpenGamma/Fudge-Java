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

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Builder for {@code Class} objects.
 * <p>
 * A Java class name is mapped to a string in the Fudge message.
 * <p>
 * This builder is immutable and thread safe.
 * 
 * @author Andrew Griffin
 */
/* package */final class JavaClassBuilder implements FudgeBuilder<Class<?>> {

  /**
   * Singleton instance of the builder.
   */
  /* package */static final FudgeBuilder<Class<?>> INSTANCE = new JavaClassBuilder();

  private JavaClassBuilder() {
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Class<?> object) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    FudgeSerializationContext.addClassHeader(msg, object.getClass());
    msg.add("name", null, FudgeWireType.STRING, object.getName());
    return msg;
  }

  @Override
  public Class<?> buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    final String str = message.getString("name");
    if (str == null) {
      throw new IllegalArgumentException("Sub-message doesn't contain a Java class name");
    }
    try {
      return Class.forName(str);
    } catch (ClassNotFoundException ex) {
      throw new FudgeRuntimeException("Unable to deserialise Java class '" + str + "'", ex);
    }
  }

}

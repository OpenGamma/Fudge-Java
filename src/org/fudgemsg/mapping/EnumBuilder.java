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

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

/**
 * Builder for enums, stored as a sub-message.
 * <p>
 * This builder creates a sub-message. Note that an enumeration could alternatively
 * be reduced to a string but that then requires the receiver to know the type.
 * <p>
 * This builder is immutable and thread safe.
 * 
 * @param <E> the enumeration type
 */
/* package */final class EnumBuilder<E extends Enum<E>> implements FudgeBuilder<Enum<E>> {

  /**
   * The enum class.
   */
  private final Class<E> _clazz;

  /**
   * @param clazz type of the enumeration
   */
  /* package */EnumBuilder(Class<E> clazz) {
    _clazz = clazz;
  }

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, Enum<E> enumeration) {
    final MutableFudgeMsg msg = context.newMessage();
    // REVIEW: jim 2-Jun-2010 -- changed to getDeclaringClass() to fix problem with enums with methods that appear as anon inner classes.
    msg.add(null, 0, enumeration.getDeclaringClass().getName());
    msg.add(null, 1, enumeration.name());
    return msg;
  }

  @Override
  public Enum<E> buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    return Enum.valueOf(_clazz, message.getString(1));
  }

}

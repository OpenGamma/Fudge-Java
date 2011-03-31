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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;

/**
 * Builder for any object that contains a {@code fromFudgeMsg} method.
 * <p>
 * To be detected, a class must contain one of the following method signatures,
 * which are searched in the order shown:
 * <pre>
 *    static <T> fromFudgeMsg(FudgeFieldContainer) or
 *    static <T> fromFudgeMsg(FudgeDeserialisationContext, FudgeFieldContainer)
 * </pre>
 * This is normally paired with a {@link ToFudgeMsgMessageBuilder}.
 * <p>
 * This builder is immutable and thread safe.
 * 
 * @param <T> class supporting a {@code fromFudgeMsg} method which can be deserialised by this builder
 */
/* package */class FromFudgeMsgObjectBuilder<T> implements FudgeObjectBuilder<T> {

  /**
   * Checks a class for a  {@code fromFudgeMsg} method, creating the builder if possible.
   * 
   * @param <T> target class to build from the message 
   * @param clazz  the class to search for a {@code fromFudgeMsg} method, not null
   * @return the builder, null if no matching method
   */
  /* package */static <T> FromFudgeMsgObjectBuilder<T> create(final Class<T> clazz) {
    try {
      return new FromFudgeMsgObjectBuilder<T>(clazz.getMethod("fromFudgeMsg", FudgeDeserializationContext.class,
          FudgeFieldContainer.class), true);
    } catch (SecurityException ex) {
      // ignore
    } catch (NoSuchMethodException ex) {
      // ignore
    }
    try {
      return new FromFudgeMsgObjectBuilder<T>(clazz.getMethod("fromFudgeMsg", FudgeFieldContainer.class), false);
    } catch (SecurityException ex) {
      // ignore
    } catch (NoSuchMethodException ex) {
      // ignore
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * The method to use.
   */
  private final Method _fromFudgeMsg;
  /**
   * Whether to pass the context.
   */
  private final boolean _passContext;

  private FromFudgeMsgObjectBuilder(final Method fromFudgeMsg, final boolean passContext) {
    _fromFudgeMsg = fromFudgeMsg;
    _passContext = passContext;
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public T buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    try {
      return (T) (_passContext ? _fromFudgeMsg.invoke(null, context, message) : _fromFudgeMsg.invoke(null, message));
    } catch (IllegalArgumentException ex) {
      throw new FudgeRuntimeException("Unable to call fromFudgeMsg", ex);
    } catch (IllegalAccessException ex) {
      throw new FudgeRuntimeException("Unable to call fromFudgeMsg", ex);
    } catch (InvocationTargetException ex) {
      throw new FudgeRuntimeException("Unable to call fromFudgeMsg", ex);
    }
  }

}

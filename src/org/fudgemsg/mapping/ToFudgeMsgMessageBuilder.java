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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;

/**
 * Builder for any object that contains a {@code toFudgeMsg} method.
 * <p>
 * To be detected, a class must contain one of the following method signatures,
 * which are searched in the order shown:
 * <pre>
 *   void toFudgeMsg(FudgeSerialisationContext, MutableFudgeFieldContainer)
 *   void toFudgeMsg(FudgeMessageFactory, MutableFudgeFieldContainer)
 *   FudgeMsg toFudgeMsg(FudgeSerialisationContext)
 *   FudgeMsg toFudgeMsg(FudgeMessageFactory)
 *   void toFudgeMsg(FudgeContext, MutableFudgeFieldContainer)
 *   FudgeMsg toFudgeMsg(FudgeContext)
 * </pre>
 * This is normally paired with a {@link FromFudgeMsgObjectBuilder}.
 * <p>
 * This builder is immutable and thread safe.
 * 
 * @param <T> class that can be serialized using this builder
 */
/* package */ abstract class ToFudgeMsgMessageBuilder<T> implements FudgeMessageBuilder<T> {

  /**
   * Checks a class for a  {@code toFudgeMsg} method, creating the builder if possible.
   * 
   * @param <T> target class to build from the message 
   * @param clazz  the class to search for a {@code toFudgeMsg} method, not null
   * @return the builder, null if no matching method
   */
  /* package */static <T> ToFudgeMsgMessageBuilder<T> create(final Class<T> clazz) {
    try {
      return new AddFields<T>(clazz.getMethod("toFudgeMsg", FudgeSerializationContext.class,
          MutableFudgeMsg.class), false);
    } catch (SecurityException ex) {
      // ignore
    } catch (NoSuchMethodException ex) {
      // ignore
    }
    try {
      return new AddFields<T>(
          clazz.getMethod("toFudgeMsg", FudgeMsgFactory.class, MutableFudgeMsg.class), false);
    } catch (SecurityException ex) {
      // ignore
    } catch (NoSuchMethodException ex) {
      // ignore
    }
    try {
      return new CreateMessage<T>(clazz.getMethod("toFudgeMsg", FudgeSerializationContext.class), false);
    } catch (SecurityException ex) {
      // ignore
    } catch (NoSuchMethodException ex) {
      // ignore
    }
    try {
      return new CreateMessage<T>(clazz.getMethod("toFudgeMsg", FudgeMsgFactory.class), false);
    } catch (SecurityException ex) {
      // ignore
    } catch (NoSuchMethodException ex) {
      // ignore
    }
    try {
      return new AddFields<T>(clazz.getMethod("toFudgeMsg", FudgeContext.class, MutableFudgeMsg.class), true);
    } catch (SecurityException ex) {
      // ignore
    } catch (NoSuchMethodException ex) {
      // ignore
    }
    try {
      return new CreateMessage<T>(clazz.getMethod("toFudgeMsg", FudgeContext.class), true);
    } catch (SecurityException ex) {
      // ignore
    } catch (NoSuchMethodException ex) {
      // ignore
    }
    return null;
  }

  //-------------------------------------------------------------------------
  private final Method _toFudgeMsg;

  private ToFudgeMsgMessageBuilder(final Method toFudgeMsg) {
    _toFudgeMsg = toFudgeMsg;
  }

  //-------------------------------------------------------------------------
  /**
   * Invoke the {@code toFudgeMsg} method on the object.
   * 
   * @param obj object to invoke the method on
   * @param args parameters to pass
   * @return the value returned by the {@code toFudgeMsg} if any
   */
  protected Object invoke(Object obj, Object... args) {
    try {
      return _toFudgeMsg.invoke(obj, args);
    } catch (IllegalArgumentException ex) {
      throw new FudgeRuntimeException("Unable to call 'toFudgeMsg' on '" + obj + "'", ex);
    } catch (IllegalAccessException ex) {
      throw new FudgeRuntimeException("Unable to call 'toFudgeMsg' on '" + obj + "'", ex);
    } catch (InvocationTargetException ex) {
      throw new FudgeRuntimeException("Unable to call 'toFudgeMsg' on '" + obj + "'", ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Builder used when the method creates a new sub-message.
   */
  private static final class CreateMessage<T> extends ToFudgeMsgMessageBuilder<T> {
    private final boolean _passContext;

    private CreateMessage(final Method toFudgeMsg, final boolean passContext) {
      super(toFudgeMsg);
      _passContext = passContext;
    }

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializationContext context, T object) {
      return (MutableFudgeMsg) invoke(object, _passContext ? context.getFudgeContext() : context);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Builder used when the method populates a supplied sub-message.
   */
  private static final class AddFields<T> extends ToFudgeMsgMessageBuilder<T> {
    private final boolean _passContext;

    private AddFields(final Method toFudgeMsg, final boolean passContext) {
      super(toFudgeMsg);
      _passContext = passContext;
    }

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializationContext context, T object) {
      final MutableFudgeMsg msg = context.newMessage();
      invoke(object, _passContext ? context.getFudgeContext() : context, msg);
      return msg;
    }
  }

}
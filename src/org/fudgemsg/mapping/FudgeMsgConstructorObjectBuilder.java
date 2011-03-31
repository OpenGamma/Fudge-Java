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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;

/**
 * Builder for any object that contains a suitable Fudge-based constructor.
 * <p>
 * To be detected, a class must contain one of the following method signatures,
 * which are searched in the order shown:
 * <pre>
 *    constructor(FudgeFieldContainer) or
 *    constructor(FudgeDeserialisationContext, FudgeFieldContainer)
 * </pre>
 * This is normally paired with a {@link ToFudgeMsgMessageBuilder}.
 * <p>
 * This builder is immutable and thread safe.
 * 
 * @param <T> class supporting a Fudge-based constructor which can be deserialised by this builder
 */
/* package */ final class FudgeMsgConstructorObjectBuilder<T> implements FudgeObjectBuilder<T> {

  /**
   * Creates a new {@link FudgeMsgConstructorObjectBuilder} for the class if possible.
   * 
   * @param <T> class the builder should create objects of
   * @param clazz  the class to search for constructors, not null
   * @return the builder, null if no matching method
   */
  /* package */ static <T> FudgeMsgConstructorObjectBuilder<T> create (final Class<T> clazz) {
    try {
      return new FudgeMsgConstructorObjectBuilder<T> (clazz.getConstructor (FudgeDeserializationContext.class, FudgeFieldContainer.class), true);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    try {
      return new FudgeMsgConstructorObjectBuilder<T> (clazz.getConstructor (FudgeFieldContainer.class), false);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * The constructor to use.
   */
  private final Constructor<T> _constructor;
  /**
   * Whether to pass the context.
   */
  private final boolean _passContext;

  private FudgeMsgConstructorObjectBuilder(final Constructor<T> constructor, final boolean passContext) {
    _constructor = constructor;
    _passContext = passContext;
  }

  //-------------------------------------------------------------------------
  @Override
  public T buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    try {
      return _passContext ? _constructor.newInstance(context, message) : _constructor.newInstance(message);
    } catch (IllegalArgumentException ex) {
      throw new FudgeRuntimeException("Unable to create " + _constructor.getDeclaringClass() + " object", ex);
    } catch (InstantiationException ex) {
      throw new FudgeRuntimeException("Unable to create " + _constructor.getDeclaringClass() + " object", ex);
    } catch (IllegalAccessException ex) {
      throw new FudgeRuntimeException("Unable to create " + _constructor.getDeclaringClass() + " object", ex);
    } catch (InvocationTargetException ex) {
      throw new FudgeRuntimeException("Unable to create " + _constructor.getDeclaringClass() + " object", ex);
    }
  }

}

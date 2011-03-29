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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.types.FudgeMsgFieldType;
import org.fudgemsg.types.StringFieldType;

/**
 * Context used during conversion of an object structure to a Fudge message.
 * <p>
 * This is the central point for Java Object serialization to a Fudge message on a given stream.
 * The object builder framework methods all take a serialization context.
 * Note that the serializer cannot process cyclic object graphs at present.
 * <p>
 * <p>
 * This class is mutable but thread-safe via concurrent collections.
 * 
 * @author Andrew Griffin
 */
public class FudgeSerializationContext implements FudgeMessageFactory {

  /**
   * The field ordinal used to send type information.
   */
  public static final int TYPES_HEADER_ORDINAL = 0;

  /**
   * The parent Fudge context.
   */
  private final FudgeContext _fudgeContext;
  /**
   * The buffer for handling object graph cycles.
   */
  private final SerializationBuffer _serialisationBuffer = new SerializationBuffer();

  /**
   * Creates a new context based on a parent context.
   * 
   * @param fudgeContext  the parent context to use, not null
   */
  public FudgeSerializationContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the associated {@link FudgeContext}.
   * 
   * @return the {@code FudgeContext}.
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the buffer used to handle object graph cycles.
   * 
   * @return the buffer, not null
   */
  private SerializationBuffer getSerialisationBuffer() {
    return _serialisationBuffer;
  }

  //-------------------------------------------------------------------------
  /**
   * Resets the buffers used for object graph logics.
   * <p>
   * Calling {@code reset()} on this context should match a call to
   * {@link FudgeSerializationContext#reset()} on the context used by the deserializer
   * to keep the states of both sender and receiver consistent.
   */
  public void reset() {
    getSerialisationBuffer().reset();
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeFieldContainer newMessage() {
    return _fudgeContext.newMessage();
  }

  @Override
  public MutableFudgeFieldContainer newMessage(final FudgeFieldContainer fromMessage) {
    return _fudgeContext.newMessage(fromMessage);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a Java object to a Fudge message form.
   * <p>
   * This may be natively supported or a type known to the {@link FudgeTypeDictionary}.
   * 
   * @param message  the message to add this object to, not null
   * @param name  the field name for the field, null if no field name required
   * @param ordinal  the ordinal for the field, null if no field ordinal required
   * @param object  the value to add, null ignored
   */
  public void objectToFudgeMsg(
      final MutableFudgeFieldContainer message, final String name, final Integer ordinal, final Object object) {
    if (object == null) {
      return;
    }
    final FudgeFieldType fieldType = getFudgeContext().getTypeDictionary().getByJavaType(object.getClass());
    if ((fieldType != null) && !FudgeMsgFieldType.INSTANCE.equals(fieldType)) {
      // goes natively into a message
      message.add(name, ordinal, fieldType, object);
    } else {
      // look up a custom or default builder and embed as sub-message
      message.add(name, ordinal, FudgeMsgFieldType.INSTANCE, objectToFudgeMsg(object));
    }
  }

  /**
   * Converts a Java object to a Fudge message sending class headers.
   * <p>
   * This may be natively supported or a type known to the {@link FudgeTypeDictionary}.
   * If encoded as a sub-message then class header fields are added.
   * These specify the Java type in the message, including superclasses.
   * This makes deserialization easier at the expense of message size.
   * 
   * @param message  the message to add this object to, not null
   * @param name  the field name for the field, null if no field name required
   * @param ordinal  the ordinal for the field, null if no field ordinal required
   * @param object  the value to add, null ignored
   */
  public void objectToFudgeMsgWithClassHeaders(
      final MutableFudgeFieldContainer message, final String name, final Integer ordinal, final Object object) {
    objectToFudgeMsgWithClassHeaders(message, name, ordinal, object, Object.class);
  }

  /**
   * Converts a Java object to a Fudge message sending class headers.
   * <p>
   * This may be natively supported or a type known to the {@link FudgeTypeDictionary}.
   * If encoded as a sub-message then class header fields are added.
   * These specify the Java type in the message, with superclasses up to the specified type.
   * This makes deserialization easier at the expense of message size.
   * <p>
   * By manually specifying the receiver type, the number of superclasses can be reduced.
   * 
   * @param message  the message to add this object to, not null
   * @param name  the field name for the field, null if no field name required
   * @param ordinal  the ordinal for the field, null if no field ordinal required
   * @param object  the value to add, null ignored
   * @param receiverTarget  the Java class the receiver will expect, not null
   */
  public void objectToFudgeMsgWithClassHeaders(
      final MutableFudgeFieldContainer message, final String name,
      final Integer ordinal, final Object object, final Class<?> receiverTarget) {
    if (object == null) {
      return;
    }
    final Class<?> clazz = object.getClass();
    final FudgeFieldType fieldType = getFudgeContext().getTypeDictionary().getByJavaType(clazz);
    if ((fieldType != null) && !FudgeMsgFieldType.INSTANCE.equals(fieldType)) {
      // goes natively into a message
      message.add(name, ordinal, fieldType, object);
    } else {
      // look up a custom or default builder and embed as sub-message
      final MutableFudgeFieldContainer submsg = objectToFudgeMsg(object);
      if (!getFudgeContext().getObjectDictionary().isDefaultObject(clazz)) {
        if (submsg.getByOrdinal(TYPES_HEADER_ORDINAL) == null) {
          addClassHeader(submsg, clazz, receiverTarget);
        }
      }
      message.add(name, ordinal, FudgeMsgFieldType.INSTANCE, submsg);
    }
  }

  /**
   * Converts a Java object to a Fudge message using a registered builder.
   * <p>
   * The builder must be registered in the current {@link FudgeObjectDictionary}.
   * A mutable message is returned allowing the caller to append additional data
   * such as the class header.
   * 
   * @param object  the Java object to serialize, not null
   * @return the Fudge message created, not null
   */
  @SuppressWarnings("unchecked")
  public MutableFudgeFieldContainer objectToFudgeMsg(final Object object) {
    if (object == null) {
      throw new NullPointerException("Object cannot be null");
    }
    getSerialisationBuffer().beginObject(object);
    try {
      Class<?> clazz = object.getClass();
      FudgeMessageBuilder<Object> builder = getFudgeContext().getObjectDictionary().getMessageBuilder((Class<Object>) clazz);
      return builder.buildMessage(this, object);
    } finally {
      getSerialisationBuffer().endObject(object);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds class names to a message with field ordinal 0 for use by a deserializer.
   * <p>
   * The preferred class name is written first, followed by subsequent super-classes
   * that may be acceptable if the deserializer doesn't recognize them.
   * 
   * @param message  the message to add the fields to, not null
   * @param clazz  the Java class to add type data for, not null
   * @return the modified message, for method chaining, not null
   */
  public static MutableFudgeFieldContainer addClassHeader(final MutableFudgeFieldContainer message, Class<?> clazz) {
    while ((clazz != null) && (clazz != Object.class)) {
      message.add(null, TYPES_HEADER_ORDINAL, StringFieldType.INSTANCE, clazz.getName());
      clazz = clazz.getSuperclass();
    }
    return message;
  }

  /**
   * Adds partial class names to a message with field ordinal 0 for use by a deserializer.
   * <p>
   * The preferred class name is written first, followed by subsequent super-classes
   * that may be acceptable. It is assumed that the deserializer will already know the
   * target class by other means, so the message payload ends up being smaller
   * than with {@link #addClassHeader(MutableFudgeFieldContainer,Class)}.
   * 
   * @param message  the message to add the fields to, not null
   * @param clazz  the Java class to add type data for, not null
   * @param receiverTarget  the Java class the receiver will expect, not null
   * @return the modified message, for method chaining, not null
   */
  public static MutableFudgeFieldContainer addClassHeader(
      final MutableFudgeFieldContainer message, Class<?> clazz, Class<?> receiverTarget) {
    while ((clazz != null) && receiverTarget.isAssignableFrom(clazz) && (receiverTarget != clazz)) {
      message.add(null, TYPES_HEADER_ORDINAL, StringFieldType.INSTANCE, clazz.getName());
      clazz = clazz.getSuperclass();
    }
    return message;
  }

}

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
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Context used during conversion of an object structure to a Fudge message.
 * <p>
 * This is the central point for Java Object serialization to a Fudge message on a given stream.
 * The object builder framework methods all take a serialization context.
 * Note that the serializer cannot process cyclic object graphs at present.
 * <p>
 * This class is mutable but thread-safe via concurrent collections.
 */
public class FudgeSerializationContext implements FudgeMsgFactory {

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
  public MutableFudgeMsg newMessage() {
    return _fudgeContext.newMessage();
  }

  @Override
  public MutableFudgeMsg newMessage(final FudgeMsg fromMessage) {
    return _fudgeContext.newMessage(fromMessage);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an object to a Fudge message in the simplest way.
   * <p>
   * The object must be a native type or a type known to the {@link FudgeTypeDictionary}.
   * A complex object sent using this method may not be able to be converted back to
   * an equivalent object at the other end as only basic information about the type is sent.
   * 
   * @param message  the message to add this object to, not null
   * @param name  the field name for the field, null if no field name required
   * @param ordinal  the ordinal for the field, null if no field ordinal required
   * @param object  the value to add, null ignored
   */
  public void addToMessage(
      final MutableFudgeMsg message, final String name, final Integer ordinal, final Object object) {
    if (object == null) {
      return;
    }
    final FudgeFieldType fieldType = getFudgeContext().getTypeDictionary().getByJavaType(object.getClass());
    if (isNative(fieldType, object)) {
      message.add(name, ordinal, fieldType, object);
    } else {
      // look up a custom or default builder and embed as sub-message
      message.add(name, ordinal, FudgeWireType.SUB_MESSAGE, objectToFudgeMsg(object));
    }
  }

  /**
   * Adds an object to a Fudge message sending class name headers.
   * <p>
   * The object must be a native type or a type known to the {@link FudgeTypeDictionary}.
   * If it is a complex object, it will be converted to a sub-message.
   * The sub-message will include the Java type as a header to allow full deserialization.
   * 
   * @param message  the message to add this object to, not null
   * @param name  the field name for the field, null if no field name required
   * @param ordinal  the ordinal for the field, null if no field ordinal required
   * @param object  the value to add, null ignored
   */
  public void addToMessageWithClassHeaders(
      final MutableFudgeMsg message, final String name, final Integer ordinal, final Object object) {
    addToMessageWithClassHeaders(message, name, ordinal, object, Object.class);
  }

  /**
   * Adds an object to a Fudge message sending class name headers.
   * <p>
   * The object must be a native type or a type known to the {@link FudgeTypeDictionary}.
   * If it is a complex object, it will be converted to a sub-message.
   * The sub-message will include the Java type as a header to allow full deserialization.
   * <p>
   * Class name headers will only be sent up to the specified type (exclusive).
   * This handles subclasses of a known type effectively, reducing the message size.
   * 
   * @param message  the message to add this object to, not null
   * @param name  the field name for the field, null if no field name required
   * @param ordinal  the ordinal for the field, null if no field ordinal required
   * @param object  the value to add, null ignored
   * @param receiverTarget  the Java class the receiver will expect, not null
   */
  public void addToMessageWithClassHeaders(
      final MutableFudgeMsg message, final String name,
      final Integer ordinal, final Object object, final Class<?> receiverTarget) {
    if (object == null) {
      return;
    }
    final Class<?> clazz = object.getClass();
    final FudgeFieldType fieldType = getFudgeContext().getTypeDictionary().getByJavaType(clazz);
    if (isNative(fieldType, object)) {
      message.add(name, ordinal, fieldType, object);
    } else {
      // look up a custom or default builder and embed as sub-message
      final MutableFudgeMsg submsg = objectToFudgeMsg(object);
      if (!getFudgeContext().getObjectDictionary().isDefaultObject(clazz)) {
        if (submsg.getByOrdinal(TYPES_HEADER_ORDINAL) == null) {
          addClassHeader(submsg, clazz, receiverTarget);
        }
      }
      message.add(name, ordinal, FudgeWireType.SUB_MESSAGE, submsg);
    }
  }

  /**
   * Checks if the object is in the correct native format to send.
   * 
   * @param fieldType  the Fudge type, may be null
   * @param object  the value to add, not null
   * @return true if the object can be sent natively
   */
  private boolean isNative(final FudgeFieldType fieldType, final Object object) {
    if (fieldType == null) {
      return false;
    }
    return FudgeWireType.SUB_MESSAGE.equals(fieldType) == false ||
            (FudgeWireType.SUB_MESSAGE.equals(fieldType) && object instanceof FudgeMsg);
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
  public MutableFudgeMsg objectToFudgeMsg(final Object object) {
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
  public static MutableFudgeMsg addClassHeader(final MutableFudgeMsg message, Class<?> clazz) {
    while (clazz != null && clazz != Object.class) {
      message.add(null, TYPES_HEADER_ORDINAL, FudgeWireType.STRING, clazz.getName());
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
   * than with {@link #addClassHeader(MutableFudgeMsg,Class)}.
   * 
   * @param message  the message to add the fields to, not null
   * @param clazz  the Java class to add type data for, not null
   * @param receiverTarget  the Java class the receiver will expect, not null
   * @return the modified message, for method chaining, not null
   */
  public static MutableFudgeMsg addClassHeader(
      final MutableFudgeMsg message, Class<?> clazz, Class<?> receiverTarget) {
    while (clazz != null && receiverTarget.isAssignableFrom(clazz) && receiverTarget != clazz) {
      message.add(null, TYPES_HEADER_ORDINAL, FudgeWireType.STRING, clazz.getName());
      clazz = clazz.getSuperclass();
    }
    return message;
  }

}

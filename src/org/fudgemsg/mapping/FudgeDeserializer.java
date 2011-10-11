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

import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeTypeDictionary;

/**
 * Deserializer used to control the conversion of a Fudge message to an object structure.
 * <p>
 * This is the central point for Fudge message to Java Object deserialization on a given stream.
 * Note that the deserializer cannot process cyclic object graphs at present.
 * <p>
 * The object builder framework methods all take a deserialization context so that a
 * deserializer can refer any sub-messages to this for construction if it does not have
 * sufficient information to process them directly.
 * <p>
 * This class is mutable and intended for use by a single thread.
 */
public class FudgeDeserializer {

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
  public FudgeDeserializer(final FudgeContext fudgeContext) {
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
   * {@link FudgeSerializer#reset()} on the context used by the serializer
   * to keep the states of both sender and receiver consistent.
   */
  public void reset() {
    getSerialisationBuffer().reset();
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a field value to a Java object.
   * <p>
   * This may be natively supported or a type known to the {@link FudgeTypeDictionary}.
   * A sub-message will be expanded through {@link #fudgeMsgToObject(FudgeMsg)}.
   * 
   * @param field  the field to convert, not null
   * @return the deserialized object
   */
  public Object fieldValueToObject(final FudgeField field) {
    final Object value = field.getValue();
    if (value instanceof FudgeMsg) {
      return fudgeMsgToObject((FudgeMsg) value);
    } else {
      return value;
    }
  }

  /**
   * Converts a field value to a Java object with a specific type.
   * <p>
   * This may be natively supported or a type known to the {@link FudgeTypeDictionary}.
   * A sub-message will be expanded through {@link #fudgeMsgToObject(Class,FudgeMsg)}.
   * 
   * @param <T> target Java type to decode to
   * @param clazz  the class of the target Java type to decode to, not null
   * @param field  the value to convert, not null
   * @return the deserialized object
   */
  public <T> T fieldValueToObject(final Class<T> clazz, final FudgeField field) {
    final Object value = field.getValue();
    if (value instanceof FudgeMsg) {
      return fudgeMsgToObject(clazz, (FudgeMsg) value);
    } else {
      return getFudgeContext().getFieldValue(clazz, field);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a Fudge message to a best guess Java object.
   * <p>
   * {@link List} and {@link Map} encodings are recognized and inflated.
   * Any other encodings require field ordinal 0 to include possible class names to use.
   * 
   * @param message  the message to deserialize, not null
   * @return the deserialized object
   */
  public Object fudgeMsgToObject(final FudgeMsg message) {
    List<FudgeField> types = message.getAllByOrdinal(FudgeSerializer.TYPES_HEADER_ORDINAL);
    if (types.size() == 0) {
      // no types passed in ordinal zero
      int maxOrdinal = 0;
      boolean typeHinted = false;
      for (FudgeField field : message) {
        if (field.getOrdinal() == null) {
          continue;
        }
        if (field.getOrdinal() < 0) {
          // not a list/set/map
          return message;
        }
        if (field.getOrdinal() != BuilderUtil.KEY_TYPE_HINT_ORDINAL && field.getOrdinal() != BuilderUtil.VALUE_TYPE_HINT_ORDINAL && field.getOrdinal() > maxOrdinal) {
          maxOrdinal = field.getOrdinal();
        }
      }
      final Class<?> defaultClass = getFudgeContext().getObjectDictionary().getDefaultObjectClass(maxOrdinal);
      if (defaultClass != null) {
        return fudgeMsgToObject(defaultClass, message);
      }
    } else {
      // types passed in ordinal zero
      for (FudgeField type : types) {
        final Object obj = type.getValue();
        if (obj instanceof Number) {
          throw new UnsupportedOperationException("Serialisation framework does not support back/forward references");
        } else if (obj instanceof String) {
          try {
            Class<?> cls = getFudgeContext().getTypeDictionary().loadClass((String) obj);
            FudgeObjectBuilder<?> builder = getFudgeContext().getObjectDictionary().getObjectBuilder(cls);
            if (builder != null) {
              return builder.buildObject(this, message);
            }
          } catch (ClassNotFoundException ex) {
            // ignore
          }
        }
      }
    }
    // couldn't process - return the raw message
    return message;
  }

  /**
   * Converts a Fudge message to a specific Java type.
   * <p>
   * The {@link FudgeObjectDictionary} is used to identify a builder to delegate to.
   * If the message includes class names in ordinal 0, these will be tested for a valid
   * builder and used if they will provide a subclass of the requested class.
   * 
   * @param <T> target Java type to decode to
   * @param clazz  the class of the target Java type to decode to, not null
   * @param message  the message to deserialize, not null
   * @return the deserialized object
   */
  @SuppressWarnings("unchecked")
  public <T> T fudgeMsgToObject(final Class<T> clazz, final FudgeMsg message) {
    if (clazz == FudgeMsg.class) {
      return (T) message;
    }
    FudgeObjectBuilder<T> builder;
    Exception lastError = null;
    /*if (clazz == Object.class) {
      System.out.println(message);
    }*/
    List<FudgeField> types = message.getAllByOrdinal(FudgeSerializer.TYPES_HEADER_ORDINAL);
    if (types.size() != 0) {
      // types passed in ordinal zero - use it if we can
      for (FudgeField type : types) {
        final Object obj = type.getValue();
        if (obj instanceof Number) {
          throw new UnsupportedOperationException("Serialisation framework does not support back/forward references");
        } else if (obj instanceof String) {
          try {
            final Class<?> possibleClazz = getFudgeContext().getTypeDictionary().loadClass((String) obj);
            // System.out.println("Trying " + possibleClazz);
            if (clazz.isAssignableFrom(possibleClazz)) {
              builder = (FudgeObjectBuilder<T>) getFudgeContext().getObjectDictionary().getObjectBuilder(possibleClazz);
              // System.out.println("Builder " + builder);
              if (builder != null) {
                return builder.buildObject(this, message);
              }
            }
          } catch (ClassNotFoundException ex) {
            // ignore
          } catch (Exception ex) {
            //e.printStackTrace();
            lastError = ex;
          }
        }
      }
    }
    // try the requested type
    //System.out.println ("fallback to " + clazz);
    builder = getFudgeContext().getObjectDictionary().getObjectBuilder(clazz);
    if (builder != null) {
      try {
        return builder.buildObject(this, message);
      } catch (Exception ex) {
        lastError = ex;
      }
    }
    // nothing matched
    if (lastError != null) {
      throw new FudgeRuntimeException("Unable to create " + clazz + " from " + message, lastError);
    } else {
      throw new IllegalArgumentException("Unable to create " + clazz + " from " + message);
    }
  }

}

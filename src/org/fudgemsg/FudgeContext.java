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
package org.fudgemsg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.InputStream;
import java.io.OutputStream;

import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeObjectDictionary;
import org.fudgemsg.mapping.FudgeObjectReader;
import org.fudgemsg.mapping.FudgeObjectWriter;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.taxonomy.FudgeTaxonomy;
import org.fudgemsg.taxonomy.ImmutableMapTaxonomyResolver;
import org.fudgemsg.taxonomy.TaxonomyResolver;
import org.fudgemsg.wire.FudgeDataInputStreamReader;
import org.fudgemsg.wire.FudgeDataOutputStreamWriter;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.FudgeStreamReader;
import org.fudgemsg.wire.FudgeStreamWriter;

/**
 * <p>The primary entry-point for code to interact with the rest of the Fudge system.
 * For performance reasons, there are many options that are passed around as parameters
 * inside static methods for encoding and decoding, and many lightweight objects that
 * ideally don't know of their configuration context. However, in a large application,
 * it is often desirable to collect all configuration parameters in one location and
 * inject options into it.</p>
 * 
 * <p>{@code FudgeContext} allows application developers to have a single location
 * to inject dependent parameters and instances, and make them available through
 * simple method invocations. In addition, because it wraps all checked exceptions
 * into instances of {@link FudgeRuntimeException}, it is the ideal way to use
 * the Fudge encoding system from within Spring applications.</p>
 * 
 * <p>While most applications will have a single instance of {@code FudgeContext},
 * some applications will have one instance per unit of encoding/decoding parameters.
 * For example, if an application is consuming data from two messaging feeds, each
 * of which reuses the same taxonomy ID to represent a different
 * {@link FudgeTaxonomy}, it would configure two different instances of
 * {@code FudgeContext}, one per feed.</p>
 */
public class FudgeContext implements FudgeMsgFactory {

  /**
   * A default global {@link FudgeContext} for getting code up and running quickly.
   * The context cannot be modified in any way so can only be used for the core Fudge
   * data types and will not support a taxonomy resolver.
   * This should be used for trivial projects and code only.
   */
  public static final FudgeContext GLOBAL_DEFAULT = new UnmodifiableFudgeContext(new FudgeContext());
  /**
   * A global empty {@link FudgeMsg}.
   */
  public static final FudgeMsg EMPTY_MESSAGE = new UnmodifiableFudgeMsg(GLOBAL_DEFAULT);
  /**
   * A global empty {@link FudgeMsgEnvelope}.
   */
  public static final FudgeMsgEnvelope EMPTY_MESSAGE_ENVELOPE = new FudgeMsgEnvelope(EMPTY_MESSAGE);

  /**
   * The taxonomy resolver.
   */
  private TaxonomyResolver _taxonomyResolver;
  /**
   * The type dictionary.
   */
  private FudgeTypeDictionary _typeDictionary;
  /**
   * The object dictionary.
   */
  private FudgeObjectDictionary _objectDictionary;

  /**
   * Constructs a new context with default empty dictionaries.
   */
  public FudgeContext() {
    _taxonomyResolver = ImmutableMapTaxonomyResolver.EMPTY;
    _typeDictionary = new FudgeTypeDictionary();
    _objectDictionary = new FudgeObjectDictionary();
  }

  /**
   * Constructs a new context with a copy of the dictionaries and taxonomy resolver
   * from another context.
   * 
   * @param other  the context to copy the dictionaries and taxonomy resolver from, not null
   */
  public FudgeContext(final FudgeContext other) {
    _taxonomyResolver = other.getTaxonomyResolver();
    _typeDictionary = new FudgeTypeDictionary(other.getTypeDictionary());
    _objectDictionary = new FudgeObjectDictionary(other.getObjectDictionary());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the taxonomy resolver being used by this context.
   * <p>
   * This is used to manage taxonomies for any messages created or decoded through the context.
   * A new {@code FudgeContext} starts with its own, default, taxonomy resolver.
   * Any custom taxonomies must be registered with a resolver before they can be used.
   * 
   * @return the taxonomy resolver, not null
   */
  public TaxonomyResolver getTaxonomyResolver() {
    return _taxonomyResolver;
  }

  /**
   * Sets the taxonomy resolver to be used by this context.
   * 
   * @param taxonomyResolver  the taxonomy resolver to set, not null
   */
  public void setTaxonomyResolver(TaxonomyResolver taxonomyResolver) {
    if (taxonomyResolver == null) {
      throw new NullPointerException("TaxonomyResolver must not be null");
    }
    _taxonomyResolver = taxonomyResolver;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the type dictionary used by this context.
   * <p>
   * This is used to manage types for any messages created or decoded through the context.
   * A new {@code FudgeContext} starts with its own, default, type dictionary.
   * Any custom types must be registered with the dictionary before they can be used.
   * 
   * @return the type dictionary, not null
   */
  public FudgeTypeDictionary getTypeDictionary() {
    return _typeDictionary;
  }

  /**
   * Sets the type dictionary to be used by the context.
   * 
   * @param typeDictionary  the type dictionary to set, not null
   */
  public void setTypeDictionary(FudgeTypeDictionary typeDictionary) {
    if (typeDictionary == null) {
      throw new NullPointerException("FudgeTypeDictionary must not be null");
    }
    _typeDictionary = typeDictionary;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the object dictionary used by the context.
   * <p>
   * This is used to manage object serializaton for any messages created or decoded through the context.
   * A new {@code FudgeContext} starts with its own, default, object dictionary.
   * Any custom builders must be registered with the dictionary before they can be used.
   * 
   * @return the object dictionary, not null
   */
  public FudgeObjectDictionary getObjectDictionary() {
    return _objectDictionary;
  }

  /**
   * Sets the object dictionary to be used by the context.
   * 
   * @param objectDictionary  the object dictionary to set, not null
   */
  public void setObjectDictionary(FudgeObjectDictionary objectDictionary) {
    if (objectDictionary == null) {
      throw new NullPointerException("FudgeObjectDictionary must not be null");
    }
    _objectDictionary = objectDictionary;
  }

  //-------------------------------------------------------------------------
  /**
   * Passes this context to the configuration objects supplied to update the type and object dictionaries.
   * This can be used with Bean based frameworks to configure a context for custom types through injection.
   * 
   * @param configurations  the configuration objects to use, not null
   */
  public void setConfiguration(final FudgeContextConfiguration... configurations) {
    for (FudgeContextConfiguration configuration : configurations) {
      configuration.configureFudgeContext(this);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg newMessage() {
    return new StandardFudgeMsg(this);
  }

  @Override
  public MutableFudgeMsg newMessage(final FudgeMsg fromMessage) {
    return new StandardFudgeMsg(this, fromMessage);
  }

  //-------------------------------------------------------------------------
  /**
   * Serializes a Fudge message to the output stream, without using a taxonomy,
   * 
   * @param msg  the message to write
   * @param os  the output stream to write to, not null
   */
  public void serialize(FudgeMsg msg, OutputStream os) {
    serialize(msg, null, os);
  }

  /**
   * Serializes a Fudge message to the output stream, using an optional taxonomy.
   * <p>
   * Taxonomies are used to reduce the size of the binary message.
   * 
   * @param msg  the message to write
   * @param taxonomyId  the identifier of the taxonomy to use, may be null
   * @param os  the output stream to write to, not null
   */
  public void serialize(FudgeMsg msg, Short taxonomyId, OutputStream os) {
    int realTaxonomyId = (taxonomyId == null) ? 0 : taxonomyId.intValue();
    FudgeMsgWriter writer = createMessageWriter(os);
    FudgeMsgEnvelope envelope = new FudgeMsgEnvelope(msg);
    writer.writeMessageEnvelope(envelope, realTaxonomyId);
  }

  /**
   * Returns the Fudge encoded form of a {@link FudgeMsg} as a {@code byte} array
   * with a taxonomy reference. The encoding includes an envelope header.
   * 
   * @param msg  the Fudge message to encode
   * @param taxonomyId  the identifier of the taxonomy to use. Specify {@code null} or {@code 0} for no taxonomy
   * @return an array containing the encoded message
   */
  public byte[] toByteArray(FudgeMsg msg, Short taxonomyId) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    serialize(msg, taxonomyId, baos);
    return baos.toByteArray();
  }

  /**
   * Returns the Fudge encoded form of a {@link FudgeMsg} as a {@code byte} array
   * without a taxonomy reference. The encoding includes an envelope header.
   * 
   * @param msg  the Fudge message to encode
   * @return an array containing the encoded message
   */
  public byte[] toByteArray(FudgeMsg msg) {
    return toByteArray(msg, null);
  }

  //-------------------------------------------------------------------------
  /**
   * Decodes a Fudge message from an {@link InputStream}.
   * 
   *  @param is the {@code InputStream} to read encoded data from
   *  @return the next {@link FudgeMsgEnvelope} encoded on the stream
   */
  public FudgeMsgEnvelope deserialize(InputStream is) {
    FudgeMsgReader reader = createMessageReader(is);
    FudgeMsgEnvelope envelope = reader.nextMessageEnvelope();
    return envelope;
  }

  /**
   * Decodes a Fudge message from a {@code byte} array. If the array is
   * larger than the Fudge envelope, any additional data is ignored.
   * 
   * @param bytes
   *          an array containing the encoded Fudge message including its envelope
   * @return the decoded {@link FudgeMsgEnvelope}
   */
  public FudgeMsgEnvelope deserialize(byte[] bytes) {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    return deserialize(bais);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a new reader for extracting Fudge stream elements from an {@link InputStream}.
   * 
   * @param is the {@code InputStream} to read from
   * @return the {@link FudgeStreamReader}
   */
  public FudgeStreamReader createReader(final InputStream is) {
    return new FudgeDataInputStreamReader(this, is);
  }

  /**
   * Creates a new reader for extracting Fudge stream elements from a {@link DataInput}.
   * 
   * @param di the {@code DataInput} to read from
   * @return the {@link FudgeStreamReader}
   */
  public FudgeStreamReader createReader(final DataInput di) {
    return new FudgeDataInputStreamReader(this, di);
  }

  /**
   * Creates a new writer for encoding Fudge stream elements to a {@link OutputStream}.
   * 
   * @param outputStream the {@code OutputStream} to write to
   * @return the {@link FudgeStreamWriter}
   */
  public FudgeStreamWriter createWriter(final OutputStream outputStream) {
    return new FudgeDataOutputStreamWriter(this, outputStream);
  }

  /**
   * Creates a new writer for encoding Fudge stream elements to a {@link DataOutput}.
   * 
   * @param dataOutput the {@code DataOutput} to write to
   * @return the {@link FudgeStreamWriter}
   */
  public FudgeStreamWriter createWriter(final DataOutput dataOutput) {
    return new FudgeDataOutputStreamWriter(this, dataOutput);
  }

  /**
   * Creates a new reader for extracting whole Fudge messages from a {@link DataInput} source.
   * 
   * @param dataInput the source of data
   * @return the {@code FudgeMsgReader}
   */
  public FudgeMsgReader createMessageReader(final DataInput dataInput) {
    return new FudgeMsgReader(createReader(dataInput));
  }

  /**
   * Creates a new reader for extracting whole Fudge messages from a {@link InputStream} source.
   * 
   * @param inputStream the source of data
   * @return the {@code FudgeMsgReader}
   */
  public FudgeMsgReader createMessageReader(final InputStream inputStream) {
    return new FudgeMsgReader(createReader(inputStream));
  }

  /**
   * Creates a new writer for sending whole Fudge messages to a {@link DataOutput} target.
   * 
   * @param dataOutput the target to write to
   * @return the {@link FudgeMsgWriter}
   */
  public FudgeMsgWriter createMessageWriter(final DataOutput dataOutput) {
    return new FudgeMsgWriter(createWriter(dataOutput));
  }

  /**
   * Creates a new writer for sending whole Fudge messages to a {@link OutputStream} target.
   * 
   * @param outputStream the target to write to
   * @return the {@link FudgeMsgWriter}
   */
  public FudgeMsgWriter createMessageWriter(final OutputStream outputStream) {
    return new FudgeMsgWriter(createWriter(outputStream));
  }

  /**
   * Creates a new reader for deserialising Java objects from a Fudge data source.
   * 
   * @param dataInput the {@code DataInput} to read from
   * @return the {@link FudgeObjectReader}
   */
  public FudgeObjectReader createObjectReader(final DataInput dataInput) {
    return new FudgeObjectReader(createMessageReader(dataInput));
  }

  /**
   * Creates a new reader for deserialising Java objects from a Fudge data source.
   * 
   * @param inputStream the {@code InputStream} to read from
   * @return the {@link FudgeObjectReader}
   */
  public FudgeObjectReader createObjectReader(final InputStream inputStream) {
    return new FudgeObjectReader(createMessageReader(inputStream));
  }

  /**
   * Creates a new writer for serialising Java objects to a Fudge stream.
   * 
   * @param dataOutput the target to write to
   * @return the {@link FudgeObjectWriter}
   */
  public FudgeObjectWriter createObjectWriter(final DataOutput dataOutput) {
    return new FudgeObjectWriter(createMessageWriter(dataOutput));
  }

  /**
   * Creates a new writer for serialising Java objects to a Fudge stream.
   * 
   * @param outputStream the target to write to
   * @return the {@link FudgeObjectWriter}
   */
  public FudgeObjectWriter createObjectWriter(final OutputStream outputStream) {
    return new FudgeObjectWriter(createMessageWriter(outputStream));
  }

  //-------------------------------------------------------------------------
  /**
   * Writes a Java object to an {@link OutputStream} using the Fudge serialization framework.
   * The current {@link FudgeObjectDictionary} will be used to identify any custom message builders
   * or apply default serialization behavior. Either a new serialization context will be used or
   * an existing one reset for this operation.
   * 
   * @param object  the {@link Object} to write, null returns null
   * @param outputStream  the stream to write the Fudge encoded form of the object to, not null
   */
  public void writeObject(Object object, OutputStream outputStream) {
    if (object == null) {
      return;
    }
    FudgeObjectWriter osw = createObjectWriter(outputStream);
    osw.write(object);
  }

  /**
   * Reads a Java object from an {@link InputStream} using the Fudge serialization framework.
   * The current {@link FudgeObjectDictionary} will be used to identify any custom object builders
   * or apply default deserialization behavior. Always reads the next available Fudge message from the
   * stream even if the message cannot be converted to the requested Object. Either a new deserialization
   * context will be used or an existing one reset for this operation.
   * 
   * @param <T> the target type to decode the message to
   * @param objectClass  the target {@code Class} to decode a message of. If an object of this or a sub-class is not available, an exception will be thrown.
   * @param inputStream  the stream to read the next Fudge message from, not null
   * @return the object read
   */
  public <T> T readObject(Class<T> objectClass, InputStream inputStream) {
    FudgeObjectReader osr = createObjectReader(inputStream);
    T result = osr.read(objectClass);
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a Java object to a {@link FudgeMsgEnvelope} using the Fudge serialization framework.
   * <p>
   * This is a shortcut method for ease-of-use. The recommended approach is to create
   * a {@link FudgeSerializer} instance and use that.
   * 
   * @param obj  object to serialize, not null
   * @return the serialized message
   */
  public FudgeMsgEnvelope toFudgeMsg(Object obj) {
    final FudgeSerializer serializer = new FudgeSerializer(this);
    final MutableFudgeMsg message = serializer.objectToFudgeMsg(obj);
    final Class<?> clazz = obj.getClass();
    if (!getObjectDictionary().isDefaultObject(clazz)) {
      FudgeSerializer.addClassHeader(message, clazz);
    }
    return new FudgeMsgEnvelope(message);
  }

  /**
   * Deserializes a {@link FudgeMsg} message to a Java object, determining the type of the object automatically.
   * <p>
   * This is a shortcut method for ease-of-use.
   * The recommended approach is to create and use a {@link FudgeDeserializer}.
   * 
   * @param message  the message to process, not null
   * @return the deserialized object
   */
  public Object fromFudgeMsg(FudgeMsg message) {
    final FudgeDeserializer deserializer = new FudgeDeserializer(this);
    return deserializer.fudgeMsgToObject(message);
  }

  /**
   * Deserializes a {@link FudgeMsg} message to a Java object of the specified type.
   * <p>
   * This is a shortcut method for ease-of-use.
   * The recommended approach is to create and use a {@link FudgeDeserializer}.
   * 
   * @param <T> Java type
   * @param clazz  the target type to deserialize, not null
   * @param message  the message to process, not null
   * @return the deserialized object
   */
  public <T> T fromFudgeMsg(Class<T> clazz, FudgeMsg message) {
    final FudgeDeserializer deserializer = new FudgeDeserializer(this);
    return deserializer.fudgeMsgToObject(clazz, message);
  }

  //-------------------------------------------------------------------------
  /**
   * Type conversion for secondary types using information registered in the current type dictionary.
   * <p>
   * This delegates to {@link FudgeTypeDictionary#getFieldValue}.
   * 
   * @param <T> type to convert to
   * @param clazz  target class for the converted value
   * @param field  field containing the value to convert
   * @return the converted value
   */
  public <T> T getFieldValue(final Class<T> clazz, final FudgeField field) {
    return getTypeDictionary().getFieldValue(clazz, field);
  }

}

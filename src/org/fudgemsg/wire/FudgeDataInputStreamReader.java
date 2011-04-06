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
package org.fudgemsg.wire;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.taxonomy.FudgeTaxonomy;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * A Fudge reader that uses a {@code DataInput} stream.
 * <p>
 * This is the standard implementation of {@code FudgeStreamReader}.
 */
public class FudgeDataInputStreamReader implements FudgeStreamReader {

  // Injected Inputs:
  private final DataInput _dataInput;
  private final FudgeContext _fudgeContext;

  // Runtime State:
  private final Stack<MessageProcessingState> _processingStack = new Stack<MessageProcessingState>();
  private FudgeStreamElement _currentElement;
  private FudgeTaxonomy _taxonomy;

  // Set for the envelope
  private int _processingDirectives;
  private int _schemaVersion;
  private short _taxonomyId;
  private int _envelopeSize;

  // Set for each non-sub-msg field
  private FudgeFieldType _fieldType;
  private Integer _fieldOrdinal;
  private String _fieldName;
  private Object _fieldValue;

  /**
   * Creates a new reader wrapping an input stream.
   * <p>
   * The Fudge context supplies all the necessary configuration.
   * 
   * @param fudgeContext  the Fudge context to use, not null
   * @param dataInput  the data input stream to read from, not null
   */
  public FudgeDataInputStreamReader(final FudgeContext fudgeContext, final DataInput dataInput) {
    if (fudgeContext == null) {
      throw new NullPointerException("Must provide a FudgeContext");
    }
    if (dataInput == null) {
      throw new NullPointerException("Must provide a DataInput");
    }
    _fudgeContext = fudgeContext;
    _dataInput = dataInput;
  }

  /**
   * Creates a new reader wrapping an input stream.
   * <p>
   * The Fudge context supplies all the necessary configuration.
   * 
   * @param fudgeContext  the Fudge context to use, not null
   * @param inputStream  the input stream to read from, not null
   */
  public FudgeDataInputStreamReader(final FudgeContext fudgeContext, final InputStream inputStream) {
    this(fudgeContext, convertInputStream(inputStream));
  }

  /**
   * Efficiently converts an input stream to a data input stream.
   * 
   * @param inputStream  the input stream, not nll
   * @return the data input stream, not null
   */
  private static DataInput convertInputStream(final InputStream inputStream) {
    if (inputStream == null) {
      throw new NullPointerException("InputStream must not be null");
    }
    if (inputStream instanceof DataInput) {
      return (DataInput) inputStream;
    }
    return new DataInputStream(inputStream);
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public int getProcessingDirectives() {
    return _processingDirectives;
  }

  @Override
  public int getSchemaVersion() {
    return _schemaVersion;
  }

  @Override
  public short getTaxonomyId() {
    return _taxonomyId;
  }

  @Override
  public FudgeTaxonomy getTaxonomy() {
    return _taxonomy;
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeStreamElement getCurrentElement() {
    return _currentElement;
  }

  @Override
  public String getFieldName() {
    return _fieldName;
  }

  @Override
  public Integer getFieldOrdinal() {
    return _fieldOrdinal;
  }

  @Override
  public FudgeFieldType getFieldType() {
    return _fieldType;
  }

  @Override
  public Object getFieldValue() {
    return _fieldValue;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean hasNext() {
    if (_processingStack.size() > 1) {
      // always have at least one more.
      return true;
    } else if (_processingStack.size() == 1) {
      MessageProcessingState messageProcessingState = _processingStack.peek();
      if (messageProcessingState.consumed < messageProcessingState.messageSize) {
        // more to read
        return true;
      } else {
        // end of the outermost envelope, so clear the stack and return a temporary false
        _processingStack.pop();
        return false;
      }
    } else {
      // might have another envelope to read
      return true;
    }
  }

  @Override
  public FudgeStreamElement next() {
    try {
      if (_processingStack.isEmpty()) {
        // must be an envelope (or an EOF)
        if (!consumeMessageEnvelope()) {
          return null;
        }
      } else if (isEndOfSubMessage()) {
        _currentElement = FudgeStreamElement.SUBMESSAGE_FIELD_END;
        _fieldName = null;
        _fieldOrdinal = null;
        _fieldType = null;
      } else {
        consumeFieldData();
      }
      assert _currentElement != null;
      return _currentElement;
    } catch (IOException e) {
      throw new FudgeRuntimeIOException(e);
    }
  }

  /**
   * Detects the end of a sub-message field.
   * This the last field within the sub-message has been fully consumed.
   * After the end has been reached, further calls to {@link #next()} will
   * resume consuming fields from the containing message again.
   * 
   * @return true if the end of the sub-message has been reached
   */
  protected boolean isEndOfSubMessage() {
    if (_processingStack.size() == 1) {
      return false;
    }
    MessageProcessingState processingState = _processingStack.peek();
    if (processingState.consumed >= processingState.messageSize) {
      _processingStack.pop();
      _processingStack.peek().consumed += processingState.consumed;
      return true;
    }
    return false;
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeStreamReader skipMessageField() {
    assert _currentElement == FudgeStreamElement.SUBMESSAGE_FIELD_START;
    assert _processingStack.size() > 1;
    final MessageProcessingState processingState = _processingStack.pop();
    _processingStack.peek().consumed += processingState.messageSize;
    final byte[] buffer = new byte[processingState.messageSize];
    try {
      getDataInput().readFully(buffer);
    } catch (IOException e) {
      throw new FudgeRuntimeIOException(e);
    }
    return new EncodedFudgeMsg.Reader(buffer, getFudgeContext());
  }

  /**
   * Returns the underlying {@link DataInput}.
   * 
   * @return the {@code DataInput}
   */
  protected DataInput getDataInput() {
    return _dataInput;
  }

  /**
   * Reads the next field (prefix and value) from the input stream, setting internal
   * state to be returned by getFieldName, getFieldOrdinal, getFieldType, getCurrentElement
   * and getFieldValue. The input stream is left positioned at the start of the next field.
   * 
   * @throws IOException if the underlying stream raises one
   */
  protected void consumeFieldData() throws IOException {
    byte fieldPrefix = getDataInput().readByte();
    int typeId = getDataInput().readUnsignedByte();
    int nRead = 2;
    boolean fixedWidth = FudgeFieldPrefixCodec.isFixedWidth(fieldPrefix);
    boolean hasOrdinal = FudgeFieldPrefixCodec.hasOrdinal(fieldPrefix);
    boolean hasName = FudgeFieldPrefixCodec.hasName(fieldPrefix);
    
    Integer ordinal = null;
    if (hasOrdinal) {
      ordinal = new Integer(getDataInput().readShort());
      nRead += 2;
    }
    
    String name = null;
    if (hasName) {
      int nameSize = getDataInput().readUnsignedByte();
      nRead++;
      name = UTF8.readString(getDataInput(), nameSize);
      nRead += nameSize;
    } else if (ordinal != null) {
      if (getTaxonomy() != null) {
        name = getTaxonomy().getFieldName(ordinal.shortValue());
      }
    }
    
    FudgeWireType type = getFudgeContext().getTypeDictionary().getByTypeId(typeId);
    if (type.isTypeUnknown() && fixedWidth) {
      throw new IOException("Unknown fixed width type " + typeId + " for field " + ordinal + ":" + name + " cannot be handled.");
    }
    
    int varSize = 0;
    if (!fixedWidth) {
      int varSizeBytes = FudgeFieldPrefixCodec.getFieldWidthByteCount(fieldPrefix);
      switch (varSizeBytes) {
        case 0:
          varSize = 0;
          break;
        case 1:
          varSize = getDataInput().readUnsignedByte();
          nRead += 1;
          break;
        case 2:
          varSize = getDataInput().readShort();
          nRead += 2;
          break;
        case 4:
          varSize = getDataInput().readInt();
          nRead += 4;
          break;
        default:
          throw new IOException("Illegal number of bytes indicated for variable width encoding: " + varSizeBytes);
      }
    }
    
    _fieldName = name;
    _fieldOrdinal = ordinal;
    _fieldType = type;
    MessageProcessingState currMsgProcessingState = _processingStack.peek();
    currMsgProcessingState.consumed += nRead;
    if (typeId == FudgeWireType.SUB_MESSAGE_TYPE_ID) {
      _currentElement = FudgeStreamElement.SUBMESSAGE_FIELD_START;
      _fieldValue = null;
      pushProcessingState(0, varSize);
    } else {
      _currentElement = FudgeStreamElement.SIMPLE_FIELD;
      _fieldValue = readFieldValue(getDataInput(), _fieldType, varSize);
      if (fixedWidth) {
        currMsgProcessingState.consumed += type.getFixedSize();
      } else {
        currMsgProcessingState.consumed += varSize;
      }
    }
  }

  /**
   * Reads a Fudge encoded field value from an input stream.
   * 
   * @param is  the {@link DataInput} wrapped input stream
   * @param type  the {@link FudgeFieldType} of the data to read
   * @param varSize  number of bytes in a variable width field payload
   * @return the field value
   */
  public static Object readFieldValue(DataInput is, FudgeFieldType type, int varSize) {
    assert type != null;
    assert is != null;
    try {
      // Special fast-pass for known field types
      switch (type.getTypeId()) {
        case FudgeWireType.BOOLEAN_TYPE_ID:
          return is.readBoolean();
        case FudgeWireType.BYTE_TYPE_ID:
          return is.readByte();
        case FudgeWireType.SHORT_TYPE_ID:
          return is.readShort();
        case FudgeWireType.INT_TYPE_ID:
          return is.readInt();
        case FudgeWireType.LONG_TYPE_ID:
          return is.readLong();
        case FudgeWireType.FLOAT_TYPE_ID:
          return is.readFloat();
        case FudgeWireType.DOUBLE_TYPE_ID:
          return is.readDouble();
      }
      return type.readValue(is, varSize);
    } catch (IOException ex) {
      throw new FudgeRuntimeIOException(ex);
    }
  }

  /**
   * Reads the next message envelope from the input stream, setting internal state
   * to be returned by getCurrentElement, getProcessingDirectives, getSchemaVersion,
   * getTaxonomyId and getEnvelopeSize.
   * 
   * @throws IOException if the underlying data source raises an {@link IOException}
   *   other than an {@link EOFException} on the first byte of the envelope
   * @return {@code true} if there was an envelope to consume, {@code false} if an EOF was found on reading the first byte
   */
  protected boolean consumeMessageEnvelope() throws IOException {
    try {
      _processingDirectives = getDataInput().readUnsignedByte();
    } catch (EOFException ex) {
      _currentElement = null;
      return false;
    }
    _currentElement = FudgeStreamElement.MESSAGE_ENVELOPE;
    _schemaVersion = getDataInput().readUnsignedByte();
    _taxonomyId = getDataInput().readShort();
    _envelopeSize = getDataInput().readInt();
    _taxonomy = getFudgeContext().getTaxonomyResolver().resolveTaxonomy(_taxonomyId);
    pushProcessingState(8, _envelopeSize);
    return true;
  }

  /**
   * Pushes the current state onto the stack.
   * 
   * @param consumedBytes  the number of consumed bytes
   * @param messageSize  the message size
   */
  protected void pushProcessingState(final int consumedBytes, final int messageSize) {
    MessageProcessingState processingState = new MessageProcessingState();
    processingState.consumed = consumedBytes;
    processingState.messageSize = messageSize;
    _processingStack.add(processingState);
  }

  //-------------------------------------------------------------------------
  /**
   * Closes the underlying {@code DataInput} if it implements {@code Closeable}.
   */
  @Override
  public void close() {
    if (_dataInput == null) {
      return;
    }
    if (_dataInput instanceof Closeable) {
      try {
        ((Closeable) _dataInput).close();
      } catch (IOException ioe) {
        // ignore
      }
    }
    _currentElement = null;
    _processingStack.clear();
    
    _processingDirectives = 0;
    _schemaVersion = 0;
    _taxonomyId = 0;
    _envelopeSize = 0;
    
    _fieldType = null;
    _fieldOrdinal = null;
    _fieldName = null;
    _fieldValue = null;
  }

  //-------------------------------------------------------------------------
  /**
   * Internal holder for managing state.
   */
  private static class MessageProcessingState {
    public int messageSize;
    public int consumed;
  }

}

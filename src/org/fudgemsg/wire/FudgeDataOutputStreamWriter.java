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
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeTypeDictionary;

/**
 * A Fudge writer that uses a {@code DataOutput} stream.
 * <p>
 * This is the standard implementation of {@code FudgeStreamWriter}.
 */
public class FudgeDataOutputStreamWriter extends AbstractFudgeStreamWriter {

  /**
   * The underlying stream.
   */
  private final DataOutput _dataOutput;
  /**
   * Whether to automatically flush.
   */
  private boolean _automaticFlush = true;

  /**
   * Creates a new writer wrapping an output stream.
   * <p>
   * The Fudge context supplies all the necessary configuration.
   * 
   * @param fudgeContext  the Fudge context to use, not null
   * @param outputStream  the target stream to write to, not null
   */
  public FudgeDataOutputStreamWriter(FudgeContext fudgeContext, final OutputStream outputStream) {
    this(fudgeContext, convertOutputStream(outputStream));
  }

  /**
   * Creates a new writer wrapping a data output stream.
   * <p>
   * The Fudge context supplies all the necessary configuration.
   * 
   * @param fudgeContext  the Fudge context to use, not null
   * @param dataOutput  the target stream to write to, not null
   */
  public FudgeDataOutputStreamWriter(FudgeContext fudgeContext, final DataOutput dataOutput) {
    super(fudgeContext);
    if (dataOutput == null) {
      throw new NullPointerException("DataOutput must not be null");
    }
    _dataOutput = dataOutput;
  }

  /**
   * Efficiently convert a stream to a {@code DataOutput}.
   * 
   * @param outputStream  the stream, not null
   * @return the data stream, not null
   */
  private static DataOutput convertOutputStream(final OutputStream outputStream) {
    if (outputStream instanceof DataOutput) {
      return (DataOutput) outputStream;
    }
    return new DataOutputStream(outputStream);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks whether this writer will flush once the envelope is complete.
   * 
   * @return true if {@code flush} is to be called
   */
  public boolean isFlushOnEnvelopeComplete() {
    return _automaticFlush;
  }

  /**
   * Sets whether this writer will flush once the envelope is complete.
   * The default value is true, causing the writer to flush.
   * 
   * @param automaticFlush  true to call {@code flush} on envelope completion
   */
  public void setFlushOnEnvelopeComplete(final boolean automaticFlush) {
    _automaticFlush = automaticFlush;
  }

  /**
   * Gets the underlying data output stream.
   * 
   * @return the data output stream, not null
   */
  protected DataOutput getDataOutput() {
    return _dataOutput;
  }

  //-------------------------------------------------------------------------
  @Override
  public void writeEnvelopeHeader(int processingDirectives, int schemaVersion, int messageSize) {
    try {
      getDataOutput().writeByte(processingDirectives);
      getDataOutput().writeByte(schemaVersion);
      getDataOutput().writeShort(getCurrentTaxonomyId());
      getDataOutput().writeInt(messageSize);
    } catch (IOException ex) {
      throw new FudgeRuntimeIOException(ex);
    }
  }

  /**
   * Handles the envelope complete event.
   * <p>
   * This does not send any data as the end of the envelope is implied by the size from the header.
   * If the writer is set to automatically flush on message completion (the default) then
   * this method calls {@link #flush()}.
   */
  @Override
  public void envelopeComplete() {
    if (isFlushOnEnvelopeComplete()) {
      flush();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void writeFields(Iterable<FudgeField> fields) {
    if (fields instanceof FudgeEncoded) {
      // optimize for the case where the data is already encoded
      try {
        getDataOutput().write(((FudgeEncoded) fields).getFudgeEncoded());
      } catch (IOException ex) {
        throw new FudgeRuntimeIOException(ex);
      }
    } else {
      // encode and write the data
      writeAllFields(fields);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void writeField(String name, Integer ordinal, FudgeFieldType type, Object fieldValue) {
    if (fieldValue == null) {
      throw new NullPointerException("Value must not be null");
    }
    
    // 11/12/09 Andrew: If a taxonomy is being used, should we attempt to validate against it (i.e. refuse a mismatching fieldname/ordinal)
    // 11/12/09 Andrew: If name, ordinal and taxonomy are supplied, should we not write out the name (this would happen if no ordinal was supplied) 
    
    // convert name to ordinal
    if (name != null && ordinal == null && getCurrentTaxonomy() != null) {
      ordinal = getCurrentTaxonomy().getFieldOrdinal(name);
      if (ordinal != null) {
        name = null;
      }
    }
    
    // calculate field size
    int valueSize = 0;
    int varDataSize = 0;
    if (type.isVariableSize()) {
      valueSize = type.getSize(fieldValue, getCurrentTaxonomy());
      varDataSize = valueSize;
    } else {
      valueSize = type.getFixedSize();
      varDataSize = 0;
    }
    int fieldPrefix = FudgeFieldPrefixCodec.composeFieldPrefix(type.isFixedSize(), varDataSize, (ordinal != null), (name != null));
    
    // write the data
    writeHeader(name, ordinal, type, fieldPrefix);
    writeFieldValue(type, fieldValue, valueSize);
  }

  /**
   * Writes a field header.
   * 
   * @param name  the name of the field, null if no name
   * @param ordinal  the ordinal index of the field, null if no ordinal
   * @param type  the type of the underlying data, not null
   * @param fieldPrefix  the calculated field prefix, not null
   */
  protected void writeHeader(String name, Integer ordinal, FudgeFieldType type, int fieldPrefix) {
    try {
      getDataOutput().writeByte(fieldPrefix);
      getDataOutput().writeByte(type.getTypeId());
      if (ordinal != null) {
        getDataOutput().writeShort(ordinal.intValue());
      }
      if (name != null) {
        int utf8size = UTF8.getLengthBytes(name);
        if (utf8size > 0xFF) {
          throw new IllegalArgumentException("UTF-8 encoded field name cannot exceed 255 characters. Name \"" + name
              + "\" is " + utf8size + " bytes encoded.");
        }
        getDataOutput().writeByte(utf8size);
        UTF8.writeString(getDataOutput(), name);
      }
    } catch (IOException ex) {
      throw new FudgeRuntimeIOException(ex);
    }
  }

  /**
   * Writes the field value including the variable size data.
   * 
   * @param type  the type, not null
   * @param value  the value to write, not null
   * @param valueSize  the size of the value
   * @returns number of bytes written
   */
  protected void writeFieldValue(FudgeFieldType type, Object value, int valueSize) {
    // Note that we fast-path types for which at compile time we know how to handle
    // in an optimized way. This is because this particular method is known to
    // be a massive hot-spot for performance.
    try {
      switch (type.getTypeId()) {
        case FudgeTypeDictionary.BOOLEAN_TYPE_ID:
          getDataOutput().writeBoolean((Boolean) value);
          break;
        case FudgeTypeDictionary.BYTE_TYPE_ID:
          getDataOutput().writeByte((Byte) value);
          break;
        case FudgeTypeDictionary.SHORT_TYPE_ID:
          getDataOutput().writeShort((Short) value);
          break;
        case FudgeTypeDictionary.INT_TYPE_ID:
          getDataOutput().writeInt((Integer) value);
          break;
        case FudgeTypeDictionary.LONG_TYPE_ID:
          getDataOutput().writeLong((Long) value);
          break;
        case FudgeTypeDictionary.FLOAT_TYPE_ID:
          getDataOutput().writeFloat((Float) value);
          break;
        case FudgeTypeDictionary.DOUBLE_TYPE_ID:
          getDataOutput().writeDouble((Double) value);
          break;
        case FudgeTypeDictionary.INDICATOR_TYPE_ID:
          break;
        default:
          if (type.isVariableSize()) {
            // This is correct. We read this using a .readUnsignedByte(), so we can go to
            // 255 here.
            if (valueSize <= 255) {
              getDataOutput().writeByte(valueSize);
            } else if (valueSize <= Short.MAX_VALUE) {
              getDataOutput().writeShort(valueSize);
            } else {
              getDataOutput().writeInt(valueSize);
            }
          }
          if (value instanceof FudgeEncoded) {
            getDataOutput().write(((FudgeEncoded) value).getFudgeEncoded());
          } else if (value instanceof FudgeMsg) {
            writeAllFields((FudgeMsg) value);
          } else {
            type.writeValue(getDataOutput(), value);
          }
      }
    } catch (IOException ex) {
      throw new FudgeRuntimeIOException(ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Flushes the underlying {@code DataOutput} if it implements {@code Flushable}.
   */
  @Override
  public void flush() {
    final DataOutput out = getDataOutput();
    doFlush(out);
  }

  /**
   * Closes the underlying {@code DataOutput} if it implements {@code Closeable}.
   * This calls {@link #flush()} before attempting to close the stream.
   */
  @Override
  public void close() {
    final DataOutput out = getDataOutput();
    doFlush(out);
    doClose(out);
    clearTaxonomy();
  }

  /**
   * Flushes the data stream.
   * 
   * @param out  the data stream, may be null
   */
  private void doFlush(final DataOutput out) {
    if (out instanceof Flushable) {
      try {
        ((Flushable) out).flush();
      } catch (IOException ex) {
        throw new FudgeRuntimeIOException(ex);
      }
    }
  }

  /**
   * Closes the data stream.
   * 
   * @param out  the data stream, may be null
   */
  private void doClose(final DataOutput out) {
    if (out instanceof Closeable) {
      try {
        ((Closeable) out).close();
      } catch (IOException ex) {
        throw new FudgeRuntimeIOException(ex);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string suitable for debugging.
   * 
   * @return the string, not null
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FudgeDataOutputStreamWriter{");
    if (getDataOutput() != null) {
      sb.append(getDataOutput());
    }
    return sb.append('}').toString();
  }

}

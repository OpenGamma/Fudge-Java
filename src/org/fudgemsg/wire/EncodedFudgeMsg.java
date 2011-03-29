/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and other contributors.
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

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.util.Iterator;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsgBase;
import org.fudgemsg.FudgeMsgField;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.ImmutableFudgeFieldContainer;
import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.types.FudgeMsgFieldType;
import org.fudgemsg.util.ArgumentChecker;
import org.fudgemsg.wire.FudgeStreamReader.FudgeStreamElement;

/**
 * An immutable message backed directly by its encoded form. The fields within the
 * messages are decoded when they are required.
 * <p>
 * This is intended for use to represent sub-messages which are seldom (if ever)
 * inspected before the outer message is routed.
 * <p>
 * This is intended to be immutable, but not thread-safe.
 */
public class EncodedFudgeMsg extends FudgeMsgBase implements ImmutableFudgeFieldContainer, FudgeEncoded {

  /* package */static class Reader implements FudgeStreamReader, FudgeEncoded {
    
    private final FudgeContext _fudgeContext;
    private final byte[] _data;
    private FudgeStreamReader _streamReader;

    public Reader(final byte[] data, final FudgeContext fudgeContext) {
      _data = data;
      _fudgeContext = fudgeContext;
    }

    private FudgeStreamReader getStreamReader() {
      if (_streamReader == null) {
        _streamReader = createStreamReader(_data, 0, _data.length, getFudgeContext());
      }
      return _streamReader;
    }

    private static FudgeStreamReader createStreamReader(final byte[] data, final int offset, final int length,
        final FudgeContext fudgeContext) {
      final FudgeDataInputStreamReader reader = new FudgeDataInputStreamReader(fudgeContext,
          (DataInput) new DataInputStream(new ByteArrayInputStream(data, offset, length)));
      reader.pushProcessingState(0, length);
      return reader;
    }

    @Override
    public void close() {
      getStreamReader().close();
    }

    @Override
    public FudgeStreamElement getCurrentElement() {
      return getStreamReader().getCurrentElement();
    }

    @Override
    public String getFieldName() {
      return getStreamReader().getFieldName();
    }

    @Override
    public Integer getFieldOrdinal() {
      return getStreamReader().getFieldOrdinal();
    }

    @Override
    public FudgeFieldType getFieldType() {
      return getStreamReader().getFieldType();
    }

    @Override
    public Object getFieldValue() {
      return getStreamReader().getFieldValue();
    }

    @Override
    public FudgeContext getFudgeContext() {
      return _fudgeContext;
    }

    @Override
    public int getProcessingDirectives() {
      return getStreamReader().getProcessingDirectives();
    }

    @Override
    public int getSchemaVersion() {
      return getStreamReader().getSchemaVersion();
    }

    @Override
    public FudgeTaxonomy getTaxonomy() {
      return getStreamReader().getTaxonomy();
    }

    @Override
    public short getTaxonomyId() {
      return getStreamReader().getTaxonomyId();
    }

    @Override
    public boolean hasNext() {
      return getStreamReader().hasNext();
    }

    @Override
    public FudgeStreamElement next() {
      return getStreamReader().next();
    }

    @Override
    public FudgeStreamReader skipMessageField() {
      return getStreamReader().skipMessageField();
    }

    @Override
    public byte[] getFudgeEncoded() {
      return _data;
    }
    
  }

  private byte[] _data;
  private int _dataOffset;
  private int _dataLength;
  private FudgeStreamReader _streamReader;
  private boolean _complete;

  /**
   * Create a message instance from the Fudge binary encoded form of the message.
   * 
   * @param encodedMessage the encoded message. This must not have an envelope header, field header, or length prefix. I.e. the first element will be the first field of the message.
   * @param context the Fudge context to use to decode the message
   */
  public EncodedFudgeMsg(final byte[] encodedMessage, final FudgeContext context) {
    this(encodedMessage, 0, encodedMessage.length, context);
  }

  /**
   * Create a message instance from the Fudge binary encoded form of the message.
   * 
   * @param encodedMessage the encoded message. This must not have an envelope header, field header, or length prefix. I.e. the first element will be the first field of the message.
   * @param messageOffset the index of the first message byte
   * @param messageLength the length of the message, in bytes
   * @param fudgeContext the Fudge context to use to decode the message
   */
  public EncodedFudgeMsg(final byte[] encodedMessage, final int messageOffset, final int messageLength,
      final FudgeContext fudgeContext) {
    super(fudgeContext);
    ArgumentChecker.notNull(encodedMessage, "encodedMessage");
    ArgumentChecker.isTrue((messageOffset >= 0) && (messageOffset <= encodedMessage.length), "messageOffset");
    ArgumentChecker.isTrue((messageLength >= 0) && (messageOffset + messageLength <= encodedMessage.length),
        "messageLength");
    _data = encodedMessage;
    _dataOffset = messageOffset;
    _dataLength = messageLength;
    _complete = messageLength == 0;
  }

  /**
   * Create a message instance from a stream that will provide field elements when required.
   * 
   * @param streamReader the stream to read elements from
   */
  public EncodedFudgeMsg(final FudgeStreamReader streamReader) {
    super(streamReader.getFudgeContext());
    if (streamReader instanceof Reader) {
      final Reader reader = (Reader) streamReader;
      _streamReader = reader.getStreamReader();
      _data = reader.getFudgeEncoded();
      _dataOffset = 0;
      _dataLength = _data.length;
      _complete = _dataLength == 0;
    } else {
      _streamReader = streamReader;
    }
  }

  private FudgeStreamReader getStreamReader() {
    if (_streamReader == null) {
      _streamReader = Reader.createStreamReader(_data, _dataOffset, _dataLength, getFudgeContext());
    }
    return _streamReader;
  }

  private FudgeField nextField(final List<FudgeField> fields, final FudgeStreamReader reader) {
    final FudgeStreamElement element = reader.next();
    FudgeField field = null;
    switch (element) {
      case SIMPLE_FIELD: {
        final Integer ordinalInt = reader.getFieldOrdinal();
        field = FudgeMsgField.of(reader.getFieldType(), reader.getFieldValue(), reader.getFieldName(),
            (ordinalInt != null) ? ordinalInt.shortValue() : null);
        break;
      }
      case SUBMESSAGE_FIELD_START: {
        final Integer ordinalInt = reader.getFieldOrdinal();
        field = FudgeMsgField.of(FudgeMsgFieldType.INSTANCE, new EncodedFudgeMsg(reader.skipMessageField()), reader
            .getFieldName(), (ordinalInt != null) ? ordinalInt.shortValue() : null);
        break;
      }
      case SUBMESSAGE_FIELD_END:
        _complete = true;
        break;
    }
    if (field != null) {
      fields.add(field);
      if (!reader.hasNext()) {
        _complete = true;
      }
    }
    return field;
  }

  private Iterator<FudgeField> getFieldIterator() {
    final List<FudgeField> fields = super.getFields();
    final Iterator<FudgeField> itr = fields.iterator();
    if (_complete) {
      return itr;
    }
    return new Iterator<FudgeField>() {

      private boolean _useItr = true;

      @Override
      public boolean hasNext() {
        if (_useItr) {
          if (itr.hasNext()) {
            return true;
          }
          _useItr = false;
        }
        return !_complete;
      }

      @Override
      public FudgeField next() {
        if (_useItr) {
          if (itr.hasNext()) {
            return itr.next();
          }
          _useItr = false;
        }
        return _complete ? null : nextField(fields, getStreamReader());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  /**
   * By implementing the 'getFields' call that FudgeMsgBase methods all use, the first call will
   * result in a full unpack of the message. Specific cases can be overridden for additional
   * laziness.
   */
  @Override
  protected List<FudgeField> getFields() {
    final List<FudgeField> fields = super.getFields();
    if (!_complete) {
      final FudgeStreamReader streamReader = getStreamReader();
      while (!_complete) {
        nextField(fields, streamReader);
      }
    }
    return fields;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <T> T getFirstTypedValue(Class<T> clazz, String name, int typeId) {
    if (!_complete) {
      final Iterator<FudgeField> itr = getFieldIterator();
      while (itr.hasNext()) {
        final FudgeField field = itr.next();
        if (fieldNameEquals(name, field)) {
          if (field.getType().getTypeId() == typeId) {
            return (T) field.getValue();
          }
        }
      }
    }
    return super.getFirstTypedValue(clazz, name, typeId);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <T> T getFirstTypedValue(Class<T> clazz, int ordinal, int typeId) {
    if (!_complete) {
      final Iterator<FudgeField> itr = getFieldIterator();
      final Short ordinalAsShort = (short) ordinal;
      while (itr.hasNext()) {
        final FudgeField field = itr.next();
        if (fieldOrdinalEquals(ordinalAsShort, field)) {
          if (field.getType().getTypeId() == typeId) {
            return (T) field.getValue();
          }
        }
      }
    }
    return super.getFirstTypedValue(clazz, ordinal, typeId);
  }

  @Override
  public FudgeField getByIndex(int index) {
    final List<FudgeField> fields = super.getFields();
    if (!_complete) {
      // Have we already encountered this index
      if (index < fields.size()) {
        return fields.get(index);
      }
      // Read fields
      final FudgeStreamReader streamReader = getStreamReader();
      for (int i = fields.size(); (i < index) && !_complete; i++) {
        nextField(fields, streamReader);
      }
      if (!_complete && (fields.size() == index)) {
        return nextField(fields, streamReader);
      }
    }
    return fields.get(index);
  }

  @Override
  public FudgeField getByName(String name) {
    final Iterator<FudgeField> itr = getFieldIterator();
    while (itr.hasNext()) {
      final FudgeField field = itr.next();
      if (fieldNameEquals(name, field)) {
        return field;
      }
    }
    return null;
  }

  @Override
  public FudgeField getByOrdinal(int ordinal) {
    final Short ordinalAsShort = (short) ordinal;
    final Iterator<FudgeField> itr = getFieldIterator();
    while (itr.hasNext()) {
      final FudgeField field = itr.next();
      if (fieldOrdinalEquals(ordinalAsShort, field)) {
        return field;
      }
    }
    return null;
  }

  @Override
  public <T> T getValue(Class<T> clazz, String name) {
    final FudgeTypeDictionary dictionary = getFudgeContext().getTypeDictionary();
    final Iterator<FudgeField> itr = getFieldIterator();
    while (itr.hasNext()) {
      final FudgeField field = itr.next();
      if (fieldNameEquals(name, field) && dictionary.canConvertField(clazz, field)) {
        return dictionary.getFieldValue(clazz, field);
      }
    }
    return null;
  }

  @Override
  public <T> T getValue(Class<T> clazz, int ordinal) {
    final FudgeTypeDictionary dictionary = getFudgeContext().getTypeDictionary();
    final Short ordinalAsShort = (short) ordinal;
    final Iterator<FudgeField> itr = getFieldIterator();
    while (itr.hasNext()) {
      final FudgeField field = itr.next();
      if (fieldOrdinalEquals(ordinalAsShort, field) && dictionary.canConvertField(clazz, field)) {
        return dictionary.getFieldValue(clazz, field);
      }
    }
    return null;
  }

  @Override
  public boolean isEmpty() {
    if (!_complete) {
      final List<FudgeField> fields = super.getFields();
      if (fields.isEmpty()) {
        nextField(fields, getStreamReader());
        return fields.isEmpty();
      } else {
        return false;
      }
    }
    return super.isEmpty();
  }

  @Override
  public Iterator<FudgeField> iterator() {
    return getFieldIterator();
  }

  @Override
  public byte[] getFudgeEncoded() {
    if (_data == null) {
      if (_streamReader instanceof FudgeEncoded) {
        _data = ((FudgeEncoded) _streamReader).getFudgeEncoded();
        if (_data != null) {
          _dataOffset = 0;
          _dataLength = _data.length;
        }
      }
    }
    if (_dataOffset != 0) {
      final byte[] buffer = new byte[_dataLength];
      System.arraycopy(_data, _dataOffset, buffer, 0, _dataLength);
      return buffer;
    } else {
      return _data;
    }
  }

}

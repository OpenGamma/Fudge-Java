/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc. and other contributors.
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

package org.fudgemsg.wire.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.taxonomy.FudgeTaxonomy;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.FudgeRuntimeIOException;
import org.fudgemsg.wire.FudgeStreamReader;

/**
 * Reader that decodes XML into Fudge messages.
 */
public class FudgeXMLStreamReader implements FudgeStreamReader {
  
  private final Stack<String> _messageStack = new Stack<String>();  
  private final FudgeXMLSettings _settings;
  private final FudgeContext _fudgeContext;
  private final Reader _underlying;
  private final XMLStreamReader _xmlStreamReader;
  
  private int _taxonomyId = 0;
  private FudgeTaxonomy _taxonomy = null;
  private int _processingDirectives = 0;
  private int _schemaVersion = 0;
  private FudgeStreamElement _currentElement = null;
  private String _fieldName = null;
  private Integer _fieldOrdinal = null;
  private Object _fieldValue = null;
  private FudgeFieldType _fieldType = null;
  private int _currentEvent = XMLStreamConstants.START_DOCUMENT;
  
  public FudgeXMLStreamReader(final FudgeContext fudgeContext, final Reader reader) {
    this(fudgeContext, reader, new FudgeXMLSettings());
  }
  
  public FudgeXMLStreamReader(final FudgeContext fudgeContext, final Reader underlying, final FudgeXMLSettings settings) {
    if (fudgeContext == null) {
      throw new NullPointerException("FudgeContext must not be null");
    }
    if (underlying == null) {
      throw new NullPointerException("Reader must not be null");
    }
    if (settings == null) {
      throw new NullPointerException("FudgeXMLSettings must not be null");
    }
    _fudgeContext = fudgeContext;
    _underlying = underlying;
    _settings = settings;
    _xmlStreamReader = createXMLStreamReader(underlying);
  }

  private XMLStreamReader createXMLStreamReader(Reader reader) {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    try {
      XMLStreamReader parser = factory.createXMLStreamReader(reader);
      return parser;
    } catch (XMLStreamException e) {
      throw wrapException("create", e);
    }
  }
  
  /**
   * @param currentElement the currentElement to set
   */
  public void setCurrentElement(FudgeStreamElement currentElement) {
    _currentElement = currentElement;
  }

  /**
   * @param fieldName the fieldName to set
   */
  public void setFieldName(String fieldName) {
    _fieldName = fieldName;
  }

  /**
   * @param fieldOrdinal the fieldOrdinal to set
   */
  public void setFieldOrdinal(Integer fieldOrdinal) {
    _fieldOrdinal = fieldOrdinal;
  }

  /**
   * @param fieldType the fieldType to set
   */
  public void setFieldType(FudgeFieldType fieldType) {
    _fieldType = fieldType;
  }

  public Reader getUnderlying() {
    return _underlying;
  }

  public XMLStreamReader getXMLStreamReader() {
    return _xmlStreamReader;
  }

  @Override
  public void close() {
    if (getUnderlying() != null) {
      try {
        getUnderlying().close();
      } catch (IOException e) {
        throw new FudgeRuntimeIOException(e);
      }
    }
  }

  private RuntimeException wrapException(String message, final XMLStreamException e) {
    message = "Error " + message + " from XML stream";
    if (e.getCause() instanceof IOException) {
      return new FudgeRuntimeIOException(message, (IOException) e.getCause());
    } else {
      return new FudgeRuntimeException(message, e);
    }
  }

  @Override
  public boolean hasNext() {
    return !(isEndOfDocument() || isEndOfEnvelopeElement());
  }

  private boolean isEndOfEnvelopeElement() {
    return _currentEvent == XMLStreamConstants.END_ELEMENT && _messageStack.isEmpty();
  }
  
  private boolean isEndOfDocument() {
    return _currentEvent == XMLStreamConstants.END_DOCUMENT;
  }

  @Override
  public FudgeStreamElement next() {
    try {
      for (int event = _xmlStreamReader.next(); event != XMLStreamConstants.END_DOCUMENT; event = _xmlStreamReader.next()) {
        _currentEvent = event;
        if (event == XMLStreamConstants.START_ELEMENT) {
          setCurrentElement(processStartElement());
          break;
        }
        if (event == XMLStreamConstants.END_ELEMENT) {
          String element = _xmlStreamReader.getLocalName();
          String currentMsgName = _messageStack.peek();
          if (element.equalsIgnoreCase(currentMsgName)) {
            _messageStack.pop();
            setCurrentElement(FudgeStreamElement.SUBMESSAGE_FIELD_END);
          }
          break;
        }
      }
    } catch (XMLStreamException e) {
      throw wrapException("reading next element", e);
    }
    return _currentElement;
  }
  
  private FudgeStreamElement processStartElement() throws XMLStreamException {
    String element = _xmlStreamReader.getLocalName();
    String fieldAttrName = _xmlStreamReader.getAttributeValue(null, _settings.getFieldAttributeName());
    if (isEnvelopeElement(element)) {
      return processEnvelopeElement(element);
    } 
    String type = _xmlStreamReader.getAttributeValue(null, _settings.getFieldAttributeType());
    int ordinalAttr = toInt(_xmlStreamReader.getAttributeValue(null, _settings.getFieldAttributeOrdinal()), Integer.MIN_VALUE);
    Integer ordinal = ordinalAttr != Integer.MIN_VALUE ? Integer.valueOf(ordinalAttr) : null;
    processFieldNameAndOrdinal(element, fieldAttrName, ordinal);
    if (isMessage(type)) {        
      _messageStack.push(element);
      return FudgeStreamElement.SUBMESSAGE_FIELD_START;
    } else {
      Integer fudgeTypeId = _settings.getIdentifiersToFudgeType().get(type);
      FudgeFieldType fudgeType = _fudgeContext.getTypeDictionary().getByTypeId(fudgeTypeId);
      _fieldType = fudgeType;
      _fieldValue = convertFieldValue(fudgeType, _xmlStreamReader.getElementText());
      return FudgeStreamElement.SIMPLE_FIELD;
    }
  }

  private boolean isEnvelopeElement(String element) {
    return element.equalsIgnoreCase(_settings.getEnvelopeElementName());
  }
  
  private Object convertFieldValue(FudgeFieldType fudgeType, String elementValue) {
    switch (fudgeType.getTypeId()) {
      case FudgeTypeDictionary.INDICATOR_TYPE_ID:
        return IndicatorType.INSTANCE;
      case FudgeTypeDictionary.BOOLEAN_TYPE_ID:
        return Boolean.valueOf(elementValue);
      case FudgeTypeDictionary.BYTE_TYPE_ID:
        return Byte.valueOf(elementValue);
      case FudgeTypeDictionary.SHORT_TYPE_ID:
        return Short.valueOf(elementValue);
      case FudgeTypeDictionary.INT_TYPE_ID:
        return Integer.valueOf(elementValue);
      case FudgeTypeDictionary.LONG_TYPE_ID:
        return Long.valueOf(elementValue);
      case FudgeTypeDictionary.BYTE_ARRAY_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_4_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_8_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_16_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_20_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_32_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_64_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_128_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_256_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_512_TYPE_ID:
        return toByteArray(elementValue);
      case FudgeTypeDictionary.SHORT_ARRAY_TYPE_ID:
        return toShortArray(elementValue);
      case FudgeTypeDictionary.INT_ARRAY_TYPE_ID:
        return toIntArray(elementValue);
      case FudgeTypeDictionary.LONG_ARRAY_TYPE_ID:
        return toLongArray(elementValue);
      case FudgeTypeDictionary.FLOAT_TYPE_ID:
        return Float.valueOf(elementValue);
      case FudgeTypeDictionary.DOUBLE_TYPE_ID:
        return Double.valueOf(elementValue);
      case FudgeTypeDictionary.FLOAT_ARRAY_TYPE_ID:
        return toFloatArray(elementValue);
      case FudgeTypeDictionary.DOUBLE_ARRAY_TYPE_ID:
        return toDoubleArray(elementValue);
      case FudgeTypeDictionary.DATE_TYPE_ID:
        return LocalDate.parse(elementValue);
      case FudgeTypeDictionary.TIME_TYPE_ID:
        return LocalTime.parse(elementValue);
      case FudgeTypeDictionary.DATETIME_TYPE_ID:
        return LocalDateTime.parse(elementValue);
      default:
        return elementValue;
    }
  }
  
  private Object toShortArray(final String fieldValue) {
    final String[] values = fieldValue.split(",");
    short[] result = new short[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = Short.valueOf(values[i]);
    }
    return result;
  }

  private Object toDoubleArray(final String fieldValue) {
    final String[] values = fieldValue.split(",");
    double[] result = new double[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = Double.valueOf(values[i]);
    }
    return result;
  }

  private Object toFloatArray(final String fieldValue) {
    final String[] values = fieldValue.split(",");
    float[] result = new float[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = Float.valueOf(values[i]);
    }
    return result;
  }

  private Object toLongArray(final String fieldValue) {
    final String[] values = fieldValue.split(",");
    long[] result = new long[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = Long.valueOf(values[i]);
    }
    return result;
  }

  private Object toIntArray(final String fieldValue) {
    final String[] values = fieldValue.split(",");
    int[] result = new int[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = Integer.valueOf(values[i]);
    }
    return result;
  }

  private Object toByteArray(final String fieldValue) {
    final String[] values = fieldValue.split(",");
    byte[] result = new byte[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = Byte.valueOf(values[i]);
    }
    return result;
  }

  private void processFieldNameAndOrdinal(String element, String fieldAttrName, Integer ordinal) {
    if (element.startsWith(_settings.getFieldElementName())) {
      if (ordinal != null) {
        _fieldOrdinal = ordinal;
        _fieldName = null;
      } else {
        _fieldOrdinal = null;
        _fieldName = null;
      }
    } else {
      if (_taxonomy != null && ordinal != null) {
        String outFieldName = _taxonomy.getFieldName((short)((int)ordinal));
        if (outFieldName.equalsIgnoreCase(element)) {
          _fieldOrdinal = ordinal;
          _fieldName = null;
        }
      } else {
        _fieldName = (fieldAttrName != null ? fieldAttrName : element);
        _fieldOrdinal = ordinal;
      }
    }
  }
  
  private boolean isMessage(String type) {
    Integer fudgeTypeId = _settings.getIdentifiersToFudgeType().get(type);
    return FudgeTypeDictionary.FUDGE_MSG_TYPE_ID == fudgeTypeId;
  }

  private FudgeStreamElement processEnvelopeElement(String element) {
    String schemaVersionAttr = _xmlStreamReader.getAttributeValue(null, _settings.getEnvelopeAttributeSchemaVersion());
    _schemaVersion = toInt(schemaVersionAttr, 0);
    String processingDirectivesAttr = _xmlStreamReader.getAttributeValue(null, _settings.getEnvelopeAttributeProcessingDirectives());
    _processingDirectives = toInt(processingDirectivesAttr, 0);
    String taxonomyIdAttr = _xmlStreamReader.getAttributeValue(null, _settings.getEnvelopeAttributeTaxonomy());
    _taxonomyId = toInt(taxonomyIdAttr, 0);
    _taxonomy = _fudgeContext.getTaxonomyResolver().resolveTaxonomy((short) _taxonomyId);
    _messageStack.push(element);
    return FudgeStreamElement.MESSAGE_ENVELOPE;
  }
  
  private static int toInt(String str, int defaultValue) {
    if (str == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException nfe) {
      return defaultValue;
    }
  }
  
  @Override
  public FudgeStreamElement getCurrentElement() {
    return _currentElement;
  }

  @Override
  public Object getFieldValue() {
    return _fieldValue;
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
    return (short) _taxonomyId;
  }

  @Override
  public FudgeFieldType getFieldType() {
    return _fieldType;
  }

  @Override
  public Integer getFieldOrdinal() {
    return _fieldOrdinal;
  }

  @Override
  public String getFieldName() {
    return _fieldName;
  }

  @Override
  public FudgeTaxonomy getTaxonomy() {
    return _taxonomy;
  }

  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public FudgeStreamReader skipMessageField() {
    throw new UnsupportedOperationException();
  }

}

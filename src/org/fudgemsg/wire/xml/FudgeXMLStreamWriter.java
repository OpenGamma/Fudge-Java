/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies and other contributors.
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.types.SecondaryFieldTypeBase;
import org.fudgemsg.wire.EventBasedFudgeStreamWriter;
import org.fudgemsg.wire.FudgeRuntimeIOException;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * A Fudge writer that produces XML.
 * <p>
 * This writer writes a Fudge stream as XML to a text stream.
 * This can be used for XML output, or can be used to assist in developing/debugging
 * a streaming serializer without having to inspect the binary output.
 * <p>
 * This code should adhere to the <a href="http://wiki.fudgemsg.org/display/FDG/XML+Fudge+Messages">XML Fudge Message specification</a>.
 * <p>
 * Note that no pretty printing options are available here.
 * This implementation uses the systems default {@link XMLOutputFactory} if only passed
 * a {@link Writer} object at construction. If you require control over the output, you will
 * need to use a suitable {@link XMLStreamWriter} implementation that allows it.
 * For example <a href="http://www.java2s.com/Open-Source/Java-Document/XML/stax-utils/javanet.staxutils.htm">javanet.staxutils</a>.</p>
 */
public class FudgeXMLStreamWriter extends EventBasedFudgeStreamWriter {

  /**
   * The XML settings.
   */
  private FudgeXMLSettings _settings;
  /**
   * The XML writer.
   */
  private XMLStreamWriter _writer;

  /**
   * Creates a new instance for writing a Fudge stream to an XML writer.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param writer  the underlying writer, not null
   */
  public FudgeXMLStreamWriter(final FudgeContext fudgeContext, final XMLStreamWriter writer) {
    this(fudgeContext, writer, new FudgeXMLSettings());
  }

  /**
   * Creates a new instance for writing a Fudge stream to an XML writer.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param writer  the underlying writer, not null
   * @param settings  the XML settings to fine tune the output, not null
   */
  public FudgeXMLStreamWriter(final FudgeContext fudgeContext, final XMLStreamWriter writer, final FudgeXMLSettings settings) {
    super(fudgeContext);
    if (writer == null) {
      throw new NullPointerException("XMLStreamWriter must not be null");
    }
    if (settings == null) {
      throw new NullPointerException("FudgeXMLSettings must not be null");
    }
    _settings = settings;
    _writer = writer;
  }

  /**
   * Creates a new instance for writing a Fudge stream to a writer.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param writer  the underlying writer, not null
   */
  public FudgeXMLStreamWriter(final FudgeContext fudgeContext, final Writer writer) {
    this(fudgeContext, createXMLStreamWriter(writer));
  }

  /**
   * Creates a new instance for writing a Fudge stream to a writer.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param writer  the underlying writer, not null
   * @param settings  the XML settings to fine tune the output, not null
   */
  public FudgeXMLStreamWriter(final FudgeContext fudgeContext, final Writer writer, final FudgeXMLSettings settings) {
    this(fudgeContext, createXMLStreamWriter(writer), settings);
  }

  /**
   * Efficiently converts a writer to an XML writer.
   * 
   * @param writer  the writer to convert, not null
   * @return the XML writer, not null
   */
  private static XMLStreamWriter createXMLStreamWriter(final Writer writer) {
    try {
      return XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
    } catch (XMLStreamException ex) {
      throw wrapException("create", ex);
    }
  }

  /**
   * Wraps an exception into a runtime exception.
   * 
   * @param operation  the operation being attempted when the exception was caught
   * @param ex the exception caught
   * @return A fudgeRuntimeException 
   */
  protected static FudgeRuntimeException wrapException(final String operation, final XMLStreamException ex) {
    if (ex.getCause() instanceof IOException) {
      return new FudgeRuntimeIOException((IOException) ex.getCause());
    } else {
      return new FudgeRuntimeException("Couldn't " + operation + " XML stream", ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the XML settings.
   * 
   * @return the settings, not null
   */
  public FudgeXMLSettings getSettings() {
    return _settings;
  }

  /**
   * Sets the XML settings.
   * 
   * @param settings  the settings, not null
   */
  public void setSettings(FudgeXMLSettings settings) {
    if (settings == null) {
      throw new NullPointerException("FudgeXMLSettings must not be null");
    }
    _settings = settings;
  }

  /**
   * Gets the XML writer.
   * 
   * @return the writer, not null
   */
  public XMLStreamWriter getWriter() {
    return _writer;
  }

  /**
   * Sets the XML writer.
   * 
   * @param writer  the writer, not null
   */
  public void setWriter(XMLStreamWriter writer) {
    if (writer == null) {
      throw new NullPointerException("XMLStreamWriter must not be null");
    }
    _writer = writer;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void fudgeEnvelopeStart(int processingDirectives, int schemaVersion) {
    try {
      getWriter().writeStartDocument();
      if (getEnvelopeElementName() != null) {
        getWriter().writeStartElement(getEnvelopeElementName());
        if ((processingDirectives != 0) && (getEnvelopeAttributeProcessingDirectives() != null)) {
          getWriter().writeAttribute(getEnvelopeAttributeProcessingDirectives(), Integer.toString (processingDirectives));
        }
        if ((schemaVersion != 0) && (getEnvelopeAttributeSchemaVersion() != null)) {
          getWriter().writeAttribute (getEnvelopeAttributeSchemaVersion(), Integer.toString (schemaVersion));
        }
        if (getCurrentTaxonomyId() != 0 && getSettingsEnvelopeAttributeTaxonomy() != null) {
          getWriter().writeAttribute(getSettingsEnvelopeAttributeTaxonomy(), Integer.toString(getCurrentTaxonomyId()));
        }
      }
    } catch (XMLStreamException e) {
      throw wrapException ("write envelope header to", e);
    }
  }

  private String getSettingsEnvelopeAttributeTaxonomy() {
    return _settings.getEnvelopeAttributeTaxonomy();
  }

  @Override
  protected void fudgeEnvelopeEnd() {
    try {
      if (getEnvelopeElementName() != null) {
        getWriter().writeEndElement(); // envelope
      }
      getWriter().writeEndDocument ();
    } catch (XMLStreamException e) {
      throw wrapException ("write envelope end to", e);
    }
  }
  
  @Override
  protected boolean fudgeFieldStart (final Integer ordinal, final String name, final FudgeFieldType type) {
    try {
      return writeFudgeFieldStart(ordinal, name, type);
    } catch (XMLStreamException e) {
      throw wrapException ("write field start to", e);
    }
  }

  private boolean writeFudgeFieldStart(final Integer ordinal, final String name, final FudgeFieldType type)
      throws XMLStreamException {
    String elementName = null;
    if (getPreserveFieldNames()) {
      elementName = convertFieldName(name);
    }
    if (elementName == null) {
      if (ordinal != null) {
        if (getCurrentTaxonomy() != null) {
          elementName = convertFieldName(getCurrentTaxonomy().getFieldName(ordinal));
        }
      }
      if (elementName == null) {
        elementName = getFieldElementName();
        if ((elementName != null) && (ordinal != null) && getAppendFieldOrdinal()) {
          elementName = elementName + ordinal;
        }
      }
    }
    if (elementName == null) {
      return false;
    }
    getWriter().writeStartElement(elementName);
    if ((ordinal != null) && (getFieldAttributeOrdinal() != null)) {
      getWriter().writeAttribute(getFieldAttributeOrdinal(), ordinal.toString());
    }
    if ((name != null) && !name.equals(elementName) && (getFieldAttributeName() != null)) {
      getWriter().writeAttribute(getFieldAttributeName(), name);
    }
    if (getFieldAttributeType() != null) {
      final String typeString = fudgeTypeIdToString(type.getTypeId());
      if (typeString != null) {
        getWriter ().writeAttribute(getFieldAttributeType(), typeString);
      }
    }
    return true;
  }
  
  private String fudgeTypeIdToString(int typeId) {
    return _settings.fudgeTypeIdToString(typeId);
  }

  private String getFieldAttributeType() {
    return _settings.getFieldAttributeType();
  }

  private String getFieldAttributeName() {
    return _settings.getFieldAttributeName();
  }

  private String getFieldAttributeOrdinal() {
    return _settings.getFieldAttributeOrdinal();
  }

  private boolean getAppendFieldOrdinal() {
    return _settings.getAppendFieldOrdinal();
  }

  private String getFieldElementName() {
    return _settings.getFieldElementName();
  }

  private boolean getPreserveFieldNames() {
    return _settings.getPreserveFieldNames();
  }

  @Override
  protected void fudgeFieldValue (final FudgeFieldType type, final Object fieldValue) {
    try {
      writeFudgeFieldValue(type, fieldValue);
    } catch (XMLStreamException e) {
      throw wrapException ("write field value to", e);
    }
  }
  
  @SuppressWarnings("unchecked")
  private void writeFudgeFieldValue(FudgeFieldType type, Object fieldValue) throws XMLStreamException {

    if (type instanceof SecondaryFieldTypeBase<?,?,?>) {
      fieldValue = ((SecondaryFieldTypeBase<Object,Object,Object>)type).secondaryToPrimary(fieldValue);
    }
    switch (type.getTypeId()) {
    case FudgeWireType.INDICATOR_TYPE_ID :
      // no content
      break;
    case FudgeWireType.BOOLEAN_TYPE_ID:
      getWriter().writeCharacters((Boolean)fieldValue ? getBooleanTrue() : getBooleanFalse());
      break;
    case FudgeWireType.BYTE_TYPE_ID:
    case FudgeWireType.SHORT_TYPE_ID:
    case FudgeWireType.INT_TYPE_ID:
    case FudgeWireType.LONG_TYPE_ID:
    case FudgeWireType.FLOAT_TYPE_ID:
    case FudgeWireType.DOUBLE_TYPE_ID:
    case FudgeWireType.STRING_TYPE_ID:
    case FudgeWireType.DATE_TYPE_ID:
    case FudgeWireType.TIME_TYPE_ID:
    case FudgeWireType.DATETIME_TYPE_ID:
      getWriter().writeCharacters(fieldValue.toString());
      break;
    case FudgeWireType.BYTE_ARRAY_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_4_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_8_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_16_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_20_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_32_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_64_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_128_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_256_TYPE_ID:
    case FudgeWireType.BYTE_ARRAY_512_TYPE_ID:
      writeArray((byte[])fieldValue);
      break;
    case FudgeWireType.SHORT_ARRAY_TYPE_ID:
      writeArray((short[])fieldValue);
      break;
    case FudgeWireType.INT_ARRAY_TYPE_ID:
      writeArray((int[])fieldValue);
      break;
    case FudgeWireType.LONG_ARRAY_TYPE_ID:
      writeArray((long[])fieldValue);
      break;
    case FudgeWireType.FLOAT_ARRAY_TYPE_ID:
      writeArray((float[])fieldValue);
      break;
    case FudgeWireType.DOUBLE_ARRAY_TYPE_ID:
      writeArray((double[])fieldValue);
      break;
    default :
      if (getBase64UnknownTypes()) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(new Base64OutputStream(baos));
        try {
          type.writeValue(dos, fieldValue);
          dos.close ();
        } catch (IOException e) {
          throw new FudgeRuntimeIOException (e);
        }
        if (getFieldAttributeEncoding() != null) {
          getWriter().writeAttribute(getFieldAttributeEncoding(), getBase64EncodingName());
        }
        getWriter().writeCharacters(new String(baos.toByteArray()));
      } else {
        getWriter().writeCharacters(fieldValue.toString ());
      }
      break;
    }  
  }

  private String getBase64EncodingName() {
    return _settings.getBase64EncodingName();
  }

  private String getFieldAttributeEncoding() {
    return _settings.getFieldAttributeEncoding();
  }

  private boolean getBase64UnknownTypes() {
    return _settings.getBase64UnknownTypes();
  }

  private String getBooleanFalse() {
    return _settings.getBooleanFalse();
  }

  private String getBooleanTrue() {
    return _settings.getBooleanTrue();
  }
  
  private void writeArray (final byte[] array) throws XMLStreamException {
    boolean first = true;
    for (byte value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Byte.toString (value));
    }
  }
  
  private void writeArray (final short[] array) throws XMLStreamException {
    boolean first = true;
    for (short value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Short.toString (value));
    }
  }
  
  private void writeArray (final int[] array) throws XMLStreamException {
    boolean first = true;
    for (int value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Integer.toString (value));
    }
  }
  
  private void writeArray (final long[] array) throws XMLStreamException {
    boolean first = true;
    for (long value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Long.toString (value));
    }
  }
  
  private void writeArray (final float[] array) throws XMLStreamException {
    boolean first = true;
    for (float value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Float.toString (value));
    }
  }
  
  private void writeArray (final double[] array) throws XMLStreamException {
    boolean first = true;
    for (double value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Double.toString (value));
    }
  }

  @Override
  protected void fudgeFieldEnd() {
    try {
      getWriter().writeEndElement();
    } catch (XMLStreamException e) {
      throw wrapException ("write field end to", e);
    }
  }
  
  /**
   * Remove any invalid characters to leave an XML element name.
   */
  private String convertFieldName(String str) {
    /*
     * nameStartChar :=  ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D] | [#x37F-#x1FFF]
     *                | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF]
     *                | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
     * nameChar := nameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
     */
    if (str == null) return null;
    final StringBuilder sb = new StringBuilder ();
    boolean firstChar = true;
    for (int i = 0; i < str.length (); i++) {
      final char c = str.charAt (i);
      if ((c == ':')
       || ((c >= 'A') && (c <= 'Z'))
       || (c == '_')
       || ((c >= 'a') && (c <= 'z'))
       || ((c >= 0xC0) && (c <= 0xD6))
       || ((c >= 0xD8) && (c <= 0xF6))
       || ((c >= 0xF8) && (c <= 0x2FF))
       || ((c >= 0x370) && (c <= 0x37D))
       || ((c >= 0x37F) && (c <= 0x1FFF))
       || ((c >= 0x200C) && (c <= 0x200D))
       || ((c >= 0x2070) && (c <= 0x2FEF))
       || ((c >= 0x3001) && (c <= 0xD7FF))
       || ((c >= 0xF900) && (c <= 0xFDCF))
       || ((c >= 0xFDF0) && (c <= 0xFFFD))
       || ((c >= 0x10000) && (c <= 0xEFFFF))) {
        firstChar = false;
        sb.append (c);
      } else if (!firstChar) {
        if ((c == '-')
         || (c == '.')
         || ((c >= '0') && (c <= '9'))
         || (c == 0xB7)
         || ((c >= 0x300) && (c <= 0x36F))
         || ((c >= 0x203F) && (c <= 0x2040))) {
          sb.append (c);
        }
      }
    }
    return (sb.length () > 0) ? sb.toString () : null;
  }
  
  private String getEnvelopeElementName() {
    return _settings.getEnvelopeElementName();
  }
  
  private String getEnvelopeAttributeProcessingDirectives() {
    return _settings.getEnvelopeAttributeProcessingDirectives();
  }
  
  private String getEnvelopeAttributeSchemaVersion() {
    return _settings.getEnvelopeAttributeSchemaVersion();
  }

  @Override
  public void flush() {
    try {
      _writer.flush();
    } catch (XMLStreamException e) {
      throw wrapException("flush", e);
    }
  }
  
  @Override
  public void close () {
    try {
      _writer.close ();
    } catch (XMLStreamException e) {
      throw wrapException("close", e);
    }
  }

}

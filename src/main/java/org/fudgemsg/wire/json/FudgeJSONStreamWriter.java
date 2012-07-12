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
package org.fudgemsg.wire.json;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.*;
import org.fudgemsg.types.SecondaryFieldTypeBase;
import org.fudgemsg.wire.EventBasedFudgeStreamWriter;
import org.fudgemsg.wire.FudgeRuntimeIOException;
import org.fudgemsg.wire.types.FudgeWireType;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * A Fudge writer that produces JSON.
 * <p>
 * This writer writes a Fudge stream as JSON to a text stream.
 * This can be used for JSON output, or can be used to assist in developing/debugging
 * a streaming serializer without having to inspect the binary output.
 * <p>
 * Please refer to <a href="http://wiki.fudgemsg.org/display/FDG/JSON+Fudge+Messages">JSON Fudge Messages</a>
 * for details on the representation.
 */
public class FudgeJSONStreamWriter extends EventBasedFudgeStreamWriter {

  /**
   * The JSON settings.
   */
  private final FudgeJSONSettings _settings;
  /**
   * The underlying writer.
   */
  private final Writer _underlyingWriter;
  /**
   * The JSON writer.
   */
  private JSONWriter _writer;

  private boolean _headerFinished = false;
  private String _duplicateFieldPostfix = "_";

  /**
   * Creates a new instance for writing a Fudge stream to a JSON writer.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param writer  the underlying writer, not null
   */
  public FudgeJSONStreamWriter(final FudgeContext fudgeContext, final Writer writer) {
    this(fudgeContext, writer, new FudgeJSONSettings());
  }

  /**
   * Creates a new stream writer for writing Fudge messages in JSON format to a given
   * {@link Writer}.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param writer  the underlying writer, not null
   * @param settings  the JSON settings, not null
   */
  public FudgeJSONStreamWriter(final FudgeContext fudgeContext, final Writer writer, final FudgeJSONSettings settings) {
    super(fudgeContext);
    if (writer == null) {
      throw new NullPointerException("XMLStreamWriter must not be null");
    }
    if (settings == null) {
      throw new NullPointerException("FudgeXMLSettings must not be null");
    }
    _settings = settings;
    _underlyingWriter = writer;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JSON settings.
   * 
   * @return the JSON settings, not null
   */
  public FudgeJSONSettings getSettings() {
    return _settings;
  }

  /**
   * Gets the JSON writer being used, allocating one if necessary.
   * 
   * @return the writer, not null
   */
  protected JSONWriter getWriter() {
    if (_writer == null) {
      _writer = new JSONWriter(getUnderlying());
    }
    return _writer;
  }

  /**
   * Discards the JSON writer.
   * The implementation only allows a single use so we must drop the instance
   * after each message envelope completes.
   */
  protected void clearWriter() {
    _writer = null;
    _headerFinished = false;
    _duplicateFieldPostfix = "_";
  }

  /**
   * Gets the underlying {@link Writer} that is wrapped by {@link JSONWriter} instances for messages.
   * 
   * @return the writer, not null
   */
  protected Writer getUnderlying() {
    return _underlyingWriter;
  }

  //-------------------------------------------------------------------------
  /**
   * Wraps a JSON exception (which may in turn wrap {@link IOException} into
   * either a {@link FudgeRuntimeException} or {@link FudgeRuntimeIOException}.
   * 
   * @param message message describing the current operation
   * @param e the originating exception
   */
  protected void wrapException(String message, final JSONException e) {
    message = "Error writing " + message + " to JSON stream";
    if (e.getCause() instanceof IOException) {
      throw new FudgeRuntimeIOException(message, (IOException) e.getCause());
    } else {
      throw new FudgeRuntimeException(message, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() {
    if (getUnderlying() != null) {
      try {
        getUnderlying().flush();
      } catch (IOException e) {
        throw new FudgeRuntimeIOException(e);
      }
    }
  }

  /**
   * Begins a JSON object with the processing directives, schema and taxonomy.
   */
  @Override
  protected void fudgeEnvelopeStart (final int processingDirectives, final int schemaVersion) {
    try {
      getWriter().array();
      getWriter().object();
      if ((processingDirectives != 0) && (getSettings ().getProcessingDirectivesField () != null)) getWriter ().key (getSettings ().getProcessingDirectivesField ()).value (processingDirectives);
      if ((schemaVersion != 0) && (getSettings ().getSchemaVersionField () != null)) getWriter ().key (getSettings ().getSchemaVersionField ()).value (schemaVersion);
      if ((getCurrentTaxonomyId () != 0) && (getSettings ().getTaxonomyField () != null)) getWriter ().key (getSettings ().getTaxonomyField ()).value (getCurrentTaxonomyId ());
    } catch (JSONException e) {
      wrapException ("start of message", e);
    }
  }

  /*
    Predicate returning true if any of the given strings has matching postfix
   */
  private boolean anyEndsWith(Set<String> strings, String postfix) {
    for (String string : strings) {
      if (string.matches(".*" + postfix)) {
        return true;
      }
    }
    return false;
  }

  /*
   Predicate returning true if any of the given field name has matching postfix
  */
  private boolean anyEndsWith(Iterable<FudgeField> fields, String postfix) {
    for (FudgeField field : fields) {
      if (field.getName() != null && field.getName().matches(".*" + postfix)) {
        return true;
      }
    }
    return false;
  }

  private String duplicateFieldPostfix(String postfix, Iterable<FudgeField> fields) {
    while (anyEndsWith(fields, postfix + "\\d*")) {
      postfix = postfix + "_";
    }
    for (FudgeField field : fields) {
      if (field.getType().equals(FudgeWireType.SUB_MESSAGE)) {
        FudgeMsg msg = (FudgeMsg) field.getValue();
        postfix = duplicateFieldPostfix(postfix, msg.getAllFields());
      }
    }
    return postfix;
  }

  /**
   * Writes each field.
   * <p>
   * This implementation loops around the iterator and writes each field using
   * {@link #writeField(org.fudgemsg.FudgeField)}.
   *
   * @param fields  an iterator over a set of fields, typically a message, not null
   */
  protected void writeAllFields(final Iterable<FudgeField> fields) {
    if (!_headerFinished) {
      _headerFinished = true;
      _duplicateFieldPostfix = duplicateFieldPostfix("_", fields);
      try {
        // continue with the header            
        writeField(UnmodifiableFudgeField.of(FudgeWireType.STRING, _duplicateFieldPostfix, FudgeJSONSettings.DUPLICATE_FIELD_NAME_POSTFIX_KEY));
        // header finished
        getWriter().endObject();
        getWriter().object();
      } catch (JSONException e) {
        wrapException("write metadata to the header", e);
      }
    }
    Map<String, Integer> duplicateKeys = new HashMap<String, Integer>();
    for (FudgeField field : fields) {
      if (duplicateKeys.containsKey(field.getName())) {
        int idx = duplicateKeys.get(field.getName()) + 1;
        duplicateKeys.put(field.getName(), idx);
        String newName = field.getName() + _duplicateFieldPostfix + idx;
        writeField(UnmodifiableFudgeField.of(field.getType(), field.getValue(), newName));
      } else {
        duplicateKeys.put(field.getName(), 0);
        writeField(field);
      }
    }
  }

  /**
   * Ends the JSON object.
   */
  @Override
  protected void fudgeEnvelopeEnd () {
    try {
      getWriter().endObject();
      getWriter().endArray();
      clearWriter();
    } catch (JSONException e) {
      wrapException ("end of message", e);
    }
  }
  
  /**
   * Writes out the field name to the JSON object.
   */
  @Override
  protected boolean fudgeFieldStart (Integer ordinal, String name, FudgeFieldType type) {
    try {
      if (getSettings ().getPreserveFieldNames ()) {
        if (name != null) {
          getWriter ().key (name);
        } else if (ordinal != null) {
          getWriter ().key (ordinal.toString());
        } else {
          getWriter ().key ("");
        }
      } else {
        if (ordinal != null) {
          getWriter ().key (ordinal.toString());
        } else if (name != null) {
          getWriter ().key (name);
        } else {
          getWriter ().key ("");
        }
      }
    } catch (JSONException e) {
      wrapException ("start of field", e);
    }
    return true;
  }
  
  protected void writeArray (final byte[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  protected void writeArray (final short[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  protected void writeArray (final int[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  protected void writeArray (final long[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  protected void writeArray (final float[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  protected void writeArray (final double[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  /**
   * Writes the field value to the JSON object.
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void fudgeFieldValue (FudgeFieldType type, Object fieldValue) {
    try {
      if (type instanceof SecondaryFieldTypeBase<?,?,?>) {
        fieldValue = ((SecondaryFieldTypeBase<Object,Object,Object>)type).secondaryToPrimary(fieldValue);
      }
      switch (type.getTypeId ()) {
      case FudgeWireType.INDICATOR_TYPE_ID :
        getWriter ().value (null);
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
        writeArray ((byte[])fieldValue);
        break;
      case FudgeWireType.SHORT_ARRAY_TYPE_ID:
        writeArray ((short[])fieldValue);
        break;
      case FudgeWireType.INT_ARRAY_TYPE_ID:
        writeArray ((int[])fieldValue);
        break;
      case FudgeWireType.LONG_ARRAY_TYPE_ID:
        writeArray ((long[])fieldValue);
        break;
      case FudgeWireType.FLOAT_ARRAY_TYPE_ID:
        writeArray ((float[])fieldValue);
        break;
      case FudgeWireType.DOUBLE_ARRAY_TYPE_ID:
        writeArray ((double[])fieldValue);
        break;
      default :
        getWriter ().value (fieldValue);
        break;
      }
    } catch (JSONException e) {
      wrapException ("field value", e);
    }
  }
  
  /**
   * Starts a sub-object within the JSON object.  
   */
  @Override
  protected void fudgeSubMessageStart () {
    try {
      getWriter ().object ();
    } catch (JSONException e) {
      wrapException ("start of submessage", e);
    }
  }
  
  /**
   * Ends the JSON sub-object.
   */
  @Override
  protected void fudgeSubMessageEnd () {
    try {
      getWriter ().endObject ();
    } catch (JSONException e) {
      wrapException ("end of submessage", e);
    }
  }
  
}
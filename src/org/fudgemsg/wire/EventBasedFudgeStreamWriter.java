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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeTypeDictionary;

/**
 * Abstract Fudge writer that supplies events on major state changes.
 * <p>
 * This base implementation is designed to allow the construction of alternate writers.
 * Examples would be XML and JSON formats.
 * The subclass should implement the protected methods and write out data as necessary.
 */
public abstract class EventBasedFudgeStreamWriter extends AbstractFudgeStreamWriter {

  /**
   * Creates a new instance.
   * 
   * @param fudgeContext  the Fudge context to use, not null
   */
  protected EventBasedFudgeStreamWriter(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  //-------------------------------------------------------------------------
  @Override
  public void writeEnvelopeHeader(int processingDirectives, int schemaVersion, int messageSize) {
    fudgeEnvelopeStart(processingDirectives, schemaVersion);
  }

  @Override
  public void envelopeComplete() {
    fudgeEnvelopeEnd();
  }

  @Override
  public void writeField(FudgeField field) {
    if (field == null) {
      throw new NullPointerException("FudgeField must not be null");
    }
    writeField(field.getName(), field.getOrdinal(), field.getType(), field.getValue());
  }

  @Override
  public void writeField(String name, Integer ordinal, FudgeFieldType type, Object fieldValue) {
    if (fudgeFieldStart(ordinal, name, type)) {
      if (type.getTypeId() == FudgeTypeDictionary.FUDGE_MSG_TYPE_ID) {
        fudgeSubMessageStart();
        writeFields((FudgeMsg) fieldValue);
        fudgeSubMessageEnd();
      } else {
        fudgeFieldValue(type, fieldValue);
      }
      fudgeFieldEnd();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Event sent when an envelope starts.
   * This is sent before any other event.
   * 
   * @param processingDirectives  the envelope processing directives
   * @param schemaVersion  the envelope schema version
   */
  protected void fudgeEnvelopeStart(final int processingDirectives, final int schemaVersion) {
    // no-op
  }

  /**
   * Event sent when an envelope ends.
   * This is sent after all fields have been processed.
   */
  protected void fudgeEnvelopeEnd() {
    // no-op
  }

  /**
   * Event sent when a field starts.
   * 
   * @param ordinal  the field ordinal, may be null
   * @param name  the field name, may be null
   * @param type  the field type, not null
   * @return true to continue processing the field, false to ignore it (and other associated events)
   */
  protected boolean fudgeFieldStart(Integer ordinal, String name, FudgeFieldType type) {
    return true;
  }

  /**
   * Event sent when a field ends.
   */
  protected void fudgeFieldEnd() {
    // no-op
  }

  /**
   * Event sent for the field value.
   * This is sent between {@link #fudgeFieldStart} and {@link #fudgeFieldEnd}
   * for fields that are not sub messages.
   * 
   * @param type  the field type, not null
   * @param fieldValue  the value, not null
   */
  protected void fudgeFieldValue(FudgeFieldType type, Object fieldValue) {
    // no-op
  }

  /**
   * Event sent when a sub-message starts.
   * This is sent after the enclosing {@link #fudgeFieldStart}.
   */
  protected void fudgeSubMessageStart() {
    // no-op
  }

  /**
   * Event sent when a sub-message ends.
   * This is sent before the enclosing {@link #fudgeFieldEnd}.
   */
  protected void fudgeSubMessageEnd() {
    // no-op
  }

  //-------------------------------------------------------------------------
  /**
   * This implementation takes no action.
   */
  @Override
  public void flush() {
    // no-op
  }

  /**
   * This implementation takes no action.
   */
  @Override
  public void close() {
    // no-op
  }

}

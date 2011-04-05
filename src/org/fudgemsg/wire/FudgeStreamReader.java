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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.taxonomy.FudgeTaxonomy;

/**
 * A reader that can receive and interpret Fudge messages.
 * <p>
 * This interface provides the basic contract for classes that read Fudge messages.
 */
public interface FudgeStreamReader extends Closeable {

  /**
   * Constants for the four stream element types as returned by {@link #next()} and {@link #getCurrentElement()}.
   */
  public static enum FudgeStreamElement {
    /**
     * Issued when the envelope header is parsed.
     */
    MESSAGE_ENVELOPE,
    /**
     * Issued when a simple (non-hierarchical) field is encountered.
     */
    SIMPLE_FIELD,
    /**
     * Issued when a sub-Message field is encountered.
     */
    SUBMESSAGE_FIELD_START,
    /**
     * Issued when the end of a sub-Message field is reached.
     */
    SUBMESSAGE_FIELD_END
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Fudge context, used for type and taxonomy resolution.
   * 
   * @return the context, not null
   */
  FudgeContext getFudgeContext();

  //-------------------------------------------------------------------------
  /**
   * Checks if there is another element in the stream.
   * <p>
   * This checks if there is another element to read using a call to {@link #next()}.
   * A false result indicates the end of a message (or submessage) has been reached.
   * After the end of a sub-message, the next immediate call will indicate whether there are
   * further elements or the end of the outer message. After the end of the main message
   * referenced by the envelope header, the next immediate call may:
   * <ol>
   * <li>Return {@code false} if the source does not contain any subsequent Fudge messages; or</li>
   * <li>Return {@code true} if the source may contain further Fudge messages. Calling {@code next()} will return the envelope header
   * of the next message if one is present, or {@code null} if the source does not contain any further messages.</li>
   * </ol>
   * 
   * @return {@code true} if there is at least one more element to read
   */
  boolean hasNext();

  /**
   * Reads the next stream element from the source and returns the element type.
   * 
   * @return the type of the next element in the stream, null if the end of stream has been reached
   *   at a message boundary (i.e. attempting to read the first byte of an envelope)
   */
  FudgeStreamElement next();

  /**
   * Returns the value last returned by {@link #next()}.
   * 
   * @return the type of the current element in the stream
   */
  FudgeStreamElement getCurrentElement();

  /**
   * Skips a sub-message.
   * <p>
   * If a SUBMESSAGE_FIELD_START has just been encountered, this advances the stream
   * so that the next element read will be the field after the sub-message field.
   * The returned stream will contain the elements skipped over.
   * This is an optional operation and the stream may throw an
   * {@code UnsupportedOperationException} if it does not support it.
   * 
   * @return a reader for the skipped fields
   * @throws UnsupportedOperationException if the stream does not support this
   */
  FudgeStreamReader skipMessageField();

  //-------------------------------------------------------------------------
  /**
   * Gets the processing directives specified in the last envelope header read.
   * 
   * @return current processing directive flags 
   */
  int getProcessingDirectives();

  /**
   * Gets the schema version specified in the last envelope header read.
   * 
   * @return current message schema version
   */
  int getSchemaVersion();

  /**
   * Gets the taxonomy identifier specified in the last envelope header read.
   * 
   * @return current taxonomy identifier
   */
  short getTaxonomyId();

  /**
   * Gets the taxonomy associated with the taxonomy id in the last envelope header read.
   * This returns null if there is no associated taxonomy.
   * 
   * @return current taxonomy, null if non taxonomy
   */
  FudgeTaxonomy getTaxonomy();

  //-------------------------------------------------------------------------
  /**
   * Gets the field name of the current element if it is a field.
   * If the field has no name but does have an ordinal, then the taxonomy
   * will be used to attempt to lookup the name.
   * 
   * @return current field name, null if no name
   */
  String getFieldName();

  /**
   * Gets the field ordinal of the current element if it is a field.
   * 
   * @return current field ordinal, null if no ordinal
   */
  Integer getFieldOrdinal();

  /**
   * Gets the field type of the current element if it is a field.
   * 
   * @return current field type
   */
  FudgeFieldType getFieldType();

  /**
   * Gets the field value of the current element if it is a field.
   * 
   * @return current field value
   */
  Object getFieldValue();

  //-------------------------------------------------------------------------
  /**
   * Closes the reader, and attempts to close any underlying data source.
   */
  void close();

}

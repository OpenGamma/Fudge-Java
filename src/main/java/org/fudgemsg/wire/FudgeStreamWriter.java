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
import java.io.Flushable;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.taxonomy.FudgeTaxonomy;

/**
 * A writer that can send Fudge messages.
 * <p>
 * This interface provides the basic contract for classes that write Fudge messages.
 */
public interface FudgeStreamWriter extends Flushable, Closeable {

  /**
   * Gets the Fudge context that will be used for type and taxonomy resolution.
   * 
   * @return the context, not null
   */
  FudgeContext getFudgeContext();

  /**
   * Gets the taxonomy being used to encode fields.
   * <p>
   * A taxonomy is optionally used to encode field names into numeric ordinals.
   * This returns null if no taxonomy is in use, or the taxonomy identifier cannot
   * be resolved by the context.
   * 
   *  @return the taxonomy, not null
   */
  FudgeTaxonomy getCurrentTaxonomy();

  /**
   * Gets the taxonomy identifier.
   * 
   * @return the taxonomy identifier
   */
  int getCurrentTaxonomyId();

  /**
   * Sets the current taxonomy, by identifier, to be used to encode fields.
   * 
   * @param taxonomyId  the taxonomy identifier
   */
  void setCurrentTaxonomyId(final int taxonomyId);

  /**
   * Writes the message envelope header.
   * 
   * @param processingDirectives  the processing directive flags
   * @param version  the schema version value
   * @param messageSize  the Fudge encoded size of the underlying message, including the message envelope
   */
  void writeEnvelopeHeader(int processingDirectives, int version, int messageSize);

  /**
   * Event sent once the end of the message contained within an envelope is reached.
   * <p>
   * An implementation may not need to take any action at this point as the end of the envelope
   * can be detected based on the message size in the header.
   */
  void envelopeComplete();

  /**
   * Writes a single message field.
   * <p>
   * If the ordinal is not present and the name matches an entry in the current taxonomy,
   * then the name will be replaced by the taxonomy resolved ordinal.
   * 
   * @param field  the message field to write, not null
   */
  void writeField(FudgeField field);

  /**
   * Writes a single message field.
   * <p>
   * If the ordinal is omitted and the name matches an entry in the current taxonomy,
   * then the name will be replaced by the taxonomy resolved ordinal.
   * 
   * @param name  the name of the field, null if no name
   * @param ordinal  the ordinal index of the field, null if no ordinal
   * @param type  the type of the underlying data, not null
   * @param fieldValue  value of the field, not null
   */
  void writeField(String name, Integer ordinal, FudgeFieldType type, Object fieldValue);

  /**
   * Writes a set of fields.
   * <p>
   * If the ordinal of any field is not present and the name matches an entry in the current
   * taxonomy, then the name will be replaced by the taxonomy resolved ordinal.
   * 
   * @param fields  the fields to write, not null
   */
  void writeFields(Iterable<FudgeField> fields);

  /**
   * Flushes any data from the internal buffers.
   * <p>
   * This sends any stored data to the target stream, flushing the underlying stream if appropriate.
   */
  void flush();

  /**
   * Flushes and closes this write.
   * <p>
   * This attempts to close the underlying stream if appropriate.
   */
  void close();

}

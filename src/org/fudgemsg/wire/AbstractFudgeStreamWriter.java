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
import org.fudgemsg.taxonomy.FudgeTaxonomy;

/**
 * Abstract Fudge writer that stores the basic state.
 * <p>
 * This base implementation is designed to simplify the writing of standard writers.
 * It handles the context and taxonomy.
 */
public abstract class AbstractFudgeStreamWriter implements FudgeStreamWriter {

  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;
  /**
   * The taxonomy.
   */
  private FudgeTaxonomy _taxonomy;
  /**
   * The taxonomy id.
   */
  private int _taxonomyId;

  /**
   * Creates a new instance.
   * 
   * @param fudgeContext  the Fudge context to use, not null
   */
  protected AbstractFudgeStreamWriter(final FudgeContext fudgeContext) {
    if (fudgeContext == null) {
      throw new NullPointerException("FudgeContext must not be null");
    }
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public FudgeTaxonomy getCurrentTaxonomy() {
    return _taxonomy;
  }

  @Override
  public void setCurrentTaxonomyId(final int taxonomyId) {
    _taxonomyId = taxonomyId;
    _taxonomy = getFudgeContext().getTaxonomyResolver().resolveTaxonomy((short) taxonomyId);
  }

  @Override
  public int getCurrentTaxonomyId() {
    return _taxonomyId;
  }

  /**
   * Clears the taxonomy information.
   */
  protected void clearTaxonomy() {
    _taxonomy = null;
    _taxonomyId = 0;
  }

  //-------------------------------------------------------------------------
  /**
   * Writes each field.
   * <p>
   * This implementation loops around the iterator and writes each field using
   * {@link #writeAllFields(Iterable)}.
   * 
   * @param fields  an iterator over a set of fields, typically a message, not null
   */
  public void writeFields(final Iterable<FudgeField> fields) {
    // delegation to protected method allows more advanced subclasses to override this
    writeAllFields(fields);
  }

  /**
   * Writes each field.
   * <p>
   * This implementation loops around the iterator and writes each field using
   * {@link #writeField(FudgeField)}.
   * 
   * @param fields  an iterator over a set of fields, typically a message, not null
   */
  protected void writeAllFields(final Iterable<FudgeField> fields) {
    for (FudgeField field : fields) {
      writeField(field);
    }
  }

  /**
   * Writes a single message field.
   * <p>
   * This implementation checks for null and delegates to
   * {@link #writeField(String, Integer, FudgeFieldType, Object)}.
   * 
   * @param field  the message field to write, not null
   */
  @Override
  public void writeField(FudgeField field) {
    if (field == null) {
      throw new NullPointerException("FudgeField must not be null");
    }
    writeField(field.getName(), field.getOrdinal(), field.getType(), field.getValue());
  }

}

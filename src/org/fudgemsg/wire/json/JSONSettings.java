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

/**
 * Tunable parameters for the JSON encoding/decoding.
 * <p>
 * Please refer to <a href="http://wiki.fudgemsg.org/display/FDG/JSON+Fudge+Messages">JSON Fudge Messages</a>
 * for details on the representation.
 */
public class JSONSettings {

  /**
   * Default name for the processing directives field.
   */
  public static final String DEFAULT_PROCESSINGDIRECTIVES_FIELD = "fudgeProcessingDirectives";
  /**
   * Default name for the schema version field.
   */
  public static final String DEFAULT_SCHEMAVERSION_FIELD = "fudgeSchemaVersion";
  /**
   * Default name for the taxonomy field.
   */
  public static final String DEFAULT_TAXONOMY_FIELD = "fudgeTaxonomy";

  /**
   * The processing directives field name.
   */
  private String _processingDirectivesField = DEFAULT_PROCESSINGDIRECTIVES_FIELD;
  /**
   * The version field name.
   */
  private String _schemaVersionField = DEFAULT_SCHEMAVERSION_FIELD;
  /**
   * The taxonomy field name.
   */
  private String _taxonomyField = DEFAULT_TAXONOMY_FIELD;
  /**
   * Whether to prefer field names.
   */
  private boolean _preferFieldNames = true;

  /**
   * Creates a new settings object with the default values.
   */
  public JSONSettings() {
  }

  /**
   * Creates a new settings object copying the current values from another.
   * 
   * @param copy  the object to copy the settings from
   */
  public JSONSettings(final JSONSettings copy) {
    setProcessingDirectivesField(copy.getProcessingDirectivesField());
    setSchemaVersionField(copy.getSchemaVersionField());
    setTaxonomyField(copy.getTaxonomyField());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the field to use for the processing directives.
   * 
   * @return the field name, null to omit the directives
   */
  public String getProcessingDirectivesField() {
    return _processingDirectivesField;
  }

  /**
   * Sets the name of the field to use for the processing directives.
   * Set to {@code null} to omit the field.
   * 
   * @param processingDirectivesField  the field name, null to omit the directives
   */
  public void setProcessingDirectivesField(final String processingDirectivesField) {
    _processingDirectivesField = processingDirectivesField;
  }

  /**
   * Gets the name of the field to use for the schema version.
   * 
   * @return the field name, null to omit the version
   */
  public String getSchemaVersionField() {
    return _schemaVersionField;
  }

  /**
   * Sets the name of the field to use for the schema version.
   * Set to {@code null} to omit the field.
   * 
   * @param schemaVersionField  the field name, null to omit the version
   */
  public void setSchemaVersionField(final String schemaVersionField) {
    _schemaVersionField = schemaVersionField;
  }

  /**
   * Gets the name of the field to use for the taxonomy.
   * 
   * @return the field name, null to omit the taxonomy
   */
  public String getTaxonomyField() {
    return _taxonomyField;
  }

  /**
   * Sets the name of the field to use for the taxonomy.
   * Set to {@code null} to omit the field.
   * 
   * @param taxonomyField  the field name, null to omit the taxonomy
   */
  public void setTaxonomyField(final String taxonomyField) {
    _taxonomyField = taxonomyField;
  }

  /**
   * Gets whether to prefer field names.
   * 
   * @return true to prefer field names
   */
  public boolean getPreferFieldNames() {
    return _preferFieldNames;
  }

  /**
   * Sets whether to prefer field names.
   * 
   * @param preferFieldNames  true to prefer field names
   */
  public void setPreferFieldNames(final boolean preferFieldNames) {
    _preferFieldNames = preferFieldNames;
  }

}

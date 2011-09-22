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

import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Tunable parameters for the JSON encoding/decoding.
 * <p>
 * Please refer to <a href="http://wiki.fudgemsg.org/display/FDG/JSON+Fudge+Messages">JSON Fudge Messages</a>
 * for details on the representation.
 */
public class FudgeJSONSettings {

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
   * Default type field name suffix
   */
  public static final String DEFAULT_TYPE_SUFFIX = "~typ";
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
   * Whether to preserve field names.
   */
  private boolean _preserveFieldNames = true;
  /**
   * The type field name suffix
   */
  private String _typeSuffix = DEFAULT_TYPE_SUFFIX;
  
  private final Map<Integer, String> _fudgeTypesToIdentifier = new HashMap<Integer, String>();
  private final Map<String, Integer> _identifiersToFudgeType = new HashMap<String, Integer>();
  
  /**
   * Creates a new settings object with the default values.
   */
  public FudgeJSONSettings() {
    registerFudgeType(FudgeWireType.INDICATOR_TYPE_ID, "indicator");
    registerFudgeType(FudgeWireType.BOOLEAN_TYPE_ID, "boolean", "bool");
    registerFudgeType(FudgeWireType.BYTE_TYPE_ID, "byte", "int8");
    registerFudgeType(FudgeWireType.SHORT_TYPE_ID, "short", "int16");
    registerFudgeType(FudgeWireType.INT_TYPE_ID, "int", "int32");
    registerFudgeType(FudgeWireType.LONG_TYPE_ID, "long", "int64");
    registerFudgeType(FudgeWireType.BYTE_ARRAY_TYPE_ID, "byte[]");
    registerFudgeType(FudgeWireType.SHORT_ARRAY_TYPE_ID, "short[]");
    registerFudgeType(FudgeWireType.INT_ARRAY_TYPE_ID, "int[]");
    registerFudgeType(FudgeWireType.LONG_ARRAY_TYPE_ID, "long[]");
    registerFudgeType(FudgeWireType.FLOAT_TYPE_ID, "float");
    registerFudgeType(FudgeWireType.DOUBLE_TYPE_ID, "double");
    registerFudgeType(FudgeWireType.FLOAT_ARRAY_TYPE_ID, "float[]");
    registerFudgeType(FudgeWireType.DOUBLE_ARRAY_TYPE_ID, "double[]");
    registerFudgeType(FudgeWireType.STRING_TYPE_ID, "string");
    registerFudgeType(FudgeWireType.SUB_MESSAGE_TYPE_ID, "message");
    registerFudgeType(FudgeWireType.BYTE_ARRAY_4_TYPE_ID, "byte[4]");
    registerFudgeType(FudgeWireType.BYTE_ARRAY_8_TYPE_ID, "byte[8]");
    registerFudgeType(FudgeWireType.BYTE_ARRAY_16_TYPE_ID, "byte[16]");
    registerFudgeType(FudgeWireType.BYTE_ARRAY_20_TYPE_ID, "byte[20]");
    registerFudgeType(FudgeWireType.BYTE_ARRAY_32_TYPE_ID, "byte[32]");
    registerFudgeType(FudgeWireType.BYTE_ARRAY_64_TYPE_ID, "byte[64]");
    registerFudgeType(FudgeWireType.BYTE_ARRAY_128_TYPE_ID, "byte[128]");
    registerFudgeType(FudgeWireType.BYTE_ARRAY_256_TYPE_ID, "byte[256]");
    registerFudgeType(FudgeWireType.BYTE_ARRAY_512_TYPE_ID, "byte[512]");
    registerFudgeType(FudgeWireType.DATE_TYPE_ID, "date");
    registerFudgeType(FudgeWireType.TIME_TYPE_ID, "time");
    registerFudgeType(FudgeWireType.DATETIME_TYPE_ID, "datetime");
  }

  /**
   * Creates a new settings object copying the current values from another.
   * 
   * @param copy  the object to copy the settings from
   */
  public FudgeJSONSettings(final FudgeJSONSettings copy) {
    setProcessingDirectivesField(copy.getProcessingDirectivesField());
    setSchemaVersionField(copy.getSchemaVersionField());
    setTaxonomyField(copy.getTaxonomyField());
  }
  
  /**
   * @return the map of identifiers
   */
  public Map<String, Integer> getIdentifiersToFudgeType() {
    return _identifiersToFudgeType;
  }

  /**
   * @return the map of identifiers
   */
  public Map<Integer, String> getFudgeTypesToIdentifier() {
    return _fudgeTypesToIdentifier;
  }
  
  /**
   * @param type  the type code
   * @param identifiers  the type identifiers
   */
  protected void registerFudgeType(final int type, final String... identifiers) {
    getFudgeTypesToIdentifier().put(type, (identifiers[0] == null) ? "" : identifiers[0]);
    for (String identifier : identifiers) {
      if (identifier != null) {
        getIdentifiersToFudgeType().put(identifier.toLowerCase(), type);
      }
    }
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
   * Gets whether to preserve field names.
   * 
   * @return true to preserve field names
   */
  public boolean getPreserveFieldNames() {
    return _preserveFieldNames;
  }

  /**
   * Sets whether to preserve field names.
   * 
   * @param preserveFieldNames  true to prefer field names
   */
  public void setPreserveFieldNames(final boolean preserveFieldNames) {
    _preserveFieldNames = preserveFieldNames;
  }

  /**
   * @return the typeSuffix
   */
  public String getTypeSuffix() {
    return _typeSuffix;
  }

  /**
   * @param typeSuffix the typeSuffix to set
   */
  public void setTypeSuffix(final String typeSuffix) {
    _typeSuffix = typeSuffix;
  }
  
  /**
   * Returns a type string for a Fudge type identifier.
   * @param type  the type code
   * @return the textual name
   */
  public String fudgeTypeIdToString(final int type) {
    String s = getFudgeTypesToIdentifier().get(type);
    return (s == null) ? Integer.toString(type) : ((s.length() == 0) ? null : s);
  }

  /**
   * Returns a Fudge type identifier for a given string.
   * @param str  the textual type name
   * @return the type code
   */
  public Integer stringToFudgeTypeId(final String str) {
    return getIdentifiersToFudgeType().get(str.toLowerCase());
  }
  
}

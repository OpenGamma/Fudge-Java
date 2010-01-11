/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc. and other contributors.
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
package org.fudgemsg.taxon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeFieldContainer;

/**
 * An implementation of {@link FudgeTaxonomy} where all lookups are specified
 * at construction time and held in a {@link Map}.
 * This is extremely useful in a case where the taxonomy is generated dynamically,
 * or as a building block for loading taxonomy definitions from persistent
 * storage.
 *
 * @author kirk
 */
public class MapFudgeTaxonomy implements FudgeTaxonomy {
  private final Map<Integer, String> _namesByOrdinal;
  private final Map<String, Integer> _ordinalsByName;
  
  public MapFudgeTaxonomy() {
    this(Collections.<Integer,String>emptyMap());
  }
  
  public MapFudgeTaxonomy(Map<Integer, String> namesByOrdinal) {
    if(namesByOrdinal == null) {
      namesByOrdinal = Collections.emptyMap();
    }
    _namesByOrdinal = new HashMap<Integer, String>(namesByOrdinal);
    _ordinalsByName = new HashMap<String, Integer>(namesByOrdinal.size());
    for(Map.Entry<Integer, String> entry : namesByOrdinal.entrySet()) {
      _ordinalsByName.put(entry.getValue(), entry.getKey());
    }
  }
  
  public MapFudgeTaxonomy(int[] ordinals, String[] names) {
    if(ordinals == null) {
      throw new NullPointerException("Must provide ordinals.");
    }
    if(names == null) {
      throw new NullPointerException("Must provide names.");
    }
    if(ordinals.length != names.length) {
      throw new IllegalArgumentException("Arrays of ordinals and names must be of same length.");
    }
    _namesByOrdinal = new HashMap<Integer, String>(ordinals.length);
    _ordinalsByName = new HashMap<String, Integer>(ordinals.length);
    for(int i = 0; i < ordinals.length; i++) {
      _namesByOrdinal.put(ordinals[i], names[i]);
      _ordinalsByName.put(names[i], ordinals[i]);
    }
  }

  @Override
  public String getFieldName(short ordinal) {
    return _namesByOrdinal.get((int)ordinal);
  }

  @Override
  public Short getFieldOrdinal(String fieldName) {
    Integer ordinal = _ordinalsByName.get(fieldName);
    if(ordinal == null) {
      return null;
    }
    return ordinal.shortValue();
  }
  
  /**
   * Encodes the taxonomy as a Fudge message as per the specification. An encoded taxonomy can be decoded back to a taxonomy object by the
   * MapFudgeTaxonomy.fromFudgeMsg method on this class or equivalent function in any other language implementation. 
   */
  public FudgeMsg toFudgeMsg (final FudgeContext context) {
    final FudgeMsg msg = context.newMessage ();
    for (Map.Entry<Integer,String> entry : _namesByOrdinal.entrySet ()) {
      msg.add (entry.getKey (), entry.getValue ());
    }
    return msg;
  }
  
  /**
   * Decodes a taxonomy from a Fudge message as per the specification that is backed by a MapFudgeTaxonomy object.
   */
  public static FudgeTaxonomy fromFudgeMsg (final FudgeFieldContainer msg) {
    final List<FudgeField> fields = msg.getAllFields ();
    final Map<Integer,String> namesByOrdinal = new HashMap<Integer,String> (fields.size ());
    int i = 0;
    for (FudgeField field : fields) {
      final Short ordinal = field.getOrdinal ();
      if (ordinal == null) throw new IllegalArgumentException ("Fudge message does not contain a FudgeTaxonomy - field at index " + i + " has no ordinal");
      final Object value = field.getValue ();
      if (!(value instanceof String)) throw new IllegalArgumentException ("Fudge message does not contain a FudgeTaxonomy - field at index " + i + " (ordinal " + ordinal + ") does not contain a string");
      if (namesByOrdinal.put (ordinal.intValue (), (String)value) != null) throw new IllegalArgumentException ("Fudge message does not contain a FudgeTaxonomy - field at index " + i + " redefines ordinal " + ordinal);
      i++;
    }
    return new MapFudgeTaxonomy (namesByOrdinal);
  }

}

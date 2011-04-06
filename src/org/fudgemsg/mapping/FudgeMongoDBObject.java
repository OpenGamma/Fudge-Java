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

package org.fudgemsg.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.MutableFudgeMsg;

import com.mongodb.DBObject;

/**
 * Wraps a {@link FudgeMsg} and implements the {@link DBObject} interface,
 * without going through an object conversion stage (as the {@link MongoDBFudgeBuilder} will do).
 * This class is very much a work in progress. For details on why, please see
 * http://kirkwylie.blogspot.com/2010/06/performance-of-fudge-persistence-in.html and the comments
 * from the 10gen team at the bottom.
 */
public class FudgeMongoDBObject implements DBObject {

  /**
   * The underlying message.
   */
  private final FudgeMsg _underlying;
  /**
   * The cache.
   */
  private final Map<String, Object> _fastSingleValueCache = new HashMap<String, Object>();
  // This is used A LOT internally in MongoDB. Cache it specifically and avoid all the conversions.
  /**
   * The object ID.
   */
  private ObjectId _objectId;

  /**
   * Creates an instance decorating a Fudge message.
   * 
   * @param underlying  the underlying Fudge message to be wrapped, not null
   */
  public FudgeMongoDBObject(FudgeMsg underlying) {
    if (underlying == null) {
      throw new IllegalArgumentException("FudgeFieldContainer must not be null");
    }
    _underlying = underlying;
    buildFastSingleValueCache();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a cache.
   */
  private void buildFastSingleValueCache() {
    Set<String> fieldNamesToIgnore = new HashSet<String>();
    for (FudgeField field : getUnderlying().getAllFields()) {
      if (field.getName() == null) {
        continue;
      }
      if (fieldNamesToIgnore.contains(field.getName())) {
        continue;
      }
      if (_fastSingleValueCache.containsKey(field.getName())) {
        _fastSingleValueCache.remove(field.getName());
        fieldNamesToIgnore.add(field.getName());
        continue;
      }
      _fastSingleValueCache.put(field.getName(), convertFudgeToMongoDB(field));
      if ("_id".equals(field.getName())) {
        _objectId = new ObjectId((String)field.getValue());
      }
    }
  }

  /**
   * Gets the underlying message.
   * 
   * @return the underlying message
   */
  public FudgeMsg getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean containsField(String s) {
    if (_fastSingleValueCache.containsKey(s)) {
      return true;
    }
    return getUnderlying().hasField(s);
  }

  @Override
  public boolean containsKey(String s) {
    return containsField(s);
  }

  @Override
  public Object get(String key) {
    if ("_id".equals(key)) {
      return _objectId;
    }
    Object fastField = _fastSingleValueCache.get(key);
    if (fastField != null) {
      return fastField;
    }
    
    List<FudgeField> allFields = getUnderlying().getAllByName(key);
    if ((allFields == null) || allFields.isEmpty()) {
      return null;
    }
    if (allFields.size() > 0) {
      List<Object> listResult = new ArrayList<Object>(allFields.size());
      for (FudgeField field : allFields) {
        listResult.add(convertFudgeToMongoDB(field));
      }
      return listResult;
    } else {
      return convertFudgeToMongoDB(allFields.get(0));
    }
  }
  
  private static Object convertFudgeToMongoDB(FudgeField field) {
    if (field.getType().getTypeId() == FudgeTypeDictionary.SUB_MESSAGE_TYPE_ID) {
      // Sub-message.
      return new FudgeMongoDBObject((MutableFudgeMsg) field.getValue());
    } else {
      return field.getValue();
    }
  }

  @Override
  public boolean isPartialObject() {
    return false;
  }

  @Override
  public Set<String> keySet() {
    return getUnderlying().getAllFieldNames();
  }

  @Override
  public void markAsPartialObject() {
    // NOTE kirk 2010-06-14 -- Intentional no-op.
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Object put(String key, Object v) {
    // cast allows writing an immutable message to Mongo, but only reading a mutable one
    MutableFudgeMsg underlying = (MutableFudgeMsg) getUnderlying();
    if (v instanceof List) {
      for (Object o : (List) v) {
        put(key, o);
      }
    } else if (v instanceof DBObject) {
      put(key, FudgeContext.GLOBAL_DEFAULT.toFudgeMsg((DBObject) v));
    } else {
      if (v instanceof ObjectId) {
        // GROSS HACK HERE. Should be smarter in our fudge use.
        underlying.add(key, ((ObjectId) v).toString());
        _objectId = (ObjectId) v;
      } else {
        underlying.add(key, v);
      }
    }
    return null;
  }

  @Override
  public void putAll(BSONObject o) {
    throw new UnsupportedOperationException("Put All not yet supported");
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void putAll(Map m) {
    throw new UnsupportedOperationException("Put not yet supported");
  }

  @Override
  public Object removeField(String key) {
    throw new UnsupportedOperationException("Remove not yet supported");
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Map toMap() {
    Map result = new HashMap();
    for (FudgeField field : getUnderlying().getAllFields()) {
      if (field.getName() == null) {
        continue;
      }
      result.put(field.getName(), convertFudgeToMongoDB(field));
    }
    return result;
  }

}

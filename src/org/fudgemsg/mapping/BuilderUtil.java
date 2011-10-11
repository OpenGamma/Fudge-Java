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

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.types.FudgeTypeConverter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Utility methods for collection builders.
 * <p/>
 * This builder is immutable and thread safe.
 */
/* package */final class BuilderUtil {

  private BuilderUtil() {
  }

  /**
   * Oridinal dedicated for value type hinting in List, Maps and Sets
   */
  public static int VALUE_TYPE_HINT_ORDINAL = 999;

  /**
   * Oridinal dedicated for key type hinting in Maps
   */
  public static int KEY_TYPE_HINT_ORDINAL = 998;

  static Class getCommonNonAbstractAncestorOfObjects(Iterable<?> collection) {
    Class theCommonNonAbstractAncestor = null;
    for (Object entry : collection) {
      if (theCommonNonAbstractAncestor == null) {
        theCommonNonAbstractAncestor = entry.getClass();
      } else {
        if (theCommonNonAbstractAncestor.isAssignableFrom(entry.getClass())) {
          // it is superclass of entry.getClass() so we do not change it.
        } else if (entry.getClass().isAssignableFrom(theCommonNonAbstractAncestor)) {
          theCommonNonAbstractAncestor = entry.getClass();
        } else {
          // we have at least two classes laying on different hierarchy paths
          theCommonNonAbstractAncestor = null;
          break;
        }
      }
    }
    return theCommonNonAbstractAncestor;
  }


  static FudgeObjectBuilder findObjectBuilder(FudgeDeserializer deserializer, List<FudgeField> fields) {
    FudgeObjectBuilder<?> objectBuilder = null;
    for (FudgeField type : fields) {
      final Object obj = type.getValue();
      if (obj instanceof Number) {
        throw new UnsupportedOperationException("Serialisation framework does not support back/forward references");
      } else if (obj instanceof String) {
        try {
          Class<?> cls = deserializer.getFudgeContext().getTypeDictionary().loadClass((String) obj);
          objectBuilder = deserializer.getFudgeContext().getObjectDictionary().getObjectBuilder(cls);
          if (objectBuilder != null) {
            return objectBuilder;
          }
        } catch (ClassNotFoundException ex) {
          // ignore
        }
      }
    }
    return null;
  }

  static FudgeTypeConverter findTypeConverter(FudgeDeserializer deserializer, List<FudgeField> fields) {
    FudgeTypeConverter typeConverter = null;
    for (FudgeField type : fields) {
      final Object obj = type.getValue();
      if (obj instanceof Number) {
        throw new UnsupportedOperationException("Serialisation framework does not support back/forward references");
      } else if (obj instanceof String) {
        try {
          Class<?> cls = deserializer.getFudgeContext().getTypeDictionary().loadClass((String) obj);
          FudgeFieldType fieldType = deserializer.getFudgeContext().getTypeDictionary().getByJavaType(cls);
          if (fieldType != null && (fieldType instanceof FudgeTypeConverter)) {
            return (FudgeTypeConverter) fieldType;
          }
        } catch (ClassNotFoundException ex) {
          // ignore
        }
      }
    }
    return null;
  }

}

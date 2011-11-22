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

import org.apache.commons.lang.ClassUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.types.FudgeTypeConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility methods for objects builders.
 * <p/>
 * This builder is immutable and thread safe.
 */
/* package */final public class BuilderUtil {

  private BuilderUtil() {
  }

  /**
   * Oridinal dedicated for value type hinting in List, Maps and Sets
   */
  public final static int VALUE_TYPE_HINT_ORDINAL = -2;

  /**
   * Oridinal dedicated for key type hinting in Maps
   */
  public final static int KEY_TYPE_HINT_ORDINAL = -1;

  /**
   * Oridinal for storing map's keys or set entries
   */
  public final static int KEY_ORDINAL = 1;

  /**
   * Oridinal for storing map's values
   */
  public final static int VALUE_ORDINAL = 2;


  static List<Class> getTopTypes(Collection<?> objects) {
    // number of non null objects
    int size = 0;
    for (Object o : objects) {
      if (o != null) {
        size += 1;
      }
    }
    Set<Class> topTypes = new HashSet<Class>();
    Map<Class, AtomicInteger> allTypes = new HashMap<Class, AtomicInteger>();
    for (Object o : objects) {
      if (o != null) {
        allTypes.put(o.getClass(), new AtomicInteger());
        for (Class clazz : (List<Class>) ClassUtils.getAllSuperclasses(o.getClass())) {
          allTypes.put(clazz, new AtomicInteger());
        }
        for (Class clazz : (List<Class>) ClassUtils.getAllInterfaces(o.getClass())) {
          allTypes.put(clazz, new AtomicInteger());
        }
      }
    }

    for (Class allType : allTypes.keySet()) {
      for (Object o : objects) {
        if (o != null && allType.isAssignableFrom(o.getClass())) {
          if (allTypes.get(allType).incrementAndGet() == size) {
            topTypes.add(allType);
          }
        }
      }
    }
    Map<Class, Set<Class>> typeHierarchy = resolveTypeHierarchy(topTypes);
    typeHierarchy.remove(Object.class);
    typeHierarchy.remove(Serializable.class);
    typeHierarchy.remove(Comparable.class);
    typeHierarchy.remove(String.class);
    return reverse(topologicalSort(typeHierarchy));
  }

  private static <T> List<T> reverse(List<T> list) {
    LinkedList<T> l = new LinkedList<T>();
    for (T t : list) {
      l.addFirst(t);
    }
    return l;
  }

  private static <T> List<T> topologicalSort(final Map<T, Set<T>> entries) {
    List<T> sortedResult = new ArrayList<T>();
    Set<T> marked = new HashSet<T>();
    for (T s : entries.keySet()) {
      if(!marked.contains(s))
        topologicalSort(s, entries, marked, sortedResult);
    }
    return sortedResult;
  }

  private static <T> void topologicalSort(final T s, final Map<T, Set<T>> entries, final Set<T> marked, final List<T> sortedResult) {
    if (!marked.contains(s)) {
      for (T t : entries.get(s)) {
        if (entries.containsKey(t) && !marked.contains(t)) {
          marked.add(t);
          topologicalSort(t, entries, marked, sortedResult);
        }
      }
    }
    marked.add(s);
    sortedResult.add(s);
  }

  private static Map<Class, Set<Class>> resolveTypeHierarchy(Collection<Class> classes) {
    Map<Class, Set<Class>> hierarchy = new HashMap<Class, Set<Class>>();
    Set<Class> addedTypes = resolveTypeHierarchy(classes, hierarchy);
    while (addedTypes.size() > 0) {
      addedTypes = resolveTypeHierarchy(addedTypes, hierarchy);
    }
    return hierarchy;
  }

  private static Set<Class> resolveTypeHierarchy(Collection<Class> classes, Map<Class, Set<Class>> hierarchy) {
    Set<Class> addedTypes = new HashSet<Class>();
    for (Class cls : classes) {
      Set<Class> types = hierarchy.get(cls);
      if (types == null) {
        types = new HashSet<Class>();
        hierarchy.put(cls, types);
      }
      Collections.addAll(types, cls.getInterfaces());
      Collections.addAll(addedTypes, cls.getInterfaces());
      if (cls.getSuperclass() != null) {
        types.add(cls.getSuperclass());
        addedTypes.add(cls.getSuperclass());
      }
    }
    return addedTypes;
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

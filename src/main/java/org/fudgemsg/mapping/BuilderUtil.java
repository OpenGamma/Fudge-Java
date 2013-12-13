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

import static org.fudgemsg.util.TopologicalSort.reverse;
import static org.fudgemsg.util.TopologicalSort.topologicalSort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.types.FudgeTypeConverter;

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


  static List<Class<?>> getTopTypes(Collection<?> objects) {
    // number of non null objects
    int size = 0;
    for (Object o : objects) {
      if (o != null) {
        size += 1;
      }
    }
    Set<Class<?>> topTypes = new HashSet<>();
    Map<Class<?>, AtomicInteger> allTypes = new HashMap<>();
    for (Object o : objects) {
      if (o != null) {
        allTypes.put(o.getClass(), new AtomicInteger());
        for (Class<?> clazz : getAllSuperclasses(o.getClass())) {
          allTypes.put(clazz, new AtomicInteger());
        }
        for (Class<?> clazz : getAllInterfaces(o.getClass())) {
          allTypes.put(clazz, new AtomicInteger());
        }
      }
    }

    for (Class<?> allType : allTypes.keySet()) {
      for (Object o : objects) {
        if (o != null && allType.isAssignableFrom(o.getClass())) {
          if (allTypes.get(allType).incrementAndGet() == size) {
            topTypes.add(allType);
          }
        }
      }
    }
    Map<Class<?>, Set<Class<?>>> typeHierarchy = resolveTypeHierarchy(topTypes);
    //typeHierarchy.remove(Object.class);
    //typeHierarchy.remove(Serializable.class);
    //typeHierarchy.remove(Comparable.class);
    //typeHierarchy.remove(String.class);
    return reverse(topologicalSort(typeHierarchy));
  }

  static List<Class<?>> getAllSuperclasses(Class<?> cls) {
    List<Class<?>> result = new ArrayList<Class<?>>();
    Class<?> sc = cls.getSuperclass();
    while (sc != null) {
      result.add(sc);
      sc = sc.getSuperclass();
    }
    return result;
  }

  static Set<Class<?>> getAllInterfaces(Class<?> cls) {
    Set<Class<?>> result = new LinkedHashSet<Class<?>>();
    for (Class<?> iface : cls.getInterfaces()) {
      result.add(iface);
      result.addAll(getAllInterfaces(iface));
    }
    if (cls.getSuperclass() != null) {
      result.addAll(getAllInterfaces(cls.getSuperclass()));
    }
    return result;
  }

  private static Map<Class<?>, Set<Class<?>>> resolveTypeHierarchy(Collection<Class<?>> classes) {
    Map<Class<?>, Set<Class<?>>> hierarchy = new HashMap<Class<?>, Set<Class<?>>>();
    Set<Class<?>> addedTypes = resolveTypeHierarchy(classes, hierarchy);
    while (addedTypes.size() > 0) {
      addedTypes = resolveTypeHierarchy(addedTypes, hierarchy);
    }
    return hierarchy;
  }

  private static Set<Class<?>> resolveTypeHierarchy(Collection<Class<?>> classes, Map<Class<?>, Set<Class<?>>> hierarchy) {
    Set<Class<?>> addedTypes = new HashSet<Class<?>>();
    for (Class<?> cls : classes) {
      Set<Class<?>> types = hierarchy.get(cls);
      if (types == null) {
        types = new HashSet<Class<?>>();
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

  static List<Class<?>> typeHintsFromFields(FudgeDeserializer deserializer, List<FudgeField> fields) {
    List<Class<?>> typeHints = new ArrayList<>();
    for (FudgeField type : fields) {
      final Object obj = type.getValue();
      if (obj instanceof Number) {
        throw new UnsupportedOperationException("Serialisation framework does not support back/forward references");
      } else if (obj instanceof String) {
        try {
          typeHints.add(deserializer.getFudgeContext().getTypeDictionary().loadClass((String) obj));
        } catch (ClassNotFoundException ex) {
          // ignore
        }
      }
    }
    return typeHints;
  }

  static FudgeObjectBuilder<?> findObjectBuilder(FudgeDeserializer deserializer, List<FudgeField> fields) {
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

  static FudgeTypeConverter<?, ?> findTypeConverter(FudgeDeserializer deserializer, List<FudgeField> fields) {
    for (FudgeField type : fields) {
      final Object obj = type.getValue();
      if (obj instanceof Number) {
        throw new UnsupportedOperationException("Serialisation framework does not support back/forward references");
      } else if (obj instanceof String) {
        try {
          Class<?> cls = deserializer.getFudgeContext().getTypeDictionary().loadClass((String) obj);
          FudgeFieldType fieldType = deserializer.getFudgeContext().getTypeDictionary().getByJavaType(cls);
          if (fieldType != null && (fieldType instanceof FudgeTypeConverter)) {
            return (FudgeTypeConverter<?, ?>) fieldType;
          }
        } catch (ClassNotFoundException ex) {
          // ignore
        }
      }
    }
    return null;
  }

  /**
   * Builds object from fudge field trying in turns all provided type hints, beginning from most specific one.
   * @param deserializer
   * @param typeHints
   * @param field
   * @return object build from fudge field
   */
  public static Object fieldValueToObject(FudgeDeserializer deserializer,  List<Class<?>> typeHints, FudgeField field) {
    for (Class<?> typeHint : typeHints) {
      try {
        return deserializer.fieldValueToObject(typeHint, field);
      } catch (Exception e) {
        // ignore
      }
    }
    return deserializer.fieldValueToObject(field);
  }
}

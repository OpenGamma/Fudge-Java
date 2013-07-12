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
package org.fudgemsg;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.FudgeTypeConverter;
import org.fudgemsg.types.IndicatorFieldTypeConverter;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.types.PrimitiveFieldTypesConverter;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.SecondaryFieldTypeBase;
import org.fudgemsg.types.StringFieldTypeConverter;
import org.fudgemsg.types.secondary.SecondaryTypeLoader;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * The dictionary of all known Fudge types.
 * <p>
 * In order to efficiently send messages, Fudge needs to know the type of each piece of data.
 * This dictionary keeps track of all the known types.
 * <p>
 * This class is mutable and thread-safe via concurrent collections.
 */
public class FudgeTypeDictionary {

  /**
   * The types indexed in an array.
   */
  private AtomicReferenceArray<FudgeWireType> _typesById = new AtomicReferenceArray<FudgeWireType>(256);
  /**
   * The types indexed by Java type.
   */
  private final ConcurrentMap<Class<?>, FudgeFieldType> _typesByJavaType;
  /**
   * The types converters indexed by Java type.
   */
  private final ConcurrentMap<Class<?>, FudgeTypeConverter<?,?>> _convertersByJavaType;
  /**
   * The map of renamed classes.
   */
  private final ConcurrentMap<String, Class<?>> _renames = new ConcurrentHashMap<String, Class<?>>();
  /**
   * A flag to indicate if the classpath is scanned.
   */
  private final AtomicBoolean _haveScannedClasspath = new AtomicBoolean(false);

  /**
   * Creates a new dictionary configured with the default types from the Fudge specification.
   * Some standard secondary types will also be loaded.
   */
  public FudgeTypeDictionary() {
    _typesByJavaType = new ConcurrentHashMap<Class<?>, FudgeFieldType>();
    _convertersByJavaType = new ConcurrentHashMap<Class<?>, FudgeTypeConverter<?, ?>>();
    // primary types
    addType(FudgeWireType.INDICATOR);
    addType(FudgeWireType.BOOLEAN, Boolean.class, Boolean.TYPE);
    addType(FudgeWireType.BYTE, Byte.class, Byte.TYPE);
    addType(FudgeWireType.SHORT, Short.class, Short.TYPE);
    addType(FudgeWireType.INT, Integer.class, Integer.TYPE);
    addType(FudgeWireType.LONG, Long.class, Long.TYPE);
    addType(FudgeWireType.SHORT_ARRAY);
    addType(FudgeWireType.INT_ARRAY);
    addType(FudgeWireType.LONG_ARRAY);
    addType(FudgeWireType.FLOAT, Float.class, Float.TYPE);
    addType(FudgeWireType.DOUBLE, Double.class, Double.TYPE);
    addType(FudgeWireType.FLOAT_ARRAY);
    addType(FudgeWireType.DOUBLE_ARRAY);
    addType(FudgeWireType.STRING);
    addType(FudgeWireType.SUB_MESSAGE);
    addType(FudgeWireType.BYTE_ARRAY_4);
    addType(FudgeWireType.BYTE_ARRAY_8);
    addType(FudgeWireType.BYTE_ARRAY_16);
    addType(FudgeWireType.BYTE_ARRAY_20);
    addType(FudgeWireType.BYTE_ARRAY_32);
    addType(FudgeWireType.BYTE_ARRAY_64);
    addType(FudgeWireType.BYTE_ARRAY_128);
    addType(FudgeWireType.BYTE_ARRAY_256);
    addType(FudgeWireType.BYTE_ARRAY_512);
    addType(FudgeWireType.BYTE_ARRAY);  // must go after other byte[] elements
    addType(FudgeWireType.DATE);
    addType(FudgeWireType.TIME);
    addType(FudgeWireType.DATETIME);
    // default type conversions
    addTypeConverter(PrimitiveFieldTypesConverter.INT_CONVERTER, Integer.class, Integer.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.BOOLEAN_CONVERTER, Boolean.class, Boolean.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.BYTE_CONVERTER, Byte.class, Byte.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.SHORT_CONVERTER, Short.class, Short.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.LONG_CONVERTER, Long.class, Long.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.FLOAT_CONVERTER, Float.class, Float.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.DOUBLE_CONVERTER, Double.class, Double.TYPE);
    addTypeConverter(IndicatorFieldTypeConverter.INSTANCE, IndicatorType.class);
    addTypeConverter(StringFieldTypeConverter.INSTANCE, String.class);
    // secondary types
    SecondaryTypeLoader.addTypes(this);
    // unknown types
    for (int i = 0; i < _typesById.length(); i++) {
      if (_typesById.get(i) == null) {
        _typesById.set(i, FudgeWireType.unknown(i));
      }
    }
  }

  /**
   * Creates a new dictionary as a clone of another.
   * 
   * @param other  the dictionary to copy data from
   */
  protected FudgeTypeDictionary(final FudgeTypeDictionary other) {
    for (int i = 0; i < _typesById.length(); i++) {
      _typesById.set(i, other._typesById.get(i));
    }
    _typesByJavaType = new ConcurrentHashMap<Class<?>, FudgeFieldType>(other._typesByJavaType);
    _convertersByJavaType = new ConcurrentHashMap<Class<?>, FudgeTypeConverter<?, ?>>(other._convertersByJavaType);
  }

  //-------------------------------------------------------------------------
  /**
   * Register a new type with the dictionary.
   * <p>
   * Custom types that are not part of the Fudge specification should use IDs allocated downwards
   * from 255 for compatibility with future versions that might include additional standard types.
   * 
   * @param type  the {@code FudgeFieldType} definition of the type, not null
   * @param alternativeTypes  any additional Java classes that are synonymous with this type
   */
  public void addType(FudgeFieldType type, Class<?>... alternativeTypes) {
    if (type == null) {
      throw new NullPointerException("FudgeFieldType must not be null");
    }
    if (type instanceof FudgeWireType) {
      FudgeWireType oldType;
      do {
        oldType = _typesById.get(type.getTypeId());
        if (oldType != null && oldType.isTypeKnown() && oldType.equals(type) == false) {
          throw new IllegalArgumentException("FudgeWireType already registered with id " + type.getTypeId());
        }
      } while (_typesById.compareAndSet(type.getTypeId(), oldType, (FudgeWireType) type) == false);
      
    } else if (type instanceof SecondaryFieldTypeBase<?, ?, ?>) {
      addTypeConverter((SecondaryFieldTypeBase<?, ?, ?>) type, type.getJavaType());
      
    } else {
      throw new ClassCastException("FudgeFieldType must extend FudgeWireType or SecondaryFieldTypeBase: " + type.getClass());
    }
    _typesByJavaType.put(type.getJavaType(), type);
    for (Class<?> alternativeType : alternativeTypes) {
      _typesByJavaType.put(alternativeType, type);
    }
    _fftByJavaTypeCache.clear(); //results could have changed now
  }

  /**
   * Obtain a type by type ID.
   * <p>
   * If the type id is not known, a special "unknown" type object will be returned.
   * This can be checked using {@link FudgeWireType#isTypeUnknown()}.
   * 
   * @param typeId  the numeric type identifier, from 0 to 255
   * @return the type with the specified type identifier, not null
   * @throws IndexOutOfBoundsException if the id is not in range
   */
  public FudgeWireType getByTypeId(int typeId) {
    return _typesById.get(typeId);
  }

  //-------------------------------------------------------------------------
  /**
   * Registers a new type conversion with the dictionary.
   * <p>
   * A converter will be used by {@link #getFieldValue} to expand a non-matching type.
   * The secondary type mechanism will register the appropriate conversion
   * automatically when {@link #addType} is called.
   * 
   * @param converter  the converter to register
   * @param types  the types to register against
   */
  public void addTypeConverter(FudgeTypeConverter<?, ?> converter, Class<?>... types) {
    if (converter == null) {
      throw new NullPointerException("FudgeTypeConverter must not be null");
    }
    for (Class<?> type : types) {
      _convertersByJavaType.put(type, converter);
      type = type.getSuperclass();
      while (type != null && !Object.class.equals(type)) {
        if (_convertersByJavaType.putIfAbsent(type, converter) != null) {
          break;
        }
        type = type.getSuperclass();
      }
    }
  }

  private final static FudgeFieldType s_noFftMarker = new FudgeFieldType(0, FudgeTypeDictionary.class, false, 0);
  private final ConcurrentHashMap<Class<?>, FudgeFieldType> _fftByJavaTypeCache = new ConcurrentHashMap<Class<?>, FudgeFieldType>();

  /*
   * 
   * Resolves a Java class to a {@link FudgeFieldType} registered with this dictionary.
   * 
   * @param javaType the class to resolve
   * @return the matching Fudge type, null if none is found
   */
  public FudgeFieldType getByJavaType(final Class<?> javaType) {
    if (javaType == null) {
      return null;
    }
    FudgeFieldType fft = _fftByJavaTypeCache.get(javaType);
    if (fft != null)
    {
      return fft == s_noFftMarker ? null : fft;
    }
    
    FudgeFieldType fieldType = _typesByJavaType.get(javaType);
    if (fieldType != null) {
      _fftByJavaTypeCache.putIfAbsent(javaType, fieldType);
      return fieldType;
    }
    for (Class<?> cls : javaType.getInterfaces()) {
      fieldType = getByJavaType(cls);
      if (fieldType != null) {
        _fftByJavaTypeCache.putIfAbsent(javaType, fieldType);
        return fieldType;
      }
    }
    fft = getByJavaType(javaType.getSuperclass());
    _fftByJavaTypeCache.putIfAbsent(javaType, fft == null ? s_noFftMarker : fft);
    return fft;
  }

  /**
   * Resolves a Java class to a {@link FudgeTypeConverter}.
   * <p>
   * A converter may be derived from registration of a {@link SecondaryFieldType},
   * a default conversion between the Java classes that represent the Fudge primitive
   * types, or explicitly registered with {@link #addTypeConverter}.
   * 
   * @param <T> Java type of the class to look up
   * @param javaType  the class to look up
   * @return the registered converter, null if none is available
   */
  @SuppressWarnings("unchecked")
  protected <T> FudgeTypeConverter<Object, T> getTypeConverter(final Class<T> javaType) {
    return (FudgeTypeConverter<Object, T>) _convertersByJavaType.get(javaType);
  }

  //-------------------------------------------------------------------------
  /**
   * Type conversion for secondary types.
   * 
   * @param <T> type to convert to
   * @param clazz  the target class for the converted value, not null
   * @param field  the field containing the value to convert, null returns null
   * @return the converted value, null if no value
   * @throws IllegalArgumentException if the parameters are not valid for conversion
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> T getFieldValue(final Class<T> clazz, final FudgeField field) throws IllegalArgumentException {
    if (field == null) {
      return null;
    }
    final Object value = field.getValue();
    if (value == null) {
      return null;
    }
    if (clazz.isAssignableFrom(value.getClass())) {
      return (T) value;
    }
    final FudgeFieldType type = field.getType();
    if (type instanceof SecondaryFieldType) {
      final SecondaryFieldType sourceType = (SecondaryFieldType) type;
      if (clazz.isAssignableFrom(sourceType.getPrimaryType().getJavaType())) {
        // been asked for the primary type
        return (T) sourceType.secondaryToPrimary(value);
      } else {
        final FudgeTypeConverter<Object, T> converter = getTypeConverter(clazz);
        if (converter == null) {
          // don't recognize the requested type
          throw new IllegalArgumentException("cannot convert " + sourceType + " to unregistered secondary type " + clazz.getName());
        } else {
          if (converter.canConvertPrimary(sourceType.getPrimaryType().getJavaType())) {
            // primary and requested have a common base
            return converter.primaryToSecondary(sourceType.secondaryToPrimary(value));
          } else {
            // no common ground
            throw new IllegalArgumentException("no Fudge primary type allows conversion from " + sourceType + " to " + clazz.getName());
          }
        }
      }
    } else if (type == FudgeWireType.INDICATOR) {
      // indicators always get converted to NULL when cast to another type
      return null;
    } else {
      final FudgeTypeConverter<Object, T> converter = getTypeConverter(clazz);
      if (converter == null) {
        // don't recognize the requested type
        if (clazz.isEnum()) {
          // get the field as a string and then try to inflate the enum
          return (T) Enum.valueOf((Class<? extends Enum>) clazz, getFieldValue(String.class, field));
        } else {
          throw new IllegalArgumentException("cannot convert " + type + " to unregistered secondary type " + clazz.getName());
        }
      } else {
        if (converter.canConvertPrimary(value.getClass())) {
          // secondary type extends our current type
          return converter.primaryToSecondary(value);
        } else {
          // secondary type doesn't extend our current type
          throw new IllegalArgumentException("secondary type " + clazz.getName() + " does not allow conversion from " + value.getClass().getName());
        }
      }
    }
  }

  /**
   * Type conversion test for secondary types.
   * <p>
   * Returns {@code true} if {@link #getFieldValue} would return an object instance.
   * 
   * @param <T> type to convert to
   * @param clazz  the target class for the converted value, not null
   * @param field  the field containing the value to convert, null returns false
   * @return {@code true} if a conversion is possible, {@code false} otherwise
   *  (when {@link #getFieldValue} might return {@code null} or throw an exception)
   */
  @SuppressWarnings("rawtypes")
  public <T> boolean canConvertField (final Class<T> clazz, final FudgeField field) {
    if (field == null) {
      return false;
    }
    final Object value = field.getValue();
    if (value == null) {
      return false;
    }
    if (clazz.isAssignableFrom(value.getClass())) {
      return true;
    }
    final FudgeFieldType type = field.getType();
    if (type instanceof SecondaryFieldType) {
      final SecondaryFieldType sourceType = (SecondaryFieldType) type;
      if (clazz.isAssignableFrom(sourceType.getPrimaryType().getJavaType())) {
        // been asked for the primary type
        return true;
      } else {
        final FudgeTypeConverter<Object, T> converter = getTypeConverter(clazz);
        if (converter == null) {
          // don't recognize the requested type
          return false;
        } else {
          // check common base
          return converter.canConvertPrimary(sourceType.getPrimaryType().getJavaType());
        }
      }
    } else if (type == FudgeWireType.INDICATOR) {
      // indicators can't be converted to instances
      return false;
    } else {
      final FudgeTypeConverter<Object, T> converter = getTypeConverter(clazz);
      if (converter == null) {
        // don't recognize the requested type
        return false;
      } else {
        // does secondary type extend current type
        return converter.canConvertPrimary(value.getClass());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Scans all files available to common classpath loading system heuristics to determine
   * which ones have the {@link FudgeSecondaryType} annotation, and registers those as appropriate
   * secondary types.
   * This is potentially a <em>very</em> expensive operation, and as such is optional.
   * 
   * @param reflector  the reflector to use, not null
   */
  public void addAllAnnotatedSecondaryTypes(AnnotationReflector reflector) {
    if (_haveScannedClasspath.getAndSet(true)) {
      return;
    }
    final Set<Field> fields = reflector.getReflector().getFieldsAnnotatedWith(FudgeSecondaryType.class);
    for (Field field : fields) {
      addAnnotatedSecondaryType(field);
    }
  }

  /**
   * Add a class which is known to have a {@link FudgeBuilderFor} annotation as an
   * object or message builder (or both). 
   * 
   * @param clazz  the secondary type class
   */
  public void addAnnotatedSecondaryType(Field field) {
    if (!field.isAnnotationPresent(FudgeSecondaryType.class)) {
      throw new FudgeRuntimeException("Invalid field, no FudgeSecondaryType annotation");
    }
    int fieldModifiers = field.getModifiers();
    if (Modifier.isStatic(fieldModifiers) && Modifier.isPublic(fieldModifiers)) {
      FudgeFieldType fudgeType;
      try {
        fudgeType = (FudgeFieldType) field.get(null);
      } catch (Exception ex) {
        throw new FudgeRuntimeException("Cannot access field " + field.getName() + " on class " +
            field.getDeclaringClass().getName() + " with @FudgeSecondaryType annotation", ex);
      }
      addType(fudgeType);
    } else {
      throw new FudgeRuntimeException("Invalid field, not 'public static'");
    }
  }

  /**
   * Add a class which is known to have a {@link FudgeBuilderFor} annotation as an
   * object or message builder (or both). 
   * 
   * @param clazz  the secondary type class
   */
  public void addAnnotatedSecondaryTypeClass(Class<?> clazz) {
    for (Field field : clazz.getFields()) {
      if (field.isAnnotationPresent(FudgeSecondaryType.class)) {
        addAnnotatedSecondaryType(field);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a class rename to the dictionary.
   * This handles class names that change in refactoring.
   * 
   * @param oldClassName  the old fully qualified class name, not null
   * @param newClass  the new class, not null
   */
  public void registerClassRename(String oldClassName, Class<?> newClass) {
    Class<?> registered = _renames.putIfAbsent(oldClassName, newClass);
    if (registered != null && ! newClass.equals(registered)) {
      throw new IllegalArgumentException("Class name already registered: " + oldClassName + " already mapped to " + registered);
    }
  }

  final ClassLoader s_classLoader = FudgeTypeDictionary.class.getClassLoader(); //NOTE: for a given instance this is constant
  final Map<String, Class<?>> s_loadedClasses = new ConcurrentHashMap<String, Class<?>>(); //TODO: This should be expired at some point, but it's an insignificant leak at the moment
  
  /**
   * Loads a class from a class name, handling previously registered renames.
   * 
   * @param className  the fully qualified class name, not null
   * @return the loaded class, not null
   * @throws ClassNotFoundException if unable to load
   */
  public Class<?> loadClass(String className) throws ClassNotFoundException {
    Class<?> rename = _renames.get(className);
    if (rename != null) {
      return rename;
    }
    
    Class<?> loaded = s_loadedClasses.get(className);
    if (loaded != null)
    {
      return loaded;
    }
    loaded = s_classLoader.loadClass(className); //Must always return the same key, so can be cached happily
    s_loadedClasses.put(className, loaded);
    return s_classLoader.loadClass(className);
  }

}

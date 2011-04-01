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

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;

import com.mongodb.DBObject;

/**
 * Default factory for building Fudge message encoders and decoders.
 * <p>
 * Building a Fudge message:
 * <ul>
 *   <li>If the object has a suitable {@code HasFudgeBuilder} annotation, that will be used
 *   <li>If the object has a public {@code toFudgeMsg} method, that will be used
 *   <li>If the object is an array or enum, that will be used
 *   <li>Any registered dynamic builders are used
 *   <li>Otherwise the {@link JavaBeanBuilder} will be used
 * </ul>
 * <p>
 * Building an object:
 * <ul>
 *   <li>If the object has a suitable {@code HasFudgeBuilder} annotation, that will be used
 *   <li>If the object has a public {@code fromFudgeMsg} method, that will be used
 *   <li>If the object has a public constructor that takes a {@link FudgeMsg}, that will be used
 *   <li>If the object is an array or enum, that will be used
 *   <li>Any registered dynamic builders are used
 *   <li>Otherwise the {@link JavaBeanBuilder} will be used
 * </ul>
 * <p>
 * Dynamic builders are pre-registered for {@link Map}, {@link List}, {@link Set},
 * {@link FudgeMsg} and {@link DBObject}.
 * <p>
 * This class is mutable but thread-safe via concurrent collections.
 */ 
public class FudgeDefaultBuilderFactory implements FudgeBuilderFactory {

  /**
   * The "generic" builders, that handles abstract classes/interfaces.
   */
  private final ConcurrentMap<Class<?>, FudgeObjectBuilder<?>> _genericObjectBuilders;
  /**
   * The message builders.
   */
  private final List<MessageBuilderMapEntry> _genericMessageBuilders;

  // TODO 2010-01-29 Andrew -- we could have a builder builder, e.g. search for static methods that return
  // a FudgeObjectBuilder/FudgeMessageBuilder/FudgeBuilder instance for that class

  /**
   * Creates a new factory.
   * <p>
   * This loads the resource {@code org.fudgemsg.mapping.FudgeDefaultBuilderFactory.properties}
   * to initialize the generic builders.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public FudgeDefaultBuilderFactory() {
    _genericObjectBuilders = new ConcurrentHashMap<Class<?>, FudgeObjectBuilder<?>>();
    _genericMessageBuilders = new CopyOnWriteArrayList<MessageBuilderMapEntry>();
    final ResourceBundle genericBuilders = ResourceBundle.getBundle(getClass().getName());
    for (final String javaClassName : genericBuilders.keySet()) {
      final String builderName = genericBuilders.getString(javaClassName);
      try {
        addGenericBuilderInternal(Class.forName(javaClassName),
            (FudgeBuilder) Class.forName(builderName).getDeclaredField("INSTANCE").get(null));
      } catch (ClassNotFoundException ex) {
        // ignore; e.g. if DBObject isn't in the classpath
      } catch (Exception ex) {
        throw new FudgeRuntimeException("Unable to register builder for " + javaClassName + " (" + builderName + ")", ex);
      }
    }
  }

  /**
   * Creates a new factory as a clone of another.
   * 
   * @param other  the factory to clone, not null
   */
  /* package */FudgeDefaultBuilderFactory(final FudgeDefaultBuilderFactory other) {
    _genericObjectBuilders = new ConcurrentHashMap<Class<?>, FudgeObjectBuilder<?>>(other._genericObjectBuilders);
    _genericMessageBuilders = new CopyOnWriteArrayList<MessageBuilderMapEntry>(other._genericMessageBuilders);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the generic object builders.
   * 
   * @return the builders, not null
   */
  private Map<Class<?>, FudgeObjectBuilder<?>> getGenericObjectBuilders() {
    return _genericObjectBuilders;
  }

  /**
   * Gets the generic message builders.
   * 
   * @return the builders, not null
   */
  private List<MessageBuilderMapEntry> getGenericMessageBuilders() {
    return _genericMessageBuilders;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a builder for a type.
   * <p>
   * This picks the most appropriate builder for the given type.
   * This will check for the {@code HasFudgeBuilder} annotation, {@code fromFudgeMsg}
   * methods, suitable Fudge-based constructors, arrays, enums and "generic" builders.
   * Finally it will try using a reflection based default.
   * 
   * @param <T> Java type of the class a builder is requested for
   * @param clazz  the Java class a builder is requested for, not null
   * @return the builder, null if unable to create
   */
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> FudgeObjectBuilder<T> createObjectBuilder(final Class<T> clazz) {
    FudgeObjectBuilder<T> builder;
    if ((builder = createObjectBuilderFromAnnotation(clazz)) != null) {
      return builder;
    }
    if ((builder = FromFudgeMsgObjectBuilder.create(clazz)) != null) {
      return builder;
    }
    if ((builder = FudgeMsgConstructorObjectBuilder.create(clazz)) != null) {
      return builder;
    }
    if (clazz.isArray()) {
      return new ArrayBuilder(clazz.getComponentType());
    }
    if (Enum.class.isAssignableFrom(clazz)) {
      return new EnumBuilder(clazz);
    }
    if ((builder = (FudgeObjectBuilder<T>) getGenericObjectBuilders().get(clazz)) != null) {
      return builder;
    }
    if (clazz.isInterface()) {
      return null;
    }
    return JavaBeanBuilder.create(clazz);
  }

  /**
   * Attempts to construct a builder based on the {@code HasFudgeBuilder} annotation.
   * <p>
   * The {@link HasFudgeBuilder} annotation allows the Fudge system to be automatically
   * configured if desired.
   * 
   * @param <T> Java type of the class a builder is requested for
   * @param clazz  the Java class a builder is requested for, not null
   * @return the builder, null if unable to create
   */
  @SuppressWarnings("unchecked")
  protected <T> FudgeObjectBuilder<T> createObjectBuilderFromAnnotation(final Class<T> clazz) {
    if (!clazz.isAnnotationPresent(HasFudgeBuilder.class)) {
      return null;
    }
    HasFudgeBuilder annotation = clazz.getAnnotation(HasFudgeBuilder.class);
    Class<?> objectBuilderClass = null;
    if (!Object.class.equals(annotation.builder())) {
      objectBuilderClass = annotation.builder();
    } else if (!Object.class.equals(annotation.objectBuilder())) {
      objectBuilderClass = annotation.objectBuilder();
    }
    if (objectBuilderClass == null) {
      return null;
    }
    if (!FudgeObjectBuilder.class.isAssignableFrom(objectBuilderClass)) {
      return null;
    }
    
    FudgeObjectBuilder<T> result = null;
    try {
      result = (FudgeObjectBuilder<T>) objectBuilderClass.newInstance();
    } catch (Exception e) {
      throw new FudgeRuntimeException("Unable to instantiate annotated object builder class " + objectBuilderClass, e);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a builder for a type.
   * <p>
   * This picks the most appropriate builder for the given type.
   * This will check for the {@code HasFudgeBuilder} annotation, {@code toFudgeMsg}
   * methods, arrays, enums and "generic" builders
   * Finally it will try using a reflection based default.
   * 
   * @param <T> Java type of the class a builder is requested for
   * @param clazz  the Java class a builder is requested for, not null
   * @return the builder, null if unable to create
   */
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> FudgeMessageBuilder<T> createMessageBuilder(final Class<T> clazz) {
    FudgeMessageBuilder<T> builder;
    if ((builder = createMessageBuilderFromAnnotation(clazz)) != null) {
      return builder;
    }
    if ((builder = ToFudgeMsgMessageBuilder.create(clazz)) != null) {
      return builder;
    }
    if (clazz.isArray()) {
      return new ArrayBuilder(clazz.getComponentType());
    }
    if (Enum.class.isAssignableFrom(clazz)) {
      return new EnumBuilder(clazz);
    }
    for (MessageBuilderMapEntry defaultBuilder : getGenericMessageBuilders()) {
      if (defaultBuilder.getClazz().isAssignableFrom(clazz))
        return (FudgeMessageBuilder<T>) defaultBuilder.getMessageBuilder();
    }
    return JavaBeanBuilder.create(clazz);
  }

  /**
   * Attempts to construct a builder based on the {@code HasFudgeBuilder} annotation.
   * <p>
   * The {@link HasFudgeBuilder} annotation allows the Fudge system to be automatically
   * configured if desired.
   * 
   * @param <T> Java type of the class a builder is requested for
   * @param clazz  the Java class a builder is requested for, not null
   * @return the builder, null if unable to create
   */
  @SuppressWarnings("unchecked")
  protected <T> FudgeMessageBuilder<T> createMessageBuilderFromAnnotation(final Class<T> clazz) {
    if (!clazz.isAnnotationPresent(HasFudgeBuilder.class)) {
      return null;
    }
    HasFudgeBuilder annotation = clazz.getAnnotation(HasFudgeBuilder.class);
    Class<?> messageBuilderClass = null;
    if (!Object.class.equals(annotation.builder())) {
      messageBuilderClass = annotation.builder();
    } else if (!Object.class.equals(annotation.messageBuilder())) {
      messageBuilderClass = annotation.messageBuilder();
    }
    if (messageBuilderClass == null) {
      return null;
    }
    if (!FudgeMessageBuilder.class.isAssignableFrom(messageBuilderClass)) {
      return null;
    }
    
    FudgeMessageBuilder<T> result = null;
    try {
      result = (FudgeMessageBuilder<T>) messageBuilderClass.newInstance();
    } catch (Exception e) {
      throw new FudgeRuntimeException("Unable to instantiate annotated message builder class " + messageBuilderClass, e);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> void addGenericBuilder (final Class<T> clazz, final FudgeBuilder<T> builder) {
    addGenericBuilderInternal (clazz, builder);
  }

  private <T> void addGenericBuilderInternal(final Class<T> clazz, final FudgeBuilder<? extends T> builder) {
    getGenericObjectBuilders().put(clazz, builder);
    getGenericMessageBuilders().add(0, new MessageBuilderMapEntry(clazz, builder));
  }

  //-------------------------------------------------------------------------
  /**
   * An entry for storing builders.
   */
  private static class MessageBuilderMapEntry {
    /** The class. */
    private final Class<?> _clazz;
    /** The builder. */
    private final FudgeMessageBuilder<?> _builder;

    <T> MessageBuilderMapEntry(Class<T> clazz, FudgeMessageBuilder<? extends T> builder) {
      _clazz = clazz;
      _builder = builder;
    }

    Class<?> getClazz() {
      return _clazz;
    }

    FudgeMessageBuilder<?> getMessageBuilder() {
      return _builder;
    }
  }

}

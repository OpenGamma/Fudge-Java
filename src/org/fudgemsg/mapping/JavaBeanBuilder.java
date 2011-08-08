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

import java.beans.Beans;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.beanutils.PropertyUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;

/**
 * Builder that uses {@code BeanUtils} to reflect over a class.
 * <p>
 * This builder allows arbitrary beans to be passed to Fudge.
 * It is more efficient to write a dedicated builder.
 * <p>
 * This builder is immutable and thread safe.
 * 
 * @param <T> the bean class that can be serialized or deserialized using this builder
 */
/* package */ final class JavaBeanBuilder<T> implements FudgeBuilder<T> {

  /**
   * The properties.
   */
  private final JBProperty[] _properties;
  /**
   * The bean name.
   */
  private final String _beanName;
  /**
   * The constructor.
   */
  private final Constructor<T> _constructor;

  /**
   * Creates a new builder for a given type.
   * 
   * @param <T> class the builder should process
   * @param clazz  the class the builder should process
   * @return the builder
   */
  /* package */static <T> JavaBeanBuilder<T> create(final Class<T> clazz) {
    // customise the properties
    final ArrayList<JBProperty> propList = new ArrayList<JBProperty>();
    for (PropertyDescriptor prop : PropertyUtils.getPropertyDescriptors(clazz)) {
      // ignore the class
      if (prop.getName().equals("class"))
        continue;
      // check for FudgeFieldName annotations on either accessor or mutator
      FudgeFieldName annoName;
      FudgeFieldOrdinal annoOrdinal;
      String name = prop.getName();
      Integer ordinal = null;
      if (prop.getWriteMethod() != null) {
        // give up if it's a transient property
        if (TransientUtil.hasTransientAnnotation(prop.getWriteMethod())) {
          continue;
        }
        if ((annoName = prop.getWriteMethod().getAnnotation(FudgeFieldName.class)) != null) {
          name = annoName.value();
        }
        if ((annoOrdinal = prop.getWriteMethod().getAnnotation(FudgeFieldOrdinal.class)) != null) {
          ordinal = (int) annoOrdinal.value();
          if (annoOrdinal.noFieldName()) {
            name = null;
          }
        }
      }
      if (prop.getReadMethod() != null) {
        // give up if it's a transient property
        if (TransientUtil.hasTransientAnnotation(prop.getReadMethod())) {
          continue;
        }
        if ((annoName = prop.getReadMethod().getAnnotation(FudgeFieldName.class)) != null) {
          name = annoName.value();
        }
        if ((annoOrdinal = prop.getReadMethod().getAnnotation(FudgeFieldOrdinal.class)) != null) {
          ordinal = (int) annoOrdinal.value();
          if (annoOrdinal.noFieldName()) {
            name = null;
          }
        }
      }
      propList.add(new JBProperty(name, ordinal, prop.getReadMethod(), prop.getWriteMethod(), prop.getPropertyType()));
    }
    // try and find a constructor
    try {
      return new JavaBeanBuilder<T>(propList.toArray(new JBProperty[propList.size()]), clazz.getConstructor());
    } catch (SecurityException ex) {
      // ignore
    } catch (NoSuchMethodException ex) {
      // ignore
    }
    // otherwise bean behaviour (about 5 times slower!)
    return new JavaBeanBuilder<T>(propList.toArray(new JBProperty[propList.size()]), clazz.getName());
  }

  /**
   * Creates an instance.
   * 
   * @param properties  the properties, not null
   * @param beanName  the bean name, not null
   */
  private JavaBeanBuilder(final JBProperty[] properties, final String beanName) {
    _properties = properties;
    _beanName = beanName;
    _constructor = null;
  }

  /**
   * Creates an instance.
   * 
   * @param properties  the properties, not null
   * @param beanName  the bean name, not null
   * @param constructor  the constructor, may be null
   */
  private JavaBeanBuilder(final JBProperty[] properties, final Constructor<T> constructor) {
    _properties = properties;
    _beanName = null;
    _constructor = constructor;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the properties.
   * 
   * @return the properties, not null
   */
  private JBProperty[] getProperties() {
    return _properties;
  }

  /**
   * Gets the bean name.
   * 
   * @return the bean name, not null
   */
  private String getBeanName() {
    return _beanName;
  }

  /**
   * Gets the constructor.
   * 
   * @return the constructor, may be null
   */
  private Constructor<T> getConstructor() {
    return _constructor;
  }

  /**
   * Creates a new bean.
   * 
   * @return the bean, not null
   */
  @SuppressWarnings("unchecked")
  private T newBeanObject() throws IllegalArgumentException, InstantiationException, IllegalAccessException,
      InvocationTargetException, IOException, ClassNotFoundException {
    if (getConstructor() != null) {
      return getConstructor().newInstance();
    } else {
      // Warning: the Beans.instantiate method below was about 5 times slower in the perf tests
      return (T) Beans.instantiate(getClass().getClassLoader(), getBeanName());
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, T object) {
    final MutableFudgeMsg message = serializer.newMessage();
    try {
      for (JBProperty prop : getProperties()) {
        if (prop.getRead() == null) {
          continue;
        }
        serializer.addToMessageWithClassHeaders(
            message, prop.getName(), prop.getOrdinal(), prop.getRead().invoke(object), prop.getType());
      }
    } catch (IllegalArgumentException ex) {
      throw new FudgeRuntimeException("Unable to serialise " + object, ex);
    } catch (IllegalAccessException ex) {
      throw new FudgeRuntimeException("Unable to serialise " + object, ex);
    } catch (InvocationTargetException ex) {
      throw new FudgeRuntimeException("Unable to serialise " + object, ex);
    }
    return message;
  }

  @Override
  public T buildObject(FudgeDeserializer context, FudgeMsg message) {
    final T object;
    try {
      object = newBeanObject();
      for (JBProperty prop : getProperties()) {
        if (prop.getWrite() == null) {
          continue;
        }
        final FudgeField field;
        if (prop.getOrdinal() == null) {
          field = message.getByName(prop.getName());
        } else {
          field = message.getByOrdinal(prop.getOrdinal());
        }
        if (field == null)
          continue;
        prop.getWrite().invoke(object, context.fieldValueToObject(prop.getType(), field));
      }
    } catch (IOException ex) {
      throw new FudgeRuntimeException("Unable to deserialise " + getBeanName(), ex);
    } catch (ClassNotFoundException ex) {
      throw new FudgeRuntimeException("Unable to deserialise " + getBeanName(), ex);
    } catch (InstantiationException ex) {
      throw new FudgeRuntimeException("Unable to deserialise " + getBeanName(), ex);
    } catch (IllegalArgumentException ex) {
      throw new FudgeRuntimeException("Unable to deserialise " + getBeanName(), ex);
    } catch (IllegalAccessException ex) {
      throw new FudgeRuntimeException("Unable to deserialise " + getBeanName(), ex);
    } catch (InvocationTargetException ex) {
      throw new FudgeRuntimeException("Unable to deserialise " + getBeanName(), ex);
    }
    return object;
  }

  //-------------------------------------------------------------------------
  /**
   * Data storage class.
   */
  private static class JBProperty {
    private final String _name;
    private final Integer _ordinal;
    private final Method _read;
    private final Method _write;
    private final Class<?> _type;
    private JBProperty (final String name, final Integer ordinal, final Method read, final Method write, final Class<?> type) {
      _read = read;
      _write = write;
      _type = type;
      _name = name;
      _ordinal = ordinal;
    }
    private String getName () {
      return _name;
    }
    private Integer getOrdinal () {
      return _ordinal;
    }
    private Method getRead () {
      return _read;
    }
    private Method getWrite () {
      return _write;
    }
    private Class<?> getType () {
      return _type;
    }
  }

}

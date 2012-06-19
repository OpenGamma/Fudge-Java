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

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

/**
 * Builder for primitive Java objects.
 * <p>
 * This is required if a primitive was written out through serialization, such as with
 * a class name but is being deserialized to an Object target.
 */
/* package */class PrimitiveBuilder {

  /**
   * The value field name.
   */
  private static final String VALUE_KEY = "value";

  //-------------------------------------------------------------------------
  /**
   * Handles java.lang.Boolean
   */
  /* package */static class BuildBoolean implements FudgeBuilder<Boolean> {

    /**
     * Singleton instance.
     */
    /* package */static final FudgeBuilder<Boolean> INSTANCE = new BuildBoolean();

    private BuildBoolean() {
    }

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Boolean object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(VALUE_KEY, ((boolean) object) ? 1 : 0);
      return message;
    }

    @Override
    public Boolean buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      return message.getValue(Integer.class, VALUE_KEY) != 0;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Handles java.lang.Byte
   */
  /* package */static class BuildByte implements FudgeBuilder<Byte> {

    /**
     * Singleton instance.
     */
    /* package */static final FudgeBuilder<Byte> INSTANCE = new BuildByte();

    private BuildByte() {
    }

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Byte object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(VALUE_KEY, object);
      return message;
    }

    @Override
    public Byte buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      return message.getValue(Byte.class, VALUE_KEY);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Handles java.lang.Double
   */
  /* package */static class BuildDouble implements FudgeBuilder<Double> {

    /**
     * Singleton instance.
     */
    /* package */static final FudgeBuilder<Double> INSTANCE = new BuildDouble();

    private BuildDouble() {
    }

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Double object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(VALUE_KEY, object);

      if (Double.isNaN(object) || Double.isInfinite(object)) {
          throw new ArithmeticException("Illegal double value: " + object);
      }

      return message;
    }

    @Override
    public Double buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      return message.getValue(Double.class, VALUE_KEY);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Handles java.lang.Character. A character isn't available as a Fudge primitive, so is written as
   * a string of length 1.
   */
  /* package */static class BuildCharacter implements FudgeBuilder<Character> {

    /**
     * Singleton instance.
     */
    /* package */static final FudgeBuilder<Character> INSTANCE = new BuildCharacter();

    private BuildCharacter() {
    }

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Character object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(VALUE_KEY, object.toString());
      return message;
    }

    @Override
    public Character buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      return message.getValue(String.class, VALUE_KEY).charAt(0);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Handles java.lang.Float
   */
  /* package */static class BuildFloat implements FudgeBuilder<Float> {

    /**
     * Singleton instance.
     */
    /* package */static final FudgeBuilder<Float> INSTANCE = new BuildFloat();

    private BuildFloat() {
    }

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Float object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(VALUE_KEY, object);

      if (Float.isNaN(object) || Float.isInfinite(object)) {
          throw new ArithmeticException("Illegal float value: " + object);
      }

      return message;
    }

    @Override
    public Float buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      return message.getValue(Float.class, VALUE_KEY);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Handles java.lang.Integer
   */
  /* package */static class BuildInteger implements FudgeBuilder<Integer> {

    /**
     * Singleton instance.
     */
    /* package */static final FudgeBuilder<Integer> INSTANCE = new BuildInteger();

    private BuildInteger() {
    }

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Integer object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(VALUE_KEY, object);
      return message;
    }

    @Override
    public Integer buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      return message.getValue(Integer.class, VALUE_KEY);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Handles java.lang.Long
   */
  /* package */static class BuildLong implements FudgeBuilder<Long> {

    /**
     * Singleton instance.
     */
    /* package */static final FudgeBuilder<Long> INSTANCE = new BuildLong();

    private BuildLong() {
    }

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Long object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(VALUE_KEY, object);
      return message;
    }

    @Override
    public Long buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      return message.getValue(Long.class, VALUE_KEY);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Handles java.lang.Short
   */
  /* package */static class BuildShort implements FudgeBuilder<Short> {

    /**
     * Singleton instance.
     */
    /* package */static final FudgeBuilder<Short> INSTANCE = new BuildShort();

    private BuildShort() {
    }

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Short object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(VALUE_KEY, object);
      return message;
    }

    @Override
    public Short buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      return message.getValue(Short.class, VALUE_KEY);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Handles java.lang.String
   */
  /* package */static class BuildString implements FudgeBuilder<String> {

    /**
     * Singleton instance.
     */
    /* package */static final FudgeBuilder<String> INSTANCE = new BuildString();

    private BuildString() {
    }

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, String object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(VALUE_KEY, object);
      return message;
    }

    @Override
    public String buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      return message.getValue(String.class, VALUE_KEY);
    }
  }

}

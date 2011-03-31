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

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

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
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Boolean object) {
      final MutableFudgeFieldContainer message = context.newMessage();
      message.add(VALUE_KEY, ((boolean) object) ? 1 : 0);
      return message;
    }

    @Override
    public Boolean buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
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
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Byte object) {
      final MutableFudgeFieldContainer message = context.newMessage();
      message.add(VALUE_KEY, object);
      return message;
    }

    @Override
    public Byte buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
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
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Double objectVal) {
      final MutableFudgeFieldContainer message = context.newMessage();
      message.add(VALUE_KEY, objectVal);

      if (Double.isNaN(objectVal) || Double.isInfinite(objectVal)) {
          throw new ArithmeticException("Illegal double value: " + objectVal);
      }

      return message;
    }

    @Override
    public Double buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
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
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Character object) {
      final MutableFudgeFieldContainer message = context.newMessage();
      message.add(VALUE_KEY, object.toString());
      return message;
    }

    @Override
    public Character buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
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
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Float objectVal) {
      final MutableFudgeFieldContainer message = context.newMessage();
      message.add(VALUE_KEY, objectVal);

      if (Float.isNaN(objectVal) || Float.isInfinite(objectVal)) {
          throw new ArithmeticException("Illegal float value: " + objectVal);
      }

      return message;
    }

    @Override
    public Float buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
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
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Integer object) {
      final MutableFudgeFieldContainer message = context.newMessage();
      message.add(VALUE_KEY, object);
      return message;
    }

    @Override
    public Integer buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
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
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Long object) {
      final MutableFudgeFieldContainer message = context.newMessage();
      message.add(VALUE_KEY, object);
      return message;
    }

    @Override
    public Long buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
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
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Short object) {
      final MutableFudgeFieldContainer message = context.newMessage();
      message.add(VALUE_KEY, object);
      return message;
    }

    @Override
    public Short buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
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
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, String object) {
      final MutableFudgeFieldContainer message = context.newMessage();
      message.add(VALUE_KEY, object);
      return message;
    }

    @Override
    public String buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
      return message.getValue(String.class, VALUE_KEY);
    }
  }

}

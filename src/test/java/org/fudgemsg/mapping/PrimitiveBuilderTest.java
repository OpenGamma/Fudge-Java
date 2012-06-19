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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.junit.Before;
import org.junit.Test;

public class PrimitiveBuilderTest {

  private FudgeSerializer _serializer;
  private FudgeDeserializer _deserializer;

  @Before
  public void createContext() {
    _serializer = new FudgeSerializer(FudgeContext.GLOBAL_DEFAULT);
    _deserializer = new FudgeDeserializer(FudgeContext.GLOBAL_DEFAULT);
  }

  private FudgeMsg cycle(final FudgeMsg message) {
    final FudgeContext context = _serializer.getFudgeContext();
    final byte[] binary = context.toByteArray(message);
    final FudgeMsgEnvelope envelope = context.deserialize(binary);
    return envelope.getMessage();
  }

  private <T> void cycle(final Class<T> clazz, final T value) {
    FudgeMsg message = FudgeSerializer.addClassHeader(_serializer.objectToFudgeMsg(value), clazz, Object.class);
    System.out.println(clazz + ":" + value + " => " + message);
    message = cycle(message);
    System.out.println(clazz + ":" + value + " => " + message);
    final Object result = _deserializer.fudgeMsgToObject(Object.class, message);
    assertNotNull(result);
    assertEquals(value, result);
  }
  
  @Test
  public void testBoolean () {
    cycle(Boolean.class, Boolean.TRUE);
    cycle(Boolean.class, Boolean.FALSE);
  }
  
  @Test
  public void testByte () {
    cycle(Byte.class, (Byte) (byte) 0);
    cycle(Byte.class, (Byte) Byte.MIN_VALUE);
    cycle(Byte.class, (Byte) Byte.MAX_VALUE);
  }

  @Test
  public void testDouble () {
    cycle(Double.class, (Double) 0d);
    cycle(Double.class, (Double) 42d);
  }

  @Test(expected = ArithmeticException.class)
  public void testDouble_NaN() {
    cycle(Double.class, (Double) Double.NaN);
  }

  @Test(expected = ArithmeticException.class)
  public void testDouble_NEGATIVE_INFINITY() {
    cycle(Double.class, (Double) Double.NEGATIVE_INFINITY);
  }

  @Test(expected = ArithmeticException.class)
  public void testDouble_POSITIVE_INFINITY() {
    cycle(Double.class, (Double) Double.POSITIVE_INFINITY);
  }

  @Test
  public void testCharacter () {
    cycle(Character.class, (Character) '!');
  }

  @Test
  public void testFloat() {
    cycle(Float.class, (Float) (float) 0.0);
    cycle(Float.class, (Float) (float) 42.0);
  }

  @Test(expected = ArithmeticException.class)
  public void testFloat_NaN() {
    cycle(Float.class, (Float) Float.NaN);
  }

  @Test(expected = ArithmeticException.class)
  public void testFloat_NEGATIVE_INFINITY() {
    cycle(Float.class, (Float) Float.NEGATIVE_INFINITY);
  }

  @Test(expected = ArithmeticException.class)
  public void testFloat_POSITIVE_INFINITY() {
    cycle(Float.class, (Float) Float.POSITIVE_INFINITY);
  }

  @Test
  public void testInteger () {
    cycle(Integer.class, (Integer) 0);
    cycle(Integer.class, (Integer) Integer.MIN_VALUE);
    cycle(Integer.class, (Integer) Integer.MAX_VALUE);
  }

  @Test
  public void testLong () {
    cycle(Long.class, (Long) 0L);
    cycle(Long.class, (Long) Long.MIN_VALUE);
    cycle(Long.class, (Long) Long.MAX_VALUE);
  }

  @Test
  public void testShort () {
    cycle(Short.class, (Short) (short) 0);
    cycle(Short.class, (Short) Short.MIN_VALUE);
    cycle(Short.class, (Short) Short.MAX_VALUE);
  }

  @Test
  public void testString () {
    cycle(String.class, "");
    cycle(String.class, "Hello World");
  }

}
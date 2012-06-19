/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and other contributors.
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

package org.fudgemsg.wire;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.StandardFudgeMessages;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link EncodedFudgeMsg} implementation and lazy unpacking of sub-messages from Fudge streams.
 */
public class EncodedFudgeMsgTest {

  private static FudgeMsg createTestMessage2() {
    final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("foo", StandardFudgeMessages.createMessageAllNames(FudgeContext.GLOBAL_DEFAULT));
    msg.add(42, StandardFudgeMessages.createMessageAllNames(FudgeContext.GLOBAL_DEFAULT));
    msg.add(null, null, StandardFudgeMessages.createMessageAllNames(FudgeContext.GLOBAL_DEFAULT));
    return msg;
  }

  private static FudgeMsg createTestMessage1() {
    final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("foo", createTestMessage2());
    msg.add(42, createTestMessage2());
    msg.add(null, null, createTestMessage2());
    return msg;
  }

  private static FudgeMsg createTestMessage0() {
    final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("foo", createTestMessage1());
    msg.add(42, createTestMessage1());
    msg.add(null, null, createTestMessage1());
    return msg;
  }

  private FudgeMsg _testMessage;
  private byte[] _testMessageEnvelope;
  private EncodedFudgeMsg _testMessageEnc;

  @Before
  public void createTestMessages() {
    _testMessage = createTestMessage0();
    _testMessageEnvelope = FudgeContext.GLOBAL_DEFAULT.toByteArray(_testMessage);
    _testMessageEnc = new EncodedFudgeMsg(_testMessageEnvelope, 8, _testMessageEnvelope.length - 8,
        FudgeContext.GLOBAL_DEFAULT);
  }

  @Test
  public void testGetNumFields() {
    assertEquals(3, _testMessageEnc.getNumFields());
  }

  @Test
  public void testIsEmpty() {
    assertFalse(_testMessageEnc.isEmpty());
  }

  @Test
  public void testIterator() {
    final Iterator<FudgeField> itr = _testMessageEnc.iterator();
    assertTrue(itr.hasNext());
    assertNotNull(itr.next());
    assertNotNull(itr.next());
    assertNotNull(itr.next());
    assertFalse(itr.hasNext());
  }

  @Test
  public void testGetAllFields() {
    List<FudgeField> fields = _testMessageEnc.getAllFields();
    assertEquals(3, fields.size());
    assertTrue(fields.get(0).getValue() instanceof FudgeMsg);
    fields = ((FudgeMsg) fields.get(0).getValue()).getAllFields();
    assertEquals(3, fields.size());
    assertTrue(fields.get(0).getValue() instanceof FudgeMsg);
    fields = ((FudgeMsg) fields.get(0).getValue()).getAllFields();
    assertEquals(3, fields.size());
    assertTrue(fields.get(0).getValue() instanceof FudgeMsg);
    fields = ((FudgeMsg) fields.get(0).getValue()).getAllFields();
    assertEquals(21, fields.size());
  }

  @Test
  public void testGetAllFieldNames() {
    final Set<String> names = _testMessageEnc.getAllFieldNames();
    assertNotNull(names);
    assertEquals(1, names.size());
  }

  @Test
  public void testGetByIndex() {
    final FudgeField field = _testMessageEnc.getByIndex(1);
    assertNotNull(field);
    assertEquals((Integer) 42, field.getOrdinal());
    assertTrue(field.getValue() instanceof EncodedFudgeMsg);
  }

  @Test
  public void testHasField() {
    assertTrue(_testMessageEnc.hasField(42));
    assertTrue(_testMessageEnc.hasField("foo"));
    assertFalse(_testMessageEnc.hasField(99));
    assertFalse(_testMessageEnc.hasField("bar"));
  }

  @Test
  public void testGetAllByName() {
    List<FudgeField> fields = _testMessageEnc.getAllByName("foo");
    assertNotNull(fields);
    assertEquals(1, fields.size());
    fields = _testMessageEnc.getAllByName("bar");
    assertNotNull(fields);
    assertTrue(fields.isEmpty());
  }

  @Test
  public void testGetByName() {
    FudgeField field = _testMessageEnc.getByName("foo");
    assertNotNull(field);
    assertTrue(field.getValue() instanceof EncodedFudgeMsg);
    field = _testMessageEnc.getByName("bar");
    assertNull(field);
  }

  @Test
  public void testGetAllByOrdinal() {
    List<FudgeField> fields = _testMessageEnc.getAllByOrdinal(42);
    assertNotNull(fields);
    assertEquals(1, fields.size());
    fields = _testMessageEnc.getAllByOrdinal(99);
    assertNotNull(fields);
    assertTrue(fields.isEmpty());
  }

  @Test
  public void testGetByOrdinal() {
    FudgeField field = _testMessageEnc.getByOrdinal(42);
    assertNotNull(field);
    assertTrue(field.getValue() instanceof EncodedFudgeMsg);
    field = _testMessageEnc.getByOrdinal(99);
    assertNull(field);
  }

  @Test
  public void testGetFieldValue() {
    final FudgeMsg field = _testMessageEnc.getFieldValue(FudgeMsg.class, _testMessageEnc
        .getByIndex(1));
    assertNotNull(field);
  }

  @Test
  public void testGetValue() {
    FudgeMsg field = _testMessageEnc.getValue(FudgeMsg.class, 42);
    assertNotNull(field);
    assertTrue(field instanceof EncodedFudgeMsg);
    field = _testMessageEnc.getValue(FudgeMsg.class, "foo");
    assertNotNull(field);
    assertTrue(field instanceof EncodedFudgeMsg);
    field = _testMessageEnc.getValue(FudgeMsg.class, 99);
    assertNull(field);
    field = _testMessageEnc.getValue(FudgeMsg.class, "bar");
    assertNull(field);
  }

  private FudgeMsg getInnerMessage() {
    return _testMessageEnc.getValue(FudgeMsg.class, 42).getValue(FudgeMsg.class, 42).getValue(
        FudgeMsg.class, 42);
  }

  @Test
  public void testGetDouble() {
    final Double value = getInnerMessage().getDouble("double");
    assertNotNull(value);
  }

  @Test
  public void testGetFloat() {
    final Float value = getInnerMessage().getFloat("float");
    assertNotNull(value);
  }

  @Test
  public void testGetLong() {
    final Long value = getInnerMessage().getLong("long");
    assertNotNull(value);
  }

  @Test
  public void testGetInt() {
    final Integer value = getInnerMessage().getInt("int");
    assertNotNull(value);
  }

  @Test
  public void testGetShort() {
    final Short value = getInnerMessage().getShort("short");
    assertNotNull(value);
  }

  @Test
  public void testGetByte() {
    final Byte value = getInnerMessage().getByte("byte");
    assertNotNull(value);
  }

  @Test
  public void testGetString() {
    final String value = getInnerMessage().getString("String");
    assertNotNull(value);
  }

  @Test
  public void testGetBoolean() {
    final Boolean value = getInnerMessage().getBoolean("boolean");
    assertNotNull(value);
  }

  @Test
  public void testGetMessage() {
    final FudgeMsg value = _testMessageEnc.getMessage("foo");
    assertNotNull(value);
  }

  private static class LoopingByteArrayInputStream extends ByteArrayInputStream {

    public LoopingByteArrayInputStream(final byte[] buffer) {
      super(buffer);
    }

    @Override
    public int read() {
      final int result = super.read();
      if (pos >= count) {
        pos = 0;
      }
      return result;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) {
      final int result = super.read(b, off, len);
      if (pos >= count) {
        pos = 0;
      }
      return result;
    }

  }

  private long testLazySpeedImpl(final FudgeMsgReader reader, final FudgeMsgWriter writer, final int count) {
    final long time = System.nanoTime();
    long readTime = 0, procTime = 0, writeTime = 0, t;
    for (int i = 0; i < count; i++) {
      readTime -= System.nanoTime();
      final FudgeMsg outer = reader.nextMessage();
      readTime += (t = System.nanoTime());
      procTime -= t;
      final FudgeMsg inner = outer.getMessage(42).getMessage(42).getMessage(42);
      procTime += (t = System.nanoTime());
      writeTime -= t;
      writer.writeMessage(inner);
      writeTime += System.nanoTime();
    }
    System.out.println("R=" + ((double) readTime / 1e6) + ", P=" + ((double) procTime / 1e6) + ", W="
        + ((double) writeTime / 1e6));
    return System.nanoTime() - time;
  }

  private void testLazySpeedImpl(final ByteArrayOutputStream baos, final boolean assertTimes) {
    FudgeMsgReader reader = FudgeContext.GLOBAL_DEFAULT.createMessageReader(new LoopingByteArrayInputStream(
        _testMessageEnvelope));
    baos.reset();
    FudgeMsgWriter writer = FudgeContext.GLOBAL_DEFAULT.createMessageWriter(baos);
    final long normalTime = testLazySpeedImpl(reader, writer, 1000);
    final byte[] normal = baos.toByteArray();
    System.out.println("Time=" + normalTime + ", bytes=" + normal.length);
    reader = FudgeContext.GLOBAL_DEFAULT.createMessageReader(new LoopingByteArrayInputStream(_testMessageEnvelope));
    reader.setLazyReads(true);
    baos.reset();
    writer = FudgeContext.GLOBAL_DEFAULT.createMessageWriter(baos);
    final long lazyTime = testLazySpeedImpl(reader, writer, 1000);
    final byte[] lazy = baos.toByteArray();
    System.out.println("Time=" + lazyTime + ", bytes=" + lazy.length);
    assertArrayEquals(normal, lazy);
    System.out.println("N=" + normalTime + ", L=" + lazyTime + ", N-L=" + ((double) (normalTime - lazyTime) / 1e6)
        + "ms");
    if (assertTimes) {
      assertTrue(lazyTime <= normalTime);
    }
  }

  @Test
  public void testLazySpeed() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // Warm up
    testLazySpeedImpl(baos, false);
    // Timing test
    AssertionError ex = null;
    for (int i = 0; i < 3; i++) {
      try {
        testLazySpeedImpl(baos, true);
        return;  // sucess
      } catch (AssertionError ex2) {
        ex = ex2;
      }
    }
    throw ex;
  }

}

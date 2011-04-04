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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.junit.Test;

/**
 * Test FudgeMsg.
 */
public class FudgeMsgTest {

  private static final FudgeContext s_fudgeContext = new FudgeContext();

  /**
   * 
   */
  @Test
  public void lookupByNameSingleValue() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    FudgeField field = null;
    List<FudgeField> fields = null;
    
    field = msg.getByName("boolean");
    assertNotNull(field);
    assertEquals(FudgeWireType.BOOLEAN, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
    
    field = msg.getByName("Boolean");
    assertNotNull(field);
    assertEquals(FudgeWireType.BOOLEAN, field.getType());
    assertEquals(new Boolean(false), field.getValue());
    assertEquals("Boolean", field.getName());
    assertNull(field.getOrdinal());
    
    fields = msg.getAllByName("boolean");
    assertNotNull(fields);
    assertEquals(1, fields.size());
    field = fields.get(0);
    assertNotNull(field);
    assertEquals(FudgeWireType.BOOLEAN, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
    
    // Check the indicator type specially
    assertSame(IndicatorType.INSTANCE, msg.getValue("indicator"));
  }

  /**
   * 
   */
  @Test
  public void lookupByNameMultipleValues() {
    MutableFudgeMsg msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    FudgeField field = null;
    List<FudgeField> fields = null;
    
    // Now add a second by name.
    msg.add("boolean", Boolean.FALSE);

    field = msg.getByName("boolean");
    assertNotNull(field);
    assertEquals(FudgeWireType.BOOLEAN, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
    
    fields = msg.getAllByName("boolean");
    assertNotNull(fields);
    assertEquals(2, fields.size());
    field = fields.get(0);
    assertNotNull(field);
    assertEquals(FudgeWireType.BOOLEAN, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
    
    field = fields.get(1);
    assertNotNull(field);
    assertEquals(FudgeWireType.BOOLEAN, field.getType());
    assertEquals(Boolean.FALSE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
  }
  
  /**
   * 
   */
  @Test
  public void primitiveExactQueriesNamesMatch() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    
    assertEquals(new Byte((byte)5), msg.getByte("byte"));
    assertEquals(new Byte((byte)5), msg.getByte("Byte"));
    
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    assertEquals(new Short(shortValue), msg.getShort("short"));
    assertEquals(new Short(shortValue), msg.getShort("Short"));
    
    int intValue = ((int)Short.MAX_VALUE) + 5;
    assertEquals(new Integer(intValue), msg.getInt("int"));
    assertEquals(new Integer(intValue), msg.getInt("Integer"));
    
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    assertEquals(new Long(longValue), msg.getLong("long"));
    assertEquals(new Long(longValue), msg.getLong("Long"));
    
    assertEquals(new Float(0.5f), msg.getFloat("float"));
    assertEquals(new Float(0.5f), msg.getFloat("Float"));
    assertEquals(new Double(0.27362), msg.getDouble("double"));
    assertEquals(new Double(0.27362), msg.getDouble("Double"));
    
    assertEquals("Kirk Wylie", msg.getString("String"));
  }
  
  /**
   * 
   */
  @Test
  public void primitiveExactQueriesNamesNoMatch() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    assertNotNull(msg.getShort("byte"));
    assertNotNull(msg.getInt("short"));
    assertNotNull(msg.getLong("int"));
    assertNotNull(msg.getFloat("double"));
    assertNotNull(msg.getDouble("float"));
  }
  
  /**
   * 
   */
  @Test
  public void primitiveExactQueriesNoNames() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    
    assertNull(msg.getByte("foobar"));
    assertNull(msg.getShort("foobar"));
    assertNull(msg.getInt("foobar"));
    assertNull(msg.getLong("foobar"));
    assertNull(msg.getFloat("foobar"));
    assertNull(msg.getDouble("foobar"));
    assertNull(msg.getString("foobar"));
  }
  
  /**
   * 
   */
  @Test
  public void asQueriesToLongNames() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    
    assertEquals(new Long((byte)5), msg.getLong("byte"));
    assertEquals(new Long((byte)5), msg.getLong("Byte"));
    
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    assertEquals(new Long(shortValue), msg.getLong("short"));
    assertEquals(new Long(shortValue), msg.getLong("Short"));
    
    int intValue = ((int)Short.MAX_VALUE) + 5;
    assertEquals(new Long(intValue), msg.getLong("int"));
    assertEquals(new Long(intValue), msg.getLong("Integer"));
    
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    assertEquals(new Long(longValue), msg.getLong("long"));
    assertEquals(new Long(longValue), msg.getLong("Long"));
    
    assertEquals(new Long(0), msg.getLong("float"));
    assertEquals(new Long(0), msg.getLong("Float"));
    assertEquals(new Long(0), msg.getLong("double"));
    assertEquals(new Long(0), msg.getLong("Double"));
  }
  
  /**
   * 
   */
  @Test
  public void asQueriesToLongNoNames() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    
    assertNull(msg.getByte("foobar"));
    assertNull(msg.getShort("foobar"));
    assertNull(msg.getInt("foobar"));
    assertNull(msg.getLong("foobar"));
    assertNull(msg.getFloat("foobar"));
    assertNull(msg.getDouble("foobar"));
    assertNull(msg.getString("foobar"));
  }
  
  // ------------
  /**
   * 
   */
  @Test
  public void primitiveExactQueriesOrdinalsMatch() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext);
    
    assertEquals(new Byte((byte)5), msg.getByte((short)3));
    assertEquals(new Byte((byte)5), msg.getByte((short)4));
    
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    assertEquals(new Short(shortValue), msg.getShort((short)5));
    assertEquals(new Short(shortValue), msg.getShort((short)6));
    
    int intValue = ((int)Short.MAX_VALUE) + 5;
    assertEquals(new Integer(intValue), msg.getInt((short)7));
    assertEquals(new Integer(intValue), msg.getInt((short)8));
    
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    assertEquals(new Long(longValue), msg.getLong((short)9));
    assertEquals(new Long(longValue), msg.getLong((short)10));
    
    assertEquals(new Float(0.5f), msg.getFloat((short)11));
    assertEquals(new Float(0.5f), msg.getFloat((short)12));
    assertEquals(new Double(0.27362), msg.getDouble((short)13));
    assertEquals(new Double(0.27362), msg.getDouble((short)14));
    
    assertEquals("Kirk Wylie", msg.getString((short)15));
  }

  @Test
  public void immutableFudgeMsgTest() {
    MutableFudgeMsg mutableMsg = StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext);
    ImmutableFudgeMsg msg = new ImmutableFudgeMsg(s_fudgeContext, mutableMsg);
    
    assertEquals(null, mutableMsg.getString("field not there"));
    assertEquals(null, msg.getString("field not there"));
    
    mutableMsg.add("field not there", "is now");
    assertEquals("is now", mutableMsg.getString("field not there"));
    assertEquals(null, msg.getString("field not there"));
  }

  /**
   * 
   */
  @Test
  public void mutableFudgeMsgTest () {
    MutableFudgeMsg msg = StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext);
    assertEquals (null, msg.getString ("foo"));
    assertEquals (null, msg.getString (999));
    msg.add ("foo", "bar1");
    msg.add (999, "bar2");
    assertEquals ("bar1", msg.getString ("foo"));
    assertEquals ("bar2", msg.getString (999));
    msg.remove ("foo");
    msg.remove (999);
    assertEquals (null, msg.getString ("foo"));
    assertEquals (null, msg.getString (999));
    int sizeBefore = msg.getNumFields ();
    Iterator<FudgeField> iterator = msg.iterator ();
    int i = 0;
    while (iterator.hasNext ()) {
      iterator.next ();
      if ((i++ & 1) == 0) iterator.remove ();
    }
    assertEquals (sizeBefore / 2, msg.getNumFields ());
    msg.clear ();
    assertEquals (0, msg.getNumFields ());
  }

  /**
   * 
   */
  @Test
  public void primitiveExactQueriesOrdinalsNoMatch() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext);
    assertNotNull(msg.getShort((short)3));
    assertNotNull(msg.getInt((short)5));
    assertNotNull(msg.getLong((short)7));
    assertNotNull(msg.getFloat((short)14));
    assertNotNull(msg.getDouble((short)11));
  }
  
  /**
   * 
   */
  @Test
  public void primitiveExactOrdinalsNoOrdinals() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext);
  
    assertNull(msg.getByte((short)100));
    assertNull(msg.getShort((short)100));
    assertNull(msg.getInt((short)100));
    assertNull(msg.getLong((short)100));
    assertNull(msg.getFloat((short)100));
    assertNull(msg.getDouble((short)100));
    assertNull(msg.getString((short)100));
  }
  
  /**
   * 
   */
  @Test
  public void asQueriesToLongOrdinals() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext);
    
    assertEquals(new Long((byte)5), msg.getLong((short)3));
    assertEquals(new Long((byte)5), msg.getLong((short)4));
    
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    assertEquals(new Long(shortValue), msg.getLong((short)5));
    assertEquals(new Long(shortValue), msg.getLong((short)6));
    
    int intValue = ((int)Short.MAX_VALUE) + 5;
    assertEquals(new Long(intValue), msg.getLong((short)7));
    assertEquals(new Long(intValue), msg.getLong((short)8));
    
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    assertEquals(new Long(longValue), msg.getLong((short)9));
    assertEquals(new Long(longValue), msg.getLong((short)10));
    
    assertEquals(new Long(0), msg.getLong((short)11));
    assertEquals(new Long(0), msg.getLong((short)12));
    assertEquals(new Long(0), msg.getLong((short)13));
    assertEquals(new Long(0), msg.getLong((short)14));
  }
  
  /**
   * 
   */
  @Test
  public void toByteArray() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    byte[] bytes = s_fudgeContext.toByteArray(msg);
    assertNotNull(bytes);
    assertTrue(bytes.length > 10);
  }
  
  /**
   * 
   */
  @Test
  public void fixedLengthByteArrays() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllByteArrayLengths(s_fudgeContext);
    assertSame(FudgeWireType.BYTE_ARRAY_4, msg.getByName("byte[4]").getType());
    assertSame(FudgeWireType.BYTE_ARRAY_8, msg.getByName("byte[8]").getType());
    assertSame(FudgeWireType.BYTE_ARRAY_16, msg.getByName("byte[16]").getType());
    assertSame(FudgeWireType.BYTE_ARRAY_20, msg.getByName("byte[20]").getType());
    assertSame(FudgeWireType.BYTE_ARRAY_32, msg.getByName("byte[32]").getType());
    assertSame(FudgeWireType.BYTE_ARRAY_64, msg.getByName("byte[64]").getType());
    assertSame(FudgeWireType.BYTE_ARRAY_128, msg.getByName("byte[128]").getType());
    assertSame(FudgeWireType.BYTE_ARRAY_256, msg.getByName("byte[256]").getType());
    assertSame(FudgeWireType.BYTE_ARRAY_512, msg.getByName("byte[512]").getType());
    
    assertSame(FudgeWireType.BYTE_ARRAY, msg.getByName("byte[28]").getType());
  }
  
  /**
   * 
   */
  @Test
  public void iterable() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    int fieldCount = 0;
    for(@SuppressWarnings("unused") FudgeField field : msg) {
      fieldCount++;
    }
    assertEquals(msg.getNumFields(), fieldCount);
  }

  /**
   * 
   */
  @Test
  public void iterableContainer() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    int fieldCount = 0;
    for (@SuppressWarnings("unused") FudgeField field : msg) {
      fieldCount++;
    }
    assertEquals(msg.getNumFields(), fieldCount);
  }

  /**
   * 
   */
  @Test
  public void getMessageMethodsFRJ11() {
    FudgeMsg msg = StandardFudgeMessages.createMessageWithSubMsgs(s_fudgeContext);
    assertNull(msg.getMessage(42));
    assertNull(msg.getMessage("No Such Field"));
    assertTrue(msg.getMessage("sub1") instanceof FudgeMsg);
  }

  /**
   * 
   */
  @Test
  public void testIsEmpty() {
    MutableFudgeMsg msg = s_fudgeContext.newMessage();
    assertTrue(msg.isEmpty());
    msg.add(null, null, "foo");
    assertFalse(msg.isEmpty());
  }

  /**
   * 
   */
  @Test
  public void testEquals() {
    MutableFudgeMsg msg1 = s_fudgeContext.newMessage();
    msg1.add("foo", 1, "hello world");
    msg1.add("bar", 2, 42);
    MutableFudgeMsg msg2 = s_fudgeContext.newMessage();
    assertFalse(msg1.equals(msg2));
    assertFalse(msg2.equals(msg1));
    msg2.add("foo", 1, "hello world");
    assertFalse(msg1.equals(msg2));
    assertFalse(msg2.equals(msg1));
    msg2.add("bar", 2, 42);
    assertTrue(msg1.equals(msg2));
    assertTrue(msg2.equals(msg1));
    FudgeMsg msg3 = new ImmutableFudgeMsg(s_fudgeContext, msg2);
    FudgeMsg msg4 = new ImmutableFudgeMsg(s_fudgeContext, msg1);
    assertTrue(msg3.equals(msg4));
    assertTrue(msg4.equals(msg3));
    assertFalse(msg1.equals(msg3));
    assertFalse(msg3.equals(msg1));
    assertFalse(msg2.equals(msg4));
    assertFalse(msg4.equals(msg2));
  }

  /**
   * 
   */
  @Test
  public void hasFieldByName() {
    MutableFudgeMsg msg1 = s_fudgeContext.newMessage();
    msg1.add("foo", 1, "hello world 1");
    msg1.add("bar", 2, 42);
    msg1.add("foo", 1, "hello world 2");
    msg1.add(null, 3, "no name");

    assertTrue(msg1.hasField("foo"));
    assertTrue(msg1.hasField("bar"));
    assertFalse(msg1.hasField("foobar"));
  }

  /**
   * 
   */
  @Test
  public void hasFieldByOrdinal() {
    MutableFudgeMsg msg1 = s_fudgeContext.newMessage();
    msg1.add("foo", 1, "hello world 1");
    msg1.add("bar", 2, 42);
    msg1.add("foo", 1, "hello world 2");
    msg1.add(null, 3, "no name");

    assertTrue(msg1.hasField(1));
    assertTrue(msg1.hasField(2));
    assertTrue(msg1.hasField(3));
    assertFalse(msg1.hasField(4));
  }

}

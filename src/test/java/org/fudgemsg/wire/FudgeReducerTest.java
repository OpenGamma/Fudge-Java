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
package org.fudgemsg.wire;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.Currency;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.junit.Test;

/**
 * Test field reducing.
 */
public class FudgeReducerTest {

  @Test
  public void test_reduceSecondaryType() {
    final FudgeContext context = new FudgeContext();
    context.getTypeDictionary().addType(MockIntegerSecondaryType.INSTANCE, Currency.class);
    context.getTypeDictionary().addTypeConverter(MockIntegerSecondaryType.INSTANCE, Currency.class);
    
    MutableFudgeMsg msg = context.newMessage();
    msg.add((Integer) 24, (Integer) 5);  // 5 bytes - int -> byte (reduced)
    msg.add((Integer) 32, (Integer) 5000);  // 6 bytes - int -> short (not reduced)
    msg.add((Integer) 48, (Integer) 2000000000);  // 8 bytes - int -> int (not reduced)
    msg.add((Integer) 64, Currency.getInstance("GBP"));  // 5 bytes - int -> byte
    msg.add((Integer) 96, Currency.getInstance("EUR"));  // 8 bytes - int -> int
    
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    FudgeMsgWriter writer = context.createMessageWriter(baos);
    writer.writeMessage(msg);
    writer.close();
    byte[] bytes = baos.toByteArray();
    assertEquals(8 + 5 + 6 + 8 + 5 + 8, bytes.length);
    int pos = 0;
    pos += 8;
    assertEquals((byte) Integer.parseInt("10010000", 2), bytes[pos + 0]);  // prefix
    assertEquals((byte) (2) , bytes[pos + 1]);         // type (byte)
    assertEquals((byte) (24 >>> 8) , bytes[pos + 2]);  // ordinal
    assertEquals((byte) (24) , bytes[pos + 3]);        // ordinal
    assertEquals((byte) (5) , bytes[pos + 4]);         // value
    
    pos += 5;
    assertEquals((byte) Integer.parseInt("10010000", 2), bytes[pos + 0]);  // prefix
    assertEquals((byte) (3) , bytes[pos + 1]);         // type (short)
    assertEquals((byte) (32 >>> 8) , bytes[pos + 2]);  // ordinal
    assertEquals((byte) (32) , bytes[pos + 3]);        // ordinal
    assertEquals((byte) (5000 >>> 8) , bytes[pos + 4]); // value
    assertEquals((byte) (5000) , bytes[pos + 5]);
    
    pos += 6;
    assertEquals((byte) Integer.parseInt("10010000", 2), bytes[pos + 0]);  // prefix
    assertEquals((byte) (4) , bytes[pos + 1]);         // type (int)
    assertEquals((byte) (48 >>> 8) , bytes[pos + 2]);  // ordinal
    assertEquals((byte) (48) , bytes[pos + 3]);        // ordinal
    assertEquals((byte) (2000000000 >>> 24) , bytes[pos + 4]); // value
    assertEquals((byte) (2000000000 >>> 16) , bytes[pos + 5]);
    assertEquals((byte) (2000000000 >>> 8) , bytes[pos + 6]);
    assertEquals((byte) (2000000000) , bytes[pos + 7]);
    
    pos += 8;
    assertEquals((byte) Integer.parseInt("10010000", 2), bytes[pos + 0]);  // prefix
    assertEquals((byte) (2) , bytes[pos + 1]);         // type (byte)
    assertEquals((byte) (64 >>> 8) , bytes[pos + 2]);  // ordinal
    assertEquals((byte) (64) , bytes[pos + 3]);        // ordinal
    assertEquals((byte) (1) , bytes[pos + 4]);
    
    pos += 5;
    assertEquals((byte) Integer.parseInt("10010000", 2), bytes[pos + 0]);  // prefix
    assertEquals((byte) (4) , bytes[pos + 1]);         // type (int)
    assertEquals((byte) (96 >>> 8) , bytes[pos + 2]);  // ordinal
    assertEquals((byte) (96) , bytes[pos + 3]);        // ordinal
    assertEquals((byte) (2000000000 >>> 24) , bytes[pos + 4]); // value
    assertEquals((byte) (2000000000 >>> 16) , bytes[pos + 5]);
    assertEquals((byte) (2000000000 >>> 8) , bytes[pos + 6]);
    assertEquals((byte) (2000000000) , bytes[pos + 7]);
  }

}

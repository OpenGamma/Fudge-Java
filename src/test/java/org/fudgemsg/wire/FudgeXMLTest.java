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

import static org.fudgemsg.test.FudgeUtils.assertAllFieldsMatch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.StandardFudgeMessages;
import org.fudgemsg.taxonomy.FudgeTaxonomy;
import org.fudgemsg.taxonomy.ImmutableMapTaxonomyResolver;
import org.fudgemsg.taxonomy.MapFudgeTaxonomy;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;

/**
 * 
 */
public class FudgeXMLTest {
  
  private final FudgeContext _fudgeContext;

  private static FudgeTaxonomy getTaxonomy () {
    return new MapFudgeTaxonomy (
        new int[] { 1, 2, 3, 4, 5, 6 },
        new String[] { "boolean", "byte", "int", "string", "float", "double" }
        );
  }
  
  /**
   * 
   */
  public FudgeXMLTest () {
    _fudgeContext = new FudgeContext ();
    final Map<Short,FudgeTaxonomy> tr = new HashMap<Short,FudgeTaxonomy> ();
    tr.put ((short)1, getTaxonomy ());
    _fudgeContext.setTaxonomyResolver (new ImmutableMapTaxonomyResolver (tr));
  }

  @Test
  public void cycleXMLMessages() {
    System.out.println("cycleXMLMessages:");
    final FudgeMsg[] messages = createMessages();
    for (int i = 0; i < messages.length; i++) {
      final CharArrayWriter caw = new CharArrayWriter();
      final FudgeMsgWriter fmw = new FudgeMsgWriter(new FudgeXMLStreamWriter(_fudgeContext, caw));
      fmw.writeMessage(messages[i], 0); // no taxonomy
      final CharArrayReader car = new CharArrayReader(caw.toCharArray());
      final FudgeMsgReader fmr = new FudgeMsgReader(new FudgeXMLStreamReader(_fudgeContext, car));
      // first is the no-taxonomy version
      FudgeMsg message = fmr.nextMessage();
      assertNotNull(message);
      assertAllFieldsMatch(messages[i], message, false);

      FudgeMsg nextMessage = fmr.nextMessage();
      assertNull(nextMessage);

      fmw.close();
      fmr.close();
    }
    
    for (int i = 0; i < messages.length; i++) {
      final CharArrayWriter caw = new CharArrayWriter();
      final FudgeMsgWriter fmw = new FudgeMsgWriter(new FudgeXMLStreamWriter(_fudgeContext, caw));
      fmw.writeMessage (messages[i], 1); // taxonomy #1
      final CharArrayReader car = new CharArrayReader(caw.toCharArray());
      final FudgeMsgReader fmr = new FudgeMsgReader(new FudgeXMLStreamReader(_fudgeContext, car));
      // second is the taxonomy version
      FudgeMsg message = fmr.nextMessage ();
      assertNotNull(message);
      assertAllFieldsMatch (messages[i], message, false);
      
      FudgeMsg nextMessage = fmr.nextMessage();
      assertNull(nextMessage);
      
      fmw.close();
      fmr.close();
    }
  }

  /**
   * Tests cycling of messages containing dates and times.
   */
  @Test
  public void cycleDateAndTimes() {
    MutableFudgeMsg msg = _fudgeContext.newMessage();
    OffsetDateTime dateTimeUtc = OffsetDateTime.of(2011, 3, 8, 2, 18, 0, 0, ZoneOffset.UTC);
    OffsetDateTime dateTimePlus1 = OffsetDateTime.of(2011, 3, 8, 2, 18, 0, 0, ZoneOffset.ofHours(1));
    OffsetTime timeUtc = OffsetTime.of(2, 18, 0, 0, ZoneOffset.UTC);
    OffsetTime timePlus1 = OffsetTime.of(2, 18, 0, 0, ZoneOffset.ofHours(1));
    LocalDateTime localDateTime = LocalDateTime.of(2011, 3, 8, 2, 18, 0, 0);
    LocalDate localDate = LocalDate.of(2011, 3, 8);
    LocalTime localTime = LocalTime.of(2, 18);
    msg.add("dateTimeUtc", null, dateTimeUtc);
    msg.add("dateTimePlus1", null, dateTimePlus1);
    msg.add("localDateTime", null, localDateTime);
    msg.add("timeUtc", null, timeUtc);
    msg.add("timePlus1", null, timePlus1);
    msg.add("localDate", null, localDate);
    msg.add("localTime", null, localTime);

    StringWriter stringWriter = new StringWriter();
    FudgeXMLStreamWriter streamWriter = new FudgeXMLStreamWriter(_fudgeContext, stringWriter);
    FudgeMsgWriter msgWriter = new FudgeMsgWriter(streamWriter);
    msgWriter.writeMessage(msg);

    StringReader stringReader = new StringReader(stringWriter.toString());
    FudgeXMLStreamReader streamReader = new FudgeXMLStreamReader(_fudgeContext, stringReader);
    FudgeMsgReader msgReader = new FudgeMsgReader(streamReader);
    FudgeMsg readMsg = msgReader.nextMessage();

    assertEquals(dateTimeUtc, readMsg.getValue(OffsetDateTime.class, "dateTimeUtc"));
    assertEquals(dateTimePlus1, readMsg.getValue(OffsetDateTime.class, "dateTimePlus1"));
    assertEquals(timeUtc, readMsg.getValue(OffsetTime.class, "timeUtc"));
    assertEquals(timePlus1, readMsg.getValue(OffsetTime.class, "timePlus1"));
    assertEquals(localDateTime, readMsg.getValue(LocalDateTime.class, "localDateTime"));
    assertEquals(localDate, readMsg.getValue(LocalDate.class, "localDate"));
    assertEquals(localTime, readMsg.getValue(LocalTime.class, "localTime"));
  }

  private FudgeMsg[] createMessages () {
    return new FudgeMsg[] {
        StandardFudgeMessages.createMessageAllNames(_fudgeContext),
        StandardFudgeMessages.createMessageAllOrdinals(_fudgeContext),
        StandardFudgeMessages.createMessageWithSubMsgs(_fudgeContext),
        StandardFudgeMessages.createMessageAllByteArrayLengths(_fudgeContext),
        StandardFudgeMessages.createMessageNoNamesNoOrdinals(_fudgeContext)
    };
  }
  
}

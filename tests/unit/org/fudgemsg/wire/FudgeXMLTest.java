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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.StandardFudgeMessages;
import org.fudgemsg.taxonomy.FudgeTaxonomy;
import org.fudgemsg.taxonomy.ImmutableMapTaxonomyResolver;
import org.fudgemsg.taxonomy.MapFudgeTaxonomy;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.junit.Test;

/**
 * 
 *
 * @author Andrew
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
    }
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
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

import static org.junit.Assert.assertNotNull;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.StandardFudgeMessages;
import org.fudgemsg.taxonomy.FudgeTaxonomy;
import org.fudgemsg.taxonomy.ImmutableMapTaxonomyResolver;
import org.fudgemsg.taxonomy.MapFudgeTaxonomy;
import org.fudgemsg.test.FudgeUtils;
import org.fudgemsg.wire.json.FudgeJSONStreamReader;
import org.fudgemsg.wire.json.FudgeJSONStreamWriter;
import org.junit.Test;

/**
 * Test Fudge JSON.
 */
public class FudgeJSONTest {
  
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
  public FudgeJSONTest () {
    _fudgeContext = new FudgeContext ();
    final Map<Short,FudgeTaxonomy> tr = new HashMap<Short,FudgeTaxonomy> ();
    tr.put ((short)1, getTaxonomy ());
    _fudgeContext.setTaxonomyResolver (new ImmutableMapTaxonomyResolver (tr));
  }
  
  private FudgeMsg[] createMessages () {
    return new FudgeMsg[] {
        StandardFudgeMessages.createMessageAllNames (_fudgeContext),
        StandardFudgeMessages.createMessageAllOrdinals (_fudgeContext),
        StandardFudgeMessages.createMessageWithSubMsgs (_fudgeContext),
        StandardFudgeMessages.createMessageAllByteArrayLengths (_fudgeContext) };
  }
  
  /**
   * 
   */
  @Test
  public void writeJSONMessages () {
    System.out.println ("writeJSONMessages:");
    final FudgeMsgWriter fmw = new FudgeMsgWriter (new FudgeJSONStreamWriter (_fudgeContext, new PrintWriter (System.out)));
    final FudgeMsg[] messages = createMessages ();
    for (int i = 0; i < messages.length; i++) {
      fmw.writeMessage (messages[i], 0); // no taxonomy
      fmw.flush();
      System.out.println ();
      fmw.writeMessage (messages[i], 1); // taxonomy #1
      fmw.flush();
      System.out.println ();
    }
  }
  
  /**
   * 
   */
  @Test
  public void cycleJSONMessages () {
    System.out.println ("cycleJSONMessages:");
    final CharArrayWriter caw = new CharArrayWriter ();
    final FudgeMsgWriter fmw = new FudgeMsgWriter (new FudgeJSONStreamWriter (_fudgeContext, caw));
    final FudgeMsg[] messages = createMessages ();
    for (int i = 0; i < messages.length; i++) {
      fmw.writeMessage (messages[i], 0);
      fmw.writeMessage (messages[i], 1);
    }
    final CharArrayReader car = new CharArrayReader (caw.toCharArray ());
    final FudgeMsgReader fmr = new FudgeMsgReader (new FudgeJSONStreamReader (_fudgeContext, car));
    for (int i = 0; i < messages.length; i++) {
      // first is the no-taxonomy version
      FudgeMsg message = fmr.nextMessage ();
      assertNotNull (message);
      System.out.println (message);
//      FudgeUtils.assertAllFieldsMatch (messages[i], message, false);
      // second is the taxonomy version
      message = fmr.nextMessage ();
      assertNotNull (message);
      System.out.println (message);
//      FudgeUtils.assertAllFieldsMatch (messages[i], message, false);
    }
  }
  
}
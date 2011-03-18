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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.taxon.ImmutableMapTaxonomyResolver;
import org.fudgemsg.taxon.MapFudgeTaxonomy;
import org.fudgemsg.xml.FudgeXMLStreamWriter;
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
  
  private void xmlTest (final FudgeFieldContainer message, final int taxonomy, final String filename) throws IOException {
    String expectedXML = readXMLFile(filename);
    StringWriter sw = new StringWriter(1024);
    final FudgeMsgWriter fmw = new FudgeMsgWriter (new FudgeXMLStreamWriter (_fudgeContext, new PrintWriter (sw)));
    fmw.writeMessage (message, taxonomy);
    fmw.flush ();
    System.out.println (sw);
    assertEquals(expectedXML, sw.toString());
  }
  
  @Test
  public void xmlStreamWriterAllNamesNoTaxonomy () throws Exception {
    xmlTest (StandardFudgeMessages.createMessageAllNames (_fudgeContext), 0, "allNamesNoTaxonomy.xml");
  }
  
  @Test
  public void xmlStreamWriterAllNamesTaxonomy () throws Exception {
    xmlTest (StandardFudgeMessages.createMessageAllNames (_fudgeContext), 1, "allNamesTaxonomy.xml");
  }
  
  @Test
  public void xmlStreamWriterAllOrdinalsNoTaxonomy () throws Exception {
    xmlTest (StandardFudgeMessages.createMessageAllOrdinals (_fudgeContext), 0, "allOrdinalsNoTaxonomy.xml");
  }
  
  @Test
  public void xmlStreamWriterAllOrdinalsTaxonomy () throws Exception {
    xmlTest (StandardFudgeMessages.createMessageAllOrdinals (_fudgeContext), 1, "allOrdinalsTaxonomy.xml");
  }
  
  @Test
  public void xmlStreamWriterWithSubMsgsNoTaxonomy () throws Exception {
    xmlTest (StandardFudgeMessages.createMessageWithSubMsgs (_fudgeContext), 0, "withSubMsgsNoTaxonomy.xml");
  }
  
  @Test
  public void xmlStreamWriterWithSubMsgsTaxonomy () throws Exception {
    xmlTest (StandardFudgeMessages.createMessageWithSubMsgs (_fudgeContext), 1, "withSubMsgsTaxonomy.xml");
  }
  
  private String readXMLFile(String filename) throws IOException {
    StringWriter sw = new StringWriter();
    InputStreamReader in = new InputStreamReader(FudgeXMLTest.class.getResourceAsStream(filename));
    copy(in, sw);
    in.close();
    return sw.toString();
  }
  
  private void copy(Reader input, Writer output) throws IOException {
    char[] buffer = new char[1024 * 4];
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
        output.write(buffer, 0, n);
    }
  }
  
}
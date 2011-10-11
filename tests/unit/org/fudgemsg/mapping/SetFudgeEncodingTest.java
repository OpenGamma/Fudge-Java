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


import org.fudgemsg.AbstractFudgeBuilderTestCase;
import org.fudgemsg.wire.MockIntegerSecondaryType;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertTrue;


/**
 * Test Set Fudge encoding.
 */

public class SetFudgeEncodingTest extends AbstractFudgeBuilderTestCase {


  @Before
  public void createWriter() {

    getFudgeContext().getTypeDictionary().addType(MockCurrencySecondaryType.INSTANCE, Currency.class);
    getFudgeContext().getTypeDictionary().addTypeConverter(MockCurrencySecondaryType.INSTANCE, Currency.class);
  }

  @Test
  public void testSetContainingSingleCurrency() throws IOException {

    Set set = new TreeSet();
    set.add(Currency.getInstance("USD"));

    Set deserializedObject = cycleObject(set);

    isInstanceOf(deserializedObject, Set.class);
    isInstanceOf(deserializedObject, HashSet.class);
    Object[] entries = deserializedObject.toArray();
    isInstanceOf(entries[0], Currency.class);

  }

  @Test
  public void testSetContainingSeveralCurrency() throws IOException {

    Set set = new TreeSet<Currency>(new Comparator<Currency>() {
      @Override
      public int compare(Currency o1, Currency o2) {
        return o1.getSymbol().compareTo(o2.getSymbol());
      }
    });
    set.add(Currency.getInstance("USD"));
    set.add(Currency.getInstance("GBP"));
    set.add(Currency.getInstance("EUR"));

    Set deserializedObject = cycleObject(set);

    isInstanceOf(deserializedObject, Set.class);
    Object[] entries = deserializedObject.toArray();

    isInstanceOf(entries[0], Currency.class);
    isInstanceOf(entries[1], Currency.class);
    isInstanceOf(entries[2], Currency.class);
  }

  @Test
  public void testSetContainingSeveralCurrencyAndAString() throws IOException {

    Set set = new HashSet();
    set.add(Currency.getInstance("USD"));
    set.add(Currency.getInstance("GBP"));
    set.add(Currency.getInstance("EUR"));
    set.add("Some String");

    Set deserializedObject = cycleObject(set);

    isInstanceOf(deserializedObject, Set.class);
    Object[] entries = deserializedObject.toArray();

    assertTrue(entries[0] instanceof Byte || entries[0] instanceof Short || entries[0] instanceof Integer || entries[0] instanceof String);
    assertTrue(entries[1] instanceof Byte || entries[1] instanceof Short || entries[1] instanceof Integer || entries[1] instanceof String);
    assertTrue(entries[2] instanceof Byte || entries[2] instanceof Short || entries[2] instanceof Integer || entries[2] instanceof String);

  }

}

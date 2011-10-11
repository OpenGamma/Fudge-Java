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
  public void registerSecondaryType() {

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

    for (Object o : deserializedObject) {
      isInstanceOf(o, Currency.class);
    }

  }

  @Test
  public void testSetContainingSeveralCurrencies() throws IOException {

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

    for (Object o : deserializedObject) {
      isInstanceOf(o, Currency.class);
    }

  }

  @Test
  public void testSetContainingSeveralCurrenciesAndNull() throws IOException {

    Set set = new TreeSet<Currency>(new Comparator<Currency>() {
      @Override
      public int compare(Currency o1, Currency o2) {
        if (o1 == null && o2 == null) {
          return 0;
        } else if (o1 == null) {
          return -1;
        } else if (o2 == null) {
          return 1;
        } else {
          return o1.getSymbol().compareTo(o2.getSymbol());
        }
      }
    });
    set.add(Currency.getInstance("USD"));
    set.add(Currency.getInstance("GBP"));
    set.add(null);
    set.add(Currency.getInstance("EUR"));

    Set deserializedObject = cycleObject(set);

    isInstanceOf(deserializedObject, Set.class);

    for (Object o : deserializedObject) {
      assertTrue(o == null || o instanceof Currency);
    }

    Set exemplar = new HashSet();
    exemplar.add(Currency.getInstance("USD"));
    exemplar.add(Currency.getInstance("GBP"));
    exemplar.add(null);
    exemplar.add(Currency.getInstance("EUR"));

    assertTrue(deserializedObject.size() == 4);
    exemplar.removeAll(deserializedObject);
    assertTrue(exemplar.size() == 0);

  }

  @Test
  public void testSetContainingSeveralCurrenciesAndAString() throws IOException {

    Set set = new HashSet();
    set.add(Currency.getInstance("USD"));
    set.add(Currency.getInstance("GBP"));
    set.add(Currency.getInstance("EUR"));
    set.add("Some String");

    Set deserializedObject = cycleObject(set);

    isInstanceOf(deserializedObject, Set.class);

    for (Object o : deserializedObject) {
      assertTrue(o instanceof Byte || o instanceof Short || o instanceof Integer || o instanceof String);
    }

  }

}

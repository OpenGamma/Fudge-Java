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

import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertTrue;


/**
 * Test Map Fudge encoding.
 */

public class MapFudgeEncodingTest extends AbstractFudgeBuilderTestCase {


  @Before
  public void createWriter() {

    getFudgeContext().getTypeDictionary().addType(MockCurrencySecondaryType.INSTANCE, Currency.class);
    getFudgeContext().getTypeDictionary().addTypeConverter(MockCurrencySecondaryType.INSTANCE, Currency.class);
  }

  @Test
  public void testMapWithIntegerValues() {
    Map<String, Integer> map = new HashMap<String, Integer>();
    map.put("A", 1);
    map.put("B", 2);

    Map deserializedObject = cycleObject(map);

    isInstanceOf(deserializedObject, HashMap.class);

    Iterator<Map.Entry> mapIterator = deserializedObject.entrySet().iterator();
    Map.Entry firstEntry = mapIterator.next();
    isInstanceOf(firstEntry.getKey(), String.class);
    isInstanceOf(firstEntry.getValue(), Byte.class);
  }

  @Test
  public void testMapWithCurrencyKeys() {
    Map<Currency, String> map = new HashMap<Currency, String>();

    Currency usd = Currency.getInstance("USD");
    Currency gbp = Currency.getInstance("GBP");
    Currency eur = Currency.getInstance("EUR");

    map.put(usd, "X");
    map.put(gbp, "Y");
    map.put(eur, "Z");

    Map deserializedObject = cycleObject(map);

    isInstanceOf(deserializedObject, HashMap.class);

    Iterator<Map.Entry> mapIterator = deserializedObject.entrySet().iterator();
    Map.Entry firstEntry = mapIterator.next();
    Map.Entry secondEntry = mapIterator.next();
    Map.Entry thirdEntry = mapIterator.next();

    isInstanceOf(firstEntry.getKey(), Currency.class);
    isInstanceOf(firstEntry.getValue(), String.class);

    isInstanceOf(secondEntry.getKey(), Currency.class);
    isInstanceOf(secondEntry.getValue(), String.class);

    isInstanceOf(thirdEntry.getKey(), Currency.class);
    isInstanceOf(thirdEntry.getValue(), String.class);
  }

  @Test
  public void testMapWithCurrencyValues() {
    Map<String, Currency> map = new HashMap<String, Currency>();

    Currency usd = Currency.getInstance("USD");
    Currency gbp = Currency.getInstance("GBP");
    Currency eur = Currency.getInstance("EUR");

    map.put("X", usd);
    map.put("Y", gbp);
    map.put("Z", eur);

    Map deserializedObject = cycleObject(map);

    isInstanceOf(deserializedObject, HashMap.class);

    Iterator<Map.Entry> mapIterator = deserializedObject.entrySet().iterator();
    Map.Entry firstEntry = mapIterator.next();
    Map.Entry secondEntry = mapIterator.next();
    Map.Entry thirdEntry = mapIterator.next();

    isInstanceOf(firstEntry.getKey(), String.class);
    isInstanceOf(firstEntry.getValue(), Currency.class);

    isInstanceOf(secondEntry.getKey(), String.class);
    isInstanceOf(secondEntry.getValue(), Currency.class);

    isInstanceOf(thirdEntry.getKey(), String.class);
    isInstanceOf(thirdEntry.getValue(), Currency.class);


  }

  @Test
  public void testMapWithMixedValues() {
    Map map = new HashMap();

    Currency usd = Currency.getInstance("USD");
    Currency gbp = Currency.getInstance("GBP");
    Currency eur = Currency.getInstance("EUR");

    map.put("X", usd);
    map.put("Y", gbp);
    map.put("Z", "Some text");

    Map deserializedObject = cycleObject(map);

    isInstanceOf(deserializedObject, HashMap.class);

    Iterator<Map.Entry> mapIterator = deserializedObject.entrySet().iterator();
    Map.Entry firstEntry = mapIterator.next();
    Map.Entry secondEntry = mapIterator.next();
    Map.Entry thirdEntry = mapIterator.next();

    isInstanceOf(firstEntry.getKey(), String.class);
    assertTrue(firstEntry.getValue() instanceof Byte || firstEntry.getValue() instanceof Short || firstEntry.getValue() instanceof Integer || firstEntry.getValue() instanceof String);

    isInstanceOf(secondEntry.getKey(), String.class);
    assertTrue(secondEntry.getValue() instanceof Byte || secondEntry.getValue() instanceof Short || secondEntry.getValue() instanceof Integer || secondEntry.getValue() instanceof String);

    isInstanceOf(thirdEntry.getKey(), String.class);
    assertTrue(thirdEntry.getValue() instanceof Byte || thirdEntry.getValue() instanceof Short || thirdEntry.getValue() instanceof Integer || thirdEntry.getValue() instanceof String);
  }

}

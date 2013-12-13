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

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.fudgemsg.AbstractFudgeBuilderTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Map Fudge encoding.
 */
public class MapFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  @Before
  public void registerSecondaryType() {

    getFudgeContext().getTypeDictionary().addType(MockCurrencySecondaryType.INSTANCE, Currency.class);
    getFudgeContext().getTypeDictionary().addTypeConverter(MockCurrencySecondaryType.INSTANCE, Currency.class);
  }

  @Test
  public void testMapWithIntegerValues() {
    Map<String, Byte> map = new HashMap<>();
    map.put("A", (byte) 1);
    map.put("B", (byte) 2);

    Map<String, Byte> deserializedObject = cycleObject(map);

    isInstanceOf(deserializedObject, HashMap.class);

    for (Object o : deserializedObject.keySet()) {
      isInstanceOf(o, String.class);
    }

    isInstanceOf(deserializedObject.get("A"), Byte.class);
    isInstanceOf(deserializedObject.get("B"), Byte.class);
  }

  @Test
  public void testMapWithIntegerValues2() {
    Map map = new HashMap();
    map.put("A", (byte) 1);
    map.put("B", (byte) 2);

    Map<String, Integer> deserializedObject = cycleObject(map);

    isInstanceOf(deserializedObject, HashMap.class);

    for (Object o : deserializedObject.keySet()) {
      isInstanceOf(o, String.class);
    }

    isInstanceOf(deserializedObject.get("A"), Byte.class);
    isInstanceOf(deserializedObject.get("B"), Byte.class);
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

    Map<Currency, String> deserializedObject = cycleObject(map);

    isInstanceOf(deserializedObject, HashMap.class);

    for (Object o : deserializedObject.keySet()) {
      isInstanceOf(o, Currency.class);
    }

    for (Object o : deserializedObject.values()) {
      isInstanceOf(o, String.class);
    }
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

    Map<String, Currency> deserializedObject = cycleObject(map);

    isInstanceOf(deserializedObject, HashMap.class);

    for (Object o : deserializedObject.keySet()) {
      isInstanceOf(o, String.class);
    }

    for (Object o : deserializedObject.values()) {
      isInstanceOf(o, Currency.class);
    }

  }

  @Test
  public void testMapWithCurrencyValuesAndNulls() {
    Map<String, Currency> map = new HashMap<String, Currency>();

    Currency usd = Currency.getInstance("USD");
    Currency gbp = Currency.getInstance("GBP");
    Currency eur = Currency.getInstance("EUR");

    map.put("X", usd);
    map.put("Y", gbp);
    map.put("U", null);
    map.put("W", null);
    map.put("Z", eur);

    Map<String, Currency> deserializedObject = cycleObject(map);

    isInstanceOf(deserializedObject, HashMap.class);

    for (Object o : deserializedObject.keySet()) {
      isInstanceOf(o, String.class);
    }

    isInstanceOf(deserializedObject.get("X"), Currency.class);
    isInstanceOf(deserializedObject.get("Y"), Currency.class);
    isInstanceOf(deserializedObject.get("Z"), Currency.class);

    assertNull(deserializedObject.get("U"));
    assertNull(deserializedObject.get("W"));

    assertTrue(deserializedObject.containsKey("U"));
    assertTrue(deserializedObject.containsKey("W"));

    assertTrue(deserializedObject.size() == 5);
  }

  @Test
  public void testMapWithMixedValues() {
    Map<String, Serializable> map = new HashMap<String, Serializable>();

    Currency usd = Currency.getInstance("USD");
    Currency gbp = Currency.getInstance("GBP");
    Currency eur = Currency.getInstance("EUR");

    map.put("X", usd);
    map.put("Y", gbp);
    map.put("Z", eur);
    map.put("T", "Some text");

    Map<String, Serializable> deserializedObject = cycleObject(map);

    for (Object o : deserializedObject.keySet()) {
      isInstanceOf(o, String.class);
    }

    isInstanceOf(deserializedObject, HashMap.class);

    isInstanceOf(deserializedObject.get("X"), Byte.class);
    isInstanceOf(deserializedObject.get("Y"), Short.class);
    isInstanceOf(deserializedObject.get("Z"), Integer.class);
    isInstanceOf(deserializedObject.get("T"), String.class);
  }

  @Test
  public void testEmptyMap() {
    Map<String, String> map = new HashMap<String, String>();

    Object deserializedObject = cycleObject(map);

    isInstanceOf(deserializedObject, HashMap.class);
  }

  @Test
  public void testTypesHierarchy() {
    Map<Integer, HashSet<String>> map = new HashMap<Integer, HashSet<String>>();

    map.put(1, new HashSet<String>());
    map.put(2, new HashSet<String>());

    Object deserializedObject = cycleObject(map);

    isInstanceOf(deserializedObject, HashMap.class);
  }
}

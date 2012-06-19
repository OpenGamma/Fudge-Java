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
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertNull;


/**
 * Test List Fudge encoding.
 */

public class ListFudgeEncodingTest extends AbstractFudgeBuilderTestCase {


  @Before
  public void registerSecondaryType() {
    getFudgeContext().getTypeDictionary().addType(MockCurrencySecondaryType.INSTANCE, Currency.class);
    getFudgeContext().getTypeDictionary().addTypeConverter(MockCurrencySecondaryType.INSTANCE, Currency.class);
  }

  @Test
  public void testListContainingSingleCurrency() throws IOException {

    List list = new LinkedList();
    list.add(Currency.getInstance("USD"));

    List deserializedObject = cycleObject(list);

    isInstanceOf(deserializedObject, List.class);
    isInstanceOf(deserializedObject.get(0), Currency.class);

  }

  @Test
  public void testListContainingSeveralCurrencies() throws IOException {

    List list = new LinkedList();
    list.add(Currency.getInstance("USD"));
    list.add(Currency.getInstance("GBP"));
    list.add(Currency.getInstance("EUR"));

    List deserializedObject = cycleObject(list);

    isInstanceOf(deserializedObject, List.class);
    isInstanceOf(deserializedObject.get(0), Currency.class);
    isInstanceOf(deserializedObject.get(1), Currency.class);
    isInstanceOf(deserializedObject.get(2), Currency.class);
  }

  @Test
  public void testListContainingSeveralCurrenciesAndNulls() throws IOException {

    List list = new LinkedList();
    list.add(Currency.getInstance("USD"));
    list.add(Currency.getInstance("GBP"));
    list.add(null);
    list.add(null);
    list.add(Currency.getInstance("EUR"));

    List deserializedObject = cycleObject(list);

    isInstanceOf(deserializedObject, List.class);
    isInstanceOf(deserializedObject.get(0), Currency.class);
    isInstanceOf(deserializedObject.get(1), Currency.class);
    assertNull(deserializedObject.get(2));
    assertNull(deserializedObject.get(3));
    isInstanceOf(deserializedObject.get(4), Currency.class);
  }

  @Test
  public void testListContainingSeveralCurrenciesAndAString() throws IOException {

    List list = new LinkedList();
    list.add(Currency.getInstance("USD"));
    list.add(Currency.getInstance("GBP"));
    list.add(Currency.getInstance("EUR"));
    list.add("Some String");

    List deserializedObject = cycleObject(list);

    isInstanceOf(deserializedObject, ArrayList.class);
    isInstanceOf(deserializedObject.get(0), Byte.class);
    isInstanceOf(deserializedObject.get(1), Short.class);
    isInstanceOf(deserializedObject.get(2), Integer.class);
    isInstanceOf(deserializedObject.get(3), String.class);
  }

  @Test
  public void testEmptySet() {
    List list = new LinkedList();

    Object deserializedObject = cycleObject(list);

    isInstanceOf(deserializedObject, ArrayList.class);
  }


}

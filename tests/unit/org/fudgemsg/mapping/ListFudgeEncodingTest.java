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
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;


/**
 * Test List Fudge encoding.
 */

public class ListFudgeEncodingTest extends AbstractFudgeBuilderTestCase {


  @Before
  public void createWriter() {
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
  public void testListContainingSeveralCurrency() throws IOException {

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
  public void testListContainingSeveralCurrencyAndAString() throws IOException {

    List list = new LinkedList();
    list.add(Currency.getInstance("USD"));
    list.add(Currency.getInstance("GBP"));
    list.add(Currency.getInstance("EUR"));
    list.add("Some String");

    List deserializedObject = cycleObject(list);

    isInstanceOf(deserializedObject, List.class);
    isInstanceOf(deserializedObject.get(0), Byte.class);
    isInstanceOf(deserializedObject.get(1), Short.class);
    isInstanceOf(deserializedObject.get(2), Integer.class);
    isInstanceOf(deserializedObject.get(3), String.class);
  }

}

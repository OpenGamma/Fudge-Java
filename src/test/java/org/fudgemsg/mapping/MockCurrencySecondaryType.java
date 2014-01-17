/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and other contributors.
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

import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import java.util.Currency;

/**
 * Mock to convert Currency to a number.
 */
public class MockCurrencySecondaryType extends SecondaryFieldType<Currency, Number> {

  /**
   * Singleton instance of the type.
   */
  public static final MockCurrencySecondaryType INSTANCE = new MockCurrencySecondaryType();

  /**
   * Restricted constructor.
   */
  private MockCurrencySecondaryType() {
    super(FudgeWireType.INT, Currency.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer secondaryToPrimary(final Currency object) {
    if (object.getCurrencyCode().equals("USD")) {
      return 1;
    }
    if (object.getCurrencyCode().equals("GBP")) {
      return 200;
    }
    if (object.getCurrencyCode().equals("EUR")) {
      return 300000;
    }
    return -100000;
  }

  @Override
  public Currency primaryToSecondary(final Number object) {
    switch (object.intValue()) {
      case 1: return Currency.getInstance("USD");
      case 200: return Currency.getInstance("GBP");
      case 300000: return Currency.getInstance("EUR");
    }
    return null;
  }

}

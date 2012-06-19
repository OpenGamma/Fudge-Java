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

package org.fudgemsg.wire;

import java.util.Currency;

import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Mock to convert Currency to a number.
 */
public class MockIntegerSecondaryType extends SecondaryFieldType<Currency, Integer> {

  /**
   * Singleton instance of the type.
   */
  public static final MockIntegerSecondaryType INSTANCE = new MockIntegerSecondaryType();

  /**
   * Restricted constructor.
   */
  private MockIntegerSecondaryType() {
    super(FudgeWireType.INT, Currency.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer secondaryToPrimary(final Currency object) {
    if (object.getCurrencyCode().equals("USD")) {
      return 0;
    }
    if (object.getCurrencyCode().equals("GBP")) {
      return 1;
    }
    if (object.getCurrencyCode().equals("EUR")) {
      return 2000000000;
    }
    return -1;
  }

  @Override
  public Currency primaryToSecondary(final Integer object) {
    switch (object.intValue()) {
      case 0: return Currency.getInstance("USD");
      case 1: return Currency.getInstance("GBP");
      case 2000000000: return Currency.getInstance("EUR");
    }
    return null;
  }

}

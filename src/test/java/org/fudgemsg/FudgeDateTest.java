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

import org.fudgemsg.types.FudgeDate;
import org.junit.Test;
import org.threeten.bp.LocalDate;

/**
 * Tests the FudgeDate class.
 */
public class FudgeDateTest {

  @Test
  public void test_ofYear() {
    FudgeDate test = FudgeDate.ofYear(2012);
    assertEquals(2012, test.getYear());
    assertEquals(0, test.getMonthOfYear());
    assertEquals(0, test.getDayOfMonth());
  }

  @Test
  public void test_ofYearMonth() {
    FudgeDate test = FudgeDate.ofYearMonth(2012, 6);
    assertEquals(2012, test.getYear());
    assertEquals(6, test.getMonthOfYear());
    assertEquals(0, test.getDayOfMonth());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_of() {
    FudgeDate test = FudgeDate.of(2012, 6, 30);
    assertEquals(2012, test.getYear());
    assertEquals(6, test.getMonthOfYear());
    assertEquals(30, test.getDayOfMonth());
  }

  @Test
  public void test_of_BCE() {
    FudgeDate test = FudgeDate.of(-3, 6, 30);
    assertEquals(-3, test.getYear());
    assertEquals(6, test.getMonthOfYear());
    assertEquals(30, test.getDayOfMonth());
  }

  @Test
  public void test_of_monthDay0() {
    FudgeDate test = FudgeDate.of(2012, 0, 0);
    assertEquals(2012, test.getYear());
    assertEquals(0, test.getMonthOfYear());
    assertEquals(0, test.getDayOfMonth());
  }

  @Test
  public void test_of_day0() {
    FudgeDate test = FudgeDate.of(2012, 6, 0);
    assertEquals(2012, test.getYear());
    assertEquals(6, test.getMonthOfYear());
    assertEquals(0, test.getDayOfMonth());
  }

  @Test(expected=RuntimeException.class)
  public void test_of_year0() {
    FudgeDate.of(0, 1, 30);
  }

  @Test(expected=RuntimeException.class)
  public void test_of_yearTooLarge() {
    FudgeDate.of(FudgeDate.MAX_YEAR + 1, 1, 30);
  }

  @Test(expected=RuntimeException.class)
  public void test_of_yearTooSmall() {
    FudgeDate.of(FudgeDate.MIN_YEAR - 1, 1, 30);
  }

  @Test(expected=RuntimeException.class)
  public void test_of_monthNegative() {
    FudgeDate.of(2012, -1, 30);
  }

  @Test(expected=RuntimeException.class)
  public void test_of_month0() {
    FudgeDate.of(2012, 0, 30);
  }

  @Test(expected=RuntimeException.class)
  public void test_of_month13() {
    FudgeDate.of(2012, 13, 30);
  }

  @Test(expected=RuntimeException.class)
  public void test_of_month14() {
    FudgeDate.of(2012, 14, 30);
  }

  @Test(expected=RuntimeException.class)
  public void test_of_month15() {
    FudgeDate.of(2012, 15, 30);
  }

  @Test(expected=RuntimeException.class)
  public void test_of_dayNegative() {
    FudgeDate.of(2012, 6, -1);
  }

  @Test(expected=RuntimeException.class)
  public void test_of_day32() {
    FudgeDate.of(2012, 6, 32);
  }

  @Test(expected=RuntimeException.class)
  public void test_of_dayInvalidForMonth() {
    FudgeDate.of(2012, 6, 31);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_from_LocalDate() {
    FudgeDate test = FudgeDate.from(LocalDate.of(2012, 6, 30));
    assertEquals(2012, test.getYear());
    assertEquals(6, test.getMonthOfYear());
    assertEquals(30, test.getDayOfMonth());
    assertEquals(LocalDate.of(2012, 6, 30), test.toLocalDate());
  }

  @Test
  public void test_from_LocalDateBCE() {
    FudgeDate test = FudgeDate.from(LocalDate.of(-2, 6, 30));
    assertEquals(-3, test.getYear());
    assertEquals(6, test.getMonthOfYear());
    assertEquals(30, test.getDayOfMonth());
    assertEquals(LocalDate.of(-2, 6, 30), test.toLocalDate());
  }

  @Test
  public void test_from_LocalDateMAX() {
    FudgeDate test = FudgeDate.from(LocalDate.MAX);
    assertEquals(FudgeDate.MAX_YEAR, test.getYear());
    assertEquals(15, test.getMonthOfYear());
    assertEquals(31, test.getDayOfMonth());
    assertEquals(LocalDate.MAX, test.toLocalDate());
  }

  @Test
  public void test_from_LocalDateMIN() {
    FudgeDate test = FudgeDate.from(LocalDate.MIN);
    assertEquals(FudgeDate.MIN_YEAR, test.getYear());
    assertEquals(15, test.getMonthOfYear());
    assertEquals(31, test.getDayOfMonth());
    assertEquals(LocalDate.MIN, test.toLocalDate());
  }

}

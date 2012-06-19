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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.fudgemsg.wire.types.FudgeWireType;
import org.junit.Test;

/**
 * Test FudgeMsgField.
 */
public class FudgeMsgFieldTest {

  @Test
  public void test_of_checked() {
    FudgeField field = UnmodifiableFudgeField.of(FudgeWireType.BOOLEAN, Boolean.TRUE);
    UnmodifiableFudgeField test = UnmodifiableFudgeField.of(field);
    assertSame(test, field);
    assertEquals(true, test.equals(field));
    assertEquals(true, field.equals(test));
  }

  @Test
  public void test_of_created() {
    FudgeField expected = UnmodifiableFudgeField.of(FudgeWireType.INT, 9, "number", 12);
    FudgeField field = new FudgeField() {
      @Override
      public FudgeWireType getType() {
        return FudgeWireType.INT;
      }
      @Override
      public Object getValue() {
        return 9;
      }
      @Override
      public String getName() {
        return "number";
      }
      @Override
      public Integer getOrdinal() {
        return 12;
      }
    };
    UnmodifiableFudgeField test = UnmodifiableFudgeField.of(field);
    assertNotSame(test, field);
    assertEquals(true, test.equals(expected));
    assertEquals(true, expected.equals(test));
  }

  @Test
  public void test_noNameNoOrdinal() {
    UnmodifiableFudgeField field = UnmodifiableFudgeField.of(FudgeWireType.BOOLEAN, Boolean.TRUE);
    assertEquals(FudgeWireType.BOOLEAN, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals(null, field.getName());
    assertEquals(null, field.getOrdinal());
    assertEquals("Field[FudgeFieldType[1-boolean]-true]", field.toString());
  }

  @Test
  public void test_ordinal() {
    UnmodifiableFudgeField field = UnmodifiableFudgeField.of(FudgeWireType.BOOLEAN, Boolean.TRUE, 8);
    assertEquals(FudgeWireType.BOOLEAN, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals(null, field.getName());
    assertEquals((Integer) 8, field.getOrdinal());
    assertEquals("Field[8:FudgeFieldType[1-boolean]-true]", field.toString());
  }

  @Test
  public void test_name() {
    UnmodifiableFudgeField field = UnmodifiableFudgeField.of(FudgeWireType.BOOLEAN, Boolean.TRUE, "flag");
    assertEquals(FudgeWireType.BOOLEAN, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("flag", field.getName());
    assertEquals(null, field.getOrdinal());
    assertEquals("Field[flag:FudgeFieldType[1-boolean]-true]", field.toString());
  }

  @Test
  public void test_nameOrdinal() {
    UnmodifiableFudgeField field = UnmodifiableFudgeField.of(FudgeWireType.BOOLEAN, Boolean.TRUE, "flag", 8);
    assertEquals(FudgeWireType.BOOLEAN, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("flag", field.getName());
    assertEquals((Integer) 8, field.getOrdinal());
    assertEquals("Field[flag,8:FudgeFieldType[1-boolean]-true]", field.toString());
  }

}

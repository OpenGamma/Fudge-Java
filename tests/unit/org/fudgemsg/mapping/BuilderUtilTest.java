/*
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

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Test.
 */
public class BuilderUtilTest {

  @Test
  public void test_getAllSuperclasses_Mock1() {
    List<Class<?>> list = BuilderUtil.getAllSuperclasses(Mock1.class);
    assertEquals(1, list.size());
    assertEquals(Object.class, list.get(0));
  }

  @Test
  public void test_getAllSuperclasses_Mock2() {
    List<Class<?>> list = BuilderUtil.getAllSuperclasses(Mock2.class);
    assertEquals(2, list.size());
    assertEquals(Mock1.class, list.get(0));
    assertEquals(Object.class, list.get(1));
  }

  @Test
  public void test_getAllInterfaces_Mock1() {
    Set<Class<?>> set = BuilderUtil.getAllInterfaces(Mock1.class);
    assertEquals(4, set.size());
    Iterator<Class<?>> it = set.iterator();
    assertEquals(E.class, it.next());
    assertEquals(A.class, it.next());
    assertEquals(B.class, it.next());
    assertEquals(G.class, it.next());
  }

  @Test
  public void test_getAllInterfaces_Mock2() {
    Set<Class<?>> set = BuilderUtil.getAllInterfaces(Mock2.class);
    assertEquals(7, set.size());
    Iterator<Class<?>> it = set.iterator();
    assertEquals(F.class, it.next());
    assertEquals(E.class, it.next());
    assertEquals(A.class, it.next());
    assertEquals(B.class, it.next());
    assertEquals(D.class, it.next());
    assertEquals(C.class, it.next());
    assertEquals(G.class, it.next());
  }

  private static interface A {
  }

  private static interface B {
  }

  private static interface C {
  }

  private static interface D extends C {
  }

  private static interface E extends A, B {
  }

  private static interface F extends E, D {
  }

  private static interface G {
  }

  private static class Mock1 implements E, G {
  }

  private static class Mock2 extends Mock1 implements F {
  }

}

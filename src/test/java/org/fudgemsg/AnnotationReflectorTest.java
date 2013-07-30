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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.rules.TestWatchman;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * Test {@code AnnotationReflector}.
 */
@SuppressWarnings("deprecation")
public class AnnotationReflectorTest {

  @Test
  public void testDefault() throws Exception {
    AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
    Set<Class<?>> types = reflector.getReflector().getTypesAnnotatedWith(MockAnnotation.class);
    assertEquals(1, types.size());
    assertTrue(types.contains(MockWithTypeAnnotation.class));
    Set<Field> fields = reflector.getReflector().getFieldsAnnotatedWith(MockAnnotation.class);
    assertEquals(1, fields.size());
    assertTrue(fields.contains(MockWithFieldAnnotation.class.getDeclaredField("_field")));
  }

  @Test
  public void testDefaultNotSetTwice() {
    AnnotationReflector.getDefaultReflector();
    AnnotationReflector reflector = new AnnotationReflector(new ConfigurationBuilder());
    try {
      AnnotationReflector.initDefaultReflector(reflector);
      fail();
    } catch (FudgeRuntimeException ex) {
      // expected
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void testBasicConstructor() {
    ConfigurationBuilder config = new ConfigurationBuilder();
    config.addScanners(new TypeAnnotationsScanner());
    config.addUrls(ClasspathHelper.forJavaClassPath());
    AnnotationReflector reflector = new AnnotationReflector(config);
    Set<Class<?>> types = reflector.getReflector().getTypesAnnotatedWith(MockAnnotation.class);
    assertEquals(1, types.size());
    Set<Field> fields = reflector.getReflector().getFieldsAnnotatedWith(MockAnnotation.class);
    assertEquals(0, fields.size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAdvancedConstructor_nulls() {
    AnnotationReflector reflector = new AnnotationReflector(null, null);
    Set<Class<?>> types = reflector.getReflector().getTypesAnnotatedWith(MockAnnotation.class);
    assertEquals(1, types.size());
    Set<Field> fields = reflector.getReflector().getFieldsAnnotatedWith(MockAnnotation.class);
    assertEquals(1, fields.size());
    Set<Class<?>> deprecated = reflector.getReflector().getTypesAnnotatedWith(Deprecated.class);
    assertFalse(deprecated.contains(TestWatchman.class));
  }

  @Test
  public void testAdvancedConstructor_null_scanner() {
    AnnotationReflector reflector = new AnnotationReflector(null, null, new TypeAnnotationsScanner());
    Set<Class<?>> types = reflector.getReflector().getTypesAnnotatedWith(MockAnnotation.class);
    assertEquals(1, types.size());
    Set<Field> fields = reflector.getReflector().getFieldsAnnotatedWith(MockAnnotation.class);
    assertEquals(0, fields.size());
  }

  @Test
  public void testAdvancedConstructor_noUrls() throws Exception {
    File baseDir = new File(System.getProperty("java.io.tmpdir"));
    Set<URL> urls = new HashSet<URL>();
    urls.add(baseDir.toURI().toURL());
    AnnotationReflector reflector = new AnnotationReflector(null, urls);
    Set<Class<?>> types = reflector.getReflector().getTypesAnnotatedWith(MockAnnotation.class);
    assertEquals(0, types.size());
    Set<Field> fields = reflector.getReflector().getFieldsAnnotatedWith(MockAnnotation.class);
    assertEquals(0, fields.size());
  }

  @Test
  public void testAdvancedConstructor_filterIn() {
    AnnotationReflector reflector = new AnnotationReflector("+org.fudgemsg", null);
    Set<Class<?>> types = reflector.getReflector().getTypesAnnotatedWith(MockAnnotation.class);
    assertEquals(1, types.size());
    Set<Field> fields = reflector.getReflector().getFieldsAnnotatedWith(MockAnnotation.class);
    assertEquals(1, fields.size());
    Set<Class<?>> deprecated = reflector.getReflector().getTypesAnnotatedWith(Deprecated.class);
    assertFalse(deprecated.contains(TestWatchman.class));
    
  }

  @Test
  public void testAdvancedConstructor_filterOut() {
    AnnotationReflector reflector = new AnnotationReflector("-org.fudgemsg", null);
    Set<Class<?>> types = reflector.getReflector().getTypesAnnotatedWith(MockAnnotation.class);
    assertEquals(0, types.size());
    Set<Field> fields = reflector.getReflector().getFieldsAnnotatedWith(MockAnnotation.class);
    assertEquals(0, fields.size());
    Set<Class<?>> deprecated = reflector.getReflector().getTypesAnnotatedWith(Deprecated.class);
    assertTrue(deprecated.contains(TestWatchman.class));
  }

}

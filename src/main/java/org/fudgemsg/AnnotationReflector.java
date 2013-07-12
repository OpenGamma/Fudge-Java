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

import java.net.URL;
import java.util.Set;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * Wraps a tool that can scan annotations in classes.
 */
public class AnnotationReflector {

  /**
   * The default annotation reflector.
   */
  private static AnnotationReflector s_defaultReflector;
  /**
   * The default set of packages to exclude.
   */
  private static final String DEFAULT_ANNOTATION_REFLECTOR_FILTER =
      "-java., " +
      "-javax., " +
      "-sun., " +
      "-sunw., " +
      "-com.sun., " +
      "-org.springframework., " +
      "-org.eclipse., " +
      "-org.apache., " +
      "-org.antlr., " +
      "-org.hibernate., " +
      "-org.fudgemsg., " +
      "-org.threeten., " +
      "-org.reflections., " +
      "-org.joda., " +
      "-cern.clhep., " +
      "-cern.colt., " +
      "-cern.jet.math., " +
      "-ch.qos.logback., " +
      "-com.codahale.metrics., " +
      "-com.mongodb., " +
      "-com.sleepycat., " +
      "-com.yahoo.platform.yui., " +
      "-de.odysseus.el., " +
      "-freemarker., " +
      "-groovy., " +
      "-groovyjar, " +
      "-it.unimi.dsi.fastutil., " +
      "-jargs.gnu., " +
      "-javassist., " +
      "-jsr166y., " +
      "-net.sf.ehcache., " +
      "-org.bson., " +
      "-org.codehaus.groovy., " +
      "-org.cometd., " +
      "-com.google.common., " +
      "-org.hsqldb., " +
      "-com.jolbox., " +
      "-edu.emory.mathcs., " +
      "-info.ganglia., " +
      "-org.aopalliance., " +
      "-org.dom4j., " +
      "-org.mozilla.javascript., " +
      "-org.mozilla.classfile., " +
      "-org.objectweb.asm., " +
      "-org.osgi., " +
      "-org.postgresql., " +
      "-org.quartz., " +
      "-org.slf4j., " +
      "-org.w3c.dom, " +
      "-org.xml.sax., " +
      "-org.jcsp., " +
      "-org.json., " +
      "-redis.";

  /**
   * The annotation reflector.
   */
  private Reflections _reflections;

  //-------------------------------------------------------------------------
  /**
   * Gets the annotation reflector.
   * <p>
   * This is used to find annotations.
   * It will be automatically initialized if necessary.
   * 
   * @return the taxonomy resolver, not null
   */
  public static synchronized AnnotationReflector getDefaultReflector() {
    if (s_defaultReflector == null) {
      initDefaultReflector(null, null);
    }
    return s_defaultReflector;
  }

  /**
   * Initializes the annotation reflector.
   * <p>
   * This is used to find annotations.
   * 
   * @param filter  a filter string, such as '+com.foobar' or '-org.springframework',
   *  null uses a default excluding many common open source libraries
   * @param urlsToScan  the URLs to scan, null defaults to the class loader based classpath
   * @param scanners  the scanners to use, null or empty uses the default set
   */
  public static synchronized void initDefaultReflector(String filter, Set<URL> urlsToScan, Scanner... scanners) {
    if (s_defaultReflector != null) {
      throw new FudgeRuntimeException("Annotation reflector has already been initialized");
    }
    s_defaultReflector = new AnnotationReflector(filter, urlsToScan, scanners);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a new reflector.
   * <p>
   * This class wraps the underlying reflector.
   * 
   * @param filter  a filter string, such as '+com.foobar' or '-org.springframework',
   *  null uses a default excluding many common open source libraries
   * @param urlsToScan  the URLs to scan, null defaults to the class loader based classpath
   * @param scanners  the scanners to use, null or empty uses the default set
   */
  public AnnotationReflector(String filter, Set<URL> urlsToScan, Scanner... scanners) {
    if (filter == null) {
      filter = DEFAULT_ANNOTATION_REFLECTOR_FILTER;
    }
    if (urlsToScan == null) {
      urlsToScan = ClasspathHelper.forManifest();
    }
    if (scanners == null || scanners.length == 0) {
      scanners = new Scanner[] {new TypeAnnotationsScanner(), new FieldAnnotationsScanner()};
    }
    Configuration config = new ConfigurationBuilder()
      .setUrls(urlsToScan)
      .setScanners(scanners)
      .filterInputsBy(FilterBuilder.parse(filter))
      .useParallelExecutor();
    _reflections = new Reflections(config);
  }

  /**
   * Constructs a new reflector.
   * <p>
   * This class wraps the underlying reflector.
   * 
   * @param config  the configuration to use, not null
   */
  public AnnotationReflector(Configuration config) {
    _reflections = new Reflections(config);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying reflector.
   * 
   * @return the reflector, not null
   */
  public Reflections getReflector() {
    return _reflections;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return _reflections.toString();
  }

}

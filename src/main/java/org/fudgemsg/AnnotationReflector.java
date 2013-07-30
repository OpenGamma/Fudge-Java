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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.reflections.util.FilterBuilder.Exclude;
import org.reflections.util.FilterBuilder.Include;
import org.reflections.util.Utils;

import com.google.common.base.Predicate;

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
  public static final String DEFAULT_ANNOTATION_REFLECTOR_FILTER =
      "-java, " +
      "-javax, " +
      "-sun, " +
      "-sunw, " +
      "-com.sun, " +
      "-org.springframework, " +
      "-org.eclipse, " +
      "-org.apache, " +
      "-org.antlr, " +
      "-org.hibernate, " +
      "-org.threeten, " +
      "-org.reflections, " +
      "-org.joda, " +
      "-cern.clhep, " +
      "-cern.colt, " +
      "-cern.jet.math, " +
      "-ch.qos.logback, " +
      "-com.codahale.metrics, " +
      "-com.mongodb, " +
      "-com.sleepycat, " +
      "-com.yahoo.platform.yui, " +
      "-de.odysseus.el, " +
      "-freemarker, " +
      "-groovy, " +
      "-groovyjar*, " +
      "-it.unimi.dsi.fastutil, " +
      "-jargs.gnu, " +
      "-javassist, " +
      "-jsr166y, " +
      "-net.sf.ehcache, " +
      "-org.bson, " +
      "-org.codehaus.groovy, " +
      "-org.cometd, " +
      "-com.google.common, " +
      "-org.hsqldb, " +
      "-com.jolbox, " +
      "-edu.emory.mathcs, " +
      "-info.ganglia, " +
      "-org.aopalliance, " +
      "-org.dom4j, " +
      "-org.junit, " +
      "-org.mozilla.javascript, " +
      "-org.mozilla.classfile, " +
      "-org.objectweb.asm, " +
      "-org.osgi, " +
      "-org.postgresql, " +
      "-org.quartz, " +
      "-org.slf4j, " +
      "-org.testng, " +
      "-org.w3c.dom*, " +
      "-org.xml.sax, " +
      "-org.jcsp, " +
      "-org.json, " +
      "-redis";

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
      initDefaultReflector(new AnnotationReflector(null, null));
    }
    return s_defaultReflector;
  }

  /**
   * Initializes the annotation reflector.
   * <p>
   * This is used to find annotations.
   * 
   * @param reflector  the reflector, not null
   */
  public static synchronized void initDefaultReflector(AnnotationReflector reflector) {
    if (s_defaultReflector != null) {
      throw new FudgeRuntimeException("Annotation reflector has already been initialized");
    }
    s_defaultReflector = reflector;
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a new reflector.
   * <p>
   * This class wraps the underlying reflector.
   * <p>
   * The last argument is passed to {@link ConfigurationBuilder#build(Object...)}.
   * It typically consists of {@link Scanner} and {@link ClassLoader} instances.
   * If no scanners are specified, the type and field scanners are added.
   * If the filter is null, a default filter is added excluding common OSS projects.
   * If the URL set is null, the Java class path is used.
   * 
   * @param filter  a filter string, such as '+com.foobar' or '-org.springframework',
   *  null uses a default excluding many common open source libraries,
   *  an empty string relies on filtering being added in the Object array
   * @param urlsToScan  the URLs to scan, null defaults to the java class path
   * @param configurationObjects  the configuration objects to use, not null
   */
  public AnnotationReflector(String filter, Set<URL> urlsToScan, Object... configurationObjects) {
    List<Object> objects = new ArrayList<>(Arrays.asList(configurationObjects));
    // null filter uses our default, empty uses no filter
    if (filter == null) {
      filter = DEFAULT_ANNOTATION_REFLECTOR_FILTER;
    }
    if (filter.length() > 0) {
      objects.add(parsePackages(filter));
    }
    // null URLs uses our default
    if (urlsToScan == null) {
      urlsToScan = ClasspathHelper.forManifest(ClasspathHelper.forJavaClassPath());
    }
    objects.addAll(urlsToScan);
    // no scanners uses our default
    boolean found = false;
    for (Object object : objects) {
      if (object instanceof Scanner) {
        found = true;
        break;
      }
    }
    if (found == false) {
      objects.add(new TypeAnnotationsScanner());
      objects.add(new FieldAnnotationsScanner());
    }
    // create parallel builder
    Object[] objectArray = (Object[]) objects.toArray(new Object[objects.size()]);
    ConfigurationBuilder builder = ConfigurationBuilder.build(objectArray);
    builder.useParallelExecutor();
    _reflections = builder.build();
  }

  /**
   * Parses a string representation of an include/exclude filter.
   * <p>
   * The given includeExcludeString is a comma separated list of package name segments,
   * each starting with either + or - to indicate include/exclude.
   * <p>
   * For example parsePackages("-java, -javax, -sun, -com.sun") or parse("+com.myn,-com.myn.excluded").
   * Note that "-java" will block "java.foo" but not "javax.foo".
   * <p>
   * The input strings "-java" and "-java." are equivalent.
   */
  private static FilterBuilder parsePackages(String includeExcludeString) {
    // copy of pull request  #5 to Reflections project
    FilterBuilder builder = new FilterBuilder();
    if (!Utils.isEmpty(includeExcludeString)) {
      for (String string : includeExcludeString.split(",")) {
        String trimmed = string.trim();
        char prefix = trimmed.charAt(0);
        String pattern = trimmed.substring(1);
        if (pattern.endsWith(".") == false) {
          pattern += ".";
        }
        pattern = FilterBuilder.prefix(pattern);

        Predicate<String> filter;
        switch (prefix) {
        case '+':
          filter = new Include(pattern);
          break;
        case '-':
          filter = new Exclude(pattern);
          break;
        default:
          throw new ReflectionsException(
              "includeExclude should start with either + or -");
        }
        builder.add(filter);
      }
    }
    return builder;
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

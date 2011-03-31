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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify that this class has an associated Fudge builder.
 * <p>
 * This annotation, or {@link FudgeBuilderFor}, can be used to connect a class with
 * the associated builder. This is used on the class itself and refers to the builders.
 * All parameters are optional, having default values of {@code Object} due to
 * limitations of enums.
 * <p>
 * The Fudge system can, if desired, locate this annotation and automatically
 * configure using {@link FudgeObjectDictionary#addAllAnnotatedBuilders()}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface HasFudgeBuilder {

  /**
   * Defines the builder to be used for this class.
   * The class should implement both {@link FudgeMessageBuilder} and {@link FudgeObjectBuilder}
   * for the annotated type.
   * If specified, the other two enum values should be omitted.
   */
  Class<?> builder() default Object.class;

  /**
  * Defines the builder to be used for this class.
   * The class should implement {@link FudgeObjectBuilder} for the annotated type.
   * If specified, the other enum {@code builder} value should be omitted.
   */
  Class<?> objectBuilder() default Object.class;

  /**
  * Defines the builder to be used for this class.
   * The class should implement {@link FudgeMessageBuilder} for the annotated type.
   * If specified, the other enum {@code builder} value should be omitted.
   */
  Class<?> messageBuilder() default Object.class;

}

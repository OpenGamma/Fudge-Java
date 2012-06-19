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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Annotation used to specify the Fudge field ordinal from a POJO.
 * <p>
 * When an object is converted to a Fudge message using reflection, this annotation
 * is used to provide a specific field ordinal in corresponding Fudge messages.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FudgeFieldOrdinal {

  /**
   * Specifies the ordinal of the field within a Fudge message.
   * By default, ordinals are only written when derived from a taxonomy based on the field names.
   */
  short value();

  /**
   * Indicates that the field name must be omitted from the message.
   */
  boolean noFieldName() default false;

}
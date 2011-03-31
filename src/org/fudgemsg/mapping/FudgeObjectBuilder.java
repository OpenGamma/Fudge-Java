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

import org.fudgemsg.FudgeFieldContainer;

/**
 * Defines how to convert a Fudge message to an object.
 * <p>
 * This interface, and {@link FudgeMessageBuilder}, define how to convert between
 * an object and a Fudge message. The two interfaces are combined in {@link FudgeBuilder}.
 * <p>
 * This interface must be implemented in a thread-safe manner.
 * 
 * @param <T> the Java type this builder creates
 */
public interface FudgeObjectBuilder<T> {

  /**
   * Converts the Fudge message to an object using the specified context.
   * <p>
   * This provides the strategy for converting a message to an object to be specified.
   * The input message should not be mutated.
   * 
   * @param context  the context, not null
   * @param message  the Fudge message to convert, not null
   * @return the created object, not null
   */
  T buildObject(FudgeDeserializationContext context, FudgeFieldContainer message);

}

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

import org.fudgemsg.MutableFudgeMsg;

/**
 * Defines how to convert an object to a Fudge message.
 * <p>
 * This interface, and {@link FudgeObjectBuilder}, define how to convert between
 * an object and a Fudge message. The two interfaces are combined in {@link FudgeBuilder}.
 * <p>
 * This interface must be implemented in a thread-safe manner.
 * 
 * @param <T> the Java type this builder creates Fudge message from
 */
public interface FudgeMessageBuilder<T> {

  /**
   * Converts the object to a Fudge message using the specified context.
   * <p>
   * This provides the strategy for converting an object to a message to be specified.
   * The resulting message is mutable to enable efficient implementations.
   * 
   * @param serializer  the instance in control of serialization, not null
   * @param object  the object to convert to a Fudge message, not null
   * @return the Fudge message, not null
   */
  MutableFudgeMsg buildMessage(FudgeSerializer serializer, T object);

}

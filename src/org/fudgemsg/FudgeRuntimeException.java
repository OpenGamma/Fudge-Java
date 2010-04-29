/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc. and other contributors.
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

/**
 * A Fudge-specific subclass of {@link RuntimeException} for all non-checked-exceptions
 * generated by Fudge. The Fudge libraries will never throw a checked exception, and wrap all checked
 * exceptions in a {@code FudgeRuntimeException} or a subclass of it.
 *
 * @author Kirk Wylie
 */
public class FudgeRuntimeException extends RuntimeException {
  
  /**
   * Creates a new {@link FudgeRuntimeException}. This is provided for use by a subclass. Instances
   * of a FudgeRuntimException should not be created other than to wrap checked exceptions. When an
   * error condition is detected, one of the standard Java exceptions should be used.
   * 
   * @param message description of the error condition
   */
  protected FudgeRuntimeException(String message) {
    super(message);
  }
  
  /**
   * Creates a new {@link FudgeRuntimeException} in response to a checked exception from a system library.
   * 
   * @param message description of the error condition
   * @param cause the underlying exception
   */
  public FudgeRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

}

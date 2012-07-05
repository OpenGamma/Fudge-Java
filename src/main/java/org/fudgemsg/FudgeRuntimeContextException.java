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

/**
 * A Fudge-specific runtime exception to wrap checked exception.
 * It allows to atach arbitrary context information
 * <p>
 * Fudge will never throw a checked exception.
 * All checked exceptions will be wrapped in this exception or a subclass of it.
 */
public class FudgeRuntimeContextException extends FudgeRuntimeException {

  Object _context;

  /**
   * Creates a new exception, to be used by subclasses.
   * <p>
   * Instances should not be created other than to wrap checked exceptions.
   * A standard Java runtime exception should be used where possible.
   *
   * @param message  the description of the error condition, may be null
   * @param context  context to attach to this exception
   */
  public FudgeRuntimeContextException(String message, Object context) {
    super(message);
    _context = context;
  }

  /**
   * Creates a wrapper for a checked exception.
   *
   * @param message  the description of the error condition, may be null
   * @param cause  the underlying exception, should not be null
   * @param context  context to attach to this exception
   */
  public FudgeRuntimeContextException(String message, Throwable cause, Object context) {
    super(message, cause);
    _context = context;
  }

  /**
   * Returns context associated with this exception.
   * 
   * @return the context
   */
  public Object getContext() {
    return _context;
  }

  @Override
  public String toString() {
    return super.toString() + " context: " + getContext().toString();
  }

}

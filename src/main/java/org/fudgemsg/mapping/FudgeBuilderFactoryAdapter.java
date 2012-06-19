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

/**
 * Adapter implementation of a Fudge message factory.
 * <p>
 * This class delegates all factory method calls to an underlying factory.
 * This simplifies the creation of implementations, allowing factories to
 * be chained together.
 * <p>
 * This class has no mutable state and is thread-safe.
 * Subclasses should also be thread-safe.
 */
public class FudgeBuilderFactoryAdapter implements FudgeBuilderFactory {

  /**
   * The underlying delegate.
   */
  private final FudgeBuilderFactory _delegate;

  /**
   * Creates a new factory adapter.
   * 
   * @param delegate  the underlying factory, not null
   */
  protected FudgeBuilderFactoryAdapter(final FudgeBuilderFactory delegate) {
    if (delegate == null) {
      throw new NullPointerException("delegate cannot be null");
    }
    _delegate = delegate;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the delegate instance to pass method calls to.
   * 
   * @return the underlying factory, not null
   */
  protected FudgeBuilderFactory getDelegate() {
    return _delegate;
  }

  @Override
  public <T> void addGenericBuilder(Class<T> clazz, FudgeBuilder<T> builder) {
    getDelegate().addGenericBuilder(clazz, builder);
  }

  @Override
  public <T> FudgeMessageBuilder<T> createMessageBuilder(Class<T> clazz) {
    return getDelegate().createMessageBuilder(clazz);
  }

  @Override
  public <T> FudgeObjectBuilder<T> createObjectBuilder(Class<T> clazz) {
    return getDelegate().createObjectBuilder(clazz);
  }

}

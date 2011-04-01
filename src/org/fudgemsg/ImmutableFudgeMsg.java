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
 * A standard immutable Fudge message.
 * <p>
 * The message consists of a list of {@link FudgeField Fudge fields}.
 * This class holds the entire message in memory.
 * <p>
 * Applications are recommended to store and manipulate a {@link FudgeMsg}
 * instance or a {@link MutableFudgeMsg} rather than this class
 * for future flexibility.
 * <p>
 * This class can be created as a copy of an existing {@link FudgeMsg}.
 * For efficiency, the reference to a {@link FudgeContext} is kept and the context is not copied.
 * In that scenario, changes made to the context will be made visible through this class, for
 * example the behavior of {@link #getFieldValue}. If this is not desired, create a
 * {@link ImmutableFudgeContext} from your underlying {@code FudgeContext} for use in cloning messages.
 * Message fields are copied at one level deep only.
 * Any sub-messages, or referenced objects may be still be mutable.
 * <p>
 * This class is intended to be immutable but not all contents will necessarily be immutable.
 */
public class ImmutableFudgeMsg extends AbstractFudgeMsg {

  /**
   * Creates a new instance by copying fields from another message.
   * <p>
   * The new instance will share the same Fudge context which may be undesirable as
   * that context may be mutable.
   * 
   * @param fudgeMsg  the message to copy, not null
   */
  public ImmutableFudgeMsg(final AbstractFudgeMsg fudgeMsg) {
    this(fudgeMsg, fudgeMsg.getFudgeContext());
  }

  /**
   * Creates a new message by copying fields from another message using
   * the specified context for type resolution. 
   * 
   * @param fields  the message to copy, not null
   * @param fudgeContext  the context to use for type resolution and other services, not null
   */
  public ImmutableFudgeMsg(final FudgeMsg fields, final FudgeContext fudgeContext) {
    super(fields, fudgeContext);
  }

  /**
   * Creates an empty message.
   * 
   * @param fudgeContext  the context to use for type resolution and other services, not null
   */
  protected ImmutableFudgeMsg(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    return obj instanceof ImmutableFudgeMsg && super.equals(obj);
  }

  @Override
  public int hashCode() {
    return ImmutableFudgeMsg.class.hashCode() ^ super.hashCode();
  }

}

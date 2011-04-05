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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
 * This class makes no guarantees about the immutability or thread-safety of its
 * content, although it holds the references in an immutable and thread-safe way.
 */
public final class ImmutableFudgeMsg extends AbstractFudgeMsg {

  /**
   * The unmodifiable list of fields.
   */
  private final List<FudgeField> _fields;

  /**
   * Constructor taking a Fudge context.
   * 
   * @param fudgeContext  the context to use for type resolution and other services, not null
   */
  protected ImmutableFudgeMsg(FudgeContext fudgeContext) {
    super(fudgeContext);
    _fields = Collections.emptyList();
  }

  /**
   * Constructor taking a Fudge message to copy.
   * <p>
   * The new instance will share the same Fudge context.
   * This may be undesirable as that context may be mutable.
   * 
   * @param fudgeMsg  the Fudge message to copy, not null
   */
  public ImmutableFudgeMsg(AbstractFudgeMsg fudgeMsg) {
    this(fudgeMsg.getFudgeContext(), fudgeMsg);
  }

  /**
   * Constructor taking a Fudge context that copies another message.
   * <p>
   * The fields from the container are copied into this message, creating a new
   * field for each supplied field.
   * 
   * @param fudgeContext  the context to use for type resolution and other services, not null
   * @param fieldsToCopy  the initial set of fields to shallow copy, null ignored
   */
  public ImmutableFudgeMsg(final FudgeContext fudgeContext, Iterable<FudgeField> fieldsToCopy) {
    super(fudgeContext);
    if (fieldsToCopy != null) {
      List<FudgeField> fields = new ArrayList<FudgeField>();
      for (FudgeField field : fieldsToCopy) {
        fields.add(ImmutableFudgeField.of(field));
      }
      _fields = Collections.unmodifiableList(fields);
    } else {
      _fields = Collections.emptyList();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unmodifiable list of fields.
   * 
   * @return the unmodifiable list of fields, not null
   */
  @Override
  protected List<FudgeField> getFields() {
    return _fields;
  }

  /**
   * Gets the unmodifiable list of fields.
   * 
   * @return the unmodifiable list of fields, not null
   */
  @Override  // override for performance
  public List<FudgeField> getAllFields() {
    return _fields;
  }

  /**
   * Gets a unmodifiable iterator over the list of fields in this message.
   * <p>
   * A message is partially ordered and the returned iterator reflects that order.
   * 
   * @return the unmodifiable iterator of fields, not null
   */
  @Override  // override for performance
  public Iterator<FudgeField> iterator() {
    return _fields.iterator();
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

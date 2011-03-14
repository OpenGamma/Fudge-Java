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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgWriter;

/**
 * Reader to serialize Java objects to an underlying stream of Fudge messages.
 * 
 * @author Andrew Griffin
 */
public class FudgeObjectWriter {

  /**
   * The underlying Fudge message reader.
   */
  private final FudgeMsgWriter _messageWriter;
  /**
   * The context.
   */
  private FudgeSerializationContext _serialisationContext;

  /**
   * Creates a writer around the underlying Fudge stream.
   * 
   * @param messageWriter  the target for Fudge messages containing serialized objects
   */
  public FudgeObjectWriter(final FudgeMsgWriter messageWriter) {
    if (messageWriter == null) {
      throw new NullPointerException("messageWriter cannot be null");
    }
    _messageWriter = messageWriter;
    _serialisationContext = new FudgeSerializationContext(messageWriter.getFudgeContext());
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the underlying message target.
   * 
   * @return the underlying message writer, not null unless overridden in a subclass
   */
  public FudgeMsgWriter getMessageWriter() {
    return _messageWriter;
  }

  /**
   * Returns the current serialization context.
   * This is associated with the same {@link FudgeContext} as the target message stream.
   * 
   * @return the context, not null unless overridden in a subclass
   */
  public FudgeSerializationContext getSerialisationContext() {
    return _serialisationContext;
  }

  /**
   * Returns the underlying {@link FudgeContext}.
   * This will be the context of the {@link FudgeMsgWriter} being used.
   * 
   * @return the {@code FudgeContext}
   */
  public FudgeContext getFudgeContext() {
    final FudgeSerializationContext context = getSerialisationContext();
    if (context == null) {
      return null;
    }
    return context.getFudgeContext();
  }

  //-------------------------------------------------------------------------
  /**
   * Writes to the stream a serialized form of the given object.
   * <p>
   * This converts the Java object to a Fudge message and writes it to the target stream.
   * 
   * @param <T> type of the Java object
   * @param obj the object to write
   */
  public <T> void write(final T obj) {
    getSerialisationContext().reset();
    FudgeFieldContainer message;
    if (obj == null) {
      // write an empty message
      message = getSerialisationContext().newMessage();
    } else {
      // delegate to a message builder
      message = getSerialisationContext().objectToFudgeMsg(obj);
    }
    getMessageWriter().writeMessage(message, 0);
  }

  /**
   * Closes the underlying stream.
   */
  public void close() {
    FudgeMsgWriter mw = getMessageWriter();
    if (mw == null) {
      return;
    }
    mw.close();
  }

}

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
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.wire.FudgeMsgWriter;

/**
 * Reader to serialize Java objects to an underlying stream of Fudge messages.
 */
public class FudgeObjectWriter {

  /**
   * The underlying Fudge message reader.
   */
  private final FudgeMsgWriter _messageWriter;
  /**
   * The serializer.
   */
  private FudgeSerializer _serializer;

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
    _serializer = new FudgeSerializer(messageWriter.getFudgeContext());
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
   * Returns the current serializer.
   * This is associated with the same {@link FudgeContext} as the target message stream.
   * 
   * @return the serializer, not null unless overridden in a subclass
   */
  public FudgeSerializer getSerializer() {
    return _serializer;
  }

  /**
   * Returns the underlying {@link FudgeContext}.
   * This will be the context of the {@link FudgeMsgWriter} being used.
   * 
   * @return the {@code FudgeContext}
   */
  public FudgeContext getFudgeContext() {
    final FudgeSerializer context = getSerializer();
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
    getSerializer().reset();
    FudgeMsg message;
    if (obj == null) {
      // write an empty message
      message = getSerializer().newMessage();
    } else {
      // delegate to a message builder
      message = getSerializer().objectToFudgeMsg(obj);
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

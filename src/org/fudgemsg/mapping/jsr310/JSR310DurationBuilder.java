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

package org.fudgemsg.mapping.jsr310;

import javax.time.Duration;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * Builder for encoding and decoding JSR-310 Duration objects.
 */
public class JSR310DurationBuilder implements FudgeBuilder<Duration> {

  /**
   * Singleton instance of the builder.
   */
  public static final FudgeBuilder<Duration> INSTANCE = new JSR310DurationBuilder();
  
  private static final String SECONDS_FIELD = "seconds";
  private static final String NANOS_FIELD = "nanos";
  
  private JSR310DurationBuilder() {
  }
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, Duration object) {
    if (object == null) {
      return null;
    }
    MutableFudgeMsg msg = context.newMessage();
    msg.add(SECONDS_FIELD, object.getSeconds());
    msg.add(NANOS_FIELD, object.getNanoOfSecond());
    return msg;
  }

  @Override
  public Duration buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    long seconds = msg.getLong(SECONDS_FIELD);
    int nanos = msg.getInt(NANOS_FIELD);
    return Duration.ofSeconds(seconds, nanos);
  }

}

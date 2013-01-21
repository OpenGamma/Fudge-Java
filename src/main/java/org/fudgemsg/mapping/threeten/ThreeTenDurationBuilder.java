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
package org.fudgemsg.mapping.threeten;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Duration;

/**
 * Builder for encoding and decoding ThreeTen Duration objects.
 */
public class ThreeTenDurationBuilder implements FudgeBuilder<Duration> {

  /**
   * Singleton instance of the builder.
   */
  public static final FudgeBuilder<Duration> INSTANCE = new ThreeTenDurationBuilder();
  
  private static final String SECONDS_FIELD = "seconds";
  private static final String NANOS_FIELD = "nanos";
  
  private ThreeTenDurationBuilder() {
  }
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Duration object) {
    if (object == null) {
      return null;
    }
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(SECONDS_FIELD, object.getSeconds());
    msg.add(NANOS_FIELD, object.getNano());
    return msg;
  }

  @Override
  public Duration buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    long seconds = msg.getLong(SECONDS_FIELD);
    int nanos = msg.getInt(NANOS_FIELD);
    return Duration.ofSeconds(seconds, nanos);
  }

}

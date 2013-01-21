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

import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeObjectReader;
import org.fudgemsg.mapping.FudgeObjectWriter;
import org.fudgemsg.mapping.FudgeSerializer;
import org.junit.Before;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Base class for builder tests.
 */
public abstract class AbstractFudgeBuilderTestCase {


  private FudgeContext _context;
  private FudgeSerializer _serializer;
  private FudgeDeserializer _deserializer;

  @Before
  public void createContexts() {
    _context = new FudgeContext(FudgeContext.GLOBAL_DEFAULT);
    _serializer = new FudgeSerializer(_context);
    _deserializer = new FudgeDeserializer(_context);
  }

  protected FudgeContext getFudgeContext() {
    return _context;
  }

  protected FudgeSerializer getFudgeSerializer() {
    return _serializer;
  }

  protected FudgeDeserializer getFudgeDeserializer() {
    return _deserializer;
  }


  @SuppressWarnings("unchecked")
  protected <T> T cycleObject(final T object) {
    ByteArrayOutputStream _output = new ByteArrayOutputStream();
    FudgeObjectWriter _fudgeObjectWriter = getFudgeContext().createObjectWriter(_output);

    _fudgeObjectWriter.write(object);

    ByteArrayInputStream input = new ByteArrayInputStream(_output.toByteArray());

    FudgeObjectReader fudgeObjectReader = getFudgeContext().createObjectReader(input);

    return (T) fudgeObjectReader.read();
  }

  public static void isInstanceOf(Object parameter, Class<?> clazz) {
    if (!clazz.isInstance(parameter)) {
      throw new AssertionError("Expected an object to be instance of <" + clazz.getName() + "> but it was instance of <" + parameter.getClass().getName() + "> actually.");
    }
  }
}

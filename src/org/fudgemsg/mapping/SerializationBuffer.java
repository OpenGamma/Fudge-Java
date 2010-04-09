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

package org.fudgemsg.mapping;

import java.util.ArrayList;
import java.util.Stack;

/**
 * A buffer for the serialization and deserialization contexts that handle back references.
 * Cyclic references are detected and errors triggered.
 * 
 * @author Andrew Griffin
 */
/* package */ class SerializationBuffer {
  
  private static final String CYCLIC_REFERENCE_MESSAGE = "Serialization framework can't support cyclic references";

  public static class Handle {
    
    private Object _objectInstance;
    private Class<?> _objectClass;
    private int _queueCheckpoint;
    
    private Handle (Object object) {
      setObjectInstance (object);
    }
    
    private Handle () {
      _objectInstance = null;
      _objectClass = null;
    }
    
    public Object getObjectInstance () {
      return _objectInstance;
    }
    
    public Class<?> getObjectClass () {
      return _objectClass;
    }
    
    public void setObjectInstance (Object object) {
      if (object == null) throw new NullPointerException ("object cannot be null");
      if (_objectInstance != null) throw new IllegalStateException ("instance already set");
      _objectInstance = object;
      if (_objectClass == null) setObjectClass (object.getClass ());
    }
    
    public void setObjectClass (Class<?> clazz) {
      if (clazz == null) throw new NullPointerException ("class cannot be null");
      if (_objectClass != null) throw new IllegalStateException ("class already set");
      _objectClass = clazz;
    }
    
    private int getQueueCheckpoint () {
      return _queueCheckpoint;
    }
    
    private void setQueueCheckpoint (final int queueCheckpoint) {
      _queueCheckpoint = queueCheckpoint;
    }
    
    @Override
    public String toString () {
      final StringBuilder sb = new StringBuilder ();
      sb.append ("queueCheckpoint=").append (getQueueCheckpoint ()).append (", class=");
      sb.append ((getObjectClass () != null) ? getObjectClass ().getName () : "null");
      sb.append (", object=");
      sb.append ((getObjectInstance () != null) ? getObjectInstance ().toString () : "null");
      return sb.toString ();
    }
    
  }
  
  private final Stack<Handle> _handleStack;
  private final ArrayList<Handle> _handleQueue;
  
  /**
   * Creates a new {@link SerializationBuffer}.
   */
  SerializationBuffer () {
    _handleStack = new Stack<Handle> ();
    _handleQueue = new ArrayList<Handle> ();
  }
  
  private void queueHandle (final Handle handle) {
    _handleStack.push (handle);
    _handleQueue.add (handle);
    handle.setQueueCheckpoint (_handleQueue.size ());
  }
  
  private void unqueueHandle (final Handle handle) {
    final int c = handle.getQueueCheckpoint ();
    for (int i = _handleQueue.size (); --i >= c; ) {
      _handleQueue.remove (i);
    }
    handle.setQueueCheckpoint (0);
  }
  
  /**
   * Registers the start of an object being processed.
   * 
   * @param object the object currently being processed
   * @return a handle that can be used with {@link #cancelObject} if an error occurs
   * @throws UnsupportedOperationException if a cycle is detected
   */
  public Handle beginObject (final Object object) {
    if (object == null) throw new NullPointerException ("object cannot be null");
    final Handle handle = new Handle (object);
    queueHandle (handle);
    return handle;
  }
  
  /**
   * Registers the start of an object being processed that we can't identify (yet). The returned handle
   * can be used to pass information about the object as/when it becomes available.
   * 
   * @return a handle to identify the object
   */
  public Handle beginObject () {
    Handle handle = new Handle ();
    queueHandle (handle);
    return handle;
  }
  
  /**
   * Registers the end of an object being processed. The object is passed as a sanity check to detect errors.
   * 
   * @param object the object being processed
   */
  public void endObject (final Object object) {
    final Handle handle = _handleStack.pop ();
    assert handle.getObjectInstance () == object;
    unqueueHandle (handle);
  }
  
  /**
   * Registers the end of an object being processed. The handle is passed as a sanity check to detect errors.
   * The object is passed to update the handle if it has not been previously identified.
   * 
   * @param handle the object handle
   * @param object the object being processed
   */
  public void endObject (final Handle handle, final Object object) {
    if (handle.getObjectInstance () == null) {
      handle.setObjectInstance (object);
    } else {
      assert handle.getObjectInstance () == object;
    }
    final Handle stackHandle = _handleStack.pop ();
    assert stackHandle == handle;
    unqueueHandle (handle);
  }
  
  /**
   * Roll back the stack to before the handle was allocated. Use if an error occurs during serialization or deserialization
   * to recover to a known state.
   * 
   * @param handle handle to the object that failed
   */
  public void cancelObject (final Handle handle) {
    do {
      final Handle h = _handleStack.pop ();
      if (h == handle) {
        unqueueHandle (h);
        return;
      }
    } while (_handleStack.isEmpty ());
    throw new IllegalStateException ("handle was not on the stack");
  }
  
  /**
   * Registers an object as processed. This is equivalent to {@code beginObject(object); endObject(object); }. A stored
   * object can be referenced.
   * 
   * @param object object to store
   */
  public void storeObject (final Object object) {
    _handleQueue.add (new Handle (object));
  }
  
  /**
   * Finds the nearest index of an object with the given class.
   * 
   * @param className class name to search for
   * @return the object index offset, or {@code -1} if not found
   */
  public int findClassIndex (final String className) {
    final int l = _handleQueue.size ();
    for (int i = 2; i <= l; i++) {
      final Handle handle = _handleQueue.get (l - i);
      if (handle.getQueueCheckpoint () == 0) {
        // only consider handles that aren't on the stack to avoid cyclic reference problem
        final Class<?> clazz = handle.getObjectClass ();
        if ((clazz != null) && className.equals (clazz.getName ())) {
          return i - 1;
        }
      }
    }
    return -1;
  }
  
  /**
   * Finds the nearest index of a matching object (by reference equality).
   * 
   * @param object object to search for
   * @return the object index offset, or {@code -1} if not found
   */
  public int findObjectIndex (final Object object) {
    final int l = _handleQueue.size ();
    for (int i = 1; i <= l; i++) {
      final Handle handle = _handleQueue.get (l - i);
      if (object == handle.getObjectInstance ()) {
        if (handle.getQueueCheckpoint () > 0) {
          // possible cyclic reference issue
          throw new UnsupportedOperationException (CYCLIC_REFERENCE_MESSAGE);
        } else {
          // safe
          return i - 1;
        }
      }
    }
    return -1;
  }
  
  /**
   * Returns the class corresponding to a back index.
   * 
   * @param index the back index
   * @return the class
   * @throws UnsupportedOperationException if the back reference hasn't been resolved yet
   */
  public Class<?> getObjectClass (final int index) {
    final Class<?> clazz = _handleQueue.get (_handleQueue.size () - index).getObjectClass ();
    if (clazz == null) throw new UnsupportedOperationException (CYCLIC_REFERENCE_MESSAGE);
    return clazz;
  }
  
  /**
   * Returns the object corresponding to a back index.
   * 
   * @param index the back index
   * @return the object
   * @throws UnsupportedOperationException if the back reference hasn't been resolved yet
   */
  public Object getObjectInstance (final int index) {
    final Object object = _handleQueue.get (_handleQueue.size () - index).getObjectInstance ();
    if (object == null) throw new UnsupportedOperationException (CYCLIC_REFERENCE_MESSAGE);
    return object;
  }
  
  /**
   * Resets the state of the buffer.
   */
  /* package */ void reset () {
    _handleStack.clear ();
    _handleQueue.clear ();
  }
  
}
/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package org.fudgemsg.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TopologicalSort {
  public static <T> List<T> reverse(List<T> list) {
    LinkedList<T> l = new LinkedList<T>();
    for (T t : list) {
      l.addFirst(t);
    }
    return l;
  }

  public static <T> List<T> topologicalSort(final Map<T, Set<T>> entries) {
    List<T> sortedResult = new ArrayList<T>();
    Set<T> marked = new HashSet<T>();
    for (T s : entries.keySet()) {
      if (!marked.contains(s))
        topologicalSort(s, entries, marked, sortedResult);
    }
    return sortedResult;
  }

  public static <T> void topologicalSort(final T s, final Map<T, Set<T>> entries, final Set<T> marked, final List<T> sortedResult) {
    if (!marked.contains(s)) {
      for (T t : entries.get(s)) {
        if (entries.containsKey(t) && !marked.contains(t)) {
          topologicalSort(t, entries, marked, sortedResult);
        }
      }
    }
    marked.add(s);
    sortedResult.add(s);
  }
}

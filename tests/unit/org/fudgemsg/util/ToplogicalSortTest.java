/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package org.fudgemsg.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertTrue;
import static org.fudgemsg.util.TopologicalSort.topologicalSort;

public class ToplogicalSortTest {

  @Test
  public void simpleSort() {
    final Map<Integer, Set<Integer>> entries = new HashMap<Integer, Set<Integer>>();

    entries.put(1, new HashSet<Integer>(){{add(3);}});
    entries.put(3, new HashSet<Integer>(){{add(5);}});
    entries.put(2, new HashSet<Integer>(){{add(3);add(4);}});
    entries.put(4, new HashSet<Integer>(){{add(6);}});
    entries.put(5, new HashSet<Integer>(){{add(6);}});
    entries.put(6, new HashSet<Integer>());

    final List<Integer> sorted = new ArrayList<Integer>(){{addAll(topologicalSort(entries));}};

    assertTrue(sorted.indexOf(6) < sorted.indexOf(5));
    assertTrue(sorted.indexOf(6) < sorted.indexOf(4));
    assertTrue(sorted.indexOf(6) < sorted.indexOf(3));
    assertTrue(sorted.indexOf(6) < sorted.indexOf(2));
    assertTrue(sorted.indexOf(6) < sorted.indexOf(1));

    assertTrue(sorted.indexOf(5) < sorted.indexOf(3));
    assertTrue(sorted.indexOf(5) < sorted.indexOf(2));
    assertTrue(sorted.indexOf(5) < sorted.indexOf(1));

    assertTrue(sorted.indexOf(4) < sorted.indexOf(2));

    assertTrue(sorted.indexOf(3) < sorted.indexOf(1));
    assertTrue(sorted.indexOf(3) < sorted.indexOf(2));

  }
}


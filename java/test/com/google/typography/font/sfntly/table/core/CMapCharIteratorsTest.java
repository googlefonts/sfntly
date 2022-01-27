package com.google.typography.font.sfntly.table.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;

public class CMapCharIteratorsTest extends TestCase {

  public void testCharRangeIterator() {
    CMap.CharacterRangeIterator it = new CMap.CharacterRangeIterator(5, 8);

    assertEquals(Arrays.asList(5, 6, 7), toList(it));
  }

  public void testCharRangesIterator() {
    int[][] ranges = {{0, 5}, {8, 8}, {10, 11}, {0, 3}};

    CMap.CharacterRangesIterator it =
        new CMap.CharacterRangesIterator(ranges.length) {
          @Override
          protected int getRangeStart(int rangeIndex) {
            return ranges[rangeIndex][0];
          }

          @Override
          protected int getRangeEnd(int rangeIndex) {
            return ranges[rangeIndex][1];
          }
        };

    assertEquals(Arrays.asList(0, 1, 2, 3, 4, 10, 0, 1, 2), toList(it));
  }

  private static List<Integer> toList(Iterator<Integer> it) {
    List<Integer> list = new ArrayList<>();
    while (it.hasNext()) {
      list.add(it.next());
    }
    return list;
  }
}

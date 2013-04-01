// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests CoverageTable
 */
public class CoverageTableTests {

  @Test
  public void testCreateBuilder1() {
    CoverageTable.Builder builder = new CoverageTable.Builder();
    builder.add(1);
    builder.add(3);
    builder.add(5);
    
    CoverageTable table = builder.build();
    assertEquals("format 1", CoverageTable.Fmt1.class, table.getClass());
    
    int coveredIndexes[] = { -1, 0, -1, 1, -1, 2, -1 };
    testCoversAndIndexes(table, coveredIndexes, 0);
  }

  @Test
  public void testCreateBuilder2() {
    CoverageTable.Builder builder = new CoverageTable.Builder();
    builder.add(1);
    builder.addRange(2, 3);
    
    CoverageTable table = builder.build();
    assertEquals("format 2", CoverageTable.Fmt2.class, table.getClass());
    
    int coveredIndexes[] = { -1, 0, 1, 2, -1 };
    testCoversAndIndexes(table, coveredIndexes, 0);
  }
  
  @Test
  public void testCreateBuilder2b() {
    CoverageTable.Builder builder = new CoverageTable.Builder();
    builder.addRange(1, 8);
    builder.remove(3);
    
    CoverageTable table = builder.build();
    assertEquals("format 2", CoverageTable.Fmt2.class, table.getClass());
    
    int coveredIndexes[] = { -1, 0, 1, -1, 2, 3, 4, 5, 6, -1 };
    testCoversAndIndexes(table, coveredIndexes, 0);
  }
  
  private void testCoversAndIndexes(CoverageTable table, 
      int[] coveredIndexes, int base) {
    boolean[] covers = new boolean[coveredIndexes.length];
    for (int i = 0; i < covers.length; ++i) {
      covers[i] = coveredIndexes[i] >= 0;
    }
    testCovers(table, covers, base);
    testCoveredIndexes(table, coveredIndexes, base);
  }
  
  private void testCovers(CoverageTable table, boolean[] covers, int base) {
    for (int i = 0; i < covers.length; ++i, ++base) {
      assertEquals("covers " + base, covers[i], table.covers(base));
    }
  }
  
  private void testCoveredIndexes(CoverageTable table, int[] indexes, int base) {
    for (int i = 0; i < indexes.length; ++i, ++base) {
      assertEquals("coveredIndex: " + base, indexes[i], table.coverageIndex(base));
    }
  }
}

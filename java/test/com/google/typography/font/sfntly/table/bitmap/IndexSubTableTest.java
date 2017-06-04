package com.google.typography.font.sfntly.table.bitmap;

import junit.framework.TestCase;

public class IndexSubTableTest extends TestCase {

  public void testCreateBuilder() {
    try {
      IndexSubTable.Builder.createBuilder(27);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid Index SubTable Format 27", e.getMessage());
    }
  }

}

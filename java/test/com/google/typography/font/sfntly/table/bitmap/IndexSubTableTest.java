package com.google.typography.font.sfntly.table.bitmap;

import com.google.typography.font.sfntly.data.ReadableFontData;
import junit.framework.TestCase;

public class IndexSubTableTest extends TestCase {

  public void testCreateBuilder() {
    try {
      IndexSubTable.Builder.createBuilder(27);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid index subtable format 27", e.getMessage());
    }
  }

  public void testToString() {
    ReadableFontData data =
        ReadableFontData.createReadableFontData(
            new byte[] {
              0,
              1, // indexFormat
              0,
              17, // imageFormat
              0,
              0,
              0,
              8 // imageDataOffset
            });

    IndexSubTable table =
        new IndexSubTable(data, 'a', 'a') {
          @Override
          public int glyphStartOffset(int glyphId) {
            throw new UnsupportedOperationException();
          }

          @Override
          public int glyphLength(int glyphId) {
            throw new UnsupportedOperationException();
          }

          @Override
          public int numGlyphs() {
            throw new UnsupportedOperationException();
          }
        };

    String expected =
        String.format(
            "%s%n", "IndexSubTable: [0x61 : 0x61], format = 1, image format = 17, imageOff = 0x8");
    assertEquals(expected, table.toString());
  }
}

package com.google.typography.font.sfntly.table.truetype;

import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.core.FontHeaderTable;
import com.google.typography.font.sfntly.testutils.SfAssert;
import java.util.Arrays;
import junit.framework.TestCase;

public class LocaTableTest extends TestCase {

  public void testLoca16() {
    LocaTable.Builder builder = LocaTable.Builder.createBuilder(new Header(Tag.loca), null);
    builder.setFormatVersion(FontHeaderTable.IndexToLocFormat.shortOffset);
    builder.setNumGlyphs(4);
    builder.setLocaList(Arrays.asList(0, 10, 30, 100, 200));
    LocaTable table = builder.build();

    SfAssert.assertTableHexDumpEquals(
        "" //
            + "00 00 00 05 00 0f 00 32 00 64",
        table);

    assertEquals(0, table.loca(0));
    assertEquals(10, table.loca(1));
    assertEquals(30, table.loca(2));
    assertEquals(100, table.loca(3));
    assertEquals(200, table.loca(4));

    assertEquals(0, table.glyphOffset(0));
    assertEquals(10, table.glyphOffset(1));
    assertEquals(30, table.glyphOffset(2));
    assertEquals(100, table.glyphOffset(3));

    assertEquals(10, table.glyphLength(0));
    assertEquals(20, table.glyphLength(1));
    assertEquals(70, table.glyphLength(2));
    assertEquals(100, table.glyphLength(3));

    try {
      table.loca(-1);
      fail();
    } catch (IndexOutOfBoundsException e) {
      assertEquals("Glyph ID is out of bounds.", e.getMessage());
    }

    try {
      table.loca(5);
      fail();
    } catch (IndexOutOfBoundsException e) {
      assertEquals("Glyph ID is out of bounds.", e.getMessage());
    }

    try {
      table.glyphOffset(4);
      fail();
    } catch (IndexOutOfBoundsException e) {
      assertEquals("Glyph ID is out of bounds.", e.getMessage());
    }

    try {
      table.glyphLength(4);
      fail();
    } catch (IndexOutOfBoundsException e) {
      assertEquals("Glyph ID is out of bounds.", e.getMessage());
    }
  }

  public void testLoca32() {
    LocaTable.Builder builder = LocaTable.Builder.createBuilder(new Header(Tag.loca), null);
    builder.setFormatVersion(FontHeaderTable.IndexToLocFormat.longOffset);
    builder.setNumGlyphs(4);
    builder.setLocaList(Arrays.asList(0, 10, 30, 100, 1000 * 1000));
    LocaTable table = builder.build();

    SfAssert.assertTableHexDumpEquals(
        "" //
            + "00 00 00 00 00 00 00 0a 00 00 00 1e 00 00 00 64\n"
            + "00 0f 42 40",
        table);

    assertEquals(0, table.loca(0));
    assertEquals(10, table.loca(1));
    assertEquals(30, table.loca(2));
    assertEquals(100, table.loca(3));
    assertEquals(1000 * 1000, table.loca(4));

    assertEquals(0, table.glyphOffset(0));
    assertEquals(10, table.glyphOffset(1));
    assertEquals(30, table.glyphOffset(2));
    assertEquals(100, table.glyphOffset(3));

    assertEquals(10, table.glyphLength(0));
    assertEquals(20, table.glyphLength(1));
    assertEquals(70, table.glyphLength(2));
    assertEquals(999900, table.glyphLength(3));

    try {
      table.loca(5);
      fail();
    } catch (IndexOutOfBoundsException e) {
      assertEquals("Glyph ID is out of bounds.", e.getMessage());
    }

    try {
      table.glyphOffset(4);
      fail();
    } catch (IndexOutOfBoundsException e) {
      assertEquals("Glyph ID is out of bounds.", e.getMessage());
    }

    try {
      table.glyphLength(4);
      fail();
    } catch (IndexOutOfBoundsException e) {
      assertEquals("Glyph ID is out of bounds.", e.getMessage());
    }
  }
}

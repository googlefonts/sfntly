package com.google.typography.font.sfntly.issue_tests;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.FontFactory;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.truetype.Glyph;
import com.google.typography.font.sfntly.table.truetype.GlyphTable;
import com.google.typography.font.sfntly.table.truetype.LocaTable;
import com.google.typography.font.sfntly.testutils.TestFont;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;

/**
 * Test for <a href="https://github.com/googlei18n/sfntly/issues/28">Issue 28</a>.
 *
 * <p>Test class showing inconsistent behaviour between loading from a byte array vs loading from a
 * file input stream.
 */
public class Issue28Tests extends TestCase {

  private static byte[] readToByteArray(File file) throws IOException {
    byte[] data = new byte[(int) file.length()];

    try (FileInputStream fis = new FileInputStream(file)) {
      fis.read(data);
    }

    return data;
  }

  private static Glyph getLastGlyph(Font font) {
    LocaTable locaTable = font.getTable(Tag.loca);
    int glyphId = locaTable.numGlyphs() - 1;
    GlyphTable glyfTable = font.getTable(Tag.glyf);
    int offset = locaTable.glyphOffset(glyphId);
    int length = locaTable.glyphLength(glyphId);
    return glyfTable.glyph(offset, length);
  }

  /**
   * Ensure that the stream and byte array sourced fonts both throw an exception when you read off
   * the end of a sliced ReadableFontData
   */
  public void testStreamVsBytes() throws IOException {
    FontFactory factory = FontFactory.getInstance();

    byte[] data = readToByteArray(TestFont.TestFontNames.ROBOTO.getFile());
    Font byteFont = factory.loadFonts(data)[0];

    Font streamFont;
    try (InputStream is = new FileInputStream(TestFont.TestFontNames.ROBOTO.getFile())) {
      streamFont = factory.loadFonts(is)[0];
    }

    // first test for byte array sourced font
    {
      Glyph byteGlyph = getLastGlyph(byteFont);
      try {
        byteGlyph.xMin();
        fail();
      } catch (IndexOutOfBoundsException e) {
        assertEquals("Index attempted to be read from is out of bounds: 2", e.getMessage());
      }
    }

    // next test for stream sourced font
    {
      Glyph streamGlyph = getLastGlyph(streamFont);
      try {
        streamGlyph.xMin();
        fail();
      } catch (IndexOutOfBoundsException e) {
        assertEquals("Index attempted to be read from is out of bounds: 2", e.getMessage());
      }
    }
  }
}

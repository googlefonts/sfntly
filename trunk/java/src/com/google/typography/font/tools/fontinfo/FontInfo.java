// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.tools.fontinfo;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.core.CMap;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.sfntly.table.core.FontHeaderTable;
import com.google.typography.font.sfntly.table.core.HorizontalHeaderTable;
import com.google.typography.font.sfntly.table.core.OS2Table;
import com.google.typography.font.sfntly.table.core.PostScriptTable;
import com.google.typography.font.sfntly.table.truetype.CompositeGlyph;
import com.google.typography.font.sfntly.table.truetype.Glyph;
import com.google.typography.font.sfntly.table.truetype.Glyph.GlyphType;
import com.google.typography.font.sfntly.table.truetype.GlyphTable;
import com.google.typography.font.sfntly.table.truetype.LocaTable;
import com.google.typography.font.tools.fontinfo.DataDisplayTable.Align;

import com.ibm.icu.lang.UCharacter;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class of static functions that return information about a given font
 *
 * @author Han-Wen Yeh
 *
 */
// TODO Incorporate Brian's functions into this class to make a more centralized
// font information tool
// TODO Make abstract FontInfo class with nonstatic functions and subclass this
// as TrueTypeFontInfo
public class FontInfo {
  private static final int CHECKSUM_LENGTH = 8;
  private static final DecimalFormat twoDecimalPlaces = new DecimalFormat("#.##");

  /**
   * Gets a list of information regarding various dimensions about the given
   * font from the head, hhea, and OS/2 font tables
   *
   * @param font
   *          the source font
   * @return a list of dimensional information about the font
   */
  public static DataDisplayTable listFontMetrics(Font font) {
    String[] header = { "Name", "Value" };
    Align[] displayAlignment = { Align.Left, Align.Left };
    DataDisplayTable table = new DataDisplayTable(Arrays.asList(header));
    table.setAlignment(Arrays.asList(displayAlignment));

    // Retrieve necessary tables
    FontHeaderTable headTable = (FontHeaderTable) getTable(font, Tag.head);
    HorizontalHeaderTable hheaTable = (HorizontalHeaderTable) getTable(font, Tag.hhea);
    OS2Table os2Table = (OS2Table) getTable(font, Tag.OS_2);

    table.add(Arrays.asList(
        new String[] { "Units per em", String.format("%d", headTable.unitsPerEm()) }));
    table.add(Arrays.asList(new String[] {
        "(xMin, yMin)", String.format("(%d, %d)", headTable.xMin(), headTable.yMin()) }));
    table.add(Arrays.asList(new String[] {
        "(xMax, yMax)", String.format("(%d, %d)", headTable.xMax(), headTable.yMax()) }));
    table.add(Arrays.asList(new String[] {
        "Smallest readable size (px per em)", String.format("%d", headTable.lowestRecPPEM()) }));
    table.add(
        Arrays.asList(new String[] { "hhea ascender", String.format("%d", hheaTable.ascender()) }));
    table.add(Arrays.asList(
        new String[] { "hhea descender", String.format("%d", hheaTable.descender()) }));
    table.add(Arrays.asList(
        new String[] { "hhea typographic line gap", String.format("%d", hheaTable.lineGap()) }));
    table.add(Arrays.asList(
        new String[] { "OS/2 Windows ascender", String.format("%d", os2Table.usWinAscent()) }));
    table.add(Arrays.asList(
        new String[] { "OS/2 Windows descender", String.format("%d", os2Table.usWinDescent()) }));
    table.add(Arrays.asList(new String[] {
        "OS/2 typographic ascender", String.format("%d", os2Table.sTypoAscender()) }));
    table.add(Arrays.asList(new String[] {
        "OS/2 typographic ascender", String.format("%d", os2Table.sTypoDescender()) }));
    table.add(Arrays.asList(new String[] {
        "OS/2 typographic line gap", String.format("%d", os2Table.sTypoLineGap()) }));

    return table;
  }

  /**
   * Gets a list of tables in the font as well as their sizes.
   *
   *  This function returns a list of tables in the given font as well as their
   * sizes, both in bytes and as fractions of the overall font size. The
   * information for each font is contained in a TableInfo object.
   *
   * @param font
   *          the source font
   * @return a list of information about tables in the font provided
   */
  public static DataDisplayTable listTables(Font font) {
    String[] header = { "tag", "checksum", "length", "offset" };
    Align[] displayAlignment = { Align.Left, Align.Right, Align.Right, Align.Right };
    DataDisplayTable table = new DataDisplayTable(Arrays.asList(header));
    table.setAlignment(Arrays.asList(displayAlignment));

    // Total size of font
    int fontSize = 0;

    // Calculate font size
    Iterator<? extends Table> fontTableIter = font.iterator();
    while (fontTableIter.hasNext()) {
      Table fontTable = fontTableIter.next();
      fontSize += fontTable.headerLength();
    }

    // Add table data to output string
    fontTableIter = font.iterator();
    while (fontTableIter.hasNext()) {
      Table fontTable = fontTableIter.next();
      String name = Tag.stringValue(fontTable.headerTag());
      String checksum = String.format("0x%0" + CHECKSUM_LENGTH + "x", fontTable.headerChecksum());
      int length = fontTable.headerLength();
      double lengthPercent = length * 100.0 / fontSize;
      int offset = fontTable.headerOffset();

      // Add table data
      String[] data = { name, checksum,
          String.format("%s (%s%%)", length, twoDecimalPlaces.format(lengthPercent)),
          String.format("%d", offset) };
      table.add(Arrays.asList(data));
    }

    return table;
  }

  /**
   * Gets a list containing the platform ID, encoding ID, and format of all the
   * cmaps in a font
   *
   * @param font
   *          the source font
   * @return a list of information about the cmaps in the font
   */
  public static DataDisplayTable listCmaps(Font font) {
    String[] header = { "Platform ID", "Encoding ID", "Format" };
    Align[] displayAlignment = { Align.Right, Align.Right, Align.Right };
    DataDisplayTable table = new DataDisplayTable(Arrays.asList(header));
    table.setAlignment(Arrays.asList(displayAlignment));

    // Add information about each individual cmap in the table
    CMapTable cmapTable = getCMapTable(font);
    for (CMap cmap : cmapTable) {
      String[] data = { String.format("%d", cmap.platformId()),
          String.format("%d", cmap.encodingId()), String.format("%d", cmap.format()) };
      table.add(Arrays.asList(data));
    }

    return table;
  }

  /**
   * Gets the number of valid characters in the given font
   *
   * @param font
   *          the source font
   * @return the number of valid characters in the given font
   * @throws UnsupportedOperationException
   *           if font does not contain a UCS-4 or UCS-2 cmap
   */
  public static int countChars(Font font) {
    int numChars = 0;
    CMap cmap = getUCSCMap(font);

    // Find the number of characters that point to a valid glyph
    for (int charId : cmap) {
      if (cmap.glyphId(charId) != CMapTable.NOTDEF) {
        // Valid glyph
        numChars++;
      }
    }

    return numChars;
  }

  /**
   * Gets a list of code points of valid characters and their names in the given
   * font.
   *
   * @param font
   *          the source font
   * @return a list of code points of valid characters and their names in the
   *         given font.
   * @throws UnsupportedOperationException
   *           if font does not contain a UCS-4 or UCS-2 cmap
   */
  public static DataDisplayTable charList(Font font) {
    String[] header = { "Code point", "Name in font", "Unicode-designated name for code point" };
    Align[] displayAlignment = { Align.Right, Align.Left, Align.Left };
    DataDisplayTable table = new DataDisplayTable(Arrays.asList(header));
    table.setAlignment(Arrays.asList(displayAlignment));

    // Iterate through all code points
    CMap cmap = getUCSCMap(font);
    PostScriptTable postTable = (PostScriptTable) getTable(font, Tag.post);
    for (int charId : cmap) {
      int glyphId = cmap.glyphId(charId);
      if (glyphId != CMapTable.NOTDEF) {
        String name = "";
        try {
          name = postTable.glyphName(glyphId);
        } catch (Exception e) {
          // Set name to empty string for unsupported post table formats
        }
        String[] data = { String.format("0x%s", Integer.toHexString(charId)), name,
            UCharacter.getExtendedName(charId) };
        table.add(Arrays.asList(data));
      }
    }

    return table;
  }

  // Gets the code point and name of all the characters in the provided string
  // for the font
  // TODO public static DataDisplayTable charList(Font font, String charString)

  /**
   * Gets the size of hinting instructions in the glyph table, both in bytes and
   * as a fraction of the glyph table size.
   *
   * @param font
   *          the source font
   * @return the amount of hinting that is contained in the font
   */
  public static String hintingSize(Font font) {
    int instrSize = 0;

    LocaTable locaTable = getLocaTable(font);
    GlyphTable glyfTable = getGlyphTable(font);

    // Get hinting information from each glyph
    for (int i = 0; i < locaTable.numGlyphs(); i++) {
      Glyph glyph = glyfTable.glyph(locaTable.glyphOffset(i), locaTable.glyphLength(i));
      instrSize += glyph.instructionSize();
    }

    double percentage = instrSize * 100.0 / glyfTable.headerLength();
    return String.format("%d (%s%% of glyf table)", instrSize, twoDecimalPlaces.format(percentage));
  }

  /**
   * Gets a list of glyphs in the font that are used as subglyphs and the number
   * of times each subglyph is used as a subglyph
   *
   * @param font
   *          the source font
   * @return the number of glyphs in the font that are used as subglyphs of
   *         other glyphs more than once
   */
  public static DataDisplayTable subglyphFrequencyList(Font font) {
    Map<Integer, Integer> subglyphFreq = new HashMap<Integer, Integer>();
    String[] header = { "Glyph ID", "Frequency" };
    Align[] displayAlignment = { Align.Right, Align.Right };
    DataDisplayTable table = new DataDisplayTable(Arrays.asList(header));
    table.setAlignment(Arrays.asList(displayAlignment));

    LocaTable locaTable = getLocaTable(font);
    GlyphTable glyfTable = getGlyphTable(font);

    // Add subglyphs of all composite glyphs to hashmap
    for (int i = 0; i < locaTable.numGlyphs(); i++) {
      Glyph glyph = glyfTable.glyph(locaTable.glyphOffset(i), locaTable.glyphLength(i));
      if (glyph.glyphType() == GlyphType.Composite) {
        CompositeGlyph cGlyph = (CompositeGlyph) glyph;

        // Add all subglyphs of this glyph to hashmap
        for (int j = 0; j < cGlyph.numGlyphs(); j++) {
          int subglyphId = cGlyph.glyphIndex(j);
          int frequency = 1;
          if (subglyphFreq.containsKey(subglyphId)) {
            frequency += subglyphFreq.get(subglyphId);
          }
          subglyphFreq.put(subglyphId, frequency);
        }
      }
    }

    // Add frequency data to table
    int numSubglyphs = 0;
    Set<Integer> sortedKeySet = new TreeSet<Integer>(subglyphFreq.keySet());
    for (Integer key : sortedKeySet) {
      String[] data = { key.toString(), subglyphFreq.get(key).toString() };
      table.add(Arrays.asList(data));
    }

    return table;
  }

  // TODO Calculate savings of subglyphs
  // public static int subglyphSavings(Font font) {}

  // TODO Find the maximum glyph nesting depth in a font
  // public static int glyphNestingDepth(Font font) {}

  // TODO Find the maximum glyph nexting depth in a font using the maxp table

  // TODO Find number of code points that use simple glyphs and number of code
  // points that use composite glyphs (and provide a list of code points for
  // each one)

  // TODO Find number of times a glyph is used as a subglyph (and if it
  // corresponds to a code point)

  // TODO Find list of glyphs which are subglyphs only (check ALL cmaps)

  // TODO Max nesting depth

  /**
   * Gets the table with the specified tag for the given font
   *
   * @param font
   *          the source font
   * @param tag
   *          the tag for the table to return
   * @return the specified table for the given font
   * @throws UnsupportedOperationException
   *           if the font does not contain the table with the specified tag
   * @see Tag
   */
  private static Table getTable(Font font, int tag) {
    Table table = font.getTable(tag);
    if (table == null) {
      throw new RuntimeException("Font has no " + Tag.stringValue(tag) + " table");
    }
    return table;
  }

  /**
   * Gets the cmap table for the given font
   *
   * @param font
   *          the source font
   * @return the cmap table for the given font
   * @throws UnsupportedOperationException
   *           if the font does not contain a valid cmap table
   */
  private static CMapTable getCMapTable(Font font) {
    return (CMapTable) getTable(font, Tag.cmap);
  }

  /**
   * Gets either a UCS4 or UCS2 cmap, if available
   *
   * @param font
   *          the source font
   * @return the UCS4 or UCS2 cmap
   * @throws UnsupportedOperationException
   *           if font does not contain a UCS-4 or UCS-2 cmap
   */
  private static CMap getUCSCMap(Font font) {
    CMapTable cmapTable = getCMapTable(font);

    // Obtain the UCS-4 cmap. If it doesn't exist, then obtain the UCS-2 cmap
    CMap cmap = null;
    cmap = cmapTable.cmap(
        Font.PlatformId.Windows.value(), Font.WindowsEncodingId.UnicodeUCS4.value());
    if (cmap == null) {
      cmap = cmapTable.cmap(
          Font.PlatformId.Windows.value(), Font.WindowsEncodingId.UnicodeUCS2.value());
    }
    if (cmap == null) {
      throw new UnsupportedOperationException("Font has no UCS-4 or UCS-2 cmap");
    }

    return cmap;
  }

  /**
   * Gets the loca table for the given font
   *
   * @param font
   *          the source font
   * @return the loca table for the given font
   * @throws UnsupportedOperationException
   *           if the font does not contain a valid loca table
   */
  private static LocaTable getLocaTable(Font font) {
    return (LocaTable) getTable(font, Tag.loca);
  }

  /**
   * Gets the glyph table for the given font
   *
   * @param font
   *          the source font
   * @return the glyph table for the given font
   * @throws UnsupportedOperationException
   *           if the font does not contain a valid glyph table
   */
  private static GlyphTable getGlyphTable(Font font) {
    return (GlyphTable) getTable(font, Tag.glyf);
  }
}

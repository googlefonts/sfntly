// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.tools.fontinfo;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.math.Fixed1616;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.core.CMap;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.sfntly.table.core.FontHeaderTable;
import com.google.typography.font.sfntly.table.core.HorizontalHeaderTable;
import com.google.typography.font.sfntly.table.core.MaximumProfileTable;
import com.google.typography.font.sfntly.table.core.NameTable;
import com.google.typography.font.sfntly.table.core.OS2Table;
import com.google.typography.font.sfntly.table.truetype.CompositeGlyph;
import com.google.typography.font.sfntly.table.truetype.Glyph;
import com.google.typography.font.sfntly.table.truetype.GlyphTable;
import com.google.typography.font.sfntly.table.truetype.LocaTable;
import com.ibm.icu.impl.IllegalIcuArgumentException;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UScript;
import com.ibm.icu.text.UnicodeSet;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class of static functions that return information about a given font
 *
 * @author Brian Stell, Han-Wen Yeh
 */
// TODO Make abstract FontInfo class with nonstatic functions and subclass this
// as TrueTypeFontInfo
public class FontInfo {

  /**
   * @param font the source font
   * @return the sfnt version of the font
   */
  public static String sfntVersion(Font font) {
    double version = Fixed1616.doubleValue(font.sfntVersion());
    NumberFormat numberFormatter = NumberFormat.getInstance();
    numberFormatter.setMinimumFractionDigits(2);
    numberFormatter.setGroupingUsed(false);
    return numberFormatter.format(version);
  }

  /**
   * Gets a list of information regarding various dimensions about the given font from the head,
   * hhea, and OS/2 font tables
   *
   * @param font the source font
   * @return a list of dimensional information about the font
   */
  public static DataDisplayTable listFontMetrics(Font font) {
    DataDisplayTable table = new DataDisplayTable("Name", "Value");
    table.setAlignment(DataDisplayTable.Align.Left, DataDisplayTable.Align.Left);

    // Retrieve necessary tables
    FontHeaderTable headTable = (FontHeaderTable) FontUtils.getTable(font, Tag.head);
    HorizontalHeaderTable hheaTable = (HorizontalHeaderTable) FontUtils.getTable(font, Tag.hhea);
    OS2Table os2Table = (OS2Table) FontUtils.getTable(font, Tag.OS_2);

    addTwoColumn(table, "Units per em", "%d", headTable.unitsPerEm());
    addTwoColumn(table, "[xMin, xMax]", "[%d, %d]", headTable.xMin(), headTable.xMax());
    addTwoColumn(table, "[yMin, yMax]", "[%d, %d]", headTable.yMin(), headTable.yMax());
    addTwoColumn(table, "Smallest readable size (px per em)", "%d", headTable.lowestRecPPEM());
    addTwoColumn(table, "hhea ascender", "%d", hheaTable.ascender());
    addTwoColumn(table, "hhea descender", "%d", hheaTable.descender());
    addTwoColumn(table, "hhea typographic line gap", "%d", hheaTable.lineGap());
    addTwoColumn(table, "OS/2 Windows ascender", "%d", os2Table.usWinAscent());
    addTwoColumn(table, "OS/2 Windows descender", "%d", os2Table.usWinDescent());
    addTwoColumn(table, "OS/2 typographic ascender", "%d", os2Table.sTypoAscender());
    addTwoColumn(table, "OS/2 typographic ascender", "%d", os2Table.sTypoDescender());
    addTwoColumn(table, "OS/2 typographic line gap", "%d", os2Table.sTypoLineGap());

    return table;
  }

  /**
   * Gets a list of tables in the font as well as their sizes.
   *
   * <p>This function returns a list of tables in the given font as well as their sizes, both in
   * bytes and as fractions of the overall font size. The information for each font is contained in
   * a TableInfo object.
   *
   * @param font the source font
   * @return a list of information about tables in the font provided
   */
  public static DataDisplayTable listTables(Font font) {
    DataDisplayTable table = new DataDisplayTable("tag", "checksum", "length", "offset");
    table.setAlignment(
        DataDisplayTable.Align.Left,
        DataDisplayTable.Align.Right,
        DataDisplayTable.Align.Right,
        DataDisplayTable.Align.Right);

    // Total size of font
    int fontSize = 0;

    // Calculate font size
    for (Iterator<? extends Table> it = font.iterator(); it.hasNext(); ) {
      Table fontTable = it.next();
      fontSize += fontTable.headerLength();
    }

    // Add table data to output string
    for (Iterator<? extends Table> it = font.iterator(); it.hasNext(); ) {
      Table fontTable = it.next();
      String name = Tag.stringValue(fontTable.headerTag());
      String checksum = String.format("0x%08X", fontTable.headerChecksum());
      int length = fontTable.headerLength();
      double lengthPercent = length * 100.0 / fontSize;
      int offset = fontTable.headerOffset();

      // Add table data
      table.add(
          name,
          checksum,
          String.format("%d (%.2f%%)", length, lengthPercent),
          String.format("%d", offset));
    }

    return table;
  }

  /**
   * Gets a list of entries in the name table of a font. These entries contain information related
   * to the font, such as the font name, style name, and copyright notices.
   *
   * @param font the source font
   * @return a list of entries in the name table of the font
   */
  public static DataDisplayTable listNameEntries(Font font) {
    DataDisplayTable table =
        new DataDisplayTable("Platform", "Encoding", "Language", "Name", "Value");
    table.setAlignment(
        DataDisplayTable.Align.Left,
        DataDisplayTable.Align.Left,
        DataDisplayTable.Align.Left,
        DataDisplayTable.Align.Left,
        DataDisplayTable.Align.Left);

    NameTable nameTable = (NameTable) FontUtils.getTable(font, Tag.name);
    for (NameTable.NameEntry entry : nameTable) {

      String eidEntry = ""; // Platform-specific encoding
      String lidEntry = ""; // Language

      switch (Font.PlatformId.valueOf(entry.platformId())) {
        case Unicode:
          eidEntry = Font.UnicodeEncodingId.valueOf(entry.encodingId()).toString();
          lidEntry = NameTable.UnicodeLanguageId.valueOf(entry.languageId()).toString();
          break;
        case Macintosh:
          eidEntry = Font.MacintoshEncodingId.valueOf(entry.encodingId()).toString();
          lidEntry = NameTable.MacintoshLanguageId.valueOf(entry.languageId()).toString();
          break;
        case Windows:
          eidEntry = Font.WindowsEncodingId.valueOf(entry.encodingId()).toString();
          lidEntry = NameTable.WindowsLanguageId.valueOf(entry.languageId()).toString();
          break;
        default:
          break;
      }

      table.add(
          String.format(
              "%s (id=%d)", Font.PlatformId.valueOf(entry.platformId()), entry.platformId()),
          String.format("%s (id=%d)", eidEntry, entry.encodingId()),
          String.format("%s (id=%d)", lidEntry, entry.languageId()),
          NameTable.NameId.valueOf(entry.nameId()).toString(),
          entry.name());
    }

    return table;
  }

  /**
   * Gets a list containing the platform ID, encoding ID, and format of all the cmaps in a font
   *
   * @param font the source font
   * @return a list of information about the cmaps in the font
   */
  public static DataDisplayTable listCmaps(Font font) {
    DataDisplayTable table = new DataDisplayTable("Platform ID", "Encoding ID", "Format");
    table.setAlignment(
        DataDisplayTable.Align.Right, DataDisplayTable.Align.Right, DataDisplayTable.Align.Right);

    // Add information about each individual cmap in the table
    CMapTable cmapTable = FontUtils.getCMapTable(font);
    for (CMap cmap : cmapTable) {
      table.add(
          String.format("%d", cmap.platformId()),
          String.format("%d", cmap.encodingId()),
          String.format("%d", cmap.format()));
    }

    return table;
  }

  /**
   * Gets the number of valid characters in the given font
   *
   * @param font the source font
   * @return the number of valid characters in the given font
   * @throws UnsupportedOperationException if font does not contain a UCS-4 or UCS-2 cmap
   */
  public static int numChars(Font font) {
    int numChars = 0;
    CMap cmap = FontUtils.getUCSCMap(font);

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
   * Gets a list of code points of valid characters and their names in the given font.
   *
   * @param font the source font
   * @return a list of code points of valid characters and their names in the given font.
   * @throws UnsupportedOperationException if font does not contain a UCS-4 or UCS-2 cmap
   */
  public static DataDisplayTable listChars(Font font) {
    DataDisplayTable table =
        new DataDisplayTable("Code point", "Glyph ID", "Unicode-designated name for code point");
    table.setAlignment(
        DataDisplayTable.Align.Right, DataDisplayTable.Align.Right, DataDisplayTable.Align.Left);

    // Iterate through all code points
    CMap cmap = FontUtils.getUCSCMap(font);
    for (int charId : cmap) {
      int glyphId = cmap.glyphId(charId);
      if (glyphId != CMapTable.NOTDEF) {
        table.add(
            FontUtils.getFormattedCodePointString(charId),
            String.format("%d", glyphId),
            UCharacter.getExtendedName(charId));
      }
    }

    return table;
  }

  // Gets the code point and name of all the characters in the provided string
  // for the font
  // TODO public static DataDisplayTable listChars(Font font, String charString)

  /**
   * Gets a list of Unicode blocks covered by the font and the amount each block is covered.
   *
   * @param font the source font
   * @return a list of Unicode blocks covered by the font
   */
  // FIXME Find more elegant method of retrieving block data
  public static DataDisplayTable listCharBlockCoverage(Font font) {
    DataDisplayTable table = new DataDisplayTable("Block", "Coverage");
    table.setAlignment(DataDisplayTable.Align.Left, DataDisplayTable.Align.Right);

    // Iterate through each block to check for coverage
    CMap cmap = FontUtils.getUCSCMap(font);
    int totalCount = 0;
    for (int i = 0; i < UnicodeBlockData.numBlocks(); i++) {
      String block = UnicodeBlockData.getBlockName(i);
      UnicodeSet set;
      try {
        set = new UnicodeSet("[[:Block=" + block + ":]-[:gc=Unassigned:]-[:gc=Control:]]");
      } catch (IllegalIcuArgumentException e) {
        continue;
      }
      int count = 0;
      for (String charStr : set) {
        if (cmap.glyphId(UCharacter.codePointAt(charStr, 0)) > 0) {
          count++;
        }
      }
      if (count > 0) {
        table.add(
            String.format(
                "%s [%s, %s]",
                block, UnicodeBlockData.getBlockStartCode(i), UnicodeBlockData.getBlockEndCode(i)),
            String.format("%d / %d", count, set.size()));
      }
      totalCount += count;
    }

    // Add control code points with valid glyphs to find the total number of
    // unicode characters with valid glyphs
    UnicodeSet controlSet = new UnicodeSet("[[:gc=Control:]]");
    for (String charStr : controlSet) {
      if (cmap.glyphId(UCharacter.codePointAt(charStr, 0)) > 0) {
        totalCount++;
      }
    }
    int nonUnicodeCount = numChars(font) - totalCount;
    if (nonUnicodeCount > 0) {
      addTwoColumn(table, "Unknown", "%d", nonUnicodeCount);
    }

    return table;
  }

  /**
   * Gets a list of scripts covered by the font and the amount each block is covered.
   *
   * @param font the source font
   * @return a list of scripts covered by the font
   */
  public static DataDisplayTable listScriptCoverage(Font font) {
    DataDisplayTable table = new DataDisplayTable("Script", "Coverage");
    table.setAlignment(DataDisplayTable.Align.Left, DataDisplayTable.Align.Right);
    HashMap<Integer, Integer> coveredScripts = new HashMap<>();

    // Add to script count for the script each code point belongs to
    CMap cmap = FontUtils.getUCSCMap(font);
    for (int charId : cmap) {
      if (cmap.glyphId(charId) != CMapTable.NOTDEF) {
        int scriptCode = UScript.getScript(charId);
        int scriptCount = 1;
        if (coveredScripts.containsKey(scriptCode)) {
          scriptCount += coveredScripts.get(scriptCode);
        }
        coveredScripts.put(scriptCode, scriptCount);
      }
    }

    // For each covered script, find the total size of the script and add
    // coverage to table
    Set<Integer> sortedScripts = new TreeSet<>(coveredScripts.keySet());
    int unknown = 0;
    for (Integer scriptCode : sortedScripts) {
      UnicodeSet scriptSet;
      String scriptName = UScript.getName(scriptCode);
      try {
        scriptSet = new UnicodeSet("[[:" + scriptName + ":]]");
      } catch (IllegalIcuArgumentException e) {
        unknown += coveredScripts.get(scriptCode);
        continue;
      }

      addTwoColumn(table, scriptName, "%d / %d", coveredScripts.get(scriptCode), scriptSet.size());
    }
    if (unknown > 0) {
      addTwoColumn(table, "Unsupported script", "%d", unknown);
    }

    return table;
  }

  /**
   * Gets a list of characters needed to fully cover scripts partially covered by the font
   *
   * @param font the source font
   * @return a list of characters needed to fully cover partially-covered scripts
   */
  public static DataDisplayTable listCharsNeededToCoverScript(Font font) {
    DataDisplayTable table = new DataDisplayTable("Script", "Code Point", "Name");
    table.setAlignment(
        DataDisplayTable.Align.Left, DataDisplayTable.Align.Right, DataDisplayTable.Align.Left);
    HashMap<Integer, UnicodeSet> coveredScripts = new HashMap<>();

    // Iterate through each set
    CMap cmap = FontUtils.getUCSCMap(font);
    for (int charId : cmap) {
      if (cmap.glyphId(charId) != CMapTable.NOTDEF) {
        int scriptCode = UScript.getScript(charId);
        if (scriptCode == UScript.UNKNOWN) {
          continue;
        }

        UnicodeSet scriptSet;
        if (!coveredScripts.containsKey(scriptCode)) {
          // New covered script found, create set
          try {
            scriptSet =
                new UnicodeSet(
                    "[[:" + UScript.getName(scriptCode) + ":]-[:gc=Unassigned:]-[:gc=Control:]]");
          } catch (IllegalIcuArgumentException e) {
            continue;
          }
          coveredScripts.put(scriptCode, scriptSet);
        } else {
          // Set for script already exists, retrieve for character removal
          scriptSet = coveredScripts.get(scriptCode);
        }
        scriptSet.remove(UCharacter.toString(charId));
      }
    }

    // Insert into table in order
    Set<Integer> sortedScripts = new TreeSet<>(coveredScripts.keySet());
    for (Integer scriptCode : sortedScripts) {
      UnicodeSet uSet = coveredScripts.get(scriptCode);
      for (String charStr : uSet) {
        int codePoint = UCharacter.codePointAt(charStr, 0);
        table.add(
            String.format("%s", UScript.getName(scriptCode)),
            FontUtils.getFormattedCodePointString(codePoint),
            UCharacter.getExtendedName(codePoint));
      }
    }

    return table;
  }

  /**
   * Gets the number of glyphs in the given font
   *
   * @param font the source font
   * @return the number of glyphs in the font
   * @throws UnsupportedOperationException if font does not contain a valid glyf table
   */
  public static int numGlyphs(Font font) {
    return ((MaximumProfileTable) FontUtils.getTable(font, Tag.maxp)).numGlyphs();
  }

  /**
   * Gets a list of minimum and maximum x and y dimensions for the glyphs in the font. This is based
   * on the reported min and max values for each glyph and not on the actual outline sizes.
   *
   * @param font the source font
   * @return a list of glyph dimensions for the font
   */
  public static DataDisplayTable listGlyphDimensionBounds(Font font) {
    DataDisplayTable table = new DataDisplayTable("Dimension", "Value");
    table.setAlignment(DataDisplayTable.Align.Left, DataDisplayTable.Align.Right);

    LocaTable locaTable = FontUtils.getLocaTable(font);
    GlyphTable glyfTable = FontUtils.getGlyphTable(font);

    // Initialise boundaries
    int xMin = Integer.MAX_VALUE;
    int yMin = Integer.MAX_VALUE;
    int xMax = Integer.MIN_VALUE;
    int yMax = Integer.MIN_VALUE;

    // Find boundaries
    for (int i = 0; i < locaTable.numGlyphs(); i++) {
      Glyph glyph = glyfTable.glyph(locaTable.glyphOffset(i), locaTable.glyphLength(i));
      if (glyph.dataLength() != 0) {
        if (glyph.dataLength() > 0) {
          xMin = Math.min(xMin, glyph.xMin());
          yMin = Math.min(yMin, glyph.yMin());
          xMax = Math.max(xMax, glyph.xMax());
          yMax = Math.max(yMax, glyph.yMax());
        }
      }
    }

    addTwoColumn(table, "xMin", "%d", xMin);
    addTwoColumn(table, "xMax", "%d", xMax);
    addTwoColumn(table, "yMin", "%d", yMin);
    addTwoColumn(table, "yMax", "%d", yMax);

    return table;
  }

  /**
   * Gets the size of hinting instructions in the glyph table, both in bytes and as a fraction of
   * the glyph table size.
   *
   * @param font the source font
   * @return the amount of hinting that is contained in the font
   */
  public static String hintingSize(Font font) {
    int instrSize = 0;

    LocaTable locaTable = FontUtils.getLocaTable(font);
    GlyphTable glyfTable = FontUtils.getGlyphTable(font);

    // Get hinting information from each glyph
    for (int i = 0; i < locaTable.numGlyphs(); i++) {
      Glyph glyph = glyfTable.glyph(locaTable.glyphOffset(i), locaTable.glyphLength(i));
      instrSize += glyph.instructionSize();
    }

    double percentage = instrSize * 100.0 / glyfTable.headerLength();
    return String.format("%d bytes (%.2f%% of glyf table)", instrSize, percentage);
  }

  /**
   * Gets a list of glyphs in the font that are used as subglyphs and the number of times each
   * subglyph is used as a subglyph
   *
   * @param font the source font
   * @return the number of glyphs in the font that are used as subglyphs of other glyphs more than
   *     once
   */
  public static DataDisplayTable listSubglyphFrequency(Font font) {
    DataDisplayTable table = new DataDisplayTable("Glyph ID", "Frequency");
    table.setAlignment(DataDisplayTable.Align.Right, DataDisplayTable.Align.Right);

    LocaTable locaTable = FontUtils.getLocaTable(font);
    GlyphTable glyfTable = FontUtils.getGlyphTable(font);

    // Add subglyphs of all composite glyphs to hashmap
    Map<Integer, Integer> subglyphFreq = new HashMap<>();
    for (int i = 0; i < locaTable.numGlyphs(); i++) {
      Glyph glyph = glyfTable.glyph(locaTable.glyphOffset(i), locaTable.glyphLength(i));
      if (glyph.glyphType() == Glyph.GlyphType.Composite) {
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
    Set<Integer> sortedKeySet = new TreeSet<>(subglyphFreq.keySet());
    for (Integer key : sortedKeySet) {
      table.add(key.toString(), subglyphFreq.get(key).toString());
    }

    return table;
  }

  /**
   * Gets a list of IDs for glyphs that are not mapped by any cmap in the font
   *
   * @param font the source font
   * @return a list of unmapped glyphs
   */
  public static DataDisplayTable listUnmappedGlyphs(Font font) {
    DataDisplayTable table = new DataDisplayTable("Glyph ID");
    table.setAlignment(DataDisplayTable.Align.Right);

    // Get a set of all mapped glyph IDs
    Set<Integer> mappedGlyphs = new HashSet<>();
    CMapTable cmapTable = FontUtils.getCMapTable(font);
    for (CMap cmap : cmapTable) {
      for (Integer codePoint : cmap) {
        mappedGlyphs.add(cmap.glyphId(codePoint));
      }
    }

    // Iterate through all glyph IDs and check if in the set
    LocaTable locaTable = FontUtils.getLocaTable(font);
    for (int i = 0; i < locaTable.numGlyphs(); i++) {
      if (!mappedGlyphs.contains(i)) {
        table.add(String.format("%s", i));
      }
    }

    return table;
  }

  private static void addTwoColumn(
      DataDisplayTable table, String label, String format, Object... args) {
    table.add(label, String.format(format, args));
  }

  // TODO Calculate savings of subglyphs
  // public static int subglyphSavings(Font font) {}

  // TODO Find the maximum glyph nesting depth in a font
  // public static int glyphNestingDepth(Font font) {}

  // TODO Find the maximum glyph nexting depth in a font using the maxp table
  // public static int glyphNestingDepthMaxp(Font font) {}

  // TODO Find number of code points that use simple glyphs and number of code
  // points that use composite glyphs (and provide a list of code points for
  // each one)
  // public static int listSimpleGlyphs(Font font) {}
  // public static int listCompositeGlyphs(Font font) {}

}

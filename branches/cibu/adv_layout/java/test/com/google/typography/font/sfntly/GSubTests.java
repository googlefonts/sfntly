// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly;

import com.google.typography.font.sfntly.table.core.CMap.CMapFormat;
import com.google.typography.font.sfntly.table.core.CMapFormat4;
import com.google.typography.font.sfntly.table.core.CMapFormat4.Builder.Segment;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.sfntly.table.core.CMapTable.CMapId;
import com.google.typography.font.sfntly.table.opentype.FeatureTag;
import com.google.typography.font.sfntly.table.opentype.GSubTable;
import com.google.typography.font.sfntly.table.opentype.GsubCommonTable;
import com.google.typography.font.sfntly.table.opentype.GsubLookupTable;
import com.google.typography.font.sfntly.table.opentype.LanguageTag;
import com.google.typography.font.sfntly.table.opentype.LookupTable;
import com.google.typography.font.sfntly.table.opentype.ScriptTag;
import com.google.typography.font.sfntly.table.opentype.TableDump;
import com.google.typography.font.sfntly.testutils.TestFont.TestFontNames;
import com.google.typography.font.sfntly.testutils.TestFontUtils;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public class GSubTests extends TestCase {
  private static List<FontEntry> _gSubFontList;

  private static List<FontEntry> getGsubFontList() {
    if (_gSubFontList == null) {
      _gSubFontList = createGsubFontList();
    }
    return _gSubFontList;
  }

  private static class FontEntry {
    private final String fileName;
    private final int index;
    private final Font font;

    private FontEntry(String fileName, int index, Font font) {
      this.fileName = fileName;
      this.index = index;
      this.font = font;
    }

    @Override
    public String toString() {
      return String.format("'%s'(%d)", fileName.substring(fileName.lastIndexOf('/')), index);
    }
  }

  private static List<FontEntry> createGsubFontList() {
    List<FontEntry> list = new ArrayList<FontEntry>();
    for (TestFontNames name : TestFontNames.values()) {
      Font[] fonts;
      try {
        fonts = TestFontUtils.loadFont(name.getFile());
        assertNotNull(fonts);
      } catch (IOException e) {
        System.out.format("caught exception (%s) when loading font %s\n", e.getMessage(), name);
        continue;
      }
      for (int i = 0; i < fonts.length; ++i) {
        Font font = fonts[i];
        if (font.hasTable(Tag.GSUB)) {
          System.out.format("Font %s(%d) has GSUB\n", name, i);
          list.add(new FontEntry(name.getFile().toString(), i, font));
        }
      }
    }
    return list;
  }

  public void testGSubFiles() {
    TableDump td = new TableDump();
    for (FontEntry entry : getGsubFontList()) {
      td.println();
      td.println(entry);
      GSubTable gSubTable = entry.font.getTable(Tag.GSUB);
      td.dump(gSubTable);
      td.flush();
    }
  }

  public void testGsubCommonTables() {
    List<FontEntry> list = getGsubFontList();
    if (list.size() == 0) {
      return;
    }
    FontEntry entry = list.get(0);
    TableDump td = new TableDump();
    td.formatln("Common Tables, font: " + entry);
    GSubTable gsub = entry.font.getTable(Tag.GSUB);
    td.dump(gsub);

    td.println();
    td.println("Lookups for mlym_DFLT");
    Iterable<LookupTable> lookups = gsub.lookups(
        ScriptTag.mlym, LanguageTag.DFLT, EnumSet.allOf(FeatureTag.class));
    for (LookupTable table : lookups) {
      td.dumpLookup(table);
    }
    td.flush();
  }

  // example
  public void testCMapBuilder() {
    Font.Builder fontBuilder = FontFactory.getInstance().newFontBuilder();
    CMapTable.Builder cmapTableBuilder = (CMapTable.Builder) fontBuilder.newTableBuilder(Tag.cmap);
    CMapFormat4.Builder cmapBuilder = (CMapFormat4.Builder) cmapTableBuilder.newCMapBuilder(
        CMapId.WINDOWS_BMP, CMapFormat.Format4);
    cmapBuilder.setLanguage(0);
    cmapBuilder.getSegments().add(new Segment(0, 10, 0, 0));
    Font font = fontBuilder.build();
    assertEquals(1, font.numTables());
  }

  public void testGSubBuilder() {
    Font.Builder fontBuilder = FontFactory.getInstance().newFontBuilder();
    GSubTable.Builder gsubTableBuilder = (GSubTable.Builder) fontBuilder.newTableBuilder(Tag.GSUB);
    GsubCommonTable.Builder commonBuilder = gsubTableBuilder.commonBuilder();
    LangSysId<GsubLookupTable> langSysId = commonBuilder.newLangSys(
        ScriptTag.latn, LanguageTag.ENG);
    FeatureId<GsubLookupTable> featureId = commonBuilder.newFeature(FeatureTag.dlig);
    GsubLookupSingle.Builder lookupBuilder = new GsubLookupSingle.Builder();
    lookupBuilder.addFmt1Builder().setDeltaGlyphId(100).addRange(10, 19);
    LookupId<GsubLookupTable> lookupId = commonBuilder.newLookup(lookupBuilder.build());

    commonBuilder.addFeatureToLangSys(featureId, langSysId);
    commonBuilder.addLookupToFeature(lookupId, featureId);
    GSubTable gsubTable = gsubTableBuilder.build();
    TableDump td = new TableDump();
    td.println();
    td.println("Test GSUB Builder");
    td.dump(gsubTable);
    td.flush();
  }
}

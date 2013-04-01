// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.GsubLookupLigature.LigatureSubTable;
import com.google.typography.font.sfntly.table.opentype.GsubLookupLigature.LigatureSubTable.LigatureSet;
import com.google.typography.font.sfntly.table.opentype.GsubLookupLigature.LigatureSubTable.LigatureTable;

import junit.framework.TestCase;

public class GsubLookupLigatureTests extends TestCase {
  private static final int glyf_c = 0xc;
  private static final int glyf_e = 0xe;
  private static final int glyf_f = 0xf;
  private static final int glyf_t = 0xd;
  private static final int glyf_etc = 0x0edc;
  private static final int glyf_ff = 0x00ff;
  private static final int glyf_fff = 0x0fff;

  static final short[] ligSubData = {
    0x0001,   // 00 format 1
    0x000A,   // 02 offset to coverage table
    0x0002,   // 04 number of ligature sets
    0x0014,   // 06 offset to set 0
    0x0020,   // 08 offset to set 1
    0x0002,   // 0A coverage format table, format 2
    0x0001,   // 0C range count
    glyf_e,   // 0E start, 'e' glyph id
    glyf_f,   // 10 end, 'f' glyph id
    0x0000,   // 12 range index start
    0x0001,   // 14 set 0 starting with e, ligature count
    0x0004,   // 16 offset from 14 to ligature 0 (-> 18)
    glyf_etc, // 18 lig 0 result glyph 'edc'
    0x0003,   // 1A component count
    glyf_t,   // 1C 'd' glyph
    glyf_c,   // 1E 'c' glyph
    0x0002,   // 20 set 1 starting with f, ligature count
    0x0006,   // 22 offset from 20 to ligature 0 (-> 26)
    0x000e,   // 24 offset from 20 to ligature 1 (-> 2e)
    glyf_fff, // 26 lig 0 result glyph 'fff'
    0x0003,   // 28 component count
    glyf_f,   // 2a 'f' glyph
    glyf_f,   // 2c 'f' glyph
    glyf_ff,  // 2e lig 1 result glyph 'ff'
    0x0002,   // 30 component count
    glyf_f,   // 32 'f' glyph
  };

  void assertLigSubTable(LigatureSubTable table) {
    assertTrue(table.covers(glyf_e));
    assertTrue(table.covers(glyf_f));
    LigatureSet set0 = table.ligSetAt(0);
    assertEquals(1, set0.ligatureCount());
    {
      LigatureTable lig0 = set0.ligatureTableAt(0);
      assertEquals(glyf_etc, lig0.ligatureGlyph());
      assertEquals(3, lig0.componentCount());
      assertEquals(glyf_t, lig0.componentAt(1));
      assertEquals(glyf_c, lig0.componentAt(2));
    }
    LigatureSet set1 = table.ligSetAt(1);
    assertEquals(2, set1.ligatureCount());
    {
      LigatureTable lig0 = set1.ligatureTableAt(0);
      assertEquals(3, lig0.componentCount());
      assertEquals(glyf_fff, lig0.ligatureGlyph());
      assertEquals(glyf_f, lig0.componentAt(1));
      assertEquals(glyf_f, lig0.componentAt(2));

      LigatureTable lig1 = set1.ligatureTableAt(1);
      assertEquals(glyf_ff, lig1.ligatureGlyph());
      assertEquals(2, lig1.componentCount());
      assertEquals(glyf_f, lig1.componentAt(1));
    }
  }

  GsubLookupLigature newLigatureLookup() {
    GsubLookupLigature.Builder builder = new GsubLookupLigature.Builder();
    short[] lookupHeaderData = {
        0x0004, // 00 lookup type ligature
        0x0000, // 02 lookup flags mask
        0x0001, // 04 subtable count
        0x0008, // 06 offset to start of subtable 0 (-> 08)
    };
    WritableFontData data = WritableFontData.createWritableFontData(
        (lookupHeaderData.length + ligSubData.length) * 2);
    int pos = 0;
    for (int i = 0; i < lookupHeaderData.length; ++i, pos += 2) {
      data.writeUShort(pos, lookupHeaderData[i]);
    }
    for (int i = 0; i < ligSubData.length; ++i, pos += 2) {
      data.writeUShort(pos, ligSubData[i]);
    }
    return GsubLookupLigature.create(data);
  }

  LigatureSubTable newLigatureSubTableFromData(short[] srcdata) {
    WritableFontData data = WritableFontData.createWritableFontData(srcdata.length * 2);
    for (int i = 0; i < srcdata.length; ++i) {
      data.writeUShort(i * 2, srcdata[i]);
    }
    return LigatureSubTable.create(data);
  }

  public void testCreateLigSubTableFromData() {
    assertLigSubTable(newLigatureSubTableFromData(ligSubData));
  }

  public void testCreateGsubLookupLigatureFromData() {
    GsubLookupLigature lookup = newLigatureLookup();
    assertEquals(1, lookup.subTableCount());
    LigatureSubTable table = lookup.subTableAt(0);
    assertLigSubTable(table);
  }

  public void testBuildGsubLookupLigature() {
    LigatureSubTable.Builder builder = new LigatureSubTable.Builder();
    LigatureSet.Builder setBuilder = builder.setBuilder(glyf_e);
    setBuilder.addLigature(glyf_etc, glyf_t, glyf_c);
    setBuilder = builder.setBuilder(glyf_f);
    setBuilder.addLigature(glyf_fff, glyf_f, glyf_f);
    setBuilder.addLigature(glyf_ff, glyf_f);
    LigatureSubTable table = builder.build();
    assertLigSubTable(table);
  }

  public void testDeleteGsubLookupLigature() {
    GsubLookupLigature table = newLigatureLookup();
    GsubLookupLigature.Builder builder = table.builder();
    LigatureSubTable.Builder subBuilder = builder.subTableBuilderAt(0);
    LigatureSet.Builder setBuilder = subBuilder.setBuilder(glyf_e);
    setBuilder.removeLigature(glyf_etc);
    GsubLookupLigature newTable = builder.build();

    TableDump td = new TableDump();
    td.dump(newTable);
    td.flush();

    LigatureSubTable newSubTable = newTable.subTableAt(0);
    assertFalse(newSubTable.covers(glyf_e));
    assertTrue(newSubTable.covers(glyf_f));
    LigatureSet set0 = newSubTable.ligSetAt(0);
    assertEquals(2, set0.ligatureCount());
    {
      LigatureTable lig0 = set0.ligatureTableAt(0);
      assertEquals(3, lig0.componentCount());
      assertEquals(glyf_fff, lig0.ligatureGlyph());
      assertEquals(glyf_f, lig0.componentAt(1));
      assertEquals(glyf_f, lig0.componentAt(2));

      LigatureTable lig1 = set0.ligatureTableAt(1);
      assertEquals(glyf_ff, lig1.ligatureGlyph());
      assertEquals(2, lig1.componentCount());
      assertEquals(glyf_f, lig1.componentAt(1));
    }
  }
}

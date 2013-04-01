package com.google.typography.font.sfntly.table.opentype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.*;

import com.google.typography.font.sfntly.table.opentype.GsubLookupList.GsubLookupType;
import com.google.typography.font.sfntly.table.opentype.GsubLookupSingle.Fmt1Builder;
import com.google.typography.font.sfntly.table.opentype.GsubLookupSingle.Fmt2Builder;
import com.google.typography.font.sfntly.table.opentype.GsubLookupSingle.SingleSubTable;
import com.google.typography.font.sfntly.table.opentype.LookupTable.LookupFlag;

import org.junit.Test;

public class GsubLookupSingleTests {

  @Test
  public void testFmt1Builder() {
    GsubLookupSingle.Builder builder = new GsubLookupSingle.Builder();

    Fmt1Builder subBuilder = new Fmt1Builder();
    assertNotNull(subBuilder.asFmt1Builder());
    assertNull(subBuilder.asFmt2Builder());
    subBuilder
      .setDeltaGlyphId(2)
      .add(100)
      .addRange(105, 109);

    builder.addSubTableBuilder(subBuilder);
    GsubLookupSingle lookup = builder.build();

    assertEquals(1, lookup.subTableCount());
    assertEquals(GsubLookupSingle.FORMAT_1, lookup.subTableFormatAt(0));

    SingleSubTable subTable = lookup.subTableAt(0);
    assertEquals(GsubLookupType.GSUB_SINGLE, subTable.lookupType());
    assertEquals(102, subTable.mapGlyphId(100));
    assertEquals(107, subTable.mapGlyphId(105));
    assertEquals(108, subTable.mapGlyphId(106));
    assertEquals(109, subTable.mapGlyphId(107));
    assertEquals(110, subTable.mapGlyphId(108));
    assertEquals(111, subTable.mapGlyphId(109));
    assertEquals(0, subTable.mapGlyphId(0));
    assertEquals(104, subTable.mapGlyphId(104));
    assertEquals(110, subTable.mapGlyphId(110));
    assertTrue(subTable.covers(100));
    assertFalse(subTable.covers(104));
  }

  @Test
  public void testFmt2Builder() {
    GsubLookupSingle.Builder builder = new GsubLookupSingle.Builder();

    Fmt2Builder subBuilder = new Fmt2Builder();
    assertNotNull(subBuilder.asFmt2Builder());
    assertNull(subBuilder.asFmt1Builder());
    subBuilder
      .map(50, 60)
      .map(51, 62)
      .map(52, 64)
      .map(53, 66);

    builder.addSubTableBuilder(subBuilder);
    GsubLookupSingle lookup = builder.build();

    assertEquals(1, lookup.subTableCount());
    assertEquals(GsubLookupSingle.FORMAT_2, lookup.subTableFormatAt(0));

    SingleSubTable subTable = lookup.subTableAt(0);
    assertEquals(GsubLookupType.GSUB_SINGLE, subTable.lookupType());

    assertEquals(60, subTable.mapGlyphId(50));
    assertEquals(62, subTable.mapGlyphId(51));
    assertEquals(64, subTable.mapGlyphId(52));
    assertEquals(66, subTable.mapGlyphId(53));
    assertEquals(54, subTable.mapGlyphId(54));
    assertTrue(subTable.covers(50));
    assertFalse(subTable.covers(54));
  }

  @Test
  public void testBuilder() {
    GsubLookupSingle.Builder builder = new GsubLookupSingle.Builder();
    assertEquals(builder.lookupType(), GsubLookupType.GSUB_SINGLE);

    GsubLookupSingle lookup = builder.build();
    assertNull(lookup);

    builder
      .addSubTableBuilderAt(0, new Fmt1Builder()
          .setDeltaGlyphId(2)
          .addRange(100, 105))
      .addSubTableBuilderAt(1, new Fmt2Builder()
          .map(102, 202)
          .map(110, 210));

    lookup = builder.build();
    assertNotNull(lookup);

    assertEquals(GsubLookupType.GSUB_SINGLE, lookup.lookupType());
    assertFalse(lookup.rightToLeft());
    assertFalse(lookup.ignoreBaseGlyphs());
    assertFalse(lookup.ignoreLigatures());
    assertFalse(lookup.ignoreMarks());
    assertFalse(lookup.useMarkFilteringSet());
    assertEquals(-1, lookup.markFilteringSetOffset());
    assertEquals(0, lookup.markAttachmentType());
    assertEquals(2, lookup.subTableCount());
    assertEquals(GsubLookupSingle.FORMAT_1, lookup.subTableFormatAt(0));
    assertEquals(GsubLookupSingle.FORMAT_2, lookup.subTableFormatAt(1));

    assertEquals(102, lookup.mapGlyphId(100));
    assertEquals(210, lookup.mapGlyphId(110));

    new TableDump().dump(lookup);
  }

  @Test
  public void testBuilderOrder() {
    GsubLookupSingle.Builder builder = new GsubLookupSingle.Builder();
    assertEquals(builder.lookupType(), GsubLookupType.GSUB_SINGLE);

    GsubLookupSingle lookup = builder.build();
    assertNull(lookup);

    builder
    .addSubTableBuilderAt(0, new Fmt2Builder()
      .map(102, 202)
      .map(110, 210))
    .addSubTableBuilderAt(1, new Fmt1Builder()
      .setDeltaGlyphId(2)
      .addRange(100, 105))
    .setFlag(LookupFlag.IGNORE_BASE_GLYPHS)
    .setFlag(LookupFlag.RIGHT_TO_LEFT)
    .setFlag(LookupFlag.IGNORE_LIGATURES)
    .setFlag(LookupFlag.IGNORE_MARKS)
    .setMarkFilteringOffset(7)
    .setMarkAttachmentType(5);

    lookup = builder.build();
    assertNotNull(lookup);

    assertEquals(GsubLookupType.GSUB_SINGLE, lookup.lookupType());
    assertTrue(lookup.rightToLeft());
    assertTrue(lookup.ignoreBaseGlyphs());
    assertTrue(lookup.ignoreLigatures());
    assertTrue(lookup.ignoreMarks());
    assertTrue(lookup.useMarkFilteringSet());
    assertEquals(7, lookup.markFilteringSetOffset());
    assertEquals(5, lookup.markAttachmentType());
    assertEquals(2, lookup.subTableCount());
    assertEquals(GsubLookupSingle.FORMAT_2, lookup.subTableFormatAt(0));
    assertEquals(GsubLookupSingle.FORMAT_1, lookup.subTableFormatAt(1));

    assertEquals(202, lookup.mapGlyphId(102));
    assertEquals(105, lookup.mapGlyphId(103));

    new TableDump().dump(lookup);
  }
}

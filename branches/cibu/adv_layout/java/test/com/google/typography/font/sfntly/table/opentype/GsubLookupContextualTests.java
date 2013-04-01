// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.GsubLookupContextual.SubstitutionRecord;
import com.google.typography.font.sfntly.table.opentype.GsubLookupList.GsubLookupType;
import com.google.typography.font.sfntly.table.opentype.IntSet.IntIterator;

import junit.framework.TestCase;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public class GsubLookupContextualTests extends TestCase {

  static final short[] headerData = {
    0x0005, // 00 lookup type contextual
    0x0000, // 02 lookup flags mask
    0x0001, // 04 subtable count
    0x0008, // 06 offset to start of subtable 0 (-> 08)
  };

  // This defines two contexts.  'space dash' is one, it replaces the
  // space (at position 0) using lookup 1.  'dash space' is the other, it
  // replaces its space (at position 1) also using lookup 1.
  static final short glyf_space = 0x0028;
  static final short glyf_dash = 0x005d;
  static final short[] format1Data = {
    0x0001,      // 00 format 1
    0x000a,      // 02 coverage table offset
    0x0002,      // 04 rule set count
    0x0012,      // 06 offset to sub rule set 1
    0x0020,      // 08 offset to sub rule set 2
    0x0001,      // 0a coverage format 1
    0x0002,      // 0c glyph count
    glyf_space,  // 0e
    glyf_dash,   // 10
    0x0001,      // 12 subrule set 1, count 1
    0x0004,      // 14 offset to rule 0 from 12
    0x0002,      // 16 subrule 0, glyphcount 2
    0x0001,      // 18 substitution count
    glyf_dash,   // 1a input[1]
    0x0000,      // 1c substitution position 0
    0x0001,      // 1e lookup index 1
    0x0001,      // 20 subrule set 2, count 1
    0x0004,      // 22 offset to rule 0 from 20
    0x0002,      // 24 subrule 0, glyphcount 2
    0x0001,      // 26 substitution count
    glyf_space,  // 28
    0x0001,      // 2a substitution position 1
    0x0001,      // 2c lookup index 1
  };

  // This defines lookups to be applied to a second glyph of class 1 following
  // a glyph of class 2 or 3.  Class 2 are 'high' glyphs, Class 3 are 'very high'
  // glyphs, and class 1 are mark glyphs.  The lookups each substitute the second
  // glyph in the sequence (the mark, at position 1) with a glyph selected by
  // the lookup index, lookup 1 over a high glyph, and lookup 2 over a very high glyph.
  static final short glyf_hi_1 = 0x30;
  static final short glyf_hi_2 = 0x31;
  static final short glyf_vhi_1 = 0x40;
  static final short glyf_vhi_2 = 0x41;
  static final short glyf_mark_1 = 0x50;
  static final short glyf_mark_2 = 0x51;
  static final short[] format2Data = {
    0x0002,      // 00 format 2
    0x0010,      // 02 coverage table offset
    0x001c,      // 04 class def table offset
    0x0004,      // 06 subclass set count
    0x0000,      // 08 no subclass set for class 0
    0x0000,      // 0a no subclass set for class 1
    0x0032,      // 0c subclass set offset for class 2
    0x0040,      // 0e subclass set offset for class 3
    0x0001,      // 10 coverage table, format 1
    0x0004,      // 12 four glyphs in table
    glyf_hi_1,   // 14 high base glyph
    glyf_hi_2,   // 16 another high base glyph
    glyf_vhi_1,  // 18 very high base glyph
    glyf_vhi_2,  // 1a another very high base glyph
    0x0002,      // 1c class def table, format 2
    0x0003,      // 1e range count
    glyf_hi_1,   // 20 range[0], start glyph id, note ranges in increasing order
    glyf_hi_2,   // 22 range[0], end glyph id
    0x0002,      // 24 range[0], class 2
    glyf_vhi_1,  // 26 range[1], start glyph id
    glyf_vhi_2,  // 28 range[1], end glyph id
    0x0003,      // 2a range[1], class 3
    glyf_mark_1, // 2c range[2], start glyph id
    glyf_mark_2, // 2e range[2], end glyph id
    0x0001,      // 30 range[3], class 1
    0x0001,      // 32 subclass set[2], count 1
    0x0004,      // 34 offset to rule 0 from 32
    0x0002,      // 36 subclass set[2] rule[0] glyph count
    0x0001,      // 38 subclass set[2] rule[0] substitution count
    0x0001,      // 3a subclass set[2] rule[0] class 1
    0x0001,      // 3c subclass set[2] rule[0] sequence index
    0x0001,      // 3e subclass set[2] rule[0] lookup index
    0x0001,      // 40 subclass set[3], count 1
    0x0004,      // 42 offset to rule 0 from 40
    0x0002,      // 44 subclass set[3] rule[0] glyph count
    0x0001,      // 46 subclass set[3] rule[0] substitution count
    0x0001,      // 48 subclass set[3] rule[0] class 1
    0x0001,      // 4a subclass set[3] rule[0] sequence index
    0x0002,      // 4c subclass set[3] rule[0] lookup index
  };

  // This defines lookups to be applied to three-character sequences of
  // ascenders or descenders, x-height glyphs, and descender glyphs.  The first
  // glyph in the sequence is replaced using lookup 1, and the last glyph in the
  // sequence is replaced using lookup 2.
  static final short glyf_a = 'a';
  static final short glyf_b = 'b';
  static final short glyf_d = 'd';
  static final short glyf_g = 'g';
  static final short glyf_o = 'o';
  static final short glyf_y = 'y';
  static final short[] format3Data = {
    0x0003,      // 00 format 3
    0x0003,      // 02 glyph count
    0x0002,      // 04 substitution count
    0x0014,      // 06 coverage[0] offset
    0x0020,      // 08 coverage[1] offset
    0x0028,      // 0a coverage[2] offset
    0x0000,      // 0c substitution[0] sequence index
    0x0001,      // 0e substitution[0] lookup index
    0x0002,      // 10 substitution[1] sequence index
    0x0002,      // 12 substitution[1] lookup index
    0x0001,      // 14 coverage[0] format 1
    0x0004,      // 16 coverage[0] glyph count
    glyf_b,      // 18
    glyf_d,      // 1a
    glyf_g,      // 1c
    glyf_y,      // 1e
    0x0001,      // 20 coverage[1] format 1
    0x0002,      // 22 coverage[1] glyph count
    glyf_a,      // 24
    glyf_o,      // 26
    0x0001,      // 28 coverage[2] format 1
    0x0002,      // 2a coverage[2] glyph count
    glyf_g,      // 2c
    glyf_y,      // 2e
  };

  static GsubLookupContextual createFromData(short[] subTableData) {
    WritableFontData data = WritableFontData.createWritableFontData(
        (headerData.length + subTableData.length) * 2);
    int pos = 0;
    for (int i = 0; i < headerData.length; ++i, pos += 2) {
      data.writeUShort(pos, headerData[i]);
    }
    for (int i = 0; i < subTableData.length; ++i, pos += 2) {
      data.writeUShort(pos, subTableData[i]);
    }
    return GsubLookupContextual.create(data);
  }

  static GsubLookupContextual.Fmt1 createFormat1Sample() {
    GsubLookupContextual lookup = createFromData(format1Data);
    assertEquals(1, lookup.subTableCount());

    return (GsubLookupContextual.Fmt1) lookup.subTableAt(0);
  }

  static GsubLookupContextual.Fmt2 createFormat2Sample() {
    GsubLookupContextual lookup = createFromData(format2Data);
    assertEquals(1, lookup.subTableCount());

    return (GsubLookupContextual.Fmt2) lookup.subTableAt(0);
  }

  static GsubLookupContextual.Fmt3 createFormat3Sample() {
    GsubLookupContextual lookup = createFromData(format3Data);
    assertEquals(1, lookup.subTableCount());

    return (GsubLookupContextual.Fmt3) lookup.subTableAt(0);
  }

  static void assertFormat1Sample(GsubLookupContextual.Fmt1 subTable) {
    assertEquals(GsubLookupType.GSUB_CONTEXTUAL, subTable.lookupType());
    assertEquals(2, subTable.subRuleSetCount());

    GsubLookupContextual.Fmt1.SubRuleSet ruleSet0 = subTable.subRuleSetAt(0);
    assertEquals(1, ruleSet0.ruleCount());

    GsubLookupContextual.Fmt1.SubRuleSet.SubRule subRule0_0 = ruleSet0.subRuleAt(0);
    assertEquals(2, subRule0_0.glyphCount());
    assertEquals(glyf_dash, subRule0_0.inputGlyphIdAt(1));

    assertEquals(1, subRule0_0.substitutionCount());
    assertEquals(0, subRule0_0.substitutionSequenceIndexAt(0));
    assertEquals(1, subRule0_0.substitutionLookupIndexAt(0));

    GsubLookupContextual.Fmt1.SubRuleSet ruleSet1 = subTable.subRuleSetAt(1);
    assertEquals(1, ruleSet0.ruleCount());

    GsubLookupContextual.Fmt1.SubRuleSet.SubRule subRule1_0 = ruleSet1.subRuleAt(0);
    assertEquals(2, subRule1_0.glyphCount());
    assertEquals(glyf_space, subRule1_0.inputGlyphIdAt(1));

    assertEquals(1, subRule1_0.substitutionCount());
    assertEquals(1, subRule1_0.substitutionSequenceIndexAt(0));
    assertEquals(1, subRule1_0.substitutionLookupIndexAt(0));
  }

  static void assertFormat2Sample(GsubLookupContextual.Fmt2 subTable) {
    assertEquals(GsubLookupType.GSUB_CONTEXTUAL, subTable.lookupType());

    ClassDefTable classDefTable = subTable.classDefTable();
    assertEquals(0, classDefTable.glyphClass(glyf_space)); // not in table
    assertEquals(1, classDefTable.glyphClass(glyf_mark_1));
    assertEquals(1, classDefTable.glyphClass(glyf_mark_2));
    assertEquals(2, classDefTable.glyphClass(glyf_hi_1));
    assertEquals(2, classDefTable.glyphClass(glyf_hi_2));
    assertEquals(3, classDefTable.glyphClass(glyf_vhi_1));
    assertEquals(3, classDefTable.glyphClass(glyf_vhi_2));

    assertEquals(4, subTable.subClassSetCount());
    assertNull(subTable.subClassSetAt(0));
    assertNull(subTable.subClassSetAt(1));
    GsubLookupContextual.Fmt2.SubClassSet set0 = subTable.subClassSetAt(2);
    assertEquals(1, set0.ruleCount());
    GsubLookupContextual.Fmt2.SubClassSet.SubClassRule rule0_0 =
        set0.subRuleAt(0);
    assertEquals(2, rule0_0.glyphCount());
    assertEquals(1, rule0_0.classAt(1));
    assertEquals(1, rule0_0.substitutionCount());
    assertEquals(1, rule0_0.substitutionSequenceIndexAt(0));
    assertEquals(1, rule0_0.substitutionLookupIndexAt(0));

    GsubLookupContextual.Fmt2.SubClassSet set1 = subTable.subClassSetAt(3);
    assertEquals(1, set1.ruleCount());
    GsubLookupContextual.Fmt2.SubClassSet.SubClassRule rule1_0 =
        set1.subRuleAt(0);
    assertEquals(2, rule1_0.glyphCount());
    assertEquals(1, rule1_0.classAt(1));
    assertEquals(1, rule1_0.substitutionCount());
    assertEquals(1, rule1_0.substitutionSequenceIndexAt(0));
    assertEquals(2, rule1_0.substitutionLookupIndexAt(0));
  }

  static void assertFormat3Sample(GsubLookupContextual.Fmt3 subTable) {
    assertEquals(GsubLookupType.GSUB_CONTEXTUAL, subTable.lookupType());
    assertEquals(3, subTable.glyphCount());
    CoverageTable cov0 = subTable.coverageTableAt(0);
    assertTrue(cov0.covers(glyf_b));
    assertFalse(cov0.covers(glyf_a));
    CoverageTable cov1 = subTable.coverageTableAt(1);
    assertTrue(cov1.covers(glyf_a));
    assertFalse(cov1.covers(glyf_y));
    CoverageTable cov2 = subTable.coverageTableAt(2);
    assertTrue(cov2.covers(glyf_y));
    assertFalse(cov2.covers(glyf_b));
    assertEquals(2, subTable.subsitutionCount());
    assertEquals(0, subTable.substitutionSequenceIndexAt(0));
    assertEquals(1, subTable.substitutionLookupIndexAt(0));
    assertEquals(2, subTable.substitutionSequenceIndexAt(1));
    assertEquals(2, subTable.substitutionLookupIndexAt(1));
  }

  public void testFormat1Sample() {
    GsubLookupContextual.Fmt1 subTable = createFormat1Sample();
    assertFormat1Sample(subTable);
  }

  public void testFormat2Sample() {
    GsubLookupContextual.Fmt2 subTable = createFormat2Sample();
    assertFormat2Sample(subTable);
  }

  public void testFormat3Sample() {
    GsubLookupContextual.Fmt3 subTable = createFormat3Sample();
    assertFormat3Sample(subTable);
  }

  public void testBuildFormat1() {
    GsubLookupContextual.Builder builder = new GsubLookupContextual.Builder();
    GsubLookupContextual.Fmt1Builder fmt1Builder = builder.addFmt1Builder();
    fmt1Builder.getRuleSetBuilder(glyf_space)
      .addRule(glyf_dash)
        .addSubstitution(new SubstitutionRecord(0, 1));
    fmt1Builder.getRuleSetBuilder(glyf_dash)
      .addRule(glyf_space)
        .addSubstitution(new SubstitutionRecord(1, 1));
    GsubLookupContextual lookup = builder.build();
    GsubLookupContextual.Fmt1 fmt1 = lookup.subTableAt(0).asFmt1();
    assertFormat1Sample(fmt1);
  }

  static void assertEditedFormat1Sample(GsubLookupContextual.Fmt1 subTable) {
    assertEquals(GsubLookupType.GSUB_CONTEXTUAL, subTable.lookupType());
    assertEquals(1, subTable.subRuleSetCount());

    GsubLookupContextual.Fmt1.SubRuleSet ruleSet0 = subTable.subRuleSetAt(0);
    assertEquals(1, ruleSet0.ruleCount());

    GsubLookupContextual.Fmt1.SubRuleSet.SubRule subRule0_0 = ruleSet0.subRuleAt(0);
    assertEquals(2, subRule0_0.glyphCount());
    assertEquals(glyf_space, subRule0_0.inputGlyphIdAt(1));

    assertEquals(1, subRule0_0.substitutionCount());
    assertEquals(1, subRule0_0.substitutionSequenceIndexAt(0));
    assertEquals(1, subRule0_0.substitutionLookupIndexAt(0));
  }

  public void testEditFormat1_deleteRuleSet() {
    GsubLookupContextual.Builder builder = new GsubLookupContextual.Builder();
    GsubLookupContextual.Fmt1 subTable = createFormat1Sample();
    GsubLookupContextual.Fmt1Builder fmt1Builder = builder.addBuilder(subTable.builder());
    fmt1Builder.deleteRuleSetBuilder(glyf_space);
    GsubLookupContextual lookup = builder.build();
    GsubLookupContextual.Fmt1 fmt1 = lookup.subTableAt(0).asFmt1();
    assertEditedFormat1Sample(fmt1);
  }

  public void testEditFormat1_deleteAllRulesFromASet() {
    GsubLookupContextual.Builder builder = new GsubLookupContextual.Builder();
    GsubLookupContextual.Fmt1 subTable = createFormat1Sample();
    GsubLookupContextual.Fmt1Builder fmt1Builder = builder.addBuilder(subTable.builder());
    fmt1Builder.getRuleSetBuilder(glyf_space).deleteRuleAt(0);
    GsubLookupContextual lookup = builder.build();
    GsubLookupContextual.Fmt1 fmt1 = lookup.subTableAt(0).asFmt1();
    assertEditedFormat1Sample(fmt1);
  }

  public void testEditFormat1_deleteAllSets() {
    GsubLookupContextual.Builder builder = new GsubLookupContextual.Builder();
    GsubLookupContextual.Fmt1 subTable = createFormat1Sample();
    GsubLookupContextual.Fmt1Builder fmt1Builder = builder.addBuilder(subTable.builder());
    CoverageTable coverage = subTable.coverage();
    IntIterator ii = coverage.coveredGlyphs();
    while (ii.hasNext()) {
      fmt1Builder.deleteRuleSetBuilder(ii.next());
    }
    GsubLookupContextual lookup = builder.build();
    assertNull(lookup);
  }
}

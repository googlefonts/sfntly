// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.table.opentype.LayoutCommonTable.Builder.FeatureId;
import com.google.typography.font.sfntly.table.opentype.LayoutCommonTable.Builder.LangSysId;
import com.google.typography.font.sfntly.table.opentype.LayoutCommonTable.Builder.LookupId;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.Set;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public class LayoutCommonTableTests extends TestCase {
  @Test
  public void testLayoutCommonTableBuilder() {
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    LangSysId<GsubLookupTable> langSysId = builder.newLangSys(ScriptTag.DFLT, LanguageTag.DFLT);
    FeatureId<GsubLookupTable> featureId = builder.newFeature(FeatureTag.ccmp);

    GsubLookupSingle.Builder lookupBuilder = new GsubLookupSingle.Builder();
    lookupBuilder.addFmt1Builder().setDeltaGlyphId(10).addRange(1, 10);
    GsubLookupSingle lookupTable = lookupBuilder.build();
    assertNotNull(lookupTable);

    LookupId<GsubLookupTable> lookupId = builder.newLookup(lookupTable);

    builder.addFeatureToLangSys(featureId, langSysId);
    builder.addLookupToFeature(lookupId, featureId);

    assertEquals(1, builder.lookupCount());
    TableDump td = new TableDump();
    td.println("LayoutCommonTableTests - 1");
    dumpBuilder(td, builder);

    FeatureId<GsubLookupTable> featureId2 = builder.newFeature(FeatureTag.liga);
    builder.addFeatureToLangSys(featureId2, langSysId);
    builder.addLookupToFeature(lookupId, featureId2);
    td.println();
    td.println("LayoutCommonTableTests - 2");
    dumpBuilder(td, builder);

    td.flush();
  }

  private GsubLookupTable createSampleLookupTable() {
    GsubLookupSingle.Builder lookupBuilder = new GsubLookupSingle.Builder();
    lookupBuilder.addFmt1Builder().setDeltaGlyphId(10).addRange(1, 10);
    return lookupBuilder.build();
  }

  @Test
  public void testNewBuilderIsEmpty() {
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    assertEquals(0, builder.langSysCount());
    assertEquals(0, builder.featureCount());
    assertEquals(0, builder.lookupCount());
  }

  @Test
  public void testNewIdsAddsToCount() {
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    LangSysId<GsubLookupTable> langSysId = builder.newLangSys(ScriptTag.DFLT, LanguageTag.DFLT);
    FeatureId<GsubLookupTable> featureId = builder.newFeature(FeatureTag.ccmp);
    LookupId<GsubLookupTable> lookupId = builder.newLookup(createSampleLookupTable());
    assertEquals(1, builder.langSysCount());
    assertEquals(1, builder.featureCount());
    assertEquals(1, builder.lookupCount());
  }

  @Test
  public void testNewLangSysFeature() {
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    LangSysId<GsubLookupTable> langSysId = builder.newLangSys(ScriptTag.DFLT, LanguageTag.DFLT);
    FeatureId<GsubLookupTable> featureId = builder.newFeature(FeatureTag.ccmp);
    builder.addFeatureToLangSys(featureId, langSysId);
    assertEquals(1, builder.langSysCount());
    assertEquals(1, builder.featureCount());
    assertEquals(0, builder.lookupCount());
  }

  @Test
  public void testNewFeatureLookup() {
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    FeatureId<GsubLookupTable> featureId = builder.newFeature(FeatureTag.ccmp);
    LookupId<GsubLookupTable> lookupId = builder.newLookup(createSampleLookupTable());
    builder.addLookupToFeature(lookupId, featureId);
    assertEquals(0, builder.langSysCount());
    assertEquals(1, builder.featureCount());
    assertEquals(1, builder.lookupCount());
  }

  @Test
  public void testRemoveUnused1() {
    // After a feature is deleted, langSysses that referred only to it remain
    // until deleteUnused is called.
    // a) LangSys -- Feature
    // b) LangSys
    // c) <empty>
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    LangSysId<GsubLookupTable> langSysId = builder.newLangSys(ScriptTag.DFLT, LanguageTag.DFLT);
    FeatureId<GsubLookupTable> featureId = builder.newFeature(FeatureTag.ccmp);
    builder.addFeatureToLangSys(featureId, langSysId);

    featureId.delete();
    assertFalse(langSysId.isDeleted());
    assertTrue(featureId.isDeleted());
    assertEquals(1, builder.langSysCount());
    assertEquals(0, builder.featureCount());

    builder.deleteUnusedIds();
    assertEquals(0, builder.langSysCount());
    assertTrue(langSysId.isDeleted());
  }

  @Test
  public void testRemoveUnused2() {
    // After a lookup is deleted, features that referred only to it will remain
    // until deleteUnused is called.
    // a) Feature -- Lookup
    // b)            Lookup
    // c) <empty>
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    FeatureId<GsubLookupTable> featureId = builder.newFeature(FeatureTag.ccmp);
    LookupId<GsubLookupTable> lookupId = builder.newLookup(createSampleLookupTable());
    builder.addLookupToFeature(lookupId, featureId);

    lookupId.delete();
    assertFalse(featureId.isDeleted());
    assertTrue(lookupId.isDeleted());
    assertEquals(1, builder.featureCount());
    assertEquals(0, builder.lookupCount());

    builder.deleteUnusedIds();
    assertEquals(0, builder.featureCount());
    assertTrue(featureId.isDeleted());
  }

  @Test
  public void testRemoveUnused3() {
    // After a langSys is deleted, features that referred only to it will
    // remain until deleteUnused is called.
    // a) LangSys -- Feature
    // b)            Feature
    // c) <empty>
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    LangSysId<GsubLookupTable> langSysId = builder.newLangSys(ScriptTag.DFLT, LanguageTag.DFLT);
    FeatureId<GsubLookupTable> featureId = builder.newFeature(FeatureTag.ccmp);
    builder.addFeatureToLangSys(featureId, langSysId);

    langSysId.delete();
    assertTrue(langSysId.isDeleted());
    assertFalse(featureId.isDeleted());
    assertEquals(0, builder.langSysCount());
    assertEquals(1, builder.featureCount());

    builder.deleteUnusedIds();
    assertEquals(0, builder.featureCount());
    assertTrue(featureId.isDeleted());
  }

  @Test
  public void testRemoveUnused4() {
    // After a feature is deleted, a lookup that referred only to it will remain
    // until deleteUnused is called.
    // a) Feature -- Lookup
    // b) Feature
    // c) <empty>
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    FeatureId<GsubLookupTable> featureId = builder.newFeature(FeatureTag.ccmp);
    LookupId<GsubLookupTable> lookupId = builder.newLookup(createSampleLookupTable());
    builder.addLookupToFeature(lookupId, featureId);

    featureId.delete();
    assertTrue(featureId.isDeleted());
    assertFalse(lookupId.isDeleted());
    assertEquals(0, builder.featureCount());
    assertEquals(1, builder.lookupCount());

    builder.deleteUnusedIds();
    assertEquals(0, builder.lookupCount());
    assertTrue(lookupId.isDeleted());
  }

  @Test
  public void testRemoveUnused5() {
    // After a langSys is deleted, features and lookups that referred only to it
    // will remain until deleteUnused is called.
    // a) LangSys -- Feature -- Lookup
    // b)            Feature -- Lookup
    // c) <empty>
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    LangSysId<GsubLookupTable> langSysId = builder.newLangSys(ScriptTag.DFLT, LanguageTag.DFLT);
    FeatureId<GsubLookupTable> featureId = builder.newFeature(FeatureTag.ccmp);
    LookupId<GsubLookupTable> lookupId = builder.newLookup(createSampleLookupTable());
    builder.addFeatureToLangSys(featureId, langSysId);
    builder.addLookupToFeature(lookupId, featureId);

    langSysId.delete();
    assertTrue(langSysId.isDeleted());
    assertFalse(featureId.isDeleted());
    assertFalse(lookupId.isDeleted());
    assertEquals(0, builder.langSysCount());
    assertEquals(1, builder.featureCount());
    assertEquals(1, builder.lookupCount());
    assertTrue(featureId.hasLookup(lookupId));

    builder.deleteUnusedIds();
    assertEquals(0, builder.featureCount());
    assertTrue(featureId.isDeleted());
    assertEquals(0, builder.lookupCount());
    assertTrue(lookupId.isDeleted());
  }

  @Test
  public void testRemoveUnused6() {
    // After a lookup is deleted, features and langSysses that referred only to it
    // will remain until deleteUnused is called.
    // a) LangSys -- Feature -- Lookup
    // b) LangSys -- Feature
    // c) <empty>
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    LangSysId<GsubLookupTable> langSysId = builder.newLangSys(ScriptTag.DFLT, LanguageTag.DFLT);
    FeatureId<GsubLookupTable> featureId = builder.newFeature(FeatureTag.ccmp);
    LookupId<GsubLookupTable> lookupId = builder.newLookup(createSampleLookupTable());
    builder.addFeatureToLangSys(featureId, langSysId);
    builder.addLookupToFeature(lookupId, featureId);

    lookupId.delete();
    assertFalse(langSysId.isDeleted());
    assertFalse(featureId.isDeleted());
    assertTrue(lookupId.isDeleted());
    assertEquals(1, builder.langSysCount());
    assertEquals(1, builder.featureCount());
    assertEquals(0, builder.lookupCount());
    assertTrue(langSysId.hasFeature(featureId));

    builder.deleteUnusedIds();
    assertEquals(0, builder.langSysCount());
    assertTrue(langSysId.isDeleted());
    assertEquals(0, builder.featureCount());
    assertTrue(featureId.isDeleted());
  }

  @Test
  public void testRemoveUnused7() {
    // After a feature is deleted, langSysses and lookups that referred only to it
    // will remain until deleteUnused is called.
    // a) LangSys -- Feature -- Lookup
    // b) LangSys               Lookup
    // c) <empty>
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    LangSysId<GsubLookupTable> langSysId = builder.newLangSys(ScriptTag.DFLT, LanguageTag.DFLT);
    FeatureId<GsubLookupTable> featureId = builder.newFeature(FeatureTag.ccmp);
    LookupId<GsubLookupTable> lookupId = builder.newLookup(createSampleLookupTable());
    builder.addFeatureToLangSys(featureId, langSysId);
    builder.addLookupToFeature(lookupId, featureId);

    featureId.delete();
    assertFalse(langSysId.isDeleted());
    assertTrue(featureId.isDeleted());
    assertFalse(lookupId.isDeleted());
    assertEquals(1, builder.langSysCount());
    assertEquals(0, builder.featureCount());
    assertEquals(1, builder.lookupCount());

    assertFalse(langSysId.hasFeature(featureId));
    assertFalse(featureId.hasLookup(lookupId));

    builder.deleteUnusedIds();
    assertEquals(0, builder.langSysCount());
    assertTrue(langSysId.isDeleted());
    assertEquals(0, builder.lookupCount());
    assertTrue(lookupId.isDeleted());
  }

  @Test
  public void testRemoveUnused8() {
    // After a langSys is deleted, features and lookups that referred to it but
    // are still fully used remain after deleteUnused is called.
    // will remain until deleteUnused is called.
    // a) LangSys1 -- Feature1 -- Lookup1
    //              \ Feature2 -- Lookup2
    //    LangSys2  /
    // b)             Feature1 -- Lookup1
    //    LangSys2 -- Feature2 -- Lookup2
    // c) LangSys2 -- Feature2 -- Lookup2
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    LangSysId<GsubLookupTable> langSysId1 = builder.newLangSys(ScriptTag.DFLT, LanguageTag.DFLT);
    LangSysId<GsubLookupTable> langSysId2 = builder.newLangSys(ScriptTag.latn, LanguageTag.DFLT);
    FeatureId<GsubLookupTable> featureId1 = builder.newFeature(FeatureTag.ccmp);
    FeatureId<GsubLookupTable> featureId2 = builder.newFeature(FeatureTag.ccmp);
    LookupId<GsubLookupTable> lookupId1 = builder.newLookup(createSampleLookupTable());
    LookupId<GsubLookupTable> lookupId2 = builder.newLookup(createSampleLookupTable());
    builder.addFeatureToLangSys(featureId1, langSysId1);
    builder.addLookupToFeature(lookupId1, featureId1);
    builder.addFeatureToLangSys(featureId2, langSysId1);
    builder.addFeatureToLangSys(featureId2, langSysId2);
    builder.addLookupToFeature(lookupId2, featureId2);

    langSysId1.delete();
    assertTrue(langSysId1.isDeleted());
    assertFalse(featureId1.isDeleted());
    assertFalse(lookupId1.isDeleted());
    assertFalse(langSysId2.isDeleted());
    assertFalse(featureId2.isDeleted());
    assertFalse(lookupId2.isDeleted());
    assertEquals(1, builder.langSysCount());
    assertEquals(2, builder.featureCount());
    assertEquals(2, builder.lookupCount());
    assertFalse(langSysId1.hasFeature(featureId1));
    assertFalse(langSysId1.hasFeature(featureId2));
    assertTrue(featureId1.hasLookup(lookupId1));
    assertTrue(langSysId2.hasFeature(featureId2));
    assertTrue(featureId2.hasLookup(lookupId2));

    builder.deleteUnusedIds();
    assertTrue(langSysId1.isDeleted());
    assertTrue(featureId1.isDeleted());
    assertTrue(lookupId1.isDeleted());
    assertFalse(langSysId2.isDeleted());
    assertFalse(featureId2.isDeleted());
    assertFalse(lookupId2.isDeleted());
    assertEquals(1, builder.langSysCount());
    assertEquals(1, builder.featureCount());
    assertEquals(1, builder.lookupCount());
  }

  @Test
  public void testRemoveUnused9() {
    // After a lookup is deleted, langSysses and features that referred to it but
    // are still fully used remain after deleteUnused is called.
    // will remain until deleteUnused is called.
    // a) LangSys1 -- Feature1 -- Lookup1
    //              \ Feature2 -- Lookup2
    //    LangSys2  /
    // b) LangSys1 -- Feature1
    //              \
    //    LangSys2 -- Feature2 -- Lookup2
    // c) LangSys1
    //              \
    //    LangSys2 -- Feature2 -- Lookup2
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    LangSysId<GsubLookupTable> langSysId1 = builder.newLangSys(ScriptTag.DFLT, LanguageTag.DFLT);
    LangSysId<GsubLookupTable> langSysId2 = builder.newLangSys(ScriptTag.latn, LanguageTag.DFLT);
    FeatureId<GsubLookupTable> featureId1 = builder.newFeature(FeatureTag.ccmp);
    FeatureId<GsubLookupTable> featureId2 = builder.newFeature(FeatureTag.ccmp);
    LookupId<GsubLookupTable> lookupId1 = builder.newLookup(createSampleLookupTable());
    LookupId<GsubLookupTable> lookupId2 = builder.newLookup(createSampleLookupTable());
    builder.addFeatureToLangSys(featureId1, langSysId1);
    builder.addLookupToFeature(lookupId1, featureId1);
    builder.addFeatureToLangSys(featureId2, langSysId1);
    builder.addFeatureToLangSys(featureId2, langSysId2);
    builder.addLookupToFeature(lookupId2, featureId2);

    lookupId1.delete();
    assertFalse(langSysId1.isDeleted());
    assertFalse(featureId1.isDeleted());
    assertTrue(lookupId1.isDeleted());
    assertFalse(langSysId2.isDeleted());
    assertFalse(featureId2.isDeleted());
    assertFalse(lookupId2.isDeleted());
    assertEquals(2, builder.langSysCount());
    assertEquals(2, builder.featureCount());
    assertEquals(1, builder.lookupCount());
    assertTrue(langSysId1.hasFeature(featureId1));
    assertTrue(langSysId1.hasFeature(featureId2));
    assertFalse(featureId1.hasLookup(lookupId1));
    assertTrue(langSysId2.hasFeature(featureId2));
    assertTrue(featureId2.hasLookup(lookupId2));

    builder.deleteUnusedIds();
    assertFalse(langSysId1.isDeleted());
    assertTrue(featureId1.isDeleted());
    assertTrue(lookupId1.isDeleted());
    assertFalse(langSysId2.isDeleted());
    assertFalse(featureId2.isDeleted());
    assertFalse(lookupId2.isDeleted());
    assertEquals(2, builder.langSysCount());
    assertEquals(1, builder.featureCount());
    assertEquals(1, builder.lookupCount());
  }

  @Test
  public void testRemoveUnused10() {
    // After a lookup is deleted, langSysses and features that referred to it but
    // are still fully used remain after deleteUnused is called.
    // will remain until deleteUnused is called.
    // a) LangSys1 -- Feature1 -- Lookup1
    //    LangSys2 -- Feature2 /
    //              /
    //    LangSys3 -- Feature3 -- Lookup2
    // b) LangSys1 -- Feature1
    //    LangSys2 -- Feature2
    //              /
    //    LangSys3 -- Feature3 -- Lookup2
    // c) LangSys3 -- Feature3 -- Lookup2
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    LangSysId<GsubLookupTable> langSysId1 = builder.newLangSys(ScriptTag.DFLT, LanguageTag.DFLT);
    LangSysId<GsubLookupTable> langSysId2 = builder.newLangSys(ScriptTag.latn, LanguageTag.DFLT);
    LangSysId<GsubLookupTable> langSysId3 = builder.newLangSys(ScriptTag.arab, LanguageTag.DFLT);
    FeatureId<GsubLookupTable> featureId1 = builder.newFeature(FeatureTag.ccmp);
    FeatureId<GsubLookupTable> featureId2 = builder.newFeature(FeatureTag.ccmp);
    FeatureId<GsubLookupTable> featureId3 = builder.newFeature(FeatureTag.dlig);
    LookupId<GsubLookupTable> lookupId1 = builder.newLookup(createSampleLookupTable());
    LookupId<GsubLookupTable> lookupId2 = builder.newLookup(createSampleLookupTable());
    builder.addFeatureToLangSys(featureId1, langSysId1);
    builder.addLookupToFeature(lookupId1, featureId1);
    builder.addFeatureToLangSys(featureId2, langSysId2);
    builder.addLookupToFeature(lookupId1, featureId2);
    builder.addFeatureToLangSys(featureId2, langSysId2);
    builder.addFeatureToLangSys(featureId2, langSysId3);
    builder.addFeatureToLangSys(featureId3, langSysId3);
    builder.addLookupToFeature(lookupId2, featureId3);

    lookupId1.delete();
    assertFalse(langSysId1.isDeleted());
    assertFalse(featureId1.isDeleted());
    assertTrue(lookupId1.isDeleted());
    assertFalse(langSysId2.isDeleted());
    assertFalse(featureId2.isDeleted());
    assertFalse(langSysId3.isDeleted());
    assertFalse(featureId2.isDeleted());
    assertFalse(lookupId2.isDeleted());
    assertEquals(3, builder.langSysCount());
    assertEquals(3, builder.featureCount());
    assertEquals(1, builder.lookupCount());
    assertTrue(langSysId1.hasFeature(featureId1));
    assertFalse(featureId1.hasLookup(lookupId1));
    assertTrue(langSysId2.hasFeature(featureId2));
    assertFalse(featureId2.hasLookup(lookupId1));
    assertTrue(langSysId3.hasFeature(featureId2));
    assertTrue(langSysId3.hasFeature(featureId3));
    assertTrue(featureId3.hasLookup(lookupId2));

    builder.deleteUnusedIds();
    assertTrue(langSysId1.isDeleted());
    assertTrue(featureId1.isDeleted());
    assertTrue(langSysId2.isDeleted());
    assertTrue(featureId2.isDeleted());
    assertFalse(langSysId3.isDeleted());
    assertFalse(featureId3.isDeleted());
    assertFalse(lookupId2.isDeleted());
    assertEquals(1, builder.langSysCount());
    assertEquals(1, builder.featureCount());
    assertEquals(1, builder.lookupCount());
  }

  @Test
  public void testScriptProcessor() {
    TableDump td = new TableDump().silence();
    GsubCommonTable.Builder builder = new GsubCommonTable.Builder();
    ScriptProcessor processor = new ScriptProcessor(builder);
    processor.runScript("langsys DFLT DFLT\n" +
        "feature ccmp\n" +
        "setFeature");
    dumpBuilder(td, builder);
  }

  private <T extends LookupTable> void dumpBuilder(TableDump td,
      LayoutCommonTable.Builder<T> builder) {
    if (td.silenced()) {
      return;
    }
    Set<LangSysId<T>> langSysIds = builder.langSysIds();
    td.println("language systems:");
    td.in();
    for (LangSysId<T> id : langSysIds) {
      td.println(id);
      td.in();
      for (FeatureId<T> fid : builder.featuresForLangSys(id)) {
        td.println(fid);
      }
      td.out();
    }
    td.out();

    Set<FeatureId<T>> featureIds = builder.featureIds();
    td.println("features:");
    td.in();
    for (FeatureId<T> id : featureIds) {
      td.println(id);
      td.in();
      for (LookupId<T> lid : builder.lookupsForFeature(id)) {
        td.println(lid);
      }
      td.out();
    }
    td.out();

    td.println("lookups:");
    td.setListFormat(TableDump.ListFormat.STACK_NUMBER);
    td.dumpCollection(builder.lookupIds());
    td.flush();
  }
}

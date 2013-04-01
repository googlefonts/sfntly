// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import static org.junit.Assert.*;

import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

import org.junit.Test;

public class FeatureListTests {

  @Test
  public void testFeatureListFromData() {
    FeatureList featureList = FeatureList.create(createFeatureListData1(), false);
    // feature list is buggy, we've never edited it
    assertEquals(3, featureList.featureCount());
    assertEquals(FEATURE_LIGA, featureList.featureTagAt(0));
    assertEquals(FEATURE_KERN, featureList.featureTagAt(1));
    assertEquals(FEATURE_LIGA, featureList.featureTagAt(2));

    FeatureTable ligaFeatures = featureList.featureTableAt(0);
    FeatureTableTests.assertFeatureTableData1(ligaFeatures, FEATURE_LIGA, false);

    FeatureTable kernFeatures = featureList.featureTableAt(1);
    FeatureTableTests.assertFeatureTableData1(kernFeatures, FEATURE_KERN, false);
  }

  @Test
  public void testFeatureListBuilderFromNothing() {
    FeatureList.Builder builder = new FeatureList.Builder();
    assertEquals(0, builder.featureCount());

    builder.addFeature(FEATURE_LIGA)
      .appendLookupIndex(2);
    assertEquals(1, builder.featureCount());

    builder.addFeature(FEATURE_KERN)
      .appendLookupIndex(1);
    assertEquals(2, builder.featureCount());

    // add always creates a new builder
    FeatureTable.Builder kernBuilder = builder.addFeature(FEATURE_KERN)
      .insertLookupIndexBefore(0, 2);
    assertEquals(3, builder.featureCount());
    assertEquals(1, kernBuilder.lookupCount());

    // adding an empty feature is counted in the builder
    builder.addFeature(FEATURE_LIGA);
    assertEquals(4, builder.featureCount());

    // an empty feature is not added to the table.
    // features are sorted.
    FeatureList featureList = builder.build();
    assertEquals(3, featureList.featureCount());
    assertEquals(FEATURE_LIGA, featureList.featureTagAt(0));
    assertEquals(FEATURE_KERN, featureList.featureTagAt(1));
    assertEquals(FEATURE_KERN, featureList.featureTagAt(2));

    // the feature list is canonical
    assertTrue(featureList.dataIsCanonical());
  }

  @Test
  public void testFeatureListBuilderFromTable() {
    FeatureList featureList = FeatureList.create(createFeatureListData1(), false);
    FeatureList.Builder builder = new FeatureList.Builder(featureList);
    featureList = builder.build();

    assertTrue(featureList.dataIsCanonical());
    assertEquals(3, featureList.featureCount());
    assertEquals(FEATURE_LIGA, featureList.featureTagAt(0));
    assertEquals(FEATURE_KERN, featureList.featureTagAt(1));
    assertEquals(FEATURE_LIGA, featureList.featureTagAt(2));
  }

  static ReadableFontData createFeatureListData1() {
    WritableFontData data = WritableFontData.createWritableFontData(128);
    writeFeatureListData1(data);
    return data;
  }

  static final int FEATURE_KERN = Tag.intValue("kern");
  static final int FEATURE_LIGA = Tag.intValue("liga");

  static int writeFeatureListData1(WritableFontData data) {
    // This data is non-canonical in multiple ways:
    // 1) it lists features in the wrong order
    // 2) two features reference the same offset
    // 3) feature tables are not adjacent

    // It is valid to have multiple entries with the same tag, they just need
    // to be sorted together.

    final int OFFSET_LIGA = 32;
    final int OFFSET_KERN = 64;

    // number of tables
    data.writeUShort(0, 3);
    // first record
    data.writeULong(2, FEATURE_LIGA);
    data.writeUShort(6, OFFSET_LIGA);
    // second record
    data.writeULong(8, FEATURE_KERN);
    data.writeUShort(12, OFFSET_KERN);
    // third record
    data.writeULong(14, FEATURE_LIGA);
    data.writeUShort(18, OFFSET_LIGA);
    // first feature
    FeatureTableTests.writeFeatureTableData1(data.slice(OFFSET_LIGA));
    // second feature
    int len = FeatureTableTests.writeFeatureTableData1(data.slice(OFFSET_KERN));
    return OFFSET_KERN + len;
  }
}

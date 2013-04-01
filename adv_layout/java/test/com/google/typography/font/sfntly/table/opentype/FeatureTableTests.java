package com.google.typography.font.sfntly.table.opentype;

import static org.junit.Assert.*;

import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.FeatureTable;

import org.junit.Test;

import java.util.Arrays;

public class FeatureTableTests {

  @Test
  public void testFeatureTableFromData() {
    FeatureTable table = new FeatureTable(featureTableData1(), FEATURE_LIGA);
    assertFeatureTableData1(table, FEATURE_LIGA, false);
  }
  
  @Test
  public void testFeatureTableBuilderFromNothing() {
    FeatureTable table = new FeatureTable.Builder(FEATURE_LIGA)
      .appendLookupIndex(3)
      .appendLookupIndex(3)
      .appendLookupIndex(1)
      .appendLookupIndex(8)
      .appendLookupIndex(1)
      .build();
    assertFeatureTableData1(table, FEATURE_LIGA, true);
  }

  @Test
  public void testFeatureTableBuilderFromTable() {
    FeatureTable table = new FeatureTable(featureTableData1(), FEATURE_LIGA);
    FeatureTable.Builder builder = new FeatureTable.Builder(table);
    table = builder.build();
    assertFeatureTableData1(table, FEATURE_LIGA, true);
  }
  
  @Test
  public void testFeatureTableBuilderFromCanonicalTable() {
    FeatureTable table = new FeatureTable(featureTableData1(), FEATURE_LIGA, 
        true);
    FeatureTable.Builder builder = new FeatureTable.Builder(table);
    table = builder.build();
    assertFeatureTableData1(table, FEATURE_LIGA, true);
  }
  
  @Test
  public void testFeatureTableBuilderOps() {
    FeatureTable.Builder builder = new FeatureTable.Builder(FEATURE_LIGA);
    assertEquals(FEATURE_LIGA, builder.featureTag());
    assertEquals(0, builder.lookupCount());
    
    builder.insertLookupIndexBefore(0, 1);
    assertBuilderLookups(builder, new int[] { 1 });
    
    builder.insertLookupIndexBefore(0, 8);
    assertBuilderLookups(builder, new int[] { 8, 1 });
    
    builder.insertLookupIndexBefore(0, 3);
    assertBuilderLookups(builder, new int[] { 3, 8, 1 });
    
    builder.insertLookupIndexBefore(1, 1);
    assertBuilderLookups(builder, new int[] { 3, 1, 8, 1 });
    
    builder.insertLookupIndexBefore(0, 4);
    assertBuilderLookups(builder, new int[] { 4, 3, 1, 8, 1 });
    
    builder.setLookupIndexAt(0, 3);
    assertBuilderLookups(builder, new int[] { 3, 3, 1, 8, 1 });
    
    builder.insertLookupIndexBefore(2, 7);
    assertBuilderLookups(builder, new int[] { 3, 3, 7, 1, 8, 1 });
    
    builder.deleteLookupIndexAt(2);
    assertBuilderLookups(builder, new int[] { 3, 3, 1, 8, 1 });
    
    builder.deleteLookupIndexAt(4);
    assertBuilderLookups(builder, new int[] { 3, 3, 1, 8 });
    
    builder.setLookupIndexes(new int[] { 1, 2, 3, 4, 5 });
    assertBuilderLookups(builder, new int[] { 1, 2, 3, 4, 5 });
    
    builder.delete();
    assertEquals(0, builder.lookupCount());
  }
  
  static final int FEATURE_LIGA = Tag.intValue("liga");
  
  static void assertBuilderLookups(FeatureTable.Builder builder, int[] lookups) {
    assertEquals(lookups.length, builder.lookupCount());
    for (int i = 0; i < lookups.length; ++i) {
      assertEquals("index " + i, lookups[i], builder.lookupIndexAt(i));
    }
  }
  
  static void assertFeatureTableData1(FeatureTable table, int tag, 
      boolean canonical) {
    assertEquals(tag, table.featureTag());
    assertEquals(canonical, table.dataIsCanonical());
    assertEquals(5, table.lookupCount());
    assertEquals(3, table.lookupListIndexAt(0));
    assertEquals(1, table.lookupListIndexAt(4));
    assertTrue(Arrays.equals(new int[] { 3, 3, 1, 8, 1 }, 
        table.lookupListIndices()));

  }

  static ReadableFontData featureTableData1() {
    WritableFontData data = WritableFontData.createWritableFontData(128);
    writeFeatureTableData1(data);
    return data;
  }

  static int writeFeatureTableData1(WritableFontData data) {
    data.writeUShort(FeatureTable.FEATURE_PARAMS_OFFSET, 0);
    data.writeUShort(FeatureTable.LOOKUP_COUNT_OFFSET, 5);
    int pos = FeatureTable.LOOKUP_LIST_INDEX_BASE;
    data.writeUShort(pos, 3);
    pos += 2;
    data.writeUShort(pos, 3);
    pos += 2;
    data.writeUShort(pos, 1);
    pos += 2;
    data.writeUShort(pos, 8);
    pos += 2;
    data.writeUShort(pos, 1);
    pos += 2;
    return pos;
  }
}

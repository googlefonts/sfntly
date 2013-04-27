package com.google.typography.font.sfntly.table.opentype;

import static org.junit.Assert.*;

import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.FeatureTable;
import com.google.typography.font.sfntly.table.opentype.featuretable.Header;

import org.junit.Test;

import java.util.Arrays;

public class FeatureTableTests {

  @Test
  public void testFeatureTableFromData() {
    FeatureTable table = new FeatureTable(featureTableData(), false);
    assertFeatureTableData1(table, FEATURE_LIGA, false);
  }
  
  @Test
  public void testFeatureTableBuilderFromNothing() {
    FeatureTable table = new FeatureTable.Builder()
      .addValues(3, 3, 1)
      .addValues(1, 8, 1)
      .build();
    assertFeatureTableData1(table, FEATURE_LIGA, true);
  }

  @Test
  public void testFeatureTableBuilderFromTable() {
    FeatureTable table = new FeatureTable(featureTableData(), false);
    FeatureTable.Builder builder = new FeatureTable.Builder(table);
    table = builder.build();
    assertFeatureTableData1(table, FEATURE_LIGA, true);
  }
  
  @Test
  public void testFeatureTableBuilderFromCanonicalTable() {
    FeatureTable table = new FeatureTable(featureTableData(), 
        true);
    FeatureTable.Builder builder = new FeatureTable.Builder(table);
    table = builder.build();
    assertFeatureTableData1(table, FEATURE_LIGA, true);
  }
  
  @Test
  public void testFeatureTableBuilderOps() {
    FeatureTable.Builder builder = new FeatureTable.Builder();
    assertEquals(0, builder.valueCount());
    
    builder.addValues(1);
    assertBuilderLookups(builder, new int[] { 1 });
    
    builder.addValues(8);
    assertBuilderLookups(builder, new int[] { 1, 8, 1 });
    
//    builder.insertLookupIndexBefore(0, 3);
//    assertBuilderLookups(builder, new int[] { 3, 8, 1 });
//    
//    builder.insertLookupIndexBefore(1, 1);
//    assertBuilderLookups(builder, new int[] { 3, 1, 8, 1 });
//    
//    builder.insertLookupIndexBefore(0, 4);
//    assertBuilderLookups(builder, new int[] { 4, 3, 1, 8, 1 });
//    
//    builder.setLookupIndexAt(0, 3);
//    assertBuilderLookups(builder, new int[] { 3, 3, 1, 8, 1 });
//    
//    builder.insertLookupIndexBefore(2, 7);
//    assertBuilderLookups(builder, new int[] { 3, 3, 7, 1, 8, 1 });
//    
//    builder.deleteLookupIndexAt(2);
//    assertBuilderLookups(builder, new int[] { 3, 3, 1, 8, 1 });
//    
//    builder.deleteLookupIndexAt(4);
//    assertBuilderLookups(builder, new int[] { 3, 3, 1, 8 });
//    
//    builder.setLookupIndexes(new int[] { 1, 2, 3, 4, 5 });
//    assertBuilderLookups(builder, new int[] { 1, 2, 3, 4, 5 });
//    
//    builder.delete();
//    assertEquals(0, builder.valueCount());
  }
  
  static final int FEATURE_LIGA = Tag.intValue("liga");
  private static final int LOOKUP_COUNT_OFFSET = 2;
  private static final int LOOKUP_LIST_INDEX_BASE = 4;
  
  static void assertBuilderLookups(FeatureTable.Builder builder, int[] lookups) {
    assertEquals(lookups.length, builder.valueCount());
    for (int i = 0; i < lookups.length; ++i) {
      assertEquals("index " + i, lookups[i], builder.valueAt(i));
    }
  }
  
  static void assertFeatureTableData1(FeatureTable table, int tag, 
      boolean canonical) {
//    assertEquals(tag, table.featureTag());
    assertEquals(canonical, table.dataIsCanonical);
    assertEquals(5, table.records().count());
    assertEquals(3, table.valueAt(0));
    assertEquals(1, table.valueAt(4));
//    assertTrue(Arrays.equals(new int[] { 3, 3, 1, 8, 1 }, 
//        table.lookupListIndices()));

  }

  static ReadableFontData featureTableData() {
    WritableFontData data = WritableFontData.createWritableFontData(128);
    writeFeatureTableData1(data);
    return data;
  }

  static int writeFeatureTableData1(WritableFontData data) {
    data.writeUShort(Header.FEATURE_PARAMS_OFFSET, 0);
    data.writeUShort(LOOKUP_COUNT_OFFSET, 5);
    int pos = LOOKUP_LIST_INDEX_BASE;
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

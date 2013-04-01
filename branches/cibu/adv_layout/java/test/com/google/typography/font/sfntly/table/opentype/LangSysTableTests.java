// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import static org.junit.Assert.assertEquals;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

import org.junit.Test;

import java.io.PrintStream;
import java.io.PrintWriter;

public class LangSysTableTests {

  private static ReadableFontData emptyLangSysTableData() {
    WritableFontData data = WritableFontData.createWritableFontData(128);
    writeEmptyLangSysTableData(data);
    return data;
  }
  
  static int writeEmptyLangSysTableData(WritableFontData data) {
    data.writeUShort(LangSysTable.LOOKUP_ORDER_OFFSET, 0);
    data.writeUShort(LangSysTable.REQUIRED_FEATURE_INDEX_OFFSET, LangSysTable.NO_REQUIRED_FEATURE_INDEX);
    data.writeUShort(LangSysTable.FEATURE_COUNT_OFFSET, 0);
    return LangSysTable.FEATURE_INDEX_BASE;
  }
  
  private static void assertEmptyTable(LangSysTable table) {
    assertEquals(LangSysTable.NO_REQUIRED_FEATURE_INDEX, table.requiredFeatureIndex());
    assertEquals(0, table.featureCount());
    assertEquals(0, table.langSysTag());
  }
  
  private static ReadableFontData badLangSysTableData() {
    // This data lists a feature index twice, and also lists a feature
    // lists index that is the same as the required feature index.
    WritableFontData data = WritableFontData.createWritableFontData(128);
    writeBadLangSysTableData(data);
    return data;
  }
  
  static int writeBadLangSysTableData(WritableFontData data) {
    data.writeUShort(LangSysTable.LOOKUP_ORDER_OFFSET, 0);
    data.writeUShort(LangSysTable.REQUIRED_FEATURE_INDEX_OFFSET, 1);
    data.writeUShort(LangSysTable.FEATURE_COUNT_OFFSET, 3);
    int offset = LangSysTable.FEATURE_INDEX_BASE;
    data.writeUShort(offset, 1);
    offset += LangSysTable.FEATURE_INDEX_SIZE;
    data.writeUShort(offset, 2);
    offset += LangSysTable.FEATURE_INDEX_SIZE;
    data.writeUShort(offset, 2);
    offset += LangSysTable.FEATURE_INDEX_SIZE;
    return offset;
  }
  
  private static void assertBadTable(LangSysTable table) {
    assertEquals(1, table.requiredFeatureIndex());
    assertEquals(3, table.featureCount());
  }
  
  private static void assertFixedBadTable(LangSysTable table) {
    assertEquals(1, table.requiredFeatureIndex());
    assertEquals(1, table.featureCount());
    assertEquals(2, table.featureIndexAt(0));
  }
  
  @Test
  public void testCreateTableFromNullData() {
    LangSysTable table = LangSysTable.create(null, 0);
    assertEmptyTable(table);
    
    LangSysTable.Builder builder = new LangSysTable.Builder(table);
    LangSysTable newTable = builder.build();
    assertEmptyTable(table);
  }
  
  @Test
  public void testCreateEmptyTableFromData() {
    LangSysTable table = LangSysTable.create(emptyLangSysTableData(), 0);
    assertEmptyTable(table);
    
    LangSysTable.Builder builder = new LangSysTable.Builder(table);
    LangSysTable newTable = builder.build();
    assertEmptyTable(table);
    
    builder = new LangSysTable.Builder(emptyLangSysTableData(), 0, true);
    newTable = builder.build();
    assertEmptyTable(table);
  }
  
  @Test
  public void testCreateBadTableFromData() {
    LangSysTable table = LangSysTable.create(badLangSysTableData(), 0);
    // We just use the table as it was passed to us.
    assertBadTable(table);
    
    LangSysTable.Builder builder = new LangSysTable.Builder(table);
    LangSysTable newTable = builder.build();
    // The builder hasn't been asked to edit the table data, so nothing changes.
    assertBadTable(newTable);
    
    builder = new LangSysTable.Builder(table);
    builder.prepareToEdit();
    newTable = builder.build();
    // Editing the table fixes it.
    assertFixedBadTable(newTable);
  }
  
  @Test
  public void testCreateTableFromBuilder() {
    LangSysTable table = new LangSysTable.Builder(0)
      .setFeatureIndices(1, 2, 2)
      .setRequiredFeatureIndex(1) // removes the previously set feature index
      .build();
    assertFixedBadTable(table);
  }
  
  @Test
  public void testCreateTableFromBuilderAndOverrideRequiredFeature() {
    LangSysTable table = new LangSysTable.Builder(0)
      .setRequiredFeatureIndex(1)
      .setFeatureIndices(1, 2, 2) // resets the required feature index
      .build();
    assertEquals(LangSysTable.NO_REQUIRED_FEATURE_INDEX, table.requiredFeatureIndex());
    assertEquals(2, table.featureCount());
  }
  
  @Test
  public void testBuilderDataSize() {
    LangSysTable table = new LangSysTable.Builder(0)
      .setRequiredFeatureIndex(1)
      .setFeatureIndices(1, 2, 2)
      .build();
    
    final int EXPECTED_SIZE = 
        LangSysTable.FEATURE_INDEX_BASE + 2 * LangSysTable.FEATURE_INDEX_SIZE;
    assertEquals(EXPECTED_SIZE, table.dataLength());
  }
  
  static void dumpTable(LangSysTable table, PrintStream out) {
    PrintWriter pw = new PrintWriter(out);
    pw.format("LangSysTable %04x\n", table.langSysTag());
    pw.format("  required index: %d\n", table.requiredFeatureIndex());
    pw.format("  feature count: %s\n", table.featureCount());
    if (table.featureCount() > 0) {
      pw.print("  features:");
      for (int i = 0; i < table.featureCount(); ++i) {
        pw.print(' ');
        pw.print(table.featureIndexAt(i));
      }
      pw.println();
    }
    pw.flush();
  }
}

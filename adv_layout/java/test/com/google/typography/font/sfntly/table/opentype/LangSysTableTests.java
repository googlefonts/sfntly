// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import static org.junit.Assert.assertEquals;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.langsystable.Header;

import org.junit.Test;

import java.io.PrintStream;
import java.io.PrintWriter;

public class LangSysTableTests {

  private static ReadableFontData emptyLangSysTableNewData() {
    WritableFontData data = WritableFontData.createWritableFontData(128);
    writeEmptyLangSysTableData(data);
    return data;
  }
  
  static final int FEATURE_COUNT_OFFSET = 4;
  static final int FEATURE_INDEX_BASE = 6;
  static final int FEATURE_INDEX_SIZE = 2;
  
  static int writeEmptyLangSysTableData(WritableFontData data) {
    data.writeUShort(Header.LOOKUP_ORDER_OFFSET, 0);
    data.writeUShort(Header.REQ_FEATURE_INDEX_OFFSET, Header.NO_REQ_FEATURE);
    data.writeUShort(FEATURE_COUNT_OFFSET, 0);
    return FEATURE_INDEX_BASE;
  }
  
  private static void assertEmptyTable(LangSysTable table) {
    assertEquals(Header.NO_REQ_FEATURE, table.header.requiredFeature);
    assertEquals(0, table.records().count());
  }
  
  private static ReadableFontData badLangSysTableData() {
    // This data lists a feature index twice, and also lists a feature
    // lists index that is the same as the required feature index.
    WritableFontData data = WritableFontData.createWritableFontData(128);
    writeBadLangSysTableData(data);
    return data;
  }
  
  static int writeBadLangSysTableData(WritableFontData data) {
    data.writeUShort(Header.LOOKUP_ORDER_OFFSET, 0);
    data.writeUShort(Header.REQ_FEATURE_INDEX_OFFSET, 1);
    data.writeUShort(FEATURE_COUNT_OFFSET, 3);
    int offset = FEATURE_INDEX_BASE;
    data.writeUShort(offset, 1);
    offset += FEATURE_INDEX_SIZE;
    data.writeUShort(offset, 2);
    offset += FEATURE_INDEX_SIZE;
    data.writeUShort(offset, 2);
    offset += FEATURE_INDEX_SIZE;
    return offset;
  }
  
  private static void assertBadTable(LangSysTable table) {
    assertEquals(1, table.header.requiredFeature);
    assertEquals(3, table.records().count());
  }
  
  private static void assertFixedBadTable(LangSysTable table) {
    assertEquals(1, table.header.requiredFeature);
    assertEquals(1, table.records().count());
    assertEquals(2, table.records().get(0));
  }
  
  @Test
  public void testCreateTableFromNullData() {
    LangSysTable table = new LangSysTable(null, false);
    assertEmptyTable(table);
    
    LangSysTable.Builder builder = new LangSysTable.Builder(table);
    LangSysTable newTable = builder.build();
    assertEmptyTable(table);
  }
  
  @Test
  public void testCreateEmptyTableFromData() {
    LangSysTable table = new LangSysTable(emptyLangSysTableNewData(), true);
    assertEmptyTable(table);
    
    LangSysTable.Builder builder = new LangSysTable.Builder(table);
    LangSysTable newTable = builder.build();
    assertEmptyTable(table);
    
    builder = new LangSysTable.Builder(emptyLangSysTableNewData(), true);
    newTable = builder.build();
    assertEmptyTable(table);
  }
  
  @Test
  public void testCreateBadTableFromData() {
    LangSysTable table = new LangSysTable(badLangSysTableData(), true);
    // We just use the table as it was passed to us.
    assertBadTable(table);
    
    LangSysTable.Builder builder = new LangSysTable.Builder(table);
    LangSysTable newTable = builder.build();
    // The builder hasn't been asked to edit the table data, so nothing changes.
    assertBadTable(newTable);
    
    builder = new LangSysTable.Builder(table);
    //builder.prepareToEdit(); really need it?! (cibu)
    newTable = builder.build();
    // Editing the table fixes it.
    assertFixedBadTable(newTable);
  }
  
  @Test
  public void testCreateTableFromBuilder() {
    LangSysTable table = new LangSysTable.Builder()
      .addFeatureIndices(1, 2, 2)
      .setRequiredFeatureIndex(1) // removes the previously set feature index
      .build();
    assertFixedBadTable(table);
  }
  
  @Test
  public void testCreateTableFromBuilderAndOverrideRequiredFeature() {
    LangSysTable table = new LangSysTable.Builder()
      .setRequiredFeatureIndex(1)
      .addFeatureIndices(1, 2, 2) // resets the required feature index
      .build();
    assertEquals(Header.NO_REQ_FEATURE, table.header.requiredFeature);
    assertEquals(2, table.records().count());
  }
  
  @Test
  public void testBuilderDataSize() {
    LangSysTable table = new LangSysTable.Builder()
      .setRequiredFeatureIndex(1)
      .addFeatureIndices(1, 2, 2)
      .build();
    
    final int EXPECTED_SIZE = 
        FEATURE_INDEX_BASE + 2 * FEATURE_INDEX_SIZE;
    assertEquals(EXPECTED_SIZE, table.dataLength());
  }
  
  static void dumpTable(LangSysTable table, PrintStream out) {
    PrintWriter pw = new PrintWriter(out);
    pw.format("LangSysTableNew\n");
    pw.format("  required index: %d\n", table.header.requiredFeature);
    pw.format("  feature count: %s\n", table.records().count());
    if (table.records().count() > 0) {
      pw.print("  features:");
      for (int i = 0; i < table.records().count(); ++i) {
        pw.print(' ');
        pw.print(table.records().get(i));
      }
      pw.println();
    }
    pw.flush();
  }
}

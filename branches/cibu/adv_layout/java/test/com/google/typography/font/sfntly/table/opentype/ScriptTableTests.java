// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.component.NumRecord;
import com.google.typography.font.sfntly.table.opentype.component.RecordList;
import com.google.typography.font.sfntly.table.opentype.component.TagOffsetRecord;
import com.google.typography.font.sfntly.table.opentype.scripttable.HeaderBuilder;

import org.junit.Test;

public class ScriptTableTests {
  @Test
  public void testCreateTableFromNullData() {
    ScriptTable table = new ScriptTable(null, false);
    assertEmptyTable(table);
    
    ScriptTable.Builder builder = new ScriptTable.Builder(table.readFontData(), table.dataIsCanonical);
    ScriptTable newTable = builder.build();
    assertEmptyTable(table);
  }
  
  @Test
  public void testCreateEmptyTableFromData() {
    ScriptTable table = new ScriptTable(emptyScriptTableData(), false);
    assertEmptyTable(table);
    
    ScriptTable.Builder builder = new ScriptTable.Builder(table.readFontData(), table.dataIsCanonical);
    ScriptTable newTable = builder.build();
    assertEmptyTable(table);
    
    builder = new ScriptTable.Builder(emptyScriptTableData(), true);
    newTable = builder.build();
    assertEmptyTable(table);
  }
  
  @Test
  public void testCreateBadTableFromData() {
    ScriptTable table = new ScriptTable(badScriptTableData(), false);
    // We just use the table as it was passed to us.
    assertBadTable(table);
    
    ScriptTable.Builder builder = new ScriptTable.Builder(table.readFontData(), table.dataIsCanonical);
    ScriptTable newTable = builder.build();
    // A builder created from non-canonical data edits the data and fixes it.
    assertFixedBadTable(newTable);
  }
  
  private static void assertEmptyTable(ScriptTable table) {
    assertNull(table.defaultLangSysTable());
    assertEquals(0, table.recordList().count());
  }
  
  private static void assertBadTable(ScriptTable table) {
    assertNotNull(table.defaultLangSysTable());
    assertEquals(3, table.recordList().count());
  }
  
  private static void assertFixedBadTable(ScriptTable table) {
    assertNotNull(table.defaultLangSysTable());
    assertEquals(1, table.recordList().count());
    assertEquals(LANGSYS_JA, table.recordList().get(0).tag);
    
    // The default table has one feature, as does the JA table.
    // Both have been fixed.
    assertEquals(1, table.defaultLangSysTable().records().count());
    assertEquals(1, table.subTableAt(0).records().count());
  }
  
  private static ReadableFontData emptyScriptTableData() {
    WritableFontData data = WritableFontData.createWritableFontData(128);
    writeEmptyScriptTableData(data);
    return data;
  }

  private static int writeEmptyScriptTableData(WritableFontData data) {
    data.writeUShort(HeaderBuilder.DEFAULT_LANG_SYS_OFFSET, 0);
    data.writeUShort(NumRecord.RECORD_SIZE, 0);
    return HeaderBuilder.DEFAULT_LANG_SYS_OFFSET_LENGTH + RecordList.RECORD_BASE;
  }
  
  private static ReadableFontData badScriptTableData() {
    WritableFontData data = WritableFontData.createWritableFontData(128);
    writeBadScriptTableData(data);
    return data;
  }
  
  static final int LANGSYS_EN = Tag.intValue("ENG ");
  static final int LANGSYS_JA = Tag.intValue("JAN ");
  static final int LANGSYS_ES = Tag.intValue("ESP ");
  
  static int writeBadScriptTableData(WritableFontData data) {
    // This data includes the following issues:
    // 1) It places the default LangSysTable among its list of feature tables.  
    // 2) The index has duplicate LangSysTags.
    // 3) The index is not in order.
    // 4) Two tables in the index share the same data.
    // 5) Tables are not contiguous. 
    // 6) One table is empty.
    //
    // Some of these are actual problems, and some might be ok in practice.
    
    // When repaired, the new data:
    // 1) Fixes the JA table
    // 2) Removes the duplicate entry to the JA table
    // 3) Fixes the default table
    // 4) Removes the entry to the ES table, since it is the default.
    
    // The subtables are small, as is the index.
    int FIRST_TABLE_POS = 32; // JA data
    int SECOND_TABLE_POS = 48; // EN data - empty
    int THIRD_TABLE_POS = 64; // ES data
    
    // The default table is ok.
    data.writeUShort(HeaderBuilder.DEFAULT_LANG_SYS_OFFSET, THIRD_TABLE_POS);
    data.writeUShort(NumRecord.RECORD_SIZE, 3);
    // This first record is ok.
    int offset = HeaderBuilder.DEFAULT_LANG_SYS_OFFSET_LENGTH + RecordList.RECORD_BASE;;
    data.writeULong(offset + TagOffsetRecord.TAG_POS, LANGSYS_JA);
    data.writeUShort(offset + TagOffsetRecord.OFFSET_POS, FIRST_TABLE_POS);
    // The second record is out of order.  The table it points to is empty.
    offset += TagOffsetRecord.RECORD_SIZE;
    data.writeULong(offset + TagOffsetRecord.TAG_POS, LANGSYS_EN);
    data.writeUShort(offset + TagOffsetRecord.OFFSET_POS, SECOND_TABLE_POS);
    // The third record is a duplicate.  It overrides the previous one, however the table
    // it points to is the same.
    offset += TagOffsetRecord.RECORD_SIZE;
    data.writeULong(offset + TagOffsetRecord.TAG_POS, LANGSYS_JA);
    data.writeUShort(offset + TagOffsetRecord.OFFSET_POS, FIRST_TABLE_POS);
    // The forth record points to the default table, which is bad.  The default table
    // takes precedence and this record is removed.
    offset += TagOffsetRecord.RECORD_SIZE;
    data.writeULong(offset + TagOffsetRecord.TAG_POS, LANGSYS_ES);
    data.writeUShort(offset + TagOffsetRecord.OFFSET_POS, THIRD_TABLE_POS);
    
    LangSysTableTests.writeBadLangSysTableData(data.slice(FIRST_TABLE_POS));
    LangSysTableTests.writeEmptyLangSysTableData(data.slice(SECOND_TABLE_POS));
    int len = LangSysTableTests.writeBadLangSysTableData(data.slice(THIRD_TABLE_POS));
    
    return THIRD_TABLE_POS + len;
  }
}

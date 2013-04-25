package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

import java.util.Iterator;

public final class NumRecordList extends RecordList<NumRecord> {
  public NumRecordList(WritableFontData data) {
    super(data);
  }

  public NumRecordList(ReadableFontData data) {
    super(data);
  }

  public static int sizeOfListOfCount(int count) {
    return RecordList.RECORD_BASE + count * NumRecord.RECORD_SIZE;
  }

  public boolean contains(int value) {
    Iterator<NumRecord> iterator = iterator();
    while (iterator.hasNext()) {
      NumRecord record = iterator.next();
      if (record.value == value) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  protected NumRecord getRecordAt(ReadableFontData data, int offset) {
    return new NumRecord(data, offset);
  }

  @Override
  protected int recordSize() {
    return NumRecord.RECORD_SIZE;
  }
}

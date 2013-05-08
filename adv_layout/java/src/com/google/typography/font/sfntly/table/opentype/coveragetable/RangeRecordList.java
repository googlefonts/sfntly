package com.google.typography.font.sfntly.table.opentype.coveragetable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.component.RecordList;

public final class RangeRecordList extends RecordList<RangeRecord> {
  public RangeRecordList(WritableFontData data) {
    super(data);
  }

  public RangeRecordList(ReadableFontData data) {
    super(data);
  }

  public static int sizeOfListOfCount(int count) {
    return RecordList.RECORD_BASE_DEFAULT + count * RangeRecord.RECORD_SIZE;
  }

  @Override
  protected RangeRecord getRecordAt(ReadableFontData data, int offset) {
    return new RangeRecord(data, offset);
  }

  @Override
  protected int recordSize() {
    return RangeRecord.RECORD_SIZE;
  }
}

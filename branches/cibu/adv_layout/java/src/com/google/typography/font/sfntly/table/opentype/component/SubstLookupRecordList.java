package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

import java.util.Iterator;

public final class SubstLookupRecordList extends RecordList<SubstLookupRecord> {
  public SubstLookupRecordList(WritableFontData data) {
    super(data);
  }

  public SubstLookupRecordList(ReadableFontData data, int base) {
    super(data, 0, base);
  }

  public SubstLookupRecordList(ReadableFontData data, int countOffset, int valuesOffset) {
    super(data, 0, countOffset, valuesOffset);
  }

  public static int sizeOfListOfCount(int count) {
    return RecordList.DATA_OFFSET + count * SubstLookupRecord.RECORD_SIZE;
  }

  public SubstLookupRecord getRecordForTag(int tag) {
    Iterator<SubstLookupRecord> iterator = iterator();
    while (iterator.hasNext()) {
      SubstLookupRecord record = iterator.next();
      if (record.sequenceIndex == tag) {
        return record;
      }
    }
    return null;
  }

  @Override
  protected SubstLookupRecord getRecordAt(ReadableFontData data, int offset) {
    return new SubstLookupRecord(data, offset);
  }

  @Override
  protected int recordSize() {
    return SubstLookupRecord.RECORD_SIZE;
  }
}

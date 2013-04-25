package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

import java.util.Iterator;

public final class TagOffsetRecordList extends RecordList<TagOffsetRecord> {
  public TagOffsetRecordList(WritableFontData data) {
    super(data);
  }

  public TagOffsetRecordList(ReadableFontData data) {
    super(data);
  }

  public static int sizeOfListOfCount(int count) {
    return RecordList.RECORD_BASE + count * TagOffsetRecord.RECORD_SIZE;
  }

  public TagOffsetRecord getRecordForTag(int tag) {
    Iterator<TagOffsetRecord> iterator = iterator();
    while (iterator.hasNext()) {
      TagOffsetRecord record = iterator.next();
      if (record.tag == tag) {
        return record;
      }
    }
    return null;
  }
  
  @Override
  protected TagOffsetRecord getRecordAt(ReadableFontData data, int offset) {
    return new TagOffsetRecord(data, offset);
  }

  @Override
  protected int recordSize() {
    return TagOffsetRecord.RECORD_SIZE;
  }
}

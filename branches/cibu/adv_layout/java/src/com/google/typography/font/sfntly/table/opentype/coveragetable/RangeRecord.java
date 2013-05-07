package com.google.typography.font.sfntly.table.opentype.coveragetable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.component.Record;

public final class RangeRecord implements Record {
  public static final int RECORD_SIZE = 6;
  public static final int START_OFFSET = 0;
  public static final int END_OFFSET = 2;
  public static final int START_COVERAGE_INDEX_OFFSET = 4;
  public final int start;
  public final int end;
  public final int startCoverageIndex;

  public RangeRecord(ReadableFontData data, int base) {
    this.start = data.readULongAsInt(base + START_OFFSET);
    this.end = data.readUShort(base + END_OFFSET);
    this.startCoverageIndex = data.readUShort(base + START_COVERAGE_INDEX_OFFSET);
  }

  public RangeRecord(int tag, int offset, int startCoverageIndex) {
    this.start = tag;
    this.end = offset;
    this.startCoverageIndex = startCoverageIndex;
  }

  @Override
  public int writeTo(WritableFontData newData, int base) {
    newData.writeUShort(base + START_OFFSET, start);
    newData.writeUShort(base + END_OFFSET, end);
    return RECORD_SIZE;
  }
}

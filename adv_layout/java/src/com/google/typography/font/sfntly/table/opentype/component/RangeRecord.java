package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

public final class RangeRecord implements Record {
  public static final int RECORD_SIZE = 6;
  public static final int START_OFFSET = 0;
  public static final int END_OFFSET = 2;
  public static final int PROPERTY_OFFSET = 4;
  public final int start;
  public final int end;
  public final int property;

  public RangeRecord(ReadableFontData data, int base) {
    this.start = data.readUShort(base + START_OFFSET);
    this.end = data.readUShort(base + END_OFFSET);
    this.property = data.readUShort(base + PROPERTY_OFFSET);
  }

  public RangeRecord(int tag, int offset, int startCoverageIndex) {
    this.start = tag;
    this.end = offset;
    this.property = startCoverageIndex;
  }

  @Override
  public int writeTo(WritableFontData newData, int base) {
    newData.writeUShort(base + START_OFFSET, start);
    newData.writeUShort(base + END_OFFSET, end);
    return RECORD_SIZE;
  }
}

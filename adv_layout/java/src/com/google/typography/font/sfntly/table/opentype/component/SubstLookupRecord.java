package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

public final class SubstLookupRecord implements Record {
  public static final int RECORD_SIZE = 4;
  public static final int SEQUENCE_INDEX_OFFSET = 0;
  public static final int LOOKUP_LIST_INDEX_OFFSET = 2;
  public final int tag;
  public final int offset;

  public SubstLookupRecord(ReadableFontData data, int base) {
    this.tag = data.readULongAsInt(base + SEQUENCE_INDEX_OFFSET);
    this.offset = data.readUShort(base + LOOKUP_LIST_INDEX_OFFSET);
  }

  public SubstLookupRecord(int tag, int offset) {
    this.tag = tag;
    this.offset = offset;
  }

  @Override
  public int writeTo(WritableFontData newData, int base) {
    newData.writeUShort(base + SEQUENCE_INDEX_OFFSET, tag);
    newData.writeUShort(base + LOOKUP_LIST_INDEX_OFFSET, offset);
    return RECORD_SIZE;
  }
}

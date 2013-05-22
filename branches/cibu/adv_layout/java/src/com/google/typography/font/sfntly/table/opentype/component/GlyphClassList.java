package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

public class GlyphClassList extends NumRecordList {
  public GlyphClassList(WritableFontData data) {
    super(data);
  }

  public GlyphClassList(ReadableFontData data) {
    super(data);
  }

  public GlyphClassList(ReadableFontData data, int countDecrement) {
    super(data, countDecrement);
  }

  public GlyphClassList(ReadableFontData data, int recordBaseOffset, int countDecrement) {
    super(data, 0, recordBaseOffset, countDecrement);
  }

  public GlyphClassList(ReadableFontData data, int base, int recordBaseOffset, int countDecrement) {
    super(data, base, recordBaseOffset, countDecrement);
  }

  public static int sizeOfListOfCount(int count) {
    return RecordList.RECORD_BASE_DEFAULT + count * NumRecord.RECORD_SIZE;
  }

}

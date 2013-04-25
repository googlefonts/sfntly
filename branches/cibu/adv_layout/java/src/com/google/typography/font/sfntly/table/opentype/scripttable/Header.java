package com.google.typography.font.sfntly.table.opentype.scripttable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.LangSysTable;
import com.google.typography.font.sfntly.table.opentype.component.NumRecord;
import com.google.typography.font.sfntly.table.opentype.component.Record;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class Header extends SubTable implements Record {
  private final NumRecord record;

  public Header(ReadableFontData data) {
    super(data);
    record = new NumRecord(data, 0);
  }

  @Override
  public int writeTo(WritableFontData newData, int base) {
    return record.writeTo(newData, base);
  }

  public static class Builder {
    public static final int DEFAULT_LANG_SYS_OFFSET = 0;
    public static final int DEFAULT_LANG_SYS_TAG = 0;
    public static final int DEFAULT_LANG_SYS_OFFSET_LENGTH = NumRecord.RECORD_SIZE;

    private VisibleBuilder<LangSysTable> builder;
    private int subSerializeLength;
    ReadableFontData data;
    boolean dataIsCanonical;

    public Builder() {}

    public Builder(ReadableFontData data,  boolean dataIsCanonical) {
      this.data = data;
      this.dataIsCanonical = dataIsCanonical;

      if (data == null) {
        return;
      }
      NumRecord header = new NumRecord(data, DEFAULT_LANG_SYS_OFFSET);
      if (header.value != DEFAULT_LANG_SYS_TAG) {
        builder = new LangSysTable.Builder(
            data.slice(header.value), dataIsCanonical);
      }
    }

    public VisibleBuilder<LangSysTable> initBuilder() {
      if (builder == null) {
        builder = new LangSysTable.Builder();
      }
      return builder;
    }

    public int subDataSizeToSerialize() {
      if (subSerializeLength != 0) {
        return subSerializeLength;
      }
      if (builder != null) 
      {
        subSerializeLength = DEFAULT_LANG_SYS_OFFSET_LENGTH +
            builder.subDataSizeToSerialize();
      }
      else
      {
        subSerializeLength = (data != null) ?
            data.length() : DEFAULT_LANG_SYS_OFFSET_LENGTH;
      } 
      return subSerializeLength;
    }

    public int subSerialize(WritableFontData newData, int subTableOffset) {
      if (builder != null) {
        subTableOffset += builder.subSerialize(newData.slice(subTableOffset));
      }
      NumRecord header = new NumRecord(
          (builder != null) ? subTableOffset : DEFAULT_LANG_SYS_TAG);
      header.writeTo(newData, DEFAULT_LANG_SYS_OFFSET);
      return subTableOffset;
    }
  }
}
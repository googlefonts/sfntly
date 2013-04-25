package com.google.typography.font.sfntly.table.opentype.scripttable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.LangSysTable;
import com.google.typography.font.sfntly.table.opentype.component.NumRecord;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class HeaderBuilder {
  public static final int DEFAULT_LANG_SYS_OFFSET = 0;
  public static final int DEFAULT_LANG_SYS_TAG = 0;
  public static final int DEFAULT_LANG_SYS_OFFSET_LENGTH = NumRecord.RECORD_SIZE;

  private VisibleBuilder<LangSysTable> builder;
  private int subSerializeLength;
  private NumRecord header;
  ReadableFontData data;
  boolean dataIsCanonical;

  public HeaderBuilder() {}
  
  public HeaderBuilder(ReadableFontData data,  boolean dataIsCanonical) {
    if (header != null) {
      // Already read.
      return;
    }

    this.data = data;
    this.dataIsCanonical = dataIsCanonical;

    if (data == null) {
      return;
    }
    header = new NumRecord(data, DEFAULT_LANG_SYS_OFFSET);
    if (header.value != DEFAULT_LANG_SYS_TAG) {
      builder = new LangSysTable.Builder(
          data.slice(header.value), dataIsCanonical);
    }
  }

  public VisibleBuilder<LangSysTable> builder() {
    if (builder == null) {
      builder = new LangSysTable.Builder();
    }
    return builder;
  }
  
  public int subDataSizeToSerialize() {
    if (subSerializeLength == 0) {
      if (builder == null) {
        if (data == null) {
          subSerializeLength = DEFAULT_LANG_SYS_OFFSET_LENGTH;
        } else {
          subSerializeLength = data.length();
        }
      } else {
        subSerializeLength = DEFAULT_LANG_SYS_OFFSET_LENGTH +
            builder.subDataSizeToSerialize();
      }
    }
    return subSerializeLength;
  }

  public int subSerialize(WritableFontData newData, int subTableOffset) {
    header.writeTo(newData, DEFAULT_LANG_SYS_OFFSET);
    if (builder != null) {
      subTableOffset += builder.subSerialize(newData.slice(subTableOffset));
      return subTableOffset;
    }
    return serializeFromData(newData);
  }

  private int serializeFromData(WritableFontData newData) {
    // The source data must be canonical.
    data.copyTo(newData);
    return data.length();
  }
}
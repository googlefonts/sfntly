package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.langsystable.Header;

public final class TagOffsetRecord implements Record {
  public static final int RECORD_SIZE = 6;
  public static final int TAG_POS = 0;
  public static final int OFFSET_POS = 4;
  public final int tag;
  public final int offset;

  public TagOffsetRecord(ReadableFontData data, int base) {
    this.tag = data.readULongAsInt(base + TAG_POS);
    this.offset = data.readUShort(base + OFFSET_POS);
  }

  public TagOffsetRecord(int tag, int offset) {
    this.tag = tag;
    this.offset = offset;
  }

  @Override
  public int writeTo(WritableFontData newData, int base) {
    newData.writeULong(base + TAG_POS, tag);
    newData.writeUShort(base + OFFSET_POS, offset);
    return RECORD_SIZE;
  }
  
  @Override
  public String toHtml() {
    Class<? extends TagOffsetRecord> clzz = this.getClass();
    StringBuilder sb = new StringBuilder(clzz.getSimpleName() + "\n");
    sb.append("<div>\n");
    sb.append(String.format("tag: 0x%08X\n", tag));
    sb.append("</div>\n");
    sb.append("<div>\n");
    sb.append(String.format("offset: 0x%04X\n", offset));
    sb.append("</div>\n");
    return sb.toString();
  }
}

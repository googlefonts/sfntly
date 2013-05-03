package com.google.typography.font.sfntly.table.opentype.lookuptable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.Record;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public final class Header extends SubTable implements Record {
  public static final int RECORD_SIZE = 4;
  public static final int LOOKUP_TYPE_OFFSET = 0;
  public static final int LOOKUP_FLAG_OFFSET = 2;
  
  public final int lookupType;
  public final int lookupFlag;

  public Header(ReadableFontData data) {
    super(data);
    this.lookupType = data.readUShort(LOOKUP_TYPE_OFFSET);
    this.lookupFlag = data.readUShort(LOOKUP_FLAG_OFFSET);
  }

  public Header(int lookupType, int lookupFlag) {
    super(null);
    this.lookupType = lookupType;
    this.lookupFlag = lookupFlag;
  }

  @Override
  public int writeTo(WritableFontData newData, int base) {
    newData.writeUShort(base + LOOKUP_TYPE_OFFSET, lookupType);
    newData.writeUShort(base + LOOKUP_FLAG_OFFSET, lookupFlag);
    return RECORD_SIZE;
  }
  
  @Override
  public String toHtml() {
    Class<? extends Header> clzz = this.getClass();
    StringBuilder sb = new StringBuilder(clzz.getSimpleName());
    sb.append("\n");
    sb.append("<div>\n");
    sb.append("lookup type: " + lookupType + "\n");
    sb.append("</div>\n");
    sb.append("<div>\n");
    sb.append(String.format("lookup flag: 0x%04X\n", lookupFlag));
    sb.append("</div>\n");
    return sb.toString();
  }
  
  public static class Builder extends VisibleBuilder<Header> {
    public Header builder;

    public Builder() {}

    public Builder(ReadableFontData data,  boolean dataIsCanonical) {
      if (data != null) {
        builder = new Header(data);
      }
    }

    public Builder(Header header) {
      set(header);
    }

    public void set(Header header) {
      setModelChanged();
      builder = header;
    }
    
    @Override
    public int subDataSizeToSerialize() {
      return Header.RECORD_SIZE;
    }

    public int subSerialize(WritableFontData newData, int subTableOffset) {
      return builder.writeTo(newData, 0);
    }
    
    @Override
    public int subSerialize(WritableFontData newData) {
      return builder.writeTo(newData, 0);
    }

    @Override
    public Header subBuildTable(ReadableFontData data) {
      return new Header(data);
    }

    @Override
    public void subDataSet() {
      builder = null;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }
  }
}

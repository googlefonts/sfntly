package com.google.typography.font.sfntly.table.opentype.ligaturesubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.Record;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public final class Header extends SubTable implements Record {
  public static final int RECORD_SIZE = 4;
  public static final int SUBST_FORMAT_OFFSET = 0;
  public static final int COVERAGE_OFFSET = 2;
  
  public final int substFormat;
  public final int coverage;

  public Header(ReadableFontData data) {
    super(data);
    this.substFormat = data.readUShort(SUBST_FORMAT_OFFSET);
    if (this.substFormat != 1) {
      throw new IllegalStateException("Subt format value is " + this.substFormat + " (not 1).");
    }
    this.coverage = data.readUShort(COVERAGE_OFFSET);
  }

  public Header(int lookupType, int lookupFlag) {
    super(null);
    this.substFormat = lookupType;
    this.coverage = lookupFlag;
  }

  @Override
  public int writeTo(WritableFontData newData, int base) {
    newData.writeUShort(base + SUBST_FORMAT_OFFSET, substFormat);
    newData.writeUShort(base + COVERAGE_OFFSET, coverage);
    return RECORD_SIZE;
  }
  
  @Override
  public String toHtml() {
    return null;
  }
  
  public static class Builder extends VisibleBuilder<Header> {
    private Header builder;

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

package com.google.typography.font.sfntly.table.opentype.featuretable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.Record;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public final class Header extends SubTable implements Record {
  public static final int RECORD_SIZE = 2;
  public static final int FEATURE_PARAMS_OFFSET = 0;
  public static final int FEATURE_PARAMS_DEFAULT = 0x0000;
  
  public final int featureParams;

  public Header(ReadableFontData data) {
    super(data);
    this.featureParams = data.readUShort(FEATURE_PARAMS_OFFSET);
  }

  public Header() {
    super(null);
    this.featureParams = FEATURE_PARAMS_DEFAULT;
  }

  @Override
  public int writeTo(WritableFontData newData, int base) {
    return newData.writeUShort(base + FEATURE_PARAMS_OFFSET, featureParams);
  }
  
  
  public static class Builder extends VisibleBuilder<Header> {
    private Header builder;

    public Builder() {}

    public Builder(ReadableFontData data,  boolean dataIsCanonical) {
      builder = (data == null) ? new Header() : new Header(data);
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

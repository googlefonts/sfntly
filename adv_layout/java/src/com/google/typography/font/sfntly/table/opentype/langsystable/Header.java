package com.google.typography.font.sfntly.table.opentype.langsystable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.Record;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public final class Header extends SubTable implements Record {
  public static final int RECORD_SIZE = 4;
  public static final int LOOKUP_ORDER_OFFSET = 0;
  public static final int REQ_FEATURE_INDEX_OFFSET = 2;
  public static final int LOOKUP_ORDER_CONST = 0;
  public static final int NO_REQ_FEATURE = 0xffff;
  
  public final int lookupOrder;
  public final int requiredFeature;

  public Header(ReadableFontData data) {
    super(data);
    this.lookupOrder = data.readUShort(LOOKUP_ORDER_OFFSET);
    if (lookupOrder != LOOKUP_ORDER_CONST) {
      throw new IllegalArgumentException();
    }
    this.requiredFeature = data.readUShort(REQ_FEATURE_INDEX_OFFSET);
  }

  public Header(int requiredFeature) {
    super(null);
    this.lookupOrder = LOOKUP_ORDER_CONST;
    this.requiredFeature = requiredFeature;
  }

  public boolean hasRequiredFeature() {
    return requiredFeature != NO_REQ_FEATURE;
  }

  @Override
  public int writeTo(WritableFontData newData, int base) {
    newData.writeUShort(base + LOOKUP_ORDER_OFFSET, lookupOrder);
    newData.writeUShort(base + REQ_FEATURE_INDEX_OFFSET, requiredFeature);
    return RECORD_SIZE;
  }
  
  
  public static class Builder extends VisibleBuilder<Header> {
    private Header builder;

    public Builder() {}

    public Builder(ReadableFontData data,  boolean dataIsCanonical) {
      builder = (data == null) ? new Header(NO_REQ_FEATURE) : new Header(data);
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

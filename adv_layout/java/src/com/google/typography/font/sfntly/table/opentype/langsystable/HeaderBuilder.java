package com.google.typography.font.sfntly.table.opentype.langsystable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class HeaderBuilder  extends VisibleBuilder<Header> {
  private Header header;
  private Header builder;

  public HeaderBuilder() {}
  
  public HeaderBuilder(ReadableFontData data,  boolean dataIsCanonical) {
    if (header != null) {
      return;       // Already read.
    }
    if (data == null) {
      return;
    }
    header = new Header(data);
  }

  public void set(Header header) {
    prepareToEdit();
    builder = header;
  }
  
  private void prepareToEdit() {
    if (builder == null) {
      initFromData(internalReadData());
      setModelChanged();
    }
  }

  private void initFromData(ReadableFontData data) {
    if (data == null) {
      return;
    }
    builder = new Header(data);
  }

  @Override
  public int subDataSizeToSerialize() {
    return Header.RECORD_SIZE;
  }

  public int subSerialize(WritableFontData newData, int subTableOffset) {
    return header.writeTo(newData, 0);
  }
  
  @Override
  public int subSerialize(WritableFontData newData) {
    return header.writeTo(newData, 0);
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
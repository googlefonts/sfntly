package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;

public abstract class VisibleSubTable extends SubTable {
  public VisibleSubTable(ReadableFontData data) {
    super(data);
  }

  public abstract static class Builder<T extends SubTable> extends SubTable.Builder<T> {

    protected int serializedLength;

    public Builder() {
      super(null);
    }

    public Builder(ReadableFontData data) {
      super(data);
    }

    @Override
    public abstract int subSerialize(WritableFontData newData);

    @Override
    public abstract int subDataSizeToSerialize();

    @Override
    public abstract void subDataSet();

    @Override
    public abstract T subBuildTable(ReadableFontData data);
  }
}
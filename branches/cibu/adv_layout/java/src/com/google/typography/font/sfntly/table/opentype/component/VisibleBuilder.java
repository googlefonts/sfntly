package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;

public abstract 
  class VisibleBuilder<T extends SubTable> extends SubTable.Builder<T> {

    protected int serializedLength;
    
    protected VisibleBuilder() {
      super(null);
    }

    protected VisibleBuilder(ReadableFontData data) {
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
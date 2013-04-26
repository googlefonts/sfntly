package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.TagOffsetsTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class FeatureListTable extends TagOffsetsTable<FeatureTable> {

  public FeatureListTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }
  
  @Override
  protected FeatureTable readSubTable(
      ReadableFontData data, boolean dataIsCanonical) {
    return new FeatureTable(data, 0 /* dummy */, dataIsCanonical);
  }

  public static class Builder extends 
  TagOffsetsTable.Builder<FeatureListTable, FeatureTable> {

    public Builder() {
      super();
    }
    
    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data, 0, false);
    }
    
    @Override
    protected VisibleBuilder<FeatureTable> createSubTableBuilder(
        ReadableFontData data, int tag, boolean dataIsCanonical) {
      return new FeatureTable.Builder(data, tag, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<FeatureTable> createSubTableBuilder() {
      return new FeatureTable.Builder(0 /* dummy */);
    }

    @Override
    protected FeatureListTable readTable(
        ReadableFontData data,
        int baseUnused,
        boolean dataIsCanonical) {
      return new FeatureListTable(data, dataIsCanonical);
    }
  }
}

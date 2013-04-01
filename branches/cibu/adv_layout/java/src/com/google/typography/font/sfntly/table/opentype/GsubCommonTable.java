package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;

public class GsubCommonTable extends LayoutCommonTable<GsubLookupTable> {

  protected GsubCommonTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  @Override
  GsubLookupList createLookupList() {
    return (GsubLookupList) super.createLookupList();
  }

  @Override
  protected LookupList handleCreateLookupList(ReadableFontData data, boolean dataIsCanonical) {
    return new GsubLookupList(data, dataIsCanonical);
  }

  public static class Builder extends LayoutCommonTable.Builder<GsubLookupTable> {

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder() {
      super(null, false);
    }

    @Override
    protected LookupList handleCreateLookupList(ReadableFontData data, boolean dataIsCanonical) {
      return new GsubLookupList(data, dataIsCanonical);
    }

    @Override
    protected GsubCommonTable subBuildTable(ReadableFontData data) {
      return new GsubCommonTable(data, true);
    }

    @Override
    protected LookupList.Builder createLookupListBuilder() {
      return new GsubLookupList.Builder();
    }
  }
}

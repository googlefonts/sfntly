package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;

public class GsubCommonTable extends LayoutCommonTable<GsubLookupTable> {

  protected GsubCommonTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  @Override
  LookupListTable createLookupList() {
    return super.createLookupList();
  }

  @Override
  protected LookupListTable handleCreateLookupList(ReadableFontData data, boolean dataIsCanonical) {
    return new LookupListTable(data, dataIsCanonical);
  }

  public static class Builder extends LayoutCommonTable.Builder<GsubLookupTable> {

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder() {
      super(null, false);
    }

    @Override
    protected LookupListTable handleCreateLookupList(ReadableFontData data, boolean dataIsCanonical) {
      return new LookupListTable(data, dataIsCanonical);
    }

    @Override
    protected GsubCommonTable subBuildTable(ReadableFontData data) {
      return new GsubCommonTable(data, true);
    }

    @Override
    protected LookupListTable.Builder createLookupListBuilder() {
      return new LookupListTable.Builder();
    }
  }
}

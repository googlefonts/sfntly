package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class LookupListTable extends OffsetRecordTable<LookupTableNew> {
  public static final int FIELD_COUNT = 0;

  public LookupListTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  @Override
  protected LookupTableNew readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new LookupTableNew(data, base, dataIsCanonical);
  }

  public static class Builder extends OffsetRecordTable.Builder<LookupListTable, LookupTableNew> {

    @Override
    protected LookupListTable readTable(
        ReadableFontData data, int baseUnused, boolean dataIsCanonical) {
      return new LookupListTable(data, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<LookupTableNew> createSubTableBuilder() {
      return new LookupTableNew.Builder();
    }

    @Override
    protected VisibleBuilder<LookupTableNew> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new LookupTableNew.Builder(data, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<LookupTableNew> createSubTableBuilder(LookupTableNew subTable) {
      return new LookupTableNew.Builder(subTable);
    }

    @Override
    protected void initFields() {
    }

    @Override
    public int fieldCount() {
      return FIELD_COUNT;
    }
  }

  @Override
  public int fieldCount() {
    return FIELD_COUNT;
  }
}

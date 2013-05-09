package com.google.typography.font.sfntly.table.opentype.ligaturesubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.CoverageTableNew;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class InnerArrayFmt1 extends OffsetRecordTable<LigatureSet> {
  public static final int FIELD_COUNT = 1;

  public static final int COVERAGE_INDEX = 0;
  public static final int COVERAGE_DEFAULT = 0;
  public final CoverageTableNew coverage;

  public InnerArrayFmt1(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    int coverageOffset = getField(COVERAGE_INDEX);
    coverage = new CoverageTableNew(data.slice(coverageOffset), 0, dataIsCanonical);
  }

  @Override
  public LigatureSet readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new LigatureSet(data, 0, dataIsCanonical);
  }

  public static class Builder extends OffsetRecordTable.Builder<InnerArrayFmt1, LigatureSet> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder(InnerArrayFmt1 table) {
      super(table);
    }

    @Override
    protected InnerArrayFmt1 readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new InnerArrayFmt1(data, base, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<LigatureSet> createSubTableBuilder() {
      return new LigatureSet.Builder();
    }

    @Override
    protected VisibleBuilder<LigatureSet> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new LigatureSet.Builder(data, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<LigatureSet> createSubTableBuilder(LigatureSet subTable) {
      return new LigatureSet.Builder(subTable);
    }

    @Override
    protected void initFields() {
      setField(COVERAGE_INDEX, COVERAGE_DEFAULT);
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

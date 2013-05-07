package com.google.typography.font.sfntly.table.opentype.multiplesubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.CoverageTableNew;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class GlyphIds extends OffsetRecordTable<Sequence> {
  public static final int FIELD_COUNT = 1;

  public static final int COVERAGE_INDEX = 0;
  public static final int COVERAGE_DEFAULT = 0;
  public final CoverageTableNew coverage;

  public GlyphIds(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    int coverageOffset = getField(COVERAGE_INDEX);
    coverage = new CoverageTableNew(data.slice(coverageOffset), dataIsCanonical);
  }

  public static class Builder extends OffsetRecordTable.Builder<GlyphIds, Sequence> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder(GlyphIds table) {
      super(table);
    }

    @Override
    protected GlyphIds readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new GlyphIds(data, base, dataIsCanonical);
    }

    @Override
    protected void initFields() {
      setField(COVERAGE_INDEX, COVERAGE_DEFAULT);
    }

    @Override
    public int fieldCount() {
      return FIELD_COUNT;
    }

    @Override
    protected VisibleBuilder<Sequence> createSubTableBuilder() {
      return new Sequence.Builder();
    }

    @Override
    protected VisibleBuilder<Sequence> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new Sequence.Builder(data, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<Sequence> createSubTableBuilder(Sequence subTable) {
      return new Sequence.Builder(subTable);
    }
  }

  @Override
  public int fieldCount() {
    return FIELD_COUNT;
  }

  @Override
  public Sequence readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new Sequence(data, dataIsCanonical);
  }
}

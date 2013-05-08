package com.google.typography.font.sfntly.table.opentype.contextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.CoverageTableNew;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class SubRuleSetArray extends OffsetRecordTable<SubRuleSet> {
  public static final int FIELD_COUNT = 1;

  public static final int COVERAGE_INDEX = 0;
  public static final int COVERAGE_DEFAULT = 0;
  public final CoverageTableNew coverage;

  public SubRuleSetArray(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    int coverageOffset = getField(COVERAGE_INDEX);
    coverage = new CoverageTableNew(data.slice(coverageOffset), dataIsCanonical);
  }

  @Override
  public SubRuleSet readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new SubRuleSet(data, 0, dataIsCanonical);
  }

  public static class Builder extends OffsetRecordTable.Builder<SubRuleSetArray, SubRuleSet> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder(SubRuleSetArray table) {
      super(table);
    }

    @Override
    protected SubRuleSetArray readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new SubRuleSetArray(data, base, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<SubRuleSet> createSubTableBuilder() {
      return new SubRuleSet.Builder();
    }

    @Override
    protected VisibleBuilder<SubRuleSet> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new SubRuleSet.Builder(data, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<SubRuleSet> createSubTableBuilder(SubRuleSet subTable) {
      return new SubRuleSet.Builder(subTable);
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

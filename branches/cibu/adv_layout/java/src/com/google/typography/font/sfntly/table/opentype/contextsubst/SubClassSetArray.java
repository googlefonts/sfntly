package com.google.typography.font.sfntly.table.opentype.contextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.ClassDefTable;
import com.google.typography.font.sfntly.table.opentype.CoverageTable;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleSubTable;

public class SubClassSetArray extends OffsetRecordTable<SubClassSet> {
  private static final int FIELD_COUNT = 2;

  private static final int COVERAGE_INDEX = 0;
  private static final int COVERAGE_DEFAULT = 0;
  private static final int CLASS_DEF_INDEX = 1;
  private static final int CLASS_DEF_DEFAULT = 0;

  public final CoverageTable coverage;
  public final ClassDefTable classDef;

  public SubClassSetArray(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    int coverageOffset = getField(COVERAGE_INDEX);
    coverage = new CoverageTable(data.slice(coverageOffset), 0, dataIsCanonical);
    int classDefOffset = getField(CLASS_DEF_INDEX);
    classDef = new ClassDefTable(data.slice(classDefOffset), 0, dataIsCanonical);
  }

  @Override
  public SubClassSet readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new SubClassSet(data, 0, dataIsCanonical);
  }

  private static class Builder extends OffsetRecordTable.Builder<SubClassSetArray, SubClassSet> {

    private Builder() {
      super();
    }

    private Builder(ReadableFontData data, boolean dataIsCanonical, boolean isFmt2) {
      super(data, dataIsCanonical);
    }

    private Builder(SubClassSetArray table) {
      super(table);
    }

    @Override
    protected SubClassSetArray readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new SubClassSetArray(data, base, dataIsCanonical);
    }

    @Override
    protected VisibleSubTable.Builder<SubClassSet> createSubTableBuilder() {
      return new SubClassSet.Builder();
    }

    @Override
    protected VisibleSubTable.Builder<SubClassSet> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new SubClassSet.Builder(data, dataIsCanonical);
    }

    @Override
    protected VisibleSubTable.Builder<SubClassSet> createSubTableBuilder(SubClassSet subTable) {
      return new SubClassSet.Builder(subTable);
    }

    @Override
    protected void initFields() {
      setField(COVERAGE_INDEX, COVERAGE_DEFAULT);
      setField(CLASS_DEF_INDEX, CLASS_DEF_DEFAULT);
    }

    @Override
    protected int fieldCount() {
      return FIELD_COUNT;
    }
  }

  @Override
  public int fieldCount() {
    return FIELD_COUNT;
  }
}

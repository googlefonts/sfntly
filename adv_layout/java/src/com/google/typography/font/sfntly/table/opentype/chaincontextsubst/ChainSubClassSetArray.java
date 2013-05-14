package com.google.typography.font.sfntly.table.opentype.chaincontextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.ClassDefTable;
import com.google.typography.font.sfntly.table.opentype.CoverageTable;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class ChainSubClassSetArray extends OffsetRecordTable<ChainSubRuleSet> {
  public static final int FIELD_COUNT = 4;

  public static final int COVERAGE_INDEX = 0;
  public static final int COVERAGE_DEFAULT = 0;
  public static final int BACKTRACK_CLASS_DEF_INDEX = 1;
  public static final int BACKTRACK_CLASS_DEF_DEFAULT = 0;
  public static final int INPUT_CLASS_DEF_INDEX = 2;
  public static final int INPUT_CLASS_DEF_DEFAULT = 0;
  public static final int LOOK_AHEAD_CLASS_DEF_INDEX = 3;
  public static final int LOOK_AHEAD_CLASS_DEF_DEFAULT = 0;

  public final CoverageTable coverage;
  public final ClassDefTable backtrackClassDef;
  public final ClassDefTable inputClassDef;
  public final ClassDefTable lookAheadClassDef;

  public ChainSubClassSetArray(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    int coverageOffset = getField(COVERAGE_INDEX);
    coverage = new CoverageTable(data.slice(coverageOffset), 0, dataIsCanonical);
    int classDefOffset = getField(BACKTRACK_CLASS_DEF_INDEX);
    backtrackClassDef = new ClassDefTable(data.slice(classDefOffset), 0, dataIsCanonical);
    classDefOffset = getField(INPUT_CLASS_DEF_INDEX);
    inputClassDef = new ClassDefTable(data.slice(classDefOffset), 0, dataIsCanonical);
    classDefOffset = getField(LOOK_AHEAD_CLASS_DEF_INDEX);
    lookAheadClassDef = new ClassDefTable(data.slice(classDefOffset), 0, dataIsCanonical);
  }

  @Override
  public ChainSubRuleSet readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new ChainSubRuleSet(data, 0, dataIsCanonical);
  }

  public static class Builder
      extends OffsetRecordTable.Builder<ChainSubClassSetArray, ChainSubRuleSet> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder(ChainSubClassSetArray table) {
      super(table);
    }

    @Override
    protected ChainSubClassSetArray readTable(
        ReadableFontData data, int base, boolean dataIsCanonical) {
      return new ChainSubClassSetArray(data, base, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<ChainSubRuleSet> createSubTableBuilder() {
      return new ChainSubRuleSet.Builder();
    }

    @Override
    protected VisibleBuilder<ChainSubRuleSet> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new ChainSubRuleSet.Builder(data, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<ChainSubRuleSet> createSubTableBuilder(ChainSubRuleSet subTable) {
      return new ChainSubRuleSet.Builder(subTable);
    }

    @Override
    protected void initFields() {
      setField(COVERAGE_INDEX, COVERAGE_DEFAULT);
      setField(BACKTRACK_CLASS_DEF_INDEX, BACKTRACK_CLASS_DEF_DEFAULT);
      setField(INPUT_CLASS_DEF_INDEX, INPUT_CLASS_DEF_DEFAULT);
      setField(LOOK_AHEAD_CLASS_DEF_INDEX, LOOK_AHEAD_CLASS_DEF_DEFAULT);
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

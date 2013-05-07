package com.google.typography.font.sfntly.table.opentype.ligaturesubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.CoverageTableNew;
import com.google.typography.font.sfntly.table.opentype.NullTable;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class InnerArrayFmt1 extends OffsetRecordTable<NullTable> {
  public static final int FIELD_COUNT = 1;

  public static final int COVERAGE_INDEX = 0;
  public static final int COVERAGE_DEFAULT = 0;
  public final CoverageTableNew coverage;

  public InnerArrayFmt1(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    dumpData();
    int coverageOffset = getField(COVERAGE_INDEX);
    System.out.println("base : " + base);
    System.out.println("coverageOffset : " + coverageOffset);

    coverage = new CoverageTableNew(data.slice(coverageOffset), dataIsCanonical);
  }

  @Override
  public NullTable readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new NullTable(data, base, dataIsCanonical);
  }

  public static class Builder extends OffsetRecordTable.Builder<InnerArrayFmt1, NullTable> {

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
    protected VisibleBuilder<NullTable> createSubTableBuilder() {
      return new NullTable.Builder();
    }

    @Override
    protected VisibleBuilder<NullTable> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new NullTable.Builder(data, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<NullTable> createSubTableBuilder(NullTable subTable) {
      return new NullTable.Builder(subTable);
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

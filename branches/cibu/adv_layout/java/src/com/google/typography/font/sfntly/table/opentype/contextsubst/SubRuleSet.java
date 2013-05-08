package com.google.typography.font.sfntly.table.opentype.contextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class SubRuleSet extends OffsetRecordTable<DoubleRecordTable> {
  public SubRuleSet(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  public static class Builder extends OffsetRecordTable.Builder<SubRuleSet, DoubleRecordTable> {

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder() {
      super();
    }

    public Builder(SubRuleSet table) {
      super(table);
    }

    @Override
    protected SubRuleSet readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new SubRuleSet(data, base, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<DoubleRecordTable> createSubTableBuilder() {
      return new DoubleRecordTable.Builder();
    }

    @Override
    protected VisibleBuilder<DoubleRecordTable> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new DoubleRecordTable.Builder(data, 0, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<DoubleRecordTable> createSubTableBuilder(DoubleRecordTable subTable) {
      return new DoubleRecordTable.Builder(subTable);
    }

    @Override
    protected void initFields() {
    }

    @Override
    public int fieldCount() {
      return 0;
    }
  }

  @Override
  protected DoubleRecordTable readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new DoubleRecordTable(data, base, dataIsCanonical);
  }

  @Override
  public int fieldCount() {
    return 0;
  }
}

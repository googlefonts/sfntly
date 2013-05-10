package com.google.typography.font.sfntly.table.opentype.chaincontextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class ChainSubRuleSet extends OffsetRecordTable<ChainSubRule> {
  public ChainSubRuleSet(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  public static class Builder extends OffsetRecordTable.Builder<ChainSubRuleSet, ChainSubRule> {

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder() {
      super();
    }

    public Builder(ChainSubRuleSet table) {
      super(table);
    }

    @Override
    protected ChainSubRuleSet readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new ChainSubRuleSet(data, base, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<ChainSubRule> createSubTableBuilder() {
      return new ChainSubRule.Builder();
    }

    @Override
    protected VisibleBuilder<ChainSubRule> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new ChainSubRule.Builder(data, 0, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<ChainSubRule> createSubTableBuilder(ChainSubRule subTable) {
      return new ChainSubRule.Builder(subTable);
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
  protected ChainSubRule readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new ChainSubRule(data, base, dataIsCanonical);
  }

  @Override
  public int fieldCount() {
    return 0;
  }
}

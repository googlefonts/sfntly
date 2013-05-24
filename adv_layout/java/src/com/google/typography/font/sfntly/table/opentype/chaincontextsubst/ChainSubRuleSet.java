package com.google.typography.font.sfntly.table.opentype.chaincontextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class ChainSubRuleSet extends ChainSubGenericRuleSet<ChainSubRule> {
  public ChainSubRuleSet(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  @Override
  protected ChainSubRule readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new ChainSubRule(data, base, dataIsCanonical);
  }

  public static class Builder
      extends ChainSubGenericRuleSet.Builder<ChainSubRuleSet, ChainSubRule> {

    public Builder() {
      super();
    }

    public Builder(ChainSubRuleSet table) {
      super(table);
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
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
  }
}

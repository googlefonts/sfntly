package com.google.typography.font.sfntly.table.opentype.chaincontextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;

public class ChainSubRule extends ChainSubGenericRule {
  public ChainSubRule(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  public static class Builder extends ChainSubGenericRule.Builder<ChainSubRule> {
    public Builder() {
      super();
    }

    public Builder(ChainSubRule table) {
      super(table);
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data, base, dataIsCanonical);
    }

    @Override
    public ChainSubRule subBuildTable(ReadableFontData data) {
      return new ChainSubRule(data, 0, true);
    }
  }
}

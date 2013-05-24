package com.google.typography.font.sfntly.table.opentype.chaincontextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;

public class ChainSubClassRule extends ChainSubGenericRule {
  public ChainSubClassRule(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  public static class Builder extends ChainSubGenericRule.Builder<ChainSubClassRule> {
    public Builder() {
      super();
    }

    public Builder(ChainSubClassRule table) {
      super(table);
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data, base, dataIsCanonical);
    }

    @Override
    public ChainSubClassRule subBuildTable(ReadableFontData data) {
      return new ChainSubClassRule(data, 0, true);
    }

  }
}

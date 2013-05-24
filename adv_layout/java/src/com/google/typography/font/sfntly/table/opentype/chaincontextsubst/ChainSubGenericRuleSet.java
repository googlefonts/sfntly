package com.google.typography.font.sfntly.table.opentype.chaincontextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;

public abstract class ChainSubGenericRuleSet<T extends ChainSubGenericRule>
    extends OffsetRecordTable<T> {
  public ChainSubGenericRuleSet(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  @Override
  public int fieldCount() {
    return 0;
  }

  public static abstract class Builder<
      T extends ChainSubGenericRuleSet<?>, S extends ChainSubGenericRule>
      extends OffsetRecordTable.Builder<T, S> {

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder() {
      super();
    }

    public Builder(T table) {
      super(table);
    }

    @Override
    protected void initFields() {
    }

    @Override
    public int fieldCount() {
      return 0;
    }
  }
}

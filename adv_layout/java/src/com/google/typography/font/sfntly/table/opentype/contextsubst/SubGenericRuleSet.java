package com.google.typography.font.sfntly.table.opentype.contextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;

public abstract class SubGenericRuleSet<T extends DoubleRecordTable> extends OffsetRecordTable<T> {
  public SubGenericRuleSet(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  @Override
  public int fieldCount() {
    return 0;
  }

  public abstract static class Builder<T extends SubGenericRuleSet<S>, S extends DoubleRecordTable>
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

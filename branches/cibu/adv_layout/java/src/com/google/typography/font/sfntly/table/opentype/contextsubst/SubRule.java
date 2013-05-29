package com.google.typography.font.sfntly.table.opentype.contextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;

public class SubRule extends DoubleRecordTable {
  public SubRule(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  public static class Builder extends DoubleRecordTable.Builder<SubRule> {
    public Builder() {
      super();
    }

    public Builder(SubRule table) {
      super(table);
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data, base, dataIsCanonical);
    }

    @Override
    public SubRule subBuildTable(ReadableFontData data) {
      return new SubRule(data, 0, true);
    }
  }
}

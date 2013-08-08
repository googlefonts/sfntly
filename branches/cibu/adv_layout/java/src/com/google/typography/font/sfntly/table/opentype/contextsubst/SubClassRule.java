package com.google.typography.font.sfntly.table.opentype.contextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.GlyphClassList;

public class SubClassRule extends DoubleRecordTable {
  public SubClassRule(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  public GlyphClassList inputClasses() {
    return new GlyphClassList(inputGlyphs);
  }

  public static class Builder extends DoubleRecordTable.Builder<SubClassRule> {
    public Builder() {
      super();
    }

    public Builder(SubClassRule table) {
      super(table);
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data, base, dataIsCanonical);
    }

    @Override
    public SubClassRule subBuildTable(ReadableFontData data) {
      return new SubClassRule(data, 0, true);
    }
  }
}

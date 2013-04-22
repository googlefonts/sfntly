package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.RecordsTable;

public class ScriptTableArrayPart extends RecordsTable<LangSysTable> {
  
  public ScriptTableArrayPart(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  @Override
  protected LangSysTable readSubTable(
      ReadableFontData data, boolean dataIsCanonical) {
    return LangSysTable.create(data, 0 /* dummy */);
  }

  public static class Builder extends 
  RecordsTable.Builder<ScriptTableArrayPart, LangSysTable> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data, base, dataIsCanonical);
    }

    public Builder(RecordsTable.Builder<ScriptTableArrayPart, LangSysTable> subTableBuilder) {
      super();
      builders = subTableBuilder.builders;
    }
    
    @Override
    protected VisibleBuilder<LangSysTable> createSubTableBuilder(
        ReadableFontData data, int tag, boolean dataIsCanonical) {
      return new LangSysTable.Builder(data, tag, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<LangSysTable> createSubTableBuilder() {
      return new LangSysTable.Builder(0 /* dummy */);
    }

    @Override
    protected ScriptTableArrayPart readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new ScriptTableArrayPart(data, base, true);
    }
  }
}

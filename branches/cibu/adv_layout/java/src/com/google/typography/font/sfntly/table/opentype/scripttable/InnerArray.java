package com.google.typography.font.sfntly.table.opentype.scripttable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.LangSysTable;
import com.google.typography.font.sfntly.table.opentype.component.TagOffsetsTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class InnerArray extends TagOffsetsTable<LangSysTable> {
  
  public InnerArray(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  @Override
  public LangSysTable readSubTable(
      ReadableFontData data, boolean dataIsCanonical) {
    return new LangSysTable(data, dataIsCanonical);
  }

  public static class Builder extends 
  TagOffsetsTable.Builder<InnerArray, LangSysTable> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data, base, dataIsCanonical);
    }

    public Builder(TagOffsetsTable.Builder<InnerArray, LangSysTable> subTableBuilder) {
      super();
      builders = subTableBuilder.builders;
    }
    
    @Override
    public VisibleBuilder<LangSysTable> createSubTableBuilder(
        ReadableFontData data, int tag, boolean dataIsCanonical) {
      return new LangSysTable.Builder(data, dataIsCanonical);
    }

    @Override
    public VisibleBuilder<LangSysTable> createSubTableBuilder() {
      return new LangSysTable.Builder();
    }

    @Override
    protected InnerArray readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new InnerArray(data, base, dataIsCanonical);
    }
  }
}

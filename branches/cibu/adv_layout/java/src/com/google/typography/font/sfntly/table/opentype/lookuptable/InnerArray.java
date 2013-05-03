package com.google.typography.font.sfntly.table.opentype.lookuptable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.LigatureSubst;
import com.google.typography.font.sfntly.table.opentype.NullTable;
import com.google.typography.font.sfntly.table.opentype.SubstSubtable;
import com.google.typography.font.sfntly.table.opentype.component.GsubLookupType;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class InnerArray extends OffsetRecordTable<SubstSubtable> {

  private int lookupType;

  public InnerArray(ReadableFontData data, int base, boolean dataIsCanonical, int lookupType) {
    super(data, base, dataIsCanonical);
    this.lookupType = lookupType;
  }

  @Override
  public SubstSubtable readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    switch(GsubLookupType.forTypeNum(lookupType)) {
    case GSUB_LIGATURE:
      return new LigatureSubst(data, dataIsCanonical);
    case GSUB_SINGLE:
    case GSUB_MULTIPLE:
    case GSUB_CONTEXTUAL:
    case GSUB_CHAINING_CONTEXTUAL:
      return new NullTable(data, dataIsCanonical);
    default:
      throw new IllegalArgumentException("LookupType is " + lookupType);
    }
  }
  
  public static class Builder extends OffsetRecordTable.Builder<InnerArray, SubstSubtable> {
    private int lookupType;


    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical, int lookupType) {
      super(data, base, dataIsCanonical);
      this.lookupType = lookupType;
    }

    public Builder(InnerArray table) {
      super(table);
      this.lookupType = table.lookupType;
    }

    @Override
    protected InnerArray readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new InnerArray(data, base, dataIsCanonical, lookupType);
    }

    @Override
    protected VisibleBuilder<SubstSubtable> createSubTableBuilder() {
      return new LigatureSubst.Builder();
    }

    @Override
    protected VisibleBuilder<SubstSubtable> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new LigatureSubst.Builder(data, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<SubstSubtable> createSubTableBuilder(SubstSubtable subTable) {
      return new LigatureSubst.Builder(subTable);
    }
  }
}




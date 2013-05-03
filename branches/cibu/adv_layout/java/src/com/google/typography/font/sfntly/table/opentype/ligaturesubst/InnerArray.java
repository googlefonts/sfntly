package com.google.typography.font.sfntly.table.opentype.ligaturesubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.LigatureSubst;
import com.google.typography.font.sfntly.table.opentype.NullTable;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class InnerArray extends OffsetRecordTable<NullTable> {

  public InnerArray(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  @Override
  public NullTable readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new NullTable(data, dataIsCanonical);
  }
  
  public static class Builder extends OffsetRecordTable.Builder<InnerArray, NullTable> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data, base, dataIsCanonical);
    }

    public Builder(InnerArray table) {
      super(table);
    }

    @Override
    protected InnerArray readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new InnerArray(data, base, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<NullTable> createSubTableBuilder() {
      return new NullTable.Builder();
    }

    @Override
    protected VisibleBuilder<NullTable> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new NullTable.Builder(data, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<NullTable> createSubTableBuilder(NullTable subTable) {
      return new NullTable.Builder(subTable);
    }
  }
}

package com.google.typography.font.sfntly.table.opentype.ligaturesubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.NullTable;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class LigatureSet extends OffsetRecordTable<NullTable> {
  public LigatureSet(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  public static class Builder extends OffsetRecordTable.Builder<LigatureSet, NullTable> {

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder() {
      super();
    }

    public Builder(LigatureSet table) {
      super(table);
    }

    @Override
    protected LigatureSet readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new LigatureSet(data, base, dataIsCanonical);
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

    @Override
    protected void initFields() {
    }

    @Override
    public int fieldCount() {
      return 0;
    }
  }

  @Override
  protected NullTable readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new NullTable(data, base, dataIsCanonical);
  }

  @Override
  public int fieldCount() {
    return 0;
  }
}

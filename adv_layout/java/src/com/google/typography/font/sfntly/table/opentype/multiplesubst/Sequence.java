package com.google.typography.font.sfntly.table.opentype.multiplesubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.NumRecord;
import com.google.typography.font.sfntly.table.opentype.component.NumRecordList;
import com.google.typography.font.sfntly.table.opentype.component.RecordList;
import com.google.typography.font.sfntly.table.opentype.component.RecordsTable;

public class Sequence extends RecordsTable<NumRecord> {
  public Sequence(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  @Override
  protected RecordList<NumRecord> createRecordList(ReadableFontData data) {
    return new NumRecordList(data);
  }

  @Override
  public int fieldCount() {
    return 0;
  }

  public static class Builder extends RecordsTable.Builder<Sequence, NumRecord> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder(RecordsTable.Builder<Sequence, NumRecord> builder) {
      super();
      records = builder.records();
    }

    public Builder(Sequence table) {
      super(table);
    }

    @Override
    protected Sequence readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      if (base != 0) {
        throw new UnsupportedOperationException();
      }
      return new Sequence(data, dataIsCanonical);
    }

    @Override
    protected RecordList<NumRecord> readRecordList(ReadableFontData data, int base) {
      if (base != 0) {
        throw new UnsupportedOperationException();
      }
      return new NumRecordList(data);
    }

    @Override
    public int fieldCount() {
      return 0;
    }

    @Override
    protected void initFields() {
    }
  }

}

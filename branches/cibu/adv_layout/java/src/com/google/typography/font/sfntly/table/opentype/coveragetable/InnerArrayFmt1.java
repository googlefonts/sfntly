package com.google.typography.font.sfntly.table.opentype.coveragetable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.NumRecord;
import com.google.typography.font.sfntly.table.opentype.component.NumRecordList;
import com.google.typography.font.sfntly.table.opentype.component.RecordList;
import com.google.typography.font.sfntly.table.opentype.component.RecordsTable;

public class InnerArrayFmt1 extends RecordsTable<NumRecord> {

  public InnerArrayFmt1(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  @Override
  protected RecordList<NumRecord> createRecordList(ReadableFontData data) {
    return new NumRecordList(data);
  }

  @Override
  public int fieldCount() {
    return 0;
  }

  public static class Builder extends RecordsTable.Builder<InnerArrayFmt1, NumRecord> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data, base, dataIsCanonical);
    }

    public Builder(RecordsTable.Builder<InnerArrayFmt1, NumRecord> builder) {
      super();
      records = builder.records();
    }

    @Override
    protected InnerArrayFmt1 readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      if (base != 0) {
        throw new UnsupportedOperationException();
      }
      return new InnerArrayFmt1(data, base, dataIsCanonical);
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

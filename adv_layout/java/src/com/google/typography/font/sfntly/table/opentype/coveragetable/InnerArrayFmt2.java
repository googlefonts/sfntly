package com.google.typography.font.sfntly.table.opentype.coveragetable;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.RecordList;
import com.google.typography.font.sfntly.table.opentype.component.RecordsTable;

public class InnerArrayFmt2 extends RecordsTable<RangeRecord> {
  
  public InnerArrayFmt2(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  @Override
  protected RecordList<RangeRecord> createRecordList(ReadableFontData data) {
    return new RangeRecordList(data);
  }

  @Override
  public int fieldCount() {
    return 0;
  }

  public static class Builder extends 
  RecordsTable.Builder<InnerArrayFmt2, RangeRecord> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder(RecordsTable.Builder<InnerArrayFmt2, RangeRecord> builder) {
      super();
      records = builder.records();
    }
    
    @Override
    protected InnerArrayFmt2 readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      if (base != 0) {
        throw new UnsupportedOperationException();
      }
      return new InnerArrayFmt2(data, dataIsCanonical);
    }

    @Override
    protected RecordList<RangeRecord> readRecordList(ReadableFontData data, int base) {
      if (base != 0) {
        throw new UnsupportedOperationException();
      }      
      return new RangeRecordList(data);
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

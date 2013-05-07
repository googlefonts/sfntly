// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

public abstract class RecordsTable<R extends Record> extends HeaderTable {
  public final RecordList<R> recordList;

  // ///////////////
  // constructors

  public RecordsTable(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    recordList = createRecordList(data.slice(base + headerSize()));
  }

  public RecordsTable(ReadableFontData data, boolean dataIsCanonical) {
    this(data, 0, dataIsCanonical);
  }

  // ////////////////////////////////////
  // implementations pushed to subclasses

  abstract protected RecordList<R> createRecordList(ReadableFontData data);

  // ////////////////////////////////////
  // builder

  public abstract static class Builder<T extends HeaderTable, R extends Record>
      extends HeaderTable.Builder<T> {

    protected RecordList<R> records;
    protected int serializedLength;
    protected final int base;

    // ///////////////
    // constructors

    public Builder() {
      super();
      base = 0;
    }

    public Builder(RecordsTable<R> table) {
      this(table.readFontData(), table.base, table.dataIsCanonical);
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      this(data, 0, dataIsCanonical);
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data);
      this.base = base;
      if (!dataIsCanonical) {
        prepareToEdit();
      }
    }

    public Builder(RecordsTable.Builder<T, R> other) {
      super();
      base = other.base;
      records = other.records;
    }

    // ////////////////
    // public methods

    public RecordList<R> records() {
      return records;
    }

    public int add(R record) {
      prepareToEdit();
      records.add(record);
      return records.count();
    }

    public boolean contains(R record) {
      initFromData(internalReadData(), base);
      return records.contains(record);
    }

    public int count() {
      initFromData(internalReadData(), base);
      return records.count();
    }

    // ////////////////////////////////////
    // overriden methods

    @Override
    public int subDataSizeToSerialize() {
      if (records != null) {
        serializedLength = records.limit();
      } else {
        computeSizeFromData(internalReadData().slice(base + headerSize()));
      }
      return serializedLength;
    }

    @Override
    public int subSerialize(WritableFontData newData) {
      if (serializedLength == 0) {
        return 0;
      }

      if (records == null) {
        return serializeFromData(newData);
      }

      return records.writeTo(newData);
    }

    @Override
    public T subBuildTable(ReadableFontData data) {
      return readTable(data, 0, true);
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    public void subDataSet() {
      records = null;
    }

    // ////////////////////////////////////
    // implementations pushed to subclasses

    protected abstract T readTable(ReadableFontData data, int base, boolean dataIsCanonical);

    protected abstract RecordList<R> readRecordList(ReadableFontData data, int base);

    // ////////////////////////////////////
    // private methods

    private void prepareToEdit() {
      initFromData(internalReadData(), base + headerSize());
      setModelChanged();
    }

    private void initFromData(ReadableFontData data, int base) {
      if (records == null) {
        records = readRecordList(data, base);
      }
    }

    private void computeSizeFromData(ReadableFontData data) {
      // This assumes canonical data.
      int len = 0;
      if (data != null) {
        len = data.length();
      }
      serializedLength = len;
    }

    private int serializeFromData(WritableFontData newData) {
      // The source data must be canonical.
      ReadableFontData data = internalReadData().slice(base + headerSize());
      data.copyTo(newData);
      return data.length();
    }
  }
}

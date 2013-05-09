// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class OffsetRecordTable<S extends SubTable> extends HeaderTable
    implements Iterable<S> {
  public final NumRecordList recordList;

  // ///////////////
  // constructors

  public OffsetRecordTable(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    recordList = new NumRecordList(data.slice(base + headerSize()));
  }

  public OffsetRecordTable(ReadableFontData data, boolean dataIsCanonical) {
    this(data, 0, dataIsCanonical);
  }

  // ////////////////
  // public methods

  public S subTableAt(int index) {
    NumRecord record = recordList.get(index);
    return subTableForRecord(record);
  }

  @Override
  public Iterator<S> iterator() {
    return new Iterator<S>() {
      Iterator<NumRecord> recordIterator = recordList.iterator();

      @Override
      public boolean hasNext() {
        return recordIterator.hasNext();
      }

      @Override
      public S next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        NumRecord record = recordIterator.next();
        return subTableForRecord(record);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  // ////////////////////////////////////
  // implementations pushed to subclasses

  abstract protected S readSubTable(ReadableFontData data, boolean dataIsCanonical);

  // ////////////////////////////////////
  // private methods

  private S subTableForRecord(NumRecord record) {
    if (record.value == 0) {
      // No reference to itself is allowed.
      return null;
    }
    ReadableFontData newBase = data.slice(record.value);
    return readSubTable(newBase, dataIsCanonical);
  }

  public abstract static class Builder<
      T extends OffsetRecordTable<? extends SubTable>, S extends SubTable>
      extends HeaderTable.Builder<T> {

    public List<VisibleBuilder<S>> builders;
    protected boolean dataIsCanonical;
    protected int serializedLength;
    private int serializedCount;
    private final int base;

    // ///////////////
    // constructors

    public Builder() {
      super();
      base = 0;
    }

    public Builder(T table) {
      this(table.readFontData(), table.base, table.dataIsCanonical);
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      this(data, 0, dataIsCanonical);
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data);
      this.base = base;
      this.dataIsCanonical = dataIsCanonical;
      if (!dataIsCanonical) {
        prepareToEdit();
      }
    }

    // ////////////////
    // public methods

    public int subTableCount() {
      if (builders == null) {
        return new NumRecordList(internalReadData().slice(base + headerSize())).count();
      }
      return builders.size();
    }

    public SubTable.Builder<? extends SubTable> builderForTag(int tag) {
      prepareToEdit();
      return builders.get(tag);
    }

    public VisibleBuilder<S> addBuilder() {
      prepareToEdit();
      VisibleBuilder<S> builder = createSubTableBuilder();
      builders.add(builder);
      return builder;
    }

    public VisibleBuilder<S> addBuilder(S subTable) {
      prepareToEdit();
      VisibleBuilder<S> builder = createSubTableBuilder(subTable);
      builders.add(builder);
      return builder;
    }

    public void removeBuilderForTag(int tag) {
      prepareToEdit();
      builders.remove(tag);
    }

    // ////////////////////////////////////
    // overriden methods

    @Override
    public int subDataSizeToSerialize() {
      if (builders != null) {
        computeSizeFromBuilders();
      } else {
        computeSizeFromData(internalReadData().slice(base + headerSize()));
      }
      return serializedLength;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    public int subSerialize(WritableFontData newData) {
      if (serializedLength == 0) {
        return 0;
      }

      if (builders != null) {
        return serializeFromBuilders(newData);
      }
      return serializeFromData(newData);
    }

    @Override
    public void subDataSet() {
      builders = null;
    }

    @Override
    public T subBuildTable(ReadableFontData data) {
      return readTable(data, 0, true);
    }

    // ////////////////////////////////////
    // implementations pushed to subclasses

    protected abstract T readTable(ReadableFontData data, int base, boolean dataIsCanonical);

    protected abstract VisibleBuilder<S> createSubTableBuilder();

    protected abstract VisibleBuilder<S> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical);

    protected abstract VisibleBuilder<S> createSubTableBuilder(S subTable);

    // ////////////////////////////////////
    // private methods

    private void prepareToEdit() {
      if (builders == null) {
        initFromData(internalReadData(), base);
        setModelChanged();
      }
    }

    private void initFromData(ReadableFontData data, int base) {
      builders = new ArrayList<VisibleBuilder<S>>();
      if (data == null) {
        return;
      }

      data = data.slice(base + headerSize());
      // Start of the first subtable in the data, if we're canonical.
      NumRecordList recordList = new NumRecordList(data);
      if (recordList.count() == 0) {
        return;
      }

      int subTableLimit = recordList.limit();
      Iterator<NumRecord> recordIterator = recordList.iterator();
      do {
        NumRecord record = recordIterator.next();
        int offset = record.value;
        VisibleBuilder<S> builder = createSubTableBuilder(data, offset);
        builders.add(builder);
      } while (recordIterator.hasNext());
    }

    private void computeSizeFromBuilders() {
      // This does not merge LangSysTables that reference the same
      // features.

      // If there is no data in the default LangSysTable or any
      // of the other LangSysTables, the size is zero, and this table
      // will not be written.

      int len = 0;
      int count = 0;
      for (VisibleBuilder<S> builder : builders) {
        int sublen = builder.subDataSizeToSerialize();
        if (sublen > 0) {
          ++count;
          len += sublen;
        }
      }
      if (len > 0) {
        len += NumRecordList.sizeOfListOfCount(count);
      }
      serializedLength = len;
      serializedCount = count;
    }

    private void computeSizeFromData(ReadableFontData data) {
      // This assumes canonical data.
      int len = 0;
      int count = 0;
      if (data != null) {
        len = data.length();
        count = new NumRecordList(data).count();
      }
      serializedLength = len;
      serializedCount = count;
    }

    private int serializeFromBuilders(WritableFontData newData) {
      // The canonical form of the data consists of the header,
      // the index, then the
      // scriptTables from the index in index order. All
      // scriptTables are distinct; there's no sharing of tables.

      // Find size for table
      int tableSize = NumRecordList.sizeOfListOfCount(serializedCount);

      // Fill header in table and serialize its builder.
      int subTableFillPos = tableSize;

      NumRecordList recordList = new NumRecordList(newData);
      for (VisibleBuilder<S> builder : builders) {
        if (builder.serializedLength > 0) {
          NumRecord record = new NumRecord(subTableFillPos);
          recordList.add(record);
          subTableFillPos += builder.subSerialize(newData.slice(subTableFillPos));
        }
      }
      recordList.writeTo(newData);
      return subTableFillPos;
    }

    private int serializeFromData(WritableFontData newData) {
      // The source data must be canonical.
      ReadableFontData data = internalReadData().slice(base);
      data.copyTo(newData);
      return data.length();
    }

    private VisibleBuilder<S> createSubTableBuilder(ReadableFontData data, int offset) {
      ReadableFontData newData = data.slice(offset);
      return createSubTableBuilder(newData, dataIsCanonical);
    }
  }
}

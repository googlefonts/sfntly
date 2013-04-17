// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.TagOffsetRecord;
import com.google.typography.font.sfntly.table.opentype.component.TagOffsetRecordList;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public abstract class RecordsTable<S extends SubTable> extends SubTable implements Iterable<S> {
  final boolean dataIsCanonical;
  public final TagOffsetRecordList recordList;
  public int tag;

  public RecordsTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data);
    this.dataIsCanonical = dataIsCanonical;
    recordList = new TagOffsetRecordList(data);
  }

  public S subTableAt(int index) {
    TagOffsetRecord record = recordList.get(index);
    return subTableForRecord(record);
  }

//  public S scriptTableForTag(ScriptTag scriptTag) {
//    return scriptTableForTag(scriptTag.tag());
//  }
//
  public S subTableForTag(int tag) {
    TagOffsetRecord record = recordList.getRecordForTag(tag);
    if (record == null) {
      return null;
    }
    return subTableForRecord(record);
  }
  
  @Override
  public Iterator<S> iterator() {
    return new Iterator<S>() {
      Iterator<TagOffsetRecord> recordIterator = recordList.iterator();

      @Override
      public boolean hasNext() {
        return recordIterator.hasNext();
      }

      @Override
      public S next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        TagOffsetRecord record = recordIterator.next();
        return subTableForRecord(record);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  abstract protected S createSubTable(ReadableFontData data, int tag,
      boolean dataIsCanonical);

  private S subTableForRecord(TagOffsetRecord record) {
    int tag = record.tag;
    int offset = record.offset;
    ReadableFontData newBase = data.slice(offset);
    return createSubTable(newBase, tag, dataIsCanonical);
  }
  
  public abstract static 
  class VisibleBuilder<T extends SubTable> extends SubTable.Builder<T> {

    protected int serializedLength;
    
    protected VisibleBuilder(ReadableFontData data) {
      super(data);
    }

    @Override
    protected abstract int subSerialize(WritableFontData newData);

    @Override
    protected abstract int subDataSizeToSerialize();
  }
  
  public abstract static 
  class Builder<T extends SubTable, S extends SubTable>
       extends SubTable.Builder<T> {
    private TreeMap<Integer, VisibleBuilder<S>> builders;
    private boolean dataIsCanonical;
    private int serializedCount;
    protected int serializedLength;

    public Builder() {
      super(null);
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
      if (!dataIsCanonical) {
        prepareToEdit();
      }
    }

    public Builder(RecordsTable<T> table) {
      this(table.readFontData(), table.dataIsCanonical);
    }

    protected abstract
    VisibleBuilder<S> createSubTableBuilder(
        ReadableFontData data, int tag, boolean dataIsCanonical);
    
    private VisibleBuilder<S> createSubTableBuilder(
        ReadableFontData data, int offset, int length, int tag) {
      boolean dataIsCanonical = (length >= 0);
      ReadableFontData newData = dataIsCanonical ?
          data.slice(offset, length) : data.slice(offset);
      return createSubTableBuilder(newData, tag, dataIsCanonical);
    }

    private void initFromData(ReadableFontData data) {
      builders = new TreeMap<Integer, VisibleBuilder<S>>();
      if (data == null) {
        return;
      }

      // Start of the first subtable in the data, if we're canonical.
      TagOffsetRecordList recordList = new TagOffsetRecordList(data);
      if (recordList.count() == 0) {
        return;
      }

      int subTableLimit = recordList.limit();
      Iterator<TagOffsetRecord> recordIterator = recordList.iterator();
      if (dataIsCanonical) {
        do {
          // Each table starts where the previous one ended.
          int offset = subTableLimit;
          TagOffsetRecord record = recordIterator.next();
          int tag = record.tag;
          // Each table ends at the next start, or at the end of the data.
          subTableLimit = record.offset;
          // TODO(cibu): length computation does not seems to be correct.
          int length = subTableLimit - offset;
          VisibleBuilder<S> builder =
              createSubTableBuilder(data, offset, length, tag);
          builders.put(tag, builder);
        } while (recordIterator.hasNext());
      } else {
        do {
          TagOffsetRecord record = recordIterator.next();
          int offset = record.offset;
          int tag = record.tag;
          VisibleBuilder<S> builder =
              createSubTableBuilder(data, offset, -1, tag);
          builders.put(tag, builder);
        } while (recordIterator.hasNext());
      }
    }

    private void prepareToEdit() {
      if (builders == null) {
        initFromData(internalReadData());
        setModelChanged();
      }
    }

    public int subTableCount() {
      if (builders == null) {
        return new TagOffsetRecordList(internalReadData()).count();
      }
      return builders.size();
    }

    protected abstract
    VisibleBuilder<S> createSubTableBuilder(int tag);    
    
    /**
     * Returns the builder for the language system specified by the tag, adding a
     * new builder if necessary.
     */
    public 
    SubTable.Builder<S> addBuiderForTag(int tag) {
      prepareToEdit();
      VisibleBuilder<S> builder = builders.get(tag);
      if (builder == null) {
        builder = createSubTableBuilder(tag);
        builders.put(tag, builder);
      }
      return builder;
    }

    /**
     * Return the builder for the script specified by the tag, or null if
     * the tag is not currently listed.
     */
    public
    SubTable.Builder<? extends SubTable> builderForTag(int tag) {
      prepareToEdit();
      return builders.get(tag);
    }

    /**
     * Remove the sub table specified by the tag.
     */
    public void removeBuilderForTag(int tag) {
      prepareToEdit();
      builders.remove(tag);
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    private void computeSizeFromBuilders() {
      // This does not merge LangSysTables that reference the same
      // features.

      // If there is no data in the default LangSysTable or any
      // of the other LangSysTables, the size is zero, and this table
      // will not be written.

      int len = 0;
      int count = 0;
      for (VisibleBuilder<? extends SubTable> builder : builders.values()) {
        int sublen = builder.subDataSizeToSerialize();
        if (sublen > 0) {
          ++count;
          len += sublen;
        }
      }
      if (len > 0) {
        len += TagOffsetRecordList.sizeOfListOfCount(count);
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
        count = new TagOffsetRecordList(data).count();
      }
      serializedLength = len;
      serializedCount = count;
    }

    @Override
    public int subDataSizeToSerialize() {
      if (builders != null) {
        computeSizeFromBuilders();
      } else {
        computeSizeFromData(internalReadData());
      }
      return serializedLength;
    }

    private int serializeFromBuilders(WritableFontData newData) {
      // The canonical form of the data consists of the header,
      // the index, then the
      // scriptTables from the index in index order.  All
      // scriptTables are distinct; there's no sharing of tables.
      TagOffsetRecordList recordList = new TagOffsetRecordList(newData);
      int subTablePos = TagOffsetRecordList.sizeOfListOfCount(serializedCount);
      for (Entry<Integer, VisibleBuilder<S>> entry  : builders.entrySet()) {
        int tag = entry.getKey();
        VisibleBuilder<? extends SubTable> builder = entry.getValue();
        if (builder.serializedLength > 0) {
          TagOffsetRecord record = new TagOffsetRecord(tag, subTablePos);
          recordList.append(record);
          subTablePos += builder.subSerialize(newData.slice(subTablePos));
        }
      }
      return subTablePos;
    }

    private int serializeFromData(WritableFontData newData) {
      // The source data must be canonical.
      ReadableFontData data = internalReadData();
      data.copyTo(newData);
      return data.length();
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
    protected void subDataSet() {
      builders = null;
    }

    abstract protected T createTable(ReadableFontData data,
        boolean dataIsCanonical);

    @Override
    protected T subBuildTable(ReadableFontData data) {
      return createTable(data, true);
    }
  }
}

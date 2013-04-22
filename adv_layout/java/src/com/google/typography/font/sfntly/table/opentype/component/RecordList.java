package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class RecordList<T extends Record> implements Iterable<T> {
  private static final int COUNT_OFFSET = 0;
  public static final int RECORD_BASE = 2;

  private final ReadableFontData readData;
  private final WritableFontData writeData;
  private int count;
  
  public RecordList(WritableFontData data) {
    this.readData = null;
    this.writeData = data;
    this.count = 0;
    writeData.writeUShort(COUNT_OFFSET, 0);
  }

  public RecordList(ReadableFontData data) {
    this.readData = data;
    this.writeData = null;
    this.count = data.readUShort(COUNT_OFFSET);
  }

  public int count() {
    return count;
  }

  public int limit() {
    return sizeOfList(count);
  }

  private int sizeOfList(int count) {
    return baseAt(RECORD_BASE, count);
  }

  private int baseAt(int base, int index) {
    return base + index * recordSize();
  }

  public T get(int index) {
    return getRecordAt(readData, sizeOfList(index));
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      private int current = 0;

      @Override
      public boolean hasNext() {
        return current < count;
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return getRecordAt(readData, sizeOfList(current++));
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public void append(T record) {
    if (writeData == null) {
      throw new UnsupportedOperationException();
    }
    record.writeTo(writeData, limit());
    writeData.writeUShort(COUNT_OFFSET, ++count);
  }
  
  protected abstract T getRecordAt(ReadableFontData data, int pos);
  
  protected abstract int recordSize();
}

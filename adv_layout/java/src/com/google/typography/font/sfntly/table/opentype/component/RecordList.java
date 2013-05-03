package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.FontDataTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class RecordList<T extends Record> implements Iterable<T>, HtmlDump {
  private static final int COUNT_OFFSET = 0;
  public static final int RECORD_BASE = 2;

  private final ReadableFontData readData;
  private final WritableFontData writeData;
  private int count;
  private List<T> recordsToWrite;

  public RecordList(WritableFontData data) {
    this.readData = null;
    this.writeData = data;
    this.count = 0;
    if (writeData != null) {
      writeData.writeUShort(COUNT_OFFSET, 0);
    }
  }

  public RecordList(ReadableFontData data) {
    this.readData = data;
    this.writeData = null;
    if (readData != null) {
      this.count = data.readUShort(COUNT_OFFSET);
    }
  }

  public int count() {
    if (recordsToWrite != null) {
      return recordsToWrite.size();
    }
    return count;
  }

  public int limit() {
    return sizeOfList(count());
  }

  private int sizeOfList(int count) {
    return baseAt(RECORD_BASE, count);
  }

  private int baseAt(int base, int index) {
    return base + index * recordSize();
  }

  public T get(int index) {
    if (recordsToWrite != null) {
      return recordsToWrite.get(index);
    }
    return getRecordAt(readData, sizeOfList(index));
  }

  public boolean contains(T record) {
    if (recordsToWrite != null) {
      return recordsToWrite.contains(record);
    }

    Iterator<T> iterator = iterator();
    while(iterator.hasNext()) {
      if (record.equals(iterator.next())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Iterator<T> iterator() {
    if (recordsToWrite != null) {
      return recordsToWrite.iterator();
    }

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

  public RecordList<T> add(T record) {
    copyFromRead();
    recordsToWrite.add(record);
    return this;
  }

  public RecordList<T> addAll(Collection<T> recordsToWrite) {
    copyFromRead();
    this.recordsToWrite.addAll(recordsToWrite);
    return this;
  }

  public int write() {
    if (writeData == null) {
      throw new UnsupportedOperationException();
    }
    return writeTo(writeData);
  }

  public int writeTo(WritableFontData writeData) {
    copyFromRead();
    int bytesWrote = writeData.writeUShort(COUNT_OFFSET, count);
    for(T record : recordsToWrite) {
      bytesWrote += record.writeTo(writeData, bytesWrote);
    }
    return bytesWrote;
  }

  private void copyFromRead() {
    if (recordsToWrite == null) {
      recordsToWrite = new ArrayList<T>(count);
      Iterator<T> iterator = iterator();
      while(iterator.hasNext()) {
        recordsToWrite.add(iterator.next());
      }
    }
  }

  @Override
  public String toHtml() {
    Class<? extends RecordList> clzz = this.getClass();
    StringBuilder sb = new StringBuilder(clzz.getSimpleName());
    sb.append("\n");
    Iterator<T> iterator = iterator();
    while(iterator.hasNext()) {
      sb.append("<div>\n");
      sb.append(iterator.next().toHtml());
      sb.append("</div>\n");
    }

    return sb.toString();
  }

  protected abstract T getRecordAt(ReadableFontData data, int pos);

  protected abstract int recordSize();
}

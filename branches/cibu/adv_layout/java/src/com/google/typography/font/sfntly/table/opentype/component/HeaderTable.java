package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class HeaderTable extends SubTable {
  public static final int FIELD_SIZE = 2;
  protected boolean dataIsCanonical = false;
  protected int base = 0;

  public HeaderTable(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data);
    this.base = base;
    this.dataIsCanonical = dataIsCanonical;
  }

  public int writeFieldAt(WritableFontData newData, int index, int value) {
    return newData.writeUShort(index * FIELD_SIZE, value);
  }

  public int getField(int index) {
    return data.readUShort(base + index * FIELD_SIZE);
  }

  public int headerSize() {
    return FIELD_SIZE * fieldCount();
  }

  public abstract int fieldCount();

  public abstract static class Builder<T extends HeaderTable> extends VisibleBuilder<T> {
    private Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    protected boolean dataIsCanonical = false;

    public Builder() {
      super();
      initFields();
    }

    public Builder(ReadableFontData data) {
      super(data);
      initFields();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
      initFields();
    }

    public Builder(T table) {
      super();
      initFields();
      for (int i = 0; i < table.fieldCount(); i++) {
        map.put(i, table.getField(i));
      }
    }

    public int setField(int index, int value) {
      return map.put(index, value);
    }

    public int getField(int index) {
      return map.get(index);
    }

    protected abstract void initFields();

    public abstract int fieldCount();

    public int headerSize() {
      return FIELD_SIZE * fieldCount();
    }

    @Override
    public int subDataSizeToSerialize() {
      return headerSize();
    }

    @Override
    public int subSerialize(WritableFontData newData) {
      for (Entry<Integer, Integer> entry : map.entrySet()) {
        newData.writeUShort(entry.getKey() * FIELD_SIZE, entry.getValue());
      }
      return headerSize();
    }

    @Override
    public void subDataSet() {
      map = new HashMap<Integer, Integer>();
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }
  }
}

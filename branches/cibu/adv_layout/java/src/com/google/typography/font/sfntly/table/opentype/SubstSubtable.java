package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.HeaderTable;

public abstract class SubstSubtable extends HeaderTable {
  public static final int FIELD_COUNT = 1;
  public static final int FORMAT_INDEX = 0;
  public static final int FORMAT_DEFAULT = 0;
  public final int format;

  protected SubstSubtable(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    format = getField(FORMAT_INDEX);
  }

  @Override
  public int fieldCount() {
    return FIELD_COUNT;
  }

  public abstract static class Builder<T extends SubstSubtable, S extends SubTable>
      extends HeaderTable.Builder<T> {
    protected boolean dataIsCanonical;

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
    }

    public Builder() {
      super();
    }

    @Override
    protected void initFields() {
      setField(FORMAT_INDEX, FORMAT_DEFAULT);
    }

    @Override
    public int fieldCount() {
      return FIELD_COUNT;
    }
  }
}

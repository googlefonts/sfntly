// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.GsubLookupType;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public abstract class GsubLookupTable extends LookupTable {

  protected GsubLookupTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  @Override
  public abstract Builder<? extends GsubLookupTable> builder();

  @Override
  public abstract GsubLookupType lookupType();

  @Override
  protected abstract GsubLookupSubTable createSubTable(ReadableFontData data);

  public static abstract class Builder<T extends GsubLookupTable> extends LookupTable.Builder<T> {

    protected Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    protected Builder() {
    }

    protected Builder(T table) {
      super(table);
    }

    @Override
    abstract GsubLookupType lookupType();

    @Override
    protected abstract GsubLookupSubTable.Builder<?> createSubTableBuilder(ReadableFontData data);
  }
}

// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.GsubLookupType;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
abstract class GsubLookupSubTable extends LookupSubTable {

  private GsubLookupSubTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  @Override
  public abstract Builder<? extends GsubLookupSubTable> builder();

  @Override
  public abstract GsubLookupType lookupType();

  private static abstract class Builder<T extends GsubLookupSubTable>
      extends LookupSubTable.Builder<T> {

    private Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    private Builder(T table) {
      super(table);
    }

    @Override
    public abstract GsubLookupType lookupType();
  }
}

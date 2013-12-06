// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
abstract class GsubLookupTable extends LookupTable {

  private GsubLookupTable(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  static abstract class Builder<T extends GsubLookupTable> extends LookupTable.Builder {

    private Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    private Builder() {
    }

    private Builder(T table) {
      super(table);
    }
  }
}

// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.GsubLookupTable;
import com.google.typography.font.sfntly.table.opentype.GsubLookupList.GsubLookupType;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public class GsubLookupChainingContextual extends GsubLookupTable {
  static final int FORMAT_1 = 1;

  protected GsubLookupChainingContextual(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  public static GsubLookupChainingContextual create(ReadableFontData data) {
    return new GsubLookupChainingContextual(data, false);
  }

  @Override
  public GsubLookupType lookupType() {
    return GsubLookupType.GSUB_CHAINING_CONTEXTUAL;
  }

  @Override
  protected GsubLookupSubTable createSubTable(ReadableFontData data) {
    // TODO(dougfelt): Auto-generated method stub
    return null;
  }

  @Override
  public Builder builder() {
    return new Builder(data, dataIsCanonical);
  }

  public static class Builder extends GsubLookupTable.Builder {

    public Builder() {
      super(null, true);
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    @Override
    public GsubLookupType lookupType() {
      return GsubLookupType.GSUB_CHAINING_CONTEXTUAL;
    }

    @Override
    protected com.google.typography.font.sfntly.table.opentype.GsubLookupSubTable.Builder createSubTableBuilder(
        ReadableFontData data) {
      // TODO(dougfelt): Auto-generated method stub
      return null;
    }

    @Override
    protected GsubLookupTable subBuildTable(ReadableFontData data) {
      // TODO(dougfelt): Auto-generated method stub
      return null;
    }
  }
}

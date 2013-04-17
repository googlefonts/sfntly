// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.GsubLookupTable;
import com.google.typography.font.sfntly.table.opentype.GsubLookupContextual.Fmt1;
import com.google.typography.font.sfntly.table.opentype.GsubLookupContextual.Fmt1Builder;
import com.google.typography.font.sfntly.table.opentype.GsubLookupContextual.Fmt2;
import com.google.typography.font.sfntly.table.opentype.GsubLookupContextual.Fmt2Builder;
import com.google.typography.font.sfntly.table.opentype.GsubLookupContextual.Fmt3;
import com.google.typography.font.sfntly.table.opentype.GsubLookupContextual.Fmt3Builder;
import com.google.typography.font.sfntly.table.opentype.GsubLookupList.GsubLookupType;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public class GsubLookupChainingContextual extends GsubLookupTable {
  static final int FORMAT_OFFSET = 0;
  static final int FORMAT_1 = 1;
  static final int FORMAT_2 = 2;
  static final int FORMAT_3 = 3;

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
    int formatType = data.readUShort(FORMAT_OFFSET);
    switch (formatType) {
      case FORMAT_1: return new Fmt1(data, dataIsCanonical);
      case FORMAT_2: return new Fmt2(data, dataIsCanonical);
      case FORMAT_3: return new Fmt3(data, dataIsCanonical);
      default: throw new IllegalStateException(
          "unrecognized format type: " + formatType);
    }
  }

  @Override
  public Builder builder() {
    return new Builder(data, dataIsCanonical);
  }

  public static class Builder extends GsubLookupTable.Builder<GsubLookupChainingContextual> {

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
    protected ContextualChainingSubTable.Builder<?> createSubTableBuilder(
        ReadableFontData data) {
      // TODO(dougfelt): Auto-generated method stub
      return null;
    }

    @Override
    protected GsubLookupChainingContextual subBuildTable(ReadableFontData data) {
      // TODO(dougfelt): Auto-generated method stub
      return null;
    }
  }
  
  
  public static abstract class ContextualChainingSubTable extends GsubLookupSubTable {
    protected ContextualChainingSubTable(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    @Override
    public GsubLookupType lookupType() {
      return GsubLookupType.GSUB_CHAINING_CONTEXTUAL;
    }

    @Override
    public abstract ContextualChainingSubTable.Builder<?> builder();

    public abstract int format();

    public Fmt1 asFmt1() {
      return null;
    }

    public Fmt2 asFmt2() {
      return null;
    }

    public Fmt3 asFmt3() {
      return null;
    }

    static abstract class Builder<T extends ContextualChainingSubTable>
        extends GsubLookupSubTable.Builder<T> {

      protected Builder(ReadableFontData data, boolean dataIsCanonical) {
        super(data, dataIsCanonical);
      }

      public Builder() {
        super();
      }

      public Builder(T table) {
        super(table);
      }

      public Fmt1Builder asFmt1Builder() {
        return null;
      }

      public Fmt2Builder asFmt2Builder() {
        return null;
      }

      public Fmt3Builder asFmt3Builder() {
        return null;
      }

      @Override
      public GsubLookupType lookupType() {
        return GsubLookupType.GSUB_CHAINING_CONTEXTUAL;
      }
    }
  }


}

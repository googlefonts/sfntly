// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.LookupTable.LookupType;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public class GsubLookupList extends LookupList {

  public static enum GsubLookupType implements LookupType {
    GSUB_SINGLE,
    GSUB_MULTIPLE,
    GSUB_ALTERNATE,
    GSUB_LIGATURE,
    GSUB_CONTEXTUAL,
    GSUB_CHAINING_CONTEXTUAL,
    GSUB_EXTENSION,
    GSUB_REVERSE_CHAINING_CONTEXTUAL_SINGLE;

    @Override
    public int typeNum() {
      return ordinal() + 1;
    }

    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }

    static GsubLookupType forTypeNum(int typeNum) {
      if (typeNum <= 0 || typeNum > values.length) {
        System.err.format("unknown gsub lookup typeNum: %d\n", typeNum);
        return null;
      }
      return values[typeNum - 1];
    }

    private static final GsubLookupType[] values = values();
  }

  protected GsubLookupList(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  @Override
  public GsubLookupType lookupTypeAt(int index) {
    int offset = lookupOffsetAt(index);
    int typeNum = data.readUShort(offset + LookupTable.LOOKUP_TYPE_OFFSET);
    return GsubLookupType.forTypeNum(typeNum);
  }

  @Override
  public GsubLookupTable lookupAt(int index) {
    return (GsubLookupTable) super.lookupAt(index);
  }

  static GsubLookupType readLookupType(ReadableFontData lookupData) {
    if (lookupData == null) {
      return null;
    }
    int typeNum = lookupData.readUShort(LookupTable.LOOKUP_TYPE_OFFSET);
    return GsubLookupType.forTypeNum(typeNum);
  }

  @Override
  protected GsubLookupTable createLookup(ReadableFontData lookupData) {
    if (lookupData == null) {
      return null;
    }
    GsubLookupType lookupType = readLookupType(lookupData);
    switch (lookupType) {
      case GSUB_SINGLE: return new GsubLookupSingle(lookupData, dataIsCanonical);
      case GSUB_LIGATURE: return new GsubLookupLigature(lookupData, dataIsCanonical);
      case GSUB_CHAINING_CONTEXTUAL: return new GsubLookupChainingContextual(
          lookupData, dataIsCanonical);
      case GSUB_CONTEXTUAL: return new GsubLookupContextual(
          lookupData, dataIsCanonical);
      default: return null;
    }
  }

  public static class Builder extends LookupList.Builder {
    protected Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder() {
      super();
    }

    @Override
    protected GsubLookupList subBuildTable(ReadableFontData data) {
      return new GsubLookupList(data, true);
    }

    @Override
    protected LookupTable.Builder createLookupBuilder(ReadableFontData lookupData) {
      if (lookupData == null) {
        return null;
      }
      GsubLookupType lookupType = readLookupType(lookupData);
      switch (lookupType) {
        case GSUB_SINGLE: return new GsubLookupSingle.Builder(lookupData, dataIsCanonical);
        case GSUB_LIGATURE : return new GsubLookupLigature.Builder(lookupData, dataIsCanonical);
        default: return null;
      }
    }
  }
}

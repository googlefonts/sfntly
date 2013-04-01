// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.GsubLookupList.GsubLookupType;
import com.google.typography.font.sfntly.table.opentype.IntSet.IntIterator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public class GsubLookupSingle extends GsubLookupTable {
  static final int FORMAT_1 = 1;
  static final int FORMAT_2 = 2;

  protected GsubLookupSingle(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  @Override
  public GsubLookupType lookupType() {
    return GsubLookupType.GSUB_SINGLE;
  }

  public int subTableFormatAt(int index) {
    int offset = offsetForIndex(index);
    return data.readUShort(offset + SingleSubTable.FORMAT_OFFSET);
  }

  public int mapGlyphId(int glyphId) {
    for (int i = 0; i < subTableCount(); ++i) {
      SingleSubTable subTable = subTableAt(i);
      if (subTable.covers(glyphId)) {
        return subTable.mapGlyphId(glyphId);
      }
    }
    return glyphId;
  }

  @Override
  public SingleSubTable subTableAt(int index) {
    return (SingleSubTable) super.subTableAt(index);
  }

  @Override
  protected SingleSubTable createSubTable(ReadableFontData data) {
    int formatType = data.readUShort(SingleSubTable.FORMAT_OFFSET);
    switch (formatType) {
      case FORMAT_1: return new Fmt1(data, dataIsCanonical);
      case FORMAT_2: return new Fmt2(data, dataIsCanonical);
      default: throw new IllegalStateException(
          "unrecognized format type: " + formatType);
    }
  }

  @Override
  public Builder builder() {
    return new Builder(data, dataIsCanonical);
  }

  public static class Builder extends GsubLookupTable.Builder<GsubLookupSingle> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder(GsubLookupSingle table) {
      super(table);
    }

    @Override
    public GsubLookupType lookupType() {
      return GsubLookupType.GSUB_SINGLE;
    }

    public Fmt1Builder addFmt1Builder() {
      Fmt1Builder nb = new Fmt1Builder();
      addSubTableBuilder(nb);
      return nb;
    }

    public Fmt2Builder addFmt2Builder() {
      Fmt2Builder nb = new Fmt2Builder();
      addSubTableBuilder(nb);
      return nb;
    }

    @Override
    protected SingleSubTable.Builder createSubTableBuilder(ReadableFontData data) {
      int subFormat = data.readUShort(SingleSubTable.FORMAT_OFFSET);
      switch (subFormat) {
        case FORMAT_1: return new Fmt1Builder(data, dataIsCanonical);
        case FORMAT_2: return new Fmt2Builder(data, dataIsCanonical);
        default: throw new IllegalStateException(
            "unrecognized GSubLookup1 format: " + subFormat);
      }
    }

    @Override
    protected GsubLookupSingle subBuildTable(ReadableFontData data) {
      return new GsubLookupSingle(data, true);
    }

    @Override
    public GsubLookupSingle build() {
      return (GsubLookupSingle) super.build();
    }
  }

  /**
   * Base class for Fmt1 and Fmt2 subtables.
   */
  public abstract static class SingleSubTable extends GsubLookupSubTable {
    private static final int FORMAT_OFFSET = 0;
    private static final int COVERAGE_OFFSET = 2;

    protected final CoverageTable coverage;

    protected SingleSubTable(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
      int offset = data.readUShort(COVERAGE_OFFSET);
      coverage = CoverageTable.forData(data.slice(offset));
    }

    public abstract int mapGlyphId(int glyphId);

    public boolean covers(int glyphId) {
      return coverage.covers(glyphId);
    }

    public CoverageTable coverage() {
      return coverage;
    }

    @Override
    public GsubLookupType lookupType() {
      return GsubLookupType.GSUB_SINGLE;
    }

    /**
     * Base class for builders of Fmt1 and Fmt2 subtables.
     */
    static abstract class Builder<T extends SingleSubTable> extends GsubLookupSubTable.Builder<T> {
      protected CoverageTable.Builder coverageBuilder;
      protected int serializedCoverageOffset;

      protected Builder(ReadableFontData data, boolean dataIsCanonical) {
        super(data, dataIsCanonical);
      }

      protected Builder() {
      }

      protected Builder(T table) {
        super(table);
      }

      public Fmt1Builder asFmt1Builder() {
        return null;
      }

      public Fmt2Builder asFmt2Builder() {
        return null;
      }

      @Override
      public GsubLookupType lookupType() {
        return GsubLookupType.GSUB_SINGLE;
      }

      /**
       * Called by subclasses in subDataSizeToSerialize. The coverageTable
       * will be written at this location.
       */
      protected void setSerializedCoverageOffset(int offset) {
        serializedCoverageOffset = offset;
      }

      /**
       * Subclasses compute their data size and call setSerializedCoverageOffset
       * with it first, then return with a call to this implementation.
       */
      @Override
      protected int computeSerializedLength() {
        return serializedCoverageOffset +
            coverageBuilder.subDataSizeToSerialize();
      }

      @Override
      protected void subDataSet() {
        coverageBuilder = null;
      }

      @Override
      boolean unedited() {
        return coverageBuilder == null;
      }

      /**
       * Subclasses call this first, then read their own data.
       */
      @Override
      void readModel(ReadableFontData data, boolean dataIsCanonical) {
        if (data == null) {
          coverageBuilder = new CoverageTable.Builder();
        } else {
          ReadableFontData subData;
          int start = data.readUShort(COVERAGE_OFFSET);
          if (dataIsCanonical) {
            int limit = data.length();
            subData = data.slice(start, limit);
          } else {
            subData = data.slice(start);
          }
          coverageBuilder = new CoverageTable.Builder(subData);
        }
      }

      /**
       * Subclasses write their own data first, then return with a call to
       * this implementation.
       */
      @Override
      void writeModel(WritableFontData newData) {
        newData.writeUShort(SingleSubTable.COVERAGE_OFFSET, serializedCoverageOffset);
        coverageBuilder.subSerialize(newData.slice(serializedCoverageOffset));
      }
    }
  }

  public static class Fmt1 extends SingleSubTable {
    static final int DELTA_GLYPH_ID_OFFSET = 4;
    static final int HEADER_SIZE = 6;

    private int delta;

    private Fmt1(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
      if (data != null) {
        delta = data.readUShort(DELTA_GLYPH_ID_OFFSET);
      }
    }

    @Override
    public Fmt1Builder builder() {
      return new Fmt1Builder(this);
    }

    @Override
    public int mapGlyphId(int glyphId) {
      if (covers(glyphId)) {
        return glyphId + delta;
      }
      return glyphId;
    }
  }

  public static class Fmt2 extends SingleSubTable {
    private static final int GLYPH_COUNT_OFFSET = 6;
    private static final int SUBSTITUTE_BASE = 8;
    private static final int SUBSTITUTE_SIZE = 2;

    private Fmt2(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    @Override
    public Fmt2Builder builder() {
      return new Fmt2Builder(this);
    }

    @Override
    public int mapGlyphId(int glyphId) {
      int index = coverage.coverageIndex(glyphId);
      if (index != -1) {
        return data.readUShort(SUBSTITUTE_BASE + index * SUBSTITUTE_SIZE);
      }
      return glyphId;
    }
  }

  public static class Fmt1Builder extends SingleSubTable.Builder<Fmt1> {
    private int deltaGlyphId;

    public Fmt1Builder() {
      super();
    }

    public Fmt1Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
      this.deltaGlyphId = data.readUShort(Fmt1.DELTA_GLYPH_ID_OFFSET);
    }

    public Fmt1Builder(Fmt1 fmt1) {
      super(fmt1);
    }

    @Override
    public Fmt1Builder asFmt1Builder() {
      return this;
    }

    public Fmt1Builder setDeltaGlyphId(int deltaGlyphId) {
      prepareToEdit();
      this.deltaGlyphId = deltaGlyphId;
      return this;
    }

    public int getDeltaGlyphId() {
      return deltaGlyphId;
    }

    public Fmt1Builder add(int glyphId) {
      prepareToEdit();
      coverageBuilder.add(glyphId);
      return this;
    }

    public Fmt1Builder remove(int glyphId) {
      prepareToEdit();
      coverageBuilder.remove(glyphId);
      return this;
    }

    public Fmt1Builder addRange(int fromGlyphId, int toGlyphId) {
      prepareToEdit();
      coverageBuilder.addRange(fromGlyphId, toGlyphId);
      return this;
    }

    public Fmt1Builder removeRange(int fromGlyphId, int toGlyphId) {
      prepareToEdit();
      coverageBuilder.removeRange(fromGlyphId, toGlyphId);
      return this;
    }

    public Fmt1Builder removeAll() {
      prepareToEdit();
      coverageBuilder.removeAll();
      return this;
    }

    @Override
    void readModel(ReadableFontData data, boolean dataIsCanonical) {
      // already read it
      super.readModel(data, dataIsCanonical);
    }

    @Override
    void writeModel(WritableFontData newData) {
      newData.writeUShort(SingleSubTable.FORMAT_OFFSET, FORMAT_1);
      newData.writeUShort(Fmt1.DELTA_GLYPH_ID_OFFSET, deltaGlyphId);
      super.writeModel(newData);
    }

    @Override
    protected int computeSerializedLength() {
      setSerializedCoverageOffset(Fmt1.HEADER_SIZE);
      return super.computeSerializedLength();
    }

    @Override
    protected Fmt1 subBuildTable(ReadableFontData data) {
      return new Fmt1(data, true);
    }
  }

  public static class Fmt2Builder extends SingleSubTable.Builder<Fmt2> {
    private Map<Integer, Integer> glyphMap;

    public Fmt2Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Fmt2Builder() {
    }

    public Fmt2Builder(Fmt2 fmt2) {
      super(fmt2);
    }

    @Override
    public Fmt2Builder asFmt2Builder() {
      return this;
    }

    public boolean hasGlyphMapping(int fromGlyphId) {
      prepareToEdit();
      return glyphMap.containsKey(fromGlyphId);
    }

    public int getGlyphMapping(int fromGlyphId) {
      prepareToEdit();
      Integer value = glyphMap.get(fromGlyphId);
      if (value == null) {
        return -1;
      }
      return value;
    }

    public Fmt2Builder map(int fromGlyphId, int toGlyphId) {
      prepareToEdit();
      glyphMap.put(fromGlyphId, toGlyphId);
      coverageBuilder.add(fromGlyphId);
      return this;
    }

    public Fmt2Builder unmap(int fromGlyphId) {
      prepareToEdit();
      glyphMap.remove(fromGlyphId);
      coverageBuilder.remove(fromGlyphId);
      return this;
    }

    public Fmt2Builder unmapAll() {
      prepareToEdit();
      glyphMap.clear();
      coverageBuilder.removeAll();
      return this;
    }

    public int numMappings() {
      prepareToEdit();
      return glyphMap.size();
    }

    @Override
    boolean unedited() {
      return glyphMap == null;
    }

    @Override
    void readModel(ReadableFontData data, boolean dataIsCanonical) {
      super.readModel(data, dataIsCanonical);
      glyphMap = new HashMap<Integer, Integer>();
      if (data != null) {
        int count = data.readUShort(Fmt2.GLYPH_COUNT_OFFSET);
        int index = 0;
        int pos = Fmt2.SUBSTITUTE_BASE;
        IntIterator glyphIds = coverageBuilder.glyphIds();
        while (glyphIds.hasNext()) {
          int fromGlyphId = glyphIds.next();
          int toGlyphId = data.readUShort(pos);
          glyphMap.put(fromGlyphId, toGlyphId);
          pos += Fmt2.SUBSTITUTE_SIZE;
          ++index;
        }
        if (index != count) {
          throw new IllegalStateException("covered glyphs: " + index +
              " but count: " + count);
        }
      }
    }

    @Override
    protected void writeModel(WritableFontData newData) {
      newData.writeUShort(SingleSubTable.FORMAT_OFFSET, FORMAT_2);
      newData.writeUShort(Fmt2.GLYPH_COUNT_OFFSET, numMappings());
      int pos = Fmt2.SUBSTITUTE_BASE;
      IntIterator glyphIds = coverageBuilder.glyphIds();
      while (glyphIds.hasNext()) {
        int fromGlyphId = glyphIds.next();
        int toGlyphId = glyphMap.get(fromGlyphId);
        newData.writeUShort(pos, toGlyphId);
        pos += Fmt2.SUBSTITUTE_SIZE;
      }
      super.writeModel(newData);
    }

    @Override
    protected int computeSerializedLength() {
      setSerializedCoverageOffset(Fmt2.SUBSTITUTE_BASE +
          numMappings() * Fmt2.SUBSTITUTE_SIZE);
      return super.computeSerializedLength();
    }

    @Override
    protected void subDataSet() {
      glyphMap = null;
      super.subDataSet();
    }

    @Override
    protected Fmt2 subBuildTable(ReadableFontData data) {
      return new Fmt2(data, true);
    }
  }
}

// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.IntSet.IntIterator;
import com.google.typography.font.sfntly.table.opentype.IntSet.IntRange;
import com.google.typography.font.sfntly.table.opentype.IntSet.IntRangeIterator;

import java.util.NoSuchElementException;

/**
 * Represents an Opentype coverage subtable used by GPOS, GSUB, and GDEF.
 */
public abstract class CoverageTable extends SubTable {
  private static final int FORMAT_OFFSET = 0;
  private static final int FORMAT_1 = 1;
  private static final int FORMAT_2 = 2;

  public abstract boolean covers(int glyphId);
  public abstract int coverageIndex(int glyphId);
  public abstract IntIterator coveredGlyphs();
  public abstract int coveredGlyphCount();

  protected CoverageTable(ReadableFontData data) {
    super(data);
  }

  static int getFormat(ReadableFontData data) {
    int fmt = data.readUShort(FORMAT_OFFSET);
    if (fmt != FORMAT_1 && fmt != FORMAT_2) {
      throw new IllegalArgumentException("unrecognized CoverageTable format: " + fmt);
    }
    return fmt;
  }

  static CoverageTable forData(ReadableFontData data) {
    if (getFormat(data) == FORMAT_1) {
      return new Fmt1(data);
    }
    return new Fmt2(data);
  }

  public static class Fmt1 extends CoverageTable {
    private static final int COUNT_OFFSET = 2;
    private static final int GLYPH_ARRAY_BASE = 4;
    private static final int GLYPH_ARRAY_ELEM_SIZE = 2;

    private int count;

    Fmt1(ReadableFontData data) {
      super(data);
      this.count = data.readUShort(COUNT_OFFSET);
    }

    @Override
    public boolean covers(int glyphId) {
      return search(glyphId) != -1;
    }

    @Override
    public int coverageIndex(int glyphId) {
      return search(glyphId);
    }

    @Override
    public IntIterator coveredGlyphs() {
      return new IntIterator() {
        int pos = GLYPH_ARRAY_BASE;
        int limit = pos + count * GLYPH_ARRAY_ELEM_SIZE;
        @Override
        public boolean hasNext() {
          return pos < limit;
        }

        @Override
        public int next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          int val = data.readUShort(pos);
          pos += GLYPH_ARRAY_ELEM_SIZE;
          return val;
        }
      };
    }

    @Override
    public int coveredGlyphCount() {
      return count;
    }

    private int search(int glyphId) {
      int index = data.searchUShort(GLYPH_ARRAY_BASE, GLYPH_ARRAY_ELEM_SIZE, count, glyphId);
      return index;
    }
  }

  public static class Fmt2 extends CoverageTable {
    private static final int RANGE_COUNT_OFFSET = 2;
    private static final int RANGE_RECORD_BASE = 4;
    private static final int RANGE_RECORD_START_OFFSET = 0;
    private static final int RANGE_RECORD_END_OFFSET = 2;
    private static final int RANGE_RECORD_INDEX_OFFSET = 4;
    private static final int RANGE_RECORD_SIZE = 6;

    private int count;

    Fmt2(ReadableFontData data) {
      super(data);
      this.count = data.readUShort(RANGE_COUNT_OFFSET);
    }

    @Override
    public boolean covers(int glyphId) {
      return search(glyphId) != -1;
    }

    @Override
    public int coverageIndex(int glyphId) {
      int index = search(glyphId);
      if (index < 0) {
        return -1;
      }
      int p = RANGE_RECORD_BASE + index * RANGE_RECORD_SIZE;
      int lo = data.readUShort(p + RANGE_RECORD_START_OFFSET);
      int baseIndex = data.readUShort(p + RANGE_RECORD_INDEX_OFFSET);
      return baseIndex + glyphId - lo;
    }

    @Override
    public IntIterator coveredGlyphs() {
      return new IntIterator() {
        int rpos = RANGE_RECORD_BASE + RANGE_RECORD_START_OFFSET;
        int rlim = rpos + count * RANGE_RECORD_SIZE;
        int svalue = rpos < rlim ?
            data.readUShort(rpos + RANGE_RECORD_START_OFFSET) : -1;
        int lvalue = rpos < rlim ?
            data.readUShort(rpos + RANGE_RECORD_END_OFFSET) : -1;

        @Override
        public boolean hasNext() {
          return rpos < rlim;
        }

        @Override
        public int next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          int val = svalue;
          if (svalue < lvalue) {
            ++svalue;
          } else {
            rpos += RANGE_RECORD_SIZE;
            if (rpos < rlim) {
              svalue = data.readUShort(rpos + RANGE_RECORD_START_OFFSET);
              lvalue = data.readUShort(rpos + RANGE_RECORD_END_OFFSET);
            }
          }
          return val;
        }
      };
    }

    @Override
    public int coveredGlyphCount() {
      int rpos = RANGE_RECORD_BASE + RANGE_RECORD_START_OFFSET + (count - 1) *
          RANGE_RECORD_SIZE;
      int svalue = data.readUShort(rpos + RANGE_RECORD_START_OFFSET);
      int lvalue = data.readUShort(rpos + RANGE_RECORD_END_OFFSET);
      int startIndex = data.readUShort(rpos + RANGE_RECORD_INDEX_OFFSET);
      return startIndex + lvalue - svalue + 1;
    }

    private int search(int glyphId) {
      int index = data.searchUShort(
          RANGE_RECORD_BASE + RANGE_RECORD_START_OFFSET, RANGE_RECORD_SIZE,
          RANGE_RECORD_BASE + RANGE_RECORD_END_OFFSET, RANGE_RECORD_SIZE,
          count, glyphId);
      return index;
    }
  }

  public static class Builder extends SubTable.Builder<CoverageTable> {
    private IntSetBuilder setBuilder;
    // stash results of subReadyToSerialize
    private boolean serializeFormat1; // true to serialize format 1
    private int serializeCount; // count to write in this format
    private int serializeLength; // length of serialized data in this format

    public Builder() {
      super(null);
      setBuilder = new IntSetBuilder();
    }

    public Builder(ReadableFontData data) {
      super(data);
    }

    public Builder(CoverageTable table) {
      super(table.readFontData());
    }

    public boolean contains(int glyphId) {
      prepareToEdit();
      return setBuilder.contains(glyphId);
    }

    public Builder add(int glyphId) {
      prepareToEdit();
      setBuilder.add(glyphId);
      setModelChanged();
      return this;
    }

    public Builder addRange(int lo, int hi) {
      prepareToEdit();
      setBuilder.add(lo, hi);
      setModelChanged();
      return this;
    }

    public Builder remove(int glyphId) {
      prepareToEdit();
      setBuilder.remove(glyphId);
      setModelChanged();
      return this;
    }

    public Builder removeRange(int lo, int hi) {
      prepareToEdit();
      setBuilder.remove(lo, hi);
      setModelChanged();
      return this;
    }

    public Builder removeAll() {
      prepareToEdit();
      setBuilder.removeAll();
      setModelChanged();
      return this;
    }

    void initFromFmt1(ReadableFontData data) {
      setBuilder = new IntSetBuilder();
      int count = data.readUShort(Fmt1.COUNT_OFFSET);
      for (int p = Fmt1.GLYPH_ARRAY_BASE,
          e = p + count * Fmt1.GLYPH_ARRAY_ELEM_SIZE; p < e;
          p += Fmt1.GLYPH_ARRAY_ELEM_SIZE) {
        setBuilder.add(data.readUShort(p));
      }
    }

    void initFromFmt2(ReadableFontData data) {
      setBuilder = new IntSetBuilder();
      int count = data.readUShort(Fmt2.RANGE_COUNT_OFFSET);
      for (int p = Fmt2.RANGE_RECORD_BASE,
          e = p + count * Fmt2.RANGE_RECORD_SIZE; p < e;
          p += Fmt2.RANGE_RECORD_SIZE) {
        int lo = data.readUShort(p + Fmt2.RANGE_RECORD_START_OFFSET);
        int hi = data.readUShort(p + Fmt2.RANGE_RECORD_END_OFFSET);
        setBuilder.add(lo, hi);
      }
    }

    void serializeToFmt1(WritableFontData data) {
      int p = Fmt1.GLYPH_ARRAY_BASE;
      IntIterator iter = setBuilder.iterator();
      while (iter.hasNext()) {
        data.writeUShort(p, iter.next());
        p += Fmt1.GLYPH_ARRAY_ELEM_SIZE;
      }
      int count = (p - Fmt1.GLYPH_ARRAY_BASE) /
          Fmt1.GLYPH_ARRAY_ELEM_SIZE;
      data.writeUShort(CoverageTable.FORMAT_OFFSET, FORMAT_1);
      data.writeUShort(Fmt1.COUNT_OFFSET, count);
    }

    void serializeToFmt2(WritableFontData data) {
      int glyphIndex = 0;
      int p = Fmt2.RANGE_RECORD_BASE;
      IntRangeIterator iter = setBuilder.rangeIterator();
      while (iter.hasNext()) {
        IntRange r = iter.next();
        data.writeUShort(p + Fmt2.RANGE_RECORD_START_OFFSET, r.lo);
        data.writeUShort(p + Fmt2.RANGE_RECORD_END_OFFSET, r.hi);
        data.writeUShort(p + Fmt2.RANGE_RECORD_INDEX_OFFSET, glyphIndex);
        glyphIndex += r.hi - r.lo + 1;
        p += Fmt2.RANGE_RECORD_SIZE;
      }
      int count = (p - Fmt2.RANGE_RECORD_BASE) /
          Fmt2.RANGE_RECORD_SIZE;
      data.writeUShort(CoverageTable.FORMAT_OFFSET, FORMAT_2);
      data.writeUShort(Fmt2.RANGE_COUNT_OFFSET, count);
    }

    private void initFromData(ReadableFontData data) {
      if (data == null) {
        setBuilder = new IntSetBuilder();
      } else if (getFormat(data) == FORMAT_1) {
        initFromFmt1(data);
      } else {
        initFromFmt2(data);
      }
    }

    IntIterator glyphIds() {
      prepareToEdit();
      return setBuilder.iterator();
    }

    private void prepareToEdit() {
      if (setBuilder == null) {
        initFromData(internalReadData());
      }
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      if (serializeFormat1) {
        serializeToFmt1(newData);
      } else {
        serializeToFmt2(newData);
      }
      return serializeLength;
    }

    @Override
    protected int subDataSizeToSerialize() {
      int rangeCount = setBuilder.rangeCount();
      int glyphCount = setBuilder.elementCount();
      if (glyphCount < rangeCount * 3) { // ranges take three times as much space
        serializeFormat1 = true;
        serializeCount = glyphCount;
        serializeLength = Fmt1.GLYPH_ARRAY_BASE +
            glyphCount * Fmt1.GLYPH_ARRAY_ELEM_SIZE;
      } else {
        serializeFormat1 = false;
        serializeCount = rangeCount;
        serializeLength = Fmt2.RANGE_RECORD_BASE +
            rangeCount * Fmt2.RANGE_RECORD_SIZE;
      }
      if (serializeCount == 0) {
        serializeLength = 0;
      }
      return serializeLength;
    }

    @Override
    protected void subDataSet() {
      setBuilder = null;
    }

    @Override
    protected CoverageTable subBuildTable(ReadableFontData data) {
      return CoverageTable.forData(data);
    }
  }
}

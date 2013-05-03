// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public abstract class ClassDefTable extends OTSubTable {
  private static final int FORMAT_OFFSET = 0;
  private static final int FORMAT_1 = 1;
  private static final int FORMAT_2 = 2;

  protected ClassDefTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  public abstract int glyphClass(int glyphId);
  public abstract int format();
  public abstract int numClasses();

  static int getFormat(ReadableFontData data) {
    int fmt = data.readUShort(FORMAT_OFFSET);
    if (!(fmt == FORMAT_1 || fmt == FORMAT_2)) {
      throw new IllegalArgumentException("unrecognized ClassDefTable format: " + fmt);
    }
    return fmt;
  }

  static ClassDefTable forData(ReadableFontData data, boolean dataIsCanonical) {
    if (getFormat(data) == FORMAT_1) {
      return new Fmt1(data, dataIsCanonical);
    }
    return new Fmt2(data, dataIsCanonical);
  }
  
  static int readNumClasses(ReadableFontData data) {
    if (getFormat(data) == FORMAT_1) {
      return Fmt1.readNumClasses(data);
    }
    return Fmt2.readNumClasses(data);
  }
  
  static Iterator<RangeClassRecord> rangeClassRecordIterator(ReadableFontData data) {
    if (getFormat(data) == FORMAT_1) {
      return Fmt1.rangeClassRecordIterator(data);
    }
    return Fmt2.rangeClassRecordIterator(data);
  }

  static interface RangeClassRecord {
    int first();
    int last();
    int glyphClass();
  }
  
  public static class RangeClassRecordImpl implements RangeClassRecord {
    int first;
    int last;
    int glyphClass;
    
    RangeClassRecordImpl(int first, int last, int glyphClass) {
      this.first = first;
      this.last = last;
      this.glyphClass = glyphClass;
    }

    @Override
    public int first() {
      return first;
    }
    
    @Override
    public int last() {
      return last;
    }
    
    @Override
    public int glyphClass() {
      return glyphClass;
    }
  }
  
  static abstract class RCRI implements Iterator<RangeClassRecord>, RangeClassRecord {
    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
  
  public static class Fmt1 extends ClassDefTable {
    private static final int START_GLYPH_OFFSET = 2;
    private static final int GLYPH_COUNT_OFFSET = 4;
    private static final int CLASS_ARRAY_BASE = 6;
    private static final int CLASS_SIZE = 2;

    int numClasses = -1;
    
    protected Fmt1(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    @Override
    public int glyphClass(int glyphId) {
      int firstGlyph = startGlyph();
      if (glyphId >= firstGlyph && glyphId < firstGlyph + glyphCount()) {
        return classValueAt(glyphId - firstGlyph);
      }
      return 0;
    }
    
    static int readNumClasses(ReadableFontData data) {
      int val = -1;
      for (int pos = CLASS_ARRAY_BASE, e = pos + GLYPH_COUNT_OFFSET * 
          CLASS_SIZE; pos < e; pos += CLASS_SIZE) {
        val = Math.max(val, data.readUShort(pos));
      }
      return val + 1;
    }
    
    @Override
    public int numClasses() {
      if (numClasses == -1) {
        numClasses = readNumClasses(data);
      }
      return numClasses;
    }
    
    @Override
    public int format() {
      return FORMAT_1;
    }

    static int readStartGlyph(ReadableFontData data) {
      return data.readUShort(START_GLYPH_OFFSET);
    }

    public int startGlyph() {
      return readStartGlyph(data);
    }

    static int readGlyphCount(ReadableFontData data) {
      return data.readUShort(GLYPH_COUNT_OFFSET);
    }

    public int glyphCount() {
      return readGlyphCount(data);
    }

    static int readClassValueAt(ReadableFontData data, int index) {
      return data.readUShort(CLASS_ARRAY_BASE + index * CLASS_SIZE);
    }
    
    /**
     * Index runs from 0 up to glyphCount.
     */
    public int classValueAt(int index) {
      return readClassValueAt(data, index);
    }

    static Iterator<RangeClassRecord> rangeClassRecordIterator(final ReadableFontData data) {
      return new RCRI() {
        int glyphId = data.readUShort(START_GLYPH_OFFSET) - 1;
        int maxGlyphId = glyphId + data.readUShort(GLYPH_COUNT_OFFSET);
        int pos = CLASS_ARRAY_BASE - CLASS_SIZE;
        @Override
        public boolean hasNext() {
          return glyphId < maxGlyphId;
        }

        @Override
        public RangeClassRecord next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          glyphId += 1;
          pos += CLASS_SIZE;
          return this;
        }

        @Override
        public int first() {
          return glyphId;
        }

        @Override
        public int last() {
          return glyphId;
        }

        @Override
        public int glyphClass() {
          return data.readUShort(pos);
        }
      };
    }

    @Override
    public Builder builder() {
      return new Builder(this);
    }
    
    public static class Builder extends OTSubTable.Builder<Fmt1> {
      int startGlyph;
      List<Integer> values;
      
      public Builder() {
        super();
      }
      
      public Builder(Fmt1 table) {
        super(table);
      }
      
      public Builder(ReadableFontData data, boolean dataIsCanonical) {
        super(data, dataIsCanonical);
      }
      
      public int glyphCount() {
        if (unedited()) {
          return Fmt1.readGlyphCount(internalReadData());
        }
        return values.size();
      }
      
      public Builder setStartGlyph(int startGlyph) {
        prepareToEdit();
        this.startGlyph = startGlyph;
        return this;
      }
      
      public Builder addGlyphClass(int glyphClass) {
        prepareToEdit();
        values.add(glyphClass);
        return this;
      }
      
      public Builder setGlyphClassAt(int index, int glyphClass) {
        prepareToEdit();
        values.set(index, glyphClass);
        return this;
      }
      
      public Builder removeGlyphClassAt(int index) {
        prepareToEdit();
        values.remove(index);
        return this;
      }

      @Override
      boolean unedited() {
        return values == null;
      }

      @Override
      void readModel(ReadableFontData data, boolean dataIsCanonical) {
        values = new ArrayList<Integer>();
        if (data != null) {
          startGlyph = Fmt1.readStartGlyph(data);
          for (int i = 0, e = Fmt1.readGlyphCount(data); i < e; ++i) {
            values.add(Fmt1.readClassValueAt(data, i));
          }
        }
      }

      @Override
      int computeSerializedLength() {
        if (values.isEmpty()) {
          return 0;
        }
        return Fmt1.CLASS_ARRAY_BASE +
            values.size() * Fmt1.CLASS_SIZE;
      }

      @Override
      void writeModel(WritableFontData data) {
        data.writeUShort(FORMAT_OFFSET, FORMAT_1);
        data.writeUShort(Fmt1.GLYPH_COUNT_OFFSET, values.size());
        int pos = Fmt1.CLASS_ARRAY_BASE;
        for (int glyphClass : values) {
          data.writeUShort(pos, glyphClass);
          pos += Fmt1.CLASS_SIZE;
        }
      }

      @Override
      public void subDataSet() {
        values = null;
      }

      @Override
      public Fmt1 subBuildTable(ReadableFontData data) {
        return new Fmt1(data, true);
      }
    }
  }

  public static class Fmt2 extends ClassDefTable {
    private static final int RANGE_COUNT_OFFSET = 2;
    private static final int RANGE_RECORD_ARRAY_BASE = 4;
    private static final int RANGE_RECORD_START_GLYPH_OFFSET = 0;
    private static final int RANGE_RECORD_END_GLYPH_OFFSET = 2;
    private static final int RANGE_RECORD_CLASS_OFFSET = 4;
    private static final int RANGE_RECORD_SIZE = 6;

    int numClasses;
    
    protected Fmt2(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    @Override
    public int glyphClass(int glyphId) {
      int index = data.searchUShort(
          RANGE_RECORD_ARRAY_BASE + RANGE_RECORD_START_GLYPH_OFFSET, RANGE_RECORD_SIZE,
          RANGE_RECORD_ARRAY_BASE + RANGE_RECORD_END_GLYPH_OFFSET, RANGE_RECORD_SIZE,
          rangeCount(), glyphId);
      if (index == -1) {
        return 0;
      }
      return rangeRecordClassAt(index);
    }
    
    static int readNumClasses(ReadableFontData data) {
      int val = -1;
      for (int pos = RANGE_RECORD_ARRAY_BASE + RANGE_RECORD_CLASS_OFFSET, 
          e = pos + readRangeCount(data) * RANGE_RECORD_SIZE; pos < e; 
          pos += RANGE_RECORD_SIZE) {
        val = Math.max(val, data.readUShort(pos));
      }
      return val + 1;
    }
    
    @Override
    public int numClasses() {
      if (numClasses == -1) {
        numClasses = readNumClasses(data);
      }
      return numClasses;
    }

    @Override
    public int format() {
      return FORMAT_2;
    }

    static int readRangeCount(ReadableFontData data) {
      return data.readUShort(RANGE_COUNT_OFFSET);
    }

    public int rangeCount() {
      return readRangeCount(data);
    }

    static int rangeRecordBaseAt(int index) {
      return RANGE_RECORD_ARRAY_BASE + index * RANGE_RECORD_SIZE;
    }

    static RangeClassRecordImpl readRangeClassRecordAt(ReadableFontData data, int index) {
      int base = rangeRecordBaseAt(index);
      return new RangeClassRecordImpl(
          data.readUShort(base + RANGE_RECORD_START_GLYPH_OFFSET),
          data.readUShort(base + RANGE_RECORD_END_GLYPH_OFFSET),
          data.readUShort(base + RANGE_RECORD_CLASS_OFFSET));
    }
    
    public RangeClassRecordImpl rangeClassRecordAt(int index) {
      return readRangeClassRecordAt(data, index);
    }
    
    static int readRangeRecordStartAt(ReadableFontData data, int index) {
      return data.readUShort(rangeRecordBaseAt(index) + RANGE_RECORD_START_GLYPH_OFFSET);
    }

    public int rangeRecordStartAt(int index) {
      return readRangeRecordStartAt(data, index);
    }

    static int readRangeRecordEndAt(ReadableFontData data, int index) {
      return data.readUShort(rangeRecordBaseAt(index) + RANGE_RECORD_END_GLYPH_OFFSET);
    }

    public int rangeRecordEndAt(int index) {
      return readRangeRecordEndAt(data, index);
    }

    static int readRangeRecordClassAt(ReadableFontData data, int index) {
      return data.readUShort(rangeRecordBaseAt(index) + RANGE_RECORD_CLASS_OFFSET);
    }

    public int rangeRecordClassAt(int index) {
      return readRangeRecordClassAt(data, index);
    }

    static Iterator<RangeClassRecord> rangeClassRecordIterator(final ReadableFontData data) {
      return new RCRI() {
        int pos = RANGE_RECORD_ARRAY_BASE - RANGE_RECORD_SIZE;
        int limit = pos + readRangeCount(data) * RANGE_RECORD_SIZE;
        @Override
        public boolean hasNext() {
          return pos < limit;
        }

        @Override
        public RangeClassRecord next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          pos += RANGE_RECORD_SIZE;
          return this;
        }

        @Override
        public int first() {
          return data.readUShort(pos + RANGE_RECORD_START_GLYPH_OFFSET);
        }

        @Override
        public int last() {
          return data.readUShort(pos + RANGE_RECORD_END_GLYPH_OFFSET);
        }

        @Override
        public int glyphClass() {
          return data.readUShort(pos + RANGE_RECORD_CLASS_OFFSET);
        }
      };
    }

    @Override
    public Builder builder() {
      return new Builder(this);
    }
    
    public static class Builder extends OTSubTable.Builder<Fmt2> {
      List<RangeClassRecordImpl> ranges;
      
      public Builder() {
        super();
      }
      
      public Builder(Fmt2 table) {
        super(table);
      }

      public Builder(ReadableFontData data, boolean dataIsCanonical) {
        super(data, dataIsCanonical);
      }

      public Builder setRangeClass(RangeClassRecord r) {
        return setRangeClass(r.first(), r.last(), r.glyphClass());
      }
      
      public Builder setRangeClass(int first, int last, int glyphClass) {
        int fi = -1, li = -1;
        // optimize for append by searching from end
        for (int i = ranges.size(); --i >= 0;) {
          RangeClassRecordImpl r = ranges.get(i);
          if (first >= r.first) {
            fi = i;
            if (first > r.last + 1 ||
                (first == r.last + 1 && glyphClass != r.glyphClass)) {
              fi += 1;
            }
            if (li == -1) {
              li = fi;
            }
            break;
          }
          if (li == -1) {
            if (last >= r.first ||
                (last == r.first - 1 && glyphClass == r.glyphClass)) {
              li = i;
            }
          }
        }
        if (fi == ranges.size()) {
          // pure append case
          if (glyphClass != 0) {
            ranges.add(new RangeClassRecordImpl(first, last, glyphClass));
          }
          return this;
        }
        if (li == -1) {
          // pure prepend case
          if (glyphClass != 0) {
            ranges.add(0, new RangeClassRecordImpl(first, last, glyphClass));
          }
          return this;
        }
        RangeClassRecordImpl fr = ranges.get(fi);
        if (last < fr.first - 1 ||
            (last == fr.first - 1 && glyphClass != fr.glyphClass)) {
          // pure insert case
          if (glyphClass != 0) {
            ranges.add(fi, new RangeClassRecordImpl(first, last, glyphClass));
          }
          return this;
        }
        if (first == fr.first && last == fr.last) {
          // pure replace case
          if (glyphClass != 0) {
            fr.glyphClass = glyphClass;
          } else {
            ranges.remove(fi);
          }
          return this;
        }
        RangeClassRecordImpl lr = ranges.get(li);
        if (glyphClass != lr.glyphClass && last < lr.last) {
          ranges.add(li + 1, new RangeClassRecordImpl(last + 1, lr.last, lr.glyphClass));
        }
        if (glyphClass != fr.glyphClass && first > fr.first) {
          ranges.add(fi, new RangeClassRecordImpl(fr.first, first - 1, fr.glyphClass));
          ++fi;
          ++li;
        }
        if (glyphClass == 0) {
          ranges.subList(li, fi+1).clear();
          return this;
        }
        ranges.subList(li+1, fi+1).clear();
        ranges.set(li, new RangeClassRecordImpl(first, last, glyphClass));
        return this;
      }
      
      @Override
      boolean unedited() {
        return ranges == null;
      }

      @Override
      void readModel(ReadableFontData data, boolean dataIsCanonical) {
        ranges = new ArrayList<RangeClassRecordImpl>();
        if (data != null) {
          for (int i = 0, e = Fmt2.readRangeCount(data); i < e; ++i) {
            ranges.add(Fmt2.readRangeClassRecordAt(data, i));
          }
        }
      }

      @Override
      int computeSerializedLength() {
        if (ranges.size() == 0) {
          return 0;
        }
        return Fmt2.RANGE_RECORD_ARRAY_BASE + ranges.size() * Fmt2.RANGE_RECORD_SIZE;
      }

      @Override
      void writeModel(WritableFontData data) {
        data.writeUShort(FORMAT_OFFSET, FORMAT_2);
        data.writeUShort(Fmt2.RANGE_COUNT_OFFSET, ranges.size());
        int pos = Fmt2.RANGE_RECORD_ARRAY_BASE;
        for (RangeClassRecordImpl r : ranges) {
          data.writeUShort(pos + Fmt2.RANGE_RECORD_END_GLYPH_OFFSET, r.first);
          data.writeUShort(pos + Fmt2.RANGE_RECORD_END_GLYPH_OFFSET, r.last);
          data.writeUShort(pos + Fmt2.RANGE_RECORD_CLASS_OFFSET, r.glyphClass);
          pos += Fmt2.RANGE_RECORD_SIZE;
        }
      }

      @Override
      public void subDataSet() {
        ranges = null;
      }

      @Override
      public Fmt2 subBuildTable(ReadableFontData data) {
        return new Fmt2(data, true);
      }
    }
  }
}

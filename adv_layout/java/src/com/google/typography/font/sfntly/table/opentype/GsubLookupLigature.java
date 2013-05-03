// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.IntSet.IntIterator;
import com.google.typography.font.sfntly.table.opentype.component.GsubLookupType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GsubLookupLigature extends GsubLookupTable {
  static final int FORMAT_1 = 1;

  protected GsubLookupLigature(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  public static GsubLookupLigature create(ReadableFontData data) {
    return new GsubLookupLigature(data, false);
  }

  @Override
  public GsubLookupType lookupType() {
    return GsubLookupType.GSUB_LIGATURE;
  }

  @Override
  public LigatureSubTable subTableAt(int index) {
    return (LigatureSubTable) super.subTableAt(index);
  }

  public int subTableFormatAt(int index) {
    int offset = offsetForIndex(index);
    return data.readUShort(offset + LigatureSubTable.FORMAT_OFFSET);
  }

  @Override
  protected LigatureSubTable createSubTable(ReadableFontData data) {
    int formatType = data.readUShort(LigatureSubTable.FORMAT_OFFSET);
    switch (formatType) {
      case FORMAT_1: return new LigatureSubTable(data, dataIsCanonical);
      default: throw new IllegalStateException(
          "unrecognized format type: " + formatType);
    }
  }

  @Override
  public Builder builder() {
    return new Builder(this);
  }

  public static class Builder extends GsubLookupTable.Builder<GsubLookupLigature> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder(GsubLookupLigature table) {
      super(table);
    }

    @Override
    public GsubLookupType lookupType() {
      return GsubLookupType.GSUB_LIGATURE;
    }

    public LigatureSubTable.Builder addLigatureSubTableBuilder() {
      LigatureSubTable.Builder nb = new LigatureSubTable.Builder();
      addSubTableBuilder(nb);
      return nb;
    }

    @Override
    public LigatureSubTable.Builder subTableBuilderAt(int index) {
      return (LigatureSubTable.Builder) super.subTableBuilderAt(index);
    }

    @Override
    protected LigatureSubTable.Builder createSubTableBuilder(ReadableFontData data) {
      return new LigatureSubTable.Builder(data, dataIsCanonical);
    }

    @Override
    public GsubLookupLigature subBuildTable(ReadableFontData data) {
      return new GsubLookupLigature(data, true);
    }
  }

  public static class LigatureSubTable extends GsubLookupSubTable {
    private static final int FORMAT_OFFSET = 0;
    private static final int COVERAGE_OFFSET = 2;
    private static final int LIG_SET_COUNT_OFFSET = 4;
    private static final int LIG_SET_OFFSETS_BASE = 6;
    private static final int LIG_SET_OFFSET_SIZE = 2;

    protected final CoverageTable coverage;

    public LigatureSubTable(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
      int offset = data.readUShort(COVERAGE_OFFSET);
      coverage = CoverageTable.forData(data.slice(offset));
    }

    public static LigatureSubTable create(ReadableFontData data) {
      return new LigatureSubTable(data, false);
    }

    @Override
    public GsubLookupType lookupType() {
      return GsubLookupType.GSUB_LIGATURE;
    }

    public boolean covers(int glyphId) {
      return coverage.covers(glyphId);
    }

    public CoverageTable coverage() {
      return coverage;
    }

    static int readLigSetCount(ReadableFontData data) {
      if (data == null) {
        return 0;
      }
      return data.readUShort(LIG_SET_COUNT_OFFSET);
    }

    public int ligSetCount() {
      return readLigSetCount(data);
    }

    static int readLigSetOffsetAt(ReadableFontData data, int index) {
      return data.readUShort(LIG_SET_OFFSETS_BASE + index * LIG_SET_OFFSET_SIZE);
    }

    int ligSetOffsetAt(int index) {
      return readLigSetOffsetAt(data, index);
    }

    public LigatureSet ligSetAt(int index) {
      ReadableFontData subData;
      int start = ligSetOffsetAt(index);
      if (dataIsCanonical) {
        int limit;
        if (index < ligSetCount() - 1) {
          limit = ligSetOffsetAt(index + 1);
        } else {
          limit = data.length();
        }
        subData = data.slice(start, limit - start);
      } else {
        subData = data.slice(start);
      }
      return new LigatureSet(subData, dataIsCanonical);
    }

    @Override
    public LigatureSubTable.Builder builder() {
      return new LigatureSubTable.Builder(this);
    }

    public static class LigatureSet extends OTSubTable {
      private static final int COUNT_OFFSET = 0;
      private static final int OFFSET_BASE = 2;
      private static final int OFFSET_SIZE = 2;

      LigatureSet(ReadableFontData data, boolean dataIsCanonical) {
        super(data, dataIsCanonical);
      }

      public int ligatureCount() {
        return data.readUShort(COUNT_OFFSET);
      }

      int tableOffsetAt(int index) {
        return data.readUShort(OFFSET_BASE + index * OFFSET_SIZE);
      }

      public LigatureTable ligatureTableAt(int index) {
        int substart = tableOffsetAt(index);
        ReadableFontData subData;
        if (dataIsCanonical) {
          int sublimit = -1;
          if (index < ligatureCount() - 1) {
            sublimit = tableOffsetAt(index + 1);
          } else {
            sublimit = data.length();
          }
          subData = data.slice(substart, sublimit - substart);
        } else {
          subData = data.slice(substart);
        }
        return new LigatureTable(subData, dataIsCanonical);
      }

      @Override
      public Builder builder() {
        return new Builder(this);
      }

      public static class Builder extends OTSubTable.Builder<LigatureSet> {
        private List<LigatureTable.Builder> builders;

        public Builder() {
          super();
        }

        public Builder(LigatureSet table) {
          super(table);
        }

        public Builder(ReadableFontData data, boolean dataIsCanonical) {
          super(data, dataIsCanonical);
        }

        public Builder addLigature(int ligatureGlyph, int... followingGlyphs) {
          prepareToEdit();
          addLigData(new LigatureTable.Builder(ligatureGlyph, followingGlyphs));
          return this;
        }

        public Builder removeLigature(int ligatureGlyph) {
          prepareToEdit();
          for (int i = 0; i < builders.size();) {
            LigatureTable.Builder b = builders.get(i);
            if (b.ligatureGlyph() == ligatureGlyph) {
              builders.remove(i);
            } else {
              ++i;
            }
          }
          return this;
        }

        private void addLigData(LigatureTable.Builder builder) {
          for (LigatureTable.Builder b : builders) {
            if (b.equalFollowingGlyphs(builder)) {
              b.setLigatureGlyph(builder.ligatureGlyph());
              return;
            }
          }
          builders.add(builder);
        }

        @Override
        boolean unedited() {
          return builders == null;
        }

        @Override
        void readModel(ReadableFontData data, boolean dataIsCanonical) {
          builders = new ArrayList<LigatureTable.Builder>();
          if (data != null) {
            LigatureSet wrappedSet = new LigatureSet(data, dataIsCanonical);
            for (int i = 0; i < wrappedSet.ligatureCount(); ++i) {
              LigatureTable lt = wrappedSet.ligatureTableAt(i);
              if (dataIsCanonical) {
                builders.add(lt.builder());
              } else {
                addLigData(lt.builder());
              }
            }
          }
        }

        @Override
        int computeSerializedLength() {
          int size = 0;
          Iterator<LigatureTable.Builder> iter = builders.iterator();
          while (iter.hasNext()) {
            LigatureTable.Builder b = iter.next();
            if (b.componentCount() == 0) {
              iter.remove();
            } else {
              size += b.subDataSizeToSerialize();
            }
          }
          if (size == 0) {
            return 0;
          }
          return OFFSET_BASE + builders.size() * OFFSET_SIZE + size;
        }

        @Override
        public void writeModel(WritableFontData newData) {
          newData.writeUShort(COUNT_OFFSET, builders.size());
          int rpos = OFFSET_BASE;
          int pos = rpos + builders.size() * OFFSET_SIZE;
          for (LigatureTable.Builder b : builders) {
            newData.writeUShort(rpos, pos);
            rpos += OFFSET_SIZE;
            pos += b.subSerialize(newData.slice(pos));
          }
        }

        @Override
        public void subDataSet() {
          builders = null;
        }

        @Override
        public LigatureSet subBuildTable(ReadableFontData data) {
          return new LigatureSet(data, true);
        }
      }
    }

    public static class LigatureTable extends OTSubTable {
      private static final int LIG_GLYPH_OFFSET = 0;
      private static final int COMPONENT_COUNT_OFFSET = 2;
      private static final int COMPONENT_BASE = 4;
      private static final int COMPONENT_SIZE = 2;

      public LigatureTable(ReadableFontData data, boolean dataIsCanonical) {
        super(data, dataIsCanonical);
      }

      public static int readLigatureGlyph(ReadableFontData data) {
        return data.readUShort(LIG_GLYPH_OFFSET);
      }

      public int ligatureGlyph() {
        return readLigatureGlyph(data);
      }

      /**
       * Includes first glyph, which is in coverage table, not in this table.
       */
      static int readComponentCount(ReadableFontData data) {
        return data.readUShort(COMPONENT_COUNT_OFFSET);
      }

      /**
       * Includes first glyph, which is in coverage table, not in this table.
       */
      public int componentCount() {
        return readComponentCount(data);
      }

      /**
       * The index 1 will return the first glyph in this table, which is the
       * second glyph comprising the ligature (the first is in the coverage table).
       * The max index is componentCount - 1;
       */
      static int readComponentAt(ReadableFontData data, int index) {
        return data.readUShort(COMPONENT_BASE + (index - 1) * COMPONENT_SIZE);
      }

      /**
       * The index 1 will return the first glyph in this table, which is the
       * second glyph comprising the ligature (the first is in the coverage table).
       * The max index is componentCount - 1;
       */
      public int componentAt(int index) {
        return readComponentAt(data, index);
      }

      @Override
      public Builder builder() {
        return new Builder(this);
      }

      static class Builder extends OTSubTable.Builder<LigatureTable> {
        int ligatureGlyph;
        List<Integer> components;

        public Builder() {
          super();
        }

        public Builder(LigatureTable table) {
          super(table);
        }

        public Builder(ReadableFontData data, boolean dataIsCanonical) {
          super(data, dataIsCanonical);
        }

        public Builder(int ligatureGlyph, int... allButFirstComponentGlyphs) {
          prepareToEdit(); // in order to reset serializedLength
          this.ligatureGlyph = ligatureGlyph;
          this.components = new ArrayList<Integer>(allButFirstComponentGlyphs.length);
          for (int i = 0; i < allButFirstComponentGlyphs.length; ++i) {
            this.components.add(allButFirstComponentGlyphs[i]);
          }
        }

        public int componentCount() {
          if (components != null) {
            return components.size();
          }
          ReadableFontData data = internalReadData();
          if (data == null) {
            return 0;
          }
          return LigatureTable.readComponentCount(data);
        }

        public int ligatureGlyph() {
          if (components != null) {
            return ligatureGlyph;
          }
          ReadableFontData data = internalReadData();
          if (data == null) {
            return -1;
          }
          return LigatureTable.readLigatureGlyph(data);
        }

        public Builder setLigatureGlyph(int ligatureGlyph) {
          prepareToEdit();
          this.ligatureGlyph = ligatureGlyph;
          return this;
        }

        public int componentAt(int index) {
          if (components != null) {
            return components.get(index);
          }
          ReadableFontData data = internalReadData();
          if (data == null) {
            return -1;
          }
          return LigatureTable.readComponentAt(data, index);
        }

        public Builder setComponentGlyphAt(int index, int componentGlyph) {
          prepareToEdit();
          components.set(index, componentGlyph);
          return this;
        }

        public Builder addComponentGlyph(int componentGlyph) {
          prepareToEdit();
          components.add(componentGlyph);
          return this;
        }

        public Builder deleteComponentGlyphAt(int index) {
          prepareToEdit();
          components.remove(index);
          return this;
        }

        public boolean equalFollowingGlyphs(LigatureTable.Builder builder) {
          int count = builder.componentCount();
          if (componentCount() != count) {
            return false;
          }
          for (int i = 1; i < count; ++i) {
            if (componentAt(i) != builder.componentAt(i)) {
              return false;
            }
          }
          return true;
        }

        @Override
        boolean unedited() {
          return components == null;
        }

        @Override
        void readModel(ReadableFontData data, boolean dataIsCanonical) {
          components = new ArrayList<Integer>();
          if (data != null) {
            LigatureTable table = new LigatureTable(data, dataIsCanonical);
            ligatureGlyph = table.ligatureGlyph();
            for (int i = 1; i < table.componentCount(); ++i) {
              components.add(table.componentAt(i));
            }
          }
        }

        @Override
        int computeSerializedLength() {
          if (components.isEmpty()) {
            return 0;
          }
          return COMPONENT_BASE + components.size() * COMPONENT_SIZE;
        }

        @Override
        void writeModel(WritableFontData data) {
          data.writeUShort(LIG_GLYPH_OFFSET, ligatureGlyph);
          data.writeUShort(COMPONENT_COUNT_OFFSET, components.size() + 1);
          int pos = COMPONENT_BASE;
          for (int componentGlyph : components) {
            data.writeUShort(pos, componentGlyph);
            pos += COMPONENT_SIZE;
          }
        }

        @Override
        public void subDataSet() {
          components = null;
        }

        @Override
        public LigatureTable subBuildTable(ReadableFontData data) {
          return new LigatureTable(data, true);
        }
      }
    }

    public static class Builder extends GsubLookupSubTable.Builder<LigatureSubTable> {
      private Map<Integer, LigatureSet.Builder> glyphToSetBuilderMap;
      private CoverageTable.Builder coverageBuilder;

      public Builder() {
      }

      public Builder(LigatureSubTable table) {
        super(table);
      }

      public Builder(ReadableFontData data, boolean dataIsCanonical) {
        super(data, dataIsCanonical);
      }

      @Override
      public GsubLookupType lookupType() {
        return GsubLookupType.GSUB_LIGATURE;
      }

      public LigatureSet.Builder setBuilder(int leadingGlyph) {
        prepareToEdit();
        LigatureSet.Builder builder = glyphToSetBuilderMap.get(leadingGlyph);
        if (builder == null) {
          builder = new LigatureSet.Builder();
          glyphToSetBuilderMap.put(leadingGlyph, builder);
        }
        return builder;
      }

      @Override
      boolean unedited() {
        return glyphToSetBuilderMap == null;
      }

      @Override
      void readModel(ReadableFontData data, boolean dataIsCanonical) {
        glyphToSetBuilderMap = new TreeMap<Integer, LigatureSet.Builder>();
        if (data != null) {
          LigatureSubTable table = new LigatureSubTable(data, dataIsCanonical);
          IntIterator iter = table.coverage().coveredGlyphs();
          int i = 0;
          while (iter.hasNext()) {
            int leadingGlyph = iter.next();
            LigatureSet.Builder setBuilder =
                new LigatureSet.Builder(table.ligSetAt(i++));
            glyphToSetBuilderMap.put(leadingGlyph, setBuilder);
          }
        }
      }

      @Override
      protected void writeModel(WritableFontData newData) {
        newData.writeUShort(LigatureSubTable.FORMAT_OFFSET, 1);
        int count = glyphToSetBuilderMap.size();
        newData.writeUShort(LigatureSubTable.LIG_SET_COUNT_OFFSET, count);
        int rpos = LigatureSubTable.LIG_SET_OFFSETS_BASE;
        int pos = rpos + count * LigatureSubTable.LIG_SET_OFFSET_SIZE;
        for (LigatureSet.Builder b : glyphToSetBuilderMap.values()) {
          newData.writeUShort(rpos, pos);
          rpos += LigatureSubTable.LIG_SET_OFFSET_SIZE;
          pos += b.subSerialize(newData.slice(pos));
        }
        newData.writeUShort(LigatureSubTable.COVERAGE_OFFSET, pos);
        coverageBuilder.subSerialize(newData.slice(pos));
      }

      @Override
      protected int computeSerializedLength() {
        // Clean up the data in preparation for serialization.
        coverageBuilder = new CoverageTable.Builder();
        int size = 0;
        Iterator<Map.Entry<Integer, LigatureSet.Builder>> setIterator =
            glyphToSetBuilderMap.entrySet().iterator();
        while (setIterator.hasNext()) {
          Map.Entry<Integer, LigatureSet.Builder> entry = setIterator.next();
          LigatureSet.Builder builder = entry.getValue();
          int setSize = builder.subDataSizeToSerialize();
          if (setSize == 0) {
            setIterator.remove();
          } else {
            size += setSize;
            coverageBuilder.add(entry.getKey());
          }
        }
        if (size == 0) {
          return 0;
        }
        return LIG_SET_OFFSETS_BASE +
            glyphToSetBuilderMap.size() * LIG_SET_OFFSET_SIZE +
            size +
            coverageBuilder.subDataSizeToSerialize();
      }

      @Override
      public LigatureSubTable subBuildTable(ReadableFontData data) {
        return new LigatureSubTable(data, true);
      }

      @Override
      public void subDataSet() {
       coverageBuilder = null;
       glyphToSetBuilderMap = null;
      }
    }
  }
}

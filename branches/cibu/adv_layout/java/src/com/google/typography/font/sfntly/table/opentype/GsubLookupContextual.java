// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.GsubLookupContextual.Fmt1.SubRuleSet;
import com.google.typography.font.sfntly.table.opentype.GsubLookupContextual.Fmt1.SubRuleSet.SubRule;
import com.google.typography.font.sfntly.table.opentype.GsubLookupContextual.Fmt2.SubClassSet;
import com.google.typography.font.sfntly.table.opentype.GsubLookupList.GsubLookupType;
import com.google.typography.font.sfntly.table.opentype.IntSet.IntIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public class GsubLookupContextual extends GsubLookupTable {
  static final int FORMAT_OFFSET = 0;
  static final int FORMAT_1 = 1;
  static final int FORMAT_2 = 2;
  static final int FORMAT_3 = 3;
  // TODO(dougfelt) move these to OTSubTable or higher
  static final int GLYPHID_SIZE = 2;
  static final int OFFSET_SIZE = 2;

  protected GsubLookupContextual(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  public static GsubLookupContextual create(ReadableFontData data) {
    return new GsubLookupContextual(data, false);
  }

  @Override
  public GsubLookupTable.Builder<GsubLookupContextual> builder() {
    return new Builder(this);
  }

  @Override
  public GsubLookupType lookupType() {
    return GsubLookupType.GSUB_CONTEXTUAL;
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
  public ContextualSubTable subTableAt(int index) {
    return (ContextualSubTable) super.subTableAt(index);
  }

  public static class Builder extends GsubLookupTable.Builder<GsubLookupContextual> {
    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder() {
      super();
    }

    public Builder(GsubLookupContextual table) {
      super(table);
    }

    @Override
    public GsubLookupType lookupType() {
      return GsubLookupType.GSUB_CONTEXTUAL;
    }

    public <T extends ContextualSubTable, B extends ContextualSubTable.Builder<T>> B
        addBuilder(B nb) {
      addSubTableBuilder(nb);
      return nb;
    }

    public <S extends ContextualSubTable> ContextualSubTable.Builder<S> addBuilderFor(S table) {
      ContextualSubTable.Builder<S> builder = (ContextualSubTable.Builder<S>) table.builder();
      return addBuilder(builder);
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

    public Fmt3Builder addFmt3Builder() {
      Fmt3Builder nb = new Fmt3Builder();
      addSubTableBuilder(nb);
      return nb;
    }

    @Override
    protected ContextualSubTable.Builder<?> createSubTableBuilder(ReadableFontData data) {
      int subFormat = data.readUShort(FORMAT_OFFSET);
      switch (subFormat) {
        case FORMAT_1: return new Fmt1Builder(data, dataIsCanonical);
        case FORMAT_2: return new Fmt2Builder(data, dataIsCanonical);
        case FORMAT_3: return new Fmt3Builder(data, dataIsCanonical);
        default: throw new IllegalStateException(
            "unrecognized GSubLookup1 format: " + subFormat);
      }
    }

    @Override
    protected GsubLookupContextual subBuildTable(ReadableFontData data) {
      return new GsubLookupContextual(data, true);
    }
  }

  public static abstract class ContextualSubTable extends GsubLookupSubTable {
    protected ContextualSubTable(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    @Override
    public GsubLookupType lookupType() {
      return GsubLookupType.GSUB_CONTEXTUAL;
    }

    @Override
    public abstract ContextualSubTable.Builder<?> builder();

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

    static abstract class Builder<T extends ContextualSubTable>
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
        return GsubLookupType.GSUB_CONTEXTUAL;
      }
    }
  }

  /**
   * Represents a substitution position/lookup pair.
   *
   * @author dougfelt@google.com (Doug Felt)
   */
  public static class SubstitutionRecord {
    public static final int POSITION_OFFSET = 0;
    public static final int LOOKUP_OFFSET = 2;
    public static final int SIZE = 4;
    
    /** position in context glyph array where lookup is applied */
    public final int position;
    /** index of lookup in lookup list */
    public final int lookup;
    
    public SubstitutionRecord(int position, int lookup) {
      this.position = position;
      this.lookup = lookup;
    }
    
    static SubstitutionRecord read(ReadableFontData data, int pos) {
      return new SubstitutionRecord(
          data.readUShort(pos + POSITION_OFFSET),
          data.readUShort(pos + LOOKUP_OFFSET));
    }
    
    void write(WritableFontData data, int pos) {
      data.writeUShort(pos + POSITION_OFFSET, position);
      data.writeUShort(pos + LOOKUP_OFFSET, lookup);
    }
    
    @Override
    public boolean equals(Object rhs) {
      return rhs instanceof SubstitutionRecord &&
          equals((SubstitutionRecord) rhs);
    }
    
    public boolean equals(SubstitutionRecord rhs) {
      return rhs != null &&
          rhs.position == position &&
          rhs.lookup == lookup;
    }
    
    @Override
    public int hashCode() {
      return (position * 17) ^ lookup;
    }
  }

  public static class Fmt1 extends ContextualSubTable {
    static final int COVERAGE_OFFSET = 2;
    static final int SUB_RULE_SET_COUNT_OFFSET = 4;
    static final int SUB_RULE_SET_BASE = 6;

    CoverageTable coverage;

    protected Fmt1(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    @Override
    public Fmt1Builder builder() {
      return new Fmt1Builder(this);
    }


    @Override
    public int format() {
      return FORMAT_1;
    }

    @Override
    public Fmt1 asFmt1() {
      return this;
    }

    public CoverageTable coverage() {
      if (coverage == null) {
        int offset = data.readUShort(COVERAGE_OFFSET);
        coverage = CoverageTable.forData(data.slice(offset));
      }
      return coverage;
    }

    static int readSubRuleSetCount(ReadableFontData data) {
      return data.readUShort(SUB_RULE_SET_COUNT_OFFSET);
    }

    public int subRuleSetCount() {
      return readSubRuleSetCount(data);
    }

    static int readSubRuleSetOffsetAt(ReadableFontData data, int index) {
      return data.readUShort(SUB_RULE_SET_BASE + index * OFFSET_SIZE);
    }

    public int subRuleSetOffsetAt(int index) {
      return readSubRuleSetOffsetAt(data, index);
    }

    public SubRuleSet subRuleSetAt(int index) {
      return new SubRuleSet(data.slice(subRuleSetOffsetAt(index)), dataIsCanonical);
    }

    static class SubRuleSet extends OTSubTable {
      static final int RULE_COUNT_OFFSET = 0;
      static final int RULE_BASE = 2;

      protected SubRuleSet(ReadableFontData data, boolean dataIsCanonical) {
        super(data, dataIsCanonical);
      }

      @Override
      public Fmt1Builder.RuleSetBuilder builder() {
        return new Fmt1Builder.RuleSetBuilder(this);
      }

      static int readRuleCount(ReadableFontData data) {
        return data.readUShort(RULE_COUNT_OFFSET);
      }

      public int ruleCount() {
        return readRuleCount(data);
      }

      static int readRuleOffsetAt(ReadableFontData data, int index) {
        return data.readUShort(RULE_BASE + index * OFFSET_SIZE);
      }

      int ruleOffsetAt(int index) {
        return readRuleOffsetAt(data, index);
      }

      public SubRule subRuleAt(int index) {
        int start = ruleOffsetAt(index);
        if (!dataIsCanonical) {
          return new SubRule(data.slice(start), false);
        }
        ++index;
        int limit = index < ruleCount() ? ruleOffsetAt(index) : data.length();
        return new SubRule(data.slice(start, limit - start), true);
      }

      static class SubRule extends OTSubTable {
        static final int GLYPH_COUNT_OFFSET = 0;
        static final int SUBSTITUTION_COUNT_OFFSET = 2;
        static final int INPUT_BASE = 4;
        static final int GLYPHID_SIZE = 2;
        static final int LOOKUP_RECORD_SIZE = 4;
        static final int SEQUENCE_INDEX_OFFSET = 0;
        static final int LOOKUP_LIST_INDEX_OFFSET = 2;

        protected SubRule(ReadableFontData data, boolean dataIsCanonical) {
          super(data, dataIsCanonical);
        }

        @Override
        public Fmt1Builder.RuleSetBuilder.RuleBuilder builder() {
          return new Fmt1Builder.RuleSetBuilder.RuleBuilder(this);
        }

        /**
         * Includes the first glyph (in coverage table).
         */
        static int readGlyphCount(ReadableFontData data) {
          return data.readUShort(GLYPH_COUNT_OFFSET);
        }

        /**
         * Includes the first glyph (in coverage table).
         */
        public int glyphCount() {
          return readGlyphCount(data);
        }

        static int readSubstitutionCount(ReadableFontData data) {
          return data.readUShort(SUBSTITUTION_COUNT_OFFSET);
        }

        public int substitutionCount() {
          return readSubstitutionCount(data);
        }

        /**
         * Index is 1-based, the first glyph (glyph 0) is in the coverage table.
         * Limit is glyphCount.
         */
        static int readInputGlyphIdAt(ReadableFontData data, int index) {
          return data.readUShort(INPUT_BASE + (index - 1) * GLYPHID_SIZE);
        }

        /**
         * Index is 1-based, the first glyph (glyph 0) is in the coverage table.
         * Limit is glyphCount.
         */
        public int inputGlyphIdAt(int index) {
          return readInputGlyphIdAt(data, index);
        }

        static int readSubstitutionBaseAt(ReadableFontData data, int index) {
          return INPUT_BASE + (readGlyphCount(data) - 1) * GLYPHID_SIZE +
              index * LOOKUP_RECORD_SIZE;
        }

        static int readSubstitutionSequenceIndexAt(ReadableFontData data, int index) {
          return data.readUShort(readSubstitutionBaseAt(data, index) + SEQUENCE_INDEX_OFFSET);
        }

        static int readSubstitutionLookupListIndexAt(ReadableFontData data, int index) {
          return data.readUShort(readSubstitutionBaseAt(data, index) + LOOKUP_LIST_INDEX_OFFSET);
        }

        public int substitutionSequenceIndexAt(int index) {
          return readSubstitutionSequenceIndexAt(data, index);
        }

        public int substitutionLookupIndexAt(int index) {
          return readSubstitutionLookupListIndexAt(data, index);
        }

        static SubstitutionRecord readSubstitutionAt(ReadableFontData data, int index) {
          int base = readSubstitutionBaseAt(data, index);
          return new SubstitutionRecord(
              data.readUShort(base + SEQUENCE_INDEX_OFFSET),
              data.readUShort(base + LOOKUP_LIST_INDEX_OFFSET));
        }

        public SubstitutionRecord substitutionAt(int index) {
          return readSubstitutionAt(data, index);
        }
      }
    }

    static OffsetIterator readOffsetIterator(ReadableFontData data) {
      int count = readSubRuleSetCount(data);
      return new OffsetIterator(data, SUB_RULE_SET_BASE, count);
    }

    static CoverageTable readCoverage(ReadableFontData data) {
      int offset = data.readUShort(COVERAGE_OFFSET);
      return CoverageTable.forData(data.slice(offset));
    }
  }

  private static class OffsetIterator implements IntIterator {
    private ReadableFontData data;
    private int pos;
    private int limit;
    private int increment;

    OffsetIterator(ReadableFontData data, int pos, int count) {
      this(data, pos, count, OFFSET_SIZE);
    }

    OffsetIterator(ReadableFontData data, int pos, int count, int increment) {
      this.data = data;
      this.pos = pos;
      this.limit = pos + count * increment;
      this.increment = increment;
    }

    @Override
    public boolean hasNext() {
      return pos < limit;
    }

    @Override
    public int next() {
      int val = data.readUShort(pos);
      pos += increment;
      return val;
    }
  }

  public static class Fmt1Builder extends ContextualSubTable.Builder<Fmt1> {
    private Map<Integer, RuleSetBuilder> builderMap;
    int serializedLength;
    CoverageTable.Builder coverageBuilder;

    public Fmt1Builder() {
      super();
    }

    public Fmt1Builder(Fmt1 fmt1) {
      super(fmt1);
    }

    public Fmt1Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    @Override
    public Fmt1Builder asFmt1Builder() {
      return this;
    }

    private void addBuilder(ReadableFontData data, int start, int limit, int glyphId) {
      ReadableFontData subData = limit == -1
          ? data.slice(start)
          : data.slice(start, limit - start);
      RuleSetBuilder rsb = new RuleSetBuilder(subData, limit != -1);
      builderMap.put(glyphId, rsb);
    }

    public RuleSetBuilder getRuleSetBuilder(int glyphId) {
      prepareToEdit();
      RuleSetBuilder setBuilder = builderMap.get(glyphId);
      if (setBuilder == null) {
        setBuilder = new RuleSetBuilder();
        builderMap.put(glyphId, setBuilder);
      }
      return setBuilder;
    }

    public Fmt1Builder deleteRuleSetBuilder(int glyphId) {
      prepareToEdit();
      builderMap.remove(glyphId);
      return this;
    }

    @Override
    protected Fmt1 subBuildTable(ReadableFontData data) {
      return new Fmt1(data, true);
    }

    @Override
    boolean unedited() {
      return builderMap == null;
    }

    @Override
    void readModel(ReadableFontData data, boolean dataIsCanonical) {
      builderMap = new TreeMap<Integer, RuleSetBuilder>();
      if (data != null) {
        int count = Fmt1.readSubRuleSetCount(data);
        if (count == 0) {
          return;
        }
        OffsetIterator iter = Fmt1.readOffsetIterator(data);
        CoverageTable coverageTable = Fmt1.readCoverage(data);
        IntIterator ii = coverageTable.coveredGlyphs();
        if (dataIsCanonical) {
          int pos = iter.next();
          while (ii.hasNext()) {
            int glyphId = ii.next();
            int npos = iter.hasNext() ? iter.next() : data.length();
            addBuilder(data, pos, npos, glyphId);
            pos = npos;
          }
        } else {
          while (ii.hasNext()) {
            int glyphId = ii.next();
            int pos = iter.next();
            addBuilder(data, pos, -1, glyphId);
          }
        }
      }
    }

    @Override
    protected int computeSerializedLength() {
      int totalLength = 0;
      coverageBuilder = new CoverageTable.Builder();
      Iterator<Map.Entry<Integer, RuleSetBuilder>> iter = builderMap.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<Integer, RuleSetBuilder> e = iter.next();
        RuleSetBuilder b = e.getValue();
        int len = b.subDataSizeToSerialize();
        if (len == 0) {
          iter.remove();
        } else {
          totalLength += len;
          coverageBuilder.add(e.getKey());
        }
      }
      if (totalLength == 0) {
        return 0;
      }
      return Fmt1.SUB_RULE_SET_BASE +
          builderMap.size() * OFFSET_SIZE +
          coverageBuilder.subDataSizeToSerialize() +
          totalLength;
    }

    @Override
    protected void writeModel(WritableFontData newData) {
      newData.writeUShort(FORMAT_OFFSET, FORMAT_1);
      int count = builderMap.size();
      newData.writeUShort(SUB_TABLE_COUNT_OFFSET, count);
      int rpos = SUB_TABLE_OFFSET_BASE;
      int pos = rpos + count * OFFSET_SIZE;
      for (RuleSetBuilder rsb : builderMap.values()) {
        newData.writeUShort(rpos, pos);
        rpos += OFFSET_SIZE;
        pos += rsb.subSerialize(newData.slice(pos));
      }
      newData.writeUShort(Fmt1.COVERAGE_OFFSET, pos);
      coverageBuilder.subSerialize(newData.slice(pos));
    }

    @Override
    protected void subDataSet() {
      builderMap = null;
      coverageBuilder = null;
    }


    public static class RuleSetBuilder extends OTSubTable.Builder<SubRuleSet> {
      List<RuleBuilder> ruleList;

      RuleSetBuilder() {
        super();
      }

      RuleSetBuilder(ReadableFontData data, boolean dataIsCanonical) {
        super(data, dataIsCanonical);
      }

      RuleSetBuilder(SubRuleSet ruleSet) {
        super(ruleSet);
      }

      public int ruleCount() {
        prepareToEdit();
        return ruleList.size();
      }

      public RuleBuilder ruleAt(int index) {
        prepareToEdit();
        return ruleList.get(index);
      }

      /**
       * Returns the index of the first rule in the list matching these glyphs,
       * or -1 if there is none.
       */
      public int ruleIndex(int... subsequentGlyphs) {
        prepareToEdit();
        for (int i = 0; i < ruleList.size(); ++i) {
          RuleBuilder rb = ruleList.get(i);
          if (rb.matchesGlyphs(subsequentGlyphs)) {
            return i;
          }
        }
        return -1;
      }

      public RuleSetBuilder deleteRuleAt(int index) {
        prepareToEdit();
        ruleList.remove(index);
        return this;
      }

      /**
       * Takes the rule at fromIndex and positions it before the rule at
       * toIndex.
       */
      public RuleSetBuilder moveRuleTo(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) {
          return this;
        }
        prepareToEdit();
        RuleBuilder builder = ruleList.get(fromIndex);
        if (fromIndex > toIndex) {
          ++fromIndex;
        }
        ruleList.add(toIndex, builder);
        ruleList.remove(fromIndex);
        return this;
      }

      public RuleBuilder addRule(int... subsequentGlyphs) {
        prepareToEdit();
        RuleBuilder ruleBuilder = new RuleBuilder(subsequentGlyphs);
        ruleList.add(ruleBuilder);
        return ruleBuilder;
      }

      public RuleBuilder addRuleAt(int index, int... subsequentGlyphs) {
        prepareToEdit();
        RuleBuilder ruleBuilder = new RuleBuilder(subsequentGlyphs);
        ruleList.add(index, ruleBuilder);
        return ruleBuilder;
      }

      @Override
      boolean unedited() {
        return ruleList == null;
      }

      @Override
      void readModel(ReadableFontData data, boolean dataIsCanonical) {
        ruleList = new ArrayList<RuleBuilder>();
        if (data != null) {
          int count = SubRuleSet.readRuleCount(data);
          if (dataIsCanonical) {
            int limit = data.length();
            int rpos = SubRuleSet.RULE_BASE;
            int pos = rpos + count * OFFSET_SIZE;
            for (int i = 1; i <= count; ++i) {
              int npos;
              if (i == count) {
                npos = limit;
              } else {
                rpos += OFFSET_SIZE;
                npos = data.readUShort(rpos);
              }
              ReadableFontData subData = data.slice(pos, npos - pos);
              pos = npos;
              RuleBuilder rb = new RuleBuilder(subData, true);
              ruleList.add(rb);
            }
          } else {
            for (int i = 0; i < count; ++i) {
              int pos = SubRuleSet.readRuleOffsetAt(data, i);
              ReadableFontData subData = data.slice(pos);
              RuleBuilder rb = new RuleBuilder(subData, false);
              ruleList.add(rb);
            }
          }
        }
      }

      @Override
      protected int computeSerializedLength() {
        int totalLen = 0;
        Iterator<RuleBuilder> iter = ruleList.iterator();
        while (iter.hasNext()) {
          RuleBuilder rb = iter.next();
          int len = rb.subDataSizeToSerialize();
          if (len == 0) {
            iter.remove();
          }
          totalLen += len;
        }
        if (totalLen == 0) {
          return 0;
        }
        return SubRuleSet.RULE_BASE +
            ruleList.size() * OFFSET_SIZE +
            totalLen;
      }

      @Override
      protected void writeModel(WritableFontData newData) {
        int count = ruleList.size();
        newData.writeUShort(SubRuleSet.RULE_COUNT_OFFSET, count);
        int rpos = SubRuleSet.RULE_BASE;
        int pos = rpos + count * OFFSET_SIZE;
        for (RuleBuilder rb : ruleList) {
          newData.writeUShort(rpos, pos);
          rpos += OFFSET_SIZE;
          pos += rb.subSerialize(newData.slice(pos));
        }
      }

      @Override
      protected void subDataSet() {
        ruleList = null;
      }

      @Override
      protected SubRuleSet subBuildTable(ReadableFontData data) {
        return new SubRuleSet(data, true);
      }


      public static class RuleBuilder extends OTSubTable.Builder<SubRule> {
        int[] subsequentGlyphs;
        List<SubstitutionRecord> substitutions;

        public RuleBuilder(int... subsequentGlyphs) {
          super();
          this.subsequentGlyphs = subsequentGlyphs;
        }

        public RuleBuilder(ReadableFontData data, boolean dataIsCanonical) {
          super(data, dataIsCanonical);
        }

        public RuleBuilder(SubRule subRule) {
          super(subRule);
        }

        void initGlyphs(ReadableFontData data) {
          if (data == null) {
            subsequentGlyphs = new int[0];
          } else {
            int count = SubRule.readGlyphCount(data) - 1;
            subsequentGlyphs = new int[count];
            for (int i = 0; i < count; ++i) {
              subsequentGlyphs[i] = SubRule.readInputGlyphIdAt(data, i + 1);
            }
          }
        }

        void prepareGlyphs() {
          if (subsequentGlyphs == null) {
            initGlyphs(internalReadData());
          }
        }

        public boolean matchesGlyphs(int... subsequentGlyphs) {
          prepareGlyphs();
          return Arrays.equals(this.subsequentGlyphs, subsequentGlyphs);
        }

        @Override
        protected boolean unedited() {
          return substitutions == null;
        }

        @Override
        void readModel(ReadableFontData data, boolean dataIsCanonical) {
          substitutions = new ArrayList<SubstitutionRecord>();
          if (data != null) {
            for (int i = 0, e = SubRule.readSubstitutionCount(data);
                i < e; ++i) {
              substitutions.add(SubRule.readSubstitutionAt(data, i));
            }
          }
        }

        /**
         * Includes the initial glyph in the count.
         */
        public int glyphCount() {
          prepareGlyphs();
          return subsequentGlyphs.length + 1;
        }

        /**
         * Minimum index is 1, the initial glyph is not available through
         * this method.
         */
        public int glyphAt(int index) {
          prepareGlyphs();
          return subsequentGlyphs[index - 1];
        }

        public int substitutionCount() {
          prepareToEdit();
          return substitutions.size();
        }

        public SubstitutionRecord substitutionAt(int index) {
          prepareToEdit();
          return substitutions.get(index);
        }

        public RuleBuilder addSubstitutionAt(int index, SubstitutionRecord substitution) {
          prepareToEdit();
          substitutions.add(index, substitution);
          return this;
        }

        public RuleBuilder addSubstitution(SubstitutionRecord substitution) {
          prepareToEdit();
          substitutions.add(substitution);
          return this;
        }

        public RuleBuilder setSubstitutionAt(int index, SubstitutionRecord substitition) {
          prepareToEdit();
          substitutions.set(index, substitition);
          return this;
        }

        public RuleBuilder deleteSubstitutionAt(int index) {
          prepareToEdit();
          substitutions.remove(index);
          return this;
        }

        @Override
        protected void writeModel(WritableFontData newData) {
          prepareGlyphs();
          newData.writeUShort(SubRule.GLYPH_COUNT_OFFSET, subsequentGlyphs.length + 1);
          newData.writeUShort(SubRule.SUBSTITUTION_COUNT_OFFSET, substitutions.size());
          int pos = SubRule.INPUT_BASE;
          for (int i = 0; i < subsequentGlyphs.length; ++i) {
            newData.writeUShort(pos, subsequentGlyphs[i]);
            pos += SubRule.GLYPHID_SIZE;
          }
          for (SubstitutionRecord sr : substitutions) {
            newData.writeUShort(pos + SubRule.SEQUENCE_INDEX_OFFSET, sr.position);
            newData.writeUShort(pos + SubRule.LOOKUP_LIST_INDEX_OFFSET, sr.lookup);
            pos += SubRule.LOOKUP_RECORD_SIZE;
          }
        }

        @Override
        protected int computeSerializedLength() {
          if (substitutions.size() == 0) {
            return 0;
          }
          prepareGlyphs();
          return SubRule.INPUT_BASE +
              subsequentGlyphs.length * SubRule.GLYPHID_SIZE +
              substitutions.size() * SubRule.LOOKUP_RECORD_SIZE;
        }

        @Override
        protected void subDataSet() {
          substitutions = null;
        }

        @Override
        protected SubRule subBuildTable(ReadableFontData data) {
          return new SubRule(data, true);
        }
      }
    }
  }

  static class Fmt2 extends ContextualSubTable {
    static final int COVERAGE_OFFSET = 2;
    static final int CLASS_DEF_OFFSET = 4;
    static final int SUB_CLASS_SET_COUNT_OFFSET = 6;
    static final int SUB_CLASS_SET_BASE = 8;

    final CoverageTable coverage;

    protected Fmt2(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
      int offset = data.readUShort(COVERAGE_OFFSET);
      coverage = CoverageTable.forData(data.slice(offset));
    }

    @Override
    public Fmt2Builder builder() {
      return new Fmt2Builder(this);
    }

    @Override
    public int format() {
      return FORMAT_2;
    }

    @Override
    public Fmt2 asFmt2() {
      return this;
    }

    static int readClassDefOffset(ReadableFontData data) {
      return data.readUShort(CLASS_DEF_OFFSET);
    }

    public int classDefOffset() {
      return readClassDefOffset(data);
    }

    public ClassDefTable classDefTable() {
      return ClassDefTable.forData(data.slice(classDefOffset()), dataIsCanonical);
    }

    static int readSubClassSetCount(ReadableFontData data) {
      return data.readUShort(SUB_CLASS_SET_COUNT_OFFSET);
    }

    public int subClassSetCount() {
      return readSubClassSetCount(data);
    }

    static int readSubClassSetOffsetAt(ReadableFontData data, int index) {
      return data.readUShort(SUB_CLASS_SET_BASE + index * OFFSET_SIZE);
    }

    /**
     * Returns null if there are no substitutions starting with glyphs in class index.
     */
    public SubClassSet subClassSetAt(int index) {
      int offset = readSubClassSetOffsetAt(data, index);
      if (offset == 0) {
        return null;
      }
      return new SubClassSet(data.slice(offset), dataIsCanonical);
    }

    static class SubClassSet extends OTSubTable {
      static final int RULE_COUNT_OFFSET = 0;
      static final int RULE_BASE = 2;

      protected SubClassSet(ReadableFontData data, boolean dataIsCanonical) {
        super(data, dataIsCanonical);
      }

      static int readRuleCount(ReadableFontData data) {
        return data.readUShort(RULE_COUNT_OFFSET);
      }

      public int ruleCount() {
        return readRuleCount(data);
      }

      static int readRuleOffsetAt(ReadableFontData data, int index) {
        return data.readUShort(RULE_BASE + index * OFFSET_SIZE);
      }

      public int ruleOffsetAt(int index) {
        return readRuleOffsetAt(data, index);
      }

      public SubClassRule subRuleAt(int index) {
        ReadableFontData slice = sliceData(RULE_BASE, index, ruleCount());
        return new SubClassRule(slice, dataIsCanonical);
      }

      @Override
      public Builder builder() {
        return new Builder(this);
      }
      
      static class Builder extends OTSubTable.Builder<SubClassSet> {
        List<SubClassRule.Builder> ruleBuilderList;
        
        public Builder() {
          super();
        }
        
        public Builder(SubClassSet table) {
          super(table);
        }
        
        public Builder(ReadableFontData data, boolean dataIsCanonical) {
          super(data, dataIsCanonical);
        }
        
        @Override
        boolean unedited() {
          return ruleBuilderList == null;
        }
        
        @Override
        void readModel(ReadableFontData data, boolean dataIsCanonical) {
          ruleBuilderList = new ArrayList<SubClassRule.Builder>();
          if (data != null) {
            int count = SubClassSet.readRuleCount(data);
            if (dataIsCanonical) {
              int limit = data.length();
              int rpos = SubClassSet.RULE_BASE;
              int pos = rpos + count * OFFSET_SIZE;
              for (int i = 1; i <= count; ++i) {
                int npos;
                if (i == count) {
                  npos = limit;
                } else {
                  rpos += OFFSET_SIZE;
                  npos = data.readUShort(rpos);
                }
                ReadableFontData subData = data.slice(pos, npos - pos);
                pos = npos;
                SubClassRule.Builder rb = new SubClassRule.Builder(subData, true);
                ruleBuilderList.add(rb);
              }
            } else {
              for (int i = 0; i < count; ++i) {
                int pos = SubClassSet.readRuleOffsetAt(data, i);
                ReadableFontData subData = data.slice(pos);
                SubClassRule.Builder rb = new SubClassRule.Builder(subData, false);
                ruleBuilderList.add(rb);
              }
            }
          }
        }
        
        @Override
        int computeSerializedLength() {
          int totLen = 0;
          Iterator<SubClassRule.Builder> iter = ruleBuilderList.iterator();
          while (iter.hasNext()) {
            SubClassRule.Builder rb = iter.next();
            int len = rb.subDataSizeToSerialize();
            if (len == 0) {
              iter.remove();
            }
            totLen += len;
          }
          if (totLen == 0) {
            return 0;
          }
          return RULE_BASE + 
              ruleBuilderList.size() * OFFSET_SIZE +
              totLen;
        }
        
        @Override
        void writeModel(WritableFontData newData) {
          int count = ruleBuilderList.size();
          newData.writeUShort(SubClassSet.RULE_COUNT_OFFSET, count);
          int rpos = SubClassSet.RULE_BASE;
          int pos = rpos + count * OFFSET_SIZE;
          for (SubClassRule.Builder rb : ruleBuilderList) {
            newData.writeUShort(rpos, pos);
            rpos += OFFSET_SIZE;
            pos += rb.subSerialize(newData.slice(pos));
          }
        }
        
        @Override
        protected void subDataSet() {
          ruleBuilderList = null;
        }
        
        @Override
        protected SubClassSet subBuildTable(ReadableFontData data) {
          return new SubClassSet(data, true);
        }
      }
      
      static class SubClassRule extends OTSubTable {
        static final int GLYPH_COUNT_OFFSET = 0;
        static final int SUBSTITUTION_COUNT_OFFSET = 2;
        static final int CLASS_BASE = 4;
        static final int CLASS_OFFSET_SIZE = 2;

        protected SubClassRule(ReadableFontData data, boolean dataIsCanonical) {
          super(data, dataIsCanonical);
        }

        static int readGlyphCount(ReadableFontData data) {
          return data.readUShort(GLYPH_COUNT_OFFSET);
        }

        /**
         * Includes the first glyph (in coverage table).
         */
        public int glyphCount() {
          return readGlyphCount(data);
        }

        static int readSubstitutionCount(ReadableFontData data) {
          return data.readUShort(SUBSTITUTION_COUNT_OFFSET);
        }

        public int substitutionCount() {
          return readSubstitutionCount(data);
        }

        /**
         * Index is 1-based, the first glyph (glyph 0) is in the coverage table.
         * Limit is glyphCount.
         */
        static int readClassAt(ReadableFontData data, int index) {
          return data.readUShort(CLASS_BASE + (index - 1) * CLASS_OFFSET_SIZE);
        }

        /**
         * Index is 1-based, the first glyph (glyph 0) is in the coverage table.
         * Limit is glyphCount.
         */
        public int classAt(int index) {
          return readClassAt(data, index);
        }

        static int readSubstitutionBaseAt(ReadableFontData data, int index) {
          return CLASS_BASE + (readGlyphCount(data) - 1) * CLASS_OFFSET_SIZE +
              index * SubstitutionRecord.SIZE;
        }
        
        static SubstitutionRecord readSubstitutionRecordAt(ReadableFontData data, int index) {
          int pos = readSubstitutionBaseAt(data, index);
          return SubstitutionRecord.read(data, pos);
        }
        
        public SubstitutionRecord substitutionRecordAt(int index) {
          return readSubstitutionRecordAt(data, index);
        }

        static int readSubstitutionSequenceIndexAt(ReadableFontData data, int index) {
          return data.readUShort(readSubstitutionBaseAt(data, index) +
              SubstitutionRecord.POSITION_OFFSET);
        }

        static int readSubstitutionLookupListIndexAt(ReadableFontData data, int index) {
          return data.readUShort(readSubstitutionBaseAt(data, index) +
              SubstitutionRecord.LOOKUP_OFFSET);
        }

        public int substitutionSequenceIndexAt(int index) {
          return readSubstitutionSequenceIndexAt(data, index);
        }

        public int substitutionLookupIndexAt(int index) {
          return readSubstitutionLookupListIndexAt(data, index);
        }
        
        @Override
        public Builder builder() {
          return new Builder(this);
        }
        
        public static class Builder extends OTSubTable.Builder<SubClassRule> {
          List<Integer> glyphClasses;
          List<SubstitutionRecord> substitutions;
          
          public Builder() {
            super();
          }
          
          public Builder(SubClassRule table) {
            super(table);
          }
          
          public Builder(ReadableFontData data, boolean dataIsCanonical) {
            super(data, dataIsCanonical);
          }
          
          public int glyphClassCount() {
            if (unedited()) {
              ReadableFontData data = internalReadData();
              return data == null ? 1 : readGlyphCount(data);
            }
            return glyphClasses.size() + 1;
          }
          
          public Builder addGlyphClass(int glyphClass) {
            prepareToEdit();
            glyphClasses.add(glyphClass);
            return this;
          }
          
          /**
           * 1-based index, min index is 1 since the glyph class at 0 is in the set.
           */
          public Builder setGlyphClassAt(int index, int glyphClass) {
            prepareToEdit();
            glyphClasses.set(index - 1, glyphClass);
            return this;
          }
          
          /**
           * 1-based index, min index is 1 since the glyph class at 0 is in the set.
           */
          public Builder removeGlyphClassAt(int index) {
            prepareToEdit();
            glyphClasses.remove(index - 1);
            return this;
          }
          
          public Builder setGlyphClasses(int... glyphClasses) {
            prepareToEdit();
            this.glyphClasses.clear();
            for (int gc : glyphClasses) {
              this.glyphClasses.add(gc);
            }
            return this;
          }
          
          public int substitutionRecordCount() {
            if (unedited()) {
              ReadableFontData data = internalReadData();
              return data == null ? 0 : readSubstitutionCount(data);
            }
            return substitutions.size();
          }
          
          public Builder addSubstitutionRecord(SubstitutionRecord sr) {
            prepareToEdit();
            substitutions.add(sr);
            return this;
          }
          
          public Builder addSubstitutionRecord(int offset, int lookup) {
            return addSubstitutionRecord(new SubstitutionRecord(offset, lookup));
          }
          
          public Builder addSubstitutionRecordAt(int index, SubstitutionRecord sr) {
            prepareToEdit();
            substitutions.add(index, sr);
            return this;
          }
          
          public Builder addSubstitutionRecordAt(int index, int offset, int lookup) {
            return addSubstitutionRecordAt(index, new SubstitutionRecord(index, lookup));
          }
          
          public Builder setSubstitutionRecordAt(int index, SubstitutionRecord sr) {
            prepareToEdit();
            substitutions.set(index, sr);
            return this;
          }
          
          public Builder setSubstitutionRecordAt(int index, int offset, int lookup) {
            return setSubstitutionRecordAt(index, new SubstitutionRecord(offset, lookup));
          }
          
          public Builder setSubstitutionOffsetAt(int index, int offset) {
            prepareToEdit();
            return setSubstitutionRecordAt(index, new SubstitutionRecord(offset,
                substitutions.get(index).lookup));
          }
          
          public Builder setSubstitutionLookupAt(int index, int lookup) {
            prepareToEdit();
            return setSubstitutionRecordAt(index, new SubstitutionRecord(
                substitutions.get(index).position, lookup));
          }
          
          public Builder removeSubstitutionLookupAt(int index) {
            prepareToEdit();
            substitutions.remove(index);
            return this;
          }
          
          public Builder moveSubstitutionLookupTo(int from, int to) {
            prepareToEdit();
            SubstitutionRecord sr = substitutions.get(from);
            substitutions.add(to, sr);
            if (to < from) {
              ++from;
            }
            substitutions.remove(from);
            return this;
          }
          
          @Override
          boolean unedited() {
            return substitutions == null;
          }
          
          @Override
          void readModel(ReadableFontData data, boolean dataIsCanonical) {
            glyphClasses = new ArrayList<Integer>();
            substitutions = new ArrayList<SubstitutionRecord>();
            if (data != null) {
              for (int i = 1, e = SubClassRule.readGlyphCount(data); i < e; ++i) {
                glyphClasses.add(SubClassRule.readClassAt(data, i));
              }
              for (int i = 0, e = SubClassRule.readSubstitutionCount(data); i < e; ++i) {
                substitutions.add(SubClassRule.readSubstitutionRecordAt(data, i));
              }
            }
          }
          
          @Override
          int computeSerializedLength() {
            if (substitutions.size() == 0) {
              return 0;
            }
            return SubClassRule.CLASS_BASE +
                glyphClasses.size() * CLASS_OFFSET_SIZE +
                substitutions.size() * SubstitutionRecord.SIZE;
          }
          
          @Override
          void writeModel(WritableFontData data) {
            data.writeUShort(GLYPH_COUNT_OFFSET, glyphClasses.size() + 1);
            data.writeUShort(SUBSTITUTION_COUNT_OFFSET, substitutions.size());
            int pos = CLASS_BASE;
            for (int glyphClass : glyphClasses) {
              data.writeUShort(pos, glyphClass);
              pos += CLASS_OFFSET_SIZE;
            }
            for (SubstitutionRecord r : substitutions) {
              r.write(data, pos);
              pos += SubstitutionRecord.SIZE;
            }
          }
          
          @Override
          protected void subDataSet() {
            glyphClasses = null;
            substitutions = null;
          }
          
          @Override
          protected SubClassRule subBuildTable(ReadableFontData data) {
            return new SubClassRule(data, true);
          }
        }
      }
    }
  }

  public static class Fmt2Builder extends ContextualSubTable.Builder<Fmt2> {
    CoverageTable.Builder cb;
    ClassDefTable.Fmt2.Builder cdb;
    List<SubClassSet.Builder> setBuilders;
    
    public Fmt2Builder() {
      super();
    }

    public Fmt2Builder(Fmt2 fmt) {
      super(fmt);
    }

    public Fmt2Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    @Override
    boolean unedited() {
      return cdb == null;
    }
    
    // TODO(dougfelt) add editing methods
    
    @Override
    void readModel(ReadableFontData data, boolean dataIsCanonical) {
      cdb = null;
      setBuilders = new ArrayList<SubClassSet.Builder>();
      if (data == null) {
        cdb = new ClassDefTable.Fmt2.Builder();
      } else {
        // Build class def table.  If already format_2, just init directly from
        // the data, else convert from format_1 to format_2.  We do this because
        // Format_2 is more general.  
        // TODO(dougfelt): investigate deferring builder initialization
        ReadableFontData classDefData = data.slice(Fmt2.readClassDefOffset(data));
        int classDefFormat = ClassDefTable.getFormat(classDefData);
        if (classDefFormat == FORMAT_2) {
          cdb =  new ClassDefTable.Fmt2.Builder(data, false);
        } else {
          cdb = new ClassDefTable.Fmt2.Builder();
          Iterator<ClassDefTable.RangeClassRecord> iter = 
              ClassDefTable.rangeClassRecordIterator(classDefData);
          while (iter.hasNext()) {
            ClassDefTable.RangeClassRecord r = iter.next();
            cdb.setRangeClass(r);
          }
        }
        int numClasses = ClassDefTable.readNumClasses(classDefData);
        if (dataIsCanonical) {
          int limit = data.length();
          int rpos = Fmt2.SUB_CLASS_SET_BASE;
          int pos = Fmt2.readSubClassSetOffsetAt(data, 0);
          for (int i = 1; i <= numClasses; ++i) {
            int npos;
            if (i == numClasses) {
              npos = limit;
            } else {
              rpos += OFFSET_SIZE;
              npos = data.readUShort(rpos);
            }
            if (npos == 0) {
              setBuilders.add(null);
              continue;
            }
            ReadableFontData subData = data.slice(pos, npos - pos);
            pos = npos;
            SubClassSet.Builder scsb = new SubClassSet.Builder(subData, true);
            setBuilders.add(scsb);
          }
        } else {
          for (int i = 0; i < numClasses; ++i) {
            int pos = SubRuleSet.readRuleOffsetAt(data, i);
            if (pos == 0) {
              setBuilders.add(null);
            } else {
              ReadableFontData subData = data.slice(pos);
              SubClassSet.Builder scsb = new SubClassSet.Builder(subData, false);
              setBuilders.add(scsb);
            }
          }
        }
      }
    }

    @Override
    int computeSerializedLength() {
      boolean includeClass0 = false;
      int totLen = 0;
      Set<Integer> classSet = new HashSet<Integer>();
      for (int i = 0, e = setBuilders.size(); i < e; ++i) {
        SubClassSet.Builder scsb = setBuilders.get(i);
        if (scsb == null) {
          continue;
        }
        int len = scsb.subDataSizeToSerialize();
        if (len == 0) {
          setBuilders.set(i, null);
          continue;
        }
        if (i == 0) {
          // This is not a good situation.  The way the ClassDef
          // table works is that any glyphs not mentioned are in class 0.
          // So we have to special case the scan to extract these ranges.
          includeClass0 = true;
        } else {
          classSet.add(i);
        }
        totLen += len;
      }
      if (totLen == 0) {
        return 0;
      }
      // Empty sets at the end of the list can be dropped.
      int setCount = setBuilders.size();
      for (; setCount > 0; --setCount) {
        if (setBuilders.get(setCount - 1) != null) {
          break;
        }
      }
      if (setCount == 0) {
        return 0;
      }
      if (setCount < setBuilders.size()) {
        setBuilders = setBuilders.subList(0, setCount);
      }
      cb = new CoverageTable.Builder();
      int lastGlyphId = -1; // last in previous range
      for (ClassDefTable.RangeClassRecordImpl record : cdb.ranges) {
        if (includeClass0) {
          if (record.first > lastGlyphId + 1) {
            cb.addRange(lastGlyphId + 1, record.first - 1);
          }
          lastGlyphId = record.last;
        }
        if (classSet.contains(record.glyphClass)) {
          cb.addRange(record.first, record.last);
        }
      }
      return 0;
    }

    @Override
    void writeModel(WritableFontData newData) {
      newData.writeUShort(FORMAT_OFFSET, FORMAT_2);
      int pos = SUB_TABLE_OFFSET_BASE + setBuilders.size() * OFFSET_SIZE;
      newData.writeUShort(Fmt2.COVERAGE_OFFSET, pos);
      pos = cb.subSerialize(newData);
      newData.writeUShort(Fmt2.CLASS_DEF_OFFSET, pos);
      pos = cdb.subSerialize(newData);
      int rpos = SUB_TABLE_OFFSET_BASE;
      for (SubClassSet.Builder sb : setBuilders) {
        if (sb == null) {
          newData.writeUShort(rpos, 0);
        } else {
          pos = sb.subSerialize(newData);
        }
        rpos += OFFSET_SIZE;
      }
    }

    @Override
    protected void subDataSet() {
      cb = null;
      cdb = null;
      setBuilders = null;
    }

    @Override
    protected Fmt2 subBuildTable(ReadableFontData data) {
      return new Fmt2(data, true);
    }
  }

  static class Fmt3 extends ContextualSubTable {
    static final int GLYPH_COUNT_OFFSET = 2;
    static final int SUBSTITUTION_COUNT_OFFSET = 4;
    static final int COVERAGE_OFFSET_ARRAY_BASE = 6;
    static final int OFFSET_SIZE = 2;
    static final int LOOKUP_RECORD_SIZE = 4;
    static final int SEQUENCE_INDEX_OFFSET = 0;
    static final int LOOKUP_LIST_INDEX_OFFSET = 2;

    protected Fmt3(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    @Override
    public Fmt3Builder builder() {
      return new Fmt3Builder(this);
    }

    @Override
    public int format() {
      return FORMAT_3;
    }

    @Override
    public Fmt3 asFmt3() {
      return this;
    }

    static int readGlyphCount(ReadableFontData data) {
      return data.readUShort(GLYPH_COUNT_OFFSET);
    }

    public int glyphCount() {
      return readGlyphCount(data);
    }

    static int readSubstitutionCount(ReadableFontData data) {
      return data.readUShort(SUBSTITUTION_COUNT_OFFSET);
    }

    public int subsitutionCount() {
      return readSubstitutionCount(data);
    }

    static int readCoverageTableOffsetAt(ReadableFontData data, int index) {
      return data.readUShort(COVERAGE_OFFSET_ARRAY_BASE + index * OFFSET_SIZE);
    }

    public CoverageTable coverageTableAt(int index) {
      return CoverageTable.forData(data.slice(readCoverageTableOffsetAt(data, index)));
    }

    static int readSubstitutionBaseAt(ReadableFontData data, int index) {
      return COVERAGE_OFFSET_ARRAY_BASE + readGlyphCount(data) * OFFSET_SIZE +
          index * LOOKUP_RECORD_SIZE;
    }

    static int readSubstitutionSequenceIndexAt(ReadableFontData data, int index) {
      return data.readUShort(readSubstitutionBaseAt(data, index) + SEQUENCE_INDEX_OFFSET);
    }

    static int readSubstitutionLookupListIndexAt(ReadableFontData data, int index) {
      return data.readUShort(readSubstitutionBaseAt(data, index) + LOOKUP_LIST_INDEX_OFFSET);
    }

    public int substitutionSequenceIndexAt(int index) {
      return readSubstitutionSequenceIndexAt(data, index);
    }

    public int substitutionLookupIndexAt(int index) {
      return readSubstitutionLookupListIndexAt(data, index);
    }
  }

  public static class Fmt3Builder extends ContextualSubTable.Builder<Fmt3> {
    public Fmt3Builder() {
      super();
    }

    public Fmt3Builder(Fmt3 fmt) {
      super(fmt);
    }

    public Fmt3Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    @Override
    protected Fmt3 subBuildTable(ReadableFontData data) {
      // TODO(dougfelt): Auto-generated method stub
      return null;
    }

    @Override
    boolean unedited() {
      // TODO(dougfelt): Auto-generated method stub
      return false;
    }

    @Override
    void readModel(ReadableFontData data, boolean dataIsCanonical) {
      // TODO(dougfelt): Auto-generated method stub
    }

    @Override
    int computeSerializedLength() {
      // TODO(dougfelt): Auto-generated method stub
      return 0;
    }

    @Override
    void writeModel(WritableFontData data) {
      // TODO(dougfelt): Auto-generated method stub
    }

    @Override
    protected void subDataSet() {
      // TODO(dougfelt): Auto-generated method stub
    }
  }
}

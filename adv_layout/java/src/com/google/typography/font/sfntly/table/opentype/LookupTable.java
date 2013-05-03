// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class LookupTable extends OTSubTable {
  private final int flags;

  protected LookupTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
    flags = readLookupFlags(data);
  }

  @Override
  public abstract Builder<? extends LookupTable> builder();

  public static interface LookupType {
    int typeNum();
  }

  static final int LOOKUP_TYPE_OFFSET = 0;
  static final int LOOKUP_FLAG_OFFSET = 2;
  static final int SUB_TABLE_COUNT_OFFSET = 4;
  static final int SUB_TABLE_OFFSET_BASE = 6;
  static final int SUB_TABLE_OFFSET_SIZE = 2;

  static final int FLAG_MARK_ATTACHMENT_TYPE = 0xff00;
  static final int FLAG_USE_MARK_FILTERING_SET = 0x10;

  public enum LookupFlag {
    RIGHT_TO_LEFT(1),
    IGNORE_BASE_GLYPHS(2),
    IGNORE_LIGATURES(4),
    IGNORE_MARKS(8);

    boolean isSet(int flags) {
      return isFlagSet(flags, mask);
    }

    int set(int flags) {
      return setFlag(flags, mask);
    }

    int clear(int flags) {
      return clearFlag(flags, mask);
    }

    private final int mask;
    private LookupFlag(int mask) {
      this.mask = mask;
    }
  }

  static int readLookupType(ReadableFontData data) {
    return data.readUShort(LOOKUP_TYPE_OFFSET);
  }

  public abstract LookupType lookupType();

  static int readLookupFlags(ReadableFontData data) {
    if (data == null) {
      return 0;
    }
    return data.readUShort(LOOKUP_FLAG_OFFSET);
  }

  static boolean isFlagSet(int flags, int mask) {
    return (flags & mask) != 0;
  }

  static int setFlag(int flags, int mask) {
    return flags | mask;
  }

  static int clearFlag(int flags, int mask) {
    return flags & ~mask;
  }

  public boolean rightToLeft() {
    return LookupFlag.RIGHT_TO_LEFT.isSet(flags);
  }

  public boolean ignoreBaseGlyphs() {
    return LookupFlag.IGNORE_BASE_GLYPHS.isSet(flags);
  }

  public boolean ignoreLigatures() {
    return LookupFlag.IGNORE_LIGATURES.isSet(flags);
  }

  public boolean ignoreMarks() {
    return LookupFlag.IGNORE_MARKS.isSet(flags);
  }

  public boolean useMarkFilteringSet() {
    return isFlagSet(flags, FLAG_USE_MARK_FILTERING_SET);
  }

  public int markAttachmentType() {
    return (flags & FLAG_MARK_ATTACHMENT_TYPE) >>> 8;
  }

  static int readSubTableCount(ReadableFontData data) {
    if (data == null) {
      return 0;
    }
    return data.readUShort(SUB_TABLE_COUNT_OFFSET);
  }

  public int subTableCount() {
    return readSubTableCount(data);
  }

  static int readOffsetForIndex(ReadableFontData data, int index) {
    return data.readUShort(SUB_TABLE_OFFSET_BASE + index * SUB_TABLE_OFFSET_SIZE);
  }

  protected int offsetForIndex(int index) {
    return readOffsetForIndex(data, index);
  }

  static ReadableFontData readSubTableDataAt(ReadableFontData data, boolean dataIsCanonical,
      int index) {
    int start = readOffsetForIndex(data, index);
    if (dataIsCanonical) {
      int limit;
      if (index < readSubTableCount(data) - 1) {
        limit = readOffsetForIndex(data, index + 1);
      } else {
        limit = data.length();
      }
      return data.slice(start, limit - start);
    }
    return data.slice(start);
  }

  protected ReadableFontData subTableDataAt(int index) {
    return readSubTableDataAt(data, dataIsCanonical, index);
  }

  public LookupSubTable subTableAt(int index) {
    ReadableFontData subData = subTableDataAt(index);
    return createSubTable(subData);
  }

  protected abstract LookupSubTable createSubTable(ReadableFontData data);

  /**
   * Returns -1 if mark filtering sets are not used.
   */
  public int markFilteringSetOffset() {
    if (useMarkFilteringSet()) {
      int pos = SUB_TABLE_OFFSET_BASE + subTableCount() * SUB_TABLE_OFFSET_SIZE;
      return data.readUShort(pos);
    }
    return -1;
  }

  static abstract class Builder<T extends LookupTable> extends OTSubTable.Builder<T> {
    private List<LookupSubTable.Builder<?>> builders;
    private int flags;
    private int markFilteringSetOffset;

    protected Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    protected Builder() {
    }
    
    protected Builder(T table) {
      super(table);
    }

    abstract LookupType lookupType();

    protected abstract LookupSubTable.Builder<?> createSubTableBuilder(
        ReadableFontData data);

    public boolean isSet(LookupFlag flag) {
      return flag.isSet(flags);
    }

    public Builder<T> setFlag(LookupFlag flag) {
      flags = flag.set(flags);
      return this;
    }

    public Builder<T> clearFlag(LookupFlag flag) {
      flags = flag.clear(flags);
      return this;
    }

    public Builder<T> setMarkFilteringOffset(int offset) {
      if (offset > 0) {
        flags = LookupTable.setFlag(flags, FLAG_USE_MARK_FILTERING_SET);
        markFilteringSetOffset = offset;
      } else {
        flags = LookupTable.clearFlag(flags, FLAG_USE_MARK_FILTERING_SET);
      }
      return this;
    }

    public Builder<T> setMarkAttachmentType(int type) {
      flags &= (0x00ff);
      flags |= (type << 8) & 0xff00;
      return this;
    }

    public Builder<T> addSubTableBuilder(LookupSubTable.Builder<?> builder) {
      prepareToEdit();
      builders.add(builder);
      return this;
    }

    public Builder<T> addSubTableBuilderAt(int index, LookupSubTable.Builder<?> builder) {
      prepareToEdit();
      builders.add(index, builder);
      return this;
    }

    public LookupSubTable.Builder<?> subTableBuilderAt(int index) {
      prepareToEdit();
      return builders.get(index);
    }

    public int subTableCount() {
      prepareToEdit();
      return builders.size();
    }

    public Builder<T> removeSubTableBuilderAt(int index) {
      prepareToEdit();
      builders.remove(index);
      return this;
    }

    public Builder<T> clearSubTables() {
      prepareToEdit();
      builders.clear();
      return this;
    }

    @Override
    boolean unedited() {
      return builders == null;
    }
    
    @Override
    void readModel(ReadableFontData data, boolean dataIsCanonical) {
      flags = readLookupFlags(data);
      markFilteringSetOffset = 0;
      builders = new ArrayList<LookupSubTable.Builder<?>>();
      if (data == null) {
        return;
      }

      boolean useMarkFilteringSet = isFlagSet(flags, FLAG_USE_MARK_FILTERING_SET);
      int count = readSubTableCount(data);

      if (useMarkFilteringSet) {
        int markPos = SUB_TABLE_OFFSET_BASE + count * SUB_TABLE_OFFSET_SIZE;
        markFilteringSetOffset = data.readUShort(markPos);
      }

      int rpos = SUB_TABLE_OFFSET_BASE;
      for (int i = 0; i < count; ++i) {
        int pos = data.readUShort(rpos);
        rpos += SUB_TABLE_OFFSET_SIZE;
        LookupSubTable.Builder<?> subBuilder = createSubTableBuilder(data.slice(pos));
        builders.add(subBuilder);
      }
    }

    @Override
    int computeSerializedLength() {
      int totalLength = 0;
      Iterator<LookupSubTable.Builder<?>> iter = builders.iterator();
      while (iter.hasNext()) {
        LookupSubTable.Builder<?> builder = iter.next();
        int len = builder.subDataSizeToSerialize();
        if (len == 0) {
          iter.remove();
        } else {
          totalLength += len;
        }
      }
      if (totalLength > 0) {
        totalLength += SUB_TABLE_OFFSET_BASE;
        if (isFlagSet(flags, FLAG_USE_MARK_FILTERING_SET)) {
          totalLength += 2;
        }
        totalLength += builders.size() * SUB_TABLE_OFFSET_SIZE;
      }
      return totalLength;
    }

    @Override
    void writeModel(WritableFontData newData) {
      newData.writeUShort(LOOKUP_TYPE_OFFSET, lookupType().typeNum());
      newData.writeUShort(LOOKUP_FLAG_OFFSET, flags);
      newData.writeUShort(SUB_TABLE_COUNT_OFFSET, builders.size());
      int rpos = SUB_TABLE_OFFSET_BASE;
      int pos = rpos + builders.size() * SUB_TABLE_OFFSET_SIZE;
      if (isFlagSet(flags, FLAG_USE_MARK_FILTERING_SET)) {
        newData.writeUShort(pos, markFilteringSetOffset);
        pos += 2;
      }
      for (LookupSubTable.Builder<?> builder : builders) {
        int len = builder.subSerialize(newData.slice(pos));
        newData.writeUShort(rpos, pos);
        pos += len;
        rpos += SUB_TABLE_OFFSET_SIZE;
      }
    }

    @Override
    public void subDataSet() {
      builders = null;
    }
  }
}

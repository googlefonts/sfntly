/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.typography.font.sfntly.table.bitmap;

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.math.FontMath;
import com.google.typography.font.sfntly.table.SubTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public final class BitmapSizeTable extends SubTable {
  // binary search would be faster but many fonts have index subtables that
  // aren't sorted
  private static final boolean USE_BINARY_SEARCH = false;

  private final Object indexSubTablesLock = new Object();
  private volatile List<IndexSubTable> indexSubTables = null;

  interface Offset {
    int indexSubTableArrayOffset = 0;
    int indexTableSize = 4;
    int numberOfIndexSubTables = 8;
    int colorRef = 12;
    int hori = 16;
    int vert = 28;
    int startGlyphIndex = 40;
    int endGlyphIndex = 42;
    int ppemX = 44;
    int ppemY = 45;
    int bitDepth = 46;
    int flags = 47;
    int SIZE = 48;
  }

  protected BitmapSizeTable(ReadableFontData data, ReadableFontData masterData) {
    super(data, masterData);
  }

  public int indexSubTableArrayOffset() {
    return data.readULongAsInt(Offset.indexSubTableArrayOffset);
  }

  public int indexTableSize() {
    return data.readULongAsInt(Offset.indexTableSize);
  }

  private static int numberOfIndexSubTables(ReadableFontData data, int tableOffset) {
    return data.readULongAsInt(tableOffset + Offset.numberOfIndexSubTables);
  }

  public int numberOfIndexSubTables() {
    return numberOfIndexSubTables(data, 0);
  }

  public int colorRef() {
    return data.readULongAsInt(Offset.colorRef);
  }

  // TODO(stuartg): implement later
  public void /* SBitLineMetrics */ hori() {
    // NOP
  }

  // TODO(stuartg): implement later
  public void /* SBitLineMetrics */ vert() {
    // NOP
  }

  public int startGlyphIndex() {
    return data.readUShort(Offset.startGlyphIndex);
  }

  public int endGlyphIndex() {
    return data.readUShort(Offset.endGlyphIndex);
  }

  public int ppemX() {
    return data.readByte(Offset.ppemX);
  }

  public int ppemY() {
    return data.readByte(Offset.ppemY);
  }

  public int bitDepth() {
    return data.readByte(Offset.bitDepth);
  }

  public int flagsAsInt() {
    return data.readChar(Offset.flags);
  }

  public IndexSubTable indexSubTable(int index) {
    List<IndexSubTable> subTableList = getIndexSubTableList();
    return subTableList.get(index);
  }

  public BitmapGlyphInfo glyphInfo(int glyphId) {
    IndexSubTable subTable = searchIndexSubTables(glyphId);
    if (subTable == null) {
      return null;
    }
    return subTable.glyphInfo(glyphId);
  }

  public int glyphOffset(int glyphId) {
    IndexSubTable subTable = searchIndexSubTables(glyphId);
    if (subTable == null) {
      return -1;
    }
    return subTable.glyphOffset(glyphId);
  }

  public int glyphLength(int glyphId) {
    IndexSubTable subTable = searchIndexSubTables(glyphId);
    if (subTable == null) {
      return -1;
    }
    return subTable.glyphLength(glyphId);
  }

  public int glyphFormat(int glyphId) {
    IndexSubTable subTable = searchIndexSubTables(glyphId);
    if (subTable == null) {
      return -1;
    }
    return subTable.imageFormat();
  }

  private IndexSubTable searchIndexSubTables(int glyphId) {
    // would be faster to binary search but too many size tables don't have
    // sorted subtables
    if (USE_BINARY_SEARCH) {
      return binarySearchIndexSubTables(glyphId);
    }
    return linearSearchIndexSubTables(glyphId);
  }

  private IndexSubTable linearSearchIndexSubTables(int glyphId) {
    for (IndexSubTable subTable : getIndexSubTableList()) {
      if (subTable.firstGlyphIndex() <= glyphId && subTable.lastGlyphIndex() >= glyphId) {
        return subTable;
      }
    }
    return null;
  }

  private IndexSubTable binarySearchIndexSubTables(int glyphId) {
    List<IndexSubTable> subTableList = getIndexSubTableList();
    int index = 0;
    int bottom = 0;
    int top = subTableList.size();
    while (top != bottom) {
      index = (top + bottom) / 2;
      IndexSubTable subTable = subTableList.get(index);
      if (glyphId < subTable.firstGlyphIndex()) {
        // location below current location
        top = index;
      } else {
        if (glyphId <= subTable.lastGlyphIndex()) {
          return subTable;
        }
        // location is above the current location
        bottom = index + 1;
      }
    }
    return null;
  }

  private IndexSubTable createIndexSubTable(int index) {
    return IndexSubTable.createIndexSubTable(masterReadData(), indexSubTableArrayOffset(), index);
  }

  private List<IndexSubTable> getIndexSubTableList() {
    if (indexSubTables == null) {
      synchronized (indexSubTablesLock) {
        if (indexSubTables == null) {
          List<IndexSubTable> subTables = new ArrayList<>(numberOfIndexSubTables());
          for (int i = 0; i < numberOfIndexSubTables(); i++) {
            subTables.add(createIndexSubTable(i));
          }
          this.indexSubTables = subTables;
        }
      }
    }
    return indexSubTables;
  }

  @Override
  public String toString() {
    List<IndexSubTable> subtables = getIndexSubTableList();

    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            "BitmapSizeTable: [s=%#x, e=%#x, ppemx=%d, index subtables count=%d]\n",
            startGlyphIndex(), endGlyphIndex(), ppemX(), numberOfIndexSubTables()));

    for (int i = 0; i < subtables.size(); i++) {
      sb.append(String.format("\t%d: %s\n", i, subtables.get(i)));
    }
    return sb.toString();
  }

  public static final class Builder extends SubTable.Builder<BitmapSizeTable> {
    List<IndexSubTable.Builder<? extends IndexSubTable>> indexSubTables;

    static Builder createBuilder(WritableFontData data, ReadableFontData masterData) {
      return new Builder(data, masterData);
    }

    static Builder createBuilder(ReadableFontData data, ReadableFontData masterData) {
      return new Builder(data, masterData);
    }

    private Builder(WritableFontData data, ReadableFontData masterData) {
      super(data, masterData);
    }

    private Builder(ReadableFontData data, ReadableFontData masterData) {
      super(data, masterData);
    }

    /**
     * Gets the subtable array offset as set in the original table as read from the font file. This
     * value cannot be explicitly set and will be generated during table building.
     *
     * @return the subtable array offset
     */
    public int indexSubTableArrayOffset() {
      return internalReadData().readULongAsInt(Offset.indexSubTableArrayOffset);
    }

    /**
     * Sets the subtable array offset. This is used only during the building process when the
     * objects are being serialized.
     *
     * @param offset the offset to the index subtable array
     */
    void setIndexSubTableArrayOffset(int offset) {
      internalWriteData().writeULong(Offset.indexSubTableArrayOffset, offset);
    }

    /**
     * Gets the subtable array size as set in the original table as read from the font file. This
     * value cannot be explicitly set and will be generated during table building.
     *
     * @return the subtable array size
     */
    public int indexTableSize() {
      return internalReadData().readULongAsInt(Offset.indexTableSize);
    }

    /**
     * Sets the subtable size. This is used only during the building process when the objects are
     * being serialized.
     *
     * @param size the offset to the index subtable array
     */
    void setIndexTableSize(int size) {
      internalWriteData().writeULong(Offset.indexTableSize, size);
    }

    public int numberOfIndexSubTables() {
      return getIndexSubTableBuilders().size();
    }

    private void setNumberOfIndexSubTables(int numberOfIndexSubTables) {
      internalWriteData().writeULong(Offset.numberOfIndexSubTables, numberOfIndexSubTables);
    }

    public int colorRef() {
      return internalReadData().readULongAsInt(Offset.colorRef);
    }

    // TODO(stuartg): implement later
    public void /* SBitLineMetrics */ hori() {
      // NOP
    }

    // TODO(stuartg): implement later
    public void /* SBitLineMetrics */ vert() {
      // NOP
    }

    public int startGlyphIndex() {
      return internalReadData().readUShort(Offset.startGlyphIndex);
    }

    public int endGlyphIndex() {
      return internalReadData().readUShort(Offset.endGlyphIndex);
    }

    public int ppemX() {
      return internalReadData().readByte(Offset.ppemX);
    }

    public int ppemY() {
      return internalReadData().readByte(Offset.ppemY);
    }

    public int bitDepth() {
      return internalReadData().readByte(Offset.bitDepth);
    }

    public int flagsAsInt() {
      return internalReadData().readChar(Offset.flags);
    }

    public IndexSubTable.Builder<? extends IndexSubTable> indexSubTableBuilder(int index) {
      List<IndexSubTable.Builder<? extends IndexSubTable>> subTableList =
          getIndexSubTableBuilders();
      return subTableList.get(index);
    }

    public BitmapGlyphInfo glyphInfo(int glyphId) {
      IndexSubTable.Builder<? extends IndexSubTable> subTable = searchIndexSubTables(glyphId);
      if (subTable == null) {
        return null;
      }
      return subTable.glyphInfo(glyphId);
    }

    public int glyphOffset(int glyphId) {
      IndexSubTable.Builder<? extends IndexSubTable> subTable = searchIndexSubTables(glyphId);
      if (subTable == null) {
        return -1;
      }
      return subTable.glyphOffset(glyphId);
    }

    public int glyphLength(int glyphId) {
      IndexSubTable.Builder<? extends IndexSubTable> subTable = searchIndexSubTables(glyphId);
      if (subTable == null) {
        return -1;
      }
      return subTable.glyphLength(glyphId);
    }

    public int glyphFormat(int glyphId) {
      IndexSubTable.Builder<? extends IndexSubTable> subTable = searchIndexSubTables(glyphId);
      if (subTable == null) {
        return -1;
      }
      return subTable.imageFormat();
    }

    public List<IndexSubTable.Builder<? extends IndexSubTable>> indexSubTableBuilders() {
      return getIndexSubTableBuilders();
    }

    private class BitmapGlyphInfoIterator implements Iterator<BitmapGlyphInfo> {
      Iterator<IndexSubTable.Builder<? extends IndexSubTable>> subTableIter;
      Iterator<BitmapGlyphInfo> subTableGlyphInfoIter;

      public BitmapGlyphInfoIterator() {
        this.subTableIter = getIndexSubTableBuilders().iterator();
      }

      @Override
      public boolean hasNext() {
        if (subTableGlyphInfoIter != null && subTableGlyphInfoIter.hasNext()) {
          return true;
        }
        while (subTableIter.hasNext()) {
          IndexSubTable.Builder<? extends IndexSubTable> indexSubTable = subTableIter.next();
          this.subTableGlyphInfoIter = indexSubTable.iterator();
          if (subTableGlyphInfoIter.hasNext()) {
            return true;
          }
        }
        return false;
      }

      @Override
      public BitmapGlyphInfo next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more characters to iterate.");
        }
        return subTableGlyphInfoIter.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unable to remove a glyph info.");
      }
    }

    Iterator<BitmapGlyphInfo> iterator() {
      return new BitmapGlyphInfoIterator();
    }

    protected void revert() {
      this.indexSubTables = null;
      setModelChanged(false);
    }

    public Map<Integer, BitmapGlyphInfo> generateLocaMap() {
      Map<Integer, BitmapGlyphInfo> locaMap = new HashMap<>();
      Iterator<BitmapGlyphInfo> iter = iterator();
      while (iter.hasNext()) {
        BitmapGlyphInfo info = iter.next();
        locaMap.put(info.glyphId(), info);
      }
      return locaMap;
    }

    private IndexSubTable.Builder<? extends IndexSubTable> searchIndexSubTables(int glyphId) {
      // would be faster to binary search but too many size tables don't have
      // sorted subtables
      if (USE_BINARY_SEARCH) {
        return binarySearchIndexSubTables(glyphId);
      }
      return linearSearchIndexSubTables(glyphId);
    }

    private IndexSubTable.Builder<? extends IndexSubTable> linearSearchIndexSubTables(int glyphId) {
      List<IndexSubTable.Builder<? extends IndexSubTable>> subTableList =
          getIndexSubTableBuilders();
      for (IndexSubTable.Builder<? extends IndexSubTable> subTable : subTableList) {
        if (subTable.firstGlyphIndex() <= glyphId && subTable.lastGlyphIndex() >= glyphId) {
          return subTable;
        }
      }
      return null;
    }

    private IndexSubTable.Builder<? extends IndexSubTable> binarySearchIndexSubTables(int glyphId) {
      List<IndexSubTable.Builder<? extends IndexSubTable>> subTableList =
          getIndexSubTableBuilders();
      int index = 0;
      int bottom = 0;
      int top = subTableList.size();
      while (top != bottom) {
        index = (top + bottom) / 2;
        IndexSubTable.Builder<? extends IndexSubTable> subTable = subTableList.get(index);
        if (glyphId < subTable.firstGlyphIndex()) {
          // location below current location
          top = index;
        } else {
          if (glyphId <= subTable.lastGlyphIndex()) {
            return subTable;
          }
          // location is above the current location
          bottom = index + 1;
        }
      }
      return null;
    }

    private List<IndexSubTable.Builder<? extends IndexSubTable>> getIndexSubTableBuilders() {
      if (indexSubTables == null) {
        initialize(internalReadData());
        setModelChanged();
      }
      return indexSubTables;
    }

    private void initialize(ReadableFontData data) {
      if (indexSubTables == null) {
        this.indexSubTables = new ArrayList<>();
      } else {
        indexSubTables.clear();
      }
      if (data != null) {
        int numberOfIndexSubTables = BitmapSizeTable.numberOfIndexSubTables(data, 0);
        for (int i = 0; i < numberOfIndexSubTables; i++) {
          indexSubTables.add(createIndexSubTableBuilder(i));
        }
      }
    }

    private IndexSubTable.Builder<? extends IndexSubTable> createIndexSubTableBuilder(int index) {
      return IndexSubTable.Builder.createBuilder(
          masterReadData(), indexSubTableArrayOffset(), index);
    }

    @Override
    protected BitmapSizeTable subBuildTable(ReadableFontData data) {
      return new BitmapSizeTable(data, masterReadData());
    }

    @Override
    protected void subDataSet() {
      revert();
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (indexSubTableBuilders() == null) {
        return 0;
      }
      int size = Offset.SIZE;
      boolean variable = false;
      for (IndexSubTable.Builder<? extends IndexSubTable> subTableBuilder : indexSubTables) {
        size += EblcTable.IndexSubTableEntry.SIZE;
        int subTableSize = subTableBuilder.subDataSizeToSerialize();
        int padding = FontMath.paddingRequired(Math.abs(subTableSize), FontData.SizeOf.ULONG);
        variable = subTableSize > 0 ? variable : true;
        size += Math.abs(subTableSize) + padding;
      }
      return variable ? -size : size;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return indexSubTableBuilders() != null;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      setNumberOfIndexSubTables(indexSubTableBuilders().size());
      int size = internalReadData().copyTo(newData);
      return size;
    }
  }
}

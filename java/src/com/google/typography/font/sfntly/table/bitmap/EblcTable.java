/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.typography.font.sfntly.table.bitmap;

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.math.FontMath;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.SubTableContainerTable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** @author Stuart Gill */
public class EblcTable extends SubTableContainerTable {
  private static final boolean DEBUG = false;

  public static final int NOTDEF = -1;

  interface HeaderOffsets {
    int version = 0;
    int numSizes = 4;
    int SIZE = 8;
  }

  interface IndexSubTableEntry {
    int firstGlyphIndex = 0;
    int lastGlyphIndex = 2;
    int additionalOffsetToIndexSubtable = 4;
    int SIZE = 8;
  }

  /** Lock on all operations that will affect the value of the bitmapSizeTable. */
  private final Object bitmapSizeTableLock = new Object();

  private volatile List<BitmapSizeTable> bitmapSizeTable;

  protected EblcTable(Header header, ReadableFontData data) {
    super(header, data);
  }

  public int version() {
    return data.readFixed(HeaderOffsets.version);
  }

  public int numSizes() {
    return data.readULongAsInt(HeaderOffsets.numSizes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append("\nnum sizes = ");
    sb.append(numSizes());
    sb.append("\n");
    for (int i = 0; i < numSizes(); i++) {
      sb.append(i);
      sb.append(": ");
      BitmapSizeTable size = bitmapSizeTable(i);
      sb.append(size.toString());
    }
    return sb.toString();
  }

  public BitmapSizeTable bitmapSizeTable(int index) {
    if (index < 0 || index > numSizes()) {
      throw new IndexOutOfBoundsException("Size table index is outside of the range of tables.");
    }
    List<BitmapSizeTable> bitmapSizeTableList = getBitmapSizeTableList();
    return bitmapSizeTableList.get(index);
  }

  private List<BitmapSizeTable> getBitmapSizeTableList() {
    if (bitmapSizeTable == null) {
      synchronized (bitmapSizeTableLock) {
        if (bitmapSizeTable == null) {
          this.bitmapSizeTable = createBitmapSizeTable(data, numSizes());
        }
      }
    }
    return bitmapSizeTable;
  }

  private static List<BitmapSizeTable> createBitmapSizeTable(ReadableFontData data, int numSizes) {
    List<BitmapSizeTable> bitmapSizeTable = new ArrayList<>();
    for (int i = 0; i < numSizes; i++) {
      BitmapSizeTable.Builder sizeBuilder =
          BitmapSizeTable.Builder.createBuilder(
              data.slice(
                  HeaderOffsets.SIZE + i * BitmapSizeTable.Offset.SIZE,
                  BitmapSizeTable.Offset.SIZE),
              data);
      BitmapSizeTable size = sizeBuilder.build();
      bitmapSizeTable.add(size);
    }
    return Collections.unmodifiableList(bitmapSizeTable);
  }

  public static final class Builder extends SubTableContainerTable.Builder<EblcTable> {
    private final int version = 0x00020000; // TODO(user) constant/enum
    private List<BitmapSizeTable.Builder> sizeTableBuilders;

    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    public static Builder createBuilder(Header header, ReadableFontData data) {
      return new Builder(header, data);
    }

    protected Builder(Header header, WritableFontData data) {
      super(header, data);
    }

    protected Builder(Header header, ReadableFontData data) {
      super(header, data);
    }

    public List<BitmapSizeTable.Builder> bitmapSizeBuilders() {
      return getSizeList();
    }

    protected void revert() {
      this.sizeTableBuilders = null;
      setModelChanged(false);
    }

    /**
     * Generates the loca list for the EBDT table. The list is intended to be used by the EBDT to
     * allow it to parse the glyph data and generate glyph objects. After returning from this method
     * the list belongs to the caller. The list entries are in the same order as the size table
     * builders are at the time of this call.
     *
     * @return the list of loca maps with one for each size table builder
     */
    public List<Map<Integer, BitmapGlyphInfo>> generateLocaList() {
      List<BitmapSizeTable.Builder> sizeBuilderList = getSizeList();

      List<Map<Integer, BitmapGlyphInfo>> locaList = new ArrayList<>(sizeBuilderList.size());
      int sizeIndex = 0;
      for (BitmapSizeTable.Builder sizeBuilder : sizeBuilderList) {
        if (DEBUG) {
          System.out.printf("size table = %d%n", sizeIndex++);
        }
        Map<Integer, BitmapGlyphInfo> locaMap = sizeBuilder.generateLocaMap();
        locaList.add(locaMap);
      }

      return locaList;
    }

    private List<BitmapSizeTable.Builder> getSizeList() {
      if (sizeTableBuilders == null) {
        this.sizeTableBuilders = initialize(internalReadData());
        super.setModelChanged();
      }
      return sizeTableBuilders;
    }

    private List<BitmapSizeTable.Builder> initialize(ReadableFontData data) {
      List<BitmapSizeTable.Builder> sizeBuilders = new ArrayList<>();

      if (data != null) {
        int numSizes = data.readULongAsInt(HeaderOffsets.numSizes);
        for (int i = 0; i < numSizes; i++) {
          ReadableFontData slice =
              data.slice(
                  HeaderOffsets.SIZE + i * BitmapSizeTable.Offset.SIZE,
                  BitmapSizeTable.Offset.SIZE);
          sizeBuilders.add(BitmapSizeTable.Builder.createBuilder(slice, data));
        }
      }
      return sizeBuilders;
    }

    @Override
    protected EblcTable subBuildTable(ReadableFontData data) {
      return new EblcTable(header(), data);
    }

    @Override
    protected void subDataSet() {
      revert();
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (sizeTableBuilders == null) {
        return 0;
      }
      int size = HeaderOffsets.SIZE;
      boolean variable = false;
      int sizeIndex = 0;
      for (BitmapSizeTable.Builder sizeBuilder : sizeTableBuilders) {
        int sizeBuilderSize = sizeBuilder.subDataSizeToSerialize();
        if (DEBUG) {
          System.out.printf(
              "sizeIndex = 0x%x, sizeBuilderSize = 0x%x%n", sizeIndex++, sizeBuilderSize);
        }
        variable = sizeBuilderSize > 0 ? variable : true;
        size += Math.abs(sizeBuilderSize);
      }
      return variable ? -size : size;
    }

    @Override
    protected boolean subReadyToSerialize() {
      if (sizeTableBuilders == null) {
        return false;
      }
      for (BitmapSizeTable.Builder sizeBuilder : sizeTableBuilders) {
        if (!sizeBuilder.subReadyToSerialize()) {
          return false;
        }
      }
      return true;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      // header
      int size = newData.writeFixed(0, version);
      size += newData.writeULong(size, sizeTableBuilders.size());

      // calculate the offsets

      // offset to the start of the size table array
      int sizeTableStartOffset = size;
      // walking offset in the size table array
      int sizeTableOffset = sizeTableStartOffset;

      // offset to the start of the whole index subtable block
      int subTableBlockStartOffset =
          sizeTableOffset + sizeTableBuilders.size() * BitmapSizeTable.Offset.SIZE;
      // walking offset in the index subtable
      // points to the start of the current subtable block
      int currentSubTableBlockStartOffset = subTableBlockStartOffset;

      int sizeIndex = 0;
      for (BitmapSizeTable.Builder sizeBuilder : sizeTableBuilders) {
        sizeBuilder.setIndexSubTableArrayOffset(currentSubTableBlockStartOffset);
        List<IndexSubTable.Builder<? extends IndexSubTable>> indexSubTableBuilderList =
            sizeBuilder.indexSubTableBuilders();

        // walking offset within the current subTable array
        int indexSubTableArrayOffset = currentSubTableBlockStartOffset;
        // walking offset within the subTable entries
        int indexSubTableOffset =
            indexSubTableArrayOffset + indexSubTableBuilderList.size() * HeaderOffsets.SIZE;

        if (DEBUG) {
          System.out.printf(
              "size %d: sizeTable = %x, current subTable Block = %x, index subTable Start = %x%n",
              sizeIndex, sizeTableOffset, currentSubTableBlockStartOffset, indexSubTableOffset);
          sizeIndex++;
        }
        int subTableIndex = 0;

        for (IndexSubTable.Builder<? extends IndexSubTable> indexSubTableBuilder :
            indexSubTableBuilderList) {
          if (DEBUG) {
            System.out.printf(
                "\tsubTableIndex %d: format = %x, ",
                subTableIndex, indexSubTableBuilder.indexFormat());
            System.out.printf(
                "indexSubTableArrayOffset = %x, indexSubTableOffset = %x%n",
                indexSubTableArrayOffset, indexSubTableOffset);
            subTableIndex++;
          }
          // array entry
          indexSubTableArrayOffset +=
              newData.writeUShort(indexSubTableArrayOffset, indexSubTableBuilder.firstGlyphIndex());
          indexSubTableArrayOffset +=
              newData.writeUShort(indexSubTableArrayOffset, indexSubTableBuilder.lastGlyphIndex());
          indexSubTableArrayOffset +=
              newData.writeULong(
                  indexSubTableArrayOffset, indexSubTableOffset - currentSubTableBlockStartOffset);

          // index sub table
          int currentSubTableSize =
              indexSubTableBuilder.subSerialize(newData.slice(indexSubTableOffset));
          int padding = FontMath.paddingRequired(currentSubTableSize, FontData.SizeOf.ULONG);
          if (DEBUG) {
            System.out.printf(
                "\t\tsubTableSize = %x, padding = %x%n", currentSubTableSize, padding);
          }

          indexSubTableOffset += currentSubTableSize;
          indexSubTableOffset += newData.writePadding(indexSubTableOffset, padding);
        }

        // serialize size table
        sizeBuilder.setIndexTableSize(indexSubTableOffset - currentSubTableBlockStartOffset);
        sizeTableOffset += sizeBuilder.subSerialize(newData.slice(sizeTableOffset));

        currentSubTableBlockStartOffset = indexSubTableOffset;
      }
      return size + currentSubTableBlockStartOffset;
    }
  }
}

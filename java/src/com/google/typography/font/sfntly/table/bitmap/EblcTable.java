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

/**
 * @author Stuart Gill
 */
public class EblcTable extends SubTableContainerTable {
  private static final boolean DEBUG = false;

  public static final int NOTDEF = -1;

  private interface HeaderOffsets {
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

  enum Offset {
    // indexSubHeader
    indexSubHeaderLength(8),

    // indexSubTable - all offset relative to the subtable start

    // indexSubTable1
    indexSubTable1_offsetArray(indexSubHeaderLength.offset),
    indexSubTable1_builderDataSize(indexSubHeaderLength.offset),

    // indexSubTable2
    indexSubTable2Length(indexSubHeaderLength.offset + FontData.SizeOf.ULONG
        + BitmapGlyph.Offset.bigGlyphMetricsLength.offset),
    indexSubTable2_imageSize(indexSubHeaderLength.offset),
    indexSubTable2_bigGlyphMetrics(indexSubTable2_imageSize.offset + FontData.SizeOf.ULONG),
    indexSubTable2_builderDataSize(indexSubTable2_bigGlyphMetrics.offset + BigGlyphMetrics.SIZE),

    // indexSubTable3
    indexSubTable3_offsetArray(indexSubHeaderLength.offset),
    indexSubTable3_builderDataSize(indexSubTable3_offsetArray.offset),

    // indexSubTable4
    indexSubTable4_numGlyphs(indexSubHeaderLength.offset),
    indexSubTable4_glyphArray(indexSubTable4_numGlyphs.offset + FontData.SizeOf.ULONG),
    indexSubTable4_codeOffsetPairLength(2 * FontData.SizeOf.USHORT),
    indexSubTable4_codeOffsetPair_glyphCode(0),
    indexSubTable4_codeOffsetPair_offset(FontData.SizeOf.USHORT),
    indexSubTable4_builderDataSize(indexSubTable4_glyphArray.offset),

    // indexSubTable5
    indexSubTable5_imageSize(indexSubHeaderLength.offset),
    indexSubTable5_bigGlyphMetrics(indexSubTable5_imageSize.offset + FontData.SizeOf.ULONG),
    indexSubTable5_numGlyphs(indexSubTable5_bigGlyphMetrics.offset
        + BitmapGlyph.Offset.bigGlyphMetricsLength.offset),
    indexSubTable5_glyphArray(indexSubTable5_numGlyphs.offset + FontData.SizeOf.ULONG),
    indexSubTable5_builderDataSize(indexSubTable5_glyphArray.offset),

    // codeOffsetPair
    codeOffsetPairLength(2 * FontData.SizeOf.USHORT),
    codeOffsetPair_glyphCode(0),
    codeOffsetPair_offset(FontData.SizeOf.USHORT);

    final int offset;

    Offset(int offset) {
      this.offset = offset;
    }
  }

  /**
   * Lock on all operations that will affect the value of the bitmapSizeTable.
   */
  private final Object bitmapSizeTableLock = new Object();
  private volatile List<BitmapSizeTable> bitmapSizeTable;

  /**
   * @param header
   * @param data
   */
  protected EblcTable(Header header, ReadableFontData data) {
    super(header, data);
  }

  public int version() {
    return this.data.readFixed(HeaderOffsets.version);
  }

  public int numSizes() {
    return this.data.readULongAsInt(HeaderOffsets.numSizes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append("\nnum sizes = ");
    sb.append(this.numSizes());
    sb.append("\n");
    for (int i = 0; i < this.numSizes(); i++) {
      sb.append(i);
      sb.append(": ");
      BitmapSizeTable size = this.bitmapSizeTable(i);
      sb.append(size.toString());
    }
    return sb.toString();
  }

  public BitmapSizeTable bitmapSizeTable(int index) {
    if (index < 0 || index > this.numSizes()) {
      throw new IndexOutOfBoundsException("Size table index is outside of the range of tables.");
    }
    List<BitmapSizeTable> bitmapSizeTableList = getBitmapSizeTableList();
    return bitmapSizeTableList.get(index);
  }

  private List<BitmapSizeTable> getBitmapSizeTableList() {
    if (this.bitmapSizeTable == null) {
      synchronized (this.bitmapSizeTableLock) {
        if (this.bitmapSizeTable == null) {
          this.bitmapSizeTable = createBitmapSizeTable(this.data, this.numSizes());
        }
      }
    }
    return this.bitmapSizeTable;
  }

  private static List<BitmapSizeTable> createBitmapSizeTable(ReadableFontData data, int numSizes) {
    List<BitmapSizeTable> bitmapSizeTable = new ArrayList<BitmapSizeTable>();
    for (int i = 0; i < numSizes; i++) {
      BitmapSizeTable.Builder sizeBuilder =
          BitmapSizeTable.Builder.createBuilder(data.slice(
              HeaderOffsets.SIZE + i * BitmapSizeTable.Offset.SIZE,
              BitmapSizeTable.Offset.SIZE), data);
      BitmapSizeTable size = sizeBuilder.build();
      bitmapSizeTable.add(size);
    }
    return Collections.unmodifiableList(bitmapSizeTable);
  }

  public static final class Builder extends SubTableContainerTable.Builder<EblcTable> {
    private final int version = 0x00020000; // TODO(user) constant/enum
    private List<BitmapSizeTable.Builder> sizeTableBuilders;

    /**
     * Create a new builder using the header information and data provided.
     *
     * @param header the header information
     * @param data the data holding the table
     * @return a new builder
     */
    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    /**
     * Create a new builder using the header information and data provided.
     *
     * @param header the header information
     * @param data the data holding the table
     * @return a new builder
     */
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
      return this.getSizeList();
    }

    protected void revert() {
      this.sizeTableBuilders = null;
      this.setModelChanged(false);
    }

    /**
     * Generates the loca list for the EBDT table. The list is intended to be
     * used by the EBDT to allow it to parse the glyph data and generate glyph
     * objects. After returning from this method the list belongs to the caller.
     * The list entries are in the same order as the size table builders are at
     * the time of this call.
     *
     * @return the list of loca maps with one for each size table builder
     */
    public List<Map<Integer, BitmapGlyphInfo>> generateLocaList() {
      List<BitmapSizeTable.Builder> sizeBuilderList = this.getSizeList();

      List<Map<Integer, BitmapGlyphInfo>> locaList =
          new ArrayList<Map<Integer, BitmapGlyphInfo>>(sizeBuilderList.size());
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
      if (this.sizeTableBuilders == null) {
        this.sizeTableBuilders = this.initialize(this.internalReadData());
        super.setModelChanged();
      }
      return this.sizeTableBuilders;
    }

    private List<BitmapSizeTable.Builder> initialize(ReadableFontData data) {
      List<BitmapSizeTable.Builder> sizeBuilders = new ArrayList<BitmapSizeTable.Builder>();

      if (data != null) {
        int numSizes = data.readULongAsInt(HeaderOffsets.numSizes);
        for (int i = 0; i < numSizes; i++) {
          ReadableFontData slice = data.slice(
              HeaderOffsets.SIZE + i * BitmapSizeTable.Offset.SIZE,
              BitmapSizeTable.Offset.SIZE);
          sizeBuilders.add(BitmapSizeTable.Builder.createBuilder(slice, data));
        }
      }
      return sizeBuilders;
    }

    @Override
    protected EblcTable subBuildTable(ReadableFontData data) {
      return new EblcTable(this.header(), data);
    }

    @Override
    protected void subDataSet() {
      this.revert();
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (this.sizeTableBuilders == null) {
        return 0;
      }
      int size = HeaderOffsets.SIZE;
      boolean variable = false;
      int sizeIndex = 0;
      for (BitmapSizeTable.Builder sizeBuilder : this.sizeTableBuilders) {
        int sizeBuilderSize = sizeBuilder.subDataSizeToSerialize();
        if (DEBUG) {
          System.out.printf("sizeIndex = 0x%x, sizeBuilderSize = 0x%x%n", sizeIndex++,
              sizeBuilderSize);
        }
        variable = sizeBuilderSize > 0 ? variable : true;
        size += Math.abs(sizeBuilderSize);
      }
      return variable ? -size : size;
    }

    @Override
    protected boolean subReadyToSerialize() {
      if (this.sizeTableBuilders == null) {
        return false;
      }
      for (BitmapSizeTable.Builder sizeBuilder : this.sizeTableBuilders) {
        if (!sizeBuilder.subReadyToSerialize()) {
          return false;
        }
      }
      return true;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      // header
      int size = newData.writeFixed(0, this.version);
      size += newData.writeULong(size, this.sizeTableBuilders.size());

      // calculate the offsets

      // offset to the start of the size table array
      int sizeTableStartOffset = size;
      // walking offset in the size table array
      int sizeTableOffset = sizeTableStartOffset;

      // offset to the start of the whole index subtable block
      int subTableBlockStartOffset =
          sizeTableOffset + this.sizeTableBuilders.size() * BitmapSizeTable.Offset.SIZE;
      // walking offset in the index subtable
      // points to the start of the current subtable block
      int currentSubTableBlockStartOffset = subTableBlockStartOffset;

      int sizeIndex = 0;
      for (BitmapSizeTable.Builder sizeBuilder : this.sizeTableBuilders) {
        sizeBuilder.setIndexSubTableArrayOffset(currentSubTableBlockStartOffset);
        List<IndexSubTable.Builder<? extends IndexSubTable>> indexSubTableBuilderList =
            sizeBuilder.indexSubTableBuilders();

        // walking offset within the current subTable array
        int indexSubTableArrayOffset = currentSubTableBlockStartOffset;
        // walking offset within the subTable entries
        int indexSubTableOffset = indexSubTableArrayOffset + indexSubTableBuilderList.size()
            * Offset.indexSubHeaderLength.offset;

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
            System.out.printf("\tsubTableIndex %d: format = %x, ", subTableIndex,
                indexSubTableBuilder.indexFormat());
            System.out.printf("indexSubTableArrayOffset = %x, indexSubTableOffset = %x%n",
                indexSubTableArrayOffset, indexSubTableOffset);
            subTableIndex++;
          }
          // array entry
          indexSubTableArrayOffset +=
              newData.writeUShort(indexSubTableArrayOffset, indexSubTableBuilder.firstGlyphIndex());
          indexSubTableArrayOffset +=
              newData.writeUShort(indexSubTableArrayOffset, indexSubTableBuilder.lastGlyphIndex());
          indexSubTableArrayOffset += newData.writeULong(
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

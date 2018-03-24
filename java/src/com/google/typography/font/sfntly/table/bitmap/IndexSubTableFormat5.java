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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Format 5 Index Subtable Entry.
 *
 * @author Stuart Gill
 */
public final class IndexSubTableFormat5 extends IndexSubTable {
  private final int imageSize;

  private interface Offset {
    int imageSize = EblcTable.HeaderOffsets.SIZE;
    int bigGlyphMetrics = imageSize + FontData.SizeOf.ULONG;
    int numGlyphs = bigGlyphMetrics + BitmapGlyph.Offset.bigGlyphMetricsLength;
    int glyphArray = numGlyphs + FontData.SizeOf.ULONG;
    int builderDataSize = glyphArray;
  }

  private IndexSubTableFormat5(ReadableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
    super(data, firstGlyphIndex, lastGlyphIndex);
    this.imageSize = this.data.readULongAsInt(Offset.imageSize);
  }

  private static int numGlyphs(ReadableFontData data, int tableOffset) {
    int numGlyphs = data.readULongAsInt(tableOffset + Offset.numGlyphs);
    return numGlyphs;
  }

  public int imageSize() {
    return data.readULongAsInt(Offset.imageSize);
  }

  public BigGlyphMetrics bigMetrics() {
    return new BigGlyphMetrics(data.slice(Offset.bigGlyphMetrics, BigGlyphMetrics.SIZE));
  }

  @Override
  public int numGlyphs() {
    return numGlyphs(data, 0);
  }

  @Override
  public int glyphStartOffset(int glyphId) {
    checkGlyphRange(glyphId);
    int loca =
        readFontData()
            .searchUShort(Offset.glyphArray, FontData.SizeOf.USHORT, numGlyphs(), glyphId);
    if (loca == -1) {
      return loca;
    }
    return loca * imageSize;
  }

  @Override
  public int glyphLength(int glyphId) {
    checkGlyphRange(glyphId);
    return imageSize;
  }

  public static final class Builder extends IndexSubTable.Builder<IndexSubTableFormat5> {
    private List<Integer> glyphArray;
    private BigGlyphMetrics.Builder metrics;

    public static Builder createBuilder() {
      return new Builder();
    }

    static Builder createBuilder(
        ReadableFontData data, int indexSubTableOffset, int firstGlyphIndex, int lastGlyphIndex) {
      int length = dataLength(data, indexSubTableOffset, firstGlyphIndex, lastGlyphIndex);
      return new Builder(data.slice(indexSubTableOffset, length), firstGlyphIndex, lastGlyphIndex);
    }

    static Builder createBuilder(
        WritableFontData data, int indexSubTableOffset, int firstGlyphIndex, int lastGlyphIndex) {
      int length = dataLength(data, indexSubTableOffset, firstGlyphIndex, lastGlyphIndex);
      return new Builder(data.slice(indexSubTableOffset, length), firstGlyphIndex, lastGlyphIndex);
    }

    private static int dataLength(
        ReadableFontData data, int indexSubTableOffset, int firstGlyphIndex, int lastGlyphIndex) {
      int numGlyphs = IndexSubTableFormat5.numGlyphs(data, indexSubTableOffset);
      return Offset.glyphArray + numGlyphs * FontData.SizeOf.USHORT;
    }

    private Builder() {
      super(Offset.builderDataSize, IndexSubTable.Format.FORMAT_5);
      this.metrics = BigGlyphMetrics.Builder.createBuilder();
    }

    private Builder(WritableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
      super(data, firstGlyphIndex, lastGlyphIndex);
    }

    private Builder(ReadableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
      super(data, firstGlyphIndex, lastGlyphIndex);
    }

    public int imageSize() {
      return internalReadData().readULongAsInt(Offset.imageSize);
    }

    public void setImageSize(int imageSize) {
      internalWriteData().writeULong(Offset.imageSize, imageSize);
    }

    public BigGlyphMetrics.Builder bigMetrics() {
      if (metrics == null) {
        WritableFontData data =
            internalWriteData().slice(Offset.bigGlyphMetrics, BigGlyphMetrics.SIZE);
        this.metrics = new BigGlyphMetrics.Builder(data);
        setModelChanged();
      }
      return metrics;
    }

    @Override
    public int numGlyphs() {
      return getGlyphArray().size();
    }

    @Override
    public int glyphLength(int glyphId) {
      return imageSize();
    }

    @Override
    public int glyphStartOffset(int glyphId) {
      checkGlyphRange(glyphId);
      List<Integer> glyphArray = getGlyphArray();
      int loca = Collections.binarySearch(glyphArray, glyphId);
      if (loca == -1) {
        return -1;
      }
      return loca * imageSize();
    }

    public List<Integer> glyphArray() {
      return getGlyphArray();
    }

    private List<Integer> getGlyphArray() {
      if (glyphArray == null) {
        initialize(super.internalReadData());
        super.setModelChanged();
      }
      return glyphArray;
    }

    private void initialize(ReadableFontData data) {
      if (glyphArray == null) {
        this.glyphArray = new ArrayList<>();
      } else {
        glyphArray.clear();
      }

      if (data != null) {
        int numGlyphs = IndexSubTableFormat5.numGlyphs(data, 0);
        for (int i = 0; i < numGlyphs; i++) {
          glyphArray.add(data.readUShort(Offset.glyphArray + i * FontData.SizeOf.USHORT));
        }
      }
    }

    public void setGlyphArray(List<Integer> array) {
      this.glyphArray = array;
      setModelChanged();
    }

    private class BitmapGlyphInfoIterator implements Iterator<BitmapGlyphInfo> {
      private int offsetIndex;

      public BitmapGlyphInfoIterator() {}

      @Override
      public boolean hasNext() {
        return offsetIndex < getGlyphArray().size();
      }

      @Override
      public BitmapGlyphInfo next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more characters to iterate.");
        }
        BitmapGlyphInfo info =
            new BitmapGlyphInfo(
                getGlyphArray().get(offsetIndex),
                imageDataOffset(),
                offsetIndex * imageSize(),
                imageSize(),
                imageFormat());
        this.offsetIndex++;
        return info;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unable to remove a glyph info.");
      }
    }

    @Override
    Iterator<BitmapGlyphInfo> iterator() {
      return new BitmapGlyphInfoIterator();
    }

    @Override
    protected void revert() {
      super.revert();
      this.glyphArray = null;
    }

    @Override
    protected IndexSubTableFormat5 subBuildTable(ReadableFontData data) {
      return new IndexSubTableFormat5(data, firstGlyphIndex(), lastGlyphIndex());
    }

    @Override
    protected void subDataSet() {
      revert();
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (glyphArray == null) {
        return internalReadData().length();
      }
      return Offset.builderDataSize + glyphArray.size() * FontData.SizeOf.USHORT;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return glyphArray != null;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      int size = super.serializeIndexSubHeader(newData);
      if (!modelChanged()) {
        size += internalReadData().slice(Offset.imageSize).copyTo(newData.slice(Offset.imageSize));
      } else {
        size += newData.writeULong(Offset.imageSize, imageSize());
        size += bigMetrics().subSerialize(newData.slice(size));
        size += newData.writeULong(size, glyphArray.size());
        for (Integer glyphId : glyphArray) {
          size += newData.writeUShort(size, glyphId);
        }
      }
      return size;
    }
  }
}

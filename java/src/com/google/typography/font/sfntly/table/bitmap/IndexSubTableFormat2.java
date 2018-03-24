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
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Format 2 Index Subtable Entry.
 *
 * @author Stuart Gill
 */
public final class IndexSubTableFormat2 extends IndexSubTable {
  private final int imageSize;

  private interface Offset {
    int SIZE =
        EblcTable.HeaderOffsets.SIZE
            + FontData.SizeOf.ULONG
            + BitmapGlyph.Offset.bigGlyphMetricsLength;
    int imageSize = EblcTable.HeaderOffsets.SIZE;
    int bigGlyphMetrics = imageSize + FontData.SizeOf.ULONG;
  }

  private IndexSubTableFormat2(ReadableFontData data, int first, int last) {
    super(data, first, last);
    this.imageSize = this.data.readULongAsInt(Offset.imageSize);
  }

  public int imageSize() {
    return data.readULongAsInt(Offset.imageSize);
  }

  public BigGlyphMetrics bigMetrics() {
    return new BigGlyphMetrics(data.slice(Offset.bigGlyphMetrics, BigGlyphMetrics.SIZE));
  }

  @Override
  public int numGlyphs() {
    return lastGlyphIndex() - firstGlyphIndex() + 1;
  }

  @Override
  public int glyphStartOffset(int glyphId) {
    int loca = checkGlyphRange(glyphId);
    return loca * imageSize;
  }

  @Override
  public int glyphLength(int glyphId) {
    checkGlyphRange(glyphId);
    return imageSize;
  }

  public static final class Builder extends IndexSubTable.Builder<IndexSubTableFormat2> {

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
      return Offset.SIZE;
    }

    private Builder() {
      super(Offset.SIZE, IndexSubTable.Format.FORMAT_2);
      this.metrics = BigGlyphMetrics.Builder.createBuilder();
    }

    private Builder(WritableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
      super(data, firstGlyphIndex, lastGlyphIndex);
    }

    private Builder(ReadableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
      super(data, firstGlyphIndex, lastGlyphIndex);
    }

    @Override
    public int numGlyphs() {
      return lastGlyphIndex() - firstGlyphIndex() + 1;
    }

    @Override
    public int glyphStartOffset(int glyphId) {
      int loca = super.checkGlyphRange(glyphId);
      return loca * imageSize();
    }

    @Override
    public int glyphLength(int glyphId) {
      super.checkGlyphRange(glyphId);
      return imageSize();
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
      }
      return metrics;
    }

    private class BitmapGlyphInfoIterator implements Iterator<BitmapGlyphInfo> {
      private int glyphId;

      public BitmapGlyphInfoIterator() {
        this.glyphId = firstGlyphIndex();
      }

      @Override
      public boolean hasNext() {
        return glyphId <= lastGlyphIndex();
      }

      @Override
      public BitmapGlyphInfo next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more characters to iterate.");
        }
        BitmapGlyphInfo info =
            new BitmapGlyphInfo(
                glyphId,
                imageDataOffset(),
                glyphStartOffset(glyphId),
                glyphLength(glyphId),
                imageFormat());
        this.glyphId++;
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
    protected IndexSubTableFormat2 subBuildTable(ReadableFontData data) {
      return new IndexSubTableFormat2(data, firstGlyphIndex(), lastGlyphIndex());
    }

    @Override
    protected void subDataSet() {
      revert();
    }

    @Override
    protected int subDataSizeToSerialize() {
      return Offset.SIZE;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      int size = super.serializeIndexSubHeader(newData);
      if (metrics == null) {
        size += internalReadData().slice(size).copyTo(newData.slice(size));
      } else {
        size += newData.writeLong(Offset.imageSize, imageSize());
        size += metrics.subSerialize(newData.slice(size));
      }
      return size;
    }
  }
}

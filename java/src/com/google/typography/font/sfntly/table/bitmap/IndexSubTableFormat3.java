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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Format 3 Index Subtable Entry.
 *
 * @author Stuart Gill
 */
public final class IndexSubTableFormat3 extends IndexSubTable {

  private interface Offset {
    int offsetArray = EblcTable.HeaderOffsets.SIZE;
    int builderDataSize = offsetArray;
  }

  private IndexSubTableFormat3(ReadableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
    super(data, firstGlyphIndex, lastGlyphIndex);
  }

  @Override
  public int numGlyphs() {
    return lastGlyphIndex() - firstGlyphIndex() + 1;
  }

  @Override
  public int glyphStartOffset(int glyphId) {
    int loca = checkGlyphRange(glyphId);
    return loca(loca);
  }

  @Override
  public int glyphLength(int glyphId) {
    int loca = checkGlyphRange(glyphId);
    return loca(loca + 1) - loca(loca);
  }

  private int loca(int loca) {
    return data.readUShort(Offset.offsetArray + loca * FontData.SizeOf.USHORT);
  }

  public static final class Builder extends IndexSubTable.Builder<IndexSubTableFormat3> {
    private List<Integer> offsetArray;

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
      return EblcTable.HeaderOffsets.SIZE
          + (lastGlyphIndex - firstGlyphIndex + 1 + 1) * FontData.SizeOf.USHORT;
    }

    private Builder() {
      super(Offset.builderDataSize, IndexSubTable.Format.FORMAT_3);
    }

    private Builder(WritableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
      super(data, firstGlyphIndex, lastGlyphIndex);
    }

    private Builder(ReadableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
      super(data, firstGlyphIndex, lastGlyphIndex);
    }

    @Override
    public int numGlyphs() {
      return getOffsetArray().size() - 1;
    }

    @Override
    public int glyphLength(int glyphId) {
      int loca = checkGlyphRange(glyphId);
      List<Integer> offsetArray = getOffsetArray();
      return offsetArray.get(loca + 1) - offsetArray.get(loca);
    }

    @Override
    public int glyphStartOffset(int glyphId) {
      int loca = checkGlyphRange(glyphId);
      List<Integer> offsetArray = getOffsetArray();
      return offsetArray.get(loca);
    }

    public List<Integer> offsetArray() {
      return getOffsetArray();
    }

    private List<Integer> getOffsetArray() {
      if (offsetArray == null) {
        initialize(super.internalReadData());
        super.setModelChanged();
      }
      return offsetArray;
    }

    private void initialize(ReadableFontData data) {
      if (offsetArray == null) {
        this.offsetArray = new ArrayList<>();
      } else {
        offsetArray.clear();
      }

      if (data != null) {
        int numOffsets = (lastGlyphIndex() - firstGlyphIndex() + 1) + 1;
        for (int i = 0; i < numOffsets; i++) {
          offsetArray.add(data.readUShort(Offset.offsetArray + i * FontData.SizeOf.USHORT));
        }
      }
    }

    public void setOffsetArray(List<Integer> array) {
      this.offsetArray = array;
      setModelChanged();
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
    protected void revert() {
      super.revert();
      this.offsetArray = null;
    }

    @Override
    protected IndexSubTableFormat3 subBuildTable(ReadableFontData data) {
      return new IndexSubTableFormat3(data, firstGlyphIndex(), lastGlyphIndex());
    }

    @Override
    protected void subDataSet() {
      revert();
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (offsetArray == null) {
        return internalReadData().length();
      }
      return EblcTable.HeaderOffsets.SIZE + offsetArray.size() * FontData.SizeOf.ULONG;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return offsetArray != null;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      int size = super.serializeIndexSubHeader(newData);
      if (!modelChanged()) {
        size +=
            internalReadData().slice(Offset.offsetArray).copyTo(newData.slice(Offset.offsetArray));
      } else {

        for (Integer loca : offsetArray) {
          size += newData.writeUShort(size, loca);
        }
      }
      return size;
    }
  }
}

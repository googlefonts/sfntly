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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Format 4 Index Subtable Entry.
 *
 * @author Stuart Gill
 */
public final class IndexSubTableFormat4 extends IndexSubTable {

  private interface Offset {
    int numGlyphs = EblcTable.HeaderOffsets.SIZE;
    int glyphArray = numGlyphs + FontData.SizeOf.ULONG;
    int builderDataSize = glyphArray;
  }

  private interface PairOffset {
    int glyphCode = 0;
    int offset = 2;
    int SIZE = 4;
  }

  private IndexSubTableFormat4(ReadableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
    super(data, firstGlyphIndex, lastGlyphIndex);
  }

  private static int numGlyphs(ReadableFontData data, int tableOffset) {
    int numGlyphs = data.readULongAsInt(tableOffset + Offset.numGlyphs);
    return numGlyphs;
  }

  @Override
  public int numGlyphs() {
    return numGlyphs(data, 0);
  }

  @Override
  public int glyphStartOffset(int glyphId) {
    checkGlyphRange(glyphId);
    int pairIndex = findCodeOffsetPair(glyphId);
    if (pairIndex < 0) {
      return -1;
    }
    return data.readUShort(Offset.glyphArray + pairIndex * PairOffset.SIZE + PairOffset.offset);
  }

  @Override
  public int glyphLength(int glyphId) {
    checkGlyphRange(glyphId);
    int pairIndex = findCodeOffsetPair(glyphId);
    if (pairIndex < 0) {
      return -1;
    }

    int offset = Offset.glyphArray + pairIndex * PairOffset.SIZE + PairOffset.offset;
    return data.readUShort(offset + PairOffset.SIZE) - data.readUShort(offset);
  }

  protected int findCodeOffsetPair(int glyphId) {
    return data.searchUShort(Offset.glyphArray, PairOffset.SIZE, numGlyphs(), glyphId);
  }

  public static class CodeOffsetPair {
    protected int glyphCode;
    protected int offset;

    private CodeOffsetPair(int glyphCode, int offset) {
      this.glyphCode = glyphCode;
      this.offset = offset;
    }

    public int glyphCode() {
      return glyphCode;
    }

    public int offset() {
      return offset;
    }
  }

  public static final class CodeOffsetPairBuilder extends CodeOffsetPair {
    private CodeOffsetPairBuilder(int glyphCode, int offset) {
      super(glyphCode, offset);
    }

    public void setGlyphCode(int glyphCode) {
      this.glyphCode = glyphCode;
    }

    public void setOffset(int offset) {
      this.offset = offset;
    }
  }

  public static final Comparator<CodeOffsetPair> CodeOffsetPairComparatorByGlyphCode =
      Comparator.comparingInt((CodeOffsetPair pair) -> pair.glyphCode).reversed();

  public static final class Builder extends IndexSubTable.Builder<IndexSubTableFormat4> {
    private List<CodeOffsetPairBuilder> offsetPairArray;

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
      int numGlyphs = IndexSubTableFormat4.numGlyphs(data, indexSubTableOffset);
      return Offset.glyphArray + numGlyphs * PairOffset.SIZE;
    }

    private Builder() {
      super(Offset.builderDataSize, IndexSubTable.Format.FORMAT_4);
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
      checkGlyphRange(glyphId);
      int pairIndex = findCodeOffsetPair(glyphId);
      if (pairIndex == -1) {
        return -1;
      }
      return getOffsetArray().get(pairIndex + 1).offset()
          - getOffsetArray().get(pairIndex).offset();
    }

    @Override
    public int glyphStartOffset(int glyphId) {
      checkGlyphRange(glyphId);
      int pairIndex = findCodeOffsetPair(glyphId);
      if (pairIndex == -1) {
        return -1;
      }
      return getOffsetArray().get(pairIndex).offset();
    }

    public List<CodeOffsetPairBuilder> offsetArray() {
      return getOffsetArray();
    }

    private List<CodeOffsetPairBuilder> getOffsetArray() {
      if (offsetPairArray == null) {
        initialize(super.internalReadData());
        super.setModelChanged();
      }
      return offsetPairArray;
    }

    private void initialize(ReadableFontData data) {
      if (offsetPairArray == null) {
        this.offsetPairArray = new ArrayList<>();
      } else {
        offsetPairArray.clear();
      }

      if (data != null) {
        int numPairs = IndexSubTableFormat4.numGlyphs(data, 0) + 1;
        int offset = Offset.glyphArray;
        for (int i = 0; i < numPairs; i++) {
          int glyphCode = data.readUShort(offset + PairOffset.glyphCode);
          int glyphOffset = data.readUShort(offset + PairOffset.offset);
          offset += PairOffset.SIZE;
          CodeOffsetPairBuilder pairBuilder = new CodeOffsetPairBuilder(glyphCode, glyphOffset);
          offsetPairArray.add(pairBuilder);
        }
      }
    }

    private int findCodeOffsetPair(int glyphId) {
      List<CodeOffsetPairBuilder> pairList = getOffsetArray();
      int location = 0;
      int bottom = 0;
      int top = pairList.size();
      while (top != bottom) {
        location = (top + bottom) / 2;
        CodeOffsetPairBuilder pair = pairList.get(location);
        if (glyphId < pair.glyphCode()) {
          // location is below current location
          top = location;
        } else if (glyphId > pair.glyphCode()) {
          // location is above current location
          bottom = location + 1;
        } else {
          return location;
        }
      }
      return -1;
    }

    public void setOffsetArray(List<CodeOffsetPairBuilder> array) {
      this.offsetPairArray = array;
      setModelChanged();
    }

    private class BitmapGlyphInfoIterator implements Iterator<BitmapGlyphInfo> {
      private int codeOffsetPairIndex;

      public BitmapGlyphInfoIterator() {}

      @Override
      public boolean hasNext() {
        return codeOffsetPairIndex < getOffsetArray().size() - 1;
      }

      @Override
      public BitmapGlyphInfo next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more characters to iterate.");
        }
        List<CodeOffsetPairBuilder> offsetArray = getOffsetArray();
        CodeOffsetPair pair = offsetArray.get(codeOffsetPairIndex);
        BitmapGlyphInfo info =
            new BitmapGlyphInfo(
                pair.glyphCode(),
                imageDataOffset(),
                pair.offset(),
                offsetArray.get(codeOffsetPairIndex + 1).offset() - pair.offset(),
                imageFormat());
        this.codeOffsetPairIndex++;
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
      this.offsetPairArray = null;
    }

    @Override
    protected IndexSubTableFormat4 subBuildTable(ReadableFontData data) {
      return new IndexSubTableFormat4(data, firstGlyphIndex(), lastGlyphIndex());
    }

    @Override
    protected void subDataSet() {
      revert();
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (offsetPairArray == null) {
        return internalReadData().length();
      }
      return EblcTable.HeaderOffsets.SIZE
          + FontData.SizeOf.ULONG
          + offsetPairArray.size() * PairOffset.SIZE;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return offsetPairArray != null;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      int size = super.serializeIndexSubHeader(newData);
      if (!modelChanged()) {
        size += internalReadData().slice(Offset.numGlyphs).copyTo(newData.slice(Offset.numGlyphs));
      } else {

        size += newData.writeLong(size, offsetPairArray.size() - 1);
        for (CodeOffsetPair pair : offsetPairArray) {
          size += newData.writeUShort(size, pair.glyphCode());
          size += newData.writeUShort(size, pair.offset());
        }
      }
      return size;
    }
  }
}

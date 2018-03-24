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

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import java.util.Iterator;

public abstract class IndexSubTable extends SubTable {
  private static final boolean DEBUG = false;

  public interface Format {
    int FORMAT_1 = 1;
    int FORMAT_2 = 2;
    int FORMAT_3 = 3;
    int FORMAT_4 = 4;
    int FORMAT_5 = 5;
  }

  private interface SubOffset {
    int indexFormat = 0;
    int imageFormat = 2;
    int imageDataOffset = 4;
  }

  private final int firstGlyphIndex;
  private final int lastGlyphIndex;
  private final int indexFormat;
  private final int imageFormat;
  private final int imageDataOffset;

  protected static IndexSubTable createIndexSubTable(
      ReadableFontData data, int offsetToIndexSubTableArray, int arrayIndex) {

    Builder<? extends IndexSubTable> builder =
        Builder.createBuilder(data, offsetToIndexSubTableArray, arrayIndex);
    if (builder == null) {
      return null;
    }
    return builder.build();
  }

  protected IndexSubTable(ReadableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
    super(data);
    this.firstGlyphIndex = firstGlyphIndex;
    this.lastGlyphIndex = lastGlyphIndex;
    this.indexFormat = this.data.readUShort(SubOffset.indexFormat);
    this.imageFormat = this.data.readUShort(SubOffset.imageFormat);
    this.imageDataOffset = this.data.readULongAsInt(SubOffset.imageDataOffset);
  }

  public int indexFormat() {
    return indexFormat;
  }

  public int firstGlyphIndex() {
    return firstGlyphIndex;
  }

  public int lastGlyphIndex() {
    return lastGlyphIndex;
  }

  public int imageFormat() {
    return imageFormat;
  }

  public int imageDataOffset() {
    return imageDataOffset;
  }

  public BitmapGlyphInfo glyphInfo(int glyphId) {
    int loca = checkGlyphRange(glyphId);
    if (loca == -1) {
      return null;
    }
    if (glyphStartOffset(glyphId) == -1) {
      return null;
    }

    return new BitmapGlyphInfo(
        glyphId, imageDataOffset(), glyphStartOffset(glyphId), glyphLength(glyphId), imageFormat());
  }

  public final int glyphOffset(int glyphId) {
    int glyphStartOffset = glyphStartOffset(glyphId);
    if (glyphStartOffset == -1) {
      return -1;
    }
    return imageDataOffset() + glyphStartOffset;
  }

  /** Gets the offset of the glyph relative to the block for this index subtable. */
  public abstract int glyphStartOffset(int glyphId);

  public abstract int glyphLength(int glyphId);

  public abstract int numGlyphs();

  protected static int checkGlyphRange(int glyphId, int firstGlyphId, int lastGlyphId) {
    if (glyphId < firstGlyphId || glyphId > lastGlyphId) {
      throw new IndexOutOfBoundsException("Glyph ID is outside of the allowed range.");
    }
    return glyphId - firstGlyphId;
  }

  protected int checkGlyphRange(int glyphId) {
    return checkGlyphRange(glyphId, firstGlyphIndex(), lastGlyphIndex());
  }

  @Override
  public String toString() {
    String s =
        String.format(
            "IndexSubTable: [%#x : %#x], format = %d, image format = %s, imageOff = %#x%n",
            firstGlyphIndex(), lastGlyphIndex(), indexFormat, imageFormat(), imageDataOffset());

    if (DEBUG) {
      for (int g = firstGlyphIndex(); g < lastGlyphIndex(); g++) {
        s += String.format("\tgid = %d, offset = %d%n", g, glyphStartOffset(g));
      }
    }
    return s;
  }

  public abstract static class Builder<T extends IndexSubTable> extends SubTable.Builder<T> {
    private int firstGlyphIndex;
    private int lastGlyphIndex;
    private int indexFormat;
    private int imageFormat;
    private int imageDataOffset;

    public static Builder<? extends IndexSubTable> createBuilder(int indexFormat) {
      switch (indexFormat) {
        case 1:
          return IndexSubTableFormat1.Builder.createBuilder();
        case 2:
          return IndexSubTableFormat2.Builder.createBuilder();
        case 3:
          return IndexSubTableFormat3.Builder.createBuilder();
        case 4:
          return IndexSubTableFormat4.Builder.createBuilder();
        case 5:
          return IndexSubTableFormat5.Builder.createBuilder();
        default:
          throw new IllegalArgumentException(
              String.format("Invalid index subtable format %d", indexFormat));
      }
    }

    static Builder<? extends IndexSubTable> createBuilder(
        ReadableFontData data, int offsetToIndexSubTableArray, int arrayIndex) {

      int entryOffset = offsetToIndexSubTableArray + arrayIndex * EblcTable.IndexSubTableEntry.SIZE;

      int firstGlyphIndex =
          data.readUShort(entryOffset + EblcTable.IndexSubTableEntry.firstGlyphIndex);
      int lastGlyphIndex =
          data.readUShort(entryOffset + EblcTable.IndexSubTableEntry.lastGlyphIndex);
      int additionOffsetToIndexSubtable =
          data.readULongAsInt(
              entryOffset + EblcTable.IndexSubTableEntry.additionalOffsetToIndexSubtable);

      int indexSubTableOffset = offsetToIndexSubTableArray + additionOffsetToIndexSubtable;

      int indexFormat = data.readUShort(indexSubTableOffset);
      switch (indexFormat) {
        case 1:
          return IndexSubTableFormat1.Builder.createBuilder(
              data, indexSubTableOffset, firstGlyphIndex, lastGlyphIndex);
        case 2:
          return IndexSubTableFormat2.Builder.createBuilder(
              data, indexSubTableOffset, firstGlyphIndex, lastGlyphIndex);
        case 3:
          return IndexSubTableFormat3.Builder.createBuilder(
              data, indexSubTableOffset, firstGlyphIndex, lastGlyphIndex);
        case 4:
          return IndexSubTableFormat4.Builder.createBuilder(
              data, indexSubTableOffset, firstGlyphIndex, lastGlyphIndex);
        case 5:
          return IndexSubTableFormat5.Builder.createBuilder(
              data, indexSubTableOffset, firstGlyphIndex, lastGlyphIndex);
        default:
          throw new IllegalArgumentException(
              String.format("Invalid index subtable format %d", indexFormat));
      }
    }

    protected Builder(int dataSize, int indexFormat) {
      super(dataSize);
      this.indexFormat = indexFormat;
    }

    protected Builder(int indexFormat, int imageFormat, int imageDataOffset, int dataSize) {
      this(dataSize, indexFormat);
      this.imageFormat = imageFormat;
      this.imageDataOffset = imageDataOffset;
    }

    protected Builder(WritableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
      super(data);
      this.firstGlyphIndex = firstGlyphIndex;
      this.lastGlyphIndex = lastGlyphIndex;
      initialize(data);
    }

    protected Builder(ReadableFontData data, int firstGlyphIndex, int lastGlyphIndex) {
      super(data);
      this.firstGlyphIndex = firstGlyphIndex;
      this.lastGlyphIndex = lastGlyphIndex;
      initialize(data);
    }

    private void initialize(ReadableFontData data) {
      this.indexFormat = data.readUShort(SubOffset.indexFormat);
      this.imageFormat = data.readUShort(SubOffset.imageFormat);
      this.imageDataOffset = data.readULongAsInt(SubOffset.imageDataOffset);
    }

    /**
     * Unable to fully revert unless some changes happen to hold the original data. Until then keep
     * as protected.
     */
    protected void revert() {
      setModelChanged(false);
      initialize(internalReadData());
    }

    public int indexFormat() {
      return indexFormat;
    }

    public int firstGlyphIndex() {
      return firstGlyphIndex;
    }

    public void setFirstGlyphIndex(int firstGlyphIndex) {
      this.firstGlyphIndex = firstGlyphIndex;
    }

    public int lastGlyphIndex() {
      return lastGlyphIndex;
    }

    public void setLastGlyphIndex(int lastGlyphIndex) {
      this.lastGlyphIndex = lastGlyphIndex;
    }

    public int imageFormat() {
      return imageFormat;
    }

    public void setImageFormat(int imageFormat) {
      this.imageFormat = imageFormat;
    }

    public int imageDataOffset() {
      return imageDataOffset;
    }

    public void setImageDataOffset(int offset) {
      this.imageDataOffset = offset;
    }

    public abstract int numGlyphs();

    /** Gets the glyph info for the specified glyph id. */
    // TODO(stuartg): could be optimized by pushing down into subclasses
    public BitmapGlyphInfo glyphInfo(int glyphId) {
      return new BitmapGlyphInfo(
          glyphId,
          imageDataOffset(),
          glyphStartOffset(glyphId),
          glyphLength(glyphId),
          imageFormat());
    }

    /** Gets the full offset of the glyph within the EBDT table. */
    public final int glyphOffset(int glyphId) {
      return imageDataOffset() + glyphStartOffset(glyphId);
    }

    /** Gets the offset of the glyph relative to the block for this index subtable. */
    public abstract int glyphStartOffset(int glyphId);

    /** Gets the length of the glyph within the EBDT table. */
    public abstract int glyphLength(int glyphId);

    /**
     * Checks that the glyph id is within the correct range. If it returns the offset of the glyph
     * id from the start of the range.
     *
     * @return the offset of the glyphId from the start of the glyph range
     * @throws IndexOutOfBoundsException if the glyph id is not within the correct range
     */
    protected int checkGlyphRange(int glyphId) {
      return IndexSubTable.checkGlyphRange(glyphId, firstGlyphIndex(), lastGlyphIndex());
    }

    protected int serializeIndexSubHeader(WritableFontData data) {
      int size = data.writeUShort(SubOffset.indexFormat, indexFormat);
      size += data.writeUShort(SubOffset.imageFormat, imageFormat);
      size += data.writeULong(SubOffset.imageDataOffset, imageDataOffset);
      return size;
    }

    abstract Iterator<BitmapGlyphInfo> iterator();

    /*
     * The following methods will never be called but they need to be here to
     * allow the BitmapSizeTable to see these methods through an abstract
     * reference.
     */
    @Override
    protected T subBuildTable(ReadableFontData data) {
      return null;
    }

    @Override
    protected void subDataSet() {
      // NOP
    }

    @Override
    protected int subDataSizeToSerialize() {
      return 0;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return false;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      return 0;
    }

    @Override
    public String toString() {
      String s =
          String.format(
              "IndexSubTable: [%#x : %#x], format = %d, image format = %s, imageOff = %#x%n",
              firstGlyphIndex(), lastGlyphIndex(), indexFormat, imageFormat(), imageDataOffset());

      if (DEBUG) {
        for (int g = firstGlyphIndex(); g < lastGlyphIndex(); g++) {
          s += String.format("\tgid = %d, offset = %d%n", g, glyphStartOffset(g));
        }
      }
      return s;
    }
  }
}

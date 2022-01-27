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

import com.google.typography.font.sfntly.data.SfObjects;
import java.util.Comparator;

/**
 * An immutable class holding bitmap glyph information.
 *
 * @author Stuart Gill
 */
public final class BitmapGlyphInfo {
  private final int glyphId;
  private final boolean relative;
  private final int blockOffset;
  private final int startOffset;
  private final int length;
  private final int format;

  /**
   * Constructor for a relative located glyph. The glyph's position in the EBDT table is a
   * combination of it's block offset and it's own start offset.
   *
   * @param blockOffset the offset of the block to which the glyph belongs
   * @param startOffset the offset of the glyph within the block
   * @param length the byte length
   * @param format the glyph image format
   */
  public BitmapGlyphInfo(int glyphId, int blockOffset, int startOffset, int length, int format) {
    this.glyphId = glyphId;
    this.relative = true;
    this.blockOffset = blockOffset;
    this.startOffset = startOffset;
    this.length = length;
    this.format = format;
  }

  /**
   * Constructor for an absolute located glyph. The glyph's position in the EBDT table is only given
   * by its own start offset.
   *
   * @param startOffset the offset of the glyph within the block
   * @param length the byte length
   * @param format the glyph image format
   */
  public BitmapGlyphInfo(int glyphId, int startOffset, int length, int format) {
    this.glyphId = glyphId;
    this.relative = false;
    this.blockOffset = 0;
    this.startOffset = startOffset;
    this.length = length;
    this.format = format;
  }

  public int glyphId() {
    return glyphId;
  }

  public boolean relative() {
    return relative;
  }

  public int blockOffset() {
    return blockOffset;
  }

  public int offset() {
    return blockOffset() + startOffset();
  }

  public int startOffset() {
    return startOffset;
  }

  public int length() {
    return length;
  }

  public int format() {
    return format;
  }

  @Override
  public int hashCode() {
    return SfObjects.hash(blockOffset, format, glyphId, length, startOffset);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BitmapGlyphInfo)) {
      return false;
    }
    BitmapGlyphInfo other = (BitmapGlyphInfo) obj;
    return format == other.format
        && glyphId == other.glyphId
        && length == other.length
        && offset() == other.offset();
  }

  public static final Comparator<BitmapGlyphInfo> StartOffsetComparator =
      Comparator.comparingInt((BitmapGlyphInfo info) -> info.startOffset).reversed();
}

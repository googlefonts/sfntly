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
import com.google.typography.font.sfntly.table.SubTable;

/** @author Stuart Gill */
public abstract class BitmapGlyph extends SubTable {

  protected interface Offset {
    // header
    int version = 0;

    int smallGlyphMetricsLength = 5;
    int bigGlyphMetricsLength = 8;

    // format 1
    int glyphFormat1_imageData = smallGlyphMetricsLength;

    // format 2
    int glyphFormat2_imageData = smallGlyphMetricsLength;

    // format 3

    // format 4

    // format 5
    int glyphFormat5_imageData = 0;

    // format 6
    int glyphFormat6_imageData = bigGlyphMetricsLength;

    // format 7
    int glyphFormat7_imageData = bigGlyphMetricsLength;

    // format 8
    int glyphFormat8_numComponents = smallGlyphMetricsLength + 1;
    int glyphFormat8_componentArray = glyphFormat8_numComponents + FontData.SizeOf.USHORT;

    // format 9
    int glyphFormat9_numComponents = bigGlyphMetricsLength;
    int glyphFormat9_componentArray = glyphFormat9_numComponents + FontData.SizeOf.USHORT;

    // ebdtComponent
    int ebdtComponentLength = FontData.SizeOf.USHORT + 2 * FontData.SizeOf.CHAR;
    int ebdtComponent_glyphCode = 0;
    int ebdtComponent_xOffset = 2;
    int ebdtComponent_yOffset = 3;
  }

  private final int format;

  public static BitmapGlyph createGlyph(ReadableFontData data, int format) {
    BitmapGlyph glyph = null;
    BitmapGlyph.Builder<? extends BitmapGlyph> builder = Builder.createGlyphBuilder(data, format);
    if (builder != null) {
      glyph = builder.build();
    }
    return glyph;
  }

  protected BitmapGlyph(ReadableFontData data, int format) {
    super(data);
    this.format = format;
  }

  protected BitmapGlyph(ReadableFontData data, int offset, int length, int format) {
    super(data, offset, length);
    this.format = format;
  }

  public int format() {
    return format;
  }

  public abstract static class Builder<T extends BitmapGlyph> extends SubTable.Builder<T> {

    private final int format;

    public static Builder<? extends BitmapGlyph> createGlyphBuilder(
        ReadableFontData data, int format) {
      switch (format) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
          return new SimpleBitmapGlyph.Builder(data, format);
        case 8:
        case 9:
          return new CompositeBitmapGlyph.Builder(data, format);
      }
      return null;
    }

    protected Builder(WritableFontData data, int format) {
      super(data);
      this.format = format;
    }

    protected Builder(ReadableFontData data, int format) {
      super(data);
      this.format = format;
    }

    public int format() {
      return format;
    }

    @Override
    protected void subDataSet() {
      // NOP
    }

    @Override
    protected int subDataSizeToSerialize() {
      return internalReadData().length();
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      return internalReadData().copyTo(newData);
    }
  }

  @Override
  public String toString() {
    return "BitmapGlyph [format=" + format + ", data = " + super.toString() + "]";
  }
}

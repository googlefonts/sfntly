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

/**
 * @author Stuart Gill
 */
public class BigGlyphMetrics extends GlyphMetrics {

  public static final int SIZE = 8;

  private interface Offset {
    int height = 0;
    int width = 1;
    int horiBearingX = 2;
    int horiBearingY = 3;
    int horiAdvance = 4;
    int vertBearingX = 5;
    int vertBearingY = 6;
    int vertAdvance = 7;
  }

  /**
   * @param data
   */
  BigGlyphMetrics(ReadableFontData data) {
    super(data);
  }

  public int height() {
    return this.data.readByte(Offset.height);
  }

  public int width() {
    return this.data.readByte(Offset.width);
  }

  public int horiBearingX() {
    return this.data.readChar(Offset.horiBearingX);
  }

  public int horiBearingY() {
    return this.data.readChar(Offset.horiBearingY);
  }

  public int horiAdvance() {
    return this.data.readByte(Offset.horiAdvance);
  }

  public int vertBearingX() {
    return this.data.readChar(Offset.vertBearingX);
  }

  public int vertBearingY() {
    return this.data.readChar(Offset.vertBearingY);
  }

  public int vertAdvance() {
    return this.data.readByte(Offset.vertAdvance);
  }

  public static class Builder extends GlyphMetrics.Builder<BigGlyphMetrics> {

    public static Builder createBuilder() {
      WritableFontData data = WritableFontData.createWritableFontData(SIZE);
      return new Builder(data);
    }

    /**
     * Constructor.
     *
     * @param data
     */
    protected Builder(WritableFontData data) {
      super(data);
    }

    /**
     * Constructor.
     *
     * @param data
     */
    protected Builder(ReadableFontData data) {
      super(data);
    }

    public int height() {
      return this.internalReadData().readByte(Offset.height);
    }

    public void setHeight(byte height) {
      this.internalWriteData().writeByte(Offset.height, height);
    }

    public int width() {
      return this.internalReadData().readByte(Offset.width);
    }

    public void setWidth(byte width) {
      this.internalWriteData().writeByte(Offset.width, width);
    }

    public int horiBearingX() {
      return this.internalReadData().readChar(Offset.horiBearingX);
    }

    public void setHoriBearingX(byte bearing) {
      this.internalWriteData().writeChar(Offset.horiBearingX, bearing);
    }

    public int horiBearingY() {
      return this.internalReadData().readChar(Offset.horiBearingY);
    }

    public void setHoriBearingY(byte bearing) {
      this.internalWriteData().writeChar(Offset.horiBearingY, bearing);
    }

    public int horiAdvance() {
      return this.internalReadData().readByte(Offset.horiAdvance);
    }

    public void setHoriAdvance(byte advance) {
      this.internalWriteData().writeByte(Offset.horiAdvance, advance);
    }

    public int vertBearingX() {
      return this.internalReadData().readChar(Offset.vertBearingX);
    }

    public void setVertBearingX(byte bearing) {
      this.internalWriteData().writeChar(Offset.vertBearingX, bearing);
    }

    public int vertBearingY() {
      return this.internalReadData().readChar(Offset.vertBearingY);
    }

    public void setVertBearingY(byte bearing) {
      this.internalWriteData().writeChar(Offset.vertBearingY, bearing);
    }

    public int vertAdvance() {
      return this.internalReadData().readByte(Offset.vertAdvance);
    }

    public void setVertAdvance(byte advance) {
      this.internalWriteData().writeByte(Offset.vertAdvance, advance);
    }

    @Override
    protected BigGlyphMetrics subBuildTable(ReadableFontData data) {
      return new BigGlyphMetrics(data);
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
      return this.data().copyTo(newData);
    }
  }
}

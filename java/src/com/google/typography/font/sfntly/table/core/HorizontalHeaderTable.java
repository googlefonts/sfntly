/*
 * Copyright 2010 Google Inc. All Rights Reserved.
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

package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.TableBasedTableBuilder;


/**
 * A Horizontal Header table - 'hhea'.
 *
 * @author Stuart Gill
 * @see "ISO/IEC 14496-22:2015, section 5.2.3"
 */
public final class HorizontalHeaderTable extends Table {

  private interface Offset {
    int version = 0;
    int Ascender = 4;
    int Descender = 6;
    int LineGap = 8;
    int advanceWidthMax = 10;
    int minLeftSideBearing = 12;
    int minRightSideBearing = 14;
    int xMaxExtent = 16;
    int caretSlopeRise = 18;
    int caretSlopeRun = 20;
    int caretOffset = 22;
    int reserved24 = 24;
    int reserved26 = 26;
    int reserved28 = 28;
    int reserved30 = 30;
    int metricDataFormat = 32;
    int numberOfHMetrics = 34;
  }

  private HorizontalHeaderTable(Header header, ReadableFontData data) {
    super(header, data);
  }

  public int tableVersion() {
    return this.data.readFixed(Offset.version);
  }

  public int ascender() {
    return this.data.readShort(Offset.Ascender);
  }

  public int descender() {
    return this.data.readShort(Offset.Descender);
  }

  public int lineGap() {
    return this.data.readShort(Offset.LineGap);
  }

  public int advanceWidthMax() {
    return this.data.readUShort(Offset.advanceWidthMax);
  }

  public int minLeftSideBearing() {
    return this.data.readShort(Offset.minLeftSideBearing);
  }

  public int minRightSideBearing() {
    return this.data.readShort(Offset.minRightSideBearing);
  }

  public int xMaxExtent() {
    return this.data.readShort(Offset.xMaxExtent);
  }

  public int caretSlopeRise() {
    return this.data.readShort(Offset.caretSlopeRise);
  }

  public int caretSlopeRun() {
    return this.data.readShort(Offset.caretSlopeRun);
  }

  public int caretOffset() {
    return this.data.readShort(Offset.caretOffset);
  }

  // TODO(stuartg): an enum?
  public int metricDataFormat() {
    return this.data.readShort(Offset.metricDataFormat);
  }

  public int numberOfHMetrics() {
    return this.data.readUShort(Offset.numberOfHMetrics);
  }

  /**
   * Builder for a Horizontal Header table - 'hhea'.
   *
   */
  public static class Builder extends TableBasedTableBuilder<HorizontalHeaderTable> {

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

    protected Builder(Header header, WritableFontData data) {
      super(header, data);
    }

    protected Builder(Header header, ReadableFontData data) {
      super(header, data);
    }

    @Override
    protected HorizontalHeaderTable subBuildTable(ReadableFontData data) {
      return new HorizontalHeaderTable(this.header(), data);
    }

    public int tableVersion() {
      return this.internalReadData().readFixed(Offset.version);
    }

    public void setTableVersion(int version) {
      this.internalWriteData().writeFixed(Offset.version, version);
    }

    public int ascender() {
      return this.internalReadData().readShort(Offset.Ascender);
    }

    public void setAscender(int version) {
      this.internalWriteData().writeShort(Offset.Ascender, version);
    }

    public int descender() {
      return this.internalReadData().readShort(Offset.Descender);
    }

    public void setDescender(int version) {
      this.internalWriteData().writeShort(Offset.Descender, version);
    }

    public int lineGap() {
      return this.internalReadData().readShort(Offset.LineGap);
    }

    public void setLineGap(int version) {
      this.internalWriteData().writeShort(Offset.LineGap, version);
    }

    public int advanceWidthMax() {
      return this.internalReadData().readUShort(Offset.advanceWidthMax);
    }

    public void setAdvanceWidthMax(int version) {
      this.internalWriteData().writeUShort(Offset.advanceWidthMax, version);
    }

    public int minLeftSideBearing() {
      return this.internalReadData().readShort(Offset.minLeftSideBearing);
    }

    public void setMinLeftSideBearing(int version) {
      this.internalWriteData().writeShort(Offset.minLeftSideBearing, version);
    }

    public int minRightSideBearing() {
      return this.internalReadData().readShort(Offset.minRightSideBearing);
    }

    public void setMinRightSideBearing(int version) {
      this.internalWriteData().writeShort(Offset.minRightSideBearing, version);
    }

    public int xMaxExtent() {
      return this.internalReadData().readShort(Offset.xMaxExtent);
    }

    public void setXMaxExtent(int version) {
      this.internalWriteData().writeShort(Offset.xMaxExtent, version);
    }

    public int caretSlopeRise() {
      return this.internalReadData().readUShort(Offset.caretSlopeRise);
    }

    public void setCaretSlopeRise(int version) {
      this.internalWriteData().writeUShort(Offset.caretSlopeRise, version);
    }

    public int caretSlopeRun() {
      return this.internalReadData().readUShort(Offset.caretSlopeRun);
    }

    public void setCaretSlopeRun(int version) {
      this.internalWriteData().writeUShort(Offset.caretSlopeRun, version);
    }

    public int caretOffset() {
      return this.internalReadData().readUShort(Offset.caretOffset);
    }

    public void setCaretOffset(int version) {
      this.internalWriteData().writeUShort(Offset.caretOffset, version);
    }

    // TODO(stuartg): an enum?
    public int metricDataFormat() {
      return this.internalReadData().readUShort(Offset.metricDataFormat);
    }

    public void setMetricDataFormat(int version) {
      this.internalWriteData().writeUShort(Offset.metricDataFormat, version);
    }

    public int numberOfHMetrics() {
      return this.internalReadData().readUShort(Offset.numberOfHMetrics);
    }

    public void setNumberOfHMetrics(int version) {
      this.internalWriteData().writeUShort(Offset.numberOfHMetrics, version);
    }
  }
}

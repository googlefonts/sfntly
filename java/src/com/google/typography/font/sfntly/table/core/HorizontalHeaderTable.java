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
    return data.readFixed(Offset.version);
  }

  public int ascender() {
    return data.readShort(Offset.Ascender);
  }

  public int descender() {
    return data.readShort(Offset.Descender);
  }

  public int lineGap() {
    return data.readShort(Offset.LineGap);
  }

  public int advanceWidthMax() {
    return data.readUShort(Offset.advanceWidthMax);
  }

  public int minLeftSideBearing() {
    return data.readShort(Offset.minLeftSideBearing);
  }

  public int minRightSideBearing() {
    return data.readShort(Offset.minRightSideBearing);
  }

  public int xMaxExtent() {
    return data.readShort(Offset.xMaxExtent);
  }

  public int caretSlopeRise() {
    return data.readShort(Offset.caretSlopeRise);
  }

  public int caretSlopeRun() {
    return data.readShort(Offset.caretSlopeRun);
  }

  public int caretOffset() {
    return data.readShort(Offset.caretOffset);
  }

  // TODO(stuartg): an enum?
  public int metricDataFormat() {
    return data.readShort(Offset.metricDataFormat);
  }

  public int numberOfHMetrics() {
    return data.readUShort(Offset.numberOfHMetrics);
  }

  public static class Builder extends TableBasedTableBuilder<HorizontalHeaderTable> {

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
      return new HorizontalHeaderTable(header(), data);
    }

    public int tableVersion() {
      return internalReadData().readFixed(Offset.version);
    }

    public void setTableVersion(int version) {
      internalWriteData().writeFixed(Offset.version, version);
    }

    public int ascender() {
      return internalReadData().readShort(Offset.Ascender);
    }

    public void setAscender(int version) {
      internalWriteData().writeShort(Offset.Ascender, version);
    }

    public int descender() {
      return internalReadData().readShort(Offset.Descender);
    }

    public void setDescender(int version) {
      internalWriteData().writeShort(Offset.Descender, version);
    }

    public int lineGap() {
      return internalReadData().readShort(Offset.LineGap);
    }

    public void setLineGap(int version) {
      internalWriteData().writeShort(Offset.LineGap, version);
    }

    public int advanceWidthMax() {
      return internalReadData().readUShort(Offset.advanceWidthMax);
    }

    public void setAdvanceWidthMax(int version) {
      internalWriteData().writeUShort(Offset.advanceWidthMax, version);
    }

    public int minLeftSideBearing() {
      return internalReadData().readShort(Offset.minLeftSideBearing);
    }

    public void setMinLeftSideBearing(int version) {
      internalWriteData().writeShort(Offset.minLeftSideBearing, version);
    }

    public int minRightSideBearing() {
      return internalReadData().readShort(Offset.minRightSideBearing);
    }

    public void setMinRightSideBearing(int version) {
      internalWriteData().writeShort(Offset.minRightSideBearing, version);
    }

    public int xMaxExtent() {
      return internalReadData().readShort(Offset.xMaxExtent);
    }

    public void setXMaxExtent(int version) {
      internalWriteData().writeShort(Offset.xMaxExtent, version);
    }

    public int caretSlopeRise() {
      return internalReadData().readUShort(Offset.caretSlopeRise);
    }

    public void setCaretSlopeRise(int version) {
      internalWriteData().writeUShort(Offset.caretSlopeRise, version);
    }

    public int caretSlopeRun() {
      return internalReadData().readUShort(Offset.caretSlopeRun);
    }

    public void setCaretSlopeRun(int version) {
      internalWriteData().writeUShort(Offset.caretSlopeRun, version);
    }

    public int caretOffset() {
      return internalReadData().readUShort(Offset.caretOffset);
    }

    public void setCaretOffset(int version) {
      internalWriteData().writeUShort(Offset.caretOffset, version);
    }

    // TODO(stuartg): an enum?
    public int metricDataFormat() {
      return internalReadData().readUShort(Offset.metricDataFormat);
    }

    public void setMetricDataFormat(int version) {
      internalWriteData().writeUShort(Offset.metricDataFormat, version);
    }

    public int numberOfHMetrics() {
      return internalReadData().readUShort(Offset.numberOfHMetrics);
    }

    public void setNumberOfHMetrics(int version) {
      internalWriteData().writeUShort(Offset.numberOfHMetrics, version);
    }
  }
}

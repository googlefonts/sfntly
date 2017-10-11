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

package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.TableBasedTableBuilder;

/**
 * A Horizontal Device Metrics table - 'hdmx'.
 *
 * @author raph@google.com (Raph Levien)
 * @see "ISO/IEC 14496-22:2015, section 5.7.2"
 */
public class HorizontalDeviceMetricsTable extends Table {

  private int numGlyphs;

  private interface HeaderOffset {
    int version = 0;
    int numRecords = 2;
    int sizeDeviceRecord = 4;
    int SIZE = 8;
  }

  private interface DeviceOffset {
    int pixelSize = 0;
    int maxWidth = 1;
    int widths = 2;
  }

  private HorizontalDeviceMetricsTable(Header header, ReadableFontData data, int numGlyphs) {
    super(header, data);
    this.numGlyphs = numGlyphs;
  }

  public int version() {
    return this.data.readUShort(HeaderOffset.version);
  }

  public int numRecords() {
    return this.data.readShort(HeaderOffset.numRecords);
  }

  public int recordSize() {
    return this.data.readLong(HeaderOffset.sizeDeviceRecord);
  }

  public int pixelSize(int recordIx) {
    return this.data.readUByte(this.deviceOffset(recordIx) + DeviceOffset.pixelSize);
  }

  public int maxWidth(int recordIx) {
    return this.data.readUByte(this.deviceOffset(recordIx) + DeviceOffset.maxWidth);
  }

  public int width(int recordIx, int glyphNum) {
    if (glyphNum < 0 || glyphNum >= this.numGlyphs) {
      throw new IndexOutOfBoundsException();
    }
    return this.data.readUByte(this.deviceOffset(recordIx) + DeviceOffset.widths + glyphNum);
  }

  private int deviceOffset(int index) {
    if (index < 0 || index >= this.numRecords()) {
      throw new IndexOutOfBoundsException();
    }
    return HeaderOffset.SIZE + index * this.recordSize();
  }

  /**
   * Builder for a Horizontal Device Metrics Table - 'hdmx'.
   */
  public static class Builder extends TableBasedTableBuilder<HorizontalDeviceMetricsTable> {
    private int numGlyphs = -1;

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
    protected HorizontalDeviceMetricsTable subBuildTable(ReadableFontData data) {
      return new HorizontalDeviceMetricsTable(this.header(), data, this.numGlyphs);
    }

    public void setNumGlyphs(int numGlyphs) {
      if (numGlyphs < 0) {
        throw new IllegalArgumentException("Number of glyphs can't be negative.");
      }
      this.numGlyphs = numGlyphs;
      this.table().numGlyphs = numGlyphs;
    }
  }
}

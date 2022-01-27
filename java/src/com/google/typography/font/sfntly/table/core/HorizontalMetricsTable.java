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

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.TableBasedTableBuilder;

/**
 * A Horizontal Metrics table - 'hmtx'.
 *
 * @author Stuart Gill
 * @see "ISO/IEC 14496-22:2015, section 5.2.4"
 */
public final class HorizontalMetricsTable extends Table {

  private int numHMetrics;
  private int numGlyphs;

  private interface MetricOffset {
    int advanceWidth = 0;
    int leftSideBearing = 2;
    int SIZE = 4;
  }

  private HorizontalMetricsTable(
      Header header, ReadableFontData data, int numHMetrics, int numGlyphs) {
    super(header, data);
    this.numHMetrics = numHMetrics;
    this.numGlyphs = numGlyphs;
  }

  public int numberOfHMetrics() {
    return numHMetrics;
  }

  public int numberOfLSBs() {
    return numGlyphs - numHMetrics;
  }

  public int hMetricAdvanceWidth(int entry) {
    if (entry > numHMetrics) {
      throw new IndexOutOfBoundsException();
    }
    return data.readUShort(entry * MetricOffset.SIZE + MetricOffset.advanceWidth);
  }

  public int hMetricLSB(int entry) {
    if (entry > numHMetrics) {
      throw new IndexOutOfBoundsException();
    }
    return data.readShort(entry * MetricOffset.SIZE + MetricOffset.leftSideBearing);
  }

  public int lsbTableEntry(int entry) {
    if (entry > numberOfLSBs()) {
      throw new IndexOutOfBoundsException();
    }
    return data.readShort(numHMetrics * MetricOffset.SIZE + entry * FontData.SizeOf.SHORT);
  }

  public int advanceWidth(int glyphId) {
    if (glyphId < numHMetrics) {
      return hMetricAdvanceWidth(glyphId);
    }
    return hMetricAdvanceWidth(numHMetrics - 1);
  }

  public int leftSideBearing(int glyphId) {
    if (glyphId < numHMetrics) {
      return hMetricLSB(glyphId);
    }
    return lsbTableEntry(glyphId - numHMetrics);
  }

  /** Builder for a Horizontal Metrics Table - 'hmtx'. */
  public static class Builder extends TableBasedTableBuilder<HorizontalMetricsTable> {
    private int numHMetrics = -1;
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
    protected HorizontalMetricsTable subBuildTable(ReadableFontData data) {
      return new HorizontalMetricsTable(header(), data, numHMetrics, numGlyphs);
    }

    public void setNumberOfHMetrics(int numHMetrics) {
      if (numHMetrics < 0) {
        throw new IllegalArgumentException("Number of metrics can't be negative.");
      }
      this.numHMetrics = numHMetrics;
      table().numHMetrics = numHMetrics;
    }

    public void setNumGlyphs(int numGlyphs) {
      if (numGlyphs < 0) {
        throw new IllegalArgumentException("Number of glyphs can't be negative.");
      }
      this.numGlyphs = numGlyphs;
      table().numGlyphs = numGlyphs;
    }
  }
}

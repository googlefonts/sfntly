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
 * A Maximum Profile table - 'maxp'.
 *
 * @author Stuart Gill
 */
public final class MaximumProfileTable extends Table {

  private interface Offset {
    // version 0.5 and 1.0
    int version = 0;
    int numGlyphs = 4;

    // version 1.0
    int maxPoints = 6;
    int maxContours = 8;
    int maxCompositePoints = 10;
    int maxCompositeContours = 12;
    int maxZones = 14;
    int maxTwilightPoints = 16;
    int maxStorage = 18;
    int maxFunctionDefs = 20;
    int maxInstructionDefs = 22;
    int maxStackElements = 24;
    int maxSizeOfInstructions = 26;
    int maxComponentElements = 28;
    int maxComponentDepth = 30;
  }

  private MaximumProfileTable(Header header, ReadableFontData data) {
    super(header, data);
  }

  public int tableVersion() {
    return this.data.readFixed(Offset.version);
  }

  public int numGlyphs() {
    return this.data.readUShort(Offset.numGlyphs);
  }

  public int maxPoints() {
    return this.data.readUShort(Offset.maxPoints);
  }

  public int maxContours() {
    return this.data.readUShort(Offset.maxContours);
  }

  public int maxCompositePoints() {
    return this.data.readUShort(Offset.maxCompositePoints);
  }

  public int maxCompositeContours() {
    return this.data.readUShort(Offset.maxCompositeContours);
  }

  public int maxZones() {
    return this.data.readUShort(Offset.maxZones);
  }

  public int maxTwilightPoints() {
    return this.data.readUShort(Offset.maxTwilightPoints);
  }

  public int maxStorage() {
    return this.data.readUShort(Offset.maxStorage);
  }

  public int maxFunctionDefs() {
    return this.data.readUShort(Offset.maxFunctionDefs);
  }

  public int maxStackElements() {
    return this.data.readUShort(Offset.maxStackElements);
  }

  public int maxSizeOfInstructions() {
    return this.data.readUShort(Offset.maxSizeOfInstructions);
  }

  public int maxComponentElements() {
    return this.data.readUShort(Offset.maxComponentElements);
  }

  public int maxComponentDepth() {
    return this.data.readUShort(Offset.maxComponentDepth);
  }

  /**
   * Builder for a Maximum Profile table - 'maxp'.
   */
  public static class Builder extends TableBasedTableBuilder<MaximumProfileTable> {

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
    protected MaximumProfileTable subBuildTable(ReadableFontData data) {
      return new MaximumProfileTable(this.header(), data);
    }

    public int tableVersion() {
      return this.internalReadData().readUShort(Offset.version);
    }

    public void setTableVersion(int version) {
      this.internalWriteData().writeUShort(Offset.version, version);
    }

    public int numGlyphs() {
      return this.internalReadData().readUShort(Offset.numGlyphs);
    }

    public void setNumGlyphs(int numGlyphs) {
      this.internalWriteData().writeUShort(Offset.numGlyphs, numGlyphs);
    }

    public int maxPoints() {
      return this.internalReadData().readUShort(Offset.maxPoints);
    }

    public void maxPoints(int maxPoints) {
      this.internalWriteData().writeUShort(Offset.maxPoints, maxPoints);
    }

    public int maxContours() {
      return this.internalReadData().readUShort(Offset.maxContours);
    }

    public void setMaxContours(int maxContours) {
      this.internalWriteData().writeUShort(Offset.maxContours, maxContours);
    }

    public int maxCompositePoints() {
      return this.internalReadData().readUShort(Offset.maxCompositePoints);
    }

    public void setMaxCompositePoints(int maxCompositePoints) {
      this.internalWriteData().writeUShort(Offset.maxCompositePoints, maxCompositePoints);
    }

    public int maxCompositeContours() {
      return this.internalReadData().readUShort(Offset.maxCompositeContours);
    }

    public void setMaxCompositeContours(int maxCompositeContours) {
      this.internalWriteData().writeUShort(Offset.maxCompositeContours, maxCompositeContours);
    }

    public int maxZones() {
      return this.internalReadData().readUShort(Offset.maxZones);
    }

    public void setMaxZones(int maxZones) {
      this.internalWriteData().writeUShort(Offset.maxZones, maxZones);
    }

    public int maxTwilightPoints() {
      return this.internalReadData().readUShort(Offset.maxTwilightPoints);
    }

    public void setMaxTwilightPoints(int maxTwilightPoints) {
      this.internalWriteData().writeUShort(Offset.maxTwilightPoints, maxTwilightPoints);
    }

    public int maxStorage() {
      return this.internalReadData().readUShort(Offset.maxStorage);
    }

    public void setMaxStorage(int maxStorage) {
      this.internalWriteData().writeUShort(Offset.maxStorage, maxStorage);
    }

    public int maxFunctionDefs() {
      return this.internalReadData().readUShort(Offset.maxFunctionDefs);
    }

    public void setMaxFunctionDefs(int maxFunctionDefs) {
      this.internalWriteData().writeUShort(Offset.maxFunctionDefs, maxFunctionDefs);
    }

    public int maxStackElements() {
      return this.internalReadData().readUShort(Offset.maxStackElements);
    }

    public void setMaxStackElements(int maxStackElements) {
      this.internalWriteData().writeUShort(Offset.maxStackElements, maxStackElements);
    }

    public int maxSizeOfInstructions() {
      return this.internalReadData().readUShort(Offset.maxSizeOfInstructions);
    }

    public void setMaxSizeOfInstructions(int maxSizeOfInstructions) {
      this.internalWriteData().writeUShort(Offset.maxSizeOfInstructions, maxSizeOfInstructions);
    }

    public int maxComponentElements() {
      return this.internalReadData().readUShort(Offset.maxComponentElements);
    }

    public void setMaxComponentElements(int maxComponentElements) {
      this.internalWriteData().writeUShort(Offset.maxComponentElements, maxComponentElements);
    }

    public int maxComponentDepth() {
      return this.internalReadData().readUShort(Offset.maxComponentDepth);
    }

    public void setMaxComponentDepth(int maxComponentDepth) {
      this.internalWriteData().writeUShort(Offset.maxComponentDepth, maxComponentDepth);
    }
  }
}

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

package com.google.typography.font.sfntly.table.truetype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.SubTableContainerTable;
import java.util.ArrayList;
import java.util.List;

/**
 * The 'glyf' table contains the glyph data.
 *
 * @author Stuart Gill
 * @see LocaTable
 * @see "ISO/IEC 14496-22:2015, section 5.3.3"
 */
public final class GlyphTable extends SubTableContainerTable {

  public interface Offset {
    // header
    int numberOfContours = 0;
    int xMin = 2;
    int yMin = 4;
    int xMax = 6;
    int yMax = 8;

    // Simple Glyph Description
    int simpleEndPtsOfCountours = 10;
    // offset from the end of the contours array
    int simpleInstructionLength = 0;
    int simpleInstructions = 2;
    // flags
    // xCoordinates
    // yCoordinates

    // Composite Glyph Description
    int compositeFlags = 0;
    int compositeGlyphIndexWithoutFlag = 0;
    int compositeGlyphIndexWithFlag = 2;
  }

  private GlyphTable(Header header, ReadableFontData data) {
    super(header, data);
  }

  /**
   * Get the glyph data from a particular offset in the table.
   *
   * @param offset the offset, as returned by {@link LocaTable#glyphOffset(int)}
   * @param length the length, as returned by {@link LocaTable#glyphLength(int)}
   * @return the glyph from the given offset
   */
  public Glyph glyph(int offset, int length) {
    return Glyph.getGlyph(this, data, offset, length);
  }

  public static class Builder extends SubTableContainerTable.Builder<GlyphTable> {

    private List<Glyph.Builder<? extends Glyph>> glyphBuilders;
    private List<Integer> loca;

    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    protected Builder(Header header, WritableFontData data) {
      super(header, data);
    }

    protected Builder(Header header, ReadableFontData data) {
      super(header, data);
    }

    // glyph table level building

    public void setLoca(List<Integer> loca) {
      this.loca = new ArrayList<>(loca);
      setModelChanged(false);
      this.glyphBuilders = null;
    }

    /**
     * Generate a loca table list from the current state of the glyph table builder.
     *
     * @return a list of loca information for the glyphs
     */
    public List<Integer> generateLocaList() {
      List<Integer> locas = new ArrayList<>(getGlyphBuilders().size());
      locas.add(0);
      if (getGlyphBuilders().size() == 0) {
        locas.add(0);
      } else {
        int total = 0;
        for (Glyph.Builder<? extends Glyph> b : getGlyphBuilders()) {
          int size = b.subDataSizeToSerialize();
          locas.add(total + size);
          total += size;
        }
      }
      return locas;
    }

    private void initialize(ReadableFontData data, List<Integer> loca) {
      this.glyphBuilders = new ArrayList<>();

      if (data != null) {
        int locaValue;
        int lastLocaValue = loca.get(0);
        for (int i = 1; i < loca.size(); i++) {
          locaValue = loca.get(i);
          glyphBuilders.add(
              Glyph.Builder.getBuilder(
                  this, data, lastLocaValue /* offset */, locaValue - lastLocaValue /* length */));
          lastLocaValue = locaValue;
        }
      }
    }

    private List<Glyph.Builder<? extends Glyph>> getGlyphBuilders() {
      if (glyphBuilders == null) {
        if (internalReadData() != null && loca == null) {
          throw new IllegalStateException("Loca values not set - unable to parse glyph data.");
        }
        initialize(internalReadData(), loca);
        setModelChanged();
      }
      return glyphBuilders;
    }

    public void revert() {
      this.glyphBuilders = null;
      setModelChanged(false);
    }

    /**
     * Gets the List of glyph builders for the glyph table builder. These may be manipulated in any
     * way by the caller and the changes will be reflected in the final glyph table produced.
     *
     * <p>If there is no current data for the glyph builder or the glyph builders have not been
     * previously set then this will return an empty glyph builder List. If there is current data
     * (i.e. data read from an existing font) and the {@code loca} list has not been set or is null,
     * empty, or invalid, then an empty glyph builder List will be returned.
     */
    public List<Glyph.Builder<? extends Glyph>> glyphBuilders() {
      return getGlyphBuilders();
    }

    /**
     * Replace the internal glyph builders with the one provided. The provided list and all
     * contained objects belong to this builder.
     *
     * <p>This call is only required if the entire set of glyphs in the glyph table builder are
     * being replaced. If the glyph builder list provided from the {@link
     * GlyphTable.Builder#glyphBuilders()} is being used and modified then those changes will
     * already be reflected in the glyph table builder.
     */
    public void setGlyphBuilders(List<Glyph.Builder<? extends Glyph>> glyphBuilders) {
      this.glyphBuilders = glyphBuilders;
      setModelChanged();
    }

    // glyph builder factories

    public Glyph.Builder<? extends Glyph> glyphBuilder(ReadableFontData data) {
      Glyph.Builder<? extends Glyph> glyphBuilder = Glyph.Builder.getBuilder(this, data);
      return glyphBuilder;
    }

    // internal API for building

    @Override
    protected GlyphTable subBuildTable(ReadableFontData data) {
      return new GlyphTable(header(), data);
    }

    @Override
    protected void subDataSet() {
      this.glyphBuilders = null;
      super.setModelChanged(false);
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (glyphBuilders == null || glyphBuilders.size() == 0) {
        return 0;
      }

      boolean variable = false;
      int size = 0;

      // calculate size of each table
      for (Glyph.Builder<? extends Glyph> b : glyphBuilders) {
        int glyphSize = b.subDataSizeToSerialize();
        size += Math.abs(glyphSize);
        variable |= glyphSize <= 0;
      }
      return variable ? -size : size;
    }

    @Override
    protected boolean subReadyToSerialize() {
      if (glyphBuilders == null) {
        return false;
      }
      // TODO(stuartg): check glyphs for ready to build?
      return true;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      int size = 0;
      for (Glyph.Builder<? extends Glyph> b : glyphBuilders) {
        size += b.subSerialize(newData.slice(size));
      }
      return size;
    }
  }
}

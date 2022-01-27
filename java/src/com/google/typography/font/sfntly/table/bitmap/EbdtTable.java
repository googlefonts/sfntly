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
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.SubTableContainerTable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** @author Stuart Gill */
public final class EbdtTable extends SubTableContainerTable {

  private interface HeaderOffsets {
    int version = 0;
    int SIZE = 4;
  }

  protected EbdtTable(Header header, ReadableFontData data) {
    super(header, data);
  }

  public int version() {
    return data.readFixed(HeaderOffsets.version);
  }

  public BitmapGlyph glyph(int offset, int length, int format) {
    ReadableFontData glyphData = data.slice(offset, length);
    return BitmapGlyph.createGlyph(glyphData, format);
  }

  public static class Builder extends SubTableContainerTable.Builder<EbdtTable> {
    private final int version = 0x00020000; // TODO(stuartg) need a constant/enum
    private List<Map<Integer, BitmapGlyphInfo>> glyphLoca;
    private List<Map<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>>> glyphBuilders;

    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    public static Builder createBuilder(Header header, ReadableFontData data) {
      return new Builder(header, data);
    }

    protected Builder(Header header, WritableFontData data) {
      super(header, data);
    }

    protected Builder(Header header, ReadableFontData data) {
      super(header, data);
    }

    public void setLoca(List<Map<Integer, BitmapGlyphInfo>> locaList) {
      revert();
      this.glyphLoca = locaList;
    }

    public List<Map<Integer, BitmapGlyphInfo>> generateLocaList() {
      if (glyphBuilders == null) {
        if (glyphLoca == null) {
          return new ArrayList<>(0);
        }
        return glyphLoca;
      }

      List<Map<Integer, BitmapGlyphInfo>> newLocaList = new ArrayList<>(glyphBuilders.size());

      int startOffset = HeaderOffsets.SIZE;
      for (Map<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>> builderMap : glyphBuilders) {
        Map<Integer, BitmapGlyphInfo> newLocaMap = new TreeMap<>();
        int glyphOffset = 0;
        for (Map.Entry<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>> glyphEntry :
            builderMap.entrySet()) {
          BitmapGlyph.Builder<? extends BitmapGlyph> builder = glyphEntry.getValue();
          int size = builder.subDataSizeToSerialize();
          BitmapGlyphInfo info =
              new BitmapGlyphInfo(
                  glyphEntry.getKey(), startOffset + glyphOffset, size, builder.format());
          newLocaMap.put(glyphEntry.getKey(), info);
          glyphOffset += size;
        }
        startOffset += glyphOffset;
        newLocaList.add(Collections.unmodifiableMap(newLocaMap));
      }
      return Collections.unmodifiableList(newLocaList);
    }

    /**
     * Gets the List of glyph builders for the glyph table builder. These may be manipulated in any
     * way by the caller and the changes will be reflected in the final glyph table produced.
     *
     * <p>If there is no current data for the glyph builder or the glyph builders have not been
     * previously set then this will return an empty glyph builder List. If there is current data
     * (i.e. data read from an existing font) and the {@code loca} list has not been set or is null,
     * empty, or invalid, then an empty glyph builder List will be returned.
     *
     * @return the list of glyph builders
     */
    public List<Map<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>>> glyphBuilders() {
      return getGlyphBuilders();
    }

    /**
     * Replace the internal glyph builders with the one provided. The provided list and all
     * contained objects belong to this builder.
     *
     * <p>This call is only required if the entire set of glyphs in the glyph table builder are
     * being replaced. If the glyph builder list provided from the {@link
     * EbdtTable.Builder#glyphBuilders()} is being used and modified then those changes will already
     * be reflected in the glyph table builder.
     *
     * @param glyphBuilders the new glyph builders
     */
    public void setGlyphBuilders(
        List<Map<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>>> glyphBuilders) {
      this.glyphBuilders = glyphBuilders;
      setModelChanged();
    }

    private List<Map<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>>> getGlyphBuilders() {
      if (glyphBuilders == null) {
        if (glyphLoca == null) {
          throw new IllegalStateException("Loca values not set - unable to parse glyph data.");
        }
        this.glyphBuilders = initialize(internalReadData(), glyphLoca);
        setModelChanged();
      }
      return glyphBuilders;
    }

    public void revert() {
      this.glyphLoca = null;
      this.glyphBuilders = null;
      setModelChanged(false);
    }

    private static List<Map<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>>> initialize(
        ReadableFontData data, List<Map<Integer, BitmapGlyphInfo>> locaList) {

      List<Map<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>>> glyphBuilderList =
          new ArrayList<>(locaList.size());
      if (data != null) {
        for (Map<Integer, BitmapGlyphInfo> locaMap : locaList) {
          Map<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>> glyphBuilderMap =
              new TreeMap<>();
          for (Map.Entry<Integer, BitmapGlyphInfo> entry : locaMap.entrySet()) {
            BitmapGlyphInfo info = entry.getValue();
            BitmapGlyph.Builder<? extends BitmapGlyph> glyphBuilder =
                BitmapGlyph.Builder.createGlyphBuilder(
                    data.slice(info.offset(), info.length()), info.format());
            glyphBuilderMap.put(entry.getKey(), glyphBuilder);
          }
          glyphBuilderList.add(glyphBuilderMap);
        }
      }
      return glyphBuilderList;
    }

    @Override
    protected EbdtTable subBuildTable(ReadableFontData data) {
      return new EbdtTable(header(), data);
    }

    @Override
    protected void subDataSet() {
      revert();
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (glyphBuilders == null || glyphBuilders.isEmpty()) {
        return 0;
      }

      boolean fixed = true;
      int size = HeaderOffsets.SIZE;
      for (Map<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>> builderMap : glyphBuilders) {
        for (Map.Entry<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>> glyphEntry :
            builderMap.entrySet()) {
          BitmapGlyph.Builder<? extends BitmapGlyph> builder = glyphEntry.getValue();
          int glyphSize = builder.subDataSizeToSerialize();
          size += Math.abs(glyphSize);
          fixed = glyphSize > 0 && fixed;
        }
      }
      return (fixed ? 1 : -1) * size;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return glyphBuilders != null;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      int size = 0;
      size += newData.writeFixed(HeaderOffsets.version, version);

      for (Map<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>> builderMap : glyphBuilders) {
        for (Map.Entry<Integer, BitmapGlyph.Builder<? extends BitmapGlyph>> glyphEntry :
            builderMap.entrySet()) {
          BitmapGlyph.Builder<? extends BitmapGlyph> builder = glyphEntry.getValue();
          size += builder.subSerialize(newData.slice(size));
        }
      }
      return size;
    }
  }
}

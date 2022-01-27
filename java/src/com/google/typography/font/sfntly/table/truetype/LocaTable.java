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

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.core.FontHeaderTable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The 'loca' table maps glyphIds to their location in the 'glyf' table.
 *
 * @author Stuart Gill
 * @see "ISO/IEC 14496-22:2015, section 5.3.4"
 */
public final class LocaTable extends Table {

  private final FontHeaderTable.IndexToLocFormat version;
  private final int numGlyphs;

  private LocaTable(
      Header header,
      ReadableFontData data,
      FontHeaderTable.IndexToLocFormat version,
      int numGlyphs) {
    super(header, data);
    this.version = version;
    this.numGlyphs = numGlyphs;
  }

  /**
   * Get the table version.
   *
   * @return the table version
   */
  public FontHeaderTable.IndexToLocFormat formatVersion() {
    return version;
  }

  public int numGlyphs() {
    return numGlyphs;
  }

  /**
   * Return the offset for the given glyph id. Valid glyph ids are from 0 to the one less than the
   * number of glyphs. The zero entry is the special entry for the notdef glyph. The final entry
   * beyond the last glyph id is used to calculate the size of the last glyph.
   *
   * @param glyphId the glyph id to get the offset for; must be less than or equal to one more than
   *     the number of glyph ids
   * @return the offset in the glyph table to the specified glyph id
   */
  public int glyphOffset(int glyphId) {
    if (glyphId < 0 || glyphId >= numGlyphs) {
      throw new IndexOutOfBoundsException("Glyph ID is out of bounds.");
    }
    return loca(glyphId);
  }

  /**
   * Get the length of the data in the glyph table for the specified glyph id.
   *
   * @param glyphId the glyph id to get the offset for; must be greater than or equal to 0 and less
   *     than the number of glyphs in the font
   * @return the length of the data in the glyph table for the specified glyph id
   */
  public int glyphLength(int glyphId) {
    if (glyphId < 0 || glyphId >= numGlyphs) {
      throw new IndexOutOfBoundsException("Glyph ID is out of bounds.");
    }
    return loca(glyphId + 1) - loca(glyphId);
  }

  /**
   * Get the number of locations or locas. This will be one more than the number of glyphs for this
   * table since the last loca position is used to indicate the size of the final glyph.
   *
   * @return the number of locas
   */
  public int numLocas() {
    return numGlyphs + 1;
  }

  /**
   * Get the value from the loca table for the index specified. These are the raw values from the
   * table that are used to compute the offset and size of a glyph in the glyph table. Valid index
   * values run from 0 to the number of glyphs in the font.
   *
   * @param index the loca table index
   * @return the loca table value
   */
  public int loca(int index) {
    if (index < 0 || index > numGlyphs) {
      throw new IndexOutOfBoundsException("Glyph ID is out of bounds.");
    }
    if (version == FontHeaderTable.IndexToLocFormat.shortOffset) {
      return 2 * data.readUShort(index * FontData.SizeOf.USHORT);
    }
    return data.readULongAsInt(index * FontData.SizeOf.ULONG);
  }

  /**
   * Get an iterator over the loca values for the table. The iterator returned does not support the
   * delete operation.
   *
   * @return loca iterator
   * @see #loca
   */
  Iterator<Integer> iterator() {
    return new LocaIterator();
  }

  /** Iterator over the raw loca values. */
  private final class LocaIterator implements Iterator<Integer> {
    int index;

    private LocaIterator() {}

    @Override
    public boolean hasNext() {
      return index <= numGlyphs;
    }

    @Override
    public Integer next() {
      return loca(index++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  /** Builder for a loca table. */
  public static class Builder extends Table.Builder<LocaTable> {

    // values that need to be set to properly parse an existing loca table
    private FontHeaderTable.IndexToLocFormat formatVersion =
        FontHeaderTable.IndexToLocFormat.longOffset;
    private int numGlyphs = -1;

    // parsed loca table
    private List<Integer> loca;

    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    private Builder(Header header, WritableFontData data) {
      super(header, data);
    }

    private Builder(Header header, ReadableFontData data) {
      super(header, data);
    }

    /**
     * Initialize the internal state from the data. Done lazily since in many cases the builder will
     * be just creating a table object with no parsing required.
     *
     * @param data the data to initialize from
     */
    private void initialize(ReadableFontData data) {
      clearLoca(false);
      if (loca == null) {
        this.loca = new ArrayList<>();
      }
      if (data != null) {
        if (numGlyphs < 0) {
          throw new IllegalStateException("numglyphs not set on LocaTable Builder.");
        }

        LocaTable table = new LocaTable(header(), data, formatVersion, numGlyphs);
        Iterator<Integer> locaIter = table.iterator();
        while (locaIter.hasNext()) {
          loca.add(locaIter.next());
        }
      }
    }

    /**
     * Checks that the glyph id is within the correct range.
     *
     * @throws IndexOutOfBoundsException if the glyph id is not within the correct range
     */
    private int checkGlyphRange(int glyphId) {
      if (glyphId < 0 || glyphId > lastGlyphIndex()) {
        throw new IndexOutOfBoundsException("Glyph ID is outside of the allowed range.");
      }
      return glyphId;
    }

    private int lastGlyphIndex() {
      return loca != null ? loca.size() - 2 : numGlyphs - 1;
    }

    /**
     * Internal method to get the loca list if already generated and if not to initialize the state
     * of the builder.
     *
     * @return the loca list
     */
    private List<Integer> getLocaList() {
      if (loca == null) {
        initialize(internalReadData());
        setModelChanged();
      }
      return loca;
    }

    private void clearLoca(boolean nullify) {
      if (loca != null) {
        loca.clear();
      }
      if (nullify) {
        this.loca = null;
      }
      setModelChanged(false);
    }

    /** Get the format version that will be used when the loca table is generated. */
    public FontHeaderTable.IndexToLocFormat formatVersion() {
      return formatVersion;
    }

    /** Set the format version to be used when generating the loca table. */
    public void setFormatVersion(FontHeaderTable.IndexToLocFormat formatVersion) {
      this.formatVersion = formatVersion;
    }

    /**
     * Gets the List of locas for loca table builder. These may be manipulated in any way by the
     * caller and the changes will be reflected in the final loca table produced as long as no
     * subsequent call is made to the {@link #setLocaList(List)} method.
     *
     * <p>If there is no current data for the loca table builder or the loca list have not been
     * previously set then this will return an empty List.
     *
     * @return the list of glyph builders
     * @see #setLocaList(List)
     */
    public List<Integer> locaList() {
      return getLocaList();
    }

    /**
     * Set the list of locas to be used for building this table. If any existing list was already
     * retrieved with the {@link #locaList()} method then the connection of that previous list to
     * this builder will be broken.
     *
     * @see #locaList()
     */
    public void setLocaList(List<Integer> list) {
      this.loca = list;
      setModelChanged();
    }

    /**
     * Return the offset for the given glyph id. Valid glyph ids are from 0 to one more than the
     * number of glyphs. The zero entry is the special entry for the notdef glyph. The final entry
     * beyond the last glyph id is used to calculate the size of the last glyph.
     *
     * @param glyphId the glyph id to get the offset for; must be less than or equal to one more
     *     than the number of glyph ids
     * @return the offset in the glyph table to the specified glyph id
     */
    public int glyphOffset(int glyphId) {
      checkGlyphRange(glyphId);
      return getLocaList().get(glyphId);
    }

    /**
     * Get the length of the data in the glyph table for the specified glyph id. This is a
     * convenience method that uses the specified glyph id
     *
     * @param glyphId the glyph id to get the offset for; must be less than or equal to the number
     *     of glyphs
     * @return the length of the data in the glyph table for the specified glyph id
     */
    public int glyphLength(int glyphId) {
      checkGlyphRange(glyphId);
      return getLocaList().get(glyphId + 1) - getLocaList().get(glyphId);
    }

    /**
     * Set the number of glyphs.
     *
     * <p>This method sets the number of glyphs that the builder will attempt to parse location data
     * for from the raw binary data. This method only needs to be called (and <b>must</b> be) when
     * the raw data for this builder has been changed. It does not by itself reset the data or clear
     * any set loca list.
     *
     * @param numGlyphs the number of glyphs represented by the data
     */
    public void setNumGlyphs(int numGlyphs) {
      this.numGlyphs = numGlyphs;
    }

    /**
     * Get the number of glyphs that this builder has support for.
     *
     * @return the number of glyphs.
     */
    public int numGlyphs() {
      return lastGlyphIndex() + 1;
    }

    /**
     * Revert the loca table builder to the state contained in the last raw data set on the builder.
     * That raw data may be that read from a font file when the font builder was created, that set
     * by a user of the loca table builder, or null data if this builder was created as a new empty
     * builder.
     */
    public void revert() {
      this.loca = null;
      setModelChanged(false);
    }

    /**
     * Get the number of locations or locas. This will be one more than the number of glyphs for
     * this table since the last loca position is used to indicate the size of the final glyph.
     *
     * @return the number of locas
     */
    public int numLocas() {
      return getLocaList().size();
    }

    /**
     * Get the value from the loca table for the index specified. These are the raw values from the
     * table that are used to compute the offset and size of a glyph in the glyph table. Valid index
     * values run from 0 to the number of glyphs in the font.
     *
     * @param index the loca table index
     * @return the loca table value
     */
    public int loca(int index) {
      return getLocaList().get(index);
    }

    @Override
    protected LocaTable subBuildTable(ReadableFontData data) {
      return new LocaTable(header(), data, formatVersion, numGlyphs);
    }

    @Override
    protected void subDataSet() {
      initialize(internalReadData());
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (loca == null) {
        return 0;
      }
      if (formatVersion == FontHeaderTable.IndexToLocFormat.longOffset) {
        return loca.size() * FontData.SizeOf.ULONG;
      }
      return loca.size() * FontData.SizeOf.USHORT;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return loca != null;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      int size = 0;
      for (int l : loca) {
        if (formatVersion == FontHeaderTable.IndexToLocFormat.longOffset) {
          size += newData.writeULong(size, l);
        } else {
          size += newData.writeUShort(size, l / 2);
        }
      }
      this.numGlyphs = loca.size() - 1;
      return size;
    }
  }
}

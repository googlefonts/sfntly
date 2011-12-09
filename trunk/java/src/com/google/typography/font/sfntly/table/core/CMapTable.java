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

import com.google.typography.font.sfntly.Font.MacintoshEncodingId;
import com.google.typography.font.sfntly.Font.PlatformId;
import com.google.typography.font.sfntly.Font.UnicodeEncodingId;
import com.google.typography.font.sfntly.Font.WindowsEncodingId;
import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.math.FontMath;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.SubTableContainerTable;
import com.google.typography.font.sfntly.table.core.CMapTable.CMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A CMap table.
 *
 * @author Stuart Gill
 */
public final class CMapTable extends SubTableContainerTable implements Iterable<CMap> {

  /**
   * The .notdef glyph.
   */
  public static final int NOTDEF = 0;

  /**
   * Offsets to specific elements in the underlying data. These offsets are relative to the
   * start of the table or the start of sub-blocks within the table.
   */
  private enum Offset {
    version(0),
    numTables(2),
    encodingRecordStart(4),

    // offsets relative to the encoding record
    encodingRecordPlatformId(0),
    encodingRecordEncodingId(2),
    encodingRecordOffset(4),
    encodingRecordSize(8),

    format(0),

    // Format 0: Byte encoding table
    format0Format(0),
    format0Length(2),
    format0Language(4),
    format0GlyphIdArray(6),

    // Format 2: High-byte mapping through table
    format2Format(0),
    format2Length(2),
    format2Language(4),
    format2SubHeaderKeys(6),
    format2SubHeaders(518),
    // offset relative to the subHeader structure
    format2SubHeader_firstCode(0),
    format2SubHeader_entryCount(2),
    format2SubHeader_idDelta(4),
    format2SubHeader_idRangeOffset(6),
    format2SubHeader_structLength(8),

    // Format 4: Segment mapping to delta values
    format4Format(0),
    format4Length(2),
    format4Language(4),
    format4SegCountX2(6),
    format4SearchRange(8),
    format4EntrySelector(10),
    format4RangeShift(12),
    format4EndCount(14),
    format4FixedSize(16),

    // format 6: Trimmed table mapping
    format6Format(0),
    format6Length(2),
    format6Language(4),
    format6FirstCode(6),
    format6EntryCount(8),
    format6GlyphIdArray(10),

    // Format 8: mixed 16-bit and 32-bit coverage
    format8Format(0),
    format8Length(4),
    format8Language(8),
    format8Is32(12),
    format8nGroups(8204),
    format8Groups(8208),
    // ofset relative to the group structure
    format8Group_startCharCode(0),
    format8Group_endCharCode(4),
    format8Group_startGlyphId(8),
    format8Group_structLength(12),

    // Format 10: Trimmed array
    format10Format(0),
    format10Length(4),
    format10Language(8),
    format10StartCharCode(12),
    format10NumChars(16),
    format10Glyphs(20),

    // Format 12: Segmented coverage
    format12Format(0),
    format12Length(4),
    format12Language(8),
    format12nGroups(12),
    format12Groups(16),
    format12Groups_structLength(12),
    // offsets within the group structure
    format12_startCharCode(0),
    format12_endCharCode(4),
    format12_startGlyphId(8),

    // Format 13: Last Resort Font
    format13Format(0),
    format13Length(4),
    format13Language(8),
    format13nGroups(12),
    format13Groups(16),
    format13Groups_structLength(12),
    // offsets within the group structure
    format13_startCharCode(0),
    format13_endCharCode(4),
    format13_glyphId(8),

    // TODO: finish support for format 14
    // Format 14: Unicode Variation Sequences
    format14Format(0),
    format14Length(2);

    private final int offset;

    private Offset(int offset) {
      this.offset = offset;
    }
  }

  public static final class CMapId implements Comparable<CMapId> {

    public static final CMapId WINDOWS_BMP =
        CMapId.getInstance(PlatformId.Windows.value(), WindowsEncodingId.UnicodeUCS2.value());
    public static final CMapId WINDOWS_UCS4 =
        CMapId.getInstance(PlatformId.Windows.value(), WindowsEncodingId.UnicodeUCS4.value());
    public static final CMapId MAC_ROMAN =
        CMapId.getInstance(PlatformId.Macintosh.value(), MacintoshEncodingId.Roman.value());

    public static CMapId getInstance(int platformId, int encodingId) {
      return new CMapId(platformId, encodingId);
    }

    private final int platformId;
    private final int encodingId;

    private CMapId(int platformId, int encodingId) {
      this.platformId = platformId;
      this.encodingId = encodingId;
    }

    public int platformId() {
      return this.platformId;
    }

    public int encodingId() {
      return this.encodingId;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof CMapId)) {
        return false;
      }
      CMapId otherKey = (CMapId) obj;
      if ((otherKey.platformId == this.platformId) && (otherKey.encodingId == this.encodingId)) {
        return true;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return this.platformId << 8 | this.encodingId;
    }

    @Override
    public int compareTo(CMapId o) {
      return this.hashCode() - o.hashCode();
    }

    @Override
    public String toString() {
      StringBuilder b = new StringBuilder();
      b.append("pid = ");
      b.append(this.platformId);
      b.append(", eid = ");
      b.append(this.encodingId);
      return b.toString();
    }
  }
  /**
   * Constructor.
   *
   * @param header header for the table
   * @param data data for the table
   */
  private CMapTable(Header header, ReadableFontData data) {
    super(header, data);
  }

  /**
   * Get the table version.
   *
   * @return table version
   */
  public int version() {
    return this.data.readUShort(Offset.version.offset);
  }

  /**
   * Gets the number of cmaps within the CMap table.
   *
   * @return the number of cmaps
   */
  public int numCMaps() {
    return this.data.readUShort(Offset.numTables.offset);
  }

  /**
   * Gets the offset in the table data for the encoding record for the cmap with
   * the given index. The offset is from the beginning of the table.
   *
   * @param index the index of the cmap
   * @return offset in the table data
   */
  private static int offsetForEncodingRecord(int index) {
    return Offset.encodingRecordStart.offset + index * Offset.encodingRecordSize.offset;
  }

  /**
   * Gets the cmap id for the cmap with the given index.
   *
   * @param index the index of the cmap
   * @return the cmap id
   */
  public CMapId cmapId(int index) {
    return CMapId.getInstance(platformId(index), encodingId(index));
  }

  /**
   * Gets the platform id for the cmap with the given index.
   *
   * @param index the index of the cmap
   * @return the platform id
   */
  public int platformId(int index) {
    return this.data.readUShort(
        Offset.encodingRecordPlatformId.offset + CMapTable.offsetForEncodingRecord(index));
  }

  /**
   * Gets the encoding id for the cmap with the given index.
   *
   * @param index the index of the cmap
   * @return the encoding id
   */
  public int encodingId(int index) {
    return this.data.readUShort(
        Offset.encodingRecordEncodingId.offset + CMapTable.offsetForEncodingRecord(index));
  }

  /**
   * Gets the offset in the table data for the cmap table with the given index.
   * The offset is from the beginning of the table.
   *
   * @param index the index of the cmap
   * @return the offset in the table data
   */
  public int offset(int index) {
    return this.data.readULongAsInt(
        Offset.encodingRecordOffset.offset + CMapTable.offsetForEncodingRecord(index));
  }

  /**
   * Gets an iterator over all of the cmaps within this CMapTable.
   */
  @Override
  public Iterator<CMap> iterator() {
    return new CMapIterator();
  }

  /**
   * Gets an iterator over the cmaps within this CMap table using the provided
   * filter to select the cmaps returned.
   *
   * @param filter the filter
   * @return iterator over cmaps
   */
  public Iterator<CMap> iterator(CMapFilter filter) {
    return new CMapIterator(filter);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append(" = { ");
    for (int i = 0; i < this.numCMaps(); i++) {
      CMap cmap;
      try {
        cmap = this.cmap(i);
      } catch (IOException e) {
        continue;
      }
      sb.append("[0x");
      sb.append(Integer.toHexString(this.offset(i)));
      sb.append(" = ");
      sb.append(cmap);
      if (i < this.numCMaps() - 1) {
        sb.append("], ");
      } else {
        sb.append("]");
      }
    }
    sb.append(" }");
    return sb.toString();
  }

  /**
   * A filter on cmaps.
   */
  public interface CMapFilter {
    /**
     * Test on whether the cmap is acceptable or not.
     *
     * @param cmapId the id of the cmap
     * @return true if the cmap is acceptable; false otherwise
     */
    boolean accept(CMapId cmapId);
  }

  private class CMapIterator implements Iterator<CMap> {
    private int tableIndex = 0;
    private CMapFilter filter;

    private CMapIterator() {
      // no filter - iterate over all cmap subtables
    }

    private CMapIterator(CMapFilter filter) {
      this.filter = filter;
    }

    @Override
    public boolean hasNext() {
      if (this.filter == null) {
        if (this.tableIndex < numCMaps()) {
          return true;
        }
        return false;
      }
      for (; this.tableIndex < numCMaps(); this.tableIndex++) {
        if (filter.accept(cmapId(this.tableIndex))) {
          return true;
        }
      }
      return false;
    }

    @Override
    public CMap next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      try {
        return cmap(this.tableIndex++);
      } catch (IOException e) {
        NoSuchElementException newException =
            new NoSuchElementException("Error during the creation of the CMap.");
        newException.initCause(e);
        throw newException;
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove a CMap table from an existing font.");
    }
  }

  /**
   * Gets the cmap for the given index.
   *
   * @param index the index of the cmap
   * @return the cmap at the index
   * @throws IOException
   */
  public CMap cmap(int index) throws IOException {
    CMap.Builder<? extends CMap> builder =
        CMapTable.Builder.cmapBuilder(this.readFontData(), index);
    return builder.build();
  }

  /**
   * Gets the cmap with the given ids if it exists.
   *
   * @param platformId the platform id
   * @param encodingId the encoding id
   * @return the cmap if it exists; null otherwise
   */
  public CMap cmap(int platformId, int encodingId) {
    return cmap(CMapId.getInstance(platformId, encodingId));
  }

  public CMap cmap(final CMapId cmapId) {
    Iterator<CMap> cmapIter = this.iterator(new CMapFilter() {
      @Override
      public boolean accept(CMapId foundCMapId) {
        if (cmapId.equals(foundCMapId)) {
          return true;
        }
        return false;
      }
    });
    // can only be one cmap for each set of ids
    if (cmapIter.hasNext()) {
      return cmapIter.next();
    }
    return null;
  }

  /**
   * CMap subtable formats.
   *
   */
  public enum CMapFormat {
    Format0(0),
    Format2(2),
    Format4(4),
    Format6(6),
    Format8(8),
    Format10(10),
    Format12(12),
    Format13(13),
    Format14(14);

    private final int value;

    private CMapFormat(int value) {
      this.value = value;
    }

    public int value() {
      return this.value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static CMapFormat valueOf(int value) {
      for (CMapFormat format : CMapFormat.values()) {
        if (format.equals(value)) {
          return format;
        }
      }
      return null;
    }
  }

  /**
   * CMap Table Builder.
   *
   */
  public static class Builder extends SubTableContainerTable.Builder<CMapTable> {

    private int version = 0; // TODO(user): make a CMapTable constant
    private Map<CMapId, CMap.Builder<? extends CMap>> cmapBuilders;

    /**
     * Creates a new builder using the header information and data provided.
     *
     * @param header the header information
     * @param data the data holding the table
     * @return a new builder
     */
    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    /**
     * Constructor.
     *
     * @param header the table header
     * @param data the writable data for the table
     */
    protected Builder(Header header, WritableFontData data) {
      super(header, data);
    }

    /**
     * Constructor. This constructor will try to maintain the data as readable
     * but if editing operations are attempted then a writable copy will be made
     * the readable data will be discarded.
     *
     * @param header the table header
     * @param data the readable data for the table
     */
    protected Builder(Header header, ReadableFontData data) {
      super(header, data);
    }

    /**
     * Static factory method to create a cmap subtable builder.
     *
     * @param data the data for the whole cmap table
     * @param index the index of the cmap subtable within the table
     * @return the cmap subtable requested if it exists; null otherwise
     */
    protected static CMap.Builder<? extends CMap> cmapBuilder(ReadableFontData data, int index) {
      if (index < 0 || index > numCMaps(data)) {
        throw new IndexOutOfBoundsException(
            "CMap table is outside the bounds of the known tables.");
      }

      // read from encoding records
      int platformId = data.readUShort(
          Offset.encodingRecordPlatformId.offset + CMapTable.offsetForEncodingRecord(index));
      int encodingId = data.readUShort(
          Offset.encodingRecordEncodingId.offset + CMapTable.offsetForEncodingRecord(index));
      int offset = data.readULongAsInt(
          Offset.encodingRecordOffset.offset + CMapTable.offsetForEncodingRecord(index));
      CMapId cmapId = CMapId.getInstance(platformId, encodingId);

      CMap.Builder<? extends CMap> builder = CMap.Builder.getBuilder(data, offset, cmapId);
      return builder;
    }

    @Override
    protected void subDataSet() {
      this.cmapBuilders = null;
      super.setModelChanged(false);
    }

    private void initialize(ReadableFontData data) {
      this.cmapBuilders = new /*TreeMap*/ HashMap<CMapId, CMap.Builder<? extends CMap>>();

      int numCMaps = numCMaps(data);
      for (int i = 0; i < numCMaps; i++) {
        CMap.Builder<? extends CMap> cmapBuilder = cmapBuilder(data, i);
        cmapBuilders.put(cmapBuilder.cmapId(), cmapBuilder);
      }
    }

    private Map<CMapId, CMapTable.CMap.Builder<? extends CMap>> getCMapBuilders() {
      if (this.cmapBuilders != null) {
        return this.cmapBuilders;
      }
      this.initialize(this.internalReadData());
      this.setModelChanged();

      return this.cmapBuilders;
    }

    private static int numCMaps(ReadableFontData data) {
      if (data == null) {
        return 0;
      }
      return data.readUShort(Offset.numTables.offset);
    }

    public int numCMaps() {
      return this.getCMapBuilders().size();
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (this.cmapBuilders == null || this.cmapBuilders.size() == 0) {
        return 0;
      }

      boolean variable = false;
      int size = CMapTable.Offset.encodingRecordStart.offset + this.cmapBuilders.size()
      * CMapTable.Offset.encodingRecordSize.offset;

      // calculate size of each table
      for (CMap.Builder<? extends CMap> b : this.cmapBuilders.values()) {
        int cmapSize = b.subDataSizeToSerialize();
        size += Math.abs(cmapSize);
        variable |= cmapSize <= 0;
      }
      return variable ? -size : size;
    }

    @Override
    protected boolean subReadyToSerialize() {
      if (this.cmapBuilders == null) {
        return false;
      }
      // check each table
      for (CMap.Builder<? extends CMap> b : this.cmapBuilders.values()) {
        if (!b.subReadyToSerialize()) {
          return false;
        }
      }
      return true;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      int size = newData.writeUShort(CMapTable.Offset.version.offset, this.version());
      size += newData.writeUShort(CMapTable.Offset.numTables.offset, this.cmapBuilders.size());

      int indexOffset = size;
      size += this.cmapBuilders.size() * CMapTable.Offset.encodingRecordSize.offset;
      for (CMap.Builder<? extends CMap> b : this.cmapBuilders.values()) {
        // header entry
        indexOffset += newData.writeUShort(indexOffset, b.platformId());
        indexOffset += newData.writeUShort(indexOffset, b.encodingId());
        indexOffset += newData.writeULong(indexOffset, size);

        // cmap
        size += b.subSerialize(newData.slice(size));
      }
      return size;
    }

    @Override
    protected CMapTable subBuildTable(ReadableFontData data) {
      return new CMapTable(this.header(), data);
    }

    // public building API

    public Iterator<? extends CMap.Builder<? extends CMap>> iterator() {
      return this.getCMapBuilders().values().iterator();
    }

    public int version() {
      return this.version;
    }

    public void setVersion(int version) {
      this.version = version;
    }

    /**
     * Gets a new cmap builder for this cmap table. The new cmap builder will be
     * for the cmap id specified and initialized with the data given. The data
     * will be copied and the original data will not be modified.
     *
     * @param cmapId the id for the new cmap builder
     * @param data the data to copy for the new cmap builder
     * @return a new cmap builder initialized with the cmap id and a copy of the
     *         data
     * @throws IOException
     */
    public CMap.Builder<? extends CMap> newCMapBuilder(CMapId cmapId, ReadableFontData data)
        throws IOException {
      WritableFontData wfd = WritableFontData.createWritableFontData(data.size());
      data.copyTo(wfd);
      CMap.Builder<? extends CMap> builder = CMap.Builder.getBuilder(wfd, 0, cmapId);
      Map<CMapId, CMapTable.CMap.Builder<? extends CMap>> cmapBuilders = this.getCMapBuilders();
      cmapBuilders.put(cmapId, builder);
      return builder;
    }

    public CMap.Builder<? extends CMap> newCMapBuilder(CMapId cmapId, CMapFormat cmapFormat) {
      CMap.Builder<? extends CMap> builder = CMap.Builder.getBuilder(cmapFormat, cmapId);
      Map<CMapId, CMapTable.CMap.Builder<? extends CMap>> cmapBuilders = this.getCMapBuilders();
      cmapBuilders.put(cmapId, builder);
      return builder;
    }

    public CMap.Builder<? extends CMap> cmapBuilder(CMapId cmapId) {
      Map<CMapId, CMapTable.CMap.Builder<? extends CMap>> cmapBuilders = this.getCMapBuilders();
      return cmapBuilders.get(cmapId);
    }

  }

  /**
   * The abstract base class for all cmaps.
   *
   * CMap equality is based on the equality of the (@link {@link CMapId} that
   * defines the CMap. In the cmap table for a font there can only be one cmap
   * with a given cmap id (pair of platform and encoding ids) no matter what the
   * type of the cmap is.
   *
   * The cmap implements {@code Iterable<Integer>} to allow iteration over
   * characters that are mapped by the cmap. This iteration mostly returns the
   * characters mapped by the cmap. It will return all characters mapped by the
   * cmap to anything but .notdef <b>but</b> it may return some that are not
   * mapped or are mapped to .notdef. Various cmap tables provide ranges and
   * such to describe characters for lookup but without going the full way to
   * mapping to the glyph id it isn't always possible to tell if a character
   * will end up with a valid glyph id. So, some of the characters returned from
   * the iterator may still end up pointing to the .notdef glyph. However, the
   * number of such characters should be small in most cases with well designed
   * cmaps.
   */
  public abstract static class CMap extends SubTable implements Iterable<Integer> {
    protected final int format;
    protected final CMapId cmapId;

    /**
     * Constructor.
     *
     * @param data data for the cmap
     * @param format the format of the cmap
     * @param cmapId the id information of the cmap
     */
    protected CMap(ReadableFontData data, int format, CMapId cmapId) {
      super(data);
      this.format = format;
      this.cmapId = cmapId;
    }

    /**
     * Gets the format of the cmap.
     *
     * @return the format
     */
    public int format() {
      return this.format;
    }

    /**
     * Gets the cmap id for this cmap.
     *
     * @return cmap id
     */
    public CMapId cmapId() {
      return this.cmapId;
    }

    /**
     * Gets the platform id for this cmap.
     *
     * @return the platform id
     * @see PlatformId
     */
    public int platformId() {
      return this.cmapId().platformId();
    }

    /**
     * Gets the encoding id for this cmap.
     *
     * @return the encoding id
     * @see MacintoshEncodingId
     * @see WindowsEncodingId
     * @see UnicodeEncodingId
     */
    public int encodingId() {
      return this.cmapId().encodingId();
    }

    // TODO(user): simple implementation until all subclasses define their
    // own more efficient version
    protected class CharacterIterator implements Iterator<Integer> {
      private int character = 0;
      private final int maxCharacter;

      private CharacterIterator(int start, int end) {
        this.character = start;
        this.maxCharacter = end;
      }

      @Override
      public boolean hasNext() {
        if (character < maxCharacter) {
          return true;
        }
        return false;
      }

      @Override
      public Integer next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more characters to iterate.");
        }
        return this.character++;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unable to remove a character from cmap.");
      }
    }


     @Override
    public int hashCode() {
      return this.cmapId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof CMap)) {
        return false;
      }
      return this.cmapId.equals(((CMap) obj).cmapId);
    }

    /**
     * Gets the language of the cmap.
     *
     *  Note on the language field in 'cmap' subtables: The language field must
     * be set to zero for all cmap subtables whose platform IDs are other than
     * Macintosh (platform ID 1). For cmap subtables whose platform IDs are
     * Macintosh, set this field to the Macintosh language ID of the cmap
     * subtable plus one, or to zero if the cmap subtable is not
     * language-specific. For example, a Mac OS Turkish cmap subtable must set
     * this field to 18, since the Macintosh language ID for Turkish is 17. A
     * Mac OS Roman cmap subtable must set this field to 0, since Mac OS Roman
     * is not a language-specific encoding.
     *
     * @return the language id
     */
    public abstract int language();

    /**
     * Gets the glyph id for the character code provided.
     *
     * The character code provided must be in the encoding used by the cmap table.
     *
     * @param character character value using the encoding of the cmap table
     * @return glyph id for the character code
     */
    public abstract int glyphId(int character);

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("cmap: ");
      builder.append(this.cmapId());
      builder.append(", ");
      builder.append(CMapFormat.valueOf(this.format()));
      builder.append(", Data Size=0x");
      builder.append(Integer.toHexString(this.data.length()));
      return builder.toString();
    }

    public abstract static class Builder<T extends CMap> extends SubTable.Builder<T> {

      private final CMapFormat format;
      private final CMapId cmapId;
      private int language;

      /**
       * Constructor.
       *
       * @param data the data for the cmap
       * @param format cmap format
       * @param cmapId the id for this cmap
       */
      protected Builder(ReadableFontData data, CMapFormat format, CMapId cmapId) {
        super(data);
        this.format = format;
        this.cmapId = cmapId;
      }

      /**
       * @return the id for this cmap
       */
      public CMapId cmapId() {
        return this.cmapId;
      }

      /**
       * Gets the encoding id for the cmap. The encoding will from one of a
       * number of different sets depending on the platform id.
       *
       * @return the encoding id
       * @see MacintoshEncodingId
       * @see WindowsEncodingId
       * @see UnicodeEncodingId
       */
      public int encodingId() {
        return this.cmapId().encodingId();
      }

      /**
       * Gets the platform id for the cmap.
       *
       * @return the platform id
       * @see PlatformId
       */
      public int platformId() {
        return this.cmapId().platformId();
      }

      public CMapFormat format() {
        return this.format;
      }

      public int language() {
        return this.language;
      }

      public void setLanguage(int language) {
        this.language = language;
      }

      /**
       * @param data
       */
      protected Builder(WritableFontData data, CMapFormat format, CMapId cmapId) {
        super(data);
        this.format = format;
        this.cmapId = cmapId;
      }

      @Override
      protected void subDataSet() {
        // NOP
      }

      @Override
      protected int subDataSizeToSerialize() {
        return this.internalReadData().length();
      }

      @Override
      protected boolean subReadyToSerialize() {
        return true;
      }

      @Override
      protected int subSerialize(WritableFontData newData) {
        return this.internalReadData().copyTo(newData);
      }

      static Builder<? extends CMap> getBuilder(ReadableFontData data, int offset, CMapId cmapId) {
        // read from the front of the cmap - 1st entry is always the format
        int rawFormat = data.readUShort(offset);
        CMapFormat format = CMapFormat.valueOf(rawFormat);

        switch(format) {
          case Format0:
            return new CMapFormat0.Builder(data, offset, cmapId);
          case Format2:
            return new CMapFormat2.Builder(data, offset, cmapId);
          case Format4:
            return new CMapFormat4.Builder(data, offset, cmapId);
          case Format6:
            return new CMapFormat6.Builder(data, offset, cmapId);
          case Format8:
            return new CMapFormat8.Builder(data, offset, cmapId);
          case Format10:
            return new CMapFormat10.Builder(data, offset, cmapId);
          case Format12:
            return new CMapFormat12.Builder(data, offset, cmapId);
          case Format13:
            return new CMapFormat13.Builder(data, offset, cmapId);
          case Format14:
            return new CMapFormat14.Builder(data, offset, cmapId);
          default:
            break;
        }
        return null;
      }

      // TODO: Instead of a root factory method, the individual subtable
      // builders should get created
      // from static factory methods in each subclass
      static Builder<? extends CMap> getBuilder(CMapFormat cmapFormat, CMapId cmapId) {
        switch(cmapFormat) {
          // TODO: builders for other formats, as they're implemented
          case Format0:
            return new CMapFormat0.Builder(null, 0, cmapId);
          case Format4:
            return new CMapFormat4.Builder(null, 0, cmapId);
          default:
            break;
        }
        return null;
      }

      @Override
      public String toString() {
        return String.format("%s, format = %s", this.cmapId(), this.format());
      }
    }
  }

  /**
   * A cmap format 0 sub table.
   *
   */
  public static final class CMapFormat0 extends CMap {
    protected CMapFormat0(ReadableFontData data, CMapId cmapId) {
      super(data, CMapFormat.Format0.value, cmapId);
    }

    @Override
    public int glyphId(int character) {
      if (character < 0 || character > 255) {
        return CMapTable.NOTDEF;
      }
      return this.data.readUByte(character + Offset.format0GlyphIdArray.offset);
    }

    @Override
    public int language() {
      return this.data.readUShort(Offset.format0Language.offset);
    }

    @Override
    public Iterator<Integer> iterator() {
      return new CharacterIterator();
    }

    private class CharacterIterator implements Iterator<Integer> {
      int character = 0;
      protected static final int MAX_CHARACTER = 0xff;

      private CharacterIterator() {
      }

      @Override
      public boolean hasNext() {
        if (character <= MAX_CHARACTER) {
          return true;
        }
        return false;
      }

      @Override
      public Integer next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more characters to iterate.");
        }
        return this.character++;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unable to remove a character from cmap.");
      }
    }

    public static class Builder extends CMap.Builder<CMapFormat0> {
      protected Builder(WritableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readUShort(offset + Offset.format0Length.offset)), CMapFormat.Format0,
            cmapId);
      }

      protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readUShort(offset + Offset.format0Length.offset)), CMapFormat.Format0,
            cmapId);
      }

      @Override
      protected CMapFormat0 subBuildTable(ReadableFontData data) {
        return new CMapFormat0(data, this.cmapId());
      }
    }
  }

  /**
   * A cmap format 2 sub table.
   *
   * The format 2 cmap is used for multi-byte encodings such as SJIS,
   * EUC-JP/KR/CN, Big5, etc.
   */
  public static final class CMapFormat2 extends CMap {

    protected CMapFormat2(ReadableFontData data, CMapId cmapId) {
      super(data, CMapFormat.Format2.value, cmapId);
    }

    private int subHeaderOffset(int subHeaderIndex) {
      int subHeaderOffset = this.data.readUShort(
          Offset.format2SubHeaderKeys.offset + subHeaderIndex * FontData.DataSize.USHORT.size());
      return subHeaderOffset;
    }

    private int firstCode(int subHeaderIndex) {
      int subHeaderOffset = subHeaderOffset(subHeaderIndex);
      int firstCode =
          this.data.readUShort(subHeaderOffset + Offset.format2SubHeaderKeys.offset
              + Offset.format2SubHeader_firstCode.offset);
      return firstCode;
    }

    private int entryCount(int subHeaderIndex) {
      int subHeaderOffset = subHeaderOffset(subHeaderIndex);
      int entryCount =
          this.data.readUShort(subHeaderOffset + Offset.format2SubHeaderKeys.offset
              + Offset.format2SubHeader_entryCount.offset);
      return entryCount;
    }

    private int idRangeOffset(int subHeaderIndex) {
      int subHeaderOffset = subHeaderOffset(subHeaderIndex);
      int idRangeOffset = this.data.readUShort(subHeaderOffset + Offset.format2SubHeaderKeys.offset
          + Offset.format2SubHeader_idRangeOffset.offset);
      return idRangeOffset;
    }

    private int idDelta(int subHeaderIndex) {
      int subHeaderOffset = subHeaderOffset(subHeaderIndex);
      int idDelta =
          this.data.readShort(subHeaderOffset + Offset.format2SubHeaderKeys.offset
              + Offset.format2SubHeader_idDelta.offset);
      return idDelta;
    }

    /**
     * Returns how many bytes would be consumed by a lookup of this character
     * with this cmap. This comes about because the cmap format 2 table is
     * designed around multi-byte encodings such as SJIS, EUC-JP, Big5, etc.
     *
     * @param character
     * @return the number of bytes consumed from this "character" - either 1 or
     *         2
     */
    public int bytesConsumed(int character) {
      int highByte = (character >> 8) & 0xff;
      int offset = subHeaderOffset(highByte);

      if (offset == 0) {
        return 1;
      }
      return 2;
    }

    @Override
    public int glyphId(int character) {
      if (character > 0xffff) {
        return CMapTable.NOTDEF;
      }

      int highByte = (character >> 8) & 0xff;
      int lowByte = character & 0xff;
      int offset = subHeaderOffset(highByte);

      // only consume one byte
      if (offset == 0) {
        lowByte = highByte;
        highByte = 0;
      }

      int firstCode = firstCode(highByte);
      int entryCount = entryCount(highByte);

      if (lowByte < firstCode || lowByte >= firstCode + entryCount) {
        return CMapTable.NOTDEF;
      }

      int idRangeOffset = idRangeOffset(highByte);

      // position of idRangeOffset + value of idRangeOffset + index for low byte
      // = firstcode
      int pLocation = (offset + Offset.format2SubHeader_idRangeOffset.offset) + idRangeOffset
          + (lowByte - firstCode) * FontData.DataSize.USHORT.size();
      int p = this.data.readUShort(pLocation);
      if (p == 0) {
        return CMapTable.NOTDEF;
      }

      if (offset == 0) {
        return p;
      }
      int idDelta = idDelta(highByte);
      return (p + idDelta) % 65536;
    }

    @Override
    public int language() {
      return this.data.readUShort(Offset.format2Language.offset);
    }

    @Override
    public Iterator<Integer> iterator() {
      return new CharacterIterator(0, 0xffff);
    }

    public static class Builder extends CMap.Builder<CMapFormat2> {
      protected Builder(WritableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readUShort(offset + Offset.format2Length.offset)), CMapFormat.Format2,
            cmapId);
      }

      protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readUShort(offset + Offset.format2Length.offset)), CMapFormat.Format2,
            cmapId);
      }

      @Override
      protected CMapFormat2 subBuildTable(ReadableFontData data) {
        return new CMapFormat2(data, this.cmapId());
      }
    }
  }

  /**
   * A cmap format 4 sub table.
   */
  public static final class CMapFormat4 extends CMap {
    private final int segCount;
    private final int glyphIdArrayOffset;

    protected CMapFormat4(ReadableFontData data, CMapId cmapId) {
      super(data, CMapFormat.Format4.value, cmapId);

      this.segCount = this.data.readUShort(Offset.format4SegCountX2.offset) / 2;
      this.glyphIdArrayOffset = glyphIdArrayOffset(this.segCount);
    }

    @Override
    public int glyphId(int character) {
      int segment = this.data.searchUShort(CMapFormat4.startCodeOffset(this.segCount),
          FontData.DataSize.USHORT.size(),
          Offset.format4EndCount.offset,
          FontData.DataSize.USHORT.size(),
          this.segCount,
          character);
      if (segment == -1) {
        return CMapTable.NOTDEF;
      }
      int startCode = startCode(segment);

      return retrieveGlyphId(segment, startCode, character);
    }

    /**
     * Lower level glyph code retrieval that requires processing the Format 4 segments to use.
     *
     * @param segment the cmap segment
     * @param startCode the start code for the segment
     * @param character the character to be looked up
     * @return the glyph id for the character; CMapTable.NOTDEF if not found
     */
    public int retrieveGlyphId(int segment, int startCode, int character) {
      if (character < startCode) {
        return CMapTable.NOTDEF;
      }
      int idRangeOffset = this.idRangeOffset(segment);
      if (idRangeOffset == 0) {
        return (character + this.idDelta(segment)) % 65536;
      }
      return this.data.readUShort(
          idRangeOffset + this.idRangeOffsetLocation(segment) + 2 * (character - startCode));
    }

    /**
     * Gets the count of the number of segments in this cmap.
     *
     * @return the number of segments
     */
    public int getSegCount() {
      return segCount;
    }

    /**
     * Gets the start code for a segment.
     *
     * @param segment the segment in the look up table
     * @return the start code for the segment
     */
    public int startCode(int segment) {
      isValidIndex(segment);
      return startCode(this.data, this.segCount, segment);
    }

    private static int length(ReadableFontData data) {
      int length = data.readUShort(Offset.format4Length.offset);
      return length;
    }

    private static int segCount(ReadableFontData data) {
      int segCount = data.readUShort(Offset.format4SegCountX2.offset) / 2;
      return segCount;
    }

    private static int startCode(ReadableFontData data, int segCount, int index) {
      int startCode =
          data.readUShort(startCodeOffset(segCount) + index * FontData.DataSize.USHORT.size());
      return startCode;
    }

    private static int startCodeOffset(int segCount) {
      int startCodeOffset =
          Offset.format4EndCount.offset + FontData.DataSize.USHORT.size() + segCount
              * FontData.DataSize.USHORT.size();
      return startCodeOffset;
    }

    private static int endCode(ReadableFontData data, int segCount, int index) {
      int endCode =
          data.readUShort(Offset.format4EndCount.offset + index * FontData.DataSize.USHORT.size());
      return endCode;
    }

    private static int idDelta(ReadableFontData data, int segCount, int index) {
      int idDelta =
          data.readShort(idDeltaOffset(segCount) + index * FontData.DataSize.SHORT.size());
      return idDelta;
    }

    private static int idDeltaOffset(int segCount) {
      int idDeltaOffset =
          Offset.format4EndCount.offset + ((2 * segCount) + 1) * FontData.DataSize.USHORT.size();
      return idDeltaOffset;
    }

    private static int idRangeOffset(ReadableFontData data, int segCount, int index) {
      int idRangeOffset =
          data.readUShort(idRangeOffsetOffset(segCount) + index * FontData.DataSize.USHORT.size());
      return idRangeOffset;
    }

    private static int idRangeOffsetOffset(int segCount) {
      int idRangeOffsetOffset =
          Offset.format4EndCount.offset + ((2 * segCount) + 1) * FontData.DataSize.USHORT.size()
              + segCount * FontData.DataSize.SHORT.size();
      return idRangeOffsetOffset;
    }

    private static int glyphIdArrayOffset(int segCount) {
      int glyphIdArrayOffset =
          Offset.format4EndCount.offset + ((3 * segCount) + 1) * FontData.DataSize.USHORT.size()
              + segCount * FontData.DataSize.SHORT.size();
      return glyphIdArrayOffset;
    }

    /**
     * Gets the end code for a segment.
     *
     * @param segment the segment in the look up table
     * @return the end code for the segment
     */
    public int endCode(int segment) {
      isValidIndex(segment);
      return endCode(this.data, this.segCount, segment);
    }

    private void isValidIndex(int segment) {
      if (segment < 0 || segment >= segCount) {
        throw new IllegalArgumentException();
      }
    }

    /**
     * Gets the id delta for a segment.
     *
     * @param segment the segment in the look up table
     * @return the id delta for the segment
     */
    public int idDelta(int segment) {
      isValidIndex(segment);
      return idDelta(this.data, this.segCount, segment);
    }

    /**
     * Gets the id range offset for a segment.
     *
     * @param segment the segment in the look up table
     * @return the id range offset for the segment
     */
    public int idRangeOffset(int segment) {
      isValidIndex(segment);
      return this.data.readUShort(this.idRangeOffsetLocation(segment));
    }

    /**
     * Get the location of the id range offset for a segment.
     * @param segment the segment in the look up table
     * @return the location of the id range offset for the segment
     */
    public int idRangeOffsetLocation(int segment) {
      isValidIndex(segment);
      return idRangeOffsetOffset(this.segCount) + segment * FontData.DataSize.USHORT.size();
    }

    @SuppressWarnings("unused")
    private int glyphIdArray(int index) {
      return this.data.readUShort(
          this.glyphIdArrayOffset + index * FontData.DataSize.USHORT.size());
    }

    @Override
    public int language() {
      return this.data.readUShort(Offset.format4Language.offset);
    }

    @Override
    public Iterator<Integer> iterator() {
      return new CharacterIterator();
    }

    private class CharacterIterator implements Iterator<Integer> {
      private int segmentIndex;
      private int firstCharInSegment;
      private int lastCharInSegment;

      private int nextChar;
      private boolean nextCharSet;

      private CharacterIterator() {
        segmentIndex = 0;
        firstCharInSegment = -1;
      }

      @Override
      public boolean hasNext() {
        if (nextCharSet == true) {
          return true;
        }
        while (segmentIndex < segCount) {
          if (firstCharInSegment < 0) {
            firstCharInSegment = startCode(segmentIndex);
            lastCharInSegment = endCode(segmentIndex);
            nextChar = firstCharInSegment;
            nextCharSet = true;
            return true;
          }
          if (nextChar < lastCharInSegment) {
            nextChar++;
            nextCharSet = true;
            return true;
          }
          segmentIndex++;
          firstCharInSegment = -1;
        }
        return false;
      }

      @Override
      public Integer next() {
        if (!nextCharSet) {
          if (!hasNext()) {
            throw new NoSuchElementException("No more characters to iterate.");
          }
        }
        nextCharSet = false;
        return nextChar;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unable to remove a character from cmap.");
      }
    }

    public static class Builder extends CMap.Builder<CMapFormat4> {
      public static class Segment {
        public static List<Segment> deepCopy(List<Segment> original) {
          List<Segment> list = new ArrayList<Segment>(original.size());
          for (Segment segment : original) {
            list.add(new Segment(segment));
          }
          return list;
        }

        private int startCount;
        private int endCount;
        private int idDelta;
        private int idRangeOffset;

        public Segment() {
        }

        public Segment(Segment other) {
          this(other.startCount, other.endCount, other.idDelta, other.idRangeOffset);
        }

        public Segment(int startCount, int endCount, int idDelta, int idRangeOffset) {
          this.startCount = startCount;
          this.endCount = endCount;
          this.idDelta = idDelta;
          this.idRangeOffset = idRangeOffset;
        }

        /**
         * @return the startCount
         */
        public int getStartCount() {
          return startCount;
        }

        /**
         * @param startCount the startCount to set
         */
        public void setStartCount(int startCount) {
          this.startCount = startCount;
        }

        /**
         * @return the endCount
         */
        public int getEndCount() {
          return endCount;
        }

        /**
         * @param endCount the endCount to set
         */
        public void setEndCount(int endCount) {
          this.endCount = endCount;
        }

        /**
         * @return the idDelta
         */
        public int getIdDelta() {
          return idDelta;
        }

        /**
         * @param idDelta the idDelta to set
         */
        public void setIdDelta(int idDelta) {
          this.idDelta = idDelta;
        }

        /**
         * @return the idRangeOffset
         */
        public int getIdRangeOffset() {
          return idRangeOffset;
        }

        /**
         * @param idRangeOffset the idRangeOffset to set
         */
        public void setIdRangeOffset(int idRangeOffset) {
          this.idRangeOffset = idRangeOffset;
        }

        @Override
        public String toString() {
          return String.format("[0x%04x - 0x%04x, delta = 0x%04x, rangeOffset = 0x%04x]",
              this.startCount, this.endCount, this.idDelta, this.idRangeOffset);
        }
      }

      private List<Segment> segments;
      private List<Integer> glyphIdArray;

      protected Builder(WritableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readUShort(offset + Offset.format4Length.offset)), CMapFormat.Format4,
            cmapId);
      }

      protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readUShort(offset + Offset.format4Length.offset)), CMapFormat.Format4,
            cmapId);
      }

      private void initialize(ReadableFontData data) {
        this.segments = new ArrayList<Segment>();
        this.glyphIdArray = new ArrayList<Integer>();

        if (data == null || data.length() == 0) {
          return;
        }

        // build segments
        int segCount = CMapFormat4.segCount(data);
        for (int index = 0; index < segCount; index++) {
          Segment segment = new Segment();
          segment.setStartCount(CMapFormat4.startCode(data, segCount, index));
          segment.setEndCount(CMapFormat4.endCode(data, segCount, index));
          segment.setIdDelta(CMapFormat4.idDelta(data, segCount, index));
          segment.setIdRangeOffset(CMapFormat4.idRangeOffset(data, segCount, index));

          this.segments.add(segment);
        }

        // build glyph id array
        int glyphIdArrayLength =
            CMapFormat4.length(data) - CMapFormat4.glyphIdArrayOffset(segCount);
        for (int index = 0; index < glyphIdArrayLength; index += FontData.DataSize.USHORT.size()) {
          this.glyphIdArray.add(data.readUShort(index + CMapFormat4.glyphIdArrayOffset(segCount)));
        }
      }

      public List<Segment> getSegments() {
        if (this.segments == null) {
          this.initialize(this.internalReadData());
          this.setModelChanged();
        }
        return this.segments;
      }

      public void setSegments(List<Segment> segments) {
        this.segments = Segment.deepCopy(segments);
        this.setModelChanged();
      }

      public List<Integer> getGlyphIdArray() {
        if (this.glyphIdArray == null) {
          this.initialize(this.internalReadData());
          this.setModelChanged();
        }
        return this.glyphIdArray;
      }

      public void setGlyphIdArray(List<Integer> glyphIdArray) {
        this.glyphIdArray = new ArrayList<Integer>(glyphIdArray);
        this.setModelChanged();
      }

      @Override
      protected CMapFormat4 subBuildTable(ReadableFontData data) {
        return new CMapFormat4(data, this.cmapId());
      }

      @Override
      protected void subDataSet() {
        this.segments = null;
        this.glyphIdArray = null;
        super.setModelChanged(false);
      }

      @Override
      protected int subDataSizeToSerialize() {
        if (!this.modelChanged()) {
          return super.subDataSizeToSerialize();
        }

        int size = Offset.format4FixedSize.offset + this.segments.size()
            * (3 * FontData.DataSize.USHORT.size() + FontData.DataSize.SHORT.size())
            + this.glyphIdArray.size() * FontData.DataSize.USHORT.size();
        return size;
      }

      @Override
      protected boolean subReadyToSerialize() {
        if (!this.modelChanged()) {
          return super.subReadyToSerialize();
        }

        if (this.segments != null) {
          return true;
        }
        return false;
      }

      @Override
      protected int subSerialize(WritableFontData newData) {
        if (!this.modelChanged()) {
          return super.subSerialize(newData);
        }

        int index = 0;
        index += newData.writeUShort(index, CMapFormat.Format4.value());
        index += FontData.DataSize.USHORT.size(); // length - write this at the
                                                  // end
        index += newData.writeUShort(index, this.language());
        int segCount = this.segments.size();
        index += newData.writeUShort(index, segCount * 2);
        int log2SegCount = FontMath.log2(this.segments.size());
        int searchRange = 1 << (log2SegCount + 1);
        index += newData.writeUShort(index, searchRange);
        int entrySelector = log2SegCount;
        index += newData.writeUShort(index, entrySelector);
        int rangeShift = 2 * segCount - searchRange;
        index += newData.writeUShort(index, rangeShift);

        for (int i = 0; i < segCount; i++) {
          index += newData.writeUShort(index, this.segments.get(i).getEndCount());
        }
        index += FontData.DataSize.USHORT.size(); // reserved UShort
        for (int i = 0; i < segCount; i++) {
          index += newData.writeUShort(index, this.segments.get(i).getStartCount());
        }
        for (int i = 0; i < segCount; i++) {
          index += newData.writeShort(index, this.segments.get(i).getIdDelta());
        }
        for (int i = 0; i < segCount; i++) {
          index += newData.writeUShort(index, this.segments.get(i).getIdRangeOffset());
        }

        for (int i = 0; i < this.glyphIdArray.size(); i++) {
          index += newData.writeUShort(index, this.glyphIdArray.get(i));
        }

        newData.writeUShort(Offset.format4Length.offset, index);

        return index;
      }
    }
  }

  /**
   * A cmap format 6 sub table.
   */
  public static final class CMapFormat6 extends CMap {

    private final int firstCode;
    private final int entryCount;

    protected CMapFormat6(ReadableFontData data, CMapId cmapId) {
      super(data, CMapFormat.Format6.value, cmapId);
      this.firstCode = this.data.readUShort(Offset.format6FirstCode.offset);
      this.entryCount = this.data.readUShort(Offset.format6EntryCount.offset);
    }

    @Override
    public int glyphId(int character) {
      if (character < this.firstCode || character >= this.firstCode + this.entryCount) {
        return CMapTable.NOTDEF;
      }
      return this.data.readUShort(Offset.format6GlyphIdArray.offset + (character - this.firstCode)
          * FontData.DataSize.USHORT.size());
    }

    @Override
    public int language() {
      return this.data.readUShort(Offset.format6Language.offset);
    }

    @Override
    public Iterator<Integer> iterator() {
      return new CharacterIterator();
    }

    private class CharacterIterator implements Iterator<Integer> {
      private int character = firstCode;

      private CharacterIterator() {
        // Prevent construction.
      }

      @Override
      public boolean hasNext() {
        if (character < (firstCode + entryCount)) {
          return true;
        }
        return false;
      }

      @Override
      public Integer next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more characters to iterate.");
        }
        return this.character++;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unable to remove a character from cmap.");
      }
    }

    public static class Builder extends CMap.Builder<CMapFormat6> {
      protected Builder(WritableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readUShort(offset + Offset.format6Length.offset)), CMapFormat.Format6,
            cmapId);
      }

      protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readUShort(offset + Offset.format6Length.offset)), CMapFormat.Format6,
            cmapId);
      }

      @Override
      protected CMapFormat6 subBuildTable(ReadableFontData data) {
        return new CMapFormat6(data, this.cmapId());
      }
    }
  }

  /**
   * A cmap format 8 sub table.
   *
   */
  public static final class CMapFormat8 extends CMap {
    private final int numberOfGroups;

    protected CMapFormat8(ReadableFontData data, CMapId cmapId) {
      super(data, CMapFormat.Format8.value, cmapId);
      this.numberOfGroups = this.data.readULongAsInt(Offset.format8nGroups.offset);
    }

    private int firstChar(int groupIndex) {
      return this.readFontData().readULongAsInt(
          Offset.format8Groups.offset + groupIndex * Offset.format8Group_structLength.offset
              + Offset.format8Group_startCharCode.offset);
    }

    private int endChar(int groupIndex) {
      return this.readFontData().readULongAsInt(
          Offset.format8Groups.offset + groupIndex * Offset.format8Group_structLength.offset
              + Offset.format8Group_endCharCode.offset);
    }

    @Override
    public int glyphId(int character) {
      return this.readFontData().searchULong(Offset.format8Groups.offset
          + Offset.format8Group_startCharCode.offset,
          Offset.format8Group_structLength.offset,
          Offset.format8Groups.offset + Offset.format8Group_endCharCode.offset,
          Offset.format8Group_structLength.offset,
          numberOfGroups,
          character);
    }

    @Override
    public int language() {
      return this.data.readULongAsInt(Offset.format8Language.offset);
    }

    @Override
    public Iterator<Integer> iterator() {
      return new CharacterIterator();
    }

    private class CharacterIterator implements Iterator<Integer> {
      private int groupIndex;
      private int firstCharInGroup;
      private int endCharInGroup;

      private int nextChar;
      private boolean nextCharSet;

      private CharacterIterator() {
        groupIndex = 0;
        firstCharInGroup = -1;
      }

      @Override
      public boolean hasNext() {
        if (nextCharSet == true) {
          return true;
        }
        while (groupIndex < numberOfGroups) {
          if (firstCharInGroup < 0) {
            firstCharInGroup = firstChar(groupIndex);
            endCharInGroup = endChar(groupIndex);
            nextChar = firstCharInGroup;
            nextCharSet = true;
            return true;
          }
          if (nextChar < endCharInGroup) {
            nextChar++;
            nextCharSet = true;
            return true;
          }
          groupIndex++;
          firstCharInGroup = -1;
        }
        return false;
      }

      @Override
      public Integer next() {
        if (!nextCharSet) {
          if (!hasNext()) {
            throw new NoSuchElementException("No more characters to iterate.");
          }
        }
        nextCharSet = false;
        return nextChar;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unable to remove a character from cmap.");
      }
    }

    public static class Builder extends CMap.Builder<CMapFormat8> {
      protected Builder(WritableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readULongAsInt(offset + Offset.format8Length.offset)), CMapFormat.Format8,
            cmapId);
      }

      protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readULongAsInt(offset + Offset.format8Length.offset)), CMapFormat.Format8,
            cmapId);
      }

      @Override
      protected CMapFormat8 subBuildTable(ReadableFontData data) {
        return new CMapFormat8(data, this.cmapId());
      }
    }
  }

  /**
   * A cmap format 10 sub table.
   *
   */
  public static final class CMapFormat10 extends CMap {

    private final int startCharCode;
    private final int numChars;

    protected CMapFormat10(ReadableFontData data, CMapId cmapId) {
      super(data, CMapFormat.Format10.value, cmapId);
      this.startCharCode = this.data.readULongAsInt(Offset.format10StartCharCode.offset);
      this.numChars = this.data.readUShort(Offset.format10NumChars.offset);
    }

    @Override
    public int glyphId(int character) {
      if (character < startCharCode || character >= (startCharCode + numChars)) {
        return NOTDEF;
      }
      return this.readFontData().readUShort(character - startCharCode);
    }

    @Override
    public int language() {
      return this.data.readULongAsInt(Offset.format10Language.offset);
    }

    @Override
    public Iterator<Integer> iterator() {
      return new CharacterIterator();
    }

    private class CharacterIterator implements Iterator<Integer> {
      private int character = startCharCode;

      private CharacterIterator() {
        // Prevent construction.
      }

      @Override
      public boolean hasNext() {
        if (character < startCharCode + numChars) {
          return true;
        }
        return false;
      }

      @Override
      public Integer next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more characters to iterate.");
        }
        return this.character++;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unable to remove a character from cmap.");
      }
    }

    public static class Builder extends CMap.Builder<CMapFormat10>
    {
      protected Builder(WritableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readULongAsInt(offset + Offset.format10Length.offset)),
            CMapFormat.Format10, cmapId);
      }

      protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readULongAsInt(offset + Offset.format10Length.offset)),
            CMapFormat.Format10, cmapId);
      }

      @Override
      protected CMapFormat10 subBuildTable(ReadableFontData data) {
        return new CMapFormat10(data, this.cmapId());
      }
    }
  }

  /**
   * A cmap format 12 sub table.
   *
   */
  public static final class CMapFormat12 extends CMap {
    private final int numberOfGroups;

    protected CMapFormat12(ReadableFontData data, CMapId cmapId) {
      super(data, CMapFormat.Format12.value, cmapId);
      this.numberOfGroups = this.data.readULongAsInt(Offset.format12nGroups.offset);
    }

    private int groupStartChar(int groupIndex) {
      return this.data.readULongAsInt(
          Offset.format12Groups.offset + groupIndex * Offset.format12Groups_structLength.offset
              + Offset.format12_startCharCode.offset);
    }

    private int groupEndChar(int groupIndex) {
      return this.data.readULongAsInt(
          Offset.format12Groups.offset + groupIndex * Offset.format12Groups_structLength.offset
              + Offset.format12_endCharCode.offset);
    }

    private int groupStartGlyph(int groupIndex) {
      return this.data.readULongAsInt(
          Offset.format12Groups.offset + groupIndex * Offset.format12Groups_structLength.offset
              + Offset.format12_startGlyphId.offset);
    }

    @Override
    public int glyphId(int character) {
      int group =
          this.data.searchULong(Offset.format12Groups.offset + Offset.format12_startCharCode.offset,
              Offset.format12Groups_structLength.offset,
              Offset.format12Groups.offset + Offset.format12_endCharCode.offset,
              Offset.format12Groups_structLength.offset,
              this.numberOfGroups,
              character);
      if (group == -1) {
        return CMapTable.NOTDEF;
      }
      return groupStartGlyph(group) + (character - groupStartChar(group));
    }

    @Override
    public int language() {
      return this.data.readULongAsInt(Offset.format12Language.offset);
    }

    @Override
    public Iterator<Integer> iterator() {
      return new CharacterIterator();
    }

    private final class CharacterIterator implements Iterator<Integer> {
      private int groupIndex = 0;
      private int groupEndChar;

      private boolean nextSet = false;
      private int nextChar;

      private CharacterIterator() {
        nextChar = groupStartChar(groupIndex);
        groupEndChar = groupEndChar(groupIndex);
        nextSet = true;
      }

      @Override
      public boolean hasNext() {
        if (nextSet) {
          return true;
        }
        if (groupIndex >= numberOfGroups) {
          return false;
        }
        if (nextChar < groupEndChar) {
          nextChar++;
          nextSet = true;
          return true;
        }
        groupIndex++;
        if (groupIndex < numberOfGroups) {
          nextSet = true;
          nextChar = groupStartChar(groupIndex);
          groupEndChar = groupEndChar(groupIndex);
          return true;
        }
        return false;
      }

      @Override
      public Integer next() {
        if (!this.nextSet) {
          if (!hasNext()) {
            throw new NoSuchElementException("No more characters to iterate.");
          }
        }
        this.nextSet = false;
        return nextChar;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unable to remove a character from cmap.");
      }
    }

    public static class Builder extends CMap.Builder<CMapFormat12> {
      protected Builder(WritableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readULongAsInt(offset + Offset.format12Length.offset)),
            CMapFormat.Format12, cmapId);
      }

      protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readULongAsInt(offset + Offset.format12Length.offset)),
            CMapFormat.Format12, cmapId);
      }

      @Override
      protected CMapFormat12 subBuildTable(ReadableFontData data) {
        return new CMapFormat12(data, this.cmapId());
      }
    }
  }

  /**
   * A cmap format 13 sub table.
   */
  public static final class CMapFormat13 extends CMap {
    private final int numberOfGroups;

    protected CMapFormat13(ReadableFontData data, CMapId cmapId) {
      super(data, CMapFormat.Format12.value, cmapId);
      this.numberOfGroups = this.data.readULongAsInt(Offset.format12nGroups.offset);
    }

    private int groupStartChar(int groupIndex) {
      return this.data.readULongAsInt(
          Offset.format13Groups.offset + groupIndex * Offset.format13Groups_structLength.offset
              + Offset.format13_startCharCode.offset);
    }

    private int groupEndChar(int groupIndex) {
      return this.data.readULongAsInt(
          Offset.format13Groups.offset + groupIndex * Offset.format13Groups_structLength.offset
              + Offset.format13_endCharCode.offset);
    }

    private int groupGlyph(int groupIndex) {
      return this.data.readULongAsInt(
          Offset.format13Groups.offset + groupIndex * Offset.format13Groups_structLength.offset
              + Offset.format13_glyphId.offset);
    }

    @Override
    public int glyphId(int character) {
      int group = this.data.searchULong(
          Offset.format13Groups.offset + Offset.format13_startCharCode.offset,
          Offset.format13Groups_structLength.offset,
          Offset.format13Groups.offset + Offset.format13_endCharCode.offset,
          Offset.format13Groups_structLength.offset,
          this.numberOfGroups,
          character);
      if (group == -1) {
        return CMapTable.NOTDEF;
      }
      return groupGlyph(group);
    }

    @Override
    public int language() {
      return this.data.readULongAsInt(Offset.format12Language.offset);
    }

    @Override
    public Iterator<Integer> iterator() {
      return new CharacterIterator();
    }

    private final class CharacterIterator implements Iterator<Integer> {
      private int groupIndex = 0;
      private int groupEndChar;

      private boolean nextSet = false;
      private int nextChar;

      private CharacterIterator() {
        nextChar = groupStartChar(groupIndex);
        groupEndChar = groupEndChar(groupIndex);
        nextSet = true;
      }

      @Override
      public boolean hasNext() {
        if (nextSet) {
          return true;
        }
        if (groupIndex >= numberOfGroups) {
          return false;
        }
        if (nextChar < groupEndChar) {
          nextChar++;
          nextSet = true;
          return true;
        }
        groupIndex++;
        if (groupIndex < numberOfGroups) {
          nextSet = true;
          nextChar = groupStartChar(groupIndex);
          groupEndChar = groupEndChar(groupIndex);
          return true;
        }
        return false;
      }

      @Override
      public Integer next() {
        if (!this.nextSet) {
          if (!hasNext()) {
            throw new NoSuchElementException("No more characters to iterate.");
          }
        }
        this.nextSet = false;
        return nextChar;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unable to remove a character from cmap.");
      }
    }

    public static class Builder extends CMap.Builder<CMapFormat13> {
      protected Builder(WritableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readULongAsInt(offset + Offset.format13Length.offset)),
            CMapFormat.Format13, cmapId);
      }

      protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readULongAsInt(offset + Offset.format13Length.offset)),
            CMapFormat.Format13, cmapId);
      }

      @Override
      protected CMapFormat13 subBuildTable(ReadableFontData data) {
        return new CMapFormat13(data, this.cmapId());
      }
    }
  }

  /**
   * A cmap format 14 sub table.
   */
  // TODO(user): completely unsupported yet
  public static final class CMapFormat14 extends CMap {

    protected CMapFormat14(ReadableFontData data, CMapId cmapId) {
      super(data, CMapFormat.Format14.value, cmapId);
    }

    @Override
    public int glyphId(int character) {
      return CMapTable.NOTDEF;
    }

    @Override
    public int language() {
      return 0;
    }

    @Override
    public Iterator<Integer> iterator() {
      return null;
    }

    public static class Builder extends CMap.Builder<CMapFormat14> {
      protected Builder(WritableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readULongAsInt(offset + Offset.format14Length.offset)),
            CMapFormat.Format14, cmapId);
      }

      protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
        super(data == null ? null : data.slice(
            offset, data.readULongAsInt(offset + Offset.format14Length.offset)),
            CMapFormat.Format14, cmapId);
      }

      @Override
      protected CMapFormat14 subBuildTable(ReadableFontData data) {
        return new CMapFormat14(data, this.cmapId());
      }
    }
  }
}

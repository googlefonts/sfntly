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
import com.google.typography.font.sfntly.Font.WindowsEncodingId;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.SubTableContainerTable;
import com.google.typography.font.sfntly.table.core.CMap.CMapFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
  private interface HeaderOffsets {
    int version = 0;
    int numTables = 2;
    int SIZE = 4;
  }

  private interface EncodingRecord {
    int platformId = 0;
    int encodingId = 2;
    int offset = 4;
    int SIZE = 8;
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
      CMapId other = (CMapId) obj;
      return this.platformId == other.platformId
          && this.encodingId == other.encodingId;
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
      return String.format("pid = %d, eid = %d", this.platformId, this.encodingId);
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
    return this.data.readUShort(HeaderOffsets.version);
  }

  /**
   * Gets the number of cmaps within the CMap table.
   *
   * @return the number of cmaps
   */
  public int numCMaps() {
    return this.data.readUShort(HeaderOffsets.numTables);
  }

  /**
   * Returns the index of the cmap with the given CMapId in the table or -1 if a cmap with the
   * CMapId does not exist in the table.
   *
   * @param id the id of the cmap to get the index for; this value cannot be null
   * @return the index of the cmap in the table or -1 if the cmap with the CMapId does not exist in
   *         the table
   */
  // TODO Modify the iterator to be index-based and used here
  public int getCmapIndex(CMapId id) {
    for (int index = 0; index < numCMaps(); index++) {
      if (id.equals(cmapId(index))) {
        return index;
      }
    }

    return -1;
  }

  /**
   * Gets the offset in the table data for the encoding record for the cmap with
   * the given index. The offset is from the beginning of the table.
   *
   * @param index the index of the cmap
   * @return offset in the table data
   */
  private static int offsetForEncodingRecord(int index) {
    return HeaderOffsets.SIZE + index * EncodingRecord.SIZE;
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
    return this.data.readUShort(offsetForEncodingRecord(index) + EncodingRecord.platformId);
  }

  /**
   * Gets the encoding id for the cmap with the given index.
   *
   * @param index the index of the cmap
   * @return the encoding id
   */
  public int encodingId(int index) {
    return this.data.readUShort(offsetForEncodingRecord(index) + EncodingRecord.encodingId);
  }

  /**
   * Gets the offset in the table data for the cmap table with the given index.
   * The offset is from the beginning of the table.
   *
   * @param index the index of the cmap
   * @return the offset in the table data
   */
  public int offset(int index) {
    return this.data.readULongAsInt(offsetForEncodingRecord(index) + EncodingRecord.offset);
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
      sb.append(String.format("[%#x = %s]", this.offset(i), this.cmap(i)));
      if (i < this.numCMaps() - 1) {
        sb.append(", ");
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
        return this.tableIndex < numCMaps();
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
      return cmap(this.tableIndex++);
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
   */
  public CMap cmap(int index) {
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
        return cmapId.equals(foundCMapId);
      }
    });
    // can only be one cmap for each set of ids
    if (cmapIter.hasNext()) {
      return cmapIter.next();
    }
    return null;
  }

  /**
   * CMap Table Builder.
   *
   */
  public static class Builder extends SubTableContainerTable.Builder<CMapTable> {

    private int version = 0; // TODO(stuartg): make a CMapTable constant
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
      int platformId = data.readUShort(offsetForEncodingRecord(index) + EncodingRecord.platformId);
      int encodingId = data.readUShort(offsetForEncodingRecord(index) + EncodingRecord.encodingId);
      int offset = data.readULongAsInt(offsetForEncodingRecord(index) + EncodingRecord.offset);
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

    private Map<CMapId, CMap.Builder<? extends CMap>> getCMapBuilders() {
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
      return data.readUShort(HeaderOffsets.numTables);
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
      int size = HeaderOffsets.SIZE + this.cmapBuilders.size() * EncodingRecord.SIZE;

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
      int size = newData.writeUShort(HeaderOffsets.version, this.version());
      size += newData.writeUShort(HeaderOffsets.numTables, this.cmapBuilders.size());

      int indexOffset = size;
      size += this.cmapBuilders.size() * EncodingRecord.SIZE;
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
     */
    public CMap.Builder<? extends CMap> newCMapBuilder(CMapId cmapId, ReadableFontData data)
        throws IOException {
      WritableFontData wfd = WritableFontData.createWritableFontData(data.size());
      data.copyTo(wfd);
      CMap.Builder<? extends CMap> builder = CMap.Builder.getBuilder(wfd, 0, cmapId);
      Map<CMapId, CMap.Builder<? extends CMap>> cmapBuilders = this.getCMapBuilders();
      cmapBuilders.put(cmapId, builder);
      return builder;
    }

    public CMap.Builder<? extends CMap> newCMapBuilder(CMapId cmapId, CMapFormat cmapFormat) {
      CMap.Builder<? extends CMap> builder = CMap.Builder.getBuilder(cmapFormat, cmapId);
      Map<CMapId, CMap.Builder<? extends CMap>> cmapBuilders = this.getCMapBuilders();
      cmapBuilders.put(cmapId, builder);
      return builder;
    }

    public CMap.Builder<? extends CMap> cmapBuilder(CMapId cmapId) {
      Map<CMapId, CMap.Builder<? extends CMap>> cmapBuilders = this.getCMapBuilders();
      return cmapBuilders.get(cmapId);
    }

  }
}

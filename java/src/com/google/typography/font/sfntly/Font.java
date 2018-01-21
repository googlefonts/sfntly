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

package com.google.typography.font.sfntly;

import com.google.typography.font.sfntly.data.FontInputStream;
import com.google.typography.font.sfntly.data.FontOutputStream;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.math.Fixed1616;
import com.google.typography.font.sfntly.math.FontMath;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.sfntly.table.core.FontHeaderTable;
import com.google.typography.font.sfntly.table.core.HorizontalDeviceMetricsTable;
import com.google.typography.font.sfntly.table.core.HorizontalHeaderTable;
import com.google.typography.font.sfntly.table.core.HorizontalMetricsTable;
import com.google.typography.font.sfntly.table.core.MaximumProfileTable;
import com.google.typography.font.sfntly.table.core.NameTable;
import com.google.typography.font.sfntly.table.truetype.LocaTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * An sfnt container font object.
 * This object is immutable and thread safe.
 * To construct one, use an instance of {@link Font.Builder}.
 *
 * @author Stuart Gill
 */
public class Font {

  private static final Logger logger = Logger.getLogger(Font.class.getCanonicalName());

  // Offsets within the main directory
  private interface HeaderOffset {
    int sfntVersion = 0;
    int numTables = 4;
    int searchRange = 6;
    int entrySelector = 8;
    int rangeShift = 10;
    int SIZE = 12;
  }

  // Offsets within a specific table record
  private interface TableOffset {
    int tag = 0;
    int checkSum = 4;
    int offset = 8;
    int length = 12;
    int SIZE = 16;
  }

  /**
   * Ordering of tables for different font types.
   */
  private static final List<Integer> CFF_TABLE_ORDERING;
  private static final List<Integer> TRUE_TYPE_TABLE_ORDERING;
  static {
    Integer[] cffArray = new Integer[] {Tag.head,
        Tag.hhea,
        Tag.maxp,
        Tag.OS_2,
        Tag.name,
        Tag.cmap,
        Tag.post,
        Tag.CFF};
    List<Integer> cffList = new ArrayList<Integer>(cffArray.length);
    Collections.addAll(cffList, cffArray);
    CFF_TABLE_ORDERING = Collections.unmodifiableList(cffList);

    Integer[] ttArray = new Integer[] {Tag.head,
        Tag.hhea,
        Tag.maxp,
        Tag.OS_2,
        Tag.hmtx,
        Tag.LTSH,
        Tag.VDMX,
        Tag.hdmx,
        Tag.cmap,
        Tag.fpgm,
        Tag.prep,
        Tag.cvt,
        Tag.loca,
        Tag.glyf,
        Tag.kern,
        Tag.name,
        Tag.post,
        Tag.gasp,
        Tag.PCLT,
        Tag.DSIG};
    List<Integer> ttList = new ArrayList<Integer>(ttArray.length);
    Collections.addAll(ttList, ttArray);
    TRUE_TYPE_TABLE_ORDERING = Collections.unmodifiableList(ttList);
  }

  /**
   * Platform ids. These are used in a number of places within the font whenever
   * the platform needs to be specified.
   *
   * @see NameTable
   * @see CMapTable
   */
  public enum PlatformId {
    Unknown(-1), Unicode(0), Macintosh(1), ISO(2), Windows(3), Custom(4);

    private final int value;

    private PlatformId(int value) {
      this.value = value;
    }

    public int value() {
      return this.value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static PlatformId valueOf(int value) {
      for (PlatformId platform : PlatformId.values()) {
        if (platform.equals(value)) {
          return platform;
        }
      }
      return Unknown;
    }
  }

  /**
   * Unicode encoding ids. These are used in a number of places within the font
   * whenever character encodings need to be specified.
   *
   * @see NameTable
   * @see CMapTable
   */
  public enum UnicodeEncodingId {
    // Unicode Platform Encodings
    Unknown(-1),
    Unicode1_0(0),
    Unicode1_1(1),
    ISO10646(2),
    Unicode2_0_BMP(3),
    Unicode2_0(4),
    UnicodeVariationSequences(5);

    private final int value;

    private UnicodeEncodingId(int value) {
      this.value = value;
    }

    public int value() {
      return this.value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static UnicodeEncodingId valueOf(int value) {
      for (UnicodeEncodingId encoding : UnicodeEncodingId.values()) {
        if (encoding.equals(value)) {
          return encoding;
        }
      }
      return Unknown;
    }
  }

  /**
   * Windows encoding ids. These are used in a number of places within the font
   * whenever character encodings need to be specified.
   *
   * @see NameTable
   * @see CMapTable
   */
  public enum WindowsEncodingId {
    // Windows Platform Encodings
    Unknown(-1),
    Symbol(0),
    UnicodeUCS2(1),
    ShiftJIS(2),
    PRC(3),
    Big5(4),
    Wansung(5),
    Johab(6),
    UnicodeUCS4(10);

    private final int value;

    private WindowsEncodingId(int value) {
      this.value = value;
    }

    public int value() {
      return this.value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static WindowsEncodingId valueOf(int value) {
      for (WindowsEncodingId encoding : WindowsEncodingId.values()) {
        if (encoding.equals(value)) {
          return encoding;
        }
      }
      return Unknown;
    }
  }

  /**
   * Macintosh encoding ids. These are used in a number of places within the
   * font whenever character encodings need to be specified.
   *
   * @see NameTable
   * @see CMapTable
   */
  public enum MacintoshEncodingId {
    // Macintosh Platform Encodings
    Unknown(-1),
    Roman(0),
    Japanese(1),
    ChineseTraditional(2),
    Korean(3),
    Arabic(4),
    Hebrew(5),
    Greek(6),
    Russian(7),
    RSymbol(8),
    Devanagari(9),
    Gurmukhi(10),
    Gujarati(11),
    Oriya(12),
    Bengali(13),
    Tamil(14),
    Telugu(15),
    Kannada(16),
    Malayalam(17),
    Sinhalese(18),
    Burmese(19),
    Khmer(20),
    Thai(21),
    Laotian(22),
    Georgian(23),
    Armenian(24),
    ChineseSimplified(25),
    Tibetan(26),
    Mongolian(27),
    Geez(28),
    Slavic(29),
    Vietnamese(30),
    Sindhi(31),
    Uninterpreted(32);

    private final int value;

    private MacintoshEncodingId(int value) {
      this.value = value;
    }

    public int value() {
      return this.value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static MacintoshEncodingId valueOf(int value) {
      for (MacintoshEncodingId encoding : MacintoshEncodingId.values()) {
        if (encoding.equals(value)) {
          return encoding;
        }
      }
      return Unknown;
    }
  }

  public static final int SFNTVERSION_1 = Fixed1616.fixed(1, 0);

  private final int sfntVersion;
  private final byte[] digest;
  private long checksum;

  private Map<Integer, ? extends Table> tables; // these get set in the builder

  /**
   * @param digest the computed digest for the font; null if digest was not
   *        computed
   */
  private Font(int sfntVersion, byte[] digest) {
    this.sfntVersion = sfntVersion;
    this.digest = digest;
  }

  /**
   * Gets the sfnt version set in the sfnt wrapper of the font.
   *
   * @return the sfnt version
   */
  public int sfntVersion() {
    return this.sfntVersion;
  }

  /**
   * Gets a copy of the fonts digest that was created when the font was read. If
   * no digest was set at creation time then the return result will be null.
   *
   * @return a copy of the digest array or <code>null</code> if one wasn't set
   *         at creation time
   */
  public byte[] digest() {
    if (this.digest == null) {
      return null;
    }
    return Arrays.copyOf(this.digest, this.digest.length);
  }

  /**
   * Get the checksum for this font.
   *
   * @return the font checksum
   */
  public long checksum() {
    return this.checksum;
  }

  /**
   * Get the number of tables in this font.
   *
   * @return the number of tables
   */
  public int numTables() {
    return this.tables.size();
  }

  /**
   * Get an iterator over all the tables in the font.
   *
   * @return a table iterator
   */
  public Iterator<? extends Table> iterator() {
    return this.tables.values().iterator();
  }

  /**
   * Does the font have a particular table.
   *
   * @param tag the table identifier
   * @return true if the table is in the font; false otherwise
   */
  public boolean hasTable(int tag) {
    return this.tables.containsKey(tag);
  }

  /**
   * Get the table in this font with the specified id.
   *
   * @param tag the identifier of the table
   * @return the table specified if it exists; null otherwise
   */
  @SuppressWarnings("unchecked")
  public <T extends Table> T getTable(int tag) {
    return (T) this.tables.get(tag);
  }

  /**
   * Get a map of the tables in this font accessed by table tag.
   *
   * @return an unmodifiable view of the tables in this font
   */
  public Map<Integer, ? extends Table> tableMap() {
    return Collections.unmodifiableMap(this.tables);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    byte[] digest = this.digest();
    if (digest != null) {
      sb.append("digest = ");
      for (byte b : digest) {
        int d = 0xff & b;
        if (d < 0x10) {
          sb.append("0");
        }
        sb.append(Integer.toHexString(d));
      }
      sb.append("\n");
    }

    sb.append("[");
    sb.append(Fixed1616.toString(this.sfntVersion));
    sb.append(", ");
    sb.append(this.numTables());
    sb.append("]\n");

    for (Table table : this.tables.values()) {
      sb.append("\t");
      sb.append(table);
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * Serialize the font to the output stream.
   *
   * @param os the destination for the font serialization
   * @param tableOrdering the table ordering to apply
   */
  void serialize(OutputStream os, List<Integer> tableOrdering) throws IOException {
    List<Integer> finalTableOrdering = this.generateTableOrdering(tableOrdering);
    List<Header> tableRecords = buildTableHeadersForSerialization(finalTableOrdering);
    FontOutputStream fos = new FontOutputStream(os);
    this.serializeHeader(fos, tableRecords);
    this.serializeTables(fos, tableRecords);
  }

  /**
   * Build the table headers to be used for serialization. These headers will be
   * filled out with the data required for serialization. The headers will be
   * sorted in the order specified and only those specified will have headers
   * generated.
   *
   * @param tableOrdering the tables to generate headers for and the order to
   *        sort them
   * @return a list of table headers ready for serialization
   */
  private List<Header> buildTableHeadersForSerialization(List<Integer> tableOrdering) {
    List<Integer> finalTableOrdering = this.generateTableOrdering(tableOrdering);

    List<Header> tableHeaders = new ArrayList<Header>(this.numTables());
    int tableOffset = HeaderOffset.SIZE + this.numTables() * TableOffset.SIZE;
    for (Integer tag : finalTableOrdering) {
      Table table = this.tables.get(tag);
      if (table != null) {
        tableHeaders.add(new Header(
            tag, table.calculatedChecksum(), tableOffset, table.header().length()));
        // write on boundary of 4 bytes
        tableOffset += (table.dataLength() + 3) & ~3;
      }
    }
    return tableHeaders;
  }

  /**
   * Searialize the headers.
   *
   * @param fos the destination stream for the headers
   * @param tableHeaders the headers to serialize
   */
  private void serializeHeader(FontOutputStream fos, List<Header> tableHeaders)
      throws IOException {
    fos.writeFixed(this.sfntVersion);
    fos.writeUShort(tableHeaders.size());
    int log2OfMaxPowerOf2 = FontMath.log2(tableHeaders.size());
    int searchRange = 2 << (log2OfMaxPowerOf2 - 1 + 4);
    fos.writeUShort(searchRange);
    fos.writeUShort(log2OfMaxPowerOf2);
    fos.writeUShort((tableHeaders.size() * 16) - searchRange);

    List<Header> sortedHeaders = new ArrayList<Header>(tableHeaders);
    Collections.sort(sortedHeaders, Header.COMPARATOR_BY_TAG);

    for (Header record : sortedHeaders) {
      fos.writeULong(record.tag());
      fos.writeULong(record.checksum());
      fos.writeULong(record.offset());
      fos.writeULong(record.length());
    }
  }

  /**
   * Serialize the tables.
   *
   * @param fos the destination stream for the headers
   * @param tableHeaders the headers for the tables to serialize
   */
  private void serializeTables(FontOutputStream fos, List<Header> tableHeaders)
      throws IOException {

    for (Header record : tableHeaders) {
      Table table = this.getTable(record.tag());
      if (table == null) {
        throw new IOException("Table out of sync with font header.");
      }
      int tableSize = table.serialize(fos);
      int fillerSize = ((tableSize + 3) & ~3) - tableSize;
      for (int i = 0; i < fillerSize; i++) {
        fos.write(0);
      }
    }
  }

  /**
   * Generate the full table ordering to used for serialization. The full
   * ordering uses the partial ordering as a seed and then adds all remaining
   * tables in the font in an undefined order.
   *
   * @param defaultTableOrdering the partial ordering to be used as a seed for
   *        the full ordering
   * @return the full ordering for serialization
   */
  private List<Integer> generateTableOrdering(List<Integer> defaultTableOrdering) {
    List<Integer> tableOrdering = new ArrayList<Integer>(this.tables.size());
    if (defaultTableOrdering == null) {
      defaultTableOrdering = defaultTableOrdering();
    }

    Set<Integer> tablesInFont = new TreeSet<Integer>(this.tables.keySet());

    // add all the default ordering
    for (Integer tag : defaultTableOrdering) {
      if (this.hasTable(tag)) {
        tableOrdering.add(tag);
        tablesInFont.remove(tag);
      }
    }

    // add all the rest
    for (Integer tag : tablesInFont) {
      tableOrdering.add(tag);
    }

    return tableOrdering;
  }

  /**
   * Get the default table ordering based on the type of the font.
   *
   * @return the default table ordering
   */
  private List<Integer> defaultTableOrdering() {
    if (this.hasTable(Tag.CFF)) {
      return Font.CFF_TABLE_ORDERING;
    }
    return Font.TRUE_TYPE_TABLE_ORDERING;
  }

  /**
   * A builder for a font object. The builder allows the for the creation of
   * immutable {@link Font} objects. The builder is a one use non-thread safe
   * object and cnce the {@link Font} object has been created it is no longer
   * usable. To create a further {@link Font} object new builder will be
   * required.
   *
   * @author Stuart Gill
   */
  public static final class Builder {

    private Map<Integer, Table.Builder<? extends Table>> tableBuilders;
    private FontFactory factory;
    private int sfntVersion = SFNTVERSION_1;
    private int numTables;
    @SuppressWarnings("unused")
    private int searchRange;
    @SuppressWarnings("unused")
    private int entrySelector;
    @SuppressWarnings("unused")
    private int rangeShift;
    private Map<Header, WritableFontData> dataBlocks;
    private byte[] digest;

    private Builder(FontFactory factory) {
      this.factory = factory;
      this.tableBuilders = new HashMap<Integer, Table.Builder<? extends Table>>();
    }

    private void loadFont(InputStream is) throws IOException {
      if (is == null) {
        throw new IOException("No input stream for font.");
      }
      FontInputStream fontIS = new FontInputStream(is);
      try {
        SortedSet<Header> headers = readHeader(fontIS);
        this.dataBlocks = loadTableData(headers, fontIS);
        this.tableBuilders = buildAllTableBuilders(this.dataBlocks);
      } finally {
        fontIS.close();
      }
    }

    private void loadFont(WritableFontData wfd, int offsetToOffsetTable) throws IOException {
      if (wfd == null) {
        throw new IOException("No data for font.");
      }
      SortedSet<Header> records = readHeader(wfd, offsetToOffsetTable);
      this.dataBlocks = loadTableData(records, wfd);
      this.tableBuilders = buildAllTableBuilders(this.dataBlocks);
    }

    static Builder getOTFBuilder(FontFactory factory, InputStream is) throws IOException {
      Builder builder = new Builder(factory);
      builder.loadFont(is);
      return builder;
    }

    static Builder getOTFBuilder(
        FontFactory factory, WritableFontData wfd, int offsetToOffsetTable) throws IOException {
      Builder builder = new Builder(factory);
      builder.loadFont(wfd, offsetToOffsetTable);
      return builder;
    }

    static Builder getOTFBuilder(FontFactory factory) {
      return new Builder(factory);
    }

    /**
     * Get the font factory that created this font builder.
     *
     * @return the font factory
     */
    public FontFactory getFontFactory() {
      return this.factory;
    }

    /**
     * Is the font ready to build?
     *
     * @return true if ready to build; false otherwise
     */
    public boolean readyToBuild() {
      // just read in data with no manipulation
      if (this.tableBuilders == null && this.dataBlocks != null && this.dataBlocks.size() > 0) {
        return true;
      }

      for (Table.Builder<? extends Table> tableBuilder : this.tableBuilders.values()) {
        if (!tableBuilder.readyToBuild()) {
          return false;
        }
      }
      return true;
    }

    /**
     * Build the {@link Font}. After this call this builder will no longer be
     * usable.
     */
    public Font build() {
      Map<Integer, ? extends Table> tables = null;

      Font font = new Font(this.sfntVersion, this.digest);

      if (this.tableBuilders.size() > 0) {
        tables = buildTablesFromBuilders(font, this.tableBuilders);
      }
      font.tables = tables;
      this.tableBuilders = null;
      this.dataBlocks = null;
      return font;
    }

    /**
     * Set a unique fingerprint for the font object.
     */
    public void setDigest(byte[] digest) {
      this.digest = digest;
    }

    /**
     * Clear all table builders.
     */
    public void clearTableBuilders() {
      this.tableBuilders.clear();
    }

    /**
     * Does this font builder have the specified table builder?
     */
    public boolean hasTableBuilder(int tableBuilderTag) {
      return this.tableBuilders.containsKey(tableBuilderTag);
    }

    /**
     * Get the table builder for the given tag. If there is no builder for that
     * tag then return a null.
     */
    public Table.Builder<? extends Table> getTableBuilder(int tableBuilderTag) {
      Table.Builder<? extends Table> builder = this.tableBuilders.get(tableBuilderTag);
      return builder;
    }

    /**
     * Creates a new empty table builder for the table type given by the table
     * id tag.
     *
     *  This new table will be added to the font and will replace any existing
     * builder for that table.
     *
     * @return new empty table of the type specified by tag; if tag is not known
     *         then a generic OpenTypeTable is returned
     */
    public Table.Builder<? extends Table> newTableBuilder(int tag) {
      Header header = new Header(tag);
      Table.Builder<? extends Table> builder = Table.Builder.getBuilder(header, null);
      this.tableBuilders.put(header.tag(), builder);

      return builder;
    }

    /**
     * Creates a new table builder for the table type given by the table id tag.
     * It makes a copy of the data provided and uses that copy for the table.
     *
     *  This new table has been added to the font and will replace any existing
     * builder for that table.
     *
     * @return new empty table of the type specified by tag; if tag is not known
     *         then a generic OpenTypeTable is returned
     */
    public Table.Builder<? extends Table> newTableBuilder(int tag, ReadableFontData srcData) {
      WritableFontData data;
      data = WritableFontData.createWritableFontData(srcData.length());
      // TODO(stuartg): take over original data instead?
      srcData.copyTo(data);

      Header header = new Header(tag, data.length());
      Table.Builder<? extends Table> builder = Table.Builder.getBuilder(header, data);

      this.tableBuilders.put(tag, builder);

      return builder;
    }

    /**
     * Get a map of the table builders in this font builder accessed by table
     * tag.
     *
     * @return an unmodifiable view of the table builders in this font builder
     */
    public Map<Integer, Table.Builder<? extends Table>> tableBuilderMap() {
      return Collections.unmodifiableMap(this.tableBuilders);
    }

    /**
     * @return the removed table builder
     */
    public Table.Builder<? extends Table> removeTableBuilder(int tag) {
      return this.tableBuilders.remove(tag);
    }

    public int tableBuilderCount() {
      return this.tableBuilders.size();
    }

    @SuppressWarnings("unused")
    private int sfntWrapperSize() {
      return HeaderOffset.SIZE + this.tableBuilders.size() * TableOffset.SIZE;
    }

    private Map<Integer, Table.Builder<? extends Table>> buildAllTableBuilders(
        Map<Header, WritableFontData> tableData) {
      Map<Integer, Table.Builder<? extends Table>> builderMap =
        new HashMap<Integer, Table.Builder<? extends Table>>();
      Set<Header> records = tableData.keySet();
      for (Header record : records) {
        Table.Builder<? extends Table> builder = getTableBuilder(record, tableData.get(record));
        builderMap.put(record.tag(), builder);
      }
      interRelateBuilders(builderMap);
      return builderMap;
    }

    private Table.Builder<? extends Table> getTableBuilder(Header header, WritableFontData data) {
      Table.Builder<? extends Table> builder = Table.Builder.getBuilder(header, data);
      return builder;
    }

    private static Map<Integer, Table> buildTablesFromBuilders(Font font,
        Map<Integer, Table.Builder<? extends Table>> builderMap) {
      Map<Integer, Table> tableMap = new TreeMap<Integer, Table>();

      interRelateBuilders(builderMap);

      long fontChecksum = 0;
      boolean tablesChanged = false;
      FontHeaderTable.Builder headerTableBuilder = null;

      // now build all the tables
      for (Table.Builder<? extends Table> builder : builderMap.values()) {
        Table table = null;
        if (Tag.isHeaderTable(builder.header().tag())) {
          headerTableBuilder = (FontHeaderTable.Builder) builder;
          continue;
        }
        if (builder.readyToBuild()) {
          tablesChanged |= builder.changed();
          table = builder.build();
        }
        if (table == null) {
          throw new RuntimeException("Unable to build table - " + builder);
        }
        long tableChecksum = table.calculatedChecksum();
        fontChecksum += tableChecksum;
        tableMap.put(table.header().tag(), table);
      }

      // now fix up the header table
      Table headerTable = null;
      if (headerTableBuilder != null) {
        if (tablesChanged) {
          headerTableBuilder.setFontChecksum(fontChecksum);
        }
        if (headerTableBuilder.readyToBuild()) {
          tablesChanged |= headerTableBuilder.changed();
          headerTable = headerTableBuilder.build();
        }
        if (headerTable == null) {
          throw new RuntimeException("Unable to build table - " + headerTableBuilder);
        }
        fontChecksum += headerTable.calculatedChecksum();
        tableMap.put(headerTable.header().tag(), headerTable);
      }

      font.checksum = fontChecksum & 0xffffffffL;
      return tableMap;
    }

    private static void
    interRelateBuilders(Map<Integer, Table.Builder<? extends Table>> builderMap) {
      FontHeaderTable.Builder headerTableBuilder =
        (FontHeaderTable.Builder) builderMap.get(Tag.head);
      HorizontalHeaderTable.Builder horizontalHeaderBuilder =
        (HorizontalHeaderTable.Builder) builderMap.get(Tag.hhea);
      MaximumProfileTable.Builder maxProfileBuilder =
        (MaximumProfileTable.Builder) builderMap.get(Tag.maxp);
      LocaTable.Builder locaTableBuilder =
        (LocaTable.Builder) builderMap.get(Tag.loca);
      HorizontalMetricsTable.Builder horizontalMetricsBuilder =
        (HorizontalMetricsTable.Builder) builderMap.get(Tag.hmtx);
      HorizontalDeviceMetricsTable.Builder hdmxTableBuilder =
        (HorizontalDeviceMetricsTable.Builder) builderMap.get(Tag.hdmx);

      // set the inter table data required to build certain tables
      if (horizontalMetricsBuilder != null) {
        if (maxProfileBuilder != null) {
          horizontalMetricsBuilder.setNumGlyphs(maxProfileBuilder.numGlyphs());
        }
        if (horizontalHeaderBuilder != null) {
          horizontalMetricsBuilder.setNumberOfHMetrics(
              horizontalHeaderBuilder.numberOfHMetrics());
        }
      }

      if (locaTableBuilder != null) {
        if (maxProfileBuilder != null) {
          locaTableBuilder.setNumGlyphs(maxProfileBuilder.numGlyphs());
        }
        if (headerTableBuilder != null) {
          locaTableBuilder.setFormatVersion(headerTableBuilder.indexToLocFormat());
        }
      }

      if (hdmxTableBuilder != null) {
        if (maxProfileBuilder != null) {
          hdmxTableBuilder.setNumGlyphs(maxProfileBuilder.numGlyphs());
        }
      }
    }

    private SortedSet<Header> readHeader(FontInputStream is) throws IOException {
      this.sfntVersion = is.readFixed();
      this.numTables = is.readUShort();
      this.searchRange = is.readUShort();
      this.entrySelector = is.readUShort();
      this.rangeShift = is.readUShort();

      if (this.sfntVersion != SFNTVERSION_1 && this.sfntVersion != 0x4F54544F /* OTTO */) {
        String msg = String.format("Wrong sfntVersion 0x%08x, must be 0x%#08x", this.sfntVersion, SFNTVERSION_1);
        throw new IllegalStateException(msg);
      }

      SortedSet<Header> records = new TreeSet<Header>(Header.COMPARATOR_BY_OFFSET);
      for (int tableNumber = 0; tableNumber < this.numTables; tableNumber++) {
        int tag = is.readULongAsInt();
        long checksum = is.readULong();
        int offset = is.readULongAsInt();
        int length = is.readULongAsInt();
        records.add(new Header(tag, checksum, offset, length));
      }
      return records;
    }

    private Map<Header, WritableFontData> loadTableData(
        SortedSet<Header> headers, FontInputStream is) throws IOException {
      Map<Header, WritableFontData> tableData =
          new HashMap<Header, WritableFontData>(headers.size());
      logger.fine("########  Reading Table Data");
      for (Header tableHeader : headers) {
        is.skip(tableHeader.offset() - is.position());
        logger.finer("\t" + tableHeader);
        logger.finest("\t\tStream Position = " + Integer.toHexString((int) is.position()));
        // don't close this or the whole stream is gone
        FontInputStream tableIS = new FontInputStream(is, tableHeader.length());
        // TODO(stuartg): start tracking bad tables and other errors
        WritableFontData data = WritableFontData.createWritableFontData(tableHeader.length());
        data.copyFrom(tableIS, tableHeader.length());
        tableData.put(tableHeader, data);
      }
      return tableData;
    }

    private SortedSet<Header> readHeader(ReadableFontData fd, int offset) {

      this.sfntVersion = fd.readFixed(offset + HeaderOffset.sfntVersion);
      this.numTables = fd.readUShort(offset + HeaderOffset.numTables);
      this.searchRange = fd.readUShort(offset + HeaderOffset.searchRange);
      this.entrySelector = fd.readUShort(offset + HeaderOffset.entrySelector);
      this.rangeShift = fd.readUShort(offset + HeaderOffset.rangeShift);

      SortedSet<Header> records = new TreeSet<Header>(Header.COMPARATOR_BY_OFFSET);
      int tableOffset = offset + HeaderOffset.SIZE;
      for (int i = 0; i < this.numTables; i++, tableOffset += TableOffset.SIZE) {
        int tag = fd.readULongAsInt(tableOffset + TableOffset.tag);
        long checksum = fd.readULong(tableOffset + TableOffset.checkSum);
        int headerOffset = fd.readULongAsInt(tableOffset + TableOffset.offset);
        int length = fd.readULongAsInt(tableOffset + TableOffset.length);
        records.add(new Header(tag, checksum, headerOffset, length));
      }
      return records;
    }

    private Map<Header, WritableFontData> loadTableData(
        SortedSet<Header> headers, WritableFontData fd) {
      Map<Header, WritableFontData> tableData =
          new HashMap<Header, WritableFontData>(headers.size());
      logger.fine("########  Reading Table Data");
      for (Header tableHeader : headers) {
        WritableFontData data = fd.slice(tableHeader.offset(), tableHeader.length());
        tableData.put(tableHeader, data);
      }
      return tableData;
    }
  }
}

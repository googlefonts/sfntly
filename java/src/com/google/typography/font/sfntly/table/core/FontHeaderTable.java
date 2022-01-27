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

import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.TableBasedTableBuilder;
import com.google.typography.font.sfntly.table.truetype.LocaTable;
import java.util.EnumSet;

/**
 * A Font Header table ('head').
 *
 * @author Stuart Gill
 * @see Tag#head
 * @see "ISO/IEC 14496-22:2015, section 5.2.2"
 */
public final class FontHeaderTable extends Table {

  /**
   * Checksum adjustment base value. To compute the checksum adjustment: 1) set it to 0; 2) sum the
   * entire font as ULONG, 3) then store 0xB1B0AFBA - sum.
   */
  public static final long CHECKSUM_ADJUSTMENT_BASE = 0xB1B0AFBAL;

  /** Magic number value stored in the magic number field. */
  public static final long MAGIC_NUMBER = 0x5F0F3CF5L;

  /** The ranges to use for checksum calculation. */
  private static final int[] CHECKSUM_RANGES = {0, Offset.checkSumAdjustment, Offset.magicNumber};

  /** Offsets to specific elements in the underlying data, relative to the start of the table. */
  private interface Offset {
    int tableVersion = 0;
    int fontRevision = 4;
    int checkSumAdjustment = 8;
    int magicNumber = 12;
    int flags = 16;
    int unitsPerEm = 18;
    int created = 20;
    int modified = 28;
    int xMin = 36;
    int yMin = 38;
    int xMax = 40;
    int yMax = 42;
    int macStyle = 44;
    int lowestRecPPEM = 46;
    int fontDirectionHint = 48;
    int indexToLocFormat = 50;
    int glyphDataFormat = 52;
  }

  private FontHeaderTable(Header header, ReadableFontData data) {
    super(header, data);
    data.setCheckSumRanges(0, Offset.checkSumAdjustment, Offset.magicNumber);
  }

  public int tableVersion() {
    return data.readFixed(Offset.tableVersion);
  }

  public int fontRevision() {
    return data.readFixed(Offset.fontRevision);
  }

  /**
   * Get the checksum adjustment. To compute: set it to 0, sum the entire font as ULONG, then store
   * 0xB1B0AFBA - sum.
   */
  public long checkSumAdjustment() {
    return data.readULong(Offset.checkSumAdjustment);
  }

  /** Get the magic number. Set to 0x5F0F3CF5. */
  public long magicNumber() {
    return data.readULong(Offset.magicNumber);
  }

  /** Flag values in the font header table. */
  public enum Flags {
    BaselineAtY0,
    LeftSidebearingAtX0,
    InstructionsDependOnPointSize,
    ForcePPEMToInteger,
    InstructionsAlterAdvanceWidth,
    // Apple Flags
    Apple_Vertical,
    Apple_Zero,
    Apple_RequiresLayout,
    Apple_GXMetamorphosis,
    Apple_StrongRTL,
    Apple_IndicRearrangement,

    FontDataLossless,
    FontConverted,
    OptimizedForClearType,
    Reserved14,
    Reserved15;

    public int mask() {
      return 1 << ordinal();
    }

    public static EnumSet<Flags> asSet(int value) {
      EnumSet<Flags> set = EnumSet.noneOf(Flags.class);
      for (Flags flag : Flags.values()) {
        if ((value & flag.mask()) == flag.mask()) {
          set.add(flag);
        }
      }
      return set;
    }

    public static int value(EnumSet<Flags> set) {
      int value = 0;
      for (Flags flag : set) {
        value |= flag.mask();
      }
      return value;
    }

    public static int cleanValue(EnumSet<Flags> set) {
      EnumSet<Flags> clean = EnumSet.copyOf(set);
      clean.remove(Reserved14);
      clean.remove(Reserved15);
      return value(clean);
    }
  }

  /** @see #flags() */
  public int flagsAsInt() {
    return data.readUShort(Offset.flags);
  }

  /** @see #flagsAsInt() */
  public EnumSet<Flags> flags() {
    return Flags.asSet(flagsAsInt());
  }

  public int unitsPerEm() {
    return data.readUShort(Offset.unitsPerEm);
  }

  /** Get the created date. Number of seconds since 12:00 midnight, January 1, 1904. */
  public long created() {
    return data.readDateTimeAsLong(Offset.created);
  }

  /** Get the modified date. Number of seconds since 12:00 midnight, January 1, 1904. */
  public long modified() {
    return data.readDateTimeAsLong(Offset.modified);
  }

  /** Get the x min. For all glyph bounding boxes. */
  public int xMin() {
    return data.readShort(Offset.xMin);
  }

  /** Get the y min. For all glyph bounding boxes. */
  public int yMin() {
    return data.readShort(Offset.yMin);
  }

  /** Get the x max. For all glyph bounding boxes. */
  public int xMax() {
    return data.readShort(Offset.xMax);
  }

  /** Get the y max. For all glyph bounding boxes. */
  public int yMax() {
    return data.readShort(Offset.yMax);
  }

  /** Mac style bits set in the font header table. */
  public enum MacStyle {
    Bold,
    Italic,
    Underline,
    Outline,
    Shadow,
    Condensed,
    Extended,
    Reserved7,
    Reserved8,
    Reserved9,
    Reserved10,
    Reserved11,
    Reserved12,
    Reserved13,
    Reserved14,
    Reserved15;

    public int mask() {
      return 1 << ordinal();
    }

    public static EnumSet<MacStyle> asSet(int value) {
      EnumSet<MacStyle> set = EnumSet.noneOf(MacStyle.class);
      for (MacStyle style : MacStyle.values()) {
        if ((value & style.mask()) == style.mask()) {
          set.add(style);
        }
      }
      return set;
    }

    public static int value(EnumSet<MacStyle> set) {
      int value = 0;
      for (MacStyle style : set) {
        value |= style.mask();
      }
      return value;
    }

    public static int cleanValue(EnumSet<MacStyle> set) {
      EnumSet<MacStyle> clean = EnumSet.copyOf(set);
      clean.removeAll(reserved);
      return value(clean);
    }

    private static final EnumSet<MacStyle> reserved = EnumSet.range(Reserved7, Reserved15);
  }

  /** Get the Mac style bits as an int. */
  public int macStyleAsInt() {
    return data.readUShort(Offset.macStyle);
  }

  /** Get the Mac style bits as an enum set. */
  public EnumSet<MacStyle> macStyle() {
    return MacStyle.asSet(macStyleAsInt());
  }

  public int lowestRecPPEM() {
    return data.readUShort(Offset.lowestRecPPEM);
  }

  /** Font direction hint values in the font header table. */
  public enum FontDirectionHint {
    FullyMixed(0),
    OnlyStrongLTR(1),
    StrongLTRAndNeutral(2),
    OnlyStrongRTL(-1),
    StrongRTLAndNeutral(-2);

    private final int value;

    private FontDirectionHint(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static FontDirectionHint valueOf(int value) {
      for (FontDirectionHint hint : FontDirectionHint.values()) {
        if (hint.equals(value)) {
          return hint;
        }
      }
      return null;
    }
  }

  public int fontDirectionHintAsInt() {
    return data.readShort(Offset.fontDirectionHint);
  }

  public FontDirectionHint fontDirectionHint() {
    return FontDirectionHint.valueOf(fontDirectionHintAsInt());
  }

  /**
   * The index to location format used in the LocaTable.
   *
   * @see LocaTable
   */
  public enum IndexToLocFormat {
    shortOffset(0),
    longOffset(1);

    private final int value;

    private IndexToLocFormat(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static IndexToLocFormat valueOf(int value) {
      for (IndexToLocFormat format : IndexToLocFormat.values()) {
        if (format.equals(value)) {
          return format;
        }
      }
      return null;
    }
  }

  public int indexToLocFormatAsInt() {
    return data.readShort(Offset.indexToLocFormat);
  }

  public IndexToLocFormat indexToLocFormat() {
    return IndexToLocFormat.valueOf(indexToLocFormatAsInt());
  }

  public int glyphdataFormat() {
    return data.readShort(Offset.glyphDataFormat);
  }

  public static class Builder extends TableBasedTableBuilder<FontHeaderTable> {
    private boolean fontChecksumSet = false;
    private long fontChecksum = 0;

    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    protected Builder(Header header, WritableFontData data) {
      super(header, data);
      data.setCheckSumRanges(0, Offset.checkSumAdjustment, Offset.magicNumber);
    }

    protected Builder(Header header, ReadableFontData data) {
      super(header, data);
      data.setCheckSumRanges(CHECKSUM_RANGES);
    }

    @Override
    protected boolean subReadyToSerialize() {
      if (dataChanged()) {
        ReadableFontData data = internalReadData();
        data.setCheckSumRanges(CHECKSUM_RANGES);
      }
      if (fontChecksumSet) {
        ReadableFontData data = internalReadData();
        data.setCheckSumRanges(CHECKSUM_RANGES);
        long checksumAdjustment = CHECKSUM_ADJUSTMENT_BASE - (fontChecksum + data.checksum());
        setCheckSumAdjustment(checksumAdjustment);
      }
      return super.subReadyToSerialize();
    }

    @Override
    protected FontHeaderTable subBuildTable(ReadableFontData data) {
      return new FontHeaderTable(header(), data);
    }

    /**
     * Sets the font checksum to be used when calculating the the checksum adjustment for the header
     * table during build time.
     *
     * <p>The font checksum is the sum value of all tables but the font header table. If the font
     * checksum has been set then further setting will be ignored until the font check sum has been
     * cleared with {@link #clearFontChecksum()}. Most users will never need to set this. It is used
     * when the font is being built. If set by a client it can interfere with that process.
     */
    public void setFontChecksum(long checksum) {
      if (fontChecksumSet) {
        return;
      }
      this.fontChecksumSet = true;
      this.fontChecksum = checksum;
    }

    /**
     * Clears the font checksum to be used when calculating the the checksum adjustment for the
     * header table during build time.
     *
     * <p>The font checksum is the sum value of all tables but the font header table. If the font
     * checksum has been set then further setting will be ignored until the font check sum has been
     * cleared.
     */
    public void clearFontChecksum() {
      this.fontChecksumSet = false;
    }

    public int tableVersion() {
      return table().tableVersion();
    }

    public void setTableVersion(int version) {
      internalWriteData().writeFixed(Offset.tableVersion, version);
    }

    public int fontRevision() {
      return table().fontRevision();
    }

    public void setFontRevision(int revision) {
      internalWriteData().writeFixed(Offset.fontRevision, revision);
    }

    public long checkSumAdjustment() {
      return table().checkSumAdjustment();
    }

    public void setCheckSumAdjustment(long adjustment) {
      internalWriteData().writeULong(Offset.checkSumAdjustment, adjustment);
    }

    public long magicNumber() {
      return table().magicNumber();
    }

    public void setMagicNumber(long magicNumber) {
      internalWriteData().writeULong(Offset.magicNumber, magicNumber);
    }

    public int flagsAsInt() {
      return table().flagsAsInt();
    }

    public EnumSet<Flags> flags() {
      return table().flags();
    }

    public void setFlagsAsInt(int flags) {
      internalWriteData().writeUShort(Offset.flags, flags);
    }

    public void setFlags(EnumSet<Flags> flags) {
      setFlagsAsInt(Flags.cleanValue(flags));
    }

    public int unitsPerEm() {
      return table().unitsPerEm();
    }

    public void setUnitsPerEm(int units) {
      internalWriteData().writeUShort(Offset.unitsPerEm, units);
    }

    public long created() {
      return table().created();
    }

    public void setCreated(long date) {
      internalWriteData().writeDateTime(Offset.created, date);
    }

    public long modified() {
      return table().modified();
    }

    public void setModified(long date) {
      internalWriteData().writeDateTime(Offset.modified, date);
    }

    public int xMin() {
      return table().xMin();
    }

    public void setXMin(int xmin) {
      internalWriteData().writeShort(Offset.xMin, xmin);
    }

    public int yMin() {
      return table().yMin();
    }

    public void setYMin(int ymin) {
      internalWriteData().writeShort(Offset.yMin, ymin);
    }

    public int xMax() {
      return table().xMax();
    }

    public void setXMax(int xmax) {
      internalWriteData().writeShort(Offset.xMax, xmax);
    }

    public int yMax() {
      return table().yMax();
    }

    public void setYMax(int ymax) {
      internalWriteData().writeShort(Offset.yMax, ymax);
    }

    public int macStyleAsInt() {
      return table().macStyleAsInt();
    }

    public void setMacStyleAsInt(int style) {
      internalWriteData().writeUShort(Offset.macStyle, style);
    }

    public EnumSet<MacStyle> macStyle() {
      return table().macStyle();
    }

    public void macStyle(EnumSet<MacStyle> style) {
      setMacStyleAsInt(MacStyle.cleanValue(style));
    }

    public int lowestRecPPEM() {
      return table().lowestRecPPEM();
    }

    public void setLowestRecPPEM(int size) {
      internalWriteData().writeUShort(Offset.lowestRecPPEM, size);
    }

    public int fontDirectionHintAsInt() {
      return table().fontDirectionHintAsInt();
    }

    public void setFontDirectionHintAsInt(int hint) {
      internalWriteData().writeShort(Offset.fontDirectionHint, hint);
    }

    public FontDirectionHint fontDirectionHint() {
      return table().fontDirectionHint();
    }

    public void setFontDirectionHint(FontDirectionHint hint) {
      setFontDirectionHintAsInt(hint.value());
    }

    public int indexToLocFormatAsInt() {
      return table().indexToLocFormatAsInt();
    }

    public void setIndexToLocFormatAsInt(int format) {
      internalWriteData().writeShort(Offset.indexToLocFormat, format);
    }

    public IndexToLocFormat indexToLocFormat() {
      return table().indexToLocFormat();
    }

    public void setIndexToLocFormat(IndexToLocFormat format) {
      setIndexToLocFormatAsInt(format.value());
    }

    public int glyphdataFormat() {
      return table().glyphdataFormat();
    }

    public void setGlyphdataFormat(int format) {
      internalWriteData().writeShort(Offset.glyphDataFormat, format);
    }
  }
}

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
import java.util.EnumSet;

/**
 * An OS/2 table - 'OS/2'.
 *
 * @author Stuart Gill
 */
public final class OS2Table extends Table {

  private interface Offset {
    int version = 0;
    int xAvgCharWidth = 2;
    int usWeightClass = 4;
    int usWidthClass = 6;
    int fsType = 8;
    int ySubscriptXSize = 10;
    int ySubscriptYSize = 12;
    int ySubscriptXOffset = 14;
    int ySubscriptYOffset = 16;
    int ySuperscriptXSize = 18;
    int ySuperscriptYSize = 20;
    int ySuperscriptXOffset = 22;
    int ySuperscriptYOffset = 24;
    int yStrikeoutSize = 26;
    int yStrikeoutPosition = 28;
    int sFamilyClass = 30;
    int panose = 32;
    int panoseLength = 10; // length of panose bytes
    int ulUnicodeRange1 = 42;
    int ulUnicodeRange2 = 46;
    int ulUnicodeRange3 = 50;
    int ulUnicodeRange4 = 54;
    int achVendId = 58;
    int achVendIdLength = 4; // length of ach vend id bytes
    int fsSelection = 62;
    int usFirstCharIndex = 64;
    int usLastCharIndex = 66;
    int sTypoAscender = 68;
    int sTypoDescender = 70;
    int sTypoLineGap = 72;
    int usWinAscent = 74;
    int usWinDescent = 76;
    int ulCodePageRange1 = 78;
    int ulCodePageRange2 = 82;
    int sxHeight = 86;
    int sCapHeight = 88;
    int usDefaultChar = 90;
    int usBreakChar = 92;
    int usMaxContext = 94;
  }

  private OS2Table(Header header, ReadableFontData data) {
    super(header, data);
  }

  public int tableVersion() {
    return data.readUShort(Offset.version);
  }

  public int xAvgCharWidth() {
    return data.readShort(Offset.xAvgCharWidth);
  }

  public enum WeightClass {
    Thin(100),
    ExtraLight(200),
    UltraLight(200),
    Light(300),
    Normal(400),
    Regular(400),
    Medium(500),
    SemiBold(600),
    DemiBold(600),
    Bold(700),
    ExtraBold(800),
    UltraBold(800),
    Black(900),
    Heavy(900);

    private final int value;

    private WeightClass(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static WeightClass valueOf(int value) {
      for (WeightClass weight : WeightClass.values()) {
        if (weight.equals(value)) {
          return weight;
        }
      }
      return null;
    }
  }

  public int usWeightClass() {
    return data.readUShort(Offset.usWeightClass);
  }

  public enum WidthClass {
    UltraCondensed(1),
    ExtraCondensed(2),
    Condensed(3),
    SemiCondensed(4),
    Medium(5),
    Normal(5),
    SemiExpanded(6),
    Expanded(7),
    ExtraExpanded(8),
    UltraExpanded(9);

    private final int value;

    private WidthClass(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static WeightClass valueOf(int value) {
      for (WeightClass weight : WeightClass.values()) {
        if (weight.equals(value)) {
          return weight;
        }
      }
      return null;
    }
  }

  public int usWidthClass() {
    return data.readUShort(Offset.usWidthClass);
  }

  /** Flags to indicate the embedding licensing rights for a font. */
  public enum EmbeddingFlags {
    Reserved0,
    RestrictedLicenseEmbedding,
    PreviewAndPrintEmbedding,
    EditableEmbedding,
    Reserved4,
    Reserved5,
    Reserved6,
    Reserved7,
    NoSubsetting,
    BitmapEmbeddingOnly,
    Reserved10,
    Reserved11,
    Reserved12,
    Reserved13,
    Reserved14,
    Reserved15;

    /** @return the bit mask corresponding to this embedding flag */
    public int mask() {
      return 1 << ordinal();
    }

    /** Generates an EnumSet&lt;EmbeddingFlags&gt; representation of the supplied unsigned short. */
    public static EnumSet<EmbeddingFlags> asSet(int value) {
      EnumSet<EmbeddingFlags> set = EnumSet.noneOf(EmbeddingFlags.class);
      for (EmbeddingFlags flag : EmbeddingFlags.values()) {
        if ((value & flag.mask()) == flag.mask()) {
          set.add(flag);
        }
      }
      return set;
    }

    /** Generates an unsigned short representation of the provided flags. */
    public static int asUShort(EnumSet<EmbeddingFlags> flagSet) {
      int flags = 0;
      for (EmbeddingFlags flag : flagSet) {
        flags |= flag.mask();
      }
      return flags;
    }

    /** Tests the {@code fsType} flags. */
    public static boolean isInstallableEditing(EnumSet<EmbeddingFlags> flagSet) {
      return flagSet.isEmpty();
    }

    /** Tests the {@code fsType} flags. */
    public static boolean isInstallableEditing(int value) {
      return value == 0;
    }
  }

  public EnumSet<EmbeddingFlags> fsType() {
    return EmbeddingFlags.asSet(fsTypeAsInt());
  }

  public int fsTypeAsInt() {
    return data.readUShort(Offset.fsType);
  }

  public int ySubscriptXSize() {
    return data.readShort(Offset.ySubscriptXSize);
  }

  public int ySubscriptYSize() {
    return data.readShort(Offset.ySubscriptYSize);
  }

  public int ySubscriptXOffset() {
    return data.readShort(Offset.ySubscriptXOffset);
  }

  public int ySubscriptYOffset() {
    return data.readShort(Offset.ySubscriptYOffset);
  }

  public int ySuperscriptXSize() {
    return data.readShort(Offset.ySuperscriptXSize);
  }

  public int ySuperscriptYSize() {
    return data.readShort(Offset.ySuperscriptYSize);
  }

  public int ySuperscriptXOffset() {
    return data.readShort(Offset.ySuperscriptXOffset);
  }

  public int ySuperscriptYOffset() {
    return data.readShort(Offset.ySuperscriptYOffset);
  }

  public int yStrikeoutSize() {
    return data.readShort(Offset.yStrikeoutSize);
  }

  public int yStrikeoutPosition() {
    return data.readShort(Offset.yStrikeoutPosition);
  }

  // TODO(stuartg): IBM family enum?
  public int sFamilyClass() {
    return data.readShort(Offset.sFamilyClass);
  }

  // TODO(stuartg): panose class? individual getters for the panose values?
  public byte[] panose() {
    byte[] panose = new byte[10];
    data.readBytes(Offset.panose, panose, 0, panose.length);
    return panose;
  }

  public long ulUnicodeRange1() {
    return data.readULong(Offset.ulUnicodeRange1);
  }

  public long ulUnicodeRange2() {
    return data.readULong(Offset.ulUnicodeRange2);
  }

  public long ulUnicodeRange3() {
    return data.readULong(Offset.ulUnicodeRange3);
  }

  public long ulUnicodeRange4() {
    return data.readULong(Offset.ulUnicodeRange4);
  }

  public enum UnicodeRange {
    // Do NOT reorder. This enum relies on the ordering of the data matching the
    // ordinal numbers of the properties
    BasicLatin,
    Latin1Supplement,
    LatinExtendedA,
    LatinExtendedB,
    IPAExtensions,
    SpacingModifierLetters,
    CombiningDiacriticalMarks,
    GreekAndCoptic,
    Coptic,
    Cyrillic,
    Armenian,
    Hebrew,
    Vai,
    Arabic,
    NKo,
    Devanagari,
    Bengali,
    Gurmukhi,
    Gujarati,
    Oriya,
    Tamil,
    Telugu,
    Kannada,
    Malayalam,
    Thai,
    Lao,
    Georgian,
    Balinese,
    HangulJamo,
    LatinExtendedAdditional,
    GreekExtended,
    GeneralPunctuation,
    SuperscriptsAndSubscripts,
    CurrencySymbols,
    NumberForms,
    Arrows,
    MathematicalOperators,
    MiscTechnical,
    ControlPictures,
    OCR,
    EnclosedAlphanumerics,
    BoxDrawing,
    BlockElements,
    GeometricShapes,
    MiscSymbols,
    Dingbats,
    CJKSymbolsAndPunctuation,
    Hiragana,
    Katakana,
    Bopomofo,
    HangulCompatibilityJamo,
    Phagspa,
    EnclosedCJKLettersAndMonths,
    CJKCompatibility,
    HangulSyllables,
    NonPlane0,
    Phoenician,
    CJKUnifiedIdeographs,
    PrivateUseAreaPlane0,
    CJKStrokes,
    AlphabeticPresentationForms,
    ArabicPresentationFormsA,
    CombiningHalfMarks,
    VerticalForms,
    SmallFormVariants,
    ArabicPresentationFormsB,
    HalfwidthAndFullwidthForms,
    Specials,
    Tibetan,
    Syriac,
    Thaana,
    Sinhala,
    Myanmar,
    Ethiopic,
    Cherokee,
    UnifiedCanadianAboriginalSyllabics,
    Ogham,
    Runic,
    Khmer,
    Mongolian,
    BraillePatterns,
    YiSyllables,
    Tagalog,
    OldItalic,
    Gothic,
    Deseret,
    MusicalSymbols,
    MathematicalAlphanumericSymbols,
    PrivateUsePlane15And16,
    VariationSelectors,
    Tags,
    Limbu,
    TaiLe,
    NewTaiLue,
    Buginese,
    Glagolitic,
    Tifnagh,
    YijingHexagramSymbols,
    SylotiNagari,
    LinearB,
    AncientGreekNumbers,
    Ugaritic,
    OldPersian,
    Shavian,
    Osmanya,
    CypriotSyllabary,
    Kharoshthi,
    TaiXuanJingSymbols,
    Cuneiform,
    CountingRodNumerals,
    Sudanese,
    Lepcha,
    OlChiki,
    Saurashtra,
    KayahLi,
    Rejang,
    Charm,
    AncientSymbols,
    PhaistosDisc,
    Carian,
    DominoTiles,
    Reserved123,
    Reserved124,
    Reserved125,
    Reserved126,
    Reserved127;

    public static UnicodeRange range(int bit) {
      if (bit > UnicodeRange.values().length) {
        return null;
      }
      return UnicodeRange.values()[bit];
    }

    public static EnumSet<UnicodeRange> asSet(long range1, long range2, long range3, long range4) {
      EnumSet<UnicodeRange> set = EnumSet.noneOf(UnicodeRange.class);
      long[] range = {range1, range2, range3, range4};
      int rangeBit = 0;
      int rangeIndex = -1;
      for (UnicodeRange ur : UnicodeRange.values()) {
        if (ur.ordinal() % 32 == 0) {
          rangeBit = 0;
          rangeIndex++;
        } else {
          rangeBit++;
        }
        if ((range[rangeIndex] & 1 << rangeBit) == 1 << rangeBit) {
          set.add(ur);
        }
      }
      return set;
    }

    public static long[] asArray(EnumSet<UnicodeRange> rangeSet) {
      long[] range = new long[4];
      for (UnicodeRange ur : rangeSet) {
        int urSegment = ur.ordinal() / 32;
        long urFlag = 1 << (ur.ordinal() % 32);
        range[urSegment] |= urFlag;
      }
      return range;
    }
  }

  public EnumSet<UnicodeRange> ulUnicodeRange() {
    return UnicodeRange.asSet(
        ulUnicodeRange1(), ulUnicodeRange2(),
        ulUnicodeRange3(), ulUnicodeRange4());
  }

  public byte[] achVendId() {
    byte[] b = new byte[4];
    data.readBytes(Offset.achVendId, b, 0, b.length);
    return b;
  }

  public int fsSelectionAsInt() {
    return data.readUShort(Offset.fsSelection);
  }

  public enum FsSelection {
    ITALIC,
    UNDERSCORE,
    NEGATIVE,
    OUTLINED,
    STRIKEOUT,
    BOLD,
    REGULAR,
    USE_TYPO_METRICS,
    WWS,
    OBLIQUE;

    public int mask() {
      return 1 << ordinal();
    }

    public static EnumSet<FsSelection> asSet(int value) {
      EnumSet<FsSelection> set = EnumSet.noneOf(FsSelection.class);
      for (FsSelection selection : FsSelection.values()) {
        if ((value & selection.mask()) == selection.mask()) {
          set.add(selection);
        }
      }
      return set;
    }

    public static int asInt(EnumSet<FsSelection> fsSelectionSet) {
      int value = 0;
      for (FsSelection fsSelection : fsSelectionSet) {
        value |= fsSelection.mask();
      }
      return value;
    }
  }

  public EnumSet<FsSelection> fsSelection() {
    return FsSelection.asSet(fsSelectionAsInt());
  }

  public int usFirstCharIndex() {
    return data.readUShort(Offset.usFirstCharIndex);
  }

  public int usLastCharIndex() {
    return data.readUShort(Offset.usLastCharIndex);
  }

  public int sTypoAscender() {
    return data.readShort(Offset.sTypoAscender);
  }

  public int sTypoDescender() {
    return data.readShort(Offset.sTypoDescender);
  }

  public int sTypoLineGap() {
    return data.readShort(Offset.sTypoLineGap);
  }

  public int usWinAscent() {
    return data.readUShort(Offset.usWinAscent);
  }

  public int usWinDescent() {
    return data.readUShort(Offset.usWinDescent);
  }

  public long ulCodePageRange1() {
    return data.readULong(Offset.ulCodePageRange1);
  }

  public long ulCodePageRange2() {
    return data.readULong(Offset.ulCodePageRange2);
  }

  public enum CodePageRange {
    Latin1_1252,
    Latin2_1250,
    Cyrillic_1251,
    Greek_1253,
    Turkish_1254,
    Hebrew_1255,
    Arabic_1256,
    WindowsBaltic_1257,
    Vietnamese_1258,
    AlternateANSI9,
    AlternateANSI10,
    AlternateANSI11,
    AlternateANSI12,
    AlternateANSI13,
    AlternateANSI14,
    AlternateANSI15,
    Thai_874,
    JapanJIS_932,
    ChineseSimplified_936,
    KoreanWansung_949,
    ChineseTraditional_950,
    KoreanJohab_1361,
    AlternateANSI22,
    AlternateANSI23,
    AlternateANSI24,
    AlternateANSI25,
    AlternateANSI26,
    AlternateANSI27,
    AlternateANSI28,
    MacintoshCharacterSet,
    OEMCharacterSet,
    SymbolCharacterSet,
    ReservedForOEM32,
    ReservedForOEM33,
    ReservedForOEM34,
    ReservedForOEM35,
    ReservedForOEM36,
    ReservedForOEM37,
    ReservedForOEM38,
    ReservedForOEM39,
    ReservedForOEM40,
    ReservedForOEM41,
    ReservedForOEM42,
    ReservedForOEM43,
    ReservedForOEM44,
    ReservedForOEM45,
    ReservedForOEM46,
    ReservedForOEM47,
    IBMGreek_869,
    MSDOSRussion_866,
    MSDOSNordic_865,
    Arabic_864,
    MSDOSCanadianFrench_863,
    Hebrew_862,
    MSDOSIcelandic_861,
    MSDOSPortugese_860,
    IBMTurkish_857,
    IBMCyrillic_855,
    Latin2_852,
    MSDOSBaltic_775,
    Greek_737,
    Arabic_708,
    Latin1_850,
    US_437;

    public static UnicodeRange range(int bit) {
      if (bit > UnicodeRange.values().length) {
        return null;
      }
      return UnicodeRange.values()[bit];
    }

    public static EnumSet<CodePageRange> asSet(long range1, long range2) {
      EnumSet<CodePageRange> set = EnumSet.noneOf(CodePageRange.class);
      long[] range = {range1, range2};
      int rangeBit = 0;
      int rangeIndex = -1;
      for (CodePageRange cpr : CodePageRange.values()) {
        if (cpr.ordinal() % 32 == 0) {
          rangeBit = 0;
          rangeIndex++;
        } else {
          rangeBit++;
        }
        if ((range[rangeIndex] & 1 << rangeBit) == 1 << rangeBit) {
          set.add(cpr);
        }
      }
      return set;
    }

    public static long[] asArray(EnumSet<CodePageRange> rangeSet) {
      long[] range = new long[4];
      for (CodePageRange ur : rangeSet) {
        int urSegment = ur.ordinal() / 32;
        long urFlag = 1 << (ur.ordinal() % 32);
        range[urSegment] |= urFlag;
      }
      return range;
    }
  }

  public EnumSet<CodePageRange> ulCodePageRange() {
    return CodePageRange.asSet(ulCodePageRange1(), ulCodePageRange1());
  }

  public int sxHeight() {
    return data.readShort(Offset.sxHeight);
  }

  public int sCapHeight() {
    return data.readShort(Offset.sCapHeight);
  }

  public int usDefaultChar() {
    return data.readUShort(Offset.usDefaultChar);
  }

  public int usBreakChar() {
    return data.readUShort(Offset.usBreakChar);
  }

  public int usMaxContext() {
    return data.readUShort(Offset.usMaxContext);
  }

  public static class Builder extends TableBasedTableBuilder<OS2Table> {

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
    protected OS2Table subBuildTable(ReadableFontData data) {
      return new OS2Table(header(), data);
    }

    public int tableVersion() {
      return internalReadData().readUShort(Offset.version);
    }

    public void setTableVersion(int version) {
      internalWriteData().writeUShort(Offset.version, version);
    }

    public int xAvgCharWidth() {
      return internalReadData().readShort(Offset.xAvgCharWidth);
    }

    public void setXAvgCharWidth(int width) {
      internalWriteData().writeShort(Offset.xAvgCharWidth, width);
    }

    public int usWeightClass() {
      return internalReadData().readUShort(Offset.usWeightClass);
    }

    public void setUsWeightClass(int weight) {
      internalWriteData().writeUShort(Offset.usWeightClass, weight);
    }

    public int usWidthClass() {
      return internalReadData().readUShort(Offset.usWidthClass);
    }

    public void setUsWidthClass(int width) {
      internalWriteData().writeUShort(Offset.usWidthClass, width);
    }

    public EnumSet<EmbeddingFlags> fsType() {
      return EmbeddingFlags.asSet(fsTypeAsInt());
    }

    public int fsTypeAsInt() {
      return internalReadData().readUShort(Offset.fsType);
    }

    public void setFsType(EnumSet<EmbeddingFlags> flagSet) {
      setFsType(EmbeddingFlags.asUShort(flagSet));
    }

    public void setFsType(int fsType) {
      internalWriteData().writeUShort(Offset.fsType, fsType);
    }

    public int ySubscriptXSize() {
      return internalReadData().readShort(Offset.ySubscriptXSize);
    }

    public void setYSubscriptXSize(int size) {
      internalWriteData().writeShort(Offset.ySubscriptXSize, size);
    }

    public int ySubscriptYSize() {
      return internalReadData().readShort(Offset.ySubscriptYSize);
    }

    public void setYSubscriptYSize(int size) {
      internalWriteData().writeShort(Offset.ySubscriptYSize, size);
    }

    public int ySubscriptXOffset() {
      return internalReadData().readShort(Offset.ySubscriptXOffset);
    }

    public void setYSubscriptXOffset(int offset) {
      internalWriteData().writeShort(Offset.ySubscriptXOffset, offset);
    }

    public int ySubscriptYOffset() {
      return internalReadData().readShort(Offset.ySubscriptYOffset);
    }

    public void setYSubscriptYOffset(int offset) {
      internalWriteData().writeShort(Offset.ySubscriptYOffset, offset);
    }

    public int ySuperscriptXSize() {
      return internalReadData().readShort(Offset.ySuperscriptXSize);
    }

    public void setYSuperscriptXSize(int size) {
      internalWriteData().writeShort(Offset.ySuperscriptXSize, size);
    }

    public int ySuperscriptYSize() {
      return internalReadData().readShort(Offset.ySuperscriptYSize);
    }

    public void setYSuperscriptYSize(int size) {
      internalWriteData().writeShort(Offset.ySuperscriptYSize, size);
    }

    public int ySuperscriptXOffset() {
      return internalReadData().readShort(Offset.ySuperscriptXOffset);
    }

    public void setYSuperscriptXOffset(int offset) {
      internalWriteData().writeShort(Offset.ySuperscriptXOffset, offset);
    }

    public int ySuperscriptYOffset() {
      return internalReadData().readShort(Offset.ySuperscriptYOffset);
    }

    public void setYSuperscriptYOffset(int offset) {
      internalWriteData().writeShort(Offset.ySuperscriptYOffset, offset);
    }

    public int yStrikeoutSize() {
      return internalReadData().readShort(Offset.yStrikeoutSize);
    }

    public void setYStrikeoutSize(int size) {
      internalWriteData().writeShort(Offset.yStrikeoutSize, size);
    }

    public int yStrikeoutPosition() {
      return internalReadData().readShort(Offset.yStrikeoutPosition);
    }

    public void setYStrikeoutPosition(int position) {
      internalWriteData().writeShort(Offset.yStrikeoutPosition, position);
    }

    public int sFamilyClass() {
      return internalReadData().readShort(Offset.sFamilyClass);
    }

    public void setSFamilyClass(int family) {
      internalWriteData().writeShort(Offset.sFamilyClass, family);
    }

    public byte[] panose() {
      byte[] panose = new byte[Offset.panoseLength];
      internalReadData().readBytes(Offset.panose, panose, 0, panose.length);
      return panose;
    }

    public void setPanose(byte[] panose) {
      if (panose.length != Offset.panoseLength) {
        throw new IllegalArgumentException("Panose bytes must be exactly 10 in length.");
      }
      internalWriteData().writeBytes(Offset.panose, panose, 0, panose.length);
    }

    public long ulUnicodeRange1() {
      return internalReadData().readULong(Offset.ulUnicodeRange1);
    }

    public void setUlUnicodeRange1(long range) {
      internalWriteData().writeULong(Offset.ulUnicodeRange1, range);
    }

    public long ulUnicodeRange2() {
      return internalReadData().readULong(Offset.ulUnicodeRange2);
    }

    public void setUlUnicodeRange2(long range) {
      internalWriteData().writeULong(Offset.ulUnicodeRange2, range);
    }

    public long ulUnicodeRange3() {
      return internalReadData().readULong(Offset.ulUnicodeRange3);
    }

    public void setUlUnicodeRange3(long range) {
      internalWriteData().writeULong(Offset.ulUnicodeRange3, range);
    }

    public long ulUnicodeRange4() {
      return internalReadData().readULong(Offset.ulUnicodeRange4);
    }

    public void setUlUnicodeRange4(long range) {
      internalWriteData().writeULong(Offset.ulUnicodeRange4, range);
    }

    public EnumSet<UnicodeRange> ulUnicodeRange() {
      return UnicodeRange.asSet(
          ulUnicodeRange1(), ulUnicodeRange2(), ulUnicodeRange3(), ulUnicodeRange4());
    }

    public void setUlUnicodeRange(EnumSet<UnicodeRange> rangeSet) {
      long[] range = UnicodeRange.asArray(rangeSet);
      setUlUnicodeRange1(range[0]);
      setUlUnicodeRange2(range[1]);
      setUlUnicodeRange3(range[2]);
      setUlUnicodeRange4(range[3]);
    }

    public byte[] achVendId() {
      byte[] b = new byte[Offset.achVendIdLength];
      internalReadData().readBytes(Offset.achVendId, b, 0, b.length);
      return b;
    }

    /**
     * Sets the achVendId field.
     *
     * <p>This field is 4 bytes in length and only the first 4 bytes of the byte array will be
     * written. If the byte array is less than 4 bytes it will be padded out with space characters
     * (0x20).
     *
     * @param b ach Vendor Id
     */
    public void setAchVendId(byte[] b) {
      internalWriteData().writeBytesPad(Offset.achVendId, b, 0, Offset.achVendIdLength, (byte) ' ');
    }

    public int fsSelectionAsInt() {
      return internalReadData().readUShort(Offset.fsSelection);
    }

    public void setFsSelection(int fsSelection) {
      internalWriteData().writeUShort(Offset.fsSelection, fsSelection);
    }

    public void fsSelection(EnumSet<FsSelection> fsSelection) {
      setFsSelection(FsSelection.asInt(fsSelection));
    }

    public int usFirstCharIndex() {
      return internalReadData().readUShort(Offset.usFirstCharIndex);
    }

    public void setUsFirstCharIndex(int firstIndex) {
      internalWriteData().writeUShort(Offset.usFirstCharIndex, firstIndex);
    }

    public int usLastCharIndex() {
      return internalReadData().readUShort(Offset.usLastCharIndex);
    }

    public void setUsLastCharIndex(int lastIndex) {
      internalWriteData().writeUShort(Offset.usLastCharIndex, lastIndex);
    }

    public int sTypoAscender() {
      return internalReadData().readShort(Offset.sTypoAscender);
    }

    public void setSTypoAscender(int ascender) {
      internalWriteData().writeShort(Offset.sTypoAscender, ascender);
    }

    public int sTypoDescender() {
      return internalReadData().readShort(Offset.sTypoDescender);
    }

    public void setSTypoDescender(int descender) {
      internalWriteData().writeShort(Offset.sTypoDescender, descender);
    }

    public int sTypoLineGap() {
      return internalReadData().readShort(Offset.sTypoLineGap);
    }

    public void setSTypoLineGap(int lineGap) {
      internalWriteData().writeShort(Offset.sTypoLineGap, lineGap);
    }

    public int usWinAscent() {
      return internalReadData().readUShort(Offset.usWinAscent);
    }

    public void setUsWinAscent(int ascent) {
      internalWriteData().writeUShort(Offset.usWinAscent, ascent);
    }

    public int usWinDescent() {
      return internalReadData().readUShort(Offset.usWinDescent);
    }

    public void setUsWinDescent(int descent) {
      internalWriteData().writeUShort(Offset.usWinAscent, descent);
    }

    public long ulCodePageRange1() {
      return internalReadData().readULong(Offset.ulCodePageRange1);
    }

    public void setUlCodePageRange1(long range) {
      internalWriteData().writeULong(Offset.ulCodePageRange1, range);
    }

    public long ulCodePageRange2() {
      return internalReadData().readULong(Offset.ulCodePageRange2);
    }

    public void setUlCodePageRange2(long range) {
      internalWriteData().writeULong(Offset.ulCodePageRange2, range);
    }

    public EnumSet<CodePageRange> ulCodePageRange() {
      return CodePageRange.asSet(ulCodePageRange1(), ulCodePageRange2());
    }

    public void setUlCodePageRange(EnumSet<CodePageRange> rangeSet) {
      long[] range = CodePageRange.asArray(rangeSet);
      setUlCodePageRange1(range[0]);
      setUlCodePageRange2(range[1]);
    }

    public int sxHeight() {
      return internalReadData().readShort(Offset.sxHeight);
    }

    public void setSxHeight(int height) {
      internalWriteData().writeShort(Offset.sxHeight, height);
    }

    public int sCapHeight() {
      return internalReadData().readShort(Offset.sCapHeight);
    }

    public void setSCapHeight(int height) {
      internalWriteData().writeShort(Offset.sCapHeight, height);
    }

    public int usDefaultChar() {
      return internalReadData().readUShort(Offset.usDefaultChar);
    }

    public void setUsDefaultChar(int defaultChar) {
      internalWriteData().writeUShort(Offset.usDefaultChar, defaultChar);
    }

    public int usBreakChar() {
      return internalReadData().readUShort(Offset.usBreakChar);
    }

    public void setUsBreakChar(int breakChar) {
      internalWriteData().writeUShort(Offset.usBreakChar, breakChar);
    }

    public int usMaxContext() {
      return internalReadData().readUShort(Offset.usMaxContext);
    }

    public void setUsMaxContext(int maxContext) {
      internalWriteData().writeUShort(Offset.usMaxContext, maxContext);
    }
  }
}

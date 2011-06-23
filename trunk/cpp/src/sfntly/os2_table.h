/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_OS2_TABLE_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_OS2_TABLE_H_

#include "sfntly/port/refcount.h"
#include "sfntly/table.h"

namespace sfntly {

struct WeightClass {
  static const int32_t kThin;
  static const int32_t kExtraLight;
  static const int32_t kUltraLight;
  static const int32_t kLight;
  static const int32_t kNormal;
  static const int32_t kRegular;
  static const int32_t kMedium;
  static const int32_t kSemiBold;
  static const int32_t kDemiBold;
  static const int32_t kBold;
  static const int32_t kExtraBold;
  static const int32_t kUltraBold;
  static const int32_t kBlack;
  static const int32_t kHeavy;
};

struct WidthClass {
  static const int32_t kUltraCondensed;
  static const int32_t kExtraCondensed;
  static const int32_t kCondensed;
  static const int32_t kSemiCondensed;
  static const int32_t kMedium;
  static const int32_t kNormal;
  static const int32_t kSemiExpanded;
  static const int32_t kExpanded;
  static const int32_t kExtraExpanded;
  static const int32_t kUltraExpanded;
};

struct EmbeddingFlags {
  static const int32_t kReserved0;
  static const int32_t kRestrictedLicenseEmbedding;
  static const int32_t kPreviewAndPrintEmbedding;
  static const int32_t kEditableEmbedding;
  static const int32_t kReserved4;
  static const int32_t kReserved5;
  static const int32_t kReserved6;
  static const int32_t kReserved7;
  static const int32_t kNoSubsetting;
  static const int32_t kBitmapEmbeddingOnly;
  static const int32_t kReserved10;
  static const int32_t kReserved11;
  static const int32_t kReserved12;
  static const int32_t kReserved13;
  static const int32_t kReserved14;
  static const int32_t kReserved15;
};

struct UnicodeRange {
  enum {
    kBasicLatin,
    kLatin1Supplement,
    kLatinExtendedA,
    kLatinExtendedB,
    kIPAExtensions,
    kSpacingModifierLetters,
    kCombiningDiacriticalMarks,
    kGreekAndCoptic,
    kCoptic,
    kCyrillic,
    kArmenian,
    kHebrew,
    kVai,
    kArabic,
    kNKo,
    kDevanagari,
    kBengali,
    kGurmukhi,
    kGujarati,
    kOriya,
    kTamil,
    kTelugu,
    kKannada,
    kMalayalam,
    kThai,
    kLao,
    kGeorgian,
    kBalinese,
    kHangulJamo,
    kLatinExtendedAdditional,
    kGreekExtended,
    kGeneralPunctuation,
    kSuperscriptsAndSubscripts,
    kCurrencySymbols,
    kNumberForms,
    kArrows,
    kMathematicalOperators,
    kMiscTechnical,
    kControlPictures,
    kOCR,
    kEnclosedAlphanumerics,
    kBoxDrawing,
    kBlockElements,
    kGeometricShapes,
    kMiscSymbols,
    kDingbats,
    kCJKSymbolsAndPunctuation,
    kHiragana,
    kKatakana,
    kBopomofo,
    kHangulCompatibilityJamo,
    kPhagspa,
    kEnclosedCJKLettersAndMonths,
    kCJKCompatibility,
    kHangulSyllables,
    kNonPlane0,
    kPhoenician,
    kCJKUnifiedIdeographs,
    kPrivateUseAreaPlane0,
    kCJKStrokes,
    kAlphabeticPresentationForms,
    kArabicPresentationFormsA,
    kCombiningHalfMarks,
    kVerticalForms,
    kSmallFormVariants,
    kArabicPresentationFormsB,
    kHalfwidthAndFullwidthForms,
    kSpecials,
    kTibetan,
    kSyriac,
    kThaana,
    kSinhala,
    kMyanmar,
    kEthiopic,
    kCherokee,
    kUnifiedCanadianAboriginalSyllabics,
    kOgham,
    kRunic,
    kKhmer,
    kMongolian,
    kBraillePatterns,
    kYiSyllables,
    kTagalog,
    kOldItalic,
    kGothic,
    kDeseret,
    kMusicalSymbols,
    kMathematicalAlphanumericSymbols,
    kPrivateUsePlane15And16,
    kVariationSelectors,
    kTags,
    kLimbu,
    kTaiLe,
    kNewTaiLue,
    kBuginese,
    kGlagolitic,
    kTifnagh,
    kYijingHexagramSymbols,
    kSylotiNagari,
    kLinearB,
    kAncientGreekNumbers,
    kUgaritic,
    kOldPersian,
    kShavian,
    kOsmanya,
    kCypriotSyllabary,
    kKharoshthi,
    kTaiXuanJingSymbols,
    kCuneiform,
    kCountingRodNumerals,
    kSudanese,
    kLepcha,
    kOlChiki,
    kSaurashtra,
    kKayahLi,
    kRejang,
    kCharm,
    kAncientSymbols,
    kPhaistosDisc,
    kCarian,
    kDominoTiles,
    kReserved123,
    kReserved124,
    kReserved125,
    kReserved126,
    kReserved127,
    kLast = kReserved127
  };

  int32_t range(int32_t bit);
  // UNIMPLEMENTED: EnumSet<UnicodeRange> asSet(long range1, long range2,
  //                                            long range3, long range4)
};

struct FsSelection {
  static const int32_t kITALIC;
  static const int32_t kUNDERSCORE;
  static const int32_t kNEGATIVE;
  static const int32_t kOUTLINED;
  static const int32_t kSTRIKEOUT;
  static const int32_t kBOLD;
  static const int32_t kREGULAR;
  static const int32_t kUSE_TYPO_METRICS;
  static const int32_t kWWS;
  static const int32_t kOBLIQUE;
};

struct CodePageRange {
  static const int64_t kLatin1_1252;
  static const int64_t kLatin2_1250;
  static const int64_t kCyrillic_1251;
  static const int64_t kGreek_1253;
  static const int64_t kTurkish_1254;
  static const int64_t kHebrew_1255;
  static const int64_t kArabic_1256;
  static const int64_t kWindowsBaltic_1257;
  static const int64_t kVietnamese_1258;
  static const int64_t kAlternateANSI9;
  static const int64_t kAlternateANSI10;
  static const int64_t kAlternateANSI11;
  static const int64_t kAlternateANSI12;
  static const int64_t kAlternateANSI13;
  static const int64_t kAlternateANSI14;
  static const int64_t kAlternateANSI15;
  static const int64_t kThai_874;
  static const int64_t kJapanJIS_932;
  static const int64_t kChineseSimplified_936;
  static const int64_t kKoreanWansung_949;
  static const int64_t kChineseTraditional_950;
  static const int64_t kKoreanJohab_1361;
  static const int64_t kAlternateANSI22;
  static const int64_t kAlternateANSI23;
  static const int64_t kAlternateANSI24;
  static const int64_t kAlternateANSI25;
  static const int64_t kAlternateANSI26;
  static const int64_t kAlternateANSI27;
  static const int64_t kAlternateANSI28;
  static const int64_t kMacintoshCharacterSet;
  static const int64_t kOEMCharacterSet;
  static const int64_t kSymbolCharacterSet;
  static const int64_t kReservedForOEM32;
  static const int64_t kReservedForOEM33;
  static const int64_t kReservedForOEM34;
  static const int64_t kReservedForOEM35;
  static const int64_t kReservedForOEM36;
  static const int64_t kReservedForOEM37;
  static const int64_t kReservedForOEM38;
  static const int64_t kReservedForOEM39;
  static const int64_t kReservedForOEM40;
  static const int64_t kReservedForOEM41;
  static const int64_t kReservedForOEM42;
  static const int64_t kReservedForOEM43;
  static const int64_t kReservedForOEM44;
  static const int64_t kReservedForOEM45;
  static const int64_t kReservedForOEM46;
  static const int64_t kReservedForOEM47;
  static const int64_t kIBMGreek_869;
  static const int64_t kMSDOSRussion_866;
  static const int64_t kMSDOSNordic_865;
  static const int64_t kArabic_864;
  static const int64_t kMSDOSCanadianFrench_863;
  static const int64_t kHebrew_862;
  static const int64_t kMSDOSIcelandic_861;
  static const int64_t kMSDOSPortugese_860;
  static const int64_t kIBMTurkish_857;
  static const int64_t kIBMCyrillic_855;
  static const int64_t kLatin2_852;
  static const int64_t kMSDOSBaltic_775;
  static const int64_t kGreek_737;
  static const int64_t kArabic_708;
  static const int64_t kLatin1_850;
  static const int64_t kUS_437;
};

class OS2Table : public Table, public RefCounted<OS2Table> {
 private:
  struct Offset {
    static const int32_t kVersion;
    static const int32_t kXAvgCharWidth;
    static const int32_t kUsWeightClass;
    static const int32_t kUsWidthClass;
    static const int32_t kFsType;
    static const int32_t kYSubscriptXSize;
    static const int32_t kYSubscriptYSize;
    static const int32_t kYSubscriptXOffset;
    static const int32_t kYSubscriptYOffset;
    static const int32_t kYSuperscriptXSize;
    static const int32_t kYSuperscriptYSize;
    static const int32_t kYSuperscriptXOffset;
    static const int32_t kYSuperscriptYOffset;
    static const int32_t kYStrikeoutSize;
    static const int32_t kYStrikeoutPosition;
    static const int32_t kSFamilyClass;
    static const int32_t kPanose;
    static const int32_t kUlUnicodeRange1;
    static const int32_t kUlUnicodeRange2;
    static const int32_t kUlUnicodeRange3;
    static const int32_t kUlUnicodeRange4;
    static const int32_t kAchVendId;
    static const int32_t kFsSelection;
    static const int32_t kUsFirstCharIndex;
    static const int32_t kUsLastCharIndex;
    static const int32_t kSTypoAscender;
    static const int32_t kSTypoDescender;
    static const int32_t kSTypoLineGap;
    static const int32_t kUsWinAscent;
    static const int32_t kUsWinDescent;
    static const int32_t kUlCodePageRange1;
    static const int32_t kUlCodePageRange2;
    static const int32_t kSxHeight;
    static const int32_t kSCapHeight;
    static const int32_t kUsDefaultChar;
    static const int32_t kUsBreakChar;
    static const int32_t kUsMaxContext;
  };

  OS2Table(Header* header, ReadableFontData* data);

 public:
  ~OS2Table();

  int32_t version();
  int32_t xAvgCharWidth();
  int32_t usWeightClass();
  int32_t usWidthClass();
  // UNIMPLEMENTED: public EnumSet<EmbeddingFlags> fsType()
  int32_t fsType();
  int32_t ySubscriptXSize();
  int32_t ySubscriptYSize();
  int32_t ySubscriptXOffset();
  int32_t ySubscriptYOffset();
  int32_t ySuperscriptXSize();
  int32_t ySuperscriptYSize();
  int32_t ySuperscriptXOffset();
  int32_t ySuperscriptYOffset();
  int32_t yStrikeoutSize();
  int32_t yStrikeoutPosition();
  int32_t sFamilyClass();
  void panose(ByteVector* value);
  int64_t ulUnicodeRange1();
  int64_t ulUnicodeRange2();
  int64_t ulUnicodeRange3();
  int64_t ulUnicodeRange4();
  // UNIMPLEMENTED: public EnumSet<UnicodeRange> ulUnicodeRange()
  void achVendId(ByteVector* b);
  // UNIMPLEMENTED: public EnumSet<FsSelection> fsSelection()
  int32_t fsSelection();
  int32_t usFirstCharIndex();
  int32_t usLastCharIndex();
  int32_t sTypoAscender();
  int32_t sTypoDecender();
  int32_t sTypoLineGap();
  int32_t usWinAscent();
  int32_t usWinDescent();
  int64_t ulCodePageRange1();
  int64_t ulCodePageRange2();
  // UNIMPLEMENTED: public EnumSet<CodePageRange> ulCodePageRange()
  int64_t ulCodePageRange();
  int32_t sxHeight();
  int32_t sCapHeight();
  int32_t usDefaultChar();
  int32_t usBreakChar();
  int32_t usMaxContext();

 public:
  class Builder : public Table::TableBasedTableBuilder,
                  public RefCounted<Builder> {
   public:
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
            WritableFontData* data);
    virtual ~Builder();
    virtual CALLER_ATTACH FontDataTable* subBuildTable(ReadableFontData* data);
  };
};

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_OS2_TABLE_H_

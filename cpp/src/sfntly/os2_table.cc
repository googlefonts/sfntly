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

#include "sfntly/os2_table.h"

namespace sfntly {
/******************************************************************************
 * Constants
 ******************************************************************************/
const int32_t OS2Table::Offset::kVersion = 0;
const int32_t OS2Table::Offset::kXAvgCharWidth = 2;
const int32_t OS2Table::Offset::kUsWeightClass = 4;
const int32_t OS2Table::Offset::kUsWidthClass = 6;
const int32_t OS2Table::Offset::kFsType = 8;
const int32_t OS2Table::Offset::kYSubscriptXSize = 10;
const int32_t OS2Table::Offset::kYSubscriptYSize = 12;
const int32_t OS2Table::Offset::kYSubscriptXOffset = 14;
const int32_t OS2Table::Offset::kYSubscriptYOffset = 16;
const int32_t OS2Table::Offset::kYSuperscriptXSize = 18;
const int32_t OS2Table::Offset::kYSuperscriptYSize = 20;
const int32_t OS2Table::Offset::kYSuperscriptXOffset = 22;
const int32_t OS2Table::Offset::kYSuperscriptYOffset = 24;
const int32_t OS2Table::Offset::kYStrikeoutSize = 26;
const int32_t OS2Table::Offset::kYStrikeoutPosition = 28;
const int32_t OS2Table::Offset::kSFamilyClass = 30;
const int32_t OS2Table::Offset::kPanose = 32;
const int32_t OS2Table::Offset::kUlUnicodeRange1 = 42;
const int32_t OS2Table::Offset::kUlUnicodeRange2 = 46;
const int32_t OS2Table::Offset::kUlUnicodeRange3 = 50;
const int32_t OS2Table::Offset::kUlUnicodeRange4 = 54;
const int32_t OS2Table::Offset::kAchVendId = 58;
const int32_t OS2Table::Offset::kFsSelection = 62;
const int32_t OS2Table::Offset::kUsFirstCharIndex = 64;
const int32_t OS2Table::Offset::kUsLastCharIndex = 66;
const int32_t OS2Table::Offset::kSTypoAscender = 68;
const int32_t OS2Table::Offset::kSTypoDescender = 70;
const int32_t OS2Table::Offset::kSTypoLineGap = 72;
const int32_t OS2Table::Offset::kUsWinAscent = 74;
const int32_t OS2Table::Offset::kUsWinDescent = 76;
const int32_t OS2Table::Offset::kUlCodePageRange1 = 78;
const int32_t OS2Table::Offset::kUlCodePageRange2 = 82;
const int32_t OS2Table::Offset::kSxHeight = 86;
const int32_t OS2Table::Offset::kSCapHeight = 88;
const int32_t OS2Table::Offset::kUsDefaultChar = 90;
const int32_t OS2Table::Offset::kUsBreakChar = 92;
const int32_t OS2Table::Offset::kUsMaxContext = 94;

const int32_t WeightClass::kThin = 100;
const int32_t WeightClass::kExtraLight = 200;
const int32_t WeightClass::kUltraLight = 200;
const int32_t WeightClass::kLight = 300;
const int32_t WeightClass::kNormal = 400;
const int32_t WeightClass::kRegular = 400;
const int32_t WeightClass::kMedium = 500;
const int32_t WeightClass::kSemiBold = 600;
const int32_t WeightClass::kDemiBold = 600;
const int32_t WeightClass::kBold = 700;
const int32_t WeightClass::kExtraBold = 800;
const int32_t WeightClass::kUltraBold = 800;
const int32_t WeightClass::kBlack = 900;
const int32_t WeightClass::kHeavy = 900;

const int32_t WidthClass::kUltraCondensed = 1;
const int32_t WidthClass::kExtraCondensed = 2;
const int32_t WidthClass::kCondensed = 3;
const int32_t WidthClass::kSemiCondensed = 4;
const int32_t WidthClass::kMedium = 5;
const int32_t WidthClass::kNormal = 5;
const int32_t WidthClass::kSemiExpanded = 6;
const int32_t WidthClass::kExpanded = 7;
const int32_t WidthClass::kExtraExpanded = 8;
const int32_t WidthClass::kUltraExpanded = 9;

const int32_t EmbeddingFlags::kReserved0 = 1 << 0;
const int32_t EmbeddingFlags::kRestrictedLicenseEmbedding = 1 << 1;
const int32_t EmbeddingFlags::kPreviewAndPrintEmbedding = 1 << 2;
const int32_t EmbeddingFlags::kEditableEmbedding = 1 << 3;
const int32_t EmbeddingFlags::kReserved4 = 1 << 4;
const int32_t EmbeddingFlags::kReserved5 = 1 << 5;
const int32_t EmbeddingFlags::kReserved6 = 1 << 6;
const int32_t EmbeddingFlags::kReserved7 = 1 << 7;
const int32_t EmbeddingFlags::kNoSubsetting = 1 << 8;
const int32_t EmbeddingFlags::kBitmapEmbeddingOnly = 1 << 9;
const int32_t EmbeddingFlags::kReserved10 = 1 << 10;
const int32_t EmbeddingFlags::kReserved11 = 1 << 11;
const int32_t EmbeddingFlags::kReserved12 = 1 << 12;
const int32_t EmbeddingFlags::kReserved13 = 1 << 13;
const int32_t EmbeddingFlags::kReserved14 = 1 << 14;
const int32_t EmbeddingFlags::kReserved15 = 1 << 15;

const int32_t FsSelection::kITALIC = 1 << 0;
const int32_t FsSelection::kUNDERSCORE = 1 << 1;
const int32_t FsSelection::kNEGATIVE = 1 << 2;
const int32_t FsSelection::kOUTLINED = 1 << 3;
const int32_t FsSelection::kSTRIKEOUT = 1 << 4;
const int32_t FsSelection::kBOLD = 1 << 5;
const int32_t FsSelection::kREGULAR = 1 << 6;
const int32_t FsSelection::kUSE_TYPO_METRICS = 1 << 7;
const int32_t FsSelection::kWWS = 1 << 8;
const int32_t FsSelection::kOBLIQUE = 1 << 9;

const int64_t CodePageRange::kLatin1_1252 = (int64_t)1 << 0;
const int64_t CodePageRange::kLatin2_1250 = (int64_t)1 << (int64_t)1;
const int64_t CodePageRange::kCyrillic_1251 = (int64_t)1 << 2;
const int64_t CodePageRange::kGreek_1253 = (int64_t)1 << 3;
const int64_t CodePageRange::kTurkish_1254 = (int64_t)1 << 4;
const int64_t CodePageRange::kHebrew_1255 = (int64_t)1 << 5;
const int64_t CodePageRange::kArabic_1256 = (int64_t)1 << 6;
const int64_t CodePageRange::kWindowsBaltic_1257 = (int64_t)1 << 7;
const int64_t CodePageRange::kVietnamese_1258 = (int64_t)1 << 8;
const int64_t CodePageRange::kAlternateANSI9 = (int64_t)1 << 9;
const int64_t CodePageRange::kAlternateANSI10 = (int64_t)1 << 10;
const int64_t CodePageRange::kAlternateANSI11 = (int64_t)1 << 11;
const int64_t CodePageRange::kAlternateANSI12 = (int64_t)1 << 12;
const int64_t CodePageRange::kAlternateANSI13 = (int64_t)1 << 13;
const int64_t CodePageRange::kAlternateANSI14 = (int64_t)1 << 14;
const int64_t CodePageRange::kAlternateANSI15 = (int64_t)1 << 15;
const int64_t CodePageRange::kThai_874 = (int64_t)1 << 16;
const int64_t CodePageRange::kJapanJIS_932 = (int64_t)1 << 17;
const int64_t CodePageRange::kChineseSimplified_936 = (int64_t)1 << 18;
const int64_t CodePageRange::kKoreanWansung_949 = (int64_t)1 << 19;
const int64_t CodePageRange::kChineseTraditional_950 = (int64_t)1 << 20;
const int64_t CodePageRange::kKoreanJohab_1361 = (int64_t)1 << 21;
const int64_t CodePageRange::kAlternateANSI22 = (int64_t)1 << 22;
const int64_t CodePageRange::kAlternateANSI23 = (int64_t)1 << 23;
const int64_t CodePageRange::kAlternateANSI24 = (int64_t)1 << 24;
const int64_t CodePageRange::kAlternateANSI25 = (int64_t)1 << 25;
const int64_t CodePageRange::kAlternateANSI26 = (int64_t)1 << 26;
const int64_t CodePageRange::kAlternateANSI27 = (int64_t)1 << 27;
const int64_t CodePageRange::kAlternateANSI28 = (int64_t)1 << 28;
const int64_t CodePageRange::kMacintoshCharacterSet = (int64_t)1 << 29;
const int64_t CodePageRange::kOEMCharacterSet = (int64_t)1 << 30;
const int64_t CodePageRange::kSymbolCharacterSet = (int64_t)1 << 31;
const int64_t CodePageRange::kReservedForOEM32 = (int64_t)1 << 32;
const int64_t CodePageRange::kReservedForOEM33 = (int64_t)1 << 33;
const int64_t CodePageRange::kReservedForOEM34 = (int64_t)1 << 34;
const int64_t CodePageRange::kReservedForOEM35 = (int64_t)1 << 35;
const int64_t CodePageRange::kReservedForOEM36 = (int64_t)1 << 36;
const int64_t CodePageRange::kReservedForOEM37 = (int64_t)1 << 37;
const int64_t CodePageRange::kReservedForOEM38 = (int64_t)1 << 38;
const int64_t CodePageRange::kReservedForOEM39 = (int64_t)1 << 39;
const int64_t CodePageRange::kReservedForOEM40 = (int64_t)1 << 40;
const int64_t CodePageRange::kReservedForOEM41 = (int64_t)1 << 41;
const int64_t CodePageRange::kReservedForOEM42 = (int64_t)1 << 42;
const int64_t CodePageRange::kReservedForOEM43 = (int64_t)1 << 43;
const int64_t CodePageRange::kReservedForOEM44 = (int64_t)1 << 44;
const int64_t CodePageRange::kReservedForOEM45 = (int64_t)1 << 45;
const int64_t CodePageRange::kReservedForOEM46 = (int64_t)1 << 46;
const int64_t CodePageRange::kReservedForOEM47 = (int64_t)1 << 47;
const int64_t CodePageRange::kIBMGreek_869 = (int64_t)1 << 48;
const int64_t CodePageRange::kMSDOSRussion_866 = (int64_t)1 << 49;
const int64_t CodePageRange::kMSDOSNordic_865 = (int64_t)1 << 50;
const int64_t CodePageRange::kArabic_864 = (int64_t)1 << 51;
const int64_t CodePageRange::kMSDOSCanadianFrench_863 = (int64_t)1 << 52;
const int64_t CodePageRange::kHebrew_862 = (int64_t)1 << 53;
const int64_t CodePageRange::kMSDOSIcelandic_861 = (int64_t)1 << 54;
const int64_t CodePageRange::kMSDOSPortugese_860 = (int64_t)1 << 55;
const int64_t CodePageRange::kIBMTurkish_857 = (int64_t)1 << 56;
const int64_t CodePageRange::kIBMCyrillic_855 = (int64_t)1 << 57;
const int64_t CodePageRange::kLatin2_852 = (int64_t)1 << 58;
const int64_t CodePageRange::kMSDOSBaltic_775 = (int64_t)1 << 59;
const int64_t CodePageRange::kGreek_737 = (int64_t)1 << 60;
const int64_t CodePageRange::kArabic_708 = (int64_t)1 << 61;
const int64_t CodePageRange::kLatin1_850 = (int64_t)1 << 62;
const int64_t CodePageRange::kUS_437 = (int64_t)1 << 63;

/******************************************************************************
 * struct UnicodeRange
 ******************************************************************************/
int32_t UnicodeRange::range(int32_t bit) {
  if (bit < 0 || bit > kLast) {
    return -1;
  }
  return bit;
}

/******************************************************************************
 * class OS2Table
 ******************************************************************************/
OS2Table::OS2Table(Header* header, ReadableFontData* data)
    : Table(header, data) {
}

OS2Table::~OS2Table() {}

int32_t OS2Table::version() {
  return data_->readUShort(Offset::kVersion);
}

int32_t OS2Table::xAvgCharWidth() {
  return data_->readShort(Offset::kXAvgCharWidth);
}

int32_t OS2Table::usWeightClass() {
  return data_->readUShort(Offset::kUsWeightClass);
}

int32_t OS2Table::usWidthClass() {
  return data_->readUShort(Offset::kUsWidthClass);
}

int32_t OS2Table::fsType() {
  return data_->readUShort(Offset::kFsType);
}

int32_t OS2Table::ySubscriptXSize() {
  return data_->readShort(Offset::kYSubscriptXSize);
}

int32_t OS2Table::ySubscriptYSize() {
  return data_->readShort(Offset::kYSubscriptYSize);
}

int32_t OS2Table::ySubscriptXOffset() {
  return data_->readShort(Offset::kYSubscriptXOffset);
}

int32_t OS2Table::ySubscriptYOffset() {
  return data_->readShort(Offset::kYSubscriptYOffset);
}

int32_t OS2Table::ySuperscriptXSize() {
  return data_->readShort(Offset::kYSuperscriptXSize);
}

int32_t OS2Table::ySuperscriptYSize() {
  return data_->readShort(Offset::kYSuperscriptYSize);
}

int32_t OS2Table::ySuperscriptXOffset() {
  return data_->readShort(Offset::kYSuperscriptXOffset);
}

int32_t OS2Table::ySuperscriptYOffset() {
  return data_->readShort(Offset::kYSuperscriptYOffset);
}

int32_t OS2Table::yStrikeoutSize() {
  return data_->readShort(Offset::kYStrikeoutSize);
}

int32_t OS2Table::yStrikeoutPosition() {
  return data_->readShort(Offset::kYStrikeoutPosition);
}

int32_t OS2Table::sFamilyClass() {
  return data_->readShort(Offset::kSFamilyClass);
}

void OS2Table::panose(ByteVector* value) {
  value->clear();
  value->resize(10);
  data_->readBytes(Offset::kPanose, value, 0, 10);
}

int64_t OS2Table::ulUnicodeRange1() {
  return data_->readULong(Offset::kUlUnicodeRange1);
}

int64_t OS2Table::ulUnicodeRange2() {
  return data_->readULong(Offset::kUlUnicodeRange2);
}

int64_t OS2Table::ulUnicodeRange3() {
  return data_->readULong(Offset::kUlUnicodeRange3);
}

int64_t OS2Table::ulUnicodeRange4() {
  return data_->readULong(Offset::kUlUnicodeRange4);
}

void OS2Table::achVendId(ByteVector* b) {
  b->clear();
  b->resize(4);
  data_->readBytes(Offset::kAchVendId, b, 0, 4);
}

int32_t OS2Table::fsSelection() {
  return data_->readUShort(Offset::kFsSelection);
}

int32_t OS2Table::usFirstCharIndex() {
  return data_->readUShort(Offset::kUsFirstCharIndex);
}

int32_t OS2Table::usLastCharIndex() {
  return data_->readUShort(Offset::kUsLastCharIndex);
}

int32_t OS2Table::sTypoAscender() {
  return data_->readShort(Offset::kSTypoAscender);
}

int32_t OS2Table::sTypoDecender() {
  return data_->readShort(Offset::kSTypoDescender);
}

int32_t OS2Table::sTypoLineGap() {
  return data_->readShort(Offset::kSTypoLineGap);
}

int32_t OS2Table::usWinAscent() {
  return data_->readUShort(Offset::kUsWinAscent);
}

int32_t OS2Table::usWinDescent() {
  return data_->readUShort(Offset::kUsWinDescent);
}

int64_t OS2Table::ulCodePageRange1() {
  return data_->readULong(Offset::kUlCodePageRange1);
}

int64_t OS2Table::ulCodePageRange2() {
  return data_->readULong(Offset::kUlCodePageRange2);
}

int64_t OS2Table::ulCodePageRange() {
  return ((0xffffffff & ulCodePageRange2()) << 32) |
         (0xffffffff & ulCodePageRange1());
}

int32_t OS2Table::sxHeight() {
  return data_->readShort(Offset::kSxHeight);
}

int32_t OS2Table::usDefaultChar() {
  return data_->readUShort(Offset::kUsDefaultChar);
}

int32_t OS2Table::usBreakChar() {
  return data_->readUShort(Offset::kUsBreakChar);
}

int32_t OS2Table::usMaxContext() {
  return data_->readUShort(Offset::kUsMaxContext);
}

/******************************************************************************
 * class OS2Table::Builder
 ******************************************************************************/
OS2Table::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                           Header* header, WritableFontData* data) :
    Table::TableBasedTableBuilder(font_builder, header, data) {
}

OS2Table::Builder::~Builder() {}

CALLER_ATTACH FontDataTable* OS2Table::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table = new OS2Table(header(), data);
  return table.detach();
}

}  // namespace sfntly

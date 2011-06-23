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

#include "sfntly/font_header_table.h"

namespace sfntly {
/******************************************************************************
 * Constants
 ******************************************************************************/
const int32_t FontHeaderTable::Offset::kTableVersion = 0;
const int32_t FontHeaderTable::Offset::kFontRevision = 4;
const int32_t FontHeaderTable::Offset::kCheckSumAdjustment = 8;
const int32_t FontHeaderTable::Offset::kMagicNumber = 12;
const int32_t FontHeaderTable::Offset::kFlags = 16;
const int32_t FontHeaderTable::Offset::kUnitsPerEm = 18;
const int32_t FontHeaderTable::Offset::kCreated = 20;
const int32_t FontHeaderTable::Offset::kModified = 28;
const int32_t FontHeaderTable::Offset::kXMin = 36;
const int32_t FontHeaderTable::Offset::kYMin = 38;
const int32_t FontHeaderTable::Offset::kXMax = 40;
const int32_t FontHeaderTable::Offset::kYMax = 42;
const int32_t FontHeaderTable::Offset::kMacStyle = 44;
const int32_t FontHeaderTable::Offset::kLowestRecPPEM = 46;
const int32_t FontHeaderTable::Offset::kFontDirectionHint = 48;
const int32_t FontHeaderTable::Offset::kIndexToLocFormat = 50;
const int32_t FontHeaderTable::Offset::kGlyphDataFormat = 52;

const int32_t IndexToLocFormat::kShortOffset = 0;
const int32_t IndexToLocFormat::kLongOffset = 1;

const int32_t FontDirectionHint::kFullyMixed = 0;
const int32_t FontDirectionHint::kOnlyStrongLTR = 1;
const int32_t FontDirectionHint::kStrongLTRAndNeutral = 2;
const int32_t FontDirectionHint::kOnlyStrongRTL = -1;
const int32_t FontDirectionHint::kStrongRTLAndNeutral = -2;

/******************************************************************************
 * FontHeaderTable class
 ******************************************************************************/
FontHeaderTable::FontHeaderTable(Header* header, ReadableFontData* data)
    : Table(header, data) {
  IntegerList checksum_ranges;
  checksum_ranges.push_back(0);
  checksum_ranges.push_back(Offset::kCheckSumAdjustment);
  checksum_ranges.push_back(Offset::kMagicNumber);
  data_->setCheckSumRanges(checksum_ranges);
}

FontHeaderTable::~FontHeaderTable() {}

int32_t FontHeaderTable::tableVersion() {
  return data_->readFixed(Offset::kTableVersion);
}

int32_t FontHeaderTable::fontRevision() {
  return data_->readFixed(Offset::kFontRevision);
}

int64_t FontHeaderTable::checksumAdjustment() {
  return data_->readULong(Offset::kCheckSumAdjustment);
}

int64_t FontHeaderTable::magicNumber() {
  return data_->readULong(Offset::kMagicNumber);
}

int32_t FontHeaderTable::flagsAsInt() {
  return data_->readUShort(Offset::kFlags);
}

int32_t FontHeaderTable::unitsPerEm() {
  return data_->readUShort(Offset::kUnitsPerEm);
}

int64_t FontHeaderTable::created() {
  return data_->readDateTimeAsLong(Offset::kCreated);
}

int64_t FontHeaderTable::modified() {
  return data_->readDateTimeAsLong(Offset::kModified);
}

int32_t FontHeaderTable::xMin() {
  return data_->readUShort(Offset::kXMin);
}

int32_t FontHeaderTable::yMin() {
  return data_->readUShort(Offset::kYMin);
}

int32_t FontHeaderTable::xMax() {
  return data_->readUShort(Offset::kXMax);
}

int32_t FontHeaderTable::yMax() {
  return data_->readUShort(Offset::kYMax);
}

int32_t FontHeaderTable::macStyleAsInt() {
  return data_->readUShort(Offset::kMacStyle);
}

int32_t FontHeaderTable::lowestRecPPEM() {
  return data_->readUShort(Offset::kLowestRecPPEM);
}

int32_t FontHeaderTable::fontDirectionHint() {
  return data_->readShort(Offset::kFontDirectionHint);
}

int32_t FontHeaderTable::indexToLocFormat() {
  return data_->readShort(Offset::kIndexToLocFormat);
}

int32_t FontHeaderTable::glyphDataFormat() {
  return data_->readShort(Offset::kGlyphDataFormat);
}

/******************************************************************************
 * FontHeaderTable::Builder class
 ******************************************************************************/
FontHeaderTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                                  Header* header, WritableFontData* data) :
    Table::TableBasedTableBuilder(font_builder, header, data) {
}

FontHeaderTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                                  Header* header, ReadableFontData* data) :
    Table::TableBasedTableBuilder(font_builder, header, data) {
}

FontHeaderTable::Builder::~Builder() {}

CALLER_ATTACH FontDataTable* FontHeaderTable::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table = new FontHeaderTable(header(), data);
  return table.detach();
}

int32_t FontHeaderTable::Builder::tableVersion() {
  return down_cast<FontHeaderTable*>(table())->tableVersion();
}

void FontHeaderTable::Builder::setTableVersion(int32_t version) {
  internalWriteData()->writeFixed(Offset::kTableVersion, version);
}

int32_t FontHeaderTable::Builder::fontRevision() {
  return down_cast<FontHeaderTable*>(table())->fontRevision();
}

void FontHeaderTable::Builder::setFontRevision(int32_t revision) {
  internalWriteData()->writeFixed(Offset::kFontRevision, revision);
}

int64_t FontHeaderTable::Builder::checksumAdjustment() {
  return down_cast<FontHeaderTable*>(table())->checksumAdjustment();
}

void FontHeaderTable::Builder::setChecksumAdjustment(int64_t adjustment) {
  internalWriteData()->writeULong(Offset::kCheckSumAdjustment, adjustment);
}

int64_t FontHeaderTable::Builder::magicNumber() {
  return down_cast<FontHeaderTable*>(table())->magicNumber();
}

void FontHeaderTable::Builder::setMagicNumber(int64_t magic_number) {
  internalWriteData()->writeULong(Offset::kMagicNumber, magic_number);
}

int32_t FontHeaderTable::Builder::flagsAsInt() {
  return down_cast<FontHeaderTable*>(table())->flagsAsInt();
}

void FontHeaderTable::Builder::setFlagsAsInt(int32_t flags) {
  internalWriteData()->writeUShort(Offset::kFlags, flags);
}

int32_t FontHeaderTable::Builder::unitsPerEm() {
  return down_cast<FontHeaderTable*>(table())->unitsPerEm();
}

void FontHeaderTable::Builder::setUnitsPerEm(int32_t units) {
  internalWriteData()->writeUShort(Offset::kUnitsPerEm, units);
}

int64_t FontHeaderTable::Builder::created() {
  return down_cast<FontHeaderTable*>(table())->modified();
}

void FontHeaderTable::Builder::setCreated(int64_t date) {
  internalWriteData()->writeDateTime(Offset::kCreated, date);
}

int64_t FontHeaderTable::Builder::modified() {
  return down_cast<FontHeaderTable*>(table())->modified();
}

void FontHeaderTable::Builder::setModified(int64_t date) {
  internalWriteData()->writeDateTime(Offset::kModified, date);
}

int32_t FontHeaderTable::Builder::xMin() {
  return down_cast<FontHeaderTable*>(table())->xMin();
}

void FontHeaderTable::Builder::setXMin(int32_t xmin) {
  internalWriteData()->writeShort(Offset::kXMin, xmin);
}

int32_t FontHeaderTable::Builder::yMin() {
  return down_cast<FontHeaderTable*>(table())->yMin();
}

void FontHeaderTable::Builder::setYMin(int32_t ymin) {
  internalWriteData()->writeShort(Offset::kYMin, ymin);
}

int32_t FontHeaderTable::Builder::xMax() {
  return down_cast<FontHeaderTable*>(table())->xMax();
}

void FontHeaderTable::Builder::setXMax(int32_t xmax) {
  internalWriteData()->writeShort(Offset::kXMax, xmax);
}

int32_t FontHeaderTable::Builder::yMax() {
  return down_cast<FontHeaderTable*>(table())->yMax();
}

void FontHeaderTable::Builder::setYMax(int32_t ymax) {
  internalWriteData()->writeShort(Offset::kYMax, ymax);
}

int32_t FontHeaderTable::Builder::macStyleAsInt() {
  return down_cast<FontHeaderTable*>(table())->macStyleAsInt();
}

void FontHeaderTable::Builder::setMacStyleAsInt(int32_t style) {
  internalWriteData()->writeUShort(Offset::kMacStyle, style);
}

int32_t FontHeaderTable::Builder::lowestRecPPEM() {
  return down_cast<FontHeaderTable*>(table())->lowestRecPPEM();
}

void FontHeaderTable::Builder::setLowestRecPPEM(int32_t size) {
  internalWriteData()->writeUShort(Offset::kLowestRecPPEM, size);
}

int32_t FontHeaderTable::Builder::fontDirectionHint() {
  return down_cast<FontHeaderTable*>(table())->fontDirectionHint();
}

void FontHeaderTable::Builder::setFontDirectionHint(int32_t hint) {
  internalWriteData()->writeShort(Offset::kFontDirectionHint, hint);
}

int32_t FontHeaderTable::Builder::indexToLocFormat() {
  return down_cast<FontHeaderTable*>(table())->indexToLocFormat();
}

void FontHeaderTable::Builder::setIndexToLocFormat(int32_t format) {
  internalWriteData()->writeShort(Offset::kIndexToLocFormat, format);
}

int32_t FontHeaderTable::Builder::glyphDataFormat() {
  return down_cast<FontHeaderTable*>(table())->glyphDataFormat();
}

void FontHeaderTable::Builder::setGlyphDataFormat(int32_t format) {
  internalWriteData()->writeShort(Offset::kGlyphDataFormat, format);
}

}  // namespace sfntly

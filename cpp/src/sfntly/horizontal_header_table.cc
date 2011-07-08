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

// Note: original Java version is inconsistent itself (e.g. readUShort in
//       Builder but readShort in Table.  Need Fix.
// TODO(arthurhsu): check with Stuart G and fix it.

#include "sfntly/horizontal_header_table.h"

namespace sfntly {
/******************************************************************************
 * HorizontalHeaderTable class
 ******************************************************************************/
HorizontalHeaderTable:: HorizontalHeaderTable(Header* header,
                                              ReadableFontData* data) :
    Table(header, data) {}

HorizontalHeaderTable:: ~HorizontalHeaderTable() {}

int32_t HorizontalHeaderTable::version() {
  return data_->readFixed(Offset::kVersion);
}

int32_t HorizontalHeaderTable::ascender() {
  return data_->readShort(Offset::kAscender);
}

int32_t HorizontalHeaderTable::descender() {
  return data_->readShort(Offset::kDescender);
}

int32_t HorizontalHeaderTable::lineGap() {
  return data_->readShort(Offset::kLineGap);
}

int32_t HorizontalHeaderTable::advanceWidthMax() {
  return data_->readUShort(Offset::kAdvanceWidthMax);
}

int32_t HorizontalHeaderTable::minLeftSideBearing() {
  return data_->readShort(Offset::kMinLeftSideBearing);
}

int32_t HorizontalHeaderTable::minRightSideBearing() {
  return data_->readShort(Offset::kMinRightSideBearing);
}

int32_t HorizontalHeaderTable::xMaxExtent() {
  return data_->readShort(Offset::kXMaxExtent);
}

int32_t HorizontalHeaderTable::caretSlopeRise() {
  return data_->readShort(Offset::kCaretSlopeRise);
}

int32_t HorizontalHeaderTable::caretSlopeRun() {
  return data_->readShort(Offset::kCaretSlopeRun);
}

int32_t HorizontalHeaderTable::caretOffset() {
  return data_->readShort(Offset::kCaretOffset);
}

int32_t HorizontalHeaderTable::metricDataFormat() {
  return data_->readShort(Offset::kMetricDataFormat);
}

int32_t HorizontalHeaderTable::numberOfHMetrics() {
  return data_->readUShort(Offset::kNumberOfHMetrics);
}

/******************************************************************************
 * HorizontalHeaderTable::Builder class
 ******************************************************************************/
HorizontalHeaderTable::Builder::Builder(
    FontDataTableBuilderContainer* font_builder, Header* header,
    WritableFontData* data) :
    Table::TableBasedTableBuilder(font_builder, header, data) {
}

HorizontalHeaderTable::Builder::Builder(
    FontDataTableBuilderContainer* font_builder, Header* header,
    ReadableFontData* data) :
    Table::TableBasedTableBuilder(font_builder, header, data) {
}

HorizontalHeaderTable::Builder::~Builder() {}

CALLER_ATTACH FontDataTable* HorizontalHeaderTable::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table = new HorizontalHeaderTable(header(), data);
  return table.detach();
}

int32_t HorizontalHeaderTable::Builder::version() {
  return internalReadData()->readFixed(Offset::kVersion);
}

void HorizontalHeaderTable::Builder::setVersion(int32_t version) {
  internalWriteData()->writeFixed(Offset::kVersion, version);
}

int32_t HorizontalHeaderTable::Builder::ascender() {
  return internalReadData()->readUShort(Offset::kAscender);
}

void HorizontalHeaderTable::Builder::setAscender(int32_t ascender) {
  internalWriteData()->writeUShort(Offset::kVersion, ascender);
}

int32_t HorizontalHeaderTable::Builder::descender() {
  return internalReadData()->readUShort(Offset::kDescender);
}

void HorizontalHeaderTable::Builder::setDescender(int32_t descender) {
  internalWriteData()->writeUShort(Offset::kDescender, descender);
}

int32_t HorizontalHeaderTable::Builder::lineGap() {
  return internalReadData()->readUShort(Offset::kLineGap);
}

void HorizontalHeaderTable::Builder::setLineGap(int32_t line_gap) {
  internalWriteData()->writeUShort(Offset::kLineGap, line_gap);
}

int32_t HorizontalHeaderTable::Builder::advanceWidthMax() {
  return internalReadData()->readUShort(Offset::kAdvanceWidthMax);
}

void HorizontalHeaderTable::Builder::setAdvanceWidthMax(int32_t value) {
  internalWriteData()->writeUShort(Offset::kAdvanceWidthMax, value);
}

int32_t HorizontalHeaderTable::Builder::minLeftSideBearing() {
  return internalReadData()->readUShort(Offset::kMinLeftSideBearing);
}

void HorizontalHeaderTable::Builder::setMinLeftSideBearing(int32_t value) {
  internalWriteData()->writeUShort(Offset::kMinLeftSideBearing, value);
}

int32_t HorizontalHeaderTable::Builder::minRightSideBearing() {
  return internalReadData()->readUShort(Offset::kMinRightSideBearing);
}

void HorizontalHeaderTable::Builder::setMinRightSideBearing(int32_t value) {
  internalWriteData()->writeUShort(Offset::kMinRightSideBearing, value);
}

int32_t HorizontalHeaderTable::Builder::xMaxExtent() {
  return internalReadData()->readUShort(Offset::kXMaxExtent);
}

void HorizontalHeaderTable::Builder::setXMaxExtent(int32_t value) {
  internalWriteData()->writeUShort(Offset::kXMaxExtent, value);
}

int32_t HorizontalHeaderTable::Builder::caretSlopeRise() {
  return internalReadData()->readUShort(Offset::kCaretSlopeRise);
}

void HorizontalHeaderTable::Builder::setCaretSlopeRise(int32_t value) {
  internalWriteData()->writeUShort(Offset::kCaretSlopeRise, value);
}

int32_t HorizontalHeaderTable::Builder::caretSlopeRun() {
  return internalReadData()->readUShort(Offset::kCaretSlopeRun);
}

void HorizontalHeaderTable::Builder::setCaretSlopeRun(int32_t value) {
  internalWriteData()->writeUShort(Offset::kCaretSlopeRun, value);
}

int32_t HorizontalHeaderTable::Builder::caretOffset() {
  return internalReadData()->readUShort(Offset::kCaretOffset);
}

void HorizontalHeaderTable::Builder::setCaretOffset(int32_t value) {
  internalWriteData()->writeUShort(Offset::kCaretOffset, value);
}

int32_t HorizontalHeaderTable::Builder::metricDataFormat() {
  return internalReadData()->readUShort(Offset::kMetricDataFormat);
}

void HorizontalHeaderTable::Builder::setMetricDataFormat(int32_t value) {
  internalWriteData()->writeUShort(Offset::kMetricDataFormat, value);
}

int32_t HorizontalHeaderTable::Builder::numberOfHMetrics() {
  return internalReadData()->readUShort(Offset::kNumberOfHMetrics);
}

void HorizontalHeaderTable::Builder::setNumberOfHMetrics(int32_t value) {
  internalWriteData()->writeUShort(Offset::kNumberOfHMetrics, value);
}

}  // namespace sfntly

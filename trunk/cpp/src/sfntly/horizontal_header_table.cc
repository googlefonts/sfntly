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
HorizontalHeaderTable:: ~HorizontalHeaderTable() {}

int32_t HorizontalHeaderTable::Version() {
  return data_->ReadFixed(Offset::kVersion);
}

int32_t HorizontalHeaderTable::Ascender() {
  return data_->ReadShort(Offset::kAscender);
}

int32_t HorizontalHeaderTable::Descender() {
  return data_->ReadShort(Offset::kDescender);
}

int32_t HorizontalHeaderTable::LineGap() {
  return data_->ReadShort(Offset::kLineGap);
}

int32_t HorizontalHeaderTable::AdvanceWidthMax() {
  return data_->ReadUShort(Offset::kAdvanceWidthMax);
}

int32_t HorizontalHeaderTable::MinLeftSideBearing() {
  return data_->ReadShort(Offset::kMinLeftSideBearing);
}

int32_t HorizontalHeaderTable::MinRightSideBearing() {
  return data_->ReadShort(Offset::kMinRightSideBearing);
}

int32_t HorizontalHeaderTable::XMaxExtent() {
  return data_->ReadShort(Offset::kXMaxExtent);
}

int32_t HorizontalHeaderTable::CaretSlopeRise() {
  return data_->ReadShort(Offset::kCaretSlopeRise);
}

int32_t HorizontalHeaderTable::CaretSlopeRun() {
  return data_->ReadShort(Offset::kCaretSlopeRun);
}

int32_t HorizontalHeaderTable::CaretOffset() {
  return data_->ReadShort(Offset::kCaretOffset);
}

int32_t HorizontalHeaderTable::MetricDataFormat() {
  return data_->ReadShort(Offset::kMetricDataFormat);
}

int32_t HorizontalHeaderTable::NumberOfHMetrics() {
  return data_->ReadUShort(Offset::kNumberOfHMetrics);
}

HorizontalHeaderTable:: HorizontalHeaderTable(Header* header,
                                              ReadableFontData* data)
    : Table(header, data) {
}

/******************************************************************************
 * HorizontalHeaderTable::Builder class
 ******************************************************************************/
HorizontalHeaderTable::Builder::Builder(
    FontDataTableBuilderContainer* font_builder,
    Header* header,
    WritableFontData* data)
    : Table::TableBasedTableBuilder(font_builder, header, data) {
}

HorizontalHeaderTable::Builder::Builder(
    FontDataTableBuilderContainer* font_builder,
    Header* header,
    ReadableFontData* data)
    : Table::TableBasedTableBuilder(font_builder, header, data) {
}

HorizontalHeaderTable::Builder::~Builder() {}

CALLER_ATTACH FontDataTable*
    HorizontalHeaderTable::Builder::SubBuildTable(ReadableFontData* data) {
  FontDataTablePtr table = new HorizontalHeaderTable(header(), data);
  return table.Detach();
}

int32_t HorizontalHeaderTable::Builder::Version() {
  return InternalReadData()->ReadFixed(Offset::kVersion);
}

void HorizontalHeaderTable::Builder::SetVersion(int32_t version) {
  InternalWriteData()->WriteFixed(Offset::kVersion, version);
}

int32_t HorizontalHeaderTable::Builder::Ascender() {
  return InternalReadData()->ReadUShort(Offset::kAscender);
}

void HorizontalHeaderTable::Builder::SetAscender(int32_t ascender) {
  InternalWriteData()->WriteUShort(Offset::kVersion, ascender);
}

int32_t HorizontalHeaderTable::Builder::Descender() {
  return InternalReadData()->ReadUShort(Offset::kDescender);
}

void HorizontalHeaderTable::Builder::SetDescender(int32_t descender) {
  InternalWriteData()->WriteUShort(Offset::kDescender, descender);
}

int32_t HorizontalHeaderTable::Builder::LineGap() {
  return InternalReadData()->ReadUShort(Offset::kLineGap);
}

void HorizontalHeaderTable::Builder::SetLineGap(int32_t line_gap) {
  InternalWriteData()->WriteUShort(Offset::kLineGap, line_gap);
}

int32_t HorizontalHeaderTable::Builder::AdvanceWidthMax() {
  return InternalReadData()->ReadUShort(Offset::kAdvanceWidthMax);
}

void HorizontalHeaderTable::Builder::SetAdvanceWidthMax(int32_t value) {
  InternalWriteData()->WriteUShort(Offset::kAdvanceWidthMax, value);
}

int32_t HorizontalHeaderTable::Builder::MinLeftSideBearing() {
  return InternalReadData()->ReadUShort(Offset::kMinLeftSideBearing);
}

void HorizontalHeaderTable::Builder::SetMinLeftSideBearing(int32_t value) {
  InternalWriteData()->WriteUShort(Offset::kMinLeftSideBearing, value);
}

int32_t HorizontalHeaderTable::Builder::MinRightSideBearing() {
  return InternalReadData()->ReadUShort(Offset::kMinRightSideBearing);
}

void HorizontalHeaderTable::Builder::SetMinRightSideBearing(int32_t value) {
  InternalWriteData()->WriteUShort(Offset::kMinRightSideBearing, value);
}

int32_t HorizontalHeaderTable::Builder::XMaxExtent() {
  return InternalReadData()->ReadUShort(Offset::kXMaxExtent);
}

void HorizontalHeaderTable::Builder::SetXMaxExtent(int32_t value) {
  InternalWriteData()->WriteUShort(Offset::kXMaxExtent, value);
}

int32_t HorizontalHeaderTable::Builder::CaretSlopeRise() {
  return InternalReadData()->ReadUShort(Offset::kCaretSlopeRise);
}

void HorizontalHeaderTable::Builder::SetCaretSlopeRise(int32_t value) {
  InternalWriteData()->WriteUShort(Offset::kCaretSlopeRise, value);
}

int32_t HorizontalHeaderTable::Builder::CaretSlopeRun() {
  return InternalReadData()->ReadUShort(Offset::kCaretSlopeRun);
}

void HorizontalHeaderTable::Builder::SetCaretSlopeRun(int32_t value) {
  InternalWriteData()->WriteUShort(Offset::kCaretSlopeRun, value);
}

int32_t HorizontalHeaderTable::Builder::CaretOffset() {
  return InternalReadData()->ReadUShort(Offset::kCaretOffset);
}

void HorizontalHeaderTable::Builder::SetCaretOffset(int32_t value) {
  InternalWriteData()->WriteUShort(Offset::kCaretOffset, value);
}

int32_t HorizontalHeaderTable::Builder::MetricDataFormat() {
  return InternalReadData()->ReadUShort(Offset::kMetricDataFormat);
}

void HorizontalHeaderTable::Builder::SetMetricDataFormat(int32_t value) {
  InternalWriteData()->WriteUShort(Offset::kMetricDataFormat, value);
}

int32_t HorizontalHeaderTable::Builder::NumberOfHMetrics() {
  return InternalReadData()->ReadUShort(Offset::kNumberOfHMetrics);
}

void HorizontalHeaderTable::Builder::SetNumberOfHMetrics(int32_t value) {
  InternalWriteData()->WriteUShort(Offset::kNumberOfHMetrics, value);
}

}  // namespace sfntly

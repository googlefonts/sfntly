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

#include "sfntly/horizontal_metrics_table.h"
#include "sfntly/port/exception_type.h"

namespace sfntly {
/******************************************************************************
 * HorizontalMetricsTable class
 ******************************************************************************/
HorizontalMetricsTable::HorizontalMetricsTable(Header* header,
                                               ReadableFontData* data) :
    Table(header, data) {
}

HorizontalMetricsTable::HorizontalMetricsTable(Header* header,
                                               ReadableFontData* data,
                                               int32_t num_hmetrics,
                                               int32_t num_glyphs) :
    Table(header, data), num_hmetrics_(num_hmetrics), num_glyphs_(num_glyphs) {
}

HorizontalMetricsTable::~HorizontalMetricsTable() {}

int32_t HorizontalMetricsTable::numberOfHMetrics() {
  return num_hmetrics_;
}

int32_t HorizontalMetricsTable::numberOfLSBs() {
  return num_glyphs_ - num_hmetrics_;
}

int32_t HorizontalMetricsTable::hMetricAdvanceWidth(int32_t entry) {
  if (entry > num_hmetrics_) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException();
#endif
  }
  int32_t offset = Offset::kHMetricsStart + (entry * Offset::kHMetricsSize) +
                   Offset::kHMetricsAdvanceWidth;
  return data_->readUShort(offset);
}

int32_t HorizontalMetricsTable::hMetricLSB(int32_t entry) {
  if (entry > num_hmetrics_) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException();
#endif
  }
  int32_t offset = Offset::kHMetricsStart + (entry * Offset::kHMetricsSize) +
                   Offset::kHMetricsLeftSideBearing;
  return data_->readShort(offset);
}

int32_t HorizontalMetricsTable::lsbTableEntry(int32_t entry) {
  if (entry > num_hmetrics_) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException();
#endif
  }
  int32_t offset = Offset::kHMetricsStart + (entry * Offset::kHMetricsSize) +
                   Offset::kLeftSideBearingSize;
  return data_->readShort(offset);
}

int32_t HorizontalMetricsTable::advanceWidth(int32_t glyph_id) {
  if (glyph_id < num_hmetrics_) {
    return hMetricAdvanceWidth(glyph_id);
  }
  return hMetricAdvanceWidth(glyph_id - num_hmetrics_);
}

/******************************************************************************
 * HorizontalMetricsTable::Builder class
 ******************************************************************************/
void HorizontalMetricsTable::Builder::init() {
  num_hmetrics_ = -1;
  num_glyphs_ = -1;
}

HorizontalMetricsTable::Builder::Builder(
    FontDataTableBuilderContainer* font_builder, Header* header,
    WritableFontData* data) :
    Table::TableBasedTableBuilder(font_builder, header, data) {
  init();
}

HorizontalMetricsTable::Builder::Builder(
    FontDataTableBuilderContainer* font_builder, Header* header,
    ReadableFontData* data) :
    Table::TableBasedTableBuilder(font_builder, header, data) {
  init();
}

HorizontalMetricsTable::Builder::~Builder() {}

CALLER_ATTACH FontDataTable* HorizontalMetricsTable::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table =
      new HorizontalMetricsTable(header(), data, num_hmetrics_, num_glyphs_);
  return table.detach();
}

void HorizontalMetricsTable::Builder::setNumberOfHMetrics(
                                          int32_t num_hmetrics) {
  assert(num_hmetrics >= 0);
  num_hmetrics_ = num_hmetrics;
  HorizontalMetricsTable* table = down_cast<HorizontalMetricsTable*>(
                                      this->table());
  table->num_hmetrics_ = num_hmetrics;
}

void HorizontalMetricsTable::Builder::setNumGlyphs(int32_t num_glyphs) {
  assert(num_glyphs >= 0);
  num_glyphs_ = num_glyphs;
  HorizontalMetricsTable* table = down_cast<HorizontalMetricsTable*>(
                                      this->table());
  table->num_glyphs_ = num_glyphs;
}

}  // namespace sfntly

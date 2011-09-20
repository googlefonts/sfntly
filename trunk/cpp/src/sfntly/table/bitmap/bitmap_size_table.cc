/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0  = the "License");
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

#include "sfntly/table/bitmap/bitmap_size_table.h"

#include "sfntly/table/bitmap/eblc_table.h"

namespace sfntly {

BitmapSizeTable::BitmapSizeTable(ReadableFontData* data,
                                 ReadableFontData* master_data)
    : SubTable(data) {
  master_data_ = master_data;
}

BitmapSizeTable::~BitmapSizeTable() {
}

int32_t BitmapSizeTable::IndexSubTableArrayOffset() {
  return data_->ReadULongAsInt(
      EblcTable::Offset::kBitmapSizeTable_indexSubTableArrayOffset);
}

int32_t BitmapSizeTable::IndexTableSize() {
  return data_->ReadULongAsInt(
      EblcTable::Offset::kBitmapSizeTable_indexTableSize);
}

int32_t BitmapSizeTable::NumberOfIndexSubTables() {
  return data_->ReadULongAsInt(
      EblcTable::Offset::kBitmapSizeTable_numberOfIndexSubTables);
}

int32_t BitmapSizeTable::ColorRef() {
  return data_->ReadULongAsInt(EblcTable::Offset::kBitmapSizeTable_colorRef);
}

int32_t BitmapSizeTable::StartGlyphIndex() {
  return data_->ReadUShort(EblcTable::Offset::kBitmapSizeTable_startGlyphIndex);
}

int32_t BitmapSizeTable::EndGlyphIndex() {
  return data_->ReadUShort(EblcTable::Offset::kBitmapSizeTable_endGlyphIndex);
}

int32_t BitmapSizeTable::PpemX() {
  return data_->ReadByte(EblcTable::Offset::kBitmapSizeTable_ppemX);
}

int32_t BitmapSizeTable::PpemY() {
  return data_->ReadByte(EblcTable::Offset::kBitmapSizeTable_ppemY);
}

int32_t BitmapSizeTable::BitDepth() {
  return data_->ReadByte(EblcTable::Offset::kBitmapSizeTable_bitDepth);
}

int32_t BitmapSizeTable::FlagsAsInt() {
  return data_->ReadChar(EblcTable::Offset::kBitmapSizeTable_flags);
}

IndexSubTable* BitmapSizeTable::GetIndexSubTable(int32_t index) {
  IndexSubTableList* subtable_list = GetIndexSubTableList();
  if (index >= 0 && (size_t)index < subtable_list->size()) {
    return (*subtable_list)[index];
  }
  return NULL;
}

int32_t BitmapSizeTable::GlyphOffset(int32_t glyph_id) {
  IndexSubTable* subtable = SearchIndexSubTables(glyph_id);
  if (subtable == NULL) {
    return -1;
  }
  return subtable->GlyphOffset(glyph_id);
}

int32_t BitmapSizeTable::GlyphLength(int32_t glyph_id) {
  IndexSubTable* subtable = SearchIndexSubTables(glyph_id);
  if (subtable == NULL) {
    return -1;
  }
  return subtable->GlyphLength(glyph_id);
}

int32_t BitmapSizeTable::GlyphFormat(int32_t glyph_id) {
  IndexSubTable* subtable = SearchIndexSubTables(glyph_id);
  if (subtable == NULL) {
    return -1;
  }
  return subtable->image_format();
}

IndexSubTable* BitmapSizeTable::SearchIndexSubTables(int32_t glyph_id) {
  IndexSubTableList* subtable_list = GetIndexSubTableList();
  int32_t index = 0;
  int32_t bottom = 0;
  int32_t top = subtable_list->size();
  while (top != bottom) {
    index = (top + bottom) / 2;
    IndexSubTable* subtable = (*subtable_list)[index];
    if (glyph_id < subtable->first_glyph_index()) {
      // Location beow current location
      top = index;
    } else {
      if (glyph_id <= subtable->last_glyph_index()) {
        return subtable;
      } else {
        bottom = index + 1;
      }
    }
  }
  return NULL;
}

CALLER_ATTACH
IndexSubTable* BitmapSizeTable::CreateIndexSubTable(int32_t index) {
  return IndexSubTable::CreateIndexSubTable(master_data_,
                                            IndexSubTableArrayOffset(),
                                            index);
}

IndexSubTableList* BitmapSizeTable::GetIndexSubTableList() {
  AutoLock lock(atomic_subtables_lock_);
  if (atomic_subtables.empty()) {
    for (int32_t i = 0; i < NumberOfIndexSubTables(); ++i) {
      IndexSubTablePtr table;
      table.Attach(CreateIndexSubTable(i));
      atomic_subtables.push_back(table);
    }
  }
  return &atomic_subtables;
}

}  // namespace sfntly

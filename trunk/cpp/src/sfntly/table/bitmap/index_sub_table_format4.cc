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

#include "sfntly/table/bitmap/index_sub_table_format4.h"

#include "sfntly/table/bitmap/eblc_table.h"

namespace sfntly {

// static
int32_t IndexSubTableFormat4::GetDataLength(ReadableFontData* data,
                                            int32_t offset,
                                            int32_t first,
                                            int32_t last) {
  UNREFERENCED_PARAMETER(first);
  UNREFERENCED_PARAMETER(last);
  assert(data);
  return data->ReadULongAsInt(offset +
                              EblcTable::Offset::kIndexSubTable4_numGlyphs) *
         EblcTable::Offset::kCodeOffsetPairLength;
}

IndexSubTableFormat4::IndexSubTableFormat4(ReadableFontData* data,
                                           int32_t first,
                                           int32_t last)
    : IndexSubTable(data, first, last) {
}

IndexSubTableFormat4::~IndexSubTableFormat4() {
}

int32_t IndexSubTableFormat4::NumGlyphs() {
  return data_->ReadULongAsInt(EblcTable::Offset::kIndexSubTable4_numGlyphs);
}

int32_t IndexSubTableFormat4::GlyphOffset(int32_t glyph_id) {
  if (CheckGlyphRange(glyph_id)) {
    int32_t pair_index = FindCodeOffsetPair(glyph_id);
    if (pair_index < 0) {
      return -1;
    }
    return data_->ReadUShort(
               EblcTable::Offset::kIndexSubTable4_glyphArray +
               pair_index * EblcTable::Offset::kCodeOffsetPairLength +
               EblcTable::Offset::kCodeOffsetPair_offset);
  }
  return -1;
}

int32_t IndexSubTableFormat4::GlyphLength(int32_t glyph_id) {
  if (CheckGlyphRange(glyph_id)) {
    int32_t pair_index = FindCodeOffsetPair(glyph_id);
    if (pair_index < 0) {
      return -1;
    }
    return data_->ReadUShort(
               EblcTable::Offset::kIndexSubTable4_glyphArray +
               (pair_index + 1) * EblcTable::Offset::kCodeOffsetPairLength +
               EblcTable::Offset::kCodeOffsetPair_offset) -
           data_->ReadUShort(
               EblcTable::Offset::kIndexSubTable4_glyphArray +
               (pair_index) * EblcTable::Offset::kCodeOffsetPairLength +
               EblcTable::Offset::kCodeOffsetPair_offset);
  }
  return -1;
}

int32_t IndexSubTableFormat4::FindCodeOffsetPair(int32_t glyph_id) {
  return data_->SearchUShort(EblcTable::Offset::kIndexSubTable4_glyphArray,
                             EblcTable::Offset::kCodeOffsetPairLength,
                             NumGlyphs(),
                             glyph_id);
}

}  // namespace sfntly

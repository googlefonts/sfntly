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

#include "sfntly/table/bitmap/index_sub_table_format5.h"

#include "sfntly/table/bitmap/eblc_table.h"

namespace sfntly {

// static
int32_t IndexSubTableFormat5::GetDataLength(ReadableFontData* data,
                                            int32_t offset,
                                            int32_t first,
                                            int32_t last) {
  UNREFERENCED_PARAMETER(first);
  UNREFERENCED_PARAMETER(last);
  assert(data);
  return data->ReadULongAsInt(offset +
                              EblcTable::Offset::kIndexSubTable5_numGlyphs) *
         EblcTable::Offset::kCodeOffsetPairLength;
}

IndexSubTableFormat5::IndexSubTableFormat5(ReadableFontData* data,
                                           int32_t first,
                                           int32_t last)
    : IndexSubTable(data, first, last) {
}

IndexSubTableFormat5::~IndexSubTableFormat5() {
}

int32_t IndexSubTableFormat5::NumGlyphs() {
  return data_->ReadULongAsInt(EblcTable::Offset::kIndexSubTable5_numGlyphs);
}

int32_t IndexSubTableFormat5::GlyphOffset(int32_t glyph_id) {
  if (CheckGlyphRange(glyph_id)) {
    return data_->ReadUShort(
               EblcTable::Offset::kIndexSubTable5_glyphArray +
               glyph_id * DataSize::kUSHORT);
  }
  return -1;
}

int32_t IndexSubTableFormat5::GlyphLength(int32_t glyph_id) {
  if (CheckGlyphRange(glyph_id)) {
    return data_->ReadULongAsInt(EblcTable::Offset::kIndexSubTable5_imageSize);
  }
  return -1;
}

}  // namespace sfntly

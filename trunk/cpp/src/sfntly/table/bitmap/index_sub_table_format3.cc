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

#include "sfntly/table/bitmap/index_sub_table_format3.h"

#include "sfntly/table/bitmap/eblc_table.h"

namespace sfntly {

// static
int32_t IndexSubTableFormat3::GetDataLength(ReadableFontData* data,
                                            int32_t offset,
                                            int32_t first,
                                            int32_t last) {
  UNREFERENCED_PARAMETER(data);
  UNREFERENCED_PARAMETER(offset);
  return (last - first + 1 + 1) * DataSize::kUSHORT;
}

IndexSubTableFormat3::IndexSubTableFormat3(ReadableFontData* data,
                                           int32_t first,
                                           int32_t last)
    : IndexSubTable(data, first, last) {
}

IndexSubTableFormat3::~IndexSubTableFormat3() {
}

int32_t IndexSubTableFormat3::NumGlyphs() {
  return last_glyph_index() - first_glyph_index() + 1;
}

int32_t IndexSubTableFormat3::GlyphOffset(int32_t glyph_id) {
  if (CheckGlyphRange(glyph_id)) {
    return Loca(glyph_id);
  }
  return -1;
}

int32_t IndexSubTableFormat3::GlyphLength(int32_t glyph_id) {
  if (CheckGlyphRange(glyph_id)) {
    return Loca(glyph_id + 1) - Loca(glyph_id);
  }
  return -1;
}

int32_t IndexSubTableFormat3::Loca(int32_t loca_index) {
  int32_t read_offset =
      data_->ReadUShort(EblcTable::Offset::kIndexSubTable3_offsetArray +
                        (loca_index - first_glyph_index()) * DataSize::kUSHORT);
  return image_data_offset() + read_offset;
}

}  // namespace sfntly

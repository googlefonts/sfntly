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

#include "sfntly/table/bitmap/index_sub_table_format2.h"

#include "sfntly/table/bitmap/eblc_table.h"

namespace sfntly {

// static
int32_t IndexSubTableFormat2::GetDataLength(ReadableFontData* data,
                                            int32_t offset,
                                            int32_t first,
                                            int32_t last) {
  UNREFERENCED_PARAMETER(data);
  UNREFERENCED_PARAMETER(offset);
  UNREFERENCED_PARAMETER(first);
  UNREFERENCED_PARAMETER(last);
  return EblcTable::Offset::kIndexSubTable2Length;
}

IndexSubTableFormat2::IndexSubTableFormat2(ReadableFontData* data,
                                           int32_t first,
                                           int32_t last)
    : IndexSubTable(data, first, last) {
  image_size_ =
      data_->ReadULongAsInt(EblcTable::Offset::kIndexSubTable2_imageSize);
}

IndexSubTableFormat2::~IndexSubTableFormat2() {
}

int32_t IndexSubTableFormat2::NumGlyphs() {
  return last_glyph_index() - first_glyph_index() + 1;
}

int32_t IndexSubTableFormat2::GlyphOffset(int32_t glyph_id) {
  if (CheckGlyphRange(glyph_id)) {
    return image_data_offset() + (glyph_id - first_glyph_index()) * image_size_;
  }
  return -1;
}

int32_t IndexSubTableFormat2::GlyphLength(int32_t glyph_id) {
  if (CheckGlyphRange(glyph_id)) {
    return image_size_;
  }
  return -1;
}

}  // namespace sfntly

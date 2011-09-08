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

#ifndef SFNTLY_CPP_SRC_SFNTLY_TABLE_BITMAP_INDEX_SUBTABLE_H_
#define SFNTLY_CPP_SRC_SFNTLY_TABLE_BITMAP_INDEX_SUBTABLE_H_

#include <vector>

#include "sfntly/table/subtable.h"

namespace sfntly {

class IndexSubTable : public SubTable {
 public:
  int32_t first_glyph_index() { return first_glyph_index_; }
  int32_t last_glyph_index() { return last_glyph_index_; }
  int32_t image_format() { return image_format_; }
  int32_t image_data_offset() { return image_data_offset_; }

  virtual int32_t GlyphOffset(int32_t glyph_id) = 0;
  virtual int32_t GlyphLength(int32_t glyph_id) = 0;
  virtual int32_t NumGlyphs() = 0;

  static CALLER_ATTACH IndexSubTable*
      CreateIndexSubTable(ReadableFontData* data,
                          int32_t offset_to_index_sub_table_array,
                          int32_t array_index);

 protected:
  // Note: the constructor does not implement offset/length form provided in
  //       Java to avoid heavy lifting in constructors.  Callers to call
  //       GetDataLength() static method of the derived class to get proper
  //       length and slice ahead.
  IndexSubTable(ReadableFontData* data, int32_t first, int32_t last);

  // Note: change return type to bool in C++ since we may not throw.
  bool CheckGlyphRange(int32_t glyph_id);

 private:
  int32_t first_glyph_index_;
  int32_t last_glyph_index_;
  int32_t index_format_;
  int32_t image_format_;
  int32_t image_data_offset_;
};
typedef Ptr<IndexSubTable> IndexSubTablePtr;
typedef std::vector<IndexSubTablePtr> IndexSubTableList;

}  // namespace sfntly

#endif  // SFNTLY_CPP_SRC_SFNTLY_TABLE_BITMAP_INDEX_SUBTABLE_H_

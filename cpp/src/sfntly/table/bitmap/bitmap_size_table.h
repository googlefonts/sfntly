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

#ifndef SFNTLY_CPP_SRC_SFNTLY_TABLE_BITMAP_BITMAP_SIZE_TABLE_H_
#define SFNTLY_CPP_SRC_SFNTLY_TABLE_BITMAP_BITMAP_SIZE_TABLE_H_

#include <vector>

#include "sfntly/table/bitmap/index_sub_table.h"

namespace sfntly {

class BitmapSizeTable : public SubTable,
                        public RefCounted<BitmapSizeTable> {
 public:
  // Note: C++ port take two ReadableFontData.  The first is sliced data, and
  //       the second is unsliced data.  The second one is used to correctly
  //       construct index sub tables since Java version calculate the offset
  //       based on unsliced data.
  BitmapSizeTable(ReadableFontData* data,
                  ReadableFontData* master_data);
  virtual ~BitmapSizeTable();

  int32_t IndexSubTableArrayOffset();
  int32_t IndexTableSize();
  int32_t NumberOfIndexSubTables();
  int32_t ColorRef();
  int32_t StartGlyphIndex();
  int32_t EndGlyphIndex();
  int32_t PpemX();
  int32_t PpemY();
  int32_t BitDepth();
  int32_t FlagsAsInt();

  // Note: renamed from indexSubTable()
  IndexSubTable* GetIndexSubTable(int32_t index);

  int32_t GlyphOffset(int32_t glyph_id);
  int32_t GlyphLength(int32_t glyph_id);
  int32_t GlyphFormat(int32_t glyph_id);

 private:
  IndexSubTable* SearchIndexSubTables(int32_t glyph_id);
  CALLER_ATTACH IndexSubTable* CreateIndexSubTable(int32_t index);
  IndexSubTableList* GetIndexSubTableList();

  ReadableFontDataPtr master_data_;
  IndexSubTableList atomic_subtables;
};
typedef Ptr<BitmapSizeTable> BitmapSizeTablePtr;
typedef std::vector<BitmapSizeTablePtr> BitmapSizeTableList;

}  // namespace sfntly

#endif  // SFNTLY_CPP_SRC_SFNTLY_TABLE_BITMAP_BITMAP_SIZE_TABLE_H_

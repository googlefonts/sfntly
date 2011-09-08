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

#include "gtest/gtest.h"
#include "sfntly/font.h"
#include "sfntly/table/bitmap/ebdt_table.h"
#include "sfntly/table/bitmap/eblc_table.h"
#include "test/test_data.h"
#include "test/test_font_utils.h"

namespace sfntly {

const int32_t NUM_STRIKES = 4;
const int32_t STRIKE1_ARRAY_OFFSET = 0xc8;
const int32_t STRIKE1_INDEX_TABLE_SIZE = 0x4f4;
const int32_t STRIKE1_NUM_INDEX_TABLES = 1;
const int32_t STRIKE1_COLOR_REF = 0;
const int32_t STRIKE1_START_GLYPH_INDEX = 0;
const int32_t STRIKE1_END_GLYPH_INDEX = 623;
const int32_t STRIKE1_PPEM_X = 10;
const int32_t STRIKE1_PPEM_Y = 10;
const int32_t STRIKE1_BIT_DEPTH = 1;
const int32_t STRIKE1_FLAGS = 0x01;

const int32_t STRIKE4_SUB1_IMAGE_FORMAT = 1;
const int32_t STRIKE4_SUB1_IMAGE_DATA_OFFSET = 0x00005893;
const int32_t STRIKE4_SUB1_GLYPH_OFFSET[] = {
    0x00005893, 0x00005898, 0x0000589d, 0x000058a2, 0x000058a7,
    0x000058b2, 0x000058c2, 0x000058d0, 0x000058de, 0x000058e6 };
const int32_t NUM_STRIKE4_SUB1_GLYPH_OFFSET = 10;  // must be 1 less

bool TestReadingBitmapTable() {
  FontFactoryPtr factory;
  factory.Attach(FontFactory::GetInstance());
  FontArray font_array;
  LoadFont(SAMPLE_BITMAP_FONT, factory, &font_array);
  FontPtr font = font_array[0];

  EblcTablePtr bitmap_loca = down_cast<EblcTable*>(font->GetTable(Tag::EBLC));
  EbdtTablePtr bitmap_table = down_cast<EbdtTable*>(font->GetTable(Tag::EBDT));

  EXPECT_FALSE(bitmap_loca == NULL);
  EXPECT_FALSE(bitmap_table == NULL);

  EXPECT_EQ(bitmap_loca->NumSizes(), NUM_STRIKES);

  // Strike 1
  BitmapSizeTablePtr strike1 = bitmap_loca->GetBitmapSizeTable(0);
  EXPECT_FALSE(strike1 == NULL);
  EXPECT_EQ(strike1->IndexSubTableArrayOffset(), STRIKE1_ARRAY_OFFSET);
  EXPECT_EQ(strike1->IndexTableSize(), STRIKE1_INDEX_TABLE_SIZE);
  EXPECT_EQ(strike1->NumberOfIndexSubTables(), STRIKE1_NUM_INDEX_TABLES);
  EXPECT_EQ(strike1->ColorRef(), STRIKE1_COLOR_REF);
  EXPECT_EQ(strike1->StartGlyphIndex(), STRIKE1_START_GLYPH_INDEX);
  EXPECT_EQ(strike1->EndGlyphIndex(), STRIKE1_END_GLYPH_INDEX);
  EXPECT_EQ(strike1->PpemX(), STRIKE1_PPEM_X);
  EXPECT_EQ(strike1->PpemY(), STRIKE1_PPEM_Y);
  EXPECT_EQ(strike1->BitDepth(), STRIKE1_BIT_DEPTH);
  EXPECT_EQ(strike1->FlagsAsInt(), STRIKE1_FLAGS);

  // Strike 4
  // In this test font, all strikes and all subtables have same glyphs.
  BitmapSizeTablePtr strike4 = bitmap_loca->GetBitmapSizeTable(3);
  EXPECT_FALSE(strike4 == NULL);
  EXPECT_EQ(strike4->StartGlyphIndex(), STRIKE1_START_GLYPH_INDEX);
  EXPECT_EQ(strike4->EndGlyphIndex(), STRIKE1_END_GLYPH_INDEX);
  IndexSubTablePtr sub1 = strike4->GetIndexSubTable(0);
  EXPECT_FALSE(sub1 == NULL);
  EXPECT_EQ(sub1->first_glyph_index(), STRIKE1_START_GLYPH_INDEX);
  EXPECT_EQ(sub1->last_glyph_index(), STRIKE1_END_GLYPH_INDEX);
  EXPECT_EQ(sub1->image_format(), STRIKE4_SUB1_IMAGE_FORMAT);
  EXPECT_EQ(sub1->image_data_offset(), STRIKE4_SUB1_IMAGE_DATA_OFFSET);

  for (int32_t i = 0; i < NUM_STRIKE4_SUB1_GLYPH_OFFSET; ++i) {
      EXPECT_EQ(sub1->GlyphOffset(i), STRIKE4_SUB1_GLYPH_OFFSET[i]);
  }

  return true;
}

}  // namespace sfntly

TEST(BitmapTable, All) {
  ASSERT_TRUE(sfntly::TestReadingBitmapTable());
}

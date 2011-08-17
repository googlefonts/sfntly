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
#include "sfntly/font_factory.h"
#include "sfntly/data/memory_byte_array.h"
#include "test/test_data.h"
#include "test/test_font_utils.h"
#include "test/serialization_test.h"

namespace sfntly {

bool TestSerialization() {
  FontFactoryPtr factory1, factory2, factory3;
  factory1.Attach(FontFactory::GetInstance());
  FontArray font_array;
  LoadFont(SAMPLE_TTF_FILE, factory1, &font_array);
  FontPtr original = font_array[0];

  factory2.Attach(FontFactory::GetInstance());
  FontBuilderArray font_builder_array;
  BuilderForFontFile(SAMPLE_TTF_FILE, factory2, &font_builder_array);
  FontBuilderPtr font_builder = font_builder_array[0];

  FontPtr intermediate;
  intermediate.Attach(font_builder->Build());
  MemoryOutputStream os;
  factory2->SerializeFont(intermediate, &os);

  factory3.Attach(FontFactory::GetInstance());
  FontArray new_font_array;
  ByteArrayPtr ba = new MemoryByteArray(os.Get(), os.Size());
  factory3->LoadFonts(ba, &new_font_array);
  FontPtr serialized = new_font_array[0];

  // Check number of tables
  EXPECT_EQ(original->num_tables(), serialized->num_tables());

  // Check if same set of tables
  TableMap* original_tables = original->Tables();
  TableMap* serialized_tables = serialized->Tables();
  EXPECT_EQ(original_tables->size(), serialized_tables->size());
  TableMap::iterator not_found = serialized_tables->end();
  for (TableMap::iterator b = original_tables->begin(),
                          e = original_tables->end(); b != e; ++b) {
    EXPECT_TRUE((serialized_tables->find(b->first) != not_found));
  }

  // TODO(arthurhsu): check cmap equivalence
  // Check checksum equivalence
  for (size_t i = 0; i < SAMPLE_TTF_KNOWN_TAGS; ++i) {
      TablePtr original_table = original->GetTable(TTF_KNOWN_TAGS[i]);
      TablePtr serialized_table = serialized->GetTable(TTF_KNOWN_TAGS[i]);
    EXPECT_EQ(original_table->CalculatedChecksum(),
              serialized_table->CalculatedChecksum());
    EXPECT_EQ(original_table->Length(), serialized_table->Length());

    if (TTF_KNOWN_TAGS[i] == Tag::hhea) {
      EXPECT_TRUE(VerifyHHEA(original_table, serialized_table));
    } else if (TTF_KNOWN_TAGS[i] == Tag::glyf) {
        EXPECT_TRUE(VerifyGLYF(original_table, serialized_table));
    } else if (TTF_KNOWN_TAGS[i] == Tag::hmtx) {
        EXPECT_TRUE(VerifyHMTX(original_table, serialized_table));
    } else if (TTF_KNOWN_TAGS[i] == Tag::loca) {
        EXPECT_TRUE(VerifyLOCA(original_table, serialized_table));
    } else if (TTF_KNOWN_TAGS[i] == Tag::maxp) {
        EXPECT_TRUE(VerifyMAXP(original_table, serialized_table));
    } else if (TTF_KNOWN_TAGS[i] == Tag::name) {
        EXPECT_TRUE(VerifyNAME(original_table, serialized_table));
    } else if (TTF_KNOWN_TAGS[i] == Tag::OS_2) {
        EXPECT_TRUE(VerifyOS_2(original_table, serialized_table));
    }
  }

  return true;
}

}  // namespace sfntly

TEST(Serialization, All) {
  ASSERT_TRUE(sfntly::TestSerialization());
}

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
#include "sfntly/font_header_table.h"
#include "sfntly/tag.h"
#include "test/otf_basic_editing_test.h"

namespace sfntly {

// TODO(arthurhsu): port over TestFontUtils
bool testOTFBasicEditing() {
  ByteVector input_buffer;
#if defined WIN32
  FILE* input_file;
  fopen_s(&input_file, "..\\data\\ext\\arial.ttf", "rb");
#else
  FILE* input_file = fopen("../data/ext/arial.ttf", "rb");
#endif
  EXPECT_TRUE(input_file != NULL);
  if (input_file == NULL) {
    return false;
  }
  fseek(input_file, 0, SEEK_END);
  size_t file_size = ftell(input_file);
  fseek(input_file, 0, SEEK_SET);
  input_buffer.resize(file_size);
  fread(&(input_buffer[0]), 1, file_size, input_file);
  fclose(input_file);

  ByteArrayPtr ba = new MemoryByteArray(&(input_buffer[0]), file_size);
  FontFactoryPtr factory = FontFactory::getInstance();

  FontBuilderArray font_builder_array;
  factory->loadFontsForBuilding(ba, &font_builder_array);
  EXPECT_GT(font_builder_array.size(), static_cast<size_t>(0));
  FontBuilderPtr font_builder = font_builder_array[0];

  // ensure the builder is not bogus
  EXPECT_TRUE(font_builder != NULL);
  TableBuilderMap* builder_map = font_builder->tableBuilders();
  EXPECT_TRUE(builder_map != NULL);
  for (TableBuilderMap::iterator i = builder_map->begin(),
                                 e = builder_map->end(); i != e; ++i) {
    EXPECT_TRUE(i->second != NULL);
    if (i->second == NULL) {
      char tag[5] = {0};
      memcpy(tag, &(i->first), 4);
      fprintf(stderr, "tag %s does not have valid builder\n", tag);
    }
  }

  IntegerSet builder_tags;
  font_builder->tableBuilderTags(&builder_tags);
  FontHeaderTableBuilderPtr header_builder =
      down_cast<FontHeaderTable::Builder*>(
          font_builder->getTableBuilder(Tag::head));
  int64_t mod_date = header_builder->modified();
  header_builder->setModified(mod_date + 1);
  FontPtr font;
  font.attach(font_builder->build());

  // ensure every table had a builder
  TableMap* table_map = font->tables();
  for (TableMap::iterator i = table_map->begin(), e = table_map->end();
                          i != e; ++i) {
    TablePtr table = (*i).second;
    TableHeaderPtr header = table->header();
    EXPECT_TRUE(builder_tags.find(header->tag()) != builder_tags.end());
    builder_tags.erase(header->tag());
  }
  EXPECT_TRUE(builder_tags.empty());

  FontHeaderTablePtr header = down_cast<FontHeaderTable*>(
      font->table(Tag::head));
  int64_t after_mod_date = header->modified();
  EXPECT_EQ(mod_date + 1, after_mod_date);
  return true;
}

}  // namespace sfntly

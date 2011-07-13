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
#include "sfntly/font_header_table.h"
#include "sfntly/tag.h"
#include "sfntly/table.h"
#include "sfntly/data/font_input_stream.h"
#include "sfntly/data/memory_byte_array.h"
#include "sfntly/port/file_input_stream.h"
#include "test/font_parsing_test.h"
#include "test/test_data.h"
#include "test/test_font_utils.h"

namespace sfntly {

bool testFontParsing() {
  ByteVector input_buffer;
  loadFile(SAMPLE_TTF_FILE, &input_buffer);
  ByteArrayPtr ba = new MemoryByteArray(&(input_buffer[0]),
                                        input_buffer.size());

  FontFactoryPtr factory = FontFactory::getInstance();
  // File based
  FontBuilderArray font_builder_array;
  builderForFontFile(SAMPLE_TTF_FILE, factory, &font_builder_array);
  FontBuilderPtr font_builder = font_builder_array[0];
  // Memory based
  FontBuilderArray font_builder_array2;
  factory->loadFontsForBuilding(ba, &font_builder_array2);
  FontBuilderPtr font_builder2 = font_builder_array2[0];

  for (size_t i = 0; i < SAMPLE_TTF_KNOWN_TAGS; ++i) {
    EXPECT_TRUE(font_builder->hasTableBuilder(TTF_KNOWN_TAGS[i]));
    EXPECT_TRUE(font_builder2->hasTableBuilder(TTF_KNOWN_TAGS[i]));
  }

  // Generic table
  Ptr<Table::GenericTableBuilder> gdef_builder =
      down_cast<Table::GenericTableBuilder*>(
          font_builder->getTableBuilder(Tag::GDEF));
  Ptr<Table::Header> gdef_header = gdef_builder->header();
  EXPECT_EQ(gdef_header->length(), TTF_LENGTH[SAMPLE_TTF_GDEF]);
  EXPECT_EQ(gdef_header->offset(), TTF_OFFSET[SAMPLE_TTF_GDEF]);
  EXPECT_EQ(gdef_header->checksum(), TTF_CHECKSUM[SAMPLE_TTF_GDEF]);
  EXPECT_TRUE(gdef_header->checksumValid());

  WritableFontDataPtr wfd = gdef_builder->data();
  ByteVector b;
  b.resize(TTF_LENGTH[SAMPLE_TTF_GDEF]);
  wfd->readBytes(0, &b, 0, TTF_LENGTH[SAMPLE_TTF_GDEF]);
  EXPECT_EQ(memcmp(&(b[0]), TTF_GDEF_DATA, TTF_LENGTH[SAMPLE_TTF_GDEF]), 0);

  // Header table
  FontHeaderTableBuilderPtr header_builder =
      down_cast<FontHeaderTable::Builder*>(
          font_builder->getTableBuilder(Tag::head));
  Ptr<Table::Header> header_header = header_builder->header();
  EXPECT_EQ(header_header->length(), TTF_LENGTH[SAMPLE_TTF_HEAD]);
  EXPECT_EQ(header_header->offset(), TTF_OFFSET[SAMPLE_TTF_HEAD]);
  EXPECT_EQ(header_header->checksum(), TTF_CHECKSUM[SAMPLE_TTF_HEAD]);
  EXPECT_TRUE(header_header->checksumValid());

  // Data conformance
  for (size_t i = 0; i < SAMPLE_TTF_KNOWN_TAGS; ++i) {
    ByteVector b1, b2;
    b1.resize(TTF_LENGTH[i]);
    b2.resize(TTF_LENGTH[i]);
    TableBuilderPtr builder1 =
        font_builder->getTableBuilder(TTF_KNOWN_TAGS[i]);
    TableBuilderPtr builder2 =
        font_builder2->getTableBuilder(TTF_KNOWN_TAGS[i]);
    WritableFontDataPtr wfd1 = builder1->data();
    WritableFontDataPtr wfd2 = builder2->data();
    wfd1->readBytes(0, &b1, 0, TTF_LENGTH[i]);
    wfd2->readBytes(0, &b2, 0, TTF_LENGTH[i]);
    EXPECT_EQ(memcmp(&(b1[0]), &(b2[0]), TTF_LENGTH[i]), 0);
  }

  return true;
}

bool testTTFReadWrite() {
  FontFactoryPtr factory = FontFactory::getInstance();
  FontBuilderArray font_builder_array;
  builderForFontFile(SAMPLE_TTF_FILE, factory, &font_builder_array);
  FontBuilderPtr font_builder = font_builder_array[0];
  FontPtr font = font_builder->build();
  MemoryOutputStream output_stream;
  factory->serializeFont(font, &output_stream);
  EXPECT_GE(output_stream.size(), SAMPLE_TTF_SIZE);

  return true;
}

bool testTTFMemoryBasedReadWrite() {
  ByteVector input_buffer;
  loadFile(SAMPLE_TTF_FILE, &input_buffer);

  FontFactoryPtr factory = FontFactory::getInstance();
  FontBuilderArray font_builder_array;
  ByteArrayPtr ba = new MemoryByteArray(&(input_buffer[0]),
                                        input_buffer.size());
  factory->loadFontsForBuilding(ba, &font_builder_array);
  FontBuilderPtr font_builder = font_builder_array[0];
  FontPtr font = font_builder->build();
  MemoryOutputStream output_stream;
  factory->serializeFont(font, &output_stream);
  EXPECT_GE(output_stream.size(), input_buffer.size());

  return true;
}

}  // namespace sfntly

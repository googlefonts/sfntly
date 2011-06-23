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

// Remove VC++ nag on fopen.
#define _CRT_SECURE_NO_WARNINGS

#include <stdio.h>
#include <vector>
#include <memory>

#include "sfntly/port/type.h"
#include "sfntly/font.h"
#include "sfntly/tools/subsetter/subsetter.h"
#include "sample/subsetter/subset_util.h"
#include "sfntly/data/memory_byte_array.h"
#include "sfntly/port/memory_output_stream.h"
#include "sfntly/tag.h"

namespace sfntly {

SubsetUtil::SubsetUtil() {
}

SubsetUtil::~SubsetUtil() {
}

void SubsetUtil::subset(const char *input_file_path,
                        const char *output_file_path) {
  UNREFERENCED_PARAMETER(output_file_path);
  ByteVector input_buffer;
  FILE* input_file = fopen(input_file_path, "rb");
  if (input_file == NULL) {
    fprintf(stderr, "file not found\n");
    return;
  }
  fseek(input_file, 0, SEEK_END);
  size_t file_size = ftell(input_file);
  fseek(input_file, 0, SEEK_SET);
  input_buffer.resize(file_size);
  fread(&(input_buffer[0]), 1, file_size, input_file);
  fclose(input_file);

  ByteArrayPtr ba = new MemoryByteArray(&(input_buffer[0]), file_size);
  FontFactoryPtr factory;
  factory.attach(FontFactory::getInstance());

  FontArray font_array;
  factory->loadFonts(ba, &font_array);
  if (font_array.empty() || font_array[0] == NULL)
    return;

  IntegerList glyphs;
  for (int32_t i = 0; i < 10; i++) {
    glyphs.push_back(i);
  }
  glyphs.push_back(11);
  glyphs.push_back(10);

  Ptr<Subsetter> subsetter = new Subsetter(font_array[0], factory);
  subsetter->setGlyphs(&glyphs);
  IntegerSet remove_tables;
  remove_tables.insert(Tag::DSIG);
  subsetter->setRemoveTables(&remove_tables);

  FontBuilderPtr font_builder;
  font_builder.attach(subsetter->subset());

  FontPtr new_font;
  new_font.attach(font_builder->build());

  // TODO(arthurhsu): glyph renumbering/Loca table
  // TODO(arthurhsu): alter CMaps

  MemoryOutputStream output_stream;
  factory->serializeFont(new_font, &output_stream);

  FILE* output_file = fopen(output_file_path, "wb");
  fwrite(output_stream.get(), 1, output_stream.size(), output_file);
  fflush(output_file);
  fclose(output_file);
}

}  // namespace sfntly

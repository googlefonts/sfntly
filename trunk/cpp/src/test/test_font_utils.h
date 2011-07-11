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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_TEST_TEST_FONT_UTILS_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_TEST_TEST_FONT_UTILS_H_

#include "sfntly/font.h"
#include "sfntly/font_factory.h"
#include "sfntly/port/memory_output_stream.h"

namespace sfntly {

void builderForFontFile(const char* font_path, FontFactory* factory,
                        FontBuilderArray* builders);
void serializeFont(const char* font_path, FontFactory* factory, Font* font);
void loadFont(const char* font_path, FontFactory* factory, FontArray* fonts);

void loadFile(const char* input_file_path, ByteVector* input_buffer);
void serializeToFile(MemoryOutputStream* output_stream, const char* file_path);

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_TEST_TEST_FONT_UTILS_H_

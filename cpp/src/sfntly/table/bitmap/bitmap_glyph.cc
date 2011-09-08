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

#include "sfntly/table/bitmap/bitmap_glyph.h"

namespace sfntly {
/******************************************************************************
 * BitmapGlyph class
 ******************************************************************************/
BitmapGlyph::~BitmapGlyph() {
}

BitmapGlyph::BitmapGlyph(ReadableFontData* data, int32_t format)
    : SubTable(data), format_(format) {
}

/******************************************************************************
 * BitmapGlyph::Builder class
 ******************************************************************************/
BitmapGlyph::Builder::~Builder() {
}

BitmapGlyph::Builder::Builder(WritableFontData* data)
    : SubTable::Builder(data) {
}

BitmapGlyph::Builder::Builder(ReadableFontData* data)
    : SubTable::Builder(data) {
}

CALLER_ATTACH
FontDataTable* BitmapGlyph::Builder::SubBuildTable(ReadableFontData* data) {
  UNREFERENCED_PARAMETER(data);
  return NULL;
}

void BitmapGlyph::Builder::SubDataSet() {
}

int32_t BitmapGlyph::Builder::SubDataSizeToSerialize() {
  return 0;
}

bool BitmapGlyph::Builder::SubReadyToSerialize() {
  return false;
}

int32_t BitmapGlyph::Builder::SubSerialize(WritableFontData* new_data) {
  UNREFERENCED_PARAMETER(new_data);
  return 0;
}

}  // namespace sfntly

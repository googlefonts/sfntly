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

#include "sfntly/table/bitmap/ebdt_table.h"

#include "sfntly/table/bitmap/composite_bitmap_glyph.h"
#include "sfntly/table/bitmap/simple_bitmap_glyph.h"

namespace sfntly {
/******************************************************************************
 * EbdtTable class
 ******************************************************************************/
EbdtTable::~EbdtTable() {
}

int32_t EbdtTable::Version() {
  return data_->ReadFixed(Offset::kVersion);
}

CALLER_ATTACH
BitmapGlyph* EbdtTable::Glyph(int32_t offset, int32_t length, int32_t format) {
  ReadableFontDataPtr new_data;
  new_data.Attach(down_cast<ReadableFontData*>(data_->Slice(offset, length)));
  BitmapGlyphPtr glyph;
  switch (format) {
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
    case 7:
      glyph = new SimpleBitmapGlyph(new_data, format);
      break;
    case 8:
    case 9:
      glyph = new CompositeBitmapGlyph(new_data, format);
      break;
    default:
      assert(false);
      break;
  }
  return glyph.Detach();
}

EbdtTable::EbdtTable(Header* header, ReadableFontData* data)
    : SubTableContainerTable(header, data) {
}

/******************************************************************************
 * EbdtTable::Builder class
 ******************************************************************************/
EbdtTable::Builder::Builder(Header* header, WritableFontData* data)
  : SubTableContainerTable::Builder(header, data) {
}

EbdtTable::Builder::Builder(Header* header, ReadableFontData* data)
  : SubTableContainerTable::Builder(header, data) {
}

EbdtTable::Builder::~Builder() {
}

CALLER_ATTACH FontDataTable*
    EbdtTable::Builder::SubBuildTable(ReadableFontData* data) {
  FontDataTablePtr table = new EbdtTable(header(), data);
  return table.Detach();
}

void EbdtTable::Builder::SubDataSet() {
  // NOP
}

int32_t EbdtTable::Builder::SubDataSizeToSerialize() {
  return 0;
}

bool EbdtTable::Builder::SubReadyToSerialize() {
  return false;
}

int32_t EbdtTable::Builder::SubSerialize(WritableFontData* new_data) {
  UNREFERENCED_PARAMETER(new_data);
  return 0;
}

CALLER_ATTACH
EbdtTable::Builder* EbdtTable::Builder::CreateBuilder(Header* header,
                                                      WritableFontData* data) {
  Ptr<EbdtTable::Builder> builder;
  builder = new Builder(header, data);
  return builder.Detach();
}


}  // namespace sfntly

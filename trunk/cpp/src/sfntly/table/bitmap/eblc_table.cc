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

#include "sfntly/table/bitmap/eblc_table.h"

namespace sfntly {
/******************************************************************************
 * EblcTable class
 ******************************************************************************/
int32_t EblcTable::Version() {
  return data_->ReadFixed(Offset::kVersion);
}

int32_t EblcTable::NumSizes() {
  return data_->ReadULongAsInt(Offset::kNumSizes);
}

BitmapSizeTable* EblcTable::GetBitmapSizeTable(int32_t index) {
  if (index < 0 || index > NumSizes()) {
#if !defined (SFNTLY_NO_EXCEPTION)
    throw IndexOutOfBoundException(
        "Size table index is outside the range of tables.");
#endif
    return NULL;
  }
  BitmapSizeTableList* bitmap_size_table_list = GetBitmapSizeTableList();
  if (bitmap_size_table_list) {
    return (*bitmap_size_table_list)[index];
  }
  return NULL;
}

EblcTable::EblcTable(Header* header, ReadableFontData* data)
    : SubTableContainerTable(header, data) {
}

BitmapSizeTableList* EblcTable::GetBitmapSizeTableList() {
  // TODO(arthurhsu): thread locking.
  if (bitmap_size_table_.empty()) {
    CreateBitmapSizeTable(data_, NumSizes(), &bitmap_size_table_);
  }
  return &bitmap_size_table_;
}

// static
void EblcTable::CreateBitmapSizeTable(ReadableFontData* data,
                                      int32_t num_sizes,
                                      BitmapSizeTableList* output) {
  assert(data);
  assert(output);
  for (int32_t i = 0; i < num_sizes; ++i) {
    ReadableFontDataPtr new_data;
    new_data.Attach(down_cast<ReadableFontData*>(
        data->Slice(Offset::kBitmapSizeTableArrayStart +
                    i * Offset::kBitmapSizeTableLength,
                    Offset::kBitmapSizeTableLength)));
    BitmapSizeTablePtr new_table = new BitmapSizeTable(new_data, data);
    output->push_back(new_table);
  }
}

/******************************************************************************
 * EblcTable::Builder class
 ******************************************************************************/
EblcTable::Builder::Builder(Header* header, WritableFontData* data)
    : SubTableContainerTable::Builder(header, data) {
}

EblcTable::Builder::Builder(Header* header, ReadableFontData* data)
    : SubTableContainerTable::Builder(header, data) {
}

EblcTable::Builder::~Builder() {
}

int32_t EblcTable::Builder::SubSerialize(WritableFontData* new_data) {
  UNREFERENCED_PARAMETER(new_data);
  return 0;
}

bool EblcTable::Builder::SubReadyToSerialize() {
  return false;
}

int32_t EblcTable::Builder::SubDataSizeToSerialize() {
  return 0;
}

void EblcTable::Builder::SubDataSet() {
  // NOP
}

CALLER_ATTACH
FontDataTable* EblcTable::Builder::SubBuildTable(ReadableFontData* data) {
  Ptr<EblcTable> new_table = new EblcTable(header(), data);
  return new_table.Detach();
}

// static
CALLER_ATTACH EblcTable::Builder*
    EblcTable::Builder::CreateBuilder(Header* header, WritableFontData* data) {
  Ptr<EblcTable::Builder> new_builder = new EblcTable::Builder(header, data);
  return new_builder.Detach();
}

}  // namespace sfntly

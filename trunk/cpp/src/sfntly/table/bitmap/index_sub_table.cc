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

#include "sfntly/table/bitmap/index_sub_table.h"

#include "sfntly/table/bitmap/eblc_table.h"
#include "sfntly/table/bitmap/index_sub_table_format1.h"
#include "sfntly/table/bitmap/index_sub_table_format2.h"
#include "sfntly/table/bitmap/index_sub_table_format3.h"
#include "sfntly/table/bitmap/index_sub_table_format4.h"
#include "sfntly/table/bitmap/index_sub_table_format5.h"

namespace sfntly {

bool IndexSubTable::CheckGlyphRange(int32_t glyph_id) {
  if (glyph_id < first_glyph_index() || glyph_id > last_glyph_index()) {
#if !defined (SFNTLY_NO_EXCEPTION)
    throw IndexOutOfBoundException("Glyph ID is outside of the allowed range");
#endif
    return false;
  }
  return true;
}

template <typename IndexSubTableType>
static IndexSubTableType* CreateTable(ReadableFontData* data,
                                      int32_t index_sub_table_offset,
                                      int32_t first_glyph_index,
                                      int32_t last_glyph_index) {
  ReadableFontDataPtr new_data;
  new_data.Attach(down_cast<ReadableFontData*>(
      data->Slice(index_sub_table_offset,
                  IndexSubTableType::GetDataLength(data,
                                                   index_sub_table_offset,
                                                   first_glyph_index,
                                                   last_glyph_index))));
  return new IndexSubTableType(new_data, first_glyph_index, last_glyph_index);
}

// static
CALLER_ATTACH IndexSubTable*
    IndexSubTable::CreateIndexSubTable(ReadableFontData* data,
                                       int32_t offset_to_index_sub_table_array,
                                       int32_t array_index) {
  int32_t index_sub_table_entry_offset =
      offset_to_index_sub_table_array +
      array_index * EblcTable::Offset::kIndexSubTableEntryLength;

  int32_t first_glyph_index =
      data->ReadUShort(index_sub_table_entry_offset +
                       EblcTable::Offset::kIndexSubTableEntry_firstGlyphIndex);
  int32_t last_glyph_index =
      data->ReadUShort(index_sub_table_entry_offset +
                       EblcTable::Offset::kIndexSubTableEntry_lastGlyphIndex);
  int32_t additional_offset_to_index_subtable = data->ReadULongAsInt(
      index_sub_table_entry_offset +
      EblcTable::Offset::kIndexSubTableEntry_additionalOffsetToIndexSubTable);

  int32_t index_sub_table_offset = offset_to_index_sub_table_array +
                                   additional_offset_to_index_subtable;

  int32_t index_format = data->ReadUShort(index_sub_table_offset);
  IndexSubTablePtr new_table;
  ReadableFontDataPtr new_data;
  switch (index_format) {
    case 1:
      new_table = CreateTable<IndexSubTableFormat1>(data,
                                                    index_sub_table_offset,
                                                    first_glyph_index,
                                                    last_glyph_index);
      break;

    case 2:
      new_table = CreateTable<IndexSubTableFormat2>(data,
                                                    index_sub_table_offset,
                                                    first_glyph_index,
                                                    last_glyph_index);
      break;

    case 3:
      new_table = CreateTable<IndexSubTableFormat3>(data,
                                                    index_sub_table_offset,
                                                    first_glyph_index,
                                                    last_glyph_index);
      break;

    case 4:
      new_table = CreateTable<IndexSubTableFormat4>(data,
                                                    index_sub_table_offset,
                                                    first_glyph_index,
                                                    last_glyph_index);
      break;

    case 5:
      new_table = CreateTable<IndexSubTableFormat5>(data,
                                                    index_sub_table_offset,
                                                    first_glyph_index,
                                                    last_glyph_index);
      break;

    default:
      // Unknown format and unable to process.
#if !defined (SFNTLY_NO_EXCEPTION)
      throw IllegalArgumentException("Invalid Index Subtable Format");
#endif
      break;
  }

  return new_table.Detach();
}

IndexSubTable::IndexSubTable(ReadableFontData* data,
                             int32_t first,
                             int32_t last)
    : SubTable(data), first_glyph_index_(first), last_glyph_index_(last) {
  index_format_ =
      data_->ReadUShort(EblcTable::Offset::kIndexSubHeader_indexFormat);
  image_format_ =
      data_->ReadUShort(EblcTable::Offset::kIndexSubHeader_imageFormat);
  image_data_offset_ =
      data_->ReadULongAsInt(EblcTable::Offset::kIndexSubHeader_imageDataOffset);
}

}  // namespace sfntly

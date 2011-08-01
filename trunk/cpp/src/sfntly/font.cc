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

#include "sfntly/font.h"

#include <functional>
#include <algorithm>
#include <map>
#include <typeinfo>

#include "sfntly/data/font_input_stream.h"
#include "sfntly/font_factory.h"
#include "sfntly/math/fixed1616.h"
#include "sfntly/math/font_math.h"
#include "sfntly/font_header_table.h"
#include "sfntly/horizontal_header_table.h"
#include "sfntly/horizontal_metrics_table.h"
#include "sfntly/loca_table.h"
#include "sfntly/maximum_profile_table.h"
#include "sfntly/port/exception_type.h"
#include "sfntly/tag.h"

namespace sfntly {

const int32_t SFNTVERSION_1 = Fixed1616::Fixed(1, 0);

/******************************************************************************
 * Font class
 ******************************************************************************/
Font::~Font() {}

bool Font::HasTable(int32_t tag) {
  TableMap::const_iterator result = tables_.find(tag);
  TableMap::const_iterator end = tables_.end();
  return (result != end);
}

Table* Font::GetTable(int32_t tag) {
  if (!HasTable(tag)) {
    return NULL;
  }
  return tables_[tag];
}

TableMap* Font::Tables() {
  return &tables_;
}

void Font::Serialize(OutputStream* os, IntegerList* table_ordering) {
  assert(table_ordering);
  IntegerList final_table_ordering;
  TableOrdering(table_ordering, &final_table_ordering);
  TableHeaderList table_records;
  BuildTableHeadersForSerialization(&final_table_ordering, &table_records);

  FontOutputStream fos(os);
  SerializeHeader(&fos, &table_records);
  SerializeTables(&fos, &table_records);
}

CALLER_ATTACH WritableFontData* Font::GetNewData(int32_t size) {
  return factory_->GetNewData(size);
}

Font::Font(FontFactory* factory, int32_t sfnt_version, ByteVector* digest,
           TableMap* tables)
    : factory_(factory),
      sfnt_version_(sfnt_version) {
  // non-trivial assignments that makes debugging hard if placed in
  // initialization list
  digest_ = *digest;
  tables_ = *tables;
}

void Font::BuildTableHeadersForSerialization(IntegerList* table_ordering,
                                             TableHeaderList* table_headers) {
  assert(table_headers);
  assert(table_ordering);

  IntegerList final_table_ordering;
  TableOrdering(table_ordering, &final_table_ordering);
  int32_t table_offset = Offset::kTableRecordBegin + num_tables() *
                         Offset::kTableRecordSize;
  for (IntegerList::iterator tag = final_table_ordering.begin(),
                             tag_end = final_table_ordering.end();
                             tag != tag_end; ++tag) {
    TablePtr table = tables_[*tag];
    if (table != NULL) {
      TableHeaderPtr header =
          new Table::Header(*tag, table->CalculatedChecksum(), table_offset,
                            table->Length());
      table_headers->push_back(header);
      table_offset += (table->Length() + 3) & ~3;
    }
  }
}

void Font::SerializeHeader(FontOutputStream* fos,
                           TableHeaderList* table_headers) {
  fos->WriteFixed(sfnt_version_);
  fos->WriteUShort(table_headers->size());
  int32_t log2_of_max_power_of_2 = FontMath::Log2(table_headers->size());
  int32_t search_range = 2 << (log2_of_max_power_of_2 - 1 + 4);
  fos->WriteUShort(search_range);
  fos->WriteUShort(log2_of_max_power_of_2);
  fos->WriteUShort((table_headers->size() * 16) - search_range);

  for (TableHeaderList::iterator record = table_headers->begin(),
                                 record_end = table_headers->end();
                                 record != record_end; ++record) {
    fos->WriteULong((*record)->tag());
    fos->WriteULong((int32_t)((*record)->checksum()));
    fos->WriteULong((*record)->offset());
    fos->WriteULong((*record)->length());
  }
}

void Font::SerializeTables(FontOutputStream* fos,
                           TableHeaderList* table_headers) {
  ByteVector SERIALIZATION_FILLER(3);
  std::fill(SERIALIZATION_FILLER.begin(), SERIALIZATION_FILLER.end(), 0);
  for (TableHeaderList::iterator record = table_headers->begin(),
                                 end_of_headers = table_headers->end();
                                 record != end_of_headers; ++record) {
    TablePtr target_table = GetTable((*record)->tag());
    if (target_table == NULL) {
#if defined (SFNTLY_NO_EXCEPTION)
      return;
#else
      throw IOException("Table out of sync with font header.");
#endif
    }
    int32_t table_size = target_table->Serialize(fos);
    if (table_size != (*record)->length()) {
      assert(false);
    }
    int32_t filler_size = ((table_size + 3) & ~3) - table_size;
    fos->Write(&SERIALIZATION_FILLER, 0, filler_size);
  }
}

void Font::TableOrdering(IntegerList* default_table_ordering,
                         IntegerList* table_ordering) {
  assert(default_table_ordering);
  assert(table_ordering);
  table_ordering->clear();
  if (default_table_ordering->empty()) {
    DefaultTableOrdering(default_table_ordering);
  }

  typedef std::map<int32_t, bool> Int2Bool;
  typedef std::pair<int32_t, bool> Int2BoolEntry;
  Int2Bool tables_in_font;
  for (TableMap::iterator table = tables_.begin(), table_end = tables_.end();
                          table != table_end; ++table) {
    tables_in_font.insert(Int2BoolEntry(table->first, false));
  }
  for (IntegerList::iterator tag = default_table_ordering->begin(),
                             tag_end = default_table_ordering->end();
                             tag != tag_end; ++tag) {
    if (HasTable(*tag)) {
      table_ordering->push_back(*tag);
      tables_in_font[*tag] = true;
    }
  }
  for (Int2Bool::iterator table = tables_in_font.begin(),
                          table_end = tables_in_font.end();
                          table != table_end; ++table) {
    if (table->second == false)
      table_ordering->push_back(table->first);
  }
}

void Font::DefaultTableOrdering(IntegerList* default_table_ordering) {
  assert(default_table_ordering);
  default_table_ordering->clear();
  if (HasTable(Tag::CFF)) {
    default_table_ordering->resize(CFF_TABLE_ORDERING_SIZE);
    std::copy(CFF_TABLE_ORDERING, CFF_TABLE_ORDERING + CFF_TABLE_ORDERING_SIZE,
              default_table_ordering->begin());
    return;
  }
  default_table_ordering->resize(TRUE_TYPE_TABLE_ORDERING_SIZE);
  std::copy(TRUE_TYPE_TABLE_ORDERING,
            TRUE_TYPE_TABLE_ORDERING + TRUE_TYPE_TABLE_ORDERING_SIZE,
            default_table_ordering->begin());
}

/******************************************************************************
 * Font::Builder class
 ******************************************************************************/
Font::Builder::~Builder() {}

CALLER_ATTACH Font::Builder* Font::Builder::GetOTFBuilder(
    FontFactory* factory, InputStream* is) {
  FontBuilderPtr builder = new Builder(factory);
  builder->LoadFont(is);
  return builder.Detach();
}

CALLER_ATTACH Font::Builder* Font::Builder::GetOTFBuilder(
    FontFactory* factory, ByteArray* ba, int32_t offset_to_offset_table) {
  FontBuilderPtr builder = new Builder(factory);
  builder->LoadFont(ba, offset_to_offset_table);
  return builder.Detach();
}

CALLER_ATTACH Font::Builder* Font::Builder::GetOTFBuilder(
    FontFactory* factory) {
  FontBuilderPtr builder = new Builder(factory);
  return builder.Detach();
}

bool Font::Builder::ReadyToBuild() {
  // just read in data with no manipulation
  if (table_builders_.empty() && !data_blocks_.empty()) {
    return true;
  }

  // TODO(stuartg): font level checks - required tables etc.
  for (TableBuilderMap::iterator table_builder = table_builders_.begin(),
                                 table_builder_end = table_builders_.end();
                                 table_builder != table_builder_end;
                                 ++table_builder) {
    if (!table_builder->second->ReadyToBuild())
      return false;
  }
  return true;
}

CALLER_ATTACH Font* Font::Builder::Build() {
  TableMap tables;
  if (!table_builders_.empty()) {
    BuildTablesFromBuilders(&table_builders_, &tables);
  }
  FontPtr font = new Font(factory_, sfnt_version_, &digest_, &tables);
  table_builders_.clear();
  data_blocks_.clear();
  return font.Detach();
}

CALLER_ATTACH WritableFontData* Font::Builder::GetNewData(int32_t capacity) {
  return factory_->GetNewData(capacity);
}

CALLER_ATTACH WritableFontData* Font::Builder::GetNewGrowableData(
    ReadableFontData* src_data) {
  return factory_->GetNewGrowableData(src_data);
}

void Font::Builder::SetDigest(ByteVector* digest) {
  digest_.clear();
  digest_ = *digest;
}

void Font::Builder::CleanTableBuilders() {
  table_builders_.clear();
}

bool Font::Builder::HasTableBuilder(int32_t tag) {
  return (table_builders_.find(tag) != table_builders_.end());
}

Table::Builder* Font::Builder::GetTableBuilder(int32_t tag) {
  if (HasTableBuilder(tag))
    return table_builders_[tag];
  return NULL;
}

CALLER_ATTACH Table::Builder* Font::Builder::NewTableBuilder(int32_t tag) {
  TableHeaderPtr header = new Table::Header(tag);
  TableBuilderPtr builder = Table::Builder::GetBuilder(this, header, NULL);
  table_builders_.insert(TableBuilderEntry(header->tag(), builder));
  return builder;
}

CALLER_ATTACH Table::Builder*
    Font::Builder::NewTableBuilder(int32_t tag, ReadableFontData* src_data) {
  WritableFontDataPtr data;
  data.Attach(GetNewGrowableData(src_data));
  TableHeaderPtr header = new Table::Header(tag);
  TableBuilderPtr builder = Table::Builder::GetBuilder(this, header, data);
  table_builders_.insert(TableBuilderEntry(tag, builder));
  return builder;
}

void Font::Builder::TableBuilderTags(IntegerSet* key_set) {
  assert(key_set);
  key_set->clear();
  for (TableBuilderMap::iterator i = table_builders_.begin(),
                                 e = table_builders_.end(); i != e; ++i) {
    key_set->insert(i->first);
  }
}

void Font::Builder::RemoveTableBuilder(int32_t tag) {
  TableBuilderMap::iterator target = table_builders_.find(tag);
  if (target != table_builders_.end()) {
    table_builders_.erase(target);
  }
}

Font::Builder::Builder(FontFactory* factory)
    : factory_(factory), sfnt_version_(SFNTVERSION_1) {
}

void Font::Builder::LoadFont(InputStream* is) {
  // Note: we do not throw exception here for is.  This is more of an assertion.
  assert(is);
  FontInputStream font_is(is);
  TableHeaderSortedSet records;
  ReadHeader(&font_is, &records);
  LoadTableData(&records, &font_is, &data_blocks_);
  BuildAllTableBuilders(&data_blocks_, &table_builders_);
  font_is.Close();
}

void Font::Builder::LoadFont(ByteArray* ba,
                             int32_t offset_to_offset_table) {
  // Note: we do not throw exception here for is.  This is more of an assertion.
  assert(ba);
  WritableFontDataPtr fd = new WritableFontData(ba);
  TableHeaderSortedSet records;
  ReadHeader(fd, offset_to_offset_table, &records);
  LoadTableData(&records, fd, &data_blocks_);
  BuildAllTableBuilders(&data_blocks_, &table_builders_);
}

int32_t Font::Builder::SfntWrapperSize() {
  return Offset::kSfntHeaderSize +
         (Offset::kTableRecordSize * table_builders_.size());
}

void Font::Builder::BuildAllTableBuilders(DataBlockMap* table_data,
                                          TableBuilderMap* builder_map) {
  for (DataBlockMap::iterator record = table_data->begin(),
                              record_end = table_data->end();
                              record != record_end; ++record) {
    TableBuilderPtr builder;
    builder.Attach(GetTableBuilder(record->first.p_, record->second.p_));
    builder_map->insert(TableBuilderEntry(record->first->tag(), builder));
  }
  InterRelateBuilders(&table_builders_);
}

CALLER_ATTACH Table::Builder*
    Font::Builder::GetTableBuilder(Table::Header* header,
                                   WritableFontData* data) {
  return Table::Builder::GetBuilder(this, header, data);
}

void Font::Builder::BuildTablesFromBuilders(TableBuilderMap* builder_map,
                                            TableMap* table_map) {
  InterRelateBuilders(builder_map);

  // Now build all the tables.
  for (TableBuilderMap::iterator builder = builder_map->begin(),
                                 builder_end = builder_map->end();
                                 builder != builder_end; ++builder) {
    TablePtr table;
    if (builder->second && builder->second->ReadyToBuild()) {
#if !defined (SFNTLY_NO_EXCEPTION)
      try {
#endif
        table.Attach(down_cast<Table*>(builder->second->Build()));
#if !defined (SFNTLY_NO_EXCEPTION)
      } catch(IOException& e) {
        std::string builder_string = "Unable to build table - ";
        builder_string += typeid(builder->second).name();
        builder_string += e.what();
        throw RuntimeException(builder_string.c_str());
      }
#endif
    }
    if (table == NULL) {
#if defined (SFNTLY_NO_EXCEPTION)
      return;
#else
      std::string builder_string = "Unable to build table - ";
      builder_string += typeid(builder->second).name();
      throw RuntimeException(builder_string.c_str());
#endif
    }
    table_map->insert(TableMapEntry(table->header()->tag(), table));
  }
}

void Font::Builder::InterRelateBuilders(TableBuilderMap* builder_map) {
  FontHeaderTableBuilderPtr header_table_builder =
      down_cast<FontHeaderTable::Builder*>((*builder_map)[Tag::head].p_);
  HorizontalHeaderTableBuilderPtr horizontal_header_builder =
      down_cast<HorizontalHeaderTable::Builder*>((*builder_map)[Tag::hhea].p_);
  MaximumProfileTableBuilderPtr max_profile_builder =
      down_cast<MaximumProfileTable::Builder*>((*builder_map)[Tag::maxp].p_);
  LocaTableBuilderPtr loca_table_builder =
      down_cast<LocaTable::Builder*>((*builder_map)[Tag::loca].p_);
  HorizontalMetricsTableBuilderPtr horizontal_metrics_builder =
      down_cast<HorizontalMetricsTable::Builder*>(
          (*builder_map)[Tag::hmtx].p_);

  // set the inter table data required to build certain tables
  if (horizontal_metrics_builder != NULL) {
    if (max_profile_builder != NULL) {
      horizontal_metrics_builder->SetNumGlyphs(
          max_profile_builder->NumGlyphs());
    }
    if (horizontal_header_builder != NULL) {
      horizontal_metrics_builder->SetNumberOfHMetrics(
          horizontal_header_builder->NumberOfHMetrics());
    }
  }

  if (loca_table_builder != NULL) {
    if (max_profile_builder != NULL) {
      loca_table_builder->SetNumGlyphs(max_profile_builder->NumGlyphs());
    }
    if (header_table_builder != NULL) {
      loca_table_builder->SetFormatVersion(
          header_table_builder->IndexToLocFormat());
    }
  }
}

void Font::Builder::ReadHeader(FontInputStream* is,
                               TableHeaderSortedSet* records) {
  assert(records);
  sfnt_version_ = is->ReadFixed();
  num_tables_ = is->ReadUShort();
  search_range_ = is->ReadUShort();
  entry_selector_ = is->ReadUShort();
  range_shift_ = is->ReadUShort();

  for (int32_t table_number = 0; table_number < num_tables_; ++table_number) {
    // Need to use temporary vars here.  C++ evaluates function parameters from
    // right to left and thus breaks the order of input stream.
    int32_t tag = is->ReadULongAsInt();
    int64_t checksum = is->ReadULong();
    int32_t offset = is->ReadULongAsInt();
    int32_t length = is->ReadULongAsInt();
    TableHeaderPtr table = new Table::Header(tag, checksum, offset, length);
    records->insert(table);
  }
}

void Font::Builder::ReadHeader(ReadableFontData* fd,
                               int32_t offset,
                               TableHeaderSortedSet* records) {
  assert(records);
  sfnt_version_ = fd->ReadFixed(offset + Offset::kSfntVersion);
  num_tables_ = fd->ReadUShort(offset + Offset::kNumTables);
  search_range_ = fd->ReadUShort(offset + Offset::kSearchRange);
  entry_selector_ = fd->ReadUShort(offset + Offset::kEntrySelector);
  range_shift_ = fd->ReadUShort(offset + Offset::kRangeShift);

  int32_t table_offset = offset + Offset::kTableRecordBegin;
  for (int32_t table_number = 0;
       table_number < num_tables_;
       table_number++, table_offset += Offset::kTableRecordSize) {
    int32_t tag = fd->ReadULongAsInt(table_offset + Offset::kTableTag);
    int64_t checksum = fd->ReadULong(table_offset + Offset::kTableCheckSum);
    int32_t offset = fd->ReadULongAsInt(table_offset + Offset::kTableOffset);
    int32_t length = fd->ReadULongAsInt(table_offset + Offset::kTableLength);
    TableHeaderPtr table = new Table::Header(tag, checksum, offset, length);
    records->insert(table);
  }
}

void Font::Builder::LoadTableData(TableHeaderSortedSet* headers,
                                  FontInputStream* is,
                                  DataBlockMap* table_data) {
  assert(table_data);
  for (TableHeaderSortedSet::iterator
           table_header = headers->begin(), table_end = headers->end();
           table_header != table_end; ++table_header) {
    is->Skip((*table_header)->offset() - is->position());
    FontInputStream table_is(is, (*table_header)->length());
    int32_t roundup_length = ((*table_header)->length() + 3) & ~3;
    ByteArrayPtr array;
    array.Attach(factory_->GetNewArray(roundup_length));
    array->CopyFrom(&table_is, (*table_header)->length());
    WritableFontDataPtr data = new WritableFontData(array);
    table_data->insert(DataBlockEntry(*table_header, data));
  }
}

void Font::Builder::LoadTableData(TableHeaderSortedSet* headers,
                                  WritableFontData* fd,
                                  DataBlockMap* table_data) {
  for (TableHeaderSortedSet::iterator
           table_header = headers->begin(), table_end = headers->end();
           table_header != table_end; ++table_header) {
    int32_t roundup_length = ((*table_header)->length() + 3) & ~3;
    FontDataPtr sliced_data;
    sliced_data.Attach(fd->Slice((*table_header)->offset(), roundup_length));
    WritableFontDataPtr data = down_cast<WritableFontData*>(sliced_data.p_);
    table_data->insert(DataBlockEntry(*table_header, data));
  }
}

}  // namespace sfntly

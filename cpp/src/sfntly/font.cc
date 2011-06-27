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

#include <functional>
#include <algorithm>
#include <map>
#include <typeinfo>

#include "sfntly/font.h"
#include "sfntly/font_factory.h"
#include "sfntly/tag.h"
#include "sfntly/math/fixed1616.h"
#include "sfntly/math/font_math.h"
#include "sfntly/data/font_input_stream.h"
#include "sfntly/font_header_table.h"
#include "sfntly/horizontal_header_table.h"
#include "sfntly/horizontal_metrics_table.h"
#include "sfntly/maximum_profile_table.h"
#include "sfntly/loca_table.h"
#include "sfntly/port/exception_type.h"

namespace sfntly {

/******************************************************************************
 * constant definitions
 ******************************************************************************/
const int32_t PlatformId::kUnknown = -1;
const int32_t PlatformId::kUnicode = 0;
const int32_t PlatformId::kMacintosh = 1;
const int32_t PlatformId::kISO = 2;
const int32_t PlatformId::kWindows = 3;
const int32_t PlatformId::kCustom = 4;

const int32_t UnicodeEncodingId::kUnknown = -1;
const int32_t UnicodeEncodingId::kUnicode1_0 = 0;
const int32_t UnicodeEncodingId::kUnicode1_1 = 1;
const int32_t UnicodeEncodingId::kISO10646 = 2;
const int32_t UnicodeEncodingId::kUnicode2_0_BMP = 3;
const int32_t UnicodeEncodingId::kUnicode2_0 = 4;
const int32_t UnicodeEncodingId::kUnicodeVariationSequences = 5;

const int32_t WindowsEncodingId::kUnknown = 0xffffffff;
const int32_t WindowsEncodingId::kSymbol = 0;
const int32_t WindowsEncodingId::kUnicodeUCS2 = 1;
const int32_t WindowsEncodingId::kShiftJIS = 2;
const int32_t WindowsEncodingId::kPRC = 3;
const int32_t WindowsEncodingId::kBig5 = 4;
const int32_t WindowsEncodingId::kWansung = 5;
const int32_t WindowsEncodingId::kJohab = 6;
const int32_t WindowsEncodingId::kUnicodeUCS4 = 10;

const int32_t MacintoshEncodingId::kUnknown = -1;
const int32_t MacintoshEncodingId::kRoman = 0;
const int32_t MacintoshEncodingId::kJapanese = 1;
const int32_t MacintoshEncodingId::kChineseTraditional = 2;
const int32_t MacintoshEncodingId::kKorean = 3;
const int32_t MacintoshEncodingId::kArabic = 4;
const int32_t MacintoshEncodingId::kHebrew = 5;
const int32_t MacintoshEncodingId::kGreek = 6;
const int32_t MacintoshEncodingId::kRussian = 7;
const int32_t MacintoshEncodingId::kRSymbol = 8;
const int32_t MacintoshEncodingId::kDevanagari = 9;
const int32_t MacintoshEncodingId::kGurmukhi = 10;
const int32_t MacintoshEncodingId::kGujarati = 11;
const int32_t MacintoshEncodingId::kOriya = 12;
const int32_t MacintoshEncodingId::kBengali = 13;
const int32_t MacintoshEncodingId::kTamil = 14;
const int32_t MacintoshEncodingId::kTelugu = 15;
const int32_t MacintoshEncodingId::kKannada = 16;
const int32_t MacintoshEncodingId::kMalayalam = 17;
const int32_t MacintoshEncodingId::kSinhalese = 18;
const int32_t MacintoshEncodingId::kBurmese = 19;
const int32_t MacintoshEncodingId::kKhmer = 20;
const int32_t MacintoshEncodingId::kThai = 21;
const int32_t MacintoshEncodingId::kLaotian = 22;
const int32_t MacintoshEncodingId::kGeorgian = 23;
const int32_t MacintoshEncodingId::kArmenian = 24;
const int32_t MacintoshEncodingId::kChineseSimplified = 25;
const int32_t MacintoshEncodingId::kTibetan = 26;
const int32_t MacintoshEncodingId::kMongolian = 27;
const int32_t MacintoshEncodingId::kGeez = 28;
const int32_t MacintoshEncodingId::kSlavic = 29;
const int32_t MacintoshEncodingId::kVietnamese = 30;
const int32_t MacintoshEncodingId::kSindhi = 31;
const int32_t MacintoshEncodingId::kUninterpreted = 32;

const int32_t SFNTVERSION_1 = Fixed1616::fixed(1, 0);

const int32_t Font::Offset::kSfntVersion = 0;
const int32_t Font::Offset::kNumTables = 4;
const int32_t Font::Offset::kSearchRange = 6;
const int32_t Font::Offset::kEntrySelector = 8;
const int32_t Font::Offset::kRangeShift = 10;
const int32_t Font::Offset::kTableRecordBegin = 12;
const int32_t Font::Offset::kSfntHeaderSize = 12;
const int32_t Font::Offset::kTableTag = 0;
const int32_t Font::Offset::kTableCheckSum = 4;
const int32_t Font::Offset::kTableOffset = 8;
const int32_t Font::Offset::kTableLength = 12;
const int32_t Font::Offset::kTableRecordSize = 16;

/******************************************************************************
 * Font class
 ******************************************************************************/
Font::~Font() {}

Font::Font(FontFactory* factory, int32_t sfnt_version, ByteVector* digest,
           TableMap* tables)
    : factory_(factory), sfnt_version_(sfnt_version) {
  // non-trivial assignments that makes debugging hard if placed in
  // initialization list
  digest_ = *digest;
  tables_ = *tables;
}

int32_t Font::version() {
  return sfnt_version_;
}

ByteVector* Font::digest() {
  return &digest_;
}

int64_t Font::checksum() {
  return checksum_;
}

int32_t Font::numTables() {
  return (int32_t)tables_.size();
}

bool Font::hasTable(int32_t tag) {
  TableMap::const_iterator result = tables_.find(tag);
  TableMap::const_iterator end = tables_.end();
  return (result != end);
}

Table* Font::table(int32_t tag) {
  if (!hasTable(tag)) {
    return NULL;
  }
  return tables_[tag];
}

TableMap* Font::tables() {
  return &tables_;
}

void Font::serialize(OutputStream* os, IntegerList* table_ordering) {
  assert(table_ordering);
  IntegerList final_table_ordering;
  tableOrdering(table_ordering, &final_table_ordering);
  TableHeaderList table_records;
  buildTableHeadersForSerialization(&final_table_ordering, &table_records);

  FontOutputStream fos(os);
  serializeHeader(&fos, &table_records);
  serializeTables(&fos, &table_records);
}

void Font::buildTableHeadersForSerialization(IntegerList* table_ordering,
                                             TableHeaderList* table_headers) {
  assert(table_headers);
  assert(table_ordering);

  IntegerList final_table_ordering;
  tableOrdering(table_ordering, &final_table_ordering);
  int32_t table_offset = Offset::kTableRecordBegin + numTables() *
                         Offset::kTableRecordSize;
  for (IntegerList::iterator tag = final_table_ordering.begin(),
                             tag_end = final_table_ordering.end();
                             tag != tag_end; ++tag) {
    TablePtr table = tables_[*tag];
    if (table != NULL) {
      TableHeaderPtr header =
          new Table::Header(*tag, table->calculatedChecksum(), table_offset,
                            table->length());
      table_headers->push_back(header);
      table_offset += (table->length() + 3) & ~3;
    }
  }
}

void Font::serializeHeader(FontOutputStream* fos,
                           TableHeaderList* table_headers) {
  fos->writeFixed(sfnt_version_);
  fos->writeUShort(table_headers->size());
  int32_t log2_of_max_power_of_2 = FontMath::log2(table_headers->size());
  int32_t search_range = 2 << (log2_of_max_power_of_2 - 1 + 4);
  fos->writeUShort(search_range);
  fos->writeUShort(log2_of_max_power_of_2);
  fos->writeUShort((table_headers->size() * 16) - search_range);

  for (TableHeaderList::iterator record = table_headers->begin(),
                                 record_end = table_headers->end();
                                 record != record_end; ++record) {
    fos->writeULong((*record)->tag());
    fos->writeULong((int32_t)((*record)->checksum()));
    fos->writeULong((*record)->offset());
    fos->writeULong((*record)->length());
  }
}

void Font::serializeTables(FontOutputStream* fos,
                           TableHeaderList* table_headers) {
  ByteVector SERIALIZATION_FILLER(3);
  std::fill(SERIALIZATION_FILLER.begin(), SERIALIZATION_FILLER.end(), 0);
  for (TableHeaderList::iterator record = table_headers->begin(),
                                 end_of_headers = table_headers->end();
                                 record != end_of_headers; ++record) {
    TablePtr target_table = table((*record)->tag());
    if (target_table == NULL) {
#if defined (SFNTLY_NO_EXCEPTION)
      return;
#else
      throw IOException("Table out of sync with font header.");
#endif
    }
    int32_t table_size = target_table->serialize(fos);
    int32_t filler_size = ((table_size + 3) & ~3) - table_size;
    fos->write(&SERIALIZATION_FILLER, 0, filler_size);
  }
}

void Font::tableOrdering(IntegerList* default_table_ordering,
                         IntegerList* table_ordering) {
  assert(default_table_ordering);
  assert(table_ordering);
  table_ordering->clear();
  if (default_table_ordering->empty()) {
    defaultTableOrdering(default_table_ordering);
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
    if (hasTable(*tag)) {
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

void Font::defaultTableOrdering(IntegerList* default_table_ordering) {
  assert(default_table_ordering);
  default_table_ordering->clear();
  if (hasTable(Tag::CFF)) {
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

CALLER_ATTACH WritableFontData* Font::getNewData(int32_t size) {
  return factory_->getNewData(size);
}

/******************************************************************************
 * Font::Builder class
 ******************************************************************************/
Font::Builder::~Builder() {}

Font::Builder::Builder(FontFactory* factory)
    : factory_(factory), sfnt_version_(SFNTVERSION_1) {
}

void Font::Builder::loadFont(InputStream* is) {
  // Note: we do not throw exception here for is.  This is more of an assertion.
  assert(is);
  FontInputStream font_is(is);
  TableHeaderSortedSet records;
  readHeader(&font_is, &records);
  loadTableData(&records, &font_is, &data_blocks_);
  buildAllTableBuilders(&data_blocks_, &table_builders_);
  font_is.close();
}

void Font::Builder::loadFont(ByteArray* ba,
                             int32_t offset_to_offset_table) {
  // Note: we do not throw exception here for is.  This is more of an assertion.
  assert(ba);
  WritableFontDataPtr fd = new WritableFontData(ba);
  TableHeaderSortedSet records;
  readHeader(fd, offset_to_offset_table, &records);
  loadTableData(&records, fd, &data_blocks_);
  buildAllTableBuilders(&data_blocks_, &table_builders_);
}

CALLER_ATTACH Font::Builder* Font::Builder::getOTFBuilder(
    FontFactory* factory, InputStream* is) {
  FontBuilderPtr builder = new Builder(factory);
  builder->loadFont(is);
  return builder.detach();
}

CALLER_ATTACH Font::Builder* Font::Builder::getOTFBuilder(
    FontFactory* factory, ByteArray* ba, int32_t offset_to_offset_table) {
  FontBuilderPtr builder = new Builder(factory);
  builder->loadFont(ba, offset_to_offset_table);
  return builder.detach();
}

CALLER_ATTACH Font::Builder* Font::Builder::getOTFBuilder(
    FontFactory* factory) {
  FontBuilderPtr builder = new Builder(factory);
  return builder.detach();
}

bool Font::Builder::readyToBuild() {
  // just read in data with no manipulation
  if (table_builders_.empty() && !data_blocks_.empty()) {
    return true;
  }

  // TODO(stuartg): font level checks - required tables etc.
  for (TableBuilderMap::iterator table_builder = table_builders_.begin(),
                                 table_builder_end = table_builders_.end();
                                 table_builder != table_builder_end;
                                 ++table_builder) {
    if (!table_builder->second->readyToBuild())
      return false;
  }
  return true;
}

CALLER_ATTACH Font* Font::Builder::build() {
  TableMap tables;
  if (!table_builders_.empty()) {
    buildTablesFromBuilders(&table_builders_, &tables);
  }
  FontPtr font = new Font(factory_, sfnt_version_, &digest_, &tables);
  table_builders_.clear();
  data_blocks_.clear();
  return font.detach();
}

CALLER_ATTACH WritableFontData* Font::Builder::getNewData(int32_t capacity) {
  return factory_->getNewData(capacity);
}

CALLER_ATTACH WritableFontData* Font::Builder::getNewGrowableData(
    ReadableFontData* src_data) {
  return factory_->getNewGrowableData(src_data);
}

void Font::Builder::setDigest(ByteVector* digest) {
  digest_.clear();
  digest_ = *digest;
}

void Font::Builder::cleanTableBuilders() {
  table_builders_.clear();
}

bool Font::Builder::hasTableBuilder(int32_t tag) {
  return (table_builders_.find(tag) != table_builders_.end());
}

Table::Builder* Font::Builder::getTableBuilder(int32_t tag) {
  if (hasTableBuilder(tag))
    return table_builders_[tag];
  return NULL;
}

CALLER_ATTACH Table::Builder* Font::Builder::newTableBuilder(int32_t tag) {
  TableHeaderPtr header = new Table::Header(tag);
  TableBuilderPtr builder = Table::Builder::getBuilder(this, header, NULL);
  table_builders_.insert(TableBuilderEntry(header->tag(), builder));
  return builder;
}

CALLER_ATTACH Table::Builder* Font::Builder::newTableBuilder(
    int32_t tag, ReadableFontData* src_data) {
  WritableFontDataPtr data;
  data.attach(getNewGrowableData(src_data));
  TableHeaderPtr header = new Table::Header(tag);
  TableBuilderPtr builder = Table::Builder::getBuilder(this, header, data);
  table_builders_.insert(TableBuilderEntry(tag, builder));
  return builder;
}

TableBuilderMap* Font::Builder::tableBuilders() {
  return &table_builders_;
}

void Font::Builder::tableBuilderTags(IntegerSet* key_set) {
  assert(key_set);
  key_set->clear();
  for (TableBuilderMap::iterator i = table_builders_.begin(),
                                 e = table_builders_.end(); i != e; ++i) {
    key_set->insert(i->first);
  }
}

void Font::Builder::removeTableBuilder(int32_t tag) {
  TableBuilderMap::iterator target = table_builders_.find(tag);
  if (target != table_builders_.end()) {
    table_builders_.erase(target);
  }
}

int32_t Font::Builder::numberOfTableBuilders() {
  return (int32_t)table_builders_.size();
}

int32_t Font::Builder::sfntWrapperSize() {
  return Offset::kSfntHeaderSize +
         (Offset::kTableRecordSize * table_builders_.size());
}

void Font::Builder::buildAllTableBuilders(DataBlockMap* table_data,
                                          TableBuilderMap* builder_map) {
  for (DataBlockMap::iterator record = table_data->begin(),
                              record_end = table_data->end();
                              record != record_end; ++record) {
    TableBuilderPtr builder;
    builder.attach(getTableBuilder(record->first.p_, record->second.p_));
    builder_map->insert(TableBuilderEntry(record->first->tag(), builder));
  }
  interRelateBuilders(&table_builders_);
}

CALLER_ATTACH Table::Builder* Font::Builder::getTableBuilder(
    Table::Header* header, WritableFontData* data) {
  return Table::Builder::getBuilder(this, header, data);
}

void Font::Builder::buildTablesFromBuilders(TableBuilderMap* builder_map,
                                            TableMap* table_map) {
  interRelateBuilders(builder_map);

  // Now build all the tables.
  for (TableBuilderMap::iterator builder = builder_map->begin(),
                                 builder_end = builder_map->end();
                                 builder != builder_end; ++builder) {
    TablePtr table;
    if (builder->second->readyToBuild()) {
#if !defined (SFNTLY_NO_EXCEPTION)
      try {
#endif
        table.attach(down_cast<Table*>(builder->second->build()));
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

void Font::Builder::interRelateBuilders(TableBuilderMap* builder_map) {
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
      horizontal_metrics_builder->setNumGlyphs(
          max_profile_builder->numGlyphs());
    }
    if (horizontal_header_builder != NULL) {
      horizontal_metrics_builder->setNumberOfHMetrics(
          horizontal_header_builder->numberOfHMetrics());
    }
  }

  if (loca_table_builder != NULL) {
    if (max_profile_builder != NULL) {
      loca_table_builder->setNumGlyphs(max_profile_builder->numGlyphs());
    }
    if (header_table_builder != NULL) {
      loca_table_builder->setFormatVersion(
          header_table_builder->indexToLocFormat());
    }
  }
}

void Font::Builder::readHeader(FontInputStream* is,
                               TableHeaderSortedSet* records) {
  assert(records);
  sfnt_version_ = is->readFixed();
  num_tables_ = is->readUShort();
  search_range_ = is->readUShort();
  entry_selector_ = is->readUShort();
  range_shift_ = is->readUShort();

  for (int32_t table_number = 0; table_number < num_tables_; ++table_number) {
    // Need to use temporary vars here.  C++ evaluates function parameters from
    // right to left and thus breaks the order of input stream.
    int32_t tag = is->readULongAsInt();
    int64_t checksum = is->readULong();
    int32_t offset = is->readULongAsInt();
    int32_t length = is->readULongAsInt();
    TableHeaderPtr table = new Table::Header(tag, checksum, offset, length);
    records->insert(table);
  }
}

void Font::Builder::loadTableData(TableHeaderSortedSet* headers,
                                  FontInputStream* is,
                                  DataBlockMap* table_data) {
  assert(table_data);
  for (TableHeaderSortedSet::iterator
           table_header = headers->begin(), table_end = headers->end();
           table_header != table_end; ++table_header) {
    is->skip((*table_header)->offset() - is->position());
    FontInputStream table_is(is, (*table_header)->length());
    int32_t roundup_length = ((*table_header)->length() + 3) & ~3;
    ByteArrayPtr array = factory_->getNewArray(roundup_length);
    array->copyFrom(&table_is, (*table_header)->length());
    WritableFontDataPtr data = new WritableFontData(array);
    table_data->insert(DataBlockEntry(*table_header, data));
  }
}

void Font::Builder::readHeader(ReadableFontData* fd, int32_t offset,
                               TableHeaderSortedSet* records) {
  assert(records);
  sfnt_version_ = fd->readFixed(offset + Offset::kSfntVersion);
  num_tables_ = fd->readUShort(offset + Offset::kNumTables);
  search_range_ = fd->readUShort(offset + Offset::kSearchRange);
  entry_selector_ = fd->readUShort(offset + Offset::kEntrySelector);
  range_shift_ = fd->readUShort(offset + Offset::kRangeShift);

  int32_t table_offset = offset + Offset::kTableRecordBegin;
  for (int32_t table_number = 0;
       table_number < num_tables_;
       table_number++, table_offset += Offset::kTableRecordSize) {
    int32_t tag = fd->readULongAsInt(table_offset + Offset::kTableTag);
    int64_t checksum = fd->readULong(table_offset + Offset::kTableCheckSum);
    int32_t offset = fd->readULongAsInt(table_offset + Offset::kTableOffset);
    int32_t length = fd->readULongAsInt(table_offset + Offset::kTableLength);
    TableHeaderPtr table = new Table::Header(tag, checksum, offset, length);
    records->insert(table);
  }
}

void Font::Builder::loadTableData(TableHeaderSortedSet* headers,
                                  WritableFontData* fd,
                                  DataBlockMap* table_data) {
  for (TableHeaderSortedSet::iterator
           table_header = headers->begin(), table_end = headers->end();
           table_header != table_end; ++table_header) {
    int32_t roundup_length = ((*table_header)->length() + 3) & ~3;
    FontDataPtr sliced_data;
    sliced_data.attach(fd->slice((*table_header)->offset(), roundup_length));
    WritableFontDataPtr data = down_cast<WritableFontData*>(sliced_data.p_);
    table_data->insert(DataBlockEntry(*table_header, data));
  }
}

}  // namespace sfntly

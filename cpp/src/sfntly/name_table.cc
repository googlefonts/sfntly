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

// TODO(arthurhsu): IMPLEMENT: not really used and tested, need cleanup

#include "sfntly/name_table.h"

namespace sfntly {
/******************************************************************************
 * NameTable class
 ******************************************************************************/
NameTable::NameTable(Header* header, ReadableFontData* data)
    : Table(header, data) {}

NameTable::~NameTable() {}

int32_t NameTable::format() {
  return data_->readUShort(Offset::kFormat);
}

int32_t NameTable::nameCount() {
  return data_->readUShort(Offset::kCount);
}

int32_t NameTable::stringOffset() {
  return data_->readUShort(Offset::kStringOffset);
}

int32_t NameTable::offsetForNameRecord(int32_t index) {
  return Offset::kNameRecordStart + index * Offset::kNameRecordSize;
}

int32_t NameTable::platformId(int32_t index) {
  return data_->readUShort(Offset::kNameRecordPlatformId +
                           offsetForNameRecord(index));
}

int32_t NameTable::encodingId(int32_t index) {
  return data_->readUShort(Offset::kNameRecordEncodingId +
                           offsetForNameRecord(index));
}

int32_t NameTable::languageId(int32_t index) {
  return data_->readUShort(Offset::kNameRecordLanguageId +
                           offsetForNameRecord(index));
}

int32_t NameTable::nameId(int32_t index) {
  return data_->readUShort(Offset::kNameRecordNameId +
                           offsetForNameRecord(index));
}

int32_t NameTable::nameLength(int32_t index) {
  return data_->readUShort(Offset::kNameRecordStringLength +
                           offsetForNameRecord(index));
}

int32_t NameTable::nameOffset(int32_t index) {
  return data_->readUShort(Offset::kNameRecordStringOffset +
                           offsetForNameRecord(index) + stringOffset());
}

void NameTable::nameAsBytes(int32_t index, ByteVector* b) {
  assert(b);
  int32_t length = nameLength(index);
  b->clear();
  b->resize(length);
  data_->readBytes(nameOffset(index), b, 0, length);
}

CALLER_ATTACH NameTable::NameEntry* NameTable::nameEntry(int32_t index) {
  ByteVector b;
  nameAsBytes(index, &b);
  NameEntryPtr instance = new NameEntry(platformId(index), encodingId(index),
                                        languageId(index), nameId(index), b);
  return instance.detach();
}

/******************************************************************************
 * NameTable::NameEntry class
 ******************************************************************************/
void NameTable::NameEntry::init(int32_t platform_id, int32_t encoding_id,
                                int32_t language_id, int32_t name_id,
                                const ByteVector* name_bytes) {
  platform_id_ = platform_id;
  encoding_id_ = encoding_id;
  language_id_ = language_id;
  name_id_ = name_id;
  if (name_bytes)
    name_bytes_ = *name_bytes;
}

NameTable::NameEntry::NameEntry() {
  init(0, 0, 0, 0, NULL);
}

NameTable::NameEntry::NameEntry(int32_t platform_id, int32_t encoding_id,
                                int32_t language_id, int32_t name_id,
                                const ByteVector& name_bytes) {
  init(platform_id, encoding_id, language_id, name_id, &name_bytes);
}

NameTable::NameEntry::~NameEntry() {}
int32_t NameTable::NameEntry::platformId() { return platform_id_; }
int32_t NameTable::NameEntry::encodingId() { return encoding_id_; }
int32_t NameTable::NameEntry::languageId() { return language_id_; }
int32_t NameTable::NameEntry::nameId() { return name_id_; }
int32_t NameTable::NameEntry::nameBytesLength() { return name_bytes_.size(); }
ByteVector* NameTable::NameEntry::nameBytes() { return &name_bytes_; }

bool NameTable::NameEntry::operator==(const NameEntry& obj) {
  return (encoding_id_ == obj.encoding_id_ &&
          language_id_ == obj.language_id_ &&
          platform_id_ == obj.platform_id_ &&
          name_id_ == obj.name_id_);
}

int NameTable::NameEntry::hashCode() {
  return ((encoding_id_ & 0x3f) << 26) | ((name_id_ & 0x3f) << 16) |
         ((platform_id_ & 0x0f) << 12) | (language_id_ & 0xff);
}

int NameTable::NameEntry::compareTo(const NameEntry& o) {
  if (platform_id_ != o.platform_id_) {
    return platform_id_ - o.platform_id_;
  }
  if (encoding_id_ != o.encoding_id_) {
    return encoding_id_ - o.encoding_id_;
  }
  if (language_id_ != o.language_id_) {
    return language_id_ - o.language_id_;
  }
  return name_id_ - o.name_id_;
}

/******************************************************************************
 * NameTable::NameEntryBuilder class
 ******************************************************************************/
NameTable::NameEntryBuilder::NameEntryBuilder() {
  init(0, 0, 0, 0, NULL);
}

NameTable::NameEntryBuilder::NameEntryBuilder(
    int32_t platform_id, int32_t encoding_id, int32_t language_id,
    int32_t name_id, const ByteVector& name_bytes) {
  init(platform_id, encoding_id, language_id, name_id, &name_bytes);
}

NameTable::NameEntryBuilder::NameEntryBuilder(
    int32_t platform_id, int32_t encoding_id, int32_t language_id,
    int32_t name_id) {
  init(platform_id, encoding_id, language_id, name_id, NULL);
}

NameTable::NameEntryBuilder::NameEntryBuilder(NameEntry* b) {
  init(b->platform_id_, b->encoding_id_, b->language_id_, b->name_id_,
       b->nameBytes());
}

NameTable::NameEntryBuilder::~NameEntryBuilder() {}

/******************************************************************************
 * NameTable::NameEntryIterator class
 ******************************************************************************/
NameTable::NameEntryIterator::NameEntryIterator(NameTable* table,
                                                NameEntryFilter* filter) :
    table_(table), name_index_(0), filter_(filter) {
}

bool NameTable::NameEntryIterator::hasNext() {
  if (!filter_) {
    if (name_index_ < table_->nameCount()) {
      return true;
    }
    return false;
  }
  for (; name_index_ < table_->nameCount(); ++name_index_) {
    if (filter_->accept(table_->platformId(name_index_),
                        table_->encodingId(name_index_),
                        table_->languageId(name_index_),
                        table_->nameId(name_index_))) {
      return true;
    }
  }
  return false;
}

NameTable::NameEntry* NameTable::NameEntryIterator::next() {
  if (!hasNext())
    return NULL;
  return table_->nameEntry(name_index_++);
}

/******************************************************************************
 * NameTable::Builder class
 ******************************************************************************/
NameTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header, WritableFontData* data) :
    Table::ArrayElementTableBuilder(font_builder, header, data) {
}

NameTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header, ReadableFontData* data) :
    Table::ArrayElementTableBuilder(font_builder, header, data) {
}

void NameTable::Builder::initialize(ReadableFontData* data) {
  if (data) {
    NameTablePtr table = new NameTable(header(), data);
    NameEntryIterator name_iter(table, NULL);
    while (name_iter.hasNext()) {
      NameEntryPtr name_entry(name_iter.next());
      NameEntryBuilderPtr name_entry_builder = new NameEntryBuilder(name_entry);
      name_entry_map_.insert(NameEntryMapEntry(name_entry_builder,
                                               name_entry_builder));
    }
  }
}

int32_t NameTable::Builder::subSerialize(WritableFontData* new_data) {
  int32_t string_table_start_offset =
      NameTable::Offset::kNameRecordStart + name_entry_map_.size() *
      NameTable::Offset::kNameRecordSize;

  // header
  new_data->writeUShort(NameTable::Offset::kFormat, 0);
  new_data->writeUShort(NameTable::Offset::kCount, name_entry_map_.size());
  new_data->writeUShort(NameTable::Offset::kStringOffset,
                        string_table_start_offset);
  int32_t name_record_offset = NameTable::Offset::kNameRecordStart;
  int32_t string_offset = 0;
  for (NameEntryMap::iterator b = name_entry_map_.begin(),
                              end = name_entry_map_.end(); b != end; ++b) {
    new_data->writeUShort(NameTable::Offset::kNameRecordPlatformId,
                          b->first->platformId());
    new_data->writeUShort(NameTable::Offset::kNameRecordEncodingId,
                          b->first->encodingId());
    new_data->writeUShort(NameTable::Offset::kNameRecordLanguageId,
                          b->first->languageId());
    new_data->writeUShort(NameTable::Offset::kNameRecordNameId,
                          b->first->nameId());
    new_data->writeUShort(NameTable::Offset::kNameRecordStringLength,
                          b->first->nameBytesLength());
    new_data->writeUShort(NameTable::Offset::kNameRecordStringOffset,
                          string_offset);
    name_record_offset += NameTable::Offset::kNameRecordSize;
    string_offset += new_data->writeBytes(
        string_offset + string_table_start_offset, b->first->nameBytes());
  }

  return string_offset + string_table_start_offset;
}

bool NameTable::Builder::subReadyToSerialize() {
  return !name_entry_map_.empty();
}

int32_t NameTable::Builder::subDataSizeToSerialize() {
  if (name_entry_map_.empty()) {
    return 0;
  }

  int32_t size = NameTable::Offset::kNameRecordStart + name_entry_map_.size() *
                 NameTable::Offset::kNameRecordSize;
  for (NameEntryMap::iterator b = name_entry_map_.begin(),
                              end = name_entry_map_.end(); b != end; ++b) {
    size += b->first->nameBytesLength();
  }
  return size;
}

void NameTable::Builder::subDataSet() {
  name_entry_map_.clear();
  setModelChanged(false);
}

CALLER_ATTACH FontDataTable* NameTable::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table = new NameTable(header(), data);
  return table.detach();
}

}  // namespace sfntly

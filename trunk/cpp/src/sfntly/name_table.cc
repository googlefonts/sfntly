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

#include "port/type.h"

#include <stdio.h>
#include <string.h>

#include <unicode/unistr.h>

#include "sfntly/name_table.h"
#include "sfntly/font.h"
#include "sfntly/port/exception_type.h"

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
                           offsetForNameRecord(index)) + stringOffset();
}

void NameTable::nameAsBytes(int32_t index, ByteVector* b) {
  assert(b);
  int32_t length = nameLength(index);
  b->clear();
  b->resize(length);
  data_->readBytes(nameOffset(index), b, 0, length);
}

void NameTable::nameAsBytes(int32_t platform_id, int32_t encoding_id,
                            int32_t language_id, int32_t name_id,
                            ByteVector* b) {
  assert(b);
  NameEntryPtr entry;
  entry.attach(nameEntry(platform_id, encoding_id, language_id, name_id));
  if (entry) {
    ByteVector* name = entry->nameAsBytes();
    std::copy(name->begin(), name->end(), b->begin());
  }
}

UChar* NameTable::name(int index) {
  ByteVector b;
  nameAsBytes(index, &b);
  return convertFromNameBytes(&b, platformId(index), encodingId(index));
}

UChar* NameTable::name(int32_t platform_id, int32_t encoding_id,
                       int32_t language_id, int32_t name_id) {
  NameEntryPtr entry;
  entry.attach(nameEntry(platform_id, encoding_id, language_id, name_id));
  if (entry) {
    return entry->name();
  }
  return NULL;
}

CALLER_ATTACH NameTable::NameEntry* NameTable::nameEntry(int32_t index) {
  ByteVector b;
  nameAsBytes(index, &b);
  NameEntryPtr instance = new NameEntry(platformId(index), encodingId(index),
                                        languageId(index), nameId(index), b);
  return instance.detach();
}

CALLER_ATTACH NameTable::NameEntry* NameTable::nameEntry(int32_t platform_id,
                                                         int32_t encoding_id,
                                                         int32_t language_id,
                                                         int32_t name_id) {
  NameTable::NameEntryFilterInPlace filter(platform_id, encoding_id,
                                           language_id, name_id);
  NameTable::NameEntryIterator* name_entry_iter = iterator(&filter);
  NameEntryPtr result;
  if (name_entry_iter->hasNext()) {
    result = name_entry_iter->next();
    delete name_entry_iter;
    return result;
  }
  delete name_entry_iter;
  return NULL;
}

NameTable::NameEntryIterator* NameTable::iterator() {
  return new NameTable::NameEntryIterator(this);
}

NameTable::NameEntryIterator* NameTable::iterator(NameEntryFilter* filter) {
  return new NameTable::NameEntryIterator(this, filter);
}

const char* NameTable::getEncodingName(int32_t platform_id,
                                       int32_t encoding_id) {
  switch (platform_id) {
    case PlatformId::kUnicode:
      return "UTF-16BE";
    case PlatformId::kMacintosh:
      switch (encoding_id) {
        case MacintoshEncodingId::kRoman:
          return "MacRoman";
        case MacintoshEncodingId::kJapanese:
          return "Shift-JIS";
        case MacintoshEncodingId::kChineseTraditional:
          return "Big5";
        case MacintoshEncodingId::kKorean:
          return "EUC-KR";
        case MacintoshEncodingId::kArabic:
          return "MacArabic";
        case MacintoshEncodingId::kHebrew:
          return "MacHebrew";
        case MacintoshEncodingId::kGreek:
          return "MacGreek";
        case MacintoshEncodingId::kRussian:
          return "MacCyrillic";
        case MacintoshEncodingId::kRSymbol:
          return "MacSymbol";
        case MacintoshEncodingId::kThai:
          return "MacThai";
        case MacintoshEncodingId::kChineseSimplified:
          return "EUC-CN";
        default:  // unknown/unconfirmed cases are not ported
          break;
      }
      break;
    case PlatformId::kISO:
      break;
    case PlatformId::kWindows:
      switch (encoding_id) {
        case WindowsEncodingId::kSymbol:
        case WindowsEncodingId::kUnicodeUCS2:
          return "UTF-16BE";
        case WindowsEncodingId::kShiftJIS:
          return "windows-933";
        case WindowsEncodingId::kPRC:
          return "windows-936";
        case WindowsEncodingId::kBig5:
          return "windows-950";
        case WindowsEncodingId::kWansung:
          return "windows-949";
        case WindowsEncodingId::kJohab:
          return "ms1361";
        case WindowsEncodingId::kUnicodeUCS4:
          return "UCS-4";
      }
      break;
    case PlatformId::kCustom:
      break;
    default:
      break;
  }
  return NULL;
}

UConverter* NameTable::getCharset(int32_t platform_id, int32_t encoding_id) {
  UErrorCode error_code = U_ZERO_ERROR;
  UConverter* conv = ucnv_open(getEncodingName(platform_id, encoding_id),
                               &error_code);
  if (U_SUCCESS(error_code)) {
    return conv;
  }

  if (conv) {
    ucnv_close(conv);
  }
  return NULL;
}

void NameTable::convertToNameBytes(const UChar* name, int32_t platform_id,
                                   int32_t encoding_id, ByteVector* b) {
  assert(b);
  assert(name);
  b->clear();
  UConverter* cs = getCharset(platform_id, encoding_id);
  if (cs == NULL) {
    return;
  }

  // Preflight to get buffer size.
  UErrorCode error_code = U_ZERO_ERROR;
  int32_t length = ucnv_fromUChars(cs, NULL, 0, name, -1, &error_code);
  b->resize(length + 4);  // the longest termination "\0" is 4 bytes
  memset(&((*b)[0]), 0, length + 4);
  error_code = U_ZERO_ERROR;
  ucnv_fromUChars(cs, reinterpret_cast<char*>(&((*b)[0])), length + 4, name,
                  -1, &error_code);
  if (!U_SUCCESS(error_code)) {
    b->clear();
  }
  ucnv_close(cs);
}

UChar* NameTable::convertFromNameBytes(ByteVector* name_bytes,
    int32_t platform_id, int32_t encoding_id) {
  if (name_bytes == NULL) {
    return NULL;
  }
  UConverter* cs = getCharset(platform_id, encoding_id);
  UErrorCode error_code = U_ZERO_ERROR;
  if (cs == NULL) {
    char buffer[11] = {0};
#if defined (WIN32)
    _itoa_s(platform_id, buffer, 16);
#else
    snprintf(buffer, sizeof(buffer), "%x", platform_id);
#endif
    UChar* result = new UChar[12];
    memset(result, 0, sizeof(UChar) * 12);
    cs = ucnv_open("utf-8", &error_code);
    if (U_SUCCESS(error_code)) {
      ucnv_toUChars(cs, result, 12, buffer, 11, &error_code);
      ucnv_close(cs);
      if (U_SUCCESS(error_code)) {
        return result;
      }
    }
    delete[] result;
    return NULL;
  }

  // No preflight needed here, we will be bigger.
  UChar* output_buffer = new UChar[name_bytes->size() + 1];
  memset(output_buffer, 0, sizeof(UChar) * (name_bytes->size() + 1));
  int32_t length = ucnv_toUChars(cs, output_buffer, name_bytes->size(),
                                 reinterpret_cast<char*>(&((*name_bytes)[0])),
                                 name_bytes->size(), &error_code);
  ucnv_close(cs);
  if (length > 0) {
    return output_buffer;
  }

  delete[] output_buffer;
  return NULL;
}

/******************************************************************************
 * NameTable::NameEntryId class
 ******************************************************************************/
NameTable::NameEntryId::NameEntryId()
    : platform_id_(0), encoding_id_(0), language_id_(0), name_id_(0) {
}

NameTable::NameEntryId::NameEntryId(int32_t platform_id, int32_t encoding_id,
                                    int32_t language_id, int32_t name_id)
    : platform_id_(platform_id), encoding_id_(encoding_id),
      language_id_(language_id), name_id_(name_id) {
}

NameTable::NameEntryId::NameEntryId(const NameTable::NameEntryId& rhs) {
  *this = rhs;
}

NameTable::NameEntryId::~NameEntryId() {}

int32_t NameTable::NameEntryId::getPlatformId() const {
  return platform_id_;
}

int32_t NameTable::NameEntryId::getEncodingId() const {
  return encoding_id_;
}

int32_t NameTable::NameEntryId::getLanguageId() const {
  return language_id_;
}

int32_t NameTable::NameEntryId::getNameId() const {
  return name_id_;
}

const NameTable::NameEntryId& NameTable::NameEntryId::operator=(
    const NameTable::NameEntryId& rhs) const {
  platform_id_ = rhs.platform_id_;
  encoding_id_ = rhs.encoding_id_;
  language_id_ = rhs.language_id_;
  name_id_ = rhs.name_id_;
  return *this;
}

bool NameTable::NameEntryId::operator==(const NameEntryId& rhs) const {
  return platform_id_ == rhs.platform_id_ && encoding_id_ == rhs.encoding_id_ &&
         language_id_ == rhs.language_id_ && name_id_ == rhs.name_id_;
}

bool NameTable::NameEntryId::operator<(const NameEntryId& rhs) const {
  if (platform_id_ != rhs.platform_id_) return platform_id_ < rhs.platform_id_;
  if (encoding_id_ != rhs.encoding_id_) return encoding_id_ < rhs.encoding_id_;
  if (language_id_ != rhs.language_id_) return language_id_ < rhs.language_id_;
  return name_id_ < rhs.name_id_;
}

/******************************************************************************
 * NameTable::NameEntry class
 ******************************************************************************/
void NameTable::NameEntry::init(int32_t platform_id, int32_t encoding_id,
                                int32_t language_id, int32_t name_id,
                                const ByteVector* name_bytes) {
  name_entry_id_ = NameEntryId(platform_id, encoding_id, language_id, name_id);
  if (name_bytes) {
    name_bytes_ = *name_bytes;
  } else {
    name_bytes_.clear();
  }
}

NameTable::NameEntry::NameEntry() {
  init(0, 0, 0, 0, NULL);
}

NameTable::NameEntry::NameEntry(const NameEntryId& name_entry_id,
                                const ByteVector& name_bytes) {
  init(name_entry_id.getPlatformId(), name_entry_id.getEncodingId(),
       name_entry_id.getLanguageId(), name_entry_id.getNameId(), &name_bytes);
}

NameTable::NameEntry::NameEntry(int32_t platform_id, int32_t encoding_id,
                                int32_t language_id, int32_t name_id,
                                const ByteVector& name_bytes) {
  init(platform_id, encoding_id, language_id, name_id, &name_bytes);
}

NameTable::NameEntry::~NameEntry() {}

NameTable::NameEntryId& NameTable::NameEntry::getNameEntryId() {
  return name_entry_id_;
}

int32_t NameTable::NameEntry::platformId() {
  return name_entry_id_.getPlatformId();
}

int32_t NameTable::NameEntry::encodingId() {
  return name_entry_id_.getEncodingId();
}

int32_t NameTable::NameEntry::languageId() {
  return name_entry_id_.getLanguageId();
}

int32_t NameTable::NameEntry::nameId() {
  return name_entry_id_.getNameId();
}

int32_t NameTable::NameEntry::nameBytesLength() {
  return name_bytes_.size();
}

ByteVector* NameTable::NameEntry::nameAsBytes() {
  return &name_bytes_;
}

UChar* NameTable::NameEntry::name() {
  return NameTable::convertFromNameBytes(&name_bytes_, platformId(),
                                         encodingId());
}

bool NameTable::NameEntry::operator==(const NameEntry& rhs) const {
  return (name_entry_id_ == rhs.name_entry_id_ &&
          name_bytes_ == rhs.name_bytes_);
}

/******************************************************************************
 * NameTable::NameEntryBuilder class
 ******************************************************************************/
NameTable::NameEntryBuilder::NameEntryBuilder() {
  init(0, 0, 0, 0, NULL);
}

NameTable::NameEntryBuilder::NameEntryBuilder(
    const NameEntryId& name_entry_id, const ByteVector& name_bytes) {
  init(name_entry_id.getPlatformId(), name_entry_id.getEncodingId(),
       name_entry_id.getLanguageId(), name_entry_id.getNameId(), &name_bytes);
}

NameTable::NameEntryBuilder::NameEntryBuilder(
    const NameEntryId& name_entry_id) {
  init(name_entry_id.getPlatformId(), name_entry_id.getEncodingId(),
       name_entry_id.getLanguageId(), name_entry_id.getNameId(), NULL);
}

NameTable::NameEntryBuilder::NameEntryBuilder(NameEntry* b) {
  init(b->platformId(), b->encodingId(), b->languageId(), b->nameId(),
       b->nameAsBytes());
}

NameTable::NameEntryBuilder::~NameEntryBuilder() {}

void NameTable::NameEntryBuilder::setName(const UChar* name) {
  if (name == NULL) {
    name_entry_->name_bytes_.clear();
    return;
  }
  NameTable::convertToNameBytes(name, name_entry_->platformId(),
                                name_entry_->encodingId(),
                                &name_entry_->name_bytes_);
}

void NameTable::NameEntryBuilder::setName(const ByteVector& name_bytes) {
  name_entry_->name_bytes_.clear();
  std::copy(name_bytes.begin(), name_bytes.end(),
            name_entry_->name_bytes_.begin());
}

void NameTable::NameEntryBuilder::setName(const ByteVector& name_bytes,
    int32_t offset, int32_t length) {
  name_entry_->name_bytes_.clear();
  std::copy(name_bytes.begin() + offset, name_bytes.begin() + offset + length,
            name_entry_->name_bytes_.begin());
}

CALLER_ATTACH NameTable::NameEntry* NameTable::NameEntryBuilder::entry() {
  return name_entry_;
}

void NameTable::NameEntryBuilder::init(int32_t platform_id, int32_t encoding_id,
    int32_t language_id, int32_t name_id, const ByteVector* name_bytes) {
  name_entry_ = new NameEntry();
  name_entry_->init(platform_id, encoding_id, language_id, name_id, name_bytes);
}

/******************************************************************************
 * NameTable::NameEntryFilterInPlace class (C++ port only)
 ******************************************************************************/
NameTable::NameEntryFilterInPlace::NameEntryFilterInPlace(int32_t platform_id,
    int32_t encoding_id, int32_t language_id, int32_t name_id)
    : platform_id_(platform_id), encoding_id_(encoding_id),
      language_id_(language_id), name_id_(name_id) {
}

bool NameTable::NameEntryFilterInPlace::accept(int32_t platform_id,
    int32_t encoding_id, int32_t language_id, int32_t name_id) {
  return (platform_id_ == platform_id && encoding_id_ == encoding_id &&
          language_id_ == language_id && name_id_ == name_id);
}

/******************************************************************************
 * NameTable::NameEntryIterator class
 ******************************************************************************/
void NameTable::NameEntryIterator::init(NameTable* table,
                                        NameEntryFilter* filter) {
  table_ = table;
  filter_ = filter;
  name_index_ = 0;
}

NameTable::NameEntryIterator::NameEntryIterator(NameTable* table) {
  init(table, NULL);
}

NameTable::NameEntryIterator::NameEntryIterator(NameTable* table,
                                                NameEntryFilter* filter) {
  init(table, filter);
}

NameTable::NameEntryIterator::~NameEntryIterator() {
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

CALLER_ATTACH NameTable::NameEntry* NameTable::NameEntryIterator::next() {
  if (!hasNext())
    return NULL;
  return table_->nameEntry(name_index_++);
}

void NameTable::NameEntryIterator::remove() {
#if !defined (SFNTLY_NO_EXCEPTION)
  throw UnsupportedOperationException(
            "Cannot remove a CMap table from an existing font.");
#endif
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
      NameEntryPtr name_entry = name_iter.next();
      NameEntryBuilderPtr name_entry_builder = new NameEntryBuilder(name_entry);
      NameEntry* builder_entry = name_entry_builder->entry();
      NameEntryId probe = builder_entry->getNameEntryId();
      name_entry_map_[probe] = name_entry_builder;
    }
  }
}

NameTable::NameEntryBuilderMap* NameTable::Builder::getNameBuilders() {
  if (name_entry_map_.empty()) {
    initialize(internalReadData());
  }
  setModelChanged();
  return &name_entry_map_;
}

void NameTable::Builder::revertNames() {
  name_entry_map_.clear();
  setModelChanged(false);
}

int32_t NameTable::Builder::builderCount() {
  getNameBuilders();  // ensure name_entry_map_ is built
  return (int32_t)name_entry_map_.size();
}

bool NameTable::Builder::has(int32_t platform_id, int32_t encoding_id,
                             int32_t language_id, int32_t name_id) {
  NameEntryId probe(platform_id, encoding_id, language_id, name_id);
  getNameBuilders();  // ensure name_entry_map_ is built
  return (name_entry_map_.find(probe) != name_entry_map_.end());
}

CALLER_ATTACH NameTable::NameEntryBuilder* NameTable::Builder::nameBuilder(
    int32_t platform_id, int32_t encoding_id, int32_t language_id,
    int32_t name_id) {
  NameEntryId probe(platform_id, encoding_id, language_id, name_id);
  NameEntryBuilderMap builders;
  getNameBuilders();  // ensure name_entry_map_ is built
  if (name_entry_map_.find(probe) != name_entry_map_.end()) {
    return name_entry_map_[probe];
  }
  NameEntryBuilderPtr builder = new NameEntryBuilder(probe);
  name_entry_map_[probe] = builder;
  return builder.detach();
}

bool NameTable::Builder::remove(int32_t platform_id, int32_t encoding_id,
                                int32_t language_id, int32_t name_id) {
  NameEntryId probe(platform_id, encoding_id, language_id, name_id);
  getNameBuilders();  // ensure name_entry_map_ is built
  NameEntryBuilderMap::iterator position = name_entry_map_.find(probe);
  if (position != name_entry_map_.end()) {
    name_entry_map_.erase(position);
    return true;
  }
  return false;
}

CALLER_ATTACH FontDataTable* NameTable::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table = new NameTable(header(), data);
  return table.detach();
}

void NameTable::Builder::subDataSet() {
  name_entry_map_.clear();
  setModelChanged(false);
}

int32_t NameTable::Builder::subDataSizeToSerialize() {
  if (name_entry_map_.empty()) {
    return 0;
  }

  int32_t size = NameTable::Offset::kNameRecordStart + name_entry_map_.size() *
                 NameTable::Offset::kNameRecordSize;
  for (NameEntryBuilderMap::iterator b = name_entry_map_.begin(),
                                     end = name_entry_map_.end();
                                     b != end; ++b) {
    NameEntryBuilderPtr p = b->second;
    NameEntry* entry = p->entry();
    size += entry->nameBytesLength();
  }
  return size;
}

bool NameTable::Builder::subReadyToSerialize() {
  return !name_entry_map_.empty();
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
  // Note: we offered operator< in NameEntryId, which will be used by std::less,
  //       and therefore our map will act like TreeMap in Java to provide
  //       sorted key set.
  for (NameEntryBuilderMap::iterator b = name_entry_map_.begin(),
                                     end = name_entry_map_.end();
                                     b != end; ++b) {
    new_data->writeUShort(
        name_record_offset + NameTable::Offset::kNameRecordPlatformId,
        b->first.getPlatformId());
    new_data->writeUShort(
        name_record_offset + NameTable::Offset::kNameRecordEncodingId,
        b->first.getEncodingId());
    new_data->writeUShort(
        name_record_offset + NameTable::Offset::kNameRecordLanguageId,
        b->first.getLanguageId());
    new_data->writeUShort(
        name_record_offset + NameTable::Offset::kNameRecordNameId,
        b->first.getNameId());
    NameEntry* builder_entry = b->second->entry();
    new_data->writeUShort(
        name_record_offset + NameTable::Offset::kNameRecordStringLength,
        builder_entry->nameBytesLength());
    new_data->writeUShort(
        name_record_offset + NameTable::Offset::kNameRecordStringOffset,
        string_offset);
    name_record_offset += NameTable::Offset::kNameRecordSize;
    string_offset += new_data->writeBytes(
        string_offset + string_table_start_offset,
        builder_entry->nameAsBytes());
  }

  return string_offset + string_table_start_offset;
}

}  // namespace sfntly

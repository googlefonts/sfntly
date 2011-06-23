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

#include "sfntly/cmap_table.h"
#include "sfntly/name_table.h"
#include "sfntly/font.h"
#include "sfntly/port/endian.h"

namespace sfntly {

/******************************************************************************
 * Constants
 ******************************************************************************/
const int32_t CMapTable::NOTDEF = 0;
const int32_t CMapTable::Offset::kVersion = 0;
const int32_t CMapTable::Offset::kNumTables = 2;
const int32_t CMapTable::Offset::kEncodingRecordStart = 4;
const int32_t CMapTable::Offset::kEncodingRecordPlatformId = 0;
const int32_t CMapTable::Offset::kEncodingRecordEncodingId = 2;
const int32_t CMapTable::Offset::kEncodingRecordOffset = 4;
const int32_t CMapTable::Offset::kEncodingRecordSize = 8;
const int32_t CMapTable::Offset::kFormat = 0;
const int32_t CMapTable::Offset::kFormat0Format = 0;
const int32_t CMapTable::Offset::kFormat0Length = 2;
const int32_t CMapTable::Offset::kFormat0Language = 4;
const int32_t CMapTable::Offset::kFormat0GlyphIdArray = 6;
const int32_t CMapTable::Offset::kFormat2Format = 0;
const int32_t CMapTable::Offset::kFormat2Length = 2;
const int32_t CMapTable::Offset::kFormat2Language = 4;
const int32_t CMapTable::Offset::kFormat2SubHeaderKeys = 6;
const int32_t CMapTable::Offset::kFormat2SubHeaders = 518;
const int32_t CMapTable::Offset::kFormat2SubHeader_firstCode = 0;
const int32_t CMapTable::Offset::kFormat2SubHeader_entryCount = 2;
const int32_t CMapTable::Offset::kFormat2SubHeader_idDelta = 4;
const int32_t CMapTable::Offset::kFormat2SubHeader_idRangeOffset = 6;
const int32_t CMapTable::Offset::kFormat2SubHeader_structLength = 8;
const int32_t CMapTable::Offset::kFormat4Format = 0;
const int32_t CMapTable::Offset::kFormat4Length = 2;
const int32_t CMapTable::Offset::kFormat4Language = 4;
const int32_t CMapTable::Offset::kFormat4SegCountX2 = 6;
const int32_t CMapTable::Offset::kFormat4SearchRange = 8;
const int32_t CMapTable::Offset::kFormat4EntrySelector = 10;
const int32_t CMapTable::Offset::kFormat4RangeShift = 12;
const int32_t CMapTable::Offset::kFormat4EndCount = 14;
const int32_t CMapTable::Offset::kFormat6Format = 0;
const int32_t CMapTable::Offset::kFormat6Length = 2;
const int32_t CMapTable::Offset::kFormat6Language = 4;
const int32_t CMapTable::Offset::kFormat6FirstCode = 6;
const int32_t CMapTable::Offset::kFormat6EntryCount = 8;
const int32_t CMapTable::Offset::kFormat6GlyphIdArray = 10;
const int32_t CMapTable::Offset::kFormat8Format = 0;
const int32_t CMapTable::Offset::kFormat8Length = 4;
const int32_t CMapTable::Offset::kFormat8Language = 8;
const int32_t CMapTable::Offset::kFormat8Is32 = 12;
const int32_t CMapTable::Offset::kFormat8nGroups204 = 8204;
const int32_t CMapTable::Offset::kFormat8Groups208 = 8208;
const int32_t CMapTable::Offset::kFormat8Group_startCharCode = 0;
const int32_t CMapTable::Offset::kFormat8Group_endCharCode = 4;
const int32_t CMapTable::Offset::kFormat8Group_startGlyphId = 8;
const int32_t CMapTable::Offset::kFormat8Group_structLength = 12;
const int32_t CMapTable::Offset::kFormat10Format = 0;
const int32_t CMapTable::Offset::kFormat10Length = 4;
const int32_t CMapTable::Offset::kFormat10Language = 8;
const int32_t CMapTable::Offset::kFormat10StartCharCode = 12;
const int32_t CMapTable::Offset::kFormat10NumChars = 16;
const int32_t CMapTable::Offset::kFormat10Glyphs0 = 20;
const int32_t CMapTable::Offset::kFormat12Format = 0;
const int32_t CMapTable::Offset::kFormat12Length = 4;
const int32_t CMapTable::Offset::kFormat12Language = 8;
const int32_t CMapTable::Offset::kFormat12nGroups = 12;
const int32_t CMapTable::Offset::kFormat12Groups = 16;
const int32_t CMapTable::Offset::kFormat12Groups_structLength = 12;
const int32_t CMapTable::Offset::kFormat12_startCharCode = 0;
const int32_t CMapTable::Offset::kFormat12_endCharCode = 4;
const int32_t CMapTable::Offset::kFormat12_startGlyphId = 8;
const int32_t CMapTable::Offset::kFormat13Format = 0;
const int32_t CMapTable::Offset::kFormat13Length = 4;
const int32_t CMapTable::Offset::kFormat13Language = 8;
const int32_t CMapTable::Offset::kFormat13nGroups = 12;
const int32_t CMapTable::Offset::kFormat13Groups = 16;
const int32_t CMapTable::Offset::kFormat13Groups_structLength = 12;
const int32_t CMapTable::Offset::kFormat13_startCharCode = 0;
const int32_t CMapTable::Offset::kFormat13_endCharCode = 4;
const int32_t CMapTable::Offset::kFormat13_glyphId = 8;
const int32_t CMapTable::Offset::kFormat14Format = 0;
const int32_t CMapTable::Offset::kFormat14Length = 2;
const int32_t CMapTable::Offset::kLast = -1;

const int32_t CMapTable::CMapFormat::kFormat0 = 0;
const int32_t CMapTable::CMapFormat::kFormat2 = 2;
const int32_t CMapTable::CMapFormat::kFormat4 = 4;
const int32_t CMapTable::CMapFormat::kFormat6 = 6;
const int32_t CMapTable::CMapFormat::kFormat8 = 8;
const int32_t CMapTable::CMapFormat::kFormat10 = 10;
const int32_t CMapTable::CMapFormat::kFormat12 = 12;
const int32_t CMapTable::CMapFormat::kFormat13 = 13;
const int32_t CMapTable::CMapFormat::kFormat14 = 14;

/******************************************************************************
 * CMapTable class
 ******************************************************************************/
CMapTable::CMapTable(Header* header, ReadableFontData* data)
    : Table(header, data) {}
CMapTable::~CMapTable() {}

int32_t CMapTable::version() {
  return data_->readUShort(Offset::kVersion);
}

int32_t CMapTable::numCMaps() {
  return data_->readUShort(Offset::kNumTables);
}

int32_t CMapTable::offsetForEncodingRecord(int32_t index) {
  return Offset::kEncodingRecordStart + index * Offset::kEncodingRecordSize;
}

CMapTable::CMapId CMapTable::cmapId(int32_t index) {
  return CMapId(platformId(index), encodingId(index));
}

int32_t CMapTable::platformId(int32_t index) {
  return data_->readUShort(Offset::kEncodingRecordPlatformId +
                           offsetForEncodingRecord(index));
}

int32_t CMapTable::encodingId(int32_t index) {
  return data_->readUShort(Offset::kEncodingRecordEncodingId +
                           offsetForEncodingRecord(index));
}

int32_t CMapTable::offset(int32_t index) {
  return data_->readULongAsInt(Offset::kEncodingRecordOffset +
                               offsetForEncodingRecord(index));
}

/******************************************************************************
 * CMapTable::CMapId class
 ******************************************************************************/
CMapTable::CMapId::CMapId(int32_t platform_id, int32_t encoding_id)
    : platform_id_(platform_id), encoding_id_(encoding_id) {
}

CMapTable::CMapId::CMapId(const CMapId& obj)
    : platform_id_(obj.platform_id_), encoding_id_(obj.encoding_id_) {
}

int32_t CMapTable::CMapId::platformId() { return platform_id_; }
int32_t CMapTable::CMapId::encodingId() { return encoding_id_; }

bool CMapTable::CMapId::operator==(const CMapTable::CMapId& obj) {
  return obj.platform_id_ == platform_id_ && obj.encoding_id_ == encoding_id_;
}

const CMapTable::CMapId& CMapTable::CMapId::operator=(
    const CMapTable::CMapId& obj) {
  platform_id_ = obj.platform_id_;
  encoding_id_ = obj.encoding_id_;
  return *this;
}

int CMapTable::CMapId::hashCode() const {
  return platform_id_ << 8 | encoding_id_;
}

CMapTable::CMapId WINDOWS_BMP(PlatformId::kWindows,
                              WindowsEncodingId::kUnicodeUCS2);
CMapTable::CMapId WINDOWS_UCS4(PlatformId::kWindows,
                               WindowsEncodingId::kUnicodeUCS4);
CMapTable::CMapId MAC_ROMAN(PlatformId::kWindows, MacintoshEncodingId::kRoman);

/******************************************************************************
 * CMapTable::CMapIdComparator class
 ******************************************************************************/

bool CMapTable::CMapIdComparator::operator()(const CMapId& lhs,
                                             const CMapId& rhs) {
  return lhs.hashCode() > rhs.hashCode();
}

/******************************************************************************
 * CMapTable::CMap class
 ******************************************************************************/
CMapTable::CMap::CMap(ReadableFontData* data, int32_t format,
                      const CMapId& cmap_id)
    : SubTable(data), format_(format), cmap_id_(cmap_id) {
}

CMapTable::CMap::~CMap() {}
int32_t CMapTable::CMap::format() { return format_; }
CMapTable::CMapId CMapTable::CMap::cmapId() { return cmap_id_; }
int32_t CMapTable::CMap::platformId() { return cmap_id_.platformId(); }
int32_t CMapTable::CMap::encodingId() { return cmap_id_.encodingId(); }

/******************************************************************************
 * CMapTable::CMap::Builder class
 ******************************************************************************/
CMapTable::CMap::Builder::Builder(FontDataTableBuilderContainer* container,
    ReadableFontData* data, int32_t format, const CMapId& cmap_id) :
    SubTable::Builder(container, data), format_(format), cmap_id_(cmap_id) {
}

CMapTable::CMap::Builder::Builder(FontDataTableBuilderContainer* container,
    WritableFontData* data, int32_t format, const CMapId& cmap_id) :
    SubTable::Builder(container, data), format_(format), cmap_id_(cmap_id) {
}

CMapTable::CMap::Builder::~Builder() {}

CMapTable::CMapId CMapTable::CMap::Builder::cmapId() {
  return cmap_id_;
}

int32_t CMapTable::CMap::Builder::platformId() {
  return cmap_id_.platformId();
}

int32_t CMapTable::CMap::Builder::encodingId() {
  return cmap_id_.encodingId();
}

int32_t CMapTable::CMap::Builder::subSerialize(WritableFontData* new_data) {
  return internalReadData()->copyTo(new_data);
}

bool CMapTable::CMap::Builder::subReadyToSerialize() {
  return true;
}

int32_t CMapTable::CMap::Builder::subDataSizeToSerialize() {
  return internalReadData()->length();
}

void CMapTable::CMap::Builder::subDataSet() {
  // NOP
}

CALLER_ATTACH CMapTable::CMap::Builder* CMapTable::CMap::Builder::getBuilder(
    FontDataTableBuilderContainer* container, ReadableFontData* data,
    int32_t offset, const CMapId& cmap_id) {
  // NOT IMPLEMENTED: Java enum value validation
  int32_t format = data->readUShort(offset);
  CMapBuilderPtr builder;
  switch (format) {
  case CMapFormat::kFormat0:
    builder = new CMapFormat0::Builder(container, data, offset, cmap_id);
  case CMapFormat::kFormat2:
    builder = new CMapFormat0::Builder(container, data, offset, cmap_id);
  default:
    break;
  }
  return builder.detach();
}

/******************************************************************************
 * CMapTable::CMapFormat0 and CMapTable::CMapFormat0::Builder
 ******************************************************************************/
CMapTable::CMapFormat0::CMapFormat0(ReadableFontData* data,
    const CMapId& cmap_id) : CMap(data, CMapFormat::kFormat0, cmap_id) {
}

CMapTable::CMapFormat0::~CMapFormat0() {}

int32_t CMapTable::CMapFormat0::glyphId(int32_t character) {
  if (character < 0 || character > 255) {
    return CMapTable::NOTDEF;
  }
  return data_->readByte(character + Offset::kFormat0GlyphIdArray);
}

int32_t CMapTable::CMapFormat0::language() {
  return 0;
}

CMapTable::CMapFormat0::Builder::Builder(
    FontDataTableBuilderContainer* container, WritableFontData* data,
    int32_t offset, const CMapId& cmap_id)
    : CMapTable::CMap::Builder(container,
                               data ? down_cast<WritableFontData*>(
                                          data->slice(offset, data->readUShort(
                                              offset + Offset::kFormat0Length)))
                                    : reinterpret_cast<WritableFontData*>(NULL),
                               CMapFormat::kFormat0, cmap_id) {
  // TODO(arthurhsu): FIXIT: heavy lifting and leak, need fix.
}

CMapTable::CMapFormat0::Builder::Builder(
    FontDataTableBuilderContainer* container, ReadableFontData* data,
    int32_t offset, const CMapId& cmap_id)
    : CMapTable::CMap::Builder(container,
                               data ? down_cast<ReadableFontData*>(
                                          data->slice(offset, data->readUShort(
                                              offset + Offset::kFormat0Length)))
                                    : reinterpret_cast<WritableFontData*>(NULL),
                               CMapFormat::kFormat0, cmap_id) {
  // TODO(arthurhsu): FIXIT: heavy lifting and leak, need fix.
}

CMapTable::CMapFormat0::Builder::~Builder() {}

CALLER_ATTACH FontDataTable* CMapTable::CMapFormat0::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table = new CMapFormat0(data, cmapId());
  return table.detach();
}

/******************************************************************************
 * CMapTable::CMapFormat2 and CMapTable::CMapFormat2::Builder
 ******************************************************************************/
CMapTable::CMapFormat2::CMapFormat2(ReadableFontData* data,
    const CMapId& cmap_id) : CMap(data, CMapFormat::kFormat2, cmap_id) {
}

CMapTable::CMapFormat2::~CMapFormat2() {}

int32_t CMapTable::CMapFormat2::subHeaderOffset(int32_t sub_header_index) {
  return data_->readUShort(Offset::kFormat2SubHeaderKeys + sub_header_index *
                           DataSize::kUSHORT);
}

int32_t CMapTable::CMapFormat2::firstCode(int32_t sub_header_index) {
  int32_t sub_header_offset = subHeaderOffset(sub_header_index);
  return data_->readUShort(sub_header_offset + Offset::kFormat2SubHeaderKeys +
                           Offset::kFormat2SubHeader_firstCode);
}

int32_t CMapTable::CMapFormat2::entryCount(int32_t sub_header_index) {
  int32_t sub_header_offset = subHeaderOffset(sub_header_index);
  return data_->readUShort(sub_header_offset + Offset::kFormat2SubHeaderKeys +
                           Offset::kFormat2SubHeader_entryCount);
}

int32_t CMapTable::CMapFormat2::idRangeOffset(int32_t sub_header_index) {
  int32_t sub_header_offset = subHeaderOffset(sub_header_index);
  return data_->readUShort(sub_header_offset + Offset::kFormat2SubHeaderKeys +
                           Offset::kFormat2SubHeader_idRangeOffset);
}

int32_t CMapTable::CMapFormat2::idDelta(int32_t sub_header_index) {
  int32_t sub_header_offset = subHeaderOffset(sub_header_index);
  return data_->readUShort(sub_header_offset + Offset::kFormat2SubHeaderKeys +
                           Offset::kFormat2SubHeader_idDelta);
}

int32_t CMapTable::CMapFormat2::bytesConsumed(int32_t character) {
  uint32_t c = toBE32(character);
  int32_t high_byte = (c >> 8) & 0xff;
  int32_t offset = subHeaderOffset(high_byte);
  return (offset == 0) ? 1 : 2;
}

int32_t CMapTable::CMapFormat2::glyphId(int32_t character) {
  if (character > 0xffff) {
    return CMapTable::NOTDEF;
  }

  uint32_t c = toBE32(character);
  byte_t high_byte = (c >> 8) & 0xff;
  byte_t low_byte = c & 0xff;
  int32_t offset = subHeaderOffset(high_byte);

  if (offset == 0) {
    low_byte = high_byte;
    high_byte = 0;
  }

  int32_t first_code = firstCode(high_byte);
  int32_t entry_count = entryCount(high_byte);

  if (low_byte < first_code || low_byte >= first_code + entry_count) {
    return CMapTable::NOTDEF;
  }

  int32_t id_range_offset = idRangeOffset(high_byte);

  // position of idRangeOffset + value of idRangeOffset + index for low byte
  // = firstcode
  int32_t p_location = (offset + Offset::kFormat2SubHeader_idRangeOffset) +
                       id_range_offset +
                       (low_byte - first_code) * DataSize::kUSHORT;
  int p = data_->readUShort(p_location);
  if (p == 0) {
    return CMapTable::NOTDEF;
  }

  if (offset == 0) {
    return p;
  }
  int id_delta = idDelta(high_byte);
  return (p + id_delta) % 65536;
}

int32_t CMapTable::CMapFormat2::language() {
  return 0;
}

CMapTable::CMapFormat2::Builder::Builder(
    FontDataTableBuilderContainer* container, WritableFontData* data,
    int32_t offset, const CMapId& cmap_id)
    : CMapTable::CMap::Builder(container,
                               data ? down_cast<WritableFontData*>(
                                          data->slice(offset, data->readUShort(
                                              offset + Offset::kFormat0Length)))
                                    : reinterpret_cast<WritableFontData*>(NULL),
                               CMapFormat::kFormat2, cmap_id) {
  // TODO(arthurhsu): FIXIT: heavy lifting and leak, need fix.
}

CMapTable::CMapFormat2::Builder::Builder(
    FontDataTableBuilderContainer* container, ReadableFontData* data,
    int32_t offset, const CMapId& cmap_id)
    : CMapTable::CMap::Builder(container,
                               data ? down_cast<ReadableFontData*>(
                                          data->slice(offset, data->readUShort(
                                              offset + Offset::kFormat0Length)))
                                    : reinterpret_cast<ReadableFontData*>(NULL),
                               CMapFormat::kFormat2, cmap_id) {
  // TODO(arthurhsu): FIXIT: heavy lifting and leak, need fix.
}

CMapTable::CMapFormat2::Builder::~Builder() {}

CALLER_ATTACH FontDataTable* CMapTable::CMapFormat2::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table = new CMapFormat2(data, cmapId());
  return table.detach();
}

/******************************************************************************
 * CMapTable::Iterator class
 ******************************************************************************/
CMapTable::CMapIterator::CMapIterator(CMapTable* table, CMapFilter* filter)
    : table_index_(0), table_(table), filter_(filter) {}

bool CMapTable::CMapIterator::hasNext() {
  if (!filter_) {
    if (table_index_ < table_->numCMaps()) {
      return true;
    }
    return false;
  }

  for (; table_index_ < table_->numCMaps(); ++table_index_) {
    if (filter_->accept(table_->cmapId(table_index_))) {
      return true;
    }
  }
  return false;
}

/******************************************************************************
 * CMapTable::Builder class
 ******************************************************************************/
CMapTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header, WritableFontData* data)
    : Table::ArrayElementTableBuilder(font_builder, header, data) {
}

CMapTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header, ReadableFontData* data)
    : Table::ArrayElementTableBuilder(font_builder, header, data) {
}

int32_t CMapTable::Builder::subSerialize(WritableFontData* new_data) {
  // TODO(arthurhsu): IMPLEMENT
  UNREFERENCED_PARAMETER(new_data);
  return 0;
}

bool CMapTable::Builder::subReadyToSerialize() {
  // TODO(arthurhsu): IMPLEMENT
  return false;
}

int32_t CMapTable::Builder::subDataSizeToSerialize() {
  // TODO(arthurhsu): IMPLEMENT
  return 0;
}

void CMapTable::Builder::subDataSet() {
  // TODO(arthurhsu): IMPLEMENT
}

CALLER_ATTACH FontDataTable* CMapTable::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table = new CMapTable(header(), data);
  return table.detach();
}

CALLER_ATTACH CMapTable::CMap::Builder* CMapTable::Builder::cmapBuilder(
    FontDataTableBuilderContainer* container, ReadableFontData* data,
    int32_t index) {
  if (index < 0 || index > numCMaps(data))
    return NULL;

  int32_t record_offset = Offset::kEncodingRecordOffset + index *
                          Offset::kEncodingRecordSize;
  int32_t platform_id = data->readUShort(Offset::kEncodingRecordPlatformId +
                                         record_offset);
  int32_t encoding_id = data->readUShort(Offset::kEncodingRecordEncodingId +
                                         record_offset);
  CMapId cmap_id(platform_id, encoding_id);
  int32_t offset = data->readULongAsInt(Offset::kEncodingRecordOffset +
                                        record_offset);
  return CMap::Builder::getBuilder(container, data, offset, cmap_id);
}

int32_t CMapTable::Builder::numCMaps(ReadableFontData* data) {
  if (data == NULL) {
    return 0;
  }
  return data->readUShort(Offset::kNumTables);
}

}  // namespace sfntly

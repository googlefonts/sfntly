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

#include <cstdlib>

#include "sfntly/table/core/cmap_table.h"
#include "sfntly/table/core/name_table.h"
#include "sfntly/font.h"
#include "sfntly/port/endian.h"

namespace sfntly {

const int32_t CMapTable::NOTDEF = 0;

/******************************************************************************
 * CMapTable class
 ******************************************************************************/
CMapTable::~CMapTable() {}

int32_t CMapTable::Version() {
  return data_->ReadUShort(Offset::kVersion);
}

int32_t CMapTable::NumCMaps() {
  return data_->ReadUShort(Offset::kNumTables);
}

CMapTable::CMapId CMapTable::GetCMapId(int32_t index) {
  return CMapId(PlatformId(index), EncodingId(index));
}

int32_t CMapTable::PlatformId(int32_t index) {
  return data_->ReadUShort(Offset::kEncodingRecordPlatformId +
                           OffsetForEncodingRecord(index));
}

int32_t CMapTable::EncodingId(int32_t index) {
  return data_->ReadUShort(Offset::kEncodingRecordEncodingId +
                           OffsetForEncodingRecord(index));
}

int32_t CMapTable::Offset(int32_t index) {
  return data_->ReadULongAsInt(Offset::kEncodingRecordOffset +
                               OffsetForEncodingRecord(index));
}

CMapTable::CMapTable(Header* header, ReadableFontData* data)
    : Table(header, data) {
}

int32_t CMapTable::OffsetForEncodingRecord(int32_t index) {
  return Offset::kEncodingRecordStart + index * Offset::kEncodingRecordSize;
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

bool CMapTable::CMapId::operator==(const CMapTable::CMapId& obj) {
  return obj.platform_id_ == platform_id_ && obj.encoding_id_ == encoding_id_;
}

const CMapTable::CMapId& CMapTable::CMapId::operator=(
    const CMapTable::CMapId& obj) {
  platform_id_ = obj.platform_id_;
  encoding_id_ = obj.encoding_id_;
  return *this;
}

int CMapTable::CMapId::HashCode() const {
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
  return lhs.HashCode() > rhs.HashCode();
}

/******************************************************************************
 * CMapTable::CMap class
 ******************************************************************************/
CMapTable::CMap::CMap(ReadableFontData* data, int32_t format,
                      const CMapId& cmap_id)
    : SubTable(data), format_(format), cmap_id_(cmap_id) {
}

CMapTable::CMap::~CMap() {
}

/******************************************************************************
 * CMapTable::CMap::Builder class
 ******************************************************************************/
CMapTable::CMap::Builder::~Builder() {
}

CALLER_ATTACH CMapTable::CMap::Builder*
    CMapTable::CMap::Builder::GetBuilder(
        FontDataTableBuilderContainer* container,
        ReadableFontData* data,
        int32_t offset,
        const CMapId& cmap_id) {
  // NOT IMPLEMENTED: Java enum value validation
  int32_t format = data->ReadUShort(offset);
  CMapBuilderPtr builder;
  switch (format) {
    case CMapFormat::kFormat0:
      builder = new CMapFormat0::Builder(container, data, offset, cmap_id);
      break;
    case CMapFormat::kFormat2:
      builder = new CMapFormat0::Builder(container, data, offset, cmap_id);
      break;
    default:
      break;
  }
  return builder.Detach();
}

CMapTable::CMap::Builder::Builder(FontDataTableBuilderContainer* container,
                                  ReadableFontData* data, int32_t format,
                                  const CMapId& cmap_id)
    : SubTable::Builder(container, data),
      format_(format),
      cmap_id_(cmap_id) {
}

CMapTable::CMap::Builder::Builder(FontDataTableBuilderContainer* container,
                                  WritableFontData* data, int32_t format,
                                  const CMapId& cmap_id)
    : SubTable::Builder(container, data),
      format_(format),
      cmap_id_(cmap_id) {
}

int32_t CMapTable::CMap::Builder::SubSerialize(WritableFontData* new_data) {
  return InternalReadData()->CopyTo(new_data);
}

bool CMapTable::CMap::Builder::SubReadyToSerialize() {
  return true;
}

int32_t CMapTable::CMap::Builder::SubDataSizeToSerialize() {
  return InternalReadData()->Length();
}

void CMapTable::CMap::Builder::SubDataSet() {
  // NOP
}

/******************************************************************************
 * CMapTable::CMapFormat0
 ******************************************************************************/
CMapTable::CMapFormat0::~CMapFormat0() {
}

int32_t CMapTable::CMapFormat0::Language() {
  return 0;
}

int32_t CMapTable::CMapFormat0::GlyphId(int32_t character) {
  if (character < 0 || character > 255) {
    return CMapTable::NOTDEF;
  }
  return data_->ReadByte(character + Offset::kFormat0GlyphIdArray);
}

CMapTable::CMapFormat0::CMapFormat0(ReadableFontData* data,
                                    const CMapId& cmap_id)
    : CMap(data, CMapFormat::kFormat0, cmap_id) {
}

/******************************************************************************
 * CMapTable::CMapFormat0::Builder
 ******************************************************************************/
CMapTable::CMapFormat0::Builder::Builder(
    FontDataTableBuilderContainer* container,
    WritableFontData* data,
    int32_t offset,
    const CMapId& cmap_id)
    : CMapTable::CMap::Builder(container,
                               data ? down_cast<WritableFontData*>(
                                   data->Slice(offset, data->ReadUShort(
                                       offset + Offset::kFormat0Length)))
                               : reinterpret_cast<WritableFontData*>(NULL),
                               CMapFormat::kFormat0, cmap_id) {
  // TODO(arthurhsu): FIXIT: heavy lifting and leak, need fix.
}

CMapTable::CMapFormat0::Builder::Builder(
    FontDataTableBuilderContainer* container,
    ReadableFontData* data,
    int32_t offset,
    const CMapId& cmap_id)
    : CMapTable::CMap::Builder(container,
                               data ? down_cast<ReadableFontData*>(
                                   data->Slice(offset, data->ReadUShort(
                                       offset + Offset::kFormat0Length)))
                               : reinterpret_cast<WritableFontData*>(NULL),
                               CMapFormat::kFormat0, cmap_id) {
  // TODO(arthurhsu): FIXIT: heavy lifting and leak, need fix.
}

CMapTable::CMapFormat0::Builder::~Builder() {
}

CALLER_ATTACH FontDataTable*
    CMapTable::CMapFormat0::Builder::SubBuildTable(ReadableFontData* data) {
  FontDataTablePtr table = new CMapFormat0(data, cmap_id());
  return table.Detach();
}

/******************************************************************************
 * CMapTable::CMapFormat2
 ******************************************************************************/
CMapTable::CMapFormat2::~CMapFormat2() {
}

int32_t CMapTable::CMapFormat2::Language() {
  return 0;
}

int32_t CMapTable::CMapFormat2::GlyphId(int32_t character) {
  if (character > 0xffff) {
    return CMapTable::NOTDEF;
  }

  uint32_t c = ToBE32(character);
  byte_t high_byte = (c >> 8) & 0xff;
  byte_t low_byte = c & 0xff;
  int32_t offset = SubHeaderOffset(high_byte);

  if (offset == 0) {
    low_byte = high_byte;
    high_byte = 0;
  }

  int32_t first_code = FirstCode(high_byte);
  int32_t entry_count = EntryCount(high_byte);

  if (low_byte < first_code || low_byte >= first_code + entry_count) {
    return CMapTable::NOTDEF;
  }

  int32_t id_range_offset = IdRangeOffset(high_byte);

  // position of idRangeOffset + value of idRangeOffset + index for low byte
  // = firstcode
  int32_t p_location = (offset + Offset::kFormat2SubHeader_idRangeOffset) +
      id_range_offset +
      (low_byte - first_code) * DataSize::kUSHORT;
  int p = data_->ReadUShort(p_location);
  if (p == 0) {
    return CMapTable::NOTDEF;
  }

  if (offset == 0) {
    return p;
  }
  int id_delta = IdDelta(high_byte);
  return (p + id_delta) % 65536;
}

int32_t CMapTable::CMapFormat2::BytesConsumed(int32_t character) {
  uint32_t c = ToBE32(character);
  int32_t high_byte = (c >> 8) & 0xff;
  int32_t offset = SubHeaderOffset(high_byte);
  return (offset == 0) ? 1 : 2;
}

CMapTable::CMapFormat2::CMapFormat2(ReadableFontData* data,
                                    const CMapId& cmap_id)
    : CMap(data, CMapFormat::kFormat2, cmap_id) {
}

int32_t CMapTable::CMapFormat2::SubHeaderOffset(int32_t sub_header_index) {
  return data_->ReadUShort(Offset::kFormat2SubHeaderKeys +
                           sub_header_index * DataSize::kUSHORT);
}

int32_t CMapTable::CMapFormat2::FirstCode(int32_t sub_header_index) {
  int32_t sub_header_offset = SubHeaderOffset(sub_header_index);
  return data_->ReadUShort(sub_header_offset +
                           Offset::kFormat2SubHeaderKeys +
                           Offset::kFormat2SubHeader_firstCode);
}

int32_t CMapTable::CMapFormat2::EntryCount(int32_t sub_header_index) {
  int32_t sub_header_offset = SubHeaderOffset(sub_header_index);
  return data_->ReadUShort(sub_header_offset +
                           Offset::kFormat2SubHeaderKeys +
                           Offset::kFormat2SubHeader_entryCount);
}

int32_t CMapTable::CMapFormat2::IdRangeOffset(int32_t sub_header_index) {
  int32_t sub_header_offset = SubHeaderOffset(sub_header_index);
  return data_->ReadUShort(sub_header_offset +
                           Offset::kFormat2SubHeaderKeys +
                           Offset::kFormat2SubHeader_idRangeOffset);
}

int32_t CMapTable::CMapFormat2::IdDelta(int32_t sub_header_index) {
  int32_t sub_header_offset = SubHeaderOffset(sub_header_index);
  return data_->ReadUShort(sub_header_offset +
                           Offset::kFormat2SubHeaderKeys +
                           Offset::kFormat2SubHeader_idDelta);
}

/******************************************************************************
 * CMapTable::CMapFormat2::Builder
 ******************************************************************************/
CMapTable::CMapFormat2::Builder::Builder(
    FontDataTableBuilderContainer* container,
    WritableFontData* data,
    int32_t offset,
    const CMapId& cmap_id)
    : CMapTable::CMap::Builder(container,
                               data ? down_cast<WritableFontData*>(
                                   data->Slice(offset, data->ReadUShort(
                                       offset + Offset::kFormat0Length)))
                               : reinterpret_cast<WritableFontData*>(NULL),
                               CMapFormat::kFormat2, cmap_id) {
  // TODO(arthurhsu): FIXIT: heavy lifting and leak, need fix.
}

CMapTable::CMapFormat2::Builder::Builder(
    FontDataTableBuilderContainer* container,
    ReadableFontData* data,
    int32_t offset,
    const CMapId& cmap_id)
    : CMapTable::CMap::Builder(container,
                               data ? down_cast<ReadableFontData*>(
                                   data->Slice(offset, data->ReadUShort(
                                       offset + Offset::kFormat0Length)))
                               : reinterpret_cast<ReadableFontData*>(NULL),
                               CMapFormat::kFormat2, cmap_id) {
  // TODO(arthurhsu): FIXIT: heavy lifting and leak, need fix.
}

CMapTable::CMapFormat2::Builder::~Builder() {
}

CALLER_ATTACH FontDataTable*
    CMapTable::CMapFormat2::Builder::SubBuildTable(ReadableFontData* data) {
  FontDataTablePtr table = new CMapFormat2(data, cmap_id());
  return table.Detach();
}

/******************************************************************************
 * CMapTable::Iterator class
 ******************************************************************************/
CMapTable::CMapIterator::CMapIterator(CMapTable* table, CMapFilter* filter)
    : table_index_(0), filter_(filter), table_(table) {}

bool CMapTable::CMapIterator::HasNext() {
  if (!filter_) {
    if (table_index_ < table_->NumCMaps()) {
      return true;
    }
    return false;
  }

  for (; table_index_ < table_->NumCMaps(); ++table_index_) {
    if (filter_->accept(table_->GetCMapId(table_index_))) {
      return true;
    }
  }
  return false;
}

/******************************************************************************
 * CMapTable::Builder class
 ******************************************************************************/
CMapTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header,
                            WritableFontData* data)
    : Table::ArrayElementTableBuilder(font_builder, header, data) {
}

CMapTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header,
                            ReadableFontData* data)
    : Table::ArrayElementTableBuilder(font_builder, header, data) {
}

CMapTable::Builder::~Builder() {
}

int32_t CMapTable::Builder::SubSerialize(WritableFontData* new_data) {
  int32_t size = new_data->WriteUShort(CMapTable::Offset::kVersion,
                                       version_);
  size += new_data->WriteUShort(CMapTable::Offset::kNumTables,
                                cmap_builders_.size());

  int32_t index_offset = size;
  size += cmap_builders_.size() * CMapTable::Offset::kEncodingRecordSize;
  for (CMapBuilderMap::iterator it = cmap_builders_.begin(),
                                e = cmap_builders_.end(); it != e; ++it) {
    CMapBuilderPtr b = it->second;
    // header entry
    index_offset += new_data->WriteUShort(index_offset, b->platform_id());
    index_offset += new_data->WriteUShort(index_offset, b->encoding_id());
    index_offset += new_data->WriteULong(index_offset, size);

    // cmap
    FontDataPtr slice;
    slice.Attach(new_data->Slice(size));
    size += b->SubSerialize(down_cast<WritableFontData*>(slice.p_));
  }
  return size;
}

bool CMapTable::Builder::SubReadyToSerialize() {
  if (cmap_builders_.empty())
    return false;

  // check each table
  for (CMapBuilderMap::iterator it = cmap_builders_.begin(),
                                e = cmap_builders_.end(); it != e; ++it) {
    if (!it->second->SubReadyToSerialize())
      return false;
  }
  return true;
}

int32_t CMapTable::Builder::SubDataSizeToSerialize() {
  if (cmap_builders_.empty())
    return 0;

  bool variable = false;
  int32_t size = CMapTable::Offset::kEncodingRecordStart +
                 cmap_builders_.size() * CMapTable::Offset::kEncodingRecordSize;

  // calculate size of each table
  for (CMapBuilderMap::iterator it = cmap_builders_.begin(),
                                e = cmap_builders_.end(); it != e; ++it) {
    int32_t cmap_size = it->second->SubDataSizeToSerialize();
    size += abs(cmap_size);
    variable |= cmap_size <= 0;
  }
  return variable ? -size : size;
}

void CMapTable::Builder::SubDataSet() {
  cmap_builders_.clear();
  Table::Builder::set_model_changed(false);
}

CALLER_ATTACH FontDataTable*
    CMapTable::Builder::SubBuildTable(ReadableFontData* data) {
  FontDataTablePtr table = new CMapTable(header(), data);
  return table.Detach();
}

CALLER_ATTACH CMapTable::CMap::Builder* CMapTable::Builder::CMapBuilder(
    FontDataTableBuilderContainer* container, ReadableFontData* data,
    int32_t index) {
  if (index < 0 || index > NumCMaps(data))
    return NULL;

  int32_t record_offset = Offset::kEncodingRecordOffset + index *
      Offset::kEncodingRecordSize;
  int32_t platform_id =
      data->ReadUShort(Offset::kEncodingRecordPlatformId + record_offset);
  int32_t encoding_id =
      data->ReadUShort(Offset::kEncodingRecordEncodingId + record_offset);
  CMapId cmap_id(platform_id, encoding_id);
  int32_t offset =
      data->ReadULongAsInt(Offset::kEncodingRecordOffset + record_offset);
  return CMap::Builder::GetBuilder(container, data, offset, cmap_id);
}

int32_t CMapTable::Builder::NumCMaps(ReadableFontData* data) {
  if (data == NULL) {
    return 0;
  }
  return data->ReadUShort(Offset::kNumTables);
}

}  // namespace sfntly

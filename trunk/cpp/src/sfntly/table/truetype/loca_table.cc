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

#include "sfntly/table/truetype/loca_table.h"
#include "sfntly/port/exception_type.h"

namespace sfntly {
/******************************************************************************
 * LocaTable class
 ******************************************************************************/
LocaTable::~LocaTable() {}

int32_t LocaTable::NumGlyphs() {
  return num_glyphs_;
}

int32_t LocaTable::GlyphOffset(int32_t glyph_id) {
  if (glyph_id < 0 || glyph_id >= num_glyphs_) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException("Glyph ID is out of bounds.");
#endif
  }
  return Loca(glyph_id);
}

int32_t LocaTable::GlyphLength(int32_t glyph_id) {
  if (glyph_id < 0 || glyph_id >= num_glyphs_) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException("Glyph ID is out of bounds.");
#endif
  }
  return Loca(glyph_id + 1) - Loca(glyph_id);
}

int32_t LocaTable::NumLocas() {
  return num_glyphs_ + 1;
}

int32_t LocaTable::Loca(int32_t index) {
  if (index > num_glyphs_) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException();
#endif
  }
  if (version_ == IndexToLocFormat::kShortOffset) {
    return 2 * data_->ReadUShort(index * DataSize::kUSHORT);
  }
  return data_->ReadULongAsInt(index * DataSize::kULONG);
}

LocaTable::LocaTable(Header* header, ReadableFontData* data)
    : Table(header, data) {
}

LocaTable::LocaTable(Header* header,
                     ReadableFontData* data,
                     int32_t version,
                     int32_t num_glyphs)
    : Table(header, data), version_(version), num_glyphs_(num_glyphs) {
}

/******************************************************************************
 * LocaTable::Iterator class
 ******************************************************************************/
LocaTable::LocaIterator::LocaIterator(LocaTable* table)
    : index_(-1) {
  table_ = table;
}

bool LocaTable::LocaIterator::HasNext() {
  return index_ <= table_->num_glyphs_;
}

int32_t LocaTable::LocaIterator::Next() {
  return table_->Loca(index_++);
}

/******************************************************************************
 * LocaTable::Builder class
 ******************************************************************************/
LocaTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header, WritableFontData* data) :
    Table::ArrayElementTableBuilder(font_builder, header, data) {
  Init();
}

LocaTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header, ReadableFontData* data) :
    Table::ArrayElementTableBuilder(font_builder, header, data) {
  Init();
}

LocaTable::Builder::~Builder() {}

void LocaTable::Builder::SetFormatVersion(int32_t format_version) {
  format_version_ = format_version;
}

IntegerList* LocaTable::Builder::LocaList() {
  return GetLocaList();
}

void LocaTable::Builder::SetLocaList(IntegerList* list) {
  loca_.clear();
  if (list) {
    loca_ = *list;
    num_glyphs_ = loca_.size();
    set_model_changed();
  }
}

int32_t LocaTable::Builder::GlyphOffset(int32_t glyph_id) {
  if (glyph_id < 0 || glyph_id > (num_glyphs_ + 1)) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException("Glyph ID is out of bounds.");
#endif
  }
  return Loca(glyph_id);
}

int32_t LocaTable::Builder::GlyphLength(int32_t glyph_id) {
  if (glyph_id < 0 || glyph_id > (num_glyphs_ + 1)) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException("Glyph ID is out of bounds.");
#endif
  }
  return Loca(glyph_id + 1) - Loca(glyph_id);
}

void LocaTable::Builder::SetNumGlyphs(int32_t num_glyphs) {
  num_glyphs_ = num_glyphs;
}

int32_t LocaTable::Builder::NumGlyphs() {
  if (!loca_.empty()) {
    return loca_.size() - 1;
  }
  return num_glyphs_;
}

void LocaTable::Builder::Revert() {
  loca_.clear();
  set_model_changed(false);
}

void LocaTable::Builder::Clear() {
  GetLocaList()->clear();
}

int32_t LocaTable::Builder::NumLocas() {
  return GetLocaList()->size();
}

int32_t LocaTable::Builder::Loca(int32_t index) {
  return GetLocaList()->at(index);
}

CALLER_ATTACH FontDataTable* LocaTable::Builder::SubBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table =
      new LocaTable(header(), data, format_version_, num_glyphs_);
  return table.Detach();
}

void LocaTable::Builder::SubDataSet() {
  Initialize(InternalReadData());
}

int32_t LocaTable::Builder::SubDataSizeToSerialize() {
  if (loca_.empty()) {
    return 0;
  }
  if (format_version_ == IndexToLocFormat::kLongOffset) {
    return loca_.size() * DataSize::kULONG;
  }
  return loca_.size() * DataSize::kUSHORT;
}

bool LocaTable::Builder::SubReadyToSerialize() {
  return !loca_.empty();
}

int32_t LocaTable::Builder::SubSerialize(WritableFontData* new_data) {
  int32_t size = 0;
  for (IntegerList::iterator l = loca_.begin(), end = loca_.end();
                             l != end; ++l) {
    if (format_version_ == IndexToLocFormat::kLongOffset) {
      size += new_data->WriteULong(size, *l);
    } else {
      size += new_data->WriteUShort(size, *l / 2);
    }
  }
  return 0;
}

void LocaTable::Builder::Init() {
  num_glyphs_ = -1;
  format_version_ = IndexToLocFormat::kLongOffset;
}

void LocaTable::Builder::Initialize(ReadableFontData* data) {
  if (data) {
    if (NumGlyphs() < 0) {
#if defined (SFNTLY_NO_EXCEPTION)
      return;
#else
      throw IllegalStateException("numglyphs not set on LocaTable Builder.");
#endif
    }
    LocaTablePtr table =
        new LocaTable(header(), data, format_version_, num_glyphs_);
    LocaTable::LocaIterator loca_iter(table);
    while (loca_iter.HasNext()) {
      loca_.push_back(loca_iter.Next());
    }
  }
}

IntegerList* LocaTable::Builder::GetLocaList() {
  if (loca_.empty()) {
    Initialize(InternalReadData());
    set_model_changed();
  }
  return &loca_;
}

}  // namespace sfntly

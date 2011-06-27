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

#include "sfntly/loca_table.h"
#include "sfntly/port/exception_type.h"

namespace sfntly {
/******************************************************************************
 * LocaTable class
 ******************************************************************************/
LocaTable::LocaTable(Header* header, ReadableFontData* data)
    : Table(header, data) {}

LocaTable::LocaTable(Header* header, ReadableFontData* data, int32_t version,
                     int32_t num_glyphs)
    : Table(header, data), version_(version), num_glyphs_(num_glyphs) {}

LocaTable::~LocaTable() {}

int32_t LocaTable::numGlyphs() {
  return num_glyphs_;
}

int32_t LocaTable::glyphOffset(int32_t glyph_id) {
  if (glyph_id < 0 || glyph_id >= num_glyphs_) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException("Glyph ID is out of bounds.");
#endif
  }
  return loca(glyph_id);
}

int32_t LocaTable::glyphLength(int32_t glyph_id) {
  if (glyph_id < 0 || glyph_id >= num_glyphs_) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException("Glyph ID is out of bounds.");
#endif
  }
  return loca(glyph_id + 1) - loca(glyph_id);
}

int32_t LocaTable::numLocas() {
  return num_glyphs_ + 1;
}

int32_t LocaTable::loca(int32_t index) {
  if (index > num_glyphs_) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException();
#endif
  }
  if (version_ == IndexToLocFormat::kShortOffset) {
    return 2 * data_->readShort(index * DataSize::kUSHORT);
  }
  return data_->readULongAsInt(index * DataSize::kULONG);
}

/******************************************************************************
 * LocaTable::Builder class
 ******************************************************************************/
LocaTable::LocaIterator::LocaIterator(LocaTable* table) : index_(-1) {
  table_ = table;
}

bool LocaTable::LocaIterator::hasNext() {
  return index_ <= table_->num_glyphs_;
}

int32_t LocaTable::LocaIterator::next() {
  return table_->loca(index_++);
}

/******************************************************************************
 * LocaTable::Builder class
 ******************************************************************************/
void LocaTable::Builder::init() {
  num_glyphs_ = -1;
  format_version_ = IndexToLocFormat::kLongOffset;
}

LocaTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header, WritableFontData* data) :
    Table::ArrayElementTableBuilder(font_builder, header, data) {
  init();
}

LocaTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header, ReadableFontData* data) :
    Table::ArrayElementTableBuilder(font_builder, header, data) {
  init();
}

LocaTable::Builder::~Builder() {}

void LocaTable::Builder::initialize(ReadableFontData* data) {
  if (data) {
    if (numGlyphs() < 0) {
#if defined (SFNTLY_NO_EXCEPTION)
      return;
#else
      throw IllegalStateException("numglyphs not set on LocaTable Builder.");
#endif
    }
    LocaTablePtr table =
        new LocaTable(header(), data, format_version_, num_glyphs_);
    LocaTable::LocaIterator loca_iter(table);
    while (loca_iter.hasNext()) {
      loca_.push_back(loca_iter.next());
    }
  }
}

IntegerList* LocaTable::Builder::getLocaList() {
  if (loca_.empty()) {
    initialize(internalReadData());
    setModelChanged();
  }
  return &loca_;
}

void LocaTable::Builder::setFormatVersion(int32_t format_version) {
  format_version_ = format_version;
}

IntegerList* LocaTable::Builder::locaList() {
  return getLocaList();
}

void LocaTable::Builder::setLocaList(IntegerList* list) {
  loca_.clear();
  if (list) {
    loca_ = *list;
    num_glyphs_ = loca_.size();
    setModelChanged();
  }
}

int32_t LocaTable::Builder::glyphOffset(int32_t glyph_id) {
  if (glyph_id < 0 || glyph_id > (num_glyphs_ + 1)) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException("Glyph ID is out of bounds.");
#endif
  }
  return loca(glyph_id);
}

int32_t LocaTable::Builder::glyphLength(int32_t glyph_id) {
  if (glyph_id < 0 || glyph_id > (num_glyphs_ + 1)) {
#if defined (SFNTLY_NO_EXCEPTION)
    return 0;
#else
    throw IndexOutOfBoundException("Glyph ID is out of bounds.");
#endif
  }
  return loca(glyph_id + 1) - loca(glyph_id);
}

void LocaTable::Builder::setNumGlyphs(int32_t num_glyphs) {
  num_glyphs_ = num_glyphs;
}

int32_t LocaTable::Builder::numGlyphs() {
  if (!loca_.empty()) {
    return loca_.size() - 1;
  }
  return num_glyphs_;
}

void LocaTable::Builder::revert() {
  loca_.clear();
  setModelChanged(false);
}

void LocaTable::Builder::clear() {
  getLocaList()->clear();
}

int32_t LocaTable::Builder::numLocas() {
  return getLocaList()->size();
}

int32_t LocaTable::Builder::loca(int32_t index) {
  return getLocaList()->at(index);
}

CALLER_ATTACH FontDataTable* LocaTable::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table =
      new LocaTable(header(), data, format_version_, num_glyphs_);
  return table.detach();
}

void LocaTable::Builder::subDataSet() {
  initialize(internalReadData());
}

int32_t LocaTable::Builder::subDataSizeToSerialize() {
  if (loca_.empty()) {
    return 0;
  }
  if (format_version_ == IndexToLocFormat::kLongOffset) {
    return loca_.size() * DataSize::kULONG;
  }
  return loca_.size() * DataSize::kUSHORT;
}

bool LocaTable::Builder::subReadyToSerialize() {
  return !loca_.empty();
}

int32_t LocaTable::Builder::subSerialize(WritableFontData* new_data) {
  int32_t size = 0;
  for (IntegerList::iterator l = loca_.begin(), end = loca_.end();
                             l != end; ++l) {
    if (format_version_ == IndexToLocFormat::kLongOffset) {
      size += new_data->writeULong(size, *l);
    } else {
      size += new_data->writeUShort(size, *l / 2);
    }
  }
  return 0;
}

}  // namespace sfntly

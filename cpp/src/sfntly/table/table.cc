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

#include "sfntly/table/table.h"

#include "sfntly/font.h"
#include "sfntly/tag.h"
#include "sfntly/table/core/cmap_table.h"
#include "sfntly/table/core/font_header_table.h"
#include "sfntly/table/core/horizontal_header_table.h"
#include "sfntly/table/core/horizontal_metrics_table.h"
#include "sfntly/table/core/maximum_profile_table.h"
#include "sfntly/table/core/name_table.h"
#include "sfntly/table/core/os2_table.h"
#include "sfntly/table/truetype/glyph_table.h"
#include "sfntly/table/truetype/loca_table.h"

namespace sfntly {

/******************************************************************************
 * Table class
 ******************************************************************************/
Table::~Table() {}

int64_t Table::CalculatedChecksum() {
  return data_->Checksum();
}

void Table::SetFont(Font* font) {
  font_ = font;
}

Table::Table(Header* header, ReadableFontData* data)
    : FontDataTable(data) {
  header_ = header;
}

/******************************************************************************
 * Table::Header class
 ******************************************************************************/
Table::Header::Header(int32_t tag)
    : tag_(tag),
      offset_(0),
      offset_valid_(false),
      length_(0),
      length_valid_(false),
      checksum_(0),
      checksum_valid_(false) {
}

Table::Header::Header(int32_t tag, int32_t length)
    : tag_(tag),
      offset_(0),
      offset_valid_(false),
      length_(length),
      length_valid_(true),
      checksum_(0),
      checksum_valid_(false) {
}

Table::Header::Header(int32_t tag,
                      int64_t checksum,
                      int32_t offset,
                      int32_t length)
    : tag_(tag),
      offset_(offset),
      offset_valid_(true),
      length_(length),
      length_valid_(true),
      checksum_(checksum),
      checksum_valid_(true) {
}

Table::Header::~Header() {}

bool HeaderComparatorByOffset::operator() (const TableHeaderPtr lhs,
                                           const TableHeaderPtr rhs) {
  return lhs->offset_ > rhs->offset_;
}

bool HeaderComparatorByTag::operator() (const TableHeaderPtr lhs,
                                        const TableHeaderPtr rhs) {
  return lhs->tag_ > rhs->tag_;
}

/******************************************************************************
 * Table::Builder class
 ******************************************************************************/
Table::Builder::~Builder() {
  header_.Release();
}

void Table::Builder::NotifyPostTableBuild(FontDataTable* table) {
  if (model_changed() || data_changed()) {
    Table* derived_table = down_cast<Table*>(table);
    derived_table->header_ = new Header(header()->tag(),
                                        derived_table->DataLength());
  }
}

CALLER_ATTACH
Table::Builder* Table::Builder::GetBuilder(Header* header,
                                           WritableFontData* table_data) {
  int32_t tag = header->tag();
  Table::Builder* builder_raw = NULL;

  // Note: Tables are commented out when they are not used/ported.
  // TODO(arthurhsu): IMPLEMENT: finish tables that are not ported.
  /*if (tag == Tag::cmap) {
    builder_raw = static_cast<Table::Builder*>(
        CMapTable::CreateBuilder(font_builder, header, table_data));
  } else*/ if (tag == Tag::head) {
    builder_raw = static_cast<Table::Builder*>(
        FontHeaderTable::Builder::CreateBuilder(header, table_data));
  } else if (tag == Tag::hhea) {
    builder_raw = static_cast<Table::Builder*>(
        HorizontalHeaderTable::Builder::CreateBuilder(header, table_data));
  } else if (tag == Tag::hmtx) {
    builder_raw = static_cast<Table::Builder*>(
        HorizontalMetricsTable::Builder::CreateBuilder(header, table_data));
  } else if (tag == Tag::maxp) {
    builder_raw = static_cast<Table::Builder*>(
        MaximumProfileTable::Builder::CreateBuilder(header, table_data));
  } else if (tag == Tag::name) {
    builder_raw = static_cast<Table::Builder*>(
        NameTable::Builder::CreateBuilder(header, table_data));
  } else if (tag == Tag::OS_2) {
    builder_raw = static_cast<Table::Builder*>(
        OS2Table::Builder::CreateBuilder(header, table_data));
  }/* else if (tag == Tag::PostScript) {
    builder_raw = static_cast<Table::Builder*>(
        PostScriptTable::Builder::CreateBuilder(header, table_data));
  } else if (tag == Tag::cvt) {
    builder_raw = static_cast<Table::Builder*>(
        ControlValueTable::Builder::CreateBuilder(header, table_data));
  }*/ else if (tag == Tag::glyf) {
    builder_raw = static_cast<Table::Builder*>(
        GlyphTable::Builder::CreateBuilder(header, table_data));
  } else if (tag == Tag::loca) {
    builder_raw = static_cast<Table::Builder*>(
        LocaTable::Builder::CreateBuilder(header, table_data));
  }/* else if (tag == Tag::prep) {
    builder_raw = static_cast<Table::Builder*>(
        ControlProgramTable::Builder::CreateBuilder(header, table_data));
  }*/ else if (tag == Tag::bhed) {
    builder_raw = static_cast<Table::Builder*>(
        FontHeaderTable::Builder::CreateBuilder(header, table_data));
  } else {
    builder_raw = static_cast<Table::Builder*>(
        Table::GenericTableBuilder::CreateBuilder(header, table_data));
  }

  return builder_raw;
}

Table::Builder::Builder(Header* header, WritableFontData* data)
    : FontDataTable::Builder(data) {
  header_ = header;
}

Table::Builder::Builder(Header* header, ReadableFontData* data)
    : FontDataTable::Builder(data) {
  header_ = header;
}

Table::Builder::Builder(Header* header) {
  header_ = header;
}

/******************************************************************************
 * Table::TableBasedTableBuilder class
 ******************************************************************************/
Table::TableBasedTableBuilder::~TableBasedTableBuilder() {}

int32_t Table::TableBasedTableBuilder::SubSerialize(WritableFontData* data) {
  UNREFERENCED_PARAMETER(data);
  return 0;
}

bool Table::TableBasedTableBuilder::SubReadyToSerialize() {
  return false;
}

int32_t Table::TableBasedTableBuilder::SubDataSizeToSerialize() {
  return 0;
}

void Table::TableBasedTableBuilder::SubDataSet() {
  table_ = NULL;
}

CALLER_ATTACH FontDataTable* Table::TableBasedTableBuilder::Build() {
  FontDataTablePtr table = static_cast<FontDataTable*>(GetTable());
  return table.Detach();
}

Table::TableBasedTableBuilder::TableBasedTableBuilder(Header* header,
                                                      WritableFontData* data)
    : Builder(header, data) {
}

Table::TableBasedTableBuilder::TableBasedTableBuilder(Header* header,
                                                      ReadableFontData* data)
    : Builder(header, data) {
}

Table::TableBasedTableBuilder::TableBasedTableBuilder(Header* header)
    : Builder(header) {
}

Table* Table::TableBasedTableBuilder::GetTable() {
  if (table_ == NULL) {
    table_.Attach(down_cast<Table*>(SubBuildTable(InternalReadData())));
  }
  return table_;
}

/******************************************************************************
 * Table::GenericTableBuilder class
 ******************************************************************************/
Table::GenericTableBuilder::GenericTableBuilder(Header* header,
                                                WritableFontData* data)
    : TableBasedTableBuilder(header, data) {
}

CALLER_ATTACH FontDataTable*
    Table::GenericTableBuilder::SubBuildTable(ReadableFontData* data) {
  // Note: In C++ port, we use GenericTable, the ref-counted version of Table
  UNREFERENCED_PARAMETER(data);
  FontDataTablePtr table = new GenericTable(this->header(), InternalReadData());
  return table.Detach();
}

CALLER_ATTACH Table::GenericTableBuilder*
    Table::GenericTableBuilder::CreateBuilder(Header* header,
                                              WritableFontData* data) {
  Ptr<Table::GenericTableBuilder> builder =
      new Table::GenericTableBuilder(header, data);
  return builder.Detach();
}

}  // namespace sfntly

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

#include "sfntly/table.h"

#include "sfntly/font.h"
#include "sfntly/tag.h"
#include "sfntly/cmap_table.h"
#include "sfntly/font_header_table.h"
#include "sfntly/glyph_table.h"
#include "sfntly/horizontal_header_table.h"
#include "sfntly/horizontal_metrics_table.h"
#include "sfntly/loca_table.h"
#include "sfntly/maximum_profile_table.h"
#include "sfntly/name_table.h"
#include "sfntly/os2_table.h"

namespace sfntly {

/******************************************************************************
 * Table class
 ******************************************************************************/
Table::~Table() {}

int64_t Table::CalculatedChecksum() {
  return data_->Checksum();
}

WritableFontData* Table::GetNewData(int32_t size) {
  return font_->GetNewData(size);
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
      length_(0),
      offset_valid_(false),
      checksum_(0),
      checksum_valid_(false) {
}

Table::Header::Header(int32_t tag, int32_t length)
    : tag_(tag),
      offset_(0),
      length_(length),
      offset_valid_(false),
      checksum_(0),
      checksum_valid_(false) {
}

Table::Header::Header(int32_t tag,
                      int64_t checksum,
                      int32_t offset,
                      int32_t length)
    : tag_(tag),
      offset_(offset),
      length_(length),
      offset_valid_(true),
      checksum_(checksum),
      checksum_valid_(true) {
}

Table::Header::~Header() {}

bool TableHeaderComparator::operator() (const TableHeaderPtr lhs,
                                        const TableHeaderPtr rhs) {
  return lhs->offset_ > rhs->offset_;
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
    header_ = new Header(header()->tag(),
                         derived_table->ReadFontData()->Length());
  }
}

WritableFontData* Table::Builder::GetNewData(int32_t size) {
  UNREFERENCED_PARAMETER(size);
  return InternalWriteData();
}

CALLER_ATTACH Table::Builder*
    Table::Builder::GetBuilder(FontDataTableBuilderContainer* font_builder,
                               Header* header,
                               WritableFontData* table_data) {
  int32_t tag = header->tag();
  TableBuilderPtr builder;
  Table::Builder* builder_raw = NULL;

  // Note: Tables are commented out when they are not used/ported.
  // TODO(arthurhsu): IMPLEMENT: finish tables that are not ported.
  /*if (tag == Tag::cmap) {
    builder_raw = static_cast<Table::Builder*>(
        new CMapTable::Builder(font_builder, header, table_data));
  } else*/ if (tag == Tag::head) {
    builder_raw = static_cast<Table::Builder*>(
        new FontHeaderTable::Builder(font_builder, header, table_data));
  } else if (tag == Tag::hhea) {
    builder_raw = static_cast<Table::Builder*>(
        new HorizontalHeaderTable::Builder(font_builder, header, table_data));
  } else if (tag == Tag::hmtx) {
    builder_raw = static_cast<Table::Builder*>(
        new HorizontalMetricsTable::Builder(font_builder, header, table_data));
  } else if (tag == Tag::maxp) {
    builder_raw = static_cast<Table::Builder*>(
        new MaximumProfileTable::Builder(font_builder, header, table_data));
  } else if (tag == Tag::name) {
    builder_raw = static_cast<Table::Builder*>(
        new NameTable::Builder(font_builder, header, table_data));
  } else if (tag == Tag::OS_2) {
    builder_raw = static_cast<Table::Builder*>(
        new OS2Table::Builder(font_builder, header, table_data));
  }/* else if (tag == Tag::PostScript) {
    builder_raw = static_cast<Table::Builder*>(
        new PostScriptTable::Builder(font_builder, header, table_data));
  } else if (tag == Tag::cvt) {
    builder_raw = static_cast<Table::Builder*>(
        new ControlValueTable::Builder(font_builder, header, table_data));
  }*/ else if (tag == Tag::glyf) {
    builder_raw = static_cast<Table::Builder*>(
        new GlyphTable::Builder(font_builder, header, table_data));
  } else if (tag == Tag::loca) {
    builder_raw = static_cast<Table::Builder*>(
        new LocaTable::Builder(font_builder, header, table_data));
  }/* else if (tag == Tag::prep) {
    builder_raw = static_cast<Table::Builder*>(
        new ControlProgramTable::Builder(font_builder, header, table_data));
  }*/ else if (tag == Tag::bhed) {
    builder_raw = static_cast<Table::Builder*>(
        new FontHeaderTable::Builder(font_builder, header, table_data));
  } else {
    builder_raw = static_cast<Table::Builder*>(
        new Table::GenericTableBuilder(font_builder, header, table_data));
  }

  builder = builder_raw;
  return builder.Detach();
}

Table::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                        Header* header,
                        WritableFontData* data)
    : FontDataTable::Builder(font_builder, data) {
  header_ = header;
}

Table::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                        Header* header,
                        ReadableFontData* data)
    : FontDataTable::Builder(font_builder, data) {
  header_ = header;
}

Table::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                        Header* header)
    : FontDataTable::Builder(font_builder) {
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

Table::TableBasedTableBuilder::TableBasedTableBuilder(
    FontDataTableBuilderContainer* font_builder,
    Header* header,
    WritableFontData* data)
    : Builder(font_builder, header, data) {
}

Table::TableBasedTableBuilder::TableBasedTableBuilder(
    FontDataTableBuilderContainer* font_builder,
    Header* header,
    ReadableFontData* data)
    : Builder(font_builder, header, data) {
}

Table::TableBasedTableBuilder::TableBasedTableBuilder(
    FontDataTableBuilderContainer* font_builder,
    Header* header)
    : Builder(font_builder, header) {
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
Table::GenericTableBuilder::GenericTableBuilder(
    FontDataTableBuilderContainer* font_builder,
    Header* header,
    WritableFontData* data)
    : TableBasedTableBuilder(font_builder, header, data) {
}

CALLER_ATTACH FontDataTable*
    Table::GenericTableBuilder::SubBuildTable(ReadableFontData* data) {
  // Note: In C++ port, we use GenericTable, the ref-counted version of Table
  UNREFERENCED_PARAMETER(data);
  FontDataTablePtr table = new GenericTable(this->header(), InternalReadData());
  return table.Detach();
}

/******************************************************************************
 * Table::ArrayElementTableBuilder class
 ******************************************************************************/
Table::ArrayElementTableBuilder::~ArrayElementTableBuilder() {}

Table::ArrayElementTableBuilder::ArrayElementTableBuilder(
    FontDataTableBuilderContainer* font_builder,
    Header* header,
    WritableFontData* data)
    : Builder(font_builder, header, data) {
}

Table::ArrayElementTableBuilder::ArrayElementTableBuilder(
    FontDataTableBuilderContainer* font_builder,
    Header* header,
    ReadableFontData* data)
    : Builder(font_builder, header, data) {
}

}  // namespace sfntly

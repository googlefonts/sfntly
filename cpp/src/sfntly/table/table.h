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

#ifndef SFNTLY_CPP_SRC_SFNTLY_TABLE_TABLE_H_
#define SFNTLY_CPP_SRC_SFNTLY_TABLE_TABLE_H_

#include <set>
#include <map>
#include <vector>
#include <utility>

#include "sfntly/port/type.h"
#include "sfntly/table/font_data_table.h"

namespace sfntly {
class Font;
class Table : public FontDataTable, public FontDataTableBuilderContainer {
 public:
  class Header : public RefCounted<Header> {
   public:
    explicit Header(int32_t tag);
    Header(int32_t tag, int32_t length);
    Header(int32_t tag, int64_t checksum, int32_t offset, int32_t length);
    virtual ~Header();

    int32_t tag() { return tag_; }
    int32_t offset() { return offset_; }
    int32_t length() { return length_; }
    bool offset_valid() { return offset_valid_; }
    int64_t checksum() { return checksum_; }
    bool checksum_valid() { return checksum_valid_; }

   private:
    int32_t tag_;
    int32_t offset_;
    int32_t length_;
    bool offset_valid_;
    int64_t checksum_;
    bool checksum_valid_;

    friend class TableHeaderComparator;
  };

  // Note: original version is Builder<T extends Table>
  //       C++ template is not designed that way so plain old inheritance is
  //       chosen.
  class Builder : public FontDataTable::Builder,
                  public FontDataTableBuilderContainer {
   public:
    virtual ~Builder();
    virtual Header* header() { return header_; }
    virtual void NotifyPostTableBuild(FontDataTable* table);
    virtual WritableFontData* GetNewData(int32_t size);

    static CALLER_ATTACH Builder*
        GetBuilder(FontDataTableBuilderContainer* font_builder,
                   Header* header,
                   WritableFontData* table_data);

   protected:
    // Note: original version is Font.Builder font_builder. This results in
    //       mutual inclusion happiness that Java solved for C++.  Therefore,
    //       we need to avoid that happiness when we port it to C++.
    Builder(FontDataTableBuilderContainer* font_builder,
            Header* header,
            WritableFontData* data);
    Builder(FontDataTableBuilderContainer* font_builder,
            Header* header,
            ReadableFontData* data);
    Builder(FontDataTableBuilderContainer* font_builder,
            Header* header);

   private:
    Ptr<Header> header_;
  };

  class TableBasedTableBuilder : public Builder {
   public:
    virtual ~TableBasedTableBuilder();

    virtual int32_t SubSerialize(WritableFontData* new_data);
    virtual bool SubReadyToSerialize();
    virtual int32_t SubDataSizeToSerialize();
    virtual void SubDataSet();

   protected:
    TableBasedTableBuilder(FontDataTableBuilderContainer* font_builder,
                           Header* header,
                           WritableFontData* data);
    TableBasedTableBuilder(FontDataTableBuilderContainer* font_builder,
                           Header* header,
                           ReadableFontData* data);
    TableBasedTableBuilder(FontDataTableBuilderContainer* font_builder,
                           Header* header);

    // C++ port: renamed table() to GetTable()
    virtual Table* GetTable();

    // TODO(arthurhsu): style guide violation: protected member, need refactor
    Ptr<Table> table_;
  };

  class GenericTableBuilder : public TableBasedTableBuilder,
                              public RefCounted<GenericTableBuilder> {
   public:
    GenericTableBuilder(FontDataTableBuilderContainer* font_builder,
                        Header* header,
                        WritableFontData* data);
    virtual CALLER_ATTACH FontDataTable* SubBuildTable(ReadableFontData* data);
  };

  class ArrayElementTableBuilder : public Builder {
   public:
    virtual ~ArrayElementTableBuilder();

   protected:
    ArrayElementTableBuilder(FontDataTableBuilderContainer* font_builder,
                             Header* header,
                             WritableFontData* data);
    ArrayElementTableBuilder(FontDataTableBuilderContainer* font_builder,
                             Header* header,
                             ReadableFontData* data);
  };

  virtual ~Table();
  virtual int64_t CalculatedChecksum();
  virtual Header* header()          { return header_; }
  virtual int32_t header_tag()      { return header_->tag(); }
  virtual int32_t header_offset()   { return header_->offset(); }
  virtual int32_t header_length()   { return header_->length(); }
  virtual int64_t header_checksum() { return header_->checksum(); }
  virtual WritableFontData* GetNewData(int32_t size);
  virtual void SetFont(Font* font);

 protected:
  Table(Header* header, ReadableFontData* data);

 private:
  Ptr<Header> header_;
  Ptr<Font> font_;
};

// C++ port only
class GenericTable : public Table, public RefCounted<GenericTable> {
 public:
  GenericTable(Header* header, ReadableFontData* data) : Table(header, data) {}
  virtual ~GenericTable() {}
};

typedef Ptr<Table> TablePtr;
typedef Ptr<Table::Header> TableHeaderPtr;
typedef std::vector<TableHeaderPtr> TableHeaderList;
typedef Ptr<Table::Builder> TableBuilderPtr;
typedef std::map<int32_t, TablePtr> TableMap;
typedef std::pair<int32_t, TablePtr> TableMapEntry;

typedef std::map<TableHeaderPtr, WritableFontDataPtr> DataBlockMap;
typedef std::pair<TableHeaderPtr, WritableFontDataPtr> DataBlockEntry;
typedef std::map<int32_t, TableBuilderPtr> TableBuilderMap;
typedef std::pair<int32_t, TableBuilderPtr> TableBuilderEntry;

class TableHeaderComparator {
 public:
  bool operator()(const TableHeaderPtr h1, const TableHeaderPtr h2);
};
typedef std::set<TableHeaderPtr, TableHeaderComparator> TableHeaderSortedSet;

}  // namespace sfntly

#endif  // SFNTLY_CPP_SRC_SFNTLY_TABLE_TABLE_H_

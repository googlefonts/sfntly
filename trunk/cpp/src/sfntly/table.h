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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_TABLE_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_TABLE_H_

#include <set>
#include <map>
#include <vector>
#include <utility>

#include "sfntly/port/type.h"
#include "sfntly/font_data_table.h"

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

   public:  // class is final, no virtual functions unless from parent
    int32_t tag();
    int32_t offset();
    int32_t length();
    bool offsetValid();
    int64_t checksum();
    bool checksumValid();

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
   protected:
    // Note: original version is Font.Builder font_builder. This results in
    //       mutual inclusion happiness that Java solved for C++.  Therefore,
    //       we need to avoid that happiness when we port it to C++.
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
            WritableFontData* data);
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
            ReadableFontData* data);
    Builder(FontDataTableBuilderContainer* font_builder, Header* header);

   public:
    virtual ~Builder();
    virtual Header* header();
    virtual void notifyPostTableBuild(FontDataTable* table);
    virtual WritableFontData* getNewData(int32_t size);

   public:
    static CALLER_ATTACH Builder* getBuilder(
        FontDataTableBuilderContainer* font_builder, Header* header,
        WritableFontData* table_data);

   private:
    Ptr<Header> header_;
  };

  class TableBasedTableBuilder : public Builder {
   protected:
    TableBasedTableBuilder(FontDataTableBuilderContainer* font_builder,
      Header* header, WritableFontData* data);
    TableBasedTableBuilder(FontDataTableBuilderContainer* font_builder,
      Header* header, ReadableFontData* data);
    TableBasedTableBuilder(FontDataTableBuilderContainer* font_builder,
      Header* header);
   public:
    virtual ~TableBasedTableBuilder();

   protected:
    virtual Table* table();

   public:
    virtual int32_t subSerialize(WritableFontData* new_data);
    virtual bool subReadyToSerialize();
    virtual int32_t subDataSizeToSerialize();
    virtual void subDataSet();

   protected:
    Ptr<Table> table_;
  };

  class GenericTableBuilder : public TableBasedTableBuilder,
    public RefCounted<GenericTableBuilder> {
   public:
    GenericTableBuilder(FontDataTableBuilderContainer* font_builder,
      Header* header, WritableFontData* data);
    virtual CALLER_ATTACH FontDataTable* subBuildTable(ReadableFontData* data);
  };

  class ArrayElementTableBuilder : public Builder {
   protected:
    ArrayElementTableBuilder(FontDataTableBuilderContainer* font_builder,
                             Header* header, WritableFontData* data);
    ArrayElementTableBuilder(FontDataTableBuilderContainer* font_builder,
                             Header* header, ReadableFontData* data);
   public:
    virtual ~ArrayElementTableBuilder();
  };

 protected:
  Table(Header* header, ReadableFontData* data);

 public:
  virtual ~Table();
  virtual int64_t calculatedChecksum();
  virtual Header* header();
  virtual int32_t headerTag();
  virtual int32_t headerOffset();
  virtual int32_t headerLength();
  virtual int64_t headerChecksum();

 public:  // FontDataTableBuilderContainer, deprecated
  virtual WritableFontData* getNewData(int32_t size);
  virtual void setFont(Font* font);

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

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_TABLE_H_

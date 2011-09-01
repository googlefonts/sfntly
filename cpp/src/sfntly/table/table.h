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

// A concrete implementation of a root level table in the font. This is the base
// class used for all specific table implementations and is used as the generic
// table for all tables which have no specific implementations.
class Table : public FontDataTable {
 public:
  class Header : public RefCounted<Header> {
   public:
    // Make a partial header with only the basic info for an empty new table.
    explicit Header(int32_t tag);

    // Make a partial header with only the basic info for a new table.
    Header(int32_t tag, int32_t length);

    // Make a full header as read from an existing font.
    Header(int32_t tag, int64_t checksum, int32_t offset, int32_t length);
    virtual ~Header();

    // Get the table tag.
    int32_t tag() { return tag_; }

    // Get the table offset. The offset is from the start of the font file.
    int32_t offset() { return offset_; }

    // Is the offset in the header valid. The offset will not be valid if the
    // table was constructed during building and has no physical location in a
    // font file.
    bool offset_valid() { return offset_valid_; }

    // Get the length of the table as recorded in the table record header.
    int32_t length() { return length_; }

    // Is the length in the header valid. The length will not be valid if the
    // table was constructed during building and has no physical location in a
    // font file.
    bool length_valid() { return length_valid_; }

    // Get the checksum for the table as recorded in the table record header.
    int64_t checksum() { return checksum_; }

    // Is the checksum valid. The checksum will not be valid if the table was
    // constructed during building and has no physical location in a font file.
    // Note that this does *NOT* check the validity of the checksum against
    // the calculated checksum for the table data.
    bool checksum_valid() { return checksum_valid_; }

    // UNIMPLEMENTED: boolean equals(Object obj)
    //                int hashCode()
    //                string toString()

   private:
    int32_t tag_;
    int32_t offset_;
    bool offset_valid_;
    int32_t length_;
    bool length_valid_;
    int64_t checksum_;
    bool checksum_valid_;

    friend class HeaderComparatorByOffset;
    friend class HeaderComparatorByTag;
  };

  // Note: original version is Builder<T extends Table>
  //       C++ template is not designed that way so plain old inheritance is
  //       chosen.
  class Builder : public FontDataTable::Builder {
   public:
    virtual ~Builder();
    virtual Header* header() { return header_; }
    virtual void NotifyPostTableBuild(FontDataTable* table);

    // Get a builder for the table type specified by the data in the header.
    // @param header the header for the table
    // @param tableData the data to be used to build the table from
    // @return builder for the table specified
    static CALLER_ATTACH Builder* GetBuilder(Header* header,
                                             WritableFontData* table_data);

    // UNIMPLEMENTED: toString()

   protected:
    Builder(Header* header, WritableFontData* data);
    Builder(Header* header, ReadableFontData* data);
    Builder(Header* header);

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
    virtual CALLER_ATTACH FontDataTable* Build();

   protected:
    TableBasedTableBuilder(Header* header, WritableFontData* data);
    TableBasedTableBuilder(Header* header, ReadableFontData* data);
    TableBasedTableBuilder(Header* header);

    // C++ port: renamed table() to GetTable()
    virtual Table* GetTable();

    // TODO(arthurhsu): style guide violation: protected member, need refactor
    Ptr<Table> table_;
  };

  class GenericTableBuilder : public TableBasedTableBuilder,
                              public RefCounted<GenericTableBuilder> {
   public:
    GenericTableBuilder(Header* header, WritableFontData* data);
    virtual CALLER_ATTACH FontDataTable* SubBuildTable(ReadableFontData* data);

    static CALLER_ATTACH
           GenericTableBuilder* CreateBuilder(Header* header,
                                              WritableFontData* data);
  };

  virtual ~Table();

  // Get the calculated checksum for the data in the table.
  virtual int64_t CalculatedChecksum();

  // Get the header for the table.
  virtual Header* header()          { return header_; }

  // Get the tag for the table from the record header.
  virtual int32_t header_tag()      { return header_->tag(); }

  // Get the offset for the table from the record header.
  virtual int32_t header_offset()   { return header_->offset(); }

  // Get the length of the table from the record header.
  virtual int32_t header_length()   { return header_->length(); }

  // Get the checksum for the table from the record header.
  virtual int64_t header_checksum() { return header_->checksum(); }

  // UNIMPLEMENTED: toString()

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

class HeaderComparator {
 public:
  virtual ~HeaderComparator() {}
  virtual bool operator()(const TableHeaderPtr h1,
                          const TableHeaderPtr h2) = 0;
};

class HeaderComparatorByOffset : public HeaderComparator {
 public:
  virtual ~HeaderComparatorByOffset() {}
  virtual bool operator()(const TableHeaderPtr h1,
                          const TableHeaderPtr h2);
};

class HeaderComparatorByTag : public HeaderComparator {
 public:
  virtual ~HeaderComparatorByTag() {}
  virtual bool operator()(const TableHeaderPtr h1,
                          const TableHeaderPtr h2);
};

typedef std::set<TableHeaderPtr, HeaderComparatorByOffset>
        HeaderOffsetSortedSet;
typedef std::set<TableHeaderPtr, HeaderComparatorByTag>
        HeaderTagSortedSet;

}  // namespace sfntly

#endif  // SFNTLY_CPP_SRC_SFNTLY_TABLE_TABLE_H_

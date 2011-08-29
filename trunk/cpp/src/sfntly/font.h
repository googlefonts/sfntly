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

#ifndef SFNTLY_CPP_SRC_SFNTLY_FONT_H_
#define SFNTLY_CPP_SRC_SFNTLY_FONT_H_

#include <vector>

#include "sfntly/port/refcount.h"
#include "sfntly/port/type.h"
#include "sfntly/port/endian.h"
#include "sfntly/font_data_table_builder_container.h"
#include "sfntly/data/font_input_stream.h"
#include "sfntly/data/font_output_stream.h"
#include "sfntly/data/writable_font_data.h"
#include "sfntly/table/table.h"

namespace sfntly {

// Note: following constants are embedded in Font class in Java.  They are
//       extracted out for easier reference from other classes.  Offset is the
//       one that is kept within class.
// Platform ids. These are used in a number of places within the font whenever
// the platform needs to be specified.
struct PlatformId {
  enum {
    kUnknown = -1,
    kUnicode = 0,
    kMacintosh = 1,
    kISO = 2,
    kWindows = 3,
    kCustom = 4
  };
};

// Unicode encoding ids. These are used in a number of places within the font
// whenever character encodings need to be specified.
struct UnicodeEncodingId {
  enum {
    kUnknown = -1,
    kUnicode1_0 = 0,
    kUnicode1_1 = 1,
    kISO10646 = 2,
    kUnicode2_0_BMP = 3,
    kUnicode2_0 = 4,
    kUnicodeVariationSequences = 5
  };
};

// Windows encoding ids. These are used in a number of places within the font
// whenever character encodings need to be specified.
struct WindowsEncodingId {
  enum {
    kUnknown = 0xffffffff,
    kSymbol = 0,
    kUnicodeUCS2 = 1,
    kShiftJIS = 2,
    kPRC = 3,
    kBig5 = 4,
    kWansung = 5,
    kJohab = 6,
    kUnicodeUCS4 = 10
  };
};

// Macintosh encoding ids. These are used in a number of places within the
// font whenever character encodings need to be specified.
struct MacintoshEncodingId {
  // Macintosh Platform Encodings
  enum {
    kUnknown = -1,
    kRoman = 0,
    kJapanese = 1,
    kChineseTraditional = 2,
    kKorean = 3,
    kArabic = 4,
    kHebrew = 5,
    kGreek = 6,
    kRussian = 7,
    kRSymbol = 8,
    kDevanagari = 9,
    kGurmukhi = 10,
    kGujarati = 11,
    kOriya = 12,
    kBengali = 13,
    kTamil = 14,
    kTelugu = 15,
    kKannada = 16,
    kMalayalam = 17,
    kSinhalese = 18,
    kBurmese = 19,
    kKhmer = 20,
    kThai = 21,
    kLaotian = 22,
    kGeorgian = 23,
    kArmenian = 24,
    kChineseSimplified = 25,
    kTibetan = 26,
    kMongolian = 27,
    kGeez = 28,
    kSlavic = 29,
    kVietnamese = 30,
    kSindhi = 31,
    kUninterpreted = 32
  };
};

extern const int32_t SFNTVERSION_1;

class FontFactory;
class Font : public RefCounted<Font> {
 public:
  class Builder : public FontDataTableBuilderContainer,
                  public RefCounted<Builder> {
   public:
    virtual ~Builder();

    static CALLER_ATTACH Builder*
        GetOTFBuilder(FontFactory* factory, InputStream* is);
    static CALLER_ATTACH Builder*
        GetOTFBuilder(FontFactory* factory,
                      ByteArray* ba,
                      int32_t offset_to_offset_table);
    static CALLER_ATTACH Builder* GetOTFBuilder(FontFactory* factory);
    virtual bool ReadyToBuild();
    virtual CALLER_ATTACH Font* Build();
    virtual CALLER_ATTACH WritableFontData* GetNewData(int32_t capacity);
    virtual CALLER_ATTACH WritableFontData*
        GetNewGrowableData(ReadableFontData* data);
    virtual void SetDigest(ByteVector* digest);
    virtual void CleanTableBuilders();
    virtual bool HasTableBuilder(int32_t tag);
    virtual Table::Builder* GetTableBuilder(int32_t tag);

    // Creates a new table builder for the table type given by the table id tag.
    // This new table has been added to the font and will replace any existing
    // builder for that table.
    // @return new empty table of the type specified by tag; if tag is not known
    //         then a generic OpenTypeTable is returned
    virtual Table::Builder* NewTableBuilder(int32_t tag);
    virtual Table::Builder* NewTableBuilder(int32_t tag,
                                            ReadableFontData* src_data);
    virtual TableBuilderMap* table_builders() { return &table_builders_; }
    virtual void TableBuilderTags(IntegerSet* key_set);
    // Note: different from Java: we don't return object in removeTableBuilder
    virtual void RemoveTableBuilder(int32_t tag);
    virtual int32_t number_of_table_builders() {
      return (int32_t)table_builders_.size();
    }

   private:
    explicit Builder(FontFactory* factory);
    virtual void LoadFont(InputStream* is);
    virtual void LoadFont(ByteArray* buffer, int32_t offset_to_offset_table);
    int32_t SfntWrapperSize();
    void BuildAllTableBuilders(DataBlockMap* table_data,
                               TableBuilderMap* builder_map);
    CALLER_ATTACH Table::Builder*
        GetTableBuilder(Table::Header* header, WritableFontData* data);
    void BuildTablesFromBuilders(TableBuilderMap* builder_map,
                                 TableMap* tables);
    static void InterRelateBuilders(TableBuilderMap* builder_map);
    void ReadHeader(FontInputStream* is, TableHeaderSortedSet* records);
    void ReadHeader(ReadableFontData* fd,
                    int32_t offset,
                    TableHeaderSortedSet* records);
    void LoadTableData(TableHeaderSortedSet* headers,
                       FontInputStream* is,
                       DataBlockMap* table_data);
    void LoadTableData(TableHeaderSortedSet* headers,
                       WritableFontData* fd,
                       DataBlockMap* table_data);

    TableBuilderMap table_builders_;
    FontFactory* factory_;  // dumb pointer, avoid circular refcounting
    int32_t sfnt_version_;
    int32_t num_tables_;
    int32_t search_range_;
    int32_t entry_selector_;
    int32_t range_shift_;
    DataBlockMap data_blocks_;
    ByteVector digest_;
  };

  virtual ~Font();

  // Gets the sfnt version set in the sfnt wrapper of the font.
  int32_t version() { return sfnt_version_; }

  // Gets a copy of the fonts digest that was created when the font was read. If
  // no digest was set at creation time then the return result will be null.
  ByteVector* digest() { return &digest_; }

  // Get the checksum for this font.
  int64_t checksum() { return checksum_; }

  // Get the number of tables in this font.
  int32_t num_tables() { return (int32_t)tables_.size(); }

  // Whether the font has a particular table.
  bool HasTable(int32_t tag);

  // UNIMPLEMENTED: public Iterator<? extends Table> iterator

  // Get the table in this font with the specified id.
  // @param tag the identifier of the table
  // @return the table specified if it exists; null otherwise
  // C++ port: rename table() to GetTable()
  Table* GetTable(int32_t tag);

  // Get a map of the tables in this font accessed by table tag.
  // @return an unmodifiable view of the tables in this font
  TableMap* Tables();

  // Serialize the font to the output stream.
  // @param os the destination for the font serialization
  // @param tableOrdering the table ordering to apply
  void Serialize(OutputStream* os, IntegerList* table_ordering);

  // Get a new data object. The size is a request for a data object and the
  // returned data object will support at least that amount. A value greater
  // than zero for the size is a request for a fixed size data object. A
  // negative or zero value for the size is a request for a variable sized data
  // object with the absolute value of the size being an estimate of the space
  // required.
  // @param size greater than zero is a request for a fixed size data object of
  //        the given size; less than or equal to zero is a request for a
  //        variable size data object with the absolute size as an estimate
  CALLER_ATTACH WritableFontData* GetNewData(int32_t size);

 private:
  // Offsets to specific elements in the underlying data. These offsets are
  // relative to the start of the table or the start of sub-blocks within the
  // table.
  struct Offset {
    enum {
      // Offsets within the main directory
      kSfntVersion = 0,
      kNumTables = 4,
      kSearchRange = 6,
      kEntrySelector = 8,
      kRangeShift = 10,
      kTableRecordBegin = 12,
      kSfntHeaderSize = 12,

      // Offsets within a specific table record
      kTableTag = 0,
      kTableCheckSum = 4,
      kTableOffset = 8,
      kTableLength = 12,
      kTableRecordSize = 16
    };
  };

  // Note: the two constants are moved to tag.h to avoid VC++ bug.
//  static const int32_t CFF_TABLE_ORDERING[];
//  static const int32_t TRUE_TYPE_TABLE_ORDERING[];

  Font(FontFactory* factory, int32_t sfnt_version, ByteVector* digest,
       TableMap* tables);

  void BuildTableHeadersForSerialization(IntegerList* table_ordering,
                                         TableHeaderList* table_headers);
  void SerializeHeader(FontOutputStream* fos, TableHeaderList* table_headers);
  void SerializeTables(FontOutputStream* fos, TableHeaderList* table_headers);
  void TableOrdering(IntegerList* default_table_ordering,
                     IntegerList* table_ordering);
  void DefaultTableOrdering(IntegerList* default_table_ordering);

  FontFactory* factory_;  // dumb pointer, avoid circular ref-counting
  int32_t sfnt_version_;
  ByteVector digest_;
  int64_t checksum_;
  TableMap tables_;
};
typedef Ptr<Font> FontPtr;
typedef std::vector<FontPtr> FontArray;
typedef Ptr<Font::Builder> FontBuilderPtr;
typedef std::vector<FontBuilderPtr> FontBuilderArray;

}  // namespace sfntly

#endif  // SFNTLY_CPP_SRC_SFNTLY_FONT_H_

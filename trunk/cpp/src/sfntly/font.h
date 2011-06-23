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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_H_

#include <vector>

#include "sfntly/port/refcount.h"
#include "sfntly/port/type.h"
#include "sfntly/port/endian.h"
#include "sfntly/table.h"
#include "sfntly/font_data_table_builder_container.h"
#include "sfntly/data/font_input_stream.h"
#include "sfntly/data/font_output_stream.h"
#include "sfntly/data/writable_font_data.h"

namespace sfntly {

// Note: following constants are embedded in Font class in Java.  They are
//       extracted out for easier reference from other classes.  Offset is the
//       one that is kept within class.
// Platform ids. These are used in a number of places within the font whenever
// the platform needs to be specified.
struct PlatformId {
  static const int32_t kUnknown;
  static const int32_t kUnicode;
  static const int32_t kMacintosh;
  static const int32_t kISO;
  static const int32_t kWindows;
  static const int32_t kCustom;
};

// Unicode encoding ids. These are used in a number of places within the font
// whenever character encodings need to be specified.
struct UnicodeEncodingId {
  static const int32_t kUnknown;
  static const int32_t kUnicode1_0;
  static const int32_t kUnicode1_1;
  static const int32_t kISO10646;
  static const int32_t kUnicode2_0_BMP;
  static const int32_t kUnicode2_0;
  static const int32_t kUnicodeVariationSequences;
};

// Windows encoding ids. These are used in a number of places within the font
// whenever character encodings need to be specified.
struct WindowsEncodingId {
  static const int32_t kUnknown;
  static const int32_t kSymbol;
  static const int32_t kUnicodeUCS2;
  static const int32_t kShiftJIS;
  static const int32_t kPRC;
  static const int32_t kBig5;
  static const int32_t kWansung;
  static const int32_t kJohab;
  static const int32_t kUnicodeUCS4;
};

// Macintosh encoding ids. These are used in a number of places within the
// font whenever character encodings need to be specified.
struct MacintoshEncodingId {
  // Macintosh Platform Encodings
  static const int32_t kUnknown;
  static const int32_t kRoman;
  static const int32_t kJapanese;
  static const int32_t kChineseTraditional;
  static const int32_t kKorean;
  static const int32_t kArabic;
  static const int32_t kHebrew;
  static const int32_t kGreek;
  static const int32_t kRussian;
  static const int32_t kRSymbol;
  static const int32_t kDevanagari;
  static const int32_t kGurmukhi;
  static const int32_t kGujarati;
  static const int32_t kOriya;
  static const int32_t kBengali;
  static const int32_t kTamil;
  static const int32_t kTelugu;
  static const int32_t kKannada;
  static const int32_t kMalayalam;
  static const int32_t kSinhalese;
  static const int32_t kBurmese;
  static const int32_t kKhmer;
  static const int32_t kThai;
  static const int32_t kLaotian;
  static const int32_t kGeorgian;
  static const int32_t kArmenian;
  static const int32_t kChineseSimplified;
  static const int32_t kTibetan;
  static const int32_t kMongolian;
  static const int32_t kGeez;
  static const int32_t kSlavic;
  static const int32_t kVietnamese;
  static const int32_t kSindhi;
  static const int32_t kUninterpreted;
};

extern const int32_t SFNTVERSION_1;

class FontFactory;
class Font : public RefCounted<Font> {
 private:
  // Offsets to specific elements in the underlying data. These offsets are
  // relative to the start of the table or the start of sub-blocks within the
  // table.
  struct Offset {
    // Offsets within the main directory
    static const int32_t kSfntVersion;
    static const int32_t kNumTables;
    static const int32_t kSearchRange;
    static const int32_t kEntrySelector;
    static const int32_t kRangeShift;
    static const int32_t kTableRecordBegin;
    static const int32_t kSfntHeaderSize;

    // Offsets within a specific table record
    static const int32_t kTableTag;
    static const int32_t kTableCheckSum;
    static const int32_t kTableOffset;
    static const int32_t kTableLength;
    static const int32_t kTableRecordSize;
  };

  // Note: the two constants are moved to tag.h to avoid VC++ bug.
//  static const int32_t CFF_TABLE_ORDERING[];
//  static const int32_t TRUE_TYPE_TABLE_ORDERING[];

 public:
  virtual ~Font();

 private:
  Font(FontFactory* factory, int32_t sfnt_version, ByteVector* digest,
       TableMap* tables);

 public:
  // Gets the sfnt version set in the sfnt wrapper of the font.
  virtual int32_t version();

  // Gets a copy of the fonts digest that was created when the font was read. If
  // no digest was set at creation time then the return result will be null.
  virtual ByteVector* digest();

  // Get the checksum for this font.
  virtual int64_t checksum();

  // Get the number of tables in this font.
  virtual int32_t numTables();

  // Whether the font has a particular table.
  virtual bool hasTable(int32_t tag);

  // UNIMPLEMENTED: public Iterator<? extends Table> iterator

  // Get the table in this font with the specified id.
  // @param tag the identifier of the table
  // @return the table specified if it exists; null otherwise
  virtual Table* table(int32_t tag);

  // Get a map of the tables in this font accessed by table tag.
  // @return an unmodifiable view of the tables in this font
  virtual TableMap* tables();

  // Serialize the font to the output stream.
  // @param os the destination for the font serialization
  // @param tableOrdering the table ordering to apply
  virtual void serialize(OutputStream* os, IntegerList* table_ordering);

 private:
  void buildTableHeadersForSerialization(IntegerList* table_ordering,
    TableHeaderList* table_headers);
  void serializeHeader(FontOutputStream* fos, TableHeaderList* table_headers);
  void serializeTables(FontOutputStream* fos, TableHeaderList* table_headers);
  void tableOrdering(IntegerList* default_table_ordering,
    IntegerList* table_ordering);
  void defaultTableOrdering(IntegerList* default_table_ordering);

 public:
  class Builder : public FontDataTableBuilderContainer,
                  public RefCounted<Builder> {
   public:
    virtual ~Builder();

   private:
    explicit Builder(FontFactory* factory);
    virtual void loadFont(InputStream* is);
    virtual void loadFont(ByteArray* buffer, int32_t offset_to_offset_table);

   public:
    static CALLER_ATTACH Builder*
        getOTFBuilder(FontFactory* factory, InputStream* is);
    static CALLER_ATTACH Builder*
        getOTFBuilder(FontFactory* factory, ByteArray* ba,
                      int32_t offset_to_offset_table);
    static CALLER_ATTACH Builder* getOTFBuilder(FontFactory* factory);
    virtual bool readyToBuild();
    virtual CALLER_ATTACH Font* build();
    virtual CALLER_ATTACH WritableFontData* getNewData(int32_t capacity);
    virtual CALLER_ATTACH WritableFontData*
        getNewGrowableData(ReadableFontData* data);
    virtual void setDigest(ByteVector* digest);
    virtual void cleanTableBuilders();
    virtual bool hasTableBuilder(int32_t tag);
    virtual Table::Builder* getTableBuilder(int32_t tag);

    // Creates a new table builder for the table type given by the table id tag.
    // This new table has been added to the font and will replace any existing
    // builder for that table.
    // @return new empty table of the type specified by tag; if tag is not known
    //         then a generic OpenTypeTable is returned
    virtual CALLER_ATTACH Table::Builder* newTableBuilder(int32_t tag);
    virtual CALLER_ATTACH Table::Builder*
        newTableBuilder(int32_t tag, ReadableFontData* src_data);
    virtual TableBuilderMap* tableBuilders();
    virtual void tableBuilderTags(IntegerSet* key_set);
    // Note: different from Java: we don't return object in removeTableBuilder
    virtual void removeTableBuilder(int32_t tag);
    virtual int32_t numberOfTableBuilders();

   private:
    int32_t sfntWrapperSize();
    void buildAllTableBuilders(DataBlockMap* table_data,
                               TableBuilderMap* builder_map);
    CALLER_ATTACH Table::Builder*
        getTableBuilder(Table::Header* header, WritableFontData* data);
    void buildTablesFromBuilders(TableBuilderMap* builder_map,
                                 TableMap* tables);
    static void interRelateBuilders(TableBuilderMap* builder_map);
    void readHeader(FontInputStream* is, TableHeaderSortedSet* records);
    void loadTableData(TableHeaderSortedSet* headers, FontInputStream* is,
                       DataBlockMap* table_data);
    void readHeader(ReadableFontData* fd, int32_t offset,
                    TableHeaderSortedSet* records);
    void loadTableData(TableHeaderSortedSet* headers, WritableFontData* fd,
                       DataBlockMap* table_data);

   private:
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

 public:
  // Get a new data object. The size is a request for a data object and the
  // returned data object will support at least that amount. A value greater
  // than zero for the size is a request for a fixed size data object. A
  // negative or zero value for the size is a request for a variable sized data
  // object with the absolute value of the size being an estimate of the space
  // required.
  // @param size greater than zero is a request for a fixed size data object of
  //        the given size; less than or equal to zero is a request for a
  //        variable size data object with the absolute size as an estimate
  CALLER_ATTACH WritableFontData* getNewData(int32_t size);

 private:
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

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_H_

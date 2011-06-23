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
#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_CMAP_TABLE_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_CMAP_TABLE_H_

#include <vector>
#include <map>

#include "sfntly/port/refcount.h"
#include "sfntly/table.h"
#include "sfntly/subtable.h"

namespace sfntly {

// A CMap table
class CMapTable : public Table, public RefCounted<CMapTable> {
 private:
  static const int32_t NOTDEF;
  struct Offset {
    static const int32_t kVersion;
    static const int32_t kNumTables;
    static const int32_t kEncodingRecordStart;

    // offsets relative to the encoding record
    static const int32_t kEncodingRecordPlatformId;
    static const int32_t kEncodingRecordEncodingId;
    static const int32_t kEncodingRecordOffset;
    static const int32_t kEncodingRecordSize;

    static const int32_t kFormat;

    // Format 0: Byte encoding table
    static const int32_t kFormat0Format;
    static const int32_t kFormat0Length;
    static const int32_t kFormat0Language;
    static const int32_t kFormat0GlyphIdArray;

    // Format 2: High-byte mapping through table
    static const int32_t kFormat2Format;
    static const int32_t kFormat2Length;
    static const int32_t kFormat2Language;
    static const int32_t kFormat2SubHeaderKeys;
    static const int32_t kFormat2SubHeaders;
    // offset relative to the subHeader structure
    static const int32_t kFormat2SubHeader_firstCode;
    static const int32_t kFormat2SubHeader_entryCount;
    static const int32_t kFormat2SubHeader_idDelta;
    static const int32_t kFormat2SubHeader_idRangeOffset;
    static const int32_t kFormat2SubHeader_structLength;

    // Format 4: Segment mapping to delta values
    static const int32_t kFormat4Format;
    static const int32_t kFormat4Length;
    static const int32_t kFormat4Language;
    static const int32_t kFormat4SegCountX2;
    static const int32_t kFormat4SearchRange;
    static const int32_t kFormat4EntrySelector;
    static const int32_t kFormat4RangeShift;
    static const int32_t kFormat4EndCount;

    // format 6: Trimmed table mapping
    static const int32_t kFormat6Format;
    static const int32_t kFormat6Length;
    static const int32_t kFormat6Language;
    static const int32_t kFormat6FirstCode;
    static const int32_t kFormat6EntryCount;
    static const int32_t kFormat6GlyphIdArray;

    // Format 8: mixed 16-bit and 32-bit coverage
    static const int32_t kFormat8Format;
    static const int32_t kFormat8Length;
    static const int32_t kFormat8Language;
    static const int32_t kFormat8Is32;
    static const int32_t kFormat8nGroups204;
    static const int32_t kFormat8Groups208;
    // ofset relative to the group structure
    static const int32_t kFormat8Group_startCharCode;
    static const int32_t kFormat8Group_endCharCode;
    static const int32_t kFormat8Group_startGlyphId;
    static const int32_t kFormat8Group_structLength;

    // Format 10: Trimmed array
    static const int32_t kFormat10Format;
    static const int32_t kFormat10Length;
    static const int32_t kFormat10Language;
    static const int32_t kFormat10StartCharCode;
    static const int32_t kFormat10NumChars;
    static const int32_t kFormat10Glyphs0;

    // Format 12: Segmented coverage
    static const int32_t kFormat12Format;
    static const int32_t kFormat12Length;
    static const int32_t kFormat12Language;
    static const int32_t kFormat12nGroups;
    static const int32_t kFormat12Groups;
    static const int32_t kFormat12Groups_structLength;
    // offsets within the group structure
    static const int32_t kFormat12_startCharCode;
    static const int32_t kFormat12_endCharCode;
    static const int32_t kFormat12_startGlyphId;

    // Format 13: Last Resort Font
    static const int32_t kFormat13Format;
    static const int32_t kFormat13Length;
    static const int32_t kFormat13Language;
    static const int32_t kFormat13nGroups;
    static const int32_t kFormat13Groups;
    static const int32_t kFormat13Groups_structLength;
    // offsets within the group structure
    static const int32_t kFormat13_startCharCode;
    static const int32_t kFormat13_endCharCode;
    static const int32_t kFormat13_glyphId;

    // Format 14: Unicode Variation Sequences
    static const int32_t kFormat14Format;
    static const int32_t kFormat14Length;

    // TODO(stuartg): finish tables
    // Default UVS Table

    // Non-default UVS Table
    static const int32_t kLast;
  };

  // CMap subtable formats
  struct CMapFormat {
    static const int32_t kFormat0;
    static const int32_t kFormat2;
    static const int32_t kFormat4;
    static const int32_t kFormat6;
    static const int32_t kFormat8;
    static const int32_t kFormat10;
    static const int32_t kFormat12;
    static const int32_t kFormat13;
    static const int32_t kFormat14;
  };

 public:
  class CMapId {
   public:
    CMapId(int32_t platform_id, int32_t encoding_id);
    CMapId(const CMapId& obj);
    int32_t platformId();
    int32_t encodingId();
    bool operator==(const CMapId& obj);
    const CMapId& operator=(const CMapId& obj);
    int hashCode() const;

    friend class CMapIdComparator;

   private:
    int32_t platform_id_;
    int32_t encoding_id_;
  };
  static CMapId WINDOWS_BMP;
  static CMapId WINDOWS_UCS4;
  static CMapId MAC_ROMAN;

  class CMapIdComparator {
   public:
    bool operator()(const CMapId& lhs, const CMapId& rhs);
  };

  class CMapFilter {
   public:
    virtual bool accept(CMapId cmap_id) = 0;
    // Make gcc -Wnon-virtual-dtor happy.
    virtual ~CMapFilter() {}
  };

  // The abstract base class for all cmaps.
  //
  // CMap equality is based on the equality of the (@link {@link CMapId} that
  // defines the CMap. In the cmap table for a font there can only be one cmap
  // with a given cmap id (pair of platform and encoding ids) no matter what the
  // type of the cmap is.
  //
  // The cmap implements {@code Iterable<Integer>} to allow iteration over
  // characters that are mapped by the cmap. This iteration mostly returns the
  // characters mapped by the cmap. It will return all characters mapped by the
  // cmap to anything but .notdef <b>but</b> it may return some that are mapped
  // or are mapped to .notdef. Various cmap tables provide ranges and such to
  // describe characters for lookup but without going the full way to mapping to
  // the glyph id it isn't always possible to tell if a character will end up
  // with a valid glyph id. So, some of the characters returned from the
  // iterator may still end up pointing to the .notdef glyph. However, the
  // number of such characters should be small in most cases with well designed
  // cmaps.
  class CMap : public SubTable {
   public:
    CMap(ReadableFontData* data, int32_t format, const CMapId& cmap_id);
    virtual ~CMap();
    virtual int32_t format();
    virtual CMapId cmapId();
    virtual int32_t platformId();
    virtual int32_t encodingId();

    // Get the language of the cmap.
    //
    // Note on the language field in 'cmap' subtables: The language field must
    // be set to zero for all cmap subtables whose platform IDs are other than
    // Macintosh (platform ID 1). For cmap subtables whose platform IDs are
    // Macintosh, set this field to the Macintosh language ID of the cmap
    // subtable plus one, or to zero if the cmap subtable is not
    // language-specific. For example, a Mac OS Turkish cmap subtable must set
    // this field to 18, since the Macintosh language ID for Turkish is 17. A
    // Mac OS Roman cmap subtable must set this field to 0, since Mac OS Roman
    // is not a language-specific encoding.
    //
    // @return the language id
    virtual int32_t language() = 0;

    // Gets the glyph id for the character code provided.
    // The character code provided must be in the encoding used by the cmap
    // table.
    virtual int32_t glyphId(int32_t character) = 0;

   public:
    class Builder : public SubTable::Builder {
     protected:
      Builder(FontDataTableBuilderContainer* container, ReadableFontData* data,
              int32_t format, const CMapId& cmap_id);
      Builder(FontDataTableBuilderContainer* container, WritableFontData* data,
              int32_t format, const CMapId& cmap_id);

     public:
      virtual ~Builder();
      CALLER_ATTACH static Builder*
          getBuilder(FontDataTableBuilderContainer* container,
                     ReadableFontData* data, int32_t offset,
                     const CMapId& cmap_id);

      // Note: yes, an object is returned on stack since it's small enough.
      virtual CMapId cmapId();
      virtual int32_t platformId();
      virtual int32_t encodingId();

     protected:
      virtual int32_t subSerialize(WritableFontData* new_data);
      virtual bool subReadyToSerialize();
      virtual int32_t subDataSizeToSerialize();
      virtual void subDataSet();

     private:
      int32_t format_;
      CMapId cmap_id_;
    };

   private:
    int32_t format_;
    CMapId cmap_id_;
  };
  typedef Ptr<CMap::Builder> CMapBuilderPtr;
  typedef std::map<CMapId, CMapBuilderPtr, CMapIdComparator> CMapBuilderMap;

  // A cmap format 0 sub table
  class CMapFormat0 : public CMap, public RefCounted<CMapFormat0> {
   protected:
    CMapFormat0(ReadableFontData* data, const CMapId& cmap_id);

   public:
    virtual ~CMapFormat0();
    virtual int32_t language();
    virtual int32_t glyphId(int32_t character);

    class Builder : public CMap::Builder,
                    public RefCounted<Builder> {
     public:
      Builder(FontDataTableBuilderContainer* container, ReadableFontData* data,
        int32_t offset, const CMapId& cmap_id);
      Builder(FontDataTableBuilderContainer* container, WritableFontData* data,
        int32_t offset, const CMapId& cmap_id);
      virtual ~Builder();
     protected:
      virtual CALLER_ATTACH FontDataTable*
          subBuildTable(ReadableFontData* data);
    };
  };

  // A cmap format 2 sub table
  // The format 2 cmap is used for multi-byte encodings such as SJIS,
  // EUC-JP/KR/CN, Big5, etc.
  class CMapFormat2 : public CMap, public RefCounted<CMapFormat2> {
   protected:
    CMapFormat2(ReadableFontData* data, const CMapId& cmap_id);

   public:
    virtual ~CMapFormat2();
    virtual int32_t language();
    virtual int32_t glyphId(int32_t character);

    // Returns how many bytes would be consumed by a lookup of this character
    // with this cmap. This comes about because the cmap format 2 table is
    // designed around multi-byte encodings such as SJIS, EUC-JP, Big5, etc.
    // return the number of bytes consumed from this "character" - either 1 or 2
    virtual int32_t bytesConsumed(int32_t character);

   private:
    int32_t subHeaderOffset(int32_t sub_header_index);
    int32_t firstCode(int32_t sub_header_index);
    int32_t entryCount(int32_t sub_header_index);
    int32_t idRangeOffset(int32_t sub_header_index);
    int32_t idDelta(int32_t sub_header_index);

    class Builder : public CMap::Builder,
                    public RefCounted<Builder> {
     public:
      Builder(FontDataTableBuilderContainer* container, ReadableFontData* data,
        int32_t offset, const CMapId& cmap_id);
      Builder(FontDataTableBuilderContainer* container, WritableFontData* data,
        int32_t offset, const CMapId& cmap_id);
      virtual ~Builder();
     protected:
      virtual CALLER_ATTACH FontDataTable*
          subBuildTable(ReadableFontData* data);
    };
  };

  class Builder : public Table::ArrayElementTableBuilder {
   public:
    // Constructor scope altered to public because C++ does not allow base
    // class to instantiate derived class with protected constructors.
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
      WritableFontData* data);
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
      ReadableFontData* data);

    virtual int32_t subSerialize(WritableFontData* new_data);
    virtual bool subReadyToSerialize();
    virtual int32_t subDataSizeToSerialize();
    virtual void subDataSet();
    virtual CALLER_ATTACH FontDataTable* subBuildTable(ReadableFontData* data);

   protected:
    static CALLER_ATTACH CMap::Builder* cmapBuilder(
        FontDataTableBuilderContainer* container, ReadableFontData* data,
        int32_t index);

   private:
    static int32_t numCMaps(ReadableFontData* data);

   private:
    int32_t version_;
    CMapBuilderMap cmap_builders_;
  };

 private:
  class CMapIterator {
   public:
    // If filter is NULL, filter through all tables.
    CMapIterator(CMapTable* table, CMapFilter* filter);
    bool hasNext();
    CMap* next();

   private:
    int32_t table_index_;
    CMapFilter* filter_;
    CMapTable* table_;
  };

 private:
  CMapTable(Header* header, ReadableFontData* data);

 public:
  virtual ~CMapTable();

  // Get the table version.
  virtual int32_t version();

  // Get the number of cmaps within the CMap table.
  virtual int32_t numCMaps();

  // Get the cmap id for the cmap with the given index.
  // Note: yes, an object is returned on stack since it's small enough.
  virtual CMapId cmapId(int32_t index);

  virtual int32_t platformId(int32_t index);
  virtual int32_t encodingId(int32_t index);

  // Get the offset in the table data for the cmap table with the given index.
  // The offset is from the beginning of the table.
  virtual int32_t offset(int32_t index);

 private:
  // Get the offset in the table data for the encoding record for the cmap with
  // the given index. The offset is from the beginning of the table.
  int32_t offsetForEncodingRecord(int32_t index);
};
typedef std::vector<CMapTable::CMapId> CMapIdList;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_CMAP_TABLE_H_

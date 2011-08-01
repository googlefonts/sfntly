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

// CMap subtable formats
struct CMapFormat {
  enum {
    kFormat0 = 0,
    kFormat2 = 2,
    kFormat4 = 4,
    kFormat6 = 6,
    kFormat8 = 8,
    kFormat10 = 10,
    kFormat12 = 12,
    kFormat13 = 13,
    kFormat14 = 14
  };
};

// A CMap table
class CMapTable : public Table, public RefCounted<CMapTable> {
public:
  // CMapTable::CMapId
  class CMapId {
   public:
    CMapId(int32_t platform_id, int32_t encoding_id);
    CMapId(const CMapId& obj);

    int32_t platform_id() { return platform_id_; }
    int32_t encoding_id() { return encoding_id_; }

    bool operator==(const CMapId& obj);
    const CMapId& operator=(const CMapId& obj);
    int HashCode() const;

    friend class CMapIdComparator;

   private:
    int32_t platform_id_;
    int32_t encoding_id_;
  };
  static CMapId WINDOWS_BMP;
  static CMapId WINDOWS_UCS4;
  static CMapId MAC_ROMAN;

  // CMapTable::CMapIdComparator
  class CMapIdComparator {
   public:
    bool operator()(const CMapId& lhs, const CMapId& rhs);
  };

  // A filter on cmap
  // CMapTable::CMapFilter
  class CMapFilter {
   public:
    // Test on whether the cmap is acceptable or not
    // @param cmap_id the id of the cmap
    // @return true if the cmap is acceptable; false otherwise
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
  // cmap to anything but .notdef <b>but</b> it may return some that are not
  // mapped or are mapped to .notdef. Various cmap tables provide ranges and
  // such to describe characters for lookup but without going the full way to
  // mapping to the glyph id it isn't always possible to tell if a character
  // will end up with a valid glyph id. So, some of the characters returned from
  // the iterator may still end up pointing to the .notdef glyph. However, the
  // number of such characters should be small in most cases with well designed
  // cmaps.
  class Builder;
  class CMap : public SubTable {
   public:
    // CMapTable::CMap::Builder
    class Builder : public SubTable::Builder {
     public:
      virtual ~Builder();

      CALLER_ATTACH static Builder*
          GetBuilder(FontDataTableBuilderContainer* container,
                     ReadableFontData* data,
                     int32_t offset,
                     const CMapId& cmap_id);

      // Note: yes, an object is returned on stack since it's small enough.
      virtual CMapId cmap_id() { return cmap_id_; }
      virtual int32_t platform_id() { return cmap_id_.platform_id(); }
      virtual int32_t encoding_id() { return cmap_id_.encoding_id(); }

     protected:
      Builder(FontDataTableBuilderContainer* container,
              ReadableFontData* data,
              int32_t format,
              const CMapId& cmap_id);
      Builder(FontDataTableBuilderContainer* container,
              WritableFontData* data,
              int32_t format,
              const CMapId& cmap_id);

      virtual int32_t SubSerialize(WritableFontData* new_data);
      virtual bool SubReadyToSerialize();
      virtual int32_t SubDataSizeToSerialize();
      virtual void SubDataSet();

     private:
      int32_t format_;
      CMapId cmap_id_;

      friend class CMapTable::Builder;
    };

    CMap(ReadableFontData* data, int32_t format, const CMapId& cmap_id);
    virtual ~CMap();
    virtual int32_t format() { return format_; }
    virtual CMapId cmap_id() { return cmap_id_; }
    virtual int32_t platform_id() { return cmap_id_.platform_id(); }
    virtual int32_t encoding_id() { return cmap_id_.encoding_id(); }

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
    virtual int32_t Language() = 0;

    // Gets the glyph id for the character code provided.
    // The character code provided must be in the encoding used by the cmap
    // table.
    virtual int32_t GlyphId(int32_t character) = 0;

   private:
    int32_t format_;
    CMapId cmap_id_;
  };
  typedef Ptr<CMap::Builder> CMapBuilderPtr;
  typedef std::map<CMapId, CMapBuilderPtr, CMapIdComparator> CMapBuilderMap;

  // A cmap format 0 sub table
  class CMapFormat0 : public CMap, public RefCounted<CMapFormat0> {
   public:
    // CMapTable::CMapFormat0::Builder
    class Builder : public CMap::Builder,
                    public RefCounted<Builder> {
     public:
      Builder(FontDataTableBuilderContainer* container,
              ReadableFontData* data,
              int32_t offset,
              const CMapId& cmap_id);
      Builder(FontDataTableBuilderContainer* container,
              WritableFontData* data,
              int32_t offset,
              const CMapId& cmap_id);
      virtual ~Builder();

     protected:
      virtual CALLER_ATTACH FontDataTable*
          SubBuildTable(ReadableFontData* data);
    };

    virtual ~CMapFormat0();
    virtual int32_t Language();
    virtual int32_t GlyphId(int32_t character);

   private:
    CMapFormat0(ReadableFontData* data, const CMapId& cmap_id);
  };

  // A cmap format 2 sub table
  // The format 2 cmap is used for multi-byte encodings such as SJIS,
  // EUC-JP/KR/CN, Big5, etc.
  class CMapFormat2 : public CMap, public RefCounted<CMapFormat2> {
   public:
    // CMapTable::CMapFormat2::Builder
    class Builder : public CMap::Builder,
                    public RefCounted<Builder> {
     public:
      Builder(FontDataTableBuilderContainer* container,
              ReadableFontData* data,
              int32_t offset,
              const CMapId& cmap_id);
      Builder(FontDataTableBuilderContainer* container,
              WritableFontData* data,
              int32_t offset,
              const CMapId& cmap_id);
      virtual ~Builder();

     protected:
      virtual CALLER_ATTACH FontDataTable*
          SubBuildTable(ReadableFontData* data);
    };

    virtual ~CMapFormat2();
    virtual int32_t Language();
    virtual int32_t GlyphId(int32_t character);

    // Returns how many bytes would be consumed by a lookup of this character
    // with this cmap. This comes about because the cmap format 2 table is
    // designed around multi-byte encodings such as SJIS, EUC-JP, Big5, etc.
    // return the number of bytes consumed from this "character" - either 1 or 2
    virtual int32_t BytesConsumed(int32_t character);

   private:
    CMapFormat2(ReadableFontData* data, const CMapId& cmap_id);

    int32_t SubHeaderOffset(int32_t sub_header_index);
    int32_t FirstCode(int32_t sub_header_index);
    int32_t EntryCount(int32_t sub_header_index);
    int32_t IdRangeOffset(int32_t sub_header_index);
    int32_t IdDelta(int32_t sub_header_index);
  };

  // CMapTable::Builder
  class Builder : public Table::ArrayElementTableBuilder,
                  public RefCounted<Builder> {
   public:
    // Constructor scope altered to public because C++ does not allow base
    // class to instantiate derived class with protected constructors.
    Builder(FontDataTableBuilderContainer* font_builder,
            Header* header,
            WritableFontData* data);
    Builder(FontDataTableBuilderContainer* font_builder,
            Header* header,
            ReadableFontData* data);
    virtual ~Builder();

    virtual int32_t SubSerialize(WritableFontData* new_data);
    virtual bool SubReadyToSerialize();
    virtual int32_t SubDataSizeToSerialize();
    virtual void SubDataSet();
    virtual CALLER_ATTACH FontDataTable* SubBuildTable(ReadableFontData* data);

   protected:
    static CALLER_ATTACH CMap::Builder*
        CMapBuilder(FontDataTableBuilderContainer* container,
                    ReadableFontData* data,
                    int32_t index);

   private:
    static int32_t NumCMaps(ReadableFontData* data);

    int32_t version_;
    CMapBuilderMap cmap_builders_;
  };

  virtual ~CMapTable();

  // Get the table version.
  virtual int32_t Version();

  // Get the number of cmaps within the CMap table.
  virtual int32_t NumCMaps();

  // Get the cmap id for the cmap with the given index.
  // Note: yes, an object is returned on stack since it's small enough.
  //       This function is renamed from cmapId to GetCMapId().
  virtual CMapId GetCMapId(int32_t index);

  virtual int32_t PlatformId(int32_t index);
  virtual int32_t EncodingId(int32_t index);

  // Get the offset in the table data for the cmap table with the given index.
  // The offset is from the beginning of the table.
  virtual int32_t Offset(int32_t index);

 private:
  static const int32_t NOTDEF;

  // Offsets to specific elements in the underlying data. These offsets are
  // relative to the start of the table or the start of sub-blocks within
  // the table.
  struct Offset {
    enum {
      kVersion = 0,
      kNumTables = 2,
      kEncodingRecordStart = 4,

      // offsets relative to the encoding record
      kEncodingRecordPlatformId = 0,
      kEncodingRecordEncodingId = 2,
      kEncodingRecordOffset = 4,
      kEncodingRecordSize = 8,

      kFormat = 0,

      // Format 0: Byte encoding table
      kFormat0Format = 0,
      kFormat0Length = 2,
      kFormat0Language = 4,
      kFormat0GlyphIdArray = 6,

      // Format 2: High-byte mapping through table
      kFormat2Format = 0,
      kFormat2Length = 2,
      kFormat2Language = 4,
      kFormat2SubHeaderKeys = 6,
      kFormat2SubHeaders = 518,
      // offset relative to the subHeader structure
      kFormat2SubHeader_firstCode = 0,
      kFormat2SubHeader_entryCount = 2,
      kFormat2SubHeader_idDelta = 4,
      kFormat2SubHeader_idRangeOffset = 6,
      kFormat2SubHeader_structLength = 8,

      // Format 4: Segment mapping to delta values
      kFormat4Format = 0,
      kFormat4Length = 2,
      kFormat4Language = 4,
      kFormat4SegCountX2 = 6,
      kFormat4SearchRange = 8,
      kFormat4EntrySelector = 10,
      kFormat4RangeShift = 12,
      kFormat4EndCount = 14,

      // format 6: Trimmed table mapping
      kFormat6Format = 0,
      kFormat6Length = 2,
      kFormat6Language = 4,
      kFormat6FirstCode = 6,
      kFormat6EntryCount = 8,
      kFormat6GlyphIdArray = 10,

      // Format 8: mixed 16-bit and 32-bit coverage
      kFormat8Format = 0,
      kFormat8Length = 4,
      kFormat8Language = 8,
      kFormat8Is32 = 12,
      kFormat8nGroups204 = 8204,
      kFormat8Groups208 = 8208,
      // offset relative to the group structure
      kFormat8Group_startCharCode = 0,
      kFormat8Group_endCharCode = 4,
      kFormat8Group_startGlyphId = 8,
      kFormat8Group_structLength = 12,

      // Format 10: Trimmed array
      kFormat10Format = 0,
      kFormat10Length = 4,
      kFormat10Language = 8,
      kFormat10StartCharCode = 12,
      kFormat10NumChars = 16,
      kFormat10Glyphs0 = 20,

      // Format 12: Segmented coverage
      kFormat12Format = 0,
      kFormat12Length = 4,
      kFormat12Language = 8,
      kFormat12nGroups = 12,
      kFormat12Groups = 16,
      kFormat12Groups_structLength = 12,
      // offsets within the group structure
      kFormat12_startCharCode = 0,
      kFormat12_endCharCode = 4,
      kFormat12_startGlyphId = 8,

      // Format 13: Last Resort Font
      kFormat13Format = 0,
      kFormat13Length = 4,
      kFormat13Language = 8,
      kFormat13nGroups = 12,
      kFormat13Groups = 16,
      kFormat13Groups_structLength = 12,
      // offsets within the group structure
      kFormat13_startCharCode = 0,
      kFormat13_endCharCode = 4,
      kFormat13_glyphId = 8,

      // Format 14: Unicode Variation Sequences
      kFormat14Format = 0,
      kFormat14Length = 2,

      // TODO(stuartg): finish tables
      // Default UVS Table

      // Non-default UVS Table
      kLast = -1
    };
  };

  class CMapIterator {
   public:
    // If filter is NULL, filter through all tables.
    CMapIterator(CMapTable* table, CMapFilter* filter);
    bool HasNext();
    CMap* Next();

   private:
    int32_t table_index_;
    CMapFilter* filter_;
    CMapTable* table_;
  };

  CMapTable(Header* header, ReadableFontData* data);

  // Get the offset in the table data for the encoding record for the cmap with
  // the given index. The offset is from the beginning of the table.
  int32_t OffsetForEncodingRecord(int32_t index);
};
typedef std::vector<CMapTable::CMapId> CMapIdList;
typedef Ptr<CMapTable> CMapTablePtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_CMAP_TABLE_H_

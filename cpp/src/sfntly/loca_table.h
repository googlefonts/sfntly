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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_LOCA_TABLE_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_LOCA_TABLE_H_

#include "sfntly/table.h"
#include "sfntly/font_header_table.h"

namespace sfntly {

class LocaTable : public Table, public RefCounted<LocaTable> {
 public:
  // Note: different implementation than Java, caller to instantiate this class
  //       object directly from stack instead of calling LocaTable::iterator().
  class LocaIterator {
   public:
    explicit LocaIterator(LocaTable* table);
    bool HasNext();
    int32_t Next();

   private:
    int32_t index_;
    LocaTable* table_;  // use dumb pointer since it's a composition object
  };

  class Builder : public Table::ArrayElementTableBuilder,
                  public RefCounted<Builder> {
   public:
    // Constructor scope altered to public for base class to instantiate.
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
            WritableFontData* data);
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
            ReadableFontData* data);
    virtual ~Builder();

    void SetFormatVersion(int32_t format_version);

    // Gets the List of locas for loca table builder. These may be manipulated
    // in any way by the caller and the changes will be reflected in the final
    // loca table produced.
    // If there is no current data for the loca table builder or the loca list
    // have not been previously set then this will return an empty List.
    IntegerList* LocaList();
    void SetLocaList(IntegerList* list);

    // Return the offset for the given glyph id. Valid glyph ids are from 0 to
    // one less than the number of glyphs. The zero entry is the special entry
    // for the notdef glyph. The final entry beyond the last glyph id is used to
    // calculate the size of the last glyph.
    // @param glyphId the glyph id to get the offset for; must be less than or
    //        equal to one more than the number of glyph ids
    // @return the offset in the glyph table to the specified glyph id
    int32_t GlyphOffset(int32_t glyph_id);

    // Get the length of the data in the glyph table for the specified glyph id.
    int32_t GlyphLength(int32_t glyph_id);

    // Set the number of glyphs.
    // This method sets the number of glyphs that the builder will attempt to
    // parse location data for from the raw binary data. This method only needs
    // to be called (and <b>must</b> be) when the raw data for this builder has
    // been changed.
    void SetNumGlyphs(int32_t num_glyphs);
    int NumGlyphs();

    void Revert();
    void Clear();

    // Get the number of locations or locas. This will be one more than the
    // number of glyphs for this table since the last loca position is used to
    // indicate the size of the final glyph.
    int32_t NumLocas();
    int32_t Loca(int32_t index);

    virtual CALLER_ATTACH FontDataTable* SubBuildTable(ReadableFontData* data);
    virtual void SubDataSet();
    virtual int32_t SubDataSizeToSerialize();
    virtual bool SubReadyToSerialize();
    virtual int32_t SubSerialize(WritableFontData* new_data);

   private:
    void Init();  // short hand for common code in ctors, C++ port only
    void Initialize(ReadableFontData* data);  // ported from Java
    IntegerList* GetLocaList();

    int32_t format_version_;  // Note: IndexToLocFormat
    int32_t num_glyphs_;
    IntegerList loca_;
  };

  virtual ~LocaTable();
  int32_t NumGlyphs();

  // Return the offset for the given glyph id. Valid glyph ids are from 0 to the
  // one less than the number of glyphs. The zero entry is the special entry for
  // the notdef glyph. The final entry beyond the last glyph id is used to
  // calculate the size of the last glyph.
  // @param glyphId the glyph id to get the offset for; must be less than or
  //        equal to one more than the number of glyph ids
  // @return the offset in the glyph table to the specified glyph id
  int32_t GlyphOffset(int32_t glyph_id);

  // Get the length of the data in the glyph table for the specified glyph id.
  int32_t GlyphLength(int32_t glyph_id);

  // Get the number of locations or locas. This will be one more than the number
  // of glyphs for this table since the last loca position is used to indicate
  // the size of the final glyph.
  int32_t NumLocas();

  // Get the value from the loca table for the index specified. Valid index
  // values run from 0 to the number of glyphs in the font.
  int32_t Loca(int32_t index);

 private:
  LocaTable(Header* header, ReadableFontData* data);
  LocaTable(Header* header,
            ReadableFontData* data,
            int32_t version,
            int32_t num_glyphs);

  int32_t version_;  // Note: IndexToLocFormat
  int32_t num_glyphs_;

  friend class LocaIterator;
};
typedef Ptr<LocaTable> LocaTablePtr;
typedef Ptr<LocaTable::Builder> LocaTableBuilderPtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_LOCA_TABLE_H_

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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_HEADER_TABLE_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_HEADER_TABLE_H_

#include "sfntly/table.h"

namespace sfntly {

struct IndexToLocFormat {
  enum {
    kShortOffset = 0,
    kLongOffset = 1
  };
};

struct FontDirectionHint {
  enum {
    kFullyMixed = 0,
    kOnlyStrongLTR = 1,
    kStrongLTRAndNeutral = 2,
    kOnlyStrongRTL = -1,
    kStrongRTLAndNeutral = -2
  };
};

class FontHeaderTable : public Table, public RefCounted<FontHeaderTable> {
 private:
  struct Offset {
    enum {
      kTableVersion = 0,
      kFontRevision = 4,
      kCheckSumAdjustment = 8,
      kMagicNumber = 12,
      kFlags = 16,
      kUnitsPerEm = 18,
      kCreated = 20,
      kModified = 28,
      kXMin = 36,
      kYMin = 38,
      kXMax = 40,
      kYMax = 42,
      kMacStyle = 44,
      kLowestRecPPEM = 46,
      kFontDirectionHint = 48,
      kIndexToLocFormat = 50,
      kGlyphDataFormat = 52
    };
  };

 private:
  FontHeaderTable(Header* header, ReadableFontData* data);

 public:  // class is final, no virtual functions unless from parent
  virtual ~FontHeaderTable();
  int32_t tableVersion();
  int32_t fontRevision();

  // Get the checksum adjustment. To compute: set it to 0, sum the entire font
  // as ULONG, then store 0xB1B0AFBA - sum.
  int64_t checksumAdjustment();

  // Get the magic number. Set to 0x5F0F3CF5.
  int64_t magicNumber();

  // TODO(arthurhsu): IMPLEMENT: EnumSet<Flags>
  int32_t flagsAsInt();
  // TODO(arthurhsu): IMPLEMENT: flags() returning EnumSet<Flags>

  int32_t unitsPerEm();

  // Get the created date. Number of seconds since 12:00 midnight, January 1,
  // 1904. 64-bit integer.
  int64_t created();
  // Get the modified date. Number of seconds since 12:00 midnight, January 1,
  // 1904. 64-bit integer.
  int64_t modified();

  // Get the x min. For all glyph bounding boxes.
  int32_t xMin();
  // Get the y min. For all glyph bounding boxes.
  int32_t yMin();
  // Get the x max. For all glyph bounding boxes.
  int32_t xMax();
  // Get the y max. For all glyph bounding boxes.
  int32_t yMax();

  // TODO(arthurhsu): IMPLEMENT: EnumSet<MacStyle>
  int32_t macStyleAsInt();
  // TODO(arthurhsu): IMPLEMENT: macStyle() returning EnumSet<MacStyle>

  int32_t lowestRecPPEM();
  int32_t fontDirectionHint();  // Note: no AsInt() form, already int
  int32_t indexToLocFormat();  // Note: no AsInt() form, already int
  int32_t glyphDataFormat();

 public:
  class Builder : public Table::TableBasedTableBuilder,
                  public RefCounted<Builder> {
   public:
    // Constructor scope altered to public because C++ does not allow base
    // class to instantiate derived class with protected constructors.
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
            WritableFontData* data);
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
            ReadableFontData* data);
    virtual ~Builder();
    virtual CALLER_ATTACH FontDataTable* subBuildTable(ReadableFontData* data);

    virtual int32_t tableVersion();
    virtual void setTableVersion(int32_t version);
    virtual int32_t fontRevision();
    virtual void setFontRevision(int32_t revision);
    virtual int64_t checksumAdjustment();
    virtual void setChecksumAdjustment(int64_t adjustment);
    virtual int64_t magicNumber();
    virtual void setMagicNumber(int64_t magic_number);
    virtual int32_t flagsAsInt();
    virtual void setFlagsAsInt(int32_t flags);
    // TODO(arthurhsu): IMPLEMENT EnumSet<Flags> flags()
    // TODO(arthurhsu): IMPLEMENT setFlags(EnumSet<Flags> flags)
    virtual int32_t unitsPerEm();
    virtual void setUnitsPerEm(int32_t units);
    virtual int64_t created();
    virtual void setCreated(int64_t date);
    virtual int64_t modified();
    virtual void setModified(int64_t date);
    virtual int32_t xMin();
    virtual void setXMin(int32_t xmin);
    virtual int32_t yMin();
    virtual void setYMin(int32_t ymin);
    virtual int32_t xMax();
    virtual void setXMax(int32_t xmax);
    virtual int32_t yMax();
    virtual void setYMax(int32_t ymax);
    virtual int32_t macStyleAsInt();
    virtual void setMacStyleAsInt(int32_t style);
    // TODO(arthurhsu): IMPLEMENT EnumSet<MacStyle> macStyle()
    // TODO(arthurhsu): IMPLEMENT setMacStyle(EnumSet<MacStyle> style)
    virtual int32_t lowestRecPPEM();
    virtual void setLowestRecPPEM(int32_t size);
    virtual int32_t fontDirectionHint();
    virtual void setFontDirectionHint(int32_t hint);
    virtual int32_t indexToLocFormat();
    virtual void setIndexToLocFormat(int32_t format);
    virtual int32_t glyphDataFormat();
    virtual void setGlyphDataFormat(int32_t format);
  };
};
typedef Ptr<FontHeaderTable> FontHeaderTablePtr;
typedef Ptr<FontHeaderTable::Builder> FontHeaderTableBuilderPtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_HEADER_TABLE_H_

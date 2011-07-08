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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_GLYPH_TABLE_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_GLYPH_TABLE_H_

#include <vector>

#include "sfntly/table.h"
#include "sfntly/subtable.h"

namespace sfntly {

struct GlyphType {
  enum {
    kSimple = 0,
    kComposite = 1
  };
};

// Note: due to the complexity of this class, the order of declaration is
//       different from its Java counter part.  GlyphTable::Glyph is defined
//       before GlyphTable::Builder to avoid compilation errors.
class GlyphTable : public Table, public RefCounted<GlyphTable> {
 public:
  struct Offset {
    enum {
      // header
      kNumberOfContours = 0,
      kXMin = 2,
      kYMin = 4,
      kXMax = 6,
      kYMax = 8,

      // Simple Glyph Description
      kSimpleEndPtsOfCountours = 10,
      // offset from the end of the contours array
      kSimpleInstructionLength = 0,
      kSimpleInstructions = 2,
      // flags
      // xCoordinates
      // yCoordinates

      // Composite Glyph Description
      kCompositeFlags = 0,
      kCompositeGyphIndexWithoutFlag = 0,
      kCompositeGlyphIndexWithFlag = 2,
    };
  };

 private:
  GlyphTable(Header* header, ReadableFontData* data);

 public:
  virtual ~GlyphTable();

  class Glyph;
  Glyph* glyph(int32_t offset, int32_t length);

  class Builder;
  class Glyph : public SubTable {
   protected:
    // Note: constructor refactored in C++ to avoid heavy lifting.
    //       caller need to do data->slice(offset, length) beforehand.
    Glyph(ReadableFontData* data, int32_t glyph_type);

   private:
    static int32_t glyphType(ReadableFontData* data, int32_t offset,
                             int32_t length);

   public:
    virtual ~Glyph();
    static CALLER_ATTACH Glyph* getGlyph(ReadableFontData* data, int32_t offset,
                                         int32_t length);
    virtual int32_t glyphType();
    virtual int32_t numberOfContours();
    virtual int32_t xMin();
    virtual int32_t xMax();
    virtual int32_t yMin();
    virtual int32_t yMax();
    virtual int32_t padding();  // override FontDataTable::padding()

    virtual int32_t instructionSize() = 0;
    virtual ReadableFontData* instructions() = 0;

    // Note: Contour is an empty class for the version ported
    class Contour {
     protected:
      Contour() {}
      virtual ~Contour() {}
    };

    class Builder : public SubTable::Builder {
     protected:
      // Incoming table_builder is GlyphTable::Builder*.
      // Note: constructor refactored in C++ to avoid heavy lifting.
      //       caller need to do data->slice(offset, length) beforehand.
      Builder(FontDataTableBuilderContainer* table_builder,
              WritableFontData* data);
      Builder(FontDataTableBuilderContainer* table_builder,
              ReadableFontData* data);

     public:
      virtual ~Builder();

     protected:
      static CALLER_ATTACH Builder* getBuilder(
          FontDataTableBuilderContainer* table_builder, ReadableFontData* data);
      static CALLER_ATTACH Builder* getBuilder(
          FontDataTableBuilderContainer* table_builder, ReadableFontData* data,
          int32_t offset, int32_t length);
      virtual void subDataSet();
      virtual int32_t subDataSizeToSerialize();
      virtual bool subReadyToSerialize();
      virtual int32_t subSerialize(WritableFontData* new_data);

     protected:
      int32_t format_;
      friend class GlyphTable::Builder;
    };

   private:
    int32_t glyph_type_;
    int32_t number_of_contours_;

   protected:
    int32_t padding_;
  };

  typedef Ptr<GlyphTable::Glyph::Builder> GlyphBuilderPtr;
  typedef std::vector<GlyphBuilderPtr> GlyphBuilderList;

  class Builder : public Table::ArrayElementTableBuilder,
                  public RefCounted<GlyphTable::Builder> {
   public:
    // Note: Constructor scope altered to public for base class to instantiate.
    Builder(FontDataTableBuilderContainer* font_builder, Header* header,
            WritableFontData* data);
    virtual ~Builder();

    virtual void setLoca(const IntegerList& loca);
    virtual void generateLocaList(IntegerList* locas);

   private:
    void initialize(ReadableFontData* data, const IntegerList& loca);
    GlyphBuilderList* getGlyphBuilders();
    void revert();

   public:
    // Gets the List of glyph builders for the glyph table builder. These may be
    // manipulated in any way by the caller and the changes will be reflected in
    // the final glyph table produced.
    // If there is no current data for the glyph builder or the glyph builders
    // have not been previously set then this will return an empty glyph builder
    // List. If there is current data (i.e. data read from an existing font) and
    // the <code>loca</code> list has not been set or is null, empty, or
    // invalid, then an empty glyph builder List will be returned.
    GlyphBuilderList* glyphBuilders();

    // Replace the internal glyph builders with the one provided.
    void setGlyphBuilders(GlyphBuilderList* glyph_builders);

    // Glyph builder factories
    CALLER_ATTACH Glyph::Builder* glyphBuilder(ReadableFontData* data);

   protected:  // internal API for building
    virtual CALLER_ATTACH FontDataTable* subBuildTable(ReadableFontData* data);
    virtual void subDataSet();
    virtual int32_t subDataSizeToSerialize();
    virtual bool subReadyToSerialize();
    virtual int32_t subSerialize(WritableFontData* new_data);

   private:
    GlyphBuilderList glyph_builders_;
    IntegerList loca_;
  };

  class SimpleGlyph : public Glyph, public RefCounted<SimpleGlyph> {
   public:
    static const int32_t kFLAG_ONCURVE;
    static const int32_t kFLAG_XSHORT;
    static const int32_t kFLAG_YSHORT;
    static const int32_t kFLAG_REPEAT;
    static const int32_t kFLAG_XREPEATSIGN;
    static const int32_t kFLAG_YREPEATSIGN;

   public:
    // Note: constructor refactored in C++ to avoid heavy lifting.
    //       caller need to do data->slice(offset, length) beforehand.
    explicit SimpleGlyph(ReadableFontData* data);
    virtual ~SimpleGlyph();

   public:
    class SimpleContour : public Glyph::Contour {
     protected:
      SimpleContour() {}
      virtual ~SimpleContour() {}
    };

   private:
    void initialize();
    void parseData(bool fill_arrays);
    int32_t flagAsInt(int32_t index);
    int32_t contourEndPoint(int32_t contour);

   public:  // class is final, , no virtual functions unless from parent
    virtual int32_t instructionSize();
    virtual CALLER_ATTACH ReadableFontData* instructions();
    int32_t numberOfPoints(int32_t contour);
    int32_t xCoordinate(int32_t contour, int32_t point);
    int32_t yCoordinate(int32_t contour, int32_t point);
    bool onCurve(int32_t contour, int32_t point);

   public:
    class SimpleGlyphBuilder : public Glyph::Builder,
                               public RefCounted<SimpleGlyphBuilder> {
     public:
      virtual ~SimpleGlyphBuilder();

     protected:
      // Note: constructor refactored in C++ to avoid heavy lifting.
      //       caller need to do data->slice(offset, length) beforehand.
      SimpleGlyphBuilder(FontDataTableBuilderContainer* table_builder,
                         WritableFontData* data);
      SimpleGlyphBuilder(FontDataTableBuilderContainer* table_builder,
                         ReadableFontData* data);
      virtual CALLER_ATTACH FontDataTable*
          subBuildTable(ReadableFontData* data);
      friend class Glyph::Builder;
    };

   private:
    bool initialized_;
    int32_t instruction_size_;
    int32_t number_of_points_;

    // start offsets of the arrays
    int32_t instructions_offset_;
    int32_t flags_offset_;
    int32_t x_coordinates_offset_;
    int32_t y_coordinates_offset_;

    int32_t flag_byte_count_;
    int32_t x_byte_count_;
    int32_t y_byte_count_;

    IntegerList x_coordinates_;
    IntegerList y_coordinates_;
    std::vector<bool> on_curve_;
    IntegerList contour_index_;
  };

  class CompositeGlyph : public Glyph, public RefCounted<CompositeGlyph> {
   public:
    static const int32_t kFLAG_ARG_1_AND_2_ARE_WORDS;
    static const int32_t kFLAG_ARGS_ARE_XY_VALUES;
    static const int32_t kFLAG_ROUND_XY_TO_GRID;
    static const int32_t kFLAG_WE_HAVE_A_SCALE;
    static const int32_t kFLAG_RESERVED;
    static const int32_t kFLAG_MORE_COMPONENTS;
    static const int32_t kFLAG_WE_HAVE_AN_X_AND_Y_SCALE;
    static const int32_t kFLAG_WE_HAVE_A_TWO_BY_TWO;
    static const int32_t kFLAG_WE_HAVE_INSTRUCTIONS;
    static const int32_t kFLAG_USE_MY_METRICS;
    static const int32_t kFLAG_OVERLAP_COMPOUND;
    static const int32_t kFLAG_SCALED_COMPONENT_OFFSET;
    static const int32_t kFLAG_UNSCALED_COMPONENT_OFFSET;

   public:
    // Note: constructor refactored in C++ to avoid heavy lifting.
    //       caller need to do data->slice(offset, length) beforehand.
    explicit CompositeGlyph(ReadableFontData* data);
    virtual ~CompositeGlyph();

   private:
    void parseData();

   public:  // class is final, no virtual functions unless from parent
    int32_t flags(int32_t contour);
    int32_t numGlyphs();
    int32_t glyphIndex(int32_t contour);
    int32_t argument1(int32_t contour);
    int32_t argument2(int32_t contour);
    int32_t transformationSize(int32_t contour);
    void transformation(int32_t contour, ByteVector* transformation);
    virtual int32_t instructionSize();
    virtual CALLER_ATTACH ReadableFontData* instructions();

   public:
    class CompositeGlyphBuilder : public Glyph::Builder,
                                  public RefCounted<CompositeGlyphBuilder> {
     public:
      virtual ~CompositeGlyphBuilder();

     protected:
      // Note: constructor refactored in C++ to avoid heavy lifting.
      //       caller need to do data->slice(offset, length) beforehand.
      CompositeGlyphBuilder(FontDataTableBuilderContainer* table_builder,
                            WritableFontData* data);
      CompositeGlyphBuilder(FontDataTableBuilderContainer* table_builder,
                            ReadableFontData* data);

      virtual CALLER_ATTACH FontDataTable*
          subBuildTable(ReadableFontData* data);
      friend class Glyph::Builder;
    };

   private:
    IntegerList contour_index_;
    int32_t instruction_size_;
    int32_t instructions_offset_;
  };
};
typedef Ptr<GlyphTable> GlyphTablePtr;
typedef Ptr<GlyphTable::Builder> GlyphTableBuilderPtr;
typedef std::vector<GlyphTableBuilderPtr> GlyphTableBuilderList;
typedef Ptr<GlyphTable::Glyph> GlyphPtr;
typedef Ptr<GlyphTable::Glyph::Builder> GlyphBuilderPtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_GLYPH_TABLE_H_

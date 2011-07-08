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

#include <stdlib.h>

#include "sfntly/glyph_table.h"
#include "sfntly/port/exception_type.h"

namespace sfntly {
/******************************************************************************
 * Constants
 ******************************************************************************/
const int32_t GlyphTable::SimpleGlyph::kFLAG_ONCURVE = 1;
const int32_t GlyphTable::SimpleGlyph::kFLAG_XSHORT = 1 << 1;
const int32_t GlyphTable::SimpleGlyph::kFLAG_YSHORT = 1 << 2;
const int32_t GlyphTable::SimpleGlyph::kFLAG_REPEAT = 1 << 3;
const int32_t GlyphTable::SimpleGlyph::kFLAG_XREPEATSIGN = 1 << 4;
const int32_t GlyphTable::SimpleGlyph::kFLAG_YREPEATSIGN = 1 << 5;

const int32_t GlyphTable::CompositeGlyph::kFLAG_ARG_1_AND_2_ARE_WORDS = 1 << 0;
const int32_t GlyphTable::CompositeGlyph::kFLAG_ARGS_ARE_XY_VALUES = 1 << 1;
const int32_t GlyphTable::CompositeGlyph::kFLAG_ROUND_XY_TO_GRID = 1 << 2;
const int32_t GlyphTable::CompositeGlyph::kFLAG_WE_HAVE_A_SCALE = 1 << 3;
const int32_t GlyphTable::CompositeGlyph::kFLAG_RESERVED = 1 << 4;
const int32_t GlyphTable::CompositeGlyph::kFLAG_MORE_COMPONENTS = 1 << 5;
const int32_t GlyphTable::CompositeGlyph::kFLAG_WE_HAVE_AN_X_AND_Y_SCALE = 1 << 6;
const int32_t GlyphTable::CompositeGlyph::kFLAG_WE_HAVE_A_TWO_BY_TWO = 1 << 7;
const int32_t GlyphTable::CompositeGlyph::kFLAG_WE_HAVE_INSTRUCTIONS = 1 << 8;
const int32_t GlyphTable::CompositeGlyph::kFLAG_USE_MY_METRICS = 1 << 9;
const int32_t GlyphTable::CompositeGlyph::kFLAG_OVERLAP_COMPOUND = 1 << 10;
const int32_t GlyphTable::CompositeGlyph::kFLAG_SCALED_COMPONENT_OFFSET = 1 << 11;
const int32_t GlyphTable::CompositeGlyph::kFLAG_UNSCALED_COMPONENT_OFFSET = 1 << 12;

/******************************************************************************
 * GlyphTable class
 ******************************************************************************/
GlyphTable::GlyphTable(Header* header, ReadableFontData* data)
    : Table(header, data) {}

GlyphTable::~GlyphTable() {}

GlyphTable::Glyph* GlyphTable::glyph(int32_t offset, int32_t length) {
  return GlyphTable::Glyph::getGlyph(data_, offset, length);
}

/******************************************************************************
 * GlyphTable::Builder class
 ******************************************************************************/
GlyphTable::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                            Header* header, WritableFontData* data) :
    Table::ArrayElementTableBuilder(font_builder, header, data) {}

GlyphTable::Builder::~Builder() {}

void GlyphTable::Builder::setLoca(const IntegerList& loca) {
  loca_ = loca;
  setModelChanged(false);
  glyph_builders_.clear();
}

void GlyphTable::Builder::generateLocaList(IntegerList* locas) {
  assert(locas);
  GlyphBuilderList* glyph_builders = getGlyphBuilders();
  locas->resize(glyph_builders->size());
  locas->push_back(0);
  if (glyph_builders->size() == 0) {
    locas->push_back(0);
  } else {
    int32_t total = 0;
    for (GlyphBuilderList::iterator b = glyph_builders->begin(),
                                    b_end = glyph_builders->end();
                                    b != b_end; ++b) {
      int32_t size = (*b)->subDataSizeToSerialize();
      locas->push_back(total + size);
      total += size;
    }
  }
}

void GlyphTable::Builder::initialize(ReadableFontData* data,
                                     const IntegerList& loca) {
  if (data != NULL) {
    if (loca_.empty()) {
#if defined (SFNTLY_NO_EXCEPTION)
      return;
#else
      throw IllegalStateException(
          "Loca values not set - unable to parse glyph data.");
#endif
    }
    int32_t loca_value;
    int32_t last_loca_value = loca[0];
    for (size_t i = 1; i < loca.size(); ++i) {
      loca_value = loca[i];
      GlyphBuilderPtr builder;
      builder.attach(Glyph::Builder::getBuilder(this, data,
          last_loca_value /*offset*/, loca_value - last_loca_value /*length*/));
      glyph_builders_.push_back(builder);
      last_loca_value = loca_value;
    }
  }
}

GlyphTable::GlyphBuilderList* GlyphTable::Builder::getGlyphBuilders() {
  if (glyph_builders_.empty()) {
    initialize(internalReadData(), loca_);
    setModelChanged();
  }
  return &glyph_builders_;
}

void GlyphTable::Builder::revert() {
  glyph_builders_.clear();
  setModelChanged(false);
}

GlyphTable::GlyphBuilderList* GlyphTable::Builder::glyphBuilders() {
  return getGlyphBuilders();
}

void GlyphTable::Builder::setGlyphBuilders(GlyphBuilderList* glyph_builders) {
  glyph_builders_ = *glyph_builders;
  setModelChanged();
}

CALLER_ATTACH GlyphTable::Glyph::Builder* GlyphTable::Builder::glyphBuilder(
    ReadableFontData* data) {
  return Glyph::Builder::getBuilder(this, data);
}

CALLER_ATTACH FontDataTable* GlyphTable::Builder::subBuildTable(
    ReadableFontData* data) {
  FontDataTablePtr table = new GlyphTable(header(), data);
  return table.detach();
}

void GlyphTable::Builder::subDataSet() {
  glyph_builders_.clear();
  setModelChanged(false);
}

int32_t GlyphTable::Builder::subDataSizeToSerialize() {
  if (glyph_builders_.empty())
    return 0;

  bool variable = false;
  int32_t size = 0;

  // Calculate size of each table.
  for (GlyphBuilderList::iterator b = glyph_builders_.begin(),
                                  end = glyph_builders_.end(); b != end; ++b) {
      int32_t glyph_size = (*b)->subDataSizeToSerialize();
      size += abs(glyph_size);
      variable |= glyph_size <= 0;
  }
  return variable ? -size : size;
}

bool GlyphTable::Builder::subReadyToSerialize() {
  return !glyph_builders_.empty();
}

int32_t GlyphTable::Builder::subSerialize(WritableFontData* new_data) {
  int32_t size = 0;
  for (GlyphBuilderList::iterator b = glyph_builders_.begin(),
                                  end = glyph_builders_.end(); b != end; ++b) {
    FontDataPtr data;
    data.attach(new_data->slice(size));
    size += (*b)->subSerialize(down_cast<WritableFontData*>(data.p_));
  }
  return size;
}

/******************************************************************************
 * GlyphTable::Glyph class
 ******************************************************************************/
GlyphTable::Glyph::Glyph(ReadableFontData* data, int32_t glyph_type)
    : SubTable(data), glyph_type_(glyph_type) {
  if (data_->length() == 0) {
    number_of_contours_ = 0;
  } else {
    // -1 if composite
    number_of_contours_ = data_->readShort(Offset::kNumberOfContours);
  }
}

GlyphTable::Glyph::~Glyph() {}

int32_t GlyphTable::Glyph::glyphType(ReadableFontData* data, int32_t offset,
                                     int32_t length) {
  if (length == 0) {
    return GlyphType::kSimple;
  }
  int32_t number_of_contours = data->readShort(offset);
  if (number_of_contours >= 0) {
    return GlyphType::kSimple;
  }
  return GlyphType::kComposite;
}

CALLER_ATTACH GlyphTable::Glyph* GlyphTable::Glyph::getGlyph(
    ReadableFontData* data, int32_t offset, int32_t length) {
  int32_t type = glyphType(data, offset, length);
  GlyphPtr glyph;

  ReadableFontDataPtr sliced_data;
  sliced_data.attach(down_cast<ReadableFontData*>(data->slice(offset, length)));
  if (type == GlyphType::kSimple) {
    glyph = new SimpleGlyph(sliced_data);
  }
  glyph = new CompositeGlyph(sliced_data);
  return glyph.detach();
}

int32_t GlyphTable::Glyph::glyphType() {
  return glyph_type_;
}

int32_t GlyphTable::Glyph::numberOfContours() {
  return number_of_contours_;
}

int32_t GlyphTable::Glyph::xMin() {
  return data_->readShort(Offset::kXMin);
}

int32_t GlyphTable::Glyph::xMax() {
  return data_->readShort(Offset::kXMax);
}

int32_t GlyphTable::Glyph::yMin() {
  return data_->readShort(Offset::kYMin);
}

int32_t GlyphTable::Glyph::yMax() {
  return data_->readShort(Offset::kYMax);
}

int32_t GlyphTable::Glyph::padding() {
  return padding_;
}

/******************************************************************************
 * GlyphTable::Glyph::Builder class
 ******************************************************************************/
GlyphTable::Glyph::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                                    WritableFontData* data) :
    SubTable::Builder(font_builder, data) {
}

GlyphTable::Glyph::Builder::Builder(FontDataTableBuilderContainer* font_builder,
                                    ReadableFontData* data) :
    SubTable::Builder(font_builder, data) {
}

GlyphTable::Glyph::Builder::~Builder() {}

CALLER_ATTACH GlyphTable::Glyph::Builder*
    GlyphTable::Glyph::Builder::getBuilder(
        FontDataTableBuilderContainer* table_builder, ReadableFontData* data) {
  return getBuilder(table_builder, data, 0, data->length());
}

CALLER_ATTACH GlyphTable::Glyph::Builder*
    GlyphTable::Glyph::Builder::getBuilder(
        FontDataTableBuilderContainer* table_builder, ReadableFontData* data,
        int32_t offset, int32_t length) {
  int32_t type = Glyph::glyphType(data, offset, length);
  GlyphBuilderPtr builder;
  ReadableFontDataPtr sliced_data;
  sliced_data.attach(down_cast<ReadableFontData*>(data->slice(offset, length)));
  if (type == GlyphType::kSimple) {
    builder = new SimpleGlyph::SimpleGlyphBuilder(table_builder, sliced_data);
  } else {
    builder = new CompositeGlyph::CompositeGlyphBuilder(table_builder,
                                                        sliced_data);
  }
  return builder.detach();
}

void GlyphTable::Glyph::Builder::subDataSet() {
  // NOP
}

int32_t GlyphTable::Glyph::Builder::subDataSizeToSerialize() {
  return internalReadData()->length();
}

bool GlyphTable::Glyph::Builder::subReadyToSerialize() {
  return true;
}

int32_t GlyphTable::Glyph::Builder::subSerialize(WritableFontData* new_data) {
  return internalReadData()->copyTo(new_data);
}

/******************************************************************************
 * GlyphTable::SimpleGlyph and its builder
 ******************************************************************************/
GlyphTable::SimpleGlyph::SimpleGlyph(ReadableFontData* data)
    : GlyphTable::Glyph(data, GlyphType::kSimple) {
}

GlyphTable::SimpleGlyph::~SimpleGlyph() {}

void GlyphTable::SimpleGlyph::initialize() {
  if (initialized_) {
    return;
  }

  if (readFontData()->length() == 0) {
    instruction_size_ = 0;
    number_of_points_ = 0;
    instructions_offset_ = 0;
    flags_offset_ = 0;
    x_coordinates_offset_ = 0;
    y_coordinates_offset_ = 0;
    return;
  }

  instruction_size_ = data_->readUShort(Offset::kSimpleEndPtsOfCountours +
      numberOfContours() * DataSize::kUSHORT);
  instructions_offset_ = Offset::kSimpleEndPtsOfCountours +
      (numberOfContours() + 1) * DataSize::kUSHORT;
  flags_offset_ = instructions_offset_ + instruction_size_ * DataSize::kBYTE;
  number_of_points_ = contourEndPoint(numberOfContours() - 1) + 1;
  x_coordinates_.resize(number_of_points_);
  y_coordinates_.resize(number_of_points_);
  on_curve_.resize(number_of_points_);
  parseData(false);
  x_coordinates_offset_ = flags_offset_ + flag_byte_count_ * DataSize::kBYTE;
  y_coordinates_offset_ = x_coordinates_offset_ + x_byte_count_ *
      DataSize::kBYTE;
  contour_index_.resize(numberOfContours() + 1);
  contour_index_[0] = 0;
  for (uint32_t contour = 0; contour < contour_index_.size() - 1; ++contour) {
    contour_index_[contour + 1] = contourEndPoint(contour) + 1;
  }
  parseData(true);
  int32_t non_padded_data_length =
    5 * DataSize::kSHORT +
    (numberOfContours() * DataSize::kUSHORT) +
    DataSize::kUSHORT +
    (instruction_size_ * DataSize::kBYTE) +
    (flag_byte_count_ * DataSize::kBYTE) +
    (x_byte_count_ * DataSize::kBYTE) +
    (y_byte_count_ * DataSize::kBYTE);
  padding_ = length() - non_padded_data_length;
  initialized_ = true;
}

void GlyphTable::SimpleGlyph::parseData(bool fill_arrays) {
  int32_t flag = 0;
  int32_t flag_repeat = 0;
  int32_t flag_index = 0;
  int32_t x_byte_index = 0;
  int32_t y_byte_index = 0;

  for (int32_t point_index = 0; point_index < number_of_points_;
       ++point_index) {
    // get the flag for the current point
    if (flag_repeat == 0) {
      flag = flagAsInt(flag_index++);
      if ((flag & kFLAG_REPEAT) == kFLAG_REPEAT) {
        flag_repeat = flagAsInt(flag_index++);
      }
    } else {
      flag_repeat--;
    }

    // on the curve?
    if (fill_arrays) {
      on_curve_[point_index] = ((flag & kFLAG_ONCURVE) == kFLAG_ONCURVE);
    }
    // get the x coordinate
    if ((flag & kFLAG_XSHORT) == kFLAG_XSHORT) {
      // single byte x coord value
      if (fill_arrays) {
        x_coordinates_[point_index] =
            data_->readUByte(x_coordinates_offset_ + x_byte_index);
        x_coordinates_[point_index] *=
            ((flag & kFLAG_XREPEATSIGN) == kFLAG_XREPEATSIGN) ? 1 : -1;
      }
      x_byte_index++;
    } else {
      // double byte coord value
      if (!((flag & kFLAG_XREPEATSIGN) == kFLAG_XREPEATSIGN)) {
        if (fill_arrays) {
          x_coordinates_[point_index] =
            data_->readShort(x_coordinates_offset_ + x_byte_index);
        }
        x_byte_index += 2;
      }
    }
    if (fill_arrays && point_index > 0) {
      x_coordinates_[point_index] += x_coordinates_[point_index - 1];
    }

    // get the y coordinate
    if ((flag & kFLAG_YSHORT) == kFLAG_YSHORT) {
      if (fill_arrays) {
        y_coordinates_[point_index] =
          data_->readUByte(y_coordinates_offset_ + y_byte_index);
        y_coordinates_[point_index] *=
          ((flag & kFLAG_YREPEATSIGN) == kFLAG_YREPEATSIGN) ? 1 : -1;
      }
      y_byte_index++;
    } else {
      if (!((flag & kFLAG_YREPEATSIGN) == kFLAG_YREPEATSIGN)) {
        if (fill_arrays) {
          y_coordinates_[point_index] =
            data_->readShort(y_coordinates_offset_ + y_byte_index);
        }
        y_byte_index += 2;
      }
    }
    if (fill_arrays && point_index > 0) {
      y_coordinates_[point_index] += y_coordinates_[point_index - 1];
    }
  }
  flag_byte_count_ = flag_index;
  x_byte_count_ = x_byte_index;
  y_byte_count_ = y_byte_index;
}

int32_t GlyphTable::SimpleGlyph::flagAsInt(int32_t index) {
  return data_->readUByte(flags_offset_ + index * DataSize::kBYTE);
}

int32_t GlyphTable::SimpleGlyph::contourEndPoint(int32_t contour) {
  return data_->readUShort(contour * DataSize::kUSHORT +
                           Offset::kSimpleEndPtsOfCountours);
}

int32_t GlyphTable::SimpleGlyph::instructionSize() {
  initialize();
  return instruction_size_;
}

CALLER_ATTACH ReadableFontData* GlyphTable::SimpleGlyph::instructions() {
  initialize();
  return down_cast<ReadableFontData*>(
             data_->slice(instructions_offset_, instructionSize()));
}

int32_t GlyphTable::SimpleGlyph::numberOfPoints(int32_t contour) {
  initialize();
  if (contour >= numberOfContours()) {
    return 0;
  }
  return contour_index_[contour + 1] - contour_index_[contour];
}

int32_t GlyphTable::SimpleGlyph::xCoordinate(int32_t contour, int32_t point) {
  initialize();
  return x_coordinates_[contour_index_[contour] + point];
}

int32_t GlyphTable::SimpleGlyph::yCoordinate(int32_t contour, int32_t point) {
  initialize();
  return y_coordinates_[contour_index_[contour] + point];
}

bool GlyphTable::SimpleGlyph::onCurve(int32_t contour, int32_t point) {
  initialize();
  return on_curve_[contour_index_[contour] + point];
}

GlyphTable::SimpleGlyph::SimpleGlyphBuilder::SimpleGlyphBuilder(
    FontDataTableBuilderContainer* table_builder, WritableFontData* data) :
    Glyph::Builder(table_builder, data) {
}

GlyphTable::SimpleGlyph::SimpleGlyphBuilder::SimpleGlyphBuilder(
    FontDataTableBuilderContainer* table_builder, ReadableFontData* data) :
    Glyph::Builder(table_builder, data) {
}

GlyphTable::SimpleGlyph::SimpleGlyphBuilder::~SimpleGlyphBuilder() {}

CALLER_ATTACH FontDataTable*
    GlyphTable::SimpleGlyph::SimpleGlyphBuilder::subBuildTable(
        ReadableFontData* data) {
  FontDataTablePtr table = new SimpleGlyph(data);
  return table.detach();
}

/******************************************************************************
 * GlyphTable::CompositeGlyph and its builder
 ******************************************************************************/
GlyphTable::CompositeGlyph::CompositeGlyph(ReadableFontData* data)
    : GlyphTable::Glyph(data, GlyphType::kComposite),
      instruction_size_(0), instructions_offset_(0) {
  parseData();
}

GlyphTable::CompositeGlyph::~CompositeGlyph() {}

void GlyphTable::CompositeGlyph::parseData() {
  int32_t index = 5 * DataSize::kUSHORT;
  int32_t flags = kFLAG_MORE_COMPONENTS;

  while ((flags & kFLAG_MORE_COMPONENTS) == kFLAG_MORE_COMPONENTS) {
    contour_index_.push_back(index);
    flags = data_->readUShort(index);
    index += 2 * DataSize::kUSHORT;  // flags and glyphIndex
    if ((flags & kFLAG_ARG_1_AND_2_ARE_WORDS) == kFLAG_ARG_1_AND_2_ARE_WORDS) {
      index += 2 * DataSize::kSHORT;
    } else {
      index += 2 * DataSize::kBYTE;
    }
    if ((flags & kFLAG_WE_HAVE_A_SCALE) == kFLAG_WE_HAVE_A_SCALE) {
      index += DataSize::kF2DOT14;
    } else if ((flags & kFLAG_WE_HAVE_AN_X_AND_Y_SCALE) ==
                        kFLAG_WE_HAVE_AN_X_AND_Y_SCALE) {
      index += 2 * DataSize::kF2DOT14;
    } else if ((flags & kFLAG_WE_HAVE_A_TWO_BY_TWO) ==
                        kFLAG_WE_HAVE_A_TWO_BY_TWO) {
      index += 4 * DataSize::kF2DOT14;
    }
    int32_t non_padded_data_length = index;
    if ((flags & kFLAG_WE_HAVE_INSTRUCTIONS) == kFLAG_WE_HAVE_INSTRUCTIONS) {
      instruction_size_ = data_->readUShort(index);
      index += DataSize::kUSHORT;
      instructions_offset_ = index;
      non_padded_data_length = index + (instruction_size_ * DataSize::kBYTE);
    }
    padding_ = length() - non_padded_data_length;
  }
}

int32_t GlyphTable::CompositeGlyph::flags(int32_t contour) {
  return data_->readUShort(contour_index_[contour]);
}

int32_t GlyphTable::CompositeGlyph::numGlyphs() {
  return contour_index_.size();
}

int32_t GlyphTable::CompositeGlyph::glyphIndex(int32_t contour) {
  return data_->readUShort(DataSize::kUSHORT + contour_index_[contour]);
}

int32_t GlyphTable::CompositeGlyph::argument1(int32_t contour) {
  int32_t index = 2 * DataSize::kUSHORT + contour_index_[contour];
  int32_t contour_flags = flags(contour);
  if ((contour_flags & kFLAG_ARG_1_AND_2_ARE_WORDS) ==
                       kFLAG_ARG_1_AND_2_ARE_WORDS) {
    return data_->readUShort(index);
  }
  return data_->readByte(index);
}

int32_t GlyphTable::CompositeGlyph::argument2(int32_t contour) {
  int32_t index = 2 * DataSize::kUSHORT + contour_index_[contour];
  int32_t contour_flags = flags(contour);
  if ((contour_flags & kFLAG_ARG_1_AND_2_ARE_WORDS) ==
                       kFLAG_ARG_1_AND_2_ARE_WORDS) {
    return data_->readUShort(index + DataSize::kUSHORT);
  }
  return data_->readByte(index + DataSize::kUSHORT);
}

int32_t GlyphTable::CompositeGlyph::transformationSize(int32_t contour) {
  int32_t contour_flags = flags(contour);
  if ((contour_flags & kFLAG_WE_HAVE_A_SCALE) == kFLAG_WE_HAVE_A_SCALE) {
      return DataSize::kF2DOT14;
    } else if ((contour_flags & kFLAG_WE_HAVE_AN_X_AND_Y_SCALE) ==
                                kFLAG_WE_HAVE_AN_X_AND_Y_SCALE) {
      return 2 * DataSize::kF2DOT14;
    } else if ((contour_flags & kFLAG_WE_HAVE_A_TWO_BY_TWO) ==
                                kFLAG_WE_HAVE_A_TWO_BY_TWO) {
      return 4 * DataSize::kF2DOT14;
    }
    return 0;
}

void GlyphTable::CompositeGlyph::transformation(int32_t contour,
                                                ByteVector* transformation) {
  int32_t contour_flags = flags(contour);
  int32_t index = contour_index_[contour] + 2 * DataSize::kUSHORT;
  if ((contour_flags & kFLAG_ARG_1_AND_2_ARE_WORDS) ==
                       kFLAG_ARG_1_AND_2_ARE_WORDS) {
    index += 2 * DataSize::kSHORT;
  } else {
    index += 2 * DataSize::kBYTE;
  }
  int32_t tsize = transformationSize(contour);
  transformation->resize(tsize);
  data_->readBytes(index, transformation, 0, tsize);
}

int32_t GlyphTable::CompositeGlyph::instructionSize() {
  return instruction_size_;
}

CALLER_ATTACH ReadableFontData* GlyphTable::CompositeGlyph::instructions() {
  return down_cast<ReadableFontData*>(
             data_->slice(instructions_offset_, instructionSize()));
}

GlyphTable::CompositeGlyph::CompositeGlyphBuilder::CompositeGlyphBuilder(
    FontDataTableBuilderContainer* table_builder, WritableFontData* data) :
    Glyph::Builder(table_builder, data) {
}

GlyphTable::CompositeGlyph::CompositeGlyphBuilder::CompositeGlyphBuilder(
    FontDataTableBuilderContainer* table_builder, ReadableFontData* data) :
    Glyph::Builder(table_builder, data) {
}

GlyphTable::CompositeGlyph::CompositeGlyphBuilder::~CompositeGlyphBuilder() {}

CALLER_ATTACH FontDataTable*
    GlyphTable::CompositeGlyph::CompositeGlyphBuilder::subBuildTable(
        ReadableFontData* data) {
  FontDataTablePtr table = new CompositeGlyph(data);
  return table.detach();
}

}  // namespace sfntly

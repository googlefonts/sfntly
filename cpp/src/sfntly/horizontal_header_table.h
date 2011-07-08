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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_HORIZONTAL_HEADER_TABLE_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_HORIZONTAL_HEADER_TABLE_H_

#include "sfntly/table.h"

namespace sfntly {

class HorizontalHeaderTable : public Table,
                              public RefCounted<HorizontalHeaderTable> {
 private:
  struct Offset {
    enum {
      kVersion = 0,
      kAscender = 4,
      kDescender = 6,
      kLineGap = 8,
      kAdvanceWidthMax = 10,
      kMinLeftSideBearing = 12,
      kMinRightSideBearing = 14,
      kXMaxExtent = 16,
      kCaretSlopeRise = 18,
      kCaretSlopeRun = 20,
      kCaretOffset = 22,
      kMetricDataFormat = 32,
      kNumberOfHMetrics = 34,
    };
  };

 private:
  HorizontalHeaderTable(Header* header, ReadableFontData* data);

 public:
  virtual ~HorizontalHeaderTable();
  int32_t version();
  int32_t ascender();
  int32_t descender();
  int32_t lineGap();
  int32_t advanceWidthMax();
  int32_t minLeftSideBearing();
  int32_t minRightSideBearing();
  int32_t xMaxExtent();
  int32_t caretSlopeRise();
  int32_t caretSlopeRun();
  int32_t caretOffset();
  int32_t metricDataFormat();
  int32_t numberOfHMetrics();

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

    int32_t version();
    void setVersion(int32_t version);
    int32_t ascender();
    void setAscender(int32_t ascender);
    int32_t descender();
    void setDescender(int32_t descender);
    int32_t lineGap();
    void setLineGap(int32_t line_gap);
    int32_t advanceWidthMax();
    void setAdvanceWidthMax(int32_t value);
    int32_t minLeftSideBearing();
    void setMinLeftSideBearing(int32_t value);
    int32_t minRightSideBearing();
    void setMinRightSideBearing(int32_t value);
    int32_t xMaxExtent();
    void setXMaxExtent(int32_t value);
    int32_t caretSlopeRise();
    void setCaretSlopeRise(int32_t value);
    int32_t caretSlopeRun();
    void setCaretSlopeRun(int32_t value);
    int32_t caretOffset();
    void setCaretOffset(int32_t value);
    int32_t metricDataFormat();
    void setMetricDataFormat(int32_t value);
    int32_t numberOfHMetrics();
    void setNumberOfHMetrics(int32_t value);
  };
};
typedef Ptr<HorizontalHeaderTable> HorizontalHeaderTablePtr;
typedef Ptr<HorizontalHeaderTable::Builder> HorizontalHeaderTableBuilderPtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_HORIZONTAL_HEADER_TABLE_H_

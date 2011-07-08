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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_MAXIMUM_PROFILE_TABLE_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_MAXIMUM_PROFILE_TABLE_H_

#include "sfntly/port/refcount.h"
#include "sfntly/table.h"

namespace sfntly {

class MaximumProfileTable : public Table,
                            public RefCounted<MaximumProfileTable> {
 private:
  struct Offset {
    enum {
      // version 0.5 and 1.0
      kVersion = 0,
      kNumGlyphs = 4,

      // version 1.0
      kMaxPoints = 6,
      kMaxContours = 8,
      kMaxCompositePoints = 10,
      kMaxCompositeContours = 12,
      kMaxZones = 14,
      kMaxTwilightPoints = 16,
      kMaxStorage = 18,
      kMaxFunctionDefs = 20,
      kMaxInstructionDefs = 22,
      kMaxStackElements = 24,
      kMaxSizeOfInstructions = 26,
      kMaxComponentElements = 28,
      kMaxComponentDepth = 30,
    };
  };

 private:
  MaximumProfileTable(Header* header, ReadableFontData* data);

 public:  // Class is final, no virtual functions unless derived from parent.
  virtual ~MaximumProfileTable();
  int32_t version();
  int32_t numGlyphs();
  int32_t maxPoints();
  int32_t maxContours();
  int32_t maxCompositePoints();
  int32_t maxZones();
  int32_t maxTwilightPoints();
  int32_t maxStorage();
  int32_t maxFunctionDefs();
  int32_t maxStackElements();
  int32_t maxSizeOfInstructions();
  int32_t maxComponentElements();
  int32_t maxComponentDepth();

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

   public:  // Class is static, no virtual functions unless derived from parent.
    int32_t version();
    void setVersion(int32_t version);
    int32_t numGlyphs();
    void setNumGlyphs(int32_t num_glyphs);
    int32_t maxPoints();
    void setMaxPoints(int32_t max_points);
    int32_t maxContours();
    void setMaxContours(int32_t max_contours);
    int32_t maxCompositePoints();
    void setMaxCompositePoints(int32_t max_composite_points);
    int32_t maxZones();
    void setMaxZones(int32_t max_zones);
    int32_t maxTwilightPoints();
    void setMaxTwilightPoints(int32_t max_twilight_points);
    int32_t maxStorage();
    void setMaxStorage(int32_t max_storage);
    int32_t maxFunctionDefs();
    void setMaxFunctionDefs(int32_t max_function_defs);
    int32_t maxStackElements();
    void setMaxStackElements(int32_t max_stack_elements);
    int32_t maxSizeOfInstructions();
    void setMaxSizeOfInstructions(int32_t max_size_of_instructions);
    int32_t maxComponentElements();
    void setMaxComponentElements(int32_t max_component_elements);
    int32_t maxComponentDepth();
    void setMaxComponentDepth(int32_t max_component_depth);
  };
};
typedef Ptr<MaximumProfileTable> MaximumProfileTablePtr;
typedef Ptr<MaximumProfileTable::Builder> MaximumProfileTableBuilderPtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_MAXIMUM_PROFILE_TABLE_H_

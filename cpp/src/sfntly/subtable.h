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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_SUBTABLE_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_SUBTABLE_H_

#include "sfntly/font_data_table.h"

namespace sfntly {

// An abstract base class for subtables. Subtables are smaller tables nested
// within other tables and don't have an entry in the main font index. Examples
// of these are the CMap subtables within CMap table (cmap) or a glyph within
// the glyph table (glyf).
class SubTable : public FontDataTable {
 protected:
  // Note: constructor refactored in C++ to avoid heavy lifting.
  //       caller need to do data->slice(offset, length) beforehand.
  explicit SubTable(ReadableFontData* data);

 public:
  virtual ~SubTable();

 public:
  class Builder : public FontDataTable::Builder {
   protected:
    Builder(FontDataTableBuilderContainer* container, WritableFontData* data);
    Builder(FontDataTableBuilderContainer* container, ReadableFontData* data);

   public:
    virtual ~Builder();
  };
};

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_SUBTABLE_H_

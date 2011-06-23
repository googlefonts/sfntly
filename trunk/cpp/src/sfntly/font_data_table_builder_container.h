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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_DATA_TABLE_BUILDER_CONTAINER_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_DATA_TABLE_BUILDER_CONTAINER_H_

#include "sfntly/data/writable_font_data.h"

namespace sfntly {

class FontDataTableBuilderContainer {
 public:
  virtual WritableFontData* getNewData(int32_t size) = 0;
  // Make gcc -Wnon-virtual-dtor happy.
  virtual ~FontDataTableBuilderContainer() {}
};
typedef Ptr<FontDataTableBuilderContainer> FontDataTableBuilderContainerPtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_FONT_DATA_TABLE_BUILDER_CONTAINER_H_

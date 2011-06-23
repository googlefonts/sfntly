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

#include <limits.h>
#include <algorithm>
#include <functional>

#include "sfntly/data/font_data.h"

namespace sfntly {

const int32_t DataSize::kBYTE = 1;
const int32_t DataSize::kCHAR = 1;
const int32_t DataSize::kUSHORT = 2;
const int32_t DataSize::kSHORT = 2;
const int32_t DataSize::kUINT24 = 3;
const int32_t DataSize::kULONG = 4;
const int32_t DataSize::kLONG = 4;
const int32_t DataSize::kFixed = 4;
const int32_t DataSize::kFUNIT = 4;
const int32_t DataSize::kFWORD = 2;
const int32_t DataSize::kUFWORD = 2;
const int32_t DataSize::kF2DOT14 = 2;
const int32_t DataSize::kLONGDATETIME = 8;
const int32_t DataSize::kTag = 4;
const int32_t DataSize::kGlyphID = 2;
const int32_t DataSize::kOffset = 2;

FontData::~FontData() {}

void FontData::init(ByteArray* ba) {
  array_ = ba;
  bound_offset_ = 0;
  bound_length_ = INT_MAX;
}

FontData::FontData(ByteArray* ba) {
  init(ba);
}

FontData::FontData(FontData* data, int32_t offset, int32_t length) {
  init(data->array_);
  bound(data->bound_offset_ + offset, length);
}

FontData::FontData(FontData* data, int32_t offset) {
  init(data->array_);
  bound(offset);
}

bool FontData::bound(int32_t offset, int32_t length) {
  if (offset + length > size() || offset < 0 || length < 0)
    return false;

  bound_offset_ += offset;
  bound_length_ = length;
  return true;
}

bool FontData::bound(int32_t offset) {
if (offset > size() || offset < 0)
    return false;

  bound_offset_ += offset;
  return true;
}

int32_t FontData::length() const {
  return std::min<int32_t>(array_->length() - bound_offset_, bound_length_);
}

int32_t FontData::size() const {
  return std::min<int32_t>(array_->size() - bound_offset_, bound_length_);
}

int32_t FontData::boundOffset(int32_t offset) {
  return offset + bound_offset_;
}

int32_t FontData::boundLength(int32_t offset, int32_t length) {
  return std::min<int32_t>(length, bound_length_ - offset);
}

}  // namespace sfntly

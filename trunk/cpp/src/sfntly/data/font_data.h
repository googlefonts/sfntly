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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_FONT_DATA_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_FONT_DATA_H_

#include <vector>
#include "sfntly/port/type.h"
#include "sfntly/data/byte_array.h"
#include "sfntly/port/refcount.h"

namespace sfntly {

struct DataSize {
  static const int32_t kBYTE;
  static const int32_t kCHAR;
  static const int32_t kUSHORT;
  static const int32_t kSHORT;
  static const int32_t kUINT24;
  static const int32_t kULONG;
  static const int32_t kLONG;
  static const int32_t kFixed;
  static const int32_t kFUNIT;
  static const int32_t kFWORD;
  static const int32_t kUFWORD;
  static const int32_t kF2DOT14;
  static const int32_t kLONGDATETIME;
  static const int32_t kTag;
  static const int32_t kGlyphID;
  static const int32_t kOffset;
};

class FontData : virtual public RefCount {
 public:
  // Get the maximum size of the FontData. This is the maximum number of bytes
  // that the font data can hold and all of it may not be filled with data or
  // even fully allocated yet.
  // @return the size of this array
  virtual int32_t size() const;

  // Sets limits on the size of the FontData. The font data is
  // @param offset the start of the new bounds
  // @param length the number of bytes in the bounded array
  // @return true if the bounding range was successful; false otherwise
  virtual bool bound(int32_t offset, int32_t length);

  // Sets limits on the size of the FontData. This is a offset bound only so if
  // the FontData is writable and growable then there is no limit to that growth
  // from the bounding operation.
  // @param offset the start of the new bounds which must be within the current
  //        size of the FontData
  // @return true if the bounding range was successful; false otherwise
  virtual bool bound(int32_t offset);

  // Makes a slice of this FontData. The returned slice will share the data with
  // the original FontData.
  // @param offset the start of the slice
  // @param length the number of bytes in the slice
  // @return a slice of the original FontData
  virtual CALLER_ATTACH FontData* slice(int32_t offset, int32_t length) = 0;

  // Makes a bottom bound only slice of this array. The returned slice will
  // share the data with the original FontData.
  // @param offset the start of the slice
  // @return a slice of the original FontData
  virtual CALLER_ATTACH FontData* slice(int32_t offset) = 0;

  // Get the length of the data.
  virtual int32_t length() const;


 protected:
  virtual ~FontData();

  // Constructor.
  // @param ba the byte array to use for the backing data
  explicit FontData(ByteArray* ba);

  // Constructor.
  // @param data the data to wrap
  // @param offset the offset to start the wrap from
  // @param length the length of the data wrapped
  FontData(FontData* data, int32_t offset, int32_t length);

  // Constructor.
  // @param data the data to wrap
  // @param offset the offset to start the wrap from
  FontData(FontData* data, int32_t offset);

  void init(ByteArray* ba);
  int32_t boundOffset(int32_t offset);
  int32_t boundLength(int32_t offset, int32_t length);

 protected:
  ByteArrayPtr array_;

 private:
  int32_t bound_offset_;
  int32_t bound_length_;
};
typedef Ptr<FontData> FontDataPtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_FONT_DATA_H_

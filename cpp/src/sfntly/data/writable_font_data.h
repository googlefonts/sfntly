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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_WRITABLE_FONT_DATA_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_WRITABLE_FONT_DATA_H_

#include "sfntly/data/readable_font_data.h"

namespace sfntly {

class WritableFontData : public ReadableFontData {
 public:
  explicit WritableFontData(ByteArray* ba);
  virtual ~WritableFontData();

  // Constructs a writable font data object. If the length is specified as
  // positive then a fixed size font data object will be created. If the length
  // is zero or less then a growable font data object will be created and the
  // size will be used as an estimate to help in allocating the original space.
  //
  // @param length if length > 0 create a fixed length font data; otherwise
  //        create a growable font data
  // @return a new writable font data
  static CALLER_ATTACH WritableFontData* CreateWritableFontData(int32_t length);

  // Constructs a writable font data object. The new font data object will wrap
  // the bytes passed in to the factory and it will take make a copy of those
  // bytes.
  //
  // @param b the byte vector to wrap
  // @return a new writable font data
  static CALLER_ATTACH WritableFontData* CreateWritableFontData(ByteVector* b);

  virtual int32_t WriteByte(int32_t index, byte_t b);
  virtual int32_t WriteBytes(int32_t offset,
                             ByteVector* b,
                             int32_t index,
                             int32_t length);
  virtual int32_t WriteBytes(int32_t index, ByteVector* b);
  virtual int32_t WriteChar(int32_t index, byte_t c);
  virtual int32_t WriteUShort(int32_t index, int32_t us);
  virtual int32_t WriteUShortLE(int32_t index, int32_t us);
  virtual int32_t WriteShort(int32_t index, int32_t s);
  virtual int32_t WriteUInt24(int32_t index, int32_t ui);
  virtual int32_t WriteULong(int32_t index, int64_t ul);
  virtual int32_t WriteULongLE(int32_t index, int64_t ul);
  virtual int32_t WriteLong(int32_t index, int64_t l);
  virtual int32_t WriteFixed(int32_t index, int32_t l);
  virtual int32_t WriteDateTime(int32_t index, int64_t date);

  // Makes a slice of this FontData. The returned slice will share the data with
  // the original FontData.
  // @param offset the start of the slice
  // @param length the number of bytes in the slice
  // @return a slice of the original FontData
  virtual CALLER_ATTACH FontData* Slice(int32_t offset, int32_t length);

  // Makes a bottom bound only slice of this array. The returned slice will
  // share the data with the original FontData.
  // @param offset the start of the slice
  // @return a slice of the original FontData
  virtual CALLER_ATTACH FontData* Slice(int32_t offset);

 private:
  WritableFontData(WritableFontData* data, int32_t offset);
  WritableFontData(WritableFontData* data, int32_t offset, int32_t length);
};
typedef Ptr<WritableFontData> WritableFontDataPtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_WRITABLE_FONT_DATA_H_

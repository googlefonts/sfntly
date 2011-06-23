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

 private:
  WritableFontData(WritableFontData* data, int32_t offset);
  WritableFontData(WritableFontData* data, int32_t offset, int32_t length);

 public:
  virtual int32_t writeByte(int32_t index, byte_t b);
  virtual int32_t writeBytes(int32_t offset, ByteVector* b, int32_t index,
                             int32_t length);
  virtual int32_t writeBytes(int32_t index, ByteVector* b);
  virtual int32_t writeChar(int32_t index, byte_t c);
  virtual int32_t writeUShort(int32_t index, int32_t us);
  virtual int32_t writeUShortLE(int32_t index, int32_t us);
  virtual int32_t writeShort(int32_t index, int32_t s);
  virtual int32_t writeUInt24(int32_t index, int32_t ui);
  virtual int32_t writeULong(int32_t index, int64_t ul);
  virtual int32_t writeULongLE(int32_t index, int64_t ul);
  virtual int32_t writeLong(int32_t index, int64_t l);
  virtual int32_t writeFixed(int32_t index, int32_t l);
  virtual int32_t writeDateTime(int32_t index, int64_t date);

  // Makes a slice of this FontData. The returned slice will share the data with
  // the original FontData.
  // @param offset the start of the slice
  // @param length the number of bytes in the slice
  // @return a slice of the original FontData
  virtual CALLER_ATTACH FontData* slice(int32_t offset, int32_t length);

  // Makes a bottom bound only slice of this array. The returned slice will
  // share the data with the original FontData.
  // @param offset the start of the slice
  // @return a slice of the original FontData
  virtual CALLER_ATTACH FontData* slice(int32_t offset);
};
typedef Ptr<WritableFontData> WritableFontDataPtr;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_WRITABLE_FONT_DATA_H_

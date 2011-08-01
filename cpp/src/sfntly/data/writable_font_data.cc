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

#include "sfntly/data/writable_font_data.h"

namespace sfntly {

WritableFontData::WritableFontData(ByteArray* ba) : ReadableFontData(ba) {
}

WritableFontData::~WritableFontData() {}

int32_t WritableFontData::WriteByte(int32_t index, byte_t b) {
  array_->Put(BoundOffset(index), b);
  return 1;
}

int32_t WritableFontData::WriteBytes(int32_t offset,
                                     ByteVector* b,
                                     int32_t index,
                                     int32_t length) {
  return array_->Put(BoundOffset(offset),
                     b,
                     index,
                     BoundLength(offset, length));
}

int32_t WritableFontData::WriteBytes(int32_t index, ByteVector* b) {
  return WriteBytes(index, b, 0, b->size());
}

int32_t WritableFontData::WriteChar(int32_t index, byte_t c) {
  return WriteByte(index, c);
}

int32_t WritableFontData::WriteUShort(int32_t index, int32_t us) {
  WriteByte(index, (byte_t)((us >> 8) & 0xff));
  WriteByte(index + 1, (byte_t)(us & 0xff));
  return 2;
}

int32_t WritableFontData::WriteUShortLE(int32_t index, int32_t us) {
  WriteByte(index, (byte_t)(us & 0xff));
  WriteByte(index + 1, (byte_t)((us >> 8) & 0xff));
  return 2;
}

int32_t WritableFontData::WriteShort(int32_t index, int32_t s) {
  return WriteUShort(index, s);
}

int32_t WritableFontData::WriteUInt24(int32_t index, int32_t ui) {
  WriteByte(index, (byte_t)((ui >> 16) & 0xff));
  WriteByte(index + 1, (byte_t)((ui >> 8) & 0xff));
  WriteByte(index + 2, (byte_t)(ui & 0xff));
  return 3;
}

int32_t WritableFontData::WriteULong(int32_t index, int64_t ul) {
  WriteByte(index, (byte_t)((ul >> 24) & 0xff));
  WriteByte(index + 1, (byte_t)((ul >> 16) & 0xff));
  WriteByte(index + 2, (byte_t)((ul >> 8) & 0xff));
  WriteByte(index + 3, (byte_t)(ul & 0xff));
  return 4;
}

int32_t WritableFontData::WriteULongLE(int32_t index, int64_t ul) {
  WriteByte(index, (byte_t)(ul & 0xff));
  WriteByte(index + 1, (byte_t)((ul >> 8) & 0xff));
  WriteByte(index + 2, (byte_t)((ul >> 16) & 0xff));
  WriteByte(index + 3, (byte_t)((ul >> 24) & 0xff));
  return 4;
}

int32_t WritableFontData::WriteLong(int32_t index, int64_t l) {
  return WriteULong(index, l);
}

int32_t WritableFontData::WriteFixed(int32_t index, int32_t l) {
  return WriteLong(index, l);
}

int32_t WritableFontData::WriteDateTime(int32_t index, int64_t date) {
  WriteULong(index, (date >> 32) & 0xffffffff);
  WriteULong(index + 4, date & 0xffffffff);
  return 8;
}

CALLER_ATTACH FontData* WritableFontData::Slice(int32_t offset,
                                                int32_t length) {
  if (offset < 0 || offset + length > Size()) {
    return NULL;
  }
  FontDataPtr slice = new WritableFontData(this, offset, length);
  // Note: exception not ported because the condition is always false in C++.
  // if (slice == null) { throw new IndexOutOfBoundsException( ...
  return slice.Detach();
}

CALLER_ATTACH FontData* WritableFontData::Slice(int32_t offset) {
  if (offset > Size()) {
    return NULL;
  }
  FontDataPtr slice = new WritableFontData(this, offset);
  // Note: exception not ported because the condition is always false in C++.
  // if (slice == null) { throw new IndexOutOfBoundsException( ...
  return slice.Detach();
}

WritableFontData::WritableFontData(WritableFontData* data, int32_t offset)
    : ReadableFontData(data, offset) {
}

WritableFontData::WritableFontData(WritableFontData* data,
                                   int32_t offset,
                                   int32_t length)
    : ReadableFontData(data, offset, length) {
}

}  // namespace sfntly

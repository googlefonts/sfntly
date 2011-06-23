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

WritableFontData::~WritableFontData() {}

WritableFontData::WritableFontData(ByteArray* ba) : ReadableFontData(ba) {
}

WritableFontData::WritableFontData(WritableFontData* data, int32_t offset)
    : ReadableFontData(data, offset) {
}

WritableFontData::WritableFontData(WritableFontData* data, int32_t offset,
                                   int32_t length)
    : ReadableFontData(data, offset, length) {
}

int32_t WritableFontData::writeByte(int32_t index, byte_t b) {
  array_->put(boundOffset(index), b);
  return 1;
}

int32_t WritableFontData::writeBytes(int32_t offset, ByteVector* b,
                                     int32_t index, int32_t length) {
  return array_->put(boundOffset(offset), b, index,
                     boundLength(offset, length));
}

int32_t WritableFontData::writeBytes(int32_t index, ByteVector* b) {
  return writeBytes(index, b, 0, b->size());
}

int32_t WritableFontData::writeChar(int32_t index, byte_t c) {
  return writeByte(index, c);
}

int32_t WritableFontData::writeUShort(int32_t index, int32_t us) {
  writeByte(index, (byte_t)((us >> 8) & 0xff));
  writeByte(index + 1, (byte_t)(us & 0xff));
  return 2;
}

int32_t WritableFontData::writeUShortLE(int32_t index, int32_t us) {
  writeByte(index, (byte_t)(us & 0xff));
  writeByte(index + 1, (byte_t)((us >> 8) & 0xff));
  return 2;
}

int32_t WritableFontData::writeShort(int32_t index, int32_t s) {
  return writeUShort(index, s);
}

int32_t WritableFontData::writeUInt24(int32_t index, int32_t ui) {
  writeByte(index, (byte_t)((ui >> 16) & 0xff));
  writeByte(index + 1, (byte_t)((ui >> 8) & 0xff));
  writeByte(index + 2, (byte_t)(ui & 0xff));
  return 3;
}

int32_t WritableFontData::writeULong(int32_t index, int64_t ul) {
  writeByte(index, (byte_t)((ul >> 24) & 0xff));
  writeByte(index + 1, (byte_t)((ul >> 16) & 0xff));
  writeByte(index + 2, (byte_t)((ul >> 8) & 0xff));
  writeByte(index + 3, (byte_t)(ul & 0xff));
  return 4;
}

int32_t WritableFontData::writeULongLE(int32_t index, int64_t ul) {
  writeByte(index, (byte_t)(ul & 0xff));
  writeByte(index + 1, (byte_t)((ul >> 8) & 0xff));
  writeByte(index + 2, (byte_t)((ul >> 16) & 0xff));
  writeByte(index + 3, (byte_t)((ul >> 24) & 0xff));
  return 4;
}

int32_t WritableFontData::writeLong(int32_t index, int64_t l) {
  return writeULong(index, l);
}

int32_t WritableFontData::writeFixed(int32_t index, int32_t l) {
  return writeLong(index, l);
}

int32_t WritableFontData::writeDateTime(int32_t index, int64_t date) {
  writeULong(index, (date >> 32) & 0xffffffff);
  writeULong(index + 4, date & 0xffffffff);
  return 8;
}

CALLER_ATTACH FontData* WritableFontData::slice(int32_t offset,
                                                int32_t length) {
  if (offset < 0 || offset + length > size()) {
    return NULL;
  }
  FontDataPtr slice = new WritableFontData(this, offset, length);
  // Note: exception not ported because the condition is always false in C++.
  // if (slice == null) { throw new IndexOutOfBoundsException( ...
  return slice.detach();
}

CALLER_ATTACH FontData* WritableFontData::slice(int32_t offset) {
  if (offset > size()) {
    return NULL;
  }
  FontDataPtr slice = new WritableFontData(this, offset);
  // Note: exception not ported because the condition is always false in C++.
  // if (slice == null) { throw new IndexOutOfBoundsException( ...
  return slice.detach();
}

}  // namespace sfntly

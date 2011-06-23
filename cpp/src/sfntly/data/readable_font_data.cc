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

#include "sfntly/data/readable_font_data.h"
#include "sfntly/data/writable_font_data.h"
#include "sfntly/port/exception_type.h"

namespace sfntly {

ReadableFontData::~ReadableFontData() {}

ReadableFontData::ReadableFontData(ByteArray* array)
    : FontData(array), checksum_(0), checksum_set_(false) {
}

ReadableFontData::ReadableFontData(ReadableFontData* data, int32_t offset)
    : FontData(data, offset), checksum_(0), checksum_set_(false) {
}

ReadableFontData::ReadableFontData(ReadableFontData* data, int32_t offset,
                                   int32_t length)
    : FontData(data, offset, length), checksum_(0), checksum_set_(false) {
}

int64_t ReadableFontData::checksum() {
  // TODO(arthurhsu): IMPLEMENT: atomicity
  if (!checksum_set_) {
    computeChecksum();
  }
  return checksum_;
}

/* OpenType checksum
ULONG
CalcTableChecksum(ULONG *Table, ULONG Length)
{
ULONG Sum = 0L;
ULONG *Endptr = Table+((Length+3) & ~3) / sizeof(ULONG);
while (Table < EndPtr)
  Sum += *Table++;
return Sum;
}
*/
void ReadableFontData::computeChecksum() {
  // TODO(arthurhsu): IMPLEMENT: synchronization/atomicity
  int64_t sum = 0;
  if (checksum_range_.empty()) {
    sum = computeCheckSum(0, length());
  } else {
    for (uint32_t low_bound_index = 0; low_bound_index < checksum_range_.size();
         low_bound_index += 2) {
      int32_t low_bound = checksum_range_[low_bound_index];
      int32_t high_bound = (low_bound_index == checksum_range_.size() - 1) ?
                                length() :
                                checksum_range_[low_bound_index + 1];
      sum += computeCheckSum(low_bound, high_bound);
    }
  }

  checksum_ = sum & 0xffffffffL;
  checksum_set_ = true;
}

int64_t ReadableFontData::computeCheckSum(int32_t low_bound,
                                          int32_t high_bound) {
  int64_t sum = 0;
  for (int32_t i = low_bound; i < high_bound; i += 4) {
    int32_t b3 = readUByte(i);
    b3 = (b3 == -1) ? 0 : b3;
    int32_t b2 = readUByte(i + 1);
    b2 = (b2 == -1) ? 0 : b2;
    int32_t b1 = readUByte(i + 2);
    b1 = (b1 == -1) ? 0 : b1;
    int32_t b0 = readUByte(i + 3);
    b0 = (b0 == -1) ? 0 : b0;
    sum += (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
  }
  return sum;
}

void ReadableFontData::setCheckSumRanges(const IntegerList& ranges) {
  checksum_range_ = ranges;
  checksum_set_ = false;  // UNIMPLEMENTED: atomicity
}

int32_t ReadableFontData::readUByte(int32_t index) {
  return 0xff & array_->get(boundOffset(index));
}

int32_t ReadableFontData::readByte(int32_t index) {
  return (array_->get(boundOffset(index)) << 24) >> 24;
}

int32_t ReadableFontData::readBytes(int32_t index, ByteVector* b,
                                    int32_t offset, int32_t length) {
  return array_->get(boundOffset(index), b, offset, boundLength(index, length));
}

int32_t ReadableFontData::readChar(int32_t index) {
  return readUByte(index);
}

int32_t ReadableFontData::readUShort(int32_t index) {
  return 0xffff & (readUByte(index) << 8 | readUByte(index + 1));
}

int32_t ReadableFontData::readShort(int32_t index) {
  return ((readByte(index) << 8 | readUByte(index + 1)) << 16) >> 16;
}

int32_t ReadableFontData::readUInt24(int32_t index) {
  return 0xffffff & (readUByte(index) << 16 |
                     readUByte(index + 1) << 8 | readUByte(index + 2));
}

int64_t ReadableFontData::readULong(int32_t index) {
  return 0xffffffffL & (readUByte(index) << 24 | readUByte(index + 1) << 16 |
                        readUByte(index + 2) << 8 | readUByte(index + 3));
}

int32_t ReadableFontData::readULongAsInt(int32_t index) {
  int64_t ulong = readULong(index);
  if ((ulong & 0x80000000) == 0x80000000) {
    throw ArithmeticException("Long value too large to fit into an integer.");
  }
  return ((int32_t)ulong) & ~0x80000000;
}

int32_t ReadableFontData::readLong(int32_t index) {
  return readByte(index) << 24 | readUByte(index + 1) << 16 |
         readUByte(index + 2) << 8 | readUByte(index + 3);
}

int32_t ReadableFontData::readFixed(int32_t index) {
  return readLong(index);
}

int64_t ReadableFontData::readDateTimeAsLong(int32_t index) {
  return (int64_t)readULong(index) << 32 | readULong(index + 4);
}

int32_t ReadableFontData::readFWord(int32_t index) {
  return readShort(index);
}

int32_t ReadableFontData::readFUFWord(int32_t index) {
  return readUShort(index);
}

int32_t ReadableFontData::copyTo(OutputStream* os) {
  return array_->copyTo(os, boundOffset(0), length());
}

int32_t ReadableFontData::copyTo(WritableFontData* wfd) {
  return array_->copyTo(wfd->boundOffset(0), wfd->array_, boundOffset(0),
                        length());
}

int32_t ReadableFontData::copyTo(ByteArray* ba) {
  return array_->copyTo(ba, boundOffset(0), length());
}

CALLER_ATTACH FontData* ReadableFontData::slice(int32_t offset,
                                                int32_t length) {
  if (offset < 0 || offset + length > size()) {
    return NULL;
  }
  FontDataPtr slice = new ReadableFontData(this, offset, length);
  // Note: exception not ported because the condition is always false in C++.
  // if (slice == null) { throw new IndexOutOfBoundsException( ...
  return slice.detach();
}

CALLER_ATTACH FontData* ReadableFontData::slice(int32_t offset) {
  if (offset < 0 || offset > size()) {
    return NULL;
  }
  FontDataPtr slice = new ReadableFontData(this, offset);
  // Note: exception not ported because the condition is always false in C++.
  // if (slice == null) { throw new IndexOutOfBoundsException( ...
  return slice.detach();
}

}  // namespace sfntly

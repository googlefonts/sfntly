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

#include <algorithm>

#include "sfntly/data/font_output_stream.h"
#include "sfntly/port/endian.h"

namespace sfntly {

FontOutputStream::FontOutputStream(OutputStream* os)
    : stream_(os), position_(0) {
}

FontOutputStream::~FontOutputStream() {
  close();
}

size_t FontOutputStream::position() {
  return position_;
}

void FontOutputStream::write(byte_t b) {
  if (stream_) {
    stream_->write(b);
  }
  position_++;
}

void FontOutputStream::write(ByteVector* b) {
  if (b) {
    write(b, 0, b->size());
  }
}

void FontOutputStream::write(ByteVector* b, int32_t offset, int32_t length) {
  if (stream_ && b) {
    stream_->write(b, offset, length);
  }
}

void FontOutputStream::writeChar(byte_t c) {
  write(c);
}

void FontOutputStream::writeUShort(int32_t us) {
  write((byte_t)((us >> 8) & 0xff));
  write((byte_t)(us & 0xff));
}

void FontOutputStream::writeShort(int32_t s) {
  writeUShort(s);
}

void FontOutputStream::writeUInt24(int32_t ui) {
  write((byte_t)(ui >> 16) & 0xff);
  write((byte_t)(ui >> 8) & 0xff);
  write((byte_t)ui & 0xff);
}

void FontOutputStream::writeULong(int64_t ul) {
  write((byte_t)((ul >> 24) & 0xff));
  write((byte_t)((ul >> 16) & 0xff));
  write((byte_t)((ul >> 8) & 0xff));
  write((byte_t)(ul & 0xff));
}

void FontOutputStream::writeLong(int64_t l) {
  writeULong(l);
}

void FontOutputStream::writeFixed(int32_t f) {
  writeULong(f);
}

void FontOutputStream::writeDateTime(int64_t date) {
  writeULong((date >> 32) & 0xffffffff);
  writeULong(date & 0xffffffff);
}

void FontOutputStream::flush() {
  if (stream_) {
    stream_->flush();
  }
}

void FontOutputStream::close() {
  if (stream_) {
    stream_->flush();
    stream_->close();
  }
}

}  // namespace sfntly

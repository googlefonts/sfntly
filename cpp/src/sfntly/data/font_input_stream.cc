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

#include "sfntly/data/font_input_stream.h"

namespace sfntly {

FontInputStream::FontInputStream(InputStream* is)
    : stream_(is), position_(0), length_(0), bounded_(false) {
}

FontInputStream::FontInputStream(InputStream* is, size_t length)
    : stream_(is), position_(0), length_(length), bounded_(false) {
}

FontInputStream::~FontInputStream() {
  close();
}

int32_t FontInputStream::available() {
  if (stream_) {
    return stream_->available();
  }
  return 0;
}

void FontInputStream::close() {
  if (stream_) {
    stream_->close();
  }
}

void FontInputStream::mark(int32_t readlimit) {
  if (stream_) {
    stream_->mark(readlimit);
  }
}

bool FontInputStream::markSupported() {
  if (stream_) {
    return stream_->markSupported();
  }
  return false;
}

void FontInputStream::reset() {
  if (stream_) {
    stream_->reset();
  }
}

int32_t FontInputStream::read() {
  if (!stream_ || (bounded_ && position_ >= length_)) {
    return -1;
  }
  int32_t b = stream_->read();
  if (b >= 0) {
    position_++;
  }
  return b;
}

int32_t FontInputStream::read(ByteVector* b, int32_t offset, int32_t length) {
  if (!stream_ || offset < 0 || length < 0 ||
      (bounded_ && position_ >= length_)) {
    return -1;
  }
  int32_t bytes_to_read =
      bounded_ ? std::min<int32_t>(length, (int32_t)(length_ - position_)) :
                 length;
  int32_t bytes_read = stream_->read(b, offset, bytes_to_read);
  position_ += bytes_read;
  return bytes_read;
}

int32_t FontInputStream::read(ByteVector* b) {
  return read(b, 0, b->size());
}

int64_t FontInputStream::position() {
  return position_;
}

int32_t FontInputStream::readChar() {
  return read();
}

int32_t FontInputStream::readUShort() {
  return 0xffff & (read() << 8 | read());
}

int32_t FontInputStream::readShort() {
  return ((read() << 8 | read()) << 16) >> 16;
}

int32_t FontInputStream::readUInt24() {
  return 0xffffff & (read() << 16 | read() << 8 | read());
}

int64_t FontInputStream::readULong() {
  return 0xffffffffL & readLong();
}

int32_t FontInputStream::readULongAsInt() {
  int64_t ulong = readULong();
  return ((int32_t)ulong) & ~0x80000000;
}

int32_t FontInputStream::readLong() {
  return read() << 24 | read() << 16 | read() << 8 | read();
}

int32_t FontInputStream::readFixed() {
  return readLong();
}

int64_t FontInputStream::readDateTimeAsLong() {
  return (int64_t)readULong() << 32 | readULong();
}

int64_t FontInputStream::skip(int64_t n) {
  if (stream_) {
    int64_t skipped = stream_->skip(n);
    position_ += skipped;
    return skipped;
  }
  return 0;
}

}  // namespace sfntly

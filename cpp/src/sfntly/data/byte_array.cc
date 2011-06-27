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

#include "sfntly/data/byte_array.h"
#include "sfntly/port/exception_type.h"

namespace sfntly {

const int32_t ByteArray::COPY_BUFFER_SIZE = 8192;

void ByteArray::init(int32_t filled_length, int32_t storage_length,
                     bool growable) {
  storage_length_ = storage_length;
  growable_ = growable;
  setFilledLength(filled_length);
}

ByteArray::ByteArray(int32_t filled_length, int32_t storage_length,
                     bool growable) {
  init(filled_length, storage_length, growable);
}

ByteArray::ByteArray(int32_t filled_length, int32_t storage_length) {
  init(filled_length, storage_length, false);
}

ByteArray::~ByteArray() {}

int32_t ByteArray::length() { return filled_length_; }
int32_t ByteArray::size() { return storage_length_; }
bool ByteArray::growable() { return growable_; }

int32_t ByteArray::setFilledLength(int32_t filled_length) {
  filled_length_ = std::min<int32_t>(filled_length, storage_length_);
  return filled_length_;
}

byte_t ByteArray::get(int32_t index) {
  return internalGet(index);
}

int32_t ByteArray::get(int32_t index, ByteVector* b) {
  assert(b);
  return get(index, b, 0, b->size());
}

int32_t ByteArray::get(int32_t index, ByteVector* b, int32_t offset,
                       int32_t length) {
  assert(b);
  if (index < 0 || index >= filled_length_) {
    return -1;
  }
  int32_t actual_length = std::min<int32_t>(length, filled_length_ - index);
  if (actual_length < 0) {
      return -1;
  }
  return internalGet(index, b, offset, actual_length);
}

bool ByteArray::put(int32_t index, byte_t b) {
  if (index < 0 || index >= size()) {
    return false;
  }
  bool result = internalPut(index, b);
  filled_length_ = std::max<int32_t>(filled_length_, index + 1);
  return result;
}

int32_t ByteArray::put(int index, ByteVector* b) {
  assert(b);
  return put(index, b, 0, b->size());
}

int32_t ByteArray::put(int32_t index, ByteVector* b, int32_t offset,
                       int32_t length) {
  assert(b);
  if (index < 0 || index >= size()) {
    return 0;
  }
  int32_t actual_length = std::min<int32_t>(length, size() - index);
  int32_t bytes_written = internalPut(index, b, offset, actual_length);
  filled_length_ = std::max<int32_t>(filled_length_, index + bytes_written);
  return bytes_written;
}

int32_t ByteArray::copyTo(ByteArray* array) {
  return copyTo(array, 0, length());
}

int32_t ByteArray::copyTo(ByteArray* array, int32_t offset, int32_t length) {
  return copyTo(0, array, offset, length);
}

int32_t ByteArray::copyTo(int32_t dst_offset, ByteArray* array,
                          int32_t src_offset, int32_t length) {
  assert(array);
  if (array->size() < dst_offset + length) {  // insufficient space
    return -1;
  }

  ByteVector b(COPY_BUFFER_SIZE);
  int32_t bytes_read = 0;
  int32_t index = 0;
  int32_t remaining_length = length;
  int32_t buffer_length = std::min<int32_t>(COPY_BUFFER_SIZE, length);
  while ((bytes_read = get(index + src_offset, &b, 0, buffer_length)) > 0) {
    int bytes_written = array->put(index + dst_offset, &b, 0, bytes_read);
    if (bytes_written != bytes_read) {
#if defined (SFNTLY_NO_EXCEPTION)
      return 0;
#else
      throw IOException("Error writing bytes.");
#endif
    }
    index += bytes_read;
    remaining_length -= bytes_read;
    buffer_length = std::min<int32_t>(b.size(), remaining_length);
  }
  return index;
}

int32_t ByteArray::copyTo(OutputStream* os) {
    return copyTo(os, 0, length());
}

int32_t ByteArray::copyTo(OutputStream* os, int32_t offset, int32_t length) {
  ByteVector b(COPY_BUFFER_SIZE);
  int32_t bytes_read = 0;
  int32_t index = offset;
  int32_t buffer_length = std::min<int32_t>(COPY_BUFFER_SIZE, length);
  while ((bytes_read = get(index, &b, 0, buffer_length)) > 0) {
    os->write(&b, 0, bytes_read);
    index += bytes_read;
    buffer_length = std::min<int32_t>(b.size(), length - index);
  }
  return index;
}

bool ByteArray::copyFrom(InputStream* is, int32_t length) {
  ByteVector b(COPY_BUFFER_SIZE);
  int32_t bytes_read = 0;
  int32_t index = 0;
  int32_t buffer_length = std::min<int32_t>(COPY_BUFFER_SIZE, length);
  while ((bytes_read =
          is->read(&b, 0, buffer_length)) > 0) {
    if (put(index, &b, 0, bytes_read) != bytes_read) {
#if defined (SFNTLY_NO_EXCEPTION)
      return 0;
#else
      throw IOException("Error writing bytes.");
#endif
    }
    index += bytes_read;
    length -= bytes_read;
    buffer_length = std::min<int32_t>(b.size(), length);
  }
  return true;
}

bool ByteArray::copyFrom(InputStream* is) {
  ByteVector b(COPY_BUFFER_SIZE);
  int32_t bytes_read = 0;
  int32_t index = 0;
  int32_t buffer_length = COPY_BUFFER_SIZE;
  while ((bytes_read =
          is->read(&b, 0, buffer_length)) > 0) {
    if (put(index, &b, 0, bytes_read) != bytes_read) {
#if defined (SFNTLY_NO_EXCEPTION)
      return 0;
#else
      throw IOException("Error writing bytes.");
#endif
    }
    index += bytes_read;
  }
  return true;
}

}  // namespace sfntly

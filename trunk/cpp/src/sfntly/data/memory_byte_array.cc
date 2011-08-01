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

#include "sfntly/data/memory_byte_array.h"

#include <string.h>

namespace sfntly {

// Note: this constructor can fail under low-memory situation.
MemoryByteArray::MemoryByteArray(int32_t length)
    : ByteArray(0, length), b_(NULL), allocated_(true) {
}

MemoryByteArray::MemoryByteArray(byte_t* b, int32_t buffer_length)
    : ByteArray(buffer_length, buffer_length), b_(b), allocated_(false) {
}

MemoryByteArray::MemoryByteArray(byte_t* b,
                                 int32_t buffer_length,
                                 int32_t filled_length)
    : ByteArray(filled_length, buffer_length), b_(b), allocated_(false) {
}

MemoryByteArray::~MemoryByteArray() {
  Close();
}

void MemoryByteArray::Init() {
  if (allocated_ && b_ == NULL) {
    b_ = new byte_t[Size()];
    memset(b_, 0, Size());
  }
}

bool MemoryByteArray::InternalPut(int32_t index, byte_t b) {
  Init();
  b_[index] = b;
  return true;
}

int32_t MemoryByteArray::InternalPut(int32_t index,
                                     ByteVector* b,
                                     int32_t offset,
                                     int32_t length) {
  Init();
  memcpy(b_ + index, &((*b)[offset]), length);
  return length;
}

byte_t MemoryByteArray::InternalGet(int32_t index) {
  Init();
  return b_[index];
}

int32_t MemoryByteArray::InternalGet(int32_t index,
                                     ByteVector* b,
                                     int32_t offset,
                                     int32_t length) {
  Init();
  memcpy(&((*b)[offset]), b_ + index, length);
  return length;
}

void MemoryByteArray::Close() {
  if (allocated_ && b_) {
    delete[] b_;
  }
  b_ = NULL;
}

byte_t* MemoryByteArray::Begin() {
  Init();
  return b_;
}

}  // namespace sfntly

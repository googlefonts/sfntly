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

#include <stdio.h>

#include "gtest/gtest.h"
#include "sfntly/data/memory_byte_array.h"
#include "sfntly/data/growable_memory_byte_array.h"
#include "test/byte_array_test.h"

namespace sfntly {
namespace byte_array_test {

const int32_t BYTE_ARRAY_SIZES[] =
    {1, 7, 127, 128, 129, 255, 256, 257, 666, 1023, 10000, 0xffff, 0x10000};

void fillTestByteArray(ByteArray* ba, int32_t size) {
  for (int32_t i = 0; i < size; ++i) {
    ba->put(i, (byte_t)(i % 256));
  }
}

void readByteArrayWithBuffer(ByteArray* ba, ByteVector* buffer, ByteVector* b) {
  b->resize(ba->length());
  int32_t index = 0;
  while (index < ba->length()) {
    int32_t bytes_read = ba->get(index, buffer);
    std::copy(buffer->begin(), buffer->begin() + bytes_read,
              b->begin() + index);
    index += bytes_read;
  }
}

void readByteArrayWithSlidingWindow(ByteArray* ba, int window_size,
                                    ByteVector* b) {
  b->resize(ba->length());
  int32_t index = 0;
  int32_t actual_window_size = window_size;
  while (index < ba->length()) {
    actual_window_size =
        std::min<int32_t>(actual_window_size, b->size() - index);
    int32_t bytes_read = ba->get(index, b, index, actual_window_size);
    index += bytes_read;
  }
}

bool readComparison(ByteArray* ba1, ByteArray* ba2) {
  // single byte reads
  for (int i = 0; i < ba1->length(); ++i) {
    EXPECT_EQ(ba1->get(i), ba2->get(i));
  }

  ByteVector b1, b2;
  // buffer reads
  int increments = std::max<int32_t>(ba1->length() / 11, 1);
  for (int buffer_size = 1; buffer_size < ba1->length();
       buffer_size += increments) {
    ByteVector buffer(buffer_size);
    readByteArrayWithBuffer(ba1, &buffer, &b1);
    readByteArrayWithBuffer(ba2, &buffer, &b2);
    EXPECT_GT(b1.size(), static_cast<size_t>(0));
    EXPECT_EQ(b1.size(), b2.size());
    EXPECT_TRUE(std::equal(b1.begin(), b1.end(), b2.begin()));
  }

  // sliding window reads
  b1.clear();
  b2.clear();
  for (int window_size = 1; window_size < ba1->length();
       window_size += increments) {
    readByteArrayWithSlidingWindow(ba1, window_size, &b1);
    readByteArrayWithSlidingWindow(ba2, window_size, &b2);
    EXPECT_GT(b1.size(), static_cast<size_t>(0));
    EXPECT_EQ(b1.size(), b2.size());
    EXPECT_TRUE(std::equal(b1.begin(), b1.end(), b2.begin()));
  }

  return true;
}

bool copyTest(ByteArray* ba) {
  ByteArrayPtr fixed_copy = new MemoryByteArray(ba->length());
  ba->copyTo(fixed_copy);
  EXPECT_EQ(ba->length(), fixed_copy->length());
  EXPECT_TRUE(readComparison(ba, fixed_copy));

  ByteArrayPtr growable_copy = new GrowableMemoryByteArray();
  ba->copyTo(growable_copy);
  EXPECT_EQ(ba->length(), growable_copy->length());
  EXPECT_TRUE(readComparison(ba, growable_copy));

  return true;
}

bool byteArrayTester(ByteArray* ba) {
  return copyTest(ba);
}

}  // namespace byte_array_test

bool testMemoryByteArray() {
  for (int32_t i = 0;
       i < sizeof(byte_array_test::BYTE_ARRAY_SIZES) / sizeof(int32_t); ++i) {
    int32_t size = byte_array_test::BYTE_ARRAY_SIZES[i];
    fprintf(stderr, "fixed mem: iteration %d, size %d\n", i, size);
    ByteArrayPtr ba = new MemoryByteArray(size);
    byte_array_test::fillTestByteArray(ba, size);
    EXPECT_TRUE(byte_array_test::byteArrayTester(ba));
  }
  return true;
}

bool testGrowableMemoryByteArray() {
  for (int32_t i = 0;
       i < sizeof(byte_array_test::BYTE_ARRAY_SIZES) / sizeof(int32_t); ++i) {
    int32_t size = byte_array_test::BYTE_ARRAY_SIZES[i];
    fprintf(stderr, "growable mem: iteration %d, size %d\n", i, size);
    ByteArrayPtr ba = new GrowableMemoryByteArray();
    byte_array_test::fillTestByteArray(ba, size);
    EXPECT_TRUE(byte_array_test::byteArrayTester(ba));
  }
  return true;
}

}  // namespace sfntly

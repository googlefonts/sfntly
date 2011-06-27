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

#include <vector>
#include <algorithm>

#include "gtest/gtest.h"
#include "sfntly/port/type.h"
#include "sfntly/data/writable_font_data.h"
#include "sfntly/data/memory_byte_array.h"
#include "test/font_data_test.h"

namespace sfntly {

const int32_t BYTE_ARRAY_SIZES[] =
    {1, 7, 127, 128, 129, 255, 256, 257, 666, 1023, 0x10000};

void fillTestByteArray(ByteArray* ba, int32_t size) {
  for (int32_t i = 0; i < size; ++i) {
    ba->put(i, (byte_t)(i % 256));
  }
}

void readFontDataWithSingleByte(ReadableFontData* rfd, ByteVector* buffer) {
  buffer->resize(rfd->length());
  for (int32_t index = 0; index < rfd->length(); ++index) {
    (*buffer)[index] = (byte_t)(rfd->readByte(index));
  }
}

void readFontDataWithBuffer(ReadableFontData* rfd, int32_t buffer_size,
                            ByteVector* b) {
  ByteVector buffer(buffer_size);
  b->resize(rfd->length());

  int32_t index = 0;
  while (index < rfd->length()) {
    int32_t bytes_read = rfd->readBytes(index, &buffer, 0, buffer.size());
    EXPECT_GE(bytes_read, 0);
    std::copy(buffer.begin(), buffer.begin() + bytes_read, b->begin() + index);
    index += bytes_read;
  }
}

void readFontDataWithSlidingWindow(ReadableFontData* rfd, int32_t window_size,
                                   ByteVector* b) {
  b->resize(rfd->length());
  int32_t index = 0;
  while (index < rfd->length()) {
    int32_t actual_window_size =
        std::min<int32_t>(window_size, b->size() - index);
    int32_t bytes_read = rfd->readBytes(index, b, index, actual_window_size);
    EXPECT_GE(bytes_read, 0);
    index += bytes_read;
  }
}

void writeFontDataWithSingleByte(ReadableFontData* rfd, WritableFontData* wfd) {
  for (int32_t index = 0; index < rfd->length(); ++index) {
    byte_t b = (byte_t)(rfd->readByte(index));
    wfd->writeByte(index, b);
  }
}

void writeFontDataWithBuffer(ReadableFontData* rfd, WritableFontData* wfd,
                             int32_t buffer_size) {
  ByteVector buffer(buffer_size);
  int32_t index = 0;
  while (index < rfd->length()) {
    int32_t bytesRead = rfd->readBytes(index, &buffer, 0, buffer.size());
    wfd->writeBytes(index, &buffer, 0, buffer.size());
    index += bytesRead;
  }
}

void writeFontDataWithSlidingWindow(ReadableFontData* rfd,
                                    WritableFontData* wfd,
                                    int32_t window_size) {
  ByteVector b(rfd->length());
  int32_t index = 0;
  while (index < rfd->length()) {
    int32_t sliding_size = std::min<int32_t>(window_size, b.size() - index);
    int32_t bytes_read = rfd->readBytes(index, &b, index, sliding_size);
    wfd->writeBytes(index, &b, index, sliding_size);
    index += bytes_read;
  }
}

bool readComparison(int32_t offset, int32_t length, ReadableFontData* rfd1,
                    ReadableFontData* rfd2) {
  EXPECT_TRUE(length == rfd2->length());
  ByteVector b1, b2;
  b1.resize(length);
  b2.resize(length);

  // single byte reads
  readFontDataWithSingleByte(rfd1, &b1);
  readFontDataWithSingleByte(rfd2, &b2);
  EXPECT_EQ(memcmp(&(b1[offset]), &(b2[0]), length), 0);

  // buffer reads
  int32_t increments = std::max<int32_t>(length / 11, 1);
  for (int32_t buffer_size = 1; buffer_size <= length;
       buffer_size += increments) {
    b1.clear();
    b2.clear();
    b1.resize(length);
    b2.resize(length);
    readFontDataWithBuffer(rfd1, buffer_size, &b1);
    readFontDataWithBuffer(rfd2, buffer_size, &b2);
    int result = memcmp(&(b1[offset]), &(b2[0]), length);
    EXPECT_EQ(result, 0);
  }

  // sliding window reads
  for (int32_t window_size = 1; window_size <= length;
       window_size += increments) {
    b1.clear();
    b2.clear();
    b1.resize(length);
    b2.resize(length);
    readFontDataWithSlidingWindow(rfd1, window_size, &b1);
    readFontDataWithSlidingWindow(rfd2, window_size, &b2);
    int result = memcmp(&(b1[offset]), &(b2[0]), length);
    EXPECT_EQ(result, 0);
  }
  return true;
}

void slicingReadTest(ReadableFontData* rfd) {
  for (int32_t trim = 0; trim < (rfd->length() / 2) + 1;
       trim += (rfd->length() / 21) + 1) {
    fprintf(stderr, "\tread - trim = %d\n", trim);
    int32_t length = rfd->length() - 2 * trim;
    ReadableFontDataPtr slice;
    slice.attach(down_cast<ReadableFontData*>(rfd->slice(trim, length)));
    EXPECT_TRUE(readComparison(trim, length, rfd, slice));
  }
}

void slicingWriteTest(ReadableFontData* rfd, WritableFontData* wfd) {
  for (int32_t trim = 0; trim < (rfd->length() / 2) + 1;
       trim += (rfd->length() / 21) + 1) {
    fprintf(stderr, "\twrite - trim = %d\n", trim);
    int32_t length = rfd->length() - 2 * trim;
    WritableFontDataPtr w_slice;
    ReadableFontDataPtr r_slice;

    // single byte writes
    w_slice.attach(down_cast<WritableFontData*>(wfd->slice(trim, length)));
    r_slice.attach(down_cast<ReadableFontData*>(rfd->slice(trim, length)));
    writeFontDataWithSingleByte(r_slice, w_slice);
    EXPECT_TRUE(readComparison(trim, length, rfd, w_slice));

    // buffer writes
    int32_t increments = std::max<int32_t>(length / 11, 1);
    for (int32_t buffer_size = 1; buffer_size < length;
         buffer_size += increments) {
      w_slice.attach(down_cast<WritableFontData*>(wfd->slice(trim, length)));
      r_slice.attach(down_cast<ReadableFontData*>(rfd->slice(trim, length)));
      writeFontDataWithBuffer(r_slice, w_slice, buffer_size);
      EXPECT_TRUE(readComparison(trim, length, rfd, w_slice));
    }

    // sliding window writes
    for (int window_size = 1; window_size < length; window_size += increments) {
      w_slice.attach(down_cast<WritableFontData*>(wfd->slice(trim, length)));
      r_slice.attach(down_cast<ReadableFontData*>(rfd->slice(trim, length)));
      writeFontDataWithSlidingWindow(r_slice, w_slice, window_size);
      EXPECT_TRUE(readComparison(trim, length, rfd, w_slice));
    }
  }
}

bool testReadableFontData() {
  for (size_t i = 0; i < sizeof(BYTE_ARRAY_SIZES) / sizeof(int32_t); ++i) {
    int32_t size = BYTE_ARRAY_SIZES[i];
    ByteArrayPtr ba = new MemoryByteArray(size);
    fillTestByteArray(ba, size);
    ReadableFontDataPtr rfd = new ReadableFontData(ba);
    slicingReadTest(rfd);
  }
  return true;
}

bool testWritableFontData() {
  for (size_t i = 0; i < sizeof(BYTE_ARRAY_SIZES) / sizeof(int32_t); ++i) {
    int32_t size = BYTE_ARRAY_SIZES[i];
    ByteArrayPtr ba = new MemoryByteArray(size);
    fillTestByteArray(ba, size);
    WritableFontDataPtr wfd = new WritableFontData(ba);
    slicingReadTest(wfd);
    ByteArrayPtr temp = new MemoryByteArray(size);
    WritableFontDataPtr wfd_copy = new WritableFontData(temp);
    slicingWriteTest(wfd, wfd_copy);
  }
  return true;
}

}  // namespace sfntly

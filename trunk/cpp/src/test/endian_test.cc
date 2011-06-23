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

#include "gtest/gtest.h"
#include "sfntly/tag.h"
#include "sfntly/data/growable_memory_byte_array.h"
#include "sfntly/data/writable_font_data.h"
#include "sfntly/math/fixed1616.h"
#include "sfntly/port/memory_output_stream.h"
#include "sfntly/data/font_output_stream.h"
#include "test/endian_test.h"

namespace sfntly {

bool testEndian() {
  byte_t test_data[] = {
      0x68, 0x65, 0x61, 0x64,  // 0: head
      0xca, 0xca, 0xca, 0xca,  // 4: ubyte, byte, char
      0x00, 0x18, 0x80, 0x18,  // 8: ushort, short
      0x00, 0x00, 0x18, 0x00,  // 12: uint24
      0x00, 0x00, 0x00, 0x18,  // 16: ulong
      0xff, 0xff, 0xff, 0x00,  // 20: long
      0x00, 0x01, 0x00, 0x00   // 24: fixed
  };

  ByteArrayPtr ba1 = new GrowableMemoryByteArray();
  for (size_t i = 0; i < sizeof(test_data); ++i) {
    ba1->put(i, test_data[i]);
  }
  ReadableFontDataPtr rfd = new ReadableFontData(ba1);
  EXPECT_EQ(rfd->readULongAsInt(0), Tag::head);
  EXPECT_EQ(rfd->readUByte(4), 202);
  EXPECT_EQ(rfd->readByte(5), -54);
  EXPECT_EQ(rfd->readChar(6), 202);
  EXPECT_EQ(rfd->readUShort(8), 24);
  EXPECT_EQ(rfd->readShort(10), -32744);
  EXPECT_EQ(rfd->readUInt24(12), 24);
  EXPECT_EQ(rfd->readULong(16), 24);
  EXPECT_EQ(rfd->readLong(20), -256);
  EXPECT_EQ(rfd->readFixed(24), Fixed1616::fixed(1, 0));

  MemoryOutputStream os;
  FontOutputStream fos(&os);
  fos.writeULong(Tag::head);
  fos.write(202);
  fos.write(202);
  fos.write(202);
  fos.write(202);
  fos.writeUShort(24);
  fos.writeShort(-32744);
  fos.writeUInt24(24);
  fos.writeChar(0);
  fos.writeULong(24);
  fos.writeLong(-256);
  fos.writeFixed(Fixed1616::fixed(1, 0));
  EXPECT_EQ(memcmp(os.get(), test_data, sizeof(test_data)), 0);

  return true;
}

}  // namespace sfntly

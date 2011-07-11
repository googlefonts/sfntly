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

// TODO(arthurhsu): wrap individul tests in ASSERTs so that they fail early

#include <limits.h>

#include "gtest/gtest.h"
#include "test/smart_pointer_test.h"
#include "test/endian_test.h"
#include "test/byte_array_test.h"
#include "test/file_io_test.h"
#include "test/font_data_test.h"
#include "test/open_type_data_test.h"
#include "test/otf_basic_editing_test.h"

#define RUN_LENGTHY_TESTS

TEST(SmartPointer, All) {
  EXPECT_TRUE(testSmartPointer());
}

TEST(Endian, All) {
  EXPECT_TRUE(sfntly::testEndian());
}

#if defined RUN_LENGTHY_TESTS
TEST(ByteArray, All) {
  EXPECT_TRUE(sfntly::testMemoryByteArray());
  EXPECT_TRUE(sfntly::testGrowableMemoryByteArray());
}
#endif

#if defined RUN_LENGTHY_TESTS
TEST(FontData, All) {
  EXPECT_TRUE(sfntly::testReadableFontData());
  EXPECT_TRUE(sfntly::testWritableFontData());
}
#endif

TEST(FileIO, All) {
  EXPECT_TRUE(sfntly::testFileInputStream());
  EXPECT_TRUE(sfntly::testFontInputStream());
}

TEST(OpenTypeData, All) {
  EXPECT_TRUE(sfntly::testOTFRead());
  EXPECT_TRUE(sfntly::testOTFCopy());
}

TEST(OTFBasicEditing, All) {
  EXPECT_TRUE(sfntly::testOTFBasicEditing());
}

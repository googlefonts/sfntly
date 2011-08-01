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
#include "test/font_parsing_test.h"
#include "test/open_type_data_test.h"
#include "test/otf_basic_editing_test.h"
#include "test/name_editing_test.h"
#include "test/test_utils_test.h"

#define RUN_LENGTHY_TESTS

namespace sfntly {

TEST(SmartPointer, All) {
  EXPECT_TRUE(TestSmartPointer());
}

TEST(Endian, All) {
  EXPECT_TRUE(sfntly::TestEndian());
}

#if defined RUN_LENGTHY_TESTS
TEST(ByteArray, All) {
  EXPECT_TRUE(sfntly::TestMemoryByteArray());
  EXPECT_TRUE(sfntly::TestGrowableMemoryByteArray());
}
#endif

#if defined RUN_LENGTHY_TESTS
TEST(FontData, All) {
  EXPECT_TRUE(sfntly::TestReadableFontData());
  EXPECT_TRUE(sfntly::TestWritableFontData());
}
#endif

TEST(FileIO, All) {
  EXPECT_TRUE(sfntly::TestFileInputStream());
  EXPECT_TRUE(sfntly::TestFontInputStreamBasic());
  EXPECT_TRUE(sfntly::TestFontInputStreamTableLoading());
}

TEST(OpenTypeData, All) {
  EXPECT_TRUE(sfntly::TestOTFRead());
  EXPECT_TRUE(sfntly::TestOTFCopy());
}

TEST(FontParsing, All) {
  EXPECT_TRUE(sfntly::TestFontParsing());
  EXPECT_TRUE(sfntly::TestTTFReadWrite());
  EXPECT_TRUE(sfntly::TestTTFMemoryBasedReadWrite());
}

TEST(OTFBasicEditing, All) {
  EXPECT_TRUE(sfntly::TestOTFBasicEditing());
}

TEST(NameEditing, All) {
  EXPECT_TRUE(sfntly::TestChangeOneName());
  EXPECT_TRUE(sfntly::TestModifyNameTableAndRevert());
  EXPECT_TRUE(sfntly::TestRemoveOneName());
}

TEST(TestUtils, Extension) {
  EXPECT_TRUE(sfntly::TestExtension());
}

TEST(TestUtils, Encoding) {
  EXPECT_TRUE(sfntly::TestEncoding());
}

}  // namespace sfntly

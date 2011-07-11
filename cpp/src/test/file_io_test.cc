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
#include "sfntly/port/file_input_stream.h"
#include "sfntly/data/font_input_stream.h"

namespace sfntly {

bool testFileInputStream() {
  const char* test_file = "arial.ttf";
    FILE* file_handle = NULL;
#if defined (WIN32)
  fopen_s(&file_handle, test_file, "rb");
#else
  file_handle = fopen(test_file, "rb");
#endif
  if (file_handle == NULL) {
    return false;
  }
  fseek(file_handle, 0, SEEK_END);
  size_t length = ftell(file_handle);
  fseek(file_handle, 0, SEEK_SET);
  ByteVector b1;
  b1.resize(length);
  fread(&(b1[0]), 1, length, file_handle);
  fclose(file_handle);

  // Full file reading test
  FileInputStream is;
  is.open(test_file);
  EXPECT_EQ(length, is.available());
  ByteVector b2;
  is.read(&b2, 0, length);
  is.close();
  EXPECT_EQ(memcmp(&(b1[0]), &(b2[0]), length), 0);
  b2.clear();

  // Partial reading test
  is.open(test_file);
  is.skip(89);
  is.read(&b2, 0, 100);
  EXPECT_EQ(memcmp(&(b1[89]), &(b2[0]), 100), 0);
  b2.clear();

  // Skip test
  is.skip(-89);
  is.read(&b2, 0, 100);
  EXPECT_EQ(memcmp(&(b1[100]), &(b2[0]), 100), 0);
  b2.clear();
  is.skip(100);
  is.read(&b2, 0, 100);
  EXPECT_EQ(memcmp(&(b1[300]), &(b2[0]), 100), 0);
  is.skip(-400);
  b2.clear();

  // Offset test
  is.read(&b2, 0, 100);
  is.read(&b2, 100, 100);
  EXPECT_EQ(memcmp(&(b1[0]), &(b2[0]), 200), 0);

  // Unread test
  ByteVector b3;
  b3.resize(200);
  is.unread(&b3);
  EXPECT_EQ(memcmp(&(b3[0]), &(b2[0]), 200), 0);

  return true;
}

bool testFontInputStream() {
  const char* test_file = "arial.ttf";
    FILE* file_handle = NULL;
#if defined (WIN32)
  fopen_s(&file_handle, test_file, "rb");
#else
  file_handle = fopen(test_file, "rb");
#endif
  if (file_handle == NULL) {
    return false;
  }
  fseek(file_handle, 0, SEEK_END);
  size_t length = ftell(file_handle);
  fseek(file_handle, 0, SEEK_SET);
  ByteVector b1;
  b1.resize(length);
  fread(&(b1[0]), 1, length, file_handle);
  fclose(file_handle);

  FileInputStream is;
  is.open(test_file);
  FontInputStream font_is1(&is);
  EXPECT_EQ(font_is1.available(), length);

  ByteVector b2;
  font_is1.read(&b2, 0, length);
  font_is1.close();
  EXPECT_EQ(memcmp(&(b1[0]), &(b2[0]), length), 0);
  b2.clear();

  is.open(test_file);
  is.skip(89);
  FontInputStream font_is2(&is, 200);
  font_is2.read(&b2, 0, 100);
  EXPECT_EQ(memcmp(&(b1[89]), &(b2[0]), 100), 0);
  font_is2.read(&b2, 100, 100);
  EXPECT_EQ(memcmp(&(b1[89]), &(b2[0]), 200), 0);
  b2.clear();
  font_is2.skip(-200);
  font_is2.read(&b2, 0, 100);
  EXPECT_EQ(memcmp(&(b1[89]), &(b2[0]), 100), 0);

  return true;
}

}  // namespace sfntly

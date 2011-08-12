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
#include "sfntly/font.h"
#include "sfntly/name_table.h"
#include "test/serialization_test.h"

namespace sfntly {

const int32_t NAME_FORMAT = 0;
const int32_t NAME_COUNT = 31;
const NameTable::NameEntryId NAME_IDS[] = {
    NameTable::NameEntryId(1, 0, 0, 0),  // 0
    NameTable::NameEntryId(1, 0, 0, 1),  // 1
    NameTable::NameEntryId(1, 0, 0, 2),  // 2
    NameTable::NameEntryId(1, 0, 0, 3),  // 3
    NameTable::NameEntryId(1, 0, 0, 4),  // 4
    NameTable::NameEntryId(1, 0, 0, 5),  // 5
    NameTable::NameEntryId(1, 0, 0, 6),  // 6
    NameTable::NameEntryId(1, 0, 0, 9),  // 7
    NameTable::NameEntryId(1, 0, 0, 11),  // 8
    NameTable::NameEntryId(1, 0, 0, 12),  // 9
    NameTable::NameEntryId(1, 0, 0, 13),  // 10
    NameTable::NameEntryId(3, 1, 1031, 2),  // 11
    NameTable::NameEntryId(3, 1, 1033, 0),  // 12
    NameTable::NameEntryId(3, 1, 1033, 1),  // 13
    NameTable::NameEntryId(3, 1, 1033, 2),  // 14
    NameTable::NameEntryId(3, 1, 1033, 3),  // 15
    NameTable::NameEntryId(3, 1, 1033, 4),  // 16
    NameTable::NameEntryId(3, 1, 1033, 5),  // 17
    NameTable::NameEntryId(3, 1, 1033, 6),  // 18
    NameTable::NameEntryId(3, 1, 1033, 9),  // 19
    NameTable::NameEntryId(3, 1, 1033, 11),  // 20
    NameTable::NameEntryId(3, 1, 1033, 12),  // 21
    NameTable::NameEntryId(3, 1, 1033, 13),  // 22
    NameTable::NameEntryId(3, 1, 1034, 2),  // 23
    NameTable::NameEntryId(3, 1, 1036, 2),  // 24
    NameTable::NameEntryId(3, 1, 1038, 2),  // 25
    NameTable::NameEntryId(3, 1, 1040, 2),  // 26
    NameTable::NameEntryId(3, 1, 1043, 2),  // 27
    NameTable::NameEntryId(3, 1, 1044, 2),  // 28
    NameTable::NameEntryId(3, 1, 1049, 2),  // 29
    NameTable::NameEntryId(3, 1, 1053, 2),  // 30
};

static bool VerifyNAME(Table* table) {
  // TODO(arthurhsu): Better testing can be done here.  Right now we just
  //                  iterate through the entries and get entry ids.
  NameTablePtr name = down_cast<NameTable*>(table);
  if (name == NULL) {
    return false;
  }

  EXPECT_EQ(name->Format(), NAME_FORMAT);
  EXPECT_EQ(name->NameCount(), NAME_COUNT);
  fprintf(stderr, "checking name entry: ");
  for (int32_t i = 0; i < NAME_COUNT; ++i) {
    fprintf(stderr, "%d ", i);
    EXPECT_EQ(name->PlatformId(i), NAME_IDS[i].platform_id());
    EXPECT_EQ(name->EncodingId(i), NAME_IDS[i].encoding_id());
    EXPECT_EQ(name->LanguageId(i), NAME_IDS[i].language_id());
    EXPECT_EQ(name->NameId(i), NAME_IDS[i].name_id());
  }
  fprintf(stderr, "\n");
  return true;
}

bool VerifyNAME(Table* original, Table* target) {
  EXPECT_TRUE(VerifyNAME(original));
  EXPECT_TRUE(VerifyNAME(target));
  return true;
}

}  // namespace sfntly

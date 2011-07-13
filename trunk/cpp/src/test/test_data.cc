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

#include "sfntly/tag.cc"
#include "test/test_data.h"

namespace sfntly {

#if defined WIN32
const char* SAMPLE_TTF_FILE = "Tuffy.ttf";
const char* SAMPLE_OTF_FILE = "Tuffy.otf";
#else
const char* SAMPLE_TTF_FILE = "Tuffy.ttf";
const char* SAMPLE_OTF_FILE = "Tuffy.otf";
#endif

const size_t SAMPLE_TTF_SIZE = 18444;
const size_t SAMPLE_TTF_TABLES = 16;
const size_t SAMPLE_TTF_KNOWN_TAGS = 15;
const size_t SAMPLE_TTF_GDEF = 0;
const size_t SAMPLE_TTF_HEAD = 8;

const int32_t TTF_KNOWN_TAGS[] = {
    Tag::GDEF, Tag::GPOS, Tag::GSUB, Tag::OS_2, Tag::cmap, Tag::cvt,
    Tag::gasp, Tag::glyf, Tag::head, Tag::hhea, Tag::hmtx, Tag::loca,
    Tag::maxp, Tag::name, Tag::post };

const int64_t TTF_CHECKSUM[] = {
    0x002900cb, 0xe1b5ef99, 0x6c91748f, 0xae08cb6b, 0xc62ba2e8, 0x00210279,
    0xffff0003, 0x8ed2ba01, 0xe8afc442, 0x0dce8e86, 0x77c44a8a, 0xbfd5cd52,
    0x011200bc, 0x2f64d0e9, 0x6548bddf };

const int64_t TTF_OFFSET[] = {
    0x4650, 0x4690, 0x4670, 0x0188, 0x04f4, 0x0738,
    0x4648, 0x08c8, 0x010c, 0x0144, 0x01e0, 0x073c,
    0x0168, 0x3d9c, 0x4468 };

const int32_t TTF_LENGTH[] = {
    0x001e, 0x015e, 0x0020, 0x0056, 0x0242, 0x0004,
    0x0008, 0x34d4, 0x0036, 0x0024, 0x0314, 0x018c,
    0x0020, 0x06cb, 0x01dd };

const unsigned char TTF_GDEF_DATA[] = {
    0, 1, 0, 0, 0, 0x0c, 0, 0, 0, 0x16, 0, 0, 0, 2, 0, 1,
    0, 3, 0, 0xc4, 0, 1, 0, 4, 0, 0, 0, 2, 0, 0 };

const unsigned char TTF_HEAD_DATA[] = {
    0, 1, 0, 0,
    0, 1, 0x19, 0x99, 9, 6, 0x34, 0xb7, 0x5f, 0xf, 0x3c, 0xf5, 0, 0xb, 8, 0,
    0, 0, 0, 0, 0xc1, 0xf5, 0x2f, 0xd2, 0, 0, 0, 0, 0xc1, 0xf5, 0x2f, 0xd2,
    0xff, 0x89, 0xfe, 0x3f, 6, 0x1d, 7, 0xc9, 0, 0, 0, 8, 0, 2, 0, 0,
    0, 0 };

const size_t SAMPLE_OTF_SIZE = 18900;

}  // namespace sfntly

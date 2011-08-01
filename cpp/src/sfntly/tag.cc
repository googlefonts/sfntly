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

#include "sfntly/tag.h"
#include "sfntly/port/endian.h"

namespace sfntly {

const int32_t Tag::ttcf = GenerateTag('t', 't', 'c', 'f');
const int32_t Tag::cmap = GenerateTag('c', 'm', 'a', 'p');
const int32_t Tag::head = GenerateTag('h', 'e', 'a', 'd');
const int32_t Tag::hhea = GenerateTag('h', 'h', 'e', 'a');
const int32_t Tag::hmtx = GenerateTag('h', 'm', 't', 'x');
const int32_t Tag::maxp = GenerateTag('m', 'a', 'x', 'p');
const int32_t Tag::name = GenerateTag('n', 'a', 'm', 'e');
const int32_t Tag::OS_2 = GenerateTag('O', 'S', '/', '2');
const int32_t Tag::post = GenerateTag('p', 'o', 's', 't');
const int32_t Tag::cvt  = GenerateTag('c', 'v', 't', ' ');
const int32_t Tag::fpgm = GenerateTag('f', 'p', 'g', 'm');
const int32_t Tag::glyf = GenerateTag('g', 'l', 'y', 'f');
const int32_t Tag::loca = GenerateTag('l', 'o', 'c', 'a');
const int32_t Tag::prep = GenerateTag('p', 'r', 'e', 'p');
const int32_t Tag::CFF  = GenerateTag('C', 'F', 'F', ' ');
const int32_t Tag::VORG = GenerateTag('V', 'O', 'R', 'G');
const int32_t Tag::EBDT = GenerateTag('E', 'B', 'D', 'T');
const int32_t Tag::EBLC = GenerateTag('E', 'B', 'L', 'C');
const int32_t Tag::EBSC = GenerateTag('E', 'B', 'S', 'C');
const int32_t Tag::BASE = GenerateTag('B', 'A', 'S', 'E');
const int32_t Tag::GDEF = GenerateTag('G', 'D', 'E', 'F');
const int32_t Tag::GPOS = GenerateTag('G', 'P', 'O', 'S');
const int32_t Tag::GSUB = GenerateTag('G', 'S', 'U', 'B');
const int32_t Tag::JSTF = GenerateTag('J', 'S', 'T', 'F');
const int32_t Tag::DSIG = GenerateTag('D', 'S', 'I', 'G');
const int32_t Tag::gasp = GenerateTag('g', 'a', 's', 'p');
const int32_t Tag::hdmx = GenerateTag('h', 'd', 'm', 'x');
const int32_t Tag::kern = GenerateTag('k', 'e', 'r', 'n');
const int32_t Tag::LTSH = GenerateTag('L', 'T', 'S', 'H');
const int32_t Tag::PCLT = GenerateTag('P', 'C', 'L', 'T');
const int32_t Tag::VDMX = GenerateTag('V', 'D', 'M', 'X');
const int32_t Tag::vhea = GenerateTag('v', 'h', 'e', 'a');
const int32_t Tag::vmtx = GenerateTag('v', 'm', 't', 'x');
const int32_t Tag::bhed = GenerateTag('b', 'h', 'e', 'd');
const int32_t Tag::bdat = GenerateTag('b', 'd', 'a', 't');
const int32_t Tag::bloc = GenerateTag('b', 'l', 'o', 'c');

const int32_t CFF_TABLE_ORDERING[] = {
    Tag::head,
    Tag::hhea,
    Tag::maxp,
    Tag::OS_2,
    Tag::name,
    Tag::cmap,
    Tag::post,
    Tag::CFF };
const size_t CFF_TABLE_ORDERING_SIZE =
    sizeof(CFF_TABLE_ORDERING) / sizeof(int32_t);

const int32_t TRUE_TYPE_TABLE_ORDERING[] = {
    Tag::head,
    Tag::hhea,
    Tag::maxp,
    Tag::OS_2,
    Tag::hmtx,
    Tag::LTSH,
    Tag::VDMX,
    Tag::hdmx,
    Tag::cmap,
    Tag::fpgm,
    Tag::prep,
    Tag::cvt,
    Tag::loca,
    Tag::glyf,
    Tag::kern,
    Tag::name,
    Tag::post,
    Tag::gasp,
    Tag::PCLT,
    Tag::DSIG };
const size_t TRUE_TYPE_TABLE_ORDERING_SIZE =
    sizeof(TRUE_TYPE_TABLE_ORDERING) / sizeof(int32_t);

}  // namespace sfntly

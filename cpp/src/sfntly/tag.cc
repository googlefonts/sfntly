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

const int32_t Tag::ttcf = generate_tag('t', 't', 'c', 'f');
const int32_t Tag::cmap = generate_tag('c', 'm', 'a', 'p');
const int32_t Tag::head = generate_tag('h', 'e', 'a', 'd');
const int32_t Tag::hhea = generate_tag('h', 'h', 'e', 'a');
const int32_t Tag::hmtx = generate_tag('h', 'm', 't', 'x');
const int32_t Tag::maxp = generate_tag('m', 'a', 'x', 'p');
const int32_t Tag::name = generate_tag('n', 'a', 'm', 'e');
const int32_t Tag::OS_2 = generate_tag('O', 'S', '/', '2');
const int32_t Tag::post = generate_tag('p', 'o', 's', 't');
const int32_t Tag::cvt  = generate_tag('c', 'v', 't', ' ');
const int32_t Tag::fpgm = generate_tag('f', 'p', 'g', 'm');
const int32_t Tag::glyf = generate_tag('g', 'l', 'y', 'f');
const int32_t Tag::loca = generate_tag('l', 'o', 'c', 'a');
const int32_t Tag::prep = generate_tag('p', 'r', 'e', 'p');
const int32_t Tag::CFF  = generate_tag('C', 'F', 'F', ' ');
const int32_t Tag::VORG = generate_tag('V', 'O', 'R', 'G');
const int32_t Tag::EBDT = generate_tag('E', 'B', 'D', 'T');
const int32_t Tag::EBLC = generate_tag('E', 'B', 'L', 'C');
const int32_t Tag::EBSC = generate_tag('E', 'B', 'S', 'C');
const int32_t Tag::BASE = generate_tag('B', 'A', 'S', 'E');
const int32_t Tag::GDEF = generate_tag('G', 'D', 'E', 'F');
const int32_t Tag::GPOS = generate_tag('G', 'P', 'O', 'S');
const int32_t Tag::GSUB = generate_tag('G', 'S', 'U', 'B');
const int32_t Tag::JSTF = generate_tag('J', 'S', 'T', 'F');
const int32_t Tag::DSIG = generate_tag('D', 'S', 'I', 'G');
const int32_t Tag::gasp = generate_tag('g', 'a', 's', 'p');
const int32_t Tag::hdmx = generate_tag('h', 'd', 'm', 'x');
const int32_t Tag::kern = generate_tag('k', 'e', 'r', 'n');
const int32_t Tag::LTSH = generate_tag('L', 'T', 'S', 'H');
const int32_t Tag::PCLT = generate_tag('P', 'C', 'L', 'T');
const int32_t Tag::VDMX = generate_tag('V', 'D', 'M', 'X');
const int32_t Tag::vhea = generate_tag('v', 'h', 'e', 'a');
const int32_t Tag::vmtx = generate_tag('v', 'm', 't', 'x');
const int32_t Tag::bhed = generate_tag('b', 'h', 'e', 'd');
const int32_t Tag::bdat = generate_tag('b', 'd', 'a', 't');
const int32_t Tag::bloc = generate_tag('b', 'l', 'o', 'c');

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

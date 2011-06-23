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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_TAG_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_TAG_H_

#include "sfntly/port/type.h"

namespace sfntly {

// Font identification tags used for tables, features, etc.
// Tag names are consistent with the OpenType and sfnt specs.
struct Tag {
  static const int32_t ttcf;

  // Table Type Tags
  // required tables
  static const int32_t cmap;
  static const int32_t head;
  static const int32_t hhea;
  static const int32_t hmtx;
  static const int32_t maxp;
  static const int32_t name;
  static const int32_t OS_2;
  static const int32_t post;

  // TrueType outline tables
  static const int32_t cvt;
  static const int32_t fpgm;
  static const int32_t glyf;
  static const int32_t loca;
  static const int32_t prep;

  // PostScript outline tables
  static const int32_t CFF;
  static const int32_t VORG;

  // bitmap glyph outlines
  static const int32_t EBDT;
  static const int32_t EBLC;
  static const int32_t EBSC;

  // advanced typographic features
  static const int32_t BASE;
  static const int32_t GDEF;
  static const int32_t GPOS;
  static const int32_t GSUB;
  static const int32_t JSTF;

  // other
  static const int32_t DSIG;
  static const int32_t gasp;
  static const int32_t hdmx;
  static const int32_t kern;
  static const int32_t LTSH;
  static const int32_t PCLT;
  static const int32_t VDMX;
  static const int32_t vhea;
  static const int32_t vmtx;

  // GX Tables
  // TODO(stuartg): add these tables

  // Bitmap font tables
  static const int32_t bhed;
  static const int32_t bdat;
  static const int32_t bloc;
};

inline int32_t generate_tag(char a, char b, char c, char d) {
  return (((int32_t)(a) << 24) |
          ((int32_t)(b) << 16) |
          ((int32_t)(c) <<  8) |
           (int32_t)(d));
}

// Note: For Java, these two orderings are in Font class.  Moved here to avoid
//       VC++ bug of not populating correct values.
extern const int32_t CFF_TABLE_ORDERING[];
extern const size_t CFF_TABLE_ORDERING_SIZE;
extern const int32_t TRUE_TYPE_TABLE_ORDERING[];
extern const size_t TRUE_TYPE_TABLE_ORDERING_SIZE;

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_TAG_H_

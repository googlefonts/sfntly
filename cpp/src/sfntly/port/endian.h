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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_PORT_ENDIAN_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_PORT_ENDIAN_H_

#include "sfntly/port/config.h"
#include "sfntly/port/type.h"

namespace sfntly {

static inline uint16_t endian_swap16(uint16_t value) {
  return (uint16_t)((value >> 8) | (value << 8));
}

static inline int32_t endian_swap32(int32_t value) {
  return (((value & 0x000000ff) << 24) |
          ((value & 0x0000ff00) <<  8) |
          ((value & 0x00ff0000) >>  8) |
          ((value & 0xff000000) >> 24));
}

static inline uint64_t endian_swap64(uint64_t value) {
  return (((value & 0x00000000000000ffLL) << 56) |
          ((value & 0x000000000000ff00LL) << 40) |
          ((value & 0x0000000000ff0000LL) << 24) |
          ((value & 0x00000000ff000000LL) << 8)  |
          ((value & 0x000000ff00000000LL) >> 8)  |
          ((value & 0x0000ff0000000000LL) >> 24) |
          ((value & 0x00ff000000000000LL) >> 40) |
          ((value & 0xff00000000000000LL) >> 56));
}

#ifdef SFNTLY_LITTLE_ENDIAN
  #define toBE16(n) endian_swap16(n)
  #define toBE32(n) endian_swap32(n)
  #define toBE64(n) endian_swap64(n)
  #define toLE16(n) (n)
  #define toLE32(n) (n)
  #define toLE64(n) (n)
  #define fromBE16(n) endian_swap16(n)
  #define fromBE32(n) endian_swap32(n)
  #define fromBE64(n) endian_swap64(n)
  #define fromLE16(n) (n)
  #define fromLE32(n) (n)
  #define fromLE64(n) (n)
#else  // SFNTLY_BIG_ENDIAN
  #define toBE16(n) (n)
  #define toBE32(n) (n)
  #define toBE64(n) (n)
  #define toLE16(n) endian_swap16(n)
  #define toLE32(n) endian_swap32(n)
  #define toLE64(n) endian_swap64(n)
  #define fromBE16(n) (n)
  #define fromBE32(n) (n)
  #define fromBE64(n) (n)
  #define fromLE16(n) endian_swap16(n)
  #define fromLE32(n) endian_swap32(n)
  #define fromLE64(n) endian_swap64(n)
#endif

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_PORT_ENDIAN_H_

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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_PORT_ATOMIC_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_PORT_ATOMIC_H_

#if defined (WIN32)

#include <windows.h>

static inline size_t atomicIncrement(size_t* address) {
#if defined (_WIN64)
  return InterlockedIncrement64(reinterpret_cast<LONGLONG*>(address));
#else
  return InterlockedIncrement(reinterpret_cast<LONG*>(address));
#endif
}

static inline size_t atomicDecrement(size_t* address) {
#if defined (_WIN64)
  return InterlockedDecrement64(reinterpret_cast<LONGLONG*>(address));
#else
  return InterlockedDecrement(reinterpret_cast<LONG*>(address));
#endif
}

#elif defined (__APPLE__)

#include <libkern/OSAtomic.h>

static inline size_t atomicIncrement(size_t* address) {
  return OSAtomicIncrement32Barrier(reinterpret_cast<int32_t*>(address));
}

static inline size_t atomicDecrement(size_t* address) {
  return OSAtomicDecrement32Barrier(reinterpret_cast<int32_t*>(address));
}

#elif defined (__GCC_HAVE_SYNC_COMPARE_AND_SWAP_4)

#include <stddef.h>

static inline size_t atomicIncrement(size_t* address) {
  return __sync_add_and_fetch(address, 1);
}

static inline size_t atomicDecrement(size_t* address) {
  return __sync_sub_and_fetch(address, 1);
}

#else

#error "Compiler not supported"

#endif  // WIN32

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_PORT_ATOMIC_H_

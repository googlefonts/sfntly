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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_FONT_OUTPUT_STREAM_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_FONT_OUTPUT_STREAM_H_

#include "sfntly/port/type.h"
#include "sfntly/port/output_stream.h"

namespace sfntly {

// Note: Original class inherits from Java's FilterOutputStream, which wraps
//       an InputStream within.  In C++, we directly do the wrapping without
//       defining another layer of abstraction.  The wrapped output stream is
//       *NOT* reference counted (because it's meaningless to ref-count an I/O
//       stream).
class FontOutputStream : public OutputStream {
 public:
  explicit FontOutputStream(OutputStream* os);
  virtual ~FontOutputStream();

  virtual size_t position();

  virtual void write(byte_t b);
  virtual void write(ByteVector* b);
  virtual void write(ByteVector* b, int32_t offset, int32_t len);
  virtual void writeChar(byte_t c);
  virtual void writeUShort(int32_t us);
  virtual void writeShort(int32_t s);
  virtual void writeUInt24(int32_t ui);
  virtual void writeULong(int64_t ul);
  virtual void writeLong(int64_t l);
  virtual void writeFixed(int32_t l);
  virtual void writeDateTime(int64_t date);

  virtual void flush();
  virtual void close();

 private:
  OutputStream* stream_;
  size_t position_;
};

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_FONT_OUTPUT_STREAM_H_

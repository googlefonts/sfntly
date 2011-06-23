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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_FONT_INPUT_STREAM_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_FONT_INPUT_STREAM_H_

#include "sfntly/port/type.h"
#include "sfntly/port/input_stream.h"

namespace sfntly {

// Note: Original class inherits from Java's FilterOutputStream, which wraps
//       an InputStream within.  In C++, we directly do the wrapping without
//       defining another layer of abstraction.  The wrapped output stream is
//       *NOT* reference counted (because it's meaningless to ref-count an I/O
//       stream).
class FontInputStream : public InputStream {
 public:
  explicit FontInputStream(InputStream* is);
  FontInputStream(InputStream* is, size_t length);
  virtual ~FontInputStream();

  virtual int32_t available();
  virtual void close();
  virtual void mark(int32_t readlimit);
  virtual bool markSupported();
  virtual void reset();

  virtual int32_t read();
  virtual int32_t read(ByteVector* buffer);
  virtual int32_t read(ByteVector* buffer, int32_t offset, int32_t length);

  virtual int64_t position();

  virtual int32_t readChar();
  virtual int32_t readUShort();
  virtual int32_t readShort();
  virtual int32_t readUInt24();
  virtual int64_t readULong();
  virtual int32_t readULongAsInt();
  virtual int32_t readLong();
  virtual int32_t readFixed();
  virtual int64_t readDateTimeAsLong();
  virtual int64_t skip(int64_t n);  // n can be negative

 private:
  InputStream* stream_;
  int64_t position_;
  int64_t length_;  // bound on length of data to read
  bool bounded_;
};

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_FONT_INPUT_STREAM_H_

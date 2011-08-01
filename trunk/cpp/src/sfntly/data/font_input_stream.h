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

  virtual int32_t Available();
  virtual void Close();
  virtual void Mark(int32_t readlimit);
  virtual bool MarkSupported();
  virtual void Reset();

  virtual int32_t Read();
  virtual int32_t Read(ByteVector* buffer);
  virtual int32_t Read(ByteVector* buffer, int32_t offset, int32_t length);

  virtual int64_t position() { return position_; }

  virtual int32_t ReadChar();
  virtual int32_t ReadUShort();
  virtual int32_t ReadShort();
  virtual int32_t ReadUInt24();
  virtual int64_t ReadULong();
  virtual int32_t ReadULongAsInt();
  virtual int32_t ReadLong();
  virtual int32_t ReadFixed();
  virtual int64_t ReadDateTimeAsLong();
  virtual int64_t Skip(int64_t n);  // n can be negative.

 private:
  InputStream* stream_;
  int64_t position_;
  int64_t length_;  // Bound on length of data to read.
  bool bounded_;
};

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_DATA_FONT_INPUT_STREAM_H_

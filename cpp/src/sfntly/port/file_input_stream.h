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

#ifndef TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_PORT_FILE_INPUT_STREAM_H_
#define TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_PORT_FILE_INPUT_STREAM_H_

#include <stdio.h>

#include "sfntly/port/input_stream.h"

namespace sfntly {

class FileInputStream : public PushbackInputStream {
 public:
  FileInputStream();
  virtual ~FileInputStream();

  // InputStream methods
  virtual int32_t available();
  virtual void close();
  virtual void mark(int32_t readlimit);
  virtual bool markSupported();
  virtual int32_t read();
  virtual int32_t read(ByteVector* b);
  virtual int32_t read(ByteVector* b, int32_t offset, int32_t length);
  virtual void reset();
  virtual int64_t skip(int64_t n);

  // PushbackInputStream methods
  virtual void unread(ByteVector* b);
  virtual void unread(ByteVector* b, int32_t offset, int32_t length);

  // Own methods
  virtual bool open(const char* file_path);

 private:
  FILE* file_;
  size_t position_;
  size_t length_;
};

}  // namespace sfntly

#endif  // TYPOGRAPHY_FONT_SFNTLY_SRC_SFNTLY_PORT_FILE_INPUT_STREAM_H_

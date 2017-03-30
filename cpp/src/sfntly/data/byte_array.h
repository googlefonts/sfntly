/*
 * Copyright (C) 2011 The sfntly Open Source Project
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

#ifndef SFNTLY_CPP_SRC_SFNTLY_DATA_BYTE_ARRAY_H_
#define SFNTLY_CPP_SRC_SFNTLY_DATA_BYTE_ARRAY_H_

#include "sfntly/port/refcount.h"
#include "sfntly/port/type.h"
#include "sfntly/port/input_stream.h"
#include "sfntly/port/output_stream.h"

#include <cstring>

namespace sfntly {

// An abstraction to a contiguous array of bytes.
// C++ port of this class assumes that the data are stored in a linear region
// like std::vector.
class ByteArray : public RefCounted<ByteArray> {
 public:
  ByteArray() {}
  ~ByteArray() {}

  // Gets the current filled and readable length of the array.
  int32_t Length() const {
    assert(data_.size() <= INT32_MAX);
    return static_cast<int32_t>(data_.size());
  }

  // Gets the maximum size of the array. This is the maximum number of bytes that
  // the array can hold and all of it may not be filled with data or even fully
  // allocated yet.
  int32_t Size() const { return INT32_MAX; }

  // Determines whether or not this array is growable or of fixed size.
  bool growable() const { return true; }

  int32_t SetFilledLength(int32_t filled_length) {
    data_.resize(filled_length);
    return filled_length;
  }

  // Gets the byte from the given index.
  // @param index the index into the byte array
  // @return the byte or -1 if reading beyond the bounds of the data
  int32_t Get(int32_t index) const {
    return index >= 0 && index < Length() ? data_[index] : -1;
  }

  // Gets the bytes from the given index and fill the buffer with them. As many
  // bytes as will fit into the buffer are read unless that would go past the
  // end of the array.
  // @param index the index into the byte array
  // @param b the buffer to put the bytes read into
  // @return the number of bytes read from the buffer
  int32_t Get(int32_t index, std::vector<uint8_t>* b) const {
    assert(b);
    return Get(index, b->data(), 0, b->size());
  }

  // Gets the bytes from the given index and fill the buffer with them starting
  // at the offset given. As many bytes as the specified length are read unless
  // that would go past the end of the array.
  // @param index the index into the byte array
  // @param b the buffer to put the bytes read into
  // @param offset the location in the buffer to start putting the bytes
  // @param length the number of bytes to put into the buffer
  // @return the number of bytes read from the buffer
  int32_t Get(int32_t index,
              uint8_t* b,
              int32_t offset,
              int32_t length) const {
    if (!b || !length || index < 0 || index >= Length()) {
      return 0;
    }
    int32_t actual_length = std::min<int32_t>(length, Length() - index);
    std::memcpy(b + offset, data_.data() + index, actual_length);
    return actual_length;
  }

  // Puts the specified byte into the array at the given index unless that would
  // be beyond the length of the array and it isn't growable.
  void Put(int32_t index, uint8_t b) {
    if (index < 0 || index >= Size()) {
    #if defined (SFNTLY_NO_EXCEPTION)
      assert(false);
      return;
    #else
      throw IndexOutOfBoundException(
          "Attempt to write outside the bounds of the data");
    #endif
    }
    if (index >= Length()) {
      data_.resize(static_cast<size_t>(index) + 1);
    }
    data_[index] = b;
  }

  // Puts the specified bytes into the array at the given index. The entire
  // buffer is put into the array unless that would extend beyond the length and
  // the array isn't growable.
  int32_t Put(int32_t index, std::vector<uint8_t>* b) {
    assert(b);
    return Put(index, b->data(), 0, b->size());
  }

  // Puts the specified bytes into the array at the given index. All of the bytes
  // specified are put into the array unless that would extend beyond the length
  // and the array isn't growable. The bytes to be put into the array are those
  // in the buffer from the given offset and for the given length.
  // @param index the index into the ByteArray
  // @param b the bytes to put into the array
  // @param offset the offset in the bytes to start copying from
  // @param length the number of bytes to copy into the array
  // @return the number of bytes actually written
  int32_t Put(int32_t index,
              uint8_t* b,
              int32_t offset,
              int32_t length) {
    if (!b || length <= 0) {
      return 0;
    }
    if (index < 0 || index + length > Size()) {
    #if defined (SFNTLY_NO_EXCEPTION)
      return 0;
    #else
      throw IndexOutOfBoundException(
          "Attempt to write outside the bounds of the data");
    #endif
    }
    if ((size_t)index + (size_t)length >= data_.size()) {
      data_.resize((size_t)(index + length));
    }
    std::memcpy(data_.data() + index, b + offset, length);
    return length;
  }

  // Fully copies this ByteArray to another ByteArray to the extent that the
  // destination array has storage for the data copied.
  int32_t CopyTo(ByteArray* array) const {
    return CopyTo(array, 0, Length());
  }

  // Copies a segment of this ByteArray to another ByteArray.
  // @param array the destination
  // @param offset the offset in this ByteArray to start copying from
  // @param length the maximum length in bytes to copy
  // @return the number of bytes copied
  int32_t CopyTo(ByteArray* array, int32_t offset, int32_t length) const {
    return CopyTo(0, array, offset, length);
  }

  // Copies this ByteArray to another ByteArray.
  // @param dstOffset the offset in the destination array to start copying to
  // @param array the destination
  // @param srcOffset the offset in this ByteArray to start copying from
  // @param length the maximum length in bytes to copy
  // @return the number of bytes copied
  int32_t CopyTo(int32_t dst_offset,
                 ByteArray* array,
                 int32_t src_offset,
                 int32_t length) const {
    assert(array);
    if (array->Size() < dst_offset + length) {
      return -1;
    }
    if (src_offset >= Length()) {
      return 0;
    }
    int32_t actual_length = std::min<int32_t>(length, Length() - src_offset);
    if (array->Length() < dst_offset + actual_length) {
      array->data_.resize(dst_offset + actual_length);
    }
    std::memcpy(array->data_.data() + dst_offset, data_.data() + src_offset, actual_length);
    return actual_length;
  }

  // Copies this ByteArray to an OutputStream.
  // @param os the destination
  // @return the number of bytes copied
  int32_t CopyTo(OutputStream* os) {
    return CopyTo(os, 0, Length());
  }

  // Copies this ByteArray to an OutputStream.
  // @param os the destination
  // @param offset
  // @param length
  // @return the number of bytes copied
  int32_t CopyTo(OutputStream* os, int32_t offset, int32_t length) {
    assert(os);
    if (!os || offset >= Length()) {
      return 0;
    }
    int32_t actual_length = std::min<int32_t>(length, Length() - offset);
    os->Write(data_.data() + offset, 0, actual_length);
    return actual_length;
  }

  // Copies from the InputStream into this ByteArray.
  // @param is the source
  // @param length the number of bytes to copy
  bool CopyFrom(InputStream* is, int32_t length) {
    static const int32_t COPY_BUFFER_SIZE = 8192;
    std::vector<uint8_t> buffer(COPY_BUFFER_SIZE);
    int32_t bytes_read = 0;
    int32_t index = 0;
    while ((bytes_read = is->Read(&buffer, 0, COPY_BUFFER_SIZE)) > 0) {
      if (bytes_read != Put(index, buffer.data(), 0, bytes_read)) {
      #if defined (SFNTLY_NO_EXCEPTION)
        return 0;
      #else
        throw IOException("Error writing bytes.");
      #endif
      }
      index += bytes_read;
    }
    return true;
  }

  // Copies everything from the InputStream into this ByteArray.
  // @param is the source
  bool CopyFrom(InputStream* is) {
    static const int32_t COPY_BUFFER_SIZE = 8192;
    std::vector<uint8_t> b(COPY_BUFFER_SIZE);
    int32_t bytes_read = 0;
    int32_t index = 0;
    while ((bytes_read = is->Read(&b, 0, COPY_BUFFER_SIZE)) > 0) {
      if (Put(index, &b[0], 0, bytes_read) != bytes_read) {
      #if defined (SFNTLY_NO_EXCEPTION)
        return 0;
      #else
        throw IOException("Error writing bytes.");
      #endif
      }
      index += bytes_read;
    }
    return true;
  }

 private:
  std::vector<uint8_t> data_;
};
typedef Ptr<ByteArray> ByteArrayPtr;

}  // namespace sfntly

#endif  // SFNTLY_CPP_SRC_SFNTLY_DATA_BYTE_ARRAY_H_

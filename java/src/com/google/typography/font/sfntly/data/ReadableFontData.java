/*
 * Copyright 2010 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.typography.font.sfntly.data;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

/**
 * Readable font data wrapper. Supports reading of data primitives in the TrueType / OpenType spec.
 *
 * <p>The data types used are:
 *
 * <table>
 * <tr>
 * <td>BYTE</td>
 * <td>8-bit unsigned integer.</td>
 * </tr>
 * <tr>
 * <td>CHAR</td>
 * <td>8-bit signed integer.</td>
 * </tr>
 * <tr>
 * <td>USHORT</td>
 * <td>16-bit unsigned integer.</td>
 * </tr>
 * <tr>
 * <td>SHORT</td>
 * <td>16-bit signed integer.</td>
 * </tr>
 * <tr>
 * <td>UINT24</td>
 * <td>24-bit unsigned integer.</td>
 * </tr>
 * <tr>
 * <td>ULONG</td>
 * <td>32-bit unsigned integer.</td>
 * </tr>
 * <tr>
 * <td>LONG</td>
 * <td>32-bit signed integer.</td>
 * </tr>
 * <tr>
 * <td>Fixed</td>
 * <td>32-bit signed fixed-point number (16.16)</td>
 * </tr>
 * <tr>
 * <td>FUNIT</td>
 * <td>Smallest measurable distance in the em space.</td>
 * </tr>
 * <tr>
 * <td>FWORD</td>
 * <td>16-bit signed integer (SHORT) that describes a quantity in FUnits.</td>
 * </tr>
 * <tr>
 * <td>UFWORD</td>
 * <td>16-bit unsigned integer (USHORT) that describes a quantity in FUnits.
 * </td>
 * </tr>
 * <tr>
 * <td>F2DOT14</td>
 * <td>16-bit signed fixed number with the low 14 bits of fraction (2.14).</td>
 * </tr>
 * <tr>
 * <td>LONGDATETIME</td>
 * <td>Date represented in number of seconds since 12:00 midnight, January 1,
 * 1904. The value is represented as a signed 64-bit integer.</td>
 * </tr>
 * </table>
 *
 * @author Stuart Gill
 * @see WritableFontData
 */
public class ReadableFontData extends FontData {

  public static ReadableFontData createReadableFontData(byte[] b) {
    ByteArray ba = new MemoryByteArray(b);
    return new ReadableFontData(ba);
  }

  /** Flag on whether the checksum has been set. */
  private volatile boolean checksumSet = false;
  /** Lock on all operations that will affect the value of the checksum. */
  private final Object checksumLock = new Object();

  private volatile long checksum;
  private volatile int[] checksumRange;

  /** @param array byte array to wrap */
  protected ReadableFontData(ByteArray array) {
    super(array);
  }

  /**
   * Creates a bounded wrapper of another ReadableFontData from the given offset until the end of
   * the original ReadableFontData.
   *
   * @param data data to wrap
   * @param offset the start of this data's view of the original data
   */
  protected ReadableFontData(ReadableFontData data, int offset) {
    super(data, offset);
  }

  /**
   * Creates a bounded wrapper of another ReadableFontData from the given offset and length.
   *
   * @param data data to wrap
   * @param offset the start of this data's view of the original data
   * @param length the length of the other FontData to use
   */
  protected ReadableFontData(ReadableFontData data, int offset, int length) {
    super(data, offset, length);
  }

  /**
   * Makes a slice of this FontData. The returned slice will share the data with the original {@code
   * FontData}.
   *
   * @param offset the start of the slice
   * @param length the number of bytes in the slice
   * @return a slice of the original FontData
   */
  @Override
  public ReadableFontData slice(int offset, int length) {
    if (offset < 0
        || length < 0
        || offset > Integer.MAX_VALUE - length
        || (offset + length) > size()) {
      throw new IndexOutOfBoundsException("Attempt to bind data outside of its limits.");
    }
    ReadableFontData slice = new ReadableFontData(this, offset, length);
    return slice;
  }

  /**
   * Makes a bottom bound only slice of this array. The returned slice will share the data with the
   * original {@code FontData}.
   *
   * @param offset the start of the slice
   * @return a slice of the original FontData
   */
  @Override
  public ReadableFontData slice(int offset) {
    if (offset < 0 || offset > size()) {
      throw new IndexOutOfBoundsException("Attempt to bind data outside of its limits.");
    }
    ReadableFontData slice = new ReadableFontData(this, offset);
    return slice;
  }

  /**
   * Generates a String representation of the object with a certain number of data bytes.
   *
   * @param length number of bytes of the data to include in the String
   * @return String representation of the object
   */
  public String toString(int length) {
    return String.format(
        "[l=%d, cs=%d]\n%s",
        length(), checksum(), array.toString(boundOffset(0), boundLength(0, length)));
  }

  @Override
  public String toString() {
    return toString(0);
  }

  /**
   * Gets a computed checksum for the data. This checksum uses the OpenType spec calculation. Every
   * ULong value (32 bit unsigned) in the data is summed and the resulting value is truncated to 32
   * bits. If the data length in bytes is not an integral multiple of 4 then any remaining bytes are
   * treated as the start of a 4 byte sequence whose remaining bytes are zero.
   */
  public long checksum() {
    if (!checksumSet) {
      computeChecksum();
    }
    return checksum;
  }

  /**
   * Computes the checksum for the font data using any ranges set for the calculation. Updates the
   * internal state of this object in a threadsafe way.
   */
  private void computeChecksum() {
    synchronized (checksumLock) {
      if (checksumSet) {
        // another thread computed the checksum while were waiting to do so
        return;
      }
      long sum = 0;
      if (checksumRange == null) {
        sum = computeCheckSum(0, length());
      } else {
        for (int lowBoundIndex = 0; lowBoundIndex < checksumRange.length; lowBoundIndex += 2) {
          int lowBound = checksumRange[lowBoundIndex];
          int highBound =
              (lowBoundIndex == checksumRange.length - 1)
                  ? length()
                  : checksumRange[lowBoundIndex + 1];
          sum += computeCheckSum(lowBound, highBound);
        }
      }
      this.checksum = sum & 0xffffffffL;
      this.checksumSet = true;
    }
  }

  /**
   * Do the actual computation of the checksum for a range using the TrueType/OpenType checksum
   * algorithm. The range used is from the low bound to the high bound in steps of four bytes. If
   * any of the bytes within that 4 byte segment are not readable then it will considered a zero for
   * calculation.
   *
   * <p>Only called from within a synchronized method so it does not need to be synchronized itself.
   *
   * @param lowBound first position to start a 4 byte segment on
   * @param highBound last possible position to start a 4 byte segment on
   * @return the checksum for the total range
   */
  private long computeCheckSum(int lowBound, int highBound) {
    long sum = 0;
    // checksum all whole 4-byte chunks
    for (int i = lowBound; i <= highBound - 4; i += 4) {
      sum += readULong(i);
    }
    // add last fragment if not 4-byte multiple
    int off = highBound & -4;
    if (off < highBound) {
      int b3 = readUByte(off);
      int b2 = (off + 1 < highBound) ? readUByte(off + 1) : 0;
      int b1 = (off + 2 < highBound) ? readUByte(off + 2) : 0;
      int b0 = 0;
      sum += (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }
    return sum;
  }

  /**
   * Sets the ranges to use for computing the checksum. These ranges are in begin and end pairs. If
   * an odd number is given then the final range is assumed to extend to the end of the data. The
   * lengths of each range must be a multiple of 4.
   *
   * @param ranges the range bounds to use for the checksum
   */
  public void setCheckSumRanges(int... ranges) {
    synchronized (checksumLock) {
      if (ranges != null && ranges.length > 0) {
        this.checksumRange = Arrays.copyOf(ranges, ranges.length);
      } else {
        this.checksumRange = null;
      }
      this.checksumSet = false;
    }
  }

  /**
   * Gets the ranges that are used for computing the checksum. These ranges are in begin and end
   * pairs. If an odd number is given then the final range is assumed to extend to the end of the
   * data. The lengths of each range must be a multiple of 4.
   *
   * @return the range bounds used for the checksum
   */
  public int[] checkSumRange() {
    synchronized (checksumLock) {
      if (checksumRange != null && checksumRange.length > 0) {
        return Arrays.copyOf(checksumRange, checksumRange.length);
      }
      return new int[0];
    }
  }

  /**
   * Reads the UBYTE at the given index.
   *
   * @param index index into the font data
   * @return the UBYTE; -1 if outside the bounds of the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public int readUByte(int index) {
    if (!boundsCheck(index, 1)) {
      throw new IndexOutOfBoundsException(
          "Index attempted to be read from is out of bounds: " + Integer.toHexString(index));
    }
    int b = array.get(boundOffset(index));
    if (b < 0) {
      throw new IndexOutOfBoundsException(
          "Index attempted to be read from is out of bounds: " + Integer.toHexString(index));
    }
    return b;
    // return this.array.get(this.boundOffset(index));
  }

  /**
   * Reads the BYTE at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public int readByte(int index) {
    if (!boundsCheck(index, 1)) {
      throw new IndexOutOfBoundsException(
          "Index attempted to be read from is out of bounds: " + Integer.toHexString(index));
    }
    int b = array.get(boundOffset(index));
    if (b < 0) {
      throw new IndexOutOfBoundsException(
          "Index attempted to be read from is out of bounds: " + Integer.toHexString(index));
    }
    return (b << 24) >> 24;
    // return (this.array.get(this.boundOffset(index)) << 24) >> 24;
  }

  /**
   * Reads the bytes at the given index into the array.
   *
   * @param index index into the font data
   * @param b the destination for the bytes read
   * @param offset offset in the byte array to place the bytes
   * @param length the length of bytes to read
   * @return the number of bytes actually read; -1 if the index is outside the bounds of the font
   *     data
   */
  public int readBytes(int index, byte[] b, int offset, int length) {
    int bytesRead = array.get(boundOffset(index), b, offset, boundLength(index, length));
    if (bytesRead < 0) {
      throw new IndexOutOfBoundsException(
          "Index attempted to be read from is out of bounds: " + Integer.toHexString(index));
    }
    return bytesRead;
  }

  /**
   * Reads the CHAR at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public int readChar(int index) {
    return readUByte(index);
  }

  /**
   * Reads the USHORT at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public int readUShort(int index) {
    return 0xffff & (readUByte(index) << 8 | readUByte(index + 1));
  }

  /**
   * Reads the SHORT at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public int readShort(int index) {
    return ((readByte(index) << 8 | readUByte(index + 1)) << 16) >> 16;
  }

  /**
   * Reads the UINT24 at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public int readUInt24(int index) {
    return 0xffffff & (readUByte(index) << 16 | readUByte(index + 1) << 8 | readUByte(index + 2));
  }

  /**
   * Reads the ULONG at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public long readULong(int index) {
    return 0xffffffffL
        & (readUByte(index) << 24
            | readUByte(index + 1) << 16
            | readUByte(index + 2) << 8
            | readUByte(index + 3));
  }

  /**
   * Reads the ULONG at the given index as an int.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   * @throws ArithmeticException if the value will not fit into an integer
   */
  public int readULongAsInt(int index) {
    long ulong = readULong(index);
    if ((ulong & 0x80000000) == 0x80000000) {
      throw new ArithmeticException("Long value too large to fit into an integer.");
    }
    return (int) ulong;
  }

  /**
   * Reads the ULONG at the given index, little-endian variant.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public long readULongLE(int index) {
    return 0xffffffffL
        & (readUByte(index)
            | readUByte(index + 1) << 8
            | readUByte(index + 2) << 16
            | readUByte(index + 3) << 24);
  }

  /**
   * Reads the LONG at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public int readLong(int index) {
    return readByte(index) << 24
        | readUByte(index + 1) << 16
        | readUByte(index + 2) << 8
        | readUByte(index + 3);
  }

  /**
   * Reads the Fixed at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public int readFixed(int index) {
    return readLong(index);
  }

  /**
   * Reads the F2DOT14 at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public BigDecimal readF2Dot14(int index) {
    throw new UnsupportedOperationException();
  }

  /**
   * Reads the LONGDATETIME at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public long readDateTimeAsLong(int index) {
    return readULong(index) << 32 | readULong(index + 4);
  }

  /**
   * Reads the LONGDATETIME at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public Date readLongDateTime(int index) {
    throw new UnsupportedOperationException();
  }

  /**
   * Reads the FUNIT at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public int readFUnit(int index) {
    throw new UnsupportedOperationException();
  }

  /**
   * Reads the FWORD at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public int readFWord(int index) {
    return readShort(index);
  }

  /**
   * Reads the UFWORD at the given index.
   *
   * @param index index into the font data
   * @throws IndexOutOfBoundsException if index is outside the FontData's range
   */
  public int readUFWord(int index) {
    return readUShort(index);
  }

  /**
   * Copy the FontData to an OutputStream.
   *
   * @return number of bytes copied
   */
  public int copyTo(OutputStream os) throws IOException {
    return array.copyTo(os, boundOffset(0), length());
  }

  /**
   * Copies the FontData to a WritableFontData.
   *
   * @param wfd the destination
   * @return number of bytes copied
   */
  public int copyTo(WritableFontData wfd) {
    return array.copyTo(wfd.boundOffset(0), wfd.array, boundOffset(0), length());
  }

  /**
   * Search for the key value in the range tables provided.
   *
   * <p>The search looks through the start-end pairs looking for the key value. It is assumed that
   * the start-end pairs are both represented by UShort values, ranges do not overlap, and are
   * monotonically increasing.
   *
   * @param startIndex the position to read the first start value from
   * @param startOffset the offset between subsequent start values
   * @param endIndex the position to read the first end value from
   * @param endOffset the offset between subsequent end values
   * @param length the number of start-end pairs
   * @param key the value to search for
   * @return the index of the start-end pairs in which the key was found; -1 otherwise
   */
  public int searchUShort(
      int startIndex, int startOffset, int endIndex, int endOffset, int length, int key) {
    int location = 0;
    int bottom = 0;
    int top = length;
    while (top != bottom) {
      location = (top + bottom) / 2;
      int locationStart = readUShort(startIndex + location * startOffset);
      if (key < locationStart) {
        // location is below current location
        top = location;
      } else {
        // is key below the upper bound?
        int locationEnd = readUShort(endIndex + location * endOffset);
        if (key <= locationEnd) {
          return location;
        }
        // location is above the current location
        bottom = location + 1;
      }
    }
    return -1;
  }

  /**
   * Search for the key value in the range tables provided.
   *
   * <p>The search looks through the start-end pairs looking for the key value. It is assumed that
   * the start-end pairs are both represented by ULong values that can be represented within 31
   * bits, ranges do not overlap, and are monotonically increasing.
   *
   * @param startIndex the position to read the first start value from
   * @param startDelta the offset between subsequent start values
   * @param endIndex the position to read the first end value from
   * @param endDelta the offset between subsequent end values
   * @param length the number of start-end pairs
   * @param key the value to search for
   * @return the index of the start-end pairs in which the key was found; -1 otherwise
   */
  public int searchULong(
      int startIndex, int startDelta, int endIndex, int endDelta, int length, int key) {
    int location = 0;
    int bottom = 0;
    int top = length;
    while (top != bottom) {
      location = (top + bottom) / 2;
      int locationStart = readULongAsInt(startIndex + location * startDelta);
      if (key < locationStart) {
        // location is below current location
        top = location;
      } else {
        // is key below the upper bound?
        int locationEnd = readULongAsInt(endIndex + location * endDelta);
        if (key <= locationEnd) {
          return location;
        }
        // location is above the current location
        bottom = location + 1;
      }
    }
    return -1;
  }

  /**
   * Search for the key value in the table provided.
   *
   * <p>The search looks through the values looking for the key value. It is assumed that the are
   * represented by UShort values and are monotonically increasing.
   *
   * @param startIndex the position to read the first start value from
   * @param startOffset the offset between subsequent start values
   * @param length the number of start-end pairs
   * @param key the value to search for
   * @return the index of the start-end pairs in which the key was found; -1 otherwise
   */
  public int searchUShort(int startIndex, int startOffset, int length, int key) {
    int location = 0;
    int bottom = 0;
    int top = length;
    while (top != bottom) {
      location = (top + bottom) / 2;
      int locationStart = readUShort(startIndex + location * startOffset);
      if (key < locationStart) {
        // location is below current location
        top = location;
      } else if (key > locationStart) {
        // location is above current location
        bottom = location + 1;
      } else {
        return location;
      }
    }
    return -1;
  }
}

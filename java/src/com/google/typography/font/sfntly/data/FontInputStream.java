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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream for reading font data.
 *
 * <p>The data types used are as listed:
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
 * @see FontOutputStream
 */
public class FontInputStream extends FilterInputStream {
  private long position;
  private long length; // bound on length of data to read
  private boolean bounded;

  /** @param is input stream to wrap */
  public FontInputStream(InputStream is) {
    super(is);
  }

  /**
   * Creates a bounded font input stream.
   *
   * @param is input stream to wrap
   * @param length the maximum length of bytes to read
   */
  public FontInputStream(InputStream is, int length) {
    this(is);
    this.length = length;
    this.bounded = true;
  }

  @Override
  public int read() throws IOException {
    if (bounded && position >= length) {
      return -1;
    }
    int b = super.read();
    if (b >= 0) {
      this.position++;
    }
    return b;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (bounded && position >= length) {
      return -1;
    }
    int bytesToRead = bounded ? (int) Math.min(len, length - position) : len;
    int bytesRead = super.read(b, off, bytesToRead);
    this.position += bytesRead;
    return bytesRead;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  /**
   * Get the current position in the stream in bytes.
   *
   * @return the current position in bytes
   */
  public long position() {
    return position;
  }

  /** Read a Char value. */
  public int readChar() throws IOException {
    return read();
  }

  /**
   * Read a UShort value.
   *
   * @return UShort value
   */
  public int readUShort() throws IOException {
    return 0xffff & (read() << 8 | read());
  }

  /**
   * Read a Short value.
   *
   * @return Short value
   */
  public int readShort() throws IOException {
    return ((read() << 8 | read()) << 16) >> 16;
  }

  /** Read a UInt24 value. */
  public int readUInt24() throws IOException {
    return 0xffffff & (read() << 16 | read() << 8 | read());
  }

  /** Read a ULong value. */
  public long readULong() throws IOException {
    return 0xffffffffL & readLong();
  }

  /**
   * Read a ULong value as an int.
   *
   * @throws ArithmeticException if the value is not representable as an int
   */
  public int readULongAsInt() throws IOException {
    long ulong = readULong();
    if ((ulong & 0x80000000) == 0x80000000) {
      throw new ArithmeticException("Long value too large to fit into an integer.");
    }
    return ((int) ulong) & ~0x80000000;
  }

  /** Read a Long value. */
  public int readLong() throws IOException {
    return read() << 24 | read() << 16 | read() << 8 | read();
  }

  /** Read a Fixed value. */
  public int readFixed() throws IOException {
    return readLong();
  }

  /** Read a DateTime value as a long. */
  public long readDateTimeAsLong() throws IOException {
    return readULong() << 32 | readULong();
  }

  @Override
  public long skip(long n) throws IOException {
    // The bytes must be read nevertheless for computing the digest.
    long skipped = 0;
    while (skipped < n && read() != -1) {
      skipped++;
    }
    if (skipped < n) {
      throw new IOException(String.format("Cannot skip %d bytes, skipped only %d", n, skipped));
    }
    return skipped;
  }
}

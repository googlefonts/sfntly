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

/**
 * An abstract base for font data in the TrueType / OpenType spec.
 *
 * @author Stuart Gill
 */
public abstract class FontData {

  protected static final int GROWABLE_SIZE = Integer.MAX_VALUE;

  /**
   * Note: constant names intended to match the names used in the OpenType and sfnt specs.
   *
   * @see "ISO/IEC 14496-22:2015, section 4.3"
   */
  public interface SizeOf {
    int BYTE = 1;
    int CHAR = 1;
    int USHORT = 2;
    int SHORT = 2;
    int UINT24 = 3;
    int ULONG = 4;
    int LONG = 4;
    int Fixed = 4;
    int FUNIT = 4;
    int FWORD = 2;
    int UFWORD = 2;
    int F2DOT14 = 2;
    int LONGDATETIME = 8;
    int Tag = 4;
    int GlyphID = 2;
    int Offset = 2;
  }

  /** The internal data. */
  protected final ByteArray array;
  /** Offset to apply as a lower bound on the internal byte array. */
  private int boundOffset;
  /** The length of the bound on the internal byte array. */
  private int boundLength = GROWABLE_SIZE;

  protected FontData(ByteArray backingArray) {
    this.array = backingArray;
  }

  /**
   * @param data the data to wrap
   * @param offset the offset to start the wrap from
   * @param length the length of the data wrapped
   */
  protected FontData(FontData data, int offset, int length) {
    this(data.array);
    bound(data.boundOffset + offset, length);
  }

  /**
   * @param data the data to wrap
   * @param offset the offset to start the wrap from
   */
  protected FontData(FontData data, int offset) {
    this(data.array);
    bound(
        data.boundOffset + offset,
        data.boundLength == GROWABLE_SIZE ? GROWABLE_SIZE : data.boundLength - offset);
  }

  /**
   * Sets limits on the size of the FontData. The FontData is then only visible within the bounds
   * set.
   *
   * @param offset the start of the new bounds
   * @param length the number of bytes in the bounded array
   * @return true if the bounding range was successful; false otherwise
   */
  public boolean bound(int offset, int length) {
    if ((offset + length > size()) || offset < 0 || length < 0) {
      return false;
    }
    this.boundOffset += offset;
    this.boundLength = length;
    return true;
  }

  /**
   * Sets limits on the size of the FontData. This is an offset bound only so if the FontData is
   * writable and growable then there is no limit to that growth from the bounding operation.
   *
   * @param offset the start of the new bounds which must be within the current size of the FontData
   * @return true if the bounding range was successful; false otherwise
   */
  public boolean bound(int offset) {
    if (offset > size() || offset < 0) {
      return false;
    }
    this.boundOffset += offset;
    return true;
  }

  /**
   * Makes a slice of this FontData. The returned slice will share the data with the original {@code
   * FontData}.
   *
   * @param offset the start of the slice
   * @param length the number of bytes in the slice
   * @return a slice of the original FontData
   */
  public abstract FontData slice(int offset, int length);

  /**
   * Makes a bottom bound only slice of this array. The returned slice will share the data with the
   * original {@code FontData}.
   *
   * @param offset the start of the slice
   * @return a slice of the original FontData
   */
  public abstract FontData slice(int offset);

  /**
   * Gets the length of the data.
   *
   * @return the length of the data
   */
  public int length() {
    return Math.min(array.length() - boundOffset, boundLength);
  }

  /**
   * Gets the maximum size of the FontData. This is the maximum number of bytes that the font data
   * can hold and all of it may not be filled with data or even fully allocated yet.
   *
   * @return the maximum size of this font data
   */
  public int size() {
    return Math.min(array.size() - boundOffset, boundLength);
  }

  /** Returns the offset in the underlying data taking into account any bounds on the data. */
  public final int dataOffset() {
    return boundOffset;
  }

  /**
   * Gets the offset in the underlying data taking into account any bounds on the data.
   *
   * @param offset the offset to get the bound compensated offset for
   * @return the bound compensated offset
   */
  protected final int boundOffset(int offset) {
    return offset + boundOffset;
  }

  /**
   * Gets the length in the underlying data taking into account any bounds on the data.
   *
   * @param offset the offset that the length is being used at
   * @param length the length to get the bound compensated length for
   * @return the bound compensated length
   */
  protected final int boundLength(int offset, int length) {
    return Math.min(length, boundLength - offset);
  }

  protected final boolean boundsCheck(int offset, int length) {
    if (offset < 0 || offset >= boundLength) {
      return false;
    }
    if (length < 0 || length + offset > boundLength) {
      return false;
    }
    return true;
  }
}

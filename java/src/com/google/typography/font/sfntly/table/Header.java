/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.typography.font.sfntly.table;

import com.google.typography.font.sfntly.Tag;
import java.util.Comparator;

/**
 * The header entry for a table in the OffsetTable for the font.
 *
 * <p>For equality purposes the only property of the header that is considered is the tag - the name
 * of the table that is referred to by this header. There can only be one table with each tag in the
 * font and it doesn't matter what the other properties of that header are for that purpose.
 *
 * @author Stuart Gill
 */
public final class Header {
  private final int tag;
  private final int offset;
  private final boolean offsetValid;
  private final int length;
  private final boolean lengthValid;
  private final long checksum;
  private final boolean checksumValid;

  public static final Comparator<Header> COMPARATOR_BY_OFFSET =
      Comparator.comparingInt(h -> h.offset);

  public static final Comparator<Header> COMPARATOR_BY_TAG = Comparator.comparingInt(h -> h.tag);

  /** Make a full header as read from an existing font. */
  public Header(int tag, long checksum, int offset, int length) {
    this.tag = tag;
    this.checksum = checksum;
    this.checksumValid = true;
    this.offset = offset;
    this.offsetValid = true;
    this.length = length;
    this.lengthValid = true;
  }

  /** Make a partial header with only the basic info for a new table. */
  public Header(int tag, int length) {
    this.tag = tag;
    this.checksum = 0;
    this.checksumValid = false;
    this.offset = 0;
    this.offsetValid = false;
    this.length = length;
    this.lengthValid = true;
  }

  /** Make a partial header with only the basic info for an empty new table. */
  public Header(int tag) {
    this.tag = tag;
    this.checksum = 0;
    this.checksumValid = false;
    this.offset = 0;
    this.offsetValid = false;
    this.length = 0;
    this.lengthValid = true;
  }

  /** Get the table tag. */
  public int tag() {
    return tag;
  }

  /**
   * Get the table offset. The offset is from the start of the font file. This offset value is what
   * was read from the font file during construction of the font. It may not be meaningful if the
   * font was maninpulated through the builders.
   */
  public int offset() {
    return offset;
  }

  /**
   * Is the offset in the header valid. The offset will not be valid if the table was constructed
   * during building and has no physical location in a font file.
   *
   * @return true if the offset is valid; false otherwise
   */
  public boolean offsetValid() {
    return offsetValid;
  }

  /**
   * Get the length of the table as recorded in the table record header. During building the header
   * length will reflect the length that was initially read from the font file. This may not be
   * consistent with the current state of the data.
   */
  public int length() {
    return length;
  }

  /**
   * Is the length in the header valid. The length will not be valid if the table was constructed
   * during building and has no physical location in a font file until the table is built from the
   * builder.
   *
   * @return true if the offset is valid; false otherwise
   */
  public boolean lengthValid() {
    return lengthValid;
  }

  /** Get the checksum for the table as recorded in the table record header. */
  public long checksum() {
    return checksum;
  }

  /**
   * Is the checksum valid. The checksum will not be valid if the table was constructed during
   * building and has no physical location in a font file. Note that this does <b>not</b> check the
   * validity of the checksum against the calculated checksum for the table data.
   *
   * @return true if the checksum is valid; false otherwise
   */
  public boolean checksumValid() {
    return checksumValid;
  }

  /**
   * Checks equality of this Header against another object. The only property of the Header object
   * that is considered is the tag.
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Header)) {
      return false;
    }
    return ((Header) obj).tag == tag;
  }

  /**
   * Computes the hashcode for this Header . The only property of the Header object that is
   * considered is the tag.
   */
  @Override
  public int hashCode() {
    return tag;
  }

  @Override
  public String toString() {
    return String.format("[%s, %x, %x, %x]", Tag.stringValue(tag), checksum, offset, length);
  }
}

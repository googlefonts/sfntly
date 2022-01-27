// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;

/** @author dougfelt@google.com (Doug Felt) */
public interface TaggedData {
  void tagRange(String label, int start, int length, int nestingDepth);

  /**
   * @param width number of bytes for the field at position
   * @param value the value in those bytes
   * @param alt an alternate presentation of the value (in decimal, a tag)
   */
  void tagField(int position, int width, int value, String alt, String label);

  /**
   * @param position the position of the reference to target
   * @param value the raw value of the field
   * @param targetPosition the target position;
   * @param label name for this reference, or null
   */
  void tagTarget(int position, int value, int targetPosition, String label);

  void pushRange(String label, ReadableFontData data);

  void pushRangeAtOffset(String label, int base);

  int tagRangeField(FieldType ft, String label);

  void setRangePosition(int rangePosition);

  void popRange();

  static enum FieldType {
    TAG,
    SHORT,
    SHORT_IGNORED,
    SHORT_IGNORED_FFFF,
    OFFSET,
    OFFSET_NONZERO,
    OFFSET32,
    GLYPH;
  }
}

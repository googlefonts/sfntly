package com.google.typography.font.sfntly.table.bitmap;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.SubTable;

public class SbitLineMetrics extends SubTable {

  public static final int SIZE = 12;

  private interface Offset {
    int ascender = 0;
    int descender = 1;
    int widthMax = 2;
    int caretSlopeNumerator = 3;
    int caretSlopeDenominator = 4;
    int caretOffset = 5;
    int minOriginSB = 6;
    int minAdvanceSB = 7;
    int maxBeforeBL = 8;
    int minAfterBL = 9;
    int pad1 = 10;
    int pad2 = 11;
  }

  public SbitLineMetrics(ReadableFontData data, ReadableFontData masterData) {
    super(data, masterData);
  }

  public int ascender() {
    return data.readChar(Offset.ascender);
  }

  public int descender() {
    return data.readChar(Offset.descender);
  }

  public int widthMax() {
    return data.readByte(Offset.widthMax);
  }

  public int caretSlopeNumerator() {
    return data.readChar(Offset.caretSlopeNumerator);
  }

  public int caretSlopeDenominator() {
    return data.readChar(Offset.caretSlopeDenominator);
  }

  public int caretOffset() {
    return data.readChar(Offset.caretOffset);
  }

  public int minOriginSB() {
    return data.readChar(Offset.minOriginSB);
  }

  public int minAdvanceSB() {
    return data.readChar(Offset.minAdvanceSB);
  }

  public int maxBeforeBL() {
    return data.readChar(Offset.maxBeforeBL);
  }

  public int minAfterBL() {
    return data.readChar(Offset.minAfterBL);
  }
}

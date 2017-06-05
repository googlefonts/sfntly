package com.google.typography.font.sfntly.math;

import junit.framework.TestCase;

public class FontMathTest extends TestCase {

  public void testLog2() {
    assertEquals(-1, FontMath.log2(0));
    assertEquals(0, FontMath.log2(1));
    assertEquals(1, FontMath.log2(2));
    assertEquals(1, FontMath.log2(3));
    assertEquals(2, FontMath.log2(4));
    assertEquals(4, FontMath.log2(31));
    assertEquals(5, FontMath.log2(32));
    assertEquals(31, FontMath.log2(-1));
  }

  public void testPaddingRequired() {
    assertEquals(0, FontMath.paddingRequired(4, 4));
    assertEquals(3, FontMath.paddingRequired(5, 4));
    assertEquals(1, FontMath.paddingRequired(7, 4));
    assertEquals(0, FontMath.paddingRequired(8, 4));
    assertEquals(0, FontMath.paddingRequired(17, 1));
  }
}

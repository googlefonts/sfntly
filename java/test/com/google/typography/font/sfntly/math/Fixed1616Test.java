package com.google.typography.font.sfntly.math;

import junit.framework.TestCase;

public class Fixed1616Test extends TestCase {

  public void testIntegral() {
    assertEquals(32768, Fixed1616.integral(Integer.MIN_VALUE)); // FIXME: must be -32768
    assertEquals(65535, Fixed1616.integral(0xffff0000)); // FIXME: must be -1
    assertEquals(65535, Fixed1616.integral(0xffff8000)); // FIXME: must be -1
    assertEquals(65535, Fixed1616.integral(0xffffffff)); // FIXME: must be -1
    assertEquals(0, Fixed1616.integral(0x00000000));
    assertEquals(0, Fixed1616.integral(0x00008000));
    assertEquals(0, Fixed1616.integral(0x0000ffff));
    assertEquals(1, Fixed1616.integral(0x00010000));
    assertEquals(32767, Fixed1616.integral(Integer.MAX_VALUE));
  }

  public void testFractional() {
    assertEquals(0x0000, Fixed1616.fractional(Integer.MIN_VALUE));
    assertEquals(0x0000, Fixed1616.fractional(0xffff0000));
    assertEquals(0x8000, Fixed1616.fractional(0xffff8000));
    assertEquals(0xffff, Fixed1616.fractional(0xffffffff));
    assertEquals(0x0000, Fixed1616.fractional(0x00000000));
    assertEquals(0x8000, Fixed1616.fractional(0x00008000));
    assertEquals(0xffff, Fixed1616.fractional(0x0000ffff));
    assertEquals(0x0000, Fixed1616.fractional(0x00010000));
    assertEquals(0xffff, Fixed1616.fractional(Integer.MAX_VALUE));
  }

  public void testDoubleValue() {
    assertEquals(-32768.0, Fixed1616.doubleValue(Integer.MIN_VALUE));
    assertEquals(-1.0, Fixed1616.doubleValue(0xffff0000));
    assertEquals(-0.5, Fixed1616.doubleValue(0xffff8000));
    assertEquals(-0.0000152587890625, Fixed1616.doubleValue(0xffffffff));
    assertEquals(0.0, Fixed1616.doubleValue(0x00000000));
    assertEquals(0.0000152587890625, Fixed1616.doubleValue(0x00000001));
    assertEquals(0.5, Fixed1616.doubleValue(0x00008000));
    assertEquals(0.9999847412109375, Fixed1616.doubleValue(0x0000ffff));
    assertEquals(32767.99998474121, Fixed1616.doubleValue(Integer.MAX_VALUE));
  }

  public void testFixed() {
    assertEquals(Integer.MIN_VALUE, Fixed1616.fixed(-32768, 0x0000));
    assertEquals(0xffff0000, Fixed1616.fixed(-1, 0x0000));
    assertEquals(0xffff8000, Fixed1616.fixed(-1, 0x8000));
    assertEquals(0xffffffff, Fixed1616.fixed(-1, 0xffff));
    assertEquals(0x00000000, Fixed1616.fixed(0, 0x0000));
    assertEquals(0x00000001, Fixed1616.fixed(0, 0x0001));
    assertEquals(0x00008000, Fixed1616.fixed(0, 0x8000));
    assertEquals(0x0000ffff, Fixed1616.fixed(0, 0xffff));
    assertEquals(Integer.MAX_VALUE, Fixed1616.fixed(32767, 0xffff));
  }

  public void testToString() {
    assertEquals("32768.0", Fixed1616.toString(Integer.MIN_VALUE)); // FIXME: must be -32768.0
    assertEquals("65535.0", Fixed1616.toString(0xffff0000)); // FIXME: must be -1.0
    assertEquals("65535.32768", Fixed1616.toString(0xffff8000)); // FIXME: must be -1.32768
    assertEquals("65535.65535", Fixed1616.toString(0xffffffff)); // FIXME: must be -1.65535
    assertEquals("0.0", Fixed1616.toString(0x00000000));
    assertEquals("0.32768", Fixed1616.toString(0x00008000));
    assertEquals("0.65535", Fixed1616.toString(0x0000ffff));
    assertEquals("1.0", Fixed1616.toString(0x00010000));
    assertEquals("32767.65535", Fixed1616.toString(Integer.MAX_VALUE));
  }
}

package com.google.typography.font.sfntly.data;

import java.util.Arrays;
import java.util.HashSet;
import junit.framework.TestCase;

public class SfStringUtilsTest extends TestCase {

  public void testGetAllCodepoints() {
    assertEquals(
        new HashSet<>(Arrays.asList(72, 101, 108, 111)), SfStringUtils.getAllCodepoints("Hello"));

    assertEquals(
        new HashSet<>(Arrays.asList(0x1F645)),
        SfStringUtils.getAllCodepoints(new String(new int[] {0x1F645}, 0, 1)));
  }
}

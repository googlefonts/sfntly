package com.google.typography.font.sfntly.data;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;

public class SfStringUtilsTest extends TestCase {

  public void testGetAllCodepoints() {
    assertEquals(
        new HashSet<Integer>(Arrays.asList(72, 101, 108, 111)),
        SfStringUtils.getAllCodepoints("Hello"));

    assertEquals(
        new HashSet<Integer>(Arrays.asList(0x1F645)),
        SfStringUtils.getAllCodepoints(new String(new int[] { 0x1F645 }, 0, 1)));
  }

}

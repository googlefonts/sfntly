package com.google.typography.font.sfntly.data;

import java.util.Arrays;

public class SfObjects {

  /** Same as Object.equals from Java 7. */
  public static boolean equals(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
  }

  /** Same as Objects.hash from Java 7. */
  public static int hash(Object... values) {
    return Arrays.hashCode(values);
  }
}

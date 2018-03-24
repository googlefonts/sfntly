package com.google.typography.font.sfntly.data;

import java.util.HashSet;
import java.util.Set;

public final class SfStringUtils {

  public static Set<Integer> getAllCodepoints(String s) {
    Set<Integer> list = new HashSet<>();
    for (int cp, i = 0, len = s.length(); i < len; i += Character.charCount(cp)) {
      cp = s.codePointAt(i);
      list.add(cp);
    }
    return list;
  }
}

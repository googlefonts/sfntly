package com.google.typography.font.sfntly.table.opentype.component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.TreeSet;

public class GlyphGroup extends LinkedHashSet<Integer> {
  private static final long serialVersionUID = 1L;

  public GlyphGroup() {
    super();
  }

  public GlyphGroup(int glyph) {
    super.add(glyph);
  }

  public GlyphGroup(Collection<Integer> glyphs) {
    super.addAll(glyphs);
  }

  public GlyphGroup(int[] glyphs) {
    for (int glyph : glyphs) {
      super.add(glyph);
    }
  }

  public boolean isIntersecting(GlyphGroup other) {
    return !Collections.disjoint(this, other);
  }

  @Override
  public String toString() {
    return new TreeSet<Integer>(this).toString();
  }
}

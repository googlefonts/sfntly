package com.google.typography.font.sfntly.table.opentype.component;

import java.util.LinkedList;

public class RuleSegment extends LinkedList<GlyphGroup> {
  private static final long serialVersionUID = 4563803321401665616L;

  public RuleSegment() {
    super();
  }

  public RuleSegment(GlyphGroup glyphGroup) {
    super.add(glyphGroup);
  }

  public RuleSegment(int glyph) {
    GlyphGroup glyphGroup = new GlyphGroup(glyph);
    super.add(glyphGroup);
  }

  public RuleSegment(GlyphList glyphs) {
    for (int glyph : glyphs) {
      GlyphGroup glyphGroup = new GlyphGroup(glyph);
      super.add(glyphGroup);
    }
  }

  public boolean add(int glyph) {
    GlyphGroup glyphGroup = new GlyphGroup(glyph);
    return super.add(glyphGroup);
  }

  public boolean match(GlyphList glyphs) {
    if (glyphs.size() != this.size()) {
      return false;
    }
    int i = 0;
    for (int glyph : glyphs) {
      GlyphGroup glyphGroup = this.get(i);
      if (glyphGroup.size() != 1 || !glyphGroup.contains(glyph)) {
        return false;
      }
      i++;
    }
    return true;
  }
}

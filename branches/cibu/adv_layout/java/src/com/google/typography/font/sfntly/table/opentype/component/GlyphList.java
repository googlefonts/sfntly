package com.google.typography.font.sfntly.table.opentype.component;

import java.util.ArrayList;

public class GlyphList extends ArrayList<Integer> {
  private static final long serialVersionUID = 4699092062720505377L;

  public GlyphList() {
    super();
  }

  public GlyphList(int glyph) {
    super.add(glyph);
  }
}

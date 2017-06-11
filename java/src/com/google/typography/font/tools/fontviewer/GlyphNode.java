package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.truetype.Glyph;

import javax.swing.JComponent;
import javax.swing.JTextArea;

public class GlyphNode extends AbstractNode {
  private final int glyphId;
  private final Glyph glyph;

  public GlyphNode(int glyphId, Glyph glyph) {
    this.glyphId = glyphId;
    this.glyph = glyph;
  }

  @Override
  protected String getNodeName() {
    return String.valueOf(glyphId);
  }

  @Override
  JComponent render() {
    return new JTextArea(glyph.toString());
  }
}

package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.truetype.Glyph;
import com.google.typography.font.sfntly.table.truetype.SimpleGlyph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JPanel;
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
    if (glyph instanceof SimpleGlyph) {
      GlyphRenderer renderer = new GlyphRenderer();
      JTextArea text = new JTextArea(glyph.toString());
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      panel.add(renderer, BorderLayout.NORTH);
      panel.add(text, BorderLayout.CENTER);
      return panel;
    } else {
      return new JTextArea(glyph.toString());
    }
  }

  private class GlyphRenderer extends JComponent {

    private static final int MARGIN = 10;
    private static final int SIZE = 100;

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(MARGIN + SIZE + MARGIN, MARGIN + SIZE + MARGIN);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      int minX = glyph.xMin(), minY = glyph.yMin();
      int maxX = glyph.xMax(), maxY = glyph.yMax();

      double scale = (double) SIZE / Math.max(maxX - minX, maxY - minY);

      SimpleGlyph glyph = (SimpleGlyph) GlyphNode.this.glyph;
      int firstScreenX = 0, firstScreenY = 0;
      int lastScreenX = 0, lastScreenY = 0;
      for (int c = 0, cmax = glyph.numberOfContours(); c < cmax; c++) {
        for (int p = 0, pmax = glyph.numberOfPoints(c); p < pmax; p++) {
          int x = glyph.xCoordinate(c, p);
          int y = glyph.yCoordinate(c, p);
          boolean on = glyph.onCurve(c, p);
          int screenX = MARGIN + (int) (scale * (x - minX));
          int screenY = MARGIN + SIZE - (int) (scale * (y - minY));
          g.setColor(on ? Color.BLACK : Color.GREEN);
          g.drawOval(screenX - 2, screenY - 2, 4, 4);
          if (p != 0) {
            g.setColor(Color.BLUE);
            g.drawLine(lastScreenX, lastScreenY, screenX, screenY);
          } else {
            firstScreenX = screenX;
            firstScreenY = screenY;
          }
          if (p == pmax - 1) {
            g.setColor(Color.BLUE);
            g.drawLine(screenX, screenY, firstScreenX, firstScreenY);
          }
          lastScreenX = screenX;
          lastScreenY = screenY;
        }
      }
    }
  }
}

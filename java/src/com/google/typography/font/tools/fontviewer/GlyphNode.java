package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.truetype.Glyph;
import com.google.typography.font.sfntly.table.truetype.SimpleGlyph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
    return String.valueOf(this.glyphId);
  }

  @Override
  JComponent render() {
    if (this.glyph instanceof SimpleGlyph) {
      GlyphRenderer renderer = new GlyphRenderer();
      JComponent text = new JScrollPane(new JTextArea(this.glyph.toString()));
      text.setPreferredSize(new Dimension(500, 200));
      final JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, renderer, text);
      pane.addPropertyChangeListener(
          JSplitPane.DIVIDER_LOCATION_PROPERTY,
          new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
              AppState.glyphRendererHeight = (Integer) evt.getNewValue() - pane.getInsets().top;
            }
          });
      pane.setPreferredSize(new Dimension(500, 500));
      return pane;
    } else {
      return new JTextArea(this.glyph.toString());
    }
  }

  @Override
  boolean renderInScrollPane() {
    return !(this.glyph instanceof SimpleGlyph);
  }

  private class GlyphRenderer extends JComponent {

    private static final int MARGIN = 10;

    private final int minX = GlyphNode.this.glyph.xMin();
    private final int minY = GlyphNode.this.glyph.yMin();
    private final int maxX = GlyphNode.this.glyph.xMax();
    private final int maxY = GlyphNode.this.glyph.yMax();

    private double scale;

    private void updateScale() {
      int size = Math.min(this.getWidth(), this.getHeight()) - MARGIN - MARGIN;
      this.scale = (double) size / Math.max(this.maxX - this.minX, this.maxY - this.minY);
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(500, AppState.glyphRendererHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      ((Graphics2D) g).setRenderingHint(
          RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

      updateScale();

      SimpleGlyph glyph = (SimpleGlyph) GlyphNode.this.glyph;
      for (int c = 0, cmax = glyph.numberOfContours(); c < cmax; c++) {
        Polygon polygon = new Polygon();
        for (int p = 0, pmax = glyph.numberOfPoints(c); p < pmax; p++) {
          int x = glyph.xCoordinate(c, p);
          int y = glyph.yCoordinate(c, p);
          polygon.addPoint(screenX(x), screenY(y));
        }
        g.setColor(Color.BLUE);
        g.drawPolygon(polygon);

        for (int p = 0, pmax = glyph.numberOfPoints(c); p < pmax; p++) {
          int x = glyph.xCoordinate(c, p);
          int y = glyph.yCoordinate(c, p);
          boolean on = glyph.onCurve(c, p);
          g.setColor(on ? Color.BLACK : Color.GREEN);
          g.drawOval(screenX(x) - 2, screenY(y) - 2, 4, 4);
        }
      }
    }

    private int screenX(int x) {
      return MARGIN + (int) Math.round(this.scale * (x - this.minX));
    }

    private int screenY(int y) {
      return MARGIN + (int) Math.round(this.scale * (this.maxY - y));
    }
  }
}

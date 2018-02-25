package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.truetype.CompositeGlyph;
import com.google.typography.font.sfntly.table.truetype.Glyph;
import com.google.typography.font.sfntly.table.truetype.GlyphTable;
import com.google.typography.font.sfntly.table.truetype.LocaTable;
import com.google.typography.font.sfntly.table.truetype.SimpleGlyph;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class GlyphNode extends AbstractNode {
  private final int glyphId;
  private final Glyph glyph;
  private final GlyphTable glyf;
  private final LocaTable loca;

  public GlyphNode(int glyphId, Glyph glyph, GlyphTable glyf, LocaTable loca) {
    this.glyphId = glyphId;
    this.glyph = glyph;
    this.glyf = glyf;
    this.loca = loca;
  }

  @Override
  protected String getNodeName() {
    return String.valueOf(this.glyphId);
  }

  @Override
  JComponent render() {
    GlyphRenderer renderer = new GlyphRenderer();
    final JComponent text = new JScrollPane(new JTextArea(this.glyph.toString()));
    text.setPreferredSize(new Dimension(500, 200));
    final JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, renderer, text);
    pane.addPropertyChangeListener(
        JSplitPane.DIVIDER_LOCATION_PROPERTY,
        new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
            // Somehow, if the text field is forced to be smaller than its preferred height,
            // it occupies the whole height, leaving only a single pixel for the glyph drawing.
            if (text.getHeight() >= text.getPreferredSize().height) {
              AppState.glyphRendererHeight = (Integer) evt.getNewValue() - pane.getInsets().top;
            }
          }
        });
    pane.setPreferredSize(new Dimension(500, 500));
    return pane;
  }

  @Override
  boolean renderInScrollPane() {
    return false;
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
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);

      Graphics2D g = (Graphics2D) graphics;
      g.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

      this.updateScale();

      Glyph glyph = GlyphNode.this.glyph;
      if (glyph instanceof SimpleGlyph) {
        this.paintSimpleGlyph(g, (SimpleGlyph) glyph, 0, 0);
      } else {
        this.paintCompositeGlyph(g, (CompositeGlyph) glyph);
      }
    }

    private void paintCompositeGlyph(Graphics2D g, CompositeGlyph composite) {
      for (int i = 0; i < composite.numGlyphs(); i++) {
        int glyphIndex = composite.glyphIndex(i);
        int offset = GlyphNode.this.loca.glyphOffset(glyphIndex);
        int length = GlyphNode.this.loca.glyphLength(glyphIndex);
        if (length != 0) {
          SimpleGlyph simple = (SimpleGlyph) GlyphNode.this.glyf.glyph(offset, length);
          int deltaX = composite.argument1(i);
          int deltaY = composite.argument2(i);
          this.paintSimpleGlyph(g, simple, deltaX, deltaY);
        }
      }
    }

    private void paintSimpleGlyph(Graphics2D g, SimpleGlyph glyph, int deltaX, int deltaY) {
      for (int c = 0, cmax = glyph.numberOfContours(); c < cmax; c++) {
        ScreenCoordinateMapper screen = new ScreenCoordinateMapper(glyph, c, MARGIN, this.scale,
            this.minX - deltaX, this.maxY - deltaY);
        int pmax = glyph.numberOfPoints(c);

        int firstOn = 0;
        for (int p = 0; p < pmax; p++) {
          if (screen.onCurve(p)) {
            firstOn = p;
            break;
          }
        }

        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        path.moveTo(screen.x(firstOn), screen.y(firstOn));

        for (int i = 0; i < pmax; i++) {
          int icurr = firstOn + i + 1;
          int inext = firstOn + i + 2;

          int currx = screen.x(icurr);
          int curry = screen.y(icurr);
          if (screen.onCurve(icurr)) {
            path.lineTo(currx, curry);
          } else {
            double nextx = screen.x(inext);
            double nexty = screen.y(inext);
            if (!screen.onCurve(inext)) {
              nextx = 0.5 * (currx + nextx);
              nexty = 0.5 * (curry + nexty);
            }
            path.quadTo(currx, curry, nextx, nexty);
          }
        }

        path.closePath();

        g.setColor(Color.BLUE);
        g.draw(path);

        for (int p = 0; p < pmax; p++) {
          g.setColor(screen.onCurve(p) ? Color.BLACK : Color.GREEN);
          g.drawOval(screen.x(p) - 2, screen.y(p) - 2, 4, 4);
          g.drawString(c + ":" + p, screen.x(p) + 5, screen.y(p) - 5);
        }
      }
    }
  }

  private static class ScreenCoordinateMapper {
    private final SimpleGlyph glyph;
    private final int contour;
    private final int points;

    private final int margin;
    private final double scale;
    private final double minX;
    private final double maxY;

    ScreenCoordinateMapper(SimpleGlyph glyph, int contour, int margin, double scale, double minX, double maxY) {
      this.glyph = glyph;
      this.contour = contour;
      this.points = glyph.numberOfPoints(contour);

      this.margin = margin;
      this.scale = scale;
      this.minX = minX;
      this.maxY = maxY;
    }

    int x(int point) {
      int x = this.glyph.xCoordinate(this.contour, index(point));
      return this.margin + (int) Math.round(this.scale * (x - this.minX));
    }

    int y(int point) {
      int y = this.glyph.yCoordinate(this.contour, index(point));
      return this.margin + (int) Math.round(this.scale * (this.maxY - y));
    }

    private int index(int point) {
      return (point + this.points) % this.points;
    }

    boolean onCurve(int point) {
      return this.glyph.onCurve(this.contour, index(point));
    }
  }
}

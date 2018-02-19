package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.truetype.Glyph;
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
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);

      Graphics2D g = (Graphics2D) graphics;
      g.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

      updateScale();

      SimpleGlyph glyph = (SimpleGlyph) GlyphNode.this.glyph;
      for (int c = 0, cmax = glyph.numberOfContours(); c < cmax; c++) {
        ScreenCoordinateMapper screen = new ScreenCoordinateMapper(glyph, c, MARGIN, this.scale, this.minX, this.maxY);
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

        // XXX: This rendering is better than just connecting the dots
        // with straight lines, but the curves don't look correct.
        for (int i = 0; i < pmax; i++) {
          int p0 = firstOn + i - 1;
          int p1 = firstOn + i;
          int p2 = firstOn + i + 1;
          int p3 = firstOn + i + 2;
          if (screen.onCurve(p1)) {
            path.lineTo(screen.x(p1), screen.y(p1));
          } else if (screen.onCurve(p2)) {
            path.quadTo(
                screen.x(p1), screen.y(p1),
                screen.x(p2), screen.y(p2));
            i++;
          } else {
            // XXX: assuming screen.on(p3) doesn't always work.
            // XXX: Curves with 3 or more off-curve points are rendered wrong.
            path.curveTo(
                screen.sx(p1, p0), screen.sy(p1, p0),
                screen.sx(p2, p3), screen.sy(p2, p3),
                screen.x(p3), screen.y(p3));
            i += 2;
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

    int sx(int point, int from) {
      int sxPoint = x(point);
      int sxFrom = x(from);
      return sxFrom + (int) (1.2 * (sxPoint - sxFrom));
    }

    int sy(int point, int from) {
      int sxPoint = y(point);
      int sxFrom = y(from);
      return sxFrom + (int) (1.2 * (sxPoint - sxFrom));
    }

    private int index(int point) {
      return (point + this.points) % this.points;
    }

    boolean onCurve(int point) {
      return this.glyph.onCurve(this.contour, index(point));
    }
  }
}

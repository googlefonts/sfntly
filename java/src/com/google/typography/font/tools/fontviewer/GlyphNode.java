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
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Shows the graphical representation of a glyph, as well as a textual description of the contours.
 *
 * <p>Note: the drawing code is based on trial-and-error and does not follow the OpenType standard.
 * It might work or not.
 */
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
    return String.valueOf(glyphId);
  }

  @Override
  JComponent render() {
    JTabbedPane pane = new JTabbedPane(SwingConstants.TOP);
    pane.addTab("Graphical", new GlyphRenderer());
    pane.add("Text", new JScrollPane(new JTextArea(glyph.toString())));

    pane.setPreferredSize(new Dimension(500, 500));
    return pane;
  }

  @Override
  boolean renderInScrollPane() {
    return false;
  }

  private class GlyphRenderer extends JComponent {

    private static final int MARGIN = 10;

    private double scale;

    private void updateScale() {
      int size = Math.min(getWidth(), getHeight()) - MARGIN - MARGIN;
      this.scale =
          (double) size / Math.max(glyph.xMax() - glyph.xMin(), glyph.yMax() - glyph.yMin());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);

      Graphics2D g = (Graphics2D) graphics;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      updateScale();

      Glyph glyph = GlyphNode.this.glyph;
      if (glyph instanceof SimpleGlyph) {
        paintSimpleGlyph(g, (SimpleGlyph) glyph);
      } else {
        paintCompositeGlyph(g, (CompositeGlyph) glyph);
      }
    }

    /**
     * Note: the drawing code is based on trial-and-error and does not follow the OpenType standard.
     * It might work or not.
     */
    private void paintCompositeGlyph(Graphics2D g, CompositeGlyph composite) {
      for (int i = 0; i < composite.numGlyphs(); i++) {
        int glyphIndex = composite.glyphIndex(i);
        int offset = loca.glyphOffset(glyphIndex);
        int length = loca.glyphLength(glyphIndex);
        if (length != 0) {
          Glyph glyph = glyf.glyph(offset, length);
          if (glyph instanceof SimpleGlyph) {
            paintSimpleGlyph(g, (SimpleGlyph) glyph);
          } else {
            paintCompositeGlyph(g, (CompositeGlyph) glyph);
          }
        }
      }
    }

    /**
     * Note: the drawing code is based on trial-and-error and does not follow the OpenType standard.
     * It might work or not.
     */
    private void paintSimpleGlyph(Graphics2D g, SimpleGlyph glyph) {
      for (int c = 0, cmax = glyph.numberOfContours(); c < cmax; c++) {
        ScreenCoordinateMapper screen =
            new ScreenCoordinateMapper(glyph, c, MARGIN, scale, glyph.xMin(), glyph.yMax());
        int pmax = glyph.numberOfPoints(c);

        int firstOn = 0;
        for (int p = 0; p < pmax; p++) {
          if (screen.onCurve(p)) {
            firstOn = p;
            break;
          }
        }

        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        path.moveTo(screen.cx(firstOn), screen.cy(firstOn));

        for (int i = 0; i < pmax; i++) {
          int icurr = firstOn + i + 1;
          int inext = firstOn + i + 2;

          int currx = screen.cx(icurr);
          int curry = screen.cy(icurr);
          if (screen.onCurve(icurr)) {
            path.lineTo(currx, curry);
          } else {
            double nextx = screen.cx(inext);
            double nexty = screen.cy(inext);
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
          g.drawOval(screen.cx(p) - 2, screen.cy(p) - 2, 4, 4);
          g.drawString(c + ":" + p, screen.cx(p) + 5, screen.cy(p) - 5);
        }
      }
    }
  }

  /**
   * Translates coordinates from the glyph coordinate for a specific contour system to the screen.
   */
  private static class ScreenCoordinateMapper {

    private final Glyph glyph;
    private final SimpleGlyph simple;
    private final int contour;
    private final int points;

    private final int margin;
    private final double scale;
    private final double minX;
    private final double maxY;

    ScreenCoordinateMapper(
        SimpleGlyph glyph, int contour, int margin, double scale, double minX, double maxY) {
      this.glyph = glyph;
      this.simple = glyph instanceof SimpleGlyph ? glyph : null;
      this.contour = contour;
      this.points = glyph.numberOfPoints(contour);

      this.margin = margin;
      this.scale = scale;
      this.minX = minX;
      this.maxY = maxY;
    }

    /** The x coordinate on the screen for the given x coordinate in the glyph coordinate system. */
    private int x(double x) {
      return margin + (int) Math.round(scale * (x - minX));
    }

    /** The y coordinate on the screen for the given y coordinate in the glyph coordinate system. */
    private int y(int y) {
      return margin + (int) Math.round(scale * (maxY - y));
    }

    /** For a simple glyph, the x screen coordinate for the given point on the contour. */
    int cx(int point) {
      int x = simple.xCoordinate(contour, index(point));
      return x(x);
    }

    /** For a simple glyph, the y screen coordinate for the given point on the contour. */
    int cy(int point) {
      int y = simple.yCoordinate(contour, index(point));
      return y(y);
    }

    private int index(int point) {
      return (point + points) % points;
    }

    /** For a simple glyph, whether the point is on the curve, or off the curve. */
    boolean onCurve(int point) {
      return simple.onCurve(contour, index(point));
    }
  }
}

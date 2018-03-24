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
    final JTabbedPane pane = new JTabbedPane(SwingConstants.TOP);
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

    private final int minX = glyph.xMin();
    private final int minY = glyph.yMin();
    private final int maxX = glyph.xMax();
    private final int maxY = glyph.yMax();

    private double scale;

    private void updateScale() {
      int size = Math.min(getWidth(), getHeight()) - MARGIN - MARGIN;
      this.scale = (double) size / Math.max(maxX - minX, maxY - minY);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);

      Graphics2D g = (Graphics2D) graphics;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      updateScale();

      Glyph glyph = GlyphNode.this.glyph;
      if (glyph instanceof SimpleGlyph) {
        paintSimpleGlyph(g, (SimpleGlyph) glyph, 0, 0);
      } else {
        paintCompositeGlyph(g, (CompositeGlyph) glyph);
      }
    }

    private void paintCompositeGlyph(Graphics2D g, CompositeGlyph composite) {
      for (int i = 0; i < composite.numGlyphs(); i++) {
        int glyphIndex = composite.glyphIndex(i);
        int offset = loca.glyphOffset(glyphIndex);
        int length = loca.glyphLength(glyphIndex);
        if (length != 0) {
          SimpleGlyph simple = (SimpleGlyph) glyf.glyph(offset, length);
          int deltaX = composite.argument1(i);
          int deltaY = composite.argument2(i);
          paintSimpleGlyph(g, simple, deltaX, deltaY);
        }
      }
    }

    private void paintSimpleGlyph(Graphics2D g, SimpleGlyph glyph, int deltaX, int deltaY) {
      for (int c = 0, cmax = glyph.numberOfContours(); c < cmax; c++) {
        ScreenCoordinateMapper screen =
            new ScreenCoordinateMapper(
                glyph, c, MARGIN, scale, minX - deltaX, maxY - deltaY);
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

    ScreenCoordinateMapper(
        SimpleGlyph glyph, int contour, int margin, double scale, double minX, double maxY) {
      this.glyph = glyph;
      this.contour = contour;
      this.points = glyph.numberOfPoints(contour);

      this.margin = margin;
      this.scale = scale;
      this.minX = minX;
      this.maxY = maxY;
    }

    int x(int point) {
      int x = glyph.xCoordinate(contour, index(point));
      return margin + (int) Math.round(scale * (x - minX));
    }

    int y(int point) {
      int y = glyph.yCoordinate(contour, index(point));
      return margin + (int) Math.round(scale * (maxY - y));
    }

    private int index(int point) {
      return (point + points) % points;
    }

    boolean onCurve(int point) {
      return glyph.onCurve(contour, index(point));
    }
  }
}

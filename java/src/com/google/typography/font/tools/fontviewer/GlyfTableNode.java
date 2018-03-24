package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.truetype.GlyphTable;
import com.google.typography.font.sfntly.table.truetype.LocaTable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTable;

class GlyfTableNode extends AbstractNode {

  private final GlyphTable glyf;
  private final LocaTable loca;
  private final List<GlyphNode> glyphNodes = new ArrayList<>();

  GlyfTableNode(GlyphTable glyf, LocaTable loca) {
    this.glyf = glyf;
    this.loca = loca;
    for (int i = 0, imax = loca.numGlyphs(); i < imax; i++) {
      int offset = loca.glyphOffset(i);
      int length = loca.glyphLength(i);
      if (length != 0) {
        glyphNodes.add(new GlyphNode(i, glyf.glyph(offset, length), glyf, loca));
      }
    }
  }

  @Override
  protected String getNodeName() {
    return "glyf";
  }

  @Override
  public int getChildCount() {
    return glyphNodes.size();
  }

  @Override
  public AbstractNode getChildAt(int index) {
    return glyphNodes.get(index);
  }

  @Override
  JComponent render() {
    JTable table = new JTable(new Model());
    JTableUtils.setNumberColumn(table, 0);
    JTableUtils.setNumberColumn(table, 1);
    JTableUtils.setNumberColumn(table, 2);
    return table;
  }

  private class Model extends ColumnTableModel {

    Model() {
      super("Glyph ID", "Offset", "Length", "Glyph data");
    }

    @Override
    public int getRowCount() {
      return loca.numGlyphs();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case 0:
          return rowIndex;
        case 1:
          return loca.glyphOffset(rowIndex);
        case 2:
          return loca.glyphLength(rowIndex);
        case 3:
          int length = loca.glyphLength(rowIndex);
          int offset = loca.glyphOffset(rowIndex);
          return length != 0 ? glyf.glyph(offset, length) : "(empty)";
      }
      throw new IllegalStateException();
    }
  }
}

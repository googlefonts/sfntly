package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.truetype.LocaTable;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

class LocaTableNode extends AbstractNode {

  private final LocaTable loca;

  LocaTableNode(LocaTable loca, String name) {
    this.setUserObject(name);
    this.loca = loca;
  }

  @Override
  protected String getNodeName() {
    return (String) this.getUserObject();
  }

  @Override
  public JComponent render() {
    JTable table = new JTable(new LocaTableModel());
    JTableUtils.setNumberColumn(table, 0);
    JTableUtils.setNumberColumn(table, 1);
    JTableUtils.setNumberColumn(table, 2);
    return table;
  }

  private class LocaTableModel extends AbstractTableModel {

    private final String[] columnNames = { "ID", "Glyph offset", "Glyph length" };

    @Override
    public String getColumnName(int column) {
      return this.columnNames[column];
    }

    @Override
    public int getRowCount() {
      return LocaTableNode.this.loca.numGlyphs();
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      switch (columnIndex) {
      case 0:
        return rowIndex;
      case 1:
        return LocaTableNode.this.loca.glyphOffset(rowIndex);
      case 2:
        return LocaTableNode.this.loca.glyphLength(rowIndex);
      default:
        throw new IllegalStateException();
      }
    }
  }
}

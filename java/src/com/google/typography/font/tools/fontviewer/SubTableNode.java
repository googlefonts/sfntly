package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.FontDataTable;
import com.google.typography.font.sfntly.table.core.CMap;
import com.google.typography.font.sfntly.table.core.CMapFormat4;
import com.google.typography.font.sfntly.table.core.CMapTable;
import javax.swing.JComponent;
import javax.swing.JTextArea;

class SubTableNode extends AbstractNode {

  private static final long serialVersionUID = 1L;

  private final FontDataTable table;

  SubTableNode(FontDataTable table, String name) {
    setUserObject(name);
    this.table = table;
  }

  @Override
  public String getNodeName() {
    return (String) getUserObject();
  }

  FontDataTable getTable() {
    return table;
  }

  @Override
  public int getChildCount() {
    if (table instanceof CMapTable) {
      return ((CMapTable) table).numCMaps();
    }
    return 0;
  }

  @Override
  public AbstractNode getChildAt(int index) {
    if (table instanceof CMapTable) {
      CMap child = ((CMapTable) table).cmap(index);
      if (child.format() == 4) {
        return new CMapFormat4Node((CMapFormat4) child);
      }
      return new SubTableNode(child, child.toString());
    }
    throw new IllegalStateException();
  }

  @Override
  JComponent render() {
    return new JTextArea(table.readFontData().toString(4096));
  }
}

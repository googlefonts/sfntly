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
    this.setUserObject(name);
    this.table = table;
  }

  @Override
  public String getNodeName() {
    return (String) this.getUserObject();
  }

  FontDataTable getTable() {
    return this.table;
  }

  @Override
  public int getChildCount() {
    if (this.table instanceof CMapTable) {
      return ((CMapTable) this.table).numCMaps();
    }
    return 0;
  }

  @Override
  public AbstractNode getChildAt(int index) {
    if (this.table instanceof CMapTable) {
      CMap child = ((CMapTable) this.table).cmap(index);
      if (child.format() == 4) {
        return new CMapFormat4Node((CMapFormat4) child);
      }
      return new SubTableNode(child, child.toString());
    }
    throw new IllegalStateException();
  }

  @Override
  JComponent render() {
    return new JTextArea(this.table.readFontData().toString(4096));
  }
}

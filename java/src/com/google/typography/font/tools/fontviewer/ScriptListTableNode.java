package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.opentype.ScriptListTable;
import com.google.typography.font.sfntly.table.opentype.ScriptTable;
import com.google.typography.font.sfntly.table.opentype.ScriptTag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JComponent;
import javax.swing.JTable;

public class ScriptListTableNode extends AbstractNode {

  private final List<Map.Entry<ScriptTag, ScriptTable>> children;

  public ScriptListTableNode(ScriptListTable table) {
    this.children = new ArrayList<>(new TreeMap<>(table.map()).entrySet());
  }

  @Override
  protected String getNodeName() {
    return "Scripts";
  }

  @Override
  JComponent render() {
    return new JTable(new Model());
  }

  private class Model extends ColumnTableModel {

    Model() {
      super("Key", "Value");
    }

    @Override
    public int getRowCount() {
      return children.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case 0:
          return children.get(rowIndex).getKey();
        case 1:
          return children.get(rowIndex).getValue();
        default:
          throw new IllegalStateException();
      }
    }
  }
}

package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.opentype.component.Rule;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JTable;

public class GsubRuleSetNode extends AbstractNode {
  private final int ruleSetId;
  private final List<Rule> rules;

  public GsubRuleSetNode(int ruleSetId, Set<Rule> rules) {
    this.ruleSetId = ruleSetId;
    this.rules = new ArrayList<>(rules);
  }

  @Override
  protected String getNodeName() {
    return Integer.toString(ruleSetId);
  }

  @Override
  public int getChildCount() {
    return rules.size();
  }

  @Override
  public AbstractNode getChildAt(int index) {
    return new GsubRuleNode(index, rules.get(index));
  }

  @Override
  JComponent render() {
    return new JTable(new Model());
  }

  private class Model extends ColumnTableModel {

    Model() {
      super("Index", "Rule");
    }

    @Override
    public int getRowCount() {
      return rules.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case 0:
          return Integer.toString(rowIndex);
        case 1:
          return rules.get(rowIndex);
      }
      throw new IllegalStateException();
    }
  }
}

package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.opentype.GSubTable;
import com.google.typography.font.sfntly.table.opentype.component.Rule;
import com.google.typography.font.sfntly.table.opentype.component.RuleExtractor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JTable;

public class GsubRuleSetsNode extends AbstractNode {
  private final List<Map.Entry<Integer, Set<Rule>>> rules;

  public GsubRuleSetsNode(GSubTable gsub) {
    this.rules =
        new ArrayList<Map.Entry<Integer, Set<Rule>>>(
            RuleExtractor.extract(gsub.lookupList()).entrySet());
  }

  @Override
  protected String getNodeName() {
    return "Rules";
  }

  @Override
  public int getChildCount() {
    return this.rules.size();
  }

  @Override
  public AbstractNode getChildAt(int index) {
    return new GsubRuleSetNode(this.rules.get(index).getKey(), this.rules.get(index).getValue());
  }

  @Override
  JComponent render() {
    return new JTable(new Model());
  }

  private class Model extends ColumnTableModel {

    Model() {
      super("Rule set ID", "Number of rules");
    }

    @Override
    public int getRowCount() {
      return GsubRuleSetsNode.this.rules.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case 0:
          return GsubRuleSetsNode.this.rules.get(rowIndex).getKey();
        case 1:
          return GsubRuleSetsNode.this.rules.get(rowIndex).getValue().size();
      }
      throw new IllegalStateException();
    }
  }
}

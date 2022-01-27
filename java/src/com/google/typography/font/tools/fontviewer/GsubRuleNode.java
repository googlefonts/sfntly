package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.opentype.component.Rule;
import javax.swing.JComponent;
import javax.swing.JTextArea;

public class GsubRuleNode extends AbstractNode {
  private final int index;
  private final Rule rule;

  public GsubRuleNode(int index, Rule rule) {
    this.index = index;
    this.rule = rule;
  }

  @Override
  protected String getNodeName() {
    return Integer.toString(index);
  }

  @Override
  JComponent render() {
    return new JTextArea(rule.toString());
  }
}

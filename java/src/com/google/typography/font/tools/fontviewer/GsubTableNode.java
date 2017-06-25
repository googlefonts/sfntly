package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.opentype.GSubTable;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextArea;

public class GsubTableNode extends AbstractNode {

  private final GSubTable gsub;
  private final List<AbstractNode> children = new ArrayList<AbstractNode>();

  public GsubTableNode(GSubTable gsub) {
    this.gsub = gsub;
    this.children.add(new ScriptListTableNode(gsub.scriptList()));
    this.children.add(new SubTableNode(gsub.featureList(), "feature"));
    this.children.add(new SubTableNode(gsub.lookupList(), "lookup"));
  }

  @Override
  protected String getNodeName() {
    return "GSUB";
  }

  @Override
  public int getChildCount() {
    return children.size();
  }

  @Override
  public AbstractNode getChildAt(int index) {
    return children.get(index);
  }

  @Override
  JComponent render() {
    return new JTextArea(this.gsub.toString());
  }
}

package com.google.typography.font.tools.fontviewer;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;

abstract class AbstractNode extends DefaultMutableTreeNode {

  @Override
  public final String toString() {
    return this.getNodeName();
  }

  protected abstract String getNodeName();

  abstract JComponent render();

  boolean renderInScrollPane() {
    return true;
  }

  @Override
  public AbstractNode getChildAt(int index) {
    throw new UnsupportedOperationException();
  }
}

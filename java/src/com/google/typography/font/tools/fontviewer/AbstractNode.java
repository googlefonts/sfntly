package com.google.typography.font.tools.fontviewer;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The tree in the left panel is built from these nodes. Most of these nodes correspond to a "table"
 * of the font.
 *
 * @see FontNode
 */
abstract class AbstractNode extends DefaultMutableTreeNode {

  @Override
  public final String toString() {
    return getNodeName();
  }

  protected abstract String getNodeName();

  abstract JComponent render();

  /**
   * Whether the component from {@link #render()} is wrapped in a {@link JScrollPane} so that it can
   * be arbitrarily large. Otherwise it can easily be truncated on small screens.
   */
  boolean renderInScrollPane() {
    return true;
  }

  @Override
  public AbstractNode getChildAt(int index) {
    throw new UnsupportedOperationException();
  }
}

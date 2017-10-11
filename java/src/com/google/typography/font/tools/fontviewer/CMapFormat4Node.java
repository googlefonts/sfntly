package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.core.CMapFormat4;

import javax.swing.JComponent;
import javax.swing.JTextArea;

class CMapFormat4Node extends AbstractNode {

  private final CMapFormat4 cmap;

  CMapFormat4Node(CMapFormat4 cmap) {
    this.cmap = cmap;
  }

  @Override
  public int getChildCount() {
    return this.cmap.getSegCount();
  }

  @Override
  public AbstractNode getChildAt(int index) {
    return new CmapSegmentNode(this.cmap, index);
  }

  @Override
  public String getNodeName() {
    return this.cmap.toString();
  }

  @Override
  public JComponent render() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("Number of segments: %d%n", this.cmap.getSegCount()));
    for (int i = 0, imax = this.cmap.getSegCount(); i < imax; i++) {
      sb.append(String.format("%3d: from U+%04X until U+%04X%n",
          i, this.cmap.startCode(i), this.cmap.endCode(i)));
    }

    return new JTextArea(sb.toString());
  }
}

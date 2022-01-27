package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.table.core.CMapFormat4;
import javax.swing.JComponent;
import javax.swing.JTextArea;

class CmapSegmentNode extends AbstractNode {

  private final CMapFormat4 cmap;
  private final int index;

  CmapSegmentNode(CMapFormat4 cmap, int index) {
    this.cmap = cmap;
    this.index = index;
  }

  @Override
  public String getNodeName() {
    return String.format("U+%04X to U+%04X", cmap.startCode(index), cmap.endCode(index));
  }

  @Override
  public JComponent render() {
    int start = cmap.startCode(index);
    int end = cmap.endCode(index);

    StringBuilder sb = new StringBuilder();
    for (int cp = start; cp <= end; cp++) {
      sb.append(String.format("U+%04X -> %d%n", cp, cmap.glyphId(cp)));
    }
    return new JTextArea(sb.toString());
  }
}

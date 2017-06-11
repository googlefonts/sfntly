package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.core.NameTable;
import com.google.typography.font.sfntly.table.opentype.GSubTable;
import com.google.typography.font.sfntly.table.truetype.GlyphTable;
import com.google.typography.font.sfntly.table.truetype.LocaTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextArea;

class FontNode extends AbstractNode {

  private final Font font;
  private final List<AbstractNode> tables;

  FontNode(Font font) {
    this.font = font;
    this.tables = createTableNodes(font);
  }

  private static List<AbstractNode> createTableNodes(Font font) {
    List<AbstractNode> tableNodes = new ArrayList<AbstractNode>();
    for (Iterator<? extends Table> it = font.iterator(); it.hasNext(); ) {
      Table table = it.next();
      tableNodes.add(nodeFor(font, table));
    }
    Collections.sort(tableNodes, new Comparator<AbstractNode>() {
      @Override
      public int compare(AbstractNode o1, AbstractNode o2) {
        return o1.getNodeName().compareTo(o2.getNodeName());
      }
    });
    return tableNodes;
  }

  private static AbstractNode nodeFor(Font font, Table table) {
    int tag = table.headerTag();
    if (tag == Tag.loca) {
      return new LocaTableNode((LocaTable) table, Tag.stringValue(table.headerTag()));
    } else if (tag == Tag.glyf) {
      return new GlyfTableNode((GlyphTable) table, (LocaTable) font.getTable(Tag.loca));
    } else if (tag == Tag.GSUB) {
      return new GsubTableNode((GSubTable) table);
    } else {
      return new SubTableNode(table, Tag.stringValue(table.headerTag()));
    }
  }

  @Override
  public int getChildCount() {
    return this.tables.size();
  }

  @Override
  public AbstractNode getChildAt(int index) {
    return this.tables.get(index);
  }

  @Override
  protected String getNodeName() {
    NameTable nameTable = this.font.getTable(Tag.name);
    return nameTable.name(
        Font.PlatformId.Unicode.value(), Font.UnicodeEncodingId.Unicode2_0_BMP.value(),
        NameTable.UnicodeLanguageId.All.value(), NameTable.NameId.FullFontName.value());
  }

  @Override
  public JComponent render() {
    return new JTextArea(this.font.toString());
  }
}

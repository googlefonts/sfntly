// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.opentype.GsubLookupLigature.LigatureSubTable;
import com.google.typography.font.sfntly.table.opentype.GsubLookupLigature.LigatureSubTable.LigatureSet;
import com.google.typography.font.sfntly.table.opentype.GsubLookupLigature.LigatureSubTable.LigatureTable;
import com.google.typography.font.sfntly.table.opentype.GsubLookupList.GsubLookupType;
import com.google.typography.font.sfntly.table.opentype.GsubLookupSingle.SingleSubTable;
import com.google.typography.font.sfntly.table.opentype.IntSet.IntIterator;

import java.io.PrintWriter;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public class TableDump {
  private IndentingPrintWriter pw;
  private int maxItemsInList = 10;
  private ListFormat listFormat = ListFormat.STACK;
  private String listSeparator = " ";
  private String elemSeparator = " ";
  private boolean silenced;

  public enum ListFormat {
    STACK, STACK_NUMBER, LIST
  }

  public TableDump() {
    this(new PrintWriter(System.out, true));
  }

  public TableDump(PrintWriter pw) {
    this.pw = new IndentingPrintWriter(pw);
  }

  public TableDump setListFormat(ListFormat listFormat) {
    this.listFormat = listFormat;
    return this;
  }

  public TableDump format(String fmt, Object ... objs) {
    pw.format(fmt, objs);
    return this;
  }

  public TableDump formatln(String fmt, Object... objs) {
    pw.formatln(fmt, objs);
    return this;
  }

  public TableDump print(Object obj) {
    pw.print(obj);
    return this;
  }

  public TableDump println(Object obj) {
    pw.println(obj);
    return this;
  }

  public TableDump println() {
    pw.println();
    return this;
  }

  public TableDump flush() {
    pw.flush();
    return this;
  }

  public TableDump silence() {
    return this.silence(true);
  }

  public TableDump silence(boolean silence) {
    this.silenced = silence;
    return this;
  }

  public boolean silenced() {
    return silenced;
  }

  void in() {
    pw.in();
  }

  void out() {
    pw.out();
  }

  public void setMaxItemsInList(int maxItemsInList) {
    this.maxItemsInList = maxItemsInList;
  }

  public boolean displayType(GsubLookupType type) {
    return type == GsubLookupType.GSUB_SINGLE ||
        type == GsubLookupType.GSUB_LIGATURE;
  }

  public boolean silenced(Object obj) {
    return silenced;
  }

  private boolean isStack() {
    return listFormat != ListFormat.LIST;
  }

  private boolean isNumbered() {
    return listFormat == ListFormat.STACK_NUMBER;
  }

  public void dumpCollection(Iterable<?> collection) {
    if (silenced) {
      return;
    }
    boolean stack = isStack();
    boolean numbered = isNumbered();
    if (stack) {
      in();
    } else {
      if (!pw.empty) {
        print(elemSeparator);
      }
      print("[");
    }
    boolean first = true;
    int num = 0;
    for (Object o : collection) {
      if (!stack) {
        if (first) {
          first = false;
        } else {
          print(listSeparator);
        }
      }
      if (numbered) {
        format("[%d] ", num++);
      }
      dump(o);
    }
    if (stack) {
      out();
    } else {
      print("]");
    }
  }

  public void dump(Object obj) {
    if (silenced(obj)) {
      return;
    }
    if (obj == null) {
      println("null");
      return;
    }
    if (obj instanceof GsubLookupTable) {
      dumpLookup((GsubLookupTable) obj);
      return;
    }
    println(obj);
  }

  public void dumpLookup(GsubLookupTable lookup) {
    if (silenced(lookup)) {
      return;
    }
    switch (lookup.lookupType()) {
      case GSUB_SINGLE: dump((GsubLookupSingle) lookup); break;
      case GSUB_LIGATURE: dump((GsubLookupLigature) lookup); break;
      default: println(lookup.lookupType());
    }
  }

  public void dump(ScriptListTable scriptListTable) {
    if (silenced(scriptListTable)) {
      return;
    }
    int numScripts = scriptListTable.scriptCount();
    formatln("Script List");
    for (int i = 0; i < numScripts; ++i) {
      ScriptTable scriptTable = scriptListTable.scriptTableAt(i);
      format("%3d: ", i);
      in();
      dump(scriptTable);
      out();
      if (i > maxItemsInList) {
        formatln("... %d scripts in script list", numScripts);
        break;
      }
    }
  }

  public void dump(ScriptTable scriptTable) {
    if (silenced(scriptTable)) {
      return;
    }
    println(Tag.stringValue(scriptTable.scriptTag()));
    in();
    LangSysTable langSysTable = scriptTable.defaultLangSysTable();
    if (langSysTable != null) {
      print("---: DFLT ");
      in();
      dump(langSysTable);
      out();
    }
    int numLangSystems = scriptTable.langSysCount();
    for (int i = 0; i < numLangSystems; ++i) {
      format("%3d: %s ", i, Tag.stringValue(scriptTable.langSysTagAt(i)));
      langSysTable = scriptTable.langSysTableAt(i);
      in();
      dump(langSysTable);
      out();
      if (i > maxItemsInList) {
        formatln("... %d language systems in script table", numLangSystems);
        break;
      }
    }
    out();
  }

  public void dump(LangSysTable langSysTable) {
    if (silenced(langSysTable)) {
      return;
    }
    if (langSysTable.hasRequiredFeature()) {
      print(langSysTable.requiredFeatureIndex());
    } else {
      print("-");
    }
    int featureCount = langSysTable.featureCount();
    print(" [");
    for (int i = 0; i < featureCount; ++i) {
      if (i > 0) {
        print(" ");
      }
      print(langSysTable.featureIndexAt(i));
    }
    println("]");
  }

  public void dump(FeatureList featureList) {
    if (silenced(featureList)) {
      return;
    }
    int numFeatures = featureList.featureCount();
    println("FeatureList");
    for (int i = 0; i < numFeatures; ++i) {
      FeatureTable featureTable = featureList.featureTableAt(i);
      format("%3d: ", i);
      in();
      dump(featureTable);
      out();
      if (i >= maxItemsInList) {
        formatln("... %d features in feature list", numFeatures);
        break;
      }
    }
  }

  public void dump(FeatureTable featureTable) {
    if (silenced(featureTable)) {
      return;
    }
    print(Tag.stringValue(featureTable.featureTag()));
    print(" [");
    for (int i = 0; i < featureTable.lookupCount(); ++i) {
      if (i > 0) {
        print(" ");
      }
      print(featureTable.lookupListIndexAt(i));
    }
    println("]");
  }

  public void dump(GsubLookupList lookupList) {
    if (silenced(lookupList)) {
      return;
    }
    int numLookups = lookupList.lookupCount();
    println("Lookup List");
    for (int i = 0; i < numLookups; ++i) {
      GsubLookupType type = lookupList.lookupTypeAt(i);
      format("%3d: ", i);
      if (displayType(type)) {
        in();
        dumpLookup(lookupList.lookupAt(i));
        out();
      } else {
        println(type);
      }
      if (i >= maxItemsInList) {
        formatln("... %d lookups in lookup list", numLookups);
        break;
      }
    }
  }

  public void dump(GsubLookupSingle lookup) {
    if (silenced(lookup)) {
      return;
    }
    int numSubTables = lookup.subTableCount();
    println(lookup.lookupType());
    for (int j = 0; j < numSubTables; ++j) {
      int formatType = lookup.subTableFormatAt(j);
      formatln("%3d: fmt %d", j, formatType);
      in();
      SingleSubTable sst = lookup.subTableAt(j);
      IntIterator coveredGlyphs = sst.coverage().coveredGlyphs();
      int count = 0;
      while (coveredGlyphs.hasNext()) {
        int fromGlyphId = coveredGlyphs.next();
        int toGlyphId = sst.mapGlyphId(fromGlyphId);
        formatln("%3d: %04x -> %04x", count, fromGlyphId, toGlyphId);
        if (count >= maxItemsInList) {
          formatln("... %d covered glyphs in %s subtable %d",
              sst.coverage().coveredGlyphCount(), lookup.lookupType(), j);
          break;
        }
        ++count;
      }
      out();
    }
  }

  public void dump(GsubLookupLigature lookup) {
    if (silenced(lookup)) {
      return;
    }
    int numSubTables = lookup.subTableCount();
    println(lookup.lookupType());
    for (int i = 0; i < numSubTables; ++i) {
      int formatType = lookup.subTableFormatAt(i);
      formatln("%3d: fmt %d", i, formatType);
      in();
      LigatureSubTable lst = lookup.subTableAt(i);
      IntIterator coveredGlyphs = lst.coverage().coveredGlyphs();
      int count = 0;
      while (coveredGlyphs.hasNext()) {
        int firstGlyph = coveredGlyphs.next();
        LigatureSet set = lst.ligSetAt(count);
        int setCount = set.ligatureCount();
        formatln("%3d: starting with glyph %x", count, firstGlyph);
        in();
        for (int j = 0; j < setCount; ++j) {
          LigatureTable table = set.ligatureTableAt(j);
          format("%3d: [%x", j, firstGlyph);
          for (int k = 1; k < table.componentCount(); ++k) {
            format(" %x", table.componentAt(k));
          }
          formatln("] -> %x", table.ligatureGlyph());
          if (j > maxItemsInList) {
            formatln("... %d ligatures in %s subtable %d glyph %d (%x)",
                setCount, lookup.lookupType(), i, count, firstGlyph);
            break;
          }
        }
        out();
        count++;
        if (count > maxItemsInList) {
          formatln("... %d covered glyphs in %s subtable %d", lst.coverage().coveredGlyphCount(),
              lookup.lookupType(), i);
          break;
        }
      }
      out();
    }
  }

  public void dump(GSubTable gSubTable) {
    if (silenced(gSubTable)) {
      return;
    }
    Header gSubHeader = gSubTable.header();
    println(gSubHeader);
    in();
    formatln("Version: %x", gSubTable.version());
    ScriptListTable scriptListTable = gSubTable.scriptList();
    dump(scriptListTable);
    FeatureList featureList = gSubTable.featureList();
    dump(featureList);
    GsubLookupList lookupList = gSubTable.lookupList();
    dump(lookupList);
    out();
  }

  private static class IndentingPrintWriter {
    private PrintWriter pw;
    private String indentString;
    private int indentLevel;
    private boolean empty = true;

    public IndentingPrintWriter(PrintWriter pw) {
      this(pw, "  ");
    }

    public IndentingPrintWriter(PrintWriter pw, String indentString) {
      this.pw = pw;
      this.indentString = indentString;
    }

    public IndentingPrintWriter in() {
      ++indentLevel;
      return this;
    }

    public IndentingPrintWriter out() {
      if (indentLevel > 0) {
        --indentLevel;
      }
      return this;
    }

    public IndentingPrintWriter println() {
      pw.println();
      empty = true;
      return this;
    }

    public IndentingPrintWriter format(String fmt, Object... objs) {
      print(String.format(fmt, objs));
      return this;
    }

    public IndentingPrintWriter formatln(String fmt, Object... objs) {
      format(fmt, objs);
      println();
      return this;
    }

    public IndentingPrintWriter print(Object o) {
      String s = o.toString();
      if (s.isEmpty()) {
        return this;
      }
      if (empty) {
        for (int i = 0; i < indentLevel; ++i) {
          pw.print(indentString);
        }
        empty = false;
      }
      pw.print(s);
      return this;
    }

    public IndentingPrintWriter println(Object o) {
      print(o);
      println();
      return this;
    }

    public IndentingPrintWriter flush() {
      pw.flush();
      return this;
    }
  }
}

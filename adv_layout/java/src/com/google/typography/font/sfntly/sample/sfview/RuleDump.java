package com.google.typography.font.sfntly.sample.sfview;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.FontFactory;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.core.CMap;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.sfntly.table.core.PostScriptTable;
import com.google.typography.font.sfntly.table.opentype.GSubTable;
import com.google.typography.font.sfntly.table.opentype.LookupListTable;
import com.google.typography.font.sfntly.table.opentype.component.GlyphGroup;
import com.google.typography.font.sfntly.table.opentype.component.Rule;
import com.google.typography.font.sfntly.table.opentype.component.RuleExtractor;
import com.google.typography.font.sfntly.table.opentype.component.RuleSegment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RuleDump {
  public static void main(String[] args) throws IOException {

    String fontName = args[0];
    String[] charCodes = Arrays.copyOfRange(args, 1, args.length);

    List<Integer> codes = new ArrayList<Integer>();
    for (String charCodeStr : charCodes) {
      codes.add(Integer.parseInt(charCodeStr, 16));
    }

    System.out.println("Rules from font: " + fontName);
    Font[] fonts = loadFont(new File(fontName));
    if (fonts == null) {
      throw new IllegalArgumentException("No font found");
    }
    for (Font font : fonts) {
      GSubTable gsub = font.getTable(Tag.GSUB);
      if (gsub != null) {
        CMapTable cmapTable = font.getTable(Tag.cmap);

        GlyphGroup glyphGroup = new GlyphGroup();

        for (int code : codes) {
          for (CMap cmap : cmapTable) {
            int glyph = cmap.glyphId(code);
            if (glyph != CMapTable.NOTDEF) {
              glyphGroup.add(glyph);
            }
          }
        }

        LookupListTable lookupList = gsub.lookupList();
        Map<Integer, List<Rule>> rules = RuleExtractor.extract(lookupList);
        PostScriptTable post = font.getTable(Tag.post);
        dump(rules, post);
        System.out.println("Closure:");
        System.out.println(toString(Rule.closure(rules, glyphGroup), post));
      } else {
        throw new IllegalArgumentException("No GSUB Table found");
      }
    }
  }

  public static Font[] loadFont(File file) throws IOException {
    FontFactory fontFactory = FontFactory.getInstance();
    fontFactory.fingerprintFont(true);
    FileInputStream is = null;
    try {
      is = new FileInputStream(file);
      return fontFactory.loadFonts(is);
    } catch (FileNotFoundException e) {
      System.err.println("Could not load the font: " + file.getName());
      return null;
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

  public static void dump(Map<Integer, List<Rule>> rulesList, PostScriptTable post) {
    for (int index : rulesList.keySet()) {
      List<Rule> rules = rulesList.get(index);
      System.out.println(
          "------------------------------ " + index + " --------------------------------");
      for (Rule rule : rules) {
        System.out.println(toString(rule, post));
      }
    }
  }

  static String toString(Rule rule, PostScriptTable post) {
    StringBuilder sb = new StringBuilder();
    if (rule.backtrack != null && rule.backtrack.size() > 0) {
      sb.append(toString(rule.backtrack, post));
      sb.append("} ");
    }
    sb.append(toString(rule.input, post));
    if (rule.lookAhead != null && rule.lookAhead.size() > 0) {
      sb.append("{ ");
      sb.append(toString(rule.lookAhead, post));
    }
    sb.append("=> ");
    sb.append(toString(rule.subst, post));
    return sb.toString();
  }

  static String toString(RuleSegment context, PostScriptTable post) {
    StringBuilder sb = new StringBuilder();
    for (GlyphGroup glyphGroup : context) {
      sb.append(" [ ");
      sb.append(toString(glyphGroup, post));
      sb.append("] ");
    }
    return sb.toString();
  }

  static String toString(Collection<Integer> glyphIds, PostScriptTable post) {
    StringBuilder sb = new StringBuilder();
    for (int glyphId : glyphIds) {
      sb.append(glyphId);

      String glyphName = (glyphId < 0) ? "(all)" : post.glyphName(glyphId);
      if (glyphName != null) {
        sb.append("-");
        sb.append(glyphName);
      }
      sb.append(" ");
    }
    return sb.toString();
  }
}

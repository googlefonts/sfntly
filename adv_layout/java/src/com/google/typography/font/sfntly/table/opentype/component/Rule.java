package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.core.CMap;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.sfntly.table.core.PostScriptTable;
import com.google.typography.font.sfntly.table.opentype.FeatureListTable;
import com.google.typography.font.sfntly.table.opentype.FeatureTable;
import com.google.typography.font.sfntly.table.opentype.GSubTable;
import com.google.typography.font.sfntly.table.opentype.LookupListTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Rule {
  public final RuleSegment backtrack;
  public final GlyphList input;
  public final RuleSegment lookAhead;
  public final RuleSegment subst;

  public Rule(RuleSegment backtrack, GlyphList input, RuleSegment lookAhead, RuleSegment subst) {
    this.backtrack = backtrack;
    this.input = input;
    this.lookAhead = lookAhead;
    this.subst = subst;
  }

  public Rule(Rule other, RuleSegment subst) {
    this.backtrack = other.backtrack;
    this.input = other.input;
    this.lookAhead = other.lookAhead;
    this.subst = subst;
  }

  public RuleSegment apply(RuleSegment srcGlyphIds, int at) {

    int i = at;
    if (backtrack != null) {
      for (GlyphGroup b : backtrack) {
        i--;
        if (i < 0 || i >= srcGlyphIds.size() || !b.isIntersecting(srcGlyphIds.get(i))) {
          if (!b.contains(-1)) {
            return null;
          }
        }
      }
    }

    if (input != null) {
      i = at;
      for (int in : input) {
        if (i < 0 || i >= srcGlyphIds.size() || !srcGlyphIds.get(i).contains(in)) {
          return null;
        }
        i++;
      }
    }

    if (lookAhead != null) {
      i = at + input.size();
      for (GlyphGroup l : lookAhead) {
        if (i < 0 || i >= srcGlyphIds.size() || !l.isIntersecting(srcGlyphIds.get(i))) {
          if (!l.contains(-1)) {
            return null;
          }
        }
        i++;
      }
    }

    RuleSegment result = new RuleSegment();
    result.addAll(srcGlyphIds.subList(0, at));
    result.addAll(subst);
    result.addAll(srcGlyphIds.subList(at + input.size(), srcGlyphIds.size()));
    return result;
  }

  static RuleSegment apply(List<Rule> rules, RuleSegment given, int at) {
    for (Rule rule : rules) {
      RuleSegment result = rule.apply(given, at);
      if (result != null) {
        return result;
      }
    }
    return given;
  }

  public GlyphGroup apply(GlyphGroup glyphs) {
    if (backtrack != null) {
      for (GlyphGroup b : backtrack) {
        if (b.contains(-1)) {
          continue;
        }
        if (!b.isIntersecting(glyphs)) {
          return glyphs;
        }
      }
    }

    for (int in : input) {
      if (!glyphs.contains(in)) {
        return glyphs;
      }
    }

    if (lookAhead != null) {
      for (GlyphGroup l : lookAhead) {
        if (l.contains(-1)) {
          continue;
        }
        if (!l.isIntersecting(glyphs)) {
          return glyphs;
        }
      }
    }

    GlyphGroup result = new GlyphGroup(glyphs);
    for (GlyphGroup glyphGroup : subst) {
      result.addAll(glyphGroup);
    }
    return result;
  }

  public static GlyphGroup closure(List<Rule> lookupRules, GlyphGroup glyphs) {
    int prevSize = 0;
    while (glyphs.size() > prevSize) {
      prevSize = glyphs.size();
      for (Rule rule : lookupRules) {
        glyphs = rule.apply(glyphs);
      }
    }
    return glyphs;
  }

  static List<Rule> applyOnRuleSubsts(List<Rule> targetRules, int at, List<Rule> rulesToApply) {
    List<Rule> result = new ArrayList<Rule>();
    for (Rule targetRule : targetRules) {
      RuleSegment newSubst = new RuleSegment();
      newSubst.addAll(Rule.apply(rulesToApply, targetRule.subst, at));
      Rule newRule = new Rule(targetRule, newSubst);
      result.add(newRule);
    }
    return result;
  }

  static Rule prependToInput(int prefix, Rule other) {
    GlyphList input = new GlyphList(prefix);
    input.addAll(other.input);

    return new Rule(other.backtrack, input, other.lookAhead, other.subst);
  }

  static List<Rule> prependToInput(int prefix, List<Rule> rules) {
    List<Rule> result = new ArrayList<Rule>();
    for (Rule rule : rules) {
      result.add(prependToInput(prefix, rule));
    }
    return result;
  }

  static List<Rule> deltaRules(List<Integer> glyphIds, int delta) {
    List<Rule> result = new ArrayList<Rule>();
    for (int glyphId : glyphIds) {
      GlyphList input = new GlyphList(glyphId);
      RuleSegment subst = new RuleSegment(glyphId + delta);
      result.add(new Rule(null, input, null, subst));
    }
    return result;
  }

  static List<Rule> oneToOneRules(List<Integer> inputs, List<Integer> substs) {
    if (inputs.size() != substs.size()) {
      throw new IllegalArgumentException("input - subst should have same count");
    }

    List<Rule> result = new ArrayList<Rule>();
    for (int i = 0; i < inputs.size(); i++) {
      GlyphList input = new GlyphList(inputs.get(i));
      RuleSegment subst = new RuleSegment(substs.get(i));
      result.add(new Rule(null, input, null, subst));
    }
    return result;
  }

  static List<GlyphList> permuteToSegments(List<GlyphGroup> glyphGroups) {
    List<GlyphList> result = new ArrayList<GlyphList>();
    result.add(new GlyphList());

    for (GlyphGroup glyphGroup : glyphGroups) {
      List<GlyphList> newResult = new ArrayList<GlyphList>();
      for (Integer glyphId : glyphGroup) {
        for (GlyphList glyphList : result) {
          GlyphList newGlyphList = new GlyphList();
          newGlyphList.addAll(glyphList);
          newGlyphList.add(glyphId);
          newResult.add(newGlyphList);
        }
      }
      result = newResult;
    }
    return result;
  }

  static List<Rule> permuteContext(
      RuleSegment backtrack, List<GlyphList> inputs, RuleSegment lookAhead) {
    List<Rule> result = new ArrayList<Rule>();
    for (GlyphList input : inputs) {
      result.add(new Rule(backtrack, input, lookAhead, new RuleSegment(input)));
    }
    return result;
  }

  public static List<Rule> featuredRules(
      FeatureListTable featureList, Map<Integer, List<Rule>> ruleMap) {
    Set<Integer> lookupIds = featuredLookups(featureList);
    List<Rule> rules = new ArrayList<Rule>();
    for (int lookupId : lookupIds) {
      rules.addAll(ruleMap.get(lookupId));
    }
    return rules;
  }

  private static Set<Integer> featuredLookups(FeatureListTable featureList) {
    Set<Integer> lookupIds = new HashSet<Integer>();
    for (FeatureTable feature : featureList) {
      for (NumRecord lookupIdRecord : feature) {
        int lookupId = lookupIdRecord.value;
        lookupIds.add(lookupId);
      }
    }
    // System.out.println("Featured Lookups: " + lookupIds);
    return lookupIds;
  }

  public static GlyphGroup charGlyphClosure(String txt, Font font) {
    PostScriptTable post = font.getTable(Tag.post);
    CMapTable cmapTable = font.getTable(Tag.cmap);
    GlyphGroup glyphGroup = glyphGroupForText(txt, cmapTable);

    List<Rule> featuredRules = featuredRules(font);
    GlyphGroup ruleClosure = Rule.closure(featuredRules, glyphGroup);
    System.out.println("Closure: " + toString(ruleClosure, post));
    return ruleClosure;
  }

  public static List<Rule> featuredRules(Font font) {
    GSubTable gsub = font.getTable(Tag.GSUB);
    if (gsub == null) {
      System.err.println("No GSUB Table found");
      return null;
    }

    LookupListTable lookupList = gsub.lookupList();
    FeatureListTable featureList = gsub.featureList();

    Map<Integer, List<Rule>> ruleMap = RuleExtractor.extract(lookupList);
    PostScriptTable post = font.getTable(Tag.post);
    dump(ruleMap, post);

    List<Rule> featuredRules = Rule.featuredRules(featureList, ruleMap);
    return featuredRules;
  }

  public static GlyphGroup glyphGroupForText(String str, CMapTable cmapTable) {
    GlyphGroup glyphGroup = new GlyphGroup();
    List<Integer> codes = codepointsFromStr(str);
    for (int code : codes) {
      for (CMap cmap : cmapTable) {
        int glyph = cmap.glyphId(code);
        if (glyph != CMapTable.NOTDEF) {
          glyphGroup.add(glyph);
        }
      }
    }
    return glyphGroup;
  }

  private static List<Integer> codepointsFromStr(String s) {
    List<Integer> list = new ArrayList<Integer>();
    for (int cp, i = 0; i < s.length(); i += Character.charCount(cp)) {
      cp = s.codePointAt(i);
      list.add(cp);
    }
    return list;
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
      sb.append(") ");
    }
    sb.append(toString(rule.input, post));
    if (rule.lookAhead != null && rule.lookAhead.size() > 0) {
      sb.append("( ");
      sb.append(toString(rule.lookAhead, post));
    }
    sb.append("=> ");
    sb.append(toString(rule.subst, post));
    return sb.toString();
  }

  static String toString(RuleSegment context, PostScriptTable post) {
    StringBuilder sb = new StringBuilder();
    for (GlyphGroup glyphGroup : context) {
      int glyphCount = glyphGroup.size();
      if (glyphCount > 1) {
        sb.append("{ ");
      }
      sb.append(toString(glyphGroup, post));
      sb.append(" ");
      if (glyphCount > 1) {
        sb.append("} ");
      }
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

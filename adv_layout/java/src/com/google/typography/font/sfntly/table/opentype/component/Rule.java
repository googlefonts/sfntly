package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.core.CMap;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.sfntly.table.core.PostScriptTable;
import com.google.typography.font.sfntly.table.opentype.FeatureListTable;
import com.google.typography.font.sfntly.table.opentype.GSubTable;
import com.google.typography.font.sfntly.table.opentype.LangSysTable;
import com.google.typography.font.sfntly.table.opentype.LookupListTable;
import com.google.typography.font.sfntly.table.opentype.ScriptListTable;
import com.google.typography.font.sfntly.table.opentype.ScriptTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Rule {
  public final RuleSegment backtrack;
  public final RuleSegment input;
  public final RuleSegment lookAhead;
  public final RuleSegment subst;

  public Rule(RuleSegment backtrack, RuleSegment input, RuleSegment lookAhead, RuleSegment subst) {
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

  // Closure related

  public static GlyphGroup charGlyphClosure(String txt, Font font) {
    PostScriptTable post = font.getTable(Tag.post);
    CMapTable cmapTable = font.getTable(Tag.cmap);
    GlyphGroup glyphGroup = glyphGroupForText(txt, cmapTable);

    List<Rule> featuredRules = featuredRules(font);
    Map<Integer, List<Rule>> glyphRuleMap = createGlyphRuleMap(featuredRules);
    GlyphGroup ruleClosure = closure(glyphRuleMap, glyphGroup);
    System.out.println("Closure: " + toString(ruleClosure, post));
    return ruleClosure;
  }

  static GlyphGroup closure(Map<Integer, List<Rule>> glyphRuleMap, GlyphGroup glyphs) {
    int prevSize = 0;
    while (glyphs.size() > prevSize) {
      prevSize = glyphs.size();
      for (Rule rule : rulesForGlyph(glyphRuleMap, glyphs)) {
        rule.addMatchingTargetGlyphs(glyphs);
      }
    }
    return glyphs;
  }

  private void addMatchingTargetGlyphs(GlyphGroup glyphs) {
    for (RuleSegment seg : new RuleSegment[] { input, backtrack, lookAhead }) {
      if (seg == null) {
        continue;
      }
      for (GlyphGroup g : seg) {
        if (g.contains(-1)) {
          continue;
        }
        if (!g.isIntersecting(glyphs)) {
          return;
        }
      }
    }

    for (GlyphGroup glyphGroup : subst) {
      glyphs.addAll(glyphGroup);
    }
  }

  public static Map<Integer, List<Rule>> glyphRulesMap(Font font) {
    List<Rule> featuredRules = Rule.featuredRules(font);
    if (featuredRules == null) {
      return null;
    }
    return createGlyphRuleMap(featuredRules);
  }

  private static Map<Integer, List<Rule>> createGlyphRuleMap(List<Rule> lookupRules) {
    Map<Integer, List<Rule>> map = new HashMap<>();

    for (Rule rule : lookupRules) {
      for (GlyphGroup glyphGroup : rule.input) {
        for (int glyph : glyphGroup) {
          if (!map.containsKey(glyph)) {
            map.put(glyph, new ArrayList<Rule>());
          }
          map.get(glyph).add(rule);
        }
      }
    }
    return map;
  }

  private static Set<Rule> rulesForGlyph(Map<Integer, List<Rule>> glyphRuleMap, Set<Integer> glyphs) {
    Set<Rule> set = new HashSet<>();
    for(int glyph : glyphs) {
      if (glyphRuleMap.containsKey(glyph)) {
        set.addAll(glyphRuleMap.get(glyph));
      }
    }
    return set;
  }

  private static List<Rule> featuredRules(
      Set<Integer> lookupIds, Map<Integer, List<Rule>> ruleMap) {
    List<Rule> rules = new ArrayList<>();
    for (int lookupId : lookupIds) {
      rules.addAll(ruleMap.get(lookupId));
    }
    return rules;
  }

  private static List<Rule> featuredRules(Font font) {
    GSubTable gsub = font.getTable(Tag.GSUB);
    if (gsub == null) {
      return null;
    }

    ScriptListTable scripts = gsub.scriptList();
    FeatureListTable featureList = gsub.featureList();
    LookupListTable lookupList = gsub.lookupList();
    Map<Integer, List<Rule>> ruleMap = RuleExtractor.extract(lookupList);

    Set<Integer> features = new HashSet<>();
    Set<Integer> lookupIds = new HashSet<>();

    for (ScriptTable script : scripts.map().values()) {
      for (LangSysTable langSys : script.map().values()) {
        // We are assuming if required feature exists, it will be in the list
        // of features as well.
        for (NumRecord feature : langSys) {
          if (!features.contains(feature.value)) {
            features.add(feature.value);
            for (NumRecord lookup : featureList.subTableAt(feature.value)) {
              lookupIds.add(lookup.value);
            }
          }
        }
      }
    }
    List<Rule> featuredRules = Rule.featuredRules(lookupIds, ruleMap);
    return featuredRules;
  }


  // Utility method for glyphs for text

  public static GlyphGroup glyphGroupForText(String str, CMapTable cmapTable) {
    GlyphGroup glyphGroup = new GlyphGroup();
    Set<Integer> codes = codepointsFromStr(str);
    for (int code : codes) {
      for (CMap cmap : cmapTable) {
        if (cmap.platformId() == 3 && cmap.encodingId() == 1 || // Unicode BMP
            cmap.platformId() == 3 && cmap.encodingId() == 10 || // UCS2
            cmap.platformId() == 0 && cmap.encodingId() == 5) { // Variation
          int glyph = cmap.glyphId(code);
          if (glyph != CMapTable.NOTDEF) {
            glyphGroup.add(glyph);
          }
          // System.out.println("code: " + code + " glyph: " + glyph + " platform: " + cmap.platformId() + " encodingId: " + cmap.encodingId() + " format: " + cmap.format());

        }
      }
    }
    return glyphGroup;
  }

  // Rule operation

  public RuleSegment apply(RuleSegment srcGlyphIds, int at) {
    if (!match(srcGlyphIds, at)) {
      return null;
    }

    RuleSegment result = new RuleSegment();
    result.addAll(srcGlyphIds.subList(0, at));
    result.addAll(subst);
    result.addAll(srcGlyphIds.subList(at + input.size(), srcGlyphIds.size()));
    return result;
  }

  private boolean match(RuleSegment srcGlyphIds, int at) {
    if (at < 0 || at >= srcGlyphIds.size()) {
      return false;
    }

    if (srcGlyphIds.size() - at < input.size()) {
      return false;
    }

    if (backtrack != null && at < backtrack.size()) {
      return false;
    }

    if (lookAhead != null && srcGlyphIds.size() - at < input.size() + lookAhead.size()) {
      return false;
    }

    int i = at;
    for (GlyphGroup g : input)  {
      if (g.contains(-1)) {
        continue;
      }
      if (!g.isIntersecting(srcGlyphIds.get(i))) {
        return false;
      }
      i++;
    }

    if (backtrack != null) {
      i = at - 1;
      for (GlyphGroup g : backtrack) {
        if (g.contains(-1)) {
          continue;
        }
        if (!g.isIntersecting(srcGlyphIds.get(i))) {
          return false;
        }
        i--;
      }
    }

    if (lookAhead != null) {
      i = at + input.size();
      for (GlyphGroup g : lookAhead) {
        if (g.contains(-1)) {
          continue;
        }
        if (!g.isIntersecting(srcGlyphIds.get(i))) {
          return false;
        }
        i++;
      }
    }
    return true;
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

  static Rule applyOnNoSubstRule(Rule ruleToApply, Rule targetRule, int at) {

    if (!ruleToApply.match(targetRule.input, at)) {
      return null;
    }

    RuleSegment newBacktrack = new RuleSegment();
    if (targetRule.backtrack != null) {
      newBacktrack.addAll(targetRule.backtrack);
    }
    newBacktrack.addAll(targetRule.input.subList(0, at));

    RuleSegment newLookAhead = new RuleSegment();
    newLookAhead.addAll(targetRule.input.subList(at + ruleToApply.input.size(), targetRule.input.size()));
    if (targetRule.lookAhead != null) {
      newLookAhead.addAll(targetRule.lookAhead);
    }

    return new Rule(newBacktrack, ruleToApply.input, newLookAhead, ruleToApply.subst);

  }

  static List<Rule> applyOnRuleSubsts(List<Rule> targetRules, int at, List<Rule> rulesToApply) {
    List<Rule> result = new ArrayList<>();
    for (Rule targetRule : targetRules) {
      if (targetRule.subst != null) {
        RuleSegment newSubst = apply(rulesToApply, targetRule.subst, at);
        Rule newRule = new Rule(targetRule, newSubst);
        result.add(newRule);
      } else {
        for (Rule ruleToApply : rulesToApply) {
          Rule newRule = applyOnNoSubstRule(ruleToApply, targetRule, at);
          if (newRule != null) {
            result.add(newRule);
          }
        }
        result.add(targetRule);
      }
    }
    return result;
  }

  static Rule prependToInput(int prefix, Rule other) {
    RuleSegment input = new RuleSegment(prefix);
    input.addAll(other.input);

    return new Rule(other.backtrack, input, other.lookAhead, other.subst);
  }

  static List<Rule> prependToInput(int prefix, List<Rule> rules) {
    List<Rule> result = new ArrayList<>();
    for (Rule rule : rules) {
      result.add(prependToInput(prefix, rule));
    }
    return result;
  }

  static List<Rule> deltaRules(List<Integer> glyphIds, int delta) {
    List<Rule> result = new ArrayList<>();
    for (int glyphId : glyphIds) {
      RuleSegment input = new RuleSegment(glyphId);
      RuleSegment subst = new RuleSegment(glyphId + delta);
      result.add(new Rule(null, input, null, subst));
    }
    return result;
  }

  static List<Rule> oneToOneRules(RuleSegment backtrack, List<Integer> inputs, RuleSegment lookAhead, List<Integer> substs) {
    if (inputs.size() != substs.size()) {
      throw new IllegalArgumentException("input - subst should have same count");
    }

    List<Rule> result = new ArrayList<>();
    for (int i = 0; i < inputs.size(); i++) {
      RuleSegment input = new RuleSegment(inputs.get(i));
      RuleSegment subst = new RuleSegment(substs.get(i));
      result.add(new Rule(backtrack, input, lookAhead, subst));
    }
    return result;
  }

  static List<Rule> oneToOneRules(List<Integer> inputs, List<Integer> substs) {
    return oneToOneRules(null, inputs, null, substs);
  }

  static List<Rule> addContextToInputs(
      RuleSegment backtrack, List<RuleSegment> inputs, RuleSegment lookAhead) {
    List<Rule> result = new ArrayList<>();
    for (RuleSegment input : inputs) {
      result.add(new Rule(backtrack, input, lookAhead, null));
    }
    return result;
  }

  // Dump routines

  private static Set<Integer> codepointsFromStr(String s) {
    Set<Integer> list = new HashSet<>();
    for (int cp, i = 0; i < s.length(); i += Character.charCount(cp)) {
      cp = s.codePointAt(i);
      list.add(cp);
    }
    return list;
  }

  public static void dumpRuleMap(Map<Integer, List<Rule>> rulesList, PostScriptTable post) {
    for (int index : rulesList.keySet()) {
      List<Rule> rules = rulesList.get(index);
      System.out.println(
          "------------------------------ " + index + " --------------------------------");
      for (Rule rule : rules) {
        System.out.println(toString(rule, post));
      }
    }
  }


  static void dumpLookups(Font font) {
    GSubTable gsub = font.getTable(Tag.GSUB);
    Map<Integer, List<Rule>> ruleMap = RuleExtractor.extract(gsub.lookupList());
    PostScriptTable post = font.getTable(Tag.post);
    dumpRuleMap(ruleMap, post);
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

      String glyphName = glyphId < 0 ? "(all)" : post.glyphName(glyphId);
      if (glyphName != null) {
        sb.append("-");
        sb.append(glyphName);
      }
      sb.append(" ");
    }
    return sb.toString();
  }
}
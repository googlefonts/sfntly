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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

    Set<Rule> featuredRules = featuredRules(font);
    Map<Integer, Set<Rule>> glyphRuleMap = createGlyphRuleMap(featuredRules);
    GlyphGroup ruleClosure = closure(glyphRuleMap, glyphGroup);
    System.out.println("Closure: " + toString(ruleClosure, post));
    return ruleClosure;
  }

  static GlyphGroup closure(Map<Integer, Set<Rule>> glyphRuleMap, GlyphGroup glyphs) {
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
        if (!g.intersects(glyphs)) {
          return;
        }
      }
    }

    for (GlyphGroup glyphGroup : subst) {
      glyphs.addAll(glyphGroup);
    }
  }

  public static Map<Integer, Set<Rule>> glyphRulesMap(Font font) {
    Set<Rule> featuredRules = Rule.featuredRules(font);
    if (featuredRules == null) {
      return null;
    }
    return createGlyphRuleMap(featuredRules);
  }

  private static Map<Integer, Set<Rule>> createGlyphRuleMap(Set<Rule> lookupRules) {
    Map<Integer, Set<Rule>> map = new HashMap<>();

    for (Rule rule : lookupRules) {
      for (int glyph : rule.input.get(0)) {
        if (!map.containsKey(glyph)) {
          map.put(glyph, new HashSet<Rule>());
        }
        map.get(glyph).add(rule);
      }
    }
    return map;
  }

  private static Set<Rule> rulesForGlyph(Map<Integer, Set<Rule>> glyphRuleMap, GlyphGroup glyphs) {
    Set<Rule> set = new HashSet<>();
    for(int glyph : glyphs) {
      if (glyphRuleMap.containsKey(glyph)) {
        set.addAll(glyphRuleMap.get(glyph));
      }
    }
    return set;
  }

  private static Set<Rule> featuredRules(
      Set<Integer> lookupIds, Map<Integer, Set<Rule>> ruleMap) {
    Set<Rule> rules = new LinkedHashSet<>();
    for (int lookupId : lookupIds) {
      Set<Rule> ruleForLookup = ruleMap.get(lookupId);
      if (ruleForLookup == null) {
        System.err.printf("Lookup ID %d is used in features but not defined.\n", lookupId);
        continue;
      }
      rules.addAll(ruleForLookup);
    }
    return rules;
  }

  private static Set<Rule> featuredRules(Font font) {
    GSubTable gsub = font.getTable(Tag.GSUB);
    if (gsub == null) {
      return null;
    }

    ScriptListTable scripts = gsub.scriptList();
    FeatureListTable featureList = gsub.featureList();
    LookupListTable lookupList = gsub.lookupList();
    Map<Integer, Set<Rule>> ruleMap = RuleExtractor.extract(lookupList);

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
    Set<Rule> featuredRules = Rule.featuredRules(lookupIds, ruleMap);
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

  public Rule applyRuleOnRuleWithSubst(Rule targetRule, int at) {
    if (!targetRule.match(this, at)) {
      return null;
    }

    if (at < targetRule.subst.size() && at + input.size() <=  targetRule.subst.size()) {
      RuleSegment newSubst = new RuleSegment();
      newSubst.addAll(targetRule.subst.subList(0, at));
      newSubst.addAll(subst);
      newSubst.addAll(targetRule.subst.subList(at + input.size(), targetRule.subst.size()));

      Rule newTargetRule = new Rule(targetRule, newSubst);
      return newTargetRule;
    }

    if (at == targetRule.subst.size()) {
      RuleSegment newInput = new RuleSegment();
      newInput.addAll(targetRule.input);
      newInput.addAll(input);

      RuleSegment newLookAhead = new RuleSegment();
      newLookAhead.addAll(targetRule.lookAhead.subList(input.size(), targetRule.lookAhead.size()));

      RuleSegment newSubst = new RuleSegment();
      newSubst.addAll(targetRule.subst);
      newSubst.addAll(subst);

      Rule newTargetRule = new Rule(targetRule.backtrack, newInput, newLookAhead, newSubst);
      return newTargetRule;
    }

    return null;
  }

  static Rule applyRuleOnRuleWithoutSubst(Rule ruleToApply, Rule targetRule, int at) {

    if (!targetRule.match(ruleToApply, at)) {
      return null;
    }

    RuleSegment newBacktrack = targetRule.backtrack;
    if (0 < at) {
      newBacktrack = new RuleSegment();
      if (targetRule.backtrack != null) {
        newBacktrack.addAll(targetRule.backtrack);
      }
      newBacktrack.addAll(targetRule.input.subList(0, at));
    }

    int remainingTargetInputBegin = at + ruleToApply.input.size();
    int targetInputEnd = targetRule.input.size();

    RuleSegment newLookAhead = targetRule.lookAhead;
    if (remainingTargetInputBegin < targetInputEnd) {
      newLookAhead = new RuleSegment();
      newLookAhead.addAll(targetRule.input.subList(remainingTargetInputBegin, targetInputEnd));
      if (targetRule.lookAhead != null) {
        newLookAhead.addAll(targetRule.lookAhead);
      }
    }

    return new Rule(newBacktrack, ruleToApply.input, newLookAhead, ruleToApply.subst);
  }

  static void applyRulesOnRuleWithSubst(Set<Rule> rulesToApply, Rule targetRule, int at, Set<Rule> accumulateTo) {
    for (Rule ruleToApply : rulesToApply) {
      Rule newRule = ruleToApply.applyRuleOnRuleWithSubst(targetRule, at);
      if (newRule != null) {
        accumulateTo.add(newRule);
      }
    }
  }

  static void applyRulesOnRuleWithoutSubst(Set<Rule> rulesToApply, Rule targetRule, int at, Set<Rule> accumulateTo) {
    for (Rule ruleToApply : rulesToApply) {
      Rule newRule = applyRuleOnRuleWithoutSubst(ruleToApply, targetRule, at);
      if (newRule != null) {
        accumulateTo.add(newRule);
      }
    }
  }

  static Set<Rule> applyRulesOnRules(Set<Rule> rulesToApply, Set<Rule> targetRules, int at) {
    Set<Rule> result = new LinkedHashSet<>();
    for (Rule targetRule : targetRules) {
      if (targetRule.subst != null) {
        applyRulesOnRuleWithSubst(rulesToApply, targetRule, at, result);
      } else {
        applyRulesOnRuleWithoutSubst(rulesToApply, targetRule, at, result);
      }
    }
    return result;
  }

  private boolean match(Rule other, int at) {
    if (at < 0) {
      throw new IllegalStateException();
    }

    RuleSegment thisAllSegments = new RuleSegment();
    if (backtrack != null) {
      thisAllSegments.addAll(backtrack);
    }
    thisAllSegments.addAll(input);
    if (lookAhead != null) {
      thisAllSegments.addAll(lookAhead);
    }

    RuleSegment otherAllSegments = new RuleSegment();
    if (other.backtrack != null) {
      otherAllSegments.addAll(other.backtrack);
    }
    otherAllSegments.addAll(other.input);
    if (other.lookAhead != null) {
      otherAllSegments.addAll(other.lookAhead);
    }

    int backtrackSize = backtrack != null ? backtrack.size() : 0;
    int otherBacktrackSize = other.backtrack != null ? other.backtrack.size() : 0;
    int initialPos = backtrackSize - otherBacktrackSize + at;

    if (initialPos < 0) {
      return false;
    }

    if (thisAllSegments.size() - initialPos < otherAllSegments.size()) {
      return false;
    }

    for(int i = 0; i < otherAllSegments.size(); i++) {
      GlyphGroup thisGlyphs = thisAllSegments.get(i + initialPos);
      GlyphGroup otherGlyphs = otherAllSegments.get(i);

      if (!thisGlyphs.intersects(otherGlyphs)) {
        return false;
      }
    }

    return true;
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

  public static void dumpRuleMap(Map<Integer, Set<Rule>> rulesList, PostScriptTable post) {
    for (int index : rulesList.keySet()) {
      Set<Rule> rules = rulesList.get(index);
      System.out.println(
          "------------------------------ " + index + " --------------------------------");
      for (Rule rule : rules) {
        System.out.println(toString(rule, post));
      }
    }
  }


  static void dumpLookups(Font font) {
    GSubTable gsub = font.getTable(Tag.GSUB);
    Map<Integer, Set<Rule>> ruleMap = RuleExtractor.extract(gsub.lookupList());
    PostScriptTable post = font.getTable(Tag.post);
    dumpRuleMap(ruleMap, post);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (backtrack != null && backtrack.size() > 0) {
      sb.append(backtrack.toString());
      sb.append("> ");
    }
    sb.append(input.toString());
    if (lookAhead != null && lookAhead.size() > 0) {
      sb.append("< ");
      sb.append(lookAhead.toString());
    }
    sb.append(" => ");
    sb.append(subst.toString());
    return sb.toString();
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

  static String toString(GlyphGroup glyphIds, PostScriptTable post) {
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
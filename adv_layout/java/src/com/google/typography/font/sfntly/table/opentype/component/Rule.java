package com.google.typography.font.sfntly.table.opentype.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        if (!l.contains(-1)) {
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

  public static GlyphGroup closure(Map<Integer, List<Rule>> lookupRules, GlyphGroup glyphs) {
    int prevSize = 0;
    while (glyphs.size() > prevSize) {
      prevSize = glyphs.size();
      for (List<Rule> rules : lookupRules.values()) {
        for (Rule rule : rules) {
          glyphs = rule.apply(glyphs);
        }
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
}

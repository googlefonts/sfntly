package com.google.typography.font.sfntly.table.opentype.component;

import java.util.ArrayList;
import java.util.List;

public class Rule {
  public final List<Integer> backtrack;
  public final List<Integer> input;
  public final List<Integer> lookAhead;
  public final List<Integer> subst;

  public Rule(
      List<Integer> backtrack, List<Integer> input, List<Integer> lookAhead, List<Integer> subst) {
    this.backtrack = backtrack;
    this.input = input;
    this.lookAhead = lookAhead;
    this.subst = subst;
  }

  public Rule(Rule other, List<Integer> subst) {
    this.backtrack = other.backtrack;
    this.input = other.input;
    this.lookAhead = other.lookAhead;
    this.subst = subst;
  }

  public List<Integer> apply(List<Integer> given, int at) {
    int i = at;
    if (backtrack != null) {
      for (Integer b : backtrack) {
        i--;
        if (i < 0 || i >= given.size() || !given.get(i).equals(b)) {
          return null;
        }
      }
    }

    if (input != null) {
      i = at;
      for (Integer in : input) {
        if (i < 0 || i >= given.size() || !given.get(i).equals(in)) {
          return null;
        }
        i++;
      }
    }

    if (lookAhead != null) {
      i = at + input.size();
      for (Integer l : lookAhead) {
        if (i < 0 || i >= given.size() || !given.get(i).equals(l)) {
          return null;
        }
        i++;
      }
    }

    List<Integer> result = new ArrayList<Integer>();
    result.addAll(given.subList(0, at));
    result.addAll(subst);
    result.addAll(given.subList(at + input.size(), given.size()));
    return result;
  }

  static List<Integer> apply(List<Rule> rules, List<Integer> given, int at) {
    for (Rule rule : rules) {
      List<Integer> result = rule.apply(given, at);
      if (result != null) {
        return result;
      }
    }
    return given;
  }

  static List<Rule> applyOnRuleSubsts(List<Rule> rulesToApply, List<Rule> targetRules, int at) {
    List<Rule> result = new ArrayList<Rule>();
    for (Rule targetRule : targetRules) {
      List<Integer> newSubst = Rule.apply(rulesToApply, targetRule.subst, at);
      Rule newRule = new Rule(targetRule, newSubst);
      result.add(newRule);
    }
    return result;
  }

  static Rule prependToInput(int prefix, Rule other) {
    List<Integer> newInput = new ArrayList<Integer>(other.input.size() + 1);
    newInput.add(prefix);
    newInput.addAll(other.input);

    return new Rule(other.backtrack, newInput, other.lookAhead, other.subst);
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
    for (Integer glyphId : glyphIds) {
      List<Integer> input = new ArrayList<Integer>();
      input.add(glyphId);
      List<Integer> subst = new ArrayList<Integer>();
      subst.add(glyphId + delta);
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
      List<Integer> input = new ArrayList<Integer>();
      input.add(inputs.get(i));
      List<Integer> subst = new ArrayList<Integer>();
      subst.add(substs.get(i));
      result.add(new Rule(null, input, null, subst));
    }
    return result;
  }

  static List<List<Integer>> permuteToRows(List<List<Integer>> lists) {
    List<List<Integer>> result = new ArrayList<List<Integer>>();
    result.add(new ArrayList<Integer>());

    for (List<Integer> list : lists) {
      List<List<Integer>> newResult = new ArrayList<List<Integer>>();
      for (Integer glyphId : list) {
        for (List<Integer> row : result) {
          List<Integer> newRow = new ArrayList<Integer>(row);
          newRow.add(glyphId);
          newResult.add(newRow);
        }
      }
      result = newResult;
    }
    return result;
  }

  static List<Rule> permuteContext(
      List<List<Integer>> backtracks, List<List<Integer>> inputs, List<List<Integer>> lookAheads) {
    List<Rule> result = new ArrayList<Rule>();
    backtracks = addNullIfEmpty(backtracks);
    lookAheads = addNullIfEmpty(lookAheads);
    for (List<Integer> backtrack : backtracks) {
      for (List<Integer> input : inputs) {
        for (List<Integer> lookAhead : lookAheads) {
          result.add(new Rule(backtrack, input, lookAhead, input));
        }
      }
    }
    return result;
  }

  private static List<List<Integer>> addNullIfEmpty(List<List<Integer>> rows) {
    if (rows.size() == 0) {
      rows = new ArrayList<List<Integer>>();
      rows.add(null);
    }
    return rows;
  }
}

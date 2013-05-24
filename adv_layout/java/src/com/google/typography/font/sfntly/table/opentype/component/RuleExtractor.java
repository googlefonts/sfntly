package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.table.opentype.AlternateSubst;
import com.google.typography.font.sfntly.table.opentype.ChainContextSubst;
import com.google.typography.font.sfntly.table.opentype.ClassDefTable;
import com.google.typography.font.sfntly.table.opentype.ContextSubst;
import com.google.typography.font.sfntly.table.opentype.CoverageTable;
import com.google.typography.font.sfntly.table.opentype.ExtensionSubst;
import com.google.typography.font.sfntly.table.opentype.LigatureSubst;
import com.google.typography.font.sfntly.table.opentype.LookupListTable;
import com.google.typography.font.sfntly.table.opentype.LookupTable;
import com.google.typography.font.sfntly.table.opentype.SingleSubst;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.ChainSubClassRule;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.ChainSubClassSet;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.ChainSubClassSetArray;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.ChainSubRuleSetArray;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.CoverageArray;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.InnerArraysFmt3;
import com.google.typography.font.sfntly.table.opentype.classdef.InnerArrayFmt1;
import com.google.typography.font.sfntly.table.opentype.ligaturesubst.Ligature;
import com.google.typography.font.sfntly.table.opentype.ligaturesubst.LigatureSet;
import com.google.typography.font.sfntly.table.opentype.singlesubst.HeaderFmt1;
import com.google.typography.font.sfntly.table.opentype.singlesubst.InnerArrayFmt2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleExtractor {

  public static List<Rule> extract(LigatureSubst table) {
    List<Rule> allRules = new ArrayList<Rule>();
    List<Integer> prefixChars = extract(table.coverage());

    for (int i = 0; i < table.subTableCount(); i++) {
      List<Rule> subRules = extract(table.subTableAt(i));
      subRules = Rule.prependToInput(prefixChars.get(i), subRules);
      allRules.addAll(subRules);
    }
    return allRules;
  }

  public static GlyphList extract(CoverageTable table) {
    switch (table.format) {
    case 1:
      return extract(table.fmt1Table());
    case 2:
      GlyphList result = new GlyphList();
      for (GlyphGroup glyphIds : extract(table.fmt2Table()).values()) {
        result.addAll(glyphIds);
      }
      return result;
    default:
      throw new IllegalArgumentException("unimplemented format " + table.format);
    }
  }

  public static GlyphList extract(RecordsTable<NumRecord> table) {
    GlyphList result = new GlyphList();
    for (NumRecord record : table.recordList) {
      result.add(record.value);
    }
    return result;
  }

  public static Map<Integer, GlyphGroup> extract(RangeRecordTable table) {
    Map<Integer, GlyphGroup> result = new HashMap<Integer, GlyphGroup>();
    for (RangeRecord record : table.recordList) {
      if (!result.containsKey(record.property)) {
        result.put(record.property, new GlyphGroup());
      }
      GlyphGroup existingGlyphs = result.get(record.property);
      existingGlyphs.addAll(extract(record));
    }
    return result;
  }

  public static GlyphGroup extract(RangeRecord record) {
    int len = record.end - record.start + 1;
    GlyphGroup result = new GlyphGroup();
    for (int i = record.start; i <= record.end; i++) {
      result.add(i);
    }
    return result;
  }

  public static List<Rule> extract(LigatureSet table) {
    List<Rule> allRules = new ArrayList<Rule>();

    for (int i = 0; i < table.subTableCount(); i++) {
      Rule subRule = extract(table.subTableAt(i));
      allRules.add(subRule);
    }
    return allRules;
  }

  public static Rule extract(Ligature table) {

    int glyphId = table.getField(Ligature.LIG_GLYPH_INDEX);
    RuleSegment subst = new RuleSegment(glyphId);
    GlyphList input = new GlyphList();
    for (NumRecord record : table.recordList) {
      input.add(record.value);
    }
    return new Rule(null, input, null, subst);
  }

  public static List<Rule> extract(SingleSubst table) {
    switch (table.format) {
    case 1:
      return extract(table.fmt1Table());
    case 2:
      return extract(table.fmt2Table());
    default:
      throw new IllegalArgumentException("unimplemented format " + table.format);
    }
  }

  private static List<Rule> extract(HeaderFmt1 fmt1Table) {
    List<Integer> coverage = extract(fmt1Table.coverage);
    int delta = fmt1Table.getDelta();
    return Rule.deltaRules(coverage, delta);
  }

  private static List<Rule> extract(InnerArrayFmt2 fmt2Table) {
    List<Integer> coverage = extract(fmt2Table.coverage);
    List<Integer> substs = extract((RecordsTable<NumRecord>) fmt2Table);
    return Rule.oneToOneRules(coverage, substs);
  }

  public static List<Rule> extract(AlternateSubst table) {
    List<Rule> result = new ArrayList<Rule>();

    GlyphList coverage = extract(table.coverage());
    int i = 0;
    for (NumRecordTable glyphIds : table) {
      GlyphList input = new GlyphList(coverage.get(i));

      GlyphList glyphList = extract(glyphIds);
      GlyphGroup glyphGroup = new GlyphGroup(glyphList);
      RuleSegment subst = new RuleSegment(glyphGroup);

      Rule rule = new Rule(null, input, null, subst);
      result.add(rule);
      i++;
    }
    return result;
  }

  public static List<Rule> extract(ContextSubst table) {
    throw new IllegalArgumentException("unimplemented extractor for ContextSubst");
  }

  public static List<Rule> extract(ChainContextSubst table, List<List<Rule>> allLookupRules) {
    switch (table.format) {
    case 1:
      return extract(table.fmt1Table());
    case 2:
      return extract(table.fmt2Table(), allLookupRules);
    case 3:
      return extract(table.fmt3Table(), allLookupRules);
    default:
      throw new IllegalArgumentException("unimplemented format " + table.format);
    }
  }

  public static List<Rule> extract(ChainSubRuleSetArray table) {
    throw new IllegalArgumentException("unimplemented extractor for ChainSubRuleSetArray");
  }

  public static List<Rule> extract(ChainSubClassSetArray table, List<List<Rule>> allLookupRules) {
    Map<Integer, GlyphGroup> backtrackClassDef = extract(table.backtrackClassDef);
    Map<Integer, GlyphGroup> inputClassDef = extract(table.inputClassDef);
    Map<Integer, GlyphGroup> lookAheadClassDef = extract(table.lookAheadClassDef);

    List<Rule> result = new ArrayList<Rule>();
    int i = 0;
    for (ChainSubClassSet chainSubRuleSet : table) {

      if (chainSubRuleSet != null) {
        result.addAll(extract(chainSubRuleSet,
            backtrackClassDef,
            i,
            inputClassDef,
            lookAheadClassDef,
            allLookupRules));
      }
      i++;
    }
    return result;
  }

  public static Map<Integer, GlyphGroup> extract(ClassDefTable table) {
    switch (table.format) {
    case 1:
      return extract(table.fmt1Table());
    case 2:
      return extract(table.fmt2Table());
    default:
      throw new IllegalArgumentException("unimplemented format " + table.format);
    }
  }

  public static Map<Integer, GlyphGroup> extract(InnerArrayFmt1 table) {
    Map<Integer, GlyphGroup> result = new HashMap<Integer, GlyphGroup>();
    int glyphId = table.getField(InnerArrayFmt1.START_GLYPH_INDEX);
    for (NumRecord record : table) {
      int classId = record.value;
      if (!result.containsKey(classId)) {
        result.put(classId, new GlyphGroup());
      }

      result.get(classId).add(glyphId);
      glyphId++;
    }
    return result;
  }

  public static List<Rule> extract(ChainSubClassSet table,
      Map<Integer, GlyphGroup> backtrackClassDef,
      int firstInputClass,
      Map<Integer, GlyphGroup> inputClassDef,
      Map<Integer, GlyphGroup> lookAheadClassDef,
      List<List<Rule>> allLookupRules) {
    List<Rule> result = new ArrayList<Rule>();
    for (ChainSubClassRule chainSubRule : table) {
      result.addAll(extract(chainSubRule,
          backtrackClassDef,
          firstInputClass,
          inputClassDef,
          lookAheadClassDef,
          allLookupRules));
    }
    return result;
  }

  public static List<Rule> extract(ChainSubClassRule table,
      Map<Integer, GlyphGroup> backtrackClassDef,
      int firstInputClass,
      Map<Integer, GlyphGroup> inputClassDef,
      Map<Integer, GlyphGroup> lookAheadClassDef,
      List<List<Rule>> allLookupRules) {
    RuleSegment backtrack = extract(table.backtrackGlyphs, backtrackClassDef);
    List<GlyphList> inputRows = extract(firstInputClass, table.inputGlyphs, inputClassDef);
    RuleSegment lookAhead = extract(table.lookAheadGlyphs, lookAheadClassDef);

    List<Rule> rulesSansSubst = Rule.permuteContext(backtrack, inputRows, lookAhead);
    rulesSansSubst = applyChainingLookup(rulesSansSubst, table.lookupRecords, allLookupRules);
    return rulesSansSubst;
  }

  public static List<GlyphList> extract(
      int firstInputClass, GlyphClassList list, Map<Integer, GlyphGroup> classDef) {
    List<GlyphGroup> data = new ArrayList<GlyphGroup>();
    data.add(classDef.get(firstInputClass));
    for (NumRecord record : list) {
      data.add(classDef.get(record.value));
    }
    return Rule.permuteToSegments(data);
  }

  public static RuleSegment extract(GlyphClassList list, Map<Integer, GlyphGroup> classDef) {
    RuleSegment segment = new RuleSegment();
    for (NumRecord record : list) {
      segment.add(classDef.get(record.value));
    }
    return segment;
  }

  public static List<Rule> extract(InnerArraysFmt3 table, List<List<Rule>> allLookupRules) {
    RuleSegment backtrackContext = new RuleSegment();
    backtrackContext.addAll(extract(table.backtrackGlyphs));

    List<GlyphList> inputs = Rule.permuteToSegments(extract(table.inputGlyphs));

    RuleSegment lookAheadContext = new RuleSegment();
    lookAheadContext.addAll(extract(table.lookAheadGlyphs));

    List<Rule> rulesWithBaseSubst = Rule.permuteContext(backtrackContext, inputs, lookAheadContext);
    List<Rule> result = applyChainingLookup(
        rulesWithBaseSubst, table.lookupRecords, allLookupRules);
    return result;
  }

  private static List<Rule> applyChainingLookup(
      List<Rule> rulesSansSubst, SubstLookupRecordList lookups, List<List<Rule>> allLookupRules) {
    for (SubstLookupRecord lookup : lookups) {
      int at = lookup.sequenceIndex;
      int lookupIndex = lookup.lookupListIndex;
      List<Rule> rulesToApply = allLookupRules.get(lookupIndex);
      rulesSansSubst = Rule.applyOnRuleSubsts(rulesToApply, rulesSansSubst, at);
    }

    List<Rule> result = new ArrayList<Rule>();
    for (Rule rule : rulesSansSubst) {
      if (!rule.subst.match(rule.input)) {
        result.add(rule);
      }
    }

    return result;
  }

  public static List<List<Rule>> extract(LookupListTable table) {
    List<List<Rule>> allRules = new ArrayList<List<Rule>>(table.subTableCount());
    for (int i = 0; i < table.subTableCount(); i++) {
      allRules.add(null);
    }
    for (int i = 0; i < table.subTableCount(); i++) {
      LookupTable subTable = table.subTableAt(i);
      switch (subTable.lookupType()) {
      case GSUB_LIGATURE:
      case GSUB_SINGLE:
      case GSUB_ALTERNATE:
        allRules.set(i, extract(subTable));
        break;
      case GSUB_MULTIPLE:
        throw new IllegalArgumentException("LookupType is " + subTable.lookupType());
      }
    }
    for (int i = 0; i < table.subTableCount(); i++) {
      LookupTable subTable = table.subTableAt(i);
      switch (subTable.lookupType()) {
      case GSUB_CHAINING_CONTEXTUAL:
        allRules.set(i, extract(subTable, allRules));
        break;
      case GSUB_CONTEXTUAL:
      case GSUB_EXTENSION:
      case GSUB_REVERSE_CHAINING_CONTEXTUAL_SINGLE:
        throw new IllegalArgumentException("LookupType is " + subTable.lookupType());
      }
    }
    return allRules;
  }

  public static List<Rule> extract(LookupTable table) {
    List<Rule> allRules = new ArrayList<Rule>();

    for (int i = 0; i < table.subTableCount(); i++) {
      switch (table.lookupType()) {
      case GSUB_LIGATURE:
        allRules.addAll(extract((LigatureSubst) table.subTableAt(i)));
        break;
      case GSUB_SINGLE:
        allRules.addAll(extract((SingleSubst) table.subTableAt(i)));
        break;
      case GSUB_ALTERNATE:
        allRules.addAll(extract((AlternateSubst) table.subTableAt(i)));
        break;
      default:
        throw new IllegalArgumentException("LookupType is " + table.lookupType());
      }
    }
    return allRules;
  }

  public static List<Rule> extract(LookupTable table, List<List<Rule>> allLookupRules) {
    List<Rule> allRules = new ArrayList<Rule>();

    for (int i = 0; i < table.subTableCount(); i++) {
      switch (table.lookupType()) {
      case GSUB_CHAINING_CONTEXTUAL:
        allRules.addAll(extract((ChainContextSubst) table.subTableAt(i), allLookupRules));
        break;
      default:
        throw new IllegalArgumentException("LookupType is " + table.lookupType());
      }
    }
    return allRules;
  }

  public static List<GlyphGroup> extract(CoverageArray table) {
    List<GlyphGroup> result = new ArrayList<GlyphGroup>();
    for (CoverageTable coverage : table) {
      GlyphGroup glyphGroup = new GlyphGroup();
      glyphGroup.addAll(extract(coverage));
      result.add(glyphGroup);
    }
    return result;
  }

  public static List<Rule> extract(ExtensionSubst table) {
    throw new IllegalArgumentException("unimplemented extractor for ExtensionSubst");
  }
}

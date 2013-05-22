package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.table.opentype.ChainContextSubst;
import com.google.typography.font.sfntly.table.opentype.ContextSubst;
import com.google.typography.font.sfntly.table.opentype.CoverageTable;
import com.google.typography.font.sfntly.table.opentype.ExtensionSubst;
import com.google.typography.font.sfntly.table.opentype.LigatureSubst;
import com.google.typography.font.sfntly.table.opentype.LookupListTable;
import com.google.typography.font.sfntly.table.opentype.LookupTableNew;
import com.google.typography.font.sfntly.table.opentype.MultipleSubst;
import com.google.typography.font.sfntly.table.opentype.SingleSubst;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.ChainSubRuleSetArray;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.CoverageArray;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.InnerArraysFmt3;
import com.google.typography.font.sfntly.table.opentype.ligaturesubst.Ligature;
import com.google.typography.font.sfntly.table.opentype.ligaturesubst.LigatureSet;
import com.google.typography.font.sfntly.table.opentype.singlesubst.HeaderFmt1;
import com.google.typography.font.sfntly.table.opentype.singlesubst.InnerArrayFmt2;

import java.util.ArrayList;
import java.util.List;

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

  public static List<Integer> extract(CoverageTable table) {
    switch (table.format) {
    case 1:
      return extract(table.fmt1Table());
    case 2:
      return extract(table.fmt2Table());
    default:
      throw new IllegalArgumentException("unimplemented format " + table.format);
    }
  }

  public static List<Integer> extract(RecordsTable<NumRecord> table) {
    List<Integer> result = new ArrayList<Integer>(table.recordList.count());
    for (NumRecord record : table.recordList) {
      result.add(record.value);
    }
    return result;
  }

  public static List<Integer> extract(RangeRecordTable table) {
    List<Integer> result = new ArrayList<Integer>();
    for (RangeRecord record : table.recordList) {
      result.addAll(extract(record));
    }
    return result;
  }

  public static List<Integer> extract(RangeRecord record) {
    int len = record.end - record.start + 1;
    List<Integer> result = new ArrayList<Integer>(len);
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
    List<Integer> subst = new ArrayList<Integer>(1);
    subst.add(glyphId);

    List<Integer> input = new ArrayList<Integer>(table.recordList.count());
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

  public static List<Rule> extract(MultipleSubst table) {
    throw new IllegalArgumentException("unimplemented extractor for MultipleSubst");
  }

  public static List<Rule> extract(ContextSubst table) {
    throw new IllegalArgumentException("unimplemented extractor for ContextSubst");
  }

  public static List<Rule> extract(ChainContextSubst table, List<List<Rule>> allLookupRules) {
    switch (table.format) {
    case 1:
      return extract(table.fmt1Table());
    case 3:
      return extract(table.fmt3Table(), allLookupRules);
    default:
      throw new IllegalArgumentException("unimplemented format " + table.format);
    }
  }

  public static List<Rule> extract(ChainSubRuleSetArray table) {
    throw new IllegalArgumentException("unimplemented extractor for ChainSubRuleSetArray");
  }

  public static List<Rule> extract(InnerArraysFmt3 table, List<List<Rule>> allLookupRules) {
    List<Rule> rulesSansSubst = Rule.permuteContext(
        extract(table.backtrackGlyphs), extract(table.inputGlyphs), extract(table.lookAheadGlyphs));
    for (SubstLookupRecord lookup : table.lookupRecords) {
      int at = lookup.sequenceIndex;
      int lookupIndex = lookup.lookupListIndex;
      List<Rule> rulesToApply = allLookupRules.get(lookupIndex);
      rulesSansSubst = Rule.applyOnRuleSubsts(rulesToApply, rulesSansSubst, at);
    }
    return rulesSansSubst;
  }

  public static List<List<Rule>> extract(LookupListTable table) {
    List<List<Rule>> allRules = new ArrayList<List<Rule>>(table.subTableCount());
    for (int i = 0; i < table.subTableCount(); i++) {
      allRules.add(null);
    }
    for (int i = 0; i < table.subTableCount(); i++) {
      LookupTableNew subTable = table.subTableAt(i);
      switch (subTable.lookupType()) {
      case GSUB_LIGATURE:
        allRules.set(i, extract(subTable));
        break;
      case GSUB_SINGLE:
        allRules.set(i, extract(subTable));
        break;
      case GSUB_MULTIPLE:
      case GSUB_ALTERNATE:
      }
    }
    for (int i = 0; i < table.subTableCount(); i++) {
      LookupTableNew subTable = table.subTableAt(i);
      switch (subTable.lookupType()) {
      case GSUB_CHAINING_CONTEXTUAL:
        allRules.set(i, extract(subTable, allRules));
        break;
      case GSUB_CONTEXTUAL:
      case GSUB_EXTENSION:
      }
    }
    return allRules;
  }

  public static List<Rule> extract(LookupTableNew table) {
    List<Rule> allRules = new ArrayList<Rule>();

    for (int i = 0; i < table.subTableCount(); i++) {
      switch (table.lookupType()) {
      case GSUB_LIGATURE:
        allRules.addAll(extract((LigatureSubst) table.subTableAt(i)));
        break;
      case GSUB_SINGLE:
        allRules.addAll(extract((SingleSubst) table.subTableAt(i)));
        break;
      case GSUB_MULTIPLE:
      case GSUB_ALTERNATE:
      default:
        throw new IllegalArgumentException("LookupType is " + table.lookupType());
      }
    }
    return allRules;
  }

  public static List<Rule> extract(LookupTableNew table, List<List<Rule>> allLookupRules) {
    List<Rule> allRules = new ArrayList<Rule>();

    for (int i = 0; i < table.subTableCount(); i++) {
      switch (table.lookupType()) {
      case GSUB_CHAINING_CONTEXTUAL:
        allRules.addAll(extract((ChainContextSubst) table.subTableAt(i), allLookupRules));
        break;
      case GSUB_CONTEXTUAL:
      case GSUB_EXTENSION:
      default:
        throw new IllegalArgumentException("LookupType is " + table.lookupType());
      }
    }
    return allRules;
  }

  public static List<List<Integer>> extract(CoverageArray table) {
    List<List<Integer>> data = new ArrayList<List<Integer>>();
    for (CoverageTable coverage : table) {
      data.add(extract(coverage));
    }
    return Rule.permuteToRows(data);
  }

  public static List<Rule> extract(ExtensionSubst table) {
    throw new IllegalArgumentException("unimplemented extractor for ExtensionSubst");
  }
}

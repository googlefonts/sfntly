package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.NumRecordList;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;
import com.google.typography.font.sfntly.table.opentype.contextsubst.SubClassSetArray;
import com.google.typography.font.sfntly.table.opentype.contextsubst.SubRuleSet;
import com.google.typography.font.sfntly.table.opentype.contextsubst.SubRuleSetArray;

import java.util.Iterator;

public class ContextSubst extends SubstSubtable implements Iterable<SubRuleSet> {
  private final SubRuleSetArray ruleSets;
  private SubClassSetArray classSets;

  // //////////////
  // Constructors

  public ContextSubst(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    switch (format) {
    case 1:
      ruleSets = new SubRuleSetArray(data, headerSize(), dataIsCanonical);
      classSets = null;
      break;
    case 2:
      ruleSets = null;
      classSets = new SubClassSetArray(data, headerSize(), dataIsCanonical);
      break;
    default:
      throw new IllegalStateException("Subt format value is " + format + " (should be 1 or 2).");
    }
  }

  // //////////////////////////////////
  // Methods redirected to the array

  public NumRecordList recordList() {
    return (format == 1) ? ruleSets.recordList : classSets.recordList;
  }

  public SubRuleSet subTableAt(int index) {
    return (format == 1) ? ruleSets.subTableAt(index) : classSets.subTableAt(index);
  }

  @Override
  public Iterator<SubRuleSet> iterator() {
    return (format == 1) ? ruleSets.iterator() : classSets.iterator();
  }

  protected SubRuleSet createSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return (format == 1) ? ruleSets.readSubTable(data, dataIsCanonical)
        : classSets.readSubTable(data, dataIsCanonical);
  }

  // //////////////////////////////////
  // Methods specific to this class

  public CoverageTableNew coverage() {
    return (format == 1) ? ruleSets.coverage : classSets.coverage;
  }

  public ClassDefTableNew classDef() {
    return (format == 2) ? classSets.classDef : null;
  }

  // //////////////////////////////////
  // Builder

  public static class Builder extends SubstSubtable.Builder<SubstSubtable> {

    private final SubRuleSetArray.Builder arrayBuilder;

    // //////////////
    // Constructors

    public Builder() {
      super();
      arrayBuilder = new SubRuleSetArray.Builder();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
      arrayBuilder = new SubRuleSetArray.Builder(data, dataIsCanonical);
    }

    public Builder(SubstSubtable subTable) {
      ContextSubst ligSubst = (ContextSubst) subTable;
      arrayBuilder = new SubRuleSetArray.Builder(ligSubst.ruleSets);
    }

    // /////////////////////////////
    // Public methods for builders

    public int subTableCount() {
      return arrayBuilder.subTableCount();
    }

    public SubTable.Builder<? extends SubTable> builderForTag(int tag) {
      setModelChanged();
      return arrayBuilder.builderForTag(tag);
    }

    public VisibleBuilder<SubRuleSet> addBuilder() {
      setModelChanged();
      return arrayBuilder.addBuilder();
    }

    public void removeBuilderForTag(int tag) {
      setModelChanged();
      arrayBuilder.removeBuilderForTag(tag);
    }

    // ///////////////////////////////
    // Public methods to serialize

    @Override
    public int subDataSizeToSerialize() {
      return arrayBuilder.subDataSizeToSerialize();
    }

    @Override
    public int subSerialize(WritableFontData newData) {
      return arrayBuilder.subSerialize(newData);
    }

    // /////////////////////////////////
    // must implement abstract methods

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    public void subDataSet() {
      arrayBuilder.subDataSet();
    }

    @Override
    public ContextSubst subBuildTable(ReadableFontData data) {
      return new ContextSubst(data, 0, true);
    }
  }
}

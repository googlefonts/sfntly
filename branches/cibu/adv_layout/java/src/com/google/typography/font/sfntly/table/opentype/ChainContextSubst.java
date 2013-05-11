package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.ChainSubClassSetArray;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.ChainSubRuleSet;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.ChainSubRuleSetArray;
import com.google.typography.font.sfntly.table.opentype.component.NumRecordList;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

import java.util.Iterator;

public class ChainContextSubst extends SubstSubtable implements Iterable<ChainSubRuleSet> {
  private final ChainSubRuleSetArray ruleSets;
  private final ChainSubClassSetArray classSets;

  // //////////////
  // Constructors

  public ChainContextSubst(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    switch (format) {
    case 1:
      ruleSets = new ChainSubRuleSetArray(data, headerSize(), dataIsCanonical);
      classSets = null;
      break;
    case 2:
      ruleSets = null;
      classSets = new ChainSubClassSetArray(data, headerSize(), dataIsCanonical);
      dumpData();
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

  public ChainSubRuleSet subTableAt(int index) {
    return (format == 1) ? ruleSets.subTableAt(index) : classSets.subTableAt(index);
  }

  @Override
  public Iterator<ChainSubRuleSet> iterator() {
    return (format == 1) ? ruleSets.iterator() : classSets.iterator();
  }

  protected ChainSubRuleSet createSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return (format == 1) ? ruleSets.readSubTable(data, dataIsCanonical)
        : classSets.readSubTable(data, dataIsCanonical);
  }

  // //////////////////////////////////
  // Methods specific to this class

  public CoverageTableNew coverage() {
    return (format == 1) ? ruleSets.coverage : classSets.coverage;
  }

  public ClassDefTableNew backtrackClassDef() {
    return (format == 2) ? classSets.backtrackClassDef : null;
  }

  public ClassDefTableNew inputClassDef() {
    return (format == 2) ? classSets.inputClassDef : null;
  }

  public ClassDefTableNew lookAheadClassDef() {
    return (format == 2) ? classSets.lookAheadClassDef : null;
  }

  // //////////////////////////////////
  // Builder

  public static class Builder extends SubstSubtable.Builder<SubstSubtable> {

    private final ChainSubRuleSetArray.Builder arrayBuilder;

    // //////////////
    // Constructors

    public Builder() {
      super();
      arrayBuilder = new ChainSubRuleSetArray.Builder();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
      arrayBuilder = new ChainSubRuleSetArray.Builder(data, dataIsCanonical);
    }

    public Builder(SubstSubtable subTable) {
      ChainContextSubst ligSubst = (ChainContextSubst) subTable;
      arrayBuilder = new ChainSubRuleSetArray.Builder(ligSubst.ruleSets);
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

    public VisibleBuilder<ChainSubRuleSet> addBuilder() {
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
    public ChainContextSubst subBuildTable(ReadableFontData data) {
      return new ChainContextSubst(data, 0, true);
    }
  }
}

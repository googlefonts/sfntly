package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.chaincontextsubst.ChainSubRuleSetArray;
import com.google.typography.font.sfntly.table.opentype.component.NumRecordList;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

import java.util.Iterator;

public class ChainContextSubst extends SubstSubtable implements Iterable<NullTable> {
  private final ChainSubRuleSetArray ruleSets;
  private final NullTable classSets;

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
      System.out.println(this.getClass().getSimpleName() + " format " + format);
      classSets = new NullTable(data, headerSize(), dataIsCanonical);
      break;
    default:
      throw new IllegalStateException("Subt format value is " + format + " (should be 1 or 2).");
    }
  }

  // //////////////////////////////////
  // Methods redirected to the array

  public NumRecordList recordList() {
    return (format == 1) ? ruleSets.recordList : null;
  }

  public NullTable subTableAt(int index) {
    return (format == 1) ? ruleSets.subTableAt(index) : null;
  }

  @Override
  public Iterator<NullTable> iterator() {
    return (format == 1) ? ruleSets.iterator() : null;
  }

  protected NullTable createSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return (format == 1) ? ruleSets.readSubTable(data, dataIsCanonical) : null;
  }

  // //////////////////////////////////
  // Methods specific to this class

  public CoverageTableNew coverage() {
    return (format == 1) ? ruleSets.coverage : null;
  }

  public ClassDefTableNew classDef() {
    return (format == 2) ? null : null;
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

    public VisibleBuilder<NullTable> addBuilder() {
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

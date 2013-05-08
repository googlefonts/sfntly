package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.NumRecordList;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;
import com.google.typography.font.sfntly.table.opentype.contextsubst.SubRuleSet;
import com.google.typography.font.sfntly.table.opentype.contextsubst.SubRuleSetArray;

import java.util.Iterator;

public class ContextSubst extends SubstSubtable implements Iterable<SubRuleSet> {
  private final SubRuleSetArray array;
  private NullTable fmt2;

  // //////////////
  // Constructors

  public ContextSubst(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    switch (format) {
    case 1:
      array = new SubRuleSetArray(data, headerSize(), dataIsCanonical);
      fmt2 = null;
      break;
    case 2:
      array = null;
      fmt2 = new NullTable(data, headerSize(), dataIsCanonical);
      break;
    default:
      throw new IllegalStateException("Subt format value is " + format + " (should be 1 or 2).");
    }
  }

  // //////////////////////////////////
  // Methods redirected to the array

  public NumRecordList recordList() {
    return array.recordList;
  }

  public SubRuleSet subTableAt(int index) {
    return array.subTableAt(index);
  }

  @Override
  public Iterator<SubRuleSet> iterator() {
    return array.iterator();
  }

  protected SubRuleSet createSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return array.readSubTable(data, dataIsCanonical);
  }

  // //////////////////////////////////
  // Methods specific to this class

  public CoverageTableNew coverage() {
    return array.coverage;
  }

  // //////////////////////////////////
  // Builder

  public static class Builder extends SubstSubtable.Builder<SubstSubtable, SubRuleSet> {

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
      arrayBuilder = new SubRuleSetArray.Builder(ligSubst.array);
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

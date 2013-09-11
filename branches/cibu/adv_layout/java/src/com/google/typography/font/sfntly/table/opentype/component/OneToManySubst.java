package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.CoverageTable;
import com.google.typography.font.sfntly.table.opentype.SubstSubtable;
import com.google.typography.font.sfntly.table.opentype.multiplesubst.GlyphIds;

import java.util.Iterator;

public class OneToManySubst extends SubstSubtable implements Iterable<NumRecordTable> {
  private final GlyphIds array;

  // //////////////
  // Constructors

  public OneToManySubst(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    if (format != 1) {
      throw new IllegalStateException("Subt format value is " + format + " (should be 1).");
    }
    array = new GlyphIds(data, headerSize(), dataIsCanonical);
  }

  // //////////////////////////////////
  // Methods redirected to the array

  public NumRecordList recordList() {
    return array.recordList;
  }

  public NumRecordTable subTableAt(int index) {
    return array.subTableAt(index);
  }

  @Override
  public Iterator<NumRecordTable> iterator() {
    return array.iterator();
  }

  protected NumRecordTable createSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return array.readSubTable(data, dataIsCanonical);
  }

  // //////////////////////////////////
  // Methods specific to this class

  public CoverageTable coverage() {
    return array.coverage;
  }

  // //////////////////////////////////
  // Builder

  public static class Builder extends SubstSubtable.Builder<SubstSubtable> {

    private final GlyphIds.Builder arrayBuilder;

    // //////////////
    // Constructors

    public Builder() {
      super();
      arrayBuilder = new GlyphIds.Builder();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
      arrayBuilder = new GlyphIds.Builder(data, dataIsCanonical);
    }

    public Builder(SubstSubtable subTable) {
      OneToManySubst multiSubst = (OneToManySubst) subTable;
      arrayBuilder = new GlyphIds.Builder(multiSubst.array);
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

    public VisibleSubTable.Builder<NumRecordTable> addBuilder() {
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
    public OneToManySubst subBuildTable(ReadableFontData data) {
      return new OneToManySubst(data, 0, true);
    }
  }
}

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.NumRecordList;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;
import com.google.typography.font.sfntly.table.opentype.ligaturesubst.InnerArray;

import java.util.Iterator;


public class LigatureSubst extends SubstSubtable implements Iterable<NullTable>  {
  private final InnerArray array;
  protected final CoverageTableNew coverage;

  ////////////////
  // Constructors

  public LigatureSubst(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
//    System.out.println("\n\nLigatureSubst Header");
//    for (int i = 0; i < 20; i++) {
//      System.out.printf("0x%04X %d\n", data.readUShort(i*2), data.readUShort(i*2));
//    }
    array = new InnerArray(data, dataIsCanonical);
    coverage = new CoverageTableNew(data.slice(array.getField(InnerArray.COVERAGE_INDEX)), dataIsCanonical);
  }

  ////////////////////////////////////
  // Methods redirected to the array

  public NumRecordList recordList() {
    return array.recordList;
  }

  public NullTable subTableAt(int index) {
    return array.subTableAt(index);
  }

  @Override
  public Iterator<NullTable> iterator() {
    return array.iterator();
  }

  protected NullTable createSubTable(
      ReadableFontData data, boolean dataIsCanonical) {
    return array.readSubTable(data, dataIsCanonical);
  }

  ////////////////////////////////////
  // Methods specific to this class

  public CoverageTableNew coverage() {
    return coverage;
  }
 
  ////////////////////////////////////
  // Builder

  public static class Builder extends SubstSubtable.Builder<SubstSubtable, NullTable> {

    private final InnerArray.Builder arrayBuilder;

    ////////////////
    // Constructors

    public Builder() {
      super();
      arrayBuilder = new InnerArray.Builder();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
      this.dataIsCanonical = dataIsCanonical;
      arrayBuilder = new InnerArray.Builder(data, dataIsCanonical);
    }

    public Builder(SubstSubtable subTable) {
      LigatureSubst ligSubst = (LigatureSubst) subTable;
      this.dataIsCanonical = subTable.dataIsCanonical;
      arrayBuilder = new InnerArray.Builder(ligSubst.array);
    }

    ///////////////////////////////
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

    /////////////////////////////////
    // Public methods to serialize

    @Override
    public int subDataSizeToSerialize() {
      return arrayBuilder.subDataSizeToSerialize();
    }

    @Override
    public int subSerialize(WritableFontData newData) {
      return arrayBuilder.subSerialize(newData);
    }

    ///////////////////////////////////
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
    public LigatureSubst subBuildTable(ReadableFontData data) {
      return new LigatureSubst(data, true);
    } 
  }
}

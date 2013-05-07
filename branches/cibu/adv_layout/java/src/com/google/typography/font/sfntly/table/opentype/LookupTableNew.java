package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.GsubLookupType;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class LookupTableNew extends OffsetRecordTable<SubstSubtable> {
  public static final int FIELD_COUNT = 2;
  
  public static final int LOOKUP_TYPE_INDEX = 0;
  public static final int LOOKUP_TYPE_DEFAULT = 0;
  
  public static final int LOOKUP_FLAG_INDEX = 1;
  public static final int LOOKUP_FLAG_DEFAULT = 0;

  public LookupTableNew(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  public LookupTableNew(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }

  @Override
  public SubstSubtable readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    int lookupType = getField(LOOKUP_TYPE_INDEX);
    switch(GsubLookupType.forTypeNum(lookupType)) {
    case GSUB_LIGATURE:
      return new LigatureSubst(data, dataIsCanonical);
    case GSUB_SINGLE:
    case GSUB_MULTIPLE:
    case GSUB_CONTEXTUAL:
    case GSUB_CHAINING_CONTEXTUAL:
      return new NullTable(data, dataIsCanonical);
    default:
      throw new IllegalArgumentException("LookupType is " + lookupType);
    }
  }
  
  @Override
  public int fieldCount() {
    return FIELD_COUNT;
  }

  public static class Builder extends OffsetRecordTable.Builder<LookupTableNew, SubstSubtable> {
    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      this(data, 0, dataIsCanonical);
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data, base, dataIsCanonical);
    }

    public Builder(LookupTableNew table) {
      super(table);
    }

    @Override
    protected LookupTableNew readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new LookupTableNew(data, base, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<SubstSubtable> createSubTableBuilder() {
      return new LigatureSubst.Builder();
    }

    @Override
    protected VisibleBuilder<SubstSubtable> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new LigatureSubst.Builder(data, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<SubstSubtable> createSubTableBuilder(SubstSubtable subTable) {
      return new LigatureSubst.Builder(subTable);
    }

    @Override
    public int fieldCount() {
      return FIELD_COUNT;
    }

    @Override
    protected void initFields() {
      setField(LOOKUP_TYPE_INDEX, LOOKUP_TYPE_DEFAULT);
      setField(LOOKUP_FLAG_INDEX, LOOKUP_FLAG_INDEX);
    }
  }
}



//public class LookupTableNew extends SubTable implements Iterable<SubstSubtable>  {
//  public final Header header;
//  private final InnerArray array;
//  public final boolean dataIsCanonical;
//
//  ////////////////
//  // Constructors
//
//  public LookupTableNew(ReadableFontData data, boolean dataIsCanonical) {
//    super(data);
//    System.out.println("\n\nLookupTableNew Header");
//    for (int i = 0; i < 20; i++) {
//      System.out.printf("0x%04X %d\n", data.readUShort(i*2), data.readUShort(i*2));
//    }
//
//    this.dataIsCanonical = dataIsCanonical;
//    header = new Header(data);
//    array = new InnerArray(data, Header.RECORD_SIZE, dataIsCanonical, header.lookupType);
//  }
//
//  ////////////////////////////////////
//  // Methods redirected to the array
//
//  public NumRecordList recordList() {
//    return array.recordList;
//  }
//
//  public SubstSubtable subTableAt(int index) {
//    return array.subTableAt(index);
//  }
//
//  @Override
//  public Iterator<SubstSubtable> iterator() {
//    return array.iterator();
//  }
//
//  protected SubstSubtable createSubTable(
//      ReadableFontData data, boolean dataIsCanonical) {
//    return array.readSubTable(data, dataIsCanonical);
//  }
//
//  ////////////////////////////////////
//  // Methods specific to this class
//
//
//  ////////////////////////////////////
//  // Builder
//
//  public static class Builder extends VisibleBuilder<LookupTableNew> {
//
//    protected boolean dataIsCanonical;
//    private final Header.Builder headerBuilder;
//    private final InnerArray.Builder arrayBuilder;
//
//    ////////////////
//    // Constructors
//
//    public Builder() {
//      super();
//      headerBuilder = new Header.Builder();
//      arrayBuilder = new InnerArray.Builder();
//    }
//
//    public Builder(ReadableFontData data, boolean dataIsCanonical) {
//      super(data);
//      this.dataIsCanonical = dataIsCanonical;
//      headerBuilder = new Header.Builder(data, dataIsCanonical);
//      arrayBuilder = new InnerArray.Builder(data, Header.RECORD_SIZE, dataIsCanonical, headerBuilder.builder.lookupType);
//    }
//
//    public Builder(LookupTableNew table) {
//      this.dataIsCanonical = table.dataIsCanonical;
//      headerBuilder = new Header.Builder(table.header);
//      arrayBuilder = new InnerArray.Builder(table.array);
//    }
//
//    ///////////////////////////////
//    // Public methods for builders
//
//    public int subTableCount() {
//      return arrayBuilder.subTableCount();
//    }
//
//    public SubTable.Builder<? extends SubTable> builderForTag(int tag) {
//      setModelChanged();
//      return arrayBuilder.builderForTag(tag);
//    }
//
//    public VisibleBuilder<SubstSubtable> addBuilder() {
//      setModelChanged();
//      return arrayBuilder.addBuilder();
//    }
//
//    public void removeBuilderForTag(int tag) {
//      setModelChanged();
//      arrayBuilder.removeBuilderForTag(tag);
//    }
//
//    public VisibleBuilder<Header> buiderForHeader() {
//      return headerBuilder;
//    }
//
//    /////////////////////////////////
//    // Public methods to serialize
//
//    @Override
//    public int subDataSizeToSerialize() {
//      return headerBuilder.subDataSizeToSerialize() + arrayBuilder.subDataSizeToSerialize();
//    }
//
//    @Override
//    public int subSerialize(WritableFontData newData) {
//      int newOffset = arrayBuilder.subSerialize(newData.slice(Header.RECORD_SIZE));
//      return headerBuilder.subSerialize(newData, newOffset);
//    }
//
//    ///////////////////////////////////
//    // must implement abstract methods
//
//    @Override
//    protected boolean subReadyToSerialize() {
//      return true;
//    }
//
//    @Override
//    public void subDataSet() {
//      arrayBuilder.subDataSet();
//    }
//
//    @Override
//    public LookupTableNew subBuildTable(ReadableFontData data) {
//      return new LookupTableNew(data, true);
//    } 
//  }
//}

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.RecordList;
import com.google.typography.font.sfntly.table.opentype.component.RecordsTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;
import com.google.typography.font.sfntly.table.opentype.coveragetable.Header;
import com.google.typography.font.sfntly.table.opentype.coveragetable.InnerArrayFmt1;
import com.google.typography.font.sfntly.table.opentype.coveragetable.InnerArrayFmt2;

public class CoverageTableNew extends SubTable {
  public final Header header;
  public final RecordsTable<?> array;
  public boolean dataIsCanonical;

  ////////////////
  // Constructors

  public CoverageTableNew(ReadableFontData data, boolean dataIsCanonical) {
    super(data);
    this.dataIsCanonical = dataIsCanonical;
    header = new Header(data);

    switch (header.format) {
    case 1: 
      array = new InnerArrayFmt1(data.slice(Header.RECORD_SIZE), dataIsCanonical);
      break;
    case 2:
      array = new InnerArrayFmt2(data.slice(Header.RECORD_SIZE), dataIsCanonical);
      break;
    default:
      throw new IllegalArgumentException("coverage format " + header.format + " unexpected");
    }
  }

  //////////////////////////////////////////
  // Utility methods specific to this class
  
  public RecordList<?> records() {
    return array.recordList;
  }

  ////////////////////////////////////
  // Builder

  public static class Builder extends VisibleBuilder<CoverageTableNew> {

    protected boolean dataIsCanonical;
    protected final Header.Builder headerBuilder;
    protected final RecordsTable.Builder<?, ?> arrayBuilder;

    ////////////////
    // Constructors

    public Builder() {
      super();
      headerBuilder = new Header.Builder();
      arrayBuilder = new InnerArrayFmt1.Builder();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
      headerBuilder = new Header.Builder(data, dataIsCanonical);
      switch (headerBuilder.builder.format) {
      case 1: 
        arrayBuilder = new InnerArrayFmt1.Builder(
            data.slice(Header.RECORD_SIZE), dataIsCanonical);
        break;
      case 2:
        arrayBuilder = new InnerArrayFmt1.Builder(
            data.slice(Header.RECORD_SIZE), dataIsCanonical);
        break;
      default:
        throw new IllegalArgumentException("coverage format " + headerBuilder.builder.format + " unexpected");
      }
    }

    public Builder(CoverageTableNew table) {
      this(table.readFontData(), table.dataIsCanonical);
    }
    
    ////////////////////////////////
    // Public methods to update

    public int valueCount() {
      return arrayBuilder.count();
    }

    ////////////////////////////////
    // Public methods to serialize

    @Override
    public int subDataSizeToSerialize() {
      return headerBuilder.subDataSizeToSerialize() + arrayBuilder.subDataSizeToSerialize();
    }

    @Override
    public int subSerialize(WritableFontData newData) {
      int newOffset = headerBuilder.subSerialize(newData);
      return arrayBuilder.subSerialize(newData.slice(newOffset));
    }

    /////////////////////
    // Overriden methods

    @Override
    public CoverageTableNew subBuildTable(ReadableFontData data) {
      return new CoverageTableNew(data, false);
    } 

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    public void subDataSet() {
      headerBuilder.subDataSet();
      arrayBuilder.subDataSet();
    }
  }
}

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.NumRecord;
import com.google.typography.font.sfntly.table.opentype.component.RecordList;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;
import com.google.typography.font.sfntly.table.opentype.langsystable.Header;
import com.google.typography.font.sfntly.table.opentype.langsystable.InnerArray;

public class LangSysTable extends SubTable {
  public final Header header;
  private final InnerArray array;
  private boolean dataIsCanonical;

  ////////////////
  // Constructors

  public LangSysTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data);
    this.dataIsCanonical = dataIsCanonical;
    header = new Header(data);
    array = new InnerArray(data.slice(Header.RECORD_SIZE), dataIsCanonical);
  }

  //////////////////////////////////////////
  // Utility methods specific to this class
  
  public RecordList<NumRecord> records() {
    return array.recordList;
  }

  ////////////////////////////////////
  // Builder

  public static class Builder extends VisibleBuilder<LangSysTable> {

    protected boolean dataIsCanonical;
    protected final Header.Builder headerBuilder;
    protected final InnerArray.Builder arrayBuilder;

    ////////////////
    // Constructors

    public Builder() {
      super();
      headerBuilder = new Header.Builder();
      arrayBuilder = new InnerArray.Builder();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
      headerBuilder = new Header.Builder(data, dataIsCanonical);
      arrayBuilder = new InnerArray.Builder(
          data.slice(Header.RECORD_SIZE), dataIsCanonical);
    }

    public Builder(LangSysTable table) {
      this(table.readFontData(), table.dataIsCanonical);
    }
    
    ////////////////////////////////
    // Public methods to update

    public Builder addFeatureIndices(int... indices) {
      for (int index : indices) {
        NumRecord record = new NumRecord(index);
        if (!arrayBuilder.contains(record)) {
          arrayBuilder.add(new NumRecord(index));
        }
      }
      return this;
    }

    public Builder setRequiredFeatureIndex(int index) {
      NumRecord record = new NumRecord(index);
      if (!arrayBuilder.contains(record)) {
        return this;
      }
      
      Header header = new Header(index);
      headerBuilder.set(header);
      return this;
    }
    
    public int featureIndexCount() {
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
    public LangSysTable subBuildTable(ReadableFontData data) {
      return new LangSysTable(data, false);
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

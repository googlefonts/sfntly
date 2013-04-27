package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.NumRecord;
import com.google.typography.font.sfntly.table.opentype.component.RecordList;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;
import com.google.typography.font.sfntly.table.opentype.featuretable.Header;
import com.google.typography.font.sfntly.table.opentype.featuretable.InnerArray;

public class FeatureTable extends SubTable {
  public final Header header;
  private final InnerArray array;
  public boolean dataIsCanonical;

  ////////////////
  // Constructors

  public FeatureTable(ReadableFontData data, boolean dataIsCanonical) {
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

  public int valueAt(int i) {
    return array.recordList.get(i).value;
  }

  ////////////////////////////////////
  // Builder

  public static class Builder extends VisibleBuilder<FeatureTable> {

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

    public Builder(FeatureTable table) {
      this(table.readFontData(), table.dataIsCanonical);
    }
    
    ////////////////////////////////
    // Public methods to update

    public Builder addValues(int... indices) {
      for (int index : indices) {
        NumRecord record = new NumRecord(index);
        if (!arrayBuilder.contains(record)) {
          arrayBuilder.add(new NumRecord(index));
        }
      }
      return this;
    }

    public int valueAt(int i) {
      return arrayBuilder.records().get(i).value;
    }

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
    public FeatureTable subBuildTable(ReadableFontData data) {
      return new FeatureTable(data, false);
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

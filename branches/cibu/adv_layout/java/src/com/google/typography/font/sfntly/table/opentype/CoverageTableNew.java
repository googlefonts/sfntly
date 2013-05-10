package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.component.NumRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.RangeRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.RecordList;
import com.google.typography.font.sfntly.table.opentype.component.RecordsTable;

public class CoverageTableNew extends SubstSubtable {
  public final RecordsTable<?> array;

  // //////////////
  // Constructors

  public CoverageTableNew(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    switch (format) {
    case 1:
      array = new NumRecordTable(data, headerSize(), dataIsCanonical);
      break;
    case 2:
      array = new RangeRecordTable(data, headerSize(), dataIsCanonical);
      break;
    default:
      throw new IllegalArgumentException("coverage format " + format + " unexpected");
    }
  }

  // ////////////////////////////////////////
  // Utility methods specific to this class

  public RecordList<?> records() {
    return array.recordList;
  }

  // //////////////////////////////////
  // Builder

  public static class Builder extends SubstSubtable.Builder<CoverageTableNew> {

    protected final RecordsTable.Builder<?, ?> arrayBuilder;

    // //////////////
    // Constructors

    public Builder() {
      super();
      arrayBuilder = new NumRecordTable.Builder();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
      switch (format) {
      case 1:
        arrayBuilder = new NumRecordTable.Builder(data, headerSize(), dataIsCanonical);
        break;
      case 2:
        arrayBuilder = new RangeRecordTable.Builder(data, headerSize(), dataIsCanonical);
        break;
      default:
        throw new IllegalArgumentException("coverage format " + format + " unexpected");
      }
    }

    public Builder(CoverageTableNew table) {
      this(table.readFontData(), table.dataIsCanonical);
    }

    // //////////////////////////////
    // Public methods to update

    public int valueCount() {
      return arrayBuilder.count();
    }

    // //////////////////////////////
    // Public methods to serialize

    @Override
    public int subDataSizeToSerialize() {
      return super.subDataSizeToSerialize() + arrayBuilder.subDataSizeToSerialize();
    }

    @Override
    public int subSerialize(WritableFontData newData) {
      int newOffset = super.subSerialize(newData);
      return arrayBuilder.subSerialize(newData.slice(newOffset));
    }

    // ///////////////////
    // Overriden methods

    @Override
    public CoverageTableNew subBuildTable(ReadableFontData data) {
      return new CoverageTableNew(data, 0, false);
    }

    @Override
    protected boolean subReadyToSerialize() {
      return super.subReadyToSerialize();
    }

    @Override
    public void subDataSet() {
      super.subDataSet();
      arrayBuilder.subDataSet();
    }
  }
}

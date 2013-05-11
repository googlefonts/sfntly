package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.classdef.InnerArrayFmt1;
import com.google.typography.font.sfntly.table.opentype.component.RangeRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.RecordList;
import com.google.typography.font.sfntly.table.opentype.component.RecordsTable;

public class ClassDefTableNew extends SubstSubtable {
  public final RecordsTable<?> array;
  public boolean dataIsCanonical;

  // //////////////
  // Constructors

  public ClassDefTableNew(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    this.dataIsCanonical = dataIsCanonical;

    switch (format) {
    case 1:
      array = new InnerArrayFmt1(data, headerSize(), dataIsCanonical);
      break;
    case 2:
      array = new RangeRecordTable(data, headerSize(), dataIsCanonical);
      break;
    default:
      throw new IllegalArgumentException("class def format " + format + " unexpected");
    }
  }

  // ////////////////////////////////////////
  // Utility methods specific to this class

  public RecordList<?> records() {
    return array.recordList;
  }

  // //////////////////////////////////
  // Builder

  public static class Builder extends SubstSubtable.Builder<ClassDefTableNew> {

    protected final RecordsTable.Builder<?, ?> arrayBuilder;

    // //////////////
    // Constructors

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
      switch (format) {
      case 1:
        arrayBuilder = new InnerArrayFmt1.Builder(data, headerSize(), dataIsCanonical);
        break;
      case 2:
        arrayBuilder = new RangeRecordTable.Builder(data, headerSize(), dataIsCanonical);
        break;
      default:
        throw new IllegalArgumentException("class def format " + format + " unexpected");
      }
    }

    public Builder(ClassDefTableNew table) {
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
    public ClassDefTableNew subBuildTable(ReadableFontData data) {
      return new ClassDefTableNew(data, 0, false);
    }

    @Override
    protected boolean subReadyToSerialize() {
      return super.subReadyToSerialize() && true;
    }

    @Override
    public void subDataSet() {
      super.subDataSet();
      arrayBuilder.subDataSet();
    }
  }
}

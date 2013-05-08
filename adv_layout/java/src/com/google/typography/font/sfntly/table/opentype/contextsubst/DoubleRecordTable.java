// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype.contextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.NumRecordList;
import com.google.typography.font.sfntly.table.opentype.component.SubstLookupRecordList;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class DoubleRecordTable extends SubTable {
  public final NumRecordList inputGlyphIds;
  public final SubstLookupRecordList substLookupRecords;

  // ///////////////
  // constructors

  public DoubleRecordTable(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data);
    inputGlyphIds = new NumRecordList(data, 2, 1);
    substLookupRecords = new SubstLookupRecordList(data, 2, inputGlyphIds.limit());
  }

  public DoubleRecordTable(ReadableFontData data, boolean dataIsCanonical) {
    this(data, 0, dataIsCanonical);
  }

  public static class Builder extends VisibleBuilder<DoubleRecordTable> {

    protected NumRecordList inputGlyphIdsBuilder;
    protected SubstLookupRecordList substLookupRecordsBuilder;
    protected int serializedLength;

    // ///////////////
    // constructors

    public Builder() {
      super();
    }

    public Builder(DoubleRecordTable table) {
      this(table.readFontData(), 0, false);
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data);
      if (!dataIsCanonical) {
        prepareToEdit();
      }
    }

    public Builder(Builder other) {
      super();
      inputGlyphIdsBuilder = other.inputGlyphIdsBuilder;
      substLookupRecordsBuilder = other.substLookupRecordsBuilder;
    }

    // ////////////////////////////////////
    // overriden methods

    @Override
    public int subDataSizeToSerialize() {
      if (substLookupRecordsBuilder != null) {
        serializedLength = substLookupRecordsBuilder.limit();
      } else {
        computeSizeFromData(internalReadData());
      }
      return serializedLength;
    }

    @Override
    public int subSerialize(WritableFontData newData) {
      if (serializedLength == 0) {
        return 0;
      }

      if (inputGlyphIdsBuilder == null || substLookupRecordsBuilder == null) {
        return serializeFromData(newData);
      }

      return inputGlyphIdsBuilder.writeTo(newData) + substLookupRecordsBuilder.writeTo(newData);
    }

    @Override
    public DoubleRecordTable subBuildTable(ReadableFontData data) {
      return new DoubleRecordTable(data, 0, true);
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    public void subDataSet() {
      inputGlyphIdsBuilder = null;
      substLookupRecordsBuilder = null;
    }

    // ////////////////////////////////////
    // private methods

    private void prepareToEdit() {
      initFromData(internalReadData());
      setModelChanged();
    }

    private void initFromData(ReadableFontData data) {
      if (inputGlyphIdsBuilder == null || substLookupRecordsBuilder == null) {
        inputGlyphIdsBuilder = new NumRecordList(data, 2, 1);
        substLookupRecordsBuilder = new SubstLookupRecordList(
            data, 2, inputGlyphIdsBuilder.limit());
      }
    }

    private void computeSizeFromData(ReadableFontData data) {
      // This assumes canonical data.
      int len = 0;
      if (data != null) {
        len = data.length();
      }
      serializedLength = len;
    }

    private int serializeFromData(WritableFontData newData) {
      // The source data must be canonical.
      ReadableFontData data = internalReadData();
      data.copyTo(newData);
      return data.length();
    }
  }
}

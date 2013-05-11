package com.google.typography.font.sfntly.table.opentype.chaincontextsubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.NumRecordList;
import com.google.typography.font.sfntly.table.opentype.component.SubstLookupRecordList;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class ChainSubRule extends SubTable {
  public final NumRecordList backtrackGlyphs;
  public final NumRecordList inputGlyphs;
  public final NumRecordList lookAheadGlyphs;
  public final SubstLookupRecordList lookupRecords;

  // //////////////
  // Constructors

  public ChainSubRule(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data);
    backtrackGlyphs = new NumRecordList(data);
    inputGlyphs = new NumRecordList(data, backtrackGlyphs.limit(), 0, 1);
    lookAheadGlyphs = new NumRecordList(data, inputGlyphs.limit(), 0, 0);
    lookupRecords = new SubstLookupRecordList(data, lookAheadGlyphs.limit(), 0);
  }

  public static class Builder extends VisibleBuilder<ChainSubRule> {

    protected boolean dataIsCanonical;
    public NumRecordList backtrackGlyphsBuilder;
    public NumRecordList inputGlyphsBuilder;
    public NumRecordList lookAheadGlyphsBuilder;
    public SubstLookupRecordList lookupRecordsBuilder;

    // ///////////////
    // constructors

    public Builder() {
      super();
    }

    public Builder(ChainSubRule table) {
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
      backtrackGlyphsBuilder = other.backtrackGlyphsBuilder;
      inputGlyphsBuilder = other.inputGlyphsBuilder;
      lookAheadGlyphsBuilder = other.lookAheadGlyphsBuilder;
      lookupRecordsBuilder = other.lookupRecordsBuilder;
    }

    // ////////////////////////////////////
    // overriden methods

    @Override
    public int subDataSizeToSerialize() {
      if (lookupRecordsBuilder != null) {
        serializedLength = lookupRecordsBuilder.limit();
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

      if (backtrackGlyphsBuilder == null || inputGlyphsBuilder == null
          || lookAheadGlyphsBuilder == null || lookupRecordsBuilder == null) {
        return serializeFromData(newData);
      }

      return backtrackGlyphsBuilder.writeTo(newData) + inputGlyphsBuilder.writeTo(newData)
          + lookAheadGlyphsBuilder.writeTo(newData) + lookupRecordsBuilder.writeTo(newData);
    }

    @Override
    public ChainSubRule subBuildTable(ReadableFontData data) {
      return new ChainSubRule(data, 0, true);
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    public void subDataSet() {
      backtrackGlyphsBuilder = null;
      inputGlyphsBuilder = null;
      lookupRecordsBuilder = null;
      lookAheadGlyphsBuilder = null;
    }

    // ////////////////////////////////////
    // private methods

    private void prepareToEdit() {
      initFromData(internalReadData());
      setModelChanged();
    }

    private void initFromData(ReadableFontData data) {
      if (backtrackGlyphsBuilder == null || inputGlyphsBuilder == null
          || lookAheadGlyphsBuilder == null || lookupRecordsBuilder == null) {
        backtrackGlyphsBuilder = new NumRecordList(data);
        inputGlyphsBuilder = new NumRecordList(data, backtrackGlyphsBuilder.limit(), 0, 0);
        lookAheadGlyphsBuilder = new NumRecordList(data, inputGlyphsBuilder.limit(), 0, 0);
        lookupRecordsBuilder = new SubstLookupRecordList(data, lookAheadGlyphsBuilder.limit(), 0);
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

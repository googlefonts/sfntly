package com.google.typography.font.sfntly.table.opentype.ligaturesubst;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.NullTable;
import com.google.typography.font.sfntly.table.opentype.component.OffsetRecordTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public class InnerArray extends OffsetRecordTable<NullTable> {
  public static final int FIELD_COUNT = 2;

  public static final int SUBST_FORMAT_INDEX = 0;
  public static final int SUBST_FORMAT_DEFAULT = 1;

  public static final int COVERAGE_INDEX = 1;
  public static final int COVERAGE_DEFAULT = 0;

  public InnerArray(ReadableFontData data, boolean dataIsCanonical) {
    this(data, 0, dataIsCanonical);
  }

  public InnerArray(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
    int substFormat = getField(SUBST_FORMAT_INDEX);
    if (substFormat != SUBST_FORMAT_DEFAULT) {
      throw new IllegalStateException("Subt format value is " + substFormat + " (should be 1).");
    }
  }

  @Override
  public NullTable readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new NullTable(data, dataIsCanonical);
  }

  public static class Builder extends OffsetRecordTable.Builder<InnerArray, NullTable> {

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data, dataIsCanonical);
    }

    public Builder(InnerArray table) {
      super(table);
    }

    @Override
    protected InnerArray readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new InnerArray(data, base, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<NullTable> createSubTableBuilder() {
      return new NullTable.Builder();
    }

    @Override
    protected VisibleBuilder<NullTable> createSubTableBuilder(
        ReadableFontData data, boolean dataIsCanonical) {
      return new NullTable.Builder(data, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<NullTable> createSubTableBuilder(NullTable subTable) {
      return new NullTable.Builder(subTable);
    }

    @Override
    protected void initFields() {
      setField(SUBST_FORMAT_INDEX, SUBST_FORMAT_DEFAULT);
      setField(COVERAGE_INDEX, COVERAGE_DEFAULT);
    }

    @Override
    public int fieldCount() {
      return FIELD_COUNT;
    }
  }

  @Override
  public int fieldCount() {
    return FIELD_COUNT;
  }
}
//
//
//public class InnerArray extends OffsetRecordTable<NullTable> {
//
//  public InnerArray(ReadableFontData data, int base, boolean dataIsCanonical) {
//    super(data, base, dataIsCanonical);
//  }
//
//  @Override
//  public NullTable readSubTable(ReadableFontData data, boolean dataIsCanonical) {
//    return new NullTable(data, dataIsCanonical);
//  }
//  
//  public static class Builder extends OffsetRecordTable.Builder<InnerArray, NullTable> {
//
//    public Builder() {
//      super();
//    }
//
//    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
//      super(data, base, dataIsCanonical);
//    }
//
//    public Builder(InnerArray table) {
//      super(table);
//    }
//
//    @Override
//    protected InnerArray readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
//      return new InnerArray(data, base, dataIsCanonical);
//    }
//
//    @Override
//    protected VisibleBuilder<NullTable> createSubTableBuilder() {
//      return new NullTable.Builder();
//    }
//
//    @Override
//    protected VisibleBuilder<NullTable> createSubTableBuilder(
//        ReadableFontData data, boolean dataIsCanonical) {
//      return new NullTable.Builder(data, dataIsCanonical);
//    }
//
//    @Override
//    protected VisibleBuilder<NullTable> createSubTableBuilder(NullTable subTable) {
//      return new NullTable.Builder(subTable);
//    }
//
//    @Override
//    protected void initFields() {
//    }
//
//    @Override
//    public int fieldCount() {
//      return 0;
//    }
//  }
//
//  @Override
//  public int fieldCount() {
//    return 0;
//  }
//}

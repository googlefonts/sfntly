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

  @Override
  public SubstSubtable readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    int lookupType = getField(LOOKUP_TYPE_INDEX);
    switch (GsubLookupType.forTypeNum(lookupType)) {
    case GSUB_LIGATURE:
      return new LigatureSubst(data, base, dataIsCanonical);
    case GSUB_SINGLE:
      return new SingleSubst(data, base, dataIsCanonical);
    case GSUB_MULTIPLE:
      return new MultipleSubst(data, base, dataIsCanonical);
    case GSUB_CONTEXTUAL:
      return new ContextSubst(data, base, dataIsCanonical);
    case GSUB_CHAINING_CONTEXTUAL:
      return new ChainContextSubst(data, base, dataIsCanonical);
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

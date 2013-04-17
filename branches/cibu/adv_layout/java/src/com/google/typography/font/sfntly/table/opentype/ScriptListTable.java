package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.opentype.component.RecordsTable;

public class ScriptListTable extends RecordsTable<ScriptTable> {

  public ScriptListTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data, dataIsCanonical);
  }
  
  @Override
  protected ScriptTable createSubTable(
      ReadableFontData data, int tag, boolean dataIsCanonical) {
    return ScriptTable.create(data, tag, dataIsCanonical);
  }

  public static class Builder extends 
  RecordsTable.Builder<ScriptListTable, ScriptTable> {

    @Override
    protected VisibleBuilder<ScriptTable> createSubTableBuilder(
        ReadableFontData data, int tag, boolean dataIsCanonical) {
      return new ScriptTable.Builder(data, tag, dataIsCanonical);
    }

    @Override
    protected VisibleBuilder<ScriptTable> createSubTableBuilder(int tag) {
      return new ScriptTable.Builder(tag);
    }

    @Override
    protected ScriptListTable createTable(ReadableFontData data, boolean dataIsCanonical) {
      return new ScriptListTable(data, true);
    }    
  }
}

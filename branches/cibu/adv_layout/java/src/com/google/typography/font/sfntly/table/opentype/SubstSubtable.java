package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

public abstract class SubstSubtable extends SubTable  {
  public final boolean dataIsCanonical;

  protected SubstSubtable(ReadableFontData data, boolean dataIsCanonical) {
    super(data);
    this.dataIsCanonical = dataIsCanonical;
  }
  public abstract static 
  class Builder<T extends SubstSubtable, S extends SubTable> extends VisibleBuilder<T> {
    protected boolean dataIsCanonical;
    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
    }
    public Builder() {
      super();
    }
  }
}
 
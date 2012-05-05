/**
 * 
 */
package com.google.typography.font.sfntly.table.cff;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

/**
 * @author stuartg
 *
 */
public class GenericCffSubTable extends CffSubTable {

  /**
   * @param data
   */
  private GenericCffSubTable(ReadableFontData data) {
    super(data);
  }

  public static class Builder extends CffSubTable.Builder<GenericCffSubTable> {
    public static Builder createBuilder(ReadableFontData data) {
      return new Builder(data);
    }
    
    private Builder(ReadableFontData data) {
      super(data);
    }
    
    private Builder(WritableFontData data) {
      super(data);
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      return 0;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return false;
    }

    @Override
    protected int subDataSizeToSerialize() {
      return 0;
    }

    @Override
    protected void subDataSet() {
    }

    @Override
    protected GenericCffSubTable subBuildTable(ReadableFontData data) {
      return null;
    }
  }
}

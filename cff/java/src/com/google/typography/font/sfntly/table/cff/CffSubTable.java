/**
 * 
 */
package com.google.typography.font.sfntly.table.cff;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;

/**
 * @author stuartg
 *
 */
abstract class CffSubTable extends SubTable {

  /**
   * @param data
   * @param masterData
   */
  protected CffSubTable(ReadableFontData data, ReadableFontData masterData) {
    super(data, masterData);
  }

  /**
   * @param data
   */
  protected CffSubTable(ReadableFontData data) {
    super(data);
  }

  /**
   * @param data
   * @param offset
   * @param length
   */
  protected CffSubTable(ReadableFontData data, int offset, int length) {
    super(data, offset, length);
  }

  static abstract class Builder<T extends CffSubTable> extends SubTable.Builder<T> {
    protected Builder(ReadableFontData data) {
      super(data);
    }

    protected Builder(WritableFontData data) {
      super(data);
    }

    /*
     * The following methods will never be called but they need to be here to
     * allow the CffTable to see these methods through an abstract
     * reference.
     */

    @Override
    protected int subSerialize(WritableFontData newData) {
      throw new UnsupportedOperationException(
          "Abstract class method called - should have been implemented in concrete subclass.");
    }

    @Override
    protected boolean subReadyToSerialize() {
      throw new UnsupportedOperationException(
          "Abstract class method called - should have been implemented in concrete subclass.");
    }

    @Override
    protected int subDataSizeToSerialize() {
      throw new UnsupportedOperationException(
          "Abstract class method called - should have been implemented in concrete subclass.");
    }

    @Override
    protected void subDataSet() {
      throw new UnsupportedOperationException(
          "Abstract class method called - should have been implemented in concrete subclass.");
    }

    @Override
    protected T subBuildTable(ReadableFontData data) {
      throw new UnsupportedOperationException(
          "Abstract class method called - should have been implemented in concrete subclass.");
    }
  }
}

/**
 * 
 */
package com.google.typography.font.sfntly.table.cff;

import com.google.typography.font.sfntly.data.ReadableFontData;

import java.util.Comparator;

/**
 * @author stuartg
 *
 */
public final class GenericDict extends DictDataSubTable {

  /**
   * @param data
   */
  private GenericDict(ReadableFontData data, Comparator<Integer> comparator) {
    super(data, comparator);
  }

  public static final class Builder extends DictDataSubTable.Builder<GenericDict> {
    private final Comparator<Integer> comparator;
    
    private Builder(ReadableFontData data, Comparator<Integer> comparator) {
      super(data);
      this.comparator = comparator;
    }

    @Override
    protected GenericDict subBuildTable(ReadableFontData data) {
      return new GenericDict(data, this.comparator);
    }

    public static Builder createBuilder(ReadableFontData data) {
      return new Builder(data, null);
    }  
    
    public static Builder createBuilder(ReadableFontData data, Comparator<Integer> comparator) {
      return new Builder(data, comparator);
    }    
  }
}

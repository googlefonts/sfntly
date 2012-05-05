/**
 * 
 */
package com.google.typography.font.sfntly.table.cff;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stuartg
 *
 */
public class GenericIndex extends IndexDataSubTable {

  /**
   * @param data
   */
  protected GenericIndex(ReadableFontData data) {
    super(data);
  }

  public static class Builder extends IndexDataSubTable.Builder<GenericIndex, ReadableFontData> {

    private Builder(ReadableFontData data) {
      super(data);
    }

    private Builder(WritableFontData data) {
      super(data);
    }
    
    public static Builder createBuilder(ReadableFontData data) {
      return new Builder(data);
    }

    @Override
    protected List<ReadableFontData> createElements() {
      List<ReadableFontData> elements = new ArrayList<ReadableFontData>();
      for (int i = 0; i < super.count(); i++) {
        elements.add(super.elementSlice(i));
      }
      return elements;
    }

    @Override
    protected int serializeElement(ReadableFontData element, WritableFontData data) {
      return element.copyTo(data);
    }

    @Override
    protected int sizeToSerializeElement(ReadableFontData element) {
      return element.length();
    }
    
    @Override
    protected GenericIndex subBuildTable(ReadableFontData data) {
      return new GenericIndex(data);
    }
  }
}

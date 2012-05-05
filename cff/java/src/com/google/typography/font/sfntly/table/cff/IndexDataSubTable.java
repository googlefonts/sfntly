/**
 * 
 */
package com.google.typography.font.sfntly.table.cff;

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stuartg
 *
 */
public abstract class IndexDataSubTable extends CffSubTable {

  //private int count;
  //private int offSize;
  
  /**
   * Offsets to specific elements in the underlying data. These offsets are relative to the
   * start of the table or the start of sub-blocks within the table.
   */
  private static final int OFFSET_COUNT = 0;
  private static final int OFFSET_OFFSIZE = FontData.DataSize.Card16.size();
  private static final int OFFSET_OFFSET_ARRAY = OFFSET_OFFSIZE + FontData.DataSize.OffSize.size();
  
  private static final int OFFSET_HEADER_SIZE = OFFSET_OFFSET_ARRAY;

  /**
   * @param data
   */
  IndexDataSubTable(ReadableFontData data) {
    super(data);
  }

  public int count() {
    return IndexDataSubTable.count(this.data);
  }
  
  public int offSize() {
    return IndexDataSubTable.offSize(this.data);
  }
  
  /**
   * Gets the offset for the specified element that is recorded in the index tables offset array.
   * 
   * @param element the zero-based element index
   * @return the offset for the data as stored in the offset array
   */
  public int offset(int element) {
    return IndexDataSubTable.offsetEntry(this.data, this.offSize(), element);
  }
  
  /**
   * Gets the offset for the specified element from the start of the index table.
   * 
   * @param element the zero-based element index
   * @return the offset for the data from the start of the index table
   */
  public int dataOffset(int element) {
    return IndexDataSubTable.dataOffset(this.data, this.offSize(), element);
  }
  
  public ReadableFontData entry(int element) {
    int offset = this.dataOffset(element);
    int nextOffset = this.dataOffset(element + 1);
    
    return this.data.slice(offset, nextOffset - offset);
  }
  
  static int count(ReadableFontData data) {
    return data.readCard16(OFFSET_COUNT);    
  }
  
  static int offSize(ReadableFontData data) {
    return data.readOffSize(OFFSET_OFFSIZE);
  }
  
  static int dataStart(ReadableFontData data) {
    return OFFSET_OFFSET_ARRAY + 
        (IndexDataSubTable.count(data) + 1) * IndexDataSubTable.offSize(data);
  }
  
  static int offsetEntry(ReadableFontData data, int offSize, int element) {
    return data.readOffset(OFFSET_OFFSET_ARRAY + element * offSize, offSize);
  }
  
  static int dataOffset(ReadableFontData data, int offSize, int element) {
    int offset = IndexDataSubTable.offsetEntry(data, offSize, element);
    offset += IndexDataSubTable.dataStart(data);
    return offset - 1;
  }
  
  static int indexTableSize(ReadableFontData data) {
    int count = IndexDataSubTable.count(data);
    if (count == 0) {
      return 2;
    }
    int offsize = IndexDataSubTable.offSize(data);
    int size = IndexDataSubTable.dataOffset(data, offsize, count);
    return size;
  }
  
  static ReadableFontData elementSlice(ReadableFontData data, int element) {
    int offSize = IndexDataSubTable.offSize(data);
    int offset = IndexDataSubTable.dataOffset(data, offSize, element);
    int nextOffset = IndexDataSubTable.dataOffset(data, offSize, element + 1);
    return data.slice(offset, nextOffset - offset);
  }
  
  public static abstract class Builder<T extends IndexDataSubTable, E extends Object> 
  extends CffSubTable.Builder<T> {
    private int offSize;
    private List<E> elementBuilders;

    protected Builder(ReadableFontData data) {
      super(data);
    }

    protected Builder(WritableFontData data) {
      super(data);
    }
    
//    protected Builder() {
//      super();
//    }

    public int count() {
      if (elementBuilders != null) {
        return this.elementBuilders.size();
      }
      return IndexDataSubTable.count(this.internalReadData());
    }
    
    public int offSize() {
      if (this.offSize == 0) {
        return IndexDataSubTable.offSize(this.internalReadData());
      }
      return this.offSize;
    }
    
    /**
     * Sets the offset size to be used when generating the new table.
     * 
     * @param offSize offset size to use for generating the table
     */
    public void setOffSize(int offSize) {
      CffTable.validateOffSizeException(offSize);
      this.offSize = offSize;
    }
    
    public int offset(int index) {
      return IndexDataSubTable.offsetEntry(this.internalReadData(), this.offSize(), index);
    }
    
    public List<Integer> offsetArray() {
      int count = this.count() + 1;
      List<Integer> offset = new ArrayList<Integer>(count);
      for (int i = 0; i < count; i++) {
        offset.add(this.offset(i));
      }
      return offset;
    }
    
    public List<E> elementBuilders() {
      return this.getElementBuilders();
    }
    
    public void setElementBuilders(List<E> elementBuilders) {
      this.elementBuilders = elementBuilders;
      this.setModelChanged();
    }
    
    public void revert() {
      this.elementBuilders = null;
      this.setModelChanged(false);
    }
    
    protected List<E> getElementBuilders() {
      if (this.elementBuilders == null) {
        this.elementBuilders = this.createElements();
        // TODO(stuartg): probably over-aggressive dirtying
        //this.setModelChanged();
      }
      return this.elementBuilders;
    }
    
    protected abstract List<E> createElements();
    
    protected abstract int serializeElement(E element, WritableFontData data);
    
    protected abstract int sizeToSerializeElement(E element);
    
    protected ReadableFontData elementSlice(int element) {
      return IndexDataSubTable.elementSlice(this.internalReadData(), element);
    }
    
    @Override
    protected int subSerialize(WritableFontData newData) {      
      if (!this.changed()) {
        return this.internalReadData().copyTo(newData);
      }
 
      int size = 0;
      int offsetSize = this.offSize();

      // header
      size += newData.writeCard16(size, this.elementBuilders.size());
      size += newData.writeOffSize(size, offsetSize);

      int offsetIndex = size;
      int elementIndex = 1; // start from end of index table
      size += (this.elementBuilders.size() + 1) * offsetSize;

      for (E element : this.elementBuilders) {
        offsetIndex += newData.writeOffset(offsetIndex, offsetSize, elementIndex);
        WritableFontData slice = newData.slice(size);
        int elementSize = this.serializeElement(element, slice);
        size += elementSize;
        elementIndex += elementSize;
      }
      newData.writeOffset(offsetIndex, offsetSize, elementIndex);

      return size;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (!this.changed()) {
        return this.internalReadData().length();
      }
 
      if (this.elementBuilders == null || this.elementBuilders.size() == 0) {
        return FontData.DataSize.Card16.size();
      }

      int size = FontData.DataSize.Card16.size() + FontData.DataSize.OffSize.size();
      boolean variable = false;
      size += this.offSize() * (this.elementBuilders.size() + 1);
      for (E element : this.elementBuilders) {
        int elementSize = this.sizeToSerializeElement(element);
        variable |= elementSize <= 0;
        size += Math.abs(elementSize);
      }
      return variable ? -size : size;
    }

    @Override
    protected void subDataSet() {
      this.revert();
    }
  }
}

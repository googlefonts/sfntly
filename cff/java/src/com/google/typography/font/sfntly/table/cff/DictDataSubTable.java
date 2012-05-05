/**
 * 
 */
package com.google.typography.font.sfntly.table.cff;

import com.google.typography.font.sfntly.data.FontDataInputStream;
import com.google.typography.font.sfntly.data.FontDataOutputStream;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.cff.DictTokenizer.DictEntry;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author stuartg
 *
 */
public abstract class DictDataSubTable extends CffSubTable {
  
  protected SortedMap<Integer, DictEntry> dict;
  
  /**
   * @param data
   * @param comparator 
   */
  DictDataSubTable(ReadableFontData data, Comparator<Integer> comparator) {
    super(data);
    this.dict = initialize(this.data, comparator);
  }

  private static SortedMap<Integer, DictEntry> initialize(
      ReadableFontData data, Comparator<Integer> comparator) {
    FontDataInputStream is = new FontDataInputStream(data);
    SortedMap<Integer, DictEntry> dict;
    if (comparator != null) {
      dict = new TreeMap<Integer, DictEntry>(comparator);
    } else {
      dict = new TreeMap<Integer, DictEntry>();
    }
    DictTokenizer.tokenMap(is, dict);
    return dict;
  }
  
  // TODO(stuartg): add laziness
  private Map<Integer, DictEntry> getDict() {
    return this.dict;
  }
  
  public DictEntry entry(int operator) {
    return this.getDict().get(operator);
  }
  
  public boolean hasEntry(int operator) {
    return this.getDict().containsKey(operator);
  }
  
  public Number numberValue(int operator, Number defaultValue) {
    DictEntry entry = this.entry(operator);
    if (entry == null) {
      return defaultValue;
    }
    return entry.getNumber();
  }
  
  public float[] arrayValue(int operator, float[] defaultValue) {
    DictEntry entry = this.entry(operator);
    if (entry == null) {
      return defaultValue;
    }
    return entry.getArray();
  }
  
  public float[] deltaValue(int operator, float[] defaultValue) {
    DictEntry entry = this.entry(operator);
    if (entry == null) {
      return defaultValue;
    }
    return entry.getDelta();
  }
  
  public Integer sidValue(int operator, Integer defaultValue) {
    DictEntry entry = this.entry(operator);
    if (entry == null) {
      return defaultValue;
    }
    return entry.getSID();
  }
  
  public Boolean booleanValue(int operator, Boolean defaultValue) {
    DictEntry entry = this.entry(operator);
    if (entry == null) {
      return defaultValue;
    }
    return entry.getBoolean();
  }
  
  /**
   * Makes a copy of the internal map used to model the dictionary.
   * 
   * @return a copy of the internal dictionary map
   */
  public Map<Integer, DictEntry> toMap() {
    Map<Integer, DictEntry> dict = this.getDict();
    if (dict == null) {
      return null;
    }
    return new HashMap<Integer, DictEntry>(dict);
  }
  
  public String toString() {
    return super.toString() + "\n" + this.getDict().toString();
  }
  
  public static abstract class Builder<T extends DictDataSubTable> extends CffSubTable.Builder<T> {
    private T dictSubTable;
    
    protected Builder(ReadableFontData data) {
      super(data);
    }

    protected Builder(WritableFontData data) {
      super(data);
    }

    protected T internalDictSubTable() {
      if (this.dictSubTable == null) {
        this.dictSubTable = subBuildTable(this.internalReadData());
      }
      return this.dictSubTable;
    }
    
    private Map<Integer, DictEntry> getDict() {
      // TODO(stuartg): add support for laziness and switching between data and modified internal map
      return this.internalDictSubTable().dict;
    }
    
    public DictEntry entry(int operator) {
      return this.getDict().get(operator);
    }
    
    public DictEntry setEntry(DictEntry entry) {
      DictEntry old = this.getDict().put(entry.operator(), entry);
      this.setModelChanged();
      return old;
    }
    
    public boolean hasEntry(int operator) {
      return this.getDict().containsKey(operator);
    }
    
    public DictEntry removeEntry(int operator) {
      DictEntry old = this.getDict().remove(operator);
      this.setModelChanged();
      return old;      
    }

    public float floatValue(int operator, float defaultValue) {
      DictEntry entry = this.entry(operator);
      if (entry == null) {
        return defaultValue;
      }
      return entry.getFloat();
    }
    
    public int integerValue(int operator, int defaultValue) {
      DictEntry entry = this.entry(operator);
      if (entry == null) {
        return defaultValue;
      }
      return entry.getInteger();
    }
    
    public Number numberValue(int operator) {
      return numberValue(operator, null);
    }
    
    public Number numberValue(int operator, Number defaultValue) {
      DictEntry entry = this.entry(operator);
      if (entry == null) {
        return defaultValue;
      }
      return entry.getNumber();
    }
    
    public float[] arrayValue(int operator) {
      return arrayValue(operator, null);
    }
    
    public float[] arrayValue(int operator, float[] defaultValue) {
      DictEntry entry = this.entry(operator);
      if (entry == null) {
        return defaultValue;
      }
      return entry.getArray();
    }
    
    public float[] deltaValue(int operator) {
      return deltaValue(operator, null);
    }
    
    public float[] deltaValue(int operator, float[] defaultValue) {
      DictEntry entry = this.entry(operator);
      if (entry == null) {
        return defaultValue;
      }
      return entry.getDelta();
    }
    
    public Integer sidValue(int operator) {
      return sidValue(operator, null);
    }
    
    public Integer sidValue(int operator, Integer defaultValue) {
      DictEntry entry = this.entry(operator);
      if (entry == null) {
        return defaultValue;
      }
      return entry.getSID();
    }
    
    public Boolean booleanValue(int operator) {
      return booleanValue(operator, null);
    }
    
    public Boolean booleanValue(int operator, Boolean defaultValue) {
      DictEntry entry = this.entry(operator);
      if (entry == null) {
        return defaultValue;
      }
      return entry.getBoolean();
    }
    
    public void revert() {
      this.dictSubTable = null;
      this.setModelChanged(false);
    }
    
    public String toString() {
      return super.toString() + "\n" + this.getDict().toString();
    }
    
    /**
     * Makes a copy of the internal map used to model the dictionary.
     * 
     * @return a copy of the internal dictionary map
     */
    public Map<Integer, DictEntry> toMap() {
      return new HashMap<Integer, DictEntry>(this.getDict());
    }
    
    @Override
    protected int subSerialize(WritableFontData newData) {
      if (!this.changed()) {
        return this.internalReadData().copyTo(newData);
      }
      int size = 0;
      try {
        size = DictTokenizer.serialize(
            new FontDataOutputStream(newData), this.dictSubTable.dict.values().iterator());
      } catch (IOException e) {
        // TODO(stuartg): handle
        e.printStackTrace();
      }
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
      return -1; // we can't know
      
    }

    @Override
    protected void subDataSet() {
      this.revert();
    }
  }
}

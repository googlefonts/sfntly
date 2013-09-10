// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public abstract class LayoutCommonTable<T extends LookupTable> extends SubTable {
  private static int VERSION_OFFSET = 0;
  private static int SCRIPT_LIST_OFFSET = 4;
  private static int FEATURE_LIST_OFFSET = 6;
  private static int LOOKUP_LIST_OFFSET = 8;
  private static int HEADER_SIZE = 10;

  private static int VERSION_ID = 0x00010000;

  protected final boolean dataIsCanonical;

  private FeatureListTable featureList;
  private LookupListTable lookupList;

  private FeatureCache featureCache;
  private LookupCache lookupCache;

  /**
   * @param data
   *          the GSUB or GPOS data
   */
  protected LayoutCommonTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data);
    this.dataIsCanonical = dataIsCanonical;
  }

  static int readScriptListOffset(ReadableFontData data) {
    return data.readUShort(SCRIPT_LIST_OFFSET);
  }

  int scriptListOffset() {
    return readScriptListOffset(data);
  }

  static ReadableFontData scriptListData(ReadableFontData commonData, boolean dataIsCanonical) {
    int start = readScriptListOffset(commonData);
    if (dataIsCanonical) {
      int limit = readFeatureListOffset(commonData);
      return commonData.slice(start, limit - start);
    }
    return commonData.slice(start);
  }

  ScriptListTable createScriptList() {
    return new ScriptListTable(scriptListData(data, dataIsCanonical), dataIsCanonical);
  }

  static int readFeatureListOffset(ReadableFontData data) {
    return data.readUShort(FEATURE_LIST_OFFSET);
  }

  int featureListOffset() {
    return readFeatureListOffset(data);
  }

  static ReadableFontData featureListData(ReadableFontData commonData, boolean dataIsCanonical) {
    int start = readFeatureListOffset(commonData);
    if (dataIsCanonical) {
      int limit = readLookupListOffset(commonData);
      return commonData.slice(start, limit - start);
    }
    return commonData.slice(start);
  }

  FeatureListTable createFeatureList() {
    return new FeatureListTable(featureListData(data, dataIsCanonical), dataIsCanonical);
  }

  static int readLookupListOffset(ReadableFontData data) {
    return data.readUShort(LOOKUP_LIST_OFFSET);
  }

  int lookupListOffset() {
    return readLookupListOffset(data);
  }

  static ReadableFontData lookupListData(ReadableFontData commonData, boolean dataIsCanonical) {
    int start = readLookupListOffset(commonData);
    if (dataIsCanonical) {
      int limit = commonData.length();
      return commonData.slice(start, limit - start);
    }
    return commonData.slice(start);
  }

  LookupListTable createLookupList() {
    return handleCreateLookupList(lookupListData(data, dataIsCanonical), dataIsCanonical);
  }

  protected abstract LookupListTable handleCreateLookupList(
      ReadableFontData data, boolean dataIsCanonical);

  // private static final Comparator<FeatureTable> featureComparator =
  // new Comparator<FeatureTable>() {
  // @Override
  // public int compare(FeatureTable o1, FeatureTable o2) {
  // return o1.featureTag() - o2.featureTag();
  // }
  // };

  private class FeatureCache {
    private final Map<Integer, FeatureTable> map;

    private FeatureCache() {
      this.map = new HashMap<Integer, FeatureTable>();
    }

    public FeatureTable get(int featureIndex) {
      FeatureTable featureTable;
      synchronized (this) {
        featureTable = map.get(featureIndex);
      }
      if (featureTable == null) {
        featureTable = createFeatureTable(featureIndex);
        if (featureTable != null) {
          synchronized (this) {
            map.put(featureIndex, featureTable);
          }
        }
      }
      return featureTable;
    }

    private FeatureTable createFeatureTable(int featureIndex) {
      return featureList.subTableAt(featureIndex);
    }
  }

  FeatureTable getFeatureTable(int featureIndex) {
    return featureCache.get(featureIndex);
  }

  class LookupCache {
    private final Map<Integer, LookupTable> map;

    private LookupCache() {
      map = new HashMap<Integer, LookupTable>();
    }

    public LookupTable get(int lookupIndex) {
      LookupTable lookup;
      synchronized (this) {
        lookup = map.get(lookupIndex);
      }
      if (lookup == null) {
        lookup = createLookupTable(lookupIndex);
        if (lookup != null) {
          synchronized (this) {
            map.put(lookupIndex, lookup);
          }
        }
      }
      return lookup;
    }

    private LookupTable createLookupTable(int lookupIndex) {
      return lookupList.subTableAt(lookupIndex);
    }
  }

  LookupTable getLookupTable(int lookupIndex) {
    return lookupCache.get(lookupIndex);
  }

  static class BidirectionalMultiMap<X, Y> {
    private final Map<X, Set<Y>> xyMap;
    private final Map<Y, Set<X>> yxMap;

    BidirectionalMultiMap() {
      xyMap = new HashMap<X, Set<Y>>();
      yxMap = new HashMap<Y, Set<X>>();
    }

    void add(X x, Y y) {
      Set<Y> yset = xyMap.get(x);
      if (yset == null) {
        yset = new TreeSet<Y>();
        xyMap.put(x, yset);
      }
      yset.add(y);
      Set<X> xset = yxMap.get(y);
      if (xset == null) {
        xset = new TreeSet<X>();
        yxMap.put(y, xset);
      }
      xset.add(x);
    }

    Set<X> xSet() {
      return xyMap.keySet();
    }

    Set<Y> ySet() {
      return yxMap.keySet();
    }

    boolean containsXY(X x, Y y) {
      Set<Y> yset = xyMap.get(x);
      return yset != null && yset.contains(y);
    }

    boolean containsX(X x) {
      return xyMap.containsKey(x);
    }

    boolean containsY(Y y) {
      return yxMap.containsKey(y);
    }

    Set<Y> getX(X x) {
      if (xyMap.containsKey(x)) {
        return Collections.unmodifiableSet(xyMap.get(x));
      }
      return null;
    }

    Set<X> getY(Y y) {
      if (yxMap.containsKey(y)) {
        return Collections.unmodifiableSet(yxMap.get(y));
      }
      return null;
    }

    void remove(X x, Y y) {
      Set<Y> yset = xyMap.get(x);
      if (yset == null) {
        return;
      }
      yset.remove(y);
      if (yset.isEmpty()) {
        xyMap.remove(x);
      }
      Set<X> xset = yxMap.get(y);
      xset.remove(x);
      if (xset.isEmpty()) {
        yxMap.remove(y);
      }
    }

    void removeX(X x) {
      Set<Y> yset = xyMap.get(x);
      if (yset == null) {
        return;
      }
      xyMap.remove(x);
      for (Y y : yset) {
        Set<X> xset = yxMap.get(y);
        xset.remove(x);
        if (xset.isEmpty()) {
          yxMap.remove(y);
        }
      }
    }

    void removeY(Y y) {
      Set<X> xset = yxMap.get(y);
      if (xset == null) {
        return;
      }
      yxMap.remove(y);
      for (X x : xset) {
        Set<Y> yset = xyMap.get(x);
        yset.remove(y);
        if (yset.isEmpty()) {
          xyMap.remove(x);
        }
      }
    }

    public int size() {
      return xyMap.keySet().size();
    }
  }

  public static abstract class Builder<T extends LookupTable>
  extends SubTable.Builder<LayoutCommonTable<T>> {
    private int serializedLength;
    private ScriptListTable.Builder serializedScriptListBuilder;
    private FeatureListTable.Builder serializedFeatureListBuilder;
    private LookupListTable.Builder serializedLookupListBuilder;

    /**
     * @param data
     *          the GSUB or GPOS data
     */
    protected Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
    }

    public Builder() {
      super(null);
    }

    protected abstract LookupListTable handleCreateLookupList(
        ReadableFontData data, boolean dataIsCanonical);

    private static abstract class ObjectId<T extends LookupTable> {
      protected Builder<T> builder;

      public abstract void delete();
    }

    void assertBuilder(ObjectId<T> id) {
      if (this != id.builder) {
        throw new IllegalArgumentException("cannot operate on foreign or deleted id");
      }
    }

    protected abstract LookupListTable.Builder createLookupListBuilder();

    @Override
    protected int subSerialize(WritableFontData newData) {
      if (serializedLength == 0) {
        return 0;
      }
      newData.writeULong(VERSION_OFFSET, VERSION_ID);
      int pos = HEADER_SIZE;
      newData.writeUShort(SCRIPT_LIST_OFFSET, pos);
      pos += serializedScriptListBuilder.subSerialize(newData.slice(pos));
      newData.writeUShort(FEATURE_LIST_OFFSET, pos);
      pos += serializedFeatureListBuilder.subSerialize(newData.slice(pos));
      newData.writeUShort(LOOKUP_LIST_OFFSET, pos);
      pos += serializedLookupListBuilder.subSerialize(newData.slice(pos));
      return serializedLength;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    protected abstract LayoutCommonTable<T> subBuildTable(ReadableFontData data);
  }
}

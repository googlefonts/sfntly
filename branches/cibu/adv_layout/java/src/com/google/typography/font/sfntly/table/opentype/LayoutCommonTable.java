// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.NumRecord;
import com.google.typography.font.sfntly.table.opentype.component.TagOffsetRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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

  private ScriptListTable scriptList;
  private FeatureListTable featureList;
  private LookupList lookupList;

  private LangSysCache langSysCache;
  private FeatureCache featureCache;
  private LookupCache lookupCache;

  /**
   * @param data the GSUB or GPOS data
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

  LookupList createLookupList() {
    return handleCreateLookupList(lookupListData(data, dataIsCanonical), dataIsCanonical);
  }

  protected abstract LookupList handleCreateLookupList(ReadableFontData data,
      boolean dataIsCanonical);

  private void init() {
    if (scriptList == null) {
      scriptList = createScriptList();
      featureList = createFeatureList();
      lookupList = createLookupList();

      langSysCache = createLangSysCache();
      featureCache = createFeatureCache();
      lookupCache = createLookupCache();
    }
  }

  /**
   * Given a script and language tag and a set of features, return the corresponding
   * lookups in the order specified by the font.  If the specification requires
   * features to be applied in a given order, they should be passed in separate calls.
   * Regardless of what is in the featureSet, required features for the LangSys will
   * always be included.
   *
   * @param stag the script tag
   * @param ltag the language tag
   * @param featureSet the features (set may be empty)
   * @return the lookups
   */
  public Iterable<T> lookups(ScriptTag stag, LanguageTag ltag, Set<FeatureTag> featureSet) {
    init();
    LangSysTable langSys = getLangSysTableNew(stag.tag(), ltag.tag());
    Set<FeatureTable> features = getFeatureTableSet(langSys, featureSet);
    Iterable<T> lookups = getLookups(features);
    return lookups;
  }

  private static class LangSysCacheKey {
    private final int scriptTag;
    private final int langSysTag;
    private LangSysCacheKey(ScriptTag stag, LanguageTag ltag) {
      this(stag.tag(), ltag.tag());
    }
    private LangSysCacheKey(int scriptTag, int langSysTag) {
      this.scriptTag = scriptTag;
      this.langSysTag = langSysTag;
    }
    @Override
    public int hashCode() {
      return scriptTag ^ langSysTag;
    }
    @Override
    public boolean equals(Object rhs) {
      return rhs instanceof LangSysCacheKey &&
          equals((LangSysCacheKey) rhs);
    }
    private boolean equals(LangSysCacheKey rhs) {
      return rhs != null &&
          this.scriptTag == rhs.scriptTag &&
          this.langSysTag == rhs.langSysTag;
    }
  }

  private class LangSysCache {
    private Map<LangSysCacheKey, LangSysTable> map;

    LangSysCache() {
      this.map = new HashMap<LangSysCacheKey, LangSysTable>();
    }

    public LangSysTable get(int scriptTag, int langSysTag) {
      LangSysCacheKey key = new LangSysCacheKey(scriptTag, langSysTag);
      LangSysTable langSysTable;
      synchronized(this) {
        langSysTable = map.get(key);
      }
      if (langSysTable == null) {
        langSysTable = createLangSysTableNew(scriptTag, langSysTag);
        if (langSysTable != null) {
          synchronized(this) {
            map.put(key, langSysTable);
          }
        }
      }
      return langSysTable;
    }

    private LangSysTable createLangSysTableNew(int scriptTag, int langSysTag) {
      ScriptTable scriptTable = scriptList.subTableForTag(scriptTag);
      if (scriptTable == null) {
        scriptTable = scriptList.subTableForTag(ScriptTag.DFLT.tag());
        if (scriptTable == null) {
          return null;
        }
      }
      LangSysTable langSysTable = scriptTable.subTableForTag(langSysTag);
      if (langSysTable == null) {
        langSysTable = scriptTable.defaultLangSysTable();
      }
      return langSysTable;
    }
  }

  private LangSysCache createLangSysCache() {
    return new LangSysCache();
  }

  LangSysTable getLangSysTableNew(int scriptTag, int langSysTag) {
    return langSysCache.get(scriptTag, langSysTag);
  }

//  private static final Comparator<FeatureTable> featureComparator =
//      new Comparator<FeatureTable>() {
//        @Override
//        public int compare(FeatureTable o1, FeatureTable o2) {
//          return o1.featureTag() - o2.featureTag();
//        }
//      };

  private class FeatureCache {
    private Map<Integer, FeatureTable> map;

    private FeatureCache() {
      this.map = new HashMap<Integer, FeatureTable>();
    }

    public FeatureTable get(int featureIndex) {
      FeatureTable featureTable;
      synchronized(this) {
        featureTable = map.get(featureIndex);
      }
      if (featureTable == null) {
        featureTable = createFeatureTable(featureIndex);
        if (featureTable != null) {
          synchronized(this) {
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

  private FeatureCache createFeatureCache() {
    return new FeatureCache();
  }

  FeatureTable getFeatureTable(int featureIndex) {
    return featureCache.get(featureIndex);
  }

  private Set<FeatureTable> getFeatureTableSet(LangSysTable langSys, Set<FeatureTag> features) {
    Set<FeatureTable> result = new LinkedHashSet<FeatureTable>(); //featureComparator);

    if (langSys == null) {
      return result;
    }
    
    // Always include the langSys required feature even if features doesn't contain it.
    if (langSys.header.hasRequiredFeature()) {
      FeatureTable table = getFeatureTable(langSys.header.requiredFeature);
      if (table != null) {
        result.add(table);
      }
    }

    // Only include other langSys features if features contains them.
    for (NumRecord record : langSys.records()) {
      
      int tagValue = featureList.tagAt(record.value);
      FeatureTable table = featureList.subTableAt(record.value);// featureCache.get(record.value);
      if (table != null) {
        FeatureTag tag = FeatureTag.forTagValue(tagValue);
        if (tag != null && features.contains(tag)) {
          result.add(table);
        }
      }
    }

    return result;
  }

  class LookupCache {
    private Map<Integer, T> map;

    private LookupCache() {
      map = new HashMap<Integer, T>();
    }

    public T get(int lookupIndex) {
      T lookup;
      synchronized(this) {
        lookup = map.get(lookupIndex);
      }
      if (lookup == null) {
        lookup = createLookupTable(lookupIndex);
        if (lookup != null) {
          synchronized(this) {
            map.put(lookupIndex, lookup);
          }
        }
      }
      return lookup;
    }

    @SuppressWarnings("unchecked")
    private T createLookupTable(int lookupIndex) {
      return (T) lookupList.lookupAt(lookupIndex);
    }
  }

  private LookupCache createLookupCache() {
    return new LookupCache();
  }

  T getLookupTable(int lookupIndex) {
    return lookupCache.get(lookupIndex);
  }

  /**
   * Returns the lookups for the provided features, in lookup list order.
   */
  private Iterable<T> getLookups(Set<FeatureTable> features) {
    Set<Integer> result = new TreeSet<Integer>();
    for (FeatureTable table : features) {
      for (NumRecord lookupRecord : table.records()) {
        result.add(lookupRecord.value);
      }
    }
    return new LookupIterable(result);
  }

  private class LookupIterable implements Iterable<T> {
    private Set<Integer> lookupIndices;

    public LookupIterable(Set<Integer> lookupIndices) {
      this.lookupIndices = lookupIndices;
    }

    @Override
    public Iterator<T> iterator() {
      return new LookupIterator(lookupIndices.iterator());
    }
  }

  private class LookupIterator implements Iterator<T> {
    private Iterator<Integer> indices;
    private T nextLookup;

    public LookupIterator(Iterator<Integer> indices) {
      this.indices = indices;
      this.nextLookup = advance();
    }

    @Override
    public boolean hasNext() {
      return nextLookup != null;
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      T table = nextLookup;
      nextLookup = advance();
      return table;
    }

    private T advance() {
      while (indices.hasNext()) {
        T table = getLookupTable(indices.next());
        if (table != null) {
          return table;
        }
      }
      return null;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  static class BidirectionalMultiMap<X, Y> {
    private Map<X, Set<Y>> xyMap;
    private Map<Y, Set<X>> yxMap;

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
    private boolean dataIsCanonical;
    private BidirectionalMultiMap<LangSysId<T>, FeatureId<T>> langSysFeatureBmm;
    private BidirectionalMultiMap<FeatureId<T>, LookupId<T>> featureLookupBmm;
    private TreeSet<LangSysId<T>> langSysSet;
    private TreeSet<FeatureId<T>> featureSet;
    private List<LookupId<T>> lookupList;

    private int nextLangSysId;
    private int nextFeatureId;
    private int nextLookupId;

    private int serializedLength;
    private ScriptListTable.Builder serializedScriptListBuilder;
    private FeatureListTable.Builder serializedFeatureListBuilder;
    private LookupList.Builder serializedLookupListBuilder;

    /**
     * @param data the GSUB or GPOS data
     */
    protected Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
    }

    public Builder() {
      super(null);
    }

    @SuppressWarnings("unchecked")
    private void initFromData(ReadableFontData data) {
      langSysSet = new TreeSet<LangSysId<T>>();
      featureSet = new TreeSet<FeatureId<T>>();
      lookupList = new ArrayList<LookupId<T>>();

      langSysFeatureBmm =
          new BidirectionalMultiMap<LangSysId<T>, FeatureId<T>>();
      featureLookupBmm =
          new BidirectionalMultiMap<FeatureId<T>, LookupId<T>>();

      if (data != null) {
        ScriptListTable sl = new ScriptListTable(scriptListData(data, dataIsCanonical), dataIsCanonical);
        FeatureListTable fl = new FeatureListTable(featureListData(data, dataIsCanonical), dataIsCanonical);
        LookupList ll = handleCreateLookupList(lookupListData(data, dataIsCanonical), dataIsCanonical);

        int lookupCount = ll.lookupCount();
        List<LookupId<T>> lookupIds = new ArrayList<LookupId<T>>(lookupCount);
        for (int i = 0; i < lookupCount; ++i) {
          T lookupTable = (T) ll.lookupAt(i);
          LookupId<T> lookupId = newLookup(lookupTable);
          lookupIds.add(lookupId);
        }

        int featureCount = fl.recordList.count();
        List<FeatureId<T>> featureIds = new ArrayList<FeatureId<T>>(featureCount);
        for (int i = 0; i < featureCount; ++i) {
          FeatureTable ft = fl.subTableAt(i);
          int featureTag = fl.tagAt(i);
          FeatureId<T> featureId = newFeature(featureTag);
          featureIds.add(featureId);
          for (NumRecord lookupRecord : ft.records()) {
            addLookupToFeature(lookupIds.get(lookupRecord.value), featureId);
          }
        }

        for (TagOffsetRecord sr : sl.recordList) {
          int scriptTag = sr.tag;
          ScriptTable st = sl.subTableForTag(scriptTag);
          for (TagOffsetRecord lr : st.recordList()) {
            int langTag = lr.tag;
            LangSysTable lt = st.subTableForTag(langTag);
            processLangSysTableNew(lt, scriptTag, langTag, featureIds);
          }
          LangSysTable lt = st.defaultLangSysTable();
          if (lt != null) {
            int languageTag = LanguageTag.DFLT.tag();
            processLangSysTableNew(lt, scriptTag, languageTag, featureIds);
          }
        }
      }
    }

    private void processLangSysTableNew(LangSysTable lt, int scriptTag, int languageTag,
        List<FeatureId<T>> featureIds) {
      LangSysId<T> langSysId = newLangSys(scriptTag, languageTag);
      int langSysFeatureCount = lt.records().count();
      for (int k = 0; k < langSysFeatureCount; ++k) {
        int featureIndex = lt.records().get(k).value;
        FeatureId<T> featureId = featureIds.get(featureIndex);
        addFeatureToLangSys(featureId, langSysId);
      }
      if (lt.header.hasRequiredFeature()) {
        int requiredFeatureIndex = lt.header.requiredFeature;
        FeatureId<T> requiredFeatureId = featureIds.get(requiredFeatureIndex);
        setLangSysRequiredFeature(langSysId, requiredFeatureId);
      }
    }

    protected abstract LookupList handleCreateLookupList(ReadableFontData data,
        boolean dataIsCanonical);

    private void prepareToEdit() {
      if (lookupList == null) {
        initFromData(internalReadData());
      }
    }

    private static abstract class ObjectId<T extends LookupTable> {
      protected Builder<T> builder;
      protected int id;
      protected ObjectId(Builder<T> builder, int id) {
        this.builder = builder;
        this.id = id;
      }
      public boolean isDeleted() {
        return builder == null;
      }
      public abstract void delete();
      void markDeleted() {
        builder = null;
      }
      // only called by builder to reset the ids
      void setId(int id) {
        this.id = id;
      }
    }

    void assertBuilder(ObjectId<T> id) {
      if (this != id.builder) {
        throw new IllegalArgumentException("cannot operate on foreign or deleted id");
      }
    }

    public static class LangSysId<T extends LookupTable> extends ObjectId<T>
        implements Comparable<LangSysId<T>>{
      private final int scriptTag;
      private final int languageTag;
      private FeatureId<T> requiredFeature;

      LangSysId(Builder<T> builder, int scriptTag, int languageTag) {
        super(builder, builder.nextLangSysId++);
        this.scriptTag = scriptTag;
        this.languageTag = languageTag;
      }
      @Override
      public int hashCode() {
        return scriptTag ^ languageTag;
      }
      @SuppressWarnings("unchecked")
      @Override
      public boolean equals(Object rhs) {
        return rhs instanceof LayoutCommonTable.Builder.LangSysId &&
            equals((LangSysId<T>) rhs);
      }
      public boolean equals(LangSysId<T> rhs) {
        return this.builder == rhs.builder &&
            this.scriptTag == rhs.scriptTag &&
            this.languageTag == rhs.languageTag;
      }
      @Override
      public String toString() {
        return Tag.stringValue(scriptTag).trim() + "_" + Tag.stringValue(languageTag).trim();
      }
      @Override
      public int compareTo(LangSysId<T> rhs) {
        int d = this.scriptTag - rhs.scriptTag;
        if (d == 0) {
          d = this.languageTag - rhs.languageTag;
        }
        return d;
      }
      @Override
      public void delete() {
        if (builder != null) {
          builder.deleteLangSys(this);
        }
      }
      boolean hasFeature(FeatureId<T> feature) {
        return !isDeleted() && builder.hasLangSysWithFeature(this, feature);
      }
      /** Called by builder only. */
      void setRequiredFeature(FeatureId<T> feature) {
        this.requiredFeature = feature;
      }
      /** Called by builder only. */
      FeatureId<T> requiredFeature() {
        return requiredFeature;
      }
    }

    public static class FeatureId<T extends LookupTable> extends ObjectId<T>
        implements Comparable<FeatureId<T>> {
      private final int featureTag;

      FeatureId(Builder<T> builder, int featureTag) {
        super(builder, builder.nextFeatureId++);
        this.featureTag = featureTag;
      }
      @Override
      public String toString() {
        return Tag.stringValue(featureTag).trim() + "(" + id + ")";
      }
      @Override
      public int compareTo(FeatureId<T> rhs) {
        // Equality is by identity.
        // Two FeatureIds can compare equal without being equal.
        int d = this.featureTag - rhs.featureTag;
        if (d == 0) {
          d = this.id - rhs.id;
        }
        return d;
      }
      @Override
      public void delete() {
        if (builder != null) {
          builder.deleteFeature(this);
        }
      }
      public boolean hasLangSys(LangSysId<T> langSysId) {
        return !isDeleted() && builder.hasLangSysWithFeature(langSysId, this);
      }
      public boolean hasLookup(LookupId<T> lookupId) {
        return !isDeleted() && builder.hasFeatureWithLookup(this, lookupId);
      }
    }

    public static class LookupId<T extends LookupTable> extends ObjectId<T>
        implements Comparable<LookupId<T>> {
      private final T lookup;
      LookupId(Builder<T> builder, T lookup) {
        super(builder, builder.nextLookupId++);
        this.lookup = lookup;
      }
      @Override
      public String toString() {
        return lookup.lookupType() + "(" + id + ")";
      }
      @Override
      public int compareTo(LookupId<T> rhs) {
        // Equality is by identity.
        // Two LookupIds can compare equal without being equal.
        return this.id - rhs.id;
      }
      @Override
      public void delete() {
        if (builder != null) {
          builder.deleteLookup(this);
        }
      }
      public boolean hasFeature(FeatureId<T> featureId) {
        return !isDeleted() && builder.hasFeatureWithLookup(featureId, this);
      }
    }

    public LangSysId<T> newLangSys(ScriptTag scriptTag, LanguageTag languageTag) {
      return newLangSys(scriptTag.tag(), languageTag.tag());
    }

    /**
     * Returns a langSysId for this builder for the given script and language tags,
     * minting a new one if needed.  Use LanguageTag.DFLT for the default language
     * system for the script.  The DFLT script can only have a DFLT language tag.
     *
     * @throws IllegalArgumentException if scriptTag is DFLT and languageTag
     * is not.
     */
    public LangSysId<T> newLangSys(int scriptTag, int languageTag) {
      prepareToEdit();
      if (scriptTag == ScriptTag.DFLT.tag()) {
        if (languageTag != LanguageTag.DFLT.tag()) {
          throw new IllegalArgumentException("DFLT script must have DFLT language tag");
        }
      }
      // There's not many, it says here.
      for (LangSysId<T> id : langSysSet) {
        if (id.scriptTag == scriptTag && id.languageTag == languageTag) {
          return id;
        }
      }
     LangSysId<T> id = new LangSysId<T>(this, scriptTag, languageTag);
     langSysSet.add(id);
     return id;
    }

    public Builder<T> deleteLangSys(LangSysId<T> langSys) {
      assertBuilder(langSys);
      prepareToEdit();
      langSys.markDeleted();
      langSysSet.remove(langSys);
      langSysFeatureBmm.removeX(langSys);
      return this;
    }

    public Builder<T> removeScript(ScriptTag scriptTag) {
      return removeScript(scriptTag.tag());
    }

    public Builder<T> removeScript(int scriptTag) {
      prepareToEdit();
      List<LangSysId<T>> ids = new ArrayList<LangSysId<T>>();
      ids.addAll(langSysSet);
      for (LangSysId<T> id : ids) {
        if (id.scriptTag == scriptTag) {
          langSysSet.remove(id);
          langSysFeatureBmm.removeX(id);
        }
      }
      return this;
    }

    public FeatureId<T> newFeature(FeatureTag featureTag) {
      return newFeature(featureTag.tag());
    }

    public FeatureId<T> newFeature(int featureTag) {
      prepareToEdit();
      FeatureId<T> id = new FeatureId<T>(this, featureTag);
      featureSet.add(id);
      return id;
    }

    public Builder<T> deleteFeature(FeatureId<T> feature) {
      assertBuilder(feature);
      prepareToEdit();
      feature.markDeleted();
      featureSet.remove(feature);
      langSysFeatureBmm.removeY(feature);
      featureLookupBmm.removeX(feature);
      return this;
    }

    public LookupId<T> newLookup(T lookup) {
      LookupId<T> id = new LookupId<T>(this, lookup);
      prepareToEdit();
      lookupList.add(id);
      return id;
    }

    public Builder<T> deleteLookup(LookupId<T> lookup) {
      assertBuilder(lookup);
      prepareToEdit();
      lookup.markDeleted();
      lookupList.remove(lookup);
      featureLookupBmm.removeY(lookup);
      return this;
    }

    public Builder<T> setLangSysRequiredFeature(LangSysId<T> langSys,
        FeatureId<T> requiredFeature) {
      langSys.setRequiredFeature(requiredFeature);
      return addFeatureToLangSys(requiredFeature, langSys);
    }

    public Builder<T> addFeatureToLangSys(FeatureId<T> feature, LangSysId<T> langSys) {
      assertBuilder(feature);
      assertBuilder(langSys);
      prepareToEdit();
      langSysFeatureBmm.add(langSys, feature);
      return this;
    }

    public Builder<T> removeFeatureFromLangSys(FeatureId<T> feature, LangSysId<T> langSys) {
      assertBuilder(feature);
      assertBuilder(langSys);
      prepareToEdit();
      langSysFeatureBmm.remove(langSys, feature);
      return this;
    }

    public Builder<T> addLookupToFeature(LookupId<T> lookup, FeatureId<T> feature) {
      assertBuilder(lookup);
      assertBuilder(feature);
      prepareToEdit();
      featureLookupBmm.add(feature, lookup);
      return this;
    }

    public Builder<T> removeLookupFromFeature(LookupId<T> lookup, FeatureId<T> feature) {
      assertBuilder(lookup);
      assertBuilder(feature);
      prepareToEdit();
      featureLookupBmm.remove(feature, lookup);
      return this;
    }

    public int langSysCount() {
      prepareToEdit();
      return langSysSet.size();
    }

    public int featureCount() {
      prepareToEdit();
      return featureSet.size();
    }

    public int lookupCount() {
      prepareToEdit();
      return lookupList.size();
    }

    public Builder<T> moveLookupTo(LookupId<T> lookup, int index) {
      assertBuilder(lookup);
      prepareToEdit();
      if (lookupList.contains(lookup)) {
        lookupList.remove(lookup);
        if (index < 0) {
          index += lookupList.size();
        }
        lookupList.add(index, lookup);
      }
      return this;
    }

    public LookupId<T> lookupAt(int index) {
      prepareToEdit();
      return lookupList.get(index);
    }

    public Set<LangSysId<T>> langSysIds() {
      prepareToEdit();
      TreeSet<LangSysId<T>> set = new TreeSet<LangSysId<T>>();
      set.addAll(langSysSet);
      return set;
    }

    public Set<FeatureId<T>> featureIds() {
      prepareToEdit();
      TreeSet<FeatureId<T>> set = new TreeSet<FeatureId<T>>();
      set.addAll(featureSet);
      return set;
    }

    public Set<FeatureId<T>> featuresForLangSys(LangSysId<T> langSysId) {
      TreeSet<FeatureId<T>> set = new TreeSet<FeatureId<T>>();
      Set<FeatureId<T>> features = langSysFeatureBmm.getX(langSysId);
      if (features != null) {
        set.addAll(features);
      }
      return set;
    }

    public boolean hasLangSysWithFeature(LangSysId<T> langSysId, FeatureId<T> featureId) {
      return langSysFeatureBmm.containsXY(langSysId, featureId);
    }

    public Set<LangSysId<T>> langSystemsForFeature(FeatureId<T> featureId) {
      TreeSet<LangSysId<T>> set = new TreeSet<LangSysId<T>>();
      Set<LangSysId<T>> langSysses = langSysFeatureBmm.getY(featureId);
      if (langSysses != null) {
        set.addAll(langSysses);
      }
      return set;
    }

    public Set<LookupId<T>> lookupsForFeature(FeatureId<T> featureId) {
      TreeSet<LookupId<T>> set = new TreeSet<LookupId<T>>();
      Set<LookupId<T>> lookups = featureLookupBmm.getX(featureId);
      if (lookups != null) {
        set.addAll(lookups);
      }
      return set;
    }

    public Set<FeatureId<T>> featuresForLookup(LookupId<T> lookupId) {
      TreeSet<FeatureId<T>> set = new TreeSet<FeatureId<T>>();
      Set<FeatureId<T>> features = featureLookupBmm.getY(lookupId);
      if (features != null) {
        set.addAll(features);
      }
      return set;
    }

    public boolean hasFeatureWithLookup(FeatureId<T> featureId, LookupId<T> lookupId) {
      return featureLookupBmm.containsXY(featureId, lookupId);
    }

    public List<LookupId<T>> lookupIds() {
      int count = lookupCount();
      List<LookupId<T>> lookups = new ArrayList<LookupId<T>>(count);
      for (int i = 0; i < count; ++i) {
        lookups.add(lookupAt(i));
      }
      return lookups;
    }

    public Builder<T> deleteUnusedIds() {
      boolean changed = true; // always check features at least once
      do {
        boolean langSysChanged = deleteUnusedLangSysIds();
        boolean lookupsChanged = deleteUnusedLookupIds();
        changed |= langSysChanged || lookupsChanged;
        if (changed) {
          changed = deleteUnusedFeatureIds();
        }
      } while (changed);
      return this;
    }

    private boolean deleteUnusedLangSysIds() {
      boolean changed = false;
      for (LangSysId<T> id : langSysIds()) {
        Set<FeatureId<T>> fids = langSysFeatureBmm.getX(id);
        if (fids == null || fids.isEmpty()) {
          id.delete();
          changed = true;
        }
      }
      return changed;
    }

    private boolean deleteUnusedLookupIds() {
      boolean changed = false;
      for (LookupId<T> id : lookupIds()) {
        Set<FeatureId<T>> fids = featureLookupBmm.getY(id);
        if (fids == null || fids.isEmpty()) {
          id.delete();
          changed = true;
        }
      }
      return changed;
    }

    private boolean deleteUnusedFeatureIds() {
      boolean changed = false;
      Iterator<FeatureId<T>> iter = featureSet.iterator();
      while (iter.hasNext()) {
        FeatureId<T> id = iter.next();
        if (!langSysFeatureBmm.containsY(id)) {
          featureLookupBmm.removeX(id);
        } else if (!featureLookupBmm.containsX(id)) {
          langSysFeatureBmm.removeY(id);
        } else {
          continue;
        }
        iter.remove();
        id.markDeleted();
        changed = true;
      }
      return changed;
    }

    void prepareToSerialize() {
      ScriptListTable.Builder slb = new ScriptListTable.Builder();
      FeatureListTable.Builder flb = new FeatureListTable.Builder();
      LookupList.Builder llb = createLookupListBuilder();

      deleteUnusedIds();
      renumberIds();

      for (LookupId<T> lookupId : lookupList) {
        llb.addLookup(lookupId.lookup);
      }

      for (FeatureId<T> featureId : featureSet) {
        FeatureTable.Builder ftb = (FeatureTable.Builder)flb.addBuilderForTag(featureId.featureTag);
        for (LookupId<T> lookupId : lookupsForFeature(featureId)) {
          ftb.addValues(lookupId.id);
        }
      }

      for (LangSysId<T> langSysId : langSysSet) {
        int scriptTag = langSysId.scriptTag;
        ScriptTable.Builder stb = (ScriptTable.Builder)slb.addBuilderForTag(scriptTag);
        int languageTag = langSysId.languageTag;
        LangSysTable.Builder lsb;
        if (languageTag == LanguageTag.DFLT.tag()) {
          lsb = (LangSysTable.Builder)stb.buiderForHeader();
        } else {
          lsb = (LangSysTable.Builder)stb.addBuiderForTag(languageTag);
        }
        if (langSysId.requiredFeature != null) {
          lsb.setRequiredFeatureIndex(langSysId.requiredFeature.id);
        }
        for (FeatureId<T> featureId : featuresForLangSys(langSysId)) {
          if (featureId.equals(langSysId.requiredFeature)) {
            continue;
          }
          lsb.addFeatureIndices(featureId.id);
        }
      }

      serializedScriptListBuilder = slb;
      serializedFeatureListBuilder = flb;
      serializedLookupListBuilder = llb;
    }

    protected abstract LookupList.Builder createLookupListBuilder();

    private int renumberIds(Iterable<? extends ObjectId<T>> ids) {
      int n = 0;
      for (ObjectId<T> id : ids) {
        id.setId(n++);
      }
      return n;
    }

    private void renumberIds() {
      nextLookupId = renumberIds(lookupList);
      nextFeatureId = renumberIds(featureSet);
      nextLangSysId = renumberIds(langSysSet);
    }

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
    protected int subDataSizeToSerialize() {
      prepareToSerialize();
      int size = serializedScriptListBuilder.subDataSizeToSerialize();
      size += serializedFeatureListBuilder.subDataSizeToSerialize();
      size += serializedLookupListBuilder.subDataSizeToSerialize();
      if (size > 0) {
        size += HEADER_SIZE;
      }
      serializedLength = size;
      return serializedLength;
    }

    @Override
    protected void subDataSet() {
      // Signals not editing, remaining members will be discarded
      lookupList = null;
    }

    @Override
    protected abstract LayoutCommonTable<T> subBuildTable(ReadableFontData data);
  }
}

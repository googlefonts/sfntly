// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

import java.util.Arrays;

public class FeatureTable extends SubTable {
  static final int FEATURE_PARAMS_OFFSET = 0;
  static final int LOOKUP_COUNT_OFFSET = 2;
  static final int LOOKUP_LIST_INDEX_BASE = 4;
  static final int LOOKUP_LIST_INDEX_SIZE = 2;

  private final int featureTag;
  boolean dataIsCanonical;

  public FeatureTable(ReadableFontData data, int featureTag) {
    this(data, featureTag, false);
  }

  FeatureTable(ReadableFontData data, int featureTag, boolean dataIsCanonical) {
    super(data);
    this.featureTag = featureTag;
    this.dataIsCanonical = dataIsCanonical;
  }

  public int featureTag() {
    return featureTag;
  }

  boolean dataIsCanonical() {
    return dataIsCanonical;
  }

  static int readLookupCount(ReadableFontData data) {
    if (data == null) {
      return 0;
    }
    return data.readUShort(LOOKUP_COUNT_OFFSET);
  }

  public int lookupCount() {
    return readLookupCount(data);
  }

  static int readLookupListIndexAt(ReadableFontData data, int index) {
    return data.readUShort(lookupListBase(index));
  }

  static int lookupListBase(int index) {
    return LOOKUP_LIST_INDEX_BASE + index * LOOKUP_LIST_INDEX_SIZE;
  }

  public int lookupListIndexAt(int index) {
    return readLookupListIndexAt(data, index);
  }

  public int[] lookupListIndices() {
    int[] indices = new int[lookupCount()];
    int p = LOOKUP_LIST_INDEX_BASE;
    for (int i = 0; i < indices.length; ++i, p += LOOKUP_LIST_INDEX_SIZE) {
      indices[i] = data.readUShort(p);
    }
    return indices;
  }

  public static class Builder extends VisibleBuilder<FeatureTable> {
    private final int featureTag;
    private int lookupCount;
    private int[] lookupList;
    private boolean dataIsCanonical;
    private int serializedLength;
    private int key;

    public Builder(int featureTag) {
      super(null);
      this.featureTag = featureTag;
    }

    public Builder(ReadableFontData data, int featureTag, boolean dataIsCanonical) {
      super(data);
      this.featureTag = featureTag;
      this.dataIsCanonical = dataIsCanonical;
      if (!dataIsCanonical) {
        prepareToEdit();
      }
    }

    public Builder(FeatureTable table) {
      this(table.readFontData(), table.featureTag, table.dataIsCanonical);
    }

    public int featureTag() {
      return featureTag;
    }

    int key() {
      return key;
    }

    void setKey(int key) {
      this.key = key;
    }

    boolean dataIsCanonical() {
      return dataIsCanonical;
    }

    public int lookupCount() {
      if (editing()) {
        return lookupCount;
      }
      return readLookupCount(internalReadData());
    }

    public int lookupIndexAt(int i) {
      if (editing()) {
        return lookupList[i];
      }
      return readLookupListIndexAt(internalReadData(), i);
    }

    private void initFromData(ReadableFontData data) {
      if (data == null) {
        lookupCount = 0;
        lookupList = new int[0x10];
      } else {
        lookupCount = readLookupCount(data);
        lookupList = new int[(lookupCount + 0xf) & ~0xf];
        for (int i = 0; i < lookupCount; ++i) {
          lookupList[i] = readLookupListIndexAt(data, i);
        }
        // Assume that repeated lookups are ok, so don't remove them.
      }
    }

    private boolean editing() {
      return lookupList != null;
    }

    public void prepareToEdit() {
      if (!editing()) {
        initFromData(internalReadData());
        setModelChanged();
      }
    }

    Builder appendLookupIndex(int lookup) {
      return insertLookupIndexBefore(lookupCount, lookup);
    }

    Builder insertLookupIndexBefore(int pos, int lookup) {
      prepareToEdit();
      if (lookupCount == lookupList.length) {
        lookupList = Arrays.copyOf(lookupList, lookupCount + 0x10);
      }
      for (int i = lookupCount; i > pos; --i) {
        lookupList[i] = lookupList[i-1];
      }
      lookupList[pos] = lookup;
      lookupCount += 1;
      return this;
    }

    Builder setLookupIndexAt(int pos, int lookup) {
      prepareToEdit();
      lookupList[pos] = lookup;
      return this;
    }

    Builder deleteLookupIndexAt(int pos) {
      prepareToEdit();
      lookupCount -= 1;
      while (pos < lookupCount) {
        lookupList[pos] = lookupList[pos + 1];
        ++pos;
      }
      return this;
    }

    Builder setLookupIndexes(int... indexes) {
      return setLookupIndexes(indexes, 0, indexes.length);
    }

    Builder setLookupIndexes(int[] indexes, int offset, int count) {
      if (count == 0) {
        lookupList = new int[16];
      } else {
        lookupList = Arrays.copyOfRange(indexes, offset, offset + count);
      }
      lookupCount = count;
      return this;
    }

    Builder delete() {
      lookupCount = 0;
      return this;
    }

    boolean isEmpty() {
      return lookupCount == 0;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    int serializedLength() {
      return serializedLength;
    }

    @Override
    public int subDataSizeToSerialize() {
      if (isEmpty()) {
        serializedLength = 0;
      } else {
        serializedLength = LOOKUP_LIST_INDEX_BASE +
            lookupCount * LOOKUP_LIST_INDEX_SIZE;
      }
      return serializedLength;
    }

    @Override
    public int subSerialize(WritableFontData newData) {
      if (serializedLength == 0) {
        return 0;
      }

      newData.writeUShort(LOOKUP_COUNT_OFFSET, lookupCount);
      int pos = LOOKUP_LIST_INDEX_BASE;
      for (int i = 0; i < lookupCount; ++i, pos += 2) {
        newData.writeUShort(pos, lookupList[i]);
      }

      return pos;
    }

    @Override
    public void subDataSet() {
      lookupList = null;
    }

    @Override
    public FeatureTable subBuildTable(ReadableFontData data) {
      return new FeatureTable(data, featureTag, true);
    }
  }
}

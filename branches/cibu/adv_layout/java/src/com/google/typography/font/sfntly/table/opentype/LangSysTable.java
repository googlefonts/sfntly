// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;

import java.util.Arrays;

public class LangSysTable extends SubTable {
  static final int LOOKUP_ORDER_OFFSET = 0;
  static final int REQUIRED_FEATURE_INDEX_OFFSET = 2;
  static final int FEATURE_COUNT_OFFSET = 4;
  static final int FEATURE_INDEX_BASE = 6;
  static final int FEATURE_INDEX_SIZE = 2;
  static final int NO_REQUIRED_FEATURE_INDEX = 0x0ffff;

  private final int langSysTag;
  private boolean dataIsCanonical;

  private LangSysTable(ReadableFontData data, int langSysTag,
      boolean dataIsCanonical) {
    super(data);
    this.langSysTag = langSysTag;
    this.dataIsCanonical = dataIsCanonical;
  }

  static LangSysTable create(ReadableFontData data, int langSysTag) {
    return new LangSysTable(data, langSysTag, false);
  }

  public int langSysTag() {
    return langSysTag;
  }

  static int readRequiredFeatureIndex(ReadableFontData data) {
    if (data == null) {
      return NO_REQUIRED_FEATURE_INDEX;
    }
    return data.readUShort(REQUIRED_FEATURE_INDEX_OFFSET);
  }

  public boolean hasRequiredFeature() {
    return requiredFeatureIndex() != NO_REQUIRED_FEATURE_INDEX;
  }

  public int requiredFeatureIndex() {
    return readRequiredFeatureIndex(data);
  }

  static int readFeatureCount(ReadableFontData data) {
    if (data == null) {
      return 0;
    }
    return data.readUShort(FEATURE_COUNT_OFFSET);
  }

  public int featureCount() {
    return readFeatureCount(data);
  }

  static int readFeatureIndexAt(ReadableFontData data, int index) {
    return data.readUShort(FEATURE_INDEX_BASE + index * FEATURE_INDEX_SIZE);
  }

  public int featureIndexAt(int index) {
    return readFeatureIndexAt(data, index);
  }

  static int readSerializedSize(ReadableFontData data) {
    int featureCount = readFeatureCount(data);
    if (featureCount == 0) {
      int requiredFeatureIndex = readRequiredFeatureIndex(data);
      if (requiredFeatureIndex == NO_REQUIRED_FEATURE_INDEX) {
        return 0;
      }
    }
    return FEATURE_INDEX_BASE + featureCount * FEATURE_INDEX_SIZE;
  }

  public static class Builder extends SubTable.Builder<LangSysTable> {
    private final int langSysTag;
    private int requiredFeatureIndex;
    private int featureCount;
    private int[] featureList;
    private boolean dataIsCanonical;
    private int serializedLength;

    public Builder(int langSysTag) {
      super(null);
      this.langSysTag = langSysTag;
    }

    public Builder(ReadableFontData data, int langSysTag, boolean dataIsCanonical) {
      super(data);
      this.langSysTag = langSysTag;
      this.dataIsCanonical = dataIsCanonical;
      if (!dataIsCanonical) {
        prepareToEdit();
      }
    }

    public Builder(LangSysTable table) {
      this(table.readFontData(), table.langSysTag, table.dataIsCanonical);
    }

    public int langSysTag() {
      return langSysTag;
    }

    boolean dataIsCanonical() {
      return dataIsCanonical;
    }

    public int requiredFeatureIndex() {
      if (editing()) {
        return requiredFeatureIndex;
      }
      return readRequiredFeatureIndex(internalReadData());
    }

    public int featureCount() {
      if (editing()) {
        return featureCount;
      }
      return readFeatureCount(internalReadData());
    }

    public int featureIndexAt(int i) {
      if (editing()) {
        return featureList[i];
      }
      return readFeatureIndexAt(internalReadData(), i);
    }

    private void initFromData(ReadableFontData data) {
      if (data == null) {
        requiredFeatureIndex = NO_REQUIRED_FEATURE_INDEX;
        featureCount = 0;
        featureList = new int[0x10];
      } else {
        requiredFeatureIndex = readRequiredFeatureIndex(data);
        featureCount = readFeatureCount(data);
        featureList = new int[(featureCount + 0xf) & ~0xf];
        for (int i = 0; i < featureCount; ++i) {
          featureList[i] = readFeatureIndexAt(data, i);
        }
        if (!dataIsCanonical) {
          // remove default feature from list
          int fc = featureCount;
          if (requiredFeatureIndex != NO_REQUIRED_FEATURE_INDEX) {
            for (int i = 0; i < fc;) {
              if (featureList[i] == requiredFeatureIndex) {
                featureList[i] = featureList[--fc];
              } else {
                ++i;
              }
            }
          }
          // remove duplicates from list
          for (int i = 0; i < fc; ++i) {
            int fi = featureList[i];
            for (int j = i + 1; j < fc;) {
              if (featureList[j] == fi) {
                featureList[j] = featureList[--fc];
              } else {
                ++j;
              }
            }
          }
          featureCount = fc;
        }
      }
    }

    private boolean editing() {
      return featureList != null;
    }

    public void prepareToEdit() {
      if (!editing()) {
        initFromData(internalReadData());
        setModelChanged();
      }
    }

    public Builder setRequiredFeatureIndex(int index) {
      prepareToEdit();
      requiredFeatureIndex = index;
      removeFeatureIndex(index);
      return this;
    }

    public Builder addFeatureIndex(int index) {
      prepareToEdit();
      for (int i = 0; i < featureCount; ++i) {
        if (featureList[i] == index) {
          return this;
        }
      }
      ensureSpace(1);
      featureList[featureCount++] = index;
      if (requiredFeatureIndex == index) {
        requiredFeatureIndex = NO_REQUIRED_FEATURE_INDEX;
      }
      return this;
    }

    public Builder removeFeatureIndex(int index) {
      prepareToEdit();
      for (int i = 0; i < featureCount; ++i) {
        if (featureList[i] == index) {
          while (++i < featureCount) {
            featureList[i-1] = featureList[i];
          }
          --featureCount;
          break;
        }
      }
      return this;
    }

    public Builder addFeatureIndices(int... indices) {
      for (int index : indices) {
        addFeatureIndex(index);
      }
      return this;
    }

    public Builder removeFeatureIndices(int... indices) {
      for (int index : indices) {
        removeFeatureIndex(index);
      }
      return this;
    }

    public Builder setFeatureIndices(int... indices) {
      featureCount = 0;
      return addFeatureIndices(indices);
    }

    public Builder delete() {
      featureCount = 0;
      requiredFeatureIndex = NO_REQUIRED_FEATURE_INDEX;
      return this;
    }

    private void ensureSpace(int addedSpace) {
      int newLen = featureCount + addedSpace;
      if (newLen > featureList.length) {
        newLen = (newLen + 0xf) & ~0xf;
        featureList = Arrays.copyOf(featureList, newLen);
      }
    }

    boolean isEmpty() {
      return requiredFeatureIndex == NO_REQUIRED_FEATURE_INDEX &&
          featureCount == 0;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    int serializedLength() {
      return serializedLength;
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (isEmpty()) {
        serializedLength = 0;
      } else {
        serializedLength = FEATURE_INDEX_BASE + featureCount * FEATURE_INDEX_SIZE;
      }
      return serializedLength;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      if (serializedLength == 0) {
        return 0;
      }

      newData.writeUShort(LOOKUP_ORDER_OFFSET, 0);
      newData.writeUShort(REQUIRED_FEATURE_INDEX_OFFSET, requiredFeatureIndex);
      newData.writeUShort(FEATURE_COUNT_OFFSET, featureCount);
      int pos = FEATURE_INDEX_BASE;
      for (int i = 0; i < featureCount; ++i, pos += 2) {
        newData.writeUShort(pos, featureList[i]);
      }

      return pos;
    }

    @Override
    protected void subDataSet() {
      featureList = null;
    }

    @Override
    protected LangSysTable subBuildTable(ReadableFontData data) {
      return new LangSysTable(data, langSysTag, true);
    }
  }
}

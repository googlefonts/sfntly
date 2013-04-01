// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class FeatureList extends SubTable implements Iterable<FeatureTable> {
  static final int FEATURE_LIST_FEATURE_COUNT_OFFSET = 0;
  static final int FEATURE_LIST_FEATURE_RECORD_BASE = 2;

  static final int FEATURE_RECORD_FEATURE_TAG_OFFSET = 0;
  static final int FEATURE_RECORD_FEATURE_OFFSET = 4;
  static final int FEATURE_RECORD_SIZE = 6;

  private boolean dataIsCanonical;

  private FeatureList(ReadableFontData data, boolean dataIsCanonical) {
    super(data);
    this.dataIsCanonical = dataIsCanonical;
  }

  static FeatureList create(ReadableFontData data, boolean dataIsCanonical) {
    return new FeatureList(data, dataIsCanonical);
  }

  boolean dataIsCanonical() {
    return dataIsCanonical;
  }

  static int readFeatureCount(ReadableFontData data) {
    if (data == null) {
      return 0;
    }
    return data.readUShort(FEATURE_LIST_FEATURE_COUNT_OFFSET);
  }

  public int featureCount() {
    return readFeatureCount(data);
  }

  static int featureRecordBase(int index) {
    return FEATURE_LIST_FEATURE_RECORD_BASE + index * FEATURE_RECORD_SIZE;
  }

  static int readFeatureTagForRecord(ReadableFontData data, int recordBase) {
    return data.readULongAsInt(recordBase + FEATURE_RECORD_FEATURE_TAG_OFFSET);
  }

  static int readFeatureBaseForRecord(ReadableFontData data, int recordBase) {
    return data.readUShort(recordBase + FEATURE_RECORD_FEATURE_OFFSET);
  }

  public int featureTagAt(int index) {
    return readFeatureTagForRecord(data, featureRecordBase(index));
  }

  public FeatureTable featureTableAt(int index) {
    return featureTableForRecord(featureRecordBase(index));
  }

  private FeatureTable featureTableForRecord(int recordBase) {
    int featureBase = readFeatureBaseForRecord(data, recordBase);
    int featureTag = readFeatureTagForRecord(data, recordBase);
    ReadableFontData newData = data.slice(featureBase);
    return new FeatureTable(newData, featureTag, dataIsCanonical);
  }

  @Override
  public Iterator<FeatureTable> iterator() {
    return new Iterator<FeatureTable>() {
      int p = FEATURE_LIST_FEATURE_RECORD_BASE;
      int e = p + featureCount() * FEATURE_RECORD_SIZE;

      @Override
      public boolean hasNext() {
        return p < e;
      }

      @Override
      public FeatureTable next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        FeatureTable table = featureTableForRecord(p);
        p += FEATURE_RECORD_SIZE;
        return table;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static class Builder extends SubTable.Builder<FeatureList> {
    private List<FeatureTable.Builder> builders;
    boolean dataIsCanonical;
    private int serializedCount;
    private int serializedLength;

    public Builder() {
      super(null);
    }

    public Builder(ReadableFontData data) {
      this(data, false);
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
      if (!dataIsCanonical) {
        prepareToEdit();
      }
    }

    public Builder(FeatureList table) {
      this(table.readFontData(), table.dataIsCanonical);
    }

    static FeatureTable.Builder createFeatureTableBuilder(ReadableFontData data,
        int offset, int length, int featureTag) {
      ReadableFontData newData;
      boolean dataIsCanonical = length >= 0;
      if (dataIsCanonical) {
        newData = data.slice(offset, length);
      } else {
        newData = data.slice(offset);
      }
      return new FeatureTable.Builder(newData, featureTag, dataIsCanonical);
    }

    private void initFromData(ReadableFontData data) {
      builders = new ArrayList<FeatureTable.Builder>();
      if (data != null) {
        int featureCount = readFeatureCount(data);

        int recordBase = FEATURE_LIST_FEATURE_RECORD_BASE;
        int recordEnd = recordBase + featureCount * FEATURE_RECORD_SIZE;

        if (featureCount > 0) {
          if (dataIsCanonical) {
            int subTableLimit = recordEnd;
            do {
              // Each table starts where the previous one ended.
              int offset = subTableLimit;
              int featureTag = readFeatureTagForRecord(data, recordBase);
              recordBase += FEATURE_RECORD_SIZE;
              // Each table ends at the next start, or at the end of the data.
              if (recordBase < recordEnd) {
                subTableLimit = readFeatureBaseForRecord(data, recordBase);
              } else {
                subTableLimit = data.length();
              }
              int length = subTableLimit - offset;
              FeatureTable.Builder builder =
                  createFeatureTableBuilder(data, offset, length, featureTag);
              builders.add(builder);
            } while (recordBase < recordEnd);
          } else {
            do {
              int offset = readFeatureBaseForRecord(data, recordBase);
              int featureTag = readFeatureTagForRecord(data, recordBase);
              recordBase += FEATURE_RECORD_SIZE;
              FeatureTable.Builder builder =
                  createFeatureTableBuilder(data, offset, -1, featureTag);
              builders.add(builder);
            } while (recordBase < recordEnd);
          }
        }
      }
    }

    public void prepareToEdit() {
      if (builders == null) {
        initFromData(internalReadData());
        setModelChanged();
      }
    }

    public int featureCount() {
      if (builders == null) {
        return readFeatureCount(internalReadData());
      }
      return builders.size();
    }

    /**
     * Adds a new builder with the given tag.
     */
    public FeatureTable.Builder addFeature(int featureTag) {
      prepareToEdit();
      FeatureTable.Builder builder = new FeatureTable.Builder(featureTag);
      builders.add(builder);
      return builder;
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    private void computeSizeFromBuilders() {
      // This does not merge FeatureTables that reference the same
      // features.
      int len = 0;
      int count = 0;
      for (FeatureTable.Builder builder : builders) {
        int sublen = builder.subDataSizeToSerialize();
        if (sublen > 0) {
          ++count;
          len += sublen;
        }
      }
      if (len > 0) {
        len += FEATURE_LIST_FEATURE_RECORD_BASE + count * FEATURE_RECORD_SIZE;
      }
      serializedLength = len;
      serializedCount = count;
    }

    private void computeSizeFromData(ReadableFontData data) {
      // This assumes canonical data.
      int len = 0;
      int count = 0;
      if (data != null) {
        len = data.length();
        count = readFeatureCount(data);
      }
      serializedLength = len;
      serializedCount = count;
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (builders != null) {
        computeSizeFromBuilders();
      } else {
        computeSizeFromData(internalReadData());
      }
      return serializedLength;
    }

    private int serializeFromBuilders(WritableFontData newData) {
      // The canonical form of the data consists of the header,
      // the index, then the featureTables from the index in index order.  All
      // featureTables are distinct; there's no sharing of tables.
      int rpos = FEATURE_LIST_FEATURE_RECORD_BASE;
      int rend = rpos + serializedCount * FEATURE_RECORD_SIZE;
      int pos = rend;
      for (FeatureTable.Builder builder : builders) {
        if (builder.serializedLength() > 0) {
          newData.writeULong(rpos + FEATURE_RECORD_FEATURE_TAG_OFFSET,
              builder.featureTag());
          newData.writeUShort(rpos + FEATURE_RECORD_FEATURE_OFFSET, pos);
          rpos += FEATURE_RECORD_SIZE;
          pos += builder.subSerialize(newData.slice(pos));
        }
      }
      newData.writeUShort(FEATURE_LIST_FEATURE_COUNT_OFFSET, serializedCount);
      return pos;
    }

    private int serializeFromData(WritableFontData newData) {
      // The source data must be canonical.
      ReadableFontData data = internalReadData();
      data.copyTo(newData);
      return data.length();
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      if (serializedLength == 0) {
        return 0;
      }
      if (builders != null) {
        return serializeFromBuilders(newData);
      }
      return serializeFromData(newData);
    }

    @Override
    protected void subDataSet() {
      builders = null;
    }

    @Override
    protected FeatureList subBuildTable(ReadableFontData data) {
      return new FeatureList(data, true);
    }
  }
}

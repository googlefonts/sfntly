// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.LookupType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
abstract class LookupList extends SubTable {
  private final boolean dataIsCanonical;

  private LookupList(ReadableFontData data, boolean dataIsCanonical) {
    super(data);
    this.dataIsCanonical = dataIsCanonical;
  }

  private static final int LOOKUP_COUNT_OFFSET = 0;
  private static final int LOOKUP_OFFSET_BASE = 2;
  private static final int LOOKUP_OFFSET_SIZE = 2;

  private static int readLookupCount(ReadableFontData data) {
    if (data == null) {
      return 0;
    }
    return data.readUShort(LOOKUP_COUNT_OFFSET);
  }

  private int lookupCount() {
    return readLookupCount(data);
  }

  private static int readLookupOffsetAt(ReadableFontData data, int index) {
    if (data == null) {
      return -1;
    }
    return data.readUShort(LOOKUP_OFFSET_BASE + index * LOOKUP_OFFSET_SIZE);
  }

  private int lookupOffsetAt(int index) {
    return readLookupOffsetAt(data, index);
  }

  private static int readLookupTypeNumAtOffset(ReadableFontData data, int offset) {
    if (data == null) {
      return -1;
    }
    return data.readUShort(offset + LookupTable.LOOKUP_TYPE_INDEX);
  }

  private static ReadableFontData readLookupData(ReadableFontData data, boolean dataIsCanonical,
      int index) {
    ReadableFontData newData;
    int offset = readLookupOffsetAt(data, index);
    if (dataIsCanonical) {
      int nextOffset;
      if (index < readLookupCount(data) - 1) {
        nextOffset = readLookupOffsetAt(data, index + 1);
      } else {
        nextOffset = data.length();
      }
      newData = data.slice(offset, nextOffset - offset);
    } else {
      newData = data.slice(offset);
    }
    return newData;
  }

  protected abstract LookupType lookupTypeAt(int index);

  private LookupTable lookupAt(int index) {
    ReadableFontData lookupData = readLookupData(data, dataIsCanonical, index);
    return createLookup(lookupData);
  }

  protected abstract LookupTable createLookup(ReadableFontData data);

  private static abstract class Builder extends SubTable.Builder<LookupList> {
    private List<LookupTable.Builder> builders;
    private boolean dataIsCanonical;
    private int serializedCount;
    private int serializedLength;

    private Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
    }

    private Builder() {
      this(null, false);
    }

    protected abstract LookupTable.Builder createLookupBuilder(
        ReadableFontData lookupData);

    private void initFromData(ReadableFontData data) {
      int count = readLookupCount(data);
      builders = new ArrayList<LookupTable.Builder>(count);
      for (int i = 0; i < count; ++i) {
        ReadableFontData lookupData = readLookupData(data, dataIsCanonical, i);
        LookupTable.Builder lookup = createLookupBuilder(lookupData);
        if (lookup != null) {
          builders.add(lookup);
        }
      }
    }

    private void prepareToEdit() {
      if (builders == null) {
        initFromData(internalReadData());
      }
    }

    private int lookupCount() {
      if (builders == null) {
        return readLookupCount(internalReadData());
      }
      return builders.size();
    }

    private LookupTable.Builder lookupAt(int index) {
      prepareToEdit();
      return builders.get(index);
    }

    private Builder addLookup(LookupTable lookup) {
      return addLookup(lookup.builder());
    }

    private Builder addLookup(LookupTable.Builder lookup) {
      return addLookupAt(lookup, lookupCount());
    }

    private Builder addLookupAt(LookupTable lookup, int index) {
      return addLookupAt(lookup.builder(), index);
    }

    private Builder addLookupAt(LookupTable.Builder lookup, int index) {
      prepareToEdit();
      builders.add(index, lookup);
      return this;
    }

    private Builder moveLookup(int fromIndex, int toIndex) {
      prepareToEdit();
      LookupTable.Builder builder = builders.remove(fromIndex);
      builders.add(toIndex, builder);
      return this;
    }

    private Builder removeLookupAt(int index) {
      prepareToEdit();
      builders.remove(index);
      return this;
    }

    private int serializeFromBuilders(WritableFontData newData) {
      if (serializedCount == 0) {
        return 0;
      }
      newData.writeUShort(LOOKUP_COUNT_OFFSET, serializedCount);
      int rpos = LOOKUP_OFFSET_BASE;
      int spos = rpos + serializedCount * LOOKUP_OFFSET_SIZE;
      for (int i = 0; i < builders.size(); ++i) {
        LookupTable.Builder builder = builders.get(i);
        int s = builder.subDataSizeToSerialize();
        if (s > 0) {
          newData.writeUShort(rpos, spos);
          rpos += LOOKUP_OFFSET_SIZE;

          WritableFontData targetData = newData.slice(spos);
          builder.subSerialize(targetData);
          spos += s;
        }
      }
      return serializedLength;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      if (builders == null) {
        // Only the case if data is canonical
        ReadableFontData data = internalReadData();
        data.copyTo(newData);
        return data.length();
      }
      return serializeFromBuilders(newData);
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    private int computeSerializedSizeFromBuilders() {
      int size = 0;
      int count = 0;
      for (int i = 0; i < builders.size(); ++i) {
        int s = builders.get(i).subDataSizeToSerialize();
        if (s > 0) {
          ++count;
          size += s;
        }
      }
      if (count > 0) {
        size += LOOKUP_OFFSET_BASE + count * LOOKUP_OFFSET_SIZE;
      }

      serializedCount = count;
      serializedLength = size;

      return serializedLength;
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (builders == null) {
        if (dataIsCanonical) {
          return internalReadData().length();
        }
        prepareToEdit();
      }
      return computeSerializedSizeFromBuilders();
    }

    @Override
    protected void subDataSet() {
      builders = null;
    }

    @Override
    protected abstract LookupList subBuildTable(ReadableFontData data);
  }
}

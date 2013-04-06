// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class ScriptListTable extends SubTable implements Iterable<ScriptTable> {
  static final int SCRIPT_COUNT_OFFSET = 0;
  static final int SCRIPT_RECORD_BASE = 2;

  static final int SCRIPT_RECORD_SCRIPT_TAG_OFFSET = 0;
  static final int SCRIPT_RECORD_SCRIPT_OFFSET = 4;
  static final int SCRIPT_RECORD_SIZE = 6;

  private boolean dataIsCanonical;

  static ScriptListTable create(ReadableFontData data,
      boolean dataIsCanonical) {
    return new ScriptListTable(data, dataIsCanonical);
  }

  private ScriptListTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data);
    this.dataIsCanonical = dataIsCanonical;
  }

  boolean dataIsCanonical() {
    return dataIsCanonical;
  }

  static int readScriptCount(ReadableFontData data) {
    if (data == null) {
      return 0;
    }
    return data.readUShort(SCRIPT_COUNT_OFFSET);
  }

  public int scriptCount() {
    return readScriptCount(data);
  }

  static int scriptRecordBase(int index) {
    return SCRIPT_RECORD_BASE + index * SCRIPT_RECORD_SIZE;
  }

  static int readScriptRecordBaseForTag(ReadableFontData data, int scriptTag) {
    int p = SCRIPT_RECORD_BASE;
    int e = p + readScriptCount(data) * SCRIPT_RECORD_SIZE;
    for (;p < e; p += SCRIPT_RECORD_SIZE) {
      if (readScriptTagForRecord(data, p) == scriptTag) {
        return p;
      }
    }
    return 0;
  }

  static int readScriptTagForRecord(ReadableFontData data, int recordBase) {
    return data.readULongAsInt(recordBase + SCRIPT_RECORD_SCRIPT_TAG_OFFSET);
  }

  static int readScriptBaseForRecord(ReadableFontData data, int recordBase) {
    return data.readUShort(recordBase + SCRIPT_RECORD_SCRIPT_OFFSET);
  }

  public int scriptTagAt(int index) {
    return readScriptTagForRecord(data, scriptRecordBase(index));
  }

  public ScriptTable scriptTableAt(int index) {
    return scriptTableForRecord(scriptRecordBase(index));
  }

  public boolean hasScriptTable(ScriptTag scriptTag) {
    return hasScriptTable(scriptTag.tag());
  }

  public boolean hasScriptTable(int scriptTag) {
    return readScriptRecordBaseForTag(data, scriptTag) > 0;
  }

  public ScriptTable scriptTableForTag(ScriptTag scriptTag) {
    return scriptTableForTag(scriptTag.tag());
  }

  public ScriptTable scriptTableForTag(int scriptTag) {
    int recordBase = readScriptRecordBaseForTag(data, scriptTag);
    return recordBase == 0 ? null : scriptTableForRecord(recordBase);
  }

  @Override
  public Iterator<ScriptTable> iterator() {
    return new Iterator<ScriptTable>() {
      int p = SCRIPT_RECORD_BASE;
      int e = p + scriptCount() * SCRIPT_RECORD_SIZE;

      @Override
      public boolean hasNext() {
        return p < e;
      }

      @Override
      public ScriptTable next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        ScriptTable table = scriptTableForRecord(p);
        p += SCRIPT_RECORD_SIZE;
        return table;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private ScriptTable scriptTableForRecord(int recordBase) {
    int scriptBase = readScriptBaseForRecord(data, recordBase);
    int scriptTag = readScriptTagForRecord(data, recordBase);
    ReadableFontData newData = data.slice(scriptBase);
    return ScriptTable.create(newData, scriptTag, dataIsCanonical);
  }

  public static class Builder extends SubTable.Builder<ScriptListTable> {
    private TreeMap<Integer, ScriptTable.Builder> builders;
    private LangSysTable.Builder defaultBuilder;
    boolean dataIsCanonical;
    private int serializedCount;
    private int serializedLength;

    public Builder() {
      super(null);
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
      if (!dataIsCanonical) {
        prepareToEdit();
      }
    }

    public Builder(ScriptListTable table) {
      this(table.readFontData(), table.dataIsCanonical);
    }

    static ScriptTable.Builder createScriptTableBuilder(ReadableFontData data,
        int offset, int length, int scriptTag) {
      ReadableFontData newData;
      boolean dataIsCanonical = length >= 0;
      if (dataIsCanonical) {
        newData = data.slice(offset, length);
      } else {
        newData = data.slice(offset);
      }
      return new ScriptTable.Builder(newData, scriptTag, dataIsCanonical);
    }

    private void initFromData(ReadableFontData data) {
      builders = new TreeMap<Integer, ScriptTable.Builder>();
      defaultBuilder = null;
      if (data != null) {
        int scriptCount = readScriptCount(data);

        int recordBase = SCRIPT_RECORD_BASE;
        int recordEnd = recordBase + scriptCount * SCRIPT_RECORD_SIZE;

        // Start of the first subtable in the data, if we're canonical.
        int subTableLimit = recordEnd;

        if (scriptCount > 0) {
          if (dataIsCanonical) {
            do {
              // Each table starts where the previous one ended.
              int offset = subTableLimit;
              int scriptTag = readScriptTagForRecord(data, recordBase);
              recordBase += SCRIPT_RECORD_SIZE;
              // Each table ends at the next start, or at the end of the data.
              if (recordBase < recordEnd) {
                subTableLimit = readScriptBaseForRecord(data, recordBase);
              } else {
                subTableLimit = data.length();
              }
              int length = subTableLimit - offset;
              ScriptTable.Builder builder =
                  createScriptTableBuilder(data, offset, length, scriptTag);
              builders.put(scriptTag, builder);
            } while (recordBase < recordEnd);
          } else {
            do {
              int offset = readScriptBaseForRecord(data, recordBase);
              int scriptTag = readScriptTagForRecord(data, recordBase);
              recordBase += SCRIPT_RECORD_SIZE;
              ScriptTable.Builder builder =
                  createScriptTableBuilder(data, offset, -1, scriptTag);
              builders.put(scriptTag, builder);
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

    public int scriptCount() {
      if (builders == null) {
        return readScriptCount(internalReadData());
      }
      return builders.size();
    }

    /**
     * Returns the builder for the language system specified by the tag, adding a
     * new builder if necessary.
     */
    public ScriptTable.Builder addScript(int scriptTag) {
      prepareToEdit();
      ScriptTable.Builder builder = builders.get(scriptTag);
      if (builder == null) {
        builder = new ScriptTable.Builder(scriptTag);
        builders.put(scriptTag, builder);
      }
      return builder;
    }

    /**
     * Return the builder for the script specified by the tag, or null if
     * the script is not currently listed.
     */
    public ScriptTable.Builder editScript(int scriptTag) {
      prepareToEdit();
      return builders.get(scriptTag);
    }

    /**
     * Remove the script specified by the tag.
     */
    public void removeScript(int scriptTag) {
      prepareToEdit();
      builders.remove(scriptTag);
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    private void computeSizeFromBuilders() {
      // This does not merge LangSysTables that reference the same
      // features.

      // If there is no data in the default LangSysTable or any
      // of the other LangSysTables, the size is zero, and this table
      // will not be written.

      int len = 0;
      int count = 0;
      if (defaultBuilder != null) {
        len += defaultBuilder.subDataSizeToSerialize();
      }
      for (ScriptTable.Builder builder : builders.values()) {
        int sublen = builder.subDataSizeToSerialize();
        if (sublen > 0) {
          ++count;
          len += sublen;
        }
      }
      if (len > 0) {
        len += SCRIPT_RECORD_BASE + count * SCRIPT_RECORD_SIZE;
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
        count = readScriptCount(data);
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
      // the index, then the
      // scriptTables from the index in index order.  All
      // scriptTables are distinct; there's no sharing of tables.
      int rpos = SCRIPT_RECORD_BASE;
      int rend = rpos + serializedCount * SCRIPT_RECORD_SIZE;
      int pos = rend;
      for (ScriptTable.Builder builder : builders.values()) {
        if (builder.serializedLength() > 0) {
          newData.writeULong(rpos + SCRIPT_RECORD_SCRIPT_TAG_OFFSET,
              builder.scriptTag());
          newData.writeUShort(rpos + SCRIPT_RECORD_SCRIPT_OFFSET, pos);
          rpos += SCRIPT_RECORD_SIZE;
          pos += builder.subSerialize(newData.slice(pos));
        }
      }
      newData.writeUShort(SCRIPT_COUNT_OFFSET, serializedCount);
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
      defaultBuilder = null;
    }

    @Override
    protected ScriptListTable subBuildTable(ReadableFontData data) {
      return new ScriptListTable(data, true);
    }
  }
}

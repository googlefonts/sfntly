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

  public boolean hasScriptTable(ScriptTag scriptTag) {
    return hasScriptTable(scriptTag.tag());
  }

  public boolean hasScriptTable(int scriptTag) {
    return ScriptRecord.createFromScriptTag(
        data, SCRIPT_RECORD_BASE, scriptCount(), scriptTag) != null;
  }

  public int scriptTagAt(int index) {
    ScriptRecord scriptRecord =
        ScriptRecord.createNthFromOffset(data, SCRIPT_RECORD_BASE, index);
    return scriptRecord.scriptTag();
  }

  public ScriptTable scriptTableAt(int index) {
    ScriptRecord scriptRecord =
        ScriptRecord.createNthFromOffset(data, SCRIPT_RECORD_BASE, index);
    return scriptTableForRecord(scriptRecord);
  }

  public ScriptTable scriptTableForTag(ScriptTag scriptTag) {
    return scriptTableForTag(scriptTag.tag());
  }

  public ScriptTable scriptTableForTag(int scriptTag) {
    ScriptRecord scriptRecord = ScriptRecord.createFromScriptTag(
        data, SCRIPT_RECORD_BASE, scriptCount(), scriptTag);
    if (scriptRecord == null) {
      return null;
    }

    scriptRecord = ScriptRecord.createFromOffset(data, scriptRecord.script());
    return scriptTableForRecord(scriptRecord);
  }
  
  @Override
  public Iterator<ScriptTable> iterator() {
    return new Iterator<ScriptTable>() {
      Iterator<ScriptRecord> scriptRecordIterator =
          ScriptRecord.getScriptRecordIterator(
              data, SCRIPT_RECORD_BASE, scriptCount());

      @Override
      public boolean hasNext() {
        return scriptRecordIterator.hasNext();
      }

      @Override
      public ScriptTable next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        ScriptRecord scriptRecord = scriptRecordIterator.next();
        ScriptTable table = scriptTableForRecord(scriptRecord);
        return table;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private ScriptTable scriptTableForRecord(ScriptRecord scriptRecord) {
    int scriptTableBase = scriptRecord.script();
    int scriptTag = scriptRecord.scriptTag();
    ReadableFontData newData = data.slice(scriptTableBase);
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

        // Start of the first subtable in the data, if we're canonical.
        int subTableLimit = ScriptRecord.getBaseFor(SCRIPT_RECORD_BASE, scriptCount);

        if (scriptCount > 0) {
          Iterator<ScriptRecord> scriptRecordIterator =
              ScriptRecord.getScriptRecordIterator(
                  data, SCRIPT_RECORD_BASE, scriptCount());
          if (dataIsCanonical) {
            do {
              // Each table starts where the previous one ended.
              int offset = subTableLimit;
              ScriptRecord scriptRecord = scriptRecordIterator.next();
              int scriptTag = scriptRecord.scriptTag();
              // Each table ends at the next start, or at the end of the data.
              subTableLimit = scriptRecord.script();
              int length = subTableLimit - offset;
              ScriptTable.Builder builder =
                  createScriptTableBuilder(data, offset, length, scriptTag);
              builders.put(scriptTag, builder);
            } while (scriptRecordIterator.hasNext());
          } else {
            do {
              ScriptRecord scriptRecord = scriptRecordIterator.next();
              int offset = scriptRecord.script();
              int scriptTag = scriptRecord.scriptTag();
              ScriptTable.Builder builder =
                  createScriptTableBuilder(data, offset, -1, scriptTag);
              builders.put(scriptTag, builder);
            } while (scriptRecordIterator.hasNext());
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
        len += ScriptRecord.getBaseFor(SCRIPT_RECORD_BASE, count);
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
      int rend = ScriptRecord.getBaseFor(SCRIPT_RECORD_BASE, serializedCount);
      int pos = rend;
      for (ScriptTable.Builder builder : builders.values()) {
        if (builder.serializedLength() > 0) {
          ScriptRecord scriptRecord =
              ScriptRecord.create(builder.scriptTag(), pos);
          rpos += ScriptRecord.write(newData, rpos, scriptRecord);
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

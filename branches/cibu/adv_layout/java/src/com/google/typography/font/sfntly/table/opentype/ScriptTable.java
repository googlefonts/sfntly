// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.RecordsTable.VisibleBuilder;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class ScriptTable extends SubTable implements Iterable<LangSysTable> {
  static final int DEFAULT_LANG_SYS_OFFSET = 0;
  static final int LANG_SYS_COUNT_OFFSET = 2;
  static final int LANG_SYS_RECORD_BASE = 4;
  static final int LANG_SYS_RECORD_LANG_SYS_TAG_OFFSET = 0;
  static final int LANG_SYS_RECORD_LANG_SYS_OFFSET = 4;
  static final int LANG_SYS_RECORD_SIZE = 6;
  static final int DEFAULT_LANG_SYS_TAG = 0;

  private final int scriptTag;
  private boolean dataIsCanonical;

  static ScriptTable create(ReadableFontData data, int scriptTag,
      boolean dataIsCanonical) {
    return new ScriptTable(data, scriptTag, dataIsCanonical);
  }

  private ScriptTable(ReadableFontData data, int scriptTag,
      boolean dataIsCanonical) {
    super(data);
    this.scriptTag = scriptTag;
    this.dataIsCanonical = dataIsCanonical;
  }

  public int scriptTag() {
    return scriptTag;
  }

  boolean dataIsCanonical() {
    return dataIsCanonical;
  }

  static int readDefaultLangSysOffset(ReadableFontData data) {
    if (data == null) {
      return 0;
    }
    return data.readUShort(DEFAULT_LANG_SYS_OFFSET);
  }

  public LangSysTable defaultLangSysTable() {
    int offset = readDefaultLangSysOffset(data);
    if (offset == 0) {
      return null;
    }
    ReadableFontData newData = data.slice(offset);
    return LangSysTable.create(newData, DEFAULT_LANG_SYS_TAG);
  }

  static int readLangSysCount(ReadableFontData data) {
    if (data == null) {
      return 0;
    }
    return data.readUShort(LANG_SYS_COUNT_OFFSET);
  }

  public int langSysCount() {
    return readLangSysCount(data);
  }

  static int langSysRecordBase(int index) {
    return LANG_SYS_RECORD_BASE + index * LANG_SYS_RECORD_SIZE;
  }

  static int readLangSysRecordBaseForTag(ReadableFontData data, int langSysTag) {
    int p = LANG_SYS_RECORD_BASE;
    int e = p + readLangSysCount(data) * LANG_SYS_RECORD_SIZE;
    for (;p < e; p += LANG_SYS_RECORD_SIZE) {
      if (readLangSysTagForRecord(data, p) == langSysTag) {
        return p;
      }
    }
    return 0;
  }

  static int readLangSysTagForRecord(ReadableFontData data, int recordBase) {
    return data.readULongAsInt(recordBase + LANG_SYS_RECORD_LANG_SYS_TAG_OFFSET);
  }

  static int readLangSysBaseForRecord(ReadableFontData data, int recordBase) {
    return data.readUShort(recordBase + LANG_SYS_RECORD_LANG_SYS_OFFSET);
  }

  public int langSysTagAt(int index) {
    return readLangSysTagForRecord(data, langSysRecordBase(index));
  }

  public LangSysTable langSysTableAt(int index) {
    return langSysTableForRecord(langSysRecordBase(index));
  }

  public boolean hasLangSystable(LanguageTag languageTag) {
    return hasLangSysTable(languageTag.tag());
  }

  public boolean hasLangSysTable(int langSysTag) {
    return readLangSysRecordBaseForTag(data, langSysTag) > 0;
  }

  public LangSysTable langSysTableForTag(LanguageTag languageTag) {
    return langSysTableForTag(languageTag.tag());
  }

  public LangSysTable langSysTableForTag(int langSysTag) {
    int recordBase = readLangSysRecordBaseForTag(data, langSysTag);
    return recordBase == 0 ? null : langSysTableForRecord(recordBase);
  }

  @Override
  public Iterator<LangSysTable> iterator() {
    return new Iterator<LangSysTable>() {
      int p = LANG_SYS_RECORD_BASE;
      int e = p + langSysCount() * LANG_SYS_RECORD_SIZE;

      @Override
      public boolean hasNext() {
        return p < e;
      }

      @Override
      public LangSysTable next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        LangSysTable table = langSysTableForRecord(p);
        p += LANG_SYS_RECORD_SIZE;
        return table;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private LangSysTable langSysTableForRecord(int recordBase) {
    int langSysBase = readLangSysBaseForRecord(data, recordBase);
    int langSysTag = readLangSysTagForRecord(data, recordBase);
    ReadableFontData newData = data.slice(langSysBase);
    return LangSysTable.create(newData, langSysTag);
  }

  public static class Builder extends VisibleBuilder<ScriptTable> {
    private final int scriptTag;
    private TreeMap<Integer, LangSysTable.Builder> builders;
    private LangSysTable.Builder defaultBuilder;
    boolean dataIsCanonical;
    private int serializedCount;
    private int serializedLength;

    public Builder(int scriptTag) {
      super(null);
      this.scriptTag = scriptTag;
    }

    public Builder(ReadableFontData data, int scriptTag, boolean dataIsCanonical) {
      super(data);
      this.scriptTag = scriptTag;
      this.dataIsCanonical = dataIsCanonical;
      if (!dataIsCanonical) {
        prepareToEdit();
      }
    }

    public Builder(ScriptTable table) {
      this(table.readFontData(), table.scriptTag, table.dataIsCanonical);
    }

    public Builder(SubTable.Builder<ScriptTable> builder, int scriptTag) {
      super(builder.data());
      this.scriptTag = scriptTag;
      this.dataIsCanonical = true;
    }

    public int scriptTag() {
      return scriptTag;
    }

    public int langSysCount() {
      if (builders == null) {
        return readLangSysCount(internalReadData());
      }
      return builders.size();
    }

    static LangSysTable.Builder createLangSysBuilder(ReadableFontData data,
        int offset, int length, int langSysTag) {
      ReadableFontData newData;
      boolean dataIsCanonical = length >= 0;
      if (dataIsCanonical) {
        newData = data.slice(offset, length);
      } else {
        newData = data.slice(offset);
      }
      return new LangSysTable.Builder(newData, langSysTag, dataIsCanonical);
    }

    private void initFromData(ReadableFontData data) {
      builders = new TreeMap<Integer, LangSysTable.Builder>();
      defaultBuilder = null;
      if (data != null) {
        int langSysCount = readLangSysCount(data);

        int recordBase = LANG_SYS_RECORD_BASE;
        int recordEnd = recordBase + langSysCount * LANG_SYS_RECORD_SIZE;

        // Start of the first subtable in the data, if we're canonical.
        int subTableLimit = recordEnd;

        int offset = readDefaultLangSysOffset(data);
        if (offset != 0) {
          int length = -1; // indicates data is not canonical
          if (dataIsCanonical) {
            // The default LangSysTable data ends either at the first
            // LangSysTable in the index, or at the end of the data.
            if (langSysCount > 0) {
              subTableLimit = readLangSysBaseForRecord(data, LANG_SYS_RECORD_BASE);
            } else {
              subTableLimit = data.length();
            }
            length = subTableLimit - offset;
          }
          defaultBuilder =
              createLangSysBuilder(data, offset, length, DEFAULT_LANG_SYS_TAG);
        }

        if (langSysCount > 0) {
          if (dataIsCanonical) {
            do {
              // Each table starts where the previous one ended.
              offset = subTableLimit;
              int langSysTag = readLangSysTagForRecord(data, recordBase);
              recordBase += LANG_SYS_RECORD_SIZE;
              // Each table ends at the next start, or at the end of the data.
              if (recordBase < recordEnd) {
                subTableLimit = readLangSysBaseForRecord(data, recordBase);
              } else {
                subTableLimit = data.length();
              }
              int length = subTableLimit - offset;
              LangSysTable.Builder builder =
                  createLangSysBuilder(data, offset, length, langSysTag);
              builders.put(langSysTag, builder);
            } while (recordBase < recordEnd);
          } else {
            do {
              offset = readLangSysBaseForRecord(data, recordBase);
              int langSysTag = readLangSysTagForRecord(data, recordBase);
              recordBase += LANG_SYS_RECORD_SIZE;
              LangSysTable.Builder builder =
                  createLangSysBuilder(data, offset, -1, langSysTag);
              builders.put(langSysTag, builder);
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

    public LangSysTable.Builder defaultLangSysTableBuilder() {
      prepareToEdit();
      if (defaultBuilder == null) {
        defaultBuilder = new LangSysTable.Builder(DEFAULT_LANG_SYS_TAG);
      }
      return defaultBuilder;
    }

    /**
     * Returns the builder for the language system specified by the tag, adding a
     * new builder if necessary.
     */
    public LangSysTable.Builder addLangSys(int langSysTag) {
      prepareToEdit();
      LangSysTable.Builder builder = builders.get(langSysTag);
      if (builder == null) {
        builder = new LangSysTable.Builder(langSysTag);
        builders.put(langSysTag, builder);
      }
      return builder;
    }

    /**
     * Return the builder for the language system specified by the tag, or null if
     * the language system is not currently listed.
     */
    public LangSysTable.Builder editLangSys(int langSysTag) {
      prepareToEdit();
      return builders.get(langSysTag);
    }

    /**
     * Remove the language system specified by the tag.
     */
    public void removeLangSys(int langSysTag) {
      prepareToEdit();
      builders.remove(langSysTag);
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
      for (LangSysTable.Builder builder : builders.values()) {
        int sublen = builder.subDataSizeToSerialize();
        if (sublen > 0) {
          ++count;
          len += sublen;
        }
      }
      if (len > 0) {
        len += LANG_SYS_RECORD_BASE + count * LANG_SYS_RECORD_SIZE;
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
        count = readLangSysCount(data);
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

    int serializedLength() {
      return serializedLength;
    }

    private int serializeFromBuilders(WritableFontData newData) {
      // The canonical form of the data consists of the header,
      // the index, the default langSysTable if present, then the
      // langSysTables from the index in index order.  All
      // LangSysTable are distinct; there's no sharing of tables.

      int rpos = LANG_SYS_RECORD_BASE;
      int rend = rpos + serializedCount * LANG_SYS_RECORD_SIZE;
      int pos = rend;
      if (defaultBuilder != null && defaultBuilder.serializedLength() > 0) {
        newData.writeUShort(DEFAULT_LANG_SYS_OFFSET, pos);
        pos += defaultBuilder.subSerialize(newData.slice(pos));
      } else {
        newData.writeUShort(DEFAULT_LANG_SYS_OFFSET, 0);
      }
      for (LangSysTable.Builder builder : builders.values()) {
        if (builder.serializedLength() > 0) {
          newData.writeULong(rpos + LANG_SYS_RECORD_LANG_SYS_TAG_OFFSET,
              builder.langSysTag());
          newData.writeUShort(rpos + LANG_SYS_RECORD_LANG_SYS_OFFSET, pos);
          rpos += LANG_SYS_RECORD_SIZE;
          pos += builder.subSerialize(newData.slice(pos));
        }
      }
      newData.writeUShort(LANG_SYS_COUNT_OFFSET, serializedCount);

      return pos;
    }

    private int serializeFromData(WritableFontData newData) {
      // The src data must be canonical.
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
    protected ScriptTable subBuildTable(ReadableFontData data) {
      return new ScriptTable(data, scriptTag, true);
    }
  }
}

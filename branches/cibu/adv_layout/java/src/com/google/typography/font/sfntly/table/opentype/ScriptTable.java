package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.component.TagOffsetsTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;

import java.util.HashMap;
import java.util.Map;

public class ScriptTable extends TagOffsetsTable<LangSysTable> {
  public static final int FIELD_COUNT = 1;

  public static final int DEFAULT_LANG_SYS_INDEX = 0;
  public static final int NO_DEFAULT_LANG_SYS = 0;

  public ScriptTable(ReadableFontData data, int base, boolean dataIsCanonical) {
    super(data, base, dataIsCanonical);
  }

  public LangSysTable defaultLangSysTable() {
    int defaultLangSysOffset = getField(DEFAULT_LANG_SYS_INDEX);
    if (defaultLangSysOffset == NO_DEFAULT_LANG_SYS) {
      return null;
    }

    ReadableFontData newData = data.slice(defaultLangSysOffset);
    LangSysTable langSysTable = new LangSysTable(newData, dataIsCanonical);
    return langSysTable;
  }

  public LanguageTag langSysAt(int index) {
    return LanguageTag.fromTag(this.tagAt(index));
  }

  public Map<LanguageTag, LangSysTable> map() {
    Map<LanguageTag, LangSysTable> map = new HashMap<LanguageTag, LangSysTable>();
    map.put(LanguageTag.DFLT, defaultLangSysTable());
    for (int i = 0; i < count(); i++) {
      map.put(langSysAt(i), subTableAt(i));
    }
    return map;
  }

  @Override
  public LangSysTable readSubTable(ReadableFontData data, boolean dataIsCanonical) {
    return new LangSysTable(data, dataIsCanonical);
  }

  @Override
  public int fieldCount() {
    return FIELD_COUNT;
  }

  public static class Builder extends TagOffsetsTable.Builder<ScriptTable, LangSysTable> {
    private VisibleBuilder<LangSysTable> defLangSysBuilder;

    public Builder() {
      super();
    }

    public Builder(ReadableFontData data, int base, boolean dataIsCanonical) {
      super(data, base, dataIsCanonical);
      int defLangSys = getField(DEFAULT_LANG_SYS_INDEX);
      if (defLangSys != NO_DEFAULT_LANG_SYS) {
        defLangSysBuilder = new LangSysTable.Builder(data.slice(defLangSys), dataIsCanonical);
      }
    }

    public Builder(ScriptTable.Builder other) {
      super(other);
      defLangSysBuilder = other.defLangSysBuilder;
    }

    @Override
    public VisibleBuilder<LangSysTable> createSubTableBuilder(
        ReadableFontData data, int tag, boolean dataIsCanonical) {
      return new LangSysTable.Builder(data, dataIsCanonical);
    }

    @Override
    public VisibleBuilder<LangSysTable> createSubTableBuilder() {
      return new LangSysTable.Builder();
    }

    @Override
    protected ScriptTable readTable(ReadableFontData data, int base, boolean dataIsCanonical) {
      return new ScriptTable(data, base, dataIsCanonical);
    }

    @Override
    public int subDataSizeToSerialize() {
      int size = super.subDataSizeToSerialize();
      if (defLangSysBuilder != null) {
        size += defLangSysBuilder.subDataSizeToSerialize();
      }
      return size;
    }

    @Override
    public int subSerialize(WritableFontData newData) {
      int byteCount = super.subSerialize(newData);
      if (defLangSysBuilder != null) {
        byteCount += defLangSysBuilder.subSerialize(newData.slice(byteCount));
      }
      return byteCount;
    }

    @Override
    public void subDataSet() {
      super.subDataSet();
      defLangSysBuilder = null;
    }

    @Override
    public int fieldCount() {
      return FIELD_COUNT;
    }

    @Override
    protected void initFields() {
      setField(DEFAULT_LANG_SYS_INDEX, NO_DEFAULT_LANG_SYS);
    }
  }
}

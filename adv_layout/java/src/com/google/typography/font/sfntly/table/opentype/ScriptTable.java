package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.NumRecord;
import com.google.typography.font.sfntly.table.opentype.component.TagOffsetRecordList;
import com.google.typography.font.sfntly.table.opentype.component.VisibleBuilder;
import com.google.typography.font.sfntly.table.opentype.scripttable.Header;
import com.google.typography.font.sfntly.table.opentype.scripttable.InnerArray;

import java.util.Iterator;

public class ScriptTable extends SubTable implements Iterable<LangSysTable>  {
  private final NumRecord header;
  private final InnerArray array;
  public final boolean dataIsCanonical;

  ////////////////
  // Constructors

  public ScriptTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data);
    this.dataIsCanonical = dataIsCanonical;
    header = new NumRecord(data, Header.Builder.DEFAULT_LANG_SYS_OFFSET);
    array = new InnerArray(data, NumRecord.RECORD_SIZE, dataIsCanonical);
  }

  ////////////////////////////////////
  // Methods redirected to the array

  public TagOffsetRecordList recordList() {
    return array.recordList;
  }

  public LangSysTable subTableAt(int index) {
    return array.subTableAt(index);
  }

  public LangSysTable subTableForTag(int tag) {
    return array.subTableForTag(tag);
  }

  @Override
  public Iterator<LangSysTable> iterator() {
    return array.iterator();
  }

  protected LangSysTable createSubTable(
      ReadableFontData data, boolean dataIsCanonical) {
    return array.readSubTable(data, dataIsCanonical);
  }

  ////////////////////////////////////
  // Methods specific to this class

  public LangSysTable defaultLangSysTable() {
    int offset = header.value;
    if (offset == Header.Builder.DEFAULT_LANG_SYS_TAG) {
      return null;
    }
    
    ReadableFontData newData = data.slice(offset);
    LangSysTable langSysTable = new LangSysTable(newData, dataIsCanonical);
    return langSysTable;
  }

  ////////////////////////////////////
  // Builder

  public static class Builder extends VisibleBuilder<ScriptTable> {

    protected boolean dataIsCanonical;
    private final Header.Builder headerBuilder;
    private final InnerArray.Builder arrayBuilder;

    ////////////////
    // Constructors

    public Builder() {
      super();
      headerBuilder = new Header.Builder();
      arrayBuilder = new InnerArray.Builder();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
      headerBuilder = new Header.Builder(data, dataIsCanonical);
      arrayBuilder = new InnerArray.Builder(
          data, Header.Builder.DEFAULT_LANG_SYS_OFFSET_LENGTH, dataIsCanonical);
    }

    ///////////////////////////////
    // Public methods for builders

    public int subTableCount() {
      return arrayBuilder.subTableCount();
    }

    public SubTable.Builder<? extends SubTable> builderForTag(int tag) {
      setModelChanged();
      return arrayBuilder.builderForTag(tag);
    }

    public VisibleBuilder<LangSysTable> addBuiderForTag(int tag) {
      setModelChanged();
      return arrayBuilder.addBuiderForTag(tag);
    }

    public void removeBuilderForTag(int tag) {
      setModelChanged();
      arrayBuilder.removeBuilderForTag(tag);
    }

    public VisibleBuilder<LangSysTable> buiderForHeader() {
      return headerBuilder.initBuilder();
    }

    /////////////////////////////////
    // Public methods to serialize

    @Override
    public int subDataSizeToSerialize() {
      return headerBuilder.subDataSizeToSerialize() + arrayBuilder.subDataSizeToSerialize();
    }

    @Override
    public int subSerialize(WritableFontData newData) {
      int newOffset = arrayBuilder.subSerialize(newData.slice(Header.Builder.DEFAULT_LANG_SYS_OFFSET_LENGTH));
      return headerBuilder.subSerialize(newData, newOffset);
    }

    /////////////////////////////////////////
    // Protected impls pushed to subclasses

    protected VisibleBuilder<LangSysTable> createSubTableBuilder() {
      return arrayBuilder.createSubTableBuilder();
    }

    protected VisibleBuilder<LangSysTable> createSubTableBuilder(
        ReadableFontData data, int tag, boolean dataIsCanonical) {
      return arrayBuilder.createSubTableBuilder(data, tag, dataIsCanonical);
    }

    protected ScriptTable createTable(ReadableFontData data, boolean dataIsCanonical) {
      return new ScriptTable(data, true);
    }    

    /////////////////////
    // Overriden methods

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    public void subDataSet() {
      arrayBuilder.subDataSet();
    }

    @Override
    public ScriptTable subBuildTable(ReadableFontData data) {
      return createTable(data, true);
    } 
  }
}

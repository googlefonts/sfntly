package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.RecordsTable;
import com.google.typography.font.sfntly.table.opentype.component.RecordsTable.VisibleBuilder;
import com.google.typography.font.sfntly.table.opentype.component.TagOffsetRecordList;

import java.util.Iterator;

public class ScriptTable extends SubTable implements Iterable<LangSysTable>  {
  private final Header header;
  private final ScriptTableArrayPart array;
  public final boolean dataIsCanonical;

  public ScriptTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data);
    this.dataIsCanonical = dataIsCanonical;
    header = new Header(data);
    array = new ScriptTableArrayPart(data, Header.DEFAULT_LANG_SYS_OFFSET_LENGTH, dataIsCanonical);
  }

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
  
  public static class Builder extends VisibleBuilder<ScriptTable> {

    private final ScriptTableArrayPart.Builder arrayBuilder;
    protected boolean dataIsCanonical;

    ////////////////
    // Constructors
    
    public Builder() {
      super();
      arrayBuilder = new ScriptTableArrayPart.Builder();
    }

    public Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = dataIsCanonical;
      if (!dataIsCanonical) {
        prepareToEdit();
      }
      arrayBuilder = new ScriptTableArrayPart.Builder(
          data, Header.DEFAULT_LANG_SYS_OFFSET_LENGTH, dataIsCanonical);
    }

    public Builder(RecordsTable.Builder<ScriptTableArrayPart, LangSysTable> subTableBuilder) {
      super();
      arrayBuilder = new ScriptTableArrayPart.Builder(subTableBuilder);
    }

    ///////////////////////////////
    // Public methods for builders
    
    public int subTableCount() {
      return arrayBuilder.subTableCount();
    }
    
    public
    SubTable.Builder<? extends SubTable> builderForTag(int tag) {
      prepareToEdit();
      return arrayBuilder.builderForTag(tag);
    }
    
    public 
    VisibleBuilder<LangSysTable> addBuiderForTag(int tag) {
      prepareToEdit();
      return arrayBuilder.addBuiderForTag(tag);
    }
    
    public void removeBuilderForTag(int tag) {
      prepareToEdit();
      arrayBuilder.removeBuilderForTag(tag);
    }
    
    public VisibleBuilder<LangSysTable> buiderForHeader() {
      return headerBuilder;
    }
    
    ///////////////////////////////
    // Public methods to serialize
    
    @Override
    public int subDataSizeToSerialize() {
      return subDataSizeToSerializeHeader() + arrayBuilder.subDataSizeToSerialize();
    }
    
    @Override
    public int subSerialize(WritableFontData newData) {
      int newOffset = arrayBuilder.subSerialize(newData.slice(Header.DEFAULT_LANG_SYS_OFFSET_LENGTH));
      return subSerializeHeader(newData, newOffset);
    }

    //////////////////
    // Protected impls
    
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
    // Private methods
    
    private Header header;
    private VisibleBuilder<LangSysTable> headerBuilder;
    private int headerSubSerializeLength;
    
    private void prepareToEdit() {
      if (header == null) {
        initFromData(internalReadData());
        setModelChanged();
      }
    }

    private void initFromData(ReadableFontData data) {
      if (data == null) {
        return;
      }
      header = new Header(data);
      if (header.offset != Header.DEFAULT_LANG_SYS_TAG) {
        headerBuilder = new LangSysTable.Builder(
            data.slice(header.offset), 0 /* dummy */, dataIsCanonical);
      }
    }

    private int subDataSizeToSerializeHeader() {
      if (headerSubSerializeLength == 0) {
        if (headerBuilder == null) {
          ReadableFontData data = internalReadData();
          if (data == null) {
            headerSubSerializeLength = Header.DEFAULT_LANG_SYS_OFFSET_LENGTH;
          } else {
            headerSubSerializeLength = data.length();
          }
        } else {
          headerSubSerializeLength = Header.DEFAULT_LANG_SYS_OFFSET_LENGTH +
              headerBuilder.subDataSizeToSerialize();
        }
      }
      return headerSubSerializeLength;
    }

    private int subSerializeHeader(WritableFontData newData, int subTableOffset) {
      header.writeTo(newData);
      if (headerBuilder != null) {
        subTableOffset += headerBuilder.subSerialize(newData.slice(subTableOffset));
        return subTableOffset;
      }
      return serializeFromData(newData);
    }

    private int serializeFromData(WritableFontData newData) {
      // The source data must be canonical.
      ReadableFontData data = internalReadData();
      data.copyTo(newData);
      return data.length();
    }

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
  
  public LangSysTable defaultLangSysTable() {
    int offset = header.offset;
    if (offset == Header.DEFAULT_LANG_SYS_TAG) {
      return null;
    }
    ReadableFontData newData = data.slice(offset);
    LangSysTable langSysTable = LangSysTable.create(newData, 0 /* dummy */);
    return langSysTable;
  }
  
  public static class Header {
    static final int DEFAULT_LANG_SYS_TAG = 0;
    static final int DEFAULT_LANG_SYS_OFFSET = 0;
    static final int DEFAULT_LANG_SYS_OFFSET_LENGTH = 2;
    public final int offset;
    
    public Header(ReadableFontData data){
      this.offset = data.readUShort(DEFAULT_LANG_SYS_OFFSET);
    }
    
    public Header(int offset){
      this.offset = offset;
    }
     
    public int writeTo(WritableFontData newData) {
      return newData.writeUShort(0, offset);
    }
  }
}

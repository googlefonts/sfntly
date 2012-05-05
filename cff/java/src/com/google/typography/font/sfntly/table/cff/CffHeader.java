/**
 * 
 */
package com.google.typography.font.sfntly.table.cff;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

/**
 * @author stuartg
 *
 */
public class CffHeader extends CffSubTable {

  private static int OFFSET_MAJOR_VERSION = 0;
  private static int OFFSET_MINOR_VERSION = 1;
  private static int OFFSET_HDR_SIZE = 2;
  private static int OFFSET_OFFSET_SIZE = 3;
  
  protected CffHeader(ReadableFontData data) {
    super(data);
  }

  public int majorVersion() {
    return CffHeader.majorVersion(this.data);
  }
  
  public int minorVersion() {
    return CffHeader.minorVersion(this.data);
  }
  
  public int hdrSize() {
    return CffHeader.hdrSize(this.data);
  }
  
  public int offSize() {
    return CffHeader.offSize(this.data);
  }

  private static int majorVersion(ReadableFontData data) {
    return data.readCard8(OFFSET_MAJOR_VERSION);
  }
  
  private static int minorVersion(ReadableFontData data) {
    return data.readCard8(OFFSET_MINOR_VERSION);
  }
  
  static int hdrSize(ReadableFontData data) {
    return data.readCard8(OFFSET_HDR_SIZE);
  }
  
  private static int offSize(ReadableFontData data) {
    return data.readCard8(OFFSET_OFFSET_SIZE);
  }
  
  public static class Builder extends CffSubTable.Builder<CffHeader> {
    
    protected Builder(ReadableFontData data) {
      super(data);
    }
    
    protected Builder(WritableFontData data) {
      super(data);
    }

    public static Builder createBuilder(ReadableFontData data) {
      return new Builder(data);
    }

    public int majorVersion() {
      return CffHeader.majorVersion(this.internalReadData());
    }
    
    public void setMajorVersion(byte major) {
      this.internalWriteData().writeCard8(OFFSET_MAJOR_VERSION, major);
    }
    
    public int minorVersion() {
      return CffHeader.minorVersion(this.internalReadData());
    }
    
    public void setMinorVersion(byte minor) {
      this.internalWriteData().writeCard8(OFFSET_MINOR_VERSION, minor);
    }
    
    public int hdrSize() {
      return CffHeader.hdrSize(this.internalReadData());
    }
    
    public void setHdrSize(byte hdrSize) {
      this.internalWriteData().writeCard8(OFFSET_HDR_SIZE, hdrSize);
    }
    
    public int offSize() {
      return CffHeader.offSize(this.internalReadData());
    }

    public void setOffSize(byte offSize) {
      this.internalWriteData().writeCard8(OFFSET_OFFSET_SIZE, offSize);
    }
    
    @Override
    protected int subSerialize(WritableFontData newData) {
      return this.internalReadData().copyTo(newData);
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    protected int subDataSizeToSerialize() {
      return this.internalReadData().length();
    }

    @Override
    protected void subDataSet() {
      // NOP
    }

    @Override
    protected CffHeader subBuildTable(ReadableFontData data) {
      return new CffHeader(data);
    }
  }
}

/**
 * 
 */
package com.google.typography.font.sfntly.table.cff;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.cff.DictTokenizer.DictEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author stuartg
 *
 */
public class TopDictIndex extends IndexDataSubTable {

  public static int DICT_version            = 0x00;
  public static int DICT_Notice             = 0x01;
  public static int DICT_Copyright          = DictTokenizer.OPERATOR_ESCAPE | 0x00;
  public static int DICT_FullName           = 0x02;
  public static int DICT_FamilyName         = 0x03;
  public static int DICT_Weight             = 0x04;
  public static int DICT_isFixedPitch       = DictTokenizer.OPERATOR_ESCAPE | 0x01;
  public static int DICT_ItalicAngle        = DictTokenizer.OPERATOR_ESCAPE | 0x02;
  public static int DICT_UnderlinePosition  = DictTokenizer.OPERATOR_ESCAPE | 0x03;
  public static int DICT_UnderlineThickness = DictTokenizer.OPERATOR_ESCAPE | 0x04;
  public static int DICT_PaintType          = DictTokenizer.OPERATOR_ESCAPE | 0x05;
  public static int DICT_CharstringType     = DictTokenizer.OPERATOR_ESCAPE | 0x06;
  public static int DICT_FontMatrix         = DictTokenizer.OPERATOR_ESCAPE | 0x07;
  public static int DICT_UniqueID           = 0x0d;
  public static int DICT_FontBBox           = 0x05;
  public static int DICT_StrokeWidth        = DictTokenizer.OPERATOR_ESCAPE | 0x08;
  public static int DICT_XUID               = 0x0e;
  public static int DICT_charset            = 0x0f;
  public static int DICT_Encoding           = 0x10;
  public static int DICT_CharStrings        = 0x11;
  public static int DICT_Private            = 0x12;
  public static int DICT_SyntheticBase      = DictTokenizer.OPERATOR_ESCAPE | 0x14;
  public static int DICT_PostScript         = DictTokenizer.OPERATOR_ESCAPE | 0x15;
  public static int DICT_BaseFontName       = DictTokenizer.OPERATOR_ESCAPE | 0x16;
  public static int DICT_BaseFontBlend      = DictTokenizer.OPERATOR_ESCAPE | 0x17;
  
  public static int DICT_ROS                = DictTokenizer.OPERATOR_ESCAPE | 0x1E;
  public static int DICT_CIDFontVersion     = DictTokenizer.OPERATOR_ESCAPE | 0x1F;
  public static int DICT_CIDFontRevision    = DictTokenizer.OPERATOR_ESCAPE | 0x20;
  public static int DICT_CIDFontType        = DictTokenizer.OPERATOR_ESCAPE | 0x21;
  public static int DICT_CIDCount           = DictTokenizer.OPERATOR_ESCAPE | 0x22;
  public static int DICT_UIDBase            = DictTokenizer.OPERATOR_ESCAPE | 0x23;
  public static int DICT_FDArray            = DictTokenizer.OPERATOR_ESCAPE | 0x24;
  public static int DICT_FDSelect           = DictTokenizer.OPERATOR_ESCAPE | 0x25;
  public static int DICT_FontName           = DictTokenizer.OPERATOR_ESCAPE | 0x26;

  
  private static final class TopDictComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer o1, Integer o2) {
      if (o1 == DICT_ROS) {
        return -1;
      }
      if (o2 == DICT_ROS) {
        return 1;
      }
      
      if (o1 == DICT_SyntheticBase) {
        return -1;
      }
      if (o2 == DICT_SyntheticBase) {
        return 1;
      }
      
      return o1 - o2;
    }   
  }
  
  private static final TopDictComparator comparator = new TopDictComparator();
  
  private DictDataSubTable topDict;
  
  /**
   * @param data
   */
  private TopDictIndex(ReadableFontData data, DictDataSubTable topDict) {
    super(data);
    this.topDict = topDict;
  }

  public DictEntry dictEntry(int operator) {
    return this.topDict.entry(operator);
  }
  
  public Integer versionSID() {
    return this.topDict.sidValue(DICT_version, null);
  }
  
  public Integer noticeSID() {
    return this.topDict.sidValue(DICT_Notice, null);
  }
  
  public Integer copyrightSID() {
    return this.topDict.sidValue(DICT_Copyright, null);
  }
  
  public Integer fullNameSID() {
    return this.topDict.sidValue(DICT_FullName, null);
  }
  
  public Integer familyNameSID() {
    return this.topDict.sidValue(DICT_FamilyName, null);
  }
  
  public Integer weightSID() {
    return this.topDict.sidValue(DICT_Weight, null);
  }
  
  public Boolean isFixedPitch() {
    return this.topDict.booleanValue(DICT_isFixedPitch, Boolean.FALSE);
  }
  
  public Number italicAngle() {
    return this.topDict.numberValue(DICT_ItalicAngle, new Integer(0));
  }
  
  public Number underlinePosition() {
    return this.topDict.numberValue(DICT_UnderlinePosition, new Integer(-100));
  }
  
  public Number underlineThickness() {
    return this.topDict.numberValue(DICT_UnderlineThickness, new Integer(50));
  }
  
  public Number paintType() {
    return this.topDict.numberValue(DICT_PaintType, new Integer(0));
  }
  
  public Number charstringType() {
    return this.topDict.numberValue(DICT_CharstringType, new Integer(2));
  }
  
  public float[] fontMatrix() {
    return this.topDict.arrayValue(DICT_FontMatrix, new float[] {0.001f, 0, 0, 0.001f, 0, 0});
  }
  
  public Number uniqueID() {
    return this.topDict.numberValue(DICT_UniqueID, null);
  }
  
  public float[] fontBBox() {
    return this.topDict.arrayValue(DICT_FontMatrix, new float[] {0, 0, 0, 0});
  }
  
  public Number strokeWidth() {
    return this.topDict.numberValue(DICT_StrokeWidth, new Integer(0));
  }
  
  public float[] xuid() {
    return this.topDict.arrayValue(DICT_FontMatrix, null);
  }
  
  public Number charset() {
    return this.topDict.numberValue(DICT_charset, new Integer(0));
  }
  
  public Number encoding() {
    return this.topDict.numberValue(DICT_Encoding, new Integer(0));
  }
  
  public float[] charStrings() {
    return this.topDict.arrayValue(DICT_Private, null);
  }
  
  public Number syntheticBase() {
    return this.topDict.numberValue(DICT_SyntheticBase, null);
  }
  
  public Number postscriptSID() {
    return this.topDict.sidValue(DICT_PostScript, null);
  }
  
  public Number baseFontNameSID() {
    return this.topDict.sidValue(DICT_BaseFontName, null);
  }
  
  public float[] baseFontBlend() {
    return this.topDict.deltaValue(DICT_BaseFontBlend, null);
  }
  
  public DictDataSubTable toDict() {
    return this.topDict;
  }
  
  public static class Builder extends IndexDataSubTable.Builder<TopDictIndex, GenericDict.Builder> {
    // TODO(stuartg): make lazy
    //GenericDict.Builder topDictBuilder;
    
    public static Builder createBuilder(ReadableFontData data) {
      return new Builder(data);
    }
    
    protected Builder(ReadableFontData data) {
      super(data);
    }
    
    protected Builder(WritableFontData data) {
      super(data);
    }
    
    @Override
    protected List<GenericDict.Builder> createElements() {
      List<GenericDict.Builder> elements = new ArrayList<GenericDict.Builder>();
      elements.add(GenericDict.Builder.createBuilder(
          IndexDataSubTable.elementSlice(this.internalReadData(), 0), TopDictIndex.comparator));      
      return elements;
    }

    @Override
    protected int serializeElement(GenericDict.Builder element, WritableFontData data) {
      return element.subSerialize(data);
    }

    @Override
    protected int sizeToSerializeElement(GenericDict.Builder element) {
      return element.subDataSizeToSerialize();
    }

    public DictEntry dictEntry(int operator) {
      return this.getElementBuilders().get(0).entry(operator);
    }
    
    private GenericDict.Builder getTopDict() {
      GenericDict.Builder topDict = this.getElementBuilders().get(0);
      // TODO(stuartg): create TopDict if null
      
      return topDict;
    }
    
    public DictEntry setDictEntry(DictEntry entry) {
      DictEntry oldEntry = this.getTopDict().setEntry(entry);
      this.setModelChanged();
      return oldEntry;
    }
    
    public DictEntry removedDictEntry(int key) {
      DictEntry oldEntry = this.getTopDict().removeEntry(key);
      this.setModelChanged();
      return oldEntry;
    }
    
    public Number versionSID() {
      return this.getTopDict().sidValue(DICT_version, null);
    }
    
    public Number noticeSID() {
      return this.getTopDict().sidValue(DICT_Notice, null);
    }
    
    public Number copyrightSID() {
      return this.getTopDict().sidValue(DICT_Copyright, null);
    }
    
    public Number fullNameSID() {
      return this.getTopDict().sidValue(DICT_FullName, null);
    }
    
    public Number familyNameSID() {
      return this.getTopDict().sidValue(DICT_FamilyName, null);
    }
    
    public Number weightSID() {
      return this.getTopDict().sidValue(DICT_Weight, null);
    }
    
    public Boolean isFixedPitch() {
      return this.getTopDict().booleanValue(DICT_isFixedPitch, false);
    }
    
    public Number italicAngle() {
      return this.getTopDict().floatValue(DICT_ItalicAngle, new Integer(0));
    }
    
    public Number underlinePosition() {
      return this.getTopDict().numberValue(DICT_UnderlinePosition, new Integer(-100));
    }
    
    public Number underlineThickness() {
      return this.getTopDict().numberValue(DICT_UnderlineThickness, new Integer(50));
    }
    
    public Number paintType() {
      return this.getTopDict().numberValue(DICT_PaintType, new Integer(0));
    }
    
    public Number charstringType() {
      return this.getTopDict().numberValue(DICT_CharstringType, new Integer(2));
    }
    
    public float[] fontMatrix() {
      return this.getTopDict().arrayValue(DICT_FontMatrix, new float[] {0.001f, 0, 0, 0.001f, 0, 0});
    }
    
    public Number uniqueID() {
      return this.getTopDict().numberValue(DICT_UniqueID, null);
    }
    
    public float[] fontBBox() {
      return this.getTopDict().arrayValue(DICT_FontMatrix, new float[] {0, 0, 0, 0});
    }
    
    public Number strokeWidth() {
      return this.getTopDict().numberValue(DICT_StrokeWidth, new Integer(0));
    }
    
    public float[] xuid() {
      return this.getTopDict().arrayValue(DICT_FontMatrix, null);
    }
    
    public Number charset() {
      return this.getTopDict().numberValue(DICT_charset, new Integer(0));
    }
    
    public Number encoding() {
      return this.getTopDict().numberValue(DICT_Encoding, new Integer(0));
    }
    
    public Number charStrings() {
      return this.getTopDict().numberValue(DICT_CharStrings, null);
    }
    
    public Number syntheticBase() {
      return this.getTopDict().numberValue(DICT_SyntheticBase, null);
    }
    
    public Number postscriptSID() {
      return this.getTopDict().sidValue(DICT_PostScript, null);
    }
    
    public Number baseFontNameSID() {
      return this.getTopDict().sidValue(DICT_BaseFontName, null);
    }
    
    public float[] baseFontBlend() {
      return this.getTopDict().deltaValue(DICT_BaseFontBlend, null);
    }
    
    public String toString() {
      return this.getTopDict().toString();
    }
    
    @Override
    protected TopDictIndex subBuildTable(ReadableFontData data) {
      DictDataSubTable topDict = this.getTopDict().build();
      return new TopDictIndex(data, topDict);
    }
  }
}

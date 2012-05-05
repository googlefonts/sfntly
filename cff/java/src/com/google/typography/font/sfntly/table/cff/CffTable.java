/**
 * 
 */
package com.google.typography.font.sfntly.table.cff;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.cff.DictTokenizer.DictEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author stuartg
 *
 */
public class CffTable extends Table {

  /*
   * ID tags used to access the CFF table pieces in the Map.
   */
  private static Integer CFF_HEADER_ID = new Integer(1);
  private static Integer CFF_NAME_INDEX_ID = new Integer(2);
  private static Integer CFF_TOP_DICT_INDEX_ID = new Integer(3);
  private static Integer CFF_STRING_INDEX_ID = new Integer(4);
  private static Integer CFF_GLOBAL_SUBR_INDEX_ID = new Integer(5);
  private static Integer CFF_ENCODINGS_ID = new Integer(6);
  private static Integer CFF_CHARSETS_ID = new Integer(7);
  private static Integer CFF_FDSELECT_ID = new Integer(8);
  private static Integer CFF_CHARSTRINGS_INDEX_ID = new Integer(9);
  private static Integer CFF_FONT_DICT_INDEX_ID = new Integer(10);
  private static Integer CFF_PRIVATE_DICT_ID = new Integer(11);
  private static Integer CFF_LOCAL_SUBR_INDEX_ID = new Integer(12);
  private static Integer CFF_NOTICES_ID = new Integer(13);

  private Map<Integer, CffSubTable> cffPieces;

  /**
   * @param header
   * @param data
   */
  private CffTable(Header header, ReadableFontData data) {
    super(header, data);
  }

  private Map<Integer, CffSubTable> getCffPieces() {
    if (this.cffPieces == null) {
      Map<Integer, CffSubTable.Builder<? extends CffSubTable>> cffPiecesBuilders = 
          Builder.parse(this.data);
      this.cffPieces = new HashMap<Integer, CffSubTable>();
      for (Map.Entry<Integer, CffSubTable.Builder<? extends CffSubTable>> entry : 
        cffPiecesBuilders.entrySet()) {
        //System.out.println(entry);
        CffSubTable subTable = entry.getValue().build();
        this.cffPieces.put(entry.getKey(), subTable);
      }
    }
    return this.cffPieces;
  }

  public GenericIndex nameIndex() {
    return (GenericIndex) this.getCffPieces().get(CFF_NAME_INDEX_ID);
  }

  public TopDictIndex topDict() {
    return (TopDictIndex) this.getCffPieces().get(CFF_TOP_DICT_INDEX_ID);
  }

  public GenericIndex stringIndex() {
    return (GenericIndex) this.getCffPieces().get(CFF_STRING_INDEX_ID);
  }

  public GenericIndex globalSubrIndex() {
    return (GenericIndex) this.getCffPieces().get(CFF_GLOBAL_SUBR_INDEX_ID);
  }

  public CharStringsIndex charStringsIndex() {
    return (CharStringsIndex) this.getCffPieces().get(CFF_CHARSTRINGS_INDEX_ID);
  }    

  static boolean validateOffSize(int offSize) {
    if (offSize >= 1 && offSize <=4) {
      return true;
    }
    return false;
  }

  static void validateOffSizeException(int offSize) {
    if (offSize >= 1 && offSize <=4) {
      return;
    }
    throw new IllegalArgumentException("Offset Size Must be between 1 and 4 inclusive.");
  }

  //  public int majorVersion() {
  //    
  //  }
  //  
  //  public int minorVersion() {
  //    
  //  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append("\r");
    for (CffSubTable sub : this.getCffPieces().values()) {
      sb.append("\t");
      sb.append(sub);
      sb.append("\r");
    }
    return sb.toString();
  }

  public static class Builder extends Table.Builder<CffTable> {
    private Map<Integer, CffSubTable.Builder<? extends CffSubTable>> cffPieces;

    public static Builder createBuilder(Header header, ReadableFontData data) {
      return new Builder(header, data);
    }

    public Builder(Header header, ReadableFontData data) {
      super(header, data);
    }

    public Builder(Header header, WritableFontData data) {
      super(header, data);
    }

    protected Builder(Header header) {
      super(header);
    }

    private Map<Integer, ? extends CffSubTable.Builder<? extends CffSubTable>> getCffPieces() {
      if (this.cffPieces == null) {
        this.cffPieces = parse(this.internalReadData());
        this.setModelChanged();
      }
      return this.cffPieces;
    }

    // TODO(stuartg): for testing - make private
    private static Map<Integer, CffSubTable.Builder<? extends CffSubTable>> 
    parse(ReadableFontData data) {
      Map<Integer, CffSubTable.Builder<? extends CffSubTable>> cffPieces = 
          new HashMap<Integer, CffSubTable.Builder<? extends CffSubTable>>();
      int walkingSize = 0;

      // temporary cff pieces
      TopDictIndex.Builder topDictIndex;

      // Start of Packed Ordered Data
      //System.out.println("1 walkingSize = " + walkingSize);

      // Header
      {
        ReadableFontData subData = data.slice(0);
        int subSize = CffHeader.hdrSize(subData);
        subData.bound(0, subSize);
        CffHeader.Builder cffHeader = CffHeader.Builder.createBuilder(subData);
        cffPieces.put(CFF_HEADER_ID, cffHeader);
        walkingSize += subSize;
      }
      //System.out.println("2 walkingSize = " + walkingSize);

      // Name Index
      {
        ReadableFontData subData = data.slice(walkingSize);
        int subSize = IndexDataSubTable.indexTableSize(subData);
        subData.bound(0, subSize);
        GenericIndex.Builder nameIndex = GenericIndex.Builder.createBuilder(subData);
        cffPieces.put(CFF_NAME_INDEX_ID, nameIndex);
        walkingSize += subSize;
      }
      //System.out.println("3 walkingSize = " + walkingSize);

      // Top Dict Index
      {
        ReadableFontData subData = data.slice(walkingSize);
        int subSize = IndexDataSubTable.indexTableSize(subData);
        subData.bound(0, subSize);
        topDictIndex = TopDictIndex.Builder.createBuilder(subData);
        cffPieces.put(CFF_TOP_DICT_INDEX_ID, topDictIndex);
        walkingSize += subSize;

        //System.out.println(topDictIndex);
      }
      //System.out.println("4 walkingSize = " + walkingSize);

      // String Index
      {
        ReadableFontData subData = data.slice(walkingSize);
        int subSize = IndexDataSubTable.indexTableSize(subData);
        subData.bound(0, subSize);
        GenericIndex.Builder stringIndex = GenericIndex.Builder.createBuilder(subData);
        cffPieces.put(CFF_STRING_INDEX_ID, stringIndex);
        walkingSize += subSize;
      }
      //System.out.println("5 walkingSize = " + walkingSize);

      // Global Subr Index
      {
        ReadableFontData subData = data.slice(walkingSize);
        int subSize = IndexDataSubTable.indexTableSize(subData);
        subData.bound(0, subSize);
        GenericIndex.Builder globalSubrIndex = GenericIndex.Builder.createBuilder(subData);
        cffPieces.put(CFF_GLOBAL_SUBR_INDEX_ID, globalSubrIndex);
        walkingSize += subSize;
      }
      //System.out.println("6 walkingSize = " + walkingSize);

      // End of packed ordered data
      // TODO(stuartg): validate and flag for missing pieces

      // Encodings
      {
        Number rawSubOffset = topDictIndex.encoding();
        if (rawSubOffset != null) {
          int subOffset = rawSubOffset.intValue();
          if (subOffset != 0) {
            ReadableFontData subData = data.slice(subOffset);
            int subSize = Encoding.encodingLength(subData);
            subData.bound(0, subSize);
            Encoding.Builder encoding = Encoding.Builder.createBuilder(subData);
            cffPieces.put(CFF_ENCODINGS_ID, encoding);
          }
        }
      }

      // Charsets
      // TODO(stuartg): decide if needed or not 
      // - difficult to calculate length
      // - must parse all the data
      //      {
      //        Number rawSubOffset = this.topDictIndex.charset();
      //        if (rawSubOffset == null) {
      //          // TODO(stuartg): handle error condition
      //        }
      //        int subOffset = rawSubOffset.intValue();
      //        ReadableFontData subData = data.slice(subOffset);
      //        int subSize = Charset.charsetLength(subData);
      //        subData.bound(0, subSize);        
      //        this.charset = Charset.Builder.createBuilder(subData);  
      //      }

      // FDSelect - only CIDFont

      // CharStrings Index
      {
        Number rawSubOffset = topDictIndex.charStrings();
        if (rawSubOffset != null) {
          int subOffset = rawSubOffset.intValue();
          if (subOffset != 0) {
            ReadableFontData subData = data.slice(subOffset);
            int subSize = IndexDataSubTable.indexTableSize(subData);
            subData.bound(0, subSize);
            CharStringsIndex.Builder charStrings = CharStringsIndex.Builder.createBuilder(subData);
            cffPieces.put(CFF_CHARSTRINGS_INDEX_ID, charStrings);
          }
        }
      }

      // Font Dict Index

      // Local Subr Index

      // Copyright and Trademark Notices

      return cffPieces;
    }

    public CffHeader.Builder cffHeader() {
      return (CffHeader.Builder) this.getCffPieces().get(CFF_HEADER_ID);      
    }

    public GenericIndex.Builder nameIndex() {
      return (GenericIndex.Builder) this.getCffPieces().get(CFF_NAME_INDEX_ID);
    }

    public TopDictIndex.Builder topDict() {
      return (TopDictIndex.Builder) this.getCffPieces().get(CFF_TOP_DICT_INDEX_ID);
    }

    public GenericIndex.Builder stringIndex() {
      return (GenericIndex.Builder) this.getCffPieces().get(CFF_STRING_INDEX_ID);
    }

    public GenericIndex.Builder globalSubrIndex() {
      return (GenericIndex.Builder) this.getCffPieces().get(CFF_GLOBAL_SUBR_INDEX_ID);
    }

    public CharStringsIndex.Builder charStringsIndex() {
      return (CharStringsIndex.Builder) this.getCffPieces().get(CFF_CHARSTRINGS_INDEX_ID);
    }    

    @Override
    protected CffTable subBuildTable(ReadableFontData data) {
      CffTable cff = new CffTable(this.header(), data);
      return cff;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      int size = 0;
      int topDictOffset = -1;
      int topDictLength = -1;
      int charsetsOffset = -1;
      int encodingsOffset = -1;
      int charstringsOffset = -1;
      int privateDictOffset = -1;

      Set<Integer> piecesToSerialize = new HashSet<Integer>(this.getCffPieces().keySet());
      System.out.printf("1 subSerialize(): size = 0x%x - %d%n", size, size);
      {
        CffSubTable.Builder<? extends CffSubTable> table = 
            this.getCffPieces().get(CFF_HEADER_ID);
        if (table != null) {
          // TODO(stuartg): what if it is null - build one?
          piecesToSerialize.remove(CFF_HEADER_ID);
          WritableFontData subData = newData.slice(size);
          size += table.subSerialize(subData);
        }
      }
      System.out.printf("2 - header subSerialize(): size = 0x%x - %d%n", size, size);

      {
        CffSubTable.Builder<? extends CffSubTable> table = 
            this.getCffPieces().get(CFF_NAME_INDEX_ID);
        if (table != null) {
          piecesToSerialize.remove(CFF_NAME_INDEX_ID);
          WritableFontData subData = newData.slice(size);
          size += table.subSerialize(subData);
        }
      }
      System.out.printf("3 - name index subSerialize(): size = 0x%x - %d%n", size, size);

      {
        // first write to know the size and to reserve the space
        CffSubTable.Builder<? extends CffSubTable> table = 
            this.getCffPieces().get(CFF_TOP_DICT_INDEX_ID);
        if (table != null) {
          topDictOffset = size;
          piecesToSerialize.remove(CFF_TOP_DICT_INDEX_ID);
          WritableFontData subData = newData.slice(size);
          topDictLength = table.subSerialize(subData);
          size += topDictLength;
        }
      }
      System.out.printf("4 - top dict subSerialize(): size = 0x%x - %d%n", size, size);

      {
        CffSubTable.Builder<? extends CffSubTable> table = 
            this.getCffPieces().get(CFF_STRING_INDEX_ID);
        if (table != null) {
          piecesToSerialize.remove(CFF_STRING_INDEX_ID);
          WritableFontData subData = newData.slice(size);
          size += table.subSerialize(subData);
        }
      }
      System.out.printf("5 - string index subSerialize(): size = 0x%x - %d%n", size, size);

      {
        CffSubTable.Builder<? extends CffSubTable> table = 
            this.getCffPieces().get(CFF_GLOBAL_SUBR_INDEX_ID);
        if (table != null) {
          piecesToSerialize.remove(CFF_GLOBAL_SUBR_INDEX_ID);
          WritableFontData subData = newData.slice(size);
          size += table.subSerialize(subData);
        }
      }
      System.out.printf("6 - global subr subSerialize(): size = 0x%x - %d%n", size, size);

      for (Integer pieceId : piecesToSerialize)
      {
        CffSubTable.Builder<? extends CffSubTable> table = 
            this.getCffPieces().get(pieceId);
        if (table != null) {
          if (pieceId == CFF_CHARSETS_ID) {
            charsetsOffset = size;
          } else if (pieceId == CFF_ENCODINGS_ID) {
            encodingsOffset = size;
          } else if (pieceId == CFF_CHARSTRINGS_INDEX_ID) {
            charstringsOffset = size;
          } else if (pieceId == CFF_PRIVATE_DICT_ID) {
            privateDictOffset = size;
          }
          WritableFontData subData = newData.slice(size);
          size += table.subSerialize(subData);
          System.out.printf("pieceId=%d, subSerialize(): size = 0x%x - %d%n", pieceId, size, size);
        }
      }

      {
        // second write to hold the correct offsets
        CffSubTable.Builder<? extends CffSubTable> table = 
            this.getCffPieces().get(CFF_TOP_DICT_INDEX_ID);
        if (table != null) {
          TopDictIndex.Builder tdb = (TopDictIndex.Builder) table;
          if (charsetsOffset != -1) {
            setAndForceFullSizeTopDictEntry(tdb, null, TopDictIndex.DICT_charset, charsetsOffset);
          }
          if (encodingsOffset != -1) {
            setAndForceFullSizeTopDictEntry(tdb, null, TopDictIndex.DICT_Encoding, encodingsOffset);
          }
          if (charstringsOffset != -1) {
            setAndForceFullSizeTopDictEntry(tdb, null, TopDictIndex.DICT_CharStrings, charstringsOffset);
          }
          if (privateDictOffset != -1) {
            setAndForceFullSizeTopDictEntry(tdb, null, TopDictIndex.DICT_Private, privateDictOffset);
          }
          WritableFontData subData = newData.slice(topDictOffset, topDictLength);
          tdb.subSerialize(subData);
        }
      }      

      return size;
    }

    @Override
    protected boolean subReadyToSerialize() {
      if (!this.changed()) {
        return true;
      }
      // TODO(stuartg): check for existence of required tables

      for (SubTable.Builder<? extends SubTable> b : this.getCffPieces().values()) {
        if (!b.readyToBuild()) {
          return false;
        }
      }
      return true;
    }

    private boolean setAndForceFullSizeTopDictEntry(
        TopDictIndex.Builder topDictBuilder, Integer pieceTag, Integer operator, int value) {
      if (pieceTag == null || this.getCffPieces().containsKey(pieceTag)) {
        DictEntry.Builder deb = DictEntry.Builder.createBuilder(operator, value);
        deb.forceFullSizeIntegerEncoding();
        topDictBuilder.setDictEntry(deb.build());
        return true;
      }
      return false;
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (!this.changed()) {
        return this.internalReadData().length();
      }
      int size = 0;
      boolean variable = false;
      System.out.printf("**** subDataSizeToSerialize()%n");
      //for (CffSubTable.Builder<? extends CffSubTable> b : this.getCffPieces().values()) {
      for (Integer id : this.getCffPieces().keySet()) {
        CffSubTable.Builder<? extends CffSubTable> b = this.getCffPieces().get(id);
        if (b instanceof TopDictIndex.Builder) {
          TopDictIndex.Builder tdb = (TopDictIndex.Builder) b;

          // fluff up the top dict to include space for the offsets to other tables
          setAndForceFullSizeTopDictEntry(
              tdb, CFF_CHARSETS_ID, TopDictIndex.DICT_charset, 0);
          setAndForceFullSizeTopDictEntry(
              tdb, CFF_ENCODINGS_ID, TopDictIndex.DICT_Encoding, 0);
          setAndForceFullSizeTopDictEntry(
              tdb, CFF_CHARSTRINGS_INDEX_ID, TopDictIndex.DICT_CharStrings, 0);
          setAndForceFullSizeTopDictEntry(
              tdb, CFF_PRIVATE_DICT_ID, TopDictIndex.DICT_Private, 0);
          
          // remove any pieces we don't want
          // TODO(stuartg): temporary only - need to handle CID fonts
//          tdb.removedDictEntry(TopDictIndex.DICT_ROS);
//          tdb.removedDictEntry(TopDictIndex.DICT_CIDFontVersion);
//          tdb.removedDictEntry(TopDictIndex.DICT_CIDFontRevision);
//          tdb.removedDictEntry(TopDictIndex.DICT_CIDFontType);
//          tdb.removedDictEntry(TopDictIndex.DICT_CIDCount);
//          tdb.removedDictEntry(TopDictIndex.DICT_UIDBase);
//          tdb.removedDictEntry(TopDictIndex.DICT_FDArray);
//          tdb.removedDictEntry(TopDictIndex.DICT_FDSelect);
//          tdb.removedDictEntry(TopDictIndex.DICT_FontName);
        }
        int pieceSize = b.subDataSizeToSerialize();
        variable |= pieceSize <= 0;
        size += Math.abs(pieceSize);
        System.out.printf("id=%d, pieceSize=0x%x, variable=%b, size=%x%n", id, pieceSize, variable, size);
      }
      return variable ? -size : size;
    }

    @Override
    protected void subDataSet() {
      this.cffPieces = null;
    }

    // TODO(stuartg): remove this method - only for testing
    public void makeDirty() {
      this.setModelChanged();
    }
  }
}

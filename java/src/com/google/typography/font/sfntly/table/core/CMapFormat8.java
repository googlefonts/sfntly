package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.core.CMapTable.CMapId;
import java.util.Iterator;

/**
 * The cmap format 8 subtable maps 16-bit and 32-bit character codes to 32-bit glyph IDs.
 *
 * @see "ISO/IEC 14496-22:2015, section 5.2.1.3.5"
 */
public final class CMapFormat8 extends CMap {
  private final int numberOfGroups;

  private interface Header {
    int format = 0;
    int length = 4;
    int language = 8;
    int is32 = 12;
    int nGroups = 8204;
    int SIZE = 8208;
  }

  private interface Group {
    int startCharCode = 0;
    int endCharCode = 4;
    int startGlyphId = 8;
    int SIZE = 12;
  }

  protected CMapFormat8(ReadableFontData data, CMapId cmapId) {
    super(data, CMapFormat.Format8.value, cmapId);
    this.numberOfGroups = this.data.readULongAsInt(Header.nGroups);
  }

  private int firstChar(int groupIndex) {
    return this.readFontData()
        .readULongAsInt(Header.SIZE + groupIndex * Group.SIZE + Group.startCharCode);
  }

  private int endChar(int groupIndex) {
    return this.readFontData()
        .readULongAsInt(Header.SIZE + groupIndex * Group.SIZE + Group.endCharCode);
  }

  @Override
  public int glyphId(int character) {
    return this.readFontData()
        .searchULong(
            Header.SIZE + Group.startCharCode,
            Group.SIZE,
            Header.SIZE + Group.endCharCode,
            Group.SIZE,
            numberOfGroups,
            character);
  }

  @Override
  public int language() {
    return this.data.readULongAsInt(Header.language);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new CharacterIterator();
  }

  private class CharacterIterator extends CMap.CharacterRangesIterator {
    CharacterIterator() {
      super(CMapFormat8.this.numberOfGroups);
    }

    @Override
    protected int getRangeStart(int rangeIndex) {
      return CMapFormat8.this.firstChar(rangeIndex);
    }

    @Override
    protected int getRangeEnd(int rangeIndex) {
      return CMapFormat8.this.endChar(rangeIndex);
    }
  }

  public static class Builder extends CMap.Builder<CMapFormat8> {
    protected Builder(WritableFontData data, int offset, CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMapFormat.Format8,
          cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMapFormat.Format8,
          cmapId);
    }

    @Override
    protected CMapFormat8 subBuildTable(ReadableFontData data) {
      return new CMapFormat8(data, this.cmapId());
    }
  }
}

package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import java.util.Iterator;

/**
 * The cmap format 12 subtable maps segmented ranges of 32-bit character codes to 32-bit glyph IDs.
 *
 * @see "ISO/IEC 14496-22:2015, section 5.2.1.3.7"
 */
public final class CMapFormat12 extends CMap {
  private final int numberOfGroups;

  private interface Header {
    int format = 0;
    int length = 4;
    int language = 8;
    int nGroups = 12;
    int groups = 16;
  }

  private interface Group {
    int startCharCode = 0;
    int endCharCode = 4;
    int startGlyphId = 8;
    int SIZE = 12;
  }

  protected CMapFormat12(ReadableFontData data, CMapTable.CMapId cmapId) {
    super(data, CMap.CMapFormat.Format12.value, cmapId);
    this.numberOfGroups = this.data.readULongAsInt(Header.nGroups);
  }

  private int groupStartChar(int groupIndex) {
    return data.readULongAsInt(Header.groups + groupIndex * Group.SIZE + Group.startCharCode);
  }

  private int groupEndChar(int groupIndex) {
    return data.readULongAsInt(Header.groups + groupIndex * Group.SIZE + Group.endCharCode);
  }

  private int groupStartGlyph(int groupIndex) {
    return data.readULongAsInt(Header.groups + groupIndex * Group.SIZE + Group.startGlyphId);
  }

  @Override
  public int glyphId(int character) {
    int group =
        data.searchULong(
            Header.groups + Group.startCharCode,
            Group.SIZE,
            Header.groups + Group.endCharCode,
            Group.SIZE,
            numberOfGroups,
            character);
    if (group == -1) {
      return CMapTable.NOTDEF;
    }
    return groupStartGlyph(group) + (character - groupStartChar(group));
  }

  @Override
  public int language() {
    return data.readULongAsInt(Header.language);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new CharacterIterator();
  }

  private final class CharacterIterator extends CMap.CharacterRangesIterator {
    CharacterIterator() {
      super(numberOfGroups);
    }

    @Override
    protected int getRangeStart(int rangeIndex) {
      return groupStartChar(rangeIndex);
    }

    @Override
    protected int getRangeEnd(int rangeIndex) {
      return groupEndChar(rangeIndex);
    }
  }

  public static class Builder extends CMap.Builder<CMapFormat12> {
    protected Builder(WritableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMap.CMapFormat.Format12,
          cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMap.CMapFormat.Format12,
          cmapId);
    }

    @Override
    protected CMapFormat12 subBuildTable(ReadableFontData data) {
      return new CMapFormat12(data, cmapId());
    }
  }
}

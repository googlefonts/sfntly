package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import java.util.Iterator;

/**
 * The cmap format 13 subtable maps ranges of 32-bit character codes to one glyph ID each.
 *
 * @see "ISO/IEC 14496-22:2015, section 5.2.1.3.8"
 */
public final class CMapFormat13 extends CMap {
  private final int numberOfGroups;

  private interface Header {
    int format = 0;
    int length = 4;
    int language = 8;
    int nGroups = 12;
    int SIZE = 16;
  }

  private interface Group {
    int startCharCode = 0;
    int endCharCode = 4;
    int glyphId = 8;
    int SIZE = 12;
  }

  protected CMapFormat13(ReadableFontData data, CMapTable.CMapId cmapId) {
    super(data, CMap.CMapFormat.Format12.value, cmapId);
    this.numberOfGroups = this.data.readULongAsInt(Header.nGroups);
  }

  private int groupStartChar(int groupIndex) {
    return data.readULongAsInt(Header.SIZE + groupIndex * Group.SIZE + Group.startCharCode);
  }

  private int groupEndChar(int groupIndex) {
    return data.readULongAsInt(Header.SIZE + groupIndex * Group.SIZE + Group.endCharCode);
  }

  private int groupGlyph(int groupIndex) {
    return data.readULongAsInt(Header.SIZE + groupIndex * Group.SIZE + Group.glyphId);
  }

  @Override
  public int glyphId(int character) {
    int group =
        data.searchULong(
            Header.SIZE + Group.startCharCode,
            Group.SIZE,
            Header.SIZE + Group.endCharCode,
            Group.SIZE,
            numberOfGroups,
            character);
    if (group == -1) {
      return CMapTable.NOTDEF;
    }
    return groupGlyph(group);
  }

  @Override
  public int language() {
    return data.readULongAsInt(Header.language);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new CharacterIterator();
  }

  private class CharacterIterator extends CMap.CharacterRangesIterator {
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

  public static class Builder extends CMap.Builder<CMapFormat13> {
    protected Builder(WritableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMap.CMapFormat.Format13,
          cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMap.CMapFormat.Format13,
          cmapId);
    }

    @Override
    protected CMapFormat13 subBuildTable(ReadableFontData data) {
      return new CMapFormat13(data, cmapId());
    }
  }
}

package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.core.CMapTable.CMapId;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The cmap format 12 subtable maps segmented ranges of 32-bit character codes to glyph IDs.
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

  protected CMapFormat12(ReadableFontData data, CMapId cmapId) {
    super(data, CMapFormat.Format12.value, cmapId);
    this.numberOfGroups = this.data.readULongAsInt(Header.nGroups);
  }

  private int groupStartChar(int groupIndex) {
    return this.data.readULongAsInt(Header.groups + groupIndex * Group.SIZE + Group.startCharCode);
  }

  private int groupEndChar(int groupIndex) {
    return this.data.readULongAsInt(Header.groups + groupIndex * Group.SIZE + Group.endCharCode);
  }

  private int groupStartGlyph(int groupIndex) {
    return this.data.readULongAsInt(Header.groups + groupIndex * Group.SIZE + Group.startGlyphId);
  }

  @Override
  public int glyphId(int character) {
    int group =
        this.data.searchULong(
            Header.groups + Group.startCharCode,
            Group.SIZE,
            Header.groups + Group.endCharCode,
            Group.SIZE,
            this.numberOfGroups,
            character);
    if (group == -1) {
      return CMapTable.NOTDEF;
    }
    return groupStartGlyph(group) + (character - groupStartChar(group));
  }

  @Override
  public int language() {
    return this.data.readULongAsInt(Header.language);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new CharacterIterator();
  }

  private final class CharacterIterator implements Iterator<Integer> {
    private int groupIndex = 0;
    private int groupEndChar;

    private boolean nextSet = false;
    private int nextChar;

    private CharacterIterator() {
      nextChar = groupStartChar(groupIndex);
      groupEndChar = groupEndChar(groupIndex);
      nextSet = true;
    }

    @Override
    public boolean hasNext() {
      if (nextSet) {
        return true;
      }
      if (groupIndex >= numberOfGroups) {
        return false;
      }
      if (nextChar < groupEndChar) {
        nextChar++;
        nextSet = true;
        return true;
      }
      groupIndex++;
      if (groupIndex < numberOfGroups) {
        nextSet = true;
        nextChar = groupStartChar(groupIndex);
        groupEndChar = groupEndChar(groupIndex);
        return true;
      }
      return false;
    }

    @Override
    public Integer next() {
      if (!this.nextSet) {
        if (!hasNext()) {
          throw new NoSuchElementException("No more characters to iterate.");
        }
      }
      this.nextSet = false;
      return nextChar;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Unable to remove a character from cmap.");
    }
  }

  public static class Builder extends CMap.Builder<CMapFormat12> {
    protected Builder(WritableFontData data, int offset, CMapId cmapId) {
      super(data == null ? null : data.slice(
          offset, data.readULongAsInt(offset + Header.length)),
          CMapFormat.Format12, cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
      super(data == null ? null : data.slice(
          offset, data.readULongAsInt(offset + Header.length)),
          CMapFormat.Format12, cmapId);
    }

    @Override
    protected CMapFormat12 subBuildTable(ReadableFontData data) {
      return new CMapFormat12(data, this.cmapId());
    }
  }
}

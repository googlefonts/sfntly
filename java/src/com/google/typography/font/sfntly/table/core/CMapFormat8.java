package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.core.CMapTable.CMapId;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The cmap format 8 subtable maps 16-bit and 32-bit character codes to glyph IDs.
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
    return this.readFontData().readULongAsInt(
        Header.SIZE + groupIndex * Group.SIZE + Group.startCharCode);
  }

  private int endChar(int groupIndex) {
    return this.readFontData().readULongAsInt(
        Header.SIZE + groupIndex * Group.SIZE + Group.endCharCode);
  }

  @Override
  public int glyphId(int character) {
    return this.readFontData().searchULong(
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

  private class CharacterIterator implements Iterator<Integer> {
    private int groupIndex;
    private int firstCharInGroup;
    private int endCharInGroup;

    private int nextChar;
    private boolean nextCharSet;

    private CharacterIterator() {
      groupIndex = 0;
      firstCharInGroup = -1;
    }

    @Override
    public boolean hasNext() {
      if (nextCharSet) {
        return true;
      }
      while (groupIndex < numberOfGroups) {
        if (firstCharInGroup < 0) {
          firstCharInGroup = firstChar(groupIndex);
          endCharInGroup = endChar(groupIndex);
          nextChar = firstCharInGroup;
          nextCharSet = true;
          return true;
        }
        if (nextChar < endCharInGroup) {
          nextChar++;
          nextCharSet = true;
          return true;
        }
        groupIndex++;
        firstCharInGroup = -1;
      }
      return false;
    }

    @Override
    public Integer next() {
      if (!nextCharSet) {
        if (!hasNext()) {
          throw new NoSuchElementException("No more characters to iterate.");
        }
      }
      nextCharSet = false;
      return nextChar;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Unable to remove a character from cmap.");
    }
  }

  public static class Builder extends CMap.Builder<CMapFormat8> {
    protected Builder(WritableFontData data, int offset, CMapId cmapId) {
      super(data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMapFormat.Format8, cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
      super(data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMapFormat.Format8, cmapId);
    }

    @Override
    protected CMapFormat8 subBuildTable(ReadableFontData data) {
      return new CMapFormat8(data, this.cmapId());
    }
  }
}

package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.core.CMapTable.CMapId;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The cmap format 6 subtable maps a single range of 16-bit character codes to glyph IDs.
 *
 * @see "ISO/IEC 14496-22:2015, section 5.2.1.3.4"
 */
public final class CMapFormat6 extends CMap {

  private final int firstCode;
  private final int entryCount;

  private interface Header {
    int format = 0;
    int length = 2;
    int language = 4;
    int firstCode = 6;
    int entryCount = 8;
    int glyphIdArray = 10;
  }

  protected CMapFormat6(ReadableFontData data, CMapId cmapId) {
    super(data, CMapFormat.Format6.value, cmapId);
    this.firstCode = this.data.readUShort(Header.firstCode);
    this.entryCount = this.data.readUShort(Header.entryCount);
  }

  @Override
  public int glyphId(int character) {
    if (character < this.firstCode || character >= this.firstCode + this.entryCount) {
      return CMapTable.NOTDEF;
    }
    return this.data.readUShort(
        Header.glyphIdArray + (character - this.firstCode) * FontData.DataSize.USHORT.size());
  }

  @Override
  public int language() {
    return this.data.readUShort(Header.language);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new CharacterIterator();
  }

  private class CharacterIterator implements Iterator<Integer> {
    private int character = firstCode;

    private CharacterIterator() {
      // Prevent construction.
    }

    @Override
    public boolean hasNext() {
      return character < firstCode + entryCount;
    }

    @Override
    public Integer next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more characters to iterate.");
      }
      return this.character++;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Unable to remove a character from cmap.");
    }
  }

  public static class Builder extends CMap.Builder<CMapFormat6> {
    protected Builder(WritableFontData data, int offset, CMapId cmapId) {
      super(data == null ? null : data.slice(offset, data.readUShort(offset + Header.length)),
          CMapFormat.Format6, cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapId cmapId) {
      super(data == null ? null : data.slice(offset, data.readUShort(offset + Header.length)),
          CMapFormat.Format6, cmapId);
    }

    @Override
    protected CMapFormat6 subBuildTable(ReadableFontData data) {
      return new CMapFormat6(data, this.cmapId());
    }
  }
}

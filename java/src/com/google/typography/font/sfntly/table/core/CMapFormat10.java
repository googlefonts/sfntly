package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import java.util.Iterator;

/**
 * The cmap format 10 subtable maps a single range of 32-bit character codes to 16-bit glyph IDs.
 *
 * @see "ISO/IEC 14496-22:2015, section 5.2.1.3.6"
 */
public final class CMapFormat10 extends CMap {

  private final int startCharCode;
  private final int numChars;

  private interface Header {
    int format = 0;
    int length = 4;
    int language = 8;
    int startCharCode = 12;
    int numChars = 16;
    int glyphs = 20;
  }

  protected CMapFormat10(ReadableFontData data, CMapTable.CMapId cmapId) {
    super(data, CMap.CMapFormat.Format10.value, cmapId);
    this.startCharCode = this.data.readULongAsInt(Header.startCharCode);
    this.numChars = this.data.readUShort(Header.numChars);
  }

  @Override
  public int glyphId(int character) {
    if (character < startCharCode || character >= (startCharCode + numChars)) {
      return CMapTable.NOTDEF;
    }
    return readFontData().readUShort(character - startCharCode);
  }

  @Override
  public int language() {
    return data.readULongAsInt(Header.language);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new CharacterRangeIterator(startCharCode, startCharCode + numChars);
  }

  public static class Builder extends CMap.Builder<CMapFormat10> {
    protected Builder(WritableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMap.CMapFormat.Format10,
          cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMap.CMapFormat.Format10,
          cmapId);
    }

    @Override
    protected CMapFormat10 subBuildTable(ReadableFontData data) {
      return new CMapFormat10(data, cmapId());
    }
  }
}

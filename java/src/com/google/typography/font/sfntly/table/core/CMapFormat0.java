package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import java.util.Iterator;

/**
 * The cmap format 0 subtable maps 8-bit character codes to 8-bit glyph IDs.
 *
 * @see "ISO/IEC 14496-22:2015, section 5.2.1.3.1"
 */
public final class CMapFormat0 extends CMap {

  private interface Header {
    int format = 0;
    int length = 2;
    int language = 4;
    int glyphIdArray = 6;
  }

  protected CMapFormat0(ReadableFontData data, CMapTable.CMapId cmapId) {
    super(data, CMap.CMapFormat.Format0.value, cmapId);
  }

  @Override
  public int glyphId(int character) {
    if (character < 0 || character > 255) {
      return CMapTable.NOTDEF;
    }
    return data.readUByte(Header.glyphIdArray + character);
  }

  @Override
  public int language() {
    return data.readUShort(Header.language);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new CharacterRangeIterator(0, 256);
  }

  public static class Builder extends CMap.Builder<CMapFormat0> {
    protected Builder(WritableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readUShort(offset + Header.length)),
          CMap.CMapFormat.Format0,
          cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readUShort(offset + Header.length)),
          CMap.CMapFormat.Format0,
          cmapId);
    }

    @Override
    protected CMapFormat0 subBuildTable(ReadableFontData data) {
      return new CMapFormat0(data, cmapId());
    }
  }
}

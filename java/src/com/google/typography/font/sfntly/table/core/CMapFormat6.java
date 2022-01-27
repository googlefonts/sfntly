package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import java.util.Iterator;

/**
 * The cmap format 6 subtable maps a single range of 16-bit character codes to 16-bit glyph IDs.
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

  protected CMapFormat6(ReadableFontData data, CMapTable.CMapId cmapId) {
    super(data, CMap.CMapFormat.Format6.value, cmapId);
    this.firstCode = this.data.readUShort(Header.firstCode);
    this.entryCount = this.data.readUShort(Header.entryCount);
  }

  @Override
  public int glyphId(int character) {
    if (character < firstCode || character >= firstCode + entryCount) {
      return CMapTable.NOTDEF;
    }
    return data.readUShort(Header.glyphIdArray + (character - firstCode) * FontData.SizeOf.USHORT);
  }

  @Override
  public int language() {
    return data.readUShort(Header.language);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new CharacterRangeIterator(firstCode, firstCode + entryCount);
  }

  public static class Builder extends CMap.Builder<CMapFormat6> {
    protected Builder(WritableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readUShort(offset + Header.length)),
          CMap.CMapFormat.Format6,
          cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readUShort(offset + Header.length)),
          CMap.CMapFormat.Format6,
          cmapId);
    }

    @Override
    protected CMapFormat6 subBuildTable(ReadableFontData data) {
      return new CMapFormat6(data, cmapId());
    }
  }
}

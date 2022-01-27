package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import java.util.Iterator;

/**
 * The cmap format 2 subtable maps multi-byte character codes to glyph IDs.
 *
 * <p>This format is typically used for encodings such as SJIS, EUC-JP/KR/CN, Big5, etc.
 *
 * @see "ISO/IEC 14496-22:2015, section 5.2.1.3.2"
 */
public final class CMapFormat2 extends CMap {

  private interface Header {
    int format = 0;
    int length = 2;
    int language = 4;
    int subHeaderKeys = 6;
  }

  private interface SubHeader {
    int firstCode = 0;
    int entryCount = 2;
    int idDelta = 4;
    int idRangeOffset = 6;
  }

  protected CMapFormat2(ReadableFontData data, CMapTable.CMapId cmapId) {
    super(data, CMap.CMapFormat.Format2.value, cmapId);
  }

  private int subHeaderOffset(int subHeaderIndex) {
    return data.readUShort(Header.subHeaderKeys + subHeaderIndex * FontData.SizeOf.USHORT);
  }

  private int firstCode(int subHeaderIndex) {
    int subHeaderOffset = subHeaderOffset(subHeaderIndex);
    return data.readUShort(Header.subHeaderKeys + subHeaderOffset + SubHeader.firstCode);
  }

  private int entryCount(int subHeaderIndex) {
    int subHeaderOffset = subHeaderOffset(subHeaderIndex);
    return data.readUShort(Header.subHeaderKeys + subHeaderOffset + SubHeader.entryCount);
  }

  private int idRangeOffset(int subHeaderIndex) {
    int subHeaderOffset = subHeaderOffset(subHeaderIndex);
    return data.readUShort(Header.subHeaderKeys + subHeaderOffset + SubHeader.idRangeOffset);
  }

  private int idDelta(int subHeaderIndex) {
    int subHeaderOffset = subHeaderOffset(subHeaderIndex);
    return data.readShort(Header.subHeaderKeys + subHeaderOffset + SubHeader.idDelta);
  }

  /**
   * Returns how many bytes would be consumed by a lookup of this character with this cmap. This
   * comes about because the cmap format 2 table is designed around multi-byte encodings such as
   * SJIS, EUC-JP, Big5, etc.
   *
   * @return the number of bytes consumed from this "character" - either 1 or 2
   */
  public int bytesConsumed(int character) {
    int highByte = (character >> 8) & 0xff;
    int offset = subHeaderOffset(highByte);

    if (offset == 0) {
      return 1;
    }
    return 2;
  }

  @Override
  public int glyphId(int character) {
    if (character > 0xffff) {
      return CMapTable.NOTDEF;
    }

    int highByte = (character >> 8) & 0xff;
    int lowByte = character & 0xff;
    int offset = subHeaderOffset(highByte);

    // only consume one byte
    if (offset == 0) {
      lowByte = highByte;
      highByte = 0;
    }

    int firstCode = firstCode(highByte);
    int entryCount = entryCount(highByte);

    if (lowByte < firstCode || lowByte >= firstCode + entryCount) {
      return CMapTable.NOTDEF;
    }

    int idRangeOffset = idRangeOffset(highByte);

    // position of idRangeOffset + value of idRangeOffset + index for low byte
    // = firstcode
    int pLocation =
        (offset + SubHeader.idRangeOffset)
            + idRangeOffset
            + (lowByte - firstCode) * FontData.SizeOf.USHORT;
    int p = data.readUShort(pLocation);
    if (p == 0) {
      return CMapTable.NOTDEF;
    }

    if (offset == 0) {
      return p;
    }
    int idDelta = idDelta(highByte);
    return (p + idDelta) % 65536;
  }

  @Override
  public int language() {
    return data.readUShort(Header.language);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new CharacterRangeIterator(0, 0x10000);
  }

  public static class Builder extends CMap.Builder<CMapFormat2> {
    protected Builder(WritableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readUShort(offset + Header.length)),
          CMap.CMapFormat.Format2,
          cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readUShort(offset + Header.length)),
          CMap.CMapFormat.Format2,
          cmapId);
    }

    @Override
    protected CMapFormat2 subBuildTable(ReadableFontData data) {
      return new CMapFormat2(data, cmapId());
    }
  }
}

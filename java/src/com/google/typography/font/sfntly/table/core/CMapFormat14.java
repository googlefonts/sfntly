package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import java.util.Iterator;

/**
 * The cmap format 14 subtable maps Unicode Variation Sequences to glyph IDs.
 *
 * @see "ISO/IEC 14496-22:2015, section 5.2.1.3.9"
 */
// TODO(stuartg): completely unsupported yet
public final class CMapFormat14 extends CMap {

  private interface Header {
    int format = 0;
    int length = 2;
  }

  protected CMapFormat14(ReadableFontData data, CMapTable.CMapId cmapId) {
    super(data, CMap.CMapFormat.Format14.value, cmapId);
  }

  @Override
  public int glyphId(int character) {
    return CMapTable.NOTDEF;
  }

  @Override
  public int language() {
    return 0;
  }

  @Override
  public Iterator<Integer> iterator() {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public static class Builder extends CMap.Builder<CMapFormat14> {
    protected Builder(WritableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMap.CMapFormat.Format14,
          cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readULongAsInt(offset + Header.length)),
          CMap.CMapFormat.Format14,
          cmapId);
    }

    @Override
    protected CMapFormat14 subBuildTable(ReadableFontData data) {
      return new CMapFormat14(data, cmapId());
    }
  }
}

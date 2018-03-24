package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The abstract base class for all cmaps.
 *
 * <p>CMap equality is based on the equality of the (@link {@link CMapTable.CMapId} that defines the
 * CMap. In the cmap table for a font there can only be one cmap with a given cmap id (pair of
 * platform and encoding ids) no matter what the type of the cmap is.
 *
 * <p>The cmap implements {@code Iterable<Integer>} to allow iteration over characters that are
 * mapped by the cmap. This iteration mostly returns the characters mapped by the cmap. It will
 * return all characters mapped by the cmap to anything but .notdef <b>but</b> it may return some
 * that are not mapped or are mapped to .notdef. Various cmap tables provide ranges and such to
 * describe characters for lookup but without going the full way to mapping to the glyph id it isn't
 * always possible to tell if a character will end up with a valid glyph id. So, some of the
 * characters returned from the iterator may still end up pointing to the .notdef glyph. However,
 * the number of such characters should be small in most cases with well designed cmaps.
 */
public abstract class CMap extends SubTable implements Iterable<Integer> {
  protected final int format;
  protected final CMapTable.CMapId cmapId;

  /**
   * CMap subtable formats.
   *
   * @see "ISO/IEO 14496-22:2015, section 5.2.1"
   */
  public enum CMapFormat {
    Format0(0),
    Format2(2),
    Format4(4),
    Format6(6),
    Format8(8),
    Format10(10),
    Format12(12),
    Format13(13),
    Format14(14);

    final int value;

    private CMapFormat(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    public boolean equals(int value) {
      return value == this.value;
    }

    public static CMapFormat valueOf(int value) {
      for (CMapFormat format : CMapFormat.values()) {
        if (format.equals(value)) {
          return format;
        }
      }
      return null;
    }
  }

  protected CMap(ReadableFontData data, int format, CMapTable.CMapId cmapId) {
    super(data);
    this.format = format;
    this.cmapId = cmapId;
  }

  public int format() {
    return format;
  }

  public CMapTable.CMapId cmapId() {
    return cmapId;
  }

  /** @see Font.PlatformId */
  public int platformId() {
    return cmapId().platformId();
  }

  /**
   * @see Font.MacintoshEncodingId
   * @see Font.WindowsEncodingId
   * @see Font.UnicodeEncodingId
   */
  public int encodingId() {
    return cmapId().encodingId();
  }

  abstract static class CharIterator implements Iterator<Integer> {

    @Override
    public final void remove() {
      throw new UnsupportedOperationException("Unable to remove a character from cmap.");
    }

    @Override
    public final Integer next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return doNext();
    }

    protected abstract Integer doNext();
  }

  static class CharacterRangeIterator extends CharIterator {
    private int character;
    private final int maxCharacter;

    CharacterRangeIterator(int start, int end) {
      this.character = start;
      this.maxCharacter = end;
    }

    @Override
    public boolean hasNext() {
      return character < maxCharacter;
    }

    @Override
    public Integer doNext() {
      return this.character++;
    }
  }

  /** Iterates over a sequence of character ranges. */
  abstract static class CharacterRangesIterator extends CharIterator {
    private final int nRanges;

    private int range = -1;
    private int curr;
    private int end;

    CharacterRangesIterator(int nRanges) {
      this.nRanges = nRanges;
    }

    @Override
    public boolean hasNext() {
      while (range < nRanges) {
        if (curr < end) {
          return true;
        }
        this.range++;
        if (range >= nRanges) {
          return false;
        }
        this.curr = getRangeStart(range);
        this.end = getRangeEnd(range);
      }
      return false;
    }

    @Override
    protected Integer doNext() {
      return this.curr++;
    }

    protected abstract int getRangeStart(int rangeIndex);

    protected abstract int getRangeEnd(int rangeIndex);
  }

  @Override
  public int hashCode() {
    return cmapId.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CMap)) {
      return false;
    }
    return cmapId.equals(((CMap) obj).cmapId);
  }

  /**
   * Gets the language of the cmap.
   *
   * <p>Note on the language field in 'cmap' subtables: The language field must be set to zero for
   * all cmap subtables whose platform IDs are other than Macintosh (platform ID 1). For cmap
   * subtables whose platform IDs are Macintosh, set this field to the Macintosh language ID of the
   * cmap subtable plus one, or to zero if the cmap subtable is not language-specific. For example,
   * a Mac OS Turkish cmap subtable must set this field to 18, since the Macintosh language ID for
   * Turkish is 17. A Mac OS Roman cmap subtable must set this field to 0, since Mac OS Roman is not
   * a language-specific encoding.
   *
   * @return the language id
   */
  public abstract int language();

  /**
   * Gets the glyph id for the character code provided.
   *
   * <p>The character code provided must be in the encoding used by the cmap table.
   *
   * @param character character value using the encoding of the cmap table
   * @return glyph id for the character code
   */
  public abstract int glyphId(int character);

  @Override
  public String toString() {
    return String.format(
        "cmap: %s, %s, Data Size=%#x", cmapId(), CMapFormat.valueOf(format()), data.length());
  }

  public abstract static class Builder<T extends CMap> extends SubTable.Builder<T> {

    private final CMapFormat format;
    private final CMapTable.CMapId cmapId;
    private int language;

    protected Builder(ReadableFontData data, CMapFormat format, CMapTable.CMapId cmapId) {
      super(data);
      this.format = format;
      this.cmapId = cmapId;
    }

    public CMapTable.CMapId cmapId() {
      return cmapId;
    }

    /**
     * Gets the encoding id for the cmap. The encoding will from one of a number of different sets
     * depending on the platform id.
     *
     * @return the encoding id
     * @see Font.MacintoshEncodingId
     * @see Font.WindowsEncodingId
     * @see Font.UnicodeEncodingId
     */
    public int encodingId() {
      return cmapId().encodingId();
    }

    /**
     * Gets the platform id for the cmap.
     *
     * @return the platform id
     * @see Font.PlatformId
     */
    public int platformId() {
      return cmapId().platformId();
    }

    public CMapFormat format() {
      return format;
    }

    public int language() {
      return language;
    }

    public void setLanguage(int language) {
      this.language = language;
    }

    protected Builder(WritableFontData data, CMapFormat format, CMapTable.CMapId cmapId) {
      super(data);
      this.format = format;
      this.cmapId = cmapId;
    }

    @Override
    protected void subDataSet() {
      // NOP
    }

    @Override
    protected int subDataSizeToSerialize() {
      return internalReadData().length();
    }

    @Override
    protected boolean subReadyToSerialize() {
      return true;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      return internalReadData().copyTo(newData);
    }

    static CMap.Builder<? extends CMap> getBuilder(
        ReadableFontData data, int offset, CMapTable.CMapId cmapId) {
      // read from the front of the cmap - 1st entry is always the format
      int rawFormat = data.readUShort(offset);
      CMapFormat format = CMapFormat.valueOf(rawFormat);

      switch (format) {
        case Format0:
          return new CMapFormat0.Builder(data, offset, cmapId);
        case Format2:
          return new CMapFormat2.Builder(data, offset, cmapId);
        case Format4:
          return new CMapFormat4.Builder(data, offset, cmapId);
        case Format6:
          return new CMapFormat6.Builder(data, offset, cmapId);
        case Format8:
          return new CMapFormat8.Builder(data, offset, cmapId);
        case Format10:
          return new CMapFormat10.Builder(data, offset, cmapId);
        case Format12:
          return new CMapFormat12.Builder(data, offset, cmapId);
        case Format13:
          return new CMapFormat13.Builder(data, offset, cmapId);
        case Format14:
          return new CMapFormat14.Builder(data, offset, cmapId);
      }
      return null;
    }

    // TODO: Instead of a root factory method, the individual subtable
    // builders should get created
    // from static factory methods in each subclass
    static CMap.Builder<? extends CMap> getBuilder(CMapFormat cmapFormat, CMapTable.CMapId cmapId) {
      switch (cmapFormat) {
          // TODO: builders for other formats, as they're implemented
        case Format0:
          return new CMapFormat0.Builder(null, 0, cmapId);
        case Format4:
          return new CMapFormat4.Builder(null, 0, cmapId);
        default:
          break;
      }
      return null;
    }

    @Override
    public String toString() {
      return String.format("%s, format = %s", cmapId(), format());
    }
  }
}

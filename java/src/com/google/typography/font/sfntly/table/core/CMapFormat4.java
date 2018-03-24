package com.google.typography.font.sfntly.table.core;

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.math.FontMath;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The cmap format 4 subtable maps segmented ranges of 16-bit character codes to 16-bit glyph IDs.
 *
 * @see "ISO/IEC 14496-22:2015, section 5.2.1.3.3"
 */
public final class CMapFormat4 extends CMap {
  private final int segCount;
  private final int glyphIdArrayOffset;

  private interface Header {
    int format = 0;
    int length = 2;
    int language = 4;
    int segCountX2 = 6;
    int searchRange = 8;
    int entrySelector = 10;
    int rangeShift = 12;
    int SIZE = 14;
  }

  protected CMapFormat4(ReadableFontData data, CMapTable.CMapId cmapId) {
    super(data, CMap.CMapFormat.Format4.value, cmapId);

    this.segCount = this.data.readUShort(Header.segCountX2) / 2;
    this.glyphIdArrayOffset = glyphIdArrayOffset(segCount);
  }

  @Override
  public int glyphId(int character) {
    int segment =
        data.searchUShort(
            startCodeOffset(segCount),
            FontData.SizeOf.USHORT,
            Header.SIZE,
            FontData.SizeOf.USHORT,
            segCount,
            character);
    if (segment == -1) {
      return CMapTable.NOTDEF;
    }
    int startCode = startCode(segment);

    return retrieveGlyphId(segment, startCode, character);
  }

  /**
   * Lower level glyph code retrieval that requires processing the Format 4 segments to use.
   *
   * @param segment the cmap segment
   * @param startCode the start code for the segment
   * @param character the character to be looked up
   * @return the glyph id for the character; CMapTable.NOTDEF if not found
   */
  public int retrieveGlyphId(int segment, int startCode, int character) {
    if (character < startCode) {
      return CMapTable.NOTDEF;
    }
    int idRangeOffset = idRangeOffset(segment);
    if (idRangeOffset == 0) {
      return (character + idDelta(segment)) % 65536;
    }
    int gid =
        data.readUShort(
            idRangeOffset + idRangeOffsetLocation(segment) + 2 * (character - startCode));
    if (gid != 0) {
      gid = (gid + idDelta(segment)) % 65536;
    }
    return gid;
  }

  /**
   * Gets the count of the number of segments in this cmap.
   *
   * @return the number of segments
   */
  public int getSegCount() {
    return segCount;
  }

  /**
   * Gets the start code for a segment.
   *
   * @param segment the segment in the look up table
   * @return the start code for the segment
   */
  public int startCode(int segment) {
    isValidIndex(segment);
    return startCode(data, segCount, segment);
  }

  private static int length(ReadableFontData data) {
    return data.readUShort(Header.length);
  }

  private static int segCount(ReadableFontData data) {
    return data.readUShort(Header.segCountX2) / 2;
  }

  private static int startCode(ReadableFontData data, int segCount, int index) {
    return data.readUShort(startCodeOffset(segCount) + index * FontData.SizeOf.USHORT);
  }

  private static int startCodeOffset(int segCount) {
    return Header.SIZE + (segCount + 1) * FontData.SizeOf.USHORT;
  }

  private static int endCode(ReadableFontData data, int segCount, int index) {
    return data.readUShort(Header.SIZE + index * FontData.SizeOf.USHORT);
  }

  private static int idDelta(ReadableFontData data, int segCount, int index) {
    return data.readShort(idDeltaOffset(segCount) + index * FontData.SizeOf.SHORT);
  }

  private static int idDeltaOffset(int segCount) {
    return Header.SIZE + ((2 * segCount) + 1) * FontData.SizeOf.USHORT;
  }

  private static int idRangeOffset(ReadableFontData data, int segCount, int index) {
    return data.readUShort(idRangeOffsetOffset(segCount) + index * FontData.SizeOf.USHORT);
  }

  private static int idRangeOffsetOffset(int segCount) {
    return Header.SIZE
        + ((2 * segCount) + 1) * FontData.SizeOf.USHORT
        + segCount * FontData.SizeOf.SHORT;
  }

  private static int glyphIdArrayOffset(int segCount) {
    return Header.SIZE
        + ((3 * segCount) + 1) * FontData.SizeOf.USHORT
        + segCount * FontData.SizeOf.SHORT;
  }

  /**
   * Gets the end code for a segment.
   *
   * @param segment the segment in the look up table
   * @return the end code for the segment
   */
  public int endCode(int segment) {
    isValidIndex(segment);
    return endCode(data, segCount, segment);
  }

  private void isValidIndex(int segment) {
    if (segment < 0 || segment >= segCount) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Gets the id delta for a segment.
   *
   * @param segment the segment in the look up table
   * @return the id delta for the segment
   */
  public int idDelta(int segment) {
    isValidIndex(segment);
    return idDelta(data, segCount, segment);
  }

  /**
   * Gets the id range offset for a segment.
   *
   * @param segment the segment in the look up table
   * @return the id range offset for the segment
   */
  public int idRangeOffset(int segment) {
    isValidIndex(segment);
    return data.readUShort(idRangeOffsetLocation(segment));
  }

  /**
   * Get the location of the id range offset for a segment.
   *
   * @param segment the segment in the look up table
   * @return the location of the id range offset for the segment
   */
  public int idRangeOffsetLocation(int segment) {
    isValidIndex(segment);
    return idRangeOffsetOffset(segCount) + segment * FontData.SizeOf.USHORT;
  }

  @SuppressWarnings("unused")
  private int glyphIdArray(int index) {
    return data.readUShort(glyphIdArrayOffset + index * FontData.SizeOf.USHORT);
  }

  @Override
  public int language() {
    return data.readUShort(Header.language);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new CharacterIterator();
  }

  private class CharacterIterator extends CMap.CharacterRangesIterator {
    CharacterIterator() {
      super(segCount);
    }

    @Override
    protected int getRangeStart(int rangeIndex) {
      return startCode(rangeIndex);
    }

    @Override
    protected int getRangeEnd(int rangeIndex) {
      return endCode(rangeIndex);
    }
  }

  public static class Builder extends CMap.Builder<CMapFormat4> {
    public static class Segment {
      public static List<Builder.Segment> deepCopy(List<Builder.Segment> original) {
        List<Builder.Segment> list = new ArrayList<>(original.size());
        for (Builder.Segment segment : original) {
          list.add(new Segment(segment));
        }
        return list;
      }

      private int startCount;
      private int endCount;
      private int idDelta;
      private int idRangeOffset;

      public Segment() {}

      public Segment(Builder.Segment other) {
        this(other.startCount, other.endCount, other.idDelta, other.idRangeOffset);
      }

      public Segment(int startCount, int endCount, int idDelta, int idRangeOffset) {
        this.startCount = startCount;
        this.endCount = endCount;
        this.idDelta = idDelta;
        this.idRangeOffset = idRangeOffset;
      }

      public int getStartCount() {
        return startCount;
      }

      public void setStartCount(int startCount) {
        this.startCount = startCount;
      }

      public int getEndCount() {
        return endCount;
      }

      public void setEndCount(int endCount) {
        this.endCount = endCount;
      }

      public int getIdDelta() {
        return idDelta;
      }

      public void setIdDelta(int idDelta) {
        this.idDelta = idDelta;
      }

      public int getIdRangeOffset() {
        return idRangeOffset;
      }

      public void setIdRangeOffset(int idRangeOffset) {
        this.idRangeOffset = idRangeOffset;
      }

      @Override
      public String toString() {
        return String.format(
            "[0x%04x - 0x%04x, delta = 0x%04x, rangeOffset = 0x%04x]",
            startCount, endCount, idDelta, idRangeOffset);
      }
    }

    private List<Builder.Segment> segments;
    private List<Integer> glyphIdArray;

    protected Builder(WritableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readUShort(offset + Header.length)),
          CMap.CMapFormat.Format4,
          cmapId);
    }

    protected Builder(ReadableFontData data, int offset, CMapTable.CMapId cmapId) {
      super(
          data == null ? null : data.slice(offset, data.readUShort(offset + Header.length)),
          CMap.CMapFormat.Format4,
          cmapId);
    }

    private void initialize(ReadableFontData data) {
      this.segments = new ArrayList<>();
      this.glyphIdArray = new ArrayList<>();

      if (data == null || data.length() == 0) {
        return;
      }

      // build segments
      int segCount = segCount(data);
      for (int index = 0; index < segCount; index++) {
        Builder.Segment segment = new Segment();
        segment.setStartCount(startCode(data, segCount, index));
        segment.setEndCount(endCode(data, segCount, index));
        segment.setIdDelta(idDelta(data, segCount, index));
        segment.setIdRangeOffset(idRangeOffset(data, segCount, index));

        segments.add(segment);
      }

      // build glyph id array
      int glyphIdArrayLength = length(data) - glyphIdArrayOffset(segCount);
      for (int index = 0; index < glyphIdArrayLength; index += FontData.SizeOf.USHORT) {
        glyphIdArray.add(data.readUShort(index + glyphIdArrayOffset(segCount)));
      }
    }

    public List<Builder.Segment> getSegments() {
      if (segments == null) {
        initialize(internalReadData());
        setModelChanged();
      }
      return segments;
    }

    public void setSegments(List<Builder.Segment> segments) {
      this.segments = Segment.deepCopy(segments);
      setModelChanged();
    }

    public List<Integer> getGlyphIdArray() {
      if (glyphIdArray == null) {
        initialize(internalReadData());
        setModelChanged();
      }
      return glyphIdArray;
    }

    public void setGlyphIdArray(List<Integer> glyphIdArray) {
      this.glyphIdArray = new ArrayList<>(glyphIdArray);
      setModelChanged();
    }

    @Override
    protected CMapFormat4 subBuildTable(ReadableFontData data) {
      return new CMapFormat4(data, cmapId());
    }

    @Override
    protected void subDataSet() {
      this.segments = null;
      this.glyphIdArray = null;
      super.setModelChanged(false);
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (!modelChanged()) {
        return super.subDataSizeToSerialize();
      }

      int size =
          Header.SIZE
              + FontData.SizeOf.USHORT // reservedPad
              + segments.size() * 4 * FontData.SizeOf.USHORT
              + glyphIdArray.size() * FontData.SizeOf.USHORT;
      return size;
    }

    @Override
    protected boolean subReadyToSerialize() {
      if (!modelChanged()) {
        return super.subReadyToSerialize();
      }

      return segments != null;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      if (!modelChanged()) {
        return super.subSerialize(newData);
      }

      int index = 0;
      index += newData.writeUShort(index, CMap.CMapFormat.Format4.value());
      index += FontData.SizeOf.USHORT; // length - write this at the end
      index += newData.writeUShort(index, language());
      int segCount = segments.size();
      index += newData.writeUShort(index, segCount * 2);
      int log2SegCount = FontMath.log2(segments.size());
      int searchRange = 1 << (log2SegCount + 1);
      index += newData.writeUShort(index, searchRange);
      int entrySelector = log2SegCount;
      index += newData.writeUShort(index, entrySelector);
      int rangeShift = 2 * segCount - searchRange;
      index += newData.writeUShort(index, rangeShift);

      for (Segment segment : segments) {
        index += newData.writeUShort(index, segment.getEndCount());
      }
      index += FontData.SizeOf.USHORT; // reservedPad
      for (Segment segment : segments) {
        index += newData.writeUShort(index, segment.getStartCount());
      }
      for (Segment segment : segments) {
        index += newData.writeShort(index, segment.getIdDelta());
      }
      for (Segment segment : segments) {
        index += newData.writeUShort(index, segment.getIdRangeOffset());
      }

      for (Integer glyphId : glyphIdArray) {
        index += newData.writeUShort(index, glyphId);
      }

      newData.writeUShort(Header.length, index);

      return index;
    }
  }
}

package com.google.typography.font.sfntly.table.truetype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;

public abstract class Glyph extends SubTable {

  public enum GlyphType {
    Simple,
    Composite;
  }

  protected volatile boolean initialized = false;
  // TOO(stuartg): should we replace this with a shared lock? more contention
  // but less space
  protected final Object initializationLock = new Object();

  private final Glyph.GlyphType glyphType;
  private final int numberOfContours;

  protected Glyph(ReadableFontData data, Glyph.GlyphType glyphType) {
    super(data);
    this.glyphType = glyphType;

    if (this.data.length() == 0) {
      this.numberOfContours = 0;
    } else {
      // -1 if composite
      this.numberOfContours = this.data.readShort(GlyphTable.Offset.numberOfContours);
    }
  }

  protected Glyph(ReadableFontData data, int offset, int length, Glyph.GlyphType glyphType) {
    super(data, offset, length);
    this.glyphType = glyphType;

    if (this.data.length() == 0) {
      this.numberOfContours = 0;
    } else {
      // -1 if composite
      this.numberOfContours = this.data.readShort(GlyphTable.Offset.numberOfContours);
    }
  }

  private static Glyph.GlyphType glyphType(ReadableFontData data, int offset, int length) {
    if (offset > data.length()) {
      throw new IndexOutOfBoundsException();
    }
    if (length == 0) {
      return GlyphType.Simple;
    }
    int numberOfContours = data.readShort(offset);
    if (numberOfContours >= 0) {
      return GlyphType.Simple;
    }
    return GlyphType.Composite;
  }

  //  @SuppressWarnings("unchecked")
  //  static <T extends Glyph> T getGlyph(
  //      GlyphTable table, ReadableFontData data, int offset, int length) {
  //    Glyph.GlyphType type = Glyph.glyphType(data, offset, length);
  //    if (type == GlyphType.Simple) {
  //      return (T) new SimpleGlyph(data, offset, length);
  //    }
  //    return (T) new CompositeGlyph(data, offset, length);
  //  }

  static Glyph getGlyph(GlyphTable table, ReadableFontData data, int offset, int length) {
    Glyph.GlyphType type = glyphType(data, offset, length);
    if (type == GlyphType.Simple) {
      return new SimpleGlyph(data, offset, length);
    }
    return new CompositeGlyph(data, offset, length);
  }

  protected abstract void initialize();

  @Override
  public int padding() {
    initialize();
    return super.padding();
  }

  public Glyph.GlyphType glyphType() {
    return glyphType;
  }

  /**
   * Gets the number of contours in the glyph. If this returns a number greater than or equal to
   * zero it is the actual number of contours and this is a simple glyph. If there are zero contours
   * in the glyph then none of the other data operations will return usable values. If it -1 then
   * the glyph is a composite glyph.
   *
   * @return number of contours
   */
  public int numberOfContours() {
    return numberOfContours;
  }

  public int xMin() {
    return data.readShort(GlyphTable.Offset.xMin);
  }

  public int xMax() {
    return data.readShort(GlyphTable.Offset.xMax);
  }

  public int yMin() {
    return data.readShort(GlyphTable.Offset.yMin);
  }

  public int yMax() {
    return data.readShort(GlyphTable.Offset.yMax);
  }

  public abstract int instructionSize();

  public abstract ReadableFontData instructions();

  @Override
  public String toString() {
    return String.format(
        "%s, contours=%d, [xmin=%d, ymin=%d, xmax=%d, ymax=%d]\n",
        glyphType(), numberOfContours(), xMin(), yMin(), xMax(), yMax());
  }

  // TODO(stuartg): interface? need methods from Composite?
  public abstract static class Contour {
    protected Contour() {}
  }

  public abstract static class Builder<T extends Glyph> extends SubTable.Builder<T> {
    protected int format;

    protected Builder(WritableFontData data) {
      super(data);
    }

    protected Builder(ReadableFontData data) {
      super(data);
    }

    protected Builder(WritableFontData data, int offset, int length) {
      this(data.slice(offset, length));
    }

    static Glyph.Builder<? extends Glyph> getBuilder(
        GlyphTable.Builder tableBuilder, ReadableFontData data) {
      return getBuilder(tableBuilder, data, 0, data.length());
    }

    static Glyph.Builder<? extends Glyph> getBuilder(
        GlyphTable.Builder tableBuilder, ReadableFontData data, int offset, int length) {
      Glyph.GlyphType type = glyphType(data, offset, length);
      if (type == GlyphType.Simple) {
        return new SimpleGlyph.SimpleGlyphBuilder(data, offset, length);
      }
      return new CompositeGlyph.CompositeGlyphBuilder(data, offset, length);
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
  }
}

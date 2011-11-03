/*
 * Copyright 2010 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.typography.font.sfntly.table.truetype;

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.SubTableContainerTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A Glyph table.
 *
 * @author Stuart Gill
 */
public final class GlyphTable extends SubTableContainerTable {

  /**
   * Offsets to specific elements in the underlying data. These offsets are relative to the
   * start of the table or the start of sub-blocks within the table.
   */
  public enum Offset {
    // header
    numberOfContours(0),
    xMin(2),
    yMin(4),
    xMax(6),
    yMax(8),

    // Simple Glyph Description
    simpleEndPtsOfCountours(10),
    // offset from the end of the contours array
    simpleInstructionLength(0),
    simpleInstructions(2),
    // flags
    // xCoordinates
    // yCoordinates

    // Composite Glyph Description
    compositeFlags(0),
    compositeGyphIndexWithoutFlag(0),
    compositeGlyphIndexWithFlag(2);

    private final int offset;

    private Offset(int offset) {
      this.offset = offset;
    }
  }

  private GlyphTable(Header header, ReadableFontData data) {
    super(header, data);
  }

  public Glyph glyph(int offset, int length) {
    return Glyph.getGlyph(this, this.data, offset, length);
  }

  public static class Builder extends SubTableContainerTable.Builder<GlyphTable> {

    private List<GlyphTable.Glyph.Builder<? extends Glyph>> glyphBuilders;
    private List<Integer> loca;

    /**
     * Create a new builder using the header information and data provided.
     *
     * @param header the header information
     * @param data the data holding the table
     * @return a new builder
     */
    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    /**
     * Constructor.
     *
     * @param header the table header
     * @param data the data for the table
     */
    protected Builder(Header header, WritableFontData data) {
      super(header, data);
    }

    /**
     * Constructor.
     *
     * @param header the table header
     * @param data the data for the table
     */
    protected Builder(Header header, ReadableFontData data) {
      super(header, data);
    }

    // glyph table level building

    public void setLoca(List<Integer> loca) {
      this.loca = new ArrayList<Integer>(loca);
      this.setModelChanged(false);
      this.glyphBuilders = null;
    }

    /**
     * Generate a loca table list from the current state of the glyph table
     * builder.
     *
     * @return a list of loca information for the glyphs
     */
    public List<Integer> generateLocaList() {
      List<Integer> locas = new ArrayList<Integer>(this.getGlyphBuilders().size());
      locas.add(0);
      if (this.getGlyphBuilders().size() == 0) {
        locas.add(0);
      } else {
        int total = 0;
        for (Glyph.Builder<? extends Glyph> b : this.getGlyphBuilders()) {
          int size = b.subDataSizeToSerialize();
          locas.add(total + size);
          total += size;
        }
      }
      return locas;
    }

    private void initialize(ReadableFontData data, List<Integer> loca) {
      this.glyphBuilders = new ArrayList<GlyphTable.Glyph.Builder<? extends Glyph>>();

      if (data != null) {
        int locaValue;
        int lastLocaValue = loca.get(0);
        for (int i = 1; i < loca.size(); i++) {
          locaValue = loca.get(i);
          this.glyphBuilders.add(Glyph.Builder.getBuilder(this, data, lastLocaValue /* offset */,
              locaValue - lastLocaValue /* length */));
          lastLocaValue = locaValue;
        }
      }
    }

    private List<GlyphTable.Glyph.Builder<? extends Glyph>> getGlyphBuilders() {
      if (this.glyphBuilders == null) {
        if (this.internalReadData() != null && this.loca == null) {
          throw new IllegalStateException("Loca values not set - unable to parse glyph data.");
        }
        this.initialize(this.internalReadData(), this.loca);
        this.setModelChanged();
      }
      return this.glyphBuilders;
    }

    public void revert() {
      this.glyphBuilders = null;
      this.setModelChanged(false);
    }

    /**
     * Gets the List of glyph builders for the glyph table builder. These may be
     * manipulated in any way by the caller and the changes will be reflected in
     * the final glyph table produced.
     *
     *  If there is no current data for the glyph builder or the glyph builders
     * have not been previously set then this will return an empty glyph builder
     * List. If there is current data (i.e. data read from an existing font) and
     * the <code>loca</code> list has not been set or is null, empty, or
     * invalid, then an empty glyph builder List will be returned.
     *
     * @return the list of glyph builders
     */
    public List<Glyph.Builder<? extends Glyph>> glyphBuilders() {
      return this.getGlyphBuilders();
    }

    /**
     * Replace the internal glyph builders with the one provided. The provided
     * list and all contained objects belong to this builder.
     *
     *  This call is only required if the entire set of glyphs in the glyph
     * table builder are being replaced. If the glyph builder list provided from
     * the {@link GlyphTable.Builder#glyphBuilders()} is being used and modified
     * then those changes will already be reflected in the glyph table builder.
     *
     * @param glyphBuilders the new glyph builders
     */
    public void setGlyphBuilders(List<GlyphTable.Glyph.Builder<? extends Glyph>> glyphBuilders) {
      this.glyphBuilders = glyphBuilders;
      this.setModelChanged();
    }

    // glyph builder factories

    public Glyph.Builder<? extends Glyph> glyphBuilder(ReadableFontData data) {
      Glyph.Builder<? extends Glyph> glyphBuilder = Glyph.Builder.getBuilder(this, data);
      return glyphBuilder;
    }


    // internal API for building

    @Override
    protected GlyphTable subBuildTable(ReadableFontData data) {
      return new GlyphTable(this.header(), data);
    }

    @Override
    protected void subDataSet() {
      this.glyphBuilders = null;
      super.setModelChanged(false);
    }

    @Override
    protected int subDataSizeToSerialize() {
      if (this.glyphBuilders == null || this.glyphBuilders.size() == 0) {
        return 0;
      }

      boolean variable = false;
      int size = 0;

      // calculate size of each table
      for (Glyph.Builder<? extends Glyph> b : this.glyphBuilders) {
        int glyphSize = b.subDataSizeToSerialize();
        size += Math.abs(glyphSize);
        variable |= glyphSize <= 0;
      }
      return variable ? -size : size;
    }

    @Override
    protected boolean subReadyToSerialize() {
      if (this.glyphBuilders == null) {
        return false;
      }
      // TODO(user): check glyphs for ready to build?
      return true;
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      int size = 0;
      for (Glyph.Builder<? extends Glyph> b : this.glyphBuilders) {
        size += b.subSerialize(newData.slice(size));
      }
      return size;
    }
  }

  public enum GlyphType {
    Simple,
    Composite;
  }

  public abstract static class Glyph extends SubTable {
    protected volatile boolean initialized = false;
    // TOO(stuartg): should we replace this with a shared lock? more contention
    // but less space
    protected final Object initializationLock = new Object();

    private final GlyphType glyphType;
    private final int numberOfContours;

    protected Glyph(ReadableFontData data, GlyphType glyphType) {
      super(data);
      this.glyphType = glyphType;

      if (this.data.length() == 0) {
        this.numberOfContours = 0;
      } else {
        // -1 if composite
        this.numberOfContours = this.data.readShort(Offset.numberOfContours.offset);
      }
    }

    protected Glyph(ReadableFontData data, int offset, int length, GlyphType glyphType) {
      super(data, offset, length);
      this.glyphType = glyphType;

      if (this.data.length() == 0) {
        this.numberOfContours = 0;
      } else {
        // -1 if composite
        this.numberOfContours = this.data.readShort(Offset.numberOfContours.offset);
      }
    }

    private static GlyphType glyphType(ReadableFontData data, int offset, int length) {
      if (length == 0) {
        return GlyphType.Simple;
      }
      int numberOfContours = data.readShort(offset);
      if (numberOfContours >= 0) {
        return GlyphType.Simple;
      }
      return GlyphType.Composite;
    }

    @SuppressWarnings("unchecked")
    private static <T extends GlyphTable.Glyph> T getGlyph(
        GlyphTable table, ReadableFontData data, int offset, int length) {
      GlyphType type = Glyph.glyphType(data, offset, length);
      if (type == GlyphType.Simple) {
        return (T) new SimpleGlyph(data, offset, length);
      }
      return (T) new CompositeGlyph(data, offset, length);
    }

    protected abstract void initialize();


    @Override
    public int padding() {
      this.initialize();
      return super.padding();
    }

    public GlyphType glyphType() {
      return this.glyphType;
    }

    public int numberOfContours() {
      return this.numberOfContours;
    }

    public int xMin() {
      return this.data.readShort(Offset.xMin.offset);
    }

    public int xMax() {
      return this.data.readShort(Offset.xMax.offset);
    }

    public int yMin() {
      return this.data.readShort(Offset.yMin.offset);
    }

    public int yMax() {
      return this.data.readShort(Offset.yMax.offset);
    }

    public abstract int instructionSize();

    public abstract ReadableFontData instructions();

    @Override
    public String toString() {
      return this.toString(0);
    }

    public String toString(int length) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.glyphType());
      sb.append(", contours=");
      sb.append(this.numberOfContours());
      sb.append(", [xmin=");
      sb.append(this.xMin());
      sb.append(", ymin=");
      sb.append(this.yMin());
      sb.append(", xmax=");
      sb.append(this.xMax());
      sb.append(", ymax=");
      sb.append(this.yMax());
      sb.append("]");
      sb.append("\n");
      return sb.toString();
    }

    // TODO(user): interface? need methods from Composite?
    public abstract static class Contour {
      protected Contour() {

      }
    }

    public abstract static class Builder<T extends Glyph> extends SubTable.Builder<T> {
      protected int format;

      protected Builder(WritableFontData data) {
        super(data);
      }

      protected Builder(ReadableFontData data) {
        super(data);
      }

      /**
       * @param data
       * @param offset
       * @param length
       */
      protected Builder(WritableFontData data, int offset, int length) {
        this(data.slice(offset, length));
      }

      @SuppressWarnings("unchecked")
      private static Glyph.Builder<? extends Glyph> getBuilder(
          GlyphTable.Builder tableBuilder, ReadableFontData data) {
        return Glyph.Builder.getBuilder(tableBuilder, data, 0, data.length());
      }

      private static Glyph.Builder<? extends Glyph> getBuilder(
          GlyphTable.Builder tableBuilder, ReadableFontData data, int offset, int length) {
        GlyphType type = Glyph.glyphType(data, offset, length);
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
        return this.internalReadData().length();
      }

      @Override
      protected boolean subReadyToSerialize() {
        return true;
      }

      @Override
      protected int subSerialize(WritableFontData newData) {
        return this.internalReadData().copyTo(newData);
      }
    }
  }

  public static final class SimpleGlyph extends Glyph {
    private static final int FLAG_ONCURVE = 0x01;
    private static final int FLAG_XSHORT = 0x01 << 1;
    private static final int FLAG_YSHORT = 0x01 << 2;
    private static final int FLAG_REPEAT = 0x01 << 3;
    private static final int FLAG_XREPEATSIGN = 0x01 << 4;
    private static final int FLAG_YREPEATSIGN = 0x01 << 5;

    private int instructionSize;
    private int numberOfPoints;

    // start offsets of the arrays
    private int instructionsOffset;
    private int flagsOffset;
    private int xCoordinatesOffset;
    private int yCoordinatesOffset;

    private int flagByteCount;
    private int xByteCount;
    @SuppressWarnings("unused")
    private int yByteCount;

    private int[] xCoordinates;
    private int[] yCoordinates;
    private boolean[] onCurve;
    private int[] contourIndex;

    public static final class SimpleContour extends Glyph.Contour {
      protected SimpleContour() {
        super();
      }
    }

    private SimpleGlyph(ReadableFontData data, int offset, int length) {
      super(data, offset, length, GlyphType.Simple);
    }

    private SimpleGlyph(ReadableFontData data) {
      super(data, GlyphType.Simple);
    }

    @Override
    protected void initialize() {
      if (this.initialized) {
        return;
      }
      synchronized (this.initializationLock) {
        if (this.initialized) {
          return;
        }

        if (this.readFontData().length() == 0) {
          this.instructionSize = 0;
          this.numberOfPoints = 0;
          this.instructionsOffset = 0;
          this.flagsOffset = 0;
          this.xCoordinatesOffset = 0;
          this.yCoordinatesOffset = 0;
          return;
        }
        this.instructionSize =
            this.data.readUShort(Offset.simpleEndPtsOfCountours.offset + this.numberOfContours()
                * FontData.DataSize.USHORT.size());
        this.instructionsOffset =
            Offset.simpleEndPtsOfCountours.offset + (this.numberOfContours() + 1)
                * FontData.DataSize.USHORT.size();
        this.flagsOffset =
            this.instructionsOffset + this.instructionSize * FontData.DataSize.BYTE.size();
        this.numberOfPoints = this.contourEndPoint(this.numberOfContours() - 1) + 1;
        this.xCoordinates = new int[this.numberOfPoints];
        this.yCoordinates = new int[this.numberOfPoints];
        this.onCurve = new boolean[this.numberOfPoints];
        parseData(false);
        this.xCoordinatesOffset =
            this.flagsOffset + this.flagByteCount * FontData.DataSize.BYTE.size();
        this.yCoordinatesOffset =
            this.xCoordinatesOffset + this.xByteCount * FontData.DataSize.BYTE.size();
        this.contourIndex = new int[this.numberOfContours() + 1];
        contourIndex[0] = 0;
        for (int contour = 0; contour < this.contourIndex.length - 1; contour++) {
          contourIndex[contour + 1] = this.contourEndPoint(contour) + 1;
        }
        parseData(true);
        int nonPaddedDataLength =
            5 * FontData.DataSize.SHORT.size()
                + (this.numberOfContours() * FontData.DataSize.USHORT.size())
                + FontData.DataSize.USHORT.size()
                + (this.instructionSize * FontData.DataSize.BYTE.size())
                + (flagByteCount * FontData.DataSize.BYTE.size())
                + (xByteCount * FontData.DataSize.BYTE.size())
                + (yByteCount * FontData.DataSize.BYTE.size());
        this.setPadding(this.dataLength() - nonPaddedDataLength);
        this.initialized = true;
      }
    }

    // TODO(user): think about replacing double parsing with ArrayList
    private void parseData(boolean fillArrays) {
      int flag = 0;
      int flagRepeat = 0;
      int flagIndex = 0;
      int xByteIndex = 0;
      int yByteIndex = 0;

      for (int pointIndex = 0; pointIndex < this.numberOfPoints; pointIndex++) {
        // get the flag for the current point
        if (flagRepeat == 0) {
          flag = this.flagAsInt(flagIndex++);
          if ((flag & FLAG_REPEAT) == FLAG_REPEAT) {
            flagRepeat = flagAsInt(flagIndex++);
          }
        } else {
          flagRepeat--;
        }

        // on the curve?
        if (fillArrays) {
          this.onCurve[pointIndex] = ((flag & FLAG_ONCURVE) == FLAG_ONCURVE) ? true : false;
        }
        // get the x coordinate
        if ((flag & FLAG_XSHORT) == FLAG_XSHORT) {
          // single byte x coord value
          if (fillArrays) {
            this.xCoordinates[pointIndex] =
                this.data.readUByte(this.xCoordinatesOffset + xByteIndex);
            this.xCoordinates[pointIndex] *=
                ((flag & FLAG_XREPEATSIGN) == FLAG_XREPEATSIGN) ? 1 : -1;
          }
          xByteIndex++;
        } else {
          // double byte coord value
          if (!((flag & FLAG_XREPEATSIGN) == FLAG_XREPEATSIGN)) {
            if (fillArrays) {
              this.xCoordinates[pointIndex] =
                  this.data.readShort(this.xCoordinatesOffset + xByteIndex);
            }
            xByteIndex += 2;
          }
        }
        if (fillArrays && pointIndex > 0) {
          this.xCoordinates[pointIndex] += this.xCoordinates[pointIndex - 1];
        }

        // get the y coordinate
        if ((flag & FLAG_YSHORT) == FLAG_YSHORT) {
          if (fillArrays) {
            this.yCoordinates[pointIndex] =
                this.data.readUByte(this.yCoordinatesOffset + yByteIndex);
            this.yCoordinates[pointIndex] *=
                ((flag & FLAG_YREPEATSIGN) == FLAG_YREPEATSIGN) ? 1 : -1;
          }
          yByteIndex++;
        } else {
          if (!((flag & FLAG_YREPEATSIGN) == FLAG_YREPEATSIGN)) {
            if (fillArrays) {
              this.yCoordinates[pointIndex] =
                  this.data.readShort(this.yCoordinatesOffset + yByteIndex);
            }
            yByteIndex += 2;
          }
        }
        if (fillArrays && pointIndex > 0) {
          this.yCoordinates[pointIndex] += this.yCoordinates[pointIndex - 1];
        }
      }
      this.flagByteCount = flagIndex;
      this.xByteCount = xByteIndex;
      this.yByteCount = yByteIndex;
    }

    private int flagAsInt(int index) {
      return this.data.readUByte(this.flagsOffset + index * FontData.DataSize.BYTE.size());
    }

    public int contourEndPoint(int contour) {
      return this.data.readUShort(
          contour * FontData.DataSize.USHORT.size() + Offset.simpleEndPtsOfCountours.offset);
    }

    @Override
    public int instructionSize() {
      this.initialize();
      return this.instructionSize;
    }

    @Override
    public ReadableFontData instructions() {
      this.initialize();
      return this.data.slice(this.instructionsOffset, this.instructionSize());
    }

    public int numberOfPoints(int contour) {
      this.initialize();
      if (contour >= this.numberOfContours()) {
        return 0;
      }
      return this.contourIndex[contour + 1] - this.contourIndex[contour];
    }

    public int xCoordinate(int contour, int point) {
      this.initialize();
      return this.xCoordinates[this.contourIndex[contour] + point];
    }

    public int yCoordinate(int contour, int point) {
      this.initialize();
      return this.yCoordinates[this.contourIndex[contour] + point];
    }

    public boolean onCurve(int contour, int point) {
      this.initialize();
      return this.onCurve[this.contourIndex[contour] + point];
    }

    @Override
    public String toString() {
      this.initialize();
      StringBuilder sb = new StringBuilder(super.toString());
      sb.append("\tinstruction bytes = " + this.instructionSize() + "\n");
      for (int contour = 0; contour < this.numberOfContours(); contour++) {
        for (int point = 0; point < this.numberOfPoints(contour); point++) {
          sb.append("\t" + contour + ":" + point + " = [" + this.xCoordinate(contour, point) + ", "
              + this.yCoordinate(contour, point) + ", " + this.onCurve(contour, point) + "]\n");
        }
      }
      return sb.toString();
    }

    public static class SimpleGlyphBuilder extends Glyph.Builder<SimpleGlyph> {
      protected SimpleGlyphBuilder(WritableFontData data, int offset, int length) {
        super(data.slice(offset, length));
      }

      protected SimpleGlyphBuilder(ReadableFontData data, int offset, int length) {
        super(data.slice(offset, length));
      }

      @Override
      protected SimpleGlyph subBuildTable(ReadableFontData data) {
        return new SimpleGlyph(data, 0, data.length());
      }
    }
  }

  public static final class CompositeGlyph extends Glyph {
    public static final int FLAG_ARG_1_AND_2_ARE_WORDS = 0x01;
    public static final int FLAG_ARGS_ARE_XY_VALUES = 0x01 << 1;
    public static final int FLAG_ROUND_XY_TO_GRID = 0x01 << 2;
    public static final int FLAG_WE_HAVE_A_SCALE = 0x01 << 3;
    public static final int FLAG_RESERVED = 0x01 << 4;
    public static final int FLAG_MORE_COMPONENTS = 0x01 << 5;
    public static final int FLAG_WE_HAVE_AN_X_AND_Y_SCALE = 0x01 << 6;
    public static final int FLAG_WE_HAVE_A_TWO_BY_TWO = 0x01 << 7;
    public static final int FLAG_WE_HAVE_INSTRUCTIONS = 0x01 << 8;
    public static final int FLAG_USE_MY_METRICS = 0x01 << 9;
    public static final int FLAG_OVERLAP_COMPOUND = 0x01 << 10;
    public static final int FLAG_SCALED_COMPONENT_OFFSET = 0x01 << 11;
    public static final int FLAG_UNSCALED_COMPONENT_OFFSET = 0x01 << 12;

    private final List<Integer> contourIndex = new LinkedList<Integer>();
    private int instructionsOffset;
    private int instructionSize;

    protected CompositeGlyph(ReadableFontData data, int offset, int length) {
      super(data, offset, length, GlyphType.Composite);
      initialize();
    }

    protected CompositeGlyph(ReadableFontData data) {
      super(data, GlyphType.Composite);
      initialize();
    }

    @Override
    protected void initialize() {
      if (this.initialized) {
        return;
      }
      synchronized (this.initializationLock) {
        if (this.initialized) {
          return;
        }

        int index = 5 * FontData.DataSize.USHORT.size(); // header
        int flags = FLAG_MORE_COMPONENTS;
        while ((flags & FLAG_MORE_COMPONENTS) == FLAG_MORE_COMPONENTS) {
          contourIndex.add(index);
          flags = this.data.readUShort(index);
          index += 2 * FontData.DataSize.USHORT.size(); // flags and
          // glyphIndex
          if ((flags & FLAG_ARG_1_AND_2_ARE_WORDS) == FLAG_ARG_1_AND_2_ARE_WORDS) {
            index += 2 * FontData.DataSize.SHORT.size();
          } else {
            index += 2 * FontData.DataSize.BYTE.size();
          }
          if ((flags & FLAG_WE_HAVE_A_SCALE) == FLAG_WE_HAVE_A_SCALE) {
            index += FontData.DataSize.F2DOT14.size();
          } else if ((flags & FLAG_WE_HAVE_AN_X_AND_Y_SCALE) == FLAG_WE_HAVE_AN_X_AND_Y_SCALE) {
            index += 2 * FontData.DataSize.F2DOT14.size();
          } else if ((flags & FLAG_WE_HAVE_A_TWO_BY_TWO) == FLAG_WE_HAVE_A_TWO_BY_TWO) {
            index += 4 * FontData.DataSize.F2DOT14.size();
          }
        }
        int nonPaddedDataLength = index;
        if ((flags & FLAG_WE_HAVE_INSTRUCTIONS) == FLAG_WE_HAVE_INSTRUCTIONS) {
          this.instructionSize = this.data.readUShort(index);
          index += FontData.DataSize.USHORT.size();
          this.instructionsOffset = index;
          nonPaddedDataLength = index + (this.instructionSize * FontData.DataSize.BYTE.size());
        }
        this.setPadding(this.dataLength() - nonPaddedDataLength);
      }
    }

    public int flags(int contour) {
      return this.data.readUShort(this.contourIndex.get(contour));
    }

    public int numGlyphs() {
      return this.contourIndex.size();
    }

    public int glyphIndex(int contour) {
      return this.data.readUShort(FontData.DataSize.USHORT.size() + this.contourIndex.get(contour));
    }

    public int argument1(int contour) {
      int index = 2 * FontData.DataSize.USHORT.size() + this.contourIndex.get(contour);
      int flags = this.flags(contour);
      if ((flags & FLAG_ARG_1_AND_2_ARE_WORDS) == FLAG_ARG_1_AND_2_ARE_WORDS) {
        return this.data.readUShort(index);
      }
      return this.data.readByte(index);
    }

    public int argument2(int contour) {
      int index = 2 * FontData.DataSize.USHORT.size() + this.contourIndex.get(contour);
      int flags = this.flags(contour);
      if ((flags & FLAG_ARG_1_AND_2_ARE_WORDS) == FLAG_ARG_1_AND_2_ARE_WORDS) {
        return this.data.readUShort(index + FontData.DataSize.USHORT.size());
      }
      return this.data.readByte(index + FontData.DataSize.BYTE.size());
    }

    public int transformationSize(int contour) {
      int flags = this.flags(contour);
      if ((flags & FLAG_WE_HAVE_A_SCALE) == FLAG_WE_HAVE_A_SCALE) {
        return FontData.DataSize.F2DOT14.size();
      } else if ((flags & FLAG_WE_HAVE_AN_X_AND_Y_SCALE) == FLAG_WE_HAVE_AN_X_AND_Y_SCALE) {
        return 2 * FontData.DataSize.F2DOT14.size();
      } else if ((flags & FLAG_WE_HAVE_A_TWO_BY_TWO) == FLAG_WE_HAVE_A_TWO_BY_TWO) {
        return 4 * FontData.DataSize.F2DOT14.size();
      }
      return 0;
    }

    public byte[] transformation(int contour) {
      int flags = this.flags(contour);
      int index = this.contourIndex.get(contour) + 2 * FontData.DataSize.USHORT.size();
      if ((flags & FLAG_ARG_1_AND_2_ARE_WORDS) == FLAG_ARG_1_AND_2_ARE_WORDS) {
        index += 2 * FontData.DataSize.SHORT.size();
      } else {
        index += 2 * FontData.DataSize.BYTE.size();
      }

      int tsize = transformationSize(contour);
      byte[] transformation = new byte[tsize];
      this.data.readBytes(index, transformation, 0, tsize);
      return transformation;
    }

    @Override
    public int instructionSize() {
      return this.instructionSize;
    }

    @Override
    public ReadableFontData instructions() {
      return this.data.slice(this.instructionsOffset, this.instructionSize());
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder(super.toString());
      sb.append("\ncontourOffset.length = ");
      sb.append(this.contourIndex.size());
      sb.append("\ninstructionSize = ");
      sb.append(this.instructionSize);
      sb.append("\n\tcontour index = [");
      for (int contour = 0; contour < this.contourIndex.size(); contour++) {
        if (contour != 0) {
          sb.append(", ");
        }
        sb.append(this.contourIndex.get(contour));
      }
      sb.append("]\n");
      for (int contour = 0; contour < this.contourIndex.size(); contour++) {
        sb.append("\t" + contour + " = [gid = " + this.glyphIndex(contour) + ", arg1 = "
            + this.argument1(contour) + ", arg2 = " + this.argument2(contour) + "]\n");
      }
      return sb.toString();
    }

    public static class CompositeGlyphBuilder extends Glyph.Builder<CompositeGlyph> {
      protected CompositeGlyphBuilder(WritableFontData data, int offset, int length) {
        super(data.slice(offset, length));
      }

      protected CompositeGlyphBuilder(ReadableFontData data, int offset, int length) {
        super(data.slice(offset, length));
      }

      @Override
      protected CompositeGlyph subBuildTable(ReadableFontData data) {
        return new CompositeGlyph(data);
      }
    }
  }
}

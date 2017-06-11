package com.google.typography.font.sfntly.table.truetype;

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.truetype.GlyphTable.Offset;

public final class SimpleGlyph extends Glyph {
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

  SimpleGlyph(ReadableFontData data, int offset, int length) {
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
              * FontData.SizeOf.USHORT);
      this.instructionsOffset =
          Offset.simpleEndPtsOfCountours.offset + (this.numberOfContours() + 1)
              * FontData.SizeOf.USHORT;
      this.flagsOffset =
          this.instructionsOffset + this.instructionSize * FontData.SizeOf.BYTE;
      this.numberOfPoints = this.contourEndPoint(this.numberOfContours() - 1) + 1;
      this.xCoordinates = new int[this.numberOfPoints];
      this.yCoordinates = new int[this.numberOfPoints];
      this.onCurve = new boolean[this.numberOfPoints];
      parseData(false);
      this.xCoordinatesOffset = this.flagsOffset + this.flagByteCount * FontData.SizeOf.BYTE;
      this.yCoordinatesOffset = this.xCoordinatesOffset + this.xByteCount * FontData.SizeOf.BYTE;
      this.contourIndex = new int[this.numberOfContours() + 1];
      contourIndex[0] = 0;
      for (int contour = 0; contour < this.contourIndex.length - 1; contour++) {
        contourIndex[contour + 1] = this.contourEndPoint(contour) + 1;
      }
      parseData(true);
      int nonPaddedDataLength =
          5 * FontData.SizeOf.SHORT
              + (this.numberOfContours() * FontData.SizeOf.USHORT)
              + FontData.SizeOf.USHORT
              + (this.instructionSize * FontData.SizeOf.BYTE)
              + (flagByteCount * FontData.SizeOf.BYTE)
              + (xByteCount * FontData.SizeOf.BYTE)
              + (yByteCount * FontData.SizeOf.BYTE);
      this.setPadding(this.dataLength() - nonPaddedDataLength);
      this.initialized = true;
    }
  }

  // TODO(stuartg): think about replacing double parsing with ArrayList
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
        if ((flag & FLAG_REPEAT) != 0) {
          flagRepeat = flagAsInt(flagIndex++);
        }
      } else {
        flagRepeat--;
      }

      // on the curve?
      if (fillArrays) {
        this.onCurve[pointIndex] = (flag & FLAG_ONCURVE) != 0;
      }
      // get the x coordinate
      if ((flag & FLAG_XSHORT) != 0) {
        // single byte x coord value
        if (fillArrays) {
          int sign = ((flag & FLAG_XREPEATSIGN) != 0) ? 1 : -1;
          int magnitude = this.data.readUByte(this.xCoordinatesOffset + xByteIndex);
          this.xCoordinates[pointIndex] = sign * magnitude;
        }
        xByteIndex++;
      } else {
        // double byte coord value
        if ((flag & FLAG_XREPEATSIGN) == 0) {
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
      if ((flag & FLAG_YSHORT) != 0) {
        if (fillArrays) {
          this.yCoordinates[pointIndex] =
              this.data.readUByte(this.yCoordinatesOffset + yByteIndex);
          this.yCoordinates[pointIndex] *= ((flag & FLAG_YREPEATSIGN) != 0) ? 1 : -1;
        }
        yByteIndex++;
      } else {
        if ((flag & FLAG_YREPEATSIGN) == 0) {
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
    return this.data.readUByte(this.flagsOffset + index * FontData.SizeOf.BYTE);
  }

  public int contourEndPoint(int contour) {
    return this.data.readUShort(
        contour * FontData.SizeOf.USHORT + Offset.simpleEndPtsOfCountours.offset);
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

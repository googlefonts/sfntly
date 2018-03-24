package com.google.typography.font.sfntly.table.truetype;

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

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
    if (initialized) {
      return;
    }
    synchronized (initializationLock) {
      if (initialized) {
        return;
      }

      if (readFontData().length() == 0) {
        this.instructionSize = 0;
        this.numberOfPoints = 0;
        this.instructionsOffset = 0;
        this.flagsOffset = 0;
        this.xCoordinatesOffset = 0;
        this.yCoordinatesOffset = 0;
        return;
      }
      this.instructionSize =
          data.readUShort(
              GlyphTable.Offset.simpleEndPtsOfCountours
                  + numberOfContours() * FontData.SizeOf.USHORT);
      this.instructionsOffset =
          GlyphTable.Offset.simpleEndPtsOfCountours
              + (numberOfContours() + 1) * FontData.SizeOf.USHORT;
      this.flagsOffset = instructionsOffset + instructionSize * FontData.SizeOf.BYTE;
      this.numberOfPoints = contourEndPoint(numberOfContours() - 1) + 1;
      this.xCoordinates = new int[numberOfPoints];
      this.yCoordinates = new int[numberOfPoints];
      this.onCurve = new boolean[numberOfPoints];
      parseData(false);
      this.xCoordinatesOffset = flagsOffset + flagByteCount * FontData.SizeOf.BYTE;
      this.yCoordinatesOffset = xCoordinatesOffset + xByteCount * FontData.SizeOf.BYTE;
      this.contourIndex = new int[numberOfContours() + 1];
      contourIndex[0] = 0;
      for (int contour = 0; contour < contourIndex.length - 1; contour++) {
        contourIndex[contour + 1] = contourEndPoint(contour) + 1;
      }
      parseData(true);
      int nonPaddedDataLength =
          5 * FontData.SizeOf.SHORT
              + (numberOfContours() * FontData.SizeOf.USHORT)
              + FontData.SizeOf.USHORT
              + (instructionSize * FontData.SizeOf.BYTE)
              + (flagByteCount * FontData.SizeOf.BYTE)
              + (xByteCount * FontData.SizeOf.BYTE)
              + (yByteCount * FontData.SizeOf.BYTE);
      setPadding(dataLength() - nonPaddedDataLength);
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

    for (int pointIndex = 0; pointIndex < numberOfPoints; pointIndex++) {
      // get the flag for the current point
      if (flagRepeat == 0) {
        flag = flagAsInt(flagIndex++);
        if ((flag & FLAG_REPEAT) != 0) {
          flagRepeat = flagAsInt(flagIndex++);
        }
      } else {
        flagRepeat--;
      }

      // on the curve?
      if (fillArrays) {
        onCurve[pointIndex] = (flag & FLAG_ONCURVE) != 0;
      }
      // get the x coordinate
      if ((flag & FLAG_XSHORT) != 0) {
        // single byte x coord value
        if (fillArrays) {
          int sign = ((flag & FLAG_XREPEATSIGN) != 0) ? 1 : -1;
          int magnitude = data.readUByte(xCoordinatesOffset + xByteIndex);
          xCoordinates[pointIndex] = sign * magnitude;
        }
        xByteIndex++;
      } else {
        // double byte coord value
        if ((flag & FLAG_XREPEATSIGN) == 0) {
          if (fillArrays) {
            xCoordinates[pointIndex] = data.readShort(xCoordinatesOffset + xByteIndex);
          }
          xByteIndex += 2;
        }
      }
      if (fillArrays && pointIndex > 0) {
        xCoordinates[pointIndex] += xCoordinates[pointIndex - 1];
      }

      // get the y coordinate
      if ((flag & FLAG_YSHORT) != 0) {
        if (fillArrays) {
          yCoordinates[pointIndex] = data.readUByte(yCoordinatesOffset + yByteIndex);
          yCoordinates[pointIndex] *= ((flag & FLAG_YREPEATSIGN) != 0) ? 1 : -1;
        }
        yByteIndex++;
      } else {
        if ((flag & FLAG_YREPEATSIGN) == 0) {
          if (fillArrays) {
            yCoordinates[pointIndex] = data.readShort(yCoordinatesOffset + yByteIndex);
          }
          yByteIndex += 2;
        }
      }
      if (fillArrays && pointIndex > 0) {
        yCoordinates[pointIndex] += yCoordinates[pointIndex - 1];
      }
    }
    this.flagByteCount = flagIndex;
    this.xByteCount = xByteIndex;
    this.yByteCount = yByteIndex;
  }

  private int flagAsInt(int index) {
    return data.readUByte(flagsOffset + index * FontData.SizeOf.BYTE);
  }

  public int contourEndPoint(int contour) {
    return data.readUShort(
        contour * FontData.SizeOf.USHORT + GlyphTable.Offset.simpleEndPtsOfCountours);
  }

  @Override
  public int instructionSize() {
    initialize();
    return instructionSize;
  }

  @Override
  public ReadableFontData instructions() {
    initialize();
    return data.slice(instructionsOffset, instructionSize());
  }

  public int numberOfPoints(int contour) {
    initialize();
    if (contour >= numberOfContours()) {
      return 0;
    }
    return contourIndex[contour + 1] - contourIndex[contour];
  }

  public int xCoordinate(int contour, int point) {
    initialize();
    return xCoordinates[contourIndex[contour] + point];
  }

  public int yCoordinate(int contour, int point) {
    initialize();
    return yCoordinates[contourIndex[contour] + point];
  }

  public boolean onCurve(int contour, int point) {
    initialize();
    return onCurve[contourIndex[contour] + point];
  }

  @Override
  public String toString() {
    initialize();
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append(String.format("\tinstruction bytes = %d\n", instructionSize()));
    for (int contour = 0; contour < numberOfContours(); contour++) {
      for (int point = 0; point < numberOfPoints(contour); point++) {
        sb.append(
            String.format(
                "\t%d:%d = [%d, %d, %s]\n",
                contour,
                point,
                xCoordinate(contour, point),
                yCoordinate(contour, point),
                onCurve(contour, point)));
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

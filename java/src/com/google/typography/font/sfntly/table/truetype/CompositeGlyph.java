package com.google.typography.font.sfntly.table.truetype;

import com.google.typography.font.sfntly.data.FontData;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import java.util.ArrayList;
import java.util.List;

public final class CompositeGlyph extends Glyph {
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

  private final List<Integer> contourIndex = new ArrayList<>();
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
    if (initialized) {
      return;
    }
    synchronized (initializationLock) {
      if (initialized) {
        return;
      }

      int index = 5 * FontData.SizeOf.USHORT; // header
      int flags = FLAG_MORE_COMPONENTS;
      while ((flags & FLAG_MORE_COMPONENTS) != 0) {
        contourIndex.add(index);
        flags = data.readUShort(index);
        index += 2 * FontData.SizeOf.USHORT; // flags and glyphIndex
        if ((flags & FLAG_ARG_1_AND_2_ARE_WORDS) != 0) {
          index += 2 * FontData.SizeOf.SHORT;
        } else {
          index += 2 * FontData.SizeOf.BYTE;
        }
        if ((flags & FLAG_WE_HAVE_A_SCALE) != 0) {
          index += FontData.SizeOf.F2DOT14;
        } else if ((flags & FLAG_WE_HAVE_AN_X_AND_Y_SCALE) != 0) {
          index += 2 * FontData.SizeOf.F2DOT14;
        } else if ((flags & FLAG_WE_HAVE_A_TWO_BY_TWO) != 0) {
          index += 4 * FontData.SizeOf.F2DOT14;
        }
      }
      int nonPaddedDataLength = index;
      if ((flags & FLAG_WE_HAVE_INSTRUCTIONS) != 0) {
        this.instructionSize = data.readUShort(index);
        index += FontData.SizeOf.USHORT;
        this.instructionsOffset = index;
        nonPaddedDataLength = index + (instructionSize * FontData.SizeOf.BYTE);
      }
      setPadding(dataLength() - nonPaddedDataLength);
    }
  }

  public int flags(int contour) {
    return data.readUShort(contourIndex.get(contour));
  }

  public int numGlyphs() {
    return contourIndex.size();
  }

  public int glyphIndex(int contour) {
    return data.readUShort(FontData.SizeOf.USHORT + contourIndex.get(contour));
  }

  public int argument1(int contour) {
    int index = 2 * FontData.SizeOf.USHORT + contourIndex.get(contour);
    int flags = flags(contour);
    if ((flags & FLAG_ARG_1_AND_2_ARE_WORDS) != 0) {
      return (flags & FLAG_ARGS_ARE_XY_VALUES) != 0
          ? data.readShort(index)
          : data.readUShort(index);
    }
    return data.readByte(index);
  }

  public int argument2(int contour) {
    int index = 2 * FontData.SizeOf.USHORT + contourIndex.get(contour);
    int flags = flags(contour);
    if ((flags & FLAG_ARG_1_AND_2_ARE_WORDS) != 0) {
      return (flags & FLAG_ARGS_ARE_XY_VALUES) != 0
          ? data.readShort(index + FontData.SizeOf.SHORT)
          : data.readUShort(index + FontData.SizeOf.USHORT);
    }
    return data.readByte(index + FontData.SizeOf.BYTE);
  }

  public int transformationSize(int contour) {
    int flags = flags(contour);
    if ((flags & FLAG_WE_HAVE_A_SCALE) != 0) {
      return FontData.SizeOf.F2DOT14;
    } else if ((flags & FLAG_WE_HAVE_AN_X_AND_Y_SCALE) != 0) {
      return 2 * FontData.SizeOf.F2DOT14;
    } else if ((flags & FLAG_WE_HAVE_A_TWO_BY_TWO) != 0) {
      return 4 * FontData.SizeOf.F2DOT14;
    }
    return 0;
  }

  public byte[] transformation(int contour) {
    int flags = flags(contour);
    int index = contourIndex.get(contour) + 2 * FontData.SizeOf.USHORT;
    if ((flags & FLAG_ARG_1_AND_2_ARE_WORDS) != 0) {
      index += 2 * FontData.SizeOf.SHORT;
    } else {
      index += 2 * FontData.SizeOf.BYTE;
    }

    int tsize = transformationSize(contour);
    byte[] transformation = new byte[tsize];
    data.readBytes(index, transformation, 0, tsize);
    return transformation;
  }

  @Override
  public int instructionSize() {
    return instructionSize;
  }

  @Override
  public ReadableFontData instructions() {
    return data.slice(instructionsOffset, instructionSize());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            "%s\ncontourOffset.length = %d\ninstructionSize = %d\n",
            super.toString(), contourIndex.size(), instructionSize));
    sb.append("\tcontour index = [");
    for (int contour = 0; contour < contourIndex.size(); contour++) {
      if (contour != 0) {
        sb.append(", ");
      }
      sb.append(contourIndex.get(contour));
    }
    sb.append("]\n");
    for (int contour = 0; contour < contourIndex.size(); contour++) {
      sb.append(
          String.format(
              "\t%d = [gid = %d, arg1 = %d, arg2 = %d]\n",
              contour, glyphIndex(contour), argument1(contour), argument2(contour)));
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

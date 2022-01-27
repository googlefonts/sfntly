package com.google.typography.font.sfntly.table.truetype;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.testutils.TestUtils;
import org.junit.Test;

public class CompositeGlyphTest {

  @Test
  public void singleSimpleGlyph_signed_xy() {
    ReadableFontData data =
        TestUtils.fromHex( //
            "FFFF", // numberOfContours
            "0000 0000 0300 0400", // xMin, yMin, xMax, yMax
            "0003 0007", // flags, glyphIndex
            "FFF5 FFF7"); // argument1, argument2
    CompositeGlyph glyph = new CompositeGlyph(data);

    assertThat(glyph.glyphType()).isEqualTo(Glyph.GlyphType.Composite);
    assertThat(glyph.xMin()).isEqualTo(0);
    assertThat(glyph.yMin()).isEqualTo(0);
    assertThat(glyph.xMax()).isEqualTo(0x0300);
    assertThat(glyph.yMax()).isEqualTo(0x0400);
    assertThat(glyph.flags(0)).isEqualTo(0x0003);
    assertThat(glyph.argument1(0)).isEqualTo(-11);
    assertThat(glyph.argument2(0)).isEqualTo(-9);
  }

  @Test
  public void singleSimpleGlyph_unsigned_xy() {
    ReadableFontData data =
        TestUtils.fromHex( //
            "FFFF", // numberOfContours
            "0000 0000 0300 0400", // xMin, yMin, xMax, yMax
            "0001 0007", // flags, glyphIndex
            "FFF5 FFF7"); // argument1, argument2
    CompositeGlyph glyph = new CompositeGlyph(data);

    assertThat(glyph.glyphType()).isEqualTo(Glyph.GlyphType.Composite);
    assertThat(glyph.xMin()).isEqualTo(0);
    assertThat(glyph.yMin()).isEqualTo(0);
    assertThat(glyph.xMax()).isEqualTo(0x0300);
    assertThat(glyph.yMax()).isEqualTo(0x0400);
    assertThat(glyph.flags(0)).isEqualTo(0x0001);
    assertThat(glyph.argument1(0)).isEqualTo(0xFFF5);
    assertThat(glyph.argument2(0)).isEqualTo(0xFFF7);
  }
}

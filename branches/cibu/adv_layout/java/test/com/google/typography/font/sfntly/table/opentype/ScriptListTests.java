// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import static org.junit.Assert.assertEquals;

import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.opentype.langsystable.Header;

import org.junit.Test;

/**
 * Tests ScriptList
 */
public class ScriptListTests {

  @Test
  public void testCreateEditor1() {
    ScriptListTable.Builder slBuilder = new ScriptListTable.Builder();
    assertEquals(0, slBuilder.subTableCount());

    ScriptTable.Builder stBuilder = (ScriptTable.Builder)slBuilder.addBuiderForTag(ScriptTag.latn.tag()); 
    assertEquals(1, slBuilder.subTableCount());
    assertEquals(0, stBuilder.subTableCount());

    LangSysTable.Builder lsBuilder = (LangSysTable.Builder) stBuilder.addBuiderForTag(LanguageTag.ENG.tag()); 
    assertEquals(1, stBuilder.subTableCount());
    assertEquals(0, lsBuilder.featureIndexCount());

    lsBuilder.addFeatureIndices(0, 2);
    assertEquals(2, lsBuilder.featureIndexCount());

    ScriptListTable slTable = slBuilder.build();
    System.out.println(slTable);

    int length = slBuilder.subDataSizeToSerialize();
    System.out.println("length: " + length);

    WritableFontData out = WritableFontData.createWritableFontData(length);
    int written = slBuilder.subSerialize(out);
    System.out.println("written: " + written);

    for (int i = 0; i < written; ++i) {
      byte b = (byte)out.readByte(i);
      char c = (b >= 0x20 && b < 0x7f) ? (char)b : '?';
      System.out.format("%3d: %02x %3d '%c'\n", i, b, b, c);
    }
  }
}

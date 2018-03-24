/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.typography.font.sfntly;

import com.google.typography.font.sfntly.table.core.OS2Table;
import com.google.typography.font.sfntly.testutils.TestFont;
import com.google.typography.font.sfntly.testutils.TestFontUtils;
import com.google.typography.font.sfntly.testutils.TestUtils;
import java.io.File;
import java.util.EnumSet;
import junit.framework.TestCase;

/** @author Stuart Gill */
public class OS2Tests extends TestCase {

  private static final File TEST_FONT_FILE = TestFont.TestFontNames.OPENSANS.getFile();

  public OS2Tests() {}

  public OS2Tests(String name) {
    super(name);
  }

  private static final byte[] achVendId_a = {'a'};
  private static final byte[] achVendId_a_pad = {'a', ' ', ' ', ' '};
  private static final byte[] achVendId_abcd = {'a', 'b', 'c', 'd'};

  public void testAchVendId() throws Exception {
    Font.Builder fontBuilder = TestFontUtils.builderForFontFile(TEST_FONT_FILE);
    OS2Table.Builder os2TableBuilder = (OS2Table.Builder) fontBuilder.getTableBuilder(Tag.OS_2);

    os2TableBuilder.setAchVendId(achVendId_a);
    assertTrue(
        TestUtils.equals(
            achVendId_a_pad, 0, os2TableBuilder.achVendId(), 0, achVendId_a_pad.length));

    os2TableBuilder.setAchVendId(achVendId_abcd);
    assertTrue(
        TestUtils.equals(achVendId_abcd, 0, os2TableBuilder.achVendId(), 0, achVendId_abcd.length));

    os2TableBuilder.setAchVendId(achVendId_a);
    assertTrue(
        TestUtils.equals(
            achVendId_a_pad, 0, os2TableBuilder.achVendId(), 0, achVendId_a_pad.length));
  }

  public void testUnicodeRange() throws Exception {
    EnumSet<OS2Table.UnicodeRange> urSet = makeUnicodeRangeSet(false);
    long[] urArray = OS2Table.UnicodeRange.asArray(urSet);
    EnumSet<OS2Table.UnicodeRange> urSetCopy =
        OS2Table.UnicodeRange.asSet(urArray[0], urArray[1], urArray[2], urArray[3]);
    assertEquals(urSet, urSetCopy);
  }

  public void testCodePageRange() throws Exception {
    EnumSet<OS2Table.CodePageRange> urSet = makeCodePageRangeSet(false);
    long[] cprArray = OS2Table.CodePageRange.asArray(urSet);
    EnumSet<OS2Table.CodePageRange> urSetCopy =
        OS2Table.CodePageRange.asSet(cprArray[0], cprArray[1]);
    assertEquals(urSet, urSetCopy);
  }

  private static EnumSet<OS2Table.UnicodeRange> makeUnicodeRangeSet(boolean odd) {
    EnumSet<OS2Table.UnicodeRange> rSet = EnumSet.noneOf(OS2Table.UnicodeRange.class);

    for (OS2Table.UnicodeRange r : OS2Table.UnicodeRange.values()) {
      if (odd) {
        rSet.add(r);
      }
      odd = odd ? false : true;
    }
    return rSet;
  }

  private static EnumSet<OS2Table.CodePageRange> makeCodePageRangeSet(boolean odd) {
    EnumSet<OS2Table.CodePageRange> rSet = EnumSet.noneOf(OS2Table.CodePageRange.class);

    for (OS2Table.CodePageRange r : OS2Table.CodePageRange.values()) {
      if (odd) {
        rSet.add(r);
      }
      odd = odd ? false : true;
    }
    return rSet;
  }
}

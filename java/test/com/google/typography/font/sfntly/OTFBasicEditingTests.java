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

package com.google.typography.font.sfntly;

import com.google.typography.font.sfntly.Font.Builder;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.core.FontHeaderTable;
import com.google.typography.font.sfntly.testutils.TestFont;
import com.google.typography.font.sfntly.testutils.TestFontUtils;

import junit.framework.TestCase;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Stuart Gill
 *
 */
public class OTFBasicEditingTests extends TestCase {

  private static final File TEST_FONT_FILE = TestFont.TestFontNames.OPENSANS.getFile();

  public OTFBasicEditingTests(String name) {
    super(name);
  }

  public void testBuildersToTables() throws Exception {
    Builder fontBuilder = TestFontUtils.builderForFontFile(TEST_FONT_FILE);
    Set<Integer> builderTags = new HashSet<Integer>(fontBuilder.tableBuilderMap().keySet());
    FontHeaderTable.Builder headerBuilder = 
      (FontHeaderTable.Builder) fontBuilder.getTableBuilder(Tag.head);
    long modDate = headerBuilder.modified();
    headerBuilder.setModified(modDate + 1);
    Font font = fontBuilder.build();

    // ensure that every table hada builder
    Iterator<? extends Table> iter = font.iterator();
    while (iter.hasNext()) {
      Table table = iter.next();
      Header header = table.header();
      assertTrue(builderTags.contains(header.tag()));
      builderTags.remove(header.tag());
    }
    // ensure that every builder turned into a table
    assertTrue(builderTags.isEmpty());

    FontHeaderTable header = font.getTable(Tag.head);
    long afterModDate = header.modified();
    assertEquals(modDate + 1, afterModDate);
  }
}

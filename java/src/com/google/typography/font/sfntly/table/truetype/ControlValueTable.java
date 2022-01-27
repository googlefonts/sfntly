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
import com.google.typography.font.sfntly.table.ByteArrayTableBuilder;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.Table;

/**
 * A Control Value table.
 *
 * @author Stuart Gill
 */
public final class ControlValueTable extends Table {

  protected ControlValueTable(Header header, ReadableFontData data) {
    super(header, data);
  }

  /** Get the data value at the specified index. */
  public int fword(int index) {
    return data.readFWord(index);
  }

  /** Get the number of FWORDs in the data. */
  public int fwordCount() {
    return dataLength() / FontData.SizeOf.FWORD;
  }

  public static class Builder extends ByteArrayTableBuilder<ControlValueTable> {

    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    protected Builder(Header header, WritableFontData data) {
      super(header, data);
    }

    protected Builder(Header header, ReadableFontData data) {
      super(header, data);
    }

    @Override
    protected ControlValueTable subBuildTable(ReadableFontData data) {
      return new ControlValueTable(header(), data);
    }
  }
}

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

package com.google.typography.font.sfntly.table.bitmap;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.SfObjects;
import com.google.typography.font.sfntly.data.WritableFontData;

/** @author Stuart Gill */
public class CompositeBitmapGlyph extends BitmapGlyph {

  public static final class Component {
    private final int glyphCode;
    private final int xOffset;
    private final int yOffset;

    protected Component(int glyphCode, int xOffset, int yOffset) {
      this.glyphCode = glyphCode;
      this.xOffset = xOffset;
      this.yOffset = yOffset;
    }

    public int glyphCode() {
      return glyphCode;
    }

    public int xOffset() {
      return xOffset;
    }

    public int yOffset() {
      return yOffset;
    }

    @Override
    public int hashCode() {
      return SfObjects.hash(glyphCode);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof Component)) {
        return false;
      }
      Component other = (Component) obj;
      return glyphCode == other.glyphCode;
    }
  }

  private int numComponentsOffset;
  private int componentArrayOffset;

  protected CompositeBitmapGlyph(ReadableFontData data, int format) {
    super(data, format);
    initialize(format);
  }

  /**
   * Initializes the internal state from the data.
   *
   * @param format the glyph format
   */
  private void initialize(int format) {
    if (format == 8) {
      this.numComponentsOffset = Offset.glyphFormat8_numComponents;
      this.componentArrayOffset = Offset.glyphFormat8_componentArray;
    } else if (format == 9) {
      this.numComponentsOffset = Offset.glyphFormat9_numComponents;
      this.componentArrayOffset = Offset.glyphFormat9_componentArray;
    } else {
      throw new IllegalStateException(
          "Attempt to create a Composite Bitmap Glyph with a non-composite format.");
    }
  }

  public int numComponents() {
    return data.readUShort(numComponentsOffset);
  }

  public Component component(int componentNum) {
    int componentOffset = componentArrayOffset + componentNum * Offset.ebdtComponentLength;
    return new Component(
        data.readUShort(componentOffset + Offset.ebdtComponent_glyphCode),
        data.readChar(componentOffset + Offset.ebdtComponent_xOffset),
        data.readChar(componentOffset + Offset.ebdtComponent_yOffset));
  }

  public static class Builder extends BitmapGlyph.Builder<CompositeBitmapGlyph> {

    protected Builder(WritableFontData data, int format) {
      super(data, format);
    }

    protected Builder(ReadableFontData data, int format) {
      super(data, format);
    }

    @Override
    protected CompositeBitmapGlyph subBuildTable(ReadableFontData data) {
      return new CompositeBitmapGlyph(data, format());
    }
  }
}

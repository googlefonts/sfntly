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
package com.google.typography.font.tools.fontinfo;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.Font.PlatformId;
import com.google.typography.font.sfntly.Font.WindowsEncodingId;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.core.CMap;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.sfntly.table.core.NameTable;
import com.google.typography.font.sfntly.table.core.NameTable.NameId;
import com.google.typography.font.sfntly.table.core.NameTable.WindowsLanguageId;
import com.google.typography.font.sfntly.table.truetype.Glyph;
import com.google.typography.font.sfntly.table.truetype.Glyph.GlyphType;
import com.google.typography.font.sfntly.table.truetype.GlyphTable;
import com.google.typography.font.sfntly.table.truetype.LocaTable;
import com.google.typography.font.sfntly.table.truetype.SimpleGlyph;

import java.text.NumberFormat;

/**
 * Summarize a font's information
 * @author Brian Stell
 */
public class FontSummary {
  private final Font font;
  private final long length;

  /**
   * FontSummary constructor
   * 
   * This constructor is needed since the font object does not know the file/bytearray size
   * 
   * @param font - the font to summarize
   * @param length - the length since the font object does not have that
   */
  FontSummary(Font font, long length) {
    this.font = font;
    this.length = length;
  }
  
  public String getCMapName() {
    String cmapName = "unknown";

    CMapTable cmapTable = font.getTable(Tag.cmap);

    if (cmapTable == null) {
      throw new RuntimeException("Missing cmap table");
    }
    CMap cmap = null;
    // Try to get the UCS-4 cmap
    cmap = cmapTable.cmap(Font.PlatformId.Windows.value(),
                          Font.WindowsEncodingId.UnicodeUCS4.value());
    if (cmap != null) {
      cmapName = "USC-4";
    } else {
      cmap = cmapTable.cmap(Font.PlatformId.Windows.value(),
                            Font.WindowsEncodingId.UnicodeUCS2.value());
      if (cmap != null) {
        cmapName = "USC-2";
      } else {
        throw new RuntimeException("Missing USC-4 and USC-2 cmap");
      }
    }
    
    return cmapName;
  }

  public String getFontFamilyName() {
    return getNameTableEntry(NameId.FontFamilyName);
  }

  public String getFontSubfamilyName() {
    return getNameTableEntry(NameId.FontSubfamilyName);
  }

  public long getGlyphsLength() {
    LocaTable locaTable = font.getTable(Tag.loca);
    int glyphsLength = 0;

    for (int glyphId = 0; glyphId < locaTable.numGlyphs(); glyphId++) {
      if (glyphId != CMapTable.NOTDEF) {
        // get the glyph length including any padding
        glyphsLength += locaTable.glyphLength(glyphId);
      }
    }
    return glyphsLength; 
  }
  
  public long getInstructionsLength() {
    LocaTable locaTable = font.getTable(Tag.loca);
    GlyphTable glyphTable = font.getTable(Tag.glyf);
    int instructionsLength = 0;

    for (int glyphId = 0; glyphId < locaTable.numGlyphs(); glyphId++) {
      if (glyphId != CMapTable.NOTDEF) {
        int offset = locaTable.glyphOffset(glyphId);
        int length = locaTable.glyphLength(glyphId);
        if (length != 0) { // glyph objects only exist for non-zero length entries
          Glyph glyph = glyphTable.glyph(offset, length);
          if (glyph.glyphType() == GlyphType.Simple){
            int glyphLength = ((SimpleGlyph) glyph).dataLength();
            int instructionLength = ((SimpleGlyph) glyph).instructionSize();
            instructionsLength += instructionLength;
          }
        }
      }
    }

    return instructionsLength; 
  }
  
  public long getLength() {
    return length;
  }

  private String getNameTableEntry(NameId entryName) {
    NameTable nameTable = font.getTable(Tag.name);
    String value = nameTable.name(PlatformId.Windows.value(), 
      WindowsEncodingId.UnicodeUCS2.value(), WindowsLanguageId.English_UnitedStates.value(), 
      entryName.value());
    return value;
  }
  
  public long getNumCodePoints() {
    CMap cmap = null;
    int maxCodePoint = 0xFFFF;
    int numCodePoints = 0;
    
    CMapTable cmapTable = font.getTable(Tag.cmap);
    if (cmapTable == null) {
      throw new RuntimeException("Missing cmap table");
    }

    // Try to get the UCS-4 cmap
    cmap = cmapTable.cmap(Font.PlatformId.Windows.value(),
                          Font.WindowsEncodingId.UnicodeUCS4.value());
    if (cmap != null) {
      maxCodePoint = 0x10FFFF;
    } else {
      cmap = cmapTable.cmap(Font.PlatformId.Windows.value(),
                            Font.WindowsEncodingId.UnicodeUCS2.value());
    }
    if (cmap != null) {
      for (int charId = 0; charId < maxCodePoint; charId++) {
        int glyphId = cmap.glyphId(charId);
        if (glyphId != CMapTable.NOTDEF) {
          numCodePoints += 1;
        }
      }
    } else {
      throw new RuntimeException("Missing USC-4 and USC-2 cmap");

    }


    return numCodePoints;
  }
  
  public long getNumGlyphs() {
    LocaTable locaTable = font.getTable(Tag.loca);
    return locaTable.numGlyphs(); 
  }
  
  public String getVersion() {
    double version = FontUtils.fixed1616ToDouble(font.sfntVersion());
    NumberFormat numberFormatter = NumberFormat.getInstance();
    numberFormatter.setMinimumFractionDigits(2);            
    numberFormatter.setGroupingUsed(false); 
    return numberFormatter.format(version);
  }

  public String getVersionString() {
    return getNameTableEntry(NameId.VersionString);
  }
}
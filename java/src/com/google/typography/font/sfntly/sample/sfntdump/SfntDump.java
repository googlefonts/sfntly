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
package com.google.typography.font.sfntly.sample.sfntdump;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.FontFactory;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.bitmap.EblcTable;
import com.google.typography.font.sfntly.table.core.CMap;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.sfntly.table.core.NameTable;
import com.google.typography.font.sfntly.table.core.PostScriptTable;
import com.google.typography.font.sfntly.table.truetype.Glyph;
import com.google.typography.font.sfntly.table.truetype.GlyphTable;
import com.google.typography.font.sfntly.table.truetype.LocaTable;
import com.google.typography.font.sfntly.table.truetype.SimpleGlyph;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SfntDump {
  private boolean countSpecialGlyphs;
  private boolean dumpTableHeadersInFont;
  private boolean dumpNameList;
  private boolean dumpCmapList;
  private boolean cmapMapping;
  private boolean dumpPost;
  private boolean dumpEblc;

  @SuppressWarnings("unused")
  private boolean dumpAllGlyphs;

  private final List<String> tablesToBinaryDump = new ArrayList<>();
  private BitSet glyphSet;
  private boolean dumpAllChars;
  private BitSet charSet;
  private CMapTable.CMapId cmapId = CMapTable.CMapId.WINDOWS_BMP;
  private final FontFactory fontFactory;

  /** Dump a font with various options based on the command line. */
  public static void main(String[] args) throws IOException {
    SfntDump dumper = new SfntDump();
    int optionCount = 0;

    if (args.length == 0
        || args[0].equals("-h")
        || args[0].equals("-help")
        || args[0].equals("-?")) {
      printUsage();
      System.exit(0);
    }

    File fontFile = new File(args[args.length - 1]);
    args = Arrays.copyOfRange(args, 0, args.length - 1);

    for (int i = 0; i < args.length; i++) {

      if (args[i].startsWith("-")) {
        String option = args[i].substring(1);
        optionCount++;

        if (option.equals("count")) {
          dumper.countSpecialGlyphs(true);
          continue;
        }

        if (option.equals("t")) {
          if (i + 1 < args.length) {
            dumper.dumpTablesAsBinary(args[++i]);
          }
          continue;
        }

        if (option.equals("cm")) {
          if (i + 1 < args.length) {
            dumper.useCMap(args[++i]);
          }
          continue;
        }

        if (option.equals("table")) {
          dumper.dumpTableList(true);
          continue;
        }

        if (option.startsWith("name")) {
          dumper.dumpNames(true);
          continue;
        }

        if (option.startsWith("cmap")) {
          dumper.dumpCMaps(true);
          if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
            dumper.dumpCMaps(args[++i]);
          }
          continue;
        }

        if (option.startsWith("post")) {
          dumper.dumpPost(true);
          continue;
        }

        if (option.startsWith("eblc")) {
          dumper.dumpEblc(true);
          continue;
        }

        if (option.equals("glyph") || option.equals("g")) {
          BitSet glyphSet = null;
          // if there's only one argument left (the filename), then i + 1 == args.length - 1
          if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
            dumper.dumpAllGlyphs(true);
            continue;
          }
          if (i + 1 < args.length) {
            i++;
            glyphSet = parseRange(args[i]);
            if (glyphSet == null) {
              glyphSet = parseList(args[i]);
            }
            if (glyphSet != null) {
              dumper.dumpGlyphs(glyphSet);
            }
          }
          if (glyphSet == null) {
            System.out.println("glyph dump option requires a glyph range or list");
            System.exit(0);
          }
        }

        if (option.equals("char") || option.equals("c")) {
          BitSet charSet = null;
          if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
            dumper.dumpAllChars(true);
            continue;
          }
          if (i + 1 < args.length) {
            i++;
            charSet = parseRange(args[i]);
            if (charSet == null) {
              charSet = parseList(args[i]);
            }
            if (charSet != null) {
              dumper.dumpChars(charSet);
            }
          }
          if (charSet == null) {
            System.out.println("character dump option requires a glyph range or list");
            System.exit(0);
          }
        }

        if (option.equals("all") || option.equals("a")) {
          dumper.dumpAll(true);
        }
      }
    }

    if (optionCount == 0) {
      dumper.dumpTableList(true);
    }

    if (fontFile.isDirectory()) {
      File[] files = fontFile.listFiles();
      for (File file : files) {
        if (file.isFile() && !file.isHidden()) {
          try {
            dumper.dumpFont(file);
            System.out.println();
          } catch (Exception e) {
            System.err.printf("Error processing file: %s%n", file);
          }
        }
      }
    } else {
      try {
        dumper.dumpFont(fontFile);
      } catch (Exception e) {
        System.err.printf("Error processing file: %s%n", fontFile);
      }
    }
  }

  private static void printUsage() {
    System.out.println(
        "FontDumper [-all|-a] [-table] [-t tag] [-name] [-cmap] "
            + "[-g|-glyph range|list] [-c|-char range|list] "
            + "[-?|-h|-help] fontfile | directory");
    System.out.println("dump information about the font file or all fonts in a directory");
    System.out.println("\t-all,-a\t\tdump all information");
    System.out.println("\t-table\t\tdump all table indexes");
    System.out.println(
        "\t-t tag\t\t" + "binary dump the table with the tag specified if it exists in the font");
    System.out.println("\t-name\t\tdump all name entries");
    System.out.println("\t-cmap [mapping]\t\tdump all cmap subtables");
    System.out.println(
        "\tif 'mapping' specified then dump the character to glyph mapping for the cmap(s)");
    System.out.println(
        "\t-cm pid,eid\t\t"
            + "use the cmap with the given platform id and "
            + "encoding id when looking for glyphs from character ids");
    System.out.println("\t-post\t\tdump the PostScript name table");
    System.out.println("\t-eblc\t\tdump the EBLC table - bitmap location");
    System.out.println("\t-g,-glyph\t\tdump the glyphs specified");
    System.out.println(
        "\t-c,-char\t\t"
            + "dump the characters specified using the Windows English Unicode "
            + "cmap or the cmap specified with the -cm option");
    System.out.println(
        "\trange\t\t"
            + "two 1 to 4 digit numbers separated by a hyphen that are "
            + "optionally preceded by an x indicating hex - e.g. x12-234");
    System.out.println(
        "\tlist\t\t"
            + "one or more 1 to 4 digit numbers separated by commas that are "
            + "optionally preceded by an x indicating hex - e.g. x12,234,666,x1234");
    System.out.println("\t-?,-h,-help\tprint this help information");
  }

  private static final Pattern RANGE_PATTERN =
      Pattern.compile("(x?)([\\da-fA-F]{1,5})-(x?)([\\da-fA-F]{1,5})");

  private static BitSet parseRange(String range) {
    BitSet set = null;

    Matcher m = RANGE_PATTERN.matcher(range);
    if (m.matches()) {
      int low = Integer.parseInt(m.group(2), m.group(1).isEmpty() ? 10 : 16);
      int high = Integer.parseInt(m.group(4), m.group(3).isEmpty() ? 10 : 16);
      set = new BitSet();
      set.set(low, high + 1);
    }
    return set;
  }

  private static final Pattern NUMBER_PATTERN = Pattern.compile("(x?)([\\da-fA-F]{1,5})");

  private static BitSet parseList(String list) {
    String[] items = list.split(",");
    if (items.length == 0) {
      return null;
    }

    BitSet set = new BitSet();
    for (String item : items) {
      Matcher m = NUMBER_PATTERN.matcher(item);
      if (!m.matches()) {
        return null;
      }
      int itemNumber = Integer.parseInt(m.group(2), m.group(1).isEmpty() ? 10 : 16);
      set.set(itemNumber);
    }
    return set;
  }

  public SfntDump() {
    this.fontFactory = FontFactory.getInstance();
  }

  public void countSpecialGlyphs(boolean count) {
    this.countSpecialGlyphs = count;
  }

  public void dumpTableList(boolean dumpTableList) {
    this.dumpTableHeadersInFont = dumpTableList;
  }

  public void dumpCMaps(boolean dumpCMaps) {
    this.dumpCmapList = dumpCMaps;
  }

  public void dumpCMaps(String option) {
    if (option.equals("mapping")) {
      this.cmapMapping = true;
    }
  }

  public void dumpNames(boolean dumpNames) {
    this.dumpNameList = dumpNames;
  }

  public void dumpPost(boolean dumpPost) {
    this.dumpPost = dumpPost;
  }

  public void dumpEblc(boolean dumpEblc) {
    this.dumpEblc = dumpEblc;
  }

  public void dumpAll(boolean dumpAll) {
    dumpCMaps(dumpAll);
    dumpNames(dumpAll);
    dumpPost(dumpAll);
    dumpTableList(dumpAll);
  }

  public void dumpGlyphs(BitSet set) {
    this.glyphSet = set;
  }

  public void dumpAllGlyphs(boolean dumpAllGlyphs) {
    this.dumpAllGlyphs = dumpAllGlyphs;
  }

  public void dumpAllChars(boolean dumpAll) {
    this.dumpAllChars = dumpAll;
  }

  public void dumpChars(BitSet set) {
    this.charSet = set;
  }

  public void dumpTablesAsBinary(String tableTag) {
    tablesToBinaryDump.add(tableTag);
  }

  public void useCMap(String cmap) {
    String[] cmapParams = cmap.split("\\D");
    this.cmapId =
        CMapTable.CMapId.getInstance(
            Integer.parseInt(cmapParams[0]), Integer.parseInt(cmapParams[1]));
  }

  public void dumpFont(File fontFile) throws IOException {
    boolean canDumpGlyphs = true;

    System.out.println(fontFile + " ============================");
    try (FileInputStream fis = new FileInputStream(fontFile)) {
      Font[] fontArray = fontFactory.loadFonts(fis);

      for (int fontNumber = 0; fontNumber < fontArray.length; fontNumber++) {
        Font font = fontArray[fontNumber];
        if (fontArray.length > 1) {
          System.out.println();
          System.out.println("======= TTC Entry #" + fontNumber);
          System.out.println();
        }
        if (dumpTableHeadersInFont) {
          for (Map.Entry<Integer, ? extends Table> entry : font.tableMap().entrySet()) {
            System.out.println(entry.getValue().header());
          }
        }

        if (countSpecialGlyphs) {
          countSpecialGlyphs(font);
        }

        if (dumpNameList) {
          System.out.println();
          System.out.println("----- Name Tables");
          NameTable name = font.getTable(Tag.name);
          for (NameTable.NameEntry entry : name) {
            System.out.println(entry);
          }
        }

        if (dumpCmapList) {
          System.out.println();
          System.out.println("------ CMap Tables");
          CMapTable cmapTable = font.getTable(Tag.cmap);
          for (CMap cmap : cmapTable) {
            System.out.println(cmap);
            if (cmapMapping) {
              dumpCMapMapping(cmap);
            }
          }
        }

        if (dumpPost) {
          System.out.println();
          System.out.println("------ Post Table");
          PostScriptTable post = font.getTable(Tag.post);
          int nGlyphs = post.numberOfGlyphs();
          for (int glyphId = 0; glyphId < nGlyphs; glyphId++) {
            System.out.printf("%d: %s%n", glyphId, post.glyphName(glyphId));
          }
        }

        if (dumpEblc) {
          System.out.println();
          System.out.println("------ EBLC Table");
          EblcTable eblcTable = font.getTable(Tag.EBLC);
          System.out.println(eblcTable.toString());
        }

        if (tablesToBinaryDump.size() > 0) {
          for (String tag : tablesToBinaryDump) {
            int tableTag = Tag.intValue(tag);
            Table table = font.getTable(tableTag);
            if (table != null) {
              System.out.println();
              System.out.printf(
                  "------ Dump Data - Table = %s, length = %x%n", tag, table.dataLength());
              ReadableFontData data = table.readFontData();
              for (int i = 0; i < data.length(); i += 16) {
                System.out.printf("%08x: ", i);
                for (int j = i; j < i + 16 && j < data.length(); j++) {
                  System.out.printf("%02x ", data.readUByte(j));
                }
                System.out.println();
              }
              System.out.println();
            }
          }
        }

        LocaTable locaTable = font.getTable(Tag.loca);
        GlyphTable glyphTable = font.getTable(Tag.glyf);
        if (locaTable == null) {
          canDumpGlyphs = false;
          System.out.println("PROBLEM: font has no 'loca' table.");
        }
        if (glyphTable == null) {
          canDumpGlyphs = false;
          System.out.println("PROBLEM: font has no 'glyf' table.");
        }

        if (canDumpGlyphs && glyphSet != null) {
          System.out.println();
          System.out.println("------ Glyphs");
          for (int glyphId = glyphSet.nextSetBit(0);
              glyphId >= 0;
              glyphId = glyphSet.nextSetBit(glyphId + 1)) {
            int offset = locaTable.glyphOffset(glyphId);
            int length = locaTable.glyphLength(glyphId);
            Glyph glyph = glyphTable.glyph(offset, length);
            System.out.println("glyph id = " + glyphId);
            System.out.println(glyph);
          }
        }

        if (canDumpGlyphs && charSet != null) {
          dumpChars(font, locaTable, glyphTable);
        }
      }
    }
  }

  private void dumpChars(Font font, LocaTable locaTable, GlyphTable glyphTable) {
    CMapTable cmapTable = font.getTable(Tag.cmap);
    if (cmapTable == null) {
      System.out.println("PROBLEM: font has no 'cmap' table.");
      return;
    }
    CMap cmap = cmapTable.cmap(cmapId);
    // if (cmap == null) {
    // cmap = cmapTable.cmap(
    // Font.PlatformId.Windows.value(),
    // Font.WindowsEncodingId.UnicodeUCS4.value());
    // }
    if (cmap == null) {
      System.out.println("PROBLEM: required cmap subtable not available.");
      return;
    }

    System.out.println();
    System.out.println("=============");
    System.out.println(cmap);

    if (dumpAllChars) {
      for (int charId : cmap) {
        dumpChar(charId, cmap, locaTable, glyphTable);
      }
    } else if (charSet != null) {
      System.out.println();
      System.out.println("------ Characters");
      for (int charId = charSet.nextSetBit(0);
          charId >= 0;
          charId = charSet.nextSetBit(charId + 1)) {
        dumpChar(charId, cmap, locaTable, glyphTable);
      }
    }
  }

  private void dumpChar(int charId, CMap cmap, LocaTable locaTable, GlyphTable glyphTable) {
    int glyphId = cmap.glyphId(charId);
    int offset = locaTable.glyphOffset(glyphId);
    int length = locaTable.glyphLength(glyphId);
    Glyph glyph = glyphTable.glyph(offset, length);
    System.out.printf("char = 0x%x, glyph id = 0x%x%n", charId, glyphId);
    System.out.println(glyph);
  }

  private void countSpecialGlyphs(Font font) {
    LocaTable locaTable = font.getTable(Tag.loca);
    GlyphTable glyphTable = font.getTable(Tag.glyf);

    int count = 0;
    for (int glyphId = 0; glyphId < locaTable.numGlyphs(); glyphId++) {
      int offset = locaTable.glyphOffset(glyphId);
      int length = locaTable.glyphLength(glyphId);
      Glyph glyph = glyphTable.glyph(offset, length);
      if (glyph instanceof SimpleGlyph) {
        SimpleGlyph simple = (SimpleGlyph) glyph;
        if (simple.numberOfContours() != 2) {
          continue;
        }
        if (simple.numberOfPoints(0) != 1 && simple.numberOfPoints(1) != 1) {
          continue;
        }
        count++;
      }
    }
    System.out.println();
    System.out.println("------ Special Glyph Count");
    System.out.println("\ttotal glyphs = " + locaTable.numGlyphs());
    System.out.println("\tspecial glyphs = " + count);
  }

  private void dumpCMapMapping(CMap cmap) {
    for (Integer c : cmap) {
      int g = cmap.glyphId(c);
      if (g != CMapTable.NOTDEF) {
        System.out.printf("%x -> %x%n", c, g);
      }
    }
  }
}

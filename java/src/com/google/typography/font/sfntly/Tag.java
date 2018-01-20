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

import java.io.UnsupportedEncodingException;


/**
 * Font identification tags used for tables, features, etc.
 *
 * Tag names are consistent with the OpenType and sfnt specs.
 *
 * @author Stuart Gill
 */
public final class Tag {
  public static final int ttcf = Tag.intValue("ttcf");

  /***********************************************************************************
   *
   * Table Type Tags
   *
   ***********************************************************************************/

  // required tables
  public static final int cmap = Tag.intValue("cmap");
  public static final int head = Tag.intValue("head");
  public static final int hhea = Tag.intValue("hhea");
  public static final int hmtx = Tag.intValue("hmtx");
  public static final int maxp = Tag.intValue("maxp");
  public static final int name = Tag.intValue("name");
  public static final int OS_2 = Tag.intValue("OS/2");
  public static final int post = Tag.intValue("post");

  // truetype outline tables
  public static final int cvt = Tag.intValue("cvt ");
  public static final int fpgm = Tag.intValue("fpgm");
  public static final int glyf = Tag.intValue("glyf");
  public static final int loca = Tag.intValue("loca");
  public static final int prep = Tag.intValue("prep");

  // postscript outline tables
  public static final int CFF = Tag.intValue("CFF ");
  public static final int VORG = Tag.intValue("VORG");

  // opentype bitmap glyph outlines
  public static final int EBDT = Tag.intValue("EBDT");
  public static final int EBLC = Tag.intValue("EBLC");
  public static final int EBSC = Tag.intValue("EBSC");

  // advanced typographic features
  public static final int BASE = Tag.intValue("BASE");
  public static final int GDEF = Tag.intValue("GDEF");
  public static final int GPOS = Tag.intValue("GPOS");
  public static final int GSUB = Tag.intValue("GSUB");
  public static final int JSTF = Tag.intValue("JSTF");

  // other
  public static final int DSIG = Tag.intValue("DSIG");
  public static final int gasp = Tag.intValue("gasp");
  public static final int hdmx = Tag.intValue("hdmx");
  public static final int kern = Tag.intValue("kern");
  public static final int LTSH = Tag.intValue("LTSH");
  public static final int PCLT = Tag.intValue("PCLT");
  public static final int VDMX = Tag.intValue("VDMX");
  public static final int vhea = Tag.intValue("vhea");
  public static final int vmtx = Tag.intValue("vmtx");

  // AAT Tables
  // TODO(stuartg): some tables may be missing from this list
  public static final int bsln = Tag.intValue("bsln");
  public static final int feat = Tag.intValue("feat");
  public static final int lcar = Tag.intValue("lcar");
  public static final int morx = Tag.intValue("morx");
  public static final int opbd = Tag.intValue("opbd");
  public static final int prop = Tag.intValue("prop");

  // Graphite tables
  public static final int Feat = Tag.intValue("Feat");
  public static final int Glat = Tag.intValue("Glat");
  public static final int Gloc = Tag.intValue("Gloc");
  public static final int Sile = Tag.intValue("Sile");
  public static final int Silf = Tag.intValue("Silf");

  // truetype bitmap font tables
  public static final int bhed = Tag.intValue("bhed");
  public static final int bdat = Tag.intValue("bdat");
  public static final int bloc = Tag.intValue("bloc");

  private Tag() {
    // Prevent construction.
  }

  public static int intValue(byte[] tag) {
    return tag[0] << 24 | tag[1] << 16 | tag[2] << 8 | tag[3];
  }

  public static byte[] byteValue(int tag) {
    byte[] b = new byte[4];
    b[0] = (byte) (0xff & (tag >> 24));
    b[1] = (byte) (0xff & (tag >> 16));
    b[2] = (byte) (0xff & (tag >> 8));
    b[3] = (byte) (0xff & tag);
    return b;
  }

  public static String stringValue(int tag) {
    String s;
    try {
      s = new String(byteValue(tag), "US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
    return s;
  }

  public static int intValue(String s) {
    byte[] b;
    try {
      b = s.substring(0, 4).getBytes("US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
    return intValue(b);
  }

  /**
   * Determines whether the tag is that for the header table.
   * @param tag table tag
   * @return true if the tag represents the font header table
   */
  public static boolean isHeaderTable(int tag) {
    if (tag == Tag.head || tag == Tag.bhed) {
      return true;
    }
    return false;
  }
}

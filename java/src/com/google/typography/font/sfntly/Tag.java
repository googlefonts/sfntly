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
 * <p>Tag names are consistent with the OpenType and sfnt specs.
 *
 * @author Stuart Gill
 */
public final class Tag {
  public static final int ttcf = intValue("ttcf");

  /**
   * *********************************************************************************
   *
   * <p>Table Type Tags
   *
   * <p>*********************************************************************************
   */

  // required tables
  public static final int cmap = intValue("cmap");

  public static final int head = intValue("head");
  public static final int hhea = intValue("hhea");
  public static final int hmtx = intValue("hmtx");
  public static final int maxp = intValue("maxp");
  public static final int name = intValue("name");
  public static final int OS_2 = intValue("OS/2");
  public static final int post = intValue("post");

  // truetype outline tables
  public static final int cvt = intValue("cvt ");
  public static final int fpgm = intValue("fpgm");
  public static final int glyf = intValue("glyf");
  public static final int loca = intValue("loca");
  public static final int prep = intValue("prep");

  // postscript outline tables
  public static final int CFF = intValue("CFF ");
  public static final int VORG = intValue("VORG");

  // opentype bitmap glyph outlines
  public static final int EBDT = intValue("EBDT");
  public static final int EBLC = intValue("EBLC");
  public static final int EBSC = intValue("EBSC");

  // advanced typographic features
  public static final int BASE = intValue("BASE");
  public static final int GDEF = intValue("GDEF");
  public static final int GPOS = intValue("GPOS");
  public static final int GSUB = intValue("GSUB");
  public static final int JSTF = intValue("JSTF");

  // other
  public static final int DSIG = intValue("DSIG");
  public static final int gasp = intValue("gasp");
  public static final int hdmx = intValue("hdmx");
  public static final int kern = intValue("kern");
  public static final int LTSH = intValue("LTSH");
  public static final int PCLT = intValue("PCLT");
  public static final int VDMX = intValue("VDMX");
  public static final int vhea = intValue("vhea");
  public static final int vmtx = intValue("vmtx");

  // AAT Tables
  // TODO(stuartg): some tables may be missing from this list
  public static final int bsln = intValue("bsln");
  public static final int feat = intValue("feat");
  public static final int lcar = intValue("lcar");
  public static final int morx = intValue("morx");
  public static final int opbd = intValue("opbd");
  public static final int prop = intValue("prop");

  // Graphite tables
  public static final int Feat = intValue("Feat");
  public static final int Glat = intValue("Glat");
  public static final int Gloc = intValue("Gloc");
  public static final int Sile = intValue("Sile");
  public static final int Silf = intValue("Silf");

  // truetype bitmap font tables
  public static final int bhed = intValue("bhed");
  public static final int bdat = intValue("bdat");
  public static final int bloc = intValue("bloc");

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
   *
   * @param tag table tag
   * @return true if the tag represents the font header table
   */
  public static boolean isHeaderTable(int tag) {
    if (tag == head || tag == bhed) {
      return true;
    }
    return false;
  }
}

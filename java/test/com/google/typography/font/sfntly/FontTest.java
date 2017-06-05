package com.google.typography.font.sfntly;

import com.google.typography.font.sfntly.testutils.TestFont;
import com.google.typography.font.sfntly.testutils.TestFontUtils;
import junit.framework.TestCase;

import java.io.IOException;

public class FontTest extends TestCase {

  public void testToStringWithDigest() throws IOException {
    Font[] fonts = TestFontUtils.loadFont(TestFont.TestFontNames.OPENSANS.getFile());

    assertEquals(""
            + "digest = 72de46254f757565bdb0558839e84c8623366041\n"
            + "[1.0, 19]\n"
            + "\t[DSIG, cs=0x9e12441d, offset=0x00033b9c, size=0x00001574]\n"
            + "\t[GDEF, cs=0x002603af, offset=0x0003377c, size=0x0000001e]\n"
            + "\t[GPOS, cs=0x0b370f37, offset=0x0003379c, size=0x00000038]\n"
            + "\t[GSUB, cs=0x0e2b3db7, offset=0x000337d4, size=0x000003c6]\n"
            + "\t[OS/2, cs=0xa13e9ec9, offset=0x000001b8, size=0x00000060]\n"
            + "\t[cmap, cs=0x29ab2f68, offset=0x000010b4, size=0x0000041a]"
            + " = { [0xc = cmap: pid = 3, eid = 1, Format4, Data Size=0x40e] }\n"
            + "\t[cvt , cs=0x0f4d18a4, offset=0x00001d90, size=0x000000a2]\n"
            + "\t[fpgm, cs=0x7e61b611, offset=0x000014d0, size=0x000007b4]\n"
            + "\t[gasp, cs=0x00150023, offset=0x0003376c, size=0x00000010]\n"
            + "\t[glyf, cs=0x7438994b, offset=0x0000258c, size=0x00012fb4]\n"
            + "\t[head, cs=0xf776e2a6, offset=0x0000013c, size=0x00000036]\n"
            + "\t[hhea, cs=0x0dcc0973, offset=0x00000174, size=0x00000024]\n"
            + "\t[hmtx, cs=0xe8353cdd, offset=0x00000218, size=0x00000e9a]\n"
            + "\t[kern, cs=0x542b097e, offset=0x00015540, size=0x0001b636]\n"
            + "\t[loca, cs=0x2914dcf1, offset=0x00001e34, size=0x00000756]\n"
            + "\t[maxp, cs=0x0543020a, offset=0x00000198, size=0x00000020]\n"
            + "\t[name, cs=0x73b08885, offset=0x00030b78, size=0x000005c7]\n"
            + "\t[post, cs=0x0243ef6c, offset=0x00031140, size=0x0000262b]\n"
            + "\t[prep, cs=0x43b796a4, offset=0x00001c84, size=0x00000109]\n",
        fonts[0].toString());
  }

  public void testToStringWithoutDigest() throws IOException {
    Font[] fonts = TestFontUtils.loadFont(TestFont.TestFontNames.OPENSANS.getFile(), false);

    assertEquals(""
            + "[1.0, 19]\n"
            + "\t[DSIG, cs=0x9e12441d, offset=0x00033b9c, size=0x00001574]\n"
            + "\t[GDEF, cs=0x002603af, offset=0x0003377c, size=0x0000001e]\n"
            + "\t[GPOS, cs=0x0b370f37, offset=0x0003379c, size=0x00000038]\n"
            + "\t[GSUB, cs=0x0e2b3db7, offset=0x000337d4, size=0x000003c6]\n"
            + "\t[OS/2, cs=0xa13e9ec9, offset=0x000001b8, size=0x00000060]\n"
            + "\t[cmap, cs=0x29ab2f68, offset=0x000010b4, size=0x0000041a]"
            + " = { [0xc = cmap: pid = 3, eid = 1, Format4, Data Size=0x40e] }\n"
            + "\t[cvt , cs=0x0f4d18a4, offset=0x00001d90, size=0x000000a2]\n"
            + "\t[fpgm, cs=0x7e61b611, offset=0x000014d0, size=0x000007b4]\n"
            + "\t[gasp, cs=0x00150023, offset=0x0003376c, size=0x00000010]\n"
            + "\t[glyf, cs=0x7438994b, offset=0x0000258c, size=0x00012fb4]\n"
            + "\t[head, cs=0xf776e2a6, offset=0x0000013c, size=0x00000036]\n"
            + "\t[hhea, cs=0x0dcc0973, offset=0x00000174, size=0x00000024]\n"
            + "\t[hmtx, cs=0xe8353cdd, offset=0x00000218, size=0x00000e9a]\n"
            + "\t[kern, cs=0x542b097e, offset=0x00015540, size=0x0001b636]\n"
            + "\t[loca, cs=0x2914dcf1, offset=0x00001e34, size=0x00000756]\n"
            + "\t[maxp, cs=0x0543020a, offset=0x00000198, size=0x00000020]\n"
            + "\t[name, cs=0x73b08885, offset=0x00030b78, size=0x000005c7]\n"
            + "\t[post, cs=0x0243ef6c, offset=0x00031140, size=0x0000262b]\n"
            + "\t[prep, cs=0x43b796a4, offset=0x00001c84, size=0x00000109]\n",
        fonts[0].toString());
  }

}

package com.google.typography.font.sfntly.testutils;

import com.google.typography.font.sfntly.table.Table;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Assert;

public final class SfAssert {

  public static void assertTableHexDumpEquals(String hex, Table table) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      table.serialize(baos);
      byte[] bytes = baos.toByteArray();

      Assert.assertEquals(hex, hexdump(bytes));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String hexdump(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bytes.length; i++) {
      sb.append("0123456789abcdef".charAt((bytes[i] >> 4) & 0x0f));
      sb.append("0123456789abcdef".charAt(bytes[i] & 0x0f));
      if (i + 1 < bytes.length) {
        sb.append(i % 16 == 15 ? "\n" : " ");
      }
    }
    return sb.toString();
  }
}

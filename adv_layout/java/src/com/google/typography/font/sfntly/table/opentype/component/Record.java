package com.google.typography.font.sfntly.table.opentype.component;

import com.google.typography.font.sfntly.data.WritableFontData;

public interface Record extends HtmlDump {
  int writeTo(WritableFontData newData, int base);
}

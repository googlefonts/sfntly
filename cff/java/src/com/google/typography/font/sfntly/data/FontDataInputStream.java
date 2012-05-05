/**
 * 
 */
package com.google.typography.font.sfntly.data;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author stuartg
 *
 */
//TODO(stuartg) make more efficient
public class FontDataInputStream extends InputStream {
  private ReadableFontData data;
  private int index;
  
  /**
   * 
   */
  public FontDataInputStream(ReadableFontData data) {
    this.data = data;
  }

  @Override
  public int read() throws IOException {
    if (this.index >= this.data.length()) {
      return -1;
    }
    return this.data.readUByte(this.index++);
  }
}

/**
 * 
 */
package com.google.typography.font.sfntly.data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author stuartg
 *
 */
//TODO(stuartg) make more efficient
public class FontDataOutputStream extends OutputStream {
  private WritableFontData data;
  private int index;
  
  /**
   * 
   */
  public FontDataOutputStream(WritableFontData data) {
    this.data = data;
  }

  /* (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write(int b) throws IOException {
    if (this.index >= this.data.size()) {
      throw new IOException("Can't write beyond the end of the data.");
    }
    this.data.writeByte(this.index++, (byte) b);
  }

}

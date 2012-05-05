/**
 * 
 */
package com.google.typography.font.sfntly.table.cff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author stuartg
 *
 */
public class DictTokenizer {

  public static final int PARSE_ERROR_NONE = 0;
  public static final int PARSE_ERROR_EOF = 1;
  public static final int PARSE_ERROR_INVALID_BYTE = 2;
  public static final int PARSE_ERROR_INVALID_STATE = 3;
  
  public static final int OPERATOR_ESCAPE_BYTE = 0x0c;
  public static final int OPERATOR_ESCAPE = OPERATOR_ESCAPE_BYTE << 8;
  public static final int OPERATOR_MASK = 0xff;
  
  public static class DictEntry {
    private List<Number> operands;
    //private boolean escapeOperator;
    private int operator;
    
    private int error;
    private boolean forceFullSizeIntegerEncoding;

    DictEntry() {
      this.operands = new ArrayList<Number>();
    }
    
    public int errorCode() {
      return this.error;
    }
    
    public boolean error() {
      return this.errorCode() != PARSE_ERROR_NONE;
    }
    
    public int operator() {
      return this.operator;
    }
    
//    public boolean operatorEscape() {
//      return this.escapeOperator;
//    }
    
    public int operandCount() {
      return this.operands.size();
    }
    
    /**
     * Gets the raw operand at the index given. Operands are numbered from 0 to count - 1 
     * in the order encounted in the dictionary stream.
     * @param index operator index
     * @return operator value
     */
    public Number getOperand(int index) {
      return this.operands.get(index);
    }
    
    public Number getNumber() {
      return this.getOperand(0);
    }
    
    public float getFloat() {
      return this.getOperand(0).floatValue();
    }
    
    public int getInteger() {
      return this.getOperand(0).intValue();
    }
    
    public float[] getArray() {
      float[] array = new float[this.operandCount()];
      for (int i = 0; i < this.operandCount(); i++) {
        array[i] = this.getOperand(i).floatValue();
      }
      return array;
    }
    
    public float[] getDelta() {
      float[] array = new float[this.operandCount()];
      int previous = 0;
      for (int i = 0; i < this.operandCount(); i++) {
        array[i] = this.getOperand(i).floatValue() + previous;
      }
      return array;      
    }
    
    public Integer getSID() {
      return this.getNumber().intValue();
    }
    
    public Boolean getBoolean() {
      return this.getInteger() == 0 ? Boolean.FALSE : Boolean.TRUE;
    }
    
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("%02x <- [" , operator));
      for (Number operand : this.operands) {
        sb.append(operand + ", ");
      }
      sb.append("]");
      return sb.toString();
    }
    
    public static class Builder {
      private DictEntry dictEntry;
      
      public static Builder createBuilder() {
        return new Builder();
      }
      
      public static Builder createBuilder(int operator, Number operand) {
        return new Builder(operator, operand);
      }
      
      private Builder() {
        this.dictEntry = new DictEntry();
      }
      
      private Builder(int operator, Number operand) {
        this();
        this.setOperator(operator);
        this.addOperand(operand);
      }
      
      public DictEntry build() {
        DictEntry entry = this.dictEntry;
        this.dictEntry = null;
        return entry;
      }
      
      public void setOperator(int operator) {
        this.dictEntry.operator = operator;
      }
      
      public void addOperand(Number operand) {
        this.dictEntry.operands.add(operand);
      }
      
      void forceFullSizeIntegerEncoding() {
        this.dictEntry.forceFullSizeIntegerEncoding = true;
      }
    }
  }
  
  /**
   * 
   */
  private DictTokenizer() {
  }

  /**
   * Gets the entries in the dictionary from the stream at the current position and puts them 
   * into a map. This fully consumes the stream from the point it is currently at. After 
   * this call the iterator will not have any more entries 
   * (i.e. {{@link #next()} will return false). Any entries already
   * removed from the stream will not be included in the map.
   * 
   * @param is the source of the dictionary data
   * @return map of the operand to tokens in a dictionary
   */
  // TODO(stuartg): parameters - duplicate entries
  public static Map<Integer, DictEntry> tokenMap(InputStream is) {
    Map<Integer, DictEntry> tokens = new HashMap<Integer, DictEntry>();
    return DictTokenizer.tokenMap(is, tokens);
  }
  
  /**
   * Gets the entries in the dictionary from the stream at the current position and puts them 
   * into a map. This fully consumes the stream from the point it is currently at. After 
   * this call the iterator will not have any more entries 
   * (i.e. {{@link #next()} will return false). Any entries already
   * removed from the stream will not be included in the map.
   * 
   * @param is the source of the dictionary data
   * @tokens the map to use to store the tokens
   * @return map of the operand to tokens in a dictionary
   */
  // TODO(stuartg): parameters - duplicate entries
  public static Map<Integer, DictEntry> tokenMap(InputStream is, Map<Integer, DictEntry> tokens) {
    Iterator<DictTokenizer.DictEntry> iter = DictTokenizer.tokenIterator(is);
    while (iter.hasNext()) {
      DictEntry entry = iter.next();
      tokens.put(entry.operator(), entry);
      //System.out.println(entry);
    }
    return tokens;
  }
  
  /**
   * Gets the entries in the dictionary from the stream at the current position and puts them 
   * into a list. This fully consumes the stream from the point it is currently at. After 
   * this call the iterator will not have any more entries 
   * (i.e. {{@link #next()} will return false). Any entries already
   * removed from the stream will not be included in the map.
   * 
   * @return map of the operand to tokens in a dictionary
   */
  public static List<DictEntry> tokenList(InputStream is) {
    Iterator<DictTokenizer.DictEntry> iter = DictTokenizer.tokenIterator(is);
    List<DictEntry> tokens = new ArrayList<DictEntry>();
    while (iter.hasNext()) {
      DictEntry entry = iter.next();
      tokens.add(entry);
      //System.out.println(entry);
    }
    return tokens;    
  }
  
  public static Iterator<DictTokenizer.DictEntry> tokenIterator(InputStream is) {
    return new DictEntryIterator(is);
  }
  
  private static class DictEntryIterator implements Iterator<DictTokenizer.DictEntry> {
    InputStream is;
    DictEntry current;
    boolean done;
    boolean nextSet;
    
    DictEntryIterator(InputStream is) {
      this.is = is;
    }
    
    @Override
    public boolean hasNext() {
      if (done) {
        return false;
      }
      try {
        this.current = readDictEntry(this.is);
      } catch (IOException e) {
        // TODO(stuartg): Handle
      }
      if (this.current.errorCode() != PARSE_ERROR_NONE) {
        this.done = true;
      }
      if (this.current.errorCode() == PARSE_ERROR_EOF && 
          (this.current.operandCount() == 0 && this.current.operator() == 0)) {
        this.nextSet = false;
        this.current = null;
        return false;
      }
      this.nextSet = true;
      return true;
    }

    @Override
    public DictEntry next() {
      if (!this.nextSet && !hasNext()) {
        throw new NoSuchElementException();
      }
      this.nextSet = false;
      return current;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Unable to remove an operator from a stream.");
    }
  }
  
  private static final String[] REAL_CHAR_FOR_BYTE = {
    "0",
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8",
    "9",
    ".",
    "E",
    "E-",
    "",   // reserved
    "-",
    ""  // end of number
  };
  
  private static DictEntry readDictEntry(InputStream is) throws IOException {
    DictEntry entry = new DictEntry();    
    int b;
    while((b = is.read()) != -1) {
      if (b >= 0 && b <= 21) {
        // operator
        if (b == 12) {
          entry.operator = OPERATOR_ESCAPE;
          //entry.escapeOperator = true;
          b = is.read();
          if (b == -1) {
            entry.error = DictTokenizer.PARSE_ERROR_EOF;
            return entry;
          }
        }
        entry.operator |= b;
        return entry;
      }
      else if (b == 28) {
        // -32768 to +32767
        int b1 = is.read();
        int b2 = is.read();
        if (b1 == -1 || b2 == -1) {
          entry.error = DictTokenizer.PARSE_ERROR_EOF;
          return entry;
        }
        int value = b1 << 8 | b2;
        entry.operands.add(value);
      } 
      else if (b == 29) {
        // -2^31 to +2^31 - 1
        int b1 = is.read();
        int b2 = is.read();
        int b3 = is.read();
        int b4 = is.read();
        if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1) {
          entry.error = DictTokenizer.PARSE_ERROR_EOF;
          return entry;
        }
        int value = b1 << 24 | b2 << 16 | b3 << 8 | b4;
        entry.operands.add(value);
      }
      else if (b == 30) {
        // real
        StringBuilder sb = new StringBuilder();
        
        REAL_BYTE_READING: while (true) {
          int b1 = is.read();
          if (b1 == -1) {
            entry.error = DictTokenizer.PARSE_ERROR_EOF;
            return entry;
          }
          
          for (int n : new int[] {b1 >> 4, b1 & 0xf}) {
            if (n == 0xf) {
              break REAL_BYTE_READING;
            }
            if (n == 0xd) {
              entry.error = PARSE_ERROR_INVALID_BYTE;
              return entry;
            }
            
            sb.append(REAL_CHAR_FOR_BYTE[n]);
          } // for
        } // while
        Float value = null;
        try {
          value = Float.valueOf(sb.toString());
        } catch (NumberFormatException e) {
          entry.error = PARSE_ERROR_INVALID_BYTE;
        }
        entry.operands.add(value);
      }
      else if (b >= 32 && b <= 246)
      {
        // tiny int: -107 to +107
        int value = b - 139;
        entry.operands.add(value);
      }
      else if (b >= 247 && b <= 250) {
        // small +ve int: +108 to +1131
        int b1 = is.read();
        if (b1 == -1) {
          entry.error = DictTokenizer.PARSE_ERROR_EOF;
          return entry;
        }
        int value = (b - 247) * 256 + b1 + 108;
        entry.operands.add(value);
      }
      else if (b >= 251 && b <= 254) {
        // small -ve int: -108 to -1131
        int b1 = is.read();
        if (b1 == -1) {
          entry.error = DictTokenizer.PARSE_ERROR_EOF;
          return entry;
        }
        int value = -(b - 251) * 256 - b1 - 108;
        entry.operands.add(value);
      }
      else {
        // reserved bytes
        entry.error = DictTokenizer.PARSE_ERROR_INVALID_BYTE;
      }
    }
    // ran out of input before the operand seen
    entry.error = DictTokenizer.PARSE_ERROR_EOF;
    return entry;
  }
  
  public static int serialize(OutputStream os, Iterator<DictEntry> iter) throws IOException {
    int size = 0;
    while (iter.hasNext()) {
      size += writeDictEntry(os, iter.next());
    }
    return size;
  }
  
  private static int writeDictEntry(OutputStream os, DictEntry entry) throws IOException {
    // output operands
    int size = 0;
    for (Number n : entry.operands) {
      if (n instanceof Float || n instanceof Double) {
        os.write(0x1e); // TODO(stuartg): constant
        size++;
        // floating point
        String s = n.toString();
        int nybble = 0;
        int lastNybble = 0;
        boolean partByte = false;
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          switch (c) {
            case '0':
              nybble = 0x0;
              break;
            case '1':
              nybble = 0x1;              
              break;
            case '2':
              nybble = 0x2;
              break;
            case '3':
              nybble = 0x3;
              break;
            case '4':
              nybble = 0x4;
              break;
            case '5':
              nybble = 0x5;
              break;
            case '6':
              nybble = 0x6;
              break;
            case '7':
              nybble = 0x7;
              break;
            case '8':
              nybble = 0x8;
              break;
            case '9':
              nybble = 0x9;
              break;
            case '.':
              nybble = 0xa;
              break;
            case 'E':
              if ((i + 1) < s.length() && s.charAt(i+1) == '-') {
                i++;
                nybble = 0xb;
              } else {
                nybble = 0xc;
              }
              break;
            case '-':
              nybble = 0xe;
              break;
          }
          if (partByte) {
            os.write(lastNybble << 4 | nybble);
            size++;
            partByte = false;
          } else {
            partByte = true;
            lastNybble = nybble;
          }
        }
        if (partByte == true) {
          os.write(lastNybble << 4 | 0xf);
          size++;
        } else {
          os.write(0xff);
          size++;
        }
      } else {
        // integer
        int i = n.intValue();
        if (i >=-107 && i <= 107 && !entry.forceFullSizeIntegerEncoding) {
          // tiny int
          os.write(i + 139);
          size++;
        } else if (i >= 108 && i <= 1131 && !entry.forceFullSizeIntegerEncoding) {
          // small +ve int
          i -= 108;
          int b0 = (i / 256) + 247;
          int b1 = i % 256;
          os.write(b0);
          os.write(b1);
          size += 2;
        } else if (i <= -108 && i >= -1131 && !entry.forceFullSizeIntegerEncoding) {
          // small -ve int
          i += 108;
          i *= -1;
          int b0 = (i / 256) + 251;
          int b1 = i % 256;
          os.write(b0);
          os.write(b1);
          size += 2;
        } else if (i >= -32768 && i <= 32767 && !entry.forceFullSizeIntegerEncoding) {
          // -32768 to +32767
          os.write(28);
          os.write(i >> 8);
          os.write(i);
          size += 3;
        } else {
          // -2^31 to +2^31 - 1
          os.write(29);
          os.write(i >> 24);
          os.write(i >> 16);
          os.write(i >> 8);
          os.write(i);
          size += 5;
        }
      }
    }
    
    // output operator
    if ((entry.operator() & OPERATOR_ESCAPE) == OPERATOR_ESCAPE) {
      os.write(OPERATOR_ESCAPE_BYTE);
      size++;
    }
    os.write(entry.operator() & OPERATOR_MASK);
    size++;
    
    return size;
  }
}
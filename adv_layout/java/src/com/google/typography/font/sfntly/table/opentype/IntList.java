// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.table.opentype.IntSet.IntIterator;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * @author dougfelt@google.com (Doug Felt)
 */
public class IntList {
  private int[] ints;
  private int start;
  private int length;
  
  public IntList(int[] ints) {
    this.ints = ints.clone();
    this.length = ints.length;
  }
  
  protected IntList(Builder b, int[] ints) {
    this.ints = ints;
    this.length = ints.length;
  }
  
  protected IntList(int[] ints, int start, int length) {
    this.ints = ints;
    this.start = start;
    this.length = length;
  }
  
  public int length() {
    return length;
  }
  
  public int get(int index) {
    assert(index >= 0 && index < length);
    return ints[start + index];
  }
  
  public IntList sublist(int start, int length) {
    assert(start >= 0 && length >= 0 && length <= this.length - start);
    return new IntList(ints, this.start + start, length);
  }
  
  public IntIterator iterator() {
    return new IntIterator() {
      int pos = start;
      int limit = start + length;

      @Override
      public boolean hasNext() {
        return pos < limit;
      }

      @Override
      public int next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return ints[pos++];
      }
    };
  }
  
  public static class Builder {
    private int[] ints;
    private int size;
    private int split;
    
    public Builder() {
      this(10);
    }
    
    public Builder(int initialCapacity) {
      this.ints = new int[initialCapacity];
    }
    
    public Builder(int[] values) {
      this.ints = values.clone();
      this.size = values.length;
    }
    
    public int length() {
      return size;
    }
    
    public Builder setLength(int length) {
      assert(length >= 0);
      if (length < size) {
        ensureCapacityAt(size, 0);
        size = length;
      } else {
        ensureCapacityAt(size, length - size);
        for (int i = size, e = i + length; i < e; ++i) {
          ints[i] = 0;
        }
      }
      return this;
    }
    
    public Builder clear() {
      ints = new int[10];
      size = split = 0;
      return this;
    }
    
    public int get(int index) {
      assert(index >= 0 && index < size);
      if (index >= split) {
        index += ints.length - size;
      }
      return ints[index];
    }
    
    public Builder set(int index, int value) {
      assert(index >= 0 && index < size);
      if (index >= split) {
        index += ints.length - size;
      }
      ints[index] = value;
      return this;
    }
    
    public Builder add(int value) {
      ensureCapacityAt(size, 1);
      ints[size++] = value;
      return this;
    }
    
    public Builder addAt(int index, int value) {
      assert(index >= 0 && index <= size);
      ensureCapacityAt(index, 1);
      ints[index] = value;
      size += 1;
      return this;
    }
    
    public Builder add(int... values) {
      ensureCapacityAt(size, values.length);
      for (int i = 0, j = size; i < values.length; ++i) {
        ints[j++] = values[i];
      }
      size += values.length;
      return this;
    }
    
    public Builder addAt(int index, int... values) {
      assert(index >= 0 && index <= size);
      ensureCapacityAt(index, values.length);
      for (int i = 0; i < values.length; ++i) {
        ints[index++] = values[i];
      }
      size += values.length;
      return this;
    }
    
    public Builder deleteAt(int index) {
      assert(index >= 0 && index < size);
      ensureCapacityAt(index, 0);
      size -= 1;
      return this;
    }
    
    public Builder deleteAt(int index, int count) {
      assert(index >= 0 && index < size);
      ensureCapacityAt(index, 0);
      size -= count;
      return this;
    }
    
    public IntList build() {
      ensureCapacityAt(size, 0);
      return new IntList(this, Arrays.copyOf(ints, size));
    }
    
    public void ensureCapacityAt(int index, int count) {
      assert(index >= 0 && index <= size && count >= 0);
      int gap = ints.length - size;
      int[] src = ints;
      if (count > gap) {
        ints = new int[ints.length + count]; // keep the same size gap
        for (int i = 0, e = Math.min(split, index); i < e; ++i) {
          ints[i] = src[i];
        }
        for (int i = gap + Math.max(split, index), e = size + gap, j = ints.length - (e - i); i < e;) {
          ints[j++] = src[i++];
        }
      }
      if (index < split) {
        for (int i = split, j = i + gap; i > index;) {
          ints[--j] = src[--i];
        }
      } else if (index > split) {
        for (int i = split, j = i + gap, e = index + gap; j < e;) {
          ints[i++] = src[j++];
        }
      }
      split = index;
    }
  }
}

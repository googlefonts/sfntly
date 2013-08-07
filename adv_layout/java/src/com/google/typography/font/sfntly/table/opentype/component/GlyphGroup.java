package com.google.typography.font.sfntly.table.opentype.component;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GlyphGroup extends BitSet implements Iterable<Integer> {
  private static final long serialVersionUID = 1L;

  public static final GlyphGroup ANY = new GlyphGroup();

  public GlyphGroup() {
    super();
  }

  public GlyphGroup(int glyph) {
    super.set(glyph);
  }

  public GlyphGroup(Collection<Integer> glyphs) {
    for (int glyph : glyphs) {
      super.set(glyph);
    }
  }

  public GlyphGroup(int[] glyphs) {
    for (int glyph : glyphs) {
      super.set(glyph);
    }
  }

  public void add(int glyph) {
    this.set(glyph);
  }

  public void addAll(Collection<Integer> glyphs) {
    for (int glyph : glyphs) {
      super.set(glyph);
    }
  }

  public void addAll(GlyphGroup other) {
    this.or(other);
  }

  public void copyTo(Collection<Integer> target) {
    List<Integer> list = new LinkedList<>();
    for ( int i = this.nextSetBit( 0 ); i >= 0; i = this.nextSetBit( i + 1 ) ) {
      target.add(i);
    }
  }

  public boolean contains(int glyph) {
    return this.get(glyph);
  }

  @Override
  public int size() {
    return cardinality();
  }

  @Override
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      int i = 0;
      @Override
      public boolean hasNext() {
        return nextSetBit(i) >= 0 ;
      }

      @Override
      public Integer next() {
        i = nextSetBit(i);
        return i++;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}

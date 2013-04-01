package com.google.typography.font.sfntly.table.opentype;

import java.util.NoSuchElementException;

public interface IntSet {
	boolean contains(int v);
	boolean containsAll(int lo, int hi);
	boolean containsAny(int lo, int hi);
	boolean containsAll(IntSet s);
	boolean containsAny(IntSet s);

	IntIterator iterator();
	IntRangeIterator rangeIterator();

	public interface Builder extends IntSet {
		Builder add(int v);
		Builder add(int lo, int hi);
		Builder add(IntSet s);

		Builder remove(int v);
		Builder remove(int lo, int hi);
		Builder remove(IntSet s);

		Builder keep(int v);
		Builder keep(int lo, int hi);
		Builder keep(IntSet s);

		IntSet build();
	}

	public interface IntIterator {
	    boolean hasNext();
	    int next();
	}

	public class IntRange {
	    public int lo;
	    public int hi;

	    public IntRange(int lo, int hi) {
	        this.lo = lo;
	        this.hi = hi;
	    }
	    public IntRange copy() {
	        return new IntRange(lo, hi);
	    }
	}

	public interface IntRangeIterator {
	    boolean hasNext();

	    /**
	     * The returned range is only good until the next call to next().
	     * @throws NoSuchElementException if there is no available range.
	     */
	    IntRange next();
	}
}

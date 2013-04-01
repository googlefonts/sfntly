package com.google.typography.font.sfntly.table.opentype;

import java.util.NoSuchElementException;

class RangeIntSet implements IntSet {
	// Even elements are range starts, and odd elements range limits.
	protected int[] ranges;

	RangeIntSet(int[] ranges) {
		this.ranges = ranges;
	}

	@Override
	public boolean contains(int v) {
		checkRange(v, v);
		return (indexOf(v) & 1) == 0;
	}

	@Override
	public boolean containsAll(int lo, int hi) {
		checkRange(lo, hi);
		int idx = indexOf(lo);
		return (idx & 1) == 0 && ranges[idx + 1] > hi;
	}

	@Override
	public boolean containsAny(int lo, int hi) {
		checkRange(lo, hi);
		int loidx = indexOf(lo);
		if ((loidx & 1) == 0) {
			return true;
		}
		int hiidx = indexOf(hi);
		if ((hiidx & 1) == 0) {
			return true;
		}
		return loidx < hiidx;
	}


	@Override
	public boolean containsAll(IntSet s) {
		if (s == null) {
			throw new NullPointerException();
		}
		IntRangeIterator iri = s.rangeIterator();
		while (iri.hasNext()) {
			IntRange r = iri.next();
			if (!containsAll(r.lo, r.hi)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean containsAny(IntSet s) {
		if (s == null) {
			throw new NullPointerException();
		}
		IntRangeIterator iri = s.rangeIterator();
		while (iri.hasNext()) {
			IntRange r = iri.next();
			if (containsAny(r.lo, r.hi)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IntIterator iterator() {
		return new Iterator();
	}

	@Override
	public IntRangeIterator rangeIterator() {
		return new RangeIterator();
	}

	private int indexOf(int v) {
		return indexOf(v, ranges, 0, ranges.length);
	}

	/**
	 * Return the position between the start and limit indices on or before v.  If there is
	 * no such range, return start - 1.  Returned values range from start - 1 to limit - 1.
	 */
	static int indexOf(int v, int[] ranges, int sInx, int lInx) {
		if (lInx == sInx || v < ranges[sInx]) {
			return sInx - 1;
		}

	    int lo = sInx;
        int hi = lInx - 1;
        int mid = 0;
        int t = 0;

        while (lo <= hi) {
            mid = (lo + hi) / 2;
            t = ranges[mid];
            if (t < v) {
                lo = mid + 1;
            } else if (t > v) {
                hi = mid - 1;
            } else {
               break;
            }
        }
        return t <= v ? mid : mid - 1;
	}

	static boolean testRange(int lo, int hi) {
		return hi >= lo &&
			lo != Integer.MIN_VALUE &&
			hi < Integer.MAX_VALUE - 1;
	}

	static void checkRange(int lo, int hi) {
		if (hi < lo) {
			throw new IllegalArgumentException("hi " + hi + " < low " + lo);
		}
		if (lo == Integer.MIN_VALUE) {
			throw new IllegalArgumentException("low value out of range");
		}
		if (hi >= Integer.MAX_VALUE - 1) {
			throw new IllegalArgumentException("high value out of range)");
		}
	}

	class Iterator implements IntIterator {
		IntRange r;
		IntRangeIterator iri;
		int v;

		private Iterator() {
			iri = rangeIterator();
			if (iri.hasNext()) {
				r = iri.next();
				v = r.lo;
			}
		}

		@Override
		public boolean hasNext() {
			return r != null;
		}

		@Override
		public int next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			int result = v;
			if (v == r.hi) {
				if (!iri.hasNext()) {
					r = null;
				} else {
					r = iri.next();
					v = r.lo;
				}
			} else {
				++v;
			}
			return result;
		}
	}

	private class RangeIterator implements IntRangeIterator {
		private IntRange r = new IntRange(0, 0);
		private int pos;

		private RangeIterator() {
		}

		@Override
		public boolean hasNext() {
			return pos < ranges.length;
		}

		@Override
		public IntRange next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			r.lo = ranges[pos];
			r.hi = ranges[pos + 1] - 1; // convert limit value to last value in range
			pos += 2;
			return r;
		}
	}
}

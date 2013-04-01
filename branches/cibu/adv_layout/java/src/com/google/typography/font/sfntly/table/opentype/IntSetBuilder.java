package com.google.typography.font.sfntly.table.opentype;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class IntSetBuilder implements IntSet.Builder {
  // Range data, the first element is Integer.MIN_VALUE, the last element is Integer.MAX_VALUE.
  // Neither value can be in the set.  Elements at even locations are out of the set.
  private int[] ranges;
  // The position of the gap.  This is usually after the last insertion or deletion.
  private int split;
  // The space remaining in ranges in ints (not pairs).  This is the gap after split.
  private int space;

  @Override
  public String toString() {
    if (space == ranges.length){
      return " ...";
    }
    StringBuilder sb = new StringBuilder();
    int lo = split > 0 ? ranges[0] : ranges[split + space];
    int hi = split + space < ranges.length ? ranges[ranges.length - 1] : ranges[split - 1];
    int maxlen = Math.max(Integer.toString(lo).length(), Integer.toString(hi).length());
    maxlen = Math.max(maxlen, Integer.toString(ranges.length).length());
    String format = " %" + maxlen + "d";
    for (int i = 0; i < split; ++i) {
      sb.append(String.format(format, i));
    }
    sb.append(' ');
    for (int i = 0; i < maxlen; ++i) {
      sb.append('.');
    }
    for (int i = split + space; i < ranges.length; ++i) {
      sb.append(String.format(format, i));
    }
    sb.append('\n');
    for (int i = 0; i < split; ++i) {
      sb.append(String.format(format, ranges[i]));
    }
    sb.append(' ');
    for (int i = 0; i < maxlen; ++i) {
      sb.append(' ');
    }
    for (int i = split + space; i < ranges.length; ++i) {
      sb.append(String.format(format, ranges[i]));
    }
    return sb.toString();
  }

  public IntSetBuilder() {
    init();
  }

  private void init() {
    ranges = new int[32];
    ranges[0] = Integer.MIN_VALUE;
    ranges[31] = Integer.MAX_VALUE;
    split = 1;
    space = 30;
  }

  public static boolean testRange(int lo, int hi) {
    return RangeIntSet.testRange(lo, hi);
  }

  public static void checkRange(int lo, int hi) {
    RangeIntSet.checkRange(lo, hi);
  }

  public static IntSetBuilder newBuilder() {
    return new IntSetBuilder();
  }

  @Override
  public IntSetBuilder add(int v) {
    return add(v, v);
  }

  @Override
  public IntSetBuilder remove(int v) {
    return remove(v, v);
  }

  @Override
  public IntSetBuilder keep(int v) {
    return keep(v, v);
  }

  @Override
  public IntSetBuilder add(int lo, int hi) {
    RangeIntSet.checkRange(lo, hi);
    return doAdd(lo, hi);
  }

  @Override
  public IntSetBuilder remove(int lo, int hi) {
    RangeIntSet.checkRange(lo, hi);
    return doRemove(lo, hi);
  }

  @Override
  public IntSetBuilder keep(int lo, int hi) {
    RangeIntSet.checkRange(lo, hi);
    return doKeep(lo, hi);
  }

  @Override
  public IntSetBuilder add(IntSet set) {
    if (set == null) {
      throw new NullPointerException();
    }
    IntRangeIterator rangeIter = set.rangeIterator();
    while (rangeIter.hasNext()) {
      IntRange r = rangeIter.next();
      doAdd(r.lo, r.hi);
    }
    return this;
  }

  @Override
  public IntSetBuilder remove(IntSet set) {
    if (set == null) {
      throw new NullPointerException();
    }
    IntRangeIterator rangeIter = set.rangeIterator();
    while (rangeIter.hasNext()) {
      IntRange r = rangeIter.next();
      doRemove(r.lo, r.hi);
    }
    return this;
  }

  @Override
  public IntSetBuilder keep(IntSet set) {
    if (set == null) {
      throw new NullPointerException();
    }
    IntRangeIterator rangeIter = set.rangeIterator();
    while (rangeIter.hasNext()) {
      IntRange r = rangeIter.next();
      doKeep(r.lo, r.hi);
    }
    return this;
  }

  public IntSetBuilder removeAll() {
    init();
    return this;
  }

  /**
   * Add the range start, limit.  Limit > start.  On exit, split if after the inserted range, if one was
   * inserted, otherwise split is left unchanged.
   */
  private IntSetBuilder doAdd(int start, int end) {
    return doAdd(start, end, true);
  }

  private IntSetBuilder doRemove(int start, int end) {
    return doAdd(start, end, false);
  }

  private IntSetBuilder doAdd(int start, int end, boolean add) {
    // We map both start and limit to an index in the array.  Each index and the following value indicate
    // a range, either 'in' or 'out' depending on whether the start index is odd or even.  We modify the
    // ranges we hit and delete any ranges in between.  Adding and removing operations are the same, we're adding
    // and/or replacing some target ranges.  If we're adding, our target is in ranges, else our target is
    // out ranges.
    //
    // If start == the start of a non-target range, we're actually extending the
    // previous target range, so we reset sInx to that range.  If the limit == the start
    // of a target range we're extending that range, but in this case the index is ok.
    //

    int limit = end + 1;
    int t = add ? 1 : 0;

    int sInx = indexOf(start);
    if ((sInx & 1) != t && start == ranges[sInx]) {
      sInx -= 1;
    }

    int lInx = indexOf(limit);

    // Easy case first.
    if (sInx == lInx) {
      // Either insert a new range since it doesn't overlap or abut existing ranges,
      // or do nothing since the entire range is already there.
      if ((sInx & 1) != t) {
        insertRangeAt(start, limit, sInx + 1);
      }
      return this;
    }

    // We're crossing a range.

    // If sInx is not a target range, we're extending the range whose start is at sInx + 1, and the
    // new start is start. Otherwise, we're operating on the range whose start is at sInx, and the
    // new start is its current start.
    //
    // We adjust sInx to point to the start of the target range to delete. Note that bumping
    // it up means sInx might be at split, but this is ok.
    if ((sInx & 1) != t) {
      sInx += 1; // start of ranges to delete
    } else {
      start = ranges[sInx];
    }

    // If lInx is not a target range, we're extending the range whose limit is at lInx, and the
    // new limit is limit.  Otherwise, we'e operating on the range whose limit is at lInx + 1,
    // and the new limit is its current limit.
    //
    // We adjust lInx to point to after the limit of the last target range to delete.
    // Note that bumping it up means lInx might be at split.
    //
    // There's a special case if we're adding and limit is MAX_VALUE.  Normally we'd merge the
    // range with the following range (bumping up by two), but in this case there's no following
    // range to merge with.  To ensure that we never encounter this situation, we disallow
    // MAX_VALUE-1 from being added to the set (along with MAX_VALUE and MIN_VALUE).
    if ((lInx & 1) != t) {
      lInx += 1; // limit of ranges to delete
    } else {
      lInx += 1;
      if (lInx == split) {
        lInx += space;
      }
      limit = ranges[lInx];
      lInx += 1;
    }

    deleteRanges(sInx, lInx);
    insertRangeAt(start, limit, split);
    // If we deleted up to ranges.limit, we added MAX_VALUE back as the limit, but before the gap.
    // We need to ensure the gap ends before this last value.
    if (split + space == ranges.length) {
      ensureSpaceAt(0, split - 1);
    }
    return this;
  }

  private IntSetBuilder doKeep(int lo, int hi) {
    doRemove(Integer.MIN_VALUE + 1, lo - 1);
    doRemove(hi+1, Integer.MAX_VALUE - 1);
    return this;
  }

  /**
   * Delete ranges from sInx up to lInx.  Both sInx and lInx are between
   * 0 and ranges.length.  Either or both can be at split, sInx might also be at
   * split + space.  sInx and lInx are either both even or both odd.
   *
   * Leaves split before the range (formerly) at lInx.  This means that we might still do real work even
   * when sInx == lInx.
   *
   * If sInx is odd, then split will be odd.
   */
  private void deleteRanges(int sInx, int lInx) {
    int newSpace = lInx - sInx;
    if (sInx < split) {
      if (lInx < split) {
        System.arraycopy(ranges, lInx, ranges, lInx + space, split - lInx);
      } else if (lInx > split) {
        // We span the gap, so don't count it in the new space.
        newSpace -= space;
      }
    } else {
      if (sInx > split + space) {
        System.arraycopy(ranges, split + space, ranges, split, sInx - (split + space));
        sInx -= space;
      } else if (sInx == split + space) {
        sInx -= space;
      }
    }
    split = sInx;
    space += newSpace;
  }

  /**
   * Inserts start and limit before the range at idx.  Split is adjusted to
   * this position, then the new range is written at split and split is bumped by 2.
   * Idx can be odd; if so split will be odd when this returns.
   */
  private void insertRangeAt(int start, int limit, int idx) {
    ensureSpaceAt(2, idx);
    ranges[split] = start;
    ranges[split+1] = limit;
    split += 2;
    space -= 2;
  }

  /**
   * Ensures there is at least requiredSpace before the range at idx, and positions
   * split before that range.  Note that even if requiredSpace == 0 this can still do
   * meaningful work, as it moves the gap.  Idx can be odd.
   *
   * idx >= 0 && <= split || >= split + space && <= ranges.length
   */
  private void ensureSpaceAt(int requiredSpace, int idx) {
    if (requiredSpace > space) {
      requiredSpace = (requiredSpace + 15) & ~0xf;
      int[] newRanges = Arrays.copyOf(ranges, ranges.length + requiredSpace);
      System.arraycopy(newRanges, split + space, newRanges, split + requiredSpace, ranges.length - (split + space));
      ranges = newRanges;
      space = requiredSpace;
    }

    if (idx != split) {
      if (idx < split) {
        System.arraycopy(ranges, idx, ranges, idx + space, split - idx);
        split = idx;
      } else {
        int len = idx - (split + space);
        if (len > 0) {
          System.arraycopy(ranges, split + space, ranges, split, len);
          split += len;
        }
      }
    }
  }

  @Override
  public IntSet build() {
    ensureSpaceAt(0, ranges.length - 1);
    int len = split - 1;
    int[] included = new int[len];
    System.arraycopy(ranges, 1, included, 0, len);
    return new RangeIntSet(included);
  }

  @Override
  public IntIterator iterator() {
    return new Iterator();
  }

  @Override
  public IntRangeIterator rangeIterator() {
    return new RangeIterator();
  }

  private class RangeIterator implements IntRangeIterator {
    private IntRange r;
    private SplitIterator iter;

    RangeIterator() {
      r = new IntRange(0, 0);
      if ((split & 1) == 0) {
        // keep ranges together
        ensureSpaceAt(0, split - 1);
      }
      if (split == 1) {
        iter = new SplitIterator(1 + space, ranges.length - 1);
      } else {
        iter = new SplitIterator(1, split);
      }
    }

    @Override
    public boolean hasNext() {
      if (iter.hasNext()) {
        return true;
      }
      if (iter.limit == split && split + space < ranges.length - 1) {
        iter = new SplitIterator(split + space, ranges.length - 1);
        return iter.hasNext();
      }
      return false;
    }

    @Override
    public IntRange next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return iter.next();
    }

    private class SplitIterator implements IntRangeIterator {
      private int pos;
      private int limit;

      private SplitIterator(int pos, int limit) {
        this.pos = pos;
        this.limit = limit;
      }
      @Override
      public boolean hasNext() {
        return pos < limit;
      }

      @Override
      public IntRange next() {
        if (!hasNext()) {
          return null;
        }
        r.lo = ranges[pos++];
        r.hi = ranges[pos++] - 1;
        return r;
      }
    }
  }

  private int indexOf(int v) {
    if (v < ranges[split + space]) {
      return RangeIntSet.indexOf(v, ranges, 0, split);
    }
    return RangeIntSet.indexOf(v, ranges, split + space, ranges.length);
  }

  @Override
  public boolean contains(int v) {
    RangeIntSet.checkRange(v, v);
    return (indexOf(v) & 0) == 1;
  }

  @Override
  public boolean containsAll(int lo, int hi) {
    RangeIntSet.checkRange(lo, hi);
    int idx = indexOf(lo);
    return (idx & 1) == 1 && ranges[idx + 1] > hi;
  }

  @Override
  public boolean containsAny(int lo, int hi) {
    RangeIntSet.checkRange(lo, hi);
    int loidx = indexOf(lo);
    if ((loidx & 1) == 1) {
      return true;
    }
    int hiidx = indexOf(hi);
    if ((hiidx & 1) == 1) {
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

  /**
   * Return a new IntSet with the given ranges.  Ranges must be pairs of start/end values, in order, where
   * end >= start, and start > previous end + 1.  All values must be greater than Integer.MIN_VALUE and
   * less than Integer.MAX_VALUE - 1.
   */
  public static IntSet newSetFromRanges(int[] ranges) {
    if ((ranges.length & 1) != 0) {
      throw new IllegalArgumentException("ranges must be pairs of start and end");
    }
    if (ranges.length == 0) {
      return new RangeIntSet(ranges);
    }

    int lastv = ranges[0];
    if (lastv == Integer.MIN_VALUE) {
      throw new IllegalArgumentException("first range start too low");
    }

    int[] newRanges = new int[ranges.length];
    newRanges[0] = lastv;
    for (int i = 1; i < ranges.length; ++i) {
      int v = ranges[i];
      if ((i & 1) == 1) {
        v += 1;
        if (v <= lastv) {
          throw new IllegalArgumentException("range end " + (v - 1) + "< range start " + lastv);
        }
      } else{
        if (v <= lastv) {
          throw new IllegalArgumentException("range start " + v + " <= previous range end " + (lastv - 1) + " + 1");
        }
      }
      newRanges[i] = lastv = v;
    }
    if (lastv >= Integer.MAX_VALUE - 1) {
      throw new IllegalArgumentException("last range limit too high");
    }

    return new RangeIntSet(newRanges);
  }

  /**
   * Return the number of included ranges.
   */
  public int rangeCount() {
    return (ranges.length - space - 2) / 2;
  }

  /**
   * Return the number of included values.
   */
  public int elementCount() {
    int count = 0;
    IntRangeIterator iter = rangeIterator();
    while (iter.hasNext()) {
      IntRange r = iter.next();
      count += r.hi - r.lo + 1;
    }
    return count;
  }
}

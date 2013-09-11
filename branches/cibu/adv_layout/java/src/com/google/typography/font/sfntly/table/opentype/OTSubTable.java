// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.SubTable;
import com.google.typography.font.sfntly.table.opentype.component.VisibleSubTable;

/**
 * Consolidates dataIsCanonical handling and building logic used by the OpenType tables.
 *
 * @author dougfelt@google.com (Doug Felt)
 */
public abstract class OTSubTable extends SubTable {
  protected final boolean dataIsCanonical;

  protected OTSubTable(ReadableFontData data, boolean dataIsCanonical) {
    super(data);
    this.dataIsCanonical = dataIsCanonical;
  }

  public abstract Builder<? extends OTSubTable> builder();

  /**
   * Returns a slice based on an index into an offset array.  If the data is
   * canonical, this returns a slice between two offsets, or between the last
   * offset and limit (typically the end of the data).  If the data is not
   * canonical, this returns a 'one-sided' slice starting at offset.
   *
   * @param base the start of the offset array
   * @param index the 0-based index into the array
   * @param count the number of items in the array
   * @param stride the number of bytes between one array element and the next
   * @param limit the limit of the last element in the array.
   * @return the slice.
   */
  ReadableFontData sliceData(int base, int index, int count, int stride, int limit) {
    int pos = base + index * stride;
    int start = data.readUShort(pos);
    if (!dataIsCanonical) {
      return data.slice(start);
    }
    if (index + 1 < count) {
      limit = data.readUShort(pos + stride);
    }
    return data.slice(start, limit - start);
  }

  /**
   * Overload of sliceData(int, int, int, int, int) that uses 2 for the stride
   * (offsets are contiguous in the index) and the length of the data as the limit
   * (these are the last objects in the data).
   */
  ReadableFontData sliceData(int base, int index, int count) {
    return sliceData(base, index, count, 2, data.length());
  }

  protected abstract static class Builder<T extends OTSubTable> extends VisibleSubTable.Builder<T> {
    protected final boolean dataIsCanonical;
    private int serializedLength;

    protected Builder(ReadableFontData data, boolean dataIsCanonical) {
      super(data);
      this.dataIsCanonical = data == null ? false : dataIsCanonical;
      if (this.dataIsCanonical) {
        serializedLength = data.length();
      } else if (data == null || data.length() == 0) {
        serializedLength = 0;
      } else {
        serializedLength = -1;
        setModelChanged();
      }
    }

    protected Builder() {
      this(null, false);
    }

    protected Builder(T table) {
      this(table.readFontData(), table.dataIsCanonical);
    }

    /**
     * Returns true if the data has not been edited, and thus there is no model
     * to serialize.
     */
    abstract boolean unedited();

    /**
     * Create a model for editing from the data.
     * This causes <code>unedited()</code> to return false;
     * @param data
     */
    abstract void readModel(ReadableFontData data, boolean dataIsCanonical);

    /**
     * Computes the serialized length of the data.  This computes its own serialized
     * length and calls subDataSizeToSerialize on any subTables to get their length.
     */
    abstract int computeSerializedLength();

    /**
     * Writes the model, which is exactly as long as computeSerializedLength.
     */
    abstract void writeModel(WritableFontData data);

    /**
     * The first time this is called, it calls initFromData to build the model and
     * calls setModelChanged to indicate that the model will need to be written out.
     * It also resets the serializedLength so it will be recomputed.
     */
    void prepareToEdit() {
      if (unedited()) {
        readModel(internalReadData(), dataIsCanonical);
        serializedLength = -1;
        setModelChanged();
      }
    }

    /**
     * This is called first by FontDataTable when the tables is to be built.  It calls
     * subDataSizeToSerialize and returns true if the result > 0.  This ensures that
     * no object is built if there is no data.
     */
    @Override
    protected final boolean subReadyToSerialize() {
      return subDataSizeToSerialize() > 0;
    }

    /**
     * This is called twice, once by subReadyToSerialize, and then again by FontDataTable.
     * The first call will compute the serializedLength, and the second just returns the
     * cached value.  The actual work is done in computeSerializedLength, which might
     * call this recursively on its sub-tables.
     */
    @Override
    public
    final int subDataSizeToSerialize() {
      if (serializedLength == -1) {
        if (unedited()) {
            prepareToEdit();
        }
        serializedLength = computeSerializedLength();
      }
      return serializedLength;
    }

    /**
     * This is called after subDataSizeToSerialize, newData has a length equal to
     * that returned by that call. If the data has not been edited, it is because
     * the data is canonical and can be copied straight to newData.  Otherwise,
     * serializeEditState is called to do the actual serialization.  When this
     * finishes, resets serializedLength.
     */
    @Override
    public
    final int subSerialize(WritableFontData newData) {
      if (unedited()) {
        internalReadData().copyTo(newData);
      } else {
        writeModel(newData);
      }
      int length = serializedLength;
      serializedLength = -1;
      return length;
    }
  }
}

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;

import java.util.Iterator;
import java.util.NoSuchElementException;

class ScriptRecord {
  static final int SCRIPT_TAG_OFFSET = 0;
  static final int SCRIPT_TABLE_OFFSET = 4;
  static final int RECORD_SIZE = 6;

  private int scriptTag;
  private int script;

  private ScriptRecord(ReadableFontData data) {
    this.scriptTag = data.readULongAsInt(SCRIPT_TAG_OFFSET);
    this.script = data.readUShort(SCRIPT_TABLE_OFFSET);
  }

  int scriptTag() {
    return scriptTag;
  }

  int script() {
    return script;
  }

  private ScriptRecord(int scriptTag, int script) {
    this.scriptTag = scriptTag;
    this.script = script;
  }

  public static ScriptRecord createFromOffset(
      ReadableFontData data, int offset) {
    return new ScriptRecord(data.slice(offset));
  }

  public static ScriptRecord create(int scriptTag, int script) {
    return new ScriptRecord(scriptTag, script);
  }

  public static ScriptRecord createNthFromOffset(
      ReadableFontData data, int base, int index) {
    return createFromOffset(data, getBaseFor(base, index));
  }

  public static ScriptRecord createFromScriptTag(
      ReadableFontData data, int base, int count, int scriptTag) {
    Iterator<ScriptRecord> scriptRecordIterator =
        ScriptRecord.getScriptRecordIterator(data, base, count);

    while(scriptRecordIterator.hasNext()) {
      ScriptRecord scriptRecord = scriptRecordIterator.next();
      if (scriptRecord.scriptTag() == scriptTag) {
        return scriptRecord;
      }
    }
    return null;
  }

  public static int getBaseFor(int base, int index) {
    return base + index * RECORD_SIZE;
  }

  public static Iterator<ScriptRecord> getScriptRecordIterator(
      final ReadableFontData data,
      final int base,
      final int count) {

    return new Iterator<ScriptRecord>() {
      int p = base;
      int e = base + count * RECORD_SIZE;

      @Override
      public boolean hasNext() {
        return p < e;
      }

      @Override
      public ScriptRecord next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        ScriptRecord scriptRecord = ScriptRecord.createFromOffset(data, p);
        p += RECORD_SIZE;
        return scriptRecord;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static int write(WritableFontData newData, int base, ScriptRecord scriptRecord) {
    newData.writeULong(base + SCRIPT_TAG_OFFSET, scriptRecord.scriptTag());
    newData.writeULong(base + SCRIPT_TABLE_OFFSET, scriptRecord.script());
    return RECORD_SIZE;
  }
}

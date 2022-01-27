/*
 * Copyright 2010 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.typography.font.sfntly.table.opentype;

import com.google.typography.font.sfntly.data.ReadableFontData;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.sfntly.table.Header;
import com.google.typography.font.sfntly.table.Table;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The 'GSUB' table handles glyph substitutions such as ligatures and context-dependent display
 * variants, such as Arabic.
 *
 * @see "ISO/IEC 14496-22:2015, section 6.3.4"
 */
public class GSubTable extends Table {
  private final GsubCommonTable gsub;
  private final AtomicReference<ScriptListTable> scriptListTable = new AtomicReference<>();
  private final AtomicReference<FeatureListTable> featureListTable = new AtomicReference<>();
  private final AtomicReference<LookupListTable> lookupListTable = new AtomicReference<>();

  private GSubTable(Header header, ReadableFontData data, boolean dataIsCanonical) {
    super(header, data);
    gsub = new GsubCommonTable(data, dataIsCanonical);
  }

  public ScriptListTable scriptList() {
    if (scriptListTable.get() == null) {
      scriptListTable.compareAndSet(null, gsub.createScriptList());
    }
    return scriptListTable.get();
  }

  public FeatureListTable featureList() {
    if (featureListTable.get() == null) {
      featureListTable.compareAndSet(null, gsub.createFeatureList());
    }
    return featureListTable.get();
  }

  public LookupListTable lookupList() {
    if (lookupListTable.get() == null) {
      lookupListTable.compareAndSet(null, gsub.createLookupList());
    }
    return lookupListTable.get();
  }

  public static class Builder extends Table.Builder<GSubTable> {
    private final GsubCommonTable.Builder gsub;

    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    /** Builds a table from the data, using copy-on-write if necessary. */
    private Builder(Header header, ReadableFontData data) {
      super(header, data);
      gsub = new GsubCommonTable.Builder(data, false);
    }

    @Override
    protected int subSerialize(WritableFontData newData) {
      return gsub.subSerialize(newData);
    }

    @Override
    protected boolean subReadyToSerialize() {
      return gsub.subReadyToSerialize();
    }

    @Override
    protected int subDataSizeToSerialize() {
      return 0; // TODO(cibu): need to implement using gsub
    }

    @Override
    protected void subDataSet() {
      // TODO(cibu): need to implement using gsub
    }

    @Override
    protected GSubTable subBuildTable(ReadableFontData data) {
      return new GSubTable(header(), data, false);
    }
  }
}

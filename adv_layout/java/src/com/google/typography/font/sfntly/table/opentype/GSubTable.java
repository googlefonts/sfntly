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

import java.util.Set;


/**
 * A GSub table.
 */
public class GSubTable extends Table {
  private GsubCommonTable gsub;

  enum Offset {
    version(0),
    scriptList(4),
    featureList(6),
    lookupList(8);

    final int offset;

    private Offset(int offset) {
      this.offset = offset;
    }
  }

  /**
   * Constructor.
   *
   * @param header header for the table
   * @param data data for the table
   */
  private GSubTable(Header header, ReadableFontData data, boolean dataIsCanonical) {
    super(header, data);
    gsub = new GsubCommonTable(data,  dataIsCanonical);
  }

  /**
   * Get the table version.
   *
   * @return table version
   */
  public int version() {
    return this.data.readFixed(Offset.version.offset);
  }

  /**
   * Return information about the script tables in this GSUB table.
   *
   * @return the ScriptList
   */
  public ScriptListTable scriptList() {
    return gsub.createScriptList();
  }

  /**
   * Return information about the feature tables in this GSUB table.
   *
   * @return the FeatureList
   */
  public FeatureList featureList() {
    return gsub.createFeatureList();
  }

  /**
   * Return information about the lookup tables in this GSUB table.
   *
   * @return the LookupList
   */
  public GsubLookupList lookupList() {
    return gsub.createLookupList();
  }

  /**
   * Given a script and language tag and a set of features, return the corresponding
   * lookups in the order specified by the font.  If the specification requires
   * features to be applied in a given order, they should be passed in separate calls.
   * Regardless of what is in the featureSet, required features for the LangSys will
   * always be included.
   *
   * @param stag the script tag
   * @param ltag the language tag
   * @param featureSet the features (set may be empty)
   * @return the lookups
   */
  public Iterable<GsubLookupTable> lookups(ScriptTag stag, LanguageTag ltag, Set<FeatureTag> featureSet) {
    return gsub.lookups(stag, ltag, featureSet);
  }

  /**
   * GSUB Table Builder.
   */
  public static class Builder extends Table.Builder<GSubTable> {
    private GsubCommonTable.Builder gsub;

    /**
     * Creates a new builder using the header information and data provided.
     *
     * @param header the header information
     * @param data the data holding the table
     * @return a new builder
     */
    public static Builder createBuilder(Header header, WritableFontData data) {
      return new Builder(header, data);
    }

    /**
     * Constructor. This constructor will try to maintain the data as readable
     * but if editing operations are attempted then a writable copy will be made
     * the readable data will be discarded.
     *
     * @param header the table header
     * @param data the readable data for the table
     */
    protected Builder(Header header, ReadableFontData data) {
      super(header, data);
      gsub = new GsubCommonTable.Builder(data, false);
    }

    public GsubCommonTable.Builder commonBuilder() {
      setModelChanged();
      return gsub;
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
      return gsub.subDataSizeToSerialize();
    }

    @Override
    protected void subDataSet() {
      gsub.subDataSet();
    }

    @Override
    protected GSubTable subBuildTable(ReadableFontData data) {
      return new GSubTable(this.header(), data, false);
    }
  }
}
